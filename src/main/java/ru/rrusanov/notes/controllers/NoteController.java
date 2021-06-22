package ru.rrusanov.notes.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rrusanov.notes.domain.HashTag;
import ru.rrusanov.notes.domain.Note;
import ru.rrusanov.notes.domain.jsonmapper.JsonDate;
import ru.rrusanov.notes.store.Hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Roman Rusanov
 * @since 21.06.2021
 * email roman9628@gmail.com
 * Class describe REST Controller working with note entity.
 */
@RestController
@RequestMapping("/note")
public class NoteController {

    /**
     * Return all saved Notes in DB.
     * @return List with notes.
     */
    @GetMapping("/")
    public List<Note> findAll() {
        return Hibernate.instOf().findAllNotes();
    }

    /**
     * Create new note passed json to http://localhost:8080/note/ post request date
     * @param note mapped instance from json.
     * @return Created instance with real id DB.
     */
    @PostMapping("/")
    public ResponseEntity<Note> create(@RequestBody Note note) {
        if (note.getText() == null || note.getDateCreate() == null) {
            return new ResponseEntity<Note>(
                    HttpStatus.CONFLICT
            );
        }
        if (!note.getHashTag().isEmpty()) {
            note.getHashTag().forEach(hashTag -> {
                HashTag tag = Hibernate.instOf().findTagById(hashTag.getId());
                if (tag == null) {
                    Hibernate.instOf().createModel(hashTag);
                } else {
                    hashTag.setTitle(tag.getTitle());
                }
            });
        }
        return new ResponseEntity<>(
                Hibernate.instOf().createModel(note),
                HttpStatus.CREATED
        );
    }

    /**
     * Update note data new data take from json http://localhost:8080/note/ put request.
     * If Note contain new HashTag when this tag persist.
     * @param note mapped instance from json.
     * @return If passed not with id exist in DB, when update and return 200 status.
     * Otherwise return conflict 409 status.
     */
    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Note note) {
        Note noteFromDB = Hibernate.instOf().findNoteById(note.getId());
        if (noteFromDB != null) {
            if (!note.getHashTag().isEmpty()) {
                note.getHashTag().forEach(hashTag -> {
                    if (Hibernate.instOf().findTagById(hashTag.getId()) == null) {
                        Hibernate.instOf().createModel(hashTag);
                    } else {
                        noteFromDB.addHashTag(hashTag);
                    }
                });
            }
            Hibernate.instOf().updateNote(note);
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<Void>(
                HttpStatus.CONFLICT
        );
    }

    /**
     * Delete note if id exist in DB.
     * Relation from table (note_hashtag) also be updated.
     * HashTag table will not be affected.
     * @param id note id.
     * @return If passed not with id exist in DB, when delete and return 200 status.
     * Otherwise return conflict 409 status.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Note> noteToDelete = Optional.ofNullable(Hibernate.instOf().findNoteById(id));
        if (noteToDelete.isPresent()) {
            Hibernate.instOf().deleteNote(noteToDelete.get().getId());
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<Void>(
                HttpStatus.CONFLICT
        );
    }

    /**
     * Get all notes which satisfy the condition.
     * @param jsonDate mapper that contain date and sign to compare.
     * @return List notes math.
     */
    @GetMapping("/findByDate/")
    public List<Note> findByDate(@RequestBody JsonDate jsonDate) {
        List<Note> result = new ArrayList<>();
        if (jsonDate.getComparisonSign().equals('>')) {
            result = Hibernate.instOf().findNotesGreaterDate(jsonDate.getDate());
        }
        if (jsonDate.getComparisonSign().equals('<')) {
            result = Hibernate.instOf().findNotesLessDate(jsonDate.getDate());
        }
        if (jsonDate.getComparisonSign().equals('=')) {
            result = Hibernate.instOf().findNotesEqualDate(jsonDate.getDate());
        }
        return result;
    }

    /**
     * Get all notes that contain the specified tag id.
     * @param id tag id.
     * @return List maths notes.
     */
    @GetMapping("/findByHashTag/{id}")
    public List<Note> findByHashTagId(@PathVariable Long id) {
        return Hibernate.instOf().findNotesByHashTagId(id);
    }

    /**
     * Get all notes that contain passed sub sting in text or topic fields.
     * @param str substring to search.
     * @return List maths notes.
     */
    @GetMapping("/findByText/{str}")
    public List<Note> findByText(@PathVariable String str) {
        return Hibernate.instOf().findNotesWithTextContain(str);
    }
}