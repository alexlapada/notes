package ua.alexlapada.web.rest;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

@Path(RstApiController.ENDPOINT)
@Slf4j
@PermitAll
public class RstApiController {
    static final String ENDPOINT = "/api/v1";

    private static final String TEST_ENDPOINT = "/test";

    @Inject
    public RstApiController() {
    }

    @Path(TEST_ENDPOINT)
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response auth() {
        return execute(() -> "Hello");
    }

    private <T> Response execute(Supplier<T> supplier) {
        return Response.ok()
                       .entity(supplier.get())
                       .build();
    }
}
