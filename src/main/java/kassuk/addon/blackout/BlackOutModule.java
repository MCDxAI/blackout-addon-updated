package kassuk.addon.blackout;

import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.modules.SwingModifier;
import kassuk.addon.blackout.utils.PriorityUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * @author OLEPOSSU
 */

public class BlackOutModule extends Module {
    private static final Component PREFIX = Component.literal("[BlackOut] ").withStyle(ChatFormatting.DARK_RED);
    private static final Component ON = Component.literal("ON").withStyle(ChatFormatting.GREEN);
    private static final Component OFF = Component.literal("OFF").withStyle(ChatFormatting.RED);
    public final int priority;

    public BlackOutModule(Category category, String name, String description) {
        super(category, name, description);
        this.priority = PriorityUtils.get(this);
    }

    //  Messages
    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.level != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            Component msg = Component.empty().append(PREFIX).append(name).append(CommonComponents.SPACE).append(isActive() ? ON : OFF);
            sendMessage(msg, hashCode());
        }
    }

    public void sendToggledMsg(String message) {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.level != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            Component msg = Component.empty().append(PREFIX).append(name).append(CommonComponents.SPACE).append(isActive() ? ON : OFF).append(CommonComponents.SPACE).append(
                Component.literal(message).withStyle(ChatFormatting.GRAY)
            );
            sendMessage(msg, hashCode());
        }
    }

    public void sendDisableMsg(String text) {
        if (mc.level != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            Component msg = Component.empty().append(PREFIX).append(name).append(CommonComponents.SPACE).append(OFF).append(
                Component.literal(text).withStyle(ChatFormatting.GRAY)
            );
            sendMessage(msg, hashCode());
        }
    }

    public void sendBOInfo(String text) {
        if (mc.level != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            Component msg = Component.empty().append(PREFIX).append(name).append(CommonComponents.SPACE).append(text);
            sendMessage(msg, Objects.hash(name + "-info"));
        }
    }
    public void debug(String text) {
        if (mc.level != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            Component msg = Component.empty().append(PREFIX).append(name).append(CommonComponents.SPACE).append(
                Component.literal(text).withStyle(ChatFormatting.AQUA)
            );
            sendMessage(msg, 0);
        }
    }

    public void sendMessage(Component text, int id) {
        ((IChatHud) mc.gui.getChat()).meteor$add(text, id);
    }

    public void sendPacket(Packet<?> packet) {
        if (mc.getConnection() == null) return;
        mc.getConnection().send(packet);
    }

    public void sendSequenced(PredictiveAction packetCreator) {
        if (mc.gameMode == null || mc.level == null || mc.getConnection() == null) return;

        BlockStatePredictionHandler sequence = mc.level.getBlockStatePredictionHandler().startPredicting();
        Packet<?> packet = packetCreator.predict(sequence.currentSequence());

        mc.getConnection().send(packet);

        sequence.close();
    }

    public void placeBlock(InteractionHand hand, Vec3 blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3 eyes = mc.player.getEyePosition();
        boolean inside =
            eyes.x > pos.getX() && eyes.x < pos.getX() + 1 &&
                eyes.y > pos.getY() && eyes.y < pos.getY() + 1 &&
                eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
        sendSequenced(s -> new ServerboundUseItemOnPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
    }

    public void interactBlock(InteractionHand hand, Vec3 blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3 eyes = mc.player.getEyePosition();
        boolean inside =
            eyes.x > pos.getX() && eyes.x < pos.getX() + 1 &&
            eyes.y > pos.getY() && eyes.y < pos.getY() + 1 &&
            eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        SettingUtils.swing(SwingState.Pre, SwingType.Interact, hand);
        sendSequenced(s -> new ServerboundUseItemOnPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        SettingUtils.swing(SwingState.Post, SwingType.Interact, hand);
    }

    public void useItem(InteractionHand hand) {
        SettingUtils.swing(SwingState.Pre, SwingType.Using, hand);
        sendSequenced(s -> new ServerboundUseItemPacket(hand, s, Managers.ROTATION.lastDir[0], Managers.ROTATION.lastDir[1]));
        SettingUtils.swing(SwingState.Post, SwingType.Using, hand);
    }

    public void clientSwing(SwingHand swingHand, InteractionHand realHand) {
        InteractionHand hand = switch (swingHand) {
            case MainHand -> InteractionHand.MAIN_HAND;
            case OffHand -> InteractionHand.OFF_HAND;
            case RealHand -> realHand;
        };

        mc.player.swing(hand, true);
        Modules.get().get(SwingModifier.class).startSwing(hand);
    }

    public Setting<Boolean> addPauseEat(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Pause Eat")
            .description("Pauses when eating")
            .defaultValue(false)
            .build()
        );
    }
}
