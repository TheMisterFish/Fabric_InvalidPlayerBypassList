package com.misterfish.config;

import com.mojang.datafixers.util.Pair;

import static com.misterfish.InvalidPlayerBypassList.MOD_ID;

public class ModConfigs {
    public static SimpleConfig CONFIG;
    private static ModConfigProvider configs;

    public static boolean IP_REQUIRED;
    public static boolean ENFORCE_BYPASSLIST;

    public static void registerConfigs() {
        configs = new ModConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(MOD_ID).provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("ip-required", true), "If IP is required when adding a player to the invalid player bypass list.");
        configs.addKeyValuePair(new Pair<>("enforce-bypasslist", true), "Enforce the invalid player bypass list.");

    }

    private static void assignConfigs() {
        IP_REQUIRED = CONFIG.getOrDefault("ip-required", true);
        ENFORCE_BYPASSLIST = CONFIG.getOrDefault("enforce-bypasslist", true);
    }
}