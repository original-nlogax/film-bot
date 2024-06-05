package com.gorges.bot.handlers.commands;


import com.gorges.bot.core.Config;
import com.gorges.bot.handlers.ActionHandler;
import com.gorges.bot.handlers.UpdateHandler;
import com.gorges.bot.handlers.commands.registries.CommandHandlerRegistry;
import com.gorges.bot.models.domain.Button;
import com.gorges.bot.models.domain.Command;
import com.gorges.bot.utils.Utils;
import com.gorges.bot.annotations.Handle;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

// todo fix yt-dlp api warnings: update (https://github.com/yt-dlp/yt-dlp/issues/9316)

@Handle
public class StartCommandHandler implements UpdateHandler {

    private final Config config;
    private final CommandHandlerRegistry commandHandlerRegistry;

    public StartCommandHandler(Config config,
                               CommandHandlerRegistry commandHandlerRegistry) {
        this.config = config;
        this.commandHandlerRegistry = commandHandlerRegistry;
    }

    @Override
    public Command getCommand() {
        return Command.START;
    }

    @Override
    public boolean canHandleUpdate(Update update) {
        return  update.hasMessage() &&
                update.getMessage().hasText() &&
                update.getMessage().getText().startsWith(Button.START.getAlias());
    }

    @Override
    public void handleUpdate(AbsSender absSender, Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();

        commandHandlerRegistry.find(Command.ENTER_URL)
            .executeCommand(absSender, update, chatId);
    }
}
