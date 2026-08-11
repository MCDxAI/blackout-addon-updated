package kassuk.addon.blackout.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.*;
import kassuk.addon.blackout.utils.meteor.BODamageUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * @author OLEPOSSU
 */
public class BedAuraPlus extends BlackOutModule {
  public BedAuraPlus() {
    super(
        BlackOut.BLACKOUT,
        "Bed Aura+",
        "Automatically places and breaks beds to cause damage to your opponents but better.");
  }

  private final SettingGroup sgGeneral = settings.getDefaultGroup();
  private final SettingGroup sgPlacing = settings.createGroup("Placing");
  private final SettingGroup sgDamage = settings.createGroup("Damage");
  private final SettingGroup sgRender = settings.createGroup("Render");

  // --------------------General--------------------//
  private final Setting<Boolean> fiveB =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("5B5T")
              .description(
                  "For example requires floor for both bed blocks and allows placing inside entities.")
              .defaultValue(false)
              .build());
  private final Setting<Boolean> pauseEat =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("Pause Eat")
              .description("Pauses when you are eating.")
              .defaultValue(true)
              .build());
  private final Setting<Boolean> doubleInteract =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("Double Interact")
              .description("Clicks both bed blocks every time.")
              .defaultValue(true)
              .build());
  private final Setting<LogicMode> logicMode =
      sgGeneral.add(
          new EnumSetting.Builder<LogicMode>()
              .name("Logic Mode")
              .description("Logic for bullying kids.")
              .defaultValue(LogicMode.PlaceBreak)
              .build());
  private final Setting<SwitchMode> switchMode =
      sgGeneral.add(
          new EnumSetting.Builder<SwitchMode>()
              .name("Switch Mode")
              .description("Method of switching. Silent is the most reliable.")
              .defaultValue(SwitchMode.Silent)
              .build());
  private final Setting<RotationMode> rotMode =
      sgGeneral.add(
          new EnumSetting.Builder<RotationMode>()
              .name("Rotation Mode")
              .description(
                  "Packet- Sends 1 rotation packet for each bed. Manager- Modifies movement packets to set rotation.")
              .defaultValue(RotationMode.Manager)
              .build());

  // --------------------Placing--------------------//
  private final Setting<SpeedMode> speedMode =
      sgPlacing.add(
          new EnumSetting.Builder<SpeedMode>()
              .name("Speed Mode")
              .description("Normal mode should be used in everywhere else than 5B.")
              .defaultValue(SpeedMode.Normal)
              .build());
  private final Setting<Double> speed =
      sgPlacing.add(
          new DoubleSetting.Builder()
              .name("Speed")
              .description("How many beds to blow up every second.")
              .defaultValue(2)
              .min(0)
              .sliderRange(0, 20)
              .visible(() -> speedMode.get() == SpeedMode.Normal)
              .build());
  private final Setting<Double> damageSpeed =
      sgPlacing.add(
          new DoubleSetting.Builder()
              .name("Damage Speed Factor")
              .description("Sets speed to damage multiplied by factor.")
              .defaultValue(1)
              .min(0)
              .sliderRange(0, 20)
              .visible(() -> speedMode.get() == SpeedMode.Damage)
              .build());
  private final Setting<Double> maxSpeed =
      sgPlacing.add(
          new DoubleSetting.Builder()
              .name("Damage Speed")
              .description("Maximum speed for damage mode.")
              .defaultValue(12)
              .min(0)
              .sliderRange(0, 20)
              .visible(() -> speedMode.get() == SpeedMode.Damage)
              .build());

  // --------------------Damage--------------------//
  private final Setting<Double> minDmg =
      sgDamage.add(
          new DoubleSetting.Builder()
              .name("Min Damage")
              .description("Minimum damage to place.")
              .defaultValue(8)
              .min(0)
              .sliderRange(0, 20)
              .build());
  private final Setting<Double> maxDmg =
      sgDamage.add(
          new DoubleSetting.Builder()
              .name("Max Damage")
              .description("Maximum self damage to place.")
              .defaultValue(6)
              .min(0)
              .sliderRange(0, 20)
              .build());
  private final Setting<Double> maxFriendDmg =
      sgDamage.add(
          new DoubleSetting.Builder()
              .name("Max Friend Damage")
              .description("Maximum friend damage to place.")
              .defaultValue(6)
              .min(0)
              .sliderRange(0, 20)
              .build());
  private final Setting<Double> minRatio =
      sgDamage.add(
          new DoubleSetting.Builder()
              .name("Min Damage Ratio")
              .description("Minimum damage ratio between self damage and enemy damage.")
              .defaultValue(2)
              .min(0)
              .sliderRange(0, 10)
              .build());
  private final Setting<Double> minFriendRatio =
      sgDamage.add(
          new DoubleSetting.Builder()
              .name("Min Friend Damage Ratio")
              .description("Minimum damage ratio between friend damage and enemy damage.")
              .defaultValue(2)
              .min(0)
              .sliderRange(0, 10)
              .build());
  private final Setting<Double> forcePop =
      sgDamage.add(
          new DoubleSetting.Builder()
              .name("Force Pop")
              .description("Ignores damage checks if enemy would pop after x explodes.")
              .defaultValue(1)
              .min(0)
              .sliderRange(0, 10)
              .build());
  private final Setting<Double> antiPop =
      sgDamage.add(
          new DoubleSetting.Builder()
              .name("Anti Pop")
              .description("Cancels actions if you would pop after x explodes.")
              .defaultValue(1)
              .min(0)
              .sliderRange(0, 10)
              .build());
  private final Setting<Double> antiFriendPop =
      sgDamage.add(
          new DoubleSetting.Builder()
              .name("Anti Friend Pop")
              .description("Cancels actions if any friend would pop after x explodes.")
              .defaultValue(1)
              .min(0)
              .sliderRange(0, 10)
              .build());
  private final Setting<Boolean> friendSacrifice =
      sgDamage.add(
          new BoolSetting.Builder()
              .name("Friend Sacrifice")
              .description("Kills your friend if you can also kill any enemy with same bed.")
              .defaultValue(true)
              .build());

  // --------------------Render--------------------//
  private final Setting<Boolean> placeSwing =
      sgRender.add(
          new BoolSetting.Builder()
              .name("Place Swing")
              .description("Renders swing animation when placing the crafting table.")
              .defaultValue(true)
              .build());
  private final Setting<SwingHand> placeHand =
      sgRender.add(
          new EnumSetting.Builder<SwingHand>()
              .name("Place Hand")
              .description("Which hand should be swung.")
              .defaultValue(SwingHand.RealHand)
              .visible(placeSwing::get)
              .build());
  private final Setting<Boolean> interactSwing =
      sgRender.add(
          new BoolSetting.Builder()
              .name("Interact Swing")
              .description("Renders swing animation when interacting with a block.")
              .defaultValue(true)
              .build());
  private final Setting<SwingHand> interactHand =
      sgRender.add(
          new EnumSetting.Builder<SwingHand>()
              .name("Interact Hand")
              .description("Which hand should be swung.")
              .defaultValue(SwingHand.RealHand)
              .visible(interactSwing::get)
              .build());
  public final Setting<ShapeMode> shapeMode =
      sgRender.add(
          new EnumSetting.Builder<ShapeMode>()
              .name("Shape Mode")
              .description("Which parts of the render should be rendered.")
              .defaultValue(ShapeMode.Both)
              .build());
  private final Setting<SettingColor> lineColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("Head Line Color")
              .description("Line color of head block.")
              .defaultValue(new SettingColor(255, 0, 0, 255))
              .build());
  public final Setting<SettingColor> color =
      sgRender.add(
          new ColorSetting.Builder()
              .name("Head Side Color")
              .description("Side color of head block.")
              .defaultValue(new SettingColor(255, 0, 0, 50))
              .build());
  private final Setting<SettingColor> fLineColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("Feet Line Color")
              .description("Line color of feet block")
              .defaultValue(new SettingColor(255, 0, 0, 255))
              .build());
  public final Setting<SettingColor> fColor =
      sgRender.add(
          new ColorSetting.Builder()
              .name("Feet Side Color")
              .description("Side color of feet block")
              .defaultValue(new SettingColor(255, 0, 0, 50))
              .build());

  private int lastIndex = 0;
  private int length = 0;
  private long tickTime = -1;
  private double bestDmg = 0;
  private long lastTime = 0;

  private BlockPos placePos = null;
  private Direction bedDir = null;
  private PlaceData placeData = null;
  private BlockPos calcPos = null;
  private Direction calcDir = null;
  private PlaceData calcData = null;
  private BlockPos renderPos = null;
  private Direction renderDir = null;
  private BlockPos[] blocks = new BlockPos[] {};
  private final List<Player> targets = new ArrayList<>();
  private final List<Player> friends = new ArrayList<>();
  private final List<Bed> beds = new ArrayList<>();

  private double timer = 0;

  private double dmg;
  private double enemyHP;
  private double self;
  private double selfHP;
  private double friend;
  private double friendHP;

  @EventHandler(priority = EventPriority.HIGHEST)
  private void onTickPre(TickEvent.Post event) {
    calculate(length - 1);
    renderPos = calcPos;
    placePos = calcPos;
    renderDir = calcDir;
    bedDir = calcDir;
    placeData = calcData;

    blocks =
        getBlocks(
            mc.player.getEyePosition(),
            Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));

    // Reset stuff
    tickTime = System.currentTimeMillis();
    length = blocks.length;
    lastIndex = 0;
    bestDmg = 0;
    calcPos = null;
    calcDir = null;
    calcData = null;

    updateTargets();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  private void onRender(Render3DEvent event) {
    double delta = (System.currentTimeMillis() - lastTime) / 1000f;
    timer += delta;
    lastTime = System.currentTimeMillis();

    List<Bed> toRemove = new ArrayList<>();
    beds.forEach(
        bed -> {
          if (System.currentTimeMillis() - bed.time > 500) {
            toRemove.add(bed);
          }
        });
    toRemove.forEach(beds::remove);

    if (tickTime < 0 || mc.player == null || mc.level == null) return;

    if (pauseCheck()) {
      update();
    }

    int index =
        Math.min(
            (int) Math.ceil((System.currentTimeMillis() - tickTime) / 50f * length), length - 1);
    calculate(index);

    if (renderPos != null && pauseCheck()) {
      event.renderer.box(bedBox(renderPos), color.get(), lineColor.get(), shapeMode.get(), 0);
      if (renderDir != null) {
        event.renderer.box(
            bedBox(renderPos.relative(renderDir)),
            fColor.get(),
            fLineColor.get(),
            shapeMode.get(),
            0);
      }
    }
  }

  private boolean pauseCheck() {
    return !pauseEat.get() || !mc.player.isUsingItem();
  }

  private void calculate(int index) {
    BlockPos pos;

    for (int i = lastIndex; i < index; i++) {
      pos = blocks[i];

      damageCalc(pos);

      if (!dmgCheck()) continue;

      for (Direction dir : Direction.Plane.HORIZONTAL) {
        PlaceData data = getData(pos, dir);

        if (!data.valid()) continue;

        if (!OLEPOSSUtils.replaceable(pos.relative(dir))
            && !(mc.level.getBlockState(pos.relative(dir)).getBlock() instanceof BedBlock))
          continue;

        if (!SettingUtils.inPlaceRange(data.pos())) continue;

        if (!fiveB.get()
            && EntityUtils.intersectsWithEntity(
                new AABB(pos.relative(dir)), entity -> !(entity instanceof ItemEntity))) continue;

        calcData = data;
        calcPos = pos;
        calcDir = dir;
        bestDmg = dmg;
      }
    }
    lastIndex = index;
  }

  private void updateTargets() {
    friends.clear();
    targets.clear();

    List<Player> players = new ArrayList<>();

    double closestDist = 1000;
    Player closest;
    double dist;

    for (int i = 3; i > 0; i--) {

      closest = null;
      for (Player player : mc.level.players()) {
        if (players.contains(player) || Friends.get().isFriend(player) || player == mc.player)
          continue;

        dist = player.distanceTo(mc.player);

        if (dist > 15) continue;

        if (closest == null || dist < closestDist) {
          closestDist = dist;
          closest = player;
        }
      }
      if (closest != null) {
        players.add(closest);
        if (Friends.get().isFriend(closest)) {
          friends.add(closest);
        } else {
          targets.add(closest);
        }
      }
    }
  }

  private BlockPos[] getBlocks(Vec3 middle, double radius) {
    ArrayList<BlockPos> result = new ArrayList<>();
    int i = (int) Math.ceil(radius);
    BlockPos pos;

    for (int x = -i; x <= i; x++) {
      for (int y = -i; y <= i; y++) {
        for (int z = -i; z <= i; z++) {
          pos = BlockPos.containing(middle).offset(x, y, z);

          if (!OLEPOSSUtils.replaceable(pos)
              && !(mc.level.getBlockState(pos).getBlock() instanceof BedBlock)) continue;

          if (fiveB.get()
              && (mc.level.getBlockState(pos.below()).getBlock() == Blocks.AIR
                  || mc.level.getBlockState(pos.below()).hasBlockEntity())) continue;

          if (!inRangeToTargets(pos)) continue;
          result.add(pos);
        }
      }
    }
    return result.toArray(new BlockPos[0]);
  }

  private boolean inRangeToTargets(BlockPos pos) {
    for (Player target : targets) {
      if (target.position().add(0, 1, 0).distanceTo(pos.getCenter()) < 3.5) {
        return true;
      }
    }
    return false;
  }

  private void update() {
    if (placePos == null || placeData == null || !placeData.valid() || bedDir == null) return;

    if (logicMode.get() == LogicMode.PlaceBreak) {
      List<BlockPos> in = interactUpdate();
      if (in != null && !in.isEmpty()) {
        in.forEach(this::removeBed);
      }

      if (timer <= 1 / getSpeed()) return;

      if (OLEPOSSUtils.replaceable(placePos)
          && OLEPOSSUtils.replaceable(placePos.relative(bedDir))
          && placeUpdate()) {
        removeBed2(placePos);
        beds.add(new Bed(placePos, placePos.relative(bedDir), true, System.currentTimeMillis()));
        timer = 0;
      }
    } else {
      if (!isBed(placePos) && !isBed(placePos.relative(bedDir)) && placeUpdate()) {
        removeBed2(placePos);
        beds.add(new Bed(placePos, placePos.relative(bedDir), true, System.currentTimeMillis()));
      }

      if (timer <= 1 / getSpeed()) return;

      List<BlockPos> in = interactUpdate();
      if (in != null && !in.isEmpty()) {
        in.forEach(this::removeBed);
        timer = 0;
      }
    }
  }

  private void removeBed(BlockPos pos) {
    List<Bed> toRemove = new ArrayList<>();
    beds.forEach(
        bed -> {
          if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
            toRemove.add(bed);
          }
        });
    toRemove.forEach(
        bed -> {
          beds.remove(bed);
          beds.add(new Bed(bed.feetBlock, bed.headBlock, false, System.currentTimeMillis()));
        });
  }

  private void removeBed2(BlockPos pos) {
    List<Bed> toRemove = new ArrayList<>();
    beds.forEach(
        bed -> {
          if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
            toRemove.add(bed);
          }
        });
    toRemove.forEach(beds::remove);
  }

  private void place(InteractionHand hand) {
    placeBlock(hand, placeData.pos().getCenter(), placeData.dir(), placeData.pos());

    if (placeSwing.get()) clientSwing(placeHand.get(), hand);
  }

  private List<BlockPos> interactUpdate() {
    if (doubleInteract.get()) {
      if (SettingUtils.shouldRotate(RotationType.Interact)
          && !Managers.ROTATION.start(
              placePos, priority, RotationType.Interact, Objects.hash(name + "explode"))) {
        return null;
      }

      List<BlockPos> list = new ArrayList<>();

      if (isBed(placePos) || isBed(placePos.relative(bedDir))) {
        if (SettingUtils.inPlaceRange(placePos) && interact(placePos)) {
          list.add(placePos);
        }
        if (SettingUtils.inPlaceRange(placePos.relative(bedDir))
            && interact(placePos.relative(bedDir))) {
          list.add(placePos.relative(bedDir));
        }
      }

      if (SettingUtils.shouldRotate(RotationType.Interact)) {
        Managers.ROTATION.end(Objects.hash(name + "explode"));
      }

      return list;
    }

    BlockPos interactPos = getInteractPos();

    if (interactPos == null) {
      return null;
    }

    Direction interactDir = SettingUtils.getPlaceOnDirection(interactPos);

    if (interactDir == null) {
      return null;
    }

    if (SettingUtils.shouldRotate(RotationType.Interact)
        && !Managers.ROTATION.start(
            interactPos, priority, RotationType.Interact, Objects.hash(name + "explode"))) {
      return null;
    }

    interactBlock(InteractionHand.MAIN_HAND, interactPos.getCenter(), interactDir, interactPos);

    if (interactSwing.get()) clientSwing(interactHand.get(), InteractionHand.MAIN_HAND);

    if (SettingUtils.shouldRotate(RotationType.Interact)) {
      Managers.ROTATION.end(Objects.hash(name + "explode"));
    }
    List<BlockPos> list = new ArrayList<>();
    list.add(interactPos);
    return list;
  }

  private boolean interact(BlockPos pos) {
    Direction dir = SettingUtils.getPlaceOnDirection(pos);

    if (dir == null) {
      return false;
    }

    interactBlock(InteractionHand.MAIN_HAND, pos.getCenter(), dir, pos);

    if (interactSwing.get()) clientSwing(interactHand.get(), InteractionHand.MAIN_HAND);
    return true;
  }

  private BlockPos getInteractPos() {
    if (isBed(placePos.relative(bedDir))
        && SettingUtils.inPlaceRange(placePos.relative(bedDir))
        && SettingUtils.getPlaceOnDirection(placePos.relative(bedDir)) != null) {
      return placePos.relative(bedDir);
    }
    if (isBed(placePos)
        && SettingUtils.inPlaceRange(placePos)
        && SettingUtils.getPlaceOnDirection(placePos) != null) {
      return placePos;
    }
    return null;
  }

  private boolean isBed(BlockPos pos) {
    for (Bed bed : beds) {
      if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
        return bed.isBed;
      }
    }
    return mc.level.getBlockState(pos).getBlock() instanceof BedBlock;
  }

  private boolean placeUpdate() {
    InteractionHand hand =
        Managers.HOLDING.getStack().getItem() instanceof BedItem
            ? InteractionHand.MAIN_HAND
            : mc.player.getOffhandItem().getItem() instanceof BedItem
                ? InteractionHand.OFF_HAND
                : null;

    int beds =
        hand == InteractionHand.MAIN_HAND
            ? Managers.HOLDING.getStack().getCount()
            : hand == InteractionHand.OFF_HAND ? mc.player.getOffhandItem().getCount() : 0;

    if (hand == null) {
      switch (switchMode.get()) {
        case Silent, Normal -> {
          FindItemResult result = InvUtils.findInHotbar(item -> item.getItem() instanceof BedItem);
          beds = result.count();
        }
        case PickSilent, InvSwitch -> {
          FindItemResult result = InvUtils.find(item -> item.getItem() instanceof BedItem);
          beds = result.slot() >= 0 ? result.count() : -1;
        }
      }
    }

    if (beds <= 0) {
      return false;
    }

    if (SettingUtils.shouldRotate(RotationType.BlockPlace)
        && !Managers.ROTATION.start(
            placeData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
      return false;
    }

    boolean switched = hand != null;

    if (rotMode.get() == RotationMode.Packet) {
      sendPacket(
          new ServerboundMovePlayerPacket.Rot(
              bedDir.getOpposite().get2DDataValue(),
              Managers.ROTATION.lastDir[1],
              Managers.ON_GROUND.isOnGround(),
              false));
    } else {
      Managers.ROTATION.startYaw(
          bedDir.getOpposite().get2DDataValue(),
          priority,
          RotationType.Other,
          Objects.hash(name + "placing"));
      if (Math.abs(
              RotationUtils.yawAngle(
                  Managers.ROTATION.lastDir[0], bedDir.getOpposite().get2DDataValue()))
          > 45) {
        return false;
      }
    }

    if (!switched) {
      switch (switchMode.get()) {
        case Silent, Normal -> {
          FindItemResult result = InvUtils.findInHotbar(item -> item.getItem() instanceof BedItem);
          InvUtils.swap(result.slot(), true);
          switched = true;
        }
        case PickSilent -> {
          FindItemResult result = InvUtils.find(item -> item.getItem() instanceof BedItem);
          switched = BOInvUtils.pickSwitch(result.slot());
        }
        case InvSwitch -> {
          FindItemResult result = InvUtils.find(item -> item.getItem() instanceof BedItem);
          switched = BOInvUtils.invSwitch(result.slot());
        }
      }
    }

    if (!switched) {
      return false;
    }

    place(hand == null ? InteractionHand.MAIN_HAND : hand);

    if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
      Managers.ROTATION.end(Objects.hash(name + "placing"));
    }

    if (hand == null) {
      switch (switchMode.get()) {
        case Silent -> InvUtils.swapBack();
        case PickSilent -> BOInvUtils.pickSwapBack();
        case InvSwitch -> BOInvUtils.swapBack();
      }
    }
    return true;
  }

  private boolean dmgCheck() {
    if (dmg < bestDmg) {
      return false;
    }

    if (self * antiPop.get() >= selfHP) {
      return false;
    }

    if (!friendSacrifice.get() && friendHP >= 0 && friend * antiFriendPop.get() >= friendHP) {
      return false;
    }
    if (enemyHP >= 0 && dmg * forcePop.get() >= enemyHP) {
      return true;
    }
    if (friendHP >= 0 && friend * antiFriendPop.get() >= friendHP) {
      return false;
    }

    if (dmg < minDmg.get()) {
      return false;
    }

    if (self > maxDmg.get()) {
      return false;
    }
    if (friend > maxFriendDmg.get()) {
      return false;
    }

    if (dmg / self < minRatio.get()) {
      return false;
    }

    return !(friendHP >= 0) || !(dmg / friend < minFriendRatio.get());
  }

  private double getDmg(BlockPos pos) {
    double highest = -1;
    for (Player target : targets) {
      highest =
          Math.max(
              highest,
              BODamageUtils.anchorDamage(
                  target, target.getBoundingBox(), pos.getCenter(), pos, false));
    }
    return highest;
  }

  private void damageCalc(BlockPos pos) {
    // Enemy
    double highest = -1;
    double highestHP = -1;
    for (Player target : targets) {
      if (target.getHealth() <= 0) continue;

      highest =
          Math.max(
              highest,
              BODamageUtils.anchorDamage(
                  target, target.getBoundingBox(), pos.getCenter(), pos, true));
      highestHP = target.getHealth() + target.getAbsorptionAmount();
    }
    dmg = highest;
    enemyHP = highestHP;

    // Self
    self =
        BODamageUtils.anchorDamage(
            mc.player, mc.player.getBoundingBox(), pos.getCenter(), pos, false);
    selfHP = mc.player.getHealth() + mc.player.getAbsorptionAmount();

    // Friend
    highest = -1;
    highestHP = -1;
    for (Player friend : friends) {
      if (friend.getHealth() <= 0) continue;

      highest =
          Math.max(
              highest,
              BODamageUtils.anchorDamage(
                  friend, friend.getBoundingBox(), pos.getCenter(), pos, true));
      highestHP = friend.getHealth() + friend.getAbsorptionAmount();
    }
    friend = highest;
    friendHP = highestHP;
  }

  private AABB bedBox(BlockPos pos) {
    return new AABB(
        pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.5, pos.getZ() + 1);
  }

  private PlaceData getData(BlockPos pos, Direction dir) {
    if (fiveB.get()) {
      return SettingUtils.getPlaceDataAND(
          pos.relative(dir),
          direction -> direction == Direction.DOWN,
          pos1 -> !(mc.level.getBlockState(pos1).getBlock() instanceof BedBlock));
    } else {
      return SettingUtils.getPlaceDataAND(
          pos.relative(dir),
          direction -> direction != dir,
          pos1 -> !(mc.level.getBlockState(pos1).getBlock() instanceof BedBlock));
    }
  }

  private double getSpeed() {
    switch (speedMode.get()) {
      case Normal -> {
        return speed.get();
      }
      case Damage -> {
        if (placePos == null) {
          return maxSpeed.get();
        }

        double dmg = getDmg(placePos);
        return Math.min(dmg * damageSpeed.get(), maxSpeed.get());
      }
    }
    return 2;
  }

  public enum LogicMode {
    PlaceBreak,
    BreakPlace
  }

  public enum RotationMode {
    Packet,
    Manager
  }

  public enum SwitchMode {
    Silent,
    Normal,
    PickSilent,
    InvSwitch,
    Disabled
  }

  public enum SpeedMode {
    Normal,
    Damage
  }

  private record Bed(BlockPos feetBlock, BlockPos headBlock, boolean isBed, long time) {}
}
