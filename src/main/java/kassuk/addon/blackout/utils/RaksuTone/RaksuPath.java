package kassuk.addon.blackout.utils.RaksuTone;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class RaksuPath {
  public final List<Movement> path = new ArrayList<>();

  public int step = 2;
  public int reverseStep = 3;
  public double speed = 0.2873;
  public int fallDist = 150;

  private List<Direction> dirs = null;

  public void calculate(int blocks, BlockPos target, boolean opposite) {
    BlockPos pos = mc.player.blockPosition().immutable();

    if (!is(pos.below())
        && !OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().move(0, -0.2, 0))) {
      for (int i = 0; i < fallDist; i++) {

        if (is(pos.below(i + 1))) {
          path.add(new Movement(true, pos.below(i), MovementType.Fall));
          break;
        }
      }
    }

    for (int i = 0; i < blocks; i++) {
      Movement m = nextPos(pos, target, true, opposite);

      if (m == null || !m.valid) {
        return;
      }

      if (pos.equals(m.pos)) {
        return;
      }

      pos = m.pos;
      path.add(m);
    }
  }

  private Movement nextPos(BlockPos pos, BlockPos target, boolean stuckCheck, boolean reversed) {
    closestDir(pos, target, reversed);

    for (Direction dir : dirs) {
      Movement m = getMovement(pos, dir);

      if (!m.valid()) {
        continue;
      }
      // Stuck check
      if (stuckCheck) {
        Movement m1 = nextPos(m.pos, target, false, reversed);
        if (m1 != null && m1.valid && m1.pos.equals(pos)) {
          continue;
        }
      }
      return m;
    }
    return null;
  }

  private Movement getMovement(BlockPos pos, Direction dir) {
    if (canWalkTrough(pos, dir)) {

      if (is(pos.relative(dir).below())) {
        return new Movement(true, pos.relative(dir), MovementType.Move);
      } else {
        Movement m = getFall(pos, dir);

        if (m.valid) {
          return m;
        }
      }
      return new Movement(false, null, null);
    }

    Movement m = getStep(pos, dir);

    if (m.valid) {
      return m;
    }
    return new Movement(false, null, null);
  }

  private Movement getStep(BlockPos pos, Direction dir) {
    for (int i = 1; i <= step; i++) {
      if (is(pos.above(i + 1))) {
        return new Movement(false, null, null);
      }
      if (!is(pos.relative(dir).above(i - 1))) {
        continue;
      }
      if (is(pos.relative(dir).above(i)) || is(pos.relative(dir).above(i + 1))) {
        continue;
      }

      return new Movement(true, pos.relative(dir).above(i), MovementType.Step);
    }
    return new Movement(false, null, null);
  }

  private Movement getFall(BlockPos pos, Direction dir) {
    for (int i = 0; i < fallDist; i++) {
      if (is(pos.relative(dir).below(i + 1))) {
        if (i < reverseStep)
          return new Movement(true, pos.relative(dir).below(i), MovementType.Reverse);
        else return new Movement(true, pos.relative(dir).below(i), MovementType.Fall);
      }
    }
    return new Movement(false, null, null);
  }

  private boolean canWalkTrough(BlockPos pos, Direction dir) {
    return !is(pos.relative(dir)) && !is(pos.relative(dir).above());
  }

  private void closestDir(BlockPos from, BlockPos target, boolean reversed) {
    if (reversed) {
      Comparator<Direction> c =
          Comparator.comparingDouble(
              i -> Vec3.atCenterOf(from.relative(i)).distanceTo(Vec3.atCenterOf(target)));
      dirs =
          Arrays.stream(
                  new Direction[] {
                    Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH
                  })
              .sorted(c.reversed())
              .toList();
    } else {
      dirs =
          Arrays.stream(
                  new Direction[] {
                    Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH
                  })
              .sorted(
                  Comparator.comparingDouble(
                      i -> Vec3.atCenterOf(from.relative(i)).distanceTo(Vec3.atCenterOf(target))))
              .toList();
    }
  }

  private boolean is(BlockPos pos) {
    return !mc.level.getBlockState(pos).getCollisionShape(mc.level, pos).isEmpty();
  }

  public record Movement(boolean valid, BlockPos pos, MovementType type) {}

  public enum MovementType {
    Step,
    Reverse,
    Fall,
    Move
  }
}
