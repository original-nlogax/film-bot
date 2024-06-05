package com.gorges.bot.handlers.commands;

import com.gorges.bot.handlers.CommandHandler;
import com.gorges.bot.handlers.UpdateHandler;
import com.gorges.bot.models.domain.Button;
import com.gorges.bot.models.domain.Command;
import com.gorges.bot.services.MovieService;
import com.gorges.bot.utils.Utils;
import com.gorges.bot.annotations.Handle;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.TimeUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Handle
public class ControlsCommandHandler implements UpdateHandler, CommandHandler {

    private static final String CONTROLS_CALLBACK = "controls=";
    private final Map<Long, Integer> controlsMessages;
    private final Map<Long, Integer> lastMovieTimes;
    private final MovieService movieService;

    public ControlsCommandHandler (MovieService movieService) {
        this.movieService = movieService;
        this.controlsMessages = new HashMap<>();
        this.lastMovieTimes = new HashMap<>();
    }

	@Override
	public boolean canHandleUpdate(Update update) {
        return hasCommand (update) || hasCallback(update, CONTROLS_CALLBACK);
	}

    private boolean hasCommand (Update update) {
        return  update.hasMessage() &&
                update.getMessage().hasText() &&
                update.getMessage().getText().startsWith(Button.CONTROLS.getAlias());
    }

    private void startTimeService (AbsSender absSender, long chatId) {
        Runnable runnable = () -> {
            try {
                updateControls(absSender, chatId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(runnable, 0, 4, TimeUnit.SECONDS);
    }

	@Override
    public void handleUpdate(AbsSender absSender, Update update) throws TelegramApiException {
        if (hasCommand(update)) {
            long chatId = update.getMessage().getChatId();
            executeCommand(absSender, update, chatId);
        } else if (hasCallback(update, CONTROLS_CALLBACK)) {
            long chatId = update.getCallbackQuery().getMessage().getChat().getId();
            String callback = update.getCallbackQuery().getData();

            switch (callback) {
                case CONTROLS_CALLBACK + "resume" -> System.out.println("resume");//movieService.resume(chatId);
                case CONTROLS_CALLBACK + "pause" -> System.out.println("pause");//movieService.pause(chatId);
                case CONTROLS_CALLBACK + "forward" -> System.out.println("forward");
                case CONTROLS_CALLBACK + "backward" -> System.out.println("backward");
            }
        }
	}

    public void updateControls (AbsSender absSender, long chatId) throws TelegramApiException {
        if (controlsMessages.get(chatId) == null) {
            sendControls(absSender, chatId);
            return;
        }

        int currentTime = (int)movieService.getCurrentMovieTime(chatId);    // todo TEST WITH MULTIPLE GROUPS!!!
        int duration = movieService.getMovie(chatId).duration();

        if (lastMovieTimes.get(chatId) != null &&
            lastMovieTimes.get(chatId) == currentTime)
            return;

        lastMovieTimes.put(chatId, currentTime);

        EditMessageText editMessageText = EditMessageText.builder()
            .text(getTimeText(currentTime, duration))
            .chatId(chatId)
            .messageId(controlsMessages.get(chatId))
            .replyMarkup(createControlsMarkup(chatId))
            .build();

        absSender.execute(editMessageText);
    }

    private void sendControls (AbsSender absSender, long chatId) throws TelegramApiException {
        int currentTime = (int)movieService.getCurrentMovieTime(chatId);    // todo TEST WITH MULTIPLE GROUPS!!!
        int duration = movieService.getMovie(chatId).duration();

        SendMessage sendMessage = SendMessage.builder()
            .text(getTimeText(currentTime, duration))
            .replyMarkup(createControlsMarkup(chatId))
            .chatId(chatId)
            .build();

        Message controlsMessage = absSender.execute(sendMessage);
        controlsMessages.put(chatId, controlsMessage.getMessageId());
    }

    private String getTimeText (int current, int duration) {
        String currentHhmmss = Utils.formatTime(current * 1000L);
        String durationHhmmss = Utils.formatTime(duration * 1000L);

        return getMoonProgressbar (current, duration, 13) + "\n" + currentHhmmss + " / " + durationHhmmss;
    }

    private String getMoonProgressbar (int current, int duration, int length) {
        final String[] emojis = new String[] {
            // watched 🌕🌖🌗🌘🌑 unwatched
            "\uD83C\uDF15",
            "\uD83C\uDF16",
            "\uD83C\uDF17",
            "\uD83C\uDF18",
            "\uD83C\uDF11"
        };

        if (current < 0) System.out.println(current);
        if (duration <= 0) return emojis[emojis.length-1].repeat(length);
        if (current >= duration) return emojis[0].repeat(length);

        int timePerEmoji = Math.round((float)duration/length);
        float remainder = current%timePerEmoji;

        String watchedEmojis = emojis[0]
            .repeat(current/timePerEmoji);
        String currentEmoji = emojis[(emojis.length-1)-(int)(emojis.length*(remainder/timePerEmoji))];
        String unwatchedEmojis = emojis[emojis.length-1]
            .repeat(duration/timePerEmoji - current/timePerEmoji - 1);

        return watchedEmojis + currentEmoji + unwatchedEmojis;
    }

	@Override
	public Command getCommand () {
		return Command.CONTROLS;
	}

    private InlineKeyboardMarkup createControlsMarkup (long chatId) {
        return InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(
                createNoopButton(),
                InlineKeyboardButton.builder()
                    .text("-30s️")
                    .callbackData(CONTROLS_CALLBACK + "backward")
                    .build(),
                InlineKeyboardButton.builder()
                    .text(movieService.isPaused(chatId) ? "▶️" : "⏸")
                    .callbackData(CONTROLS_CALLBACK +
                        (movieService.isPaused(chatId) ? "resume" : "pause"))
                    .build(),
                InlineKeyboardButton.builder()
                    .text("+30s️")
                    .callbackData(CONTROLS_CALLBACK + "forward")
                    .build(),
                createNoopButton()
            ))
            .build();
    }

    private InlineKeyboardButton createNoopButton () {
        return InlineKeyboardButton.builder()
            .text(" ")
            .callbackData("noop")
            .build();
    }

    @Override
    public void executeCommand(AbsSender absSender, Update update, Long chatId) throws TelegramApiException {
        if (controlsMessages.get(chatId) != null) {
            DeleteMessage deleteMessage = DeleteMessage.builder()
                .messageId(controlsMessages.get(chatId))
                .chatId(chatId)
                .build();

            absSender.execute(deleteMessage);

            sendControls(absSender, chatId);
            return;
        }

        startTimeService(absSender, chatId);
    }
}
