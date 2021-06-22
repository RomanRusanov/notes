package ru.rrusanov.notes.store;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rrusanov.notes.domain.HashTag;
import ru.rrusanov.notes.domain.Note;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * @author Roman Rusanov
 * @version 0.1
 * @since 30.12.2020
 * email roman9628@gmail.com
 * The class describe interaction ORM to DB.
 */
public class Hibernate implements AutoCloseable {
    /**
     * The instance with logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Hibernate.class.getName());
    /**
     * Registry for hibernate configuration.
     */
    private final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .configure().build();
    /**
     * Session factory for hibernate interaction.
     */
    private final SessionFactory sf = new MetadataSources(registry)
            .buildMetadata().buildSessionFactory();

    /**
     * The inner class guarantees that only one instance is initialized.
     */
    private static final class Lazy {
        private static final Hibernate INST = new Hibernate();
    }

    /**
     * Default private constructor.
     */
    private Hibernate() {
    }
    /**
     * The method create and get instance.
     * @return Hibernate.
     */
    public static Hibernate instOf() {
        return Lazy.INST;
    }

    /**
     * The method implements in necessary AutoCloseable interface.
     */
    @Override
    public void close() {
        StandardServiceRegistryBuilder.destroy(registry);
    }

    /**
     * The method execute query to DB.
     * @param command lambda with custom query.
     * @param <T> Expected type.
     * @return Instance created by hibernate.
     */
    private <T> T tx(final Function<Session, T> command) {
        final Session session = sf.openSession();
        final Transaction tx = session.beginTransaction();
        try {
            T rsl = command.apply(session);
            tx.commit();
            LOG.debug("Transaction commit");
            return rsl;
        } catch (final Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * The methods add model to DB.
     * @param model Instance model.
     * @param <T> Class model.
     * @return instance of model.
     */
    public <T> T createModel(T model) {
        return this.tx(session -> {
            session.save(model);
            LOG.debug("Entity persisted: {}", model);
            return model;
        });
    }

    /**
     * The Method return all items.
     * @return items collection.
     */
    public List<Note> findAllNotes() {
        return this.tx(
                session -> session.createQuery(
                        "select distinct n from Note n", Note.class).list()
        );
    }

    /**
     * The method takes an note to update the id field it finds in the note collection.
     * In that schema implementations passed id param must exist in DB.
     * @param note note to need update.
     */
    public void updateNote(Note note) {
        Note noteToUpdate = this.findNoteById(note.getId());
        noteToUpdate.setId(note.getId());
        if (!note.getHashTag().isEmpty()) {
            noteToUpdate.setHashTag(note.getHashTag());
        }
        noteToUpdate.setText(note.getText());
        noteToUpdate.setDateCreate(note.getDateCreate());
        noteToUpdate.setTopic(note.getTopic());
        this.tx(session -> {
            session.update(noteToUpdate);
            return noteToUpdate;
        });
    }

    /**
     * The method takes a long id value and looks for it in DB,
     * returns the note which has this id.
     * @param id String id note to search.
     * @return math note with id
     */
    public Note findNoteById(Long id) {
        return this.tx(
                session -> {
                    final Query query = session.createQuery(
                        "select n from Note n where n.id=:note_id");
                    query.setParameter("note_id", id);
                    return (Note) query.uniqueResult();
                }
        );
    }

    /**
     * The method takes a long id value and looks for it in DB,
     * returns the hashTag which has this id.
     * @param id String id note to search.
     * @return Math note with id.
     */
    public HashTag findTagById(Long id) {
        return this.tx(
                session -> {
                    final Query query = session.createQuery(
                            "select tag from HashTag tag where tag.id=:tag_id");
                    query.setParameter("tag_id", id);
                    return (HashTag) query.uniqueResult();
                }
        );
    }

    /**
     * The method takes an note id to delete.
     * @param id Id item.
     * @return if item with passed id delete and not find in DB return true,
     * otherwise false.
     */
    public Note deleteNote(Long id) {
        Note note = new Note();
        note.setId(id);
        return this.tx(session -> {
            session.delete(note);
            return note;
        });
    }

    /**
     * The method find notes greater than passed date.
     * @param date Date to compare.
     * @return Founded notes.
     */
    public List<Note> findNotesGreaterDate(Date date) {
        return this.tx(
                session -> {
                    final Query query = session.createQuery(
                            "select distinct n from Note n where n.dateCreate>:date", Note.class);
                    query.setParameter("date", date);
                    return (List<Note>) query.list();
                }
        );
    }

    /**
     * The method find notes less than passed date.
     * @param date Date to compare.
     * @return Founded notes.
     */
    public List<Note> findNotesLessDate(Date date) {
        return this.tx(
                session -> {
                    final Query query = session.createQuery(
                            "select distinct n from Note n where n.dateCreate<:date", Note.class);
                    query.setParameter("date", date);
                    return (List<Note>) query.list();
                }
        );
    }

    /**
     * The method find notes equals than passed date.
     * @param date Date to compare.
     * @return Founded notes.
     */
    public List<Note> findNotesEqualDate(Date date) {
        return this.tx(
                session -> {
                    final Query query = session.createQuery(
                            "select distinct n from Note n where n.dateCreate=:date", Note.class);
                    query.setParameter("date", date);
                    return (List<Note>) query.list();
                }
        );
    }

    /**
     * The method find notes that contain passed tag id.
     * @param id Tag id to find.
     * @return Founded notes.
     */
    public List<Note> findNotesByHashTagId(Long id) {
        return this.tx(
                session -> {
                    final Query query = session.createQuery(
                            "select distinct n from Note n inner join n.hashTag h where h.id=:tag_id", Note.class);
                    query.setParameter("tag_id", id);
                    return (List<Note>) query.list();
                }
        );
    }

    /**
     * The method get all notes that contains sub passed substring in text or title fields.
     * @param str Substring to find.
     * @return Founded notes.
     */
    public List<Note> findNotesWithTextContain(String str) {
        return this.tx(
                session -> {
                    final Query query = session.createQuery(
                            "select distinct n from Note n where n.text like ?1 or n.topic like ?1", Note.class);
                    query.setParameter(1, "%"+str+"%");
                    return (List<Note>) query.list();
                }
        );
    }

}