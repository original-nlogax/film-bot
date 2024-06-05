package com.gorges.bot.utils;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class Utils {

    public static boolean isText (String text) {
        return matches (text, "[a-zA-Z]|[U+0400–U+04FF]");
    }

    public static boolean isNumber (String text) {
        return matches (text, "[0-9]+$");
    }

    public static boolean matches (String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(text).find();
    }

    public static String formatTime(long millis) {
        long secs = millis / 1000;
        return String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60);
    }

    public static String getFileExtension (String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0) return "." + filename.substring(i+1);
        return "exterror";
    }

    public static String getFileWithoutExtension (String filename) {
        return filename.replace(getFileExtension(filename), "");
    }

    public static boolean isValidURL (String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
