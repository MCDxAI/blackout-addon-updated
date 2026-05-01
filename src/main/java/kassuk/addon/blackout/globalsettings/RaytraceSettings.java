package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.mixins.IClipContext;
import kassuk.addon.blackout.utils.meteor.BODamageUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

/**
 * @author OLEPOSSU
 */

public class RaytraceSettings extends BlackOutModule {
    public RaytraceSettings() {
        super(BlackOut.SETTINGS, "Raytrace", "Global raytrace settings for every blackout module.");
    }

    private final SettingGroup sgPlace = settings.createGroup("Placing");
    private final SettingGroup sgAttack = settings.createGroup("Attacking");

    //--------------------Place-Settings--------------------//
    public final Setting<Boolean> placeTrace = sgPlace.add(new BoolSetting.Builder()
        .name("Place Traces")
        .description("Raytraces when placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<PlaceTraceMode> placeMode = sgPlace.add(new EnumSetting.Builder<PlaceTraceMode>()
        .name("Place Mode")
        .description("Place trace mode.")
        .defaultValue(PlaceTraceMode.SinglePoint)
        .visible(placeTrace::get)
        .build()
    );
    private final Setting<Double> placeHeight = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-2, 2)
        .visible(() -> placeMode.get() == PlaceTraceMode.SinglePoint && placeTrace.get())
        .build()
    );
    private final Setting<Double> placeHeight1 = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height 1")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.25)
        .sliderRange(-2, 1.5)
        .visible(() -> placeMode.get() == PlaceTraceMode.DoublePoint && placeTrace.get())
        .build()
    );
    private final Setting<Double> placeHeight2 = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height 2")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(0.75)
        .sliderRange(-2, 2)
        .visible(() -> placeMode.get() == PlaceTraceMode.DoublePoint && placeTrace.get())
        .build()
    );
    private final Setting<Double> exposure = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Exposure")
        .description("How many % of the block should be seen.")
        .defaultValue(50)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(() -> placeMode.get() == PlaceTraceMode.Exposure && placeTrace.get())
        .build()
    );

    //--------------------Place-Settings--------------------//
    public final Setting<Boolean> attackTrace = sgAttack.add(new BoolSetting.Builder()
        .name("Attack Traces")
        .description("Raytraces when attacking.")
        .defaultValue(false)
        .build()
    );
    private final Setting<AttackTraceMode> attackMode = sgAttack.add(new EnumSetting.Builder<AttackTraceMode>()
        .name("Attack Mode")
        .description("Attack trace mode.")
        .defaultValue(AttackTraceMode.SinglePoint)
        .visible(attackTrace::get)
        .build()
    );
    private final Setting<Double> attackHeight = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height")
        .description("Raytraces to x blocks above the bottom.")
        .defaultValue(1.5)
        .sliderRange(-2, 2)
        .visible(() -> attackMode.get().equals(AttackTraceMode.SinglePoint) && attackTrace.get())
        .build()
    );
    private final Setting<Double> attackHeight1 = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height 1")
        .description("Raytraces to x * hitbox height above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-2, 2)
        .visible(() -> attackMode.get().equals(AttackTraceMode.DoublePoint) && attackTrace.get())
        .build()
    );
    private final Setting<Double> attackHeight2 = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height 2")
        .description("Raytraces to x * hitbox height above the bottom.")
        .defaultValue(0.5)
        .sliderRange(-2, 2)
        .visible(() -> attackMode.get().equals(AttackTraceMode.DoublePoint) && attackTrace.get())
        .build()
    );
    private final Setting<Double> attackExposure = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Exposure")
        .description("How many % of the entity should be seen.")
        .defaultValue(50)
        .range(0, 100)
        .sliderRange(0, 100)
        .visible(() -> placeMode.get() == PlaceTraceMode.Exposure && attackTrace.get())
        .build()
    );

    public enum PlaceTraceMode {
        SinglePoint,
        DoublePoint,
        Sides,
        Exposure,
        Any
    }

    public enum AttackTraceMode {
        SinglePoint,
        DoublePoint,
        Exposure,
        Any
    }

    private final Vec3 vec = new Vec3(0, 0, 0);
    public ClipContext ClipContext;
    public BlockHitResult result;
    public int hit = 0;

    public boolean placeTrace(BlockPos pos) {

        if (!placeTrace.get()) {
            return true;
        }

        updateContext();

        switch (placeMode.get()) {
            case SinglePoint -> {
                ((IClipContext) ClipContext).blackout$setTo(new Vec3(pos.getX() + 0.5, pos.getY() + placeHeight.get(), pos.getZ() + 0.5));

                result = BODamageUtils.raycast(ClipContext);
                return result.getBlockPos().equals(pos);
            }
            case DoublePoint -> {
                ((IClipContext) ClipContext).blackout$setTo(new Vec3(pos.getX() + 0.5, pos.getY() + placeHeight1.get(), pos.getZ() + 0.5));

                result = BODamageUtils.raycast(ClipContext);
                if (result.getBlockPos().equals(pos)) {
                    return true;
                }

                ((IClipContext) ClipContext).blackout$setTo(new Vec3(pos.getX() + 0.5, pos.getY() + placeHeight2.get(), pos.getZ() + 0.5));

                result = BODamageUtils.raycast(ClipContext);
                return result.getBlockPos().equals(pos);
            }
            case Sides -> {
                ((IVec3) vec).meteor$set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                for (Direction dir : Direction.values()) {
                    ((IClipContext) ClipContext).blackout$setTo(vec.add(dir.getStepX() / 2f, dir.getStepY() / 2f, dir.getStepZ() / 2f));

                    result = BODamageUtils.raycast(ClipContext);
                    if (result.getBlockPos().equals(pos)) {
                        return true;
                    }
                }
            }
            case Exposure -> {
                ((IVec3) vec).meteor$set(pos.getX(), pos.getY(), pos.getZ());

                hit = 0;
                for (int x = 0; x <= 2; x += 1) {
                    for (int y = 0; y <= 2; y += 1) {
                        for (int z = 0; z <= 2; z += 1) {
                            ((IClipContext) ClipContext).blackout$setTo(vec.add(0.1 + x * 0.4, 0.1 + y * 0.4, 0.1 + z * 0.4));

                            result = BODamageUtils.raycast(ClipContext);
                            if (result.getBlockPos().equals(pos)) {
                                hit++;
                                if (hit >= exposure.get() / 100 * 27) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            case Any -> {
                ((IVec3) vec).meteor$set(pos.getX(), pos.getY(), pos.getZ());

                hit = 0;
                for (int x = 0; x <= 2; x += 1) {
                    for (int y = 0; y <= 2; y += 1) {
                        for (int z = 0; z <= 2; z += 1) {
                            ((IClipContext) ClipContext).blackout$setTo(vec.add(0.1 + x * 0.4, 0.1 + y * 0.4, 0.1 + z * 0.4));

                            result = BODamageUtils.raycast(ClipContext);
                            if (result.getBlockPos().equals(pos)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean attackTrace(AABB box) {
        if (!attackTrace.get()) {
            return true;
        }

        updateContext();

        switch (attackMode.get()) {
            case SinglePoint -> {
                ((meteordevelopment.meteorclient.mixininterface.IClipContext) BODamageUtils.ClipContext).meteor$set(mc.player.getEyePosition(), new Vec3((box.minX + box.maxX) / 2f, box.minY + attackHeight.get(), (box.minZ + box.maxZ) / 2f), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player);

                return BODamageUtils.raycast(BODamageUtils.ClipContext).getType() != HitResult.Type.BLOCK;
            }
            case DoublePoint -> {
                ((meteordevelopment.meteorclient.mixininterface.IClipContext) BODamageUtils.ClipContext).meteor$set(mc.player.getEyePosition(), new Vec3((box.minX + box.maxX) / 2f, box.minY + attackHeight1.get(), (box.minZ + box.maxZ) / 2f), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player);
                if (BODamageUtils.raycast(BODamageUtils.ClipContext).getType() != HitResult.Type.BLOCK) {
                    return true;
                }

                ((meteordevelopment.meteorclient.mixininterface.IClipContext) BODamageUtils.ClipContext).meteor$set(mc.player.getEyePosition(), new Vec3((box.minX + box.maxX) / 2f, box.minY + attackHeight2.get(), (box.minZ + box.maxZ) / 2f), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player);
                return BODamageUtils.raycast(BODamageUtils.ClipContext).getType() != HitResult.Type.BLOCK;
            }
            case Exposure -> {
                ((IVec3) vec).meteor$set(box.min, box.minY, box.minZ);
                double xw = box.maxX - box.minX;
                double yh = box.maxY - box.minY;
                double zw = box.maxZ - box.minZ;

                hit = 0;
                for (int x = 0; x <= 2; x += 1) {
                    for (int y = 0; y <= 2; y += 1) {
                        for (int z = 0; z <= 2; z += 1) {
                            ((IClipContext) ClipContext).blackout$setTo(vec.add(Mth.lerp(x / 2f, 0.1, xw - 0.1), Mth.lerp(y / 2f, 0.0, yh - 0.1), Mth.lerp(z / 2f, 0.1, zw - 0.1)));

                            result = BODamageUtils.raycast(ClipContext);
                            if (result.getType() != HitResult.Type.BLOCK) {
                                hit++;
                                if (hit >= attackExposure.get() / 100 * 27) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            case Any -> {
                ((IVec3) vec).meteor$set(box.minX, box.minY, box.minZ);
                double xw = box.maxX - box.minX;
                double yh = box.maxY - box.minY;
                double zw = box.maxZ - box.minZ;

                for (int x = 0; x <= 2; x += 1) {
                    for (int y = 0; y <= 2; y += 1) {
                        for (int z = 0; z <= 2; z += 1) {
                            ((IClipContext) ClipContext).blackout$setTo(vec.add(Mth.lerp(x / 2f, 0.1, xw - 0.1), Mth.lerp(y / 2f, 0.0, yh - 0.1), Mth.lerp(z / 2f, 0.1, zw - 0.1)));

                            result = BODamageUtils.raycast(ClipContext);
                            if (result.getType() != HitResult.Type.BLOCK) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void updateContext() {
        if (ClipContext == null) {
            ClipContext = new ClipContext(mc.player.getEyePosition(), null, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, mc.player);
        } else {
            ((IClipContext) ClipContext).blackout$setFrom(mc.player.getEyePosition());
        }
    }
}
