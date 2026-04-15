package com.gametest.invalidplayerbypasslist.test;

import com.gametest.invalidplayerbypasslist.LogCapture;
import com.invalidplayerbypasslist.config.ModConfigs;
import com.invalidplayerbypasslist.util.BypassListUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

import static com.invalidplayerbypasslist.util.BypassListUtil.isInBypassList;

public class BypassListAddCommandTest {

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testAddPlayerWithIp(TestContext testContext) {
        LogCapture.clear();

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist add addPlayer 1.2.3.4"
        );

        List<String> ips = BypassListUtil.getIpsForPlayer("addPlayer");
        testContext.assertEquals(1, ips.size(), "Correct IP count");
        testContext.assertEquals("1.2.3.4", ips.getFirst(), "Correct IP stored");
        testContext.assertTrue(isInBypassList("addPlayer", "1.2.3.4"), "Player is in bypass list");

        testContext.assertTrue(
                LogCapture.checkAndRemove("Added addPlayer with IP 1.2.3.4"),
                "Expected log for adding player with IP"
        );

        BypassListUtil.removePlayer("addPlayer");
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testAddPlayerWithIpDuplicate(TestContext testContext) {
        LogCapture.clear();

        BypassListUtil.addPlayer("dupPlayer", "9.9.9.9");

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist add dupPlayer 9.9.9.9"
        );

        List<String> ips = BypassListUtil.getIpsForPlayer("dupPlayer");
        testContext.assertEquals(1, ips.size(), "Duplicate should not add new entry");

        testContext.assertTrue(
                LogCapture.checkAndRemove("dupPlayer with IP 9.9.9.9 is already on the bypass list."),
                "Duplicate log"
        );

        BypassListUtil.removePlayer("dupPlayer");
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testAddPlayerWithoutIp(TestContext testContext) {
        LogCapture.clear();

        boolean prev = ModConfigs.IP_REQUIRED;
        ModConfigs.IP_REQUIRED = false;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist add noIpNeeded"
        );

        List<String> ips = BypassListUtil.getIpsForPlayer("noIpNeeded");
        testContext.assertEquals(1, ips.size(), "Correct IP count");
        testContext.assertEquals("none", ips.getFirst(), "Stored IP should be 'none'");

        testContext.assertTrue(
                LogCapture.checkAndRemove("Added noIpNeeded with no IP"),
                "Expected log for adding player without IP"
        );

        ModConfigs.IP_REQUIRED = prev;
        BypassListUtil.removePlayer("noIpNeeded");
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testAddPlayerWithoutIpDuplicate(TestContext testContext) {
        LogCapture.clear();

        boolean prev = ModConfigs.IP_REQUIRED;
        ModConfigs.IP_REQUIRED = false;

        BypassListUtil.addPlayer("dupNoIp", "none");

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist add dupNoIp"
        );

        List<String> ips = BypassListUtil.getIpsForPlayer("dupNoIp");
        testContext.assertEquals(1, ips.size(), "Duplicate should not add new entry");

        testContext.assertTrue(
                LogCapture.checkAndRemove("dupNoIp with IP none is already on the bypass list."),
                "Duplicate log"
        );

        ModConfigs.IP_REQUIRED = prev;
        BypassListUtil.removePlayer("dupNoIp");
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testAddPlayerIpRequiredButMissing(TestContext testContext) {
        LogCapture.clear();

        boolean prev = ModConfigs.IP_REQUIRED;
        ModConfigs.IP_REQUIRED = true;

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(4);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist add missingIp"
        );

        List<String> ips = BypassListUtil.getIpsForPlayer("missingIp");
        testContext.assertEquals(0, ips.size(), "No entry should be added");

        testContext.assertTrue(
                LogCapture.checkAndRemove("IP is required, please provide one."),
                "IP missing log"
        );

        ModConfigs.IP_REQUIRED = prev;
        testContext.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testAddPlayerButNoOp(TestContext testContext) {
        LogCapture.clear();

        ServerCommandSource source = testContext.getWorld().getServer().getCommandSource()
                .withLevel(0);

        testContext.getWorld().getServer().getCommandManager().executeWithPrefix(
                source,
                "bypasslist add notOp 1.2.3.4"
        );

        List<String> ips = BypassListUtil.getIpsForPlayer("notOp");
        testContext.assertEquals(0, ips.size(), "Non-OP cannot add");

        testContext.assertFalse(isInBypassList("notOp", "1.2.3.4"), "Player not in bypass list");

        testContext.assertTrue(
                LogCapture.checkAndRemove("Unknown or incomplete command, see below for error"),
                "Expected Brigadier error log 1"
        );
        testContext.assertTrue(
                LogCapture.checkAndRemove("bypasslist add notOp 1.2.3.4<--[HERE]"),
                "Expected Brigadier error log 2"
        );

        testContext.complete();
    }
}
