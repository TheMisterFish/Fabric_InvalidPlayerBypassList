package com.misterfish.mixin;

import com.misterfish.InvalidPlayerBypassList;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Uuids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

import static com.misterfish.InvalidPlayerBypassList.LOGGER;
import static com.misterfish.InvalidPlayerBypassList.bypassList;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin_InvalidPlayerBypassListMixin {

    @Shadow private String profileName;
    @Shadow private void startVerify(GameProfile profile) {}
    @Shadow @Final public ClientConnection connection;

    @Inject(method = "onHello", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"), cancellable = true)
    private void injectCustomCheck(LoginHelloC2SPacket packet, CallbackInfo ci) {
        String playerName = this.profileName;
        String ip = ((InetSocketAddress) this.connection.getAddress()).getAddress().getHostAddress();

        if (bypassList && InvalidPlayerBypassList.isInBypassList(playerName, ip)) {
            LOGGER.info("[Mixin] Player is in bypass list, using offline verification.");
            GameProfile offlineProfile = Uuids.getOfflinePlayerProfile(this.profileName);
            this.startVerify(offlineProfile);
            ci.cancel();
        }
    }
}
