package net.sf.l2j.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public class ClanTable
{
	private static final Logger LOG = Logger.getLogger(ClanTable.class.getName());
	
	private final Map<Integer, Clan> _clans = new ConcurrentHashMap<>();
	
	protected ClanTable()
	{
		// Load all clans.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM clan_data");
			ResultSet rs = ps.executeQuery();
			
			while (rs.next())
			{
				int clanId = rs.getInt("clan_id");
				
				Clan clan = new Clan(clanId, rs.getInt("leader_id"));
				_clans.put(clanId, clan);
				
				clan.setName(rs.getString("clan_name"));
				clan.setLevel(rs.getInt("clan_level"));
				clan.setCastle(rs.getInt("hasCastle"));
				clan.setAllyId(rs.getInt("ally_id"));
				clan.setAllyName(rs.getString("ally_name"));
				
				// If ally expire time has been reached while server was off, keep it to 0.
				final long allyExpireTime = rs.getLong("ally_penalty_expiry_time");
				if (allyExpireTime > System.currentTimeMillis())
					clan.setAllyPenaltyExpiryTime(allyExpireTime, rs.getInt("ally_penalty_type"));
				
				// If character expire time has been reached while server was off, keep it to 0.
				final long charExpireTime = rs.getLong("char_penalty_expiry_time");
				if (charExpireTime + Config.ALT_CLAN_JOIN_DAYS * 86400000L > System.currentTimeMillis())
					clan.setCharPenaltyExpiryTime(charExpireTime);
				
				clan.setDissolvingExpiryTime(rs.getLong("dissolving_expiry_time"));
				
				clan.setCrestId(rs.getInt("crest_id"));
				clan.setCrestLargeId(rs.getInt("crest_large_id"));
				clan.setAllyCrestId(rs.getInt("ally_crest_id"));
				
				clan.addReputationScore(rs.getInt("reputation_score"));
				clan.setAuctionBiddedAt(rs.getInt("auction_bid_at"));
				clan.setNewLeaderId(rs.getInt("new_leader_id"), false);
				
				if (clan.getDissolvingExpiryTime() != 0)
					scheduleRemoveClan(clan);
				
				clan.setNoticeEnabled(rs.getBoolean("enabled"));
				clan.setNotice(rs.getString("notice"));
				
				clan.setIntroduction(rs.getString("introduction"), false);
			}
			rs.close();
			ps.close();
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "Error restoring ClanTable: ", e);
		}
		LOG.info("Loaded " + _clans.size() + " clans.");
		
		// Check for non-existing alliances.
		allianceCheck();
		
		// Restore clan wars.
		restoreWars();
		
		// Refresh clans ladder.
		refreshClansLadder(false);
	}
	
	public Collection<Clan> getClans()
	{
		return _clans.values();
	}
	
	/**
	 * @param clanId : The id of the clan to retrieve.
	 * @return the clan object based on id.
	 */
	public Clan getClan(int clanId)
	{
		return _clans.get(clanId);
	}
	
	public Clan getClanByName(String clanName)
	{
		for (Clan clan : _clans.values())
		{
			if (clan.getName().equalsIgnoreCase(clanName))
				return clan;
		}
		return null;
	}
	
	/**
	 * Creates a new clan and store clan info to database
	 * @param player The player who requested the clan creation.
	 * @param clanName The name of the clan player wants.
	 * @return null if checks fail, or L2Clan
	 */
	public Clan createClan(Player player, String clanName)
	{
		if (player == null)
			return null;
		
		if (player.getLevel() < 10)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
			return null;
		}
		
		if (player.getClanId() != 0)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CREATE_CLAN);
			return null;
		}
		
		if (System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return null;
		}
		
		if (!StringUtil.isAlphaNumeric(clanName))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INVALID);
			return null;
		}
		
		if (clanName.length() < 2 || clanName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_LENGTH_INCORRECT);
			return null;
		}
		
		if (getClanByName(clanName) != null)
		{
			// clan name is already taken
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(clanName));
			return null;
		}
		
		Clan clan = new Clan(IdFactory.getInstance().getNextId(), clanName);
		ClanMember leader = new ClanMember(clan, player);
		clan.setLeader(leader);
		leader.setPlayerInstance(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(ClanMember.calculatePledgeClass(player));
		player.setClanPrivileges(Clan.CP_ALL);
		
		_clans.put(clan.getClanId(), clan);
		
		player.sendPacket(new PledgeShowMemberListAll(clan, 0));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(SystemMessageId.CLAN_CREATED);
		return clan;
	}
	
	/**
	 * Instantly delete related clan. Run different clanId related queries, remove clan from _clans, inform clan members, delete clans from pending sieges.
	 * @param clan : The clan to delete.
	 */
	public void destroyClan(Clan clan)
	{
		if (!_clans.containsKey(clan.getClanId()))
			return;
		
		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
		
		// Drop the clan from all sieges. The related mySQL query is handled below.
		for (Castle castle : CastleManager.getInstance().getCastles())
			castle.getSiege().getRegisteredClans().keySet().removeIf(c -> c.getClanId() == clan.getClanId());
		
		// Drop all items from clan warehouse.
		clan.getWarehouse().destroyAllItems("ClanRemove", (clan.getLeader() == null) ? null : clan.getLeader().getPlayerInstance(), null);
		
		for (ClanMember member : clan.getMembers())
			clan.removeClanMember(member.getObjectId(), 0);
		
		// Numerous mySQL queries.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			ps.setInt(1, clan.getClanId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
			ps.setInt(1, clan.getClanId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			ps.setInt(1, clan.getClanId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			ps.setInt(1, clan.getClanId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?");
			ps.setInt(1, clan.getClanId());
			ps.setInt(2, clan.getClanId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");
			ps.setInt(1, clan.getClanId());
			ps.execute();
			ps.close();
			
			if (clan.getCastleId() != 0)
			{
				ps = con.prepareStatement("UPDATE castle SET taxPercent = 0 WHERE id = ?");
				ps.setInt(1, clan.getCastleId());
				ps.execute();
				ps.close();
			}
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "Error removing clan from DB.", e);
		}
		
		// Release clan id.
		IdFactory.getInstance().releaseId(clan.getClanId());
		
		// Remove the clan from the map.
		_clans.remove(clan.getClanId());
	}
	
	public void scheduleRemoveClan(final Clan clan)
	{
		if (clan == null)
			return;
		
		ThreadPool.schedule(() ->
		{
			if (clan.getDissolvingExpiryTime() != 0)
				destroyClan(clan);
		}, Math.max(clan.getDissolvingExpiryTime() - System.currentTimeMillis(), 60000));
	}
	
	public boolean isAllyExists(String allyName)
	{
		for (Clan clan : _clans.values())
		{
			if (clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName))
				return true;
		}
		return false;
	}
	
	public void storeClansWars(int clanId1, int clanId2)
	{
		final Clan clan1 = _clans.get(clanId1);
		final Clan clan2 = _clans.get(clanId2);
		
		clan1.setEnemyClan(clanId2);
		clan1.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan1), SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP).addString(clan2.getName()));
		
		clan2.setAttackerClan(clanId1);
		clan2.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan2), SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR).addString(clan1.getName()));
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2) VALUES(?,?)");
			ps.setInt(1, clanId1);
			ps.setInt(2, clanId2);
			ps.execute();
			ps.close();
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "Error storing clan wars data.", e);
		}
	}
	
	public void deleteClansWars(int clanId1, int clanId2)
	{
		final Clan clan1 = _clans.get(clanId1);
		final Clan clan2 = _clans.get(clanId2);
		
		clan1.deleteEnemyClan(clanId2);
		clan1.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan1), SystemMessage.getSystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED).addString(clan2.getName()));
		
		clan2.deleteAttackerClan(clanId1);
		clan2.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan2), SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP).addString(clan1.getName()));
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps;
			
			if (Config.ALT_CLAN_WAR_PENALTY_WHEN_ENDED > 0)
			{
				final long penaltyExpiryTime = System.currentTimeMillis() + Config.ALT_CLAN_WAR_PENALTY_WHEN_ENDED * 86400000L;
				
				clan1.addWarPenaltyTime(clanId2, penaltyExpiryTime);
				
				ps = con.prepareStatement("UPDATE clan_wars SET expiry_time=? WHERE clan1=? AND clan2=?");
				ps.setLong(1, penaltyExpiryTime);
				ps.setInt(2, clanId1);
				ps.setInt(3, clanId2);
			}
			else
			{
				ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
				ps.setInt(1, clanId1);
				ps.setInt(2, clanId2);
			}
			ps.execute();
			ps.close();
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "Error removing clan wars data.", e);
		}
	}
	
	public void checkSurrender(Clan clan1, Clan clan2)
	{
		int count = 0;
		for (ClanMember player : clan1.getMembers())
		{
			if (player != null && player.getPlayerInstance().wantsPeace())
				count++;
		}
		
		if (count == clan1.getMembersCount() - 1)
		{
			clan1.deleteEnemyClan(clan2.getClanId());
			clan2.deleteEnemyClan(clan1.getClanId());
			deleteClansWars(clan1.getClanId(), clan2.getClanId());
		}
	}
	
	/**
	 * Restore wars, checking penalties.
	 */
	private void restoreWars()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete deprecated wars (server was offline).
			PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE expiry_time > 0 AND expiry_time <= ?");
			ps.setLong(1, System.currentTimeMillis());
			ps.execute();
			ps.close();
			
			// Load all wars.
			ps = con.prepareStatement("SELECT * FROM clan_wars");
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				final int clan1 = rs.getInt("clan1");
				final int clan2 = rs.getInt("clan2");
				final long expiryTime = rs.getLong("expiry_time");
				
				// Expiry timer is found, add a penalty. Otherwise, add the regular war.
				if (expiryTime > 0)
					_clans.get(clan1).addWarPenaltyTime(clan2, expiryTime);
				else
				{
					_clans.get(clan1).setEnemyClan(clan2);
					_clans.get(clan2).setAttackerClan(clan1);
				}
			}
			rs.close();
			ps.close();
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "Error restoring clan wars data.", e);
		}
	}
	
	/**
	 * Check for nonexistent alliances
	 */
	private void allianceCheck()
	{
		for (Clan clan : _clans.values())
		{
			int allyId = clan.getAllyId();
			if (allyId != 0 && clan.getClanId() != allyId)
			{
				if (!_clans.containsKey(allyId))
				{
					clan.setAllyId(0);
					clan.setAllyName(null);
					clan.changeAllyCrest(0, true);
					clan.updateClanInDB();
					LOG.info("Removed alliance from clan: " + clan.getName());
				}
			}
		}
	}
	
	public List<Clan> getClanAllies(int allianceId)
	{
		if (allianceId == 0)
			return Collections.emptyList();
		
		return _clans.values().stream().filter(c -> c.getAllyId() == allianceId).collect(Collectors.toList());
	}
	
	/**
	 * Refresh clans ladder, picking up the 99 first best clans, and allocating their ranks accordingly.
	 * @param cleanupRank if true, cleanup ranks. Used for the task, useless for startup.
	 */
	public void refreshClansLadder(boolean cleanupRank)
	{
		// Cleanup ranks. Needed, as one clan can go off the list.
		if (cleanupRank)
		{
			for (Clan clan : _clans.values())
				if (clan != null && clan.getRank() != 0)
					clan.setRank(0);
		}
		
		// Retrieve the 99 best clans, allocate their ranks.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT clan_id FROM clan_data ORDER BY reputation_score DESC LIMIT 99");
			ResultSet rs = ps.executeQuery();
			
			int rank = 1;
			
			while (rs.next())
			{
				final Clan clan = _clans.get(rs.getInt("clan_id"));
				if (clan != null && clan.getReputationScore() > 0)
					clan.setRank(rank++);
			}
			rs.close();
			ps.close();
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "Error updating clans ladder.", e);
		}
	}
	
	public static ClanTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanTable INSTANCE = new ClanTable();
	}
}