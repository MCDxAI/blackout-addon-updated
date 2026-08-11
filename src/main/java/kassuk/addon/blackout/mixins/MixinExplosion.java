package kassuk.addon.blackout.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kassuk.addon.blackout.modules.SoundModifier;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public class MixinExplosion {
  @WrapOperation(
      method = "handleExplosion",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
  private void onExplosion(
      ClientLevel instance,
      double x,
      double y,
      double z,
      SoundEvent sound,
      SoundSource category,
      float volume,
      float pitch,
      boolean useDistance,
      Operation<Void> original) {
    SoundModifier m = Modules.get().get(SoundModifier.class);

    if (m.isActive() && m.expSound.get()) {
      original.call(
          instance,
          x,
          y,
          z,
          sound,
          category,
          (float) (volume * m.explosionVolume.get()),
          (float) (pitch * m.explosionPitch.get()),
          useDistance);
    }
  }
}
