package com.example;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {
    public static void main(String[] args) {
        String baseUri = "http://localhost:8080";
        ResourceConfig config = new ResourceConfig().packages("com.example.api");
        Server server = JettyHttpContainerFactory.createServer(URI.create(baseUri), config);
        try {
            server.start();
            System.out.println("Server started at " + baseUri);
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.destroy();
        }
    }
}