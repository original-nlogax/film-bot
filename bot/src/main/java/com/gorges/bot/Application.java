package com.gorges.bot;

import com.gorges.bot.annotations.Handle;
import com.gorges.bot.core.TelegramBot;
import com.gorges.bot.core.Config;
import com.gorges.bot.handlers.ActionHandler;
import com.gorges.bot.handlers.CommandHandler;
import com.gorges.bot.handlers.Handler;
import com.gorges.bot.handlers.UpdateHandler;
import com.gorges.bot.handlers.commands.registries.CommandHandlerRegistry;
import com.gorges.bot.userbot.Userbot;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.lang.reflect.Constructor;
import java.util.*;

@Component
public class Application {

    public static final String DOWNLOADS_FOLDER = "downloads/";

    private List<CommandHandler> commandHandlers;
    private List<UpdateHandler> updateHandlers;
    private List<ActionHandler> actionHandlers;

    private static ApplicationContext ctx;

    private final CommandHandlerRegistry commandHandlerRegistry;

    public Application(CommandHandlerRegistry commandHandlerRegistry) {
        this.commandHandlerRegistry = commandHandlerRegistry;
    }

    public void initializeHandlers(Collection<Handler> handlers) {
        commandHandlers = new ArrayList<>();
        updateHandlers = new ArrayList<>();
        actionHandlers = new ArrayList<>();

        for (Handler handler : handlers) {
            if (handler instanceof ActionHandler)
                actionHandlers.add((ActionHandler) handler);
            if (handler instanceof CommandHandler)
                commandHandlers.add((CommandHandler) handler);
            if (handler instanceof UpdateHandler)
                updateHandlers.add((UpdateHandler) handler);
        }

        commandHandlerRegistry.setCommandHandlers(commandHandlers);
    }

    public static Collection<Handler> getHandlers() {
        return ctx.getBeansOfType(Handler.class).values();
    }

    public static void main(String[] args) throws TelegramApiException {
        //Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());

        ctx = new AnnotationConfigApplicationContext(Application.class.getPackageName());

        Config config = ctx.getBean(Config.class);

        Application app = ctx.getBean(Application.class);
        app.initializeHandlers(getHandlers());

        TelegramBot bot = ctx.getBean(TelegramBot.class, app.updateHandlers, app.actionHandlers);
        new TelegramBotsApi(DefaultBotSession.class).registerBot(bot);

        new Thread(() -> {
            try {
                Userbot.start(
                    Integer.parseInt(config.get("telegram.userbot.api_id")),
                    config.get("telegram.userbot.api_hash"),
                    Integer.parseInt(config.get("telegram.admin.id")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
