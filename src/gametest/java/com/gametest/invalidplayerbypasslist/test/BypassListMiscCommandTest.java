package com.gametest.invalidplayerbypasslist.test;

import com.gametest.invalidplayerbypasslist.LogCapture;
import com.invalidplayerbypasslist.InvalidPlayerBypassList;
import com.invalidplayerbypasslist.util.BypassListUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public class BypassListMiscCommandTest {

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
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
                LogCapture.checkAndRemove("Bypass list entries:\n" +
                        "- listuser (1.1.1.1)\n"),
                "Expected log line for getting bypass list"
        );

        BypassListUtil.removePlayer("listUser");
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
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
                "Expected log line for getting bypass list"
        );

        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testToggleOn(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = false;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist on"
        );

        testContext.assertTrue(InvalidPlayerBypassList.bypassList, "Bypass list enabled");
        testContext.assertTrue(
                LogCapture.checkAndRemove("Bypasslist is now turned on"),
                "Expected log line for enabling bypass list"
        );

        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testToggleOff(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = true;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist off"
        );

        testContext.assertFalse(InvalidPlayerBypassList.bypassList, "Bypass list disabled");
        testContext.assertTrue(
                LogCapture.checkAndRemove("Bypasslist is now turned off"),
                "Expected log line for disabling bypass list"
        );

        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testToggleAlreadyOn(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = true;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist on"
        );

        testContext.assertTrue(InvalidPlayerBypassList.bypassList, "Bypass list enabled");
        testContext.assertTrue(
                LogCapture.checkAndRemove("Bypass list is already enabled."),
                "Expected log line for enabling bypass list"
        );

        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testToggleAlreadyOff(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = false;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist off"
        );

        testContext.assertFalse(InvalidPlayerBypassList.bypassList, "Bypass list disabled");
        testContext.assertTrue(
                LogCapture.checkAndRemove("Bypass list is already disabled."),
                "Expected log line for disabling bypass list"
        );

        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testToggleNoOp(TestContext testContext) {
        InvalidPlayerBypassList.bypassList = true;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(0);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist off"
        );

        testContext.assertTrue(
                LogCapture.checkAndRemove("Unknown or incomplete command, see below for error"),
                "Expected log line for disabling bypass list without op 1"
        );
        testContext.assertTrue(
                LogCapture.checkAndRemove("bypasslist off<--[HERE]"),
                "Expected log line for disabling bypass list without op 2"
        );

        testContext.assertTrue(InvalidPlayerBypassList.bypassList, "Non‑OP cannot toggle");
        testContext.complete();
    }
}
