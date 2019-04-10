package com.mavha.backend.service;

import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import com.mavha.backend.util.Response;
import org.springframework.web.multipart.MultipartFile;

public interface TodoService {
    Response getTodos(Long id, String description, Status status);
    Response saveTodo(Todo todo, MultipartFile image);
    Response updateStatus(Long id, Status status);
}
