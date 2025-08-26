package com.example.api;

import com.example.util.JwtUtil;
import io.jsonwebtoken.Claims;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        if ("register".equals(path) || "/register".equals(path)|| path.endsWith("/register")|| path.contains("/login")) {
            // Skip authentication for /register endpoint
            return;
        }
        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abort(requestContext, "Missing or invalid Authorization header Lalit");
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        try {
            // ✅ Extract claims using JwtUtil
            Claims claims = JwtUtil.extractClaims(token);
            assert claims != null;
            String username = claims.getSubject();

            if (username == null || !JwtUtil.validateToken(token, username)) {
                abort(requestContext, "Invalid or expired token");
            }

             //  Optionally set security context here

        } catch (Exception e) {
            abort(requestContext, "Invalid or expired token");
        }
    }

    private void abort(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(message)
                        .build()
        );
    }
}
