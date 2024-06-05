package com.gorges.bot.handlers.commands;

import com.gorges.bot.handlers.ActionHandler;
import com.gorges.bot.handlers.CommandHandler;
import com.gorges.bot.handlers.commands.registries.CommandHandlerRegistry;
import com.gorges.bot.models.domain.Command;
import com.gorges.bot.models.domain.UserAction;
import com.gorges.bot.annotations.Handle;
import com.gorges.bot.repositories.memory.UserActionRepository;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Handle
public class EnterUrlCommandHandler implements CommandHandler, ActionHandler {

    private final CommandHandlerRegistry commandHandlerRegistry;
    private final UserActionRepository userActionRepository;

    public EnterUrlCommandHandler(CommandHandlerRegistry commandHandlerRegistry, UserActionRepository userActionRepository) {
        this.commandHandlerRegistry = commandHandlerRegistry;
        this.userActionRepository = userActionRepository;
    }

    public UserActionRepository getUserActionRepository() {
        return userActionRepository;
    }

    // todo check if replying to bot
    @Override
    public boolean canHandleAction(Update update, String specific) {
        return  hasAction(update, specific) &&
                (isYoutubeUrl(update.getMessage().getText()) ||
                isMagnetUrl(update.getMessage().getText()));
    }

    @Override
    public void executeCommand(AbsSender absSender, Update update, Long chatId) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text("Отправьте боту magnet-ссылку или ссылку на youtube видео")
            .build();

        absSender.execute(sendMessage);

        startAction(chatId);
    }

    @Override
    public void handleAction(AbsSender absSender, Update update, String action, Long chatId) throws TelegramApiException {
        String text = update.getMessage().getText();

        if (isYoutubeUrl(text))
            commandHandlerRegistry.find(Command.YOUTUBE).executeCommand(absSender, update, chatId);
        else if (isMagnetUrl(text))
            commandHandlerRegistry.find(Command.MAGNET).executeCommand(absSender, update, chatId);
        //else
        //    executeCommand(absSender, update, chatId);

        endAction(chatId);
    }

    @Override
    public Command getCommand() {
        return Command.ENTER_URL;
    }

    private boolean isMagnetUrl (String text) {
        return text != null && text.startsWith("magnet:?");
    }

    public boolean isYoutubeUrl (String text) {
        String pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";
        return text != null && !text.isEmpty() && text.matches(pattern);
    }
}
