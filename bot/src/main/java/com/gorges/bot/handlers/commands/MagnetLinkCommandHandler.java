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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Handle
public class MagnetLinkCommandHandler implements CommandHandler {

    private final MovieService movieService;
    private final UserActionRepository userActionRepository;
    private final CommandHandlerRegistry commandHandlerRegistry;

    public MagnetLinkCommandHandler(MovieService movieService, UserActionRepository userActionRepository, CommandHandlerRegistry commandHandlerRegistry) {
        this.movieService = movieService;
        this.userActionRepository = userActionRepository;
        this.commandHandlerRegistry = commandHandlerRegistry;
    }

    private void startMagnetVideo (long chatId, String magnet) {
        final File torrentFile = movieService.getTorrentFile(magnet);

        System.out.println("torrent path: " + torrentFile.getAbsolutePath());

        final String path = Application.DOWNLOADS_FOLDER +
                movieService.getTorrentVideoDownloadName(torrentFile);

        System.out.println("video path (searching in): " + path);
        //final String path = movieService.getTorrentVideoDownloadName(torrentFile);
        final String title = new File(path).getName();
        final int videoStreamId = 0;
        final int audioStreamId = 1;

        movieService.startMagnetDownload(magnet);


        while (!Files.exists(Path.of(path))) {
            // wait until file appears
            //System.out.print('.');
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

        // if video stream has N/A duration, we get duration of one of available streams
        int duration =    mediaStreams.get(videoStreamId).has("duration") ?
                    (int) mediaStreams.get(videoStreamId).getFloat("duration")
                        : movieService.getMediaDuration(mediaStreams);

        MovieService.Movie movie = new MovieService.Movie(
            title, MediaSource.TORRENT, duration);

        movieService.startMovie (movie, chatId, mediaDesc);
    }

    @Override
    public void executeCommand(AbsSender absSender, Update update, Long chatId) throws TelegramApiException {
        String text = update.getMessage().getText();

        startMagnetVideo (chatId, text);

        commandHandlerRegistry.find(Command.CONTROLS).executeCommand(
            absSender, update, chatId);
    }

    @Override
    public Command getCommand() {
        return Command.MAGNET;
    }

}
