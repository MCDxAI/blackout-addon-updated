/*
 *   This file is a part the best minecraft mod called Blackout Client (https://github.com/KassuK1/Blackout-Client)
 *   and licensed under the GNU GENERAL PUBLIC LICENSE (check LICENCE file or https://www.gnu.org/licenses/gpl-3.0.html)
 *   Copyright (C) 2024 KassuK and OLEPOSSU
 */

package kassuk.addon.blackout.utils;

import com.mojang.authlib.properties.Property;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import kassuk.addon.blackout.mixins.AccessorCompoundTag;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.*;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.*;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.TeamColor;

/**
 * @author OLEPOSSU
 */

// H1ggsK here
// I invalidated many packet types. Some changed so much that I cannot fix the definitions.
// These were only the unneeded ones. Someone must fix the rest later.

public class PacketNames {
  public static final Map<Class<?>, PacketData<?>> s2c = new HashMap<>();
  public static final Map<Class<?>, PacketData<?>> c2s = new HashMap<>();

  static {
    /* ************************ C2S ************************ */

    // common
    c2s(
        ServerboundClientInformationPacket.class,
        "ClientOptions",
        packet ->
            "language: "
                + packet.information().language()
                + " allowsServerListing: "
                + packet.information().allowsListing()
                + " chatColorsEnabled: "
                + packet.information().chatColors()
                + " chatVisibility: "
                + packet.information().chatVisibility()
                + " filtersText: "
                + packet.information().textFilteringEnabled()
                + " mainArm: "
                + packet.information().mainHand().name()
                + " playerModelParts: "
                + packet.information().modelCustomisation()
                + " viewDistance: "
                + packet.information().viewDistance());
    c2s(ServerboundPongPacket.class, "CommonPong", packet -> "parameter: " + packet.getId());
    c2s(
        ServerboundCustomPayloadPacket.class,
        "CustomPayload",
        packet -> "identifier: " + packet.payload().type().id().toString());
    c2s(ServerboundKeepAlivePacket.class, "KeepAlive", packet -> "id: " + packet.getId());
    c2s(
        ServerboundResourcePackPacket.class,
        "ResourcePackStatus",
        packet -> "id: " + packet.id().toString() + " status: " + packet.action().name());

    // config
    c2s(
        ServerboundFinishConfigurationPacket.class,
        "Ready",
        packet -> "transitionsNetworkState: " + packet.isTerminal());

    // handshake
    c2s(
        ClientIntentionPacket.class,
        "Handshake",
        packet ->
            "address: "
                + packet.hostName()
                + " port: "
                + packet.port()
                + " protocolVersion: "
                + packet.protocolVersion()
                + " transitionsNetworkState: "
                + packet.isTerminal()
                + " intendedState: "
                + packet.intention());

    // login
    c2s(
        ServerboundLoginAcknowledgedPacket.class,
        "EnterConfiguration",
        packet -> "transitionsNetworkState: " + packet.isTerminal());
    c2s(
        ServerboundHelloPacket.class,
        "LoginHello",
        packet -> "name: " + packet.name() + " id: " + packet.profileId().toString());
    c2s(ServerboundKeyPacket.class, "LoginKey");
    c2s(
        ServerboundCustomQueryAnswerPacket.class,
        "LoginQueryResponse",
        packet -> "queryId: " + packet.transactionId());

    // play
    c2s(
        ServerboundChunkBatchReceivedPacket.class,
        "AcknowledgeChunks",
        packet -> "desiredChunksPerTick: " + packet.desiredChunksPerTick());
    c2s(
        ServerboundConfigurationAcknowledgedPacket.class,
        "AcknowledgeReconfiguration",
        packet -> "transitionsNetworkState: " + packet.isTerminal());
    c2s(
        ServerboundSeenAdvancementsPacket.class,
        "AdvancementTab",
        packet ->
            "action: " + packet.getAction().name() + " tabToOpen: " + packet.getTab().toString());
    c2s(
        ServerboundPaddleBoatPacket.class,
        "BoatPaddleState",
        packet -> "isLeftPaddling: " + packet.getLeft() + " isRightPaddling: " + packet.getRight());
    c2s(
        ServerboundEditBookPacket.class,
        "BookUpdate",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "title: " + packet.title().orElse("null") + " slot: " + packet.slot());
          for (int page = 0; page < packet.pages().size(); page++)
            builder.append("\n").append(packet.pages().get(page));
          return builder.toString();
        });
    c2s(
        ServerboundContainerButtonClickPacket.class,
        "ButtonClick",
        packet -> "syncId: " + packet.containerId() + " buttonId: " + packet.buttonId());
    c2s(
        ServerboundChatPacket.class,
        "ChatMessage",
        packet ->
            "chatMessage: "
                + packet.message()
                + " timeStamp: "
                + packet.timeStamp().toString()
                + " acknowledgementOffset: "
                + packet.lastSeenMessages().offset()
                + " signature: "
                + Objects.requireNonNull(packet.signature())
                + " salt: "
                + packet.salt());
    c2s(
        ServerboundContainerClickPacket.class,
        "ClickSlot",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "syncId: "
                      + packet.containerId()
                      + " slot: "
                      + packet.slotNum()
                      + " button: "
                      + packet.buttonNum()
                      + " action: "
                      + packet.containerInput().name()
                      + " revision: "
                      + packet.stateId());

          builder.append(" modified: {");
          packet
              .changedSlots()
              .forEach(
                  (i, stack) -> {
                    if (stack instanceof HashedStack.ActualItem impl) {
                      builder
                          .append("\nslot ")
                          .append(i)
                          .append(": ")
                          .append(
                              impl.item().value().getName(new ItemStack(impl.item())).getString())
                          .append(" ")
                          .append(impl.count());
                    } else {
                      builder.append("\nslot ").append(i).append(": <empty or unknown>");
                    }
                  });
          builder.append("\n} cursor: ");

          if (packet.carriedItem() instanceof HashedStack.ActualItem impl) {
            builder
                .append(impl.item().value().getName(new ItemStack(impl.item())).getString())
                .append(" ")
                .append(impl.count());
          } else {
            builder.append("<empty>");
          }

          return builder.toString();
        });

    c2s(
        ServerboundPlayerCommandPacket.class,
        "ClientCommand",
        packet ->
            "entityId: "
                + packet.getId()
                + " mountJumpTime: "
                + packet.getData()
                + " mode: "
                + packet.getAction().name());
    c2s(
        ServerboundClientCommandPacket.class,
        "ClientStatus",
        packet -> "mode: " + packet.getAction().name());
    c2s(
        ServerboundContainerClosePacket.class,
        "CloseHandledScreen",
        packet -> "syncId: " + packet.getContainerId());
    c2s(
        ServerboundChatCommandPacket.class,
        "CommandExecution",
        packet -> "command: " + packet.command());
    c2s(
        ServerboundPlaceRecipePacket.class,
        "CraftRequest",
        packet ->
            "syncId: "
                + packet.containerId()
                + " shouldCraftAll: "
                + packet.useMaxItems()
                + " recipe: "
                + packet.recipe().toString());
    c2s(
        ServerboundSetCreativeModeSlotPacket.class,
        "CreativeInventoryAction",
        packet ->
            "slot: "
                + packet.slotNum()
                + " name: "
                + packet.itemStack().getHoverName().getString()
                + " count: "
                + packet.itemStack().getCount());
    c2s(ServerboundSwingPacket.class, "HandSwing", packet -> "hand: " + packet.getHand().name());
    c2s(
        ServerboundJigsawGeneratePacket.class,
        "JigsawGenerating",
        packet ->
            "pos: "
                + packet.getPos().toShortString()
                + " maxDepth: "
                + packet.levels()
                + " shouldKeepJigsaws: "
                + packet.keepJigsaws());
    c2s(
        ServerboundChatAckPacket.class,
        "MessageAcknowledgment",
        packet -> "offset: " + packet.offset());
    c2s(
        ServerboundPlayerActionPacket.class,
        "PlayerAction",
        packet ->
            "action: "
                + packet.getAction().name()
                + "pos: "
                + packet.getPos().toShortString()
                + " direction: "
                + packet.getDirection().name()
                + " sequence: "
                + packet.getSequence());
    // FML // c2s(PlayerInputC2SPacket.class, "PlayerInput", packet -> "forward: " +
    // packet.getForward() + " sideways: " + packet.getSideways() + " isJumping: " +
    // packet.isJumping() + " isSneaking: " + packet.isSneaking());
    c2s(
        ServerboundUseItemOnPacket.class,
        "PlayerInteractBlock",
        packet ->
            "hand: "
                + packet.getHand().name()
                + " blockPos: "
                + packet.getHitResult().getBlockPos().toShortString()
                + " pos: "
                + packet.getHitResult().getLocation().toString()
                + " side: "
                + packet.getHitResult().getDirection()
                + " isInsideBlock: "
                + packet.getHitResult().isInside()
                + " type: "
                + packet.getHitResult().getType().name()
                + " sequence: "
                + packet.getSequence());
    c2s(
        ServerboundInteractPacket.class,
        "PlayerInteractEntity",
        packet ->
            "id: "
                + packet.entityId()
                + " hand: "
                + packet.hand()
                + " location: "
                + packet.location()
                + " isPlayerSneaking: "
                + packet.usingSecondaryAction());
    c2s(
        ServerboundAttackPacket.class,
        "PlayerInteractAttack",
        packet -> "id: " + packet.entityId());
    c2s(
        ServerboundUseItemPacket.class,
        "PlayerInteractItem",
        packet -> "hand: " + packet.getHand().name() + " sequence: " + packet.getSequence());

    c2s(
        ServerboundMovePlayerPacket.PosRot.class,
        "PlayerMove Full",
        packet ->
            "x: "
                + packet.getX(0)
                + " y: "
                + packet.getY(0)
                + " z: "
                + packet.getZ(0)
                + " yaw: "
                + packet.getYRot(0)
                + " pitch: "
                + packet.getXRot(0)
                + " isOnGround: "
                + packet.isOnGround());
    c2s(
        ServerboundMovePlayerPacket.Pos.class,
        "PlayerMove PositionAndOnGround",
        packet ->
            "x: "
                + packet.getX(0)
                + " y: "
                + packet.getY(0)
                + " z: "
                + packet.getZ(0)
                + " isOnGround: "
                + packet.isOnGround());
    c2s(
        ServerboundMovePlayerPacket.Rot.class,
        "PlayerMove LookAndOnGround",
        packet ->
            "yaw: "
                + packet.getYRot(0)
                + " pitch: "
                + packet.getXRot(0)
                + " isOnGround: "
                + packet.isOnGround());
    c2s(
        ServerboundMovePlayerPacket.StatusOnly.class,
        "PlayerMove OnGroundOnly",
        packet -> "isOnGround: " + packet.isOnGround());

    c2s(
        ServerboundChatSessionUpdatePacket.class,
        "PlayerSession",
        packet ->
            "sessionId: "
                + packet.chatSession().sessionId()
                + " isExpired: "
                + packet.chatSession().profilePublicKey().hasExpired()
                + " expiresAt: "
                + packet.chatSession().profilePublicKey().expiresAt().toString()
                + " keySignature: "
                + byteArrToString(packet.chatSession().profilePublicKey().keySignature()));
    c2s(
        ServerboundBlockEntityTagQueryPacket.class,
        "QueryBlockNbt",
        packet -> "pos: " + packet.getPos() + " transactionId: " + packet.getTransactionId());
    c2s(
        ServerboundEntityTagQueryPacket.class,
        "QueryEntityNbt",
        packet ->
            "entityId: " + packet.getEntityId() + " transactionId: " + packet.getTransactionId());
    c2s(
        ServerboundRecipeBookSeenRecipePacket.class,
        "RecipeBookData",
        packet -> "recipeId: " + packet.recipe().toString());
    c2s(
        ServerboundRecipeBookChangeSettingsPacket.class,
        "RecipeCategoryOptions",
        packet ->
            "category: "
                + packet.getBookType().name()
                + " isFilteringCraftable: "
                + packet.isFiltering()
                + " isGuiOpen: "
                + packet.isOpen());
    c2s(ServerboundRenameItemPacket.class, "RenameItem", packet -> "name: " + packet.getName());
    c2s(
        ServerboundCommandSuggestionPacket.class,
        "RequestCommandCompletions",
        packet -> "partialCommand: " + packet.getCommand() + " completionId: " + packet.getId());
    c2s(
        ServerboundSelectTradePacket.class,
        "SelectMerchantTrade",
        packet -> "tradeId: " + packet.getItem());
    c2s(
        ServerboundContainerSlotStateChangedPacket.class,
        "SlotChangedState",
        packet ->
            "slotId: "
                + packet.slotId()
                + " newState: "
                + packet.newState()
                + " screenHandlerId: "
                + packet.containerId());
    c2s(ServerboundTeleportToEntityPacket.class, "SpectatorTeleport");
    c2s(
        ServerboundAcceptTeleportationPacket.class,
        "TeleportConfirm",
        packet -> "teleportId: " + packet.getId());
    c2s(
        ServerboundSetBeaconPacket.class,
        "UpdateBeacon",
        packet -> {
          StringBuilder builder = new StringBuilder();

          builder.append("primary: ");
          if (packet.primary().isPresent())
            builder.append(packet.primary().get().getRegisteredName());
          else builder.append("null");

          builder.append(" secondary: ");
          if (packet.secondary().isPresent())
            builder.append(packet.secondary().get().getRegisteredName());
          else builder.append("null");

          return builder.toString();
        });
    c2s(
        ServerboundSetCommandBlockPacket.class,
        "UpdateCommandBlock",
        packet ->
            "command: "
                + packet.getCommand()
                + " pos: "
                + packet.getPos()
                + " isAlwaysActive: "
                + packet.isAutomatic()
                + " isConditional: "
                + packet.isConditional()
                + " shouldTrackOutput: "
                + packet.isTrackOutput()
                + " type: "
                + packet.getMode().name());
    c2s(
        ServerboundSetCommandMinecartPacket.class,
        "UpdateCommandBlockMinecart",
        packet ->
            "command: " + packet.getCommand() + " shouldTrackOutput: " + packet.isTrackOutput());
    c2s(
        ServerboundChangeDifficultyPacket.class,
        "UpdateDifficulty",
        packet -> "difficulty: " + packet.difficulty().getSerializedName());
    c2s(
        ServerboundLockDifficultyPacket.class,
        "UpdateDifficultyLock",
        packet -> "isDifficultyLocked: " + packet.isLocked());
    c2s(
        ServerboundSetJigsawBlockPacket.class,
        "UpdateJigsaw",
        packet ->
            "name: "
                + packet.getName().toString()
                + " pos: "
                + packet.getPos().toShortString()
                + " finalState: "
                + packet.getFinalState()
                + " jointType: "
                + packet.getJoint().getSerializedName()
                + " target: "
                + packet.getTarget().toString()
                + " pool: "
                + packet.getPool().toString()
                + " placementPriority: "
                + packet.getPlacementPriority()
                + " selectionPriority: "
                + packet.getSelectionPriority());
    c2s(
        ServerboundPlayerAbilitiesPacket.class,
        "UpdatePlayerAbilities",
        packet -> "isFlying: " + packet.isFlying());
    c2s(
        ServerboundSetCarriedItemPacket.class,
        "UpdateSelectedSlot",
        packet -> "selectedSlot: " + packet.getSlot());
    c2s(
        ServerboundSignUpdatePacket.class,
        "UpdateSign",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "pos: " + packet.getPos().toShortString() + " isFront: " + packet.isFrontText());
          for (String str : packet.getLines()) builder.append("\n").append(str);
          return builder.toString();
        });
    c2s(
        ServerboundSetStructureBlockPacket.class,
        "UpdateStructureBlock",
        packet ->
            "pos: "
                + packet.getPos().toShortString()
                + " rotation: "
                + packet.getRotation().getSerializedName()
                + " offset: "
                + packet.getOffset().toShortString()
                + " size: "
                + packet.getSize().toShortString()
                + " seed: "
                + packet.getSeed()
                + " templateName: "
                + packet.getName()
                + " mode: "
                + packet.getMode().getSerializedName()
                + " action: "
                + packet.getUpdateType().name()
                + " metaData: "
                + packet.getData()
                + " integrity: "
                + packet.getIntegrity()
                + " mirror: "
                + packet.getMirror());
    c2s(
        ServerboundMoveVehiclePacket.class,
        "VehicleMove",
        packet ->
            "x: "
                + packet.position().x
                + " y: "
                + packet.position().y
                + " z: "
                + packet.position().z
                + " yaw: "
                + packet.yRot()
                + " pitch: "
                + packet.xRot());

    // query
    c2s(
        ServerboundPingRequestPacket.class,
        "QueryPing",
        packet -> "startTime: " + packet.getTime());
    c2s(ServerboundStatusRequestPacket.class, "QueryRequest");

    /* ************************ S2C ************************ */

    // common
    s2c(ClientboundPingPacket.class, "CommonPing", packet -> "parameter: " + packet.getId());
    s2c(
        ClientboundCustomPayloadPacket.class,
        "CustomPayload",
        packet -> "payloadId: " + packet.payload().type().toString());
    s2c(
        ClientboundDisconnectPacket.class,
        "Disconnect",
        packet -> "reason: " + packet.reason().getString());
    s2c(ClientboundKeepAlivePacket.class, "KeepAlive", packet -> "id: " + packet.getId());
    s2c(
        ClientboundResourcePackPopPacket.class,
        "ResourcePackRemove",
        packet -> "id: " + packet.id());
    s2c(
        ClientboundResourcePackPushPacket.class,
        "ResourcePackSend",
        packet ->
            "url: "
                + packet.url()
                + " hash: "
                + packet.hash()
                + " id: "
                + packet.id().toString()
                + " prompt: "
                + packet.prompt().orElse(Component.nullToEmpty("null")).getString()
                + " required: "
                + packet.required());
    s2c(
        ClientboundUpdateTagsPacket.class,
        "SynchronizeTags",
        packet -> {
          StringBuilder builder = new StringBuilder("groups: ");
          packet
              .getTags()
              .forEach(
                  (key, serialized) ->
                      builder
                          .append("\n")
                          .append(key.toString())
                          .append(" serializedSize: ")
                          .append(serialized.size()));
          return builder.toString();
        });

    // config
    s2c(ClientboundRegistryDataPacket.class, "DynamicRegistries");
    s2c(
        ClientboundUpdateEnabledFeaturesPacket.class,
        "Features",
        packet -> {
          StringBuilder builder = new StringBuilder("features: ");
          for (Identifier v : packet.features()) builder.append("\n").append(v.toString());
          return builder.toString();
        });
    s2c(
        ClientboundFinishConfigurationPacket.class,
        "Ready",
        packet -> "transitionsNetworkState: " + packet.isTerminal());

    // login
    s2c(
        ClientboundLoginCompressionPacket.class,
        "LoginCompression",
        packet -> "compressionThreshold: " + packet.getCompressionThreshold());
    s2c(
        ClientboundLoginDisconnectPacket.class,
        "LoginDisconnect",
        packet -> "reason: " + packet.reason());
    s2c(
        ClientboundHelloPacket.class,
        "LoginHello",
        packet ->
            "serverId: "
                + packet.getServerId()
                + " nonce: "
                + byteArrToString(packet.getChallenge()));
    s2c(
        ClientboundCustomQueryPacket.class,
        "LoginQueryRequest",
        packet -> "queryId: " + packet.transactionId() + " payloadId: " + packet.payload().id());
    s2c(
        ClientboundLoginFinishedPacket.class,
        "LoginSuccess",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "name: "
                      + packet.gameProfile().name()
                      + " id: "
                      + packet.gameProfile().id().toString()
                      + " newNetworkState: "
                      + packet.gameProfile().id().toString()
                      + " properties: {");
          packet
              .gameProfile()
              .properties()
              .asMap()
              .forEach(
                  (str, collection) -> {
                    builder.append("\n").append(str);
                    for (Property v : collection) {
                      builder
                          .append("\n  ")
                          .append(v.name())
                          .append(" ")
                          .append(v.value())
                          .append(" ")
                          .append(v.signature());
                    }
                  });
          builder.append("\n}");
          return builder.toString();
        });

    // play
    s2c(
        ClientboundUpdateAdvancementsPacket.class,
        "AdvancementUpdate",
        packet -> {
          StringBuilder builder = new StringBuilder("shouldClearCurrent: " + packet.shouldReset());

          // this packet is gonna fill the whole log
          builder.append(" advancementIdsToRemove: {");
          for (Identifier v : packet.getRemoved()) builder.append("\n  ").append(v.toString());
          builder.append("\n}");

          builder.append(" advancementsToEarn: {");
          for (AdvancementHolder v : packet.getAdded()) builder.append("\n  ").append(v.toString());
          builder.append("\n}");

          builder.append(" advancementsToEarn: {");
          packet
              .getProgress()
              .forEach(
                  (id, progress) -> {
                    builder
                        .append("\n  id: ")
                        .append(id.toString())
                        .append(" isDone: ")
                        .append(progress.isDone())
                        .append(" isAnyObtained: ")
                        .append(progress.hasProgress())
                        .append(" progressBarPercentage: ")
                        .append(progress.getPercent())
                        .append(" progressBarFraction: ")
                        .append(
                            progress.getProgressText() == null
                                ? "null"
                                : progress.getProgressText().getString())
                        .append(" earliestProgressObtainDate: ")
                        .append(
                            progress.getFirstProgressDate() == null
                                ? "null"
                                : progress.getFirstProgressDate().toString())
                        .append(" obtainedCriteria: {");

                    for (String str : progress.getCompletedCriteria()) {
                      builder.append("\n    ").append(str);
                    }

                    builder.append("\n  }\n   unobtainedCriteria");
                    for (String str : progress.getRemainingCriteria()) {
                      builder.append("\n    ").append(str);
                    }
                    builder.append("\n  }");
                  });
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundBlockDestructionPacket.class,
        "BlockBreakingProgress",
        packet ->
            "pos: "
                + packet.getPos().toShortString()
                + " progress: "
                + packet.getProgress()
                + " entityId: "
                + packet.getId());
    s2c(
        ClientboundBlockEntityDataPacket.class,
        "BlockEntityUpdate",
        packet -> {
          StringBuilder builder =
              new StringBuilder("pos: " + packet.getPos().toShortString() + " blockEntityType: ");
          builder.append(packet.getType().builtInRegistryHolder().getRegisteredName());
          builder.append(" nbt: {");
          ((AccessorCompoundTag) (Object) packet.getTag())
              .blackout$getTags()
              .forEach(
                  (string, element) ->
                      builder.append("\n  ").append(string).append(" ").append(element.asString()));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundBlockEventPacket.class,
        "BlockEvent",
        packet ->
            "pos: "
                + packet.getPos().toShortString()
                + " block: "
                + packet.getBlock().getName()
                + " data: "
                + packet.getB1()
                + " type: "
                + packet.getB0());
    s2c(
        ClientboundBlockUpdatePacket.class,
        "BlockUpdate",
        packet ->
            "pos: " + packet.getPos() + " state: {" + packet.getBlockState().toString() + "}");
    s2c(ClientboundBossEventPacket.class, "BossBar");
    s2c(
        ClientboundBundlePacket.class,
        "Bundle",
        packet -> {
          StringBuilder builder = new StringBuilder("BUNDLE START");
          packet
              .subPackets()
              .forEach(
                  p -> {
                    builder
                        .append("\n")
                        .append(getData(p).getName())
                        .append(" ")
                        .append(getData(p).funnyApply(p));
                  });
          builder.append("\nBUNDLE END");
          return builder.toString();
        });
    s2c(
        ClientboundPlayerChatPacket.class,
        "ChatMessage",
        packet ->
            "unsignedContent: "
                + packet.unsignedContent().getString()
                + " sender: "
                + packet.sender().toString()
                + " index: "
                + packet.index()
                + " isWritingErrorSkippable: "
                + packet.isSkippable()
                + " isFullyFiltered: "
                + packet.filterMask().isFullyFiltered()
                + " isPassThrough: "
                + packet.filterMask().isEmpty()
                + " bodyContent: "
                + packet.body().content()
                + " bodySalt: "
                + packet.body().salt()
                + " bodyTimestamp: "
                + packet.body().timeStamp()
                + " bodyLastSeenSize: "
                + packet.body().lastSeen().entries().size()
                + " signature: "
                + byteArrToString(packet.signature().bytes())
                + " serializedParametersName: "
                + packet.chatType().name().getString()
                + " serializedParametersTargetName: "
                + packet.chatType().targetName().orElse(Component.nullToEmpty("null")).getString());
    s2c(
        ClientboundCustomChatCompletionsPacket.class,
        "ChatSuggestions",
        packet -> {
          StringBuilder builder =
              new StringBuilder("action: " + packet.action().name() + " entries: {");
          for (String entry : packet.entries()) builder.append("\n  ").append(entry);
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundChunksBiomesPacket.class,
        "ChunkBiomeData",
        packet -> {
          StringBuilder builder = new StringBuilder("chunkBiomeData: {");
          for (var v : packet.chunkBiomeData())
            builder
                .append("\n  pos: ")
                .append(v.pos())
                .append(" buffer: ")
                .append(byteArrToString(v.buffer()));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundLevelChunkWithLightPacket.class,
        "ChunkData",
        packet -> {
          String builder =
              "chunkX: "
                  + packet.getX()
                  + " chunkZ: "
                  + packet.getZ()
                  + " chunkDataSectionsDataBuf: "
                  + byteArrToString(packet.getChunkData().getReadBuffer().array())
                  + " chunkDataHeightMap: {"
                  + "\n} "
                  + " lightDataBlockNibblesSize: "
                  + packet.getLightData().getBlockUpdates().size()
                  + " lightDataBlockNibblesSize: "
                  + packet.getLightData().getSkyUpdates().size();

          return builder;
        });
    s2c(ClientboundSectionBlocksUpdatePacket.class, "ChunkDeltaUpdate");
    s2c(
        ClientboundSetChunkCacheRadiusPacket.class,
        "ChunkLoadDistance",
        packet -> "distance: " + packet.getRadius());
    s2c(
        ClientboundSetChunkCacheCenterPacket.class,
        "ChunkRenderDistanceCenter",
        packet -> "chunkX: " + packet.getX() + " chunkZ: " + packet.getZ());
    s2c(
        ClientboundChunkBatchFinishedPacket.class,
        "ChunkSent",
        packet -> "batchSize: " + packet.batchSize());
    s2c(
        ClientboundClearTitlesPacket.class,
        "ClearTitle",
        packet -> "shouldReset: " + packet.shouldResetTimes());
    s2c(
        ClientboundContainerClosePacket.class,
        "CloseScreen",
        packet -> "syncId: " + packet.getContainerId());
    s2c(
        ClientboundCommandSuggestionsPacket.class,
        "CommandSuggestions",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "id: "
                      + packet.id()
                      + " length: "
                      + packet.length()
                      + " start: "
                      + packet.start()
                      + " suggestions: {");
          packet
              .suggestions()
              .forEach(
                  suggestion ->
                      builder
                          .append("\n  text: ")
                          .append(suggestion.text())
                          .append(" toolTip: ")
                          .append(
                              suggestion
                                  .tooltip()
                                  .orElse(Component.nullToEmpty("null"))
                                  .getString()));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(ClientboundCommandsPacket.class, "CommandTree"); // TODO: should add something here
    // FML // s2c(CooldownUpdateS2CPacket.class, "CooldownUpdate", packet -> "item: " +
    // packet.item().getName() + " cooldown: " + packet.cooldown());
    s2c(
        ClientboundPlaceGhostRecipePacket.class,
        "CraftFailedResponse",
        packet -> "syncId: " + packet.containerId());
    s2c(
        ClientboundHurtAnimationPacket.class,
        "DamageTiltS2CPacket",
        packet -> "id: " + packet.id() + " yaw: " + packet.yaw());
    s2c(
        ClientboundPlayerCombatKillPacket.class,
        "DeathMessage",
        packet -> "playerId: " + packet.playerId() + " message: " + packet.message().getString());
    s2c(
        ClientboundChangeDifficultyPacket.class,
        "Difficulty",
        packet ->
            "difficulty: "
                + packet.difficulty().getSerializedName()
                + " isDifficultyLocked: "
                + packet.locked());
    s2c(ClientboundPlayerCombatEndPacket.class, "EndCombat");
    s2c(ClientboundPlayerCombatEnterPacket.class, "EnterCombat");
    s2c(ClientboundStartConfigurationPacket.class, "EnterReconfiguration");
    s2c(
        ClientboundRemoveEntitiesPacket.class,
        "EntitiesDestroy",
        packet -> {
          StringBuilder builder = new StringBuilder("entityIds: {");
          packet.getEntityIds().forEach(id -> builder.append("\n  ").append(id));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundAnimatePacket.class,
        "EntityAnimation",
        packet -> "entityId: " + packet.getId() + " animationId: " + packet.getAction());
    s2c(
        ClientboundSetEntityLinkPacket.class,
        "EntityAttach",
        packet ->
            "holdingEntityId: "
                + packet.getDestId()
                + " attachedEntityId: "
                + packet.getSourceId());
    s2c(
        ClientboundUpdateAttributesPacket.class,
        "EntityAttributes",
        packet -> {
          StringBuilder builder =
              new StringBuilder("entityId: " + packet.getEntityId() + " attributes: {");
          packet
              .getValues()
              .forEach(
                  entry -> {
                    String attribute = entry.attribute().getRegisteredName();

                    builder
                        .append("\n  attribute: ")
                        .append(attribute)
                        .append(" base: ")
                        .append(entry.base())
                        .append(" modifiers: {");
                    entry
                        .modifiers()
                        .forEach(
                            modifier ->
                                builder
                                    .append("\n    id: ")
                                    .append(modifier.id().toString())
                                    .append(" value: ")
                                    .append(modifier.amount()));
                    builder.append("\n  }");
                  });
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundDamageEventPacket.class,
        "EntityDamage",
        packet -> {
          StringBuilder builder =
              new StringBuilder("entityId: " + packet.entityId() + " sourceType: ");
          builder.append(packet.sourceType().getRegisteredName());

          builder.append(" sourcePosition: ");
          Optional<Vec3> rur = packet.sourcePosition();
          builder.append(rur.isPresent() ? rur.get().toString() : "null");

          builder
              .append(" sourceCauseId: ")
              .append(packet.sourceCauseId())
              .append(" sourceDirectId: ")
              .append(packet.sourceDirectId());
          return builder.toString();
        });
    s2c(
        ClientboundSetEquipmentPacket.class,
        "EntityEquipmentUpdate",
        packet -> {
          StringBuilder builder =
              new StringBuilder("entityId: " + packet.getEntity() + " equipment: {");
          packet
              .getSlots()
              .forEach(
                  pair ->
                      builder
                          .append("\n  type: ")
                          .append(pair.getFirst().getName())
                          .append(" item: ")
                          .append(pair.getSecond().getItem().getName(pair.getSecond()).getString())
                          .append(" count: ")
                          .append(pair.getSecond().getCount()));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundSetPassengersPacket.class,
        "EntityPassengersSet",
        packet -> {
          StringBuilder builder =
              new StringBuilder("entityId: " + packet.getVehicle() + " passengerIds: ");
          for (int id : packet.getPassengers()) builder.append("\n  ").append(id);
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundTeleportEntityPacket.class,
        "EntityPosition",
        packet ->
            "entityId: "
                + packet.id()
                + " x: "
                + packet.change().position().x
                + " y: "
                + packet.change().position().y
                + " z: "
                + packet.change().position().z
                + " yaw: "
                + packet.change().yRot()
                + " pitch: "
                + packet.change().xRot()
                + " isOnGround: "
                + packet.onGround());
    s2c(
        ClientboundMoveEntityPacket.class,
        "Entity",
        packet ->
            "deltaX: "
                + packet.getXa()
                + " deltaY: "
                + packet.getYa()
                + " deltaZ: "
                + packet.getZa()
                + " yaw: "
                + packet.getYRot()
                + " pitch: "
                + packet.getXRot()
                + " hasRotation: "
                + packet.hasRotation()
                + " isPositionChanged: "
                + packet.hasPosition()
                + " isOnGround: "
                + packet.isOnGround());
    s2c(
        ClientboundRotateHeadPacket.class,
        "EntitySetHeadYaw",
        packet -> "headYaw: " + packet.getYHeadRot());
    s2c(
        ClientboundAddEntityPacket.class,
        "EntitySpawn",
        packet ->
            "entityId: "
                + packet.getId()
                + " entityData: "
                + packet.getData()
                + " entityType: "
                + packet.getType().getDescription()
                + " uuid: "
                + packet.getUUID().toString()
                + " x: "
                + packet.getX()
                + " y: "
                + packet.getY()
                + " z: "
                + packet.getZ()
                + " yaw: "
                + packet.getYRot()
                + " pitch: "
                + packet.getXRot()
                + " headYaw: "
                + packet.getYHeadRot()
                + " velocityX: "
                + packet.getMovement().x()
                + " velocityY: "
                + packet.getMovement().y()
                + " velocityZ: "
                + packet.getMovement().z());
    s2c(
        ClientboundUpdateMobEffectPacket.class,
        "EntityStatusEffect",
        packet -> {
          String effect = packet.getEffect().getRegisteredName();
          return "effectId: "
              + effect
              + " entityId: "
              + packet.getEntityId()
              + " amplifier: "
              + packet.getEffectAmplifier()
              + " duration: "
              + packet.getEffectDurationTicks()
              + " isAmbient: "
              + packet.isEffectAmbient()
              + " shouldShowIcon: "
              + packet.effectShowsIcon()
              + " shouldShowParticles: "
              + packet.isEffectVisible();
        });
    s2c(
        ClientboundEntityEventPacket.class,
        "EntityStatus",
        packet -> String.valueOf(packet.getEventId()));
    s2c(
        ClientboundSetEntityDataPacket.class,
        "EntityTrackerUpdate",
        packet -> {
          StringBuilder builder = new StringBuilder("id: " + packet.id() + " trackedValues: {");
          packet
              .packedItems()
              .forEach(
                  entry ->
                      builder
                          .append("\n  id: ")
                          .append(entry.id())
                          .append(" value: ")
                          .append(entry.value()));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundSetEntityMotionPacket.class,
        "EntityVelocityUpdate",
        packet ->
            "entityId: "
                + packet.id()
                + " velocityX: "
                + packet.movement().x()
                + " velocityY: "
                + packet.movement().y()
                + " velocityZ: "
                + packet.movement().z());
    s2c(
        ClientboundSetExperiencePacket.class,
        "ExperienceBarUpdate",
        packet ->
            "experience: "
                + packet.getExperienceLevel()
                + " barProgress: "
                + packet.getExperienceProgress()
                + " experienceLevel: "
                + packet.getTotalExperience());
    // s2c(ExperienceOrbSpawnS2CPacket.class, "ExperienceOrbSpawn", packet -> "entityId: " +
    // packet.getEntityId() + " experience: " + packet.getExperience() + " x: " + packet.getX() + "
    // y: " + packet.getY() + " z: " + packet.getZ());

    // FML //
    /*s2c(ExplosionS2CPacket.class, "Explosion", packet -> {
        StringBuilder builder = new StringBuilder("x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " playerVelocityX: " + packet.velo() + " playerVelocityY: " + packet.getPlayerVelocityY() + " playerVelocityZ: " + packet.getPlayerVelocityZ() + " destructionType: " + packet.getDestructionType().name() + " radius: " + packet.getRadius() + " particle: " + packet.getParticle().getType().toString() + " emitterParticle: " + packet.getEmitterParticle().getType().toString() + " soundEvent: ");
        builder.append(packet.explosionSound().getIdAsString());
        builder.append(" affectedBlocks: {");
        packet.getAffectedBlocks().forEach(pos -> builder.append("\n  ").append(pos.toShortString()));
        builder.append("\n}");
        return builder.toString();
    });*/

    s2c(
        ClientboundLoginPacket.class,
        "GameJoin",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "playerEntityId: "
                      + packet.playerId()
                      + " maxPlayers: "
                      + packet.maxPlayers()
                      + " viewDistance: "
                      + packet.chunkRadius()
                      + " simulationDistance: "
                      + packet.simulationDistance()
                      + " doLimitedCrafting: "
                      + packet.doLimitedCrafting()
                      + " enforcesSecureChat: "
                      + packet.enforcesSecureChat()
                      + " hardcore: "
                      + packet.hardcore()
                      + " showDeathScreen: "
                      + packet.showDeathScreen()
                      + " reducedDebugInfo: "
                      + packet.reducedDebugInfo()
                      + " commonPlayerSpawnInfoGameMode: "
                      + packet.commonPlayerSpawnInfo().gameType()
                      + " commonPlayerSpawnInfoIsFlat: "
                      + packet.commonPlayerSpawnInfo().isFlat()
                      + " commonPlayerSpawnInfoLastGameMode: "
                      + packet.commonPlayerSpawnInfo().previousGameType()
                      + " commonPlayerSpawnInfoSeed: "
                      + packet.commonPlayerSpawnInfo().seed()
                      + " commonPlayerSpawnInfoPortalCooldown: "
                      + packet.commonPlayerSpawnInfo().portalCooldown());
          builder
              .append(" commonPlayerSpawnInfoDimension: ")
              .append(packet.commonPlayerSpawnInfo().dimension().identifier().toString());
          builder
              .append(" commonPlayerSpawnInfoDimensionType: ")
              .append(packet.commonPlayerSpawnInfo().dimensionType().getRegisteredName());
          builder
              .append(" commonPlayerSpawnInfoLastDeathPos: ")
              .append(
                  packet.commonPlayerSpawnInfo().lastDeathLocation().isPresent()
                      ? packet.commonPlayerSpawnInfo().lastDeathLocation().get().toString()
                      : "null");
          builder.append(" dimensionIds: {");
          packet
              .levels()
              .forEach(key -> builder.append("\n  ").append(key.identifier().toString()));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundSystemChatPacket.class,
        "GameMessage",
        packet -> "content: " + packet.content().getString() + " overlay: " + packet.overlay());
    s2c(
        ClientboundGameEventPacket.class,
        "GameStateChange",
        packet -> "value: " + packet.getParam());
    s2c(
        ClientboundSetHealthPacket.class,
        "HealthUpdate",
        packet ->
            "health: "
                + packet.getHealth()
                + " food: "
                + packet.getFood()
                + " saturation: "
                + packet.getSaturation());
    s2c(
        ClientboundContainerSetContentPacket.class,
        "Inventory",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "syncId: "
                      + packet.containerId()
                      + " revision: "
                      + packet.stateId()
                      + "cursorStackItem: "
                      + packet.carriedItem().getItem().getName(packet.carriedItem()).getString()
                      + " cursorStackCount:"
                      + packet.carriedItem().getCount()
                      + " contents: {");
          for (int i = 0; i < packet.items().size(); i++) {
            ItemStack stack = packet.items().get(i);
            builder
                .append("\n  slot: ")
                .append(i)
                .append(" item: ")
                .append(stack.getItem().getName(stack).getString())
                .append(" count: ")
                .append(stack.getCount());
          }
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundTakeItemEntityPacket.class,
        "ItemPickupAnimation",
        packet ->
            "entityId: "
                + packet.getItemId()
                + " collectorEntityId: "
                + packet.getPlayerId()
                + " stackAmount: "
                + packet.getAmount());
    s2c(
        ClientboundLightUpdatePacket.class,
        "LightUpdate",
        packet ->
            "chunkX: "
                + packet.getX()
                + " chunkZ: "
                + packet.getZ()
                + " blockNibblesSize: "
                + packet.getLightData().getBlockUpdates().size()
                + " skyNibblesSize: "
                + packet.getLightData().getSkyUpdates().size());
    s2c(ClientboundPlayerLookAtPacket.class, "LookAt");
    s2c(
        ClientboundMapItemDataPacket.class,
        "MapUpdate",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "mapId: "
                      + packet.mapId().id()
                      + " locked: "
                      + packet.locked()
                      + " scale: "
                      + packet.scale());

          builder.append(" decorations: {");
          if (packet.decorations().isPresent()) {
            List<MapDecoration> decorations = packet.decorations().get();
            decorations.forEach(
                decoration ->
                    builder
                        .append("\n  name: ")
                        .append(decoration.name().orElse(Component.nullToEmpty("null")).getString())
                        .append(" x: ")
                        .append(decoration.x())
                        .append(" z: ")
                        .append(decoration.y())
                        .append(" x: ")
                        .append(" type: ")
                        .append(decoration.type().getRegisteredName())
                        .append(" rotation: ")
                        .append(decoration.rot())
                        .append(" assetId: ")
                        .append(decoration.getSpriteLocation())
                        .append(" isAlwaysRendered: ")
                        .append(decoration.renderOnFrame()));
          }
          builder.append("\n}");

          String startX;
          String startZ;
          String width;
          String height;
          String colors;

          if (packet.colorPatch().isPresent()) {
            MapItemSavedData.MapPatch data = packet.colorPatch().get();
            startX = String.valueOf(data.startX());
            startZ = String.valueOf(data.startY());
            width = String.valueOf(data.width());
            height = String.valueOf(data.height());
            colors = byteArrToString(data.mapColors());
          } else {
            startX = "null";
            startZ = "null";
            width = "null";
            height = "null";
            colors = "null";
          }

          builder
              .append(" startX: ")
              .append(startX)
              .append(" startZ: ")
              .append(startZ)
              .append(" width: ")
              .append(width)
              .append(" height: ")
              .append(height)
              .append(" colors: ")
              .append(colors);
          return builder.toString();
        });
    s2c(
        ClientboundTagQueryPacket.class,
        "NbtQueryResponse",
        packet -> {
          StringBuilder builder = new StringBuilder("transactionId: " + packet.getTransactionId());
          builder.append(" nbt: {");
          ((AccessorCompoundTag) (Object) packet.getTag())
              .blackout$getTags()
              .forEach(
                  (string, element) ->
                      builder.append("\n  ").append(string).append(" ").append(element.asString()));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundOpenScreenPacket.class,
        "OpenScreen",
        packet ->
            "name: "
                + packet.getTitle().getString()
                + " syncId: "
                + packet.getContainerId()
                + " screenHandlerType: "
                + packet.getType());
    s2c(
        ClientboundOpenBookPacket.class,
        "OpenWrittenBook",
        packet -> "hand: " + packet.getHand().name());
    s2c(
        ClientboundSetActionBarTextPacket.class,
        "OverlayMessage",
        packet -> "text: " + packet.text().getString());
    // FML // s2c(ParticleS2CPacket.class, "Particle", packet -> "count: " + packet.getCount() + "
    // x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " offsetX: " +
    // packet.getOffsetX() + " offsetY: " + packet.getOffsetY() + " offsetZ: " + packet.getOffsetZ()
    // + " speed: " + packet.getSpeed() + " isLongDistance: " + packet.isLongDistance() + "
    // parameterType: " + packet.getParameters().getType());
    s2c(
        ClientboundPlayerAbilitiesPacket.class,
        "PlayerAbilities",
        packet ->
            "isCreativeMod: "
                + packet.canInstabuild()
                + " allowFlying: "
                + packet.canFly()
                + " isInvulnerable: "
                + packet.isInvulnerable()
                + " isFlying: "
                + packet.isFlying()
                + " flySpeed: "
                + packet.getFlyingSpeed()
                + " walkSpeed: "
                + packet.getWalkingSpeed());
    s2c(
        ClientboundBlockChangedAckPacket.class,
        "PlayerActionResponse",
        packet -> "sequence: " + packet.sequence());
    s2c(
        ClientboundTabListPacket.class,
        "PlayerListHeader",
        packet ->
            "header: " + packet.header().getString() + " footer: " + packet.footer().getString());
    s2c(
        ClientboundPlayerInfoUpdatePacket.class,
        "PlayerList",
        packet -> {
          StringBuilder builder = new StringBuilder("entries: {");

          packet
              .entries()
              .forEach(
                  entry ->
                      builder
                          .append("\n  displayName: ")
                          .append(entry.displayName())
                          .append(" profileId: ")
                          .append(entry.profileId())
                          .append(" gameProfileName: ")
                          .append(entry.profile() == null ? "null" : entry.profile().name())
                          .append(" gameProfileId: ")
                          .append(entry.profile() == null ? "null" : entry.profile().id())
                          .append(" listed: ")
                          .append(entry.listed())
                          .append(" gameMode: ")
                          .append(entry.gameMode())
                          .append(" latency: ")
                          .append(entry.latency())
                          .append(" chatSessionId: ")
                          .append(
                              entry.chatSession() == null
                                  ? "null"
                                  : entry.chatSession().sessionId().toString())
                          .append(" isExpired: ")
                          .append(
                              entry.chatSession() == null
                                  ? "null"
                                  : entry.chatSession().profilePublicKey().hasExpired())
                          .append(" expiresAt: ")
                          .append(
                              entry.chatSession() == null
                                  ? "null"
                                  : entry.chatSession().profilePublicKey().expiresAt().toString())
                          .append(" keySignature: ")
                          .append(
                              entry.chatSession() == null
                                  ? "null"
                                  : byteArrToString(
                                      entry.chatSession().profilePublicKey().keySignature())));
          builder.append("\n} playerAdditionEntries: {");
          packet
              .newEntries()
              .forEach(
                  entry ->
                      builder
                          .append("\n  displayName: ")
                          .append(entry.displayName())
                          .append(" profileId: ")
                          .append(entry.profileId())
                          .append(" gameProfileName: ")
                          .append(entry.profile() == null ? "null" : entry.profile().name())
                          .append(" gameProfileId: ")
                          .append(entry.profile() == null ? "null" : entry.profile().id())
                          .append(" listed: ")
                          .append(entry.listed())
                          .append(" gameMode: ")
                          .append(entry.gameMode())
                          .append(" latency: ")
                          .append(entry.latency())
                          .append(" chatSessionId: ")
                          .append(
                              entry.chatSession() == null
                                  ? "null"
                                  : entry.chatSession().sessionId().toString())
                          .append(" isExpired: ")
                          .append(
                              entry.chatSession() == null
                                  ? "null"
                                  : entry.chatSession().profilePublicKey().hasExpired())
                          .append(" expiresAt: ")
                          .append(
                              entry.chatSession() == null
                                  ? "null"
                                  : entry.chatSession().profilePublicKey().expiresAt().toString())
                          .append(" keySignature: ")
                          .append(
                              entry.chatSession() == null
                                  ? "null"
                                  : byteArrToString(
                                      entry.chatSession().profilePublicKey().keySignature())));
          builder.append("\n} actions: {");
          packet.actions().forEach(action -> builder.append("\n  ").append(action.name()));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundPlayerPositionPacket.class,
        "PlayerPositionLook",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "teleportId: "
                      + packet.id()
                      + " x: "
                      + packet.change().position().x
                      + " y: "
                      + packet.change().position().y
                      + " z: "
                      + packet.change().position().z
                      + " yaw: "
                      + packet.change().yRot()
                      + " pitch: "
                      + packet.change().xRot());
          return builder.toString();
        });
    s2c(
        ClientboundPlayerInfoRemovePacket.class,
        "PlayerRemove",
        packet -> {
          StringBuilder builder = new StringBuilder("profileIds: {");
          packet.profileIds().forEach(id -> builder.append("\n  ").append(id.toString()));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundRespawnPacket.class,
        "PlayerRespawn",
        packet ->
            "flag: "
                + Integer.toBinaryString(packet.dataToKeep())
                + " commonPlayerSpawnInfoGameMode: "
                + packet.commonPlayerSpawnInfo().gameType()
                + " commonPlayerSpawnInfoIsFlat: "
                + packet.commonPlayerSpawnInfo().isFlat()
                + " commonPlayerSpawnInfoLastGameMode: "
                + packet.commonPlayerSpawnInfo().previousGameType()
                + " commonPlayerSpawnInfoSeed: "
                + packet.commonPlayerSpawnInfo().seed()
                + " commonPlayerSpawnInfoPortalCooldown: "
                + packet.commonPlayerSpawnInfo().portalCooldown()
                + " commonPlayerSpawnInfoDimension: "
                + packet.commonPlayerSpawnInfo().dimension().identifier().toString()
                + " commonPlayerSpawnInfoDimensionType: "
                + packet.commonPlayerSpawnInfo().dimensionType().getRegisteredName()
                + " commonPlayerSpawnInfoLastDeathPos: "
                + (packet.commonPlayerSpawnInfo().lastDeathLocation().isPresent()
                    ? packet.commonPlayerSpawnInfo().lastDeathLocation().get().toString()
                    : "null"));
    s2c(
        ClientboundSetDefaultSpawnPositionPacket.class,
        "PlayerSpawnPosition",
        packet ->
            "pos: "
                + packet.respawnData().pos().toShortString() /* + " angle: " + packet.getAngle()*/);
    s2c(
        ClientboundSoundEntityPacket.class,
        "PlaySoundFromEntity",
        packet ->
            "entityId: "
                + packet.getId()
                + " pitch: "
                + packet.getPitch()
                + " volume: "
                + packet.getVolume()
                + " seed: "
                + packet.getSeed()
                + " category: "
                + packet.getSource().getName()
                + " sound: "
                + packet.getSound().getRegisteredName());
    s2c(
        ClientboundSoundPacket.class,
        "PlaySound",
        packet ->
            "x: "
                + packet.getX()
                + " y: "
                + packet.getY()
                + " z: "
                + packet.getZ()
                + " pitch: "
                + packet.getPitch()
                + " volume: "
                + packet.getVolume()
                + " seed: "
                + packet.getSeed()
                + " category: "
                + packet.getSource().getName()
                + " sound: "
                + packet.getSound().getRegisteredName());
    s2c(
        ClientboundDisguisedChatPacket.class,
        "ProfilelessChatMessage",
        packet ->
            "message: "
                + packet.message()
                + " chatTypeName: "
                + packet.chatType().name()
                + " chatTypeTargetName: "
                + packet.chatType().targetName().orElse(Component.nullToEmpty("null")).getString()
                + " chatTypeType: "
                + packet.chatType().chatType().getRegisteredName());
    s2c(
        ClientboundRemoveMobEffectPacket.class,
        "RemoveEntityStatusEffect",
        packet ->
            " entityId: " + packet.entityId() + " effect: " + packet.effect().getRegisteredName());
    s2c(
        ClientboundDeleteChatPacket.class,
        "RemoveMessage",
        packet ->
            "messageSignatureId: "
                + packet.messageSignature().id()
                + " messageSignatureFullSignature: "
                + byteArrToString(packet.messageSignature().fullSignature().bytes()));
    s2c(
        ClientboundSetDisplayObjectivePacket.class,
        "ScoreboardDisplay",
        packet ->
            "name: "
                + packet.getObjectiveName()
                + " slot: "
                + packet.getSlot().getSerializedName()
                + " slotId:"
                + packet.getSlot().id());
    s2c(
        ClientboundSetObjectivePacket.class,
        "ScoreboardObjectiveUpdate",
        packet ->
            "name: "
                + packet.getObjectiveName()
                + " displayName: "
                + packet.getDisplayName()
                + " type: "
                + packet.getRenderType().getId()
                + " mode: "
                + packet.getMethod());
    s2c(
        ClientboundResetScorePacket.class,
        "ScoreboardScoreReset",
        packet ->
            "objectiveName: " + packet.objectiveName() + " scoreHolderName: " + packet.owner());
    s2c(
        ClientboundSetScorePacket.class,
        "ScoreboardScoreUpdate",
        packet ->
            "objectiveName: "
                + packet.objectiveName()
                + " scoreHolderName: "
                + packet.owner()
                + " score: "
                + packet.score()
                + " display: "
                + packet.display().orElse(Component.nullToEmpty("null")).getString());
    s2c(
        ClientboundContainerSetDataPacket.class,
        "ScreenHandlerPropertyUpdate",
        packet ->
            "syncId: "
                + packet.getContainerId()
                + " propertyName: "
                + packet.getId()
                + " value: "
                + packet.getValue());
    s2c(
        ClientboundContainerSetSlotPacket.class,
        "ScreenHandlerSlotUpdate",
        packet ->
            "syncId: "
                + packet.getContainerId()
                + " slot: "
                + packet.getSlot()
                + " revision: "
                + packet.getStateId()
                + " item: "
                + packet.getItem().getItem().getName(packet.getItem())
                + " itemCount: "
                + packet.getItem().getCount());
    s2c(
        ClientboundSelectAdvancementsTabPacket.class,
        "SelectAdvancementTab",
        packet -> "tabId: " + packet.getTab());
    s2c(
        ClientboundServerDataPacket.class,
        "ServerMetadata",
        packet ->
            "description: "
                + packet.motd().getString()
                + " favicon: "
                + (packet.iconBytes().isPresent()
                    ? byteArrToString(packet.iconBytes().get())
                    : "null"));
    s2c(ClientboundSetCameraPacket.class, "SetCameraEntity");
    s2c(
        ClientboundMerchantOffersPacket.class,
        "SetTradeOffers",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "syncId: "
                      + packet.getContainerId()
                      + " experience: "
                      + packet.getVillagerXp()
                      + " levelProgress: "
                      + packet.getVillagerLevel()
                      + " isLeveled: "
                      + packet.showProgress()
                      + " isRefreshable: "
                      + packet.canRestock()
                      + " offers: {");
          packet
              .getOffers()
              .forEach(
                  offer -> {
                    builder.append(
                        "\n  uses: "
                            + offer.getUses()
                            + " maxUses: "
                            + offer.getMaxUses()
                            + " demandBonus: "
                            + offer.getDemand()
                            + " hasBeenUsed: "
                            + offer.needsRestock()
                            + " disabled: "
                            + offer.isOutOfStock()
                            + " specialPrice: "
                            + offer.getSpecialPriceDiff()
                            + " merchantExperience: "
                            + offer.getXp()
                            + " shouldRewardPlayerExperience: "
                            + offer.shouldRewardExp()
                            + " firstBuyItem: "
                            + offer.getItemCostA().item().getRegisteredName()
                            + " secondBuyItem: ");
                    Optional<ItemCost> v = offer.getItemCostB();
                    builder
                        .append(v.isPresent() ? v.get().item().getRegisteredName() : "null")
                        .append(" sellItem: ")
                        .append(offer.getResult().getHoverName().getString())
                        .append(" sellItemCount: ")
                        .append(offer.getResult().getCount());
                    builder
                        .append(" displayedFirstBuyItem: ")
                        .append(offer.getCostA().getItem().getName(offer.getCostA()).getString())
                        .append(" displayedFirstBuyItemCount: ")
                        .append(offer.getCostA().getCount())
                        .append(" displayedSecondBuyItem: ")
                        .append(offer.getCostB().getItem().getName(offer.getCostB()).getString())
                        .append(" displayedSecondBuyItemCount: ")
                        .append(offer.getCostB().getCount());
                    builder
                        .append(
                            " originalFirstBuyItem: "
                                + offer
                                    .getBaseCostA()
                                    .getItem()
                                    .getName(offer.getBaseCostA())
                                    .getString())
                        .append(" originalFirstBuyItemCount: " + offer.getBaseCostA().getCount());
                  });
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundOpenSignEditorPacket.class,
        "SignEditorOpen",
        packet -> "pos: " + packet.getPos().toShortString() + " isFront: " + packet.isFrontText());
    s2c(
        ClientboundSetSimulationDistancePacket.class,
        "SimulationDistance",
        packet -> "simulationDistance: " + packet.simulationDistance());
    s2c(ClientboundChunkBatchStartPacket.class, "StartChunkSend");
    s2c(
        ClientboundAwardStatsPacket.class,
        "Statistics",
        packet -> {
          StringBuilder builder = new StringBuilder("stats: {");
          packet
              .stats()
              .forEach(
                  (stat, i) -> {
                    builder
                        .append("\n  index: ")
                        .append(i)
                        .append(" statType: ")
                        .append(stat.getType().getDisplayName().getString())
                        .append(" value: ")
                        .append(stat.getValue().toString());
                  });
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundStopSoundPacket.class,
        "StopSound",
        packet -> "category: " + packet.getSource() + " soundId: " + packet.getName().toString());
    s2c(
        ClientboundSetSubtitleTextPacket.class,
        "Subtitle",
        packet -> "text: " + packet.text().getString());

    // FML //
    /*s2c(SynchronizeRecipesS2CPacket.class, "SynchronizeRecipes", packet -> {
        StringBuilder builder = new StringBuilder("recipes: {");
        packet.getRecipes().forEach(recipe -> builder.append("\n  id: ").append(recipe.id().toString()).append(" value: ").append(recipe.value().toString()));
        builder.append("\n}");
        return builder.toString();
    });*/

    s2c(
        ClientboundSetPlayerTeamPacket.class,
        "Team",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "teamName: "
                      + packet.getName()
                      + " teamOperation: "
                      + (packet.getTeamAction() == null ? "null" : packet.getTeamAction().name())
                      + " playerListOperation: "
                      + (packet.getPlayerAction() == null
                          ? "null"
                          : packet.getPlayerAction().name()));

          String displayName;
          String collisionRule;
          String color;
          String friendlyFlagsBitwise;
          String nameTagVisibilityRule;
          String prefix;
          String suffix;
          if (packet.getParameters().isPresent()) {
            ClientboundSetPlayerTeamPacket.Parameters v = packet.getParameters().get();
            displayName = v.displayName().getString();
            collisionRule = v.collisionRule().toString();
            color = v.color().map(TeamColor::getSerializedName).orElse("null");
            friendlyFlagsBitwise = String.valueOf(v.options());
            nameTagVisibilityRule = v.nameTagVisibility().toString();
            prefix = v.playerPrefix().getString();
            suffix = v.playerSuffix().getString();
          } else {
            displayName = "null";
            collisionRule = "null";
            color = "null";
            friendlyFlagsBitwise = "null";
            nameTagVisibilityRule = "null";
            prefix = "null";
            suffix = "null";
          }
          builder
              .append(" teamDisplayName: ")
              .append(displayName)
              .append(" teamCollisionRule: ")
              .append(collisionRule)
              .append(" teamColor: ")
              .append(color)
              .append(" teamFriendlyFlagsBitwise: ")
              .append(friendlyFlagsBitwise)
              .append(" teamNameTagVisibilityRule: ")
              .append(nameTagVisibilityRule)
              .append(" teamPrefix: ")
              .append(prefix)
              .append(" teamSuffix: ")
              .append(suffix);
          builder.append(" playerNames: {");
          packet.getPlayers().forEach(name -> builder.append("\n  ").append(name));
          builder.append("\n}");
          return builder.toString();
        });
    s2c(
        ClientboundTickingStepPacket.class,
        "TickStep",
        packet -> "tickSteps: " + packet.tickSteps());
    s2c(
        ClientboundSetTitlesAnimationPacket.class,
        "TitleFade",
        packet ->
            "fadeInTicks: "
                + packet.getFadeIn()
                + " stayTicks: "
                + packet.getStay()
                + " fadeOutTicks: "
                + packet.getFadeOut());
    s2c(
        ClientboundSetTitleTextPacket.class,
        "Title",
        packet -> "text: " + packet.text().getString());
    s2c(
        ClientboundForgetLevelChunkPacket.class,
        "UnloadChunk",
        packet -> "chunkX: " + packet.pos().x() + " chunkZ: " + packet.pos().z());
    s2c(
        ClientboundSetHeldSlotPacket.class,
        "UpdateSelectedSlot",
        packet -> "slot: " + packet.slot());
    s2c(
        ClientboundTickingStatePacket.class,
        "UpdateTickRate",
        packet -> "isFrozen: " + packet.isFrozen() + " tickRate: " + packet.tickRate());
    s2c(
        ClientboundMoveVehiclePacket.class,
        "VehicleMove",
        packet ->
            "x: "
                + packet.position().x
                + " y: "
                + packet.position().y
                + " z: "
                + packet.position().z
                + " yaw: "
                + packet.yRot()
                + " pitch: "
                + packet.xRot());
    s2c(
        ClientboundSetBorderCenterPacket.class,
        "WorldBorderCenterChanged",
        packet -> "centerX: " + packet.getNewCenterX() + " centerZ: " + packet.getNewCenterZ());
    s2c(
        ClientboundInitializeBorderPacket.class,
        "WorldBorderInitialize",
        packet ->
            "centerX: "
                + packet.getNewCenterX()
                + " centerZ: "
                + packet.getNewCenterZ()
                + " maxRadius: "
                + packet.getNewAbsoluteMaxSize()
                + " size: "
                + packet.getOldSize()
                + " sizeLerpTarget: "
                + packet.getNewSize()
                + " sizeLerpTime: "
                + packet.getLerpTime()
                + " warningBlocks: "
                + packet.getWarningBlocks()
                + " warningTime: "
                + packet.getWarningTime());
    s2c(
        ClientboundSetBorderLerpSizePacket.class,
        "WorldBorderInterpolateSize",
        packet ->
            "size: "
                + packet.getOldSize()
                + " sizeLerpTarget: "
                + packet.getNewSize()
                + " sizeLerpTime: "
                + packet.getLerpTime());
    s2c(
        ClientboundSetBorderSizePacket.class,
        "WorldBorderSizeChanged",
        packet -> "sizeLerpTarget: " + packet.getSize());
    s2c(
        ClientboundSetBorderWarningDistancePacket.class,
        "WorldBorderWarningBlocksChanged",
        packet -> "warningBlocks: " + packet.getWarningBlocks());
    s2c(
        ClientboundSetBorderWarningDelayPacket.class,
        "WorldBorderWarningTimeChanged",
        packet -> "warningTime: " + packet.getWarningDelay());
    s2c(
        ClientboundLevelEventPacket.class,
        "WorldEvent",
        packet ->
            "pos: "
                + packet.getPos().toShortString()
                + " data: "
                + packet.getData()
                + " eventId: "
                + packet.getType()
                + " isGlobal: "
                + packet.isGlobalEvent());
    s2c(ClientboundSetTimePacket.class, "WorldTimeUpdate", packet -> "time: " + packet.gameTime());

    // query
    s2c(ClientboundPongResponsePacket.class, "PingResult", packet -> "startTime: " + packet.time());
    s2c(
        ClientboundStatusResponsePacket.class,
        "QueryResponse",
        packet -> {
          StringBuilder builder =
              new StringBuilder(
                  "description: "
                      + packet.status().description().getString()
                      + " favicon: "
                      + (packet.status().favicon().isPresent()
                          ? byteArrToString(packet.status().favicon().get().iconBytes())
                          : "null")
                      + " secureChatEnforced: "
                      + packet.status().enforcesSecureChat());

          String gameVersion;
          String protocolVersion;

          Optional<ServerStatus.Version> v = packet.status().version();
          if (v.isPresent()) {
            ServerStatus.Version version = v.get();
            gameVersion = version.name();
            protocolVersion = String.valueOf(version.protocol());
          } else {
            gameVersion = "null";
            protocolVersion = "null";
          }

          builder
              .append(" gameVersion: ")
              .append(gameVersion)
              .append(" protocolVersion: ")
              .append(protocolVersion);

          Optional<ServerStatus.Players> v2 = packet.status().players();
          if (v2.isPresent()) {
            ServerStatus.Players players = v2.get();

            builder
                .append(" maxPlayers: ")
                .append(players.max())
                .append(" online: ")
                .append(players.online())
                .append(" players: {");
            players
                .sample()
                .forEach(
                    profile ->
                        builder
                            .append("\n  profileName: ")
                            .append(profile.name())
                            .append(" profileId: ")
                            .append(
                                profile
                                    .id()
                                    .toString()) /*.append(" propertiesSize: ").append(profile.getProperties().size())*/);
          } else {
            builder.append(" maxPlayers: null online: null players: {");
          }
          builder.append("\n}");
          return builder.toString();
        });
  }

  @SuppressWarnings("unchecked")
  public static <T extends Packet<?>> PacketData<T> getS2C(T packet) {
    return (PacketData<T>) s2c.get(packet.getClass());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Packet<?>> PacketData<T> getC2S(T packet) {
    return (PacketData<T>) c2s.get(packet.getClass());
  }

  public static <T extends Packet<?>> PacketData<T> getData(T packet) {
    return isClient(packet) ? getC2S(packet) : getS2C(packet);
  }

  private static String byteArrToString(byte[] arr) {
    int length = arr.length;
    if (length > 30) return length + " bytes";

    StringBuilder builder = new StringBuilder();
    builder.append(arr[0]);
    for (int i = 1; i < arr.length; i++) builder.append(",").append(arr[i]);
    return builder.toString();
  }

  public static class PacketData<T> {
    private final String name;
    private final Function<T, String> function;

    public PacketData(String name, Function<T, String> function) {
      this.name = name;
      this.function = function;
    }

    public PacketData(String name) {
      this.name = name;
      this.function = p -> "";
    }

    public String getName() {
      return name;
    }

    @SuppressWarnings("unchecked")
    public String funnyApply(Object packet) {
      return apply((T) packet);
    }

    public String apply(T packet) {
      try {
        return function.apply(packet);
      } catch (Exception e) {
        System.out.println(
            "crashing packet: " + name + " - " + packet.getClass().getSimpleName() + ".class");
        throw new RuntimeException(e);
      }
    }
  }

  private static <T> void s2c(Class<T> clazz, String str, Function<T, String> function) {
    s2c.put(clazz, new PacketData<>(str, function));
  }

  private static <T> void s2c(Class<T> clazz, String str) {
    s2c.put(clazz, new PacketData<>(str));
  }

  private static <T> void c2s(Class<T> clazz, String str, Function<T, String> function) {
    c2s.put(clazz, new PacketData<>(str, function));
  }

  private static <T> void c2s(Class<T> clazz, String str) {
    c2s.put(clazz, new PacketData<>(str));
  }

  public static String nameOf(Packet<?> packet) {
    return nameOf(packet.getClass());
  }

  public static String nameOf(Class<?> clazz) {
    if (c2s.containsKey(clazz)) return c2s.get(clazz).name;
    if (s2c.containsKey(clazz)) return s2c.get(clazz).name;
    Logger.getGlobal()
        .log(Level.WARNING, "packet name for " + clazz.getSimpleName() + " couldn't be found");
    return clazz.getSimpleName();
  }

  public static boolean isClient(Packet<?> packet) {
    return isClient(packet.getClass());
  }

  public static boolean isClient(Class<?> clazz) {
    return c2s.containsKey(clazz);
  }
}
