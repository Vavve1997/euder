package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.UserDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserEndToEndTest {

    @LocalServerPort
    private int port;

    @Test
    void addUser_aValidUserCanBeAdded() {
        UserDto userDto = given()
                .baseUri("http://localhost")
                .port(port)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                          "firstName": "Stannis",
                          "lastName": "Baratheon",
                          "email": "stannisshouldbeking@example.com",
                          "phoneNumber": "011111111",
                          "address": {
                            "street": "RhollorStreet",
                            "houseNumber": 12,
                            "postalCode": 3000,
                            "city": "DragonStone"
                          }
                        }
                        """)
                .when()
                .post("/books")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(UserDto.class);

        assertThat(userDto.getId()).isNotBlank();
        assertThat(userDto.getFirstName()).isEqualTo("Stannis");
        assertThat(userDto.getLastName()).isEqualTo("Baratheon");
        assertThat(userDto.getEmail())
                .isEqualTo("stannisshouldbeking@example.com");
    }
}
