package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetSemiDiameter extends PacketInt {
  public PacketSetSemiDiameter() {
  }

  public PacketSetSemiDiameter(int semiDiameter) {
    super(semiDiameter);
  }

  public static class Handler implements IMessageHandler<PacketSetSemiDiameter, IMessage> {
    @Override
    public IMessage onMessage(final PacketSetSemiDiameter message, final MessageContext ctx) {
      IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
      mainThread.addScheduledTask(new Runnable() {
        @Override
        public void run() {
          EntityPlayer player = ctx.getServerHandler().player;
          BitToolSettingsHelper.setSemiDiameter(player, player.getHeldItemMainhand(), message.value,
              null);
        }
      });
      return null;
    }

  }

}