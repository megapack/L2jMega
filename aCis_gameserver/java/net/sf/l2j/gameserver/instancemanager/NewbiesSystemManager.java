package net.sf.l2j.gameserver.instancemanager;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.network.serverpackets.TutorialCloseHtml;
import net.sf.l2j.gameserver.network.serverpackets.TutorialShowHtml;

public class NewbiesSystemManager
{
  public static final NewbiesSystemManager getInstance()
  {
    return SingletonHolder._instance;
  }
  
  public static void Welcome(Player player)
  {

    player.sendPacket(new SpecialCamera(player.getObjectId(), Config.NEWBIE_DIST, Config.NEWBIE_LADO, Config.NEWBIE_ALTURA, 999999999, 999999999, 0, 0, 1, 0));
    player.setIsParalyzed(true);
    player.setIsInvul(true);
    player.getAppearance().setInvisible();
    player.broadcastPacket(new SocialAction(player, 9));
    player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/startupsystem.htm")));
	  
  }
  
  public static void doPreview(Player player)
  {
    player.setPreview(1);
    player.setIsParalyzed(true);
    player.setIsInvul(true);
    player.getAppearance().setInvisible();
  }
  
  public static void doEquip(Player player)
  {
    player.setEquip(1);
    player.setIsParalyzed(true);
    player.setIsInvul(true);
    player.getAppearance().setInvisible();
  }
  
  public static void doWepEquip(Player player)
  {
    player.setWepEquip(1);
    player.setIsParalyzed(true);
    player.setIsInvul(true);
    player.getAppearance().setInvisible();
  }
  
  public static void onEnterEquip(Player activeChar)
  {
    activeChar.sendPacket(new SpecialCamera(activeChar.getObjectId(), Config.NEWBIE_DIST, Config.NEWBIE_LADO, Config.NEWBIE_ALTURA, 999999999, 999999999, 0, 0, 1, 0));
    if (activeChar.isMageClass())
    {
      activeChar.setEquip(1);
      activeChar.setIsParalyzed(true);
      activeChar.setIsInvul(true);
      activeChar.getAppearance().setInvisible();
      activeChar.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else
    {
      activeChar.setEquip(1);
      activeChar.setIsParalyzed(true);
      activeChar.setIsInvul(true);
      activeChar.getAppearance().setInvisible();
      activeChar.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors-1.htm")));
    }
  }
  
  public static void onEnterWepEquip(Player activeChar)
  {
    activeChar.sendPacket(new SpecialCamera(activeChar.getObjectId(), Config.NEWBIE_DIST, Config.NEWBIE_LADO, Config.NEWBIE_ALTURA, 999999999, 999999999, 0, 0, 1, 0));
    if (activeChar.isMageClass())
    {
      activeChar.setWepEquip(1);
      activeChar.setIsParalyzed(true);
      activeChar.setIsInvul(true);
      activeChar.getAppearance().setInvisible();
      activeChar.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/weapons/mageaweapons.htm")));
    }
    else
    {
      activeChar.setWepEquip(1);
      activeChar.setIsParalyzed(true);
      activeChar.setIsInvul(true);
      activeChar.getAppearance().setInvisible();
      activeChar.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/weapons/fighterweapons-1.htm")));
    }
  }
  
  public static void start(Player player)
  {
    switch (player.getClassId().getId())
    {
    case 0: 
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/classes/humanclasses.htm")));
      break;
    case 10: 
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/classes/humanmageclasses.htm")));
      break;
    case 18: 
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/classes/elfclasses.htm")));
      break;
    case 25: 
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/classes/elfmageclasses.htm")));
      break;
    case 31: 
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/classes/darkelfclasses.htm")));
      break;
    case 38: 
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/classes/darkelfmageclasses.htm")));
      break;
    case 44: 
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/classes/orcclasses.htm")));
      break;
    case 49: 
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/classes/orcmageclasses.htm")));
      break;
    case 53: 
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/classes/dwarfclasses.htm")));
      break;
    default: 
      HtmlTeleport(player);
    }
  }
  
  public void Classes(String command, Player player)
  {
    if ((command.startsWith("necromancer")))
    {
      if (player.getClassId().getId() != 10)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(13);
      player.setBaseClass(13);
      
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("sorceror")))
    {
      if (player.getClassId().getId() != 10)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(12);
      player.setBaseClass(12);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("warlock")))
    {
      if (player.getClassId().getId() != 10)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(14);
      player.setBaseClass(14);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("cleric")))
    {
      if (player.getClassId().getId() != 10)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(15);
      player.setBaseClass(15);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("bishop")))
    {
      if (player.getClassId().getId() != 10)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(16);
      player.setBaseClass(16);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("prophet")))
    {
      if (player.getClassId().getId() != 10)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(17);
      player.setBaseClass(17);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("gladiator")))
    {
      if (player.getClassId().getId() != 0)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(2);
      player.setBaseClass(2);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/lightarmors.htm")));
    }
    else if ((command.startsWith("warlord")))
    {
      if (player.getClassId().getId() != 0)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(3);
      player.setBaseClass(3);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else if ((command.startsWith("paladin")))
    {
      if (player.getClassId().getId() != 0)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(5);
      player.setBaseClass(5);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else if ((command.startsWith("darkavenger")))
    {
      if (player.getClassId().getId() != 0)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(6);
      player.setBaseClass(6);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else if ((command.startsWith("treasurehunter")))
    {
      if (player.getClassId().getId() != 0)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(8);
      player.setBaseClass(8);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/lightarmors.htm")));
    }
    else if ((command.startsWith("hawkeye")))
    {
      if (player.getClassId().getId() != 0)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(9);
      player.setBaseClass(9);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/lightarmors.htm")));
    }
    else if ((command.startsWith("temple")))
    {
      if (player.getClassId().getId() != 18)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(20);
      player.setBaseClass(20);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else if ((command.startsWith("swordsinger")))
    {
      if (player.getClassId().getId() != 18)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(21);
      player.setBaseClass(21);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else if ((command.startsWith("plain")))
    {
      if (player.getClassId().getId() != 18)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(23);
      player.setBaseClass(23);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/lightarmors.htm")));
    }
    else if ((command.startsWith("silver")))
    {
      if (player.getClassId().getId() != 18)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(24);
      player.setBaseClass(24);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/lightarmors.htm")));
    }
    else if ((command.startsWith("spellsinger")))
    {
      if (player.getClassId().getId() != 25)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(27);
      player.setBaseClass(27);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("elemental")))
    {
      if (player.getClassId().getId() != 25)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(28);
      player.setBaseClass(28);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("elder")))
    {
      if (player.getClassId().getId() != 25)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(30);
      player.setBaseClass(30);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("tanker_dark")))
    {
      if (player.getClassId().getId() != 31)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(33);
      player.setBaseClass(33);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else if ((command.startsWith("bladedancer")))
    {
      if (player.getClassId().getId() != 31)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(34);
      player.setBaseClass(34);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/lightarmors.htm")));
    }
    else if ((command.startsWith("abyss")))
    {
      if (player.getClassId().getId() != 31)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(36);
      player.setBaseClass(36);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/lightarmors.htm")));
    }
    else if ((command.startsWith("archer_dark")))
    {
      if (player.getClassId().getId() != 31)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(37);
      player.setBaseClass(37);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/lightarmors.htm")));
    }
    else if ((command.startsWith("spellhowler")))
    {
      if (player.getClassId().getId() != 38)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(40);
      player.setBaseClass(40);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("phantoms")))
    {
      if (player.getClassId().getId() != 38)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(41);
      player.setBaseClass(41);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else if ((command.startsWith("shilliene")))
    {
      if (player.getClassId().getId() != 38)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(43);
      player.setBaseClass(43);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("destroyer")))
    {
      if (player.getClassId().getId() != 44)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(46);
      player.setBaseClass(46);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else if ((command.startsWith("tyrant")))
    {
      if (player.getClassId().getId() != 44)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(48);
      player.setBaseClass(48);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/lightarmors.htm")));
    }
    else if ((command.startsWith("overlord")))
    {
      if (player.getClassId().getId() != 49)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(51);
      player.setBaseClass(51);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("warcryer")))
    {
      if (player.getClassId().getId() != 49)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(52);
      player.setBaseClass(52);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/magearmors.htm")));
    }
    else if ((command.startsWith("bounty")))
    {
      if (player.getClassId().getId() != 53)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(55);
      player.setBaseClass(55);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else if ((command.startsWith("warsmith")))
    {
      if (player.getClassId().getId() != 53)
      {
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        return;
      }
      player.sendPacket(new PlaySound("ItemSound.quest_accept"));
      
      doEquip(player);
      player.setClassId(57);
      player.setBaseClass(57);
      player.refreshOverloaded();
      player.store();
      player.sendPacket(new HennaInfo(player));
      player.broadcastUserInfo();
      player.broadcastPacket(new SocialAction(player, 12));
      player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/armors/fighterarmors.htm")));
    }
    else
    {
    Iterator<Integer> localIterator;
      if (command.startsWith("mjlight"))
      {
        player.sendPacket(new PlaySound("ItemSound.quest_accept"));
        
        List<Integer> MJL = Arrays.asList(new Integer[] { Integer.valueOf(2395), Integer.valueOf(2419), Integer.valueOf(5775), Integer.valueOf(5787), Integer.valueOf(924), Integer.valueOf(862), Integer.valueOf(893), Integer.valueOf(871), Integer.valueOf(902) });
        ItemInstance items = null;
        for (localIterator = MJL.iterator(); localIterator.hasNext();)
        {
          int id = localIterator.next().intValue();
          
          player.getInventory().addItem("Armors", id, 1, player, null);
          items = player.getInventory().getItemByItemId(id);
          items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
          player.getInventory().equipItemAndRecord(items);
          player.setEquip(0);
          doWepEquip(player);
          player.broadcastUserInfo();
          new InventoryUpdate();
          player.sendPacket(new ItemList(player, false));
        }
        player.broadcastPacket(new SocialAction(player, 11));
        player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/weapons/lightweapons.htm")));
      }
      else if (command.startsWith("dc"))
      {
        player.sendPacket(new PlaySound("ItemSound.quest_accept"));
        
        List<Integer> MageArmorDC = Arrays.asList(new Integer[] { Integer.valueOf(2407), Integer.valueOf(512), Integer.valueOf(5767), Integer.valueOf(5779), Integer.valueOf(924), Integer.valueOf(862), Integer.valueOf(893), Integer.valueOf(871), Integer.valueOf(902) });
        ItemInstance items = null;
        for (localIterator = MageArmorDC.iterator(); localIterator.hasNext();)
        {
          int id = localIterator.next().intValue();
          
          player.getInventory().addItem("Armors", id, 1, player, null);
          items = player.getInventory().getItemByItemId(id);
          items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
          player.getInventory().equipItemAndRecord(items);
          player.setEquip(0);
          doWepEquip(player);
          player.broadcastUserInfo();
          new InventoryUpdate();
          player.sendPacket(new ItemList(player, false));
        }
        player.broadcastPacket(new SocialAction(player, 11));
        player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/weapons/mageaweapons.htm")));
      }
      else if (command.startsWith("tl"))
      {
        player.sendPacket(new PlaySound("ItemSound.quest_accept"));
        
        List<Integer> MageArmorTL = Arrays.asList(new Integer[] { Integer.valueOf(2400), Integer.valueOf(2405), Integer.valueOf(547), Integer.valueOf(5770), Integer.valueOf(5782), Integer.valueOf(924), Integer.valueOf(862), Integer.valueOf(893), Integer.valueOf(871), Integer.valueOf(902) });
        ItemInstance items = null;
        for (localIterator = MageArmorTL.iterator(); localIterator.hasNext();)
        {
          int id = localIterator.next().intValue();
          
          player.getInventory().addItem("Armors", id, 1, player, null);
          items = player.getInventory().getItemByItemId(id);
          items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
          player.getInventory().equipItemAndRecord(items);
          player.setEquip(0);
          doWepEquip(player);
          player.broadcastUserInfo();
          new InventoryUpdate();
          player.sendPacket(new ItemList(player, false));
        }
        player.broadcastPacket(new SocialAction(player, 11));
        player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/weapons/mageaweapons.htm")));
      }
      else if (command.startsWith("nmh"))
      {
        player.sendPacket(new PlaySound("ItemSound.quest_accept"));
        
        List<Integer> NMH = Arrays.asList(new Integer[] { Integer.valueOf(374), Integer.valueOf(2418), Integer.valueOf(5771), Integer.valueOf(5783), Integer.valueOf(924), Integer.valueOf(862), Integer.valueOf(893), Integer.valueOf(871), Integer.valueOf(902) });
        ItemInstance items = null;
        for (localIterator = NMH.iterator(); localIterator.hasNext();)
        {
          int id = localIterator.next().intValue();
          
          player.getInventory().addItem("Armors", id, 1, player, null);
          items = player.getInventory().getItemByItemId(id);
          items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
          player.getInventory().equipItemAndRecord(items);
          player.setEquip(0);
          doWepEquip(player);
          player.broadcastUserInfo();
          new InventoryUpdate();
          player.sendPacket(new ItemList(player, false));
        }
        player.broadcastPacket(new SocialAction(player, 11));
        player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/weapons/fighterweapons.htm")));
      }
      else
      {
        Object msg;
        if (command.startsWith("majheavy"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> NMH = Arrays.asList(new Integer[] { Integer.valueOf(2383), Integer.valueOf(2419), Integer.valueOf(5774), Integer.valueOf(5786), Integer.valueOf(924), Integer.valueOf(862), Integer.valueOf(893), Integer.valueOf(871), Integer.valueOf(902) });
          ItemInstance items = null;
          for (localIterator = NMH.iterator(); localIterator.hasNext();)
          {
            int id = localIterator.next().intValue();
            
            player.getInventory().addItem("Armors", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setEquip(0);
            doWepEquip(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 11));
          msg = HtmCache.getInstance().getHtm("data/html/mods/startup/weapons/fighterweapons.htm");
          player.sendPacket(new TutorialShowHtml((String)msg));
        }
        else if (command.startsWith("som"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> SOM = Arrays.asList(new Integer[] { Integer.valueOf(5643), Integer.valueOf(641) });
          ItemInstance items = null;

          for (msg = SOM.iterator(); ((Iterator<?>)msg).hasNext();)
          {
        	  int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("bran"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> bran = Arrays.asList(new Integer[] { Integer.valueOf(5607) });
          ItemInstance items = null;
          for (msg = bran.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();

            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("dread"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> dread = Arrays.asList(new Integer[] { Integer.valueOf(5633) });
          ItemInstance items = null;
          for (msg = dread.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("darkl"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> darkl = Arrays.asList(new Integer[] { Integer.valueOf(5648), Integer.valueOf(2498) });
          ItemInstance items = null;
          for (msg = darkl.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("dragon"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> dra = Arrays.asList(new Integer[] { Integer.valueOf(5644) });
          ItemInstance items = null;
          for (msg = dra.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("ely"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> ely = Arrays.asList(new Integer[] { Integer.valueOf(5602), Integer.valueOf(2498) });
          ItemInstance items = null;
          for (msg = ely.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("carnage"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> car = Arrays.asList(new Integer[] { Integer.valueOf(5609) });
          ItemInstance items = null;
          for (msg = car.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("soulbow"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> soul = Arrays.asList(new Integer[] { Integer.valueOf(5612) });
          ItemInstance items = null;
          for (msg = soul.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("bloody"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> bloody = Arrays.asList(new Integer[] { Integer.valueOf(5614) });
          ItemInstance items = null;
          for (msg = bloody.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("soulsepa"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> soulsepa = Arrays.asList(new Integer[] { Integer.valueOf(5618) });
          ItemInstance items = null;
          for (msg = soulsepa.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("damascus"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> damascus = Arrays.asList(new Integer[] { Integer.valueOf(5706) });
          ItemInstance items = null;
          for (msg = damascus.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if (command.startsWith("garra"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          
          List<Integer> dragong = Arrays.asList(new Integer[] { Integer.valueOf(5625) });
          ItemInstance items = null;
          for (msg = dragong.iterator(); ((Iterator<?>)msg).hasNext();)
          {
            int id = ((Integer)((Iterator<?>)msg).next()).intValue();
            
            player.getInventory().addItem("Weapon", id, 1, player, null);
            items = player.getInventory().getItemByItemId(id);
            items.setEnchantLevel(Config.NEWBIE_ITEMS_ENCHANT);
            player.getInventory().equipItemAndRecord(items);
            player.setWepEquip(0);
            doPreview(player);
            player.broadcastUserInfo();
            new InventoryUpdate();
            player.sendPacket(new ItemList(player, false));
          }
          player.broadcastPacket(new SocialAction(player, 3));
          
          HtmlTeleport(player);
        }
        else if ((command.startsWith("teleport")) || (command.startsWith("back_town")))
        {
          player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          player.sendPacket(new ExShowScreenMessage("", 0, ExShowScreenMessage.SMPOS.BOTTOM_CENTER, false));
          player.sendPacket(new SpecialCamera(player.getObjectId(), 30, 10, 530, 0, 0, 0, 0, 1, 0));
          
          player.teleToLocation(Config.TELE_TO_LOCATION[0] + Rnd.get(-80, 80), Config.TELE_TO_LOCATION[1] + Rnd.get(-80, 80), Config.TELE_TO_LOCATION[2], 0);
          player.setLastCords(Config.TELE_TO_LOCATION[0], Config.TELE_TO_LOCATION[1], Config.TELE_TO_LOCATION[2]);

          player.setPreview(0);
          player.setEquip(0);
          player.setWepEquip(0);
          
          player.setIsParalyzed(false);
          if (!player.isGM())
          {
            player.getAppearance().setVisible();
            player.setIsInvul(false);
          }
          player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
          player.setCurrentCp(player.getMaxCp());
        }
      else if (command.startsWith("welcome"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
        	start(player);                   
        }
        else if (command.startsWith("page1"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          WeaponsPage1(player);
        }
        else if (command.startsWith("page2"))
        {
          player.sendPacket(new PlaySound("ItemSound.quest_accept"));
          WeaponsPage2(player);
        }
      }
    }
  }
  
  public static void HtmlBuff(Player player)
  {
    player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/buffme.htm")));
  }
  
  public static void HtmlTeleport(Player player)
  {
    player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/teleport.htm")));
  }
  
  public static void WeaponsPage1(Player player)
  {
    player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/weapons/fighterweapons-1.htm")));
  }
  
  public static void WeaponsPage2(Player player)
  {
    player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/mods/startup/weapons/fighterweapons-2.htm")));
  }
  
  public static final void Link(Player player, String request)
  {
    getInstance().Classes(request, player);
  }
  
  private static class SingletonHolder
  {
    protected static final NewbiesSystemManager _instance = new NewbiesSystemManager();
  }
}
