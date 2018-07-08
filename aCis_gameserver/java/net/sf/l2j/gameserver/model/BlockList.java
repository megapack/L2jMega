package net.sf.l2j.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class BlockList
{
	private static Logger _log = Logger.getLogger(BlockList.class.getName());
	private static Map<Integer, List<Integer>> _offlineList = new HashMap<>();
	
	private final Player _owner;
	private List<Integer> _blockList;
	
	public BlockList(Player owner)
	{
		_owner = owner;
		_blockList = _offlineList.get(owner.getObjectId());
		if (_blockList == null)
			_blockList = loadList(_owner.getObjectId());
	}
	
	private synchronized void addToBlockList(int target)
	{
		_blockList.add(target);
		updateInDB(target, true);
	}
	
	private synchronized void removeFromBlockList(int target)
	{
		_blockList.remove(Integer.valueOf(target));
		updateInDB(target, false);
	}
	
	public void playerLogout()
	{
		_offlineList.put(_owner.getObjectId(), _blockList);
	}
	
	private static List<Integer> loadList(int ObjId)
	{
		List<Integer> list = new ArrayList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 1");
			statement.setInt(1, ObjId);
			ResultSet rset = statement.executeQuery();
			
			int friendId;
			while (rset.next())
			{
				friendId = rset.getInt("friend_id");
				if (friendId == ObjId)
					continue;
				
				list.add(friendId);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error found in " + ObjId + " friendlist while loading BlockList: " + e.getMessage(), e);
		}
		return list;
	}
	
	private void updateInDB(int targetId, boolean state)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			
			if (state)
			{
				statement = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, relation) VALUES (?, ?, 1)");
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, targetId);
			}
			else
			{
				statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id = ? AND friend_id = ? AND relation = 1");
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, targetId);
			}
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not add/remove block player: " + e.getMessage(), e);
		}
	}
	
	public boolean isInBlockList(Player target)
	{
		return _blockList.contains(target.getObjectId());
	}
	
	public boolean isInBlockList(int targetId)
	{
		return _blockList.contains(targetId);
	}
	
	private boolean isBlockAll()
	{
		return _owner.isInRefusalMode();
	}
	
	public static boolean isBlocked(Player listOwner, Player target)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(target);
	}
	
	public static boolean isBlocked(Player listOwner, int targetId)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(targetId);
	}
	
	private void setBlockAll(boolean state)
	{
		_owner.setInRefusalMode(state);
	}
	
	public List<Integer> getBlockList()
	{
		return _blockList;
	}
	
	public static void addToBlockList(Player listOwner, int targetId)
	{
		if (listOwner == null)
			return;
		
		String charName = PlayerInfoTable.getInstance().getPlayerName(targetId);
		
		if (listOwner.getFriendList().contains(targetId))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(charName);
			listOwner.sendPacket(sm);
			return;
		}
		
		if (listOwner.getBlockList().getBlockList().contains(targetId))
		{
			listOwner.sendMessage("Already in ignore list.");
			return;
		}
		
		listOwner.getBlockList().addToBlockList(targetId);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);
		
		Player player = World.getInstance().getPlayer(targetId);
		
		if (player != null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(listOwner.getName());
			player.sendPacket(sm);
		}
	}
	
	public static void removeFromBlockList(Player listOwner, int targetId)
	{
		if (listOwner == null)
			return;
		
		SystemMessage sm;
		String charName = PlayerInfoTable.getInstance().getPlayerName(targetId);
		
		if (!listOwner.getBlockList().getBlockList().contains(targetId))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT);
			listOwner.sendPacket(sm);
			return;
		}
		
		listOwner.getBlockList().removeFromBlockList(targetId);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);
	}
	
	public static boolean isInBlockList(Player listOwner, Player target)
	{
		return listOwner.getBlockList().isInBlockList(target);
	}
	
	public boolean isBlockAll(Player listOwner)
	{
		return listOwner.getBlockList().isBlockAll();
	}
	
	public static void setBlockAll(Player listOwner, boolean newValue)
	{
		listOwner.getBlockList().setBlockAll(newValue);
	}
	
	public static void sendListToOwner(Player listOwner)
	{
		int i = 1;
		listOwner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER);
		
		for (int playerId : listOwner.getBlockList().getBlockList())
			listOwner.sendMessage((i++) + ". " + PlayerInfoTable.getInstance().getPlayerName(playerId));
		
		listOwner.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
	}
	
	/**
	 * @param ownerId object id of owner block list
	 * @param targetId object id of potential blocked player
	 * @return true if blocked
	 */
	public static boolean isInBlockList(int ownerId, int targetId)
	{
		Player player = World.getInstance().getPlayer(ownerId);
		
		if (player != null)
			return BlockList.isBlocked(player, targetId);
		
		if (!_offlineList.containsKey(ownerId))
			_offlineList.put(ownerId, loadList(ownerId));
		
		return _offlineList.get(ownerId).contains(targetId);
	}
}