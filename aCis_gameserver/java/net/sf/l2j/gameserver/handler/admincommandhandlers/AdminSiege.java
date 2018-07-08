package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.location.TowerSpawnLocation;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.zone.type.ClanHallZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SiegeInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles all siege commands
 */
public class AdminSiege implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_siege",
		"admin_add_attacker",
		"admin_add_defender",
		"admin_list_siege_clans",
		"admin_clear_siege_list",
		"admin_move_defenders",
		"admin_spawn_doors",
		"admin_endsiege",
		"admin_startsiege",
		"admin_setcastle",
		"admin_removecastle",
		"admin_clanhall",
		"admin_clanhallset",
		"admin_clanhalldel",
		"admin_clanhallopendoors",
		"admin_clanhallclosedoors",
		"admin_clanhallteleportself",
		"admin_reset_certificates"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		// Get castle
		Castle castle = null;
		ClanHall clanhall = null;
		
		if (command.startsWith("admin_clanhall"))
			clanhall = ClanHallManager.getInstance().getClanHallById(Integer.parseInt(st.nextToken()));
		else if (st.hasMoreTokens())
			castle = CastleManager.getInstance().getCastleByName(st.nextToken());
		
		if (clanhall == null && (castle == null || castle.getCastleId() < 0))
		{
			showCastleSelectPage(activeChar);
			return true;
		}
		
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
			player = (Player) target;
		
		if (castle != null)
		{
			if (command.equalsIgnoreCase("admin_add_attacker"))
			{
				if (player == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else
					castle.getSiege().registerAttacker(player);
			}
			else if (command.equalsIgnoreCase("admin_add_defender"))
			{
				if (player == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else
					castle.getSiege().registerDefender(player);
			}
			else if (command.equalsIgnoreCase("admin_clear_siege_list"))
			{
				castle.getSiege().clearAllClans();
			}
			else if (command.equalsIgnoreCase("admin_endsiege"))
			{
				castle.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_siege_clans"))
			{
				activeChar.sendPacket(new SiegeInfo(castle));
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_defenders"))
			{
				activeChar.sendPacket(SystemMessage.sendString("Not implemented yet."));
			}
			else if (command.equalsIgnoreCase("admin_setcastle"))
			{
				if (player == null || player.getClan() == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else if (player.getClan().hasCastle())
					activeChar.sendMessage(player.getName() + "'s clan already owns a castle.");
				else
					castle.setOwner(player.getClan());
			}
			else if (command.equalsIgnoreCase("admin_removecastle"))
			{
				Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
				if (clan != null)
					castle.removeOwner(clan);
				else
					activeChar.sendMessage("Unable to remove castle for this clan.");
			}
			else if (command.equalsIgnoreCase("admin_spawn_doors"))
			{
				castle.spawnDoors(false);
			}
			else if (command.equalsIgnoreCase("admin_startsiege"))
			{
				castle.getSiege().startSiege();
			}
			else if (command.equalsIgnoreCase("admin_reset_certificates"))
			{
				castle.setLeftCertificates(300, true);
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/admin/castle.htm");
			html.replace("%castleName%", castle.getName());
			html.replace("%circletId%", castle.getCircletId());
			html.replace("%artifactId%", castle.getArtifacts().toString());
			html.replace("%ticketsNumber%", castle.getTickets().size());
			html.replace("%droppedTicketsNumber%", castle.getDroppedTickets().size());
			html.replace("%npcsNumber%", castle.getRelatedNpcIds().size());
			html.replace("%certificates%", castle.getLeftCertificates());
			
			final StringBuilder sb = new StringBuilder();
			
			// Feed Control Tower infos.
			for (TowerSpawnLocation spawn : castle.getControlTowers())
			{
				final String teleLoc = spawn.toString().replaceAll(",", "");
				StringUtil.append(sb, "<a action=\"bypass -h admin_move_to ", teleLoc, "\">", teleLoc, "</a><br1>");
			}
			
			html.replace("%ct%", sb.toString());
			
			// Cleanup the sb to reuse it.
			sb.setLength(0);
			
			// Feed Flame Tower infos.
			for (TowerSpawnLocation spawn : castle.getFlameTowers())
			{
				final String teleLoc = spawn.toString().replaceAll(",", "");
				StringUtil.append(sb, "<a action=\"bypass -h admin_move_to ", teleLoc, "\">", teleLoc, "</a><br1>");
			}
			
			html.replace("%ft%", sb.toString());
			
			activeChar.sendPacket(html);
		}
		else if (clanhall != null)
		{
			if (command.equalsIgnoreCase("admin_clanhallset"))
			{
				if (player == null || player.getClan() == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
					activeChar.sendMessage("This ClanHall isn't free!");
				else if (!player.getClan().hasHideout())
				{
					ClanHallManager.getInstance().setOwner(clanhall.getId(), player.getClan());
					if (AuctionManager.getInstance().getAuction(clanhall.getId()) != null)
						AuctionManager.getInstance().getAuction(clanhall.getId()).deleteAuctionFromDB();
				}
				else
					activeChar.sendMessage("You have already a ClanHall!");
			}
			else if (command.equalsIgnoreCase("admin_clanhalldel"))
			{
				if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
				{
					ClanHallManager.getInstance().setFree(clanhall.getId());
					AuctionManager.getInstance().initNPC(clanhall.getId());
				}
				else
					activeChar.sendMessage("This ClanHall is already Free!");
			}
			else if (command.equalsIgnoreCase("admin_clanhallopendoors"))
			{
				clanhall.openCloseDoors(true);
			}
			else if (command.equalsIgnoreCase("admin_clanhallclosedoors"))
			{
				clanhall.openCloseDoors(false);
			}
			else if (command.equalsIgnoreCase("admin_clanhallteleportself"))
			{
				ClanHallZone zone = clanhall.getZone();
				if (zone != null)
					activeChar.teleToLocation(zone.getSpawnLoc(), 0);
			}
			
			final Clan owner = ClanTable.getInstance().getClan(clanhall.getOwnerId());
			
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/admin/clanhall.htm");
			html.replace("%clanhallName%", clanhall.getName());
			html.replace("%clanhallId%", clanhall.getId());
			html.replace("%clanhallOwner%", (owner == null) ? "None" : owner.getName());
			activeChar.sendPacket(html);
		}
		return true;
	}
	
	private static void showCastleSelectPage(Player activeChar)
	{
		int i = 0;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/castles.htm");
		
		final StringBuilder sb = new StringBuilder();
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null)
			{
				StringUtil.append(sb, "<td fixwidth=90><a action=\"bypass -h admin_siege ", castle.getName(), "\">", castle.getName(), "</a></td>");
				i++;
			}
			
			if (i > 2)
			{
				sb.append("</tr><tr>");
				i = 0;
			}
		}
		html.replace("%castles%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		i = 0;
		
		for (ClanHall clanhall : ClanHallManager.getInstance().getClanHalls().values())
		{
			if (clanhall != null)
			{
				StringUtil.append(sb, "<td fixwidth=134><a action=\"bypass -h admin_clanhall ", clanhall.getId(), "\">", clanhall.getName(), "</a></td>");
				i++;
			}
			
			if (i > 1)
			{
				sb.append("</tr><tr>");
				i = 0;
			}
		}
		html.replace("%clanhalls%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		i = 0;
		
		for (ClanHall clanhall : ClanHallManager.getInstance().getFreeClanHalls().values())
		{
			if (clanhall != null)
			{
				StringUtil.append(sb, "<td fixwidth=134><a action=\"bypass -h admin_clanhall ", clanhall.getId(), "\">", clanhall.getName(), "</a></td>");
				i++;
			}
			
			if (i > 1)
			{
				sb.append("</tr><tr>");
				i = 0;
			}
		}
		html.replace("%freeclanhalls%", sb.toString());
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}