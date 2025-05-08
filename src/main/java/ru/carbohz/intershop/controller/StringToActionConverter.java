package ru.carbohz.intershop.controller;


import org.springframework.stereotype.Component;
import ru.carbohz.intershop.model.Action;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;

@Component
public class StringToActionConverter implements Converter<String, Action> {
    @Override
    public Action convert(String source) {
        try {
            return Action.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Action. Valid values: " + Arrays.toString(Action.values())
            );
        }
    }
}
