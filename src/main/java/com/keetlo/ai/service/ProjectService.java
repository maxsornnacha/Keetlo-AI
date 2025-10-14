package com.keetlo.ai.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.keetlo.ai.model.Project;
import com.keetlo.ai.model.Message;
import com.keetlo.ai.model.ProjectTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {

    private final JdbcTemplate database;
    private final MessageService projectMessageService;

    public ProjectService(JdbcTemplate database, MessageService projectMessageService) {
        this.database = database;
        this.projectMessageService = projectMessageService;
    }

    // Create a new project/room
    public Boolean createProject(String projectId, String userId) {
        String sql = """
                INSERT INTO projects (project_id, user_id, created_at, updated_at)
                 VALUES (?, ?, NOW(), NOW())
                 """;
        database.update(sql, projectId, userId);
        return true;
    }

    public Boolean updateProjectAiGenerate(String projectId, String userId, String title, String description, String type, List<String> tags) {
        String sql = """
                UPDATE projects SET title=?, description=?, type=?, updated_at=NOW()
                 WHERE project_id=? AND user_id=?
                 """;
        database.update(sql, title, description, type, projectId, userId);
        if(tags.isEmpty()){
            return true;
        }
        for (String tag : tags) {
            this.createProjectTag(projectId, tag);
        }
        return true;
    }

    public Boolean createProjectTag(String projectId, String tag) {
        ProjectTag projectTag = new ProjectTag();
        String projectTagId = projectTag.createProjectTagId();
        String sql = """
                INSERT INTO project_tags (project_tag_id, project_id, tag, created_at)
                 VALUES (?, ?, ?, NOW())
                 """;
        database.update(sql, projectTagId, projectId, tag);
        return true;
    }

public Map<String, Object> getProjectsByUserIdPaginated(
        String userId, int page, int pageSize,
        String search, String startDate, String endDate
) {
    int offset = (page - 1) * pageSize;

    StringBuilder sql = new StringBuilder("""
        SELECT
            projects.project_id,
            projects.title,
            projects.description,
            projects.type,
            projects.is_public,
            projects.index_page,
            projects.updated_at
        FROM projects
        WHERE projects.user_id = ?
    """);

    List<Object> params = new ArrayList<>();
    params.add(userId);

    // Search filter
    if (search != null && !search.isEmpty()) {
        sql.append(" AND (LOWER(projects.title) LIKE ? OR LOWER(projects.description) LIKE ?) ");
        params.add("%" + search.toLowerCase() + "%");
        params.add("%" + search.toLowerCase() + "%");
    }

    // Date filter
    if (startDate != null && !startDate.isEmpty()) {
        sql.append(" AND projects.updated_at >= ? ");
        params.add(java.sql.Date.valueOf(startDate));
    }
    if (endDate != null && !endDate.isEmpty()) {
        sql.append(" AND projects.updated_at <= ? ");
        params.add(java.sql.Date.valueOf(endDate));
    }

    sql.append(" ORDER BY projects.updated_at DESC LIMIT ? OFFSET ? ");
    params.add(pageSize);
    params.add(offset);

    // Query projects
    List<Project> projects = database.query(sql.toString(), params.toArray(), (rs, _) -> {
        Project p = new Project();
        p.setProjectId(rs.getString("project_id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setType(rs.getString("type"));
        p.setIsPublic(rs.getInt("is_public"));
        p.setIndexPage(rs.getString("index_page"));
        p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        String html = getProjectMainHtmlContentByProjectId(p.getProjectId());
        p.setMainHtmlContent(html);
        return p;
    });

    // Total count for pagination
    StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM projects WHERE projects.user_id = ? ");
    List<Object> countParams = new ArrayList<>();
    countParams.add(userId);

    if (search != null && !search.isEmpty()) {
        countSql.append(" AND (LOWER(projects.title) LIKE ? OR LOWER(projects.description) LIKE ?) ");
        countParams.add("%" + search.toLowerCase() + "%");
        countParams.add("%" + search.toLowerCase() + "%");
    }

    if (startDate != null && !startDate.isEmpty()) {
        countSql.append(" AND projects.updated_at >= ? ");
        countParams.add(java.sql.Date.valueOf(startDate));
    }
    if (endDate != null && !endDate.isEmpty()) {
        countSql.append(" AND projects.updated_at <= ? ");
        countParams.add(java.sql.Date.valueOf(endDate));
    }

    int totalProjects = database.queryForObject(countSql.toString(), countParams.toArray(), Integer.class);
    int totalPages = (int) Math.ceil((double) totalProjects / pageSize);

    Map<String, Object> response = new HashMap<>();
    response.put("projects", projects);
    response.put("totalPages", totalPages);
    response.put("currentPage", page);

    return response;
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
            new Object[]{projectId, projectId}, 
            (rs, _) -> rs.getString("html_content")
        );

        return results.isEmpty() ? "" : results.get(0);
    }

    public Project getProjectByProjectId(String projectId, String userId) {
    String sql = "SELECT project_id, title, description, type, is_public, index_page, created_at FROM projects WHERE project_id = ? AND user_id = ?";
    Project project =  database.queryForObject(sql, new Object[]{projectId, userId}, ((resultRow,_)->{
        Project p = new Project();
        p.setProjectId(resultRow.getString("project_id"));
        p.setTitle(resultRow.getString("title"));
        p.setDescription(resultRow.getString("description"));
        p.setType(resultRow.getString("type"));
        p.setIsPublic(resultRow.getInt("is_public"));
        p.setIndexPage(resultRow.getString("index_page"));
        p.setCreatedAt(resultRow.getTimestamp("created_at").toLocalDateTime());
        String html = getProjectMainHtmlContentByProjectId(p.getProjectId());
        p.setMainHtmlContent(html);
        List<String> projectTags = this.getTagsByProjectId(projectId);
        p.setTags(projectTags);
        List<Message> messages =  projectMessageService.getProjectMessagesByProjectId(projectId);
        p.setMessages(messages);
        return p;
    }));
    return project;
    }

    public List<String> getTagsByProjectId(String projectId) {
        String sql = "SELECT tag FROM project_tags WHERE project_id = ?";
        List<String> projectTags = database.query(sql, new Object[]{projectId}, (resultRow, _) -> 
            resultRow.getString("tag")
        );
        return projectTags;
    }

    public Boolean updatePublicByProjectIdAndUserId(Integer isPublic, String indexPage, String projectId, String userId) {
        String sql = "UPDATE projects SET is_public=?, index_page=?, public_at=NOW() WHERE project_id = ? AND user_id = ?";
        database.update(sql, isPublic == 1 ? true : false, indexPage, projectId, userId);
        return true;
    }

     public Boolean updateProjectDetails(String projectId, String userId, String title, String description, String type, List<String> tags) {
        try {
            String sql = "UPDATE projects SET title=?, description=?, type=?, updated_at=NOW() WHERE project_id=? AND user_id=?";
            int rowsAffected = database.update(sql, title, description, type, projectId, userId);

            if (rowsAffected > 0) {
                // Optional: clear existing tags for this project first
                String deleteTagsSql = "DELETE FROM project_tags WHERE project_id=?";
                database.update(deleteTagsSql, projectId);

                // Insert new tags
                for (String tag : tags) {
                    this.createProjectTag(projectId, tag);
                }
            }

            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println("Error updating project: " + e.getMessage());
            return false;
        }
    }

    public Boolean deleteProjectByProjectId(String projectId, String userId) {
        try {
            String checkSql = "SELECT COUNT(*) FROM projects WHERE project_id = ? AND user_id = ?";
            Integer count = database.queryForObject(checkSql, new Object[]{projectId, userId}, Integer.class);
            if (count == null || count == 0) {
                return false;
            }

            String updateProjectCancelPublicSql = "UPDATE projects SET index_page=?, is_public=? WHERE project_id = ? AND user_id = ?";
             database.update(updateProjectCancelPublicSql, null, 0, projectId, userId);

            String deletePagesSql = """
                DELETE FROM generated_pages
                WHERE project_message_id IN (
                    SELECT project_message_id FROM project_messages WHERE project_id = ?
                )
            """;
            database.update(deletePagesSql, projectId);

            String deleteMessagesSql = "DELETE FROM project_messages WHERE project_id = ?";
            database.update(deleteMessagesSql, projectId);

            String deleteTagsSql = "DELETE FROM project_tags WHERE project_id = ?";
            database.update(deleteTagsSql, projectId);

            String deleteFavSql = "DELETE FROM favorite_projects WHERE project_id = ?";
            database.update(deleteFavSql, projectId);

            String deleteRemixSql = "DELETE FROM remixed_projects WHERE source_project_id = ? OR new_remix_project_id = ?";
            database.update(deleteRemixSql, projectId, projectId);

            String deleteProjectSql = "DELETE FROM projects WHERE project_id = ? AND user_id = ?";
            int rowsAffected = database.update(deleteProjectSql, projectId, userId);

            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println("Error deleting project: " + e.getMessage());
            return false;
        }
    }
}
