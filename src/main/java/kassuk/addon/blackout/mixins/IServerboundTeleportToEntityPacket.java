package kassuk.addon.blackout.mixins;

import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ServerboundTeleportToEntityPacket.class)
public interface IServerboundTeleportToEntityPacket {
    @Accessor("uuid")
    UUID blackout$getID();
}
