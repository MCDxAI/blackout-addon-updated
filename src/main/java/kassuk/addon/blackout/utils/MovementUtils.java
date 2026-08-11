package kassuk.addon.blackout.utils;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.meteorclient.mixininterface.IVec3;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MovementUtils {
  public static double xMovement(double speed, double yaw) {
    return Math.cos(Math.toRadians(yaw + 90)) * speed;
  }

  public static double zMovement(double speed, double yaw) {
    return Math.sin(Math.toRadians(yaw + 90)) * speed;
  }

  public static double getSpeed(double baseSpeed) {
    if (mc.player.hasEffect(MobEffects.SPEED)) {
      baseSpeed *= 1.2 + mc.player.getEffect(MobEffects.SPEED).getAmplifier() * 0.2;
    }
    if (mc.player.hasEffect(MobEffects.SLOWNESS)) {
      baseSpeed /= 1.2 + mc.player.getEffect(MobEffects.SLOWNESS).getAmplifier() * 0.2;
    }
    if (mc.player.isShiftKeyDown()) {
      baseSpeed *= 0.3;
    }
    return baseSpeed;
  }

  public static void moveTowards(
      Vec3 movement, double baseSpeed, Vec3 vec, int step, int reverseStep) {
    double speed = getSpeed(baseSpeed);

    double yaw = RotationUtils.getYaw(mc.player.position(), vec);

    double xm = xMovement(speed, yaw);
    double zm = zMovement(speed, yaw);

    double xd = vec.x - mc.player.getX();
    double zd = vec.z - mc.player.getZ();

    double x = Math.abs(xm) <= Math.abs(xd) ? xm : xd;
    double z = Math.abs(zm) <= Math.abs(zd) ? zm : zd;

    y(movement, x, z, step, reverseStep);

    ((IVec3) movement).meteor$setXZ(x, z);
  }

  private static void y(Vec3 movement, double x, double z, int step, int rev) {
    // Step
    if (mc.player.onGround()
        && !OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox())
        && OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().move(x, 0, z))) {

      double s = getStep(mc.player.getBoundingBox().move(x, 0, z), step);

      if (s > 0) {
        ((IVec3) movement).meteor$setY(s);
        mc.player.setDeltaMovement(
            mc.player.getDeltaMovement().x, 0, mc.player.getDeltaMovement().z);
      }
      return;
    }

    // Reverse
    if (mc.player.onGround()
        && !OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().move(x, -0.04, z))) {

      double s = getReverse(mc.player.getBoundingBox(), rev);

      if (s > 0) {
        ((IVec3) movement).meteor$setY(-s);
        mc.player.setDeltaMovement(
            mc.player.getDeltaMovement().x, 0, mc.player.getDeltaMovement().z);
      }
    }
  }

  private static double getStep(AABB box, int step) {
    for (double i = 0; i <= step + 0.125; i += 0.125) {
      if (!OLEPOSSUtils.inside(mc.player, box.move(0, i, 0))) {
        return i;
      }
    }
    return 0;
  }

  private static double getReverse(AABB box, int reverse) {
    for (double i = 0; i <= reverse; i += 0.125) {
      if (OLEPOSSUtils.inside(mc.player, box.move(0, -i - 0.125, 0))) {
        return i;
      }
    }
    return 0;
  }
}
