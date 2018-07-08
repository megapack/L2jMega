package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.VipManager;
import net.sf.l2j.gameserver.instancemanager.VipRewardManager;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.TutorialCloseHtml;
import net.sf.l2j.gameserver.network.serverpackets.TutorialShowHtml;
import net.sf.l2j.gameserver.vip.VIPReward;
import net.sf.l2j.gameserver.vip.VIPRewardItens;
import net.sf.l2j.util.CloseUtil;
import net.sf.l2j.util.StringUtil;

public class AdminVip
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_setvip", "admin_removevip" };
  private static final Logger _log = Logger.getLogger(AdminVip.class.getName());
  
  @Override
public boolean useAdminCommand(String command, Player activeChar)
  {

    WorldObject target = activeChar.getTarget();
    if ((target == null) || (!(target instanceof Player)))
    {
      activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      return false;
    }
    if (command.startsWith("admin_setvip"))
    {
      if ((target instanceof Player))
      {
    	  Player targetPlayer = (Player)target;
        boolean newHero = !targetPlayer.isVip();
        if (newHero)
        {
          removeVip(activeChar, targetPlayer);
          targetPlayer.setVip(true);
          targetPlayer.sendPacket(new CreatureSay(0, 17, "[VIP System]", "Voce se tornou um VIP ETERNO."));
          updateDatabase(targetPlayer, true);
          targetPlayer.broadcastUserInfo();

          targetPlayer.sendSkillList();
          ThreadPool.schedule(new VIPReward(targetPlayer), 10000L);
        }
        else
        {

          targetPlayer.setVip(false);
          targetPlayer.sendMessage("[Vip System]: Seu VIP ETERNO foi removido.");
          updateDatabase(targetPlayer, false);
          targetPlayer.sendSkillList();
          targetPlayer.broadcastUserInfo();
        }
        targetPlayer = null;
      }
      else
      {
        activeChar.sendMessage("[VIP System]: Impossible to set a non Player Target as Vip.");
        _log.info("[VIP System]: GM: " + activeChar.getName() + " is trying to set a non Player Target as VIP.");
        
        return false;
      }
    }
    else if (command.equalsIgnoreCase("admin_removevip")) {
      removeVip(activeChar, (Player)target);
    }
    return true;
  }
  
  public static void removeVip(Player activeChar, Player targetChar)
  {
    if (VipManager.getInstance().hasVipPrivileges(targetChar.getObjectId())) {
      VipManager.getInstance().removeVip(targetChar.getObjectId());

    } 

  }
  
  public static void updateDatabase(Player player, boolean newVip)
  {
    if (player == null) {
      return;
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement stmt = con.prepareStatement(newVip ? INSERT_DATA : DEL_DATA);
      if (newVip)
      {
        stmt.setInt(1, player.getObjectId());
        stmt.setString(2, player.getName());
        stmt.setInt(3, 1);
        stmt.setInt(4, 1);
        stmt.execute();
        stmt.close();
        stmt = null;
      }
      else
      {
        stmt.setInt(1, player.getObjectId());
        stmt.execute();
        stmt.close();
        stmt = null;
      }
    }
    catch (Exception e)
    {
      if (Config.DEBUG) {
        e.printStackTrace();
      }
      _log.log(Level.SEVERE, "[VIP System]: Error: no VIP eterno: ", e);
    }
    finally
    {
      CloseUtil.close(con);
    }
  }
  

  
  static String INSERT_DATA = "REPLACE INTO characters_vip_eterno (obj_Id, char_name, vip, vip_eterno) VALUES (?,?,?,?)";
  static String DEL_DATA = "UPDATE characters_vip_eterno SET vip = 0, vip_eterno = 0 WHERE obj_Id=?";
  
  public static final void LinkRewardVIP(Player player, String request)
  {
    if ((request == null) || (!request.startsWith("CO"))) {
      return;
    }
    if (VipRewardManager.getInstance().hasVipPrivileges(player.getObjectId())) {
      return;
    }
    try
    {
      int val = Integer.parseInt(request.substring(2));
      if (((val == 1) && (VipManager.getInstance().hasVipPrivileges(player.getObjectId()))) || (player.isVip()))
      {
        if (!player.isGM()) {
          for (Player p : World.getInstance().getPlayers()) {
            if ((p != null) && 
              (p.isOnline())) {
              p.sendChatMessage(0, 16, ".", "" + player.getName() + " recebeu sua recompensa VIP diaria. :.");
            }
          }
        }
        VipRewardManager.getInstance().addVip(player.getObjectId(), System.currentTimeMillis() + 86400000L);
        
        ThreadPool.schedule(new VIPRewardItens(player), 1000L);
      }
      else
      {
        player.sendMessage("SYS: Torne-se um membro VIP para receber essa premiacao.");
      }
    }
    catch (NumberFormatException localNumberFormatException) {}
    player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
  }

  
  public static final void VIPHtml(Player player)
  {
    String msg = HtmCache.getInstance().getHtm("data/html/mods/reward_vip.htm");
    
    StringBuilder menu = new StringBuilder(100);
    
    StringUtil.append(menu, new String[] { "<a action=\"link CO", "1", "\">", "REWARD", "</a><br>" });
    
    msg = msg.replaceAll("%menu%", menu.toString());
    player.sendPacket(new TutorialShowHtml(msg));
  }

  
  
  @Override
public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}
