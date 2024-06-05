package com.gorges.bot.handlers.commands;

import com.gorges.bot.Application;
import com.gorges.bot.NTgCalls;
import com.gorges.bot.handlers.CommandHandler;
import com.gorges.bot.handlers.commands.registries.CommandHandlerRegistry;
import com.gorges.bot.models.domain.Command;
import com.gorges.bot.models.domain.MediaSource;
import com.gorges.bot.repositories.memory.UserActionRepository;
import com.gorges.bot.services.MovieService;
import com.gorges.bot.annotations.Handle;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Handle
public class YoutubeLinkCommandHandler implements CommandHandler {

    private final MovieService movieService;
    private final UserActionRepository userActionRepository;
    private final CommandHandlerRegistry commandHandlerRegistry;

    public YoutubeLinkCommandHandler(MovieService movieService, UserActionRepository userActionRepository, CommandHandlerRegistry commandHandlerRegistry) {
        this.movieService = movieService;
        this.userActionRepository = userActionRepository;
        this.commandHandlerRegistry = commandHandlerRegistry;
    }

    // todo fix sometimes yt-dlp add weird shit like .f308 to format despite it being
    // not present in yt video filename
    private void startYoutubeVideo (long chatId, String url) {
        final String title = movieService.getYtVideoTitle(url);
        String path = Application.DOWNLOADS_FOLDER + movieService.getYtVideoFilename(url);
        final int videoStreamId = 0;
        final int audioStreamId = 1;

        //MovieService.Movie movie = new MovieService.Movie(
        //    title, MediaSource.YOUTUBE, movieService.getYtVideoDuration(url));


        movieService.startYtDownload(url, path);

        System.out.println("Searching " + path);

        while (!Files.exists(Path.of(path))) {
            // wait until file appears
            System.out.print('.');
        }

        // wait until some starting data downloads (do we need it?)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<MovieService.MediaStream> mediaStreams = movieService.getMediaStreams(path);
        NTgCalls.Media mediaDesc = movieService.createCallMediaFromFile(
            mediaStreams.get(audioStreamId), mediaStreams.get(videoStreamId), path);

        MovieService.Movie movie = new MovieService.Movie(
            title, MediaSource.YOUTUBE, movieService.getYtVideoDuration(url));

        movieService.startMovie (movie, chatId, mediaDesc);
    }

    @Override
    public void executeCommand(AbsSender absSender, Update update, Long chatId) throws TelegramApiException {
        String url = update.getMessage().getText();

        startYoutubeVideo(chatId, url);

        commandHandlerRegistry.find(Command.CONTROLS).executeCommand(
            absSender, update, chatId);
    }

    @Override
    public Command getCommand() {
        return Command.YOUTUBE;
    }
}
