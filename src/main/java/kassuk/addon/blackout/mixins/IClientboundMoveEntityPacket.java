package kassuk.addon.blackout.mixins;

import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundMoveEntityPacket.class)
public interface IClientboundMoveEntityPacket {
  @Accessor("entityId")
  int blackout$getEntityId();
}
