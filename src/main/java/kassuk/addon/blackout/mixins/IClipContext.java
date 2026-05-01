package kassuk.addon.blackout.mixins;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClipContext.class)
public interface IClipContext {
    @Mutable
    @Accessor("from")
    void blackout$setFrom(Vec3 start);

    @Mutable
    @Accessor("to")
    void blackout$setTo(Vec3 end);
}
