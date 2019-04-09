package com.mavha.backend.service;

import com.mavha.backend.exception.FileNotFound;
import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import com.mavha.backend.repository.SearchCriteria;
import com.mavha.backend.repository.TodoRepository;
import com.mavha.backend.repository.TodoSpecification;
import com.mavha.backend.util.FileStorageProperties;
import com.mavha.backend.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TodoServiceImpl implements TodoService {

    private TodoRepository todoRepository;
    private final Path rootLocation;

    @Autowired
    public TodoServiceImpl(TodoRepository todoRepository, FileStorageProperties properties) {
        this.todoRepository = todoRepository;
        this.rootLocation = Paths.get(properties.getLocation()).normalize();
        if (Files.notExists(this.rootLocation)) {
            try {
                Files.createDirectory(this.rootLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public Response getTodos(Todo todo) {
//        List<Todo> todos = this.todoRepository.findAll(Example.of(todo));
//        return new Response(null,
//                todos,
//                HttpStatus.OK);
//    }

    @Override
    public Response getTodos(Long id, String description, Status status) {
        List<SearchCriteria> params = new ArrayList<>();
        if(id != null) {
            params.add(new SearchCriteria("id",":", id));
        }
        if(description != null) {
            params.add(new SearchCriteria("description",":", description));
        }
        if(description != null) {
            params.add(new SearchCriteria("description",":", description));
        }
        if(status != null) {
            params.add(new SearchCriteria("status", ":", status));
        }
        Specification <Todo> spec = getSpecs(params);
        List<Todo> todos = this.todoRepository.findAll(spec);
        return new Response(null,
                todos,
                HttpStatus.OK);
    }

    private Specification<Todo> getSpecs(List<SearchCriteria> params) {
        if (params.size() == 0) {
            return null;
        }
        List<Specification> specs = params.stream()
                .map(TodoSpecification::new)
                .collect(Collectors.toList());

        Specification result = specs.get(0);

        for (int i = 1; i < params.size(); i++) {
            result = Specification.where(result).and(specs.get(i));
        }
        return result;
    }

    @Override
    public Resource getImage(Long id) {
        Todo todo = this.todoRepository.findById(id);
        try {
            Path filePath = this.rootLocation.resolve(todo.getImage()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileNotFound("Error at saving image.");
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFound("Error at saving image.");
        }
    }

    @Override
    @Transactional
    public Response saveTodo(Todo todo, MultipartFile image) {
        String path;
        try {
            path = saveImage(image);
            if(path != null) {
                todo.setImage(path);
                this.todoRepository.save(todo);
                return new Response(null, todo.getId(), HttpStatus.CREATED);
            }
        } catch (FileNotFound e) {
            return new Response(e.getMessage(), null, HttpStatus.BAD_REQUEST);
        }
        // return response
        return new Response("Error saving todo.", null, HttpStatus.BAD_REQUEST);
    }

    public String saveImage(MultipartFile image) {
        // Normalize file name
        String fileName = UUID.randomUUID().toString();
        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                return null;
            }
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.rootLocation.resolve(fileName);
            Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return this.rootLocation.toString() + "/" + fileName;
        } catch (IOException ex) {
            try {
                throw new FileNotFound("Error saving image.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Response updateStatus(Long id, Status status) {
        try {
            this.todoRepository.updateStatus(id, status);
            return new Response(null, id, HttpStatus.OK);
        } catch (Exception e) {
            return new Response("Error updating Todo", null, HttpStatus.BAD_REQUEST);
        }
    }
}
