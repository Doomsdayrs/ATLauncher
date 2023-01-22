package com.atlauncher.managers;

import java.util.ArrayList;

/**
 * 20 / 01 / 2023
 */
public class NotificationManager {
    private static ArrayList<String> messages = new ArrayList<String>();
    private static final int delay = 500;
    private static boolean isRunning = false;

    public static void enqueue(String message) {
        messages.add(message);
        if (!isRunning) {
            isRunning = true;
            new Thread(new NotificationRun());
        }
    }

    public synchronized static void popupNow(String message) {

    }

    public synchronized static void notifyNow(String message) {

    }

    private static class NotificationRun implements Runnable {

        @Override
        public void run() {
            long timeStamp = 0;
            while (messages.size() > 0) {
                long millis = System.currentTimeMillis();
                if (timeStamp + delay < millis) {
                    timeStamp = millis;
                    String message = messages.get(0);

                }
            }
            isRunning = false;
        }
    }
}
