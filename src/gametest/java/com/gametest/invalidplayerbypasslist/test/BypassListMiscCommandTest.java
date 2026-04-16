package com.gametest.invalidplayerbypasslist.test;

import com.gametest.invalidplayerbypasslist.LogCapture;
import com.themisterfish.invalidplayerbypasslist.InvalidPlayerBypassList;
import com.themisterfish.invalidplayerbypasslist.util.BypassListUtil;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.PermissionSet;

import java.util.Objects;

public class BypassListMiscCommandTest {

    private CommandSourceStack opSource(MinecraftServer server) {
        return server.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS);
    }

    private CommandSourceStack noPermSource(MinecraftServer server) {
        return server.createCommandSourceStack().withPermission(PermissionSet.NO_PERMISSIONS);
    }

    @GameTest
    public void testListCommand(GameTestHelper helper) {
        BypassListUtil.getAllPlayers().forEach(BypassListUtil::removePlayer);
        BypassListUtil.addPlayer("listUser", "1.1.1.1");

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist list");

        helper.assertTrue(
                LogCapture.checkAndRemove("""
                        Bypass list entries:
                        - listuser (1.1.1.1)
                        """),
                "Expected log line for getting bypass list"
        );

        BypassListUtil.removePlayer("listUser");
        helper.succeed();
    }

    @GameTest
    public void testListCommandWhenEmpty(GameTestHelper helper) {
        BypassListUtil.getAllPlayers().forEach(BypassListUtil::removePlayer);

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist list");

        helper.assertTrue(
                LogCapture.checkAndRemove("The bypass list is empty."),
                "Expected log line for empty bypass list"
        );

        helper.succeed();
    }

    @GameTest
    public void testToggleOn(GameTestHelper helper) {
        InvalidPlayerBypassList.bypassList = false;

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist on");

        helper.assertTrue(InvalidPlayerBypassList.bypassList, "Bypass list enabled");
        helper.assertTrue(
                LogCapture.checkAndRemove("Bypasslist is now turned on"),
                "Expected log line for enabling bypass list"
        );

        helper.succeed();
    }

    @GameTest
    public void testToggleOff(GameTestHelper helper) {
        InvalidPlayerBypassList.bypassList = true;

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist off");

        helper.assertTrue(!InvalidPlayerBypassList.bypassList, "Bypass list disabled");
        helper.assertTrue(
                LogCapture.checkAndRemove("Bypasslist is now turned off"),
                "Expected log line for disabling bypass list"
        );

        helper.succeed();
    }

    @GameTest
    public void testToggleAlreadyOn(GameTestHelper helper) {
        InvalidPlayerBypassList.bypassList = true;

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist on");

        helper.assertTrue(InvalidPlayerBypassList.bypassList, "Bypass list enabled");
        helper.assertTrue(
                LogCapture.checkAndRemove("Bypass list is already enabled."),
                "Expected log line for enabling bypass list"
        );

        helper.succeed();
    }

    @GameTest
    public void testToggleAlreadyOff(GameTestHelper helper) {
        InvalidPlayerBypassList.bypassList = false;

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist off");

        helper.assertTrue(!InvalidPlayerBypassList.bypassList, "Bypass list disabled");
        helper.assertTrue(
                LogCapture.checkAndRemove("Bypass list is already disabled."),
                "Expected log line for disabling bypass list"
        );

        helper.succeed();
    }

    @GameTest
    public void testToggleNoOp(GameTestHelper helper) {
        InvalidPlayerBypassList.bypassList = true;

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = noPermSource(server);

        server.getCommands().performPrefixedCommand(source, "bypasslist off");

        helper.assertTrue(
                LogCapture.checkAndRemove("Unknown or incomplete command"),
                "Expected Brigadier parse error log 1"
        );
        helper.assertTrue(
                LogCapture.checkAndRemove("bypasslist off<--[HERE]"),
                "Expected Brigadier parse error log 2"
        );

        helper.assertTrue(InvalidPlayerBypassList.bypassList, "Non‑OP cannot toggle");
        helper.succeed();
    }
}
