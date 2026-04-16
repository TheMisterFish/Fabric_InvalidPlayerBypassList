package com.gametest.invalidplayerbypasslist.test;

import com.gametest.invalidplayerbypasslist.LogCapture;
import com.themisterfish.invalidplayerbypasslist.util.BypassListUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.PermissionSet;

import java.util.List;
import java.util.Objects;

public class BypassListRemoveCommandTest {

    private CommandSourceStack opSource(MinecraftServer server) {
        return server.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS);
    }

    private CommandSourceStack noPermSource(MinecraftServer server) {
        return server.createCommandSourceStack().withPermission(PermissionSet.NO_PERMISSIONS);
    }

    @GameTest
    public void testRemovePlayerAllIps(GameTestHelper testContext) {
        BypassListUtil.addPlayer("removeAll", "1.1.1.1");
        BypassListUtil.addPlayer("removeAll", "2.2.2.2");

        MinecraftServer server = Objects.requireNonNull(testContext.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist remove removeAll");

        testContext.assertValueEqual(
                0,
                BypassListUtil.getIpsForPlayer("removeAll").size(),
                Component.literal("All IPs removed")
        );

        testContext.succeed();
    }

    @GameTest
    public void testRemovePlayerSpecificIp(GameTestHelper testContext) {
        BypassListUtil.addPlayer("removeOne", "1.1.1.1");
        BypassListUtil.addPlayer("removeOne", "2.2.2.2");

        MinecraftServer server = Objects.requireNonNull(testContext.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist remove removeOne 1.1.1.1");

        List<String> ips = BypassListUtil.getIpsForPlayer("removeOne");

        testContext.assertValueEqual(1, ips.size(), Component.literal("One IP remains"));
        testContext.assertValueEqual("2.2.2.2", ips.getFirst(), Component.literal("Correct IP remains"));

        BypassListUtil.removePlayer("removeOne");
        testContext.succeed();
    }

    @GameTest
    public void testRemovePlayerNotFound(GameTestHelper testContext) {
        MinecraftServer server = Objects.requireNonNull(testContext.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist remove doesNotExist");

        testContext.assertFalse(
                BypassListUtil.getAllPlayers().contains("doesNotExist"),
                Component.literal("Player doesn't exist")
        );

        testContext.assertTrue(
                LogCapture.checkAndRemove("doesNotExist not found in the bypass list."),
                Component.literal("Expected log for removing non existing player")
        );

        testContext.succeed();
    }

    @GameTest
    public void testRemovePlayerNotFoundWithIp(GameTestHelper testContext) {
        MinecraftServer server = Objects.requireNonNull(testContext.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist remove doesNotExistWithIp 1.1.1.1");

        testContext.assertFalse(
                BypassListUtil.getAllPlayers().contains("doesNotExistWithIp"),
                Component.literal("Player doesn't exist")
        );

        testContext.assertTrue(
                LogCapture.checkAndRemove("doesNotExistWithIp with IP 1.1.1.1 not found in the bypass list."),
                Component.literal("Expected log for removing non existing player")
        );

        testContext.succeed();
    }

    @GameTest
    public void testRemovePlayerNoOp(GameTestHelper testContext) {
        BypassListUtil.addPlayer("noOpRemove", "1.1.1.1");

        MinecraftServer server = Objects.requireNonNull(testContext.getLevel().getServer());
        CommandSourceStack source = noPermSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist remove noOpRemove");

        testContext.assertValueEqual(
                1,
                BypassListUtil.getIpsForPlayer("noOpRemove").size(),
                Component.literal("Entry not removed")
        );

        BypassListUtil.removePlayer("noOpRemove");
        testContext.succeed();
    }

    @GameTest
    public void testRemovePlayerSuggestions(GameTestHelper testContext) {
        BypassListUtil.addPlayer("removeAll", "1.1.1.1");
        BypassListUtil.addPlayer("removeOne", "2.2.2.2");

        MinecraftServer server = testContext.getLevel().getServer();
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
        CommandSourceStack source = opSource(server);

        ParseResults<CommandSourceStack> parse = dispatcher.parse("bypasslist remove r", source);
        Suggestions suggestions = dispatcher.getCompletionSuggestions(parse).join();

        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("removeall")),
                Component.literal("Expected suggestion: removeAll")
        );

        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("removeone")),
                Component.literal("Expected suggestion: removeOne")
        );

        BypassListUtil.removePlayer("removeAll");
        BypassListUtil.removePlayer("removeOne");
        testContext.succeed();
    }

    @GameTest
    public void testRemovePlayerIpSuggestions(GameTestHelper testContext) {
        BypassListUtil.addPlayer("removeOne", "1.1.1.1");
        BypassListUtil.addPlayer("removeOne", "1.2.3.4");

        MinecraftServer server = testContext.getLevel().getServer();
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
        CommandSourceStack source = opSource(server);

        ParseResults<CommandSourceStack> parse = dispatcher.parse("bypasslist remove removeOne 1", source);
        Suggestions suggestions = dispatcher.getCompletionSuggestions(parse).join();

        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("1.1.1.1")),
                Component.literal("Expected suggestion: 1.1.1.1")
        );

        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("1.2.3.4")),
                Component.literal("Expected suggestion: 1.2.3.4")
        );

        BypassListUtil.removePlayer("removeOne");
        testContext.succeed();
    }
}
