package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.tags.FluidTags;

public class JesusPlus extends BlackOutModule {
    public JesusPlus() {
        super(BlackOut.BLACKOUT, "Jesus+", "Better jesus");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> bob = sgGeneral.add(new DoubleSetting.Builder()
        .name("Bob force")
        .description("Use 0.005 or 0.1")
        .defaultValue(0.005)
        .min(0)
        .sliderMax(1)
        .build()
    );
    private final  Setting<Boolean> toggle = sgGeneral.add(new BoolSetting.Builder()
        .name("Change speed")
        .description("")
        .build()
    );
    private final Setting<Double> water_speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Water speed")
        .description("0.265 is generally better")
        .defaultValue(1.175)
        .min(0)
        .sliderMax(2)
        .build()
    );

    private boolean move = false;
    private boolean inWater = false;
    private boolean isSlowed = false;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (mc.player != null && mc.level != null) {
            if (mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() == Blocks.WATER || mc.level.getBlockState(mc.player.blockPosition()).getBlock() == Blocks.WATER) {
                if (!inWater) {
                    isSlowed = false;
                }
                inWater = true;
            }
            else inWater = false;

            if ((mc.player.isInWater() && !mc.player.isUnderWater()) || (mc.player.isInLava() && !mc.player.isEyeInFluid(FluidTags.LAVA))) {
                ((IVec3) mc.player.getDeltaMovement()).meteor$setY(bob.get());

                if (toggle.get() && !(mc.player.isInLava() && !mc.player.isEyeInFluid(FluidTags.LAVA)) && !isSlowed) {
                    double motion = water_speed.get();
                    if (mc.player.hasEffect(MobEffects.SPEED)) {
                        motion *= 1.2 + mc.player.getEffect(MobEffects.SPEED).getAmplifier() * 0.2;
                    }
                    if (mc.player.hasEffect(MobEffects.SLOWNESS)) {
                        motion /= 1.2 + mc.player.getEffect(MobEffects.SLOWNESS).getAmplifier() * 0.2;
                    }
                    double forward = mc.player.input.getMoveVector().y;
                    double sideways = mc.player.input.getMoveVector().x;

                    double yaw = getYaw(forward, sideways);
                    double x = Math.cos(Math.toRadians(yaw + 90.0f));
                    double z = Math.sin(Math.toRadians(yaw + 90.0f));
                    if (move) {
                        ((IVec3) event.movement).meteor$setXZ(motion * x, motion * z);
                    } else {
                        ((IVec3) event.movement).meteor$setXZ(0, 0);
                    }
                }
            }
        }
    }
    @EventHandler
    private void OnRecieve(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundPlayerPositionPacket)
            isSlowed = true;
    }
    public void onActivate() {
        inWater = false;
    }

    private double getYaw(double f, double s) {
        double yaw = mc.player.getYRot();
        if (f > 0) {
            move = true;
            yaw += s > 0 ? -45 : s < 0 ? 45 : 0;
        } else if (f < 0) {
            move = true;
            yaw += s > 0 ? -135 : s < 0 ? 135 : 180;
        } else {
            move = s != 0;
            yaw += s > 0 ? -90 : s < 0 ? 90 : 0;
        }
        return yaw;
    }
}
