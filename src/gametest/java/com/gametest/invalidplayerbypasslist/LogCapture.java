package com.gametest.invalidplayerbypasslist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LogCapture {
    private static final List<String> CAPTURED = new ArrayList<>();

    public static void init() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.Logger root =
                (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

        Appender appender = new AbstractAppender("BypassListCapture", null, null, false, null) {
            @Override
            public void append(LogEvent event) {
                CAPTURED.add(event.getMessage().getFormattedMessage());
            }
        };

        appender.start();
        root.addAppender(appender);
    }

    public static List<String> getCaptured() {
        return List.copyOf(CAPTURED);
    }

    public static boolean checkAndRemove(String contains) {
        synchronized (CAPTURED) {
            Iterator<String> it = CAPTURED.iterator();
            while (it.hasNext()) {
                String msg = it.next();
                if (msg.contains(contains)) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }
    }

    public static void clear() {
        synchronized (CAPTURED) {
            CAPTURED.clear();
        }
    }
}
