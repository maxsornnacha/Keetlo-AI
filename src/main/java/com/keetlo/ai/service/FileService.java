package com.keetlo.ai.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.keetlo.ai.model.File;
import com.keetlo.ai.model.Message;

@Service
public class FileService {

    private final JdbcTemplate database;

    public FileService(JdbcTemplate database) {
        this.database = database;
    }
    
     public String createFile(String projectMessageId, String fileName, String fileType, Integer fileSize, String fileUrl) {
        File projectFile = new File();
        String projectChatId = projectFile.createProjectFileId();
        String sql = """
                INSERT INTO project_files
                (project_file_id, project_message_id, file_name, file_type, file_size, file_url)
                 VALUES (?, ?, ?, ?, ?, ?)
                 """;
        database.update(sql, projectChatId, projectMessageId, fileName, fileType, fileSize, fileUrl);
        return projectChatId;
    }

    public List<File> getProjectFilesByMessageId(String messageId) {
        String sql = "SELECT project_file_id, file_name, file_type, file_size, file_url, created_at FROM project_files WHERE project_message_id = ? ORDER BY created_at ASC";
        List<File> projectFiles = database.query(sql, new Object[] { messageId }, (resultRow, _) -> {
            File projectFile= new File();
            projectFile.setProjectFileId(resultRow.getString("project_file_id"));
            projectFile.setFileName(resultRow.getString("file_name"));
            projectFile.setFileType(resultRow.getString("file_type"));
            projectFile.setFileSize(resultRow.getInt("file_size"));
            projectFile.setFileUrl(resultRow.getString("file_url"));
            projectFile.setCreatedAt(resultRow.getTimestamp("created_at").toLocalDateTime());
            return projectFile;
        });
        return projectFiles;
    }
}
