package com.vavedin.euder.feature.security;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.assertj.core.api.Assertions.*;
class PasswordEncoderTest {
    private final PasswordEncoder encoder = new PasswordEncoder();
    @Test void encode_hashesAndSaltsPassword_andMatchesOnlyCorrectPassword() {
        String first = encoder.encode("Secret123!");
        String second = encoder.encode("Secret123!");
        assertThat(first).isNotEqualTo("Secret123!").isNotEqualTo(second);
        assertThat(encoder.matches("Secret123!", first)).isTrue();
        assertThat(encoder.matches("Secret123!", second)).isTrue();
        assertThat(encoder.matches("wrong", first)).isFalse();
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "   "})
    void encode_rejectsMissingPassword(String password) {
        assertThatThrownBy(() -> encoder.encode(password)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must not be null or blank");
    }
    @Test void matches_returnsFalseForNullInputs() {
        assertThat(encoder.matches(null, "hash")).isFalse();
        assertThat(encoder.matches("secret", null)).isFalse();
    }
}
