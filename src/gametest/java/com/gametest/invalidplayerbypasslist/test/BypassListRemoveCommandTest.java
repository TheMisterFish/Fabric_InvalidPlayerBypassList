package com.gametest.invalidplayerbypasslist.test;

import com.gametest.invalidplayerbypasslist.LogCapture;
import com.invalidplayerbypasslist.util.BypassListUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

public class BypassListRemoveCommandTest {

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testRemovePlayerAllIps(TestContext testContext) {
        BypassListUtil.addPlayer("removeAll", "1.1.1.1");
        BypassListUtil.addPlayer("removeAll", "2.2.2.2");

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist remove removeAll"
        );

        testContext.assertEquals(0, BypassListUtil.getIpsForPlayer("removeAll").size(), "All IPs removed");
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testRemovePlayerSpecificIp(TestContext testContext) {
        BypassListUtil.addPlayer("removeOne", "1.1.1.1");
        BypassListUtil.addPlayer("removeOne", "2.2.2.2");

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist remove removeOne 1.1.1.1"
        );

        List<String> ips = BypassListUtil.getIpsForPlayer("removeOne");
        testContext.assertEquals(1, ips.size(), "One IP remains");
        testContext.assertEquals("2.2.2.2", ips.getFirst(), "Correct IP remains");

        BypassListUtil.removePlayer("removeOne");
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testRemovePlayerNotFound(TestContext testContext) {
        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist remove doesNotExist"
        );

        testContext.assertFalse(BypassListUtil.getAllPlayers().contains("doesNotExist"), "Player doesn't exist");
        testContext.assertTrue(
                LogCapture.checkAndRemove("doesNotExist not found in the bypass list."),
                "Expected log for removing non existing player"
        );

        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testRemovePlayerNotFoundWithIp(TestContext testContext) {
        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist remove doesNotExistWithIp 1.1.1.1"
        );

        testContext.assertFalse(BypassListUtil.getAllPlayers().contains("doesNotExistWithIp"), "Player doesn't exist");
        testContext.assertTrue(
                LogCapture.checkAndRemove("doesNotExistWithIp with IP 1.1.1.1 not found in the bypass list."),
                "Expected log for removing non existing player"
        );

        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testRemovePlayerNoOp(TestContext testContext) {
        BypassListUtil.addPlayer("noOpRemove", "1.1.1.1");

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(0);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist remove noOpRemove"
        );

        testContext.assertEquals(1, BypassListUtil.getIpsForPlayer("noOpRemove").size(), "Entry not removed");
        BypassListUtil.removePlayer("noOpRemove");
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testRemovePlayerSuggestions(TestContext testContext) {
        BypassListUtil.addPlayer("removeAll", "1.1.1.1");
        BypassListUtil.addPlayer("removeOne", "2.2.2.2");

        MinecraftServer server = testContext.getWorld().getServer();
        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource().withLevel(4);

        ParseResults<ServerCommandSource> parse = dispatcher.parse("bypasslist remove r", source);
        Suggestions suggestions = dispatcher.getCompletionSuggestions(parse).join();

        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("removeall")),
                "Expected suggestion: removeAll"
        );
        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("removeone")),
                "Expected suggestion: removeOne"
        );

        BypassListUtil.removePlayer("removeAll");
        BypassListUtil.removePlayer("removeOne");
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testRemovePlayerIpSuggestions(TestContext testContext) {
        BypassListUtil.addPlayer("removeOne", "1.1.1.1");
        BypassListUtil.addPlayer("removeOne", "1.2.3.4");

        MinecraftServer server = testContext.getWorld().getServer();
        CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
        ServerCommandSource source = server.getCommandSource().withLevel(4);

        ParseResults<ServerCommandSource> parse = dispatcher.parse("bypasslist remove removeOne 1", source);
        Suggestions suggestions = dispatcher.getCompletionSuggestions(parse).join();

        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("1.1.1.1")),
                "Expected suggestion: 1.1.1.1"
        );
        testContext.assertTrue(
                suggestions.getList().stream().anyMatch(s -> s.getText().equals("1.2.3.4")),
                "Expected suggestion: 1.2.3.4"
        );

        BypassListUtil.removePlayer("removeOne");
        testContext.complete();
    }
}
