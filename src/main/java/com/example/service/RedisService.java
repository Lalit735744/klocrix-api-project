package com.example.service;

import com.example.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    private final Jedis jedis;
    private final ObjectMapper mapper;

    public RedisService() {
        this.jedis = new Jedis("localhost", 6379);
        this.mapper = new ObjectMapper();
    }

    public void cacheUser(User user) {
        try {
            User userForCache = new User(user.getId(), user.getName(), user.getEmail(), null); // Exclude password
            String json = mapper.writeValueAsString(userForCache);
            jedis.setex("user:" + user.getId(), 3600, json);
        } catch (JsonProcessingException e) {
            logger.error("Failed to cache user in Redis", e);
        }
    }

    public User getCachedUser(String id) {
        String json = jedis.get("user:" + id);
        if (json == null) return null;
        try {
            return mapper.readValue(json, User.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to get cached user from Redis", e);
            return null;
        }
    }

    public void deleteCachedUser(String id) {
        jedis.del("user:" + id);
    }
}