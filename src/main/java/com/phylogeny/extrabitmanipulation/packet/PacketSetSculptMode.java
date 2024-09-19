package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetSculptMode extends PacketInt {
  public PacketSetSculptMode() {
  }

  public PacketSetSculptMode(int mode) {
    super(mode);
  }

  public static class Handler implements IMessageHandler<PacketSetSculptMode, IMessage> {
    @Override
    public IMessage onMessage(final PacketSetSculptMode message, final MessageContext ctx) {
      IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
      mainThread.addScheduledTask(new Runnable() {
        @Override
        public void run() {
          EntityPlayer player = ctx.getServerHandler().player;
          BitToolSettingsHelper.setSculptMode(player, player.getHeldItemMainhand(), message.value,
              null);
        }
      });
      return null;
    }

  }

}