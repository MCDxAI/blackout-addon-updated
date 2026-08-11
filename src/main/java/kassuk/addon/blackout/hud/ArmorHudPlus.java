package kassuk.addon.blackout.hud;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import com.mojang.blaze3d.vertex.PoseStack;
import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

/**
 * @author OLEPOSSU
 */
public class ArmorHudPlus extends HudElement {

  private final SettingGroup sgGeneral = settings.getDefaultGroup();

  private final Setting<Double> scale =
      sgGeneral.add(
          new DoubleSetting.Builder()
              .name("Scale")
              .description("Scale to render at.")
              .defaultValue(1)
              .range(0.1, 5)
              .sliderRange(0.1, 5)
              .build());
  private final Setting<Integer> rounding =
      sgGeneral.add(
          new IntSetting.Builder()
              .name("Rounding")
              .description("How rounded should the background be.")
              .defaultValue(50)
              .range(0, 100)
              .sliderRange(0, 100)
              .visible(() -> false) // trick to keep configs while rounding is temporarily disabled
              .build());
  private final Setting<Boolean> bg =
      sgGeneral.add(
          new BoolSetting.Builder()
              .name("Background")
              .description("Renders a background behind armor pieces.")
              .defaultValue(false)
              .build());
  private final Setting<SettingColor> bgColor =
      sgGeneral.add(
          new ColorSetting.Builder()
              .name("Background Color")
              .description(BlackOut.COLOR)
              .defaultValue(new SettingColor(0, 0, 0, 150))
              .build());
  private final Setting<SettingColor> durColor =
      sgGeneral.add(
          new ColorSetting.Builder()
              .name("Durability Color")
              .description(BlackOut.COLOR)
              .defaultValue(new SettingColor(255, 255, 255, 255))
              .build());
  private final Setting<DurMode> durMode =
      sgGeneral.add(
          new EnumSetting.Builder<DurMode>()
              .name("Durability Mode")
              .description("Where should durability be rendered at.")
              .defaultValue(DurMode.Bottom)
              .build());

  public static final HudElementInfo<ArmorHudPlus> INFO =
      new HudElementInfo<>(
          BlackOut.HUD_BLACKOUT,
          "ArmorHud+",
          "A target hud the fuck you thinkin bruv.",
          ArmorHudPlus::new);

  public ArmorHudPlus() {
    super(INFO);
  }

  @Override
  public void render(HudRenderer renderer) {
    if (mc.player == null) {
      return;
    }

    setSize(100 * scale.get() * 2, 28 * scale.get() * 2);
    PoseStack stack = new PoseStack();

    stack.translate(x, y, 0);
    stack.scale((float) (scale.get() * 2), (float) (scale.get() * 2), 1);

    if (bg.get()) {
      renderer.quad(x, y, 100 * scale.get() * 2, 28 * scale.get() * 2, bgColor.get());
    }

    for (EquipmentSlot slot :
        new EquipmentSlot[] {
          EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
      ItemStack itemStack = mc.player.getItemBySlot(slot);

      if (itemStack.isEmpty()) continue;

      String text =
          String.valueOf(
              Math.round(
                  100 - (float) itemStack.getDamageValue() / itemStack.getMaxDamage() * 100f));
      renderer.text(
          text,
          x + (slot.getId() * 40) * scale.get(),
          y + (durMode.get() == DurMode.Top ? 6 : 34) * scale.get(),
          durColor.get(),
          false,
          scale.get());
    }

    renderer.post(
        () -> {
          Matrix3x2fStack drawStack = renderer.graphics.pose();
          drawStack.pushMatrix();

          drawStack.translate((float) (x / 2), (float) (y / 2));
          drawStack.scale((float) (scale.get() * 2), (float) (scale.get() * 2));

          for (EquipmentSlot slot :
              new EquipmentSlot[] {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
              }) {
            ItemStack itemStack = mc.player.getItemBySlot(slot);
            renderer.item(
                itemStack,
                slot.getId() * 20,
                durMode.get() == DurMode.Top ? 10 : 0,
                scale.get().floatValue(),
                false);
          }

          drawStack.popMatrix();
        });
  }

  public enum DurMode {
    Top, // 3, 10
    Bottom // 0, 17
  }
}
