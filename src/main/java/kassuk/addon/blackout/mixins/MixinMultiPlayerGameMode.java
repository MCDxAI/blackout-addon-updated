package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.AutoMine;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {
  @Shadow
  public abstract void startPrediction(ClientLevel world, PredictiveAction packetCreator);

  @Shadow
  public abstract boolean destroyBlock(BlockPos pos);

  @Shadow @Final private Minecraft minecraft;
  @Shadow private float destroyTicks;
  @Shadow private float destroyProgress;
  @Shadow private ItemStack destroyingItem;
  @Shadow private BlockPos destroyBlockPos;
  @Shadow private boolean isDestroying;

  @Shadow
  public abstract int getDestroyStage();

  @Unique private BlockPos position = null;

  @Inject(method = "startDestroyBlock", at = @At("HEAD"))
  private void onAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
    position = pos;
  }

  @Redirect(
      method = "startDestroyBlock",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V",
              ordinal = 1))
  private void onStart(
      MultiPlayerGameMode instance, ClientLevel world, PredictiveAction packetCreator) {
    AutoMine autoMine = Modules.get().get(AutoMine.class);

    if (!autoMine.isActive()) {
      startPrediction(world, packetCreator);
      return;
    }

    BlockState blockState = world.getBlockState(position);
    boolean bl = !blockState.isAir();
    if (bl && destroyProgress == 0.0F) {
      blockState.attack(minecraft.level, position, minecraft.player);
    }

    if (bl
        && blockState.getDestroyProgress(minecraft.player, minecraft.player.level(), position)
            >= 1.0F) {
      destroyBlock(position);
    } else {
      isDestroying = true;
      destroyBlockPos = position;
      destroyingItem = minecraft.player.getMainHandItem();
      destroyProgress = 0.0F;
      destroyTicks = 0.0F;
      minecraft.level.destroyBlockProgress(
          minecraft.player.getId(), destroyBlockPos, getDestroyStage());
    }

    autoMine.onStart(position);
  }

  @Redirect(
      method = "startDestroyBlock",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V",
              ordinal = 0))
  private void onAbort(ClientPacketListener instance, Packet<?> packet) {
    AutoMine autoMine = Modules.get().get(AutoMine.class);

    if (!autoMine.isActive()) {
      instance.send(packet);
      return;
    }

    autoMine.onAbort(position);
  }

  @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
  private void onUpdateProgress(
      BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
    position = pos;
  }

  @Redirect(
      method = "continueDestroyBlock",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V",
              ordinal = 1))
  private void onStop(
      MultiPlayerGameMode instance, ClientLevel world, PredictiveAction packetCreator) {
    AutoMine autoMine = Modules.get().get(AutoMine.class);

    if (!autoMine.isActive()) {
      startPrediction(world, packetCreator);
      return;
    }

    autoMine.onStop(position);
  }

  @Redirect(
      method = "stopDestroyBlock",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
  private void cancel(ClientPacketListener instance, Packet<?> packet) {
    AutoMine autoMine = Modules.get().get(AutoMine.class);

    if (!autoMine.isActive()) {
      instance.send(packet);
      return;
    }

    autoMine.onAbort(destroyBlockPos);
  }
}
