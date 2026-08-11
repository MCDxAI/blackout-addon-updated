package kassuk.addon.blackout.utils;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NCPRaytracer {
  public static boolean raytrace(Vec3 from, Vec3 to, AABB box) {
    int lx = 0, ly = 0, lz = 0;

    for (float i = 0; i < 1; i += 0.001) {
      double x = lerp(from.x, to.x, i);
      double y = lerp(from.y, to.y, i);
      double z = lerp(from.z, to.z, i);

      if (box.contains(x, y, z)) return true;

      int ix = (int) Math.floor(x);
      int iy = (int) Math.floor(y);
      int iz = (int) Math.floor(z);

      if (lx != ix || ly != iy || lz != iz) {

        BlockPos pos = new BlockPos(ix, iy, iz);

        if (validForCheck(pos, mc.level.getBlockState(pos))) return false;
      }

      lx = ix;
      ly = iy;
      lz = iz;
    }
    return false;
  }

  private static double lerp(double from, double to, double delta) {
    return from + (to - from) * delta;
  }

  public static boolean validForCheck(BlockPos pos, BlockState state) {
    if (state.isSolid()) return true;
    if (state.getBlock() instanceof LiquidBlock) return false;
    if (state.getBlock() instanceof StairBlock) return false;
    if (state.hasBlockEntity()) return false;

    return state.isCollisionShapeFullBlock(mc.level, pos);
  }
}
