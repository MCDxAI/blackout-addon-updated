package kassuk.addon.blackout.mixins;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.HashedPatchMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPacketListener.class)
public interface ComponentHasherNetworkHandlerAccessor {
  @Accessor("decoratedHashOpsGenerator")
  HashedPatchMap.HashGenerator getDecoratedHashOpsGenerator();
}
