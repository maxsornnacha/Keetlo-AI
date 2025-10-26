package com.keetlo.ai.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.keetlo.ai.model.FavoriteProject;
import com.keetlo.ai.model.Message;
import com.keetlo.ai.model.Page;
import com.keetlo.ai.model.Project;
import com.keetlo.ai.model.ProjectTag;
import com.keetlo.ai.model.PublicProject;

@Service
public class PublicProjectService {
    private final JdbcTemplate database;

    public PublicProjectService(JdbcTemplate database) {
        this.database = database;
    }

    public Map<String, Object> getPublicProjects(
            String q, // search text (optional)
            String sort, // newest | popular | copyCount | name
            String type,
            String tagsCsv, // comma-separated tags (AND semantics)
            int page,
            int pageSize,
            String userId // optional, for is_favorite
    ) {
        int p = Math.max(1, page);
        int size = Math.max(1, pageSize);
        int offset = (p - 1) * size;

        // ---- sort mapping (MySQL-friendly; emulate NULLS LAST via IS NULL, col) ----
        String sortKey = (sort == null || sort.isBlank()) ? "newest" : sort.trim();
        if ("copyCount".equalsIgnoreCase(sortKey))
            sortKey = "remixCount";

        String orderBy = switch (sortKey) {
            case "popular" ->
                "projects.favorite_count IS NULL, projects.favorite_count DESC, " +
                        "projects.public_at IS NULL, projects.public_at DESC";
            case "remixCount" ->
                "projects.remix_count IS NULL, projects.remix_count DESC, " +
                        "projects.public_at IS NULL, projects.public_at DESC";
            case "name" ->
                "projects.title IS NULL, LOWER(projects.title) ASC, " +
                        "projects.public_at IS NULL, projects.public_at DESC";
            default ->
                "projects.public_at IS NULL, projects.public_at DESC"; // newest
        };



        // ---- parse tags (AND semantics) ----
        List<String> tags = new ArrayList<>();
        if (tagsCsv != null && !tagsCsv.isBlank()) {
            for (String t : tagsCsv.split(",")) {
                String s = t.trim();
                if (!s.isEmpty())
                    tags.add(s);
            }
        }

        // ---- WHERE (public only + optional q on title/desc/user name) ----
        StringBuilder where = new StringBuilder(" WHERE projects.is_public = 1 ");
        List<Object> whereParams = new ArrayList<>();

        if(type != null && !type.equals("") && !type.toLowerCase().equals("all") && !type.toLowerCase().equals("others")){
            String like = "%" + type.trim().toLowerCase() + "%";
            where.append("""
                 AND (LOWER(projects.type) LIKE ?)
            """);
            whereParams.add(like);
        } else if (type != null && type.toLowerCase().equals("others")){
               where.append("""
                 AND (LOWER(projects.type) != ? 
                 AND LOWER(projects.type) != ? 
                 AND LOWER(projects.type) != ? 
                 AND LOWER(projects.type) != ?
                 AND LOWER(projects.type) != ?
                 AND LOWER(projects.type) != ?
                 )
            """);
            whereParams.add("Website".toLowerCase());
            whereParams.add("Project".toLowerCase());
            whereParams.add("Natural".toLowerCase());
            whereParams.add("Internal Tools".toLowerCase());
            whereParams.add("Travel".toLowerCase());
            whereParams.add("Information".toLowerCase());
        }

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            where.append("""
                        AND (
                            LOWER(projects.title) LIKE ? OR LOWER(projects.description) LIKE ?
                            OR LOWER(users.firstname) LIKE ? OR LOWER(users.lastname) LIKE ?
                        )
                    """);
            whereParams.add(like);
            whereParams.add(like);
            whereParams.add(like);
            whereParams.add(like);
        }

        // ---- optional user-specific is_favorite expression ----
        String isFavoriteExpr = "NULL";
        boolean checkFavorite = (userId != null && !userId.isBlank());
        if (checkFavorite) {
            isFavoriteExpr = """
                        CASE WHEN EXISTS (
                            SELECT 1 FROM favorite_projects
                            WHERE favorite_projects.project_id = projects.project_id
                              AND favorite_projects.user_id = ?
                        ) THEN 1 ELSE NULL END
                    """;
        }

        // ---- optional tag join + HAVING for AND semantics ----
        String joinTags = "";
        String having = "";
        String inPlaceholders = "";
        if (!tags.isEmpty()) {
            // ? placeholders for IN (...)
            StringJoiner sj = new StringJoiner(", ");
            for (int i = 0; i < tags.size(); i++)
                sj.add("?");
            inPlaceholders = sj.toString();

            joinTags = " JOIN project_tags ON project_tags.project_id = projects.project_id ";
            having = " HAVING COUNT(DISTINCT CASE WHEN project_tags.tag IN (" + inPlaceholders
                    + ") THEN project_tags.tag END) = " + tags.size() + " ";
        }

        // ---- SELECT page items ----
        String selectSql = "SELECT\n" +
                "  projects.project_id,\n" +
                "  projects.title,\n" +
                "  projects.description,\n" +
                "  projects.index_page,\n" +
                "  projects.type,\n" +
                "  COALESCE(projects.favorite_count, 0) AS favorite_count,\n" +
                "  COALESCE(projects.remix_count, 0)    AS remix_count,\n" +
                "  projects.public_at,\n" +
                "  users.firstname,\n" +
                "  users.lastname,\n" +
                "  users.avatar_url,\n" +
                "  " + isFavoriteExpr + " AS is_favorite\n" +
                "FROM projects\n" +
                "JOIN users ON users.user_id = projects.user_id\n" +
                joinTags +
                where.toString() +
                // group by needed if we joined project_tags + used HAVING
                "GROUP BY projects.project_id, users.firstname, users.lastname, users.avatar_url\n" +
                having +
                "ORDER BY " + orderBy + "\n" +
                "LIMIT ? OFFSET ?";

        List<Object> selectParams = new ArrayList<>();
        if (checkFavorite)
            selectParams.add(userId); // for is_favorite EXISTS
        selectParams.addAll(whereParams);
        if (!tags.isEmpty())
            selectParams.addAll(tags); // for HAVING IN (...)
        selectParams.add(size);
        selectParams.add(offset);

        List<PublicProject> items = database.query(selectSql, selectParams.toArray(), (rs, _) -> {
            PublicProject pp = new PublicProject();
            pp.setProjectId(rs.getString("project_id"));
            pp.setTitle(rs.getString("title"));
            pp.setDescription(rs.getString("description"));
            pp.setIndexPage(rs.getString("index_page"));
            pp.setType(rs.getString("type"));
            pp.setIsFavorite(rs.getBoolean("is_favorite"));

            // enrich
            String mainHtml = getProjectMainHtmlContentByProjectId(pp.getProjectId());
            pp.setMainHtmlContent(mainHtml);
            pp.setLink("/public/projects/preview/" + pp.getProjectId());

            // counts
            // (They are SELECTed as favorite_count/remix_count via COALESCE)
            try {
                // If your PublicProject has int fields:
                var favoriteCountField = PublicProject.class.getDeclaredField("favoriteCount");
                favoriteCountField.setAccessible(true);
                favoriteCountField.set(pp, rs.getInt("favorite_count"));

                var remixCountField = PublicProject.class.getDeclaredField("remixCount");
                remixCountField.setAccessible(true);
                remixCountField.set(pp, rs.getInt("remix_count"));
            } catch (Exception ignore) {
            }

            // publicAt
            var ts = rs.getTimestamp("public_at");
            pp.setPublicAt(ts != null ? ts.toLocalDateTime() : null);

            // user (firstname/lastname/avatarUrl)
            com.keetlo.ai.model.User user = new com.keetlo.ai.model.User();
            user.setFirstname(rs.getString("firstname"));
            user.setLastname(rs.getString("lastname"));
            user.setAvatarUrl(rs.getString("avatar_url"));
            // If your PublicProject holds a nested user object:
            try {
                var userField = PublicProject.class.getDeclaredField("user");
                userField.setAccessible(true);
                userField.set(pp, user);
            } catch (Exception ignore) {
            }

            // tags for this project
            List<String> dataTags = getTagsByProjectId(pp.getProjectId());
            pp.setTags(dataTags);

            // is_favorite (optional)
            Object fav = rs.getObject("is_favorite");
            if (fav != null) {
                try {
                    var favField = PublicProject.class.getDeclaredField("isFavorite");
                    favField.setAccessible(true);
                    // store as boolean or 1 depending on your model; here boolean true
                    favField.set(pp, true);
                } catch (Exception ignore) {
                }
            }

            return pp;
        });

        // ---- TOTAL count (same filters) ----
        long total;
        List<Object> countParams = new ArrayList<>(whereParams);

        if (tags.isEmpty()) {
            String countSql = "SELECT COUNT(*)\n" +
                    "FROM projects\n" +
                    "JOIN users ON users.user_id = projects.user_id\n" +
                    where;
            total = database.queryForObject(countSql, countParams.toArray(), Long.class);
        } else {
            // Count distinct projects that have ALL requested tags
            String countSql = "SELECT COUNT(*) FROM (\n" +
                    "  SELECT projects.project_id\n" +
                    "  FROM projects\n" +
                    "  JOIN users ON users.user_id = projects.user_id\n" +
                    "  JOIN project_tags ON project_tags.project_id = projects.project_id\n" +
                    where +
                    "    AND project_tags.tag IN (" + inPlaceholders + ")\n" +
                    "  GROUP BY projects.project_id\n" +
                    "  HAVING COUNT(DISTINCT project_tags.tag) = ?\n" +
                    ") AS t";

            List<Object> countArgs = new ArrayList<>(countParams);
            countArgs.addAll(tags);
            countArgs.add(tags.size());
            total = database.queryForObject(countSql, countArgs.toArray(), Long.class);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("total", total);
        response.put("currentPage", p);
        response.put("pageSize", size);
        return response;
    }

    public List<String> getAllPublicTags() {
        String sql = """
                  SELECT DISTINCT project_tags.tag
                  FROM project_tags
                  JOIN projects ON projects.project_id = project_tags.project_id
                  WHERE projects.is_public = 1
                  ORDER BY project_tags.tag ASC
                """;
        return database.query(sql, (rs, _) -> rs.getString(1));
    }

    public List<String> getAllPublicTypes() {
        String sql = """
                  SELECT DISTINCT projects.type
                  FROM projects
                  WHERE projects.is_public = 1
                  ORDER BY projects.created_at ASC
                """;
        return database.query(sql, (rs, _) -> rs.getString(1));
    }

    public void setFavorite(String userId, String projectId, Boolean favorite) {
        if (favorite) {
            FavoriteProject favoriteProject = new FavoriteProject();
            String newFavoriteProjectId = favoriteProject.createFavoriteProjectId();
            String upsert = """
                      INSERT INTO favorite_projects (favorite_project_id, user_id, project_id, created_at)
                      VALUES (?, ?, ?, NOW())
                    """;
            String updateFavoriteCount = """
                      UPDATE projects SET favorite_count = favorite_count+1 WHERE project_id = ?
                    """;
            database.update(upsert, newFavoriteProjectId, userId, projectId);
            database.update(updateFavoriteCount, projectId);
        } else {
            String delete = """
                      DELETE FROM favorite_projects
                      WHERE user_id = ? AND project_id = ?
                    """;
            String updateFavoriteCount = """
                     UPDATE projects
                    SET favorite_count = GREATEST(favorite_count - 1, 0)
                    WHERE project_id = ?;
                    """;
            database.update(delete, userId, projectId);
            database.update(updateFavoriteCount, projectId);
        }
    }

    public String getProjectMainHtmlContentByProjectId(String projectId) {
        String sql = """
                    SELECT generated_pages.html_content
                    FROM project_messages
                    JOIN generated_pages
                    ON generated_pages.project_message_id = project_messages.project_message_id
                    WHERE project_messages.project_id = ?
                    ORDER BY
                        CASE WHEN generated_pages.generated_page_id =
                            (SELECT index_page FROM projects WHERE project_id = ?)
                            THEN 0 ELSE 1 END,
                        generated_pages.created_at ASC
                    LIMIT 1
                """;

        List<String> results = database.query(
                sql,
                new Object[] { projectId, projectId },
                (rs, _) -> rs.getString("html_content"));

        return results.isEmpty() ? "" : results.get(0);
    }

    public List<String> getTagsByProjectId(String projectId) {
        String sql = "SELECT tag FROM project_tags WHERE project_id = ?";
        List<String> projectTags = database.query(sql, new Object[] { projectId },
                (resultRow, _) -> resultRow.getString("tag"));
        return projectTags;
    }

    public PublicProject getProjectByProjectId(String projectId) {
        String sql = "SELECT project_id, title, description, type, index_page, created_at, public_at FROM projects WHERE project_id = ? AND is_public = 1";
        PublicProject project = database.queryForObject(sql, new Object[] { projectId }, ((resultRow, _) -> {
            PublicProject p = new PublicProject();
            p.setProjectId(resultRow.getString("project_id"));
            p.setTitle(resultRow.getString("title"));
            p.setDescription(resultRow.getString("description"));
            p.setType(resultRow.getString("type"));
            p.setIndexPage(resultRow.getString("index_page"));
            p.setPublicAt(resultRow.getTimestamp("public_at").toLocalDateTime());
            String html = getProjectMainHtmlContentByProjectId(p.getProjectId());
            p.setMainHtmlContent(html);
            List<String> projectTags = this.getTagsByProjectId(projectId);
            p.setTags(projectTags);
            List<Page> pages = this.fetchingPages(projectId);
            p.setPages(pages);
            return p;
        }));
        return project;
    }

    public List<Page> fetchingPages(String projectId) {
        try {
            String sql = """
                    SELECT generated_pages.generated_page_id, generated_pages.project_message_id, generated_pages.label, generated_pages.path, generated_pages.html_content,
                    generated_pages.height, generated_pages.width, generated_pages.`top`, generated_pages.`left`
                    FROM generated_pages
                    JOIN project_messages ON
                        generated_pages.project_message_id = project_messages.project_message_id
                    JOIN projects ON
                        project_messages.project_id = projects.project_id
                    WHERE projects.project_id = ? AND projects.is_public = 1
                    ORDER BY generated_pages.created_at DESC
                    """;
            return database.query(sql, new BeanPropertyRowMapper<>(Page.class), projectId);
        } catch (Exception error) {
            System.out.println("Error fetching pages: " + error.getMessage());
            return List.of();
        }
    }

    public String remixProject(String sourceProjectId, String requesterUserId) {

        Project sourceProject = database.queryForObject("""
                SELECT project_id, user_id, title, description, type, index_page, is_public
                FROM projects WHERE project_id = ?
                """, (resultRow, _) -> {
            Project p = new Project();
            p.setProjectId(resultRow.getString("project_id"));
            p.setUserId(resultRow.getString("user_id"));
            p.setTitle(resultRow.getString("title"));
            p.setDescription(resultRow.getString("description"));
            p.setType(resultRow.getString("type"));
            p.setIndexPage(resultRow.getString("index_page"));
            return p;
        }, sourceProjectId);

        if (sourceProject == null) {
            return null;
        }

        Project project = new Project();
        final String newProjectId = project.createProjectId();
        final String newTitle = (sourceProject.getTitle() == null || sourceProject.getTitle().isBlank())
                ? "New project" + " (Remix) " + project.createProjectId()
                : sourceProject.getTitle() + " (Remix) " + project.createProjectId();
        final String description = sourceProject.getDescription();
        final String type = sourceProject.getType();

        // 1) Insert new project (counters reset, not public)
        database.update("""
                    INSERT INTO projects
                        (project_id, user_id, title, description, type,
                         favorite_count, remix_count, index_page, is_public,
                         created_at, updated_at, public_at)
                    VALUES
                        (?, ?, ?, ?, ?, 0, 0, NULL, 0, NOW(), NOW(), NULL)
                """, newProjectId, requesterUserId, newTitle, description, type);

        // 2) Copy project_tags
        final List<ProjectTag> sourceTags = database.query("""
                    SELECT tag FROM project_tags WHERE project_id = ?
                """, (resultRow, _) -> {
            ProjectTag pt = new ProjectTag();
            pt.setTag(resultRow.getString("tag"));
            return pt;
        }, sourceProjectId);

        if (!sourceTags.isEmpty()) {
            for (ProjectTag tag : sourceTags) {
                database.update("""
                            INSERT INTO project_tags (project_tag_id, project_id, tag) VALUES (?, ?, ?)
                        """, tag.createProjectTagId(), newProjectId, tag.getTag());
            }
        }

        // 3) Load source messages (ordered) and copy them
        final List<Message> sourceMessages = database.query("""
                    SELECT project_messages.project_message_id,
                        project_messages.`role` AS role_name,
                        project_messages.message,
                        project_messages.created_at
                    FROM project_messages
                    WHERE project_messages.project_id = ?
                    ORDER BY project_messages.created_at ASC
                """, (resultRow, _) -> {
            Message m = new Message();
            m.setProjectMessageId(resultRow.getString("project_message_id"));
            m.setRole(resultRow.getString("role_name"));
            m.setMessage(resultRow.getString("message"));
            m.setCreatedAt(resultRow.getTimestamp("created_at").toLocalDateTime());
            return m;
        }, sourceProjectId);

        // Map old message id -> new message id
        final var messageIdMap = new java.util.HashMap<String, String>(sourceMessages.size());
        for (Message m : sourceMessages) {
            Message message = new Message();
            final String newMessageId = message.createProjectChatId();
            messageIdMap.put(m.getProjectMessageId(), newMessageId);
            database.update("""
                        INSERT INTO project_messages (project_message_id, project_id, user_id, role, message, created_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                    """, newMessageId, newProjectId, requesterUserId, m.getRole(), m.getMessage(), m.getCreatedAt());
        }

        // 4) Copy generated_pages linked to those messages; keep mapping oldGenId ->
        // newGenId
        final var pageIdMap = new java.util.HashMap<String, String>();
        final var newPages = new java.util.ArrayList<Page>();

        // Pull all pages of source (join to limit to project)
        final List<Page> sourcePages = database.query("""
                    SELECT
                        generated_pages.generated_page_id,
                        generated_pages.project_message_id,
                        generated_pages.label,
                        generated_pages.path,
                        generated_pages.html_content,
                        generated_pages.height,
                        generated_pages.width,
                        generated_pages.`top`,
                        generated_pages.`left`,
                        generated_pages.created_at
                    FROM generated_pages
                    JOIN project_messages  ON project_messages.project_message_id = generated_pages.project_message_id
                    WHERE project_messages.project_id = ?
                    ORDER BY generated_pages.created_at ASC, generated_pages.generated_page_id ASC
                """, (resultRow, _) -> {
            Page page = new Page();
            page.setGeneratedPageId(resultRow.getString("generated_page_id"));
            page.setProjectMessageId(resultRow.getString("project_message_id"));
            page.setLabel(resultRow.getString("label"));
            page.setPath(resultRow.getString("path"));
            page.setHtmlContent(resultRow.getString("html_content"));
            page.setHeight(resultRow.getInt("height"));
            page.setWidth(resultRow.getInt("width"));
            page.setTop(resultRow.getInt("top"));
            page.setLeft(resultRow.getInt("left"));
            return page;
        }, sourceProjectId);

        for (Page p : sourcePages) {
            final String oldMessageId = p.getProjectMessageId();
            final String newMessageId = messageIdMap.get(oldMessageId);
            if (newMessageId == null) {
                continue;
            }
            final String newGeneratedPageId = p.createGeneratedPageId();
            pageIdMap.put(p.getGeneratedPageId(), newGeneratedPageId);

            // insert cloned page
            database.update("""
                        INSERT INTO generated_pages
                            (generated_page_id, project_message_id, label, path, html_content,
                             height, width, `top`, `left`, created_at)
                        VALUES
                            (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                    """,
                    newGeneratedPageId, newMessageId, p.getLabel(), p.getPath(), p.getHtmlContent(),
                    p.getHeight(), p.getWidth(), p.getTop(), p.getLeft());

            // keep a DTO copy for computing new index & returning (if needed elsewhere)
            Page np = new Page();
            np.setGeneratedPageId(newGeneratedPageId);
            np.setProjectMessageId(newMessageId);
            np.setLabel(p.getLabel());
            np.setPath(p.getPath());
            np.setHtmlContent(p.getHtmlContent());
            np.setHeight(p.getHeight());
            np.setWidth(p.getWidth());
            np.setTop(p.getTop());
            np.setLeft(p.getLeft());
            newPages.add(np);
        }

        // 6) Insert remixed_projects on get log
        database.update("""
                    INSERT remixed_projects (remixed_project_id, user_id, source_project_id, new_remix_project_id)
                    VALUES (?, ?, ?, ?)
                """, UUID.randomUUID().toString(), requesterUserId, sourceProjectId, newProjectId);

        // 6) Increment remix_count on source
        database.update("""
                    UPDATE projects
                    SET remix_count = COALESCE(remix_count, 0) + 1, updated_at = NOW()
                    WHERE project_id = ?
                """, sourceProjectId);

        // 7) Return new project id
        return newProjectId;
    }

}
