package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.mixins.IBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class OLEPOSSUtils {
    public static Vec3 feet(AABB box) {
        return new Vec3((box.minX + box.maxX) / 2, box.minY, (box.minZ + box.maxZ) / 2);
    }

    public static boolean hasAquaAffinity(LivingEntity entity) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (hasEnchantment(Enchantments.AQUA_AFFINITY, stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEnchantment(ResourceKey<Enchantment> enchantment, ItemStack stack) {
        ItemEnchantments v = stack.getEnchantments();
        Identifier enchantmentId = enchantment.identifier();
        return v.keySet().stream().anyMatch(entry -> entry.is(enchantmentId));
    }

    public static int getLevel(ResourceKey<Enchantment> enchantment, ItemStack stack) {
        ItemEnchantments v = stack.getEnchantments();
        Identifier enchantmentId = enchantment.identifier();
        return v.keySet().stream().filter(entry -> entry.is(enchantmentId)).map(v::getLevel).max(Comparator.comparingInt(level -> level)).orElse(0);
    }

    public static Vec3 getMiddle(AABB box) {
        return new Vec3((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2);
    }

    public static boolean inside(Player en, AABB bb) {
        return mc.level != null && mc.level.getBlockCollisions(en, bb).iterator().hasNext();
    }

    public static int closerToZero(int x) {
        return (int) (x - Math.signum(x));
    }

    public static Vec3 getClosest(Vec3 pPos, Vec3 middle, double width, double height) {
        return new Vec3(Math.min(Math.max(pPos.x, middle.x - width / 2), middle.x + width / 2),
            Math.min(Math.max(pPos.y, middle.y), middle.y + height),
            Math.min(Math.max(pPos.z, middle.z - width / 2), middle.z + width / 2));
    }

    @SuppressWarnings({"DataFlowIssue", "BooleanMethodIsAlwaysInverted"})
    public static boolean strictDir(BlockPos pos, Direction dir) {
        return switch (dir) {
            case DOWN -> mc.player.getEyePosition().y <= pos.getY() + 0.5;
            case UP -> mc.player.getEyePosition().y >= pos.getY() + 0.5;
            case NORTH -> mc.player.getZ() < pos.getZ();
            case SOUTH -> mc.player.getZ() >= pos.getZ() + 1;
            case WEST -> mc.player.getX() < pos.getX();
            case EAST -> mc.player.getX() >= pos.getX() + 1;
        };
    }

    public static AABB getCrystalBox(BlockPos pos) {
        return new AABB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5);
    }

    public static AABB getCrystalBox(Vec3 pos) {
        return new AABB(pos.x() - 1, pos.y(), pos.z() - 1, pos.x() + 1, pos.y() + 2, pos.z() + 1);
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean replaceable(BlockPos block) {
        return ((IBlockSettings) BlockBehaviour.Properties.ofFullCopy(mc.level.getBlockState(block).getBlock())).blackout$replaceable();
    }

    public static boolean solid2(BlockPos block) {
        return mc.level.getBlockState(block).isSolid();
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "DataFlowIssue"})
    public static boolean solid(BlockPos block) {
        Block b = mc.level.getBlockState(block).getBlock();
        return !(b instanceof BaseFireBlock || b instanceof LiquidBlock || b instanceof AirBlock);
    }

    public static boolean isGapple(Item item) {
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
    }

    public static boolean isGapple(ItemStack stack) {
        return isGapple(stack.getItem());
    }

    public static boolean collidable(BlockPos block) {
        return !mc.level.getBlockState(block).getCollisionShape(mc.level, block).isEmpty();
    }
}
