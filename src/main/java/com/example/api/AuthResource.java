package com.example.api;

import com.example.model.User;
import com.example.service.MongoDBService;
import com.example.service.RedisService;
import com.example.util.JwtUtil;
import com.example.util.PasswordUtil;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private final MongoDBService mongoService = new MongoDBService();
    private final RedisService redisService = new RedisService();

    @POST
    @Path("/register")
    public Response register(User user) {
        if (user.getEmail() == null || user.getPassword() == null || user.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing fields").build();
        }
        if (mongoService.getUserByEmail(user.getEmail()) != null) {
            return Response.status(Response.Status.CONFLICT).entity("Email already registered").build();
        }
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        mongoService.createUser(user);
        redisService.cacheUser(user);
        return Response.ok().entity("User registered successfully").build();
    }

    @POST
    @Path("/login")
    public Response login(Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        if (email == null || password == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing fields").build();
        }
        User user = mongoService.getUserByEmail(email);
        if (user == null || !PasswordUtil.verifyPassword(password, user.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }
        String token = JwtUtil.generateToken(user.getId());
        redisService.cacheUser(user);
        Map<String, String> resp = new HashMap<>();
        resp.put("token", token);
        return Response.ok(resp).build();
    }
}
