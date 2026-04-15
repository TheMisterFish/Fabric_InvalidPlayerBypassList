package com.gametest.invalidplayerbypasslist.test;

import com.gametest.invalidplayerbypasslist.LogCapture;
import com.invalidplayerbypasslist.InvalidPlayerBypassList;
import com.invalidplayerbypasslist.util.BypassListUtil;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

public class BypassListMiscCommandTest {

    @GameTest
    public void testListCommand(TestContext testContext) {
        BypassListUtil.getAllPlayers().forEach(BypassListUtil::removePlayer);
        BypassListUtil.addPlayer("listUser", "1.1.1.1");

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist list"
        );
        testContext.assertTrue(
                LogCapture.checkAndRemove("""
                        Bypass list entries:
                        - listuser (1.1.1.1)
                        """),
                Text.of("Expected log line for getting bypass list")
        );

        BypassListUtil.removePlayer("listUser");
        testContext.complete();
    }

    @GameTest
    public void testListCommandWhenEmpty(TestContext testContext) {
        BypassListUtil.getAllPlayers().forEach(BypassListUtil::removePlayer);

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist list"
        );
        testContext.assertTrue(
                LogCapture.checkAndRemove("The bypass list is empty."),
                Text.of("Expected log line for getting bypass list")
        );

        testContext.complete();
    }

    @GameTest
    public void testToggleOn(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = false;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist on"
        );

        testContext.assertTrue(InvalidPlayerBypassList.bypassList, Text.of("Bypass list enabled"));
        testContext.assertTrue(
                LogCapture.checkAndRemove("Bypasslist is now turned on"),
                Text.of("Expected log line for enabling bypass list")
        );

        testContext.complete();
    }

    @GameTest
    public void testToggleOff(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = true;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist off"
        );

        testContext.assertFalse(InvalidPlayerBypassList.bypassList, Text.of("Bypass list disabled"));
        testContext.assertTrue(
                LogCapture.checkAndRemove("Bypasslist is now turned off"),
                Text.of("Expected log line for disabling bypass list")
        );

        testContext.complete();
    }

    @GameTest
    public void testToggleAlreadyOn(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = true;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist on"
        );

        testContext.assertTrue(InvalidPlayerBypassList.bypassList, Text.of("Bypass list enabled"));
        testContext.assertTrue(
                LogCapture.checkAndRemove("Bypass list is already enabled."),
                Text.of("Expected log line for enabling bypass list")
        );

        testContext.complete();
    }

    @GameTest
    public void testToggleAlreadyOff(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = false;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist off"
        );

        testContext.assertFalse(InvalidPlayerBypassList.bypassList, Text.of("Bypass list disabled"));
        testContext.assertTrue(
                LogCapture.checkAndRemove("Bypass list is already disabled."),
                Text.of("Expected log line for disabling bypass list")
        );

        testContext.complete();
    }

    @GameTest
    public void testToggleNoOp(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = true;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(0);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist off"
        );

        testContext.assertTrue(
                LogCapture.checkAndRemove("Unknown or incomplete command"),
                Text.of("Expected log line for disabling bypass list without op 1")
        );
        testContext.assertTrue(
                LogCapture.checkAndRemove("bypasslist off<--[HERE]"),
                Text.of("Expected log line for disabling bypass list without op 2")
        );

        testContext.assertTrue(InvalidPlayerBypassList.bypassList, Text.of("Non‑OP cannot toggle"));
        testContext.complete();
    }
}
