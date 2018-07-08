package net.sf.l2j.gameserver.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

/**
 * @author Nightmare
 */
public class SpawnTable
{
	private static Logger _log = Logger.getLogger(SpawnTable.class.getName());
	
	private final Set<L2Spawn> _spawntable = ConcurrentHashMap.newKeySet();
	
	public static SpawnTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected SpawnTable()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
			fillSpawnTable();
	}
	
	public Set<L2Spawn> getSpawnTable()
	{
		return _spawntable;
	}
	
	private void fillSpawnTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM spawnlist");
			ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					if (template1.isType("SiegeGuard"))
					{
						// Don't spawn guards, they're spawned during castle sieges.
					}
					else if (template1.isType("RaidBoss"))
					{
						// Don't spawn raidbosses ; raidbosses are supposed to be loaded in another table !
						_log.warning("SpawnTable: RB (" + template1.getIdTemplate() + ") is in regular spawnlist, move it in raidboss_spawnlist.");
					}
					else if (!Config.ALLOW_CLASS_MASTERS && template1.isType("ClassMaster"))
					{
						// Dont' spawn class masters (if config is setuped to false).
					}
					else if (!Config.WYVERN_ALLOW_UPGRADER && template1.isType("WyvernManagerNpc"))
					{
						// Dont' spawn wyvern managers (if config is setuped to false).
					}
					else
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setLoc(rset.getInt("locx"), rset.getInt("locy"), rset.getInt("locz"), rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
						spawnDat.setRespawnRandom(rset.getInt("respawn_rand"));
						
						switch (rset.getInt("periodOfDay"))
						{
							case 0: // default
								spawnDat.setRespawnState(true);
								spawnDat.doSpawn(false);
								break;
							
							case 1: // Day
								DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
								break;
							
							case 2: // Night
								DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
								break;
						}
						
						_spawntable.add(spawnDat);
					}
				}
				else
				{
					_log.warning("SpawnTable: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warning("SpawnTable: Spawn could not be initialized: " + e);
		}
		
		_log.config("SpawnTable: Loaded " + _spawntable.size() + " Npc Spawn Locations.");
	}
	
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		_spawntable.add(spawn);
		
		if (storeInDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("INSERT INTO spawnlist (npc_templateid,locx,locy,locz,heading,respawn_delay) values(?,?,?,?,?,?)");
				statement.setInt(1, spawn.getNpcId());
				statement.setInt(2, spawn.getLocX());
				statement.setInt(3, spawn.getLocY());
				statement.setInt(4, spawn.getLocZ());
				statement.setInt(5, spawn.getHeading());
				statement.setInt(6, spawn.getRespawnDelay());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				// problem with storing spawn
				_log.warning("SpawnTable: Could not store spawn in the DB:" + e);
			}
		}
	}
	
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		if (!_spawntable.remove(spawn))
			return;
		
		if (updateDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("DELETE FROM spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?");
				statement.setInt(1, spawn.getLocX());
				statement.setInt(2, spawn.getLocY());
				statement.setInt(3, spawn.getLocZ());
				statement.setInt(4, spawn.getNpcId());
				statement.setInt(5, spawn.getHeading());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				// problem with deleting spawn
				_log.warning("SpawnTable: Spawn " + spawn + " could not be removed from DB: " + e);
			}
		}
	}
	
	// just wrapper
	public void reloadAll()
	{
		_spawntable.clear();
		fillSpawnTable();
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnTable _instance = new SpawnTable();
	}
}