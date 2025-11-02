package com.keetlo.ai.service;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.keetlo.ai.model.Page;

@Service
public class PageService {

    private final JdbcTemplate database;

    public PageService(JdbcTemplate database) {
        this.database = database;
    }

    public String createPage(String projectMessageId, String label, String path, String htmlContent,
            Integer height, Integer width, Integer top, Integer left) {
        try {
            Page page = new Page();
            String newGeneratedPageId = page.createGeneratedPageId();
            String sql = """
                    INSERT INTO generated_pages
                    (generated_page_id, project_message_id, label, path, html_content, height, width, `top`, `left`)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """;
            database.update(sql, newGeneratedPageId, projectMessageId, label, path, htmlContent, height, width, top,
                    left);

            this.touchProjectUpdatedAtByProjectMessageId(projectMessageId);
            return newGeneratedPageId;
        } catch (Exception error) {
            System.out.println(error.getMessage());
            return null;
        }
    }

    private void touchProjectUpdatedAtByProjectMessageId(String projectMessageId) {
        String sql = """
                    UPDATE projects
                    JOIN project_messages ON project_messages.project_id = projects.project_id
                    SET projects.updated_at = NOW()
                    WHERE project_messages.project_message_id = ?
                """;
        database.update(sql, projectMessageId);
    }

    public List<Page> fetchingPages(String projectId, String userId) {
        try {
            String sql = """
                    SELECT generated_pages.generated_page_id, generated_pages.project_message_id, generated_pages.label, generated_pages.path, generated_pages.html_content,
                    generated_pages.height, generated_pages.width, generated_pages.`top`, generated_pages.`left`
                    FROM generated_pages
                    JOIN project_messages ON
                        generated_pages.project_message_id = project_messages.project_message_id
                    JOIN projects ON
                        project_messages.project_id = projects.project_id
                    WHERE projects.project_id = ? AND projects.user_id = ?
                    ORDER BY generated_pages.created_at ASC
                    """;
            return database.query(sql, new BeanPropertyRowMapper<>(Page.class), projectId, userId);
        } catch (Exception error) {
            System.out.println("Error fetching pages: " + error.getMessage());
            return List.of();
        }
    }

    public boolean updatePageByGeneratedPageId(
            String generatedPageId,
            Integer height,
            Integer width,
            Integer top,
            Integer left) {
        try {
            String sql = """
                        UPDATE generated_pages
                        SET height = ?, width = ?, `top` = ?, `left` = ?, updated_at = NOW()
                        WHERE generated_page_id = ?
                    """;

            int rowsAffected = database.update(sql, height, width, top, left, generatedPageId);

            return rowsAffected > 0;
        } catch (Exception error) {
            System.out.println("Error updating page: " + error.getMessage());
            return false;
        }
    }

     public boolean deletePageByGeneratedPageId(String generatedPageId, String userId) {
        try {
                String sql = """
                    DELETE generated_pages
                    FROM generated_pages
                    JOIN project_messages ON 
                        generated_pages.project_message_id = project_messages.project_message_id
                    JOIN projects ON 
                        projects.project_id = project_messages.project_id
                    WHERE generated_pages.generated_page_id = ?
                    AND projects.user_id = ?
                """;
            int rowsAffected = database.update(sql, generatedPageId, userId);
            return rowsAffected > 0;
        } catch (Exception error) {
            System.out.println("Error deleting page: " + error.getMessage());
            return false;
        }
    }

}
