package com.gametest.invalidplayerbypasslist;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.UUID;

import static net.minecraft.world.level.Level.OVERWORLD;

public class TestPlayerBuilder {
    public UUID uuid = UUID.randomUUID();
    public String name = "TestPlayer";
    public GameProfile gameProfile = new GameProfile(uuid, name);
    public TestClientConnection connection = new TestClientConnection(PacketFlow.SERVERBOUND);
    public CommonListenerCookie cookie = new CommonListenerCookie(gameProfile, 0, ClientInformation.createDefault(), false);
    public ResourceKey<Level> dimension = OVERWORLD;
    public Float health = 20.0f;
    public MinecraftServer server;
    FakePlayer fake;

    public FakePlayer buildFakePlayer(MinecraftServer server) {
        this.fake  = FakePlayer.get(Objects.requireNonNull(server.getLevel(dimension)), gameProfile);
        this.fake.connection = new ServerGamePacketListenerImpl(server, connection, fake, cookie);

        this.fake.setHealth(health);
        this.server = server;

        return fake;
    }

    public static class TestClientConnection extends Connection {
        public TestClientConnection(PacketFlow p) {
            super(p);
            EmbeddedChannel ch = new EmbeddedChannel();
            ch.pipeline().addLast("packet_handler", this);
            ch.pipeline().fireChannelActive();
        }
    }
}
