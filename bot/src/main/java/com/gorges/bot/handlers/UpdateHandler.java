package com.gorges.bot.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface UpdateHandler extends Handler {

    boolean canHandleUpdate(Update update);

    void handleUpdate(AbsSender absSender, Update update) throws TelegramApiException;

    default boolean hasCallback(Update update, String callback) {
        return update.hasCallbackQuery()
            && update.getCallbackQuery().getData().startsWith(callback);
    }

}
