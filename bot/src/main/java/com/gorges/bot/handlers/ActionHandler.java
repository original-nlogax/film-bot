package com.gorges.bot.handlers;

import com.gorges.bot.models.domain.Command;
import com.gorges.bot.models.domain.UserAction;
import com.gorges.bot.repositories.memory.UserActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface ActionHandler extends Handler {

    UserActionRepository getUserActionRepository();

    boolean canHandleAction(Update update, String specific);

    void handleAction(AbsSender absSender, Update update, String action, Long chatId) throws TelegramApiException;

    default void startAction (long chatId, String specific) {
        getUserActionRepository().updateByChatId(chatId, new UserAction(getCommand(), specific));
    }

    default void startAction (long chatId) {
        startAction(chatId, "");
    }

    default void endAction (long chatId) {
        getUserActionRepository().deleteByChatId(chatId);
    }

    default boolean hasAction (Update update, String specific) {
        return getUserActionRepository().findByChatId(update.getMessage().getChatId())
            .getSpecificAction().equals(specific);
    }
}
