package com.gametest.invalidplayerbypasslist.test;

import com.gametest.invalidplayerbypasslist.LogCapture;
import com.gametest.invalidplayerbypasslist.TestPlayerBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.themisterfish.invalidplayerbypasslist.config.ModConfigs;
import com.themisterfish.invalidplayerbypasslist.util.BypassListUtil;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

import static com.themisterfish.invalidplayerbypasslist.util.BypassListUtil.isInBypassList;

public class BypassListAddCommandTest {

    private CommandSourceStack opSource(MinecraftServer server) {
        FakePlayer fakePlayer = new TestPlayerBuilder().buildFakePlayer(server);
        server.getPlayerList().op(fakePlayer.nameAndId());
        return fakePlayer.createCommandSourceStack();
    }

    private CommandSourceStack noPermSource(MinecraftServer server) {
        return new TestPlayerBuilder().buildFakePlayer(server).createCommandSourceStack();
    }

    @GameTest
    public void testAddPlayerWithIp(GameTestHelper helper) throws CommandSyntaxException {
        LogCapture.clear();

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().getDispatcher().execute("bypasslist add addPlayer 1.2.3.4", source);

        List<String> ips = BypassListUtil.getIpsForPlayer("addPlayer");
        helper.assertTrue(ips.size() == 1, "Correct IP count");
        helper.assertTrue("1.2.3.4".equals(ips.getFirst()), "Correct IP stored");
        helper.assertTrue(isInBypassList("addPlayer", "1.2.3.4"), "Player is in bypass list");

        helper.assertTrue(
                LogCapture.checkAndRemove("Added addPlayer with IP 1.2.3.4"),
                "Expected log for adding player with IP"
        );

        BypassListUtil.removePlayer("addPlayer");
        helper.succeed();
    }

    @GameTest
    public void testAddPlayerWithIpDuplicate(GameTestHelper helper) throws CommandSyntaxException {
        LogCapture.clear();

        BypassListUtil.addPlayer("dupPlayer", "9.9.9.9");

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().getDispatcher().execute("bypasslist add dupPlayer 9.9.9.9", source);

        List<String> ips = BypassListUtil.getIpsForPlayer("dupPlayer");
        helper.assertTrue(ips.size() == 1, "Duplicate should not add new entry");

        helper.assertTrue(
                LogCapture.checkAndRemove("dupPlayer with IP 9.9.9.9 is already on the bypass list."),
                "Duplicate log"
        );

        BypassListUtil.removePlayer("dupPlayer");
        helper.succeed();
    }

    @GameTest
    public void testAddPlayerWithoutIp(GameTestHelper helper) throws CommandSyntaxException {
        LogCapture.clear();

        boolean prev = ModConfigs.IP_REQUIRED;
        ModConfigs.IP_REQUIRED = false;

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().getDispatcher().execute("bypasslist add noIpNeeded", source);

        List<String> ips = BypassListUtil.getIpsForPlayer("noIpNeeded");
        helper.assertTrue(ips.size() == 1, "Correct IP count");
        helper.assertTrue("none".equals(ips.getFirst()), "Stored IP should be 'none'");

        helper.assertTrue(
                LogCapture.checkAndRemove("Added noIpNeeded with no IP"),
                "Expected log for adding player without IP"
        );

        ModConfigs.IP_REQUIRED = prev;
        BypassListUtil.removePlayer("noIpNeeded");
        helper.succeed();
    }

    @GameTest
    public void testAddPlayerWithoutIpDuplicate(GameTestHelper helper) throws CommandSyntaxException {
        LogCapture.clear();

        boolean prev = ModConfigs.IP_REQUIRED;
        ModConfigs.IP_REQUIRED = false;

        BypassListUtil.addPlayer("dupNoIp", "none");

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().getDispatcher().execute("bypasslist add dupNoIp", source);

        List<String> ips = BypassListUtil.getIpsForPlayer("dupNoIp");
        helper.assertTrue(ips.size() == 1, "Duplicate should not add new entry");

        helper.assertTrue(
                LogCapture.checkAndRemove("dupNoIp with IP none is already on the bypass list."),
                "Duplicate log"
        );

        ModConfigs.IP_REQUIRED = prev;
        BypassListUtil.removePlayer("dupNoIp");
        helper.succeed();
    }

    @GameTest
    public void testAddPlayerIpRequiredButMissing(GameTestHelper helper) throws CommandSyntaxException {
        LogCapture.clear();

        boolean prev = ModConfigs.IP_REQUIRED;
        ModConfigs.IP_REQUIRED = true;

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = opSource(server);

        server.getCommands().getDispatcher().execute("bypasslist add missingIp", source);

        List<String> ips = BypassListUtil.getIpsForPlayer("missingIp");
        helper.assertTrue(ips.isEmpty(), "No entry should be added");

        helper.assertTrue(
                LogCapture.checkAndRemove("IP is required, please provide one."),
                "IP missing log"
        );

        ModConfigs.IP_REQUIRED = prev;
        helper.succeed();
    }

    @GameTest
    public void testAddPlayerButNoOp(GameTestHelper helper) throws CommandSyntaxException {
        LogCapture.clear();

        MinecraftServer server = Objects.requireNonNull(helper.getLevel().getServer());
        CommandSourceStack source = noPermSource(server);

        server.getCommands().getDispatcher().execute("bypasslist add notOp 1.2.3.4", source);

        List<String> ips = BypassListUtil.getIpsForPlayer("notOp");
        helper.assertTrue(ips.isEmpty(), "Non-OP cannot add");
        helper.assertTrue(!isInBypassList("notOp", "1.2.3.4"), "Player not in bypass list");

        helper.assertTrue(
                LogCapture.checkAndRemove("Unknown or incomplete command"),
                "Expected Brigadier error log 1"
        );
        helper.assertTrue(
                LogCapture.checkAndRemove("bypasslist add notOp 1.2.3.4<--[HERE]"),
                "Expected Brigadier error log 2"
        );

        helper.succeed();
    }
}
