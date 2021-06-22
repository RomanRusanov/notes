package ru.rrusanov.notes.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.rrusanov.notes.domain.Note;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest()
@AutoConfigureMockMvc
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteController noteController;

    @Test
    public void whenRequestCreate() throws Exception {
        Note note = new Note();
        note.setText("Текст заметки");
        note.setTopic("Название заметки");
        when(noteController.create(any(Note.class)))
                .thenReturn(new ResponseEntity<>(note, HttpStatus.OK));
        mockMvc.perform(
                post("/note/")
                        .content(note.getJson())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text", is("Текст заметки")))
                .andExpect(jsonPath("$.topic", is("Название заметки")));
        verify(noteController, times(1)).create(any(Note.class));
        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteController).create(captor.capture());
        assertEquals("Текст заметки", captor.getValue().getText());
        assertEquals("Название заметки", captor.getValue().getTopic());
    }

    @Test
    void whenRequestUpdate() throws Exception {
        Note note = new Note();
        note.setText("Текст заметки отредактированный");
        note.setTopic("Название заметки отредактированное");
        when(noteController.update(any(Note.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        mockMvc.perform(
                put("/note/")
                        .content(note.getJson())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        verify(noteController, times(1)).update(any(Note.class));
        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteController).update(captor.capture());
        assertEquals("Текст заметки отредактированный", captor.getValue().getText());
        assertEquals("Название заметки отредактированное", captor.getValue().getTopic());
    }

    @Test
    void whenRequestDelete() throws Exception {
        when(noteController.delete(anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        mockMvc.perform(MockMvcRequestBuilders.delete("/note/{id}", 1))
                .andDo(print())
                .andExpect(status().isOk());
        verify(noteController, times(1)).delete(anyLong());
        verifyNoMoreInteractions(noteController);
    }

}