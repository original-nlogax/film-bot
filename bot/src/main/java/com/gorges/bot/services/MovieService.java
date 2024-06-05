package com.gorges.bot.services;

import com.gorges.bot.Application;
import com.gorges.bot.NTgCalls;
import com.gorges.bot.models.domain.MediaSource;
import com.gorges.bot.utils.Utils;
import com.gorges.bot.userbot.Userbot;

import it.tdlight.jni.TdApi;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MovieService {

    // todo do something with it...
    public static final String YTDLP_FFMPEG_PATH = "../yt-dlp-ffmpeg/ffmpeg-n6.1-latest-linux64-gpl-6.1/bin/ffmpeg";

    private final Map<Long, Integer> groupCallIds = new HashMap<>();
    private final Map<Long, Integer> groupTgCallUids = new HashMap<>();
    private final Map<Long, Movie> groupMovies = new HashMap<>();

    private final NTgCalls ntgcalls;

    public MovieService(NTgCalls ntgcalls) {
        this.ntgcalls = ntgcalls;
    }

	private void runProcess (String exec, boolean printIo) {
        System.out.println(exec);

		final ProcessBuilder pb = new ProcessBuilder("sh", "-c", exec);

        if (printIo) pb.inheritIO();

        try {
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private String runProcessAndRead (String exec) {
		System.out.println(exec);

		final ProcessBuilder pb = new ProcessBuilder("sh", "-c", exec);

		Process process = null;
        try {
            process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line;

		try {
			boolean firstLine = true;
			while ((line = reader.readLine()) != null) {
				if (!firstLine) {
					builder.append(System.getProperty("line.separator"));
				} else {
					firstLine = false;
				}

				builder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String result = builder.toString();
		return result;
	}

    public int getFileVideoDuration (String path) {
        return 86400;
    }

    // get magnet file title from the magnet url
    public String getMagnetTitle (String magnet) {
        return Arrays.stream(magnet.split("&"))
            .filter(s->s.startsWith("dn="))
            .map(s->s.replace("dn=", ""))
            .map(s-> URLDecoder.decode(s, StandardCharsets.UTF_8))
            .findFirst().orElse(null);
    }

    public String getTorrentVideoDownloadName (File torrentFile) {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(torrentFile)));

            final String[] metadata = reader.readLine().split(":");
            int nameFieldIdx = -1;
            for (int i = 0; i < metadata.length; i++) {
                if (metadata[i].startsWith("name")) {
                    nameFieldIdx = i;
                    break;
                }
            }

            if (nameFieldIdx == -1 || nameFieldIdx+1>metadata.length-1)
                throw new IllegalArgumentException("No filename in torrent file");
                // todo handle

            return metadata[nameFieldIdx+1].replace("12", "");  // some random "12" at the end?
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getTorrentFile (String magnet) {
        final String exec = "webtorrent downloadmeta \"" + magnet + "\""
            + " --out " + Application.DOWNLOADS_FOLDER;

        runProcessAndRead(exec);

        // todo replace symbols in extended magnets
        return new File(Application.DOWNLOADS_FOLDER
            + magnet.replace("magnet:?xt=urn:btih:", "").toLowerCase() + ".torrent");
    }

    public void startMagnetDownload (String magnet) {
        final String exec = "webtorrent \"" + magnet + "\""
             + " --out " + Application.DOWNLOADS_FOLDER;

        Thread thread = new Thread(() -> runProcess(exec, true));
        thread.start();
    }

    public void startYtDownload (String url, String output) {
        final String exec = "yt-dlp "
            + " -o \"" + output + "\""
            //+ " -f bestvideo[ext=mp4][vcodec!=vp9]+bestaudio[ext=m4a]"
            + " --ffmpeg-location \"" + YTDLP_FFMPEG_PATH + "\""
            + " " + url + " --no-part --downloader ffmpeg";  // ffmpeg lets us to mux audio and video while not fully downloaded
            // "-o -" -- can't pipe to stdout when live muxing

        Thread thread = new Thread(() -> runProcess(exec, false));
        thread.start();
    }

    public String getYtVideoFilename (String url) {
        final String exec = "yt-dlp "
            + " --simulate --print filename --no-warnings "
            + url;

        return runProcessAndRead(exec);
    }

    public String getYtVideoTitle (String url) {
        final String exec = "yt-dlp "
            + " --simulate --print \"%(title)s\" --no-warnings "
            + url;

        return runProcessAndRead(exec);
    }

    public int getYtVideoDuration (String url) {
        final String exec = "yt-dlp "
            + " --simulate --print \"%(duration)s\" --no-warnings "
            + url;

        return Integer.parseInt(runProcessAndRead(exec));
    }

    public int getMediaDuration (List<MediaStream> mediaStreams) {
        int maxDuration = 0;

        for (MediaStream stream : mediaStreams) {
            if (stream.has("duration") && stream.getFloat("duration") > maxDuration)
                maxDuration = (int)stream.getFloat("duration");
        }

        return maxDuration;
    }

	public List<MediaStream> getMediaStreams(String input) {
		final String exec = "ffprobe -v error -show_entries stream \"" + input + "\"";

		String cmdResult = runProcessAndRead(exec);

		List<MediaStream> mediaStreams = new ArrayList<>();

		Map<String, String> streamEntries = new HashMap<>();
		for (String line : cmdResult.split(System.getProperty("line.separator"))) {
			if (line.equals("[STREAM]"))
				streamEntries = new HashMap<>();
			else if (line.equals("[/STREAM]"))
				mediaStreams.add(new MediaStream(streamEntries));

			else if (line.contains("=") && line.split("=").length >= 2) {
				String key = line.split("=")[0];
				String value = line.split("=")[1];
				streamEntries.put(key, value);
			}
		}

        System.out.println(mediaStreams);

		return mediaStreams;
	}

	public NTgCalls.Media createCallMediaFromFile (MediaStream audioStream, MediaStream videoStream,
                                                   String path) {
        // todo using rawvideo as audio produces cool results; experiment with it
        // (for example with some fast-flickering video)
        //       audioPath = videoPath;

        byte bits_per_sample = (byte)16; // todo fix ? bit depth = 0 in AAC?

        NTgCalls.Audio audio = new NTgCalls.Audio (
            NTgCalls.InputMode.SHELL,
            "ffmpeg -i \"" + path + "\""
                + " -loglevel panic -acodec pcm_s16le -f s16le "
                + " -ar " + audioStream.getInt("sample_rate")
                + " -ac " + audioStream.getByte("channels")
                + " -",
            audioStream.getInt("sample_rate"),
            bits_per_sample,
            audioStream.getByte("channels")
        );

        NTgCalls.Video video = new NTgCalls.Video (
            NTgCalls.InputMode.SHELL,
            "ffmpeg -i \"" + path + "\""
                + " -f rawvideo "
                + " -r " + videoStream.getFramerate()
                + " -vf scale=" + videoStream.getInt("width") + ":" + videoStream.getInt("height")
                + " -pix_fmt yuv420p -",
            videoStream.getInt("width"),
            videoStream.getInt("height"),
            videoStream.getFramerate()
        );

        return new NTgCalls.Media(audio, video);
    }

    public long getCurrentMovieTime (long chatId) {
        int uid = 0; // todo uid
        return ntgcalls.time(uid, chatId);
    }

    public void pause (long chatId) {
        int uid = groupTgCallUids.get(chatId);
        System.out.println("Pause...");
        int result = ntgcalls.pause(uid, chatId);
        System.out.println(result);

    }

    public void resume (long chatId) {
        int uid = groupTgCallUids.get(chatId);
        System.out.println("Resume...");
        int result = ntgcalls.resume(uid, chatId);
        System.out.println(result);
    }

    public boolean isPaused (long chatId) {
        return false;
    }

	public void startMovie (Movie movie, long chatId, NTgCalls.Media mediaDesc) {

        System.out.println("Chat id: " + chatId);

        TdApi.CreateVideoChat createVideoChat = new TdApi.CreateVideoChat();
        createVideoChat.chatId = chatId;
        createVideoChat.title = movie.title();
        TdApi.GroupCallId groupCallId = Userbot.getApp().getClient().send(createVideoChat).join();
        groupCallIds.put(chatId, groupCallId.id);

        System.out.println("Starting the movie " + movie.title());
        groupMovies.put(chatId, movie);

        int uid = ntgcalls.init();
        groupTgCallUids.put(chatId, uid);
        System.out.println("uid: " + uid);

        // todo implement size in getParams; program will break if we don't
        // define byte array prior to getParams
        byte[] params = new byte[512];
        params = ntgcalls.getParams(uid, chatId, mediaDesc, params, -1);

        String paramsJson = new String(params, StandardCharsets.UTF_8);

        TdApi.JoinGroupCall joinGroupCall = new TdApi.JoinGroupCall();
        joinGroupCall.audioSourceId = new Random().nextInt(Integer.MAX_VALUE);
        joinGroupCall.groupCallId = groupCallId.id; // chat.videoChat.groupCallId;
        joinGroupCall.isMyVideoEnabled = true;
        joinGroupCall.payload = paramsJson;
        TdApi.Text responseParams = Userbot.getApp().getClient().send(joinGroupCall).join();

        //System.out.println("Connect params:");
        //System.out.println(responseParams.text);

        int connect = ntgcalls.connect(uid, chatId, (responseParams.text+"\0").getBytes(StandardCharsets.UTF_8));
        if (connect < 0) {
            System.out.println("Failed to connect to WebRTC. \n" + connect);
            return;
        }

		System.out.println("Connected");

    }

    public void stopMovie (long chatId) {
        TdApi.LeaveGroupCall leaveGroupCall = new TdApi.LeaveGroupCall();
        leaveGroupCall.groupCallId = groupCallIds.get(chatId);
        Userbot.getApp().getClient().send(leaveGroupCall);

        int destroy = ntgcalls.destroy (groupTgCallUids.get(chatId));
        if (destroy != 0) {
            System.out.println("NTgCalls cleanup failed. (" + destroy + ")");
        }

        groupCallIds.remove(chatId);
        groupTgCallUids.remove(chatId);
        groupMovies.remove(chatId);
    }

    public Movie getMovie(long chatId) {
        return groupMovies.get(chatId);
    }

    public record Movie(String title, MediaSource source, int duration) { }
}
