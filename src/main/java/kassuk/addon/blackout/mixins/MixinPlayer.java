package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.SoundModifier;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinPlayer {
  @Unique Entity attackEntity = null;

  @Inject(method = "attack", at = @At(value = "HEAD"))
  private void inject(Entity target, CallbackInfo ci) {
    attackEntity = target;
  }

  // In 26.1.2 Player.attack no longer calls Level.playSound directly: every server-side
  // attack sound (KNOCKBACK/NODAMAGE/CRIT/STRONG/WEAK/SWEEP) is routed through the private
  // playServerSideSound helper, which performs the single `this.level().playSound(...)`.
  // Redirecting that INVOKE faithfully restores the original crystal-hit scaling for every
  // attack sound. @At owner is Level = static type of this.level() (Entity.level() : Level).
  @Redirect(
      method = "playServerSideSound",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
  private void modifyAttackSound(
      Level instance,
      Entity source,
      double x,
      double y,
      double z,
      SoundEvent sound,
      SoundSource category,
      float volume,
      float pitch) {
    SoundModifier m = Modules.get().get(SoundModifier.class);

    if (m.isActive() && m.crystalHits.get() && attackEntity instanceof EndCrystal) {
      instance.playSound(
          source,
          x,
          y,
          z,
          sound,
          category,
          (float) (volume * m.crystalHitVolume.get()),
          (float) (pitch * m.crystalHitPitch.get()));
    } else {
      instance.playSound(source, x, y, z, sound, category, volume, pitch);
    }
  }
}
