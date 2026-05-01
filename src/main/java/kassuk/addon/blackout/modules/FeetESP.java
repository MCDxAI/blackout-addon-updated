package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * @author OLEPOSSU
 */

public class FeetESP extends BlackOutModule {
    public FeetESP() {
        super(BlackOut.BLACKOUT, "Feet ESP", "No, it doesn't show you pictures of feet.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> friend = sgGeneral.add(new BoolSetting.Builder()
        .name("Friend")
        .description("Renders friends' feet.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> other = sgGeneral.add(new BoolSetting.Builder()
        .name("Other")
        .description("Renders other players' feet.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> self = sgGeneral.add(new BoolSetting.Builder()
        .name("Self")
        .description("Renders own feet.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of feet should be rendered")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the feet outlines.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Color of the feet sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Renders feet inside this range.")
        .defaultValue(25)
        .min(0)
        .sliderRange(0, 25)
        .build()
    );

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.level == null) return;

        mc.level.players().forEach(player -> {
            if (player.distanceTo(mc.player) > range.get()) return;

            if (!friend.get() && Friends.get().isFriend(player)) return;
            if (!other.get() && player != mc.player && !Friends.get().isFriend(player)) return;
            if (!self.get() && mc.player == player) return;

            float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
            render(event, new Vec3(
                Mth.lerp(tickDelta, player.xo, player.getX()),
                Mth.lerp(tickDelta, player.yo, player.getY()),
                Mth.lerp(tickDelta, player.zo, player.getZ())
            ));
        });
    }

    private void render(Render3DEvent event, Vec3 vec) {
        event.renderer.sideHorizontal(vec.x - 0.3, vec.y, vec.z - 0.3, vec.x + 0.3, vec.z + 0.3, sideColor.get(), lineColor.get(), shapeMode.get());
    }
}
