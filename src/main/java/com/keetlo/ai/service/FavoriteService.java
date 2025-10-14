package com.keetlo.ai.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.keetlo.ai.model.PublicProject;
import com.keetlo.ai.model.User;

@Service
public class FavoriteService {

    private final JdbcTemplate database;

    public FavoriteService(JdbcTemplate database) {
        this.database = database;
    }
    
    public Map<String, Object> getFavoriteProjects(
    String userId,
    String q,
    String sort,
    int page,
    int pageSize
) {
    int p = Math.max(1, page);
    int size = Math.min(Math.max(1, pageSize), 100);
    int offset = (p - 1) * size;

    // Normalize sort key
    String sortKey = (sort == null || sort.isBlank()) ? "newestFav" : sort.toLowerCase();
    String orderBy;
    switch (sortKey) {
        case "popular":
            orderBy = "projects.favorite_count DESC, projects.public_at IS NULL ASC, projects.public_at DESC";
            break;
        case "name":
            orderBy = "LOWER(projects.title) ASC, projects.public_at IS NULL ASC, projects.public_at DESC";
            break;
        case "newest":
            orderBy = "projects.public_at IS NULL ASC, projects.public_at DESC";
        default:
            orderBy =  "favorite_projects.created_at DESC";
    }

    // WHERE + JOIN
    StringBuilder where = new StringBuilder(" WHERE projects.is_public = 1 AND favorite_projects.user_id = ?");
    List<Object> params = new ArrayList<>();

    String join =
        " FROM favorite_projects " +
        " JOIN projects ON favorite_projects.project_id = projects.project_id " +
        " JOIN generated_pages ON projects.index_page = generated_pages.generated_page_id " +
        " JOIN users ON users.user_id = projects.user_id ";
    params.add(userId);

    if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase() + "%";
        where.append(" AND (")
             .append("LOWER(projects.title) LIKE ? OR LOWER(projects.description) LIKE ? ")
             .append("OR LOWER(users.firstname) LIKE ? OR LOWER(users.lastname) LIKE ?")
             .append(") ");
        params.add(like);
        params.add(like);
        params.add(like);
        params.add(like);
    }

    // SELECT
    String select =
        "SELECT " +
        " projects.project_id, " +
        " projects.title, " +
        " projects.description, " +
        " projects.index_page, " +
        " generated_pages.html_content AS main_html_content, " +
        " COALESCE(projects.favorite_count, 0) AS favorite_count, " +
        " COALESCE(projects.remix_count, 0) AS remix_count, " +
        " projects.public_at, " +
        " users.firstname, " +
        " users.lastname, " +
        " users.avatar_url " +
        join +
        where +
        " ORDER BY " + orderBy +
        " LIMIT ? OFFSET ?";

    List<Object> selectParams = new ArrayList<>(params);
    selectParams.add(size);
    selectParams.add(offset);

    List<PublicProject> items = database.query(select, selectParams.toArray(), (resultRow, _) -> {
        PublicProject publicProject = new PublicProject();
        publicProject.setProjectId(resultRow.getString("project_id"));
        publicProject.setTitle(resultRow.getString("title"));
        publicProject.setDescription(resultRow.getString("description"));
        publicProject.setIndexPage(resultRow.getString("index_page"));
        publicProject.setFavoriteCount(resultRow.getInt("favorite_count"));
        publicProject.setRemixCount(resultRow.getInt("remix_count"));
        Timestamp pubAt = resultRow.getTimestamp("public_at");
        publicProject.setPublicAt(pubAt != null ? pubAt.toLocalDateTime() : null);

        User user = new User();
        user.setFirstname(resultRow.getString("firstname"));
        user.setLastname(resultRow.getString("lastname"));
        user.setAvatarUrl(resultRow.getString("avatar_url"));
        publicProject.setUser(user);

        publicProject.setLink("/public/projects/preview/" + publicProject.getProjectId());
        publicProject.setMainHtmlContent(resultRow.getString("main_html_content"));
        publicProject.setIsFavorite(true);

        return publicProject;
    });

    // COUNT
    String countSql = "SELECT COUNT(*) " + join + where;
    long total = database.queryForObject(countSql, params.toArray(), Long.class);

    Map<String, Object> response = new HashMap<>();
    response.put("items", items);
    response.put("total", total);
    response.put("currentPage", p);
    response.put("pageSize", size);
    return response;
}

}
