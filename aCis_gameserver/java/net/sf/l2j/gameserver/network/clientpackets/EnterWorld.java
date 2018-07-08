package net.sf.l2j.gameserver.network.clientpackets;

import hwid.Hwid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map.Entry;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.CloseGame;
import net.sf.l2j.gameserver.Restart;
import net.sf.l2j.gameserver.communitybbs.Manager.MailBBSManager;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.CoupleManager;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.data.xml.AnnouncementData;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.events.ArenaTask;
import net.sf.l2j.gameserver.events.PartyFarm;
import net.sf.l2j.gameserver.events.teamvsteam.TvTEvent;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Donate;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.IPManager;
import net.sf.l2j.gameserver.instancemanager.NewbiesSystemManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.CabalType;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.SealType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.base.ClassRace;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.Siege.SiegeSide;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.SubPledge;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.Die;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExMailArrived;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.ScriptManager;
import net.sf.l2j.gameserver.skills.AbnormalEffect;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;
import net.sf.l2j.gameserver.util.Util;

public class EnterWorld extends L2GameClientPacket
{
	private static final String LOAD_PLAYER_QUESTS = "SELECT name,var,value FROM character_quests WHERE charId=?";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
		{
			getClient().closeNow();
			return;
		}
		
	    if (Config.MULTIBOX_PROTECTION_ENABLED) {
	        IPManager.getInstance().validBox(player, Integer.valueOf(Config.MULTIBOX_PROTECTION_CLIENTS_PER_PC), World.getInstance().getPlayers(), Boolean.valueOf(true)); 
	    }
	    
		TvTEvent.onLogin(player);
		
		final int objectId = player.getObjectId();
		
		if (player.isGM())
		{
		      if (!Util.contains(Config.GM_NAMES, player.getName())) {
			      NpcHtmlMessage html = new NpcHtmlMessage(0);
			      html.setFile("data/html/mods/No_admin.htm");
			      html.replace("%name%", player.getName());
			      html.replace("%secunds%", Config.TIME_ADMIN);
			      player.sendPacket(html);
			      player.setIsParalyzed(true);
			      player.setManutencao(true);
			      player.startAbnormalEffect(2048);
			      player.startAbnormalEffect(AbnormalEffect.ROOT);
			      ThreadPool.schedule(new CloseGame(player, Config.TIME_ADMIN), 0L);
		        }
			
			if (Config.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_setinvul", player.getAccessLevel()))
				player.setIsInvul(true);
			
			if (Config.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_hide", player.getAccessLevel()))
				player.getAppearance().setInvisible();
			
			if (Config.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", player.getAccessLevel()))
				player.setInRefusalMode(true);
			
			if (Config.GM_STARTUP_AUTO_LIST && AdminData.getInstance().hasAccess("admin_gmlist", player.getAccessLevel()))
				AdminData.addGm(player, false);
			else
				AdminData.addGm(player, true);
			
		    for (Player allgms : World.getAllGMs()) {
		        allgms.sendPacket(new CreatureSay(0, 1, ".", "O membro da Staff " + player.getName() + " esta online. :."));
		        }
		}
		
		// Set dead status if applies
		if (player.getCurrentHp() < 0.5)
			player.setIsDead(true);
		
		// Clan checks.
		final Clan clan = player.getClan();
		if (clan != null)
		{
			player.sendPacket(new PledgeSkillList(clan));
			
			// Refresh player instance.
			clan.getClanMember(objectId).setPlayerInstance(player);
			
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addCharName(player);
			final PledgeShowMemberListUpdate update = new PledgeShowMemberListUpdate(player);
			
			// Send packets to others members.
			for (Player member : clan.getOnlineMembers())
			{
				if (member == player)
					continue;
				
				member.sendPacket(msg);
				member.sendPacket(update);
			}
			
			// Send a login notification to sponsor or apprentice, if logged.
			if (player.getSponsor() != 0)
			{
				final Player sponsor = World.getInstance().getPlayer(player.getSponsor());
				if (sponsor != null)
					sponsor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addCharName(player));
			}
			else if (player.getApprentice() != 0)
			{
				final Player apprentice = World.getInstance().getPlayer(player.getApprentice());
				if (apprentice != null)
					apprentice.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addCharName(player));
			}
			
			// Add message at connexion if clanHall not paid.
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if (clanHall != null && !clanHall.getPaid())
				player.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
			
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				final Siege siege = castle.getSiege();
				if (!siege.isInProgress())
					continue;
				
				final SiegeSide type = siege.getSide(clan);
				if (type == SiegeSide.ATTACKER)
					player.setSiegeState((byte) 1);
				else if (type == SiegeSide.DEFENDER || type == SiegeSide.OWNER)
					player.setSiegeState((byte) 2);
			}
			
			player.sendPacket(new PledgeShowMemberListAll(clan, 0));
			
			for (SubPledge sp : clan.getAllSubPledges())
				player.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
			
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new PledgeStatusChanged(clan));
		}
		
		// Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod() && SevenSigns.getInstance().getSealOwner(SealType.STRIFE) != CabalType.NORMAL)
		{
			CabalType cabal = SevenSigns.getInstance().getPlayerCabal(objectId);
			if (cabal != CabalType.NORMAL)
			{
				if (cabal == SevenSigns.getInstance().getSealOwner(SealType.STRIFE))
					player.addSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill(), false);
				else
					player.addSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill(), false);
			}
		}
		else
		{
			player.removeSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill().getId(), false);
			player.removeSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill().getId(), false);
		}
		
        if ((ArenaTask.is_started()) && (Config.ARENA_MESSAGE_ENABLED)) {
        	player.sendPacket(new ExShowScreenMessage(Config.ARENA_MESSAGE_TEXT, Config.ARENA_MESSAGE_TIME, 2, true));
        	player.sendPacket(new CreatureSay(0, 3, ".", " Tournament 2x2 / 4x4 / 9x9 .. register now :."));
          } else if ((PartyFarm.is_started()) && (Config.PARTY_FARM_BY_TIME_OF_DAY) && (Config.PARTY_MESSAGE_ENABLED)) {
          	player.sendPacket(new ExShowScreenMessage(Config.PARTY_FARM_MESSAGE_TEXT, Config.PARTY_FARM_MESSAGE_TIME, 2, true));
        	  player.sendPacket(new CreatureSay(0, 3, ".", " Party Event is active  :."));
          }
        
   		if(Config.RESTART_BY_TIME_OF_DAY)
           {
            ShowNextRestart(player);
           }
        
	    if ((Config.ADD_SKILL_NOBLES) && (!player.isNoble()))
	    {
	      L2Skill skill = null;
	      skill = SkillTable.getInstance().getInfo(1323, 1);
	      String skill_name = skill.getName();
	      player.sendMessage("You received the skill " + skill_name + " temporarily.");
	      player.addSkill(SkillTable.getInstance().getInfo(1323, 1), false);
	    }
	    
	    if (Config.MAX_ITEM_ENCHANT_KICK > 0) {
	        for (ItemInstance i : player.getInventory().getItems()) {
	          if (!player.isGM()) {
	            if (i.isEquipable()) {
	              if (i.getEnchantLevel() > Config.MAX_ITEM_ENCHANT_KICK)
	              {
	            	  player.getInventory().destroyItem(null, i, player, null);
	                
	            	  player.sendMessage("SYS: You have over enchanted items you will be kicked from server!");
	            	  player.sendMessage("SYS: Respect our server rules.");
	            	  overEnchant(player);
	                
	                LOGGER.info("#### ATTENTION ####");
	                LOGGER.info(i + " item has been removed from " + player);
	              }
	            }
	          }
	        }
	      }
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			player.setSpawnProtection(true);
		
		player.spawnMe();
		//Hwid Check
		Hwid.enterlog(player, getClient());
		ShowHtmls(player);

		
		if (player.getOnlineTime() == 0 && Config.PLAYER_ITEMS)
			 {
			 if (player.isMageClass())
			 {
			 for (int[] mageItems : Config.MAGE_ITEMS_LIST)
			 {
			 if (mageItems == null)
			 continue;
			 player.getInventory().addItem("additems", mageItems[0], mageItems[1], player, player);
			  }
			 }
			 else
			 for (int[] fighterItems : Config.FIGHTER_ITEMS_LIST)
			 {
			 if (fighterItems == null)
			 continue;
			 player.getInventory().addItem("additems", fighterItems[0], fighterItems[1], player, player);
			 }
			}


		
		
		// Engage and notify partner.
		if (Config.ALLOW_WEDDING)
		{
			for (Entry<Integer, IntIntHolder> coupleEntry : CoupleManager.getInstance().getCouples().entrySet())
			{
				final IntIntHolder couple = coupleEntry.getValue();
				if (couple.getId() == objectId || couple.getValue() == objectId)
				{
					player.setCoupleId(coupleEntry.getKey());
					break;
				}
			}
		}
		
		// Announcements, welcome & Seven signs period messages
		player.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		player.sendPacket(SevenSigns.getInstance().getCurrentPeriod().getMessageId());
		AnnouncementData.getInstance().showAnnouncements(player, false);
		
		if (Config.ALT_OLY_END_ANNOUNCE)
			Olympiad.getInstance().olympiadEnd(player);
		
		
        // restore color pvp
		player.colorsPvPCheck();
		player.colorsPkCheck();
		
		// Restores custom status
		player.restoreCustomStatus();
		
		// if player is DE, check for shadow sense skill at night
		if (player.getRace() == ClassRace.DARK_ELF && player.hasSkill(L2Skill.SKILL_SHADOW_SENSE))
			player.sendPacket(SystemMessage.getSystemMessage((GameTimeTaskManager.getInstance().isNight()) ? SystemMessageId.NIGHT_S1_EFFECT_APPLIES : SystemMessageId.DAY_S1_EFFECT_DISAPPEARS).addSkillName(L2Skill.SKILL_SHADOW_SENSE));
		
		player.getMacroses().sendUpdate();
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new HennaInfo(player));
		player.sendPacket(new FriendList(player));
		// activeChar.queryGameGuard();
		player.sendPacket(new ItemList(player, false));
		player.sendPacket(new ShortCutInit(player));
		player.sendPacket(new ExStorageMaxCount(player));
		
		// no broadcast needed since the player will already spawn dead to others
		if (player.isAlikeDead())
			player.sendPacket(new Die(player));
		
		player.updateEffectIcons();
		player.sendPacket(new EtcStatusUpdate(player));
		player.sendSkillList();
		
		// Load quests.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_PLAYER_QUESTS))
		{
			ps.setInt(1, objectId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final String questName = rs.getString("name");
					
					// Test quest existence.
					final Quest quest = ScriptManager.getInstance().getQuest(questName);
					if (quest == null)
					{
						LOGGER.warn("Unknown quest {} for player {}.", questName, player.getName());
						continue;
					}
					
					// Each quest get a single state ; create one QuestState per found <state> variable.
					final String var = rs.getString("var");
					if (var.equals("<state>"))
					{
						new QuestState(player, quest, rs.getByte("value"));
						
						// Notify quest for enterworld event, if quest allows it.
						if (quest.getOnEnterWorld())
							quest.notifyEnterWorld(player);
					}
					// Feed an existing quest state.
					else
					{
						final QuestState qs = player.getQuestState(questName);
						if (qs == null)
						{
							LOGGER.warn("Unknown quest state {} for player {}.", questName, player.getName());
							continue;
						}
						
						qs.setInternal(var, rs.getString("value"));
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load quests for player {}.", e, player.getName());
		}
		
		player.sendPacket(new QuestList(player));
		
		// Unread mails make a popup appears.
		if (Config.ENABLE_COMMUNITY_BOARD && MailBBSManager.getInstance().checkUnreadMail(player) > 0)
		{
			player.sendPacket(SystemMessageId.NEW_MAIL);
			player.sendPacket(new PlaySound("systemmsg_e.1233"));
			player.sendPacket(ExMailArrived.STATIC_PACKET);
		}
		
		// Clan notice, if active.
		if (Config.ENABLE_COMMUNITY_BOARD && clan != null && clan.isNoticeEnabled())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/clan_notice.htm");
			html.replace("%clan_name%", clan.getName());
			html.replace("%notice_text%", clan.getNotice().replaceAll("\r\n", "<br>").replaceAll("action", "").replaceAll("bypass", ""));
			sendPacket(html);
		}
		else if (Config.SERVER_NEWS)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/servnews.htm");
			sendPacket(html);
		}
		
	    if (Config.ENABLE_STARTUP)
	    {
	    	if (player.getOnlineTime() == 0){
	          NewbiesSystemManager.onEnterEquip(player);
	          NewbiesSystemManager.onEnterWepEquip(player);
	          NewbiesSystemManager.Welcome(player);
	          
	    	}

	    }
		
		PetitionManager.getInstance().checkPetitionMessages(player);
		
		player.onPlayerEnter();
		
		player.onVipEnter(player);
		


		
		sendPacket(new SkillCoolTime(player));
		
		// If player logs back in a stadium, port him in nearest town.
		if (Olympiad.getInstance().playerInStadia(player))
			player.teleToLocation(TeleportType.TOWN);
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), false))
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
		
		if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
			player.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		
		// Attacker or spectator logging into a siege zone will be ported at town.
		if (!player.isGM() && (!player.isInSiege() || player.getSiegeState() < 2) && player.isInsideZone(ZoneId.SIEGE))
			player.teleToLocation(TeleportType.TOWN);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	

	static void overEnchant(Player activeChar)
	{
	      NpcHtmlMessage html = new NpcHtmlMessage(0);
	      html.setFile("data/html/mods/over_enchant.htm");
	      html.replace("%name%", activeChar.getName());
	      html.replace("%secunds%", Config.TIME_KICK);
	      activeChar.sendPacket(html);
	      activeChar.setIsParalyzed(true);
	      activeChar.startAbnormalEffect(2048);
	      activeChar.startAbnormalEffect(AbnormalEffect.ROOT);
	      ThreadPool.schedule(new CloseGame(activeChar, Config.TIME_KICK), 0L);
		
	}

		private static void ShowNextRestart(Player player)
		{
			player.sendMessage("Next Restart: " + Restart.getInstance().getRestartNextTime()); 
		}
	
	private static void ShowHtmls(Player player)
	{
	    if ((Config.ALLOW_MANUTENCAO) && (!player.isGM()))
	    {
	    	player.sendPacket(new ExShowScreenMessage(Config.MANUTENCAO_TEXT, 23000, 2, true));
	      NpcHtmlMessage html = new NpcHtmlMessage(0);
	      html.setFile("data/html/mods/manutencao.htm");
	      html.replace("%name%", player.getName());
	      html.replace("%secunds%", Config.TIME_MANUTENCAO);
	      player.sendPacket(html);
	      player.setIsParalyzed(true);
	      player.setManutencao(true);
	      player.startAbnormalEffect(2048);
	      player.startAbnormalEffect(AbnormalEffect.ROOT);
	      ThreadPool.schedule(new CloseGame(player, Config.TIME_MANUTENCAO), 0L);
	    }
	    else if (Config.DONATESYSTEM) {
	        Donate.showMainHtml(player);
	      } 
	    
		
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}


}