package ru.rrusanov.notes.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;


/**
 * @author Roman Rusanov
 * @since 21.06.2021
 * email roman9628@gmail.com
 * Class describe instance of note. Note related with HashTag by id fields.
 * Relation ManyToMany describe in table note_hashtag
 */
@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreate;
    @JoinTable(name = "note_hashtag",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id"))
    @Fetch(FetchMode.JOIN)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<HashTag> hashTag = new ArrayList<>();
    private String topic;

    public static Note of(String text, Date dateCreate) {
        Note note = new Note();
        note.text = text;
        note.dateCreate = dateCreate;
        return note;
    }

    public void addHashTag(HashTag tag) {
        this.hashTag.add(tag);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    public List<HashTag> getHashTag() {
        return hashTag;
    }

    public void setHashTag(List<HashTag> hashTag) {
        this.hashTag = hashTag;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return id.equals(note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", dateCreate=" + dateCreate +
                ", hashTag=" + hashTag +
                ", topic='" + topic + '\'' +
                '}';
    }

    public String getJson() {
        return new Gson().toJson(this);
    }
}