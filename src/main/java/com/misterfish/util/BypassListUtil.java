package com.misterfish.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.misterfish.InvalidPlayerBypassList.LOGGER;

public class BypassListUtil {
    private static final Path FILE_PATH = Path.of("invalidbypasslist.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<PlayerEntry> bypasslistCache = null;

    private static class PlayerEntry {
        String player;
        String ip;

        PlayerEntry(String player, String ip) {
            this.player = player.toLowerCase(Locale.ROOT);
            this.ip = ip.toLowerCase(Locale.ROOT);
        }
    }

    private static List<PlayerEntry> loadBypasslist() {
        if (bypasslistCache != null) return bypasslistCache;

        if (Files.notExists(FILE_PATH)) {
            bypasslistCache = new ArrayList<>();
            return bypasslistCache;
        }

        try (Reader reader = new FileReader(FILE_PATH.toFile())) {
            Type listType = new TypeToken<List<PlayerEntry>>() {
            }.getType();
            bypasslistCache = GSON.fromJson(reader, listType);
            if (bypasslistCache == null) {
                bypasslistCache = new ArrayList<>();
            }
            return bypasslistCache;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            bypasslistCache = new ArrayList<>();
            return bypasslistCache;
        }
    }

    private static void saveBypasslist() {
        if (bypasslistCache == null) return;

        try (Writer writer = new FileWriter(FILE_PATH.toFile())) {
            GSON.toJson(bypasslistCache, writer);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Adds a player + ip entry. If ip is null, stores as "none".
     * Returns true if added, false if duplicate player+ip found.
     */
    public static boolean addPlayer(String player, String ip) {
        if (ip == null) ip = "none";

        List<PlayerEntry> bypasslist = loadBypasslist();

        for (PlayerEntry entry : bypasslist) {
            if (entry.player.equalsIgnoreCase(player) && entry.ip.equalsIgnoreCase(ip)) {
                return false;
            }
        }

        bypasslist.add(new PlayerEntry(player, ip));
        saveBypasslist();
        return true;
    }

    /**
     * Remove entries by player name only (all IPs for that player)
     * Returns true if at least one was removed.
     */
    public static boolean removePlayer(String player) {
        List<PlayerEntry> bypasslist = loadBypasslist();

        boolean removedAny = bypasslist.removeIf(entry -> entry.player.equalsIgnoreCase(player));
        if (removedAny) saveBypasslist();
        return removedAny;
    }

    /**
     * Remove by player name AND ip (ip can be "none")
     * Returns true if found and removed, false otherwise.
     */
    public static boolean removePlayer(String player, String ip) {
        if (ip == null) ip = "none";

        List<PlayerEntry> bypasslist = loadBypasslist();

        Iterator<PlayerEntry> iter = bypasslist.iterator();
        while (iter.hasNext()) {
            PlayerEntry entry = iter.next();
            if (entry.player.equalsIgnoreCase(player) && entry.ip.equalsIgnoreCase(ip)) {
                iter.remove();
                saveBypasslist();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all unique player names currently in the bypasslist.
     */
    public static List<String> getAllPlayers() {
        List<PlayerEntry> bypasslist = loadBypasslist();

        return bypasslist.stream()
                .map(entry -> entry.player)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Returns all IPs associated with the given player.
     * IPs are unique and sorted.
     */
    public static List<String> getIpsForPlayer(String player) {
        List<PlayerEntry> bypasslist = loadBypasslist();

        return bypasslist.stream()
                .filter(entry -> entry.player.equalsIgnoreCase(player))
                .map(entry -> entry.ip)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}

