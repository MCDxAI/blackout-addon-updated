package kassuk.addon.blackout.mixins;

import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundRotateHeadPacket.class)
public interface IClientboundRotateHeadPacket {
    @Accessor("entityId")
    int blackout$getId();
}
