package com.nubemedica.api_gateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;

@Component
public class WebFilterSwagger implements WebFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.contains("/v3/api-docs")) {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                        return super.writeWith(DataBufferUtils.join(fluxBody)
                                .map(dataBuffer -> {
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    DataBufferUtils.release(dataBuffer);

                                    String originalJson = new String(content, StandardCharsets.UTF_8);
                                    try {
                                        String modifiedJson = injectSecurityScheme(originalJson);
                                        byte[] modifiedBytes = modifiedJson.getBytes(StandardCharsets.UTF_8);

                                        // CORRECCIÓN CLAVE: Actualizar la cabecera Content-Length con el tamaño del nuevo JSON
                                        getHeaders().setContentLength(modifiedBytes.length);

                                        return bufferFactory.wrap(modifiedBytes);
                                    } catch (Exception e) {
                                        // Si falla, reasignamos la longitud original para evitar que se corte
                                        getHeaders().setContentLength(content.length);
                                        return bufferFactory.wrap(content);
                                    }
                                }));
                    }
                    return super.writeWith(body);
                }
            };

            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }

        return chain.filter(exchange);
    }

    private String injectSecurityScheme(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);

        if (root.isObject()) {
            ObjectNode rootObject = (ObjectNode) root;

            ObjectNode components = rootObject.has("components") 
                    ? (ObjectNode) rootObject.get("components") 
                    : rootObject.putObject("components");

            ObjectNode securitySchemes = components.has("securitySchemes") 
                    ? (ObjectNode) components.get("securitySchemes") 
                    : components.putObject("securitySchemes");

            if (!securitySchemes.has("bearerAuth")) {
                ObjectNode bearerAuth = securitySchemes.putObject("bearerAuth");
                bearerAuth.put("type", "http");
                bearerAuth.put("scheme", "bearer");
                bearerAuth.put("bearerFormat", "JWT");
                bearerAuth.put("description", "Ingrese el token JWT obtenido del login");
            }

            ArrayNode securityRequirementArray = rootObject.has("security") 
                    ? (ArrayNode) rootObject.get("security") 
                    : rootObject.putArray("security");

            boolean hasBearerAuth = false;
            for (JsonNode node : securityRequirementArray) {
                if (node.has("bearerAuth")) {
                    hasBearerAuth = true;
                    break;
                }
            }

            if (!hasBearerAuth) {
                ObjectNode securityRequirement = objectMapper.createObjectNode();
                securityRequirement.putArray("bearerAuth");
                securityRequirementArray.add(securityRequirement);
            }

            return objectMapper.writeValueAsString(rootObject);
        }
        return json;
    }

}
