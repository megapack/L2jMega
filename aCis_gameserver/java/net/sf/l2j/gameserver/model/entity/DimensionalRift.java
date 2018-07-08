package net.sf.l2j.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;

public class DimensionalRift
{
	private static final long TIMER = 5000L;
	
	protected byte _type;
	protected Party _party;
	
	private Timer _teleporterTimer;
	private TimerTask _teleporterTimerTask;
	private Timer _spawnTimer;
	private TimerTask _spawnTimerTask;
	
	private Future<?> _earthQuakeTask;
	
	protected byte _currentJumps = 0;
	protected byte _choosenRoom = -1;
	private boolean _hasJumped = false;
	
	protected List<Byte> _completedRooms = new ArrayList<>();
	protected List<Player> _revivedInWaitingRoom = new CopyOnWriteArrayList<>();
	
	private boolean _isBossRoom = false;
	
	public DimensionalRift(Party party, byte type, byte room)
	{
		_type = type;
		_party = party;
		_choosenRoom = room;
		
		final DimensionalRiftRoom riftRoom = DimensionalRiftManager.getInstance().getRoom(type, room);
		riftRoom.setPartyInside(true);
		
		party.setDimensionalRift(this);
		
		final int[] coords = riftRoom.getTeleportCoords();
		for (Player member : party.getMembers())
			member.teleToLocation(coords[0], coords[1], coords[2], 0);
		
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}
	
	public byte getType()
	{
		return _type;
	}
	
	public byte getCurrentRoom()
	{
		return _choosenRoom;
	}
	
	protected void createTeleporterTimer(final boolean reasonTP)
	{
		if (_teleporterTimerTask != null)
		{
			_teleporterTimerTask.cancel();
			_teleporterTimerTask = null;
		}
		
		if (_teleporterTimer != null)
		{
			_teleporterTimer.cancel();
			_teleporterTimer = null;
		}
		
		if (_earthQuakeTask != null)
		{
			_earthQuakeTask.cancel(false);
			_earthQuakeTask = null;
		}
		
		_teleporterTimer = new Timer();
		_teleporterTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (_choosenRoom > -1)
					DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn();
				
				if (reasonTP && _currentJumps < Config.RIFT_MAX_JUMPS && !_party.wipedOut())
				{
					_currentJumps++;
					
					_completedRooms.add(_choosenRoom);
					_choosenRoom = -1;
					
					for (Player member : _party.getMembers())
					{
						if (!_revivedInWaitingRoom.contains(member))
							teleportToNextRoom(member, false);
					}
					
					createTeleporterTimer(true);
					createSpawnTimer(_choosenRoom);
				}
				else
				{
					for (Player member : _party.getMembers())
					{
						if (!_revivedInWaitingRoom.contains(member))
							DimensionalRiftManager.getInstance().teleportToWaitingRoom(member);
					}
					
					killRift();
					cancel();
				}
			}
		};
		
		if (reasonTP)
		{
			long jumpTime = calcTimeToNextJump();
			_teleporterTimer.schedule(_teleporterTimerTask, jumpTime); // Teleporter task, 8-10 minutes
			
			_earthQuakeTask = ThreadPool.schedule(() ->
			{
				for (Player member : _party.getMembers())
				{
					if (!_revivedInWaitingRoom.contains(member))
						member.sendPacket(new Earthquake(member.getX(), member.getY(), member.getZ(), 65, 9));
				}
			}, jumpTime - 7000);
		}
		else
			_teleporterTimer.schedule(_teleporterTimerTask, TIMER); // incorrect party member invited.
	}
	
	public void createSpawnTimer(final byte room)
	{
		if (_spawnTimerTask != null)
		{
			_spawnTimerTask.cancel();
			_spawnTimerTask = null;
		}
		
		if (_spawnTimer != null)
		{
			_spawnTimer.cancel();
			_spawnTimer = null;
		}
		
		_spawnTimer = new Timer();
		_spawnTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				DimensionalRiftManager.getInstance().getRoom(_type, room).spawn();
			}
		};
		
		_spawnTimer.schedule(_spawnTimerTask, Config.RIFT_SPAWN_DELAY);
	}
	
	public void manualTeleport(Player player, Npc npc)
	{
		final Party party = player.getParty();
		if (party == null || !party.isInDimensionalRift())
			return;
		
		if (!party.isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		if (_currentJumps == Config.RIFT_MAX_JUMPS)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/UsedAllJumps.htm", npc);
			return;
		}
		
		if (_hasJumped)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/AlreadyTeleported.htm", npc);
			return;
		}
		_hasJumped = true;
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn();
		_completedRooms.add(_choosenRoom);
		_choosenRoom = -1;
		
		for (Player member : _party.getMembers())
			teleportToNextRoom(member, true);
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).setPartyInside(true);
		
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}
	
	public void manualExitRift(Player player, Npc npc)
	{
		final Party party = player.getParty();
		if (party == null || !party.isInDimensionalRift())
			return;
		
		if (!party.isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		for (Player member : party.getMembers())
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(member);
		
		killRift();
	}
	
	/**
	 * This method allows to jump from one room to another. It calculates the next roomId.
	 * @param player to teleport
	 * @param cantJumpToBossRoom if true, Anakazel room can't be choosen (case of manual teleport).
	 */
	protected void teleportToNextRoom(Player player, boolean cantJumpToBossRoom)
	{
		if (_choosenRoom == -1)
		{
			List<Byte> emptyRooms;
			
			do
			{
				emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(_type);
				
				// Do not tp in the same room a second time
				emptyRooms.removeAll(_completedRooms);
				
				// If no room left, find any empty
				if (emptyRooms.isEmpty())
					emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(_type);
				
				// Pickup a random room
				_choosenRoom = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
				
				// This code handles Anakazel's room special behavior.
				if (cantJumpToBossRoom)
				{
					while (_choosenRoom == 9)
						_choosenRoom = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
				}
			}
			while (DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).isPartyInside());
		}
		
		final DimensionalRiftRoom riftRoom = DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom);
		riftRoom.setPartyInside(true);
		
		_isBossRoom = riftRoom.isBossRoom();
		
		final int[] coords = riftRoom.getTeleportCoords();
		player.teleToLocation(coords[0], coords[1], coords[2], 0);
	}
	
	public void killRift()
	{
		if (_party != null)
			_party.setDimensionalRift(null);
		
		_party = null;
		
		_completedRooms = null;
		_revivedInWaitingRoom = null;
		
		if (_earthQuakeTask != null)
		{
			_earthQuakeTask.cancel(false);
			_earthQuakeTask = null;
		}
		
		if (_teleporterTimer != null)
		{
			_teleporterTimer.cancel();
			_teleporterTimer = null;
		}
		
		if (_teleporterTimerTask != null)
		{
			_teleporterTimerTask.cancel();
			_teleporterTimerTask = null;
		}
		
		if (_spawnTimer != null)
		{
			_spawnTimer.cancel();
			_spawnTimer = null;
		}
		
		if (_spawnTimerTask != null)
		{
			_spawnTimerTask.cancel();
			_spawnTimerTask = null;
		}
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn();
	}
	
	private long calcTimeToNextJump()
	{
		long time = Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_MIN, Config.RIFT_AUTO_JUMPS_TIME_MAX) * 1000;
		
		if (_isBossRoom)
			return (long) (time * Config.RIFT_BOSS_ROOM_TIME_MUTIPLY);
		
		return time;
	}
	
	public void usedTeleport(Player player)
	{
		if (!_revivedInWaitingRoom.contains(player))
			_revivedInWaitingRoom.add(player);
		
		if (_party.getMembersCount() - _revivedInWaitingRoom.size() < Config.RIFT_MIN_PARTY_SIZE)
		{
			for (Player member : _party.getMembers())
			{
				if (!_revivedInWaitingRoom.contains(member))
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(member);
			}
			
			killRift();
		}
	}
}