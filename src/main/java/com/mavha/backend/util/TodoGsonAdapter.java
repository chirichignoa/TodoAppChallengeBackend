package com.mavha.backend.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;

import java.io.IOException;

public class TodoGsonAdapter extends TypeAdapter<Todo> {

    @Override
    public void write(JsonWriter jsonWriter, Todo todo) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("id").value(todo.getId());
        jsonWriter.name("title").value(todo.getTitle());
        jsonWriter.name("description").value(todo.getDescription());
        jsonWriter.name("status").value(todo.getStatus().toString());
        jsonWriter.name("path").value(todo.getImage());
        jsonWriter.endObject();
    }

    @Override
    public Todo read(JsonReader jsonReader) throws IOException {
        final Todo todo = new Todo();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "id":
                    todo.setId(jsonReader.nextLong());
                    break;
                case "title":
                    todo.setTitle(jsonReader.nextString());
                    break;
                case "description":
                    todo.setDescription(jsonReader.nextString());
                    break;
                case "status":
                    todo.setStatus(Status.PENDING.toString().equals(jsonReader.nextString()) ? Status.PENDING : Status.DONE);
                    break;
            }
        }
        jsonReader.endObject();
        return todo;
    }
}
