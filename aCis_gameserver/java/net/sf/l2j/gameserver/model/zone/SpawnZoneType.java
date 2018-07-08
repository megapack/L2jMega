package net.sf.l2j.gameserver.model.zone;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.location.Location;

/**
 * An abstract zone with spawn locations, inheriting {@link ZoneType} behavior.<br>
 * <br>
 * Two lazy initialized {@link List}s can hold {@link Location}s.
 */
public abstract class SpawnZoneType extends ZoneType
{
	private List<Location> _spawnLocs = null;
	private List<Location> _chaoticSpawnLocs = null;
	
	public SpawnZoneType(int id)
	{
		super(id);
	}
	
	/**
	 * Add a {@link Location} to _spawnLocs. Initialize _spawnLocs {@link List} if not yet initialized.
	 * @param x : The X position.
	 * @param y : The Y position.
	 * @param z : The Z position.
	 */
	public final void addSpawn(int x, int y, int z)
	{
		if (_spawnLocs == null)
			_spawnLocs = new ArrayList<>();
		
		_spawnLocs.add(new Location(x, y, z));
	}
	
	/**
	 * Add a {@link Location} to _chaoticSpawnLocs. Initialize _chaoticSpawnLocs {@link List} if not yet initialized.
	 * @param x : The X position.
	 * @param y : The Y position.
	 * @param z : The Z position.
	 */
	public final void addChaoticSpawn(int x, int y, int z)
	{
		if (_chaoticSpawnLocs == null)
			_chaoticSpawnLocs = new ArrayList<>();
		
		_chaoticSpawnLocs.add(new Location(x, y, z));
	}
	
	public final List<Location> getSpawns()
	{
		return _spawnLocs;
	}
	
	/**
	 * @return a random {@link Location} from _spawnLocs {@link List}.
	 */
	public final Location getSpawnLoc()
	{
		return Rnd.get(_spawnLocs);
	}
	
	/**
	 * @return a random {@link Location} from _chaoticSpawnLocs {@link List}. If _chaoticSpawnLocs isn't initialized, return a random Location from _spawnLocs.
	 */
	public final Location getChaoticSpawnLoc()
	{
		return Rnd.get((_chaoticSpawnLocs != null) ? _chaoticSpawnLocs : _spawnLocs);
	}
}