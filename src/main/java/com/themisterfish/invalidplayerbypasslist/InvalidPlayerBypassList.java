package com.themisterfish.invalidplayerbypasslist;

import com.themisterfish.invalidplayerbypasslist.config.ModConfigs;
import com.themisterfish.invalidplayerbypasslist.util.BypassListUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class InvalidPlayerBypassList implements ModInitializer {
    public static final String MOD_ID = "invalidPlayerBypassList";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean bypassList = true;

    @Override
    public void onInitialize() {
        ModConfigs.registerConfigs();

        LOGGER.info("InvalidPlayerBypassList initialized.");

        CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, dedicated) -> {
            registerCommands(dispatcher);
        });

        bypassList = ModConfigs.ENFORCE_BYPASSLIST;
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("bypasslist")
                .requires(source -> Objects.requireNonNull(source.getPlayer())
                        .permissions()
                        .hasPermission(Permissions.COMMANDS_ADMIN))

                .then(literal("add")
                        .then(argument("player", StringArgumentType.word())
                                .executes(ctx -> {
                                    if (ModConfigs.IP_REQUIRED) {
                                        ctx.getSource().sendFailure(Component.literal("IP is required, please provide one."));
                                        return 0;
                                    }
                                    String player = StringArgumentType.getString(ctx, "player");
                                    boolean added = BypassListUtil.addPlayer(player, "none");
                                    if (added) {
                                        ctx.getSource().sendSuccess(() -> Component.literal("Added " + player + " to the bypass list."), false);
                                        LOGGER.info("[{}: Added {} with no IP to the bypasslist]", ctx.getSource().getDisplayName(), player);
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal(player + " with IP none is already on the bypass list.").withColor(0xFF5555));
                                    }
                                    return 1;
                                })
                                .then(argument("ip", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String player = StringArgumentType.getString(ctx, "player");
                                            String ip = StringArgumentType.getString(ctx, "ip");
                                            boolean added = BypassListUtil.addPlayer(player, ip);
                                            if (added) {
                                                ctx.getSource().sendSuccess(() -> Component.literal("Added " + player + " with IP " + ip + " to the bypass list."), false);
                                                LOGGER.info("[{}: Added {} with IP {} to the bypasslist]", ctx.getSource().getDisplayName(), player, ip);
                                            } else {
                                                ctx.getSource().sendFailure(Component.literal(player + " with IP " + ip + " is already on the bypass list.").withColor(0xFF5555));
                                            }
                                            return 1;
                                        })
                                )
                        )
                )

                .then(literal("remove")
                        .then(argument("player", StringArgumentType.word())
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
                                        ctx.getSource().sendSuccess(() -> Component.literal("Removed all entries for " + player + " from the bypass list."), false);
                                        LOGGER.info("[{}: Removed all entries for {} from the bypasslist]", ctx.getSource().getDisplayName(), player);

                                    } else {
                                        ctx.getSource().sendFailure(Component.literal(player + " not found in the bypass list."));
                                    }
                                    return 1;
                                })

                                .then(argument("ip", StringArgumentType.word())
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
                                                ctx.getSource().sendSuccess(() -> Component.literal("Removed " + player + " with IP " + ip + " from the bypass list."), false);
                                                LOGGER.info("[{}: Removed {} with IP {} from the bypasslist]", ctx.getSource().getDisplayName(), player, ip);
                                            } else {
                                                ctx.getSource().sendFailure(Component.literal(player + " with IP " + ip + " not found in the bypass list."));
                                            }
                                            return 1;
                                        })
                                )
                        )
                )

                .then(literal("list")
                        .executes(ctx -> {
                            List<String> players = BypassListUtil.getAllPlayers();
                            if (players.isEmpty()) {
                                ctx.getSource().sendSuccess(() -> Component.literal("The bypass list is empty."), false);
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
                                ctx.getSource().sendSuccess(() -> Component.literal(list.toString()), false);
                            }
                            return players.size();
                        })
                )

                .then(literal("on")
                        .executes(ctx -> {
                            if (bypassList) {
                                ctx.getSource().sendFailure(Component.literal("Bypass list is already enabled.").withColor(0xFF5555));
                                return 0;
                            }
                            bypassList = true;
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("Invalid player bypass list enabled."), false);
                            LOGGER.info("[{}: Bypasslist is now turned on]", ctx.getSource().getDisplayName());
                            return 1;
                        })
                )

                .then(literal("off")
                        .executes(ctx -> {
                            if (!bypassList) {
                                ctx.getSource().sendFailure(Component.literal("Bypass list is already disabled.").withColor(0xFF5555));
                                return 0;
                            }
                            bypassList = false;
                            ctx.getSource().sendSuccess(() -> Component.literal("Invalid player bypass list disabled."), false);
                            LOGGER.info("[{}: Bypasslist is now turned off]", ctx.getSource().getDisplayName());
                            return 1;
                        })
                )
        );
    }
}