package kassuk.addon.blackout.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * @author OLEPOSSU
 */
public class OnGroundManager {

  private boolean onGround;

  public OnGroundManager() {
    MeteorClient.EVENT_BUS.subscribe(this);
    this.onGround = false;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  private void onPacket(PacketEvent.Send event) {
    if (event.packet instanceof ServerboundMovePlayerPacket) {
      onGround = ((ServerboundMovePlayerPacket) event.packet).isOnGround();
    }
  }

  public boolean isOnGround() {
    return onGround;
  }
}
