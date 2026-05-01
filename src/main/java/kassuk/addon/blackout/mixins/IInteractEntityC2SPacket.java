package kassuk.addon.blackout.mixins;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundInteractPacket.class)
public interface IInteractEntityC2SPacket {
    @Accessor("entityId")
    @Final
    @Mutable
    void blackout$setId(final int id);

    @Accessor("entityId")
    int blackout$getId();

    @Accessor("action")
    ServerboundInteractPacket.Action blackout$getAction();
}

