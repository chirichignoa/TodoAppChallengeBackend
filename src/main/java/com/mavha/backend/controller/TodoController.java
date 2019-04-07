package com.mavha.backend.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mavha.backend.model.Todo;
import com.mavha.backend.service.TodoService;
import com.mavha.backend.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class TodoController {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .create();
    private TodoService todoService;

    @Autowired
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // POST
    @RequestMapping(value = "/todo",
            method = RequestMethod.POST,
            produces = "application/json;",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    ResponseEntity<String> saveTodo(@RequestParam("description") String description, @RequestParam("image") MultipartFile image) {
        Todo todo = new Todo(description);
        Response response = this.todoService.saveTodo(todo, image);
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.gson.toJson(response));
    }

    // GET by filters

    // PATCH update state
}
