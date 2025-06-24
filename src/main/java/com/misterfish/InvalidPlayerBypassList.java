package com.misterfish;

import com.misterfish.config.ModConfigs;
import com.misterfish.util.BypassListUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class InvalidPlayerBypassList implements ModInitializer {
    public static final String MOD_ID = "invalidPlayerBypassList";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean bypassList = true;

    @Override
    public void onInitialize() {
        ModConfigs.registerConfigs();

        LOGGER.info("InvalidPlayerBypassList initialized.");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });

        bypassList = ModConfigs.ENFORCE_BYPASSLIST;
    }

    public static boolean isInBypassList(String playername, String ip) {
        List<String> ipsForPlayer = BypassListUtil.getIpsForPlayer(playername);
        if (ipsForPlayer.isEmpty()) {
            return false;
        }

        if (ModConfigs.IP_REQUIRED) {
            if (ip.equalsIgnoreCase("none")) return false;

            for (String storedIp : ipsForPlayer) {
                if (storedIp.equalsIgnoreCase(ip)) {
                    return true;
                }
            }
            return false;
        } else {
            boolean hasNone = false;
            for (String storedIp : ipsForPlayer) {
                if (storedIp.equalsIgnoreCase("none")) {
                    hasNone = true;
                    break;
                }
            }

            if (hasNone) {
                return true;
            }

            for (String storedIp : ipsForPlayer) {
                if (storedIp.equalsIgnoreCase(ip)) {
                    return true;
                }
            }

            return false;
        }
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("bypasslist")
                .requires(source -> source.hasPermissionLevel(2))

                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .executes(ctx -> {
                                    if (ModConfigs.IP_REQUIRED) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("IP is required, please provide one."), false);
                                        return 0;
                                    }
                                    String player = StringArgumentType.getString(ctx, "player");
                                    boolean added = BypassListUtil.addPlayer(player, "none");
                                    if (added) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Added " + player + " to the bypass list."), false);
                                        LOGGER.info("[{}: Added {} with no IP to the bypasslist]", ctx.getSource().getName(), player);
                                    } else {
                                        ctx.getSource().sendFeedback(() -> Text.literal(player + " with IP none is already on the bypass list.").styled(style -> style.withColor(0xFF5555)), false);
                                    }
                                    return 1;
                                })
                                .then(CommandManager.argument("ip", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String player = StringArgumentType.getString(ctx, "player");
                                            String ip = StringArgumentType.getString(ctx, "ip");
                                            boolean added = BypassListUtil.addPlayer(player, ip);
                                            if (added) {
                                                ctx.getSource().sendFeedback(() -> Text.literal("Added " + player + " with IP " + ip + " to the bypass list."), false);
                                                LOGGER.info("[{}: Added {} with IP {} to the bypasslist]", ctx.getSource().getName(), player, ip);
                                            } else {
                                                ctx.getSource().sendFeedback(() -> Text.literal(player + " with IP " + ip + " is already on the bypass list.").styled(style -> style.withColor(0xFF5555)), false);
                                            }
                                            return 1;
                                        })
                                )
                        )
                )

                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String player : BypassListUtil.getAllPlayers()) {
                                        builder.suggest(player);
                                    }
                                    return builder.buildFuture();
                                })

                                .executes(ctx -> {
                                    String player = StringArgumentType.getString(ctx, "player");
                                    boolean removed = BypassListUtil.removePlayer(player);
                                    if (removed) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Removed all entries for " + player + " from the bypass list."), false);
                                        LOGGER.info("[{}: Removed all entries for {} from the bypasslist]", ctx.getSource().getName(), player);

                                    } else {
                                        ctx.getSource().sendFeedback(() -> Text.literal(player + " not found in the bypass list."), false);
                                    }
                                    return 1;
                                })

                                .then(CommandManager.argument("ip", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            String player = StringArgumentType.getString(ctx, "player");
                                            for (String ip : BypassListUtil.getIpsForPlayer(player)) {
                                                builder.suggest(ip);
                                            }
                                            return builder.buildFuture();
                                        })

                                        .executes(ctx -> {
                                            String player = StringArgumentType.getString(ctx, "player");
                                            String ip = StringArgumentType.getString(ctx, "ip");
                                            boolean removed = BypassListUtil.removePlayer(player, ip);
                                            if (removed) {
                                                ctx.getSource().sendFeedback(() -> Text.literal("Removed " + player + " with IP " + ip + " from the bypass list."), false);
                                                LOGGER.info("[{}: Removed {} with IP {} from the bypasslist]", ctx.getSource().getName(), player, ip);
                                            } else {
                                                ctx.getSource().sendFeedback(() -> Text.literal(player + " with IP " + ip + " not found in the bypass list."), false);
                                            }
                                            return 1;
                                        })
                                )
                        )
                )

                .then(CommandManager.literal("list")
                        .executes(ctx -> {
                            List<String> players = BypassListUtil.getAllPlayers();
                            if (players.isEmpty()) {
                                ctx.getSource().sendFeedback(() -> Text.literal("The bypass list is empty."), false);
                            } else {
                                StringBuilder list = new StringBuilder("Bypass list entries:\n");
                                for (String player : players) {
                                    List<String> ips = BypassListUtil.getIpsForPlayer(player);
                                    list.append("- ").append(player);
                                    if (!ips.isEmpty()) {
                                        list.append(" (").append(String.join(", ", ips)).append(")");
                                    }
                                    list.append("\n");
                                }
                                ctx.getSource().sendFeedback(() -> Text.literal(list.toString()), false);
                            }
                            return players.size();
                        })
                )

                .then(CommandManager.literal("on")
                        .executes(ctx -> {
                            if (bypassList) {
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("Bypass list is already enabled.")
                                                .styled(style -> style.withColor(0xFF5555)), false);
                                return 0;
                            }
                            bypassList = true;
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("Invalid player bypass list enabled."), false);
                            LOGGER.info("[{}: Bypasslist is now turned on]", ctx.getSource().getName());
                            return 1;
                        })
                )

                .then(CommandManager.literal("off")
                        .executes(ctx -> {
                            if (!bypassList) {
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("Bypass list is already disabled.")
                                                .styled(style -> style.withColor(0xFF5555)), false);
                                return 0;
                            }
                            bypassList = false;
                            ctx.getSource().sendFeedback(() ->
                                    Text.literal("Invalid player bypass list disabled."), false);
                            LOGGER.info("[{}: Bypasslist is now turned off]", ctx.getSource().getName());
                            return 1;
                        })
                )
        );
    }
}