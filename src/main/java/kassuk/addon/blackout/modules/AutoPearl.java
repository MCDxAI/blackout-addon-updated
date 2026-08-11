package kassuk.addon.blackout.modules;

import java.util.Objects;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.RotationUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * @author OLEPOSSU
 */
public class AutoPearl extends BlackOutModule {
  public AutoPearl() {
    super(BlackOut.BLACKOUT, "Auto Pearl", "Easily clip inside walls with pearls.");
  }

  private final SettingGroup sgGeneral = settings.getDefaultGroup();
  private final SettingGroup sgRender = settings.createGroup("Render");

  private final Setting<Boolean> ccBypass =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("CC Bypass")
              .description("Does funny stuff to bypass cc's anti delay.")
              .defaultValue(false)
              .build());
  private final Setting<SwitchMode> ccSwitchMode =
      sgGeneral.add(
          new EnumSetting.Builder<SwitchMode>()
              .name("CC Switch Mode")
              .description("Which method of switching should be used for cc items.")
              .defaultValue(SwitchMode.Silent)
              .build());
  private final Setting<SwitchMode> switchMode =
      sgGeneral.add(
          new EnumSetting.Builder<SwitchMode>()
              .name("Switch Mode")
              .description("Which method of switching should be used.")
              .defaultValue(SwitchMode.Silent)
              .build());
  private final Setting<Integer> pitch =
      sgGeneral.add(
          new IntSetting.Builder()
              .name("Pitch")
              .description("How deep down to look.")
              .defaultValue(85)
              .range(-90, 90)
              .sliderRange(0, 90)
              .build());
  private final Setting<Boolean> instaRot =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("Instant Rotation")
              .description("Instantly rotates.")
              .defaultValue(false)
              .build());

  // --------------------Render--------------------//
  private final Setting<Boolean> swing =
      sgRender.add(
          new BoolSetting.Builder()
              .name("Swing")
              .description("Renders swing animation when throwing an ender pearl.")
              .defaultValue(true)
              .build());
  private final Setting<SwingHand> swingHand =
      sgRender.add(
          new EnumSetting.Builder<SwingHand>()
              .name("Swing Hand")
              .description("Which hand should be swung.")
              .defaultValue(SwingHand.RealHand)
              .visible(swing::get)
              .build());

  private boolean placed = false;

  public void onActivate() {
    placed = false;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  private void onRender(Render3DEvent event) {
    if (mc.player == null || mc.level == null) return;

    InteractionHand hand = getHand();

    if (switch (switchMode.get()) {
      case Normal, Silent -> !InvUtils.findInHotbar(Items.ENDER_PEARL).found();
      case PickSilent, InvSwitch -> !InvUtils.find(Items.ENDER_PEARL).found();
    }) return;

    if (ccBypass.get() && !cc() && !placed) return;

    boolean rotated =
        instaRot.get()
            || Managers.ROTATION.start(
                getYaw(), pitch.get(), priority, RotationType.Other, Objects.hash(name + "look"))
            || (RotationUtils.yawAngle(Managers.ROTATION.lastDir[0], getYaw()) < 0.1
                && pitch.get() - Managers.ROTATION.lastDir[1] < 0.1);
    if (!rotated) return;

    if (instaRot.get()) {
      sendPacket(
          new ServerboundMovePlayerPacket.Rot(
              getYaw(), pitch.get(), Managers.ON_GROUND.isOnGround(), false));
    }

    boolean switched = hand != null;

    if (!switched) {
      switch (switchMode.get()) {
        case Silent -> {
          InvUtils.swap(InvUtils.findInHotbar(Items.ENDER_PEARL).slot(), true);
          switched = true;
        }
        case PickSilent ->
            switched = BOInvUtils.pickSwitch(InvUtils.find(Items.ENDER_PEARL).slot());
        case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(Items.ENDER_PEARL).slot());
      }
    }

    if (!switched) {
      return;
    }

    useItem(hand == null ? InteractionHand.MAIN_HAND : hand);

    Managers.ROTATION.end(Objects.hash(name + "look"));
    if (swing.get()) clientSwing(swingHand.get(), hand == null ? InteractionHand.MAIN_HAND : hand);

    toggle();
    sendToggledMsg("success");

    if (hand == null) {
      switch (switchMode.get()) {
        case Silent -> InvUtils.swapBack();
        case PickSilent -> BOInvUtils.pickSwapBack();
        case InvSwitch -> BOInvUtils.swapBack();
      }
    }
  }

  private boolean cc() {
    if (switch (ccSwitchMode.get()) {
      case Normal, Silent ->
          !InvUtils.findInHotbar(item -> item.getItem() instanceof BlockItem).found();
      case PickSilent, InvSwitch ->
          !InvUtils.find(item -> item.getItem() instanceof BlockItem).found();
    }) {
      toggle();
      sendToggledMsg("cc blocks not found");
      return false;
    }

    BlockPos pos = mc.player.blockPosition();

    boolean rotated =
        instaRot.get()
            || !SettingUtils.shouldRotate(RotationType.BlockPlace)
            || Managers.ROTATION.start(
                pos.below(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"));
    if (!rotated) return false;

    if (instaRot.get())
      sendPacket(
          new ServerboundMovePlayerPacket.Rot(
              (float) RotationUtils.getYaw(mc.player.getEyePosition(), pos.getCenter()),
              (float) RotationUtils.getPitch(mc.player.getEyePosition(), pos.getCenter()),
              Managers.ON_GROUND.isOnGround(),
              false));

    InteractionHand hand =
        mc.player.getOffhandItem().getItem() instanceof BlockItem
            ? InteractionHand.OFF_HAND
            : Managers.HOLDING.getStack().getItem() instanceof BlockItem
                ? InteractionHand.MAIN_HAND
                : null;

    boolean switched = false;

    if (hand == null) {
      switch (ccSwitchMode.get()) {
        case Silent -> {
          InvUtils.swap(
              InvUtils.findInHotbar(item -> item.getItem() instanceof BlockItem).slot(), true);
          switched = true;
        }
        case PickSilent ->
            switched =
                BOInvUtils.pickSwitch(
                    InvUtils.find(item -> item.getItem() instanceof BlockItem).slot());
        case InvSwitch ->
            switched =
                BOInvUtils.invSwitch(
                    InvUtils.find(item -> item.getItem() instanceof BlockItem).slot());
      }
    }

    if (hand == null && !switched) return false;

    placeBlock(
        hand == null ? InteractionHand.MAIN_HAND : hand,
        pos.below().getCenter(),
        Direction.UP,
        pos.below());

    if (!instaRot.get() && SettingUtils.shouldRotate(RotationType.BlockPlace))
      Managers.ROTATION.end(Objects.hash(name + "placing"));
    placed = true;

    if (hand == null) {
      switch (ccSwitchMode.get()) {
        case Silent -> InvUtils.swapBack();
        case PickSilent -> BOInvUtils.pickSwapBack();
        case InvSwitch -> BOInvUtils.swapBack();
      }
    }

    return true;
  }

  private int getYaw() {
    return (int)
            Math.round(
                Rotations.getYaw(
                    new Vec3(
                        Math.floor(mc.player.getX()) + 0.5, 0, Math.floor(mc.player.getZ()) + 0.5)))
        + 180;
  }

  private InteractionHand getHand() {
    if (Managers.HOLDING.isHolding(Items.ENDER_PEARL)) {
      return InteractionHand.MAIN_HAND;
    }
    if (mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
      return InteractionHand.OFF_HAND;
    }
    return null;
  }

  public enum SwitchMode {
    Normal,
    Silent,
    PickSilent,
    InvSwitch
  }
}
