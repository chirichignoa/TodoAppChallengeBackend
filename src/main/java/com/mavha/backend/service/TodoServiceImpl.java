package com.mavha.backend.service;

import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import com.mavha.backend.repository.TodoRepository;
import com.mavha.backend.util.FileStorageProperties;
import com.mavha.backend.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class TodoServiceImpl implements TodoService {

    private TodoRepository todoRepository;
    private final Path rootLocation;

    @Autowired
    public TodoServiceImpl(TodoRepository todoRepository, FileStorageProperties properties) {
        this.todoRepository = todoRepository;
        this.rootLocation = Paths.get(properties.getLocation())
                .toAbsolutePath().normalize();
        if(Files.notExists(this.rootLocation)) {
            try {
                Files.createDirectory(this.rootLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Response getAllTodos() {
        return null;
    }

    @Override
    public Response getTodosById(Long id) {
        return null;
    }

    @Override
    public Response getTodosByDescription(String description) {
        return null;
    }

    @Override
    public Response getTodosByState(Status status) {
        return null;
    }

    @Override
    @Transactional
    public Response saveTodo(Todo todo, MultipartFile image) {
        try {
            String path = saveImage(image);
            if(path != null) {
                todo.setImage(path);
                this.todoRepository.save(todo);
                return new Response(null, todo.getId(), HttpStatus.CREATED);
            }
        } catch (Exception e) {
            return new Response(e.getMessage(), null, HttpStatus.BAD_REQUEST);
        }
        // return response
        return new Response("Error saving image", null, HttpStatus.BAD_REQUEST);
    }

    private String saveImage(MultipartFile image) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(image.getOriginalFilename());
        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                return null;
            }
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.rootLocation.resolve(fileName);
            Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            try {
                throw new Exception("Failed to store file " + image, ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
