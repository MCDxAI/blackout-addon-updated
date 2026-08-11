package kassuk.addon.blackout.modules;

import java.util.*;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.mixins.*;
import kassuk.addon.blackout.utils.PacketNames;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

/**
 * @author OLEPOSSU
 */
public class PacketLogger extends BlackOutModule {
  public PacketLogger() {
    super(BlackOut.BLACKOUT, "Logger", "Logs packets or whatever you want. (only packets rn)");
  }

  private final SettingGroup sgGeneral = settings.getDefaultGroup();

  // yoinked these settings from meteor
  private final Setting<Set<PacketType<? extends Packet<?>>>> receivePackets =
      sgGeneral.add(
          new PacketListSetting.Builder()
              .name("Receive")
              .description("Server-to-client packets to cancel.")
              .filter(packetType -> PacketUtils.getClientboundPackets().contains(packetType))
              .build());

  private final Setting<Set<PacketType<? extends Packet<?>>>> sendPackets =
      sgGeneral.add(
          new PacketListSetting.Builder()
              .name("Send")
              .description("Client-to-server packets to cancel.")
              .filter(packetType -> PacketUtils.getServerboundPackets().contains(packetType))
              .build());

  public void onSent(Packet<?> packet) {
    if (!isActive()) return;
    if (sendPackets.get().contains(packet.type())) {
      String message = packetMessage(packet);

      if (message == null) return;
      log(ChatFormatting.AQUA + "Send: " + ChatFormatting.GRAY + message);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST + 1000000000)
  private void onReceive(PacketEvent.Receive event) {
    if (receivePackets.get().contains(event.packet.type())) {
      String message = packetMessage(event.packet);

      if (message == null) return;
      log(ChatFormatting.LIGHT_PURPLE + "Receive: " + ChatFormatting.GRAY + message);
    }
  }

  private void log(String string) {
    sendMessage(Component.nullToEmpty(string), 0);
  }

  // this was not fun
  private String packetMessage(Packet<?> packet) {
    PacketNames.PacketData<?> data = PacketNames.getData(packet);
    return data == null ? null : data.funnyApply(packet);
  }
}
