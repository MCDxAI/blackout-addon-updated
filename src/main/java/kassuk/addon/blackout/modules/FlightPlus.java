package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;

/**
 * @author KassuK
 */
public class FlightPlus extends BlackOutModule {
  public FlightPlus() {
    super(BlackOut.BLACKOUT, "Flight+", "KasumsSoft Flight.");
  }

  private final SettingGroup sgGeneral = settings.getDefaultGroup();

  private final Setting<FlightMode> flyMode =
      sgGeneral.add(
          new EnumSetting.Builder<FlightMode>()
              .name("Flight Mode")
              .description("Method of flying.")
              .defaultValue(FlightMode.Momentum)
              .build());
  private final Setting<Boolean> useTimer =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("Use Timer")
              .description("Should we use timer.")
              .defaultValue(false)
              .build());
  private final Setting<Double> timer =
      sgGeneral.add(
          new DoubleSetting.Builder()
              .visible(useTimer::get)
              .name("Timer")
              .description("How many times more packets should be sent.")
              .defaultValue(1.088)
              .min(0)
              .sliderMax(10)
              .visible(useTimer::get)
              .build());
  private final Setting<Double> speed =
      sgGeneral.add(
          new DoubleSetting.Builder()
              .name("Speed")
              .description("How many blocks should be moved each tick.")
              .defaultValue(0.6)
              .min(0)
              .sliderMax(10)
              .visible(() -> flyMode.get() == FlightMode.Momentum)
              .build());
  private final Setting<Double> ySpeed =
      sgGeneral.add(
          new DoubleSetting.Builder()
              .name("Y Speed")
              .description("DA Y SPEEDOS.")
              .defaultValue(0.5)
              .min(0)
              .sliderMax(10)
              .visible(() -> flyMode.get() == FlightMode.Momentum)
              .build());
  private final Setting<Double> antiKickDelay =
      sgGeneral.add(
          new DoubleSetting.Builder()
              .name("Anti-Kick Delay")
              .description("How many ticks should be waited between antikick packets.")
              .defaultValue(10)
              .min(0)
              .sliderMax(100)
              .visible(() -> flyMode.get() == FlightMode.Momentum)
              .build());
  private final Setting<Double> antiKickAmount =
      sgGeneral.add(
          new DoubleSetting.Builder()
              .name("Anti-Kick Amount")
              .description("How much to move down.")
              .defaultValue(1)
              .min(0)
              .sliderMax(10)
              .visible(() -> flyMode.get() == FlightMode.Momentum)
              .build());
  private final Setting<Boolean> keepY =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("KeepY")
              .description("Should we try to keep the same y level when jump flying.")
              .defaultValue(true)
              .visible(() -> flyMode.get() == FlightMode.Jump)
              .build());
  private final Setting<Double> glideAmount =
      sgGeneral.add(
          new DoubleSetting.Builder()
              .name("Glide amount")
              .description("How much to glide down.")
              .defaultValue(0.2)
              .min(0)
              .sliderMax(1)
              .visible(() -> flyMode.get() == FlightMode.Glide)
              .build());

  private double startY = 0.0;
  private int tick = 0;

  @Override
  public void onActivate() {
    if (mc.player != null && mc.level != null) {
      startY = mc.player.getY();
      Modules.get().get(Timer.class).setOverride(timer.get());
    }
  }

  @EventHandler
  private void onMove(PlayerMoveEvent event) {
    if (mc.player != null && mc.level != null) {
      double[] result =
          getYaw(mc.player.input.getMoveVector().y, mc.player.input.getMoveVector().x);
      float yaw = (float) result[0] + 90;
      double x = 0, y = tick % antiKickDelay.get() == 0 ? antiKickAmount.get() * -0.04 : 0, z = 0;
      if (flyMode.get().equals(FlightMode.Momentum)) {
        if (mc.options.keyJump.isDown() && y == 0) {
          y = ySpeed.get();
        } else if (mc.options.keyShift.isDown()) {
          y = -ySpeed.get();
        }
        if (result[1] == 1) {
          x = Math.cos(Math.toRadians(yaw)) * speed.get();
          z = Math.sin(Math.toRadians(yaw)) * speed.get();
        }
        ((IVec3) event.movement).meteor$set(x, y, z);
      }
      if (flyMode.get().equals(FlightMode.Jump)) {
        if (mc.options.keyJump.consumeClick()) {
          mc.player.jumpFromGround();
          startY += 0.4;
        }
        if (mc.options.keyShift.consumeClick() && !mc.options.keyShift.isDown()) {
          startY = mc.player.getY();
        }

        if (keepY.get() && mc.player.getY() <= startY && !mc.options.keyShift.isDown())
          mc.player.jumpFromGround();
        if (result[1] == 1) {
          x = Math.cos(Math.toRadians(yaw)) * speed.get();
          z = Math.sin(Math.toRadians(yaw)) * speed.get();
        }
        ((IVec3) event.movement).meteor$setXZ(x, z);
      }
      if (flyMode.get().equals(FlightMode.Glide)) {
        if (!mc.player.onGround()) ((IVec3) event.movement).meteor$setY(-glideAmount.get());
      }
    }
  }

  @EventHandler
  private void onTick(TickEvent.Pre event) {
    tick++;
  }

  @Override
  public void onDeactivate() {
    if (mc.player != null && mc.level != null) {
      Modules.get().get(Timer.class).setOverride(1);
    }
  }

  private double[] getYaw(double f, double s) {
    double yaw = mc.player.getYRot();
    double move;
    if (f > 0) {
      move = 1;
      yaw += s > 0 ? -45 : s < 0 ? 45 : 0;
    } else if (f < 0) {
      move = 1;
      yaw += s > 0 ? -135 : s < 0 ? 135 : 180;
    } else {
      move = s != 0 ? 1 : 0;
      yaw += s > 0 ? -90 : s < 0 ? 90 : 0;
    }
    return new double[] {yaw, move};
  }

  public enum FlightMode {
    Momentum,
    Jump,
    Glide,
  }
}
