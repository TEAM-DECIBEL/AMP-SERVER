package com.amp.global.config;

import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.ErrorCode;
import com.amp.global.response.error.BaseErrorResponse;
import com.amp.global.swagger.ExampleHolder;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@OpenAPIDefinition(
        info = @Info(
                title = "AMP API 명세서",
                description = "AMP API 명세서",
                version = "v1"
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@Configuration
public class SwaggerConfig {

    @Value("${swagger.server.local.url:http://localhost:8080}")
    private String localServerUrl;

    @Value("${swagger.server.prod.url:https://ampnotice.kr}")
    private String prodServerUrl;

    @Bean
    public OpenAPI openAPI() {
        List<Server> servers = new ArrayList<>();

        if (!localServerUrl.isEmpty()) {
            servers.add(new Server()
                    .url(localServerUrl)
                    .description("Local Server"));
        }

        if (!prodServerUrl.isEmpty()) {
            servers.add(new Server()
                    .url(prodServerUrl)
                    .description("Prod Server"));
        }

        SecurityRequirement securityRequirement =
                new SecurityRequirement()
                        .addList("Bearer Authentication");

        return new OpenAPI()
                .servers(servers)
                .addSecurityItem(securityRequirement);
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            ApiErrorCodes annotation = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);

            if (annotation != null) {
                generateErrorCodeResponseExample(operation, annotation.value());
            }
            return operation;
        };
    }

    private void generateErrorCodeResponseExample(Operation operation, SwaggerResponseDescription type) {
        ApiResponses responses = operation.getResponses();

        Set<ErrorCode> errorCodeList = type.getErrorCodeList();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = errorCodeList.stream()
                .map(errorCode -> ExampleHolder.of(
                        getSwaggerExample(errorCode),
                        errorCode.getCode(),
                        errorCode.getHttpStatus().value()
                ))
                .collect(Collectors.groupingBy(ExampleHolder::status));

        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private Example getSwaggerExample(ErrorCode errorCode) {
        BaseErrorResponse errorResponse = BaseErrorResponse.of(errorCode);
        Example example = new Example();
        example.description(errorCode.getMsg());
        example.setValue(errorResponse);
        return example;
    }

    private void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach((status, holders) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse apiResponse = new ApiResponse();

            holders.forEach(holder -> mediaType.addExamples(holder.name(), holder.holder()));
            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(status.toString(), apiResponse);
        });
    }

}