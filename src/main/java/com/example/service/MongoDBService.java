package com.example.service;

import com.example.model.User;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class MongoDBService {
    private final MongoCollection<Document> collection;

    public MongoDBService() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("userdb");
        this.collection = database.getCollection("users");
    }

    public void createUser(User user) {
        Document doc = new Document()
                .append("name", user.getName())
                .append("email", user.getEmail());
        collection.insertOne(doc);
        user.setId(doc.getObjectId("_id").toString());
    }

    public User getUser(String id) {
        Document doc = collection.find(new Document("_id", new ObjectId(id))).first();
        if (doc == null) return null;
        return new User(doc.getObjectId("_id").toString(), doc.getString("name"), doc.getString("email"));
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for (Document doc : collection.find()) {
            users.add(new User(doc.getObjectId("_id").toString(), doc.getString("name"), doc.getString("email")));
        }
        return users;
    }

    public void updateUser(User user) {
        Document update = new Document()
                .append("name", user.getName())
                .append("email", user.getEmail());
        collection.updateOne(
            new Document("_id", new ObjectId(user.getId())),
            new Document("$set", update)
        );
    }

    public void deleteUser(String id) {
        collection.deleteOne(new Document("_id", new ObjectId(id)));
    }
}