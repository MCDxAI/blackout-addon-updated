package kassuk.addon.blackout.modules;

import java.util.Objects;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.*;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.*;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

/**
 * @author Marc3D Ported from Meta's AutoMineCart module
 */
public class AutoCart extends BlackOutModule {
  public AutoCart() {
    super(
        BlackOut.BLACKOUT,
        "AutoCart",
        "Automatically places and ignites TNT minecarts on enemies.");
  }

  private final SettingGroup sgGeneral = settings.getDefaultGroup();
  private final SettingGroup sgPlacement = settings.createGroup("Placement");
  private final SettingGroup sgRender = settings.createGroup("Render");

  // --------------------General--------------------//
  private final Setting<Double> range =
      sgGeneral.add(
          new DoubleSetting.Builder()
              .name("Range")
              .description("Maximum distance to target enemies.")
              .defaultValue(6.0)
              .min(1.0)
              .sliderMax(10.0)
              .build());
  private final Setting<Boolean> onlyHole =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("Only Safe Target")
              .description("Only targets enemies in holes.")
              .defaultValue(false)
              .build());
  private final Setting<Boolean> onlySelfHole =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("Only When Safe")
              .description("Only places when you are in a hole.")
              .defaultValue(false)
              .build());
  private final Setting<Integer> extrapolation =
      sgGeneral.add(
          new IntSetting.Builder()
              .name("Extrapolation")
              .description("How many ticks to predict enemy movement.")
              .defaultValue(0)
              .range(0, 100)
              .sliderMax(20)
              .build());
  private final Setting<Integer> extSmoothness =
      sgGeneral.add(
          new IntSetting.Builder()
              .name("Extrapolation Smoothness")
              .description("How many ticks to use for average motion calculation.")
              .defaultValue(2)
              .range(1, 20)
              .sliderMax(20)
              .build());

  // --------------------Placement--------------------//
  private final Setting<Integer> placeDelay =
      sgPlacement.add(
          new IntSetting.Builder()
              .name("Place Delay")
              .description("Delay in milliseconds between placements.")
              .defaultValue(100)
              .range(0, 5000)
              .sliderMax(1000)
              .build());
  private final Setting<SwitchMode> switchMode =
      sgPlacement.add(
          new EnumSetting.Builder<SwitchMode>()
              .name("Switch Mode")
              .description("How to switch items.")
              .defaultValue(SwitchMode.Silent)
              .build());
  private final Setting<Boolean> limitPlacements =
      sgPlacement.add(
          new BoolSetting.Builder()
              .name("Limit Placements")
              .description("Limit the number of minecarts placed.")
              .defaultValue(false)
              .build());
  private final Setting<Integer> placementLimit =
      sgPlacement.add(
          new IntSetting.Builder()
              .name("Placement Limit")
              .description("Max TNT minecarts placed per target position.")
              .defaultValue(26)
              .range(1, 64)
              .sliderMax(64)
              .visible(limitPlacements::get)
              .build());
  private final Setting<Boolean> autoDisable =
      sgPlacement.add(
          new BoolSetting.Builder()
              .name("Auto Disable")
              .description("Automatically disable after reaching threshold.")
              .defaultValue(false)
              .build());
  private final Setting<Integer> disableThreshold =
      sgPlacement.add(
          new IntSetting.Builder()
              .name("Disable Threshold")
              .description("Number of placements before disabling.")
              .defaultValue(21)
              .range(1, 30)
              .sliderMax(30)
              .visible(autoDisable::get)
              .build());
  private final Setting<Boolean> autoIgnite =
      sgPlacement.add(
          new BoolSetting.Builder()
              .name("Auto Ignite")
              .description("Automatically break rails and ignite minecarts.")
              .defaultValue(false)
              .build());
  private final Setting<Integer> igniteDelay =
      sgPlacement.add(
          new IntSetting.Builder()
              .name("Ignite Delay")
              .description("Ticks to wait before breaking rail and igniting.")
              .defaultValue(20)
              .range(0, 50)
              .sliderMax(50)
              .visible(autoIgnite::get)
              .build());

  // --------------------Render--------------------//
  private final Setting<Boolean> swing =
      sgRender.add(
          new BoolSetting.Builder()
              .name("Swing")
              .description("Swing hand when placing.")
              .defaultValue(true)
              .build());
  private final Setting<SwingHand> swingHand =
      sgRender.add(
          new EnumSetting.Builder<SwingHand>()
              .name("Swing Hand")
              .description("Which hand to swing.")
              .defaultValue(SwingHand.RealHand)
              .visible(swing::get)
              .build());
  private final Setting<Boolean> render =
      sgRender.add(
          new BoolSetting.Builder()
              .name("Render")
              .description("Render target position.")
              .defaultValue(true)
              .build());
  private final Setting<ShapeMode> shapeMode =
      sgRender.add(
          new EnumSetting.Builder<ShapeMode>()
              .name("Shape Mode")
              .description("How to render the box.")
              .defaultValue(ShapeMode.Both)
              .visible(render::get)
              .build());
  private final Setting<SettingColor> railStageColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("Rail Stage Color")
              .description("Color when placing rails.")
              .defaultValue(new SettingColor(255, 165, 0, 100))
              .visible(render::get)
              .build());
  private final Setting<SettingColor> cartStageColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("Cart Stage Color")
              .description("Color when placing minecarts.")
              .defaultValue(new SettingColor(255, 64, 64, 100))
              .visible(render::get)
              .build());
  private final Setting<SettingColor> igniteStageColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("Ignite Stage Color")
              .description("Color when igniting.")
              .defaultValue(new SettingColor(64, 255, 64, 100))
              .visible(() -> render.get() && autoIgnite.get())
              .build());

  // State
  private int placeCounter = 0;
  private Player currentTarget = null;
  private int igniteWait = 0;
  private boolean isPlacing = true;
  private long lastPlaceTime = 0;
  private Stage currentStage = Stage.RAIL;
  private BlockPos renderPos = null;

  @Override
  public void onActivate() {
    super.onActivate();
    resetState();
  }

  @Override
  public void onDeactivate() {
    super.onDeactivate();
    resetState();
  }

  private void resetState() {
    placeCounter = 0;
    currentTarget = null;
    igniteWait = 0;
    isPlacing = true;
    lastPlaceTime = 0;
    currentStage = Stage.RAIL;
    renderPos = null;
  }

  @Override
  public String getInfoString() {
    if (mc.player == null || mc.level == null) return null;

    if (autoIgnite.get()) {
      String status = isPlacing ? "Placing" : "Breaking";
      if (currentTarget != null) {
        return currentTarget.getName().getString() + " " + status;
      }
      return status;
    } else {
      if (currentTarget != null) {
        return currentTarget.getName().getString() + " " + placeCounter;
      }
      return String.valueOf(placeCounter);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  private void onTick(TickEvent.Post event) {
    if (mc.player == null || mc.level == null) return;

    if (autoIgnite.get()) {
      runIgnitePlacement();
    } else {
      runNormalPlacement();
    }
  }

  @EventHandler
  private void onRender(Render3DEvent event) {
    if (!render.get() || renderPos == null) return;

    SettingColor color =
        switch (currentStage) {
          case RAIL -> railStageColor.get();
          case CART -> cartStageColor.get();
          case IGNITE -> igniteStageColor.get();
        };

    AABB box =
        new AABB(
            renderPos.getX(),
            renderPos.getY(),
            renderPos.getZ(),
            renderPos.getX() + 1.0,
            renderPos.getY() + 0.125,
            renderPos.getZ() + 1.0);

    event.renderer.box(box, color, color, shapeMode.get(), 0);
  }

  private void runIgnitePlacement() {
    Player target = findTarget();
    if (target == null) {
      currentTarget = null;
      renderPos = null;
      return;
    }

    // Update current target
    if (currentTarget == null
        || !currentTarget.isAlive()
        || mc.player.distanceTo(currentTarget) > range.get()) {
      currentTarget = target;
      igniteWait = 0;
      isPlacing = true;
    }

    BlockPos targetPos = getPredictedPosition(currentTarget);
    if (mc.level.getBlockState(targetPos.below()).canBeReplaced()) {
      renderPos = null;
      return;
    }

    int cartCount = countMinecartsAt(targetPos);

    if (isPlacing) {
      // Stage 1: Place rail if needed
      if (!isRailAt(targetPos) && cartCount == 0) {
        if (!mc.level.getBlockState(targetPos).canBeReplaced()) {
          renderPos = null;
          return;
        }

        currentStage = Stage.RAIL;
        renderPos = targetPos;
        placeRail(targetPos);
        igniteWait = 0;
        return;
      }

      // Stage 2: Place cart if rail exists
      if (isRailAt(targetPos)) {
        currentStage = Stage.CART;
        renderPos = targetPos;

        if (System.currentTimeMillis() - lastPlaceTime >= placeDelay.get()) {
          placeCart(targetPos);
        }
      }

      // Wait for ignite delay
      if (igniteWait < igniteDelay.get()) {
        igniteWait++;
        return;
      }

      // Move to ignite stage
      isPlacing = false;
      igniteWait = 0;

    } else {
      // Stage 3: Break rail and ignite
      currentStage = Stage.IGNITE;

      if (isRailAt(targetPos) && cartCount > 0) {
        renderPos = targetPos;
        breakRail(targetPos);
      }

      if (!isRailAt(targetPos) && cartCount > 0) {
        renderPos = targetPos;
        igniteCart(targetPos);

        if (autoDisable.get()) {
          toggle();
        } else {
          isPlacing = true;
          igniteWait = 0;
        }
      }
    }
  }

  private void runNormalPlacement() {
    if (autoDisable.get() && placeCounter > disableThreshold.get()) {
      toggle();
      return;
    }

    Player target = findTarget();
    if (target == null) {
      currentTarget = null;
      renderPos = null;
      return;
    }

    // Update current target
    if (currentTarget == null
        || !currentTarget.isAlive()
        || mc.player.distanceTo(currentTarget) > range.get()) {
      currentTarget = target;
    }

    BlockPos targetPos = getPredictedPosition(currentTarget);
    if (mc.level.getBlockState(targetPos.below()).canBeReplaced()) {
      renderPos = null;
      return;
    }

    int cartCount = countMinecartsAt(targetPos);

    if (!isRailAt(targetPos)) {
      if (!mc.level.getBlockState(targetPos).canBeReplaced()) {
        renderPos = null;
        return;
      }

      currentStage = Stage.RAIL;
      renderPos = targetPos;
      placeRail(targetPos);
    } else {
      currentStage = Stage.CART;
      renderPos = targetPos;

      if (System.currentTimeMillis() - lastPlaceTime >= placeDelay.get()
          && (cartCount < placementLimit.get() || !limitPlacements.get())) {
        placeCart(targetPos);
      }
    }
  }

  private Player findTarget() {
    Player closest = null;
    double closestDist = range.get();

    for (Player player : mc.level.players()) {
      if (player == mc.player) continue;
      if (player.isSpectator()) continue;
      if (player.getHealth() <= 0) continue;
      if (Friends.get().isFriend(player)) continue;

      double dist = mc.player.distanceTo(player);
      if (dist > range.get()) continue;

      if (onlyHole.get() && !isInHole(player)) continue;
      if (onlySelfHole.get() && !isInHole(mc.player)) continue;

      if (closest == null || dist < closestDist) {
        closest = player;
        closestDist = dist;
      }
    }

    return closest;
  }

  // This matches the convention in Blocker and HoleFillPlus. HoleUtils.inHole is stricter
  // than the old 4-wall check. It also requires a solid floor and a known hole pattern.
  // This keeps AutoCart's onlyHole and onlySelfHole gates consistent with the rest of BlackOut.
  private boolean isInHole(Player player) {
    return HoleUtils.inHole(player);
  }

  private BlockPos getPredictedPosition(Player target) {
    if (extrapolation.get() <= 0) {
      return target.blockPosition();
    }

    AABB extrapolatedBox =
        ExtrapolationUtils.extrapolate(
            (AbstractClientPlayer) target, extrapolation.get(), extSmoothness.get());

    if (extrapolatedBox == null) {
      return target.blockPosition();
    }

    double centerX = (extrapolatedBox.minX + extrapolatedBox.maxX) / 2.0;
    double centerZ = (extrapolatedBox.minZ + extrapolatedBox.maxZ) / 2.0;
    double footY = extrapolatedBox.minY;

    return BlockPos.containing(centerX, footY, centerZ);
  }

  private boolean isRailAt(BlockPos pos) {
    return mc.level.getBlockState(pos).getBlock() == Blocks.RAIL
        || mc.level.getBlockState(pos).getBlock() == Blocks.ACTIVATOR_RAIL
        || mc.level.getBlockState(pos).getBlock() == Blocks.DETECTOR_RAIL
        || mc.level.getBlockState(pos).getBlock() == Blocks.POWERED_RAIL;
  }

  private int countMinecartsAt(BlockPos pos) {
    AABB box = new AABB(pos).inflate(0.5);
    return (int)
        mc.level.getEntitiesOfClass(MinecartTNT.class, box).stream()
            .filter(minecart -> minecart.isAlive())
            .count();
  }

  private MinecartTNT findMinecartAt(BlockPos pos) {
    AABB box = new AABB(pos).inflate(0.5);
    for (MinecartTNT minecart : mc.level.getEntitiesOfClass(MinecartTNT.class, box)) {
      if (minecart.isAlive()) return minecart;
    }
    return null;
  }

  private void placeRail(BlockPos pos) {
    if (!SettingUtils.inPlaceRange(pos)) return;

    FindItemResult rail = InvUtils.find(Items.RAIL);
    if (!rail.found()) return;

    boolean switched = false;
    InteractionHand hand = null;

    if (mc.player.getMainHandItem().is(Items.RAIL)) {
      hand = InteractionHand.MAIN_HAND;
    } else if (mc.player.getOffhandItem().is(Items.RAIL)) {
      hand = InteractionHand.OFF_HAND;
    } else {
      switched = true;
      switch (switchMode.get()) {
        case Silent, Normal -> InvUtils.swap(rail.slot(), true);
        case PickSilent -> BOInvUtils.pickSwitch(rail.slot());
        case InvSwitch -> BOInvUtils.invSwitch(rail.slot());
      }
      hand = InteractionHand.MAIN_HAND;
    }

    BlockPos placeOn = pos.below();
    Direction placeDir = Direction.UP;

    boolean rotated =
        !SettingUtils.shouldRotate(RotationType.BlockPlace)
            || Managers.ROTATION.start(
                placeOn.relative(placeDir),
                priority,
                RotationType.BlockPlace,
                Objects.hash(name + "rail"));

    if (rotated) {
      placeBlock(hand, placeOn.getCenter(), placeDir, placeOn);

      if (swing.get()) clientSwing(swingHand.get(), hand);
      if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
        Managers.ROTATION.end(Objects.hash(name + "rail"));
      }
    }

    if (switched) {
      switch (switchMode.get()) {
        case Silent -> InvUtils.swapBack();
        case PickSilent -> BOInvUtils.pickSwapBack();
        case InvSwitch -> BOInvUtils.swapBack();
      }
    }
  }

  private void placeCart(BlockPos pos) {
    if (!SettingUtils.inPlaceRange(pos)) return;

    FindItemResult cart = InvUtils.find(Items.TNT_MINECART);
    if (!cart.found()) return;

    boolean switched = false;
    InteractionHand hand = null;

    if (mc.player.getMainHandItem().is(Items.TNT_MINECART)) {
      hand = InteractionHand.MAIN_HAND;
    } else if (mc.player.getOffhandItem().is(Items.TNT_MINECART)) {
      hand = InteractionHand.OFF_HAND;
    } else {
      switched = true;
      switch (switchMode.get()) {
        case Silent, Normal -> InvUtils.swap(cart.slot(), true);
        case PickSilent -> BOInvUtils.pickSwitch(cart.slot());
        case InvSwitch -> BOInvUtils.invSwitch(cart.slot());
      }
      hand = InteractionHand.MAIN_HAND;
    }

    boolean rotated =
        !SettingUtils.shouldRotate(RotationType.Interact)
            || Managers.ROTATION.start(
                pos, priority, RotationType.Interact, Objects.hash(name + "cart"));

    if (rotated) {
      interactBlock(hand, pos.getCenter(), Direction.DOWN, pos);
      lastPlaceTime = System.currentTimeMillis();
      placeCounter++;

      if (swing.get()) clientSwing(swingHand.get(), hand);
      if (SettingUtils.shouldRotate(RotationType.Interact)) {
        Managers.ROTATION.end(Objects.hash(name + "cart"));
      }
    }

    if (switched) {
      switch (switchMode.get()) {
        case Silent -> InvUtils.swapBack();
        case PickSilent -> BOInvUtils.pickSwapBack();
        case InvSwitch -> BOInvUtils.swapBack();
      }
    }
  }

  private void breakRail(BlockPos pos) {
    FindItemResult pickaxe =
        InvUtils.find(
            itemStack ->
                itemStack.is(Items.DIAMOND_PICKAXE) || itemStack.is(Items.NETHERITE_PICKAXE));

    InteractionHand hand = null;
    boolean switched = false;

    if (mc.player.getMainHandItem().is(Items.DIAMOND_PICKAXE)
        || mc.player.getMainHandItem().is(Items.NETHERITE_PICKAXE)) {
      hand = InteractionHand.MAIN_HAND;
    } else if (mc.player.getOffhandItem().is(Items.DIAMOND_PICKAXE)
        || mc.player.getOffhandItem().is(Items.NETHERITE_PICKAXE)) {
      hand = InteractionHand.OFF_HAND;
    } else if (pickaxe.found()) {
      switched = true;
      switch (switchMode.get()) {
        case Silent, Normal -> InvUtils.swap(pickaxe.slot(), true);
        case PickSilent -> BOInvUtils.pickSwitch(pickaxe.slot());
        case InvSwitch -> BOInvUtils.invSwitch(pickaxe.slot());
      }
      hand = InteractionHand.MAIN_HAND;
    }

    if (hand == null) return;

    boolean rotated =
        !SettingUtils.shouldRotate(RotationType.Mining)
            || Managers.ROTATION.start(
                pos, priority, RotationType.Mining, Objects.hash(name + "break"));

    if (rotated) {
      sendSequenced(
          s ->
              new ServerboundPlayerActionPacket(
                  ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP, s));
      sendSequenced(
          s ->
              new ServerboundPlayerActionPacket(
                  ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP, s));

      if (swing.get()) clientSwing(swingHand.get(), hand);
      if (SettingUtils.shouldRotate(RotationType.Mining)) {
        Managers.ROTATION.end(Objects.hash(name + "break"));
      }
    }

    if (switched) {
      switch (switchMode.get()) {
        case Silent -> InvUtils.swapBack();
        case PickSilent -> BOInvUtils.pickSwapBack();
        case InvSwitch -> BOInvUtils.swapBack();
      }
    }
  }

  private void igniteCart(BlockPos pos) {
    MinecartTNT minecart = findMinecartAt(pos);
    if (minecart == null || mc.gameMode == null) return;

    FindItemResult flintAndSteel = InvUtils.find(Items.FLINT_AND_STEEL);
    if (!flintAndSteel.found()) return;

    InteractionHand hand = null;
    boolean switched = false;

    if (mc.player.getMainHandItem().is(Items.FLINT_AND_STEEL)) {
      hand = InteractionHand.MAIN_HAND;
    } else if (mc.player.getOffhandItem().is(Items.FLINT_AND_STEEL)) {
      hand = InteractionHand.OFF_HAND;
    } else {
      switched = true;
      switch (switchMode.get()) {
        case Silent, Normal -> InvUtils.swap(flintAndSteel.slot(), true);
        case PickSilent -> BOInvUtils.pickSwitch(flintAndSteel.slot());
        case InvSwitch -> BOInvUtils.invSwitch(flintAndSteel.slot());
      }
      hand = InteractionHand.MAIN_HAND;
    }

    boolean rotated =
        !SettingUtils.shouldRotate(RotationType.Interact)
            || Managers.ROTATION.start(
                pos, priority, RotationType.Interact, Objects.hash(name + "ignite"));

    if (rotated) {
      // TNT minecarts are entities. Ignite them with flint & steel on the minecart itself.
      // This sends a ServerboundInteractPacket (INTERACT_AT) via MultiPlayerGameMode.interact.
      // A block-use packet against the floor did nothing.
      EntityHitResult hitResult =
          new EntityHitResult(minecart, minecart.getBoundingBox().getCenter());
      SettingUtils.swing(SwingState.Pre, SwingType.Interact, hand);
      mc.gameMode.interact(mc.player, minecart, hitResult, hand);
      SettingUtils.swing(SwingState.Post, SwingType.Interact, hand);

      if (swing.get()) clientSwing(swingHand.get(), hand);
      if (SettingUtils.shouldRotate(RotationType.Interact)) {
        Managers.ROTATION.end(Objects.hash(name + "ignite"));
      }
    }

    if (switched) {
      switch (switchMode.get()) {
        case Silent -> InvUtils.swapBack();
        case PickSilent -> BOInvUtils.pickSwapBack();
        case InvSwitch -> BOInvUtils.swapBack();
      }
    }
  }

  public enum SwitchMode {
    Disabled,
    Normal,
    Silent,
    PickSilent,
    InvSwitch
  }

  private enum Stage {
    RAIL,
    CART,
    IGNITE
  }
}
