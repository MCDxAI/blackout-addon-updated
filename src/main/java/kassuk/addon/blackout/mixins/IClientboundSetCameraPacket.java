package kassuk.addon.blackout.mixins;

import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSetCameraPacket.class)
public interface IClientboundSetCameraPacket {
  @Accessor("cameraId")
  int blackout$getId();
}
