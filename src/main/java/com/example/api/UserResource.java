package com.example.api;

import com.example.model.User;
import com.example.service.MongoDBService;
import com.example.service.RedisService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    private final MongoDBService mongoService = new MongoDBService();
    private final RedisService redisService = new RedisService();

    @GET
    public Response getUsers() {
        try {
            List<User> users = mongoService.getAllUsers();
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to fetch users.\"}").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") String id) {
        if (id == null || id.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"User ID is required.\"}").build();
        }
        try {
            User user = redisService.getCachedUser(id);
            if (user != null) {
                return Response.ok(user).build();
            }
            user = mongoService.getUser(id);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"User not found.\"}").build();
            }
            redisService.cacheUser(user);
            return Response.ok(user).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to fetch user.\"}").build();
        }
    }

    @POST
    public Response createUser(User user) {
        if (user == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"User data is required.\"}").build();
        }
        if (user.getName() == null || user.getName().trim().isEmpty() ||
            user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"Name and email are required.\"}").build();
        }
        try {
            mongoService.createUser(user);
            redisService.cacheUser(user);
            return Response.status(Response.Status.CREATED).entity(user).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to create user.\"}").build();
        }
    }
}