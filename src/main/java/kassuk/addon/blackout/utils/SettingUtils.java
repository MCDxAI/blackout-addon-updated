package kassuk.addon.blackout.utils;

import java.util.function.Predicate;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.globalsettings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * @author OLEPOSSU
 */
public class SettingUtils {
  private static final FacingSettings facing = Modules.get().get(FacingSettings.class);
  private static final RangeSettings range = Modules.get().get(RangeSettings.class);
  private static final RaytraceSettings raytrace = Modules.get().get(RaytraceSettings.class);
  private static final RotationSettings rotation = Modules.get().get(RotationSettings.class);
  private static final ServerSettings server = Modules.get().get(ServerSettings.class);
  private static final SwingSettings swing = Modules.get().get(SwingSettings.class);

  //  Range
  public static void registerAttack(AABB bb) {
    range.registerAttack(bb);
  }

  public static double getPlaceRange() {
    return range.placeRange.get();
  }

  public static double getPlaceWallsRange() {
    return range.placeRangeWalls.get();
  }

  public static double getAttackRange() {
    return range.attackRange.get();
  }

  public static double getAttackWallsRange() {
    return range.attackRangeWalls.get();
  }

  public static double getMineRange() {
    return range.miningRange.get();
  }

  public static double getMineWallsRange() {
    return range.miningRangeWalls.get();
  }

  public static double placeRangeTo(BlockPos pos) {
    return range.placeRangeTo(pos, null);
  }

  public static boolean inPlaceRange(BlockPos pos) {
    return range.inPlaceRange(pos, null);
  }

  public static boolean inPlaceRange(BlockPos pos, Vec3 from) {
    return range.inPlaceRange(pos, from);
  }

  public static boolean inPlaceRangeNoTrace(BlockPos pos) {
    return range.inPlaceRangeNoTrace(pos, null);
  }

  public static boolean inPlaceRangeNoTrace(BlockPos pos, Vec3 from) {
    return range.inPlaceRangeNoTrace(pos, from);
  }

  public static boolean inAttackRange(AABB bb) {
    return range.inAttackRange(bb, null);
  }

  public static boolean inAttackRange(AABB bb, Vec3 from) {
    return range.inAttackRange(bb, from);
  }

  public static double mineRangeTo(BlockPos pos) {
    return range.miningRangeTo(pos, null);
  }

  public static boolean inMineRange(BlockPos pos) {
    return range.inMineRange(pos);
  }

  public static boolean inMineRangeNoTrace(BlockPos pos) {
    return range.inMineRangeNoTrace(pos);
  }

  public static boolean inAttackRangeNoTrace(AABB bb, double eyeHeight, Vec3 feet) {
    return range.inAttackRangeNoTrace(bb, feet, null);
  }

  public static boolean inAttackRangeNoTrace(AABB bb, double eyeHeight, Vec3 feet, Vec3 from) {
    return range.inAttackRangeNoTrace(bb, feet, from);
  }

  public static double attackRangeTo(AABB bb, Vec3 feet) {
    return range.attackRangeTo(bb, feet, null, true);
  }

  //  Rotate
  public static boolean startMineRot() {
    return rotation.startMineRot();
  }

  public static boolean endMineRot() {
    return rotation.endMineRot();
  }

  public static boolean shouldVanillaRotate() {
    return rotation.vanillaRotation.get();
  }

  public static boolean shouldRotate(RotationType type) {
    return rotation.shouldRotate(type);
  }

  public static boolean rotationCheck(AABB box, RotationType type) {
    return rotation.rotationCheck(box, type);
  }

  //  Swing
  public static void swing(SwingState state, SwingType type, InteractionHand hand) {
    swing.swing(state, type, hand);
  }

  public static void mineSwing(SwingSettings.MiningSwingState state) {
    swing.mineSwing(state);
  }

  //  Facing
  public static PlaceData getPlaceData(BlockPos pos) {
    return facing.getPlaceData(pos, true);
  }

  public static PlaceData getPlaceDataANDDir(BlockPos pos, Predicate<Direction> predicate) {
    return facing.getPlaceDataAND(pos, predicate, null, true);
  }

  public static PlaceData getPlaceDataANDPos(BlockPos pos, Predicate<BlockPos> predicate) {
    return facing.getPlaceDataAND(pos, null, predicate, true);
  }

  public static PlaceData getPlaceDataAND(
      BlockPos pos, Predicate<Direction> predicateDir, Predicate<BlockPos> predicate) {
    return facing.getPlaceDataAND(pos, predicateDir, predicate, true);
  }

  public static PlaceData getPlaceDataOR(BlockPos pos, Predicate<BlockPos> predicate) {
    return facing.getPlaceDataOR(pos, predicate, true);
  }

  public static PlaceData getPlaceData(BlockPos pos, boolean ignoreContainers) {
    return facing.getPlaceData(pos, ignoreContainers);
  }

  public static PlaceData getPlaceDataANDDir(
      BlockPos pos, Predicate<Direction> predicate, boolean ignoreContainers) {
    return facing.getPlaceDataAND(pos, predicate, null, ignoreContainers);
  }

  public static PlaceData getPlaceDataANDPos(
      BlockPos pos, Predicate<BlockPos> predicate, boolean ignoreContainers) {
    return facing.getPlaceDataAND(pos, null, predicate, ignoreContainers);
  }

  public static PlaceData getPlaceDataAND(
      BlockPos pos,
      Predicate<Direction> predicateDir,
      Predicate<BlockPos> predicate,
      boolean ignoreContainers) {
    return facing.getPlaceDataAND(pos, predicateDir, predicate, ignoreContainers);
  }

  public static PlaceData getPlaceDataOR(
      BlockPos pos, Predicate<BlockPos> predicate, boolean ignoreContainers) {
    return facing.getPlaceDataOR(pos, predicate, ignoreContainers);
  }

  public static Direction getPlaceOnDirection(BlockPos pos) {
    return facing.getPlaceOnDirection(pos);
  }

  //  Raytracing
  public static boolean shouldPlaceTrace() {
    return raytrace.placeTrace.get();
  }

  public static boolean shouldAttackTrace() {
    return raytrace.attackTrace.get();
  }

  public static boolean placeTrace(BlockPos pos) {
    return !shouldPlaceTrace() || raytrace.placeTrace(pos);
  }

  public static boolean attackTrace(AABB bb) {
    return !shouldAttackTrace() || raytrace.attackTrace(bb);
  }

  // Server
  public static boolean oldDamage() {
    return server.oldVerDamage.get();
  }

  public static boolean oldCrystals() {
    return server.oldVerCrystals.get();
  }

  public static boolean cc() {
    return server.cc.get();
  }
}
