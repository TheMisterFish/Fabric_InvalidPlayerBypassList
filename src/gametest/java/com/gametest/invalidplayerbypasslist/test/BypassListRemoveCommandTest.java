package com.gametest.invalidplayerbypasslist.test;

import com.gametest.invalidplayerbypasslist.LogCapture;
import com.invalidplayerbypasslist.util.BypassListUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public class BypassListRemoveCommandTest {

    @GameTest
    public void testRemovePlayerAllIps(TestContext testContext) {
        BypassListUtil.addPlayer("removeAll", "1.1.1.1");
        BypassListUtil.addPlayer("removeAll", "2.2.2.2");

        ServerCommandSource source = Objects.requireNonNull(testContext.getWorld().getServer())
                .getCommandSource()
                .withPermissions(PermissionPredicate.ALL);

        testContext.getWorld().getServer().getCommandManager().parseAndExecute(
                source,
                "bypasslist remove removeAll"
        );

        testContext.assertEquals(0, BypassListUtil.getIpsForPlayer("removeAll").size(), Text.of("All IPs removed"));
        testContext.complete();
    }

    @GameTest
    public void testRemovePlayerSpecificIp(TestContext testContext) {
        BypassListUtil.addPlayer("removeOne", "1.1.1.1");
        BypassListUtil.addPlayer("removeOne", "2.2.2.2");

        ServerCommandSource source = Objects.requireNonNull(testContext.getWorld().getServer())
                .getCommandSource()
                .withPermissions(PermissionPredicate.ALL);

        testContext.getWorld().getServer().getCommandManager().parseAndExecute(
                source,
                "bypasslist remove removeOne 1.1.1.1"
        );

        List<String> ips = BypassListUtil.getIpsForPlayer("removeOne");
        testContext.assertEquals(1, ips.size(), Text.of("One IP remains"));
        testContext.assertEquals("2.2.2.2", ips.getFirst(), Text.of("Correct IP remains"));

        BypassListUtil.removePlayer("removeOne");
        testContext.complete();
    }

    @GameTest
    public void testRemovePlayerNotFound(TestContext testContext) {
        ServerCommandSource source = Objects.requireNonNull(testContext.getWorld().getServer())
                .getCommandSource()
                .withPermissions(PermissionPredicate.ALL);

        testContext.getWorld().getServer().getCommandManager().parseAndExecute(
                source,
                "bypasslist remove doesNotExist"
        );

        testContext.assertFalse(BypassListUtil.getAllPlayers().contains("doesNotExist"), Text.of("Player doesn't exist"));
        testContext.assertTrue(
                LogCapture.checkAndRemove("doesNotExist not found in the bypass list."),
                Text.of("Expected log for removing non existing player")
        );

        testContext.complete();
    }

    @GameTest
    public void testRemovePlayerNotFoundWithIp(TestContext testContext) {
        ServerCommandSource source = Objects.requireNonNull(testContext.getWorld().getServer())
                .getCommandSource()
                .withPermissions(PermissionPredicate.ALL);

        testContext.getWorld().getServer().getCommandManager().parseAndExecute(
                source,
                "bypasslist remove doesNotExistWithIp 1.1.1.1"
        );

        testContext.assertFalse(BypassListUtil.getAllPlayers().contains("doesNotExistWithIp"), Text.of("Player doesn't exist"));
        testContext.assertTrue(
                LogCapture.checkAndRemove("doesNotExistWithIp with IP 1.1.1.1 not found in the bypass list."),
                Text.of("Expected log for removing non existing player")
        );

        testContext.complete();
    }

    @GameTest
    public void testRemovePlayerNoOp(TestContext testContext) {
        BypassListUtil.addPlayer("noOpRemove", "1.1.1.1");

        ServerCommandSource source = Objects.requireNonNull(testContext.getWorld().getServer())
                .getCommandSource()
                .withPermissions(PermissionPredicate.NONE);

        testContext.getWorld().getServer().getCommandManager().parseAndExecute(
                source,
                "bypasslist remove noOpRemove"
        );

        testContext.assertEquals(1, BypassListUtil.getIpsForPlayer("noOpRemove").size(), Text.of("Entry not removed"));
        BypassListUtil.removePlayer("noOpRemove");
        testContext.complete();
    }

    @GameTest
    public void testRemovePlayerSuggestions(TestContext testContext) {
        BypassListUtil.addPlayer("removeAll", "1.1.1.1");
        BypassListUtil.addPlayer("removeOne", "2.2.2.2");

        MinecraftServer server = testContext.getWorld().getServer();
        CommandDispatcher<ServerCommandSource> dispatcher = Objects.requireNonNull(server).getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource().withPermissions(PermissionPredicate.ALL);

        ParseResults<ServerCommandSource> parse = dispatcher.parse("bypasslist remove r", source);
        Suggestions suggestions = dispatcher.getCompletionSuggestions(parse).join();

        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("removeall")),
                Text.of("Expected suggestion: removeAll")
        );
        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("removeone")),
                Text.of("Expected suggestion: removeOne")
        );

        BypassListUtil.removePlayer("removeAll");
        BypassListUtil.removePlayer("removeOne");
        testContext.complete();
    }

    @GameTest
    public void testRemovePlayerIpSuggestions(TestContext testContext) {
        BypassListUtil.addPlayer("removeOne", "1.1.1.1");
        BypassListUtil.addPlayer("removeOne", "1.2.3.4");

        MinecraftServer server = testContext.getWorld().getServer();
        CommandDispatcher<ServerCommandSource> dispatcher = Objects.requireNonNull(server).getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource().withPermissions(PermissionPredicate.ALL);

        ParseResults<ServerCommandSource> parse = dispatcher.parse("bypasslist remove removeOne 1", source);
        Suggestions suggestions = dispatcher.getCompletionSuggestions(parse).join();

        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("1.1.1.1")),
                Text.of("Expected suggestion: 1.1.1.1")
        );
        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("1.2.3.4")),
                Text.of("Expected suggestion: 1.2.3.4")
        );

        BypassListUtil.removePlayer("removeOne");
        testContext.complete();
    }
}
