package com.vavedin.euder.testsupport;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent, immutable helper for REST Assured tests.
 *
 * <pre>
 * ValidationError error = templates.assertGet("/path")
 *         .failsWith(HttpStatus.BAD_REQUEST, ValidationError.class);
 *
 * CustomerDto user = templates.withAuthHeader("admin@example.com")
 *         .assertPost("/path", body)
 *         .returns(CustomerDto.class);
 * </pre>
 */
public class Templates {

    private final int port;
    private final Map<String, String> headers;

    public Templates(int port) {
        this(port, Map.of());
    }

    private Templates(int port, Map<String, String> headers) {
        this.port = port;
        this.headers = headers;
    }

    // Configuration (each call returns a new instance, so a shared field stays clean)

    public Templates withHeader(String name, String value) {
        Map<String, String> copy = new LinkedHashMap<>(headers);
        copy.put(name, value);
        return new Templates(port, Map.copyOf(copy));
    }

    public Templates withBasicAuth(String username, String password) {
        String credentials = HttpHeaders.encodeBasicAuth(username, password, StandardCharsets.UTF_8);
        return withHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
    }

    public RequestSpecification baseRequest() {
        return RestAssured.given()
                .baseUri("http://localhost")
                .port(port)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .headers(headers);
    }

    // Requests (nothing is sent until a terminal method on ResponseAssertion is called)

    public ResponseAssertion assertGet(String path, Object... pathParams) {
        return new ResponseAssertion(baseRequest(), Method.GET, path, pathParams, HttpStatus.OK);
    }

    public ResponseAssertion assertPost(String path, Object body, Object... pathParams) {
        return new ResponseAssertion(baseRequest().body(body), Method.POST, path, pathParams, HttpStatus.CREATED);
    }

    public ResponseAssertion assertPostEmptyBody(String path, Object... pathParams) {
        return new ResponseAssertion(baseRequest(), Method.POST, path, pathParams, HttpStatus.CREATED);
    }

    public ResponseAssertion assertPut(String path, Object body, Object... pathParams) {
        return new ResponseAssertion(baseRequest().body(body), Method.PUT, path, pathParams, HttpStatus.OK);
    }

    public ResponseAssertion assertPatch(String path, Object body, Object... pathParams) {
        return new ResponseAssertion(baseRequest().body(body), Method.PATCH, path, pathParams, HttpStatus.OK);
    }

    public ResponseAssertion assertDelete(String path, Object... pathParams) {
        return new ResponseAssertion(baseRequest(), Method.DELETE, path, pathParams, HttpStatus.NO_CONTENT);
    }

    public static final class ResponseAssertion {
        private final RequestSpecification request;
        private final Method method;
        private final String path;
        private final Object[] pathParams;
        private final HttpStatus defaultSuccess;
        private boolean enablePrint;

        private ResponseAssertion(RequestSpecification request, Method method, String path,
                                  Object[] pathParams, HttpStatus defaultSuccess) {
            this.request = request;
            this.method = method;
            this.path = path;
            this.pathParams = pathParams;
            this.defaultSuccess = defaultSuccess;
            this.enablePrint = false;
        }

        public ResponseAssertion withPrintEnabled() {
            enablePrint = true;
            return this;
        }

        public Response response(HttpStatus status) {
            return expect(status).response();
        }

        // Success

        /**
         * Expects the method's default success status (GET/PUT/PATCH 200, POST 201).
         */
        public <T> T returns(Class<T> type) {
            return returns(defaultSuccess, type);
        }

        public <T> T returns(HttpStatus status, Class<T> type) {
            return expect(status).as(type);
        }

        /**
         * For generic bodies, e.g. {@code returns(new TypeRef<List<CustomerDto>>() {})}.
         */
        public <T> T returns(TypeRef<T> type) {
            return expect(defaultSuccess).as(type);
        }

        /**
         * Expects the default success status and ignores the body (e.g. DELETE → 204).
         */
        public void succeeds() {
            expect(defaultSuccess);
        }

        public void succeeds(HttpStatus status) {
            expect(status);
        }

        // Failure

        public <T> T failsWith(HttpStatus status, Class<T> errorType) {
            requireError(status);
            return expect(status).as(errorType);
        }

        public void failsWith(HttpStatus status) {
            requireError(status);
            expect(status);
        }

        private static void requireError(HttpStatus status) {
            if (!status.isError()) {
                throw new IllegalArgumentException("failsWith expects a 4xx/5xx status but got " + status);
            }
        }

        private ExtractableResponse<Response> expect(HttpStatus status) {
            ExtractableResponse<Response> response = request.request(method, path, pathParams)
                    .then()
                    .log().ifValidationFails()
                    .statusCode(status.value())
                    .extract();

            if (enablePrint) {
                System.out.println(response.asPrettyString());
            }
            return response;
        }
    }
}
