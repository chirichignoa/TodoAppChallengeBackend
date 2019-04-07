package com.mavha.backend.service;

import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import com.mavha.backend.util.Response;
import org.springframework.web.multipart.MultipartFile;

public interface TodoService {
    // get todos
    Response getAllTodos();
    // get todos by id
    Response getTodosById(Long id);
    // get todos by description
    Response getTodosByDescription(String description);
    // get todos by state
    Response getTodosByState(Status status);
    // post todos
    Response saveTodo(Todo todo, MultipartFile image);
}
