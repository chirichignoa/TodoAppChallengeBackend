package com.mavha.backend.service;

import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import com.mavha.backend.repository.TodoRepository;
import com.mavha.backend.util.FileStorageProperties;
import com.mavha.backend.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
    public Response getTodos(Todo todo) {
        List<Todo> todos = this.todoRepository.findAll(Example.of(todo));
        return new Response(null,
                todos,
                HttpStatus.OK);
    }

    @Override
    public Resource getImage(Long id) {
        Todo todo = this.todoRepository.findById(id);
        return this.loadFileAsResource(todo.getImage());
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

    private Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.rootLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                //throw new MyFileNotFoundException("File not found " + fileName);
                return null;
            }
        } catch (MalformedURLException ex) {
            // throw new MyFileNotFoundException("File not found " + fileName, ex);
            return null;
        }
    }

    @Override
    @Transactional
    public Response updateStatus(Long id, Status status) {
        try {
            this.todoRepository.updateStatus(id, status);
            return new Response(null, id, HttpStatus.OK);
        } catch (Exception e) {
            return new Response("Error al actualizar el ToDo", null, HttpStatus.BAD_REQUEST);
        }
    }
}
