package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.modules.SwingModifier;
import kassuk.addon.blackout.modules.TickShift;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {
    @Shadow
    @Final
    public ClientPacketListener connection;
    @Unique
    private static boolean sent = false;

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At(value = "HEAD"))
    private void swingHand(InteractionHand hand, CallbackInfo ci) {
        Modules.get().get(SwingModifier.class).startSwing(hand);
    }

    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void sendPacketsHead(CallbackInfo ci) {
        sent = false;
    }

    @Inject(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void onSendPacket(CallbackInfo ci) {
        sent = true;
    }

    @Inject(method = "sendPosition", at = @At("TAIL"))
    private void sendPacketsTail(CallbackInfo ci) {
        if (!sent) {
            TickShift tickShift = Modules.get().get(TickShift.class);
            if (tickShift.isActive()) {
                tickShift.unSent = Math.min(tickShift.packets.get(), tickShift.unSent + 1);
            }
        }
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0))
    private void sendPacketFull(ClientPacketListener instance, Packet<?> packet) {
        connection.send(Managers.ROTATION.onFull((ServerboundMovePlayerPacket.PosRot) packet));
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1))
    private void sendPacketPosGround(ClientPacketListener instance, Packet<?> packet) {
        connection.send(Managers.ROTATION.onPositionOnGround((ServerboundMovePlayerPacket.Pos) packet));
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 2))
    private void sendPacketLookAndOnGround(ClientPacketListener instance, Packet<?> packet) {
        connection.send(Managers.ROTATION.onLookAndOnGround((ServerboundMovePlayerPacket.Rot) packet));
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 3))
    private void sendPacketOnGroundOnly(ClientPacketListener instance, Packet<?> packet) {
        connection.send(Managers.ROTATION.onOnlyOnground((ServerboundMovePlayerPacket.StatusOnly) packet));
    }
}
