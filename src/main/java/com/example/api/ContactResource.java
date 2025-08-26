package com.example.api;

import com.example.model.Contact;
import com.example.service.MongoDBService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.regex.Pattern;

@Path("/contacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {
    private final MongoDBService mongoService = new MongoDBService();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @GET
    public Response getAllContacts() {
        List<Contact> contacts = mongoService.getAllContacts();
        return Response.ok(contacts).build();
    }

    @GET
    @Path("/{id}")
    public Response getContact(@PathParam("id") String id) {
        Contact contact = mongoService.getContact(id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Contact not found").build();
        }
        return Response.ok(contact).build();
    }

    @POST
    public Response createContact(Contact contact) {
        String validation = validateContact(contact);
        if (validation != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(validation).build();
        }
        mongoService.createContact(contact);
        return Response.status(Response.Status.CREATED).entity(contact).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateContact(@PathParam("id") String id, Contact contact) {
        if (mongoService.getContact(id) == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Contact not found").build();
        }
        contact.setId(id);
        String validation = validateContact(contact);
        if (validation != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(validation).build();
        }
        mongoService.updateContact(contact);
        return Response.ok(contact).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteContact(@PathParam("id") String id) {
        if (mongoService.getContact(id) == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Contact not found").build();
        }
        mongoService.deleteContact(id);
        return Response.noContent().build();
    }

    private String validateContact(Contact contact) {
        if (contact.getName() == null || contact.getName().trim().isEmpty()) {
            return "Name is required";
        }
        if (contact.getEmail() == null || !EMAIL_PATTERN.matcher(contact.getEmail()).matches()) {
            return "Valid email is required";
        }
        if (contact.getPhone() == null || contact.getPhone().trim().isEmpty()) {
            return "Phone is required";
        }
        return null;
    }
}
