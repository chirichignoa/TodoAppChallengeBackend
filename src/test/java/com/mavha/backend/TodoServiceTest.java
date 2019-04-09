package com.mavha.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mavha.backend.exception.FileNotFound;
import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import com.mavha.backend.repository.SearchCriteria;
import com.mavha.backend.repository.TodoRepository;
import com.mavha.backend.repository.TodoSpecification;
import com.mavha.backend.service.TodoServiceImpl;
import com.mavha.backend.util.FileStorageProperties;
import com.mavha.backend.util.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TodoServiceTest {

    private MockMvc mockMvc;

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private FileStorageProperties fileStorageProperties;
    private TodoServiceImpl todoService;
    private Gson gson;

    @Before
    public void setup() {
        Mockito.when(fileStorageProperties.getLocation()).thenReturn("./upload-dir");

        JacksonTester.initFields(this, new ObjectMapper());
        todoService = new TodoServiceImpl(todoRepository, fileStorageProperties);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(todoService)
                .build();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .serializeNulls()
                .create();
    }

    @Test
    public void shouldSaveTodo() {
        Todo todo = new Todo("Art. Limpieza", "Comprar Articulos de limpieza");
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                "application/x-www-form-urlencoded", new byte[10]);
        // Mock repository's save method to assign an id
        Mockito.when(todoRepository.save(Mockito.any(Todo.class)))
                .thenAnswer((Answer<Todo>) invocation -> {
                    Todo todo2 = (Todo) invocation.getArguments()[0];
                    todo2.setId(1 + (long) (Math.random() * (10 - 1)));
                    todo2.setImage("./upload-dir/image1.png");
                    return todo2;
                });
        Response response = this.todoService.saveTodo(todo, image);
        assertThat(response.getContent().toString().length()).isGreaterThan(0);
        assertThat(response.getError()).isEqualTo(null);
    }

    @Test
    public void shouldNotSaveTodo() {
        Response correctResponse = new Response("Error saving image.", null, HttpStatus.BAD_REQUEST);
        Todo todo = new Todo("Art. Limpieza", "Comprar Articulos de limpieza");
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                "application/x-www-form-urlencoded", new byte[10]);

        TodoServiceImpl todoServiceMock = Mockito.spy(this.todoService);
        Mockito.doThrow(new FileNotFound("Error saving image."))
                .when(todoServiceMock)
                .saveImage(Mockito.any(MultipartFile.class));

        Response response = todoServiceMock.saveTodo(todo, image);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotSaveTodoInvalidName() {
        Response correctResponse = new Response("Error saving todo.", null, HttpStatus.BAD_REQUEST);
        Todo todo = new Todo("Art. Limpieza", "Comprar Articulos de limpieza");
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                "application/x-www-form-urlencoded", new byte[10]);

        TodoServiceImpl todoServiceMock = Mockito.spy(this.todoService);
        Mockito.doReturn(null)
                .when(todoServiceMock)
                .saveImage(Mockito.any(MultipartFile.class));

        Response response = todoServiceMock.saveTodo(todo, image);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldGetTodos() {
        List<Todo> todos = new ArrayList<>();
        Todo todo = new Todo("Art. Limpieza", "Comprar articulos de limpieza");
        todo.setId(233L);
        todo.setImage("C:/file-dir/image1.png");
        todos.add(todo);
        todo = new Todo("Doctor", "Sacar turno con pediatra");
        todo.setId(245L);
        todo.setImage("C:/file-dir/image2.png");
        todos.add(todo);
        Response correctResponse = new Response(null, todos, HttpStatus.OK);

        Mockito.when(this.todoRepository.findAll((Specification<Todo>) Mockito.eq(null))).thenReturn(todos);


        Response response = this.todoService.getTodos(null, null, null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldGetTodosWithFilter() {
        List<Todo> todos = new ArrayList<>();
        Todo todo = new Todo("Doctor", "Sacar turno con pediatra");
        todo.setId(245L);
        todo.setImage("C:/file-dir/image1.png");
        todo.setStatus(Status.DONE);
        todos.add(todo);
        todo = new Todo("Doctor", "Sacar turno con nutricionista");
        todo.setId(245L);
        todo.setImage("C:/file-dir/image2.png");
        todo.setStatus(Status.DONE);
        todos.add(todo);
        Response correctResponse = new Response(null, todos, HttpStatus.OK);

        Specification spec = Specification.where(new TodoSpecification(new SearchCriteria("description", ":", "sacar")));

        Mockito.when(this.todoRepository.findAll(Mockito.any(Specification.class))).thenReturn(todos);
        Response response = this.todoService.getTodos(null, "Sacar", null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldUpdateTodo() {
        Long id = 233L;
        Response correctResponse = new Response(null, id, HttpStatus.OK);
        Todo todo = new Todo("Art. Limpieza", "Comprar articulos de limpieza");
        todo.setId(id);
        todo.setImage("C:/file-dir/image1.png");
        Mockito.doNothing().when(this.todoRepository).updateStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Response response = this.todoService.updateStatus(id, Status.DONE);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotUpdateTodo() {
        Long id = 233L;
        Response correctResponse = new Response("Error updating Todo", null, HttpStatus.BAD_REQUEST);
        Todo todo = new Todo("Art. Limpieza", "Comprar articulos de limpieza");
        todo.setId(id);
        todo.setImage("C:/file-dir/image1.png");
        Mockito.doThrow(new RuntimeException("Some SQL exception")).when(this.todoRepository).updateStatus(Mockito.anyLong(), Mockito.any(Status.class));

        Response response = this.todoService.updateStatus(id, Status.DONE);
        assertThat(response).isEqualTo(correctResponse);
    }
}
