package kassuk.addon.blackout.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.mixins.ComponentHasherNetworkHandlerAccessor;
import meteordevelopment.meteorclient.mixininterface.IMultiPlayerGameMode;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

@SuppressWarnings("DataFlowIssue")
public class BOInvUtils {
    private static int[] slots;
    public static int pickSlot = -1;

    // Updated by H1ggsK
    public static boolean pickSwitch(int slot) {
        if (slot >= 0) {
            Managers.HOLDING.modifyStartTime = System.currentTimeMillis();
            pickSlot = slot;

            // Obtain the container ID and revision from the player's current screen handler.
            int syncId = mc.player.containerMenu.containerId;
            // Some implementations offer a revision getter; if not, you might default to 0.
            int revision = mc.player.containerMenu.getStateId();

            // Define the click parameters:
            // button: 0 for primary click (adjust if you need a different click type)
            int button = 0;
            // actionType: use SlotActionType.PICKUP for a normal click (or change as needed)
            ContainerInput actionType = ContainerInput.PICKUP;

            ItemStack stack = mc.player.containerMenu.getSlot(slot).getItem();

            Int2ObjectMap<HashedStack> modifiedStacks = new Int2ObjectOpenHashMap<>();

            HashedPatchMap.HashGenerator componentHasher = ((ComponentHasherNetworkHandlerAccessor) mc.getConnection()).getDecoratedHashOpsGenerator();
            HashedStack stackHash = HashedStack.create(stack, componentHasher);

            ServerboundContainerClickPacket packet = new ServerboundContainerClickPacket(
                syncId,
                revision,
                (short) slot,
                (byte) button,
                actionType,
                modifiedStacks,
                stackHash
            );

            mc.getConnection().send(packet);



            return true;
        }
        return false;
    }

    // Updated by H1ggsK
    public static void pickSwapBack() {
        if (pickSlot >= 0) {
            // Obtain the container (screen handler) ID and revision.
            int syncId = mc.player.containerMenu.containerId;
            // Use the current revision, or default to 0 if not available.
            int revision = mc.player.containerMenu.getStateId();

            // Set click parameters.
            int button = 0; // Typically 0 for left-click; adjust as needed.
            ContainerInput actionType = ContainerInput.PICKUP;

            // Retrieve the item stack from the stored pickSlot.
            ItemStack stack = mc.player.containerMenu.getSlot(pickSlot).getItem();

            // Create an empty map for modified stacks (populate if needed).
            Int2ObjectMap<HashedStack> modifiedStacks = new Int2ObjectOpenHashMap<>();

            HashedPatchMap.HashGenerator componentHasher = ((ComponentHasherNetworkHandlerAccessor) mc.getConnection()).getDecoratedHashOpsGenerator();
            HashedStack stackHash = HashedStack.create(stack, componentHasher);

            ServerboundContainerClickPacket packet = new ServerboundContainerClickPacket(
                syncId,
                revision,
                (short) pickSlot,
                (byte) button,
                actionType,
                modifiedStacks,
                stackHash
            );mc.getConnection().send(packet);

            // Reset pickSlot after sending the packet.
            pickSlot = -1;
        }
    }

    // Credits to rickyracuun
    public static boolean invSwitch(int slot) {
        if (slot >= 0) {
            AbstractContainerMenu handler = mc.player.containerMenu;
            ItemStack stack = handler.getSlot(slot).getItem();
            HashedPatchMap.HashGenerator componentHasher = ((ComponentHasherNetworkHandlerAccessor) mc.getConnection()).getDecoratedHashOpsGenerator();
            HashedStack stackHash = HashedStack.create(stack, componentHasher);
            Int2ObjectMap<HashedStack> modifiedStacks = new Int2ObjectOpenHashMap<>();

            mc.getConnection().send(new ServerboundContainerClickPacket(handler.containerId,
                handler.getStateId(), (short) (Inventory.INVENTORY_SIZE + Managers.HOLDING.slot),
                (byte) slot, ContainerInput.SWAP, modifiedStacks, stackHash)
            );
            ((IMultiPlayerGameMode) mc.gameMode).meteor$syncSelected();
            slots = new int[]{slot, Managers.HOLDING.slot};
            return true;
        }
        return false;
    }

    public static void swapBack() {
        AbstractContainerMenu handler = mc.player.containerMenu;
        int slot = slots[0];
        ItemStack stack = handler.getSlot(slot).getItem();
        HashedPatchMap.HashGenerator componentHasher = ((ComponentHasherNetworkHandlerAccessor) mc.getConnection()).getDecoratedHashOpsGenerator();
        HashedStack stackHash = HashedStack.create(stack, componentHasher);
        Int2ObjectMap<HashedStack> modifiedStacks = new Int2ObjectOpenHashMap<>();

        mc.getConnection().send(new ServerboundContainerClickPacket(handler.containerId,
            handler.getStateId(), (short) (Inventory.INVENTORY_SIZE + slots[1]),
            (byte) slots[0], ContainerInput.SWAP, modifiedStacks, stackHash)
        );
        ((IMultiPlayerGameMode) mc.gameMode).meteor$syncSelected();
    }
}
