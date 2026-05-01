package kassuk.addon.blackout.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class RotationUtils {
    public static float nextYaw(double current, double target, double step) {
        double i = yawAngle(current, target);

        if (step >= Math.abs(i)) {
            return (float) (current + i);
        } else {
            return (float) (current + (i < 0 ? -1 : 1) * step);
        }
    }

    public static double yawAngle(double current, double target) {
        double c = Mth.wrapDegrees(current) + 180, t = Mth.wrapDegrees(target) + 180;
        if (c > t) {
            return t + 360 - c < Math.abs(c - t) ? 360 - c + t : t - c;
        } else {
            return 360 - t + c < Math.abs(c - t) ? -(360 - t + c) : t - c;
        }
    }

    public static float nextPitch(double current, double target, double step) {
        double i = target - current;

        return (float) (Math.abs(i) <= step ? target : i >= 0 ? current + step : current - step);
    }

    public static double radAngle(Vec2 vec1, Vec2 vec2) {
        double p = vec1.x * vec2.x + vec1.y * vec2.y;
        p /= Math.sqrt(vec1.x * vec1.x + vec1.y * vec1.y);
        p /= Math.sqrt(vec2.x * vec2.x + vec2.y * vec2.y);
        return Math.acos(p);
    }

    // These 2 are from meteor rotation utils
    public static double getYaw(Vec3 start, Vec3 target) {
        return mc.player.getYRot() + Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(target.z() - start.z(), target.x() - start.x())) - 90f - mc.player.getYRot());
    }

    public static double getPitch(Vec3 start, Vec3 target) {
        double diffX = target.x() - start.x();
        double diffY = target.y() - start.y();
        double diffZ = target.z() - start.z();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getXRot() + Mth.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getXRot());
    }
}
