package ua.alexlapada.configuration;

import ua.alexlapada.web.dto.ErrorApiResponseDto;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RestApiExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable e) {
        if (e instanceof WebApplicationException) {
            return handleWebApplicationException((WebApplicationException) e);
        } else if (e instanceof IllegalArgumentException) {
            return getResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } else if (e instanceof IllegalStateException) {
            return getResponse(Response.Status.CONFLICT, e.getMessage());
        } else {
            return getResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Response getResponse(Response.Status status, String description) {
        return Response
                .status(status)
                .entity(toDto(status.getReasonPhrase(), description))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    private Response handleWebApplicationException(WebApplicationException exception) {
        Response response = exception.getResponse();
        Response.Status status = response.getStatusInfo().toEnum();
        return getResponse(status, exception.getMessage());
    }

    private ErrorApiResponseDto toDto(String msg, String description) {
        return ErrorApiResponseDto.builder()
                .message(msg)
                .description(description)
                .build();
    }
}
