package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.SwingModifier;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {
  @ModifyArgs(
      method =
          "submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/renderer/ItemInHandRenderer;submitArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
  private void setArgs(Args args) {
    SwingModifier module = Modules.get().get(SwingModifier.class);
    if (module != null && module.isActive()) {
      args.set(6, module.getY(args.get(3)));
      args.set(4, module.getSwing(args.get(3)));
    }
  }
}
