package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;

public class Banking
  implements IVoicedCommandHandler
{
  private static String[] VOICED_COMMANDS = { "bank", "withdraw", "deposit" };
  
  @Override
public boolean useVoicedCommand(String command, Player activeChar, String target)
  {
    if (command.equalsIgnoreCase("bank"))
    {
      activeChar.sendMessage(".deposit (" + Config.BANKING_SYSTEM_ADENA + " Adena = " + Config.BANKING_SYSTEM_GOLDBARS + " Goldbar) / .withdraw (" + Config.BANKING_SYSTEM_GOLDBARS + " Goldbar = " + Config.BANKING_SYSTEM_ADENA + " Adena)");
    }
    else if (command.equalsIgnoreCase("deposit"))
    {
      if (activeChar.getInventory().getInventoryItemCount(57, 0) >= Config.BANKING_SYSTEM_ADENA)
      {
        activeChar.getInventory().reduceAdena("Goldbar", Config.BANKING_SYSTEM_ADENA, activeChar, null);
        activeChar.getInventory().addItem("Goldbar", 3470, Config.BANKING_SYSTEM_GOLDBARS, activeChar, null);
        activeChar.getInventory().updateDatabase();
        activeChar.sendPacket(new ItemList(activeChar, true));
        activeChar.sendMessage("Now you have " + Config.BANKING_SYSTEM_GOLDBARS + " Goldbar(s), and " + Config.BANKING_SYSTEM_ADENA + " less adena.");
      }
      else
      {
        activeChar.sendMessage("You do not have enough Adena to convert to Goldbar(s), you need " + Config.BANKING_SYSTEM_ADENA + " Adena.");
      }
    }
    else if (command.equalsIgnoreCase("withdraw"))
    {
      long a = activeChar.getInventory().getInventoryItemCount(57, 0);
      long b = Config.BANKING_SYSTEM_ADENA;
      if (a + b > 2147483647L)
      {
        activeChar.sendMessage("You do not have enough space for all the adena in inventory!");
        return false;
      }
      if (activeChar.getInventory().getInventoryItemCount(3470, 0) >= Config.BANKING_SYSTEM_GOLDBARS)
      {
        activeChar.getInventory().destroyItemByItemId("Adena", 3470, Config.BANKING_SYSTEM_GOLDBARS, activeChar, null);
        activeChar.getInventory().addAdena("Adena", Config.BANKING_SYSTEM_ADENA, activeChar, null);
        activeChar.getInventory().updateDatabase();
        activeChar.sendPacket(new ItemList(activeChar, true));
        activeChar.sendMessage("Now you have " + Config.BANKING_SYSTEM_ADENA + " Adena, and " + Config.BANKING_SYSTEM_GOLDBARS + " less Goldbar(s).");
      }
      else
      {
        activeChar.sendMessage("You do not have any Goldbars to turn into " + Config.BANKING_SYSTEM_ADENA + " Adena.");
      }
    }
    return true;
  }
  
  @Override
public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}
