package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * @author OLEPOSSU
 */

public class PacketFly extends BlackOutModule {
    public PacketFly() {
        super(BlackOut.BLACKOUT, "Packet Fly", "Flies with packets.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFly = settings.createGroup("Fly");
    private final SettingGroup sgPhase = settings.createGroup("Phase");

    //--------------------General--------------------//
    private final Setting<Boolean> onGroundSpoof = sgGeneral.add(new BoolSetting.Builder()
        .name("On Ground Spoof")
        .description("Spoofs on ground.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> onGround = sgGeneral.add(new BoolSetting.Builder()
        .name("On Ground")
        .description("Should we tell the server that you are on ground.")
        .defaultValue(false)
        .visible(onGroundSpoof::get)
        .build()
    );
    private final Setting<Integer> xzBound = sgGeneral.add(new IntSetting.Builder()
        .name("XZ Bound")
        .description("Bounds offset horizontally.")
        .defaultValue(1337)
        .sliderRange(-1337, 1337)
        .build()
    );
    private final Setting<Integer> yBound = sgGeneral.add(new IntSetting.Builder()
        .name("Y Bound")
        .description("Bounds offset vertically.")
        .defaultValue(0)
        .sliderRange(-1337, 1337)
        .build()
    );
    private final Setting<Boolean> strictVertical = sgGeneral.add(new BoolSetting.Builder()
        .name("Strict Vertical")
        .description("Doesn't move horizontally and vertically in the same packet.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> antiKick = sgGeneral.add(new BoolSetting.Builder()
        .name("Anti-Kick")
        .description("Slowly falls down.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> antiKickAmount = sgGeneral.add(new DoubleSetting.Builder()
        .name("Anti-Kick Multiplier")
        .description("Fall speed multiplier for antikick (0.04 blocks * multiplier).")
        .defaultValue(1)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> antiKickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Anti-Kick Delay")
        .description("Tick delay between moving anti kick packets.")
        .defaultValue(10)
        .min(1)
        .sliderRange(0, 100)
        .build()
    );
    private final Setting<Boolean> predictID = sgGeneral.add(new BoolSetting.Builder()
        .name("Predict ID")
        .description("Predicts the id of next rubberband.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> debugID = sgGeneral.add(new BoolSetting.Builder()
        .name("Debug ID")
        .description("Sends rubberband packet id in chat.")
        .defaultValue(false)
        .build()
    );

    //--------------------Fly--------------------//
    private final Setting<Double> packets = sgFly.add(new DoubleSetting.Builder()
        .name("Fly Packets")
        .description("How many packets to send every movement tick.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> speed = sgFly.add(new DoubleSetting.Builder()
        .name("Fly Speed")
        .description("Distance to travel each packet.")
        .defaultValue(0.2873)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> fastVertical = sgFly.add(new BoolSetting.Builder()
        .name("Fast Vertical Fly")
        .description("Sends multiple packets every movement tick while going up.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> downSpeed = sgFly.add(new DoubleSetting.Builder()
        .name("Fly Down Speed")
        .description("How fast to fly down.")
        .defaultValue(0.062)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> upSpeed = sgFly.add(new DoubleSetting.Builder()
        .name("Fly Up Speed")
        .description("How fast to fly up.")
        .defaultValue(0.062)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    //--------------------Phase--------------------//
    private final Setting<Double> phasePackets = sgPhase.add(new DoubleSetting.Builder()
        .name("Phase Packets")
        .description("How many packets to send every movement tick.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> phaseSpeed = sgPhase.add(new DoubleSetting.Builder()
        .name("Phase Speed")
        .description("Distance to travel each packet.")
        .defaultValue(0.062)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Boolean> phaseFastVertical = sgPhase.add(new BoolSetting.Builder()
        .name("Fast Vertical Phase")
        .description("Sends multiple packets every movement tick while going up.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> phaseDownSpeed = sgPhase.add(new DoubleSetting.Builder()
        .name("Phase Down Speed")
        .description("How fast to phase down.")
        .defaultValue(0.062)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> phaseUpSpeed = sgPhase.add(new DoubleSetting.Builder()
        .name("Phase Up Speed")
        .description("How fast to phase up.")
        .defaultValue(0.062)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );

    private int ticks = 0;
    private int id = -1;
    private int sent = 0;
    private int rur = 0;
    private double packetsToSend = 0;
    private final Random random = new Random();
    private String info = null;
    private final Map<Integer, Vec3> validPos = new HashMap<>();
    private final List<ServerboundMovePlayerPacket> validPackets = new ArrayList<>();

    public boolean moving = false;

    @Override
    public void onActivate() {
        super.onActivate();
        ticks = 0;
        validPos.clear();
    }
    @Override
    public void onDeactivate() {
        validPos.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post e) {
        ticks++;
        rur++;
        if (rur % 20 == 0) {
            info = "Packets: " + sent;
            sent = 0;
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        boolean phasing = isPhasing();
        boolean semiPhasing = isSemiPhase();

        mc.player.noPhysics = semiPhasing;
        packetsToSend += packets(semiPhasing);

        boolean shouldAntiKick = antiKick.get() && ticks % antiKickDelay.get() == 0 && !phasing && !onGround();

        double yaw = getYaw();
        double motion = semiPhasing ? phaseSpeed.get() : speed.get();

        double x = 0, y = 0, z = 0;

        if (jumping()) {
            y = semiPhasing ? phaseUpSpeed.get() : upSpeed.get();
        } else if (sneaking()) {
            y = semiPhasing ? -phaseDownSpeed.get() : -downSpeed.get();
        }

        if (y != 0) {
            moving = false;
        }

        if (moving) {
            x = Math.cos(Math.toRadians(yaw + 90)) * motion;
            z = Math.sin(Math.toRadians(yaw + 90)) * motion;
        } else {
            if (semiPhasing && !phaseFastVertical.get()) {
                packetsToSend = Math.min(packetsToSend, 1);
            }
            if (!semiPhasing && !fastVertical.get()) {
                packetsToSend = Math.min(packetsToSend, 1);
            }
        }

        Vec3 offset = new Vec3(0, 0, 0);
        boolean antiKickSent = false;
        for (; packetsToSend >= 1; packetsToSend -= 1) {
            double yOffset;
            if (shouldAntiKick && y >= 0 && !antiKickSent) {
                yOffset = antiKickAmount.get() * -0.04;
                antiKickSent = true;
            } else {
                yOffset = y;
            }

            offset = offset.add(strictVertical.get() && yOffset != 0 ? 0 : x, yOffset, strictVertical.get() && yOffset != 0 ? 0 : z);

            send(offset.add(mc.player.position()), getBounds(), getOnGround());

            if (x == 0 && z == 0 && y == 0) {
                break;
            }
        }

        ((IVec3) e.movement).meteor$set(offset.x, offset.y, offset.z);

        packetsToSend = Math.min(packetsToSend, 1);
    }

    @EventHandler
    public void onSend(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundMovePlayerPacket) {
            if (!validPackets.contains((ServerboundMovePlayerPacket) event.packet)) {
                event.cancel();
            } else {
                sent++;
            }
        } else {
            sent++;
        }
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive e) {
        if (e.packet instanceof ClientboundPlayerPositionPacket packet) {
            if (debugID.get()) {
                debug("id: " + packet.id());
            }
            Vec3 vec = new Vec3(packet.change().position().x(), packet.change().position().x(), packet.change().position().x());

            if (validPos.containsKey(packet.id()) && validPos.get(packet.id()).equals(vec)) {
                if (debugID.get()) {
                    debug("true");
                }
                e.cancel();
                if (!predictID.get()) {
                    sendPacket(new ServerboundAcceptTeleportationPacket(packet.id()));
                }
                validPos.remove(packet.id());
                return;
            }
            if (debugID.get()) {
                debug("false");
            }

            id = packet.id();
        }
    }

    @Override
    public String getInfoString() {
        return info;
    }

    private boolean onGround() {
        return mc.player.onGround() || (mc.player.getBlockY() - mc.player.getY() == 0 && OLEPOSSUtils.collidable(mc.player.blockPosition().below()));
    }

    private double packets(boolean semiPhasing) {
        return semiPhasing ? phasePackets.get() : packets.get();
    }

    private Vec3 getBounds() {
        int yaw = random.nextInt(0, 360);
        return new Vec3(Math.cos(Math.toRadians(yaw)) * xzBound.get(), yBound.get(), Math.sin(Math.toRadians(yaw)) * xzBound.get());
    }

    private boolean getOnGround() {
        return onGroundSpoof.get() ? onGround.get() : mc.player.onGround();
    }

    private boolean isPhasing() {
        return OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().contract(0.0625, 0, 0.0625));
    }

    private boolean isSemiPhase() {
        return OLEPOSSUtils.inside(mc.player, mc.player.getBoundingBox().inflate(0.01, 0, 0.01));
    }

    private boolean jumping() {
        return mc.options.keyJump.isDown();
    }

    private boolean sneaking() {
        return mc.options.keyShift.isDown();
    }

    private void send(Vec3 pos, Vec3 bounds, boolean onGround) {
        ServerboundMovePlayerPacket.Pos normal = new ServerboundMovePlayerPacket.Pos(pos.x, pos.y, pos.y, onGround, false);
        ServerboundMovePlayerPacket.Pos bound = new ServerboundMovePlayerPacket.Pos(pos.x + bounds.y, pos.y + bounds.y, pos.z + bounds.z, onGround, false);

        validPackets.add(normal);
        sendPacket(normal);
        validPos.put(id + 1, pos);

        validPackets.add(bound);
        sendPacket(bound);
        if (id < 0) {
            return;
        }

        id++;
        if (predictID.get()) {
            sendPacket(new ServerboundAcceptTeleportationPacket(id));
        }
    }

    private double getYaw() {
        double f = mc.player.input.getMoveVector().y, s = mc.player.input.getMoveVector().x;

        double yaw = mc.player.getYRot();
        if (f > 0) {
            moving = true;
            yaw += s > 0 ? -45 : s < 0 ? 45 : 0;
        } else if (f < 0) {
            moving = true;
            yaw += s > 0 ? -135 : s < 0 ? 135 : 180;
        } else {
            moving = s != 0;
            yaw += s > 0 ? -90 : s < 0 ? 90 : 0;
        }
        return yaw;
    }
}
