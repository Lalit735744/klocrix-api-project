package com.example.api;

import com.example.model.User;
import com.example.service.MongoDBService;
import com.example.service.RedisService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);
    private final MongoDBService mongoService = new MongoDBService();
    private final RedisService redisService = new RedisService();

    @GET
    public Response getUsers() {
        try {
            List<User> users = mongoService.getAllUsers();
            // Remove password from response
            users.forEach(u -> u.setPassword(null));
            logger.info("Fetched all users");
            return Response.ok(users).build();
        } catch (Exception e) {
            logger.error("Failed to fetch users", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to fetch users.\"}").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") String id) {
        if (id == null || id.trim().isEmpty()) {
            logger.warn("User ID is required");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"User ID is required.\"}").build();
        }
        try {
            User user = redisService.getCachedUser(id);
            if (user != null) {
                user.setPassword(null);
                logger.info("Fetched user from Redis cache: {}", id);
                return Response.ok(user).build();
            }
            user = mongoService.getUser(id);
            if (user == null) {
                logger.warn("User not found: {}", id);
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"User not found.\"}").build();
            }
            redisService.cacheUser(user);
            user.setPassword(null); // Remove password from response
            logger.info("Fetched user from MongoDB: {}", id);
            return Response.ok(user).build();
        } catch (Exception e) {
            logger.error("Failed to fetch user", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to fetch user.\"}").build();
        }
    }

    @POST
    public Response createUser(User user) {
        if (user == null) {
            logger.warn("User data is required");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"User data is required.\"}").build();
        }
        if (user.getName() == null || user.getName().trim().isEmpty() ||
            user.getEmail() == null || user.getEmail().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            logger.warn("Name, email, and password are required");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"Name, email, and password are required.\"}").build();
        }
        try {
            mongoService.createUser(user);
            redisService.cacheUser(user);
            user.setPassword(null); // Remove password from response
            logger.info("Created user: {}", user.getId());
            return Response.status(Response.Status.CREATED).entity(user).build();
        } catch (Exception e) {
            logger.error("Failed to create user", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Failed to create user.\"}").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") String id, User user) {
        User existing = mongoService.getUser(id);
        if (existing == null) {
            logger.warn("User not found for update: {}", id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        user.setId(id);
        mongoService.updateUser(user);
        redisService.cacheUser(user);
        user.setPassword(null); // Remove password from response
        logger.info("Updated user: {}", id);
        return Response.ok(user).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String id) {
        User user = mongoService.getUser(id);
        if (user == null) {
            logger.warn("User not found for deletion: {}", id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        mongoService.deleteUser(id);
        redisService.deleteCachedUser(id);
        logger.info("Deleted user: {}", id);
        return Response.noContent().build();
    }
}