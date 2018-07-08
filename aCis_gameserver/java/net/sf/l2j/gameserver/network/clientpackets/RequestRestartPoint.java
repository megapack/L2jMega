package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.events.teamvsteam.TvTEvent;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHall.ClanHallFunction;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.Siege.SiegeSide;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.pledge.Clan;

public final class RequestRestartPoint extends L2GameClientPacket
{
	protected static final Location JAIL_LOCATION = new Location(-114356, -249645, -2984);
	protected static final Location GIRAN_LOCATION_1 = new Location(83416, 149017, -3408);
	protected static final Location GIRAN_LOCATION_2 = new Location(82687, 148473, -3472);
	protected static final Location GIRAN_LOCATION_3 = new Location(81544, 148036, -3472);
	protected static final Location GIRAN_LOCATION_4 = new Location(81422, 149226, -3472);
	
	protected int _requestType;
	
	@Override
	protected void readImpl()
	{
		_requestType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isFakeDeath())
		{
			player.stopFakeDeath(true);
			return;
		}
		
		if (!player.isDead())
			return;
		
	    if (TvTEvent.isPlayerParticipant(player.getObjectId())) {
	        return;
	      }	
		
		// Schedule a respawn delay if player is part of a clan registered in an active siege.
		if (player.getClan() != null)
		{
			final Siege siege = CastleManager.getInstance().getActiveSiege(player);
			if (siege != null && siege.checkSide(player.getClan(), SiegeSide.ATTACKER))
			{
				ThreadPool.schedule(() -> portPlayer(player), Config.ATTACKERS_RESPAWN_DELAY);
				return;
			}
		}
		
		portPlayer(player);
	}
	
	/**
	 * Teleport the {@link Player} to the associated {@link Location}, based on _requestType.
	 * @param player : The player set as parameter.
	 */
	private void portPlayer(Player player)
	{
		final Clan clan = player.getClan();
		
		Location loc = null;
		
		// Enforce type.
		if (player.isInJail())
			_requestType = 27;
		else if (player.isFestivalParticipant())
			_requestType = 4;
		
		// To clanhall.
		if (_requestType == 1)
		{
			if (clan == null || !clan.hasHideout())
				return;
			
			loc = MapRegionData.getInstance().getLocationToTeleport(player, TeleportType.CLAN_HALL);
			
			final ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if (ch != null)
			{
				final ClanHallFunction function = ch.getFunction(ClanHall.FUNC_RESTORE_EXP);
				if (function != null)
					player.restoreExp(function.getLvl());
			}
		}
		// To castle.
		else if (_requestType == 2)
		{
			final Siege siege = CastleManager.getInstance().getActiveSiege(player);
			if (siege != null)
			{
				final SiegeSide side = siege.getSide(clan);
				if (side == SiegeSide.DEFENDER || side == SiegeSide.OWNER)
					loc = MapRegionData.getInstance().getLocationToTeleport(player, TeleportType.CASTLE);
				else if (side == SiegeSide.ATTACKER)
					loc = MapRegionData.getInstance().getLocationToTeleport(player, TeleportType.TOWN);
				else
					return;
			}
			else
			{
				if (clan == null || !clan.hasCastle())
					return;
				
				loc = MapRegionData.getInstance().getLocationToTeleport(player, TeleportType.CASTLE);
			}
		}
		// To siege flag.
		else if (_requestType == 3)
			loc = MapRegionData.getInstance().getLocationToTeleport(player, TeleportType.SIEGE_FLAG);
		// Fixed.
	      else if (RequestRestartPoint.this._requestType == 4)
	      {
	        if (player.isGM())
	        {
	          loc = player.getPosition();
	        }
	        else
	        {
	          int rnd = 1 + (int)(Math.random() * 4.0D);
	          if (rnd == 1) {
	            loc = RequestRestartPoint.GIRAN_LOCATION_1;
	          } else if (rnd == 2) {
	            loc = RequestRestartPoint.GIRAN_LOCATION_2;
	          } else if (rnd == 3) {
	            loc = RequestRestartPoint.GIRAN_LOCATION_3;
	          } else if (rnd == 4) {
	            loc = RequestRestartPoint.GIRAN_LOCATION_4;
	          } else {
	            loc = RequestRestartPoint.GIRAN_LOCATION_1;
	          }
	        }
	      }
	      else if (RequestRestartPoint.this._requestType == 27)
	      {
	        if (!player.isInJail()) {
	          return;
	        }
	        loc = RequestRestartPoint.JAIL_LOCATION;
	      }
	      else
	      {
	        loc = MapRegionData.getInstance().getLocationToTeleport(player, MapRegionData.TeleportType.TOWN);
	      }
	      player.setIsIn7sDungeon(false);
	      if (player.isDead()) {
	        player.doRevive();
	      }
	      player.teleToLocation(loc, 100);
	    }
	  }