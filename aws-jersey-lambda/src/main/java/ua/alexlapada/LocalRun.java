package ua.alexlapada;//package de.zeb.saas.app.cas.tgt.cert.auth;

import com.fasterxml.jackson.core.util.JacksonFeature;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import ua.alexlapada.configuration.ApplicationConfiguration;

import java.io.IOException;
import java.net.URI;

public class LocalRun {
    public static final String BASE_URI = "http://localhost:8080/myapp/";

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }

    public static HttpServer startServer() {
        ResourceConfig jerseyServerConfiguration = new ResourceConfig()
                .register(JacksonFeature.class)
                .register(RolesAllowedDynamicFeature.class)
                .register(ApplicationConfiguration.class);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), jerseyServerConfiguration);
    }
}
