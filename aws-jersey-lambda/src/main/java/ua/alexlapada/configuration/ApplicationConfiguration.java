package ua.alexlapada.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.base.JsonMappingExceptionMapper;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.base.JsonParseExceptionMapper;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ServerProperties;
import ua.alexlapada.web.rest.RstApiController;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.text.SimpleDateFormat;

public class ApplicationConfiguration implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        configureJacksonMapper(context);
        configureDependencyInjection(context);
        configureRestApi(context);
        configureRequestValidation(context);
        return true;
    }

    private void configureRequestValidation(FeatureContext context) {
        context.property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    }

    private void configureJacksonMapper(FeatureContext context) {
        String disableMoxy = CommonProperties.MOXY_JSON_FEATURE_DISABLE + '.'
                + context.getConfiguration().getRuntimeType().name().toLowerCase();
        context.property(disableMoxy, true);
        context.register(JsonParseExceptionMapper.class);
        context.register(JsonMappingExceptionMapper.class);
        context.register(ObjectMapperContextResolver.class);
        context.register(JacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
    }

    private void configureDependencyInjection(FeatureContext context) {
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
            }
        });
    }

    private void configureRestApi(FeatureContext context) {
        context.register(RstApiController.class);
        context.register(RestApiExceptionHandler.class);
    }

    @Provider
    private static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

        private final ObjectMapper mapper;

        public ObjectMapperContextResolver() {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
            mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return mapper;
        }
    }
}
