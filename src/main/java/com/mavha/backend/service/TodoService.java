package com.mavha.backend.service;

import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import com.mavha.backend.util.Response;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface TodoService {
    Response getTodos(Todo todo);
    Response saveTodo(Todo todo, MultipartFile image);
    Response updateStatus(Long id, Status status);
    Resource getImage(Long id);
}
