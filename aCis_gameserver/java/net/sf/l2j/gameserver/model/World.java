package net.sf.l2j.gameserver.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.location.Location;

public final class World
{
	private static Logger _log = Logger.getLogger(World.class.getName());
	
	// Geodata min/max tiles
	public static final int TILE_X_MIN = 16;
	public static final int TILE_X_MAX = 26;
	public static final int TILE_Y_MIN = 10;
	public static final int TILE_Y_MAX = 25;
	
	// Map dimensions
	public static final int TILE_SIZE = 32768;
	public static final int WORLD_X_MIN = (TILE_X_MIN - 20) * TILE_SIZE;
	public static final int WORLD_X_MAX = (TILE_X_MAX - 19) * TILE_SIZE;
	public static final int WORLD_Y_MIN = (TILE_Y_MIN - 18) * TILE_SIZE;
	public static final int WORLD_Y_MAX = (TILE_Y_MAX - 17) * TILE_SIZE;
	
	// Regions and offsets
	private static final int REGION_SIZE = 2048;
	private static final int REGIONS_X = (WORLD_X_MAX - WORLD_X_MIN) / REGION_SIZE;
	private static final int REGIONS_Y = (WORLD_Y_MAX - WORLD_Y_MIN) / REGION_SIZE;
	private static final int REGION_X_OFFSET = Math.abs(WORLD_X_MIN / REGION_SIZE);
	private static final int REGION_Y_OFFSET = Math.abs(WORLD_Y_MIN / REGION_SIZE);
	
	private final Map<Integer, WorldObject> _objects = new ConcurrentHashMap<>();
	private final Map<Integer, Pet> _pets = new ConcurrentHashMap<>();
	private final Map<Integer, Player> _players = new ConcurrentHashMap<>();
	
	private final WorldRegion[][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
	
	protected World()
	{
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
				_worldRegions[i][j] = new WorldRegion(i, j);
		}
		
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int a = -1; a <= 1; a++)
				{
					for (int b = -1; b <= 1; b++)
					{
						if (validRegion(x + a, y + b))
							_worldRegions[x + a][y + b].addSurroundingRegion(_worldRegions[x][y]);
					}
				}
			}
		}
		_log.info("World: WorldRegion grid (" + REGIONS_X + " by " + REGIONS_Y + ") is now set up.");
	}
	
	public void addObject(WorldObject object)
	{
		_objects.putIfAbsent(object.getObjectId(), object);
	}
	
	public void removeObject(WorldObject object)
	{
		_objects.remove(object.getObjectId());
	}
	
	public Collection<WorldObject> getObjects()
	{
		return _objects.values();
	}
	
	public WorldObject getObject(int objectId)
	{
		return _objects.get(objectId);
	}
	
	public void addPlayer(Player cha)
	{
		_players.putIfAbsent(cha.getObjectId(), cha);
	}
	
	public void removePlayer(Player cha)
	{
		_players.remove(cha.getObjectId());
	}
	
	public Collection<Player> getPlayers()
	{
		return _players.values();
	}
	
	public Player getPlayer(String name)
	{
		return _players.get(PlayerInfoTable.getInstance().getPlayerObjectId(name));
	}
	
	public Player getPlayer(int objectId)
	{
		return _players.get(objectId);
	}
	
	public void addPet(int ownerId, Pet pet)
	{
		_pets.putIfAbsent(ownerId, pet);
	}
	
	public void removePet(int ownerId)
	{
		_pets.remove(ownerId);
	}
	
	public Pet getPet(int ownerId)
	{
		return _pets.get(ownerId);
	}
	
	public static int getRegionX(int regionX)
	{
		return (regionX - REGION_X_OFFSET) * REGION_SIZE;
	}
	
	public static int getRegionY(int regionY)
	{
		return (regionY - REGION_Y_OFFSET) * REGION_SIZE;
	}
	
	/**
	 * @param point position of the object.
	 * @return the current WorldRegion of the object according to its position (x,y).
	 */
	public WorldRegion getRegion(Location point)
	{
		return getRegion(point.getX(), point.getY());
	}
	
	public WorldRegion getRegion(int x, int y)
	{
		return _worldRegions[(x - WORLD_X_MIN) / REGION_SIZE][(y - WORLD_Y_MIN) / REGION_SIZE];
	}
	
	/**
	 * @return the whole 2d array containing the world regions used by ZoneData.java to setup zones inside the world regions
	 */
	public WorldRegion[][] getWorldRegions()
	{
		return _worldRegions;
	}
	
	/**
	 * @param x X position of the object
	 * @param y Y position of the object
	 * @return True if the given coordinates are valid WorldRegion coordinates.
	 */
	private static boolean validRegion(int x, int y)
	{
		return (x >= 0 && x <= REGIONS_X && y >= 0 && y <= REGIONS_Y);
	}
	
	/**
	 * Delete all spawns in the world.
	 */
	public void deleteVisibleNpcSpawns()
	{
		_log.info("Deleting all visible NPCs.");
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				for (WorldObject obj : _worldRegions[i][j].getObjects())
				{
					if (obj instanceof Npc)
					{
						((Npc) obj).deleteMe();
						
						final L2Spawn spawn = ((Npc) obj).getSpawn();
						if (spawn != null)
						{
							spawn.setRespawnState(false);
							SpawnTable.getInstance().deleteSpawn(spawn, false);
						}
					}
				}
			}
		}
		_log.info("All visibles NPCs are now deleted.");
	}
	
	public static World getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final World _instance = new World();
	}


	public int getAllPlayersCount()
	{
		return _players.size();
	}

	  public static List<Player> getAllGMs()
	  {
	    return AdminData.getAllGms(true);
	  }
}