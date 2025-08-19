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
    private final MongoDBService mongoService;
    private final RedisService redisService;

    public UserResource() {
        this.mongoService = new MongoDBService();
        this.redisService = new RedisService();
    }

    @GET
    public Response getUsers() {
        List<User> users = mongoService.getAllUsers();
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") String id) {
        User user = redisService.getCachedUser(id);
        if (user != null) {
            return Response.ok(user).build();
        }
        user = mongoService.getUser(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        redisService.cacheUser(user);
        return Response.ok(user).build();
    }

    @POST
    public Response createUser(User user) {
        if (user.getName() == null || user.getEmail() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Name and email are required").build();
        }
        mongoService.createUser(user);
        redisService.cacheUser(user);
        return Response.status(Response.Status.CREATED).entity(user).build();
    }
}