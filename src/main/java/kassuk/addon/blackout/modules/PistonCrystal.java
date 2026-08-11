package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.PlaceData;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;

import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author OLEPOSSU
 */

public class PistonCrystal extends BlackOutModule {
    public PistonCrystal() {
        super(BlackOut.BLACKOUT, "Piston Crystal", "Pushes crystals into your enemies to deal massive damage.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDelay = settings.createGroup("Delay");
    private final SettingGroup sgSwitch = settings.createGroup("Switch");
    private final SettingGroup sgToggle = settings.createGroup("Toggle");
    private final SettingGroup sgSwing = settings.createGroup("Swing");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> pauseEat = addPauseEat(sgGeneral);
    private final Setting<Boolean> fire = sgGeneral.add(new BoolSetting.Builder()
        .name("Fire")
        .description("Uses fire to blow up the crystal.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Redstone> redstone = sgGeneral.add(new EnumSetting.Builder<Redstone>()
        .name("Redstone")
        .description("What kind of redstone to use.")
        .defaultValue(Redstone.Torch)
        .build()
    );
    private final Setting<Boolean> alwaysAttack = sgGeneral.add(new BoolSetting.Builder()
        .name("Always Attack")
        .description("Attacks all crystals blocking crystal placing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> attackSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Attack Speed")
        .description("How many times to attack the crystal every second.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Delay--------------------//
    private final Setting<Double> pcDelay = sgDelay.add(new DoubleSetting.Builder()
        .name("Piston > Crystal")
        .description("How many seconds to wait between placing piston and redstone.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> cfDelay = sgDelay.add(new DoubleSetting.Builder()
        .name("Crystal > Fire")
        .description("How many seconds to wait after mining the redstone before starting a new cycle.")
        .defaultValue(0.2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> crDelay = sgDelay.add(new DoubleSetting.Builder()
        .name("Crystal > Redstone")
        .description("How many seconds to wait between placing redstone and starting to mine it.")
        .defaultValue(0.2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> rmDelay = sgDelay.add(new DoubleSetting.Builder()
        .name("Redstone > Mine")
        .description("How many seconds to wait after mining the redstone before starting a new cycle.")
        .defaultValue(0.2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> mpDelay = sgDelay.add(new DoubleSetting.Builder()
        .name("Mine > Piston")
        .description("How many seconds to wait after mining the redstone before starting a new cycle.")
        .defaultValue(0.2)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Switch--------------------//
    private final Setting<SwitchMode> crystalSwitch = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Crystal Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SwitchMode> pistonSwitch = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Piston Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SwitchMode> redstoneSwitch = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Redstone Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SwitchMode> fireSwitch = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("Fire Switch")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );

    //--------------------Toggle--------------------//
    /*
    private final Setting<Boolean> toggleMove = sgToggle.add(new BoolSetting.Builder()
        .name("Toggle Move")
        .description("Toggles when you move.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> toggleEnemyMove = sgToggle.add(new BoolSetting.Builder()
        .name("Toggle Enemy Move")
        .description("Toggles when your target moves.")
        .defaultValue(false)
        .build()
    );
     */

    //--------------------Swing--------------------//
    private final Setting<Boolean> crystalSwing = sgSwing.add(new BoolSetting.Builder()
        .name("Crystal Swing")
        .description("Renders swing animation when placing a crystal.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> crystalHand = sgSwing.add(new EnumSetting.Builder<SwingHand>()
        .name("Crystal Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(crystalSwing::get)
        .build()
    );
    private final Setting<Boolean> attackSwing = sgSwing.add(new BoolSetting.Builder()
        .name("Attack Swing")
        .description("Renders swing animation when attacking a crystal.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> attackHand = sgSwing.add(new EnumSetting.Builder<SwingHand>()
        .name("Attack Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(attackSwing::get)
        .build()
    );
    private final Setting<Boolean> pistonSwing = sgSwing.add(new BoolSetting.Builder()
        .name("Piston Swing")
        .description("Renders swing animation when placing a piston.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> pistonHand = sgSwing.add(new EnumSetting.Builder<SwingHand>()
        .name("Piston Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(pistonSwing::get)
        .build()
    );
    private final Setting<Boolean> redstoneSwing = sgSwing.add(new BoolSetting.Builder()
        .name("Piston Swing")
        .description("Renders swing animation when placing redstone.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> redstoneHand = sgSwing.add(new EnumSetting.Builder<SwingHand>()
        .name("Redstone Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(redstoneSwing::get)
        .build()
    );
    private final Setting<Boolean> fireSwing = sgSwing.add(new BoolSetting.Builder()
        .name("Fire Swing")
        .description("Renders swing animation when placing fire.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> fireHand = sgSwing.add(new EnumSetting.Builder<SwingHand>()
        .name("Fire Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(fireSwing::get)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Double> crystalHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("Crystal Height")
        .description(".")
        .defaultValue(0.25)
        .sliderRange(-1, 1)
        .build()
    );
    private final Setting<ShapeMode> crystalShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Crystal Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> crystalLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Crystal Line Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> crystalColor = sgRender.add(new ColorSetting.Builder()
        .name("Crystal Side Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<Double> pistonHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("Piston Height")
        .description(".")
        .defaultValue(1)
        .sliderRange(-1, 1)
        .build()
    );
    private final Setting<ShapeMode> pistonShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Piston Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> pistonLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Piston Line Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    public final Setting<SettingColor> pistonColor = sgRender.add(new ColorSetting.Builder()
        .name("Piston Side Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    private final Setting<Double> redstoneHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("Redstone Height")
        .description(".")
        .defaultValue(1)
        .sliderRange(-1, 1)
        .build()
    );
    private final Setting<ShapeMode> redstoneShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Redstone Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> redstoneLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Redstone Line Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> redstoneColor = sgRender.add(new ColorSetting.Builder()
        .name("Redstone Side Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private long lastAttack = 0;

    public BlockPos crystalPos = null;
    private BlockPos pistonPos = null;
    private BlockPos firePos = null;
    private BlockPos redstonePos = null;

    private BlockPos lastCrystalPos = null;
    private BlockPos lastPistonPos = null;
    private BlockPos lastRedstonePos = null;
    private Entity lastTarget = null;

    private Direction pistonDir = null;
    private PlaceData pistonData = null;
    private Direction crystalPlaceDir = null;
    private Direction crystalDir = null;
    private PlaceData redstoneData = null;
    private Entity target = null;

    private BlockPos closestCrystalPos = null;
    private BlockPos closestPistonPos = null;
    private BlockPos closestRedstonePos = null;
    private Direction closestPistonDir = null;
    private PlaceData closestPistonData = null;
    private Direction closestCrystalPlaceDir = null;
    private Direction closestCrystalDir = null;
    private PlaceData closestRedstoneData = null;

    private long pistonTime = 0;
    private long redstoneTime = 0;
    private long mineTime = 0;
    private long crystalTime = 0;

    private boolean minedThisTick = false;

    private boolean pistonPlaced = false;
    private boolean redstonePlaced = false;
    private boolean mined = false;
    private boolean crystalPlaced = false;
    private boolean firePlaced = false;

    private double cd;
    private double d;

    public float asRotation(int idHorizontal) {
        return (float)((idHorizontal & 3) * 90);
    }

    @Override
    public void onActivate() {
        resetPos();
        lastCrystalPos = null;
        lastPistonPos = null;
        lastRedstonePos = null;

        pistonPlaced = false;
        redstonePlaced = false;
        mined = false;
        crystalPlaced = false;
        firePlaced = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        minedThisTick = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        updatePos();

        if (crystalPos != null) {
            event.renderer.box(getBox(crystalPos, crystalHeight.get()), crystalColor.get(), crystalLineColor.get(), crystalShapeMode.get(), 0);
            event.renderer.box(getBox(pistonPos, pistonHeight.get()), pistonColor.get(), pistonLineColor.get(), pistonShapeMode.get(), 0);
            event.renderer.box(getBox(redstonePos, redstoneHeight.get()), redstoneColor.get(), redstoneLineColor.get(), redstoneShapeMode.get(), 0);
        }

        if (crystalPos == null) {
            return;
        }

        if (System.currentTimeMillis() - mineTime > mpDelay.get() * 1000 && crystalPlaced && redstonePlaced && pistonPlaced && mined && (firePlaced || !fire.get())) {
            redstonePlaced = false;
            pistonPlaced = false;
            mined = false;
            firePlaced = false;
            crystalPlaced = false;

            pistonTime = 0;
            redstoneTime = 0;
            mineTime = 0;
            crystalTime = 0;
            lastAttack = 0;
        }

        if (pauseEat.get() && mc.player.isUsingItem()) return;

        updateAttack();
        updatePiston();
        updateFire();
        updateCrystal();
        updateRedstone();
        mineUpdate();
    }

    private AABB getBox(BlockPos pos, double height) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + height, pos.getZ() + 1);
    }

    private void mineUpdate() {
        if (System.currentTimeMillis() - redstoneTime < rmDelay.get() * 1000) return;
        if (!redstonePlaced) return;
        if (minedThisTick) return;

        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (autoMine.isActive()) {
            if (redstonePos.equals(autoMine.targetPos())) return;

            autoMine.onStart(redstonePos);
        } else {
            Direction mineDir = SettingUtils.getPlaceOnDirection(redstonePos);

            if (mineDir != null) {
                sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, redstonePos, mineDir));
                sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, redstonePos, mineDir));
            }
        }

        if (!mined) mineTime = System.currentTimeMillis();

        mined = true;
        minedThisTick = true;
    }

    private void updateAttack() {
        if (!redstonePlaced) return;

        EndCrystal crystal = null;
        double cd = 10000;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal c)) continue;
            if (c.getX() == crystalPos.getX() + 0.5 && c.getZ() == crystalPos.getZ() + 0.5) continue;

            if ((!alwaysAttack.get() && (c.getX() - c.getBlockX() == 0.5 && c.getZ() - c.getBlockZ() == 0.5)) || !c.getBoundingBox().intersects(AABB.of(new BoundingBox(crystalPos)).setMaxY(crystalPos.getY() + 1))) {
                continue;
            }

            double d = mc.player.getEyePosition().distanceTo(c.position());

            if (d < cd) {
                cd = d;
                crystal = c;
            }
        }

        if (crystal == null) return;

        if (SettingUtils.shouldRotate(RotationType.Attacking) && !Managers.ROTATION.start(crystal.getBoundingBox(), priority - 0.1, RotationType.Attacking, Objects.hash(name + "attacking"))) return;

        if (System.currentTimeMillis() - lastAttack < 1000 / attackSpeed.get()) return;

        SettingUtils.swing(SwingState.Pre, SwingType.Attacking, InteractionHand.MAIN_HAND);
        sendPacket(new ServerboundAttackPacket(crystal.getId()));
        SettingUtils.swing(SwingState.Post, SwingType.Attacking, InteractionHand.MAIN_HAND);

        if (SettingUtils.shouldRotate(RotationType.Attacking)) Managers.ROTATION.end(Objects.hash(name + "attacking"));

        if (attackSwing.get()) clientSwing(attackHand.get(), InteractionHand.MAIN_HAND);

        lastAttack = System.currentTimeMillis();
    }

    private void updatePiston() {
        if (pistonPlaced) return;

        if (pistonData == null) return;

        InteractionHand hand = getHand(Items.PISTON);
        boolean available = hand != null;

        if (!available) {
            switch (pistonSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(Items.PISTON).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(Items.PISTON).found();
            }
        }

        if (!available) return;

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(pistonData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "piston"))) return;

        boolean switched = false;

        if (hand == null) {
            switch (pistonSwitch.get()) {
                case Silent -> {
                    InvUtils.swap(InvUtils.findInHotbar(Items.PISTON).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.find(Items.PISTON).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(Items.PISTON).slot());
            }
        }

        if (hand == null && !switched) return;

        sendPacket(new ServerboundMovePlayerPacket.Rot(pistonDir.getOpposite().get2DDataValue(), Managers.ROTATION.lastDir[1], Managers.ON_GROUND.isOnGround(), false));

        hand = hand == null ? InteractionHand.MAIN_HAND : hand;

        placeBlock(hand, pistonData.pos().getCenter(), pistonData.dir(), pistonData.pos());

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "piston"));
        if (pistonSwing.get()) clientSwing(pistonHand.get(), hand);

        pistonTime = System.currentTimeMillis();
        pistonPlaced = true;

        if (switched) {
            switch (pistonSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
    }

    private void updateCrystal() {
        if (!pistonPlaced || crystalPlaced) return;

        if (System.currentTimeMillis() - pistonTime < pcDelay.get() * 1000) return;

        if (crystalPlaceDir == null) return;

        if (EntityUtils.intersectsWithEntity(AABB.of(new BoundingBox(crystalPos)), entity -> !entity.isSpectator() && !(entity instanceof EndCrystal))) return;

        InteractionHand hand = getHand(Items.END_CRYSTAL);
        boolean available = hand != null;

        if (!available) {
            switch (crystalSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(Items.END_CRYSTAL).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(Items.END_CRYSTAL).found();
            }
        }

        if (!available) return;

        if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(crystalPos.below(), priority, RotationType.Interact, Objects.hash(name + "crystal"))) return;

        boolean switched = false;

        if (hand == null) {
            switch (crystalSwitch.get()) {
                case Silent -> {
                    InvUtils.swap(InvUtils.findInHotbar(Items.END_CRYSTAL).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.find(Items.END_CRYSTAL).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(Items.END_CRYSTAL).slot());
            }
        }

        if (hand == null && !switched) return;

        hand = hand == null ? InteractionHand.MAIN_HAND : hand;

        interactBlock(hand, crystalPos.below().getCenter(), crystalPlaceDir, crystalPos.below());

        if (SettingUtils.shouldRotate(RotationType.Interact)) Managers.ROTATION.end(Objects.hash(name + "crystal"));
        if (crystalSwing.get()) clientSwing(crystalHand.get(), hand);

        crystalTime = System.currentTimeMillis();
        crystalPlaced = true;

        if (switched) {
            switch (crystalSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
    }

    private void updateRedstone() {
        if (!crystalPlaced || redstonePlaced) return;
        if (System.currentTimeMillis() - crystalTime < crDelay.get() * 1000) return;

        if (redstoneData == null) return;

        InteractionHand hand = getHand(redstone.get().i);
        boolean available = hand != null;

        if (!available) {
            switch (redstoneSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(redstone.get().i).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(redstone.get().i).found();
            }
        }

        if (!available) return;

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(redstoneData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "redstone"))) return;

        boolean switched = false;

        if (hand == null) {
            switch (redstoneSwitch.get()) {
                case Silent -> {
                    InvUtils.swap(InvUtils.findInHotbar(redstone.get().i).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.find(redstone.get().i).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(redstone.get().i).slot());
            }
        }

        if (hand == null && !switched) return;

        hand = hand == null ? InteractionHand.MAIN_HAND : hand;

        placeBlock(hand, redstoneData.pos().getCenter(), redstoneData.dir(), redstoneData.pos());

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "redstone"));
        if (redstoneSwing.get()) clientSwing(redstoneHand.get(), hand);

        redstoneTime = System.currentTimeMillis();
        redstonePlaced = true;

        if (switched) {
            switch (redstoneSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
    }

    private void updateFire() {
        if (!fire.get()) return;
        if (!crystalPlaced || firePlaced) return;
        if (System.currentTimeMillis() - crystalTime < cfDelay.get() * 1000) return;

        double closesD = 10000;
        firePos = null;
        PlaceData data = null;
        boolean found = false;

        for (int x = (crystalDir.getOpposite().getStepX() == 0 ? -1 : Math.min(0, crystalDir.getStepX())); x <= (crystalDir.getOpposite().getStepX() == 0 ? 1 : Math.max(0, crystalDir.getOpposite().getStepX())); x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = (crystalDir.getOpposite().getStepZ() == 0 ? -1 : Math.min(0, crystalDir.getStepZ())); z <= (crystalDir.getOpposite().getStepZ() == 0 ? 1 : Math.max(0, crystalDir.getOpposite().getStepZ())); z++) {
                    if (found) {
                        break;
                    }

                    BlockPos pos = crystalPos.relative(crystalDir.getOpposite()).offset(x, y, z);

                    if (pos.equals(crystalPos)) continue;
                    if (pos.equals(pistonPos)) continue;
                    if (pos.equals(redstonePos)) continue;
                    if (pos.equals(pistonPos.relative(pistonDir.getOpposite()))) continue;

                    if (mc.level.getBlockState(pos).getBlock() instanceof FireBlock) {
                        found = true;
                        firePos = pos;
                        data = SettingUtils.getPlaceData(pos);
                    }

                    if (!OLEPOSSUtils.solid(pos.below())) continue;
                    if (!(mc.level.getBlockState(pos).getBlock() instanceof AirBlock)) continue;

                    double d = pos.getCenter().distanceTo(mc.player.getEyePosition());
                    if (d >= closesD) continue;

                    PlaceData da = SettingUtils.getPlaceData(pos);

                    if (!da.valid()) continue;
                    if (!SettingUtils.inPlaceRange(da.pos())) continue;

                    data = da;
                    closesD = d;
                    firePos = pos;
                }
            }
        }

        if (firePos == null) {
            firePlaced = true;
            return;
        }

        if (data == null || !data.valid()) return;

        InteractionHand hand = getHand(Items.FLINT_AND_STEEL);
        boolean available = hand != null;

        if (!available) {
            switch (fireSwitch.get()) {
                case Silent -> available = InvUtils.findInHotbar(Items.FLINT_AND_STEEL).found();
                case PickSilent, InvSwitch -> available = InvUtils.find(Items.FLINT_AND_STEEL).found();
            }
        }

        if (!available) return;

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "fire"))) return;

        boolean switched = false;

        if (hand == null) {
            switch (fireSwitch.get()) {
                case Silent -> {
                    InvUtils.swap(InvUtils.findInHotbar(Items.FLINT_AND_STEEL).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.find(Items.FLINT_AND_STEEL).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.find(Items.FLINT_AND_STEEL).slot());
            }
        }

        if (hand == null && !switched) return;

        hand = hand == null ? InteractionHand.MAIN_HAND : hand;

        interactBlock(hand, data.pos().getCenter(), data.dir(), data.pos());

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "fire"));
        if (fireSwing.get()) clientSwing(fireHand.get(), hand);

        firePlaced = true;

        if (switched) {
            switch (fireSwitch.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
    }

    private void updatePos() {
        lastCrystalPos = crystalPos;
        lastPistonPos = pistonPos;
        lastRedstonePos = redstonePos;
        lastTarget = target;

        closestCrystalPos = null;
        closestPistonPos = null;
        closestRedstonePos = null;
        closestPistonDir = null;
        closestPistonData = null;
        closestCrystalPlaceDir = null;
        closestCrystalDir = null;
        closestRedstoneData = null;

        resetPos();

        mc.level.players().stream()
            .filter(player -> player != mc.player && player.position().distanceTo(mc.player.position()) < 10 && player.getHealth() > 0 && !Friends.get().isFriend(player) && !player.isSpectator())
            .sorted(Comparator.comparingDouble(i -> i.position().distanceTo(mc.player.position()))).forEach(player -> {

                if (crystalPos == null) {
                    update(player, true);

                    if (crystalPos != null) {
                        return;
                    }

                    update(player, false);
                }
            });
    }

    private void update(Player player, boolean top) {
        cd = 10000;


        for (Direction dir : Direction.Plane.HORIZONTAL) {
            resetPos();
            BlockPos cPos = top ? BlockPos.containing(player.getEyePosition()).relative(dir).above() : BlockPos.containing(player.getEyePosition()).relative(dir);

            d = cPos.getCenter().distanceTo(mc.player.position());
            if (!cPos.equals(lastCrystalPos) && d > cd) continue;

            Block b = mc.level.getBlockState(cPos).getBlock();
            if (!(b instanceof AirBlock) && b != Blocks.PISTON_HEAD && b != Blocks.MOVING_PISTON) {
                continue;
            }
            b = mc.level.getBlockState(cPos.above()).getBlock();
            if (SettingUtils.oldCrystals() && !(b instanceof AirBlock) && b != Blocks.PISTON_HEAD && b != Blocks.MOVING_PISTON) {
                continue;
            }
            if (mc.level.getBlockState(cPos.below()).getBlock() != Blocks.OBSIDIAN && mc.level.getBlockState(cPos.below()).getBlock() != Blocks.BEDROCK) {
                continue;
            }
            if (EntityUtils.intersectsWithEntity(AABB.of(new BoundingBox(cPos)).setMaxY(cPos.getY() + (SettingUtils.cc() ? 1 : 2)), entity -> !entity.isSpectator() && entity instanceof Player)) {
                continue;
            }
            if (!SettingUtils.inPlaceRange(cPos)) {
                continue;
            }

            Direction cDir = SettingUtils.getPlaceOnDirection(cPos);
            if (cDir == null) {
                continue;
            }

            getPistonPos(cPos, dir);
            if (pistonPos == null) {
                continue;
            }

            cd = d;
            crystalPos = cPos;
            crystalPlaceDir = cDir;
            crystalDir = dir;

            closestCrystalPos = crystalPos;
            closestPistonPos = pistonPos;
            closestRedstonePos = redstonePos;
            closestPistonDir = pistonDir;
            closestPistonData = pistonData;
            closestCrystalPlaceDir = crystalPlaceDir;
            closestCrystalDir = crystalDir;
            closestRedstoneData = redstoneData;

            if (crystalPos.equals(lastCrystalPos)) break;
        }

        crystalPos = closestCrystalPos;
        pistonPos = closestPistonPos;
        redstonePos = closestRedstonePos;
        pistonDir = closestPistonDir;
        pistonData = closestPistonData;
        crystalPlaceDir = closestCrystalPlaceDir;
        crystalDir = closestCrystalDir;
        redstoneData = closestRedstoneData;
        target = player;
    }

    private void getPistonPos(BlockPos pos, Direction dir) {
        List<BlockPos> pistonBlocks = pistonBlocks(pos, dir);

        cd = 10000;

        BlockPos cPos = null;
        PlaceData cData = null;
        Direction cDir = null;
        BlockPos cRedstonePos = null;
        PlaceData cRedstoneData = null;

        for (BlockPos position : pistonBlocks) {

            d = mc.player.getEyePosition().distanceTo(position.getCenter());
            if (!position.equals(lastPistonPos) && cd < d) continue;

            PlaceData placeData = SettingUtils.getPlaceDataAND(position, d -> true, b -> !isRedstone(b) &&
                !(mc.level.getBlockState(b).getBlock() instanceof PistonBaseBlock ||
                    mc.level.getBlockState(b).getBlock() instanceof PistonHeadBlock ||
                    mc.level.getBlockState(b).getBlock() instanceof MovingPistonBlock ||
                    mc.level.getBlockState(b).getBlock() == Blocks.MOVING_PISTON ||
                    mc.level.getBlockState(b).getBlock() instanceof FireBlock));

            if (!placeData.valid()) {
                continue;
            }
            if (!SettingUtils.inPlaceRange(placeData.pos())) {
                continue;
            }

            redstonePos(position, dir.getOpposite(), pos);

            if (redstonePos == null) {
                continue;
            }

            cd = d;
            cRedstonePos = redstonePos;
            cRedstoneData = redstoneData;
            cPos = position;
            cDir = dir.getOpposite();
            cData = placeData;

            if (position.equals(lastPistonPos)) break;
        }

        pistonPos = cPos;
        pistonDir = cDir;
        pistonData = cData;
        redstonePos = cRedstonePos;
        redstoneData = cRedstoneData;
    }

    private List<BlockPos> pistonBlocks(BlockPos pos, Direction dir) {
        List<BlockPos> blocks = new ArrayList<>();

        for (int x = dir.getStepX() == 0 ? -1 : dir.getStepX(); x <= (dir.getStepX() == 0 ? 1 : dir.getStepX()); x++) {
            for (int z = dir.getStepZ() == 0 ? -1 : dir.getStepZ(); z <= (dir.getStepZ() == 0 ? 1 : dir.getStepZ()); z++) {
                for (int y = 0; y <= 1; y++) {
                    if (x == 0 && y == 0 && z == 0 || (SettingUtils.oldCrystals() && x == 0 && y == 1 && z == 0)) {
                        continue;
                    }

                    if (!upCheck(pos.offset(x, y, z))) {
                        continue;
                    }

                    blocks.add(pos.offset(x, y, z));
                }
            }
        }

        return blocks.stream().filter(b -> {
            if (blocked(b.relative(dir.getOpposite()))) {
                return false;
            }
            if (EntityUtils.intersectsWithEntity(AABB.of(new BoundingBox(b)), entity -> !entity.isSpectator() && entity instanceof Player)) {
                return false;
            }

            if (mc.level.getBlockState(b).getBlock() instanceof PistonBaseBlock ||
                mc.level.getBlockState(b).getBlock() == Blocks.MOVING_PISTON ||
                mc.level.getBlockState(b).getBlock() instanceof FireBlock) {
                return true;
            }

            return OLEPOSSUtils.replaceable(b);
        }).toList();
    }

    private void redstonePos(BlockPos pos, Direction pDir, BlockPos cPos) {
        cd = 10000;
        redstonePos = null;

        BlockPos cRedstonePos = null;
        PlaceData cRedstoneData = null;

        if (redstone.get() == Redstone.Torch) {
            for (Direction direction : Direction.values()) {
                if (direction == pDir || direction == Direction.DOWN) continue;

                BlockPos position = pos.relative(direction);

                d = position.getCenter().distanceTo(mc.player.getEyePosition());
                if (!position.equals(lastPistonPos) && cd < d) continue;

                if (position.equals(cPos)) {
                    continue;
                }
                if (SettingUtils.oldCrystals() && position.equals(cPos.above())) {
                    continue;
                }
                if (!OLEPOSSUtils.replaceable(position) && !(mc.level.getBlockState(position).getBlock() instanceof RedstoneTorchBlock) && !(mc.level.getBlockState(position).getBlock() instanceof FireBlock)) {
                    continue;
                }

                redstoneData = SettingUtils.getPlaceDataAND(position, d -> {
                    if (d == Direction.UP && !OLEPOSSUtils.solid(position.below())) {
                        return false;
                    }
                    return direction != d.getOpposite();
                }, b -> {
                    if (pos.equals(b)) {
                        return false;
                    }
                    if (mc.level.getBlockState(b).getBlock() instanceof TorchBlock) {
                        return false;
                    }
                    return !(mc.level.getBlockState(b).getBlock() instanceof PistonBaseBlock) && !(mc.level.getBlockState(b).getBlock() instanceof PistonHeadBlock);
                });

                if (!redstoneData.valid() || !SettingUtils.inPlaceRange(redstoneData.pos()) || !SettingUtils.inMineRange(position)) continue;

                cd = d;
                cRedstonePos = position;
                cRedstoneData = redstoneData;

                if (position.equals(lastRedstonePos)) break;
            }
            redstonePos = cRedstonePos;
            redstoneData = cRedstoneData;
            return;
        }

        for (Direction direction : Direction.values()) {
            if (direction == pDir) {
                continue;
            }

            BlockPos position = pos.relative(direction);

            d = position.getCenter().distanceTo(mc.player.getEyePosition());
            if (!position.equals(lastPistonPos) && cd < d) continue;

            if (position.equals(cPos)) {
                continue;
            }
            if (!OLEPOSSUtils.replaceable(position) && mc.level.getBlockState(position).getBlock() != Blocks.REDSTONE_BLOCK) {
                continue;
            }
            if (new AABB(position).intersects(OLEPOSSUtils.getCrystalBox(cPos))) {
                continue;
            }
            if (EntityUtils.intersectsWithEntity(new AABB(position), entity -> !entity.isSpectator() && entity instanceof Player)) {
                continue;
            }

            redstoneData = SettingUtils.getPlaceDataOR(position, pos::equals);

            if (!redstoneData.valid()) continue;

            cd = d;
            cRedstonePos = position;
            cRedstoneData = redstoneData;

            if (position.equals(lastRedstonePos)) break;
        }

        redstonePos = cRedstonePos;
        redstoneData = cRedstoneData;
    }


    private Entity crystalAt() {
        for (Entity entity : mc.level.getEntities((Entity) null, new AABB(crystalPos),
            e -> e.blockPosition().equals(crystalPos))) {
            return entity;
        }
        return null;
    }

    private boolean upCheck(BlockPos pos) {
        double dx = mc.player.getEyePosition().x - pos.getX() - 0.5;
        double dz = mc.player.getEyePosition().z - pos.getZ() - 0.5;


        return Math.sqrt(dx * dx + dz * dz) > Math.abs(mc.player.getEyePosition().y - pos.getY() - 0.5);
    }

    private boolean isRedstone(BlockPos pos) {
        return mc.level.getBlockState(pos).isSignalSource();
    }

    private boolean blocked(BlockPos pos) {
        Block b = mc.level.getBlockState(pos).getBlock();
        if (b == Blocks.MOVING_PISTON) {
            return false;
        }
        if (b == Blocks.PISTON_HEAD) {
            return false;
        }
        if (b == Blocks.REDSTONE_TORCH) {
            return false;
        }
        if (b instanceof FireBlock) {
            return false;
        }

        return !(mc.level.getBlockState(pos).getBlock() instanceof AirBlock);
    }

    private InteractionHand getHand(Item item) {
        return Managers.HOLDING.isHolding(item) ? InteractionHand.MAIN_HAND :
            mc.player.getOffhandItem().getItem() == item ? InteractionHand.OFF_HAND :
                null;
    }

    private void resetPos() {
        crystalPos = null;
        pistonPos = null;
        firePos = null;
        redstonePos = null;

        pistonDir = null;
        pistonData = null;
        crystalPlaceDir = null;
        crystalDir = null;
        redstoneData = null;
    }

    public enum SwitchMode {
        Disabled,
        Silent,
        PickSilent,
        InvSwitch
    }


    public enum Redstone {
        Torch(Items.REDSTONE_TORCH, Blocks.REDSTONE_TORCH),
        Block(Items.REDSTONE_BLOCK, Blocks.REDSTONE_BLOCK);

        public final Item i;
        public final Block b;

        Redstone(Item i, Block b) {
            this.i = i;
            this.b = b;
        }
    }
}
