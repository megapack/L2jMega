/*
 * Copyright (C) 2004-2014 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class Ranking implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"pvp",
		"pks",
		"clan",
		"ranking"
	};

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("ranking"))
			showRankingHtml(activeChar);      
		
		else if (command.equals("pvp"))
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(5);
			StringBuilder tb = new StringBuilder("<html><head><title>Ranking PvP</title></head><body><center><img src=\"l2ui_ch3.herotower_deco\" width=256 height=32></center><br1><table width=290><tr><td><center>Rank</center></td><td><center>Character</center></td><td><center>Pvp's</center></td><td><center>Status</center></td></tr>");
	        try (Connection con = L2DatabaseFactory.getInstance().getConnection())
            {
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pvpkills,online FROM characters WHERE pvpkills>0 AND accesslevel=0 order by pvpkills desc limit 15");
				ResultSet result = statement.executeQuery();
				int pos = 0;

				while (result.next())
				{
					String pvps = result.getString("pvpkills");
					String name = result.getString("char_name");
					pos += 1;
					String statu = result.getString("online");
					String status;
					
					if (statu.equals("1"))
						status = "<font color=00FF00>Online</font>";
					else
						status = "<font color=FF0000>Offline</font>";
					
					tb.append("<tr><td><center><font color =\"AAAAAA\">" +pos+ "</td><td><center><font color=00FFFF>" +name+ "</font></center></td><td><center>" +pvps+ "</center></td><td><center>" +status+ "</center></td></tr>");
				}
				statement.close();
				result.close();
			}
	        catch (Exception e)
			{
				//_log.log(Level.WARNING, "ranking (status): could not load statistics informations" + e.getMessage(), e);
			}        
			tb.append("</body></html>");

			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
		}
		else if (command.equals("pks"))
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(5);
			StringBuilder tb = new StringBuilder("<html><head><title>Ranking PK</title></head><body><center><img src=\"l2ui_ch3.herotower_deco\" width=256 height=32></center><br1><table width=290><tr><td><center>Rank</center></td><td><center>Character</center></td><td><center>Pk's</center></td><td><center>Status</center></td></tr>");
	        try (Connection con = L2DatabaseFactory.getInstance().getConnection())
            {
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pkkills,online FROM characters WHERE pvpkills>0 AND accesslevel=0 order by pkkills desc limit 15");
				ResultSet result = statement.executeQuery();
				int pos = 0;

				while (result.next())
				{
					String pks = result.getString("pkkills");
					String name = result.getString("char_name");
					pos += 1;
					String statu = result.getString("online");
					String status;
					
					if (statu.equals("1"))
						status = "<font color=00FF00>Online</font>";
					else
						status = "<font color=FF0000>Offline</font>";
					
					tb.append("<tr><td><center><font color =\"AAAAAA\">" +pos+ "</td><td><center><font color=00FFFF>" +name+ "</font></center></td><td><center>" +pks+ "</center></td><td><center>" +status+ "</center></td></tr>");
				}
				statement.close();
				result.close();
			}
	        catch (Exception e)
			{
				//_log.log(Level.WARNING, "ranking (status): could not load statistics informations" + e.getMessage(), e);
			}        
			tb.append("</body></html>");

			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
		}
		else if (command.equals("clan"))
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(5);
			StringBuilder tb = new StringBuilder("<html><head><title>Clan Ranking</title></head><body><center><img src=\"l2ui_ch3.herotower_deco\" width=256 height=32></center><br1><table width=290><tr><td><center>Rank</center></td><td><center>Level</center></td><td><center>Clan Name</center></td><td><center>Reputation</center></td></tr>");
	        try (Connection con = L2DatabaseFactory.getInstance().getConnection())
            {
				PreparedStatement statement = con.prepareStatement("SELECT clan_name,clan_level,reputation_score FROM clan_data WHERE clan_level>0 order by reputation_score desc limit 15");
				ResultSet result = statement.executeQuery();
				int pos = 0;

				while (result.next())
				{
					String clan_name = result.getString("clan_name");
					String clan_level = result.getString("clan_level");
					String clan_score = result.getString("reputation_score");
					pos += 1;

					tb.append("<tr><td><center><font color =\"AAAAAA\">" +pos+ "</center></td><td><center>" +clan_level+"</center></td><td><center><font color=00FFFF>" +clan_name+ "</font></center></td><td><center><font color=00FF00>" +clan_score+ "</font></center></td></tr>");
				}
				statement.close();
				result.close();
			}
	        catch (Exception e)
			{
				//_log.log(Level.WARNING, "ranking (status): could not load statistics informations" + e.getMessage(), e);
			}        
			tb.append("</body></html>");

			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
		}

		return true;
	}

	private static void showRankingHtml(Player activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/Ranking.htm"); 
		activeChar.sendPacket(html);
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}