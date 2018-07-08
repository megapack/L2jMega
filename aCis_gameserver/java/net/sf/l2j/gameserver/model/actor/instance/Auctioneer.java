package net.sf.l2j.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Auction;
import net.sf.l2j.gameserver.model.entity.Auction.Bidder;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public final class Auctioneer extends Folk
{
	private static final int PAGE_LIMIT = 15;
	
	private final Map<Integer, Auction> _pendingAuctions = new ConcurrentHashMap<>();
	
	public Auctioneer(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		
		final String actualCommand = st.nextToken();
		final String val = (st.hasMoreTokens()) ? st.nextToken() : "";
		
		// Only a few actions are possible for clanless people.
		if (actualCommand.equalsIgnoreCase("list"))
		{
			showAuctionsList(val, player);
			return;
		}
		else if (actualCommand.equalsIgnoreCase("bidding"))
		{
			if (val.isEmpty())
				return;
			
			try
			{
				final Auction auction = AuctionManager.getInstance().getAuction(Integer.parseInt(val));
				if (auction != null)
				{
					final ClanHall ch = ClanHallManager.getInstance().getClanHallById(auction.getItemId());
					final long remainingTime = auction.getEndDate() - System.currentTimeMillis();
					
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/auction/AgitAuctionInfo.htm");
					html.replace("%AGIT_NAME%", auction.getItemName());
					html.replace("%OWNER_PLEDGE_NAME%", auction.getSellerClanName());
					html.replace("%OWNER_PLEDGE_MASTER%", auction.getSellerName());
					html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
					html.replace("%AGIT_LEASE%", ch.getLease());
					html.replace("%AGIT_LOCATION%", ch.getLocation());
					html.replace("%AGIT_AUCTION_END%", new SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.getEndDate()));
					html.replace("%AGIT_AUCTION_REMAIN%", (remainingTime / 3600000) + " hours " + ((remainingTime / 60000) % 60) + " minutes");
					html.replace("%AGIT_AUCTION_MINBID%", auction.getStartingBid());
					html.replace("%AGIT_AUCTION_COUNT%", auction.getBidders().size());
					html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_list");
					html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + getObjectId() + "_bidlist " + auction.getId());
					html.replace("%AGIT_LINK_RE%", "bypass -h npc_" + getObjectId() + "_bid1 " + auction.getId());
					player.sendPacket(html);
				}
			}
			catch (Exception e)
			{
			}
			return;
		}
		else if (actualCommand.equalsIgnoreCase("location"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/auction/location.htm");
			html.replace("%location%", MapRegionData.getInstance().getClosestTownName(player.getX(), player.getY()));
			html.replace("%LOCATION%", getPictureName(player));
			html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
			player.sendPacket(html);
			return;
		}
		else if (actualCommand.equalsIgnoreCase("start"))
		{
			showChatWindow(player);
			return;
		}
		// Clanless or clan members without enough power are kicked directly.
		else
		{
			if (player.getClan() == null || !((player.getClanPrivileges() & Clan.CP_CH_AUCTION) == Clan.CP_CH_AUCTION))
			{
				showAuctionsList("1", player); // Force to display page 1.
				player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_IN_AUCTION);
				return;
			}
			
			if (actualCommand.equalsIgnoreCase("bid"))
			{
				if (val.isEmpty())
					return;
				
				try
				{
					final int bid = (st.hasMoreTokens()) ? Math.min(Integer.parseInt(st.nextToken()), Integer.MAX_VALUE) : 0;
					
					AuctionManager.getInstance().getAuction(Integer.parseInt(val)).setBid(player, bid);
				}
				catch (Exception e)
				{
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bid1"))
			{
				if (val.isEmpty())
					return;
				
				if (player.getClan() == null || player.getClan().getLevel() < 2)
				{
					showAuctionsList("1", player); // Force to display page 1.
					player.sendPacket(SystemMessageId.AUCTION_ONLY_CLAN_LEVEL_2_HIGHER);
					return;
				}
				
				if (player.getClan().hasHideout())
				{
					showAuctionsList("1", player); // Force to display page 1.
					player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_IN_AUCTION);
					return;
				}
				
				try
				{
					if ((player.getClan().getAuctionBiddedAt() > 0 && player.getClan().getAuctionBiddedAt() != Integer.parseInt(val)))
					{
						showAuctionsList("1", player); // Force to display page 1.
						player.sendPacket(SystemMessageId.ALREADY_SUBMITTED_BID);
						return;
					}
					
					final Auction auction = AuctionManager.getInstance().getAuction(Integer.parseInt(val));
					int minimumBid = auction.getHighestBidderMaxBid();
					if (minimumBid == 0)
						minimumBid = auction.getStartingBid();
					
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/auction/AgitBid1.htm");
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_bidding " + val);
					html.replace("%PLEDGE_ADENA%", player.getClan().getWarehouse().getAdena());
					html.replace("%AGIT_AUCTION_MINBID%", minimumBid);
					html.replace("npc_%objectId%_bid", "npc_" + getObjectId() + "_bid " + val);
					player.sendPacket(html);
				}
				catch (Exception e)
				{
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bidlist"))
			{
				try
				{
					int auctionId = 0;
					if (val.isEmpty())
					{
						if (player.getClan().getAuctionBiddedAt() <= 0)
							return;
						
						auctionId = player.getClan().getAuctionBiddedAt();
					}
					else
						auctionId = Integer.parseInt(val);
					
					final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					final Collection<Bidder> bidders = AuctionManager.getInstance().getAuction(auctionId).getBidders().values();
					
					final StringBuilder sb = new StringBuilder(bidders.size() * 150);
					for (Bidder bidder : bidders)
						StringUtil.append(sb, "<tr><td width=90 align=center>", bidder.getClanName(), "</td><td width=90 align=center>", bidder.getName(), "</td><td width=90 align=center>", sdf.format(bidder.getTimeBid().getTime()), "</td></tr>");
					
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/auction/AgitBidderList.htm");
					html.replace("%AGIT_LIST%", sb.toString());
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_bidding " + auctionId);
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
				}
				catch (Exception e)
				{
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("selectedItems"))
			{
				showSelectedItems(player);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("cancelBid"))
			{
				try
				{
					final int bid = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).getBidders().get(player.getClanId()).getBid();
					
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/auction/AgitBidCancel.htm");
					html.replace("%AGIT_BID%", bid);
					html.replace("%AGIT_BID_REMAIN%", (int) (bid * 0.9));
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
				}
				catch (Exception e)
				{
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelBid"))
			{
				final Auction auction = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
				if (auction != null)
				{
					auction.cancelBid(player.getClanId());
					player.sendPacket(SystemMessageId.CANCELED_BID);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("cancelAuction"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/auction/AgitSaleCancel.htm");
				html.replace("%AGIT_DEPOSIT%", ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease());
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelAuction"))
			{
				final Auction auction = AuctionManager.getInstance().getAuction(player.getClan().getHideoutId());
				if (auction != null)
				{
					auction.cancelAuction();
					player.sendPacket(SystemMessageId.CANCELED_BID);
				}
				showChatWindow(player);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("sale"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/auction/AgitSale1.htm");
				html.replace("%AGIT_DEPOSIT%", ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease());
				html.replace("%AGIT_PLEDGE_ADENA%", player.getClan().getWarehouse().getAdena());
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("rebid"))
			{
				final Auction auction = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
				if (auction != null)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/auction/AgitBid2.htm");
					html.replace("%AGIT_AUCTION_BID%", auction.getBidders().get(player.getClanId()).getBid());
					html.replace("%AGIT_AUCTION_MINBID%", auction.getStartingBid());
					html.replace("%AGIT_AUCTION_END%", new SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.getEndDate()));
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
					html.replace("npc_%objectId%_bid1", "npc_" + getObjectId() + "_bid1 " + auction.getId());
					player.sendPacket(html);
				}
				return;
			}
			/* Those bypasses check if CWH got enough adenas (case of sale auction type) */
			else
			{
				if (player.getClan().getWarehouse().getAdena() < ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease())
				{
					showSelectedItems(player);
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH);
					return;
				}
				
				if (actualCommand.equalsIgnoreCase("auction"))
				{
					if (val.isEmpty())
						return;
					
					try
					{
						final int days = Integer.parseInt(val);
						final int bid = (st.hasMoreTokens()) ? Math.min(Integer.parseInt(st.nextToken()), Integer.MAX_VALUE) : 0;
						final ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
						
						final Auction auction = new Auction(player.getClan().getHideoutId(), player.getClan(), days * 86400000L, bid, ch.getName());
						
						_pendingAuctions.put(auction.getId(), auction);
						
						final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile("data/html/auction/AgitSale3.htm");
						html.replace("%x%", val);
						html.replace("%AGIT_AUCTION_END%", new SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.getEndDate()));
						html.replace("%AGIT_AUCTION_MINBID%", auction.getStartingBid());
						html.replace("%AGIT_AUCTION_MIN%", auction.getStartingBid());
						html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale2");
						html.replace("%objectId%", getObjectId());
						player.sendPacket(html);
					}
					catch (Exception e)
					{
					}
					return;
				}
				else if (actualCommand.equalsIgnoreCase("confirmAuction"))
				{
					final int chId = player.getClan().getHideoutId();
					if (chId <= 0)
						return;
					
					final Auction auction = _pendingAuctions.get(chId);
					if (auction == null)
						return;
					
					if (Auction.takeItem(player, ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()))
					{
						auction.confirmAuction();
						
						_pendingAuctions.remove(chId);
						
						showSelectedItems(player);
						player.sendPacket(SystemMessageId.REGISTERED_FOR_CLANHALL);
					}
					return;
				}
				else if (actualCommand.equalsIgnoreCase("sale2"))
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/auction/AgitSale2.htm");
					html.replace("%AGIT_LAST_PRICE%", ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease());
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale");
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
					return;
				}
			}
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/auction/auction.htm");
		html.replace("%objectId%", getObjectId());
		html.replace("%npcId%", getNpcId());
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		if (val == 0)
			return;
		
		super.showChatWindow(player, val);
	}
	
	private void showAuctionsList(String val, Player player)
	{
		// Retrieve the whole auctions list.
		List<Auction> auctions = AuctionManager.getInstance().getAuctions();
		
		final int page = (val.isEmpty()) ? 1 : Integer.parseInt(val);
		final int max = MathUtil.countPagesNumber(auctions.size(), PAGE_LIMIT);
		
		// Cut auctions list up to page number.
		auctions = auctions.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, auctions.size()));
		
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		final StringBuilder sb = new StringBuilder(4000);
		
		sb.append("<table width=280>");
		
		// Auctions feeding.
		for (Auction auction : auctions)
			StringUtil.append(sb, "<tr><td><font color=\"aaaaff\">", ClanHallManager.getInstance().getClanHallById(auction.getItemId()).getLocation(), "</font></td><td><font color=\"ffffaa\"><a action=\"bypass -h npc_", getObjectId(), "_bidding ", auction.getId(), "\">", auction.getItemName(), " [", auction.getBidders().size(), "]</a></font></td><td>", sdf.format(auction.getEndDate()), "</td><td><font color=\"aaffff\">", auction.getStartingBid(), "</font></td></tr>");
		
		sb.append("</table><table width=280><tr>");
		
		// Page feeding.
		for (int j = 1; j <= max; j++)
			StringUtil.append(sb, "<td><center><a action=\"bypass -h npc_", getObjectId(), "_list ", j, "\"> Page ", j, " </a></center></td>");
		
		sb.append("</tr></table>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/auction/AgitAuctionList.htm");
		html.replace("%AGIT_LIST%", sb.toString());
		html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
		player.sendPacket(html);
		return;
	}
	
	private void showSelectedItems(Player player)
	{
		final Clan clan = player.getClan();
		if (clan == null)
			return;
		
		if (!clan.hasHideout() && clan.getAuctionBiddedAt() > 0)
		{
			final Auction auction = AuctionManager.getInstance().getAuction(clan.getAuctionBiddedAt());
			if (auction != null)
			{
				final ClanHall ch = ClanHallManager.getInstance().getClanHallById(auction.getItemId());
				final long remainingTime = auction.getEndDate() - System.currentTimeMillis();
				
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/auction/AgitBidInfo.htm");
				html.replace("%AGIT_NAME%", auction.getItemName());
				html.replace("%OWNER_PLEDGE_NAME%", auction.getSellerClanName());
				html.replace("%OWNER_PLEDGE_MASTER%", auction.getSellerName());
				html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
				html.replace("%AGIT_LEASE%", ch.getLease());
				html.replace("%AGIT_LOCATION%", ch.getLocation());
				html.replace("%AGIT_AUCTION_END%", new SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.getEndDate()));
				html.replace("%AGIT_AUCTION_REMAIN%", (remainingTime / 3600000) + " hours " + ((remainingTime / 60000) % 60) + " minutes");
				html.replace("%AGIT_AUCTION_MINBID%", auction.getStartingBid());
				html.replace("%AGIT_AUCTION_MYBID%", auction.getBidders().get(player.getClanId()).getBid());
				html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
				html.replace("%objectId%", getObjectId());
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
				player.sendPacket(html);
			}
			return;
		}
		else if (AuctionManager.getInstance().getAuction(clan.getHideoutId()) != null)
		{
			final Auction auction = AuctionManager.getInstance().getAuction(clan.getHideoutId());
			if (auction != null)
			{
				final ClanHall ch = ClanHallManager.getInstance().getClanHallById(auction.getItemId());
				final long remainingTime = auction.getEndDate() - System.currentTimeMillis();
				
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/auction/AgitSaleInfo.htm");
				html.replace("%AGIT_NAME%", auction.getItemName());
				html.replace("%AGIT_OWNER_PLEDGE_NAME%", auction.getSellerClanName());
				html.replace("%OWNER_PLEDGE_MASTER%", auction.getSellerName());
				html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
				html.replace("%AGIT_LEASE%", ch.getLease());
				html.replace("%AGIT_LOCATION%", ch.getLocation());
				html.replace("%AGIT_AUCTION_END%", new SimpleDateFormat("dd-MM-yyyy HH:mm").format(auction.getEndDate()));
				html.replace("%AGIT_AUCTION_REMAIN%", (remainingTime / 3600000) + " hours " + ((remainingTime / 60000) % 60) + " minutes");
				html.replace("%AGIT_AUCTION_MINBID%", auction.getStartingBid());
				html.replace("%AGIT_AUCTION_BIDCOUNT%", auction.getBidders().size());
				html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
				html.replace("%id%", auction.getId());
				html.replace("%objectId%", getObjectId());
				player.sendPacket(html);
			}
			return;
		}
		else if (clan.hasHideout())
		{
			final ClanHall ch = ClanHallManager.getInstance().getClanHallById(clan.getHideoutId());
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/auction/AgitInfo.htm");
			html.replace("%AGIT_NAME%", ch.getName());
			html.replace("%AGIT_OWNER_PLEDGE_NAME%", clan.getName());
			html.replace("%OWNER_PLEDGE_MASTER%", clan.getLeaderName());
			html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
			html.replace("%AGIT_LEASE%", ch.getLease());
			html.replace("%AGIT_LOCATION%", ch.getLocation());
			html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
			return;
		}
		else if (!clan.hasHideout())
		{
			showAuctionsList("1", player); // Force to display page 1.
			player.sendPacket(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR);
			return;
		}
	}
	
	private static String getPictureName(Player plyr)
	{
		switch (MapRegionData.getInstance().getMapRegion(plyr.getX(), plyr.getY()))
		{
			case 5:
				return "GLUDIO";
			
			case 6:
				return "GLUDIN";
			
			case 7:
				return "DION";
			
			case 8:
				return "GIRAN";
			
			case 14:
				return "RUNE";
			
			case 15:
				return "GODARD";
			
			case 16:
				return "SCHUTTGART";
			
			default:
				return "ADEN";
		}
	}
}