package com.themisterfish.invalidplayerbypasslist.mixin;

import com.themisterfish.invalidplayerbypasslist.util.BypassListUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Crypt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

import static com.themisterfish.invalidplayerbypasslist.InvalidPlayerBypassList.LOGGER;
import static com.themisterfish.invalidplayerbypasslist.InvalidPlayerBypassList.bypassList;

@Mixin(ServerLoginPacketListenerImpl.class)
public class LoginPacketListenerImplMixin {

    // Mojang name for the player name field received in the hello packet
    @Shadow
    String requestedUsername;

    @Shadow
    private void startClientVerification(GameProfile profile) {
    }

    @Shadow
    @Final
    Connection connection;

    @Inject(
            method = "handleHello",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;usesAuthentication()Z"
            ),
            cancellable = true
    )
    private void injectCustomCheck(ServerboundHelloPacket packet, CallbackInfo ci) {
        String playerName = this.requestedUsername;
        String ip = ((InetSocketAddress) this.connection.getRemoteAddress())
                .getAddress()
                .getHostAddress();

        if (bypassList && BypassListUtil.isInBypassList(playerName, ip)) {
            LOGGER.info("[Mixin] Player is in bypass list, using offline verification.");
            GameProfile offlineProfile = new GameProfile(packet.profileId(), packet.name());
            this.startClientVerification(offlineProfile);
            ci.cancel();
        }
    }
}
