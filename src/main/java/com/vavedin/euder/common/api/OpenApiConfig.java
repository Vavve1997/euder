package com.vavedin.euder.common.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eurderOpenApi() {
        Components components = new Components()
                .addSecuritySchemes(
                        "basicAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("""
                                        Enter your email address as username
                                        and your account password as password.
                                        Access depends on the account's permissions.
                                        """)
                )
                .addResponses(
                        "BadRequest",
                        errorResponse(
                                "Invalid input. Check the response body for details.",
                                "application/json"
                        )
                )
                .addResponses(
                        "Unauthorized",
                        errorResponse(
                                "Authentication is missing or the credentials are invalid.",
                                "application/problem+json"
                        ).addHeaderObject(
                                "WWW-Authenticate",
                                new Header()
                                        .description("HTTP Basic authentication challenge.")
                                        .schema(
                                                new StringSchema()
                                                        .example("Basic realm=\"eurder\"")
                                        )
                        )
                )
                .addResponses(
                        "Forbidden",
                        errorResponse(
                                """
                                The authenticated user does not have permission
                                to perform this action.
                                """,
                                "application/problem+json"
                        )
                )
                .addResponses(
                        "NotFound",
                        errorResponse(
                                "The requested resource does not exist.",
                                "application/json"
                        )
                );

        return new OpenAPI()
                .info(new Info()
                        .title("Eurder API")
                        .version("1.0.0")
                        .description("""
                                Backend for managing customers, items and orders.

                                ## Getting started
                                1. Create a customer account using POST /users.
                                2. Click Authorize and enter your email and password.
                                3. Use the endpoints allowed for your account.

                                ## Roles
                                - MEMBER: place orders, view your own order report,
                                  and reorder your own orders.
                                - ADMIN: create and update items, view customers,
                                  and view today's shipments.

                                Administrator accounts are provisioned separately.
                                Registering through POST /users creates a customer account.

                                ## Ordering
                                - Prices are stored when an order is created.
                                - Existing orders retain their original prices.
                                - Reordering uses current item prices and stock.
                                - Sufficient stock: shipping tomorrow.
                                - Insufficient stock: shipping in seven days.
                                - An order stores its delivery address.

                                ## Shipments
                                Today's shipment overview contains only item groups
                                scheduled for today, grouped by order.

                                ## Development data
                                When the dev profile is enabled, the application
                                loads Game of Thrones sample data.

                                ## Storage
                                This training application uses in-memory storage.
                                Data is reset when the application restarts.
                                """))
                .components(components);
    }

    private ApiResponse errorResponse(
            String description,
            String mediaType
    ) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        mediaType,
                        new MediaType().schema(new ObjectSchema())
                ));
    }
}