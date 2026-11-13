package com.vavedin.euder.feature.item.api;

import com.vavedin.euder.testsupport.StoryApiTest;
import com.vavedin.euder.feature.item.api.dto.*;
import com.vavedin.euder.feature.item.domain.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

class ItemStoriesTest extends StoryApiTest {
    private CreateItemDto request() {
        return new CreateItemDto("Needle", "Arya's sword", new BigDecimal("25.50"), 10);
    }

    @Test
    @DisplayName("Story 2 - Admin creates an item with all fields persisted")
    void givenAdminAndValidItem_whenPost_thenCreated() {
        ItemDto result = admin.assertPost("/items", request()).returns(ItemDto.class);
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getName()).isEqualTo("Needle");
        assertThat(result.getDescription()).isEqualTo("Arya's sword");
        assertThat(result.getPrice()).isEqualByComparingTo("25.50");
        assertThat(result.getAmount()).isEqualTo(10);
        Item stored = items.findById(result.getId()).orElseThrow();
        assertThat(stored.getName()).isEqualTo(result.getName());
        assertThat(stored.getDescription()).isEqualTo(result.getDescription());
        assertThat(stored.getPrice()).isEqualByComparingTo(result.getPrice());
        assertThat(stored.getAmount()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void givenInvalidPrice_whenCreate_thenBadRequest(int price) {
        admin.assertPost("/items", new CreateItemDto("Needle", "Sword", BigDecimal.valueOf(price), 1))
                .failsWith(BAD_REQUEST);
        assertThat(items.count()).isZero();
    }

    @Test
    void givenNegativeStock_whenCreate_thenBadRequest() {
        admin.assertPost("/items", new CreateItemDto("Needle", "Sword", BigDecimal.TEN, -1))
                .failsWith(BAD_REQUEST);
        assertThat(items.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "description", "price", "amount"})
    void givenInvalidField_whenCreate_thenBadRequest(String field) {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("name", "Needle");
        body.put("description", "Sword");
        body.put("price", new BigDecimal("10.00"));
        body.put("amount", 2);
        if (field.equals("amount")) body.put(field, -1);
        else body.put(field, null);
        admin.assertPost("/items", body).failsWith(BAD_REQUEST);
        assertThat(items.count()).isZero();
    }

    @Test
    void givenZeroStock_whenCreate_thenAllowed() {
        ItemDto result = admin.assertPost("/items", new CreateItemDto("Needle", "Sword", BigDecimal.TEN, 0))
                .returns(ItemDto.class);
        assertThat(result.getAmount()).isZero();
    }

    @Test
    @DisplayName("Story 4 - Update replaces every field and sets absolute stock")
    void givenExistingItem_whenUpdate_thenSameIdAndNewValues() {
        Item item = saveItem("Needle", "25.50", 10);
        ItemDto result = admin.assertPut("/items/{id}",
                new UpdateItemDto("Longclaw", "Valyrian steel", new BigDecimal("99.95"), 3), item.getId())
                .returns(ItemDto.class);
        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo("Longclaw");
        assertThat(result.getDescription()).isEqualTo("Valyrian steel");
        assertThat(result.getPrice()).isEqualByComparingTo("99.95");
        assertThat(result.getAmount()).isEqualTo(3);
        Item stored = items.findById(item.getId()).orElseThrow();
        assertThat(stored.getName()).isEqualTo("Longclaw");
        assertThat(stored.getDescription()).isEqualTo("Valyrian steel");
        assertThat(stored.getPrice()).isEqualByComparingTo("99.95");
        assertThat(stored.getAmount()).isEqualTo(3);
        assertThat(items.count()).isEqualTo(1);
    }

    @Test
    void givenInvalidUpdate_whenPut_thenOriginalItemUnchanged() {
        Item item = saveItem("Needle", "25.50", 10);
        admin.assertPut("/items/{id}", new UpdateItemDto("Changed", "Changed", BigDecimal.ZERO, 3), item.getId())
                .failsWith(BAD_REQUEST);
        assertThat(items.findById(item.getId()).orElseThrow()).usingRecursiveComparison().isEqualTo(item);
    }

    @Test
    void givenUnknownItem_whenUpdate_thenNotFound() {
        admin.assertPut("/items/unknown", new UpdateItemDto("Needle", "Sword", BigDecimal.TEN, 1))
                .failsWith(NOT_FOUND);
    }

    @Test
    void givenMemberOrGuest_whenCreatingOrUpdating_thenNoChanges() {
        Item item = saveItem("Needle", "25.50", 10);
        var update = new UpdateItemDto("Changed", "Changed", BigDecimal.TEN, 1);
        member.assertPost("/items", request()).failsWith(FORBIDDEN);
        guest.assertPost("/items", request()).failsWith(UNAUTHORIZED);
        member.assertPut("/items/{id}", update, item.getId()).failsWith(FORBIDDEN);
        guest.assertPut("/items/{id}", update, item.getId()).failsWith(UNAUTHORIZED);
        assertThat(items.count()).isEqualTo(1);
        assertThat(items.findById(item.getId()).orElseThrow()).usingRecursiveComparison().isEqualTo(item);
    }

    @Test
    void givenWrongPassword_whenCreating_thenUnauthorizedWithBasicChallenge() {
        var response = guest.withBasicAuth(tyrion.getEmail(), "wrong")
                .assertPost("/items", request()).response(UNAUTHORIZED);
        assertThat(response.header("WWW-Authenticate")).startsWith("Basic ");
        assertThat(items.count()).isZero();
    }
}
