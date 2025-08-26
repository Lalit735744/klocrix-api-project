package com.example.service;

import com.example.model.Contact;
import com.example.model.User;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoDBService {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBService.class);
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> contactsCollection;

    public MongoDBService() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("userdb");
        this.collection = database.getCollection("user");
        this.contactsCollection = database.getCollection("contacts");
    }

    public void createUser(User user) {
        try {
            Document doc = new Document()
                    .append("name", user.getName())
                    .append("email", user.getEmail())
                    .append("password", user.getPassword());
            collection.insertOne(doc);
            user.setId(doc.getObjectId("_id").toString());
        } catch (Exception e) {
            logger.error("Failed to create user in MongoDB", e);
            throw e;
        }
    }

    public User getUser(String id) {
        try {
            Document doc = collection.find(new Document("_id", new ObjectId(id))).first();
            if (doc == null) return null;
            return new User(
                doc.getObjectId("_id").toString(),
                doc.getString("name"),
                doc.getString("email"),
                doc.getString("password")
            );
        } catch (Exception e) {
            logger.error("Failed to get user from MongoDB", e);
            throw e;
        }
    }

    public User getUserByEmail(String email) {
        try {
            Document doc = collection.find(new Document("email", email)).first();
            if (doc == null) return null;
            return new User(
                doc.getObjectId("_id").toString(),
                doc.getString("name"),
                doc.getString("email"),
                doc.getString("password")
            );
        } catch (Exception e) {
            logger.error("Failed to get user by email from MongoDB", e);
            throw e;
        }
    }

    public List<User> getAllUsers() {
        try {
            List<User> users = new ArrayList<>();
            for (Document doc : collection.find()) {
                users.add(new User(
                    doc.getObjectId("_id").toString(),
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getString("password")
                ));
            }
            return users;
        } catch (Exception e) {
            logger.error("Failed to get all users from MongoDB", e);
            throw e;
        }
    }

    public void updateUser(User user) {
        try {
            Document update = new Document()
                    .append("name", user.getName())
                    .append("email", user.getEmail())
                    .append("password", user.getPassword());
            collection.updateOne(
                new Document("_id", new ObjectId(user.getId())),
                new Document("$set", update)
            );
        } catch (Exception e) {
            logger.error("Failed to update user in MongoDB", e);
            throw e;
        }
    }

    public void deleteUser(String id) {
        try {
            collection.deleteOne(new Document("_id", new ObjectId(id)));
        } catch (Exception e) {
            logger.error("Failed to delete user from MongoDB", e);
            throw e;
        }
    }

    // Contact CRUD methods
    public void createContact(Contact contact) {
        try {
            Document doc = new Document()
                    .append("name", contact.getName())
                    .append("email", contact.getEmail())
                    .append("phone", contact.getPhone());
            contactsCollection.insertOne(doc);
            contact.setId(doc.getObjectId("_id").toString());
        } catch (Exception e) {
            logger.error("Failed to create contact in MongoDB", e);
            throw e;
        }
    }

    public Contact getContact(String id) {
        try {
            Document doc = contactsCollection.find(new Document("_id", new ObjectId(id))).first();
            if (doc == null) return null;
            return new Contact(
                doc.getObjectId("_id").toString(),
                doc.getString("name"),
                doc.getString("email"),
                doc.getString("phone")
            );
        } catch (Exception e) {
            logger.error("Failed to get contact from MongoDB", e);
            throw e;
        }
    }

    public List<Contact> getAllContacts() {
        try {
            List<Contact> contacts = new ArrayList<>();
            for (Document doc : contactsCollection.find()) {
                contacts.add(new Contact(
                    doc.getObjectId("_id").toString(),
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getString("phone")
                ));
            }
            return contacts;
        } catch (Exception e) {
            logger.error("Failed to get all contacts from MongoDB", e);
            throw e;
        }
    }

    public void updateContact(Contact contact) {
        try {
            Document update = new Document()
                    .append("name", contact.getName())
                    .append("email", contact.getEmail())
                    .append("phone", contact.getPhone());
            contactsCollection.updateOne(
                new Document("_id", new ObjectId(contact.getId())),
                new Document("$set", update)
            );
        } catch (Exception e) {
            logger.error("Failed to update contact in MongoDB", e);
            throw e;
        }
    }

    public void deleteContact(String id) {
        try {
            contactsCollection.deleteOne(new Document("_id", new ObjectId(id)));
        } catch (Exception e) {
            logger.error("Failed to delete contact from MongoDB", e);
            throw e;
        }
    }
}