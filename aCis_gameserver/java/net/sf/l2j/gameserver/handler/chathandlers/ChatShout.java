package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.FloodProtectors.Action;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatShout implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		1
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{
		if (!FloodProtectors.performAction(activeChar.getClient(), Action.GLOBAL_CHAT))
			return;
		
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		final int region = MapRegionData.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
		
	    String convert = text.toLowerCase();
	    CreatureSay disable = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), convert);

	    
	    if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("ON")) {
	      for (Player player : World.getInstance().getPlayers())
	      {
	        if (region == MapRegionData.getInstance().getMapRegion(player.getX(), player.getY())) {
	          if ((Config.DISABLE_CAPSLOCK) && (!activeChar.isGM())) {
	            player.sendPacket(disable);
	          } else {
	            player.sendPacket(cs);
	          }
	        }
	      }
	    } else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("GLOBAL")) {
	      if (Config.GLOBAL_CHAT_WITH_PVP)
	      {
	        if ((activeChar.getPvpKills() < Config.GLOBAL_PVP_AMOUNT) && (!activeChar.isGM()))
	        {
	          activeChar.sendMessage("You must have at least " + Config.GLOBAL_PVP_AMOUNT + " pvp kills in order to speak in global chat");
	          return;
	        }
	        for (Player player : World.getInstance().getPlayers())
	        {
	          if ((Config.DISABLE_CAPSLOCK) && (!activeChar.isGM())) {
	            player.sendPacket(disable);
	          } else {
	            player.sendPacket(cs);
	          }

	        }
	      }
			for (Player player : World.getInstance().getPlayers())
			{
				if ((Config.DISABLE_CAPSLOCK) && !BlockList.isBlocked(player, activeChar) && region == MapRegionData.getInstance().getMapRegion(player.getX(), player.getY()))
				{
					 player.sendPacket(disable);
                  } else {
					player.sendPacket(cs);
	             }
			}
	      
	      }

	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}