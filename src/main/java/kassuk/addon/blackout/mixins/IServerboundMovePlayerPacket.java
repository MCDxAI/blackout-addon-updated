package kassuk.addon.blackout.mixins;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface IServerboundMovePlayerPacket {
  @Accessor("x")
  double blackout$getX();

  @Accessor("y")
  double blackout$getY();

  @Accessor("z")
  double blackout$getZ();
}
