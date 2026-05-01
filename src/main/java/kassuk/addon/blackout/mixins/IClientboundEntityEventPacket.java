package kassuk.addon.blackout.mixins;

import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundEntityEventPacket.class)
public interface IClientboundEntityEventPacket {
    @Accessor("entityId")
    int blackout$getId();
}
