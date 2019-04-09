package com.mavha.backend.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mavha.backend.exception.FileNotFound;
import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import com.mavha.backend.service.TodoService;
import com.mavha.backend.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
    ResponseEntity<String> saveTodo(@RequestParam("title") String title,
                                    @RequestParam("description") String description,
                                    @RequestParam("image") MultipartFile image) {
        Todo todo = new Todo(title, description);
        Response response = this.todoService.saveTodo(todo, image);
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.gson.toJson(response));
    }

    // GET by filters
    @RequestMapping(value = "/todo",
                    method = RequestMethod.GET,
                    produces = "application/json;")
    public @ResponseBody ResponseEntity<String> getTodos(@RequestParam(value="id", required=false) Long id,
                                                 @RequestParam(value="description", required=false) String description,
                                                 @RequestParam(value="status", required=false) String status) {
//        Todo todo = new Todo();
//        if(id != null) {
//            todo.setId(id);
//        }
//        if(description != null) {
//            todo.setDescription(description);
//        }
//        if(status != null) {
//            todo.setStatus(Status.PENDING.toString().equals(status) ? Status.PENDING : Status.DONE);
//        }
//        Response response = this.todoService.getTodos(todo);
        Status statusTodo = null;
        if(status != null) {
            statusTodo = Status.PENDING.toString().equals(status) ? Status.PENDING : Status.DONE;
        }
        Response response = this.todoService.getTodos(id, description, statusTodo);
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.gson.toJson(response));
    }

    // GET IMAGE
    @RequestMapping(value = "/todo/{id}",
            method = RequestMethod.GET,
            produces = "application/json;")
    public @ResponseBody ResponseEntity<Resource> getImage(@PathVariable Long id, HttpServletRequest request) {
        try {
            Resource resource = this.todoService.getImage(id);
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                logger.info("Could not determine file type.");
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileNotFound e) {
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

    // PATCH update state
    // PATCH
    @RequestMapping(value = "/todo/{id}", method = RequestMethod.PATCH,
            produces = "application/json; charset=utf-8",
            consumes = "application/json; charset=utf-8")
    public @ResponseBody
    ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestBody Todo body) {
        Response response = this.todoService.updateStatus(id, body.getStatus());
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.gson.toJson(response));
    }
}
