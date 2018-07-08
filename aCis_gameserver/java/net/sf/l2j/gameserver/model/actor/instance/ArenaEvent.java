/*
 * Copyright (C) 2004-2013 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.events.teamvsteam.TvTEvent;
import net.sf.l2j.gameserver.events.tournaments.Tournament;
import net.sf.l2j.gameserver.events.tournaments.UnrealTournament;
import net.sf.l2j.gameserver.events.tournaments.XtremeTournament;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class ArenaEvent extends Npc
{

	public ArenaEvent(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	  @Override
	public void showChatWindow(Player player, int val)
	  {
	    player.sendPacket(ActionFailed.STATIC_PACKET);
	    String filename = "data/html/mods/tournament/10006.htm";
	    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
	    html.setFile(filename);
	    html.replace("%objectId%", String.valueOf(getObjectId()));
	    if (Tournament.registered.size() == 0) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_0_over\" fore=\"L2UI_CH3.calculate1_0\">");
	    } else if (Tournament.registered.size() == 1) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_1_over\" fore=\"L2UI_CH3.calculate1_1\">");
	    } else if (Tournament.registered.size() == 2) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_2_over\" fore=\"L2UI_CH3.calculate1_2\">");
	    } else if (Tournament.registered.size() == 3) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_3_over\" fore=\"L2UI_CH3.calculate1_3\">");
	    } else if (Tournament.registered.size() == 4) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_4_over\" fore=\"L2UI_CH3.calculate1_4\">");
	    } else if (Tournament.registered.size() == 5) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_5_over\" fore=\"L2UI_CH3.calculate1_5\">");
	    } else if (Tournament.registered.size() == 6) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_6_over\" fore=\"L2UI_CH3.calculate1_6\">");
	    } else if (Tournament.registered.size() == 7) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_7_over\" fore=\"L2UI_CH3.calculate1_7\">");
	    } else if (Tournament.registered.size() == 8) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_8_over\" fore=\"L2UI_CH3.calculate1_8\">");
	    } else if (Tournament.registered.size() >= 9) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_9_over\" fore=\"L2UI_CH3.calculate1_9\">");
	    }
	    if (UnrealTournament.registered.size() == 0) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_0_over\" fore=\"L2UI_CH3.calculate1_0\">");
	    } else if (UnrealTournament.registered.size() == 1) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_1_over\" fore=\"L2UI_CH3.calculate1_1\">");
	    } else if (UnrealTournament.registered.size() == 2) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_2_over\" fore=\"L2UI_CH3.calculate1_2\">");
	    } else if (UnrealTournament.registered.size() == 3) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_3_over\" fore=\"L2UI_CH3.calculate1_3\">");
	    } else if (UnrealTournament.registered.size() == 4) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_4_over\" fore=\"L2UI_CH3.calculate1_4\">");
	    } else if (UnrealTournament.registered.size() == 5) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_5_over\" fore=\"L2UI_CH3.calculate1_5\">");
	    } else if (UnrealTournament.registered.size() == 6) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_6_over\" fore=\"L2UI_CH3.calculate1_6\">");
	    } else if (UnrealTournament.registered.size() == 7) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_7_over\" fore=\"L2UI_CH3.calculate1_7\">");
	    } else if (UnrealTournament.registered.size() == 8) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_8_over\" fore=\"L2UI_CH3.calculate1_8\">");
	    } else if (UnrealTournament.registered.size() >= 9) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_9_over\" fore=\"L2UI_CH3.calculate1_9\">");
	    }
	    if (XtremeTournament.registered.size() == 0) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_0_over\" fore=\"L2UI_CH3.calculate1_0\">");
	    } else if (XtremeTournament.registered.size() == 1) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_1_over\" fore=\"L2UI_CH3.calculate1_1\">");
	    } else if (XtremeTournament.registered.size() == 2) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_2_over\" fore=\"L2UI_CH3.calculate1_2\">");
	    } else if (XtremeTournament.registered.size() == 3) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_3_over\" fore=\"L2UI_CH3.calculate1_3\">");
	    } else if (XtremeTournament.registered.size() == 4) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_4_over\" fore=\"L2UI_CH3.calculate1_4\">");
	    } else if (XtremeTournament.registered.size() == 5) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_5_over\" fore=\"L2UI_CH3.calculate1_5\">");
	    } else if (XtremeTournament.registered.size() == 6) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_6_over\" fore=\"L2UI_CH3.calculate1_6\">");
	    } else if (XtremeTournament.registered.size() == 7) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_7_over\" fore=\"L2UI_CH3.calculate1_7\">");
	    } else if (XtremeTournament.registered.size() == 8) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_8_over\" fore=\"L2UI_CH3.calculate1_8\">");
	    } else if (XtremeTournament.registered.size() >= 9) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_9_over\" fore=\"L2UI_CH3.calculate1_9\">");
	    }
	    player.sendPacket(html);
	  }
	  
	
	

	@Override
	public void onBypassFeedback(Player player, String command)
	{

		if (command.startsWith("2x2")) 
		{
			if (!player.isInParty())
			{
				player.sendMessage("You dont have a party.");
				return;
			}

			if (!player.getParty().isLeader(player))
			{
				player.sendMessage("You are not the party leader!");
				return;
			}
		      if ((player.isUnrealTournament()) || (player.isXtremeTournament()))
		      {
		        player.sendMessage("Tournament: You already registered!.");
		        HTML(player);
		        return;
		      }
		      if (player.getParty().getMembersCount() < 2)
		      {
		        player.sendMessage("Voce precisa ter 2 jogadores ou mais em party.");
		        player.sendPacket(new ExShowScreenMessage("Your party does not have 2 members", 6000));
		        HTML(player);
		        return;
		      }
		      if (player.getParty().getMembersCount() > 2)
		      {
		        player.sendMessage("Sua party nao pode ter mais que 2 jogadores.");
		        player.sendPacket(new ExShowScreenMessage("Your Party can not have more than 2 members", 6000));
		        HTML(player);
		        return;
		      }

			Player assist = player.getParty().getMembers().get(1);
		      if ((assist.getClassId() == ClassId.BISHOP) || (assist.getClassId() == ClassId.CARDINAL) || (assist.getClassId() == ClassId.SHILLIEN_ELDER) || (assist.getClassId() == ClassId.SHILLIEN_SAINT))
		      {
		        assist.sendMessage("SYS: Classe Bishop nao permitida em 2x2.");
		        player.sendMessage("SYS: Classe Bishop nao permitida em 2x2.");
		        return;
		      }
		      if ((player.getClassId() == ClassId.BISHOP) || (player.getClassId() == ClassId.CARDINAL) || (player.getClassId() == ClassId.SHILLIEN_ELDER) || (player.getClassId() == ClassId.SHILLIEN_SAINT))
		      {
		        assist.sendMessage("SYS: Classe Bishop nao permitida em 2x2.");
		        player.sendMessage("SYS: Classe Bishop nao permitida em 2x2.");

		        return;
		      }
			//checks
			if (player.isCursedWeaponEquipped() || assist.isCursedWeaponEquipped() || player.isInObserverMode() || assist.isInObserverMode() || player.isInStoreMode() || assist.isInStoreMode() || player.getKarma() > 0 || assist.getKarma() > 0)
			{
				player.sendMessage("You or your member does not have the necessary requirements.");
				assist.sendMessage("You or your member does not have the necessary requirements.");
				return;
			}
			
			//oly checks
			if (OlympiadManager.getInstance().isRegistered(player) || OlympiadManager.getInstance().isRegistered(assist))
			{
				player.sendMessage("You or your member is registered in the Olympiad.");
				assist.sendMessage("You or your member is registered in the Olympiad.");
				return;
			}
			
			//event checks
			if (TvTEvent.isPlayerParticipant(assist.getObjectId()))
			{
				player.sendMessage("You or your member is registered in another event.");
				assist.sendMessage("You or your member is registered in another event.");
				return;
			}

			//pvp checks
			if (player.getPvpKills() <= Config.ARENA_PVP_AMOUNT || assist.getPvpKills() <= Config.ARENA_PVP_AMOUNT)
			{
				player.sendMessage("You or your member need " + Config.ARENA_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist.sendMessage("You or your member need " + Config.ARENA_PVP_AMOUNT + " pvp kills to register in tournament.");
				return;
			}
			
			else if (Config.DISABLE_TOURNAMENT_ID_CLASSES.contains(player.getClassId().getId()) || Config.DISABLE_TOURNAMENT_ID_CLASSES.contains(assist.getClassId().getId()))
			{
				player.sendMessage("You or your member class is not allowed in tournament.");
				assist.sendMessage("You or your member class is not allowed in tournament.");
				return;
			}
			
			//dual box checks
			if(Config.DUAL_BOX)
			if (player.getClient() != null && assist.getClient() != null)
			{
				String ip1 = player.getClient().getConnection().getInetAddress().getHostAddress();
				String ip2 = assist.getClient().getConnection().getInetAddress().getHostAddress();

				if (ip1.equals(ip2))
				{
					player.sendMessage("Dual box is not allowed on tournament.");
					assist.sendMessage("Dual box is not allowed on tournament.");
					return;
				}
			}
			
			

			if (Tournament.getInstance().register(player, assist))
			{
				player.sendMessage(player.getName() + " Your party has been registered!");
				assist.sendMessage(assist.getName() + " Your party has been registered!");
			}
			else
				return;
		}
		else if (command.startsWith("4x4")) 
		{

			
			if (!player.isInParty())
			{
				player.sendMessage("You dont have a party.");
				return;
			}

			if (!player.getParty().isLeader(player))
			{
				player.sendMessage("You are not the party leader!");
				return;
			}
		      if (player.isXtremeTournament())
		      {
		        player.sendMessage("Tournament: You already registered!.");
		        HTML(player);
		        return;
		      }
			
		      if (player.getParty().getMembersCount() < 4)
		      {
		        player.sendMessage("Voce precisa ter 4 jogadores ou mais em party.");
		        player.sendPacket(new ExShowScreenMessage("Your party does not have 4 members", 6000));
		        HTML(player);
		        return;
		      }
		      if (player.getParty().getMembersCount() > 4)
		      {
		        player.sendMessage("Sua party nao pode ter mais que 4 jogadores.");
		        player.sendPacket(new ExShowScreenMessage("Your Party can not have more than 4 members", 6000));
		        HTML(player);
		        return;
		      }

			//4 Player + 1 Leader
			Player assist1 = player.getParty().getMembers().get(1);
			Player assist2 = player.getParty().getMembers().get(2);
			Player assist3 = player.getParty().getMembers().get(3);

			//checks
			if (player.isCursedWeaponEquipped() || assist1.isCursedWeaponEquipped()  || assist2.isCursedWeaponEquipped()  || assist3.isCursedWeaponEquipped() || player.isInObserverMode() || assist1.isInObserverMode() || assist2.isInObserverMode() || assist3.isInObserverMode() || player.isInStoreMode() || assist1.isInStoreMode() || assist2.isInStoreMode() || assist3.isInStoreMode() || player.getKarma() > 0 || assist1.getKarma() > 0 || assist2.getKarma() > 0 || assist3.getKarma() > 0)
			{
				player.sendMessage("You or your member does not have the necessary requirements.");
				assist1.sendMessage("You or your member does not have the necessary requirements.");
				assist2.sendMessage("You or your member does not have the necessary requirements.");
				assist3.sendMessage("You or your member does not have the necessary requirements.");
				return;
			}
			
			//oly checks
			if (OlympiadManager.getInstance().isRegistered(player) || OlympiadManager.getInstance().isRegistered(assist1) || OlympiadManager.getInstance().isRegistered(assist2) || OlympiadManager.getInstance().isRegistered(assist3))
			{
				player.sendMessage("You or your member is registered in the Olympiad.");
				assist1.sendMessage("You or your member is registered in the Olympiad.");
				assist2.sendMessage("You or your member is registered in the Olympiad.");
				assist3.sendMessage("You or your member is registered in the Olympiad.");
				return;
			}
			
			
			//event checks
			if (TvTEvent.isPlayerParticipant(player.getObjectId()) || TvTEvent.isPlayerParticipant(assist1.getObjectId()) || TvTEvent.isPlayerParticipant(assist2.getObjectId()) || TvTEvent.isPlayerParticipant(assist3.getObjectId())) 
			{
				player.sendMessage("You or your member is registered in another event.");
				assist1.sendMessage("You or your member is registered in another event.");
				assist2.sendMessage("You or your member is registered in another event.");
				assist3.sendMessage("You or your member is registered in another event.");
				return;
			}
			
			//pvp checks
			if (player.getPvpKills() <= Config.UNREAL_TOURNAMENT_PVP_AMOUNT || assist1.getPvpKills() <= Config.UNREAL_TOURNAMENT_PVP_AMOUNT || assist2.getPvpKills() <= Config.UNREAL_TOURNAMENT_PVP_AMOUNT || assist3.getPvpKills() <= Config.UNREAL_TOURNAMENT_PVP_AMOUNT)
			{
				player.sendMessage("You or your member need " + Config.UNREAL_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist1.sendMessage("You or your member need " + Config.UNREAL_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist2.sendMessage("You or your member need " + Config.UNREAL_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist3.sendMessage("You or your member need " + Config.UNREAL_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				return;
			}
		
			//dual box checks
			if(Config.DUAL_BOX)
			if (player.getClient() != null && assist1.getClient() != null && assist2.getClient() != null && assist3.getClient() != null)
			{
				String ip1 = player.getClient().getConnection().getInetAddress().getHostAddress();
				String ip2 = assist1.getClient().getConnection().getInetAddress().getHostAddress();
				String ip3 = assist2.getClient().getConnection().getInetAddress().getHostAddress();
				String ip4 = assist3.getClient().getConnection().getInetAddress().getHostAddress();

				if (ip1.equals(ip2) || ip1.equals(ip3) || ip1.equals(ip4))
				{
					player.sendMessage("Dual box is not allowed on tournament.");
					assist1.sendMessage("Dual box is not allowed on tournament.");
					assist2.sendMessage("Dual box is not allowed on tournament.");
					assist3.sendMessage("Dual box is not allowed on tournament.");
					return;
				}
			}
			
		      ClasseCheck(player);
		      if (player.duelist_cont > Config.duelist_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.duelist_COUNT_4X4 + " Duelist's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.duelist_COUNT_4X4 + " Duelist's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.dreadnought_cont > Config.dreadnought_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.dreadnought_COUNT_4X4 + " Dread Nought's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.dreadnought_COUNT_4X4 + " Dread Nought's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.tanker_cont > Config.tanker_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.tanker_COUNT_4X4 + " Tanker's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.tanker_COUNT_4X4 + " Tanker's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.dagger_cont > Config.dagger_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.dagger_COUNT_4X4 + " Dagger's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.dagger_COUNT_4X4 + " Dagger's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.archer_cont > Config.archer_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.archer_COUNT_4X4 + " Archer's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.archer_COUNT_4X4 + " Archer's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.bs_cont > Config.bs_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.bs_COUNT_4X4 + " Bishop's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.bs_COUNT_4X4 + " Bishop's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.archmage_cont > Config.archmage_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.archmage_COUNT_4X4 + " Archmage's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.archmage_COUNT_4X4 + " Archmage's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.soultaker_cont > Config.soultaker_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.soultaker_COUNT_4X4 + " Soultaker's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.soultaker_COUNT_4X4 + " Soultaker's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.mysticMuse_cont > Config.mysticMuse_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.mysticMuse_COUNT_4X4 + " Mystic Muse's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.mysticMuse_COUNT_4X4 + " Mystic Muse's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.stormScreamer_cont > Config.stormScreamer_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.stormScreamer_COUNT_4X4 + " Storm Screamer's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.stormScreamer_COUNT_4X4 + " Storm Screamer's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.titan_cont > Config.titan_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.titan_COUNT_4X4 + " Titan's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.titan_COUNT_4X4 + " Titan's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.grandKhauatari_cont > Config.grandKhauatari_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.grandKhauatari_COUNT_4X4 + " Grand Khauatari's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.grandKhauatari_COUNT_4X4 + " Grand Khauatari's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.dominator_cont > Config.dominator_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.dominator_COUNT_4X4 + " Dominator's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.dominator_COUNT_4X4 + " Dominator's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.doomcryer_cont > Config.doomcryer_COUNT_4X4)
		      {
		        player.sendMessage("Only " + Config.doomcryer_COUNT_4X4 + " Doomcryer's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.doomcryer_COUNT_4X4 + " Doomcryer's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
			
			//Register party
			if (UnrealTournament.getInstance().register(player, assist1, assist2, assist3))
			{
				player.sendMessage(player.getName() + " Your party has been registered!");
				assist1.sendMessage(assist1.getName() + " Your party has been registered!");
				assist2.sendMessage(assist2.getName() + " Your party has been registered!");
				assist3.sendMessage(assist3.getName() + " Your party has been registered!");
			}
			else
				return;
		}
		else if (command.startsWith("9x9")) 
		{

			
			if (!player.isInParty())
			{
				player.sendMessage("You dont have a party.");
				return;
			}

			if (!player.getParty().isLeader(player))
			{
				player.sendMessage("You are not the party leader!");
				return;
			}
		      if (player.isXtremeTournament())
		      {
		        player.sendMessage("Tournament: You already registered!.");
		        HTML(player);
		        return;
		      }
			
		      if (player.getParty().getMembersCount() < 9)
		      {
		        player.sendMessage("Voce precisa ter 9 jogadores ou mais em party.");
		        player.sendPacket(new ExShowScreenMessage("Your party does not have 9 members", 6000));
		        HTML(player);
		        return;
		      }
		      if (player.getParty().getMembersCount() > 9)
		      {
		        player.sendMessage("Sua party nao pode ter mais que 9 jogadores.");
		        player.sendPacket(new ExShowScreenMessage("Your Party can not have more than 9 members", 6000));
		        HTML(player);
		        return;
		      }

			//8 Player + 1 Leader
			Player assist1 = player.getParty().getMembers().get(1);
			Player assist2 = player.getParty().getMembers().get(2);
			Player assist3 = player.getParty().getMembers().get(3);
			Player assist4 = player.getParty().getMembers().get(4);
			Player assist5 = player.getParty().getMembers().get(5);
			Player assist6 = player.getParty().getMembers().get(6);
			Player assist7 = player.getParty().getMembers().get(7);
			Player assist8 = player.getParty().getMembers().get(8);

			//checks
			if (player.isCursedWeaponEquipped() || assist1.isCursedWeaponEquipped()  || assist2.isCursedWeaponEquipped()  || assist3.isCursedWeaponEquipped()  || assist4.isCursedWeaponEquipped()  || assist5.isCursedWeaponEquipped()  || assist6.isCursedWeaponEquipped()  || assist7.isCursedWeaponEquipped()  || assist8.isCursedWeaponEquipped() || player.isInObserverMode() || assist1.isInObserverMode() || assist2.isInObserverMode()  || assist3.isInObserverMode()  || assist4.isInObserverMode()  || assist5.isInObserverMode()  || assist6.isInObserverMode()  || assist7.isInObserverMode()  || assist8.isInObserverMode() || player.isInStoreMode() || assist1.isInStoreMode() || assist2.isInStoreMode() || assist3.isInStoreMode() || assist4.isInStoreMode()  || assist5.isInStoreMode() || assist6.isInStoreMode() || assist7.isInStoreMode() || assist8.isInStoreMode() || player.getKarma() > 0 || assist1.getKarma() > 0 || assist2.getKarma() > 0 || assist3.getKarma() > 0 || assist4.getKarma() > 0 || assist5.getKarma() > 0 || assist6.getKarma() > 0 || assist7.getKarma() > 0 || assist8.getKarma() > 0)
			{
				player.sendMessage("You or your member does not have the necessary requirements.");
				assist1.sendMessage("You or your member does not have the necessary requirements.");
				assist2.sendMessage("You or your member does not have the necessary requirements.");
				assist3.sendMessage("You or your member does not have the necessary requirements.");
				assist4.sendMessage("You or your member does not have the necessary requirements.");
				assist5.sendMessage("You or your member does not have the necessary requirements.");
				assist6.sendMessage("You or your member does not have the necessary requirements.");
				assist7.sendMessage("You or your member does not have the necessary requirements.");
				assist8.sendMessage("You or your member does not have the necessary requirements.");
				return;
			}
			
			//oly checks
			if (OlympiadManager.getInstance().isRegistered(player) || OlympiadManager.getInstance().isRegistered(assist1) || OlympiadManager.getInstance().isRegistered(assist2) || OlympiadManager.getInstance().isRegistered(assist3) || OlympiadManager.getInstance().isRegistered(assist4) || OlympiadManager.getInstance().isRegistered(assist5) || OlympiadManager.getInstance().isRegistered(assist6) || OlympiadManager.getInstance().isRegistered(assist7) || OlympiadManager.getInstance().isRegistered(assist8))
			{
				player.sendMessage("You or your member is registered in the Olympiad.");
				assist1.sendMessage("You or your member is registered in the Olympiad.");
				assist2.sendMessage("You or your member is registered in the Olympiad.");
				assist3.sendMessage("You or your member is registered in the Olympiad.");
				assist4.sendMessage("You or your member is registered in the Olympiad.");
				assist5.sendMessage("You or your member is registered in the Olympiad.");
				assist6.sendMessage("You or your member is registered in the Olympiad.");
				assist7.sendMessage("You or your member is registered in the Olympiad.");
				assist8.sendMessage("You or your member is registered in the Olympiad.");
				return;
			}
			
			
			//event checks
			if (TvTEvent.isPlayerParticipant(player.getObjectId()) || TvTEvent.isPlayerParticipant(assist1.getObjectId()) || TvTEvent.isPlayerParticipant(assist2.getObjectId()) || TvTEvent.isPlayerParticipant(assist3.getObjectId()) || TvTEvent.isPlayerParticipant(assist4.getObjectId()) || TvTEvent.isPlayerParticipant(assist5.getObjectId()) || TvTEvent.isPlayerParticipant(assist7.getObjectId()) || TvTEvent.isPlayerParticipant(assist8.getObjectId())) 
			{
				player.sendMessage("You or your member is registered in another event.");
				assist1.sendMessage("You or your member is registered in another event.");
				assist2.sendMessage("You or your member is registered in another event.");
				assist3.sendMessage("You or your member is registered in another event.");
				assist4.sendMessage("You or your member is registered in another event.");
				assist5.sendMessage("You or your member is registered in another event.");
				assist6.sendMessage("You or your member is registered in another event.");
				assist7.sendMessage("You or your member is registered in another event.");
				assist8.sendMessage("You or your member is registered in another event.");
				return;
			}
			
			//pvp checks
			if (player.getPvpKills() <= Config.XTREME_TOURNAMENT_PVP_AMOUNT || assist1.getPvpKills() <= Config.XTREME_TOURNAMENT_PVP_AMOUNT || assist2.getPvpKills() <= Config.XTREME_TOURNAMENT_PVP_AMOUNT || assist3.getPvpKills() <= Config.XTREME_TOURNAMENT_PVP_AMOUNT || assist4.getPvpKills() <= Config.XTREME_TOURNAMENT_PVP_AMOUNT || assist5.getPvpKills() <= Config.XTREME_TOURNAMENT_PVP_AMOUNT || assist6.getPvpKills() <= Config.XTREME_TOURNAMENT_PVP_AMOUNT || assist7.getPvpKills() <= Config.XTREME_TOURNAMENT_PVP_AMOUNT || assist8.getPvpKills() <= Config.XTREME_TOURNAMENT_PVP_AMOUNT)
			{
				player.sendMessage("You or your member need " + Config.XTREME_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist1.sendMessage("You or your member need " + Config.XTREME_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist2.sendMessage("You or your member need " + Config.XTREME_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist3.sendMessage("You or your member need " + Config.XTREME_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist4.sendMessage("You or your member need " + Config.XTREME_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist5.sendMessage("You or your member need " + Config.XTREME_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist6.sendMessage("You or your member need " + Config.XTREME_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist7.sendMessage("You or your member need " + Config.XTREME_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				assist8.sendMessage("You or your member need " + Config.XTREME_TOURNAMENT_PVP_AMOUNT + " pvp kills to register in tournament.");
				return;
			}
		
			//dual box checks
			if(Config.DUAL_BOX)
			if (player.getClient() != null && assist1.getClient() != null && assist2.getClient() != null && assist3.getClient() != null && assist4.getClient() != null && assist5.getClient() != null && assist6.getClient() != null && assist7.getClient() != null && assist8.getClient() != null)
			{
				String ip1 = player.getClient().getConnection().getInetAddress().getHostAddress();
				String ip2 = assist1.getClient().getConnection().getInetAddress().getHostAddress();
				String ip3 = assist2.getClient().getConnection().getInetAddress().getHostAddress();
				String ip4 = assist3.getClient().getConnection().getInetAddress().getHostAddress();
				String ip5 = assist4.getClient().getConnection().getInetAddress().getHostAddress();
				String ip6 = assist5.getClient().getConnection().getInetAddress().getHostAddress();
				String ip7 = assist6.getClient().getConnection().getInetAddress().getHostAddress();
				String ip8 = assist7.getClient().getConnection().getInetAddress().getHostAddress();
				String ip9 = assist8.getClient().getConnection().getInetAddress().getHostAddress();

				if (ip1.equals(ip2) || ip1.equals(ip3) || ip1.equals(ip4) || ip1.equals(ip5) || ip1.equals(ip6) || ip1.equals(ip7) || ip1.equals(ip8) || ip1.equals(ip9))
				{
					player.sendMessage("Dual box is not allowed on tournament.");
					assist1.sendMessage("Dual box is not allowed on tournament.");
					assist2.sendMessage("Dual box is not allowed on tournament.");
					assist3.sendMessage("Dual box is not allowed on tournament.");
					assist4.sendMessage("Dual box is not allowed on tournament.");
					assist5.sendMessage("Dual box is not allowed on tournament.");
					assist6.sendMessage("Dual box is not allowed on tournament.");
					assist7.sendMessage("Dual box is not allowed on tournament.");
					assist8.sendMessage("Dual box is not allowed on tournament.");
					return;
				}
			}
			
			
		      ClasseCheck(player);
		      if (player.duelist_cont > Config.duelist_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.duelist_COUNT_9X9 + " Duelist's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.duelist_COUNT_9X9 + " Duelist's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.dreadnought_cont > Config.dreadnought_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.dreadnought_COUNT_9X9 + " Dread Nought's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.dreadnought_COUNT_9X9 + " Dread Nought's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.tanker_cont > Config.tanker_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.tanker_COUNT_9X9 + " Tanker's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.tanker_COUNT_9X9 + " Tanker's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.dagger_cont > Config.dagger_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.dagger_COUNT_9X9 + " Dagger's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.dagger_COUNT_9X9 + " Dagger's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.archer_cont > Config.archer_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.archer_COUNT_9X9 + " Archer's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.archer_COUNT_9X9 + " Archer's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.bs_cont > Config.bs_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.bs_COUNT_9X9 + " Bishop's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.bs_COUNT_9X9 + " Bishop's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.archmage_cont > Config.archmage_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.archmage_COUNT_9X9 + " Archmage's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.archmage_COUNT_9X9 + " Archmage's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.soultaker_cont > Config.soultaker_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.soultaker_COUNT_9X9 + " Soultaker's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.soultaker_COUNT_9X9 + " Soultaker's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.mysticMuse_cont > Config.mysticMuse_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.mysticMuse_COUNT_9X9 + " Mystic Muse's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.mysticMuse_COUNT_9X9 + " Mystic Muse's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.stormScreamer_cont > Config.stormScreamer_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.stormScreamer_COUNT_9X9 + " Storm Screamer's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.stormScreamer_COUNT_9X9 + " Storm Screamer's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.titan_cont > Config.titan_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.titan_COUNT_9X9 + " Titan's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.titan_COUNT_9X9 + " Titan's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.grandKhauatari_cont > Config.grandKhauatari_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.grandKhauatari_COUNT_9X9 + " Grand Khauatari's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.grandKhauatari_COUNT_9X9 + " Grand Khauatari's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.dominator_cont > Config.dominator_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.dominator_COUNT_9X9 + " Dominator's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.dominator_COUNT_9X9 + " Dominator's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
		      if (player.doomcryer_cont > Config.doomcryer_COUNT_9X9)
		      {
		        player.sendMessage("Only " + Config.doomcryer_COUNT_9X9 + " Doomcryer's allowed per party.");
		        player.sendPacket(new ExShowScreenMessage("Only " + Config.doomcryer_COUNT_9X9 + " Doomcryer's allowed per party.", 6000));
		        clean(player);
		        HTML(player);
		        return;
		      }
			
			//Register party
			if (XtremeTournament.getInstance().register(player, assist1, assist2, assist3, assist4, assist5, assist6, assist7, assist8))
			{
				player.sendMessage(player.getName() + " Your party has been registered!");
				assist1.sendMessage(assist1.getName() + " Your party has been registered!");
				assist2.sendMessage(assist2.getName() + " Your party has been registered!");
				assist3.sendMessage(assist3.getName() + " Your party has been registered!");
				assist4.sendMessage(assist4.getName() + " Your party has been registered!");
				assist5.sendMessage(assist5.getName() + " Your party has been registered!");
				assist6.sendMessage(assist6.getName() + " Your party has been registered!");
				assist7.sendMessage(assist7.getName() + " Your party has been registered!");
				assist8.sendMessage(assist8.getName() + " Your party has been registered!");
			}
			else
				return;
		}
		   else if (command.startsWith("remove"))
		    {
		      if (!player.isInParty())
		      {
		        player.sendMessage("You dont have a party.");
		        return;
		      }
		      if (!player.getParty().isLeader(player))
		      {
		        player.sendMessage("You are not the party leader!");
		        return;
		      }
		      Tournament.getInstance().remove(player);
		      UnrealTournament.getInstance().remove(player);
		      XtremeTournament.getInstance().remove(player);
		    }
		    else if (command.startsWith("observe_list"))
		    {
		      player.sendPacket(ActionFailed.STATIC_PACKET);
		      String filename = "data/html/mods/tournament/10006-1.htm";
		      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		      html.setFile(filename);
		      html.replace("%objectId%", String.valueOf(getObjectId()));
		      if (Tournament.registered.size() == 0) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_0_over\" fore=\"L2UI_CH3.calculate1_0\">");
		      } else if (Tournament.registered.size() == 1) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_1_over\" fore=\"L2UI_CH3.calculate1_1\">");
		      } else if (Tournament.registered.size() == 2) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_2_over\" fore=\"L2UI_CH3.calculate1_2\">");
		      } else if (Tournament.registered.size() == 3) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_3_over\" fore=\"L2UI_CH3.calculate1_3\">");
		      } else if (Tournament.registered.size() == 4) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_4_over\" fore=\"L2UI_CH3.calculate1_4\">");
		      } else if (Tournament.registered.size() == 5) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_5_over\" fore=\"L2UI_CH3.calculate1_5\">");
		      } else if (Tournament.registered.size() == 6) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_6_over\" fore=\"L2UI_CH3.calculate1_6\">");
		      } else if (Tournament.registered.size() == 7) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_7_over\" fore=\"L2UI_CH3.calculate1_7\">");
		      } else if (Tournament.registered.size() == 8) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_8_over\" fore=\"L2UI_CH3.calculate1_8\">");
		      } else if (Tournament.registered.size() >= 9) {
		        html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_9_over\" fore=\"L2UI_CH3.calculate1_9\">");
		      }
		      if (UnrealTournament.registered.size() == 0) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_0_over\" fore=\"L2UI_CH3.calculate1_0\">");
		      } else if (UnrealTournament.registered.size() == 1) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_1_over\" fore=\"L2UI_CH3.calculate1_1\">");
		      } else if (UnrealTournament.registered.size() == 2) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_2_over\" fore=\"L2UI_CH3.calculate1_2\">");
		      } else if (UnrealTournament.registered.size() == 3) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_3_over\" fore=\"L2UI_CH3.calculate1_3\">");
		      } else if (UnrealTournament.registered.size() == 4) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_4_over\" fore=\"L2UI_CH3.calculate1_4\">");
		      } else if (UnrealTournament.registered.size() == 5) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_5_over\" fore=\"L2UI_CH3.calculate1_5\">");
		      } else if (UnrealTournament.registered.size() == 6) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_6_over\" fore=\"L2UI_CH3.calculate1_6\">");
		      } else if (UnrealTournament.registered.size() == 7) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_7_over\" fore=\"L2UI_CH3.calculate1_7\">");
		      } else if (UnrealTournament.registered.size() == 8) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_8_over\" fore=\"L2UI_CH3.calculate1_8\">");
		      } else if (UnrealTournament.registered.size() >= 9) {
		        html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_9_over\" fore=\"L2UI_CH3.calculate1_9\">");
		      }
		      if (XtremeTournament.registered.size() == 0) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_0_over\" fore=\"L2UI_CH3.calculate1_0\">");
		      } else if (XtremeTournament.registered.size() == 1) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_1_over\" fore=\"L2UI_CH3.calculate1_1\">");
		      } else if (XtremeTournament.registered.size() == 2) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_2_over\" fore=\"L2UI_CH3.calculate1_2\">");
		      } else if (XtremeTournament.registered.size() == 3) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_3_over\" fore=\"L2UI_CH3.calculate1_3\">");
		      } else if (XtremeTournament.registered.size() == 4) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_4_over\" fore=\"L2UI_CH3.calculate1_4\">");
		      } else if (XtremeTournament.registered.size() == 5) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_5_over\" fore=\"L2UI_CH3.calculate1_5\">");
		      } else if (XtremeTournament.registered.size() == 6) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_6_over\" fore=\"L2UI_CH3.calculate1_6\">");
		      } else if (XtremeTournament.registered.size() == 7) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_7_over\" fore=\"L2UI_CH3.calculate1_7\">");
		      } else if (XtremeTournament.registered.size() == 8) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_8_over\" fore=\"L2UI_CH3.calculate1_8\">");
		      } else if (XtremeTournament.registered.size() >= 9) {
		        html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_9_over\" fore=\"L2UI_CH3.calculate1_9\">");
		      }
		      player.sendPacket(html);
		    }
		    else if (command.startsWith("observe_back"))
		    {
		      HTML(player);
		    }
		else if (command.startsWith("tournament_observe"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();

			final int cost = Integer.parseInt(st.nextToken());
			final int x = Integer.parseInt(st.nextToken());
			final int y = Integer.parseInt(st.nextToken());
			final int z = Integer.parseInt(st.nextToken());

			if (player.reduceAdena("Broadcast", cost, this, true))
			{
				player.enterObserverMode(x, y, z);
				player.sendPacket(new ItemList(player, false));
			}
		}

	}
    


	
	  public void ClasseCheck(Player activeChar)
	  {
	    Party plparty = activeChar.getParty();
	    for (Player player : plparty.getMembers()) {
	      if (player != null) {
	        if (player.getParty() != null)
	        {
	          if ((player.getClassId() == ClassId.GLADIATOR) || (player.getClassId() == ClassId.DUELIST)) {
	            activeChar.duelist_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.WARLORD) || (player.getClassId() == ClassId.DREADNOUGHT)) {
	            activeChar.dreadnought_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.PALADIN) || (player.getClassId() == ClassId.PHOENIX_KNIGHT) || (player.getClassId() == ClassId.DARK_AVENGER) || (player.getClassId() == ClassId.HELL_KNIGHT) || (player.getClassId() == ClassId.EVAS_TEMPLAR) || (player.getClassId() == ClassId.TEMPLE_KNIGHT) || (player.getClassId() == ClassId.SHILLIEN_KNIGHT) || (player.getClassId() == ClassId.SHILLIEN_TEMPLAR)) {
	            activeChar.tanker_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.ADVENTURER) || (player.getClassId() == ClassId.TREASURE_HUNTER) || (player.getClassId() == ClassId.WIND_RIDER) || (player.getClassId() == ClassId.PLAINS_WALKER) || (player.getClassId() == ClassId.GHOST_HUNTER) || (player.getClassId() == ClassId.ABYSS_WALKER)) {
	            activeChar.dagger_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.HAWKEYE) || (player.getClassId() == ClassId.SAGGITARIUS) || (player.getClassId() == ClassId.SILVER_RANGER) || (player.getClassId() == ClassId.MOONLIGHT_SENTINEL) || (player.getClassId() == ClassId.PHANTOM_RANGER) || (player.getClassId() == ClassId.GHOST_SENTINEL)) {
	            activeChar.archer_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.BISHOP) || (player.getClassId() == ClassId.CARDINAL) || (player.getClassId() == ClassId.SHILLIEN_ELDER) || (player.getClassId() == ClassId.SHILLIEN_SAINT)) {
	            activeChar.bs_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.ARCHMAGE) || (player.getClassId() == ClassId.SORCERER)) {
	            activeChar.archmage_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.SOULTAKER) || (player.getClassId() == ClassId.NECROMANCER)) {
	            activeChar.soultaker_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.MYSTIC_MUSE) || (player.getClassId() == ClassId.SPELLSINGER)) {
	            activeChar.mysticMuse_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.STORM_SCREAMER) || (player.getClassId() == ClassId.SPELLHOWLER)) {
	            activeChar.stormScreamer_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.TITAN) || (player.getClassId() == ClassId.DESTROYER)) {
	            activeChar.titan_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.TYRANT) || (player.getClassId() == ClassId.GRAND_KHAVATARI)) {
	            activeChar.grandKhauatari_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.ORC_SHAMAN) || (player.getClassId() == ClassId.OVERLORD)) {
	            activeChar.dominator_cont += 1;
	          }
	          if ((player.getClassId() == ClassId.DOOMCRYER) || (player.getClassId() == ClassId.WARCRYER)) {
	            activeChar.doomcryer_cont += 1;
	          }
	        }
	      }
	    }
	  }
	
	  
	  public void clean(Player player)
	  {
	    player.duelist_cont = 0;
	    player.dreadnought_cont = 0;
	    player.tanker_cont = 0;
	    player.dagger_cont = 0;
	    player.archer_cont = 0;
	    player.bs_cont = 0;
	    player.archmage_cont = 0;
	    player.soultaker_cont = 0;
	    player.mysticMuse_cont = 0;
	    player.stormScreamer_cont = 0;
	    player.titan_cont = 0;
	    player.grandKhauatari_cont = 0;
	    player.dominator_cont = 0;
	    player.doomcryer_cont = 0;
	  }
	  
	  public void HTML(Player player)
	  {
	    player.sendPacket(ActionFailed.STATIC_PACKET);
	    String filename = "data/html/mods/tournament/10006.htm";
	    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
	    html.setFile(filename);
	    html.replace("%objectId%", String.valueOf(getObjectId()));
	    if (Tournament.registered.size() == 0) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_0_over\" fore=\"L2UI_CH3.calculate1_0\">");
	    } else if (Tournament.registered.size() == 1) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_1_over\" fore=\"L2UI_CH3.calculate1_1\">");
	    } else if (Tournament.registered.size() == 2) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_2_over\" fore=\"L2UI_CH3.calculate1_2\">");
	    } else if (Tournament.registered.size() == 3) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_3_over\" fore=\"L2UI_CH3.calculate1_3\">");
	    } else if (Tournament.registered.size() == 4) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_4_over\" fore=\"L2UI_CH3.calculate1_4\">");
	    } else if (Tournament.registered.size() == 5) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_5_over\" fore=\"L2UI_CH3.calculate1_5\">");
	    } else if (Tournament.registered.size() == 6) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_6_over\" fore=\"L2UI_CH3.calculate1_6\">");
	    } else if (Tournament.registered.size() == 7) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_7_over\" fore=\"L2UI_CH3.calculate1_7\">");
	    } else if (Tournament.registered.size() == 8) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_8_over\" fore=\"L2UI_CH3.calculate1_8\">");
	    } else if (Tournament.registered.size() >= 9) {
	      html.replace("%2x2%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_9_over\" fore=\"L2UI_CH3.calculate1_9\">");
	    }
	    if (UnrealTournament.registered.size() == 0) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_0_over\" fore=\"L2UI_CH3.calculate1_0\">");
	    } else if (UnrealTournament.registered.size() == 1) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_1_over\" fore=\"L2UI_CH3.calculate1_1\">");
	    } else if (UnrealTournament.registered.size() == 2) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_2_over\" fore=\"L2UI_CH3.calculate1_2\">");
	    } else if (UnrealTournament.registered.size() == 3) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_3_over\" fore=\"L2UI_CH3.calculate1_3\">");
	    } else if (UnrealTournament.registered.size() == 4) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_4_over\" fore=\"L2UI_CH3.calculate1_4\">");
	    } else if (UnrealTournament.registered.size() == 5) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_5_over\" fore=\"L2UI_CH3.calculate1_5\">");
	    } else if (UnrealTournament.registered.size() == 6) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_6_over\" fore=\"L2UI_CH3.calculate1_6\">");
	    } else if (UnrealTournament.registered.size() == 7) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_7_over\" fore=\"L2UI_CH3.calculate1_7\">");
	    } else if (UnrealTournament.registered.size() == 8) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_8_over\" fore=\"L2UI_CH3.calculate1_8\">");
	    } else if (UnrealTournament.registered.size() >= 9) {
	      html.replace("%4x4%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_9_over\" fore=\"L2UI_CH3.calculate1_9\">");
	    }
	    if (XtremeTournament.registered.size() == 0) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_0_over\" fore=\"L2UI_CH3.calculate1_0\">");
	    } else if (XtremeTournament.registered.size() == 1) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_1_over\" fore=\"L2UI_CH3.calculate1_1\">");
	    } else if (XtremeTournament.registered.size() == 2) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_2_over\" fore=\"L2UI_CH3.calculate1_2\">");
	    } else if (XtremeTournament.registered.size() == 3) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_3_over\" fore=\"L2UI_CH3.calculate1_3\">");
	    } else if (XtremeTournament.registered.size() == 4) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_4_over\" fore=\"L2UI_CH3.calculate1_4\">");
	    } else if (XtremeTournament.registered.size() == 5) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_5_over\" fore=\"L2UI_CH3.calculate1_5\">");
	    } else if (XtremeTournament.registered.size() == 6) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_6_over\" fore=\"L2UI_CH3.calculate1_6\">");
	    } else if (XtremeTournament.registered.size() == 7) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_7_over\" fore=\"L2UI_CH3.calculate1_7\">");
	    } else if (XtremeTournament.registered.size() == 8) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_8_over\" fore=\"L2UI_CH3.calculate1_8\">");
	    } else if (XtremeTournament.registered.size() >= 9) {
	      html.replace("%9x9%", "<button value=\"\" action=\"\" width=32 height=32 back=\"L2UI_CH3.calculate1_9_over\" fore=\"L2UI_CH3.calculate1_9\">");
	    }
	    player.sendPacket(html);
	  }
	

}