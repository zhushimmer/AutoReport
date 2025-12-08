package io.zhushimmer.autoreportserver.utils;

public class Command {
    public static int run(ProcessBuilder pb) {
        try {
            pb.inheritIO();
            Process process = pb.start();
            return process.waitFor();
        } catch (Exception e) {
            return -1;
        }
    }
}
