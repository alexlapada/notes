package ua.alexlapada;

import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.util.JacksonFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import ua.alexlapada.configuration.ApplicationConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class Application implements RequestStreamHandler {

    private final JerseyLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler;

    public Application() {
        ResourceConfig jerseyServerConfiguration = new ResourceConfig()
                .register(JacksonFeature.class)
                .register(RolesAllowedDynamicFeature.class)
                .register(ApplicationConfiguration.class);
        handler = JerseyLambdaContainerHandler.getHttpApiV2ProxyHandler(jerseyServerConfiguration);
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        byte[] event = IOUtils.toByteArray(inputStream);
        log.info("Handle request. Event: {}", JacksonUtil.readJson(event, Object.class));
        handler.proxyStream(new ByteArrayInputStream(event), outputStream, context);
    }
}
