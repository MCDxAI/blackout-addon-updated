package kassuk.addon.blackout.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface AccessorMinecraft {
  @Accessor("rightClickDelay")
  void blackout$setRightClickDelay(int itemUseCooldown);
}
