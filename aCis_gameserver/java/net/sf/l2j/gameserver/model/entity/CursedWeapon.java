package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.group.Party.MessageType;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.ExRedSky;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * One of these swords can drop from any mob. But only one instance of each sword can exist in the world. When a cursed sword drops, the world becomes red for several seconds, the ground shakes, and there's also an announcement as a system message that a cursed sword is found.<br>
 * <br>
 * The owner automatically becomes chaotic and their HP/CP/MP are fully restored.<br>
 * <br>
 * A cursed sword is equipped automatically when it's found, and the owner doesn't have an option to unequip it, to drop it or to destroy it. With a cursed sword you get some special skills.<br>
 * <br>
 * The cursed swords disappear after a certain period of time, and it doesn't matter how much time the owner spends online. This period of time is reduced if the owner kills another player, but the abilities of the sword increase. However, the owner needs to kill at least one player per day,
 * otherwise the sword disappears in 24 hours. There will be system messages about how much lifetime the sword has and when last murder was committed.<br>
 * <br>
 * If the owner dies, the sword either disappears or drops. When the sword is gone, the owner gains back their skills and characteristics go back to normal.
 */
public class CursedWeapon
{
	protected static final Logger _log = Logger.getLogger(CursedWeapon.class.getName());
	
	private final String _name;
	
	protected final int _itemId;
	private ItemInstance _item = null;
	
	private int _playerId = 0;
	protected Player _player = null;
	
	// Skill id and max level. Max level is took from skillid (allow custom skills).
	private final int _skillId;
	private final int _skillMaxLevel;
	
	// Drop rate (when a mob is killed) and chance of dissapear (when a CW owner dies).
	private int _dropRate;
	private int _dissapearChance;
	
	// Overall duration (in hours) and hungry - used for daily task - duration (in hours)
	private int _duration;
	private int _durationLost;
	
	// Basic number used to calculate next number of needed victims for a stage (50% to 150% the given value).
	private int _stageKills;
	
	private boolean _isDropped = false;
	private boolean _isActivated = false;
	
	private ScheduledFuture<?> _overallTimerTask;
	private ScheduledFuture<?> _dailyTimerTask;
	private ScheduledFuture<?> _dropTimerTask;
	
	private int _playerKarma = 0;
	private int _playerPkKills = 0;
	
	// Number of current killed, current stage of weapon (1 by default, max is _skillMaxLevel), and number of victims needed for next stage.
	protected int _nbKills = 0;
	protected int _currentStage = 1;
	protected int _numberBeforeNextStage = 0;
	
	// Hungry timer (in minutes) and overall end timer (in ms).
	protected int _hungryTime = 0;
	protected long _endTime = 0;
	
	public CursedWeapon(StatsSet set)
	{
		_name = set.getString("name");
		_itemId = set.getInteger("id");
		_skillId = set.getInteger("skillId");
		_dropRate = set.getInteger("dropRate");
		_dissapearChance = set.getInteger("dissapearChance");
		_duration = set.getInteger("duration");
		_durationLost = set.getInteger("durationLost");
		_stageKills = set.getInteger("stageKills");
		
		_skillMaxLevel = SkillTable.getInstance().getMaxLevel(_skillId);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement ps = con.prepareStatement("SELECT * FROM cursed_weapons WHERE itemId=?");
			ps.setInt(1, _itemId);
			
			final ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				_playerId = rs.getInt("playerId");
				_playerKarma = rs.getInt("playerKarma");
				_playerPkKills = rs.getInt("playerPkKills");
				_nbKills = rs.getInt("nbKills");
				_currentStage = rs.getInt("currentStage");
				_numberBeforeNextStage = rs.getInt("numberBeforeNextStage");
				_hungryTime = rs.getInt("hungryTime");
				_endTime = rs.getLong("endTime");
				
				reActivate(false);
			}
			
			rs.close();
			ps.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore CursedWeapons data: " + e.getMessage(), e);
		}
	}
	
	public void setPlayer(Player player)
	{
		_player = player;
	}
	
	public void setItem(ItemInstance item)
	{
		_item = item;
	}
	
	public boolean isActivated()
	{
		return _isActivated;
	}
	
	public boolean isDropped()
	{
		return _isDropped;
	}
	
	public long getEndTime()
	{
		return _endTime;
	}
	
	public long getDuration()
	{
		return _duration;
	}
	
	public int getDurationLost()
	{
		return _durationLost;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public int getPlayerId()
	{
		return _playerId;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public int getPlayerKarma()
	{
		return _playerKarma;
	}
	
	public int getPlayerPkKills()
	{
		return _playerPkKills;
	}
	
	public int getNbKills()
	{
		return _nbKills;
	}
	
	public int getStageKills()
	{
		return _stageKills;
	}
	
	public boolean isActive()
	{
		return _isActivated || _isDropped;
	}
	
	public long getTimeLeft()
	{
		return _endTime - System.currentTimeMillis();
	}
	
	public int getCurrentStage()
	{
		return _currentStage;
	}
	
	public int getNumberBeforeNextStage()
	{
		return _numberBeforeNextStage;
	}
	
	public int getHungryTime()
	{
		return _hungryTime;
	}
	
	/**
	 * This method is used to destroy a {@link CursedWeapon}.<br>
	 * It manages following states :
	 * <ul>
	 * <li><u>item on a online player</u> : drops the cursed weapon from inventory, and set back ancient pk/karma values.</li>
	 * <li><u>item on a offline player</u> : make SQL operations in order to drop item from database.</li>
	 * <li><u>item on ground</u> : destroys the item directly.</li>
	 * </ul>
	 * For all cases, a message is broadcasted, and the different states are reinitialized.
	 */
	public void endOfLife()
	{
		if (_isActivated)
		{
			// Player is online ; unequip weapon && destroy it.
			if (_player != null && _player.isOnline())
			{
				_log.info(_name + " being removed online.");
				
				_player.abortAttack();
				
				_player.setKarma(_playerKarma);
				_player.setPkKills(_playerPkKills);
				_player.setCursedWeaponEquippedId(0);
				removeDemonicSkills();
				
				// Unequip && remove.
				_player.useEquippableItem(_item, true);
				_player.destroyItemByItemId("CW", _itemId, 1, _player, false);
				
				_player.broadcastUserInfo();
				
				_player.store();
			}
			// Player is offline ; make only SQL operations.
			else
			{
				_log.info(_name + " being removed offline.");
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					// Delete the item
					PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
					ps.setInt(1, _playerId);
					ps.setInt(2, _itemId);
					ps.close();
					
					// Restore the karma and PK kills.
					ps = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
					ps.setInt(1, _playerKarma);
					ps.setInt(2, _playerPkKills);
					ps.setInt(3, _playerId);
					ps.close();
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Could not delete : " + e.getMessage(), e);
				}
			}
		}
		else
		{
			// This CW is in the inventory of someone who has another cursed weapon equipped.
			if (_player != null && _player.getInventory().getItemByItemId(_itemId) != null)
			{
				_player.destroyItemByItemId("CW", _itemId, 1, _player, false);
				_log.info(_name + " item has been assimilated.");
			}
			// This CW is on the ground.
			else if (_item != null)
			{
				_item.decayMe();
				_log.info(_name + " item has been removed from world.");
			}
		}
		
		// Drop tasks.
		cancelDailyTimerTask();
		cancelOverallTimerTask();
		cancelDropTimerTask();
		
		// Delete infos from table, if any.
		removeFromDb();
		
		// Inform all ppl.
		Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED).addItemName(_itemId));
		
		// Reset state.
		_player = null;
		_item = null;
		
		_isActivated = false;
		_isDropped = false;
		
		_nbKills = 0;
		_currentStage = 1;
		_numberBeforeNextStage = 0;
		
		_hungryTime = 0;
		_endTime = 0;
		
		_playerId = 0;
		_playerKarma = 0;
		_playerPkKills = 0;
	}
	
	private void cancelDailyTimerTask()
	{
		if (_dailyTimerTask != null)
		{
			_dailyTimerTask.cancel(true);
			_dailyTimerTask = null;
		}
	}
	
	private void cancelOverallTimerTask()
	{
		if (_overallTimerTask != null)
		{
			_overallTimerTask.cancel(true);
			_overallTimerTask = null;
		}
	}
	
	private void cancelDropTimerTask()
	{
		if (_dropTimerTask != null)
		{
			_dropTimerTask.cancel(true);
			_dropTimerTask = null;
		}
	}
	
	private class DailyTimerTask implements Runnable
	{
		// Internal timer to delay messages to the next hour, instead of every minute.
		private int _timer = 0;
		
		protected DailyTimerTask()
		{
		}
		
		@Override
		public void run()
		{
			_hungryTime--;
			_timer++;
			
			if (_hungryTime <= 0)
				endOfLife();
			else if (_player != null && _player.isOnline() && _timer % 60 == 0)
			{
				SystemMessage msg;
				int timeLeft = (int) (getTimeLeft() / 60000);
				if (timeLeft > 60)
				{
					msg = SystemMessage.getSystemMessage(SystemMessageId.S2_HOUR_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
					msg.addItemName(_player.getCursedWeaponEquippedId());
					msg.addNumber(Math.round(timeLeft / 60));
				}
				else
				{
					msg = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
					msg.addItemName(_player.getCursedWeaponEquippedId());
					msg.addNumber(timeLeft);
				}
				_player.sendPacket(msg);
			}
		}
	}
	
	private class OverallTimerTask implements Runnable
	{
		protected OverallTimerTask()
		{
		}
		
		@Override
		public void run()
		{
			// Overall timer is reached, ends the life of CW.
			if (System.currentTimeMillis() >= _endTime)
				endOfLife();
			// Save data.
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement ps = con.prepareStatement("UPDATE cursed_weapons SET nbKills=?, currentStage=?, numberBeforeNextStage=?, hungryTime=?, endTime=? WHERE itemId=?");
					ps.setInt(1, _nbKills);
					ps.setInt(2, _currentStage);
					ps.setInt(3, _numberBeforeNextStage);
					ps.setInt(4, _hungryTime);
					ps.setLong(5, _endTime);
					ps.setInt(6, _itemId);
					ps.executeUpdate();
					ps.close();
				}
				catch (SQLException e)
				{
					_log.log(Level.SEVERE, "CursedWeapon: Failed to update data.", e);
				}
			}
		}
	}
	
	private class DropTimerTask implements Runnable
	{
		protected DropTimerTask()
		{
		}
		
		@Override
		public void run()
		{
			if (isDropped())
				endOfLife();
		}
	}
	
	/**
	 * This method is used to drop the {@link CursedWeapon} from its {@link Player} owner.<br>
	 * It drops the item on ground, and reset player stats and skills. Finally it broadcasts a message to all online players.
	 * @param killer : The creature who killed the cursed weapon owner.
	 */
	private void dropFromPlayer(Creature killer)
	{
		_player.abortAttack();
		
		// Prevent item from being removed by ItemsAutoDestroy.
		_item.setDestroyProtected(true);
		_player.dropItem("DieDrop", _item, killer, true);
		
		_isActivated = false;
		_isDropped = true;
		
		_player.setKarma(_playerKarma);
		_player.setPkKills(_playerPkKills);
		_player.setCursedWeaponEquippedId(0);
		removeDemonicSkills();
		
		// Cancel the daily timer. It will be reactivated when someone will pickup the weapon.
		cancelDailyTimerTask();
		
		// Activate the "1h dropped CW" timer.
		_dropTimerTask = ThreadPool.schedule(new DropTimerTask(), 3600000L);
		
		// Reset current stage to 1.
		_currentStage = 1;
		
		// Drop infos from database.
		removeFromDb();
		
		// Broadcast a message to all online players.
		Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION).addZoneName(_player.getPosition()).addItemName(_itemId));
	}
	
	/**
	 * This method is used to drop the {@link CursedWeapon} from a {@link Attackable} monster.<br>
	 * It drops the item on ground, and broadcast earthquake && red sky animations. Finally it broadcasts a message to all online players.
	 * @param attackable : The monster who dropped the cursed weapon.
	 * @param player : The player who killed the monster.
	 */
	private void dropFromMob(Attackable attackable, Player player)
	{
		_isActivated = false;
		
		// Get position.
		int x = attackable.getX() + Rnd.get(-70, 70);
		int y = attackable.getY() + Rnd.get(-70, 70);
		int z = GeoEngine.getInstance().getHeight(x, y, attackable.getZ());
		
		// Create item and drop it.
		_item = ItemTable.getInstance().createItem("CursedWeapon", _itemId, 1, player, attackable);
		_item.setDestroyProtected(true);
		_item.dropMe(attackable, x, y, z);
		
		// RedSky and Earthquake
		Broadcast.toAllOnlinePlayers(new ExRedSky(10));
		Broadcast.toAllOnlinePlayers(new Earthquake(x, y, z, 14, 3));
		
		_isDropped = true;
		
		// Broadcast a message to all online players.
		Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION).addZoneName(player.getPosition()).addItemName(_itemId));
	}
	
	/**
	 * Method used to send messages :<br>
	 * <ul>
	 * <li>one is broadcasted to warn players than {@link CursedWeapon} owner is online.</li>
	 * <li>the other shows left timer for the cursed weapon owner (either in hours or minutes).</li>
	 * </ul>
	 */
	public void cursedOnLogin()
	{
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION);
		msg.addZoneName(_player.getPosition());
		msg.addItemName(_player.getCursedWeaponEquippedId());
		Broadcast.toAllOnlinePlayers(msg);
		
		final int timeLeft = (int) (getTimeLeft() / 60000);
		if (timeLeft > 60)
		{
			msg = SystemMessage.getSystemMessage(SystemMessageId.S2_HOUR_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
			msg.addItemName(_player.getCursedWeaponEquippedId());
			msg.addNumber(Math.round(timeLeft / 60));
		}
		else
		{
			msg = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
			msg.addItemName(_player.getCursedWeaponEquippedId());
			msg.addNumber(timeLeft);
		}
		_player.sendPacket(msg);
	}
	
	/**
	 * Rebind the passive skill belonging to the {@link CursedWeapon} owner. Invoke this method if the weapon owner switches to a subclass.
	 */
	public void giveDemonicSkills()
	{
		_player.addSkill(SkillTable.getInstance().getInfo(_skillId, _currentStage), false);
		_player.sendSkillList();
	}
	
	private void removeDemonicSkills()
	{
		_player.removeSkill(_skillId, false);
		_player.sendSkillList();
	}
	
	/**
	 * Reactivate the {@link CursedWeapon}. It can be either coming from a player login, or a GM command.
	 * @param fromZero : if set to true, both _hungryTime and _endTime will be reseted to their default values.
	 */
	public void reActivate(boolean fromZero)
	{
		if (fromZero)
		{
			_hungryTime = _durationLost * 60;
			_endTime = (System.currentTimeMillis() + _duration * 3600000L);
			
			_overallTimerTask = ThreadPool.scheduleAtFixedRate(new OverallTimerTask(), 60000L, 60000L);
		}
		else
		{
			_isActivated = true;
			
			if (_endTime - System.currentTimeMillis() <= 0)
				endOfLife();
			else
			{
				_dailyTimerTask = ThreadPool.scheduleAtFixedRate(new DailyTimerTask(), 60000L, 60000L);
				_overallTimerTask = ThreadPool.scheduleAtFixedRate(new OverallTimerTask(), 60000L, 60000L);
			}
		}
	}
	
	/**
	 * Handles the drop rate of a {@link CursedWeapon}. If successful, launches the different associated tasks (end, overall and drop timers).
	 * @param attackable : The monster who drops the cursed weapon.
	 * @param player : The player who killed the monster.
	 * @return true if the drop rate is a success.
	 */
	public boolean checkDrop(Attackable attackable, Player player)
	{
		if (Rnd.get(1000000) < _dropRate)
		{
			// Drop the item.
			dropFromMob(attackable, player);
			
			// Start timers.
			_endTime = System.currentTimeMillis() + _duration * 3600000L;
			_overallTimerTask = ThreadPool.scheduleAtFixedRate(new OverallTimerTask(), 60000L, 60000L);
			_dropTimerTask = ThreadPool.schedule(new DropTimerTask(), 3600000L);
			
			return true;
		}
		return false;
	}
	
	/**
	 * Activate the {@link CursedWeapon}. We refresh {@link Player} owner, store related infos, save references, activate cursed weapon skills, expell him from the party (if any).<br>
	 * <br>
	 * Finally it broadcasts a message to all online players.
	 * @param player : The player who pickup the cursed weapon.
	 * @param item : The item used as reference.
	 */
	public void activate(Player player, ItemInstance item)
	{
		// if the player is mounted, attempt to unmount first and pick it if successful.
		if (player.isMounted() && !player.dismount())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item.getItemId()));
			item.setDestroyProtected(true);
			player.dropItem("InvDrop", item, null, true);
			return;
		}
		
		_isActivated = true;
		
		// Hold player data.
		_player = player;
		_playerId = _player.getObjectId();
		_playerKarma = _player.getKarma();
		_playerPkKills = _player.getPkKills();
		
		_item = item;
		
		// Generate a random number for next stage.
		_numberBeforeNextStage = Rnd.get((int) Math.round(_stageKills * 0.5), (int) Math.round(_stageKills * 1.5));
		
		// Renew hungry time.
		_hungryTime = _durationLost * 60;
		
		// Activate the daily timer.
		_dailyTimerTask = ThreadPool.scheduleAtFixedRate(new DailyTimerTask(), 60000L, 60000L);
		
		// Cancel the "1h dropped CW" timer.
		cancelDropTimerTask();
		
		// Save data on database.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("INSERT INTO cursed_weapons (itemId, playerId, playerKarma, playerPkKills, nbKills, currentStage, numberBeforeNextStage, hungryTime, endTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setInt(1, _itemId);
			ps.setInt(2, _playerId);
			ps.setInt(3, _playerKarma);
			ps.setInt(4, _playerPkKills);
			ps.setInt(5, _nbKills);
			ps.setInt(6, _currentStage);
			ps.setInt(7, _numberBeforeNextStage);
			ps.setInt(8, _hungryTime);
			ps.setLong(9, _endTime);
			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "CursedWeapon: Failed to insert data.", e);
		}
		
		// Change player stats
		_player.setCursedWeaponEquippedId(_itemId);
		_player.setKarma(9999999);
		_player.setPkKills(0);
		
		if (_player.isInParty())
			_player.getParty().removePartyMember(_player, MessageType.EXPELLED);
		
		// Disable active toggles
		for (L2Effect effect : _player.getAllEffects())
		{
			if (effect.getSkill().isToggle())
				effect.exit();
		}
		
		// Add CW skills
		giveDemonicSkills();
		
		// Equip the weapon
		_player.useEquippableItem(_item, true);
		
		// Fully heal player
		_player.setCurrentHpMp(_player.getMaxHp(), _player.getMaxMp());
		_player.setCurrentCp(_player.getMaxCp());
		
		// Refresh player stats
		_player.broadcastUserInfo();
		
		// _player.broadcastPacket(new SocialAction(_player, 17));
		Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION).addZoneName(_player.getPosition()).addItemName(_item.getItemId()));
	}
	
	/**
	 * Drop dynamic infos regarding {@link CursedWeapon} for the given itemId. Used in endOfLife() method.
	 */
	private void removeFromDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			ps.setInt(1, _itemId);
			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "CursedWeapon: Failed to remove data: " + e.getMessage(), e);
		}
	}
	
	/**
	 * This method checks if the {@link CursedWeapon} is dropped or simply dissapears.
	 * @param killer : The killer of cursed weapon owner.
	 */
	public void dropIt(Creature killer)
	{
		// Remove it
		if (Rnd.get(100) <= _dissapearChance)
			endOfLife();
		// Unequip & Drop
		else
			dropFromPlayer(killer);
	}
	
	/**
	 * Increase the number of kills. If actual counter reaches the number generated to reach next stage, than rank up the {@link CursedWeapon}.
	 */
	public void increaseKills()
	{
		if (_player != null && _player.isOnline())
		{
			_nbKills++;
			_hungryTime = _durationLost * 60;
			
			_player.setPkKills(_player.getPkKills() + 1);
			_player.sendPacket(new UserInfo(_player));
			
			// If current number of kills is >= to the given number, than rankUp the weapon.
			if (_nbKills >= _numberBeforeNextStage)
			{
				// Reset the number of kills to 0.
				_nbKills = 0;
				
				// Setup the new random number.
				_numberBeforeNextStage = Rnd.get((int) Math.round(_stageKills * 0.5), (int) Math.round(_stageKills * 1.5));
				
				// Rank up the CW.
				rankUp();
			}
		}
	}
	
	/**
	 * This method is used to rank up a CW.
	 */
	public void rankUp()
	{
		if (_currentStage >= _skillMaxLevel)
			return;
		
		// Rank up current stage.
		_currentStage++;
		
		// Reward skills for that CW.
		giveDemonicSkills();
	}
	
	public void goTo(Player player)
	{
		if (player == null)
			return;
		
		// Go to player holding the weapon
		if (_isActivated)
			player.teleToLocation(_player.getX(), _player.getY(), _player.getZ(), 0);
		// Go to item on the ground
		else if (_isDropped)
			player.teleToLocation(_item.getX(), _item.getY(), _item.getZ(), 0);
		else
			player.sendMessage(_name + " isn't in the world.");
	}
	
	public Location getWorldPosition()
	{
		if (_isActivated && _player != null)
			return _player.getPosition();
		
		if (_isDropped && _item != null)
			return _item.getPosition();
		
		return null;
	}
}