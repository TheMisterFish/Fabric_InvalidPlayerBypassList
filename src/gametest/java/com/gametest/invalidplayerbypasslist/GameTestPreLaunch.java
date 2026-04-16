package com.gametest.invalidplayerbypasslist;

import com.themisterfish.invalidplayerbypasslist.util.BypassListUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameTestPreLaunch implements PreLaunchEntrypoint {
    public static final Logger LOGGER = LoggerFactory.getLogger("GameTestPreLaunch");

    @Override
    public void onPreLaunch() {
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            BypassListUtil.getAllPlayers().forEach(s -> {
                LOGGER.info("Deleting {} from old bypass list", s);
                BypassListUtil.removePlayer(s);
            });

            LogCapture.init();
        });
    }
}
