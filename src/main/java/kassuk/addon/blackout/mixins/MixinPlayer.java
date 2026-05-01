package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.SoundModifier;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinPlayer {
    @Unique
    Entity attackEntity = null;

    @Inject(method = "attack", at = @At(value = "HEAD"))
    private void inject(Entity target, CallbackInfo ci) {
        attackEntity = target;
    }

    @Redirect(
        method = "attack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
        ),
        require = 0
    )
    private void modifyAttackSound(Level instance, Entity source, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
        SoundModifier m = Modules.get().get(SoundModifier.class);

        if (m.isActive() && m.crystalHits.get() && attackEntity instanceof EndCrystal) {
            instance.playSound(source, x, y, z, sound, category, (float)(volume * m.crystalHitVolume.get()), (float)(pitch * m.crystalHitPitch.get()));
        } else {
            instance.playSound(source, x, y, z, sound, category, volume, pitch);
        }
    }
}
