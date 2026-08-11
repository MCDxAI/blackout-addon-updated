package kassuk.addon.blackout.mixins;

import io.netty.channel.ChannelFutureListener;
import kassuk.addon.blackout.modules.PacketLogger;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinConnection {
  @Unique private PacketLogger packetLogger = null;

  @Inject(method = "doSendPacket", at = @At("HEAD"))
  private void onSent(
      Packet<?> packet,
      @Nullable ChannelFutureListener channelFutureListener,
      boolean flush,
      CallbackInfo ci) {
    if (packetLogger == null) packetLogger = Modules.get().get(PacketLogger.class);
    else if (packetLogger.isActive()) packetLogger.onSent(packet);
  }
}
