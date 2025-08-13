package ru.carbohz.shop.controller.converter;

import org.junit.jupiter.api.Test;
import ru.carbohz.shop.model.Action;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringToActionConverterTest {

    @Test
    void convert() {
        StringToActionConverter converter = new StringToActionConverter();
        Action action = converter.convert("  PlUs   ");
        assertThat(action).isEqualTo(Action.PLUS);
    }

    @Test
    void convertFailed() {
        assertThatThrownBy(() -> {
            StringToActionConverter converter = new StringToActionConverter();
            converter.convert("  unknown   ");
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Action. Valid values: " + Arrays.toString(Action.values()));
    }
}