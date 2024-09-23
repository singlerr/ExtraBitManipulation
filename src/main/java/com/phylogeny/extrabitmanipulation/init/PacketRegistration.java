package com.phylogeny.extrabitmanipulation.init;

import com.phylogeny.extrabitmanipulation.packet.PacketAddBitMapping;
import com.phylogeny.extrabitmanipulation.packet.PacketBitMappingsPerTool;
import com.phylogeny.extrabitmanipulation.packet.PacketBitParticles;
import com.phylogeny.extrabitmanipulation.packet.PacketChangeArmorItemList;
import com.phylogeny.extrabitmanipulation.packet.PacketChangeGlOperationList;
import com.phylogeny.extrabitmanipulation.packet.PacketClearStackBitMappings;
import com.phylogeny.extrabitmanipulation.packet.PacketCollectArmorBlocks;
import com.phylogeny.extrabitmanipulation.packet.PacketCreateBodyPartTemplate;
import com.phylogeny.extrabitmanipulation.packet.PacketCreateModel;
import com.phylogeny.extrabitmanipulation.packet.PacketCursorStack;
import com.phylogeny.extrabitmanipulation.packet.PacketCycleBitWrenchMode;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenBitMappingGui;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenChiseledArmorGui;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenInventoryGui;
import com.phylogeny.extrabitmanipulation.packet.PacketOverwriteStackBitMappings;
import com.phylogeny.extrabitmanipulation.packet.PacketPlaceEntityBit;
import com.phylogeny.extrabitmanipulation.packet.PacketReadBlockStates;
import com.phylogeny.extrabitmanipulation.packet.PacketSculpt;
import com.phylogeny.extrabitmanipulation.packet.PacketSetArmorMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetArmorMovingPart;
import com.phylogeny.extrabitmanipulation.packet.PacketSetArmorScale;
import com.phylogeny.extrabitmanipulation.packet.PacketSetBitStack;
import com.phylogeny.extrabitmanipulation.packet.PacketSetCollectionBox;
import com.phylogeny.extrabitmanipulation.packet.PacketSetDesign;
import com.phylogeny.extrabitmanipulation.packet.PacketSetDirection;
import com.phylogeny.extrabitmanipulation.packet.PacketSetEndsOpen;
import com.phylogeny.extrabitmanipulation.packet.PacketSetHollowShape;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelAreaMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelGuiOpen;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelPartConcealed;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelSnapMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetSculptMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetSemiDiameter;
import com.phylogeny.extrabitmanipulation.packet.PacketSetShapeType;
import com.phylogeny.extrabitmanipulation.packet.PacketSetTabAndStateBlockButton;
import com.phylogeny.extrabitmanipulation.packet.PacketSetTargetArmorBits;
import com.phylogeny.extrabitmanipulation.packet.PacketSetTargetBitGridVertexes;
import com.phylogeny.extrabitmanipulation.packet.PacketSetWallThickness;
import com.phylogeny.extrabitmanipulation.packet.PacketSetWrechMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSyncArmorSlot;
import com.phylogeny.extrabitmanipulation.packet.PacketThrowBit;
import com.phylogeny.extrabitmanipulation.packet.PacketUseWrench;
import java.lang.reflect.InvocationTargetException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PacketRegistration {
  public static int packetId = 0;


  public static void registerPackets() {
    registerServerPacket(PacketCycleBitWrenchMode.PACKET_TYPE,
        PacketCycleBitWrenchMode.Handler.class);
    registerServerPacket(PacketSculpt.PACKET_TYPE, PacketSculpt.Handler.class);
    registerServerPacket(PacketSetDirection.PACKET_TYPE, PacketSetDirection.Handler.class);
    registerServerPacket(PacketSetShapeType.PACKET_TYPE, PacketSetShapeType.Handler.class);
    registerServerPacket(PacketSetTargetBitGridVertexes.PACKET_TYPE,
        PacketSetTargetBitGridVertexes.Handler.class);
    registerServerPacket(PacketSetSemiDiameter.PACKET_TYPE, PacketSetSemiDiameter.Handler.class);
    registerServerPacket(PacketSetHollowShape.PACKET_TYPE, PacketSetHollowShape.Handler.class);
    registerServerPacket(PacketSetEndsOpen.PACKET_TYPE, PacketSetEndsOpen.Handler.class);
    registerServerPacket(PacketSetWallThickness.PACKET_TYPE, PacketSetWallThickness.Handler.class);
    registerServerPacket(PacketSetBitStack.PACKET_TYPE, PacketSetBitStack.Handler.class);
    registerServerPacket(PacketSetSculptMode.PACKET_TYPE, PacketSetSculptMode.Handler.class);
    registerServerPacket(PacketSetModelAreaMode.PACKET_TYPE, PacketSetModelAreaMode.Handler.class);
    registerServerPacket(PacketSetModelSnapMode.PACKET_TYPE, PacketSetModelSnapMode.Handler.class);
    registerServerPacket(PacketSetModelGuiOpen.PACKET_TYPE, PacketSetModelGuiOpen.Handler.class);
    registerServerPacket(PacketAddBitMapping.PACKET_TYPE, PacketAddBitMapping.Handler.class);
    registerServerPacket(PacketCursorStack.PACKET_TYPE, PacketCursorStack.Handler.class);
    registerServerPacket(PacketSetTabAndStateBlockButton.PACKET_TYPE,
        PacketSetTabAndStateBlockButton.Handler.class);
    registerServerPacket(PacketReadBlockStates.PACKET_TYPE, PacketReadBlockStates.Handler.class);
    registerServerPacket(PacketCreateModel.PACKET_TYPE, PacketCreateModel.Handler.class);
    registerServerPacket(PacketUseWrench.PACKET_TYPE, PacketUseWrench.Handler.class);
    registerServerPacket(PacketBitMappingsPerTool.PACKET_TYPE,
        PacketBitMappingsPerTool.Handler.class);
    registerServerPacket(PacketClearStackBitMappings.PACKET_TYPE,
        PacketClearStackBitMappings.Handler.class);
    registerServerPacket(PacketOverwriteStackBitMappings.PACKET_TYPE,
        PacketOverwriteStackBitMappings.Handler.class);
    registerServerPacket(PacketOpenBitMappingGui.PACKET_TYPE,
        PacketOpenBitMappingGui.Handler.class);
    registerServerPacket(PacketSetWrechMode.PACKET_TYPE, PacketSetWrechMode.Handler.class);
    registerServerPacket(PacketSetDesign.PACKET_TYPE, PacketSetDesign.Handler.class);
    registerServerPacket(PacketThrowBit.PACKET_TYPE, PacketThrowBit.Handler.class);
    registerClientPacket(PacketBitParticles.PACKET_TYPE, PacketBitParticles.Handler.class);
    registerClientPacket(PacketPlaceEntityBit.PACKET_TYPE, PacketPlaceEntityBit.Handler.class);
    registerServerPacket(PacketSetArmorMode.PACKET_TYPE, PacketSetArmorMode.Handler.class);
    registerServerPacket(PacketSetArmorScale.PACKET_TYPE, PacketSetArmorScale.Handler.class);
    registerServerPacket(PacketSetTargetArmorBits.PACKET_TYPE,
        PacketSetTargetArmorBits.Handler.class);
    registerServerPacket(PacketSetArmorMovingPart.PACKET_TYPE,
        PacketSetArmorMovingPart.Handler.class);
    registerServerPacket(PacketSetCollectionBox.PACKET_TYPE, PacketSetCollectionBox.Handler.class);
    registerServerPacket(PacketCreateBodyPartTemplate.PACKET_TYPE,
        PacketCreateBodyPartTemplate.Handler.class);
    registerServerPacket(PacketCollectArmorBlocks.PACKET_TYPE,
        PacketCollectArmorBlocks.Handler.class);
    registerServerPacket(PacketOpenChiseledArmorGui.PACKET_TYPE,
        PacketOpenChiseledArmorGui.Handler.class);
    registerClientPacket(PacketChangeGlOperationList.PACKET_TYPE,
        PacketChangeGlOperationList.ClientHandler.class);
    registerServerPacket(PacketChangeGlOperationList.PACKET_TYPE,
        PacketChangeGlOperationList.ServerHandler.class);
    registerClientPacket(PacketChangeArmorItemList.PACKET_TYPE,
        PacketChangeArmorItemList.ClientHandler.class);
    registerServerPacket(PacketChangeArmorItemList.PACKET_TYPE,
        PacketChangeArmorItemList.ServerHandler.class);
    registerClientPacket(PacketSyncArmorSlot.PACKET_TYPE, PacketSyncArmorSlot.Handler.class);
    registerServerPacket(PacketOpenInventoryGui.PACKET_TYPE, PacketOpenInventoryGui.Handler.class);
    registerServerPacket(PacketSetModelPartConcealed.PACKET_TYPE,
        PacketSetModelPartConcealed.Handler.class);
  }

  private static <T extends FabricPacket, H extends ServerPlayNetworking.PlayPacketHandler<T>> void registerServerPacket(
      PacketType<T> packetType, Class<H> handler) {
    try {
      ServerPlayNetworking.registerGlobalReceiver(packetType,
          handler.getConstructor().newInstance());
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static <T extends FabricPacket, H extends ClientPlayNetworking.PlayPacketHandler<T>> void registerClientPacket(
      PacketType<T> packetType, Class<H> handler) {
    try {
      ClientPlayNetworking.registerGlobalReceiver(packetType,
          handler.getConstructor().newInstance());
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }


}