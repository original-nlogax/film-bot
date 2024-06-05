package com.gorges.bot;

import java.io.IOException;

import com.gorges.bot.LibraryLoader;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class NTgCalls {

    static {
        try {
            LibraryLoader.loadLibrary("ntgcalls");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // todo i suppose we need to change Video.input and Audio.input dynamically as file gets domnloaded?

    public NTgCalls () {
        System.out.println("NTgCalls Version: " + getVersion());
    }

    public enum InputMode {
        FILE (1 << 0),
        SHELL (1 << 1),
        FFMPEG (1 << 2),
        NO_LATENCY (1 << 3);

        private final int id;

        InputMode (int id) {
            this.id = id;
        }

        public int getId () {
            return id;
        }
    }

    public static class Media {
        public Audio audio;
        public Video video;

        public Media (Audio audio, Video video) {
            this.audio = audio;
            this.video = video;
        }
    }

    public static class Video {
        public int inputMode;
        public byte[] input;
        public int width, height;
        public byte fps;

        public Video (InputMode inputMode, String input, int width, int height, byte fps) {
            this.inputMode = inputMode.getId();
            this.input = (input+"\0").getBytes(StandardCharsets.UTF_8);
            this.width = width;
            this.height = height;
            this.fps = fps;

            System.out.println("Video{" + this.inputMode + ", " + this.width + "x" + this.height + ", " + this.fps + "} ");
        }
    }

    public static class Audio {
        public int inputMode;
        public byte[] input;
        public int sampleRate;
        public byte bitsPerSample, channelCount;

        public Audio (InputMode inputMode, String input, int sampleRate, byte bitsPerSample, byte channelCount) {
            this.inputMode = inputMode.getId();
            this.input = (input+"\0").getBytes(StandardCharsets.UTF_8);
            this.sampleRate = sampleRate;
            this.bitsPerSample = bitsPerSample;
            this.channelCount = channelCount;
        }

        @Override
        public String toString () {
            return "Audio{"
                + "inputMode=" + inputMode + ";"
                + "input=" + new String(input, StandardCharsets.UTF_8) + ";"
                + "sampleRate" + sampleRate + ";"
                + "channelCount=" + channelCount + ";"
                + "bitsPerSample=" + bitsPerSample + "}";
        }
    }

    public native int init ();

    public native byte[] getParams(int uid, long chatId, Media mediaDescription, byte[] buffer, int size);

    public native int connect (int uid, long chatId, byte[] params);

    public native int destroy (int uid);

    public native long time (int uid, long chatId);

    public native int pause (int uid, long chatId);

    public native int resume (int uid, long chatId);

    public native String getVersion ();

}
