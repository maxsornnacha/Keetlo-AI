package com.keetlo.ai.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.keetlo.ai.model.File;
import com.keetlo.ai.model.Message;

@Service
public class MessageService {

    private final JdbcTemplate database;
    private final FileService fileService;

    public MessageService(JdbcTemplate database, FileService fileService) {
        this.database = database;
        this.fileService = fileService;
    }

     public String createMessage(String projectId, String userId, String role, String message) {
        Message projectChat = new Message();
        String projectChatId = projectChat.createProjectChatId();
        String sql = """
                INSERT INTO project_messages
                (project_message_id, project_id, user_id, role, message, created_at)
                 VALUES (?, ?, ?, ?, ?, NOW())
                 """;
        database.update(sql, projectChatId, projectId, userId, role, message);
        this.touchProjectUpdatedAtByProjectId(projectId);
        return projectChatId;
    }

        public void touchProjectUpdatedAtByProjectId(String projectId){
        String sql = "UPDATE projects SET updated_at = NOW() WHERE project_id = ?";
        database.update(sql, projectId);
    }

    public List<Message> getProjectMessagesByProjectId(String projectId) {
        String sql = "SELECT project_message_id, role, message, created_at FROM project_messages WHERE project_id = ? ORDER BY created_at ASC";
        List<Message> projectMessages = database.query(sql, new Object[] { projectId }, (resultRow, _) -> {
            Message projectMessage = new Message();
            projectMessage.setProjectMessageId(resultRow.getString("project_message_id"));
            projectMessage.setRole(resultRow.getString("role"));
            projectMessage.setMessage(resultRow.getString("message"));
            projectMessage.setCreatedAt(resultRow.getTimestamp("created_at").toLocalDateTime());
            List<File> files = fileService.getProjectFilesByMessageId(projectMessage.getProjectMessageId());
            projectMessage.setFiles(files);
            return projectMessage;
        });
        return projectMessages;
    }

    public Boolean updateRequestToken(String userId) {
        try {
            String sql = """
                    UPDATE user_subscription_plans
                    SET left_requests = (left_requests - 1)
                    WHERE left_requests > 0 AND user_id = ?
                    """;
            database.update(sql, userId);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean checkRequestToken(String userId) {
        String sql = """
                SELECT left_requests
                FROM user_subscription_plans
                WHERE user_id = ?
                """;
        try {
            Integer leftRequests = database.queryForObject(sql, Integer.class, userId);
                  // null = unlimited, >0 = still available
            return leftRequests == null || leftRequests > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
