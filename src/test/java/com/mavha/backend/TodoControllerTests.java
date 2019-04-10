package com.mavha.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mavha.backend.controller.TodoController;
import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import com.mavha.backend.service.TodoService;
import com.mavha.backend.util.Response;
import net.minidev.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTests {

    private MockMvc mockMvc;

    @Mock
    private TodoService todoService;
    @InjectMocks
    private TodoController todoController;
    private Gson gson;

    @Before
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(todoController)
                .build();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .serializeNulls()
                .create();
    }

    // POST
    @Test
    public void shouldCreateTodo() throws Exception {
        Todo todo = new Todo("Titulo", "Descripcion");
        todo.setId(223L);
        todo.setImage("/upload-dir/" + UUID.randomUUID().toString());
        Response correctResponse = new Response(null,
                todo,
                HttpStatus.CREATED);

        Mockito.when(todoService
                        .saveTodo(Mockito.any(Todo.class), Mockito.any(MultipartFile.class)))
                .thenReturn(correctResponse);
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                                                "application/x-www-form-urlencoded", new byte[10]);
        MockHttpServletResponse response =  mockMvc.perform(
                MockMvcRequestBuilders.multipart("/todo")
                    .file("image",image.getBytes())
                    .param("title", "Art. Limpieza")
                    .param("description", "Comprar articulos de limpieza"))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    public void shouldNotCreateTodoMissingImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                "application/x-www-form-urlencoded", new byte[10]);
        MockHttpServletResponse response =  mockMvc.perform(
                MockMvcRequestBuilders.multipart("/todo")
                        .param("title", "Art. Limpieza")
                        .param("description", "Comprar articulos de limpieza"))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateTodoMissingTitle() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                "application/x-www-form-urlencoded", new byte[10]);
        MockHttpServletResponse response =  mockMvc.perform(
                MockMvcRequestBuilders.multipart("/todo")
                        .file("image",image.getBytes())
                        .param("description", "Comprar articulos de limpieza"))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateTodoMissingDescription() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                "application/x-www-form-urlencoded", new byte[10]);
        MockHttpServletResponse response =  mockMvc.perform(
                MockMvcRequestBuilders.multipart("/todo")
                        .file("image",image.getBytes())
                        .param("title", "Art. Limpieza"))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    //GET
    @Test
    public void shouldReturnTodos() throws Exception {
        List<Todo> todos = new ArrayList<>();
        Todo todo = new Todo("Art. Limpieza", "Comprar articulos de limpieza");
        todo.setId(233L);
        todo.setImage("file-dir/image1.png");
        todos.add(todo);
        todo = new Todo("Doctor", "Sacar turno con pediatra");
        todo.setId(245L);
        todo.setImage("file-dir/image2.png");
        todos.add(todo);
        Mockito.when(todoService
                .getTodos(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Status.class)))
                .thenReturn(new Response(null, this.gson.toJson(todos), HttpStatus.OK));
        MockHttpServletResponse response = this.mockMvc.perform(get("/todo?id=25&description=asd&status=Pending"))
                                                        .andReturn()
                                                        .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    //PATCH
    @Test
    public void shouldUpdateTodo() throws Exception {
        Todo todo = new Todo("Titulo", "Descripcion");
        todo.setId(223L);
        todo.setImage("/upload-dir/" + UUID.randomUUID().toString());
        Response correctResponse = new Response(null,
                todo,
                HttpStatus.OK);
        Mockito.when(todoService
                .updateStatus(Mockito.any(Long.class), Mockito.any(Status.class)))
                .thenReturn(correctResponse);


        JSONObject reservation = new JSONObject();
        reservation.put("status", Status.DONE);

        MockHttpServletResponse response = this.mockMvc.perform(patch("/todo/" + todo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservation.toString()))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
