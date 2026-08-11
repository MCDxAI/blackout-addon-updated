/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/utils/player/DamageUtils.java
*/

package kassuk.addon.blackout.utils.meteor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import java.util.ArrayList;
import java.util.List;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.mixininterface.IClipContext;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;

public class BODamageUtils {
  public static ClipContext raycastContext;

  @PreInit
  public static void init() {
    MeteorClient.EVENT_BUS.subscribe(BODamageUtils.class);
  }

  @EventHandler
  public static void onGameJoin(GameJoinedEvent event) {
    raycastContext =
        new ClipContext(
            Vec3.ZERO, Vec3.ZERO, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);
  }

  public static double crystalDamage(
      LivingEntity entity, AABB box, Vec3 pos, boolean ignoreTerrain) {
    return crystalDamage(entity, box, pos, null, ignoreTerrain);
  }

  public static double crystalDamage(
      LivingEntity entity, AABB box, Vec3 pos, BlockPos ignorePos, boolean ignoreTerrain) {
    return explosionDamage(entity, box, pos, ignorePos, ignoreTerrain, 6);
  }

  public static double crystalDamage(
      LivingEntity entity,
      AABB box,
      Vec3 pos,
      BlockPos ignorePos,
      BlockPos obbyPos,
      boolean ignoreTerrain) {
    return explosionDamage(entity, box, pos, ignorePos, obbyPos, ignoreTerrain, 6);
  }

  public static double anchorDamage(
      LivingEntity entity, AABB box, Vec3 pos, boolean ignoreTerrain) {
    return explosionDamage(entity, box, pos, null, ignoreTerrain, 5);
  }

  public static double anchorDamage(
      LivingEntity entity, AABB box, Vec3 pos, BlockPos ignorePos, boolean ignoreTerrain) {
    return explosionDamage(entity, box, pos, ignorePos, ignoreTerrain, 5);
  }

  private static double explosionDamage(
      LivingEntity entity,
      AABB box,
      Vec3 pos,
      BlockPos ignorePos,
      boolean ignoreTerrain,
      double strength) {
    return explosionDamage(entity, box, pos, ignorePos, null, ignoreTerrain, strength);
  }

  public static double getBaseDamage(
      AABB box,
      Vec3 pos,
      BlockPos ignorePos,
      BlockPos obbyPos,
      boolean ignoreTerrain,
      double strength) {
    double q = strength * 2;
    double dist = OLEPOSSUtils.feet(box).distanceTo(pos) / q;

    if (dist > 1.0) return 0;

    double aa = getExposure(pos, box, ignorePos, obbyPos, ignoreTerrain);
    double ab = (1.0 - dist) * aa;

    return (float) ((int) ((ab * ab + ab) * 3.5 * q + 1.0));
  }

  private static double explosionDamage(
      LivingEntity entity,
      AABB box,
      Vec3 pos,
      BlockPos ignorePos,
      BlockPos obbyPos,
      boolean ignoreTerrain,
      double strength) {
    if (box == null) return 0;

    double damage = getBaseDamage(box, pos, ignorePos, obbyPos, ignoreTerrain, strength);

    damage = difficultyDamage(damage);
    damage = applyArmor(entity, damage);
    damage = applyResistance(entity, damage);
    damage = applyProtection(entity, damage, true);

    return damage;
  }

  public static int getProtectionAmount(Iterable<ItemStack> equipment, boolean explosion) {
    MutableInt mint = new MutableInt();

    for (ItemStack stack : equipment) {
      if (stack.isEmpty()) continue;

      ItemEnchantments enchantments = stack.getEnchantments();

      enchantments.keySet().stream()
          .forEach(
              entry -> {
                int level = enchantments.getLevel(entry);
                if (entry.is(Enchantments.PROTECTION.identifier())) mint.add(level);
                else if (explosion && entry.is(Enchantments.BLAST_PROTECTION.identifier()))
                  mint.add(level * 2);
              });
    }

    return mint.intValue();
  }

  public static double difficultyDamage(double damage) {
    Difficulty difficulty = mc.level.getDifficulty();
    if (difficulty == Difficulty.EASY) return Math.min(damage / 2 + 1, damage);
    if (difficulty == Difficulty.NORMAL) return damage;

    return damage * 1.5;
  }

  public static double applyArmor(LivingEntity entity, double damage) {
    double armor = entity.getArmorValue();
    double f = 2 + entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS) / 4;

    return damage * (1 - Mth.clamp(armor - damage / f, armor * 0.2, 20) / 25);
  }

  public static double applyResistance(LivingEntity entity, double damage) {
    int amplifier =
        entity.hasEffect(MobEffects.RESISTANCE)
            ? entity.getEffect(MobEffects.RESISTANCE).getAmplifier()
            : 0;

    int j = 25 - (amplifier + 1) * 5;
    return Math.max(damage * j / 25, 0);
  }

  public static double applyProtection(LivingEntity entity, double damage, boolean explosions) {
    int protectionAmount = 0;

    List<ItemStack> armorItems = new ArrayList<>();
    for (EquipmentSlot slot :
        new EquipmentSlot[] {
          EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
      armorItems.add(entity.getItemBySlot(slot));
    }
    protectionAmount = getProtectionAmount(armorItems, explosions);

    if (protectionAmount > 0) {
      damage *= (1 - Mth.clamp(protectionAmount, 0f, 20f) / 25);
    }

    return damage;
  }

  public static double getExposure(Vec3 source, AABB box) {
    return getExposure(source, box, null, null, true);
  }

  public static double getExposure(
      Vec3 source, AABB box, BlockPos ignorePos, boolean ignoreTerrain) {
    return getExposure(source, box, ignorePos, null, ignoreTerrain);
  }

  public static double getExposure(
      Vec3 source, AABB box, BlockPos ignorePos, BlockPos obbyPos, boolean ignoreTerrain) {
    ((IClipContext) raycastContext)
        .meteor$set(source, null, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);

    double lx = box.getXsize();
    double ly = box.getYsize();
    double lz = box.getZsize();

    double deltaX = 1 / (lx * 2 + 1);
    double deltaY = 1 / (ly * 2 + 1);
    double deltaZ = 1 / (lz * 2 + 1);

    double offsetX = (1 - Math.floor(1 / deltaX) * deltaX) / 2;
    double offsetZ = (1 - Math.floor(1 / deltaZ) * deltaZ) / 2;

    double stepX = deltaX * lx;
    double stepY = deltaY * ly;
    double stepZ = deltaZ * lz;

    if (stepX < 0 || stepY < 0 || stepZ < 0) return 0;

    float i = 0;
    float j = 0;

    for (double x = box.minX + offsetX, maxX = box.maxX + offsetX; x <= maxX; x += stepX) {
      for (double y = box.minY; y <= box.maxY; y += stepY) {
        for (double z = box.minZ + offsetZ, maxZ = box.maxZ + offsetZ; z <= maxZ; z += stepZ) {
          Vec3 vec3d = new Vec3(x, y, z);

          ((IClipContext) raycastContext)
              .meteor$set(
                  source, vec3d, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);
          if (raycast(BODamageUtils.raycastContext, ignorePos, obbyPos, ignoreTerrain).getType()
              == HitResult.Type.MISS) ++i;

          ++j;
        }
      }
    }

    return i / j;
  }

  public static BlockHitResult raycast(ClipContext context) {
    return raycast(context, false);
  }

  public static BlockHitResult raycast(ClipContext context, boolean ignoreTerrain) {
    return raycast(context, null, null, ignoreTerrain);
  }

  public static BlockHitResult raycast(ClipContext context, BlockPos ignorePos, BlockPos obbyPos) {
    return raycast(context, ignorePos, obbyPos, false);
  }

  public static BlockHitResult raycast(
      ClipContext context, BlockPos ignorePos, BlockPos obbyPos, boolean ignoreTerrain) {
    return BlockGetter.traverseBlocks(
        context.getFrom(),
        context.getTo(),
        context,
        (contextx, pos) -> {
          BlockState blockState;

          if (pos.equals(obbyPos)) blockState = Blocks.OBSIDIAN.defaultBlockState();
          else if (pos.equals(ignorePos)) blockState = Blocks.AIR.defaultBlockState();
          else {
            BlockState state = mc.level.getBlockState(pos);

            if (ignoreTerrain && state.getBlock().getExplosionResistance() < 200)
              blockState = Blocks.AIR.defaultBlockState();
            else blockState = state;
          }

          Vec3 vec3d = contextx.getFrom();
          Vec3 vec3d2 = contextx.getTo();

          VoxelShape voxelShape = contextx.getBlockShape(blockState, mc.level, pos);

          return mc.level.clipWithInteractionOverride(vec3d, vec3d2, pos, voxelShape, blockState);
        },
        (contextx) -> {
          Vec3 vec3d = contextx.getFrom().subtract(contextx.getTo());
          return BlockHitResult.miss(
              contextx.getTo(),
              Direction.getApproximateNearest(vec3d.x, vec3d.y, vec3d.z),
              BlockPos.containing(contextx.getTo()));
        });
  }
}
