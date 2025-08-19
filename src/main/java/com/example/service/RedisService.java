package com.example.service;

import com.example.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

public class RedisService {
    private final Jedis jedis;
    private final ObjectMapper mapper;

    public RedisService() {
        this.jedis = new Jedis("localhost", 6379);
        this.mapper = new ObjectMapper();
    }

    public void cacheUser(User user) {
        try {
            String json = mapper.writeValueAsString(user);
            jedis.setex("user:" + user.getId(), 3600, json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public User getCachedUser(String id) {
        String json = jedis.get("user:" + id);
        if (json == null) return null;
        try {
            return mapper.readValue(json, User.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}