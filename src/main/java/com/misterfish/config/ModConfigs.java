package com.misterfish.config;

import com.mojang.datafixers.util.Pair;

import static com.misterfish.InvalidPlayerBypassList.MOD_ID;

public class ModConfigs {
    public static SimpleConfig CONFIG;
    private static ModConfigProvider configs;

    public static boolean IP_REQUIRED;

    public static void registerConfigs() {
        configs = new ModConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(MOD_ID).provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("ipRequired", true), "If IP is required when adding a player to the invalid player bypass list.");
    }

    private static void assignConfigs() {
        IP_REQUIRED = CONFIG.getOrDefault("ipRequired", true);
    }
}