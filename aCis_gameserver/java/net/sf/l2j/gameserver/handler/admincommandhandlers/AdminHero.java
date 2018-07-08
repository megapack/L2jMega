package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.HeroManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.util.CloseUtil;

public class AdminHero
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_remove_hero", "admin_sethero", "admin_masshero" };
  private static final Logger _log = Logger.getLogger(AdminHero.class.getName());
  
  @Override
public boolean useAdminCommand(String command, Player activeChar)
  {

    WorldObject target = activeChar.getTarget();
    if ((target == null) || (!(target instanceof Player)))
    {
      activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      return false;
    }
    Player targetPlayer;
    if (command.startsWith("admin_sethero"))
    {
      if ((target instanceof Player))
      {
        targetPlayer = (Player)target;
        boolean newHero = !targetPlayer.isHeroEterno();
        if (newHero)
        {
          if (HeroManager.getInstance().hasHeroPrivileges(targetPlayer.getObjectId())) {
            removeHero(activeChar, targetPlayer);
          }

          targetPlayer.setHeroEterno(true);
          targetPlayer.setHero(true);
          targetPlayer.sendPacket(new CreatureSay(0, 17, "[Hero System]", "Voce se tornou um HERO ETERNO."));
          updateDatabase(targetPlayer, true);
          targetPlayer.broadcastUserInfo();
          if (activeChar.isSubClassActive()) {
            for (L2Skill s : SkillTable.getHeroSkills()) {
              activeChar.addSkill(s, false);
            }
          }
          targetPlayer.sendSkillList();
        }
        else
        {

          targetPlayer.setHero(false);
          targetPlayer.setHeroEterno(false);
          targetPlayer.sendMessage("[Hero System]: Seu HERO ETERNO foi removido.");
          updateDatabase(targetPlayer, false);
          for (L2Skill s : SkillTable.getHeroSkills()) {
            targetPlayer.removeSkill(s.getId(), false);
          }
          targetPlayer.sendSkillList();
          targetPlayer.broadcastUserInfo();
        }
        targetPlayer = null;
      }
      else
      {
        activeChar.sendMessage("[Hero System]: Impossible to set a non Player Target as hero.");
        _log.info("[Hero System]: GM: " + activeChar.getName() + " is trying to set a non Player Target as hero.");
        
        return false;
      }
    }
    else if (command.startsWith("admin_masshero"))
    {
      for (Player player : World.getInstance().getPlayers()) {
        if (player != null)
        {
          if ((!player.isPhantom()) || (!player.isHero()) || (!player.isInOlympiadMode()))
          {
            player.setHero(true);
            player.sendMessage("[Hero System]: Admin is rewarding all online players with Hero Status.");
            player.broadcastUserInfo();
          }
          player = null;
        }
      }
      Announcements.Announce("GM: Todos os Jogadores online receberam Hero ate que reloguem. Aproveitem..!");
    }
    else if (command.equalsIgnoreCase("admin_remove_hero"))
    {
      removeHero(activeChar, (Player)target);
    }
    return true;
  }
  
  public static void removeHero(Player activeChar, Player targetChar)
  {
    if (!HeroManager.getInstance().hasHeroPrivileges(targetChar.getObjectId()))
    {
      activeChar.sendMessage("Your target does not have hero privileges.");
      return;
    }
    HeroManager.getInstance().removeHero(targetChar.getObjectId());
    activeChar.sendMessage("You have removed hero privileges from " + targetChar.getName() + ".");
    targetChar.sendPacket(new ExShowScreenMessage("Your hero privileges were removed by the admin.", 10000));
  }
  
  public static void updateDatabase(Player player, boolean newHero)
  {
    if (player == null) {
      return;
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement stmt = con.prepareStatement(newHero ? INSERT_DATA : DEL_DATA);
      if (newHero)
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
      e.printStackTrace();
    }
    finally
    {
      CloseUtil.close(con);
    }
  }
  
  static String INSERT_DATA = "REPLACE INTO characters_hero_eterno (obj_Id, char_name, hero, hero_eterno) VALUES (?,?,?,?)";
  static String DEL_DATA = "UPDATE characters_hero_eterno SET hero = 0, hero_eterno = 0 WHERE obj_Id=?";
  
  @Override
public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}
