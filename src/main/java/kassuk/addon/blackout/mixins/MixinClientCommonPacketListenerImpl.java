package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.modules.PingSpoof;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class MixinClientCommonPacketListenerImpl {
  @Inject(method = "handleKeepAlive", at = @At("HEAD"), cancellable = true)
  private void keepAlive(ClientboundKeepAlivePacket packet, CallbackInfo ci) {
    if (!Modules.get().isActive(PingSpoof.class)
        || !Modules.get().get(PingSpoof.class).keepAlive.get()) return;

    ci.cancel();
    Managers.PING_SPOOF.addKeepAlive(packet.getId());
  }

  @Inject(method = "handlePing", at = @At("HEAD"), cancellable = true)
  private void pong(ClientboundPingPacket packet, CallbackInfo ci) {
    if (!Modules.get().isActive(PingSpoof.class) || !Modules.get().get(PingSpoof.class).pong.get())
      return;

    ci.cancel();
    Managers.PING_SPOOF.addPong(packet.getId());
  }
}
