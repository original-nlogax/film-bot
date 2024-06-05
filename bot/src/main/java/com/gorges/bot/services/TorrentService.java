package com.gorges.bot.services;

import com.gorges.bot.Application;
import it.tdlight.jni.TdApi;

import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
public class TorrentService {

    private void runProcess (String exec, boolean printIo) {
        System.out.println(exec);

        final ProcessBuilder pb = new ProcessBuilder(exec.split(" "));

        if (printIo) pb.inheritIO();

        try {
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
