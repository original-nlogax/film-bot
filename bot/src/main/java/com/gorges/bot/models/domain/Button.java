package com.gorges.bot.models.domain;

import static org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public enum Button {

    START("/start"),
    CONTROLS("/controls"),
    STOP("/stop"),
    EXAMPLE("\uD83D\uDCE6 Userbot");

    private final String alias;

    Button(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public static ReplyKeyboardMarkup createGeneralMenuKeyboard() {
        ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder keyboardBuilder = ReplyKeyboardMarkup.builder();
        keyboardBuilder.resizeKeyboard(true);
        keyboardBuilder.selective(true);

        keyboardBuilder.keyboardRow(new KeyboardRow(Arrays.asList(
                builder().text(
                    EXAMPLE.getAlias()).build()
                )));

        return keyboardBuilder.build();
    }

    public static boolean isValid (String text, List<Button> buttons) {
        return buttons.stream().anyMatch(b->b.getAlias().equals(text));
    }

    public static Button of (String text) {
        return Arrays.stream(values()).filter(
                b->b.getAlias().equals(text))
            .findFirst()
            .orElse(null);
    }

    public static ReplyKeyboard createMarkup(int maxWidth, List<com.gorges.bot.models.domain.Button> buttons) {
        List<KeyboardRow> rows = new ArrayList<>();
        int maxRows = (int) Math.ceil((float)buttons.size() / maxWidth);

        for (int i = 0; i < maxRows; i++) {
            KeyboardRow row = new KeyboardRow();
            for (int j = 0; j < maxWidth; j++) {
                com.gorges.bot.models.domain.Button button = buttons.get(maxRows*i+j);
                KeyboardButton keyboardButton = KeyboardButton.builder()
                    .text(button.getAlias())
                    .build();
                row.add(keyboardButton);
            }
            rows.add(row);
        }

        return ReplyKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

}
