package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.globalsettings.SwingSettings;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.phys.Vec3;

/**
 * @author KassuK
 */
public class LightsOut extends BlackOutModule {
  public LightsOut() {
    super(BlackOut.BLACKOUT, "Lights Out", "A tribute to Reliant.");
  }

  private final SettingGroup sgGeneral = settings.getDefaultGroup();

  private final Setting<Double> delay =
      sgGeneral.add(
          new DoubleSetting.Builder()
              .name("Tick Delay")
              .description("Delay between breaking torches.")
              .defaultValue(2)
              .range(0, 10)
              .sliderRange(0, 10)
              .build());
  private final Setting<Boolean> swing =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("Swing")
              .description("Renders swing animation when breaking a torch.")
              .defaultValue(true)
              .build());
  private final Setting<SwingHand> swingHand =
      sgGeneral.add(
          new EnumSetting.Builder<SwingHand>()
              .name("Swing Hand")
              .description("Which hand should be swung.")
              .defaultValue(SwingHand.RealHand)
              .visible(swing::get)
              .build());

  private double timer = 0;

  @EventHandler
  private void onTick(TickEvent.Post event) {
    BlockPos block = getLightSource(mc.player.getEyePosition(), SettingUtils.getMineRange());
    if (block != null && timer >= delay.get()) {
      timer = 0;

      SettingUtils.mineSwing(SwingSettings.MiningSwingState.Start);

      mc.getConnection()
          .send(
              new ServerboundPlayerActionPacket(
                  ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, block, Direction.UP));
      mc.getConnection()
          .send(
              new ServerboundPlayerActionPacket(
                  ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, block, Direction.UP));

      SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
      if (swing.get()) clientSwing(swingHand.get(), InteractionHand.MAIN_HAND);
    }
  }

  @EventHandler
  private void onRender(Render3DEvent event) {
    timer = Math.min(delay.get(), timer + event.frameTime);
  }

  private BlockPos getLightSource(Vec3 vec, double r) {
    int c = (int) (Math.ceil(r) + 1);
    BlockPos closest = null;
    float closestDist = -1;
    for (int x = -c; x <= c; x++) {
      for (int y = -c; y <= c; y++) {
        for (int z = -c; z <= c; z++) {
          BlockPos pos = mc.player.blockPosition().offset(x, y, z);
          // best code ever fr
          if (mc.level.getBlockState(pos).getBlock() instanceof TorchBlock) {
            float dist = (float) vec.distanceTo(pos.getCenter());
            if (dist <= r && (closest == null || dist < closestDist)) {
              closest = pos;
              closestDist = dist;
            }
          }
        }
      }
    }
    return closest;
  }
}
