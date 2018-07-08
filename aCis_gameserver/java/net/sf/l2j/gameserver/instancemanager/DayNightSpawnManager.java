package net.sf.l2j.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.RaidBoss;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

/**
 * @author godson
 */
public class DayNightSpawnManager
{
	private static Logger _log = Logger.getLogger(DayNightSpawnManager.class.getName());
	
	private final List<L2Spawn> _dayCreatures;
	private final List<L2Spawn> _nightCreatures;
	private final Map<L2Spawn, RaidBoss> _bosses;
	
	public static DayNightSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected DayNightSpawnManager()
	{
		_dayCreatures = new ArrayList<>();
		_nightCreatures = new ArrayList<>();
		_bosses = new HashMap<>();
		
		notifyChangeMode();
	}
	
	public void addDayCreature(L2Spawn spawnDat)
	{
		_dayCreatures.add(spawnDat);
	}
	
	public void addNightCreature(L2Spawn spawnDat)
	{
		_nightCreatures.add(spawnDat);
	}
	
	/**
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures()
	{
		spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
	}
	
	/**
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures()
	{
		spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
	}
	
	/**
	 * Manage Spawn/Respawn.
	 * @param unSpawnCreatures List with L2Npc must be unspawned
	 * @param spawnCreatures List with L2Npc must be spawned
	 * @param UnspawnLogInfo String for log info for unspawned L2Npc
	 * @param SpawnLogInfo String for log info for spawned L2Npc
	 */
	private static void spawnCreatures(List<L2Spawn> unSpawnCreatures, List<L2Spawn> spawnCreatures, String UnspawnLogInfo, String SpawnLogInfo)
	{
		try
		{
			if (!unSpawnCreatures.isEmpty())
			{
				int i = 0;
				for (L2Spawn spawn : unSpawnCreatures)
				{
					if (spawn == null)
						continue;
					
					spawn.setRespawnState(false);
					Npc last = spawn.getNpc();
					if (last != null)
					{
						last.deleteMe();
						i++;
					}
				}
				_log.info("DayNightSpawnManager: Removed " + i + " " + UnspawnLogInfo + " creatures");
			}
			
			int i = 0;
			for (L2Spawn spawnDat : spawnCreatures)
			{
				if (spawnDat == null)
					continue;
				
				spawnDat.setRespawnState(true);
				spawnDat.doSpawn(false);
				i++;
			}
			
			_log.info("DayNightSpawnManager: Spawned " + i + " " + SpawnLogInfo + " creatures");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while spawning creatures: " + e.getMessage(), e);
		}
	}
	
	private void changeMode(int mode)
	{
		if (_nightCreatures.isEmpty() && _dayCreatures.isEmpty())
			return;
		
		switch (mode)
		{
			case 0:
				spawnDayCreatures();
				specialNightBoss(0);
				break;
			
			case 1:
				spawnNightCreatures();
				specialNightBoss(1);
				break;
			
			default:
				_log.warning("DayNightSpawnManager: Wrong mode sent");
				break;
		}
	}
	
	public void notifyChangeMode()
	{
		try
		{
			if (GameTimeTaskManager.getInstance().isNight())
				changeMode(1);
			else
				changeMode(0);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while notifyChangeMode(): " + e.getMessage(), e);
		}
	}
	
	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}
	
	private void specialNightBoss(int mode)
	{
		try
		{
			for (Map.Entry<L2Spawn, RaidBoss> infoEntry : _bosses.entrySet())
			{
				RaidBoss boss = infoEntry.getValue();
				if (boss == null)
				{
					if (mode == 1)
					{
						final L2Spawn spawn = infoEntry.getKey();
						
						boss = (RaidBoss) spawn.doSpawn(false);
						RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
						
						_bosses.put(spawn, boss);
					}
					continue;
				}
				
				if (boss.getNpcId() == 25328 && boss.getRaidStatus().equals(RaidBossSpawnManager.StatusEnum.ALIVE))
					handleHellmans(boss, mode);
				
				return;
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while specialNoghtBoss(): " + e.getMessage(), e);
		}
	}
	
	private static void handleHellmans(RaidBoss boss, int mode)
	{
		switch (mode)
		{
			case 0:
				boss.deleteMe();
				_log.info("DayNightSpawnManager: Deleting Hellman raidboss");
				break;
			
			case 1:
				boss.spawnMe();
				_log.info("DayNightSpawnManager: Spawning Hellman raidboss");
				break;
		}
	}
	
	public RaidBoss handleBoss(L2Spawn spawnDat)
	{
		if (_bosses.containsKey(spawnDat))
			return _bosses.get(spawnDat);
		
		if (GameTimeTaskManager.getInstance().isNight())
		{
			RaidBoss raidboss = (RaidBoss) spawnDat.doSpawn(false);
			_bosses.put(spawnDat, raidboss);
			
			return raidboss;
		}
		_bosses.put(spawnDat, null);
		
		return null;
	}
	
	private static class SingletonHolder
	{
		protected static final DayNightSpawnManager _instance = new DayNightSpawnManager();
	}
}