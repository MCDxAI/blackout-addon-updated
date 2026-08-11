package kassuk.addon.blackout.mixins;

import java.util.UUID;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundTeleportToEntityPacket.class)
public interface IServerboundTeleportToEntityPacket {
  @Accessor("uuid")
  UUID blackout$getID();
}
