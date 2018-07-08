package net.sf.l2j.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

/**
 * This class manages the spawn and respawn a {@link Npc}.<br>
 * <br>
 * L2Npc can be spawned to already defined {@link SpawnLocation}. If not defined, {@link Npc} is not spawned.
 */
public final class L2Spawn implements Runnable
{
	private static final Logger _log = Logger.getLogger(L2Spawn.class.getName());
	
	// the link on the NpcTemplate object containing generic and static properties of this spawn (ex : RewardExp, RewardSP, AggroRange...)
	private NpcTemplate _template;
	
	// the generic constructor of L2Npc managed by this L2Spawn
	private Constructor<?> _constructor;
	
	// the instance if L2Npc
	private Npc _npc;
	
	// spawn location
	private SpawnLocation _loc;
	
	// respawn information
	private int _respawnDelay;
	private int _respawnRandom;
	private boolean _respawnEnabled;
	
	private int _respawnMinDelay;
	private int _respawnMaxDelay;
	
	/**
	 * Constructor of L2Spawn.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Spawn owns generic and static properties (ex : RewardExp, RewardSP, AggroRange...). All of those properties are stored in a different L2NpcTemplate for each type of L2Spawn. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of L2Spawn is
	 * created, server just create a link between the instance and the template. This link is stored in <B>_template</B><BR>
	 * <BR>
	 * Each L2Npc is linked to a L2Spawn that manages its spawn and respawn (delay, location...). This link is stored in <B>_spawn</B> of the L2Npc<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the _template of the L2Spawn</li>
	 * <li>Calculate the implementationName used to generate the generic constructor of L2Npc managed by this L2Spawn</li>
	 * <li>Create the generic constructor of L2Npc managed by this L2Spawn</li><BR>
	 * <BR>
	 * @param template : {@link NpcTemplate} the template of {@link Npc} to be spawned.
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	public L2Spawn(NpcTemplate template) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		// Set the _template of the L2Spawn
		_template = template;
		if (_template == null)
			return;
		
		// Create the generic constructor of L2Npc managed by this L2Spawn
		Class<?>[] parameters =
		{
			int.class,
			Class.forName("net.sf.l2j.gameserver.model.actor.template.NpcTemplate")
		};
		_constructor = Class.forName("net.sf.l2j.gameserver.model.actor.instance." + _template.getType()).getConstructor(parameters);
	}
	
	/**
	 * Return the template of NPC.
	 * @return {@link NpcTemplate} : Template of NPC.
	 */
	public NpcTemplate getTemplate()
	{
		return _template;
	}
	
	/**
	 * Returns the ID of NPC.
	 * @return int : ID of NPC.
	 */
	public int getNpcId()
	{
		return _template.getNpcId();
	}
	
	/**
	 * Return the instance of NPC.
	 * @return {@link Npc} : Instance of NPC.
	 */
	public Npc getNpc()
	{
		return _npc;
	}
	
	/**
	 * Sets the {@link SpawnLocation} of the spawn point.
	 * @param loc : Location.
	 */
	public void setLoc(SpawnLocation loc)
	{
		_loc = loc;
	}
	
	/**
	 * Sets the {@link Location} of the spawn point by separate coordinates.
	 * @param locX : X coordinate.
	 * @param locY : Y coordinate.
	 * @param locZ : Z coordinate.
	 * @param heading : Heading.
	 */
	public void setLoc(int locX, int locY, int locZ, int heading)
	{
		_loc = new SpawnLocation(locX, locY, locZ, heading);
	}
	
	/**
	 * Returns the {@link Location} of the spawn point.
	 * @return Location : location of spawn point.
	 */
	public SpawnLocation getLoc()
	{
		return _loc;
	}
	
	/**
	 * Returns the X coordinate of the spawn point.
	 * @return int : X coordinate of spawn point.
	 */
	public int getLocX()
	{
		return _loc.getX();
	}
	
	/**
	 * Returns the Y coordinate of the spawn point.
	 * @return int : Y coordinate of spawn point.
	 */
	public int getLocY()
	{
		return _loc.getY();
	}
	
	/**
	 * Returns the Z coordinate of the spawn point.
	 * @return int : Z coordinate of spawn point.
	 */
	public int getLocZ()
	{
		return _loc.getZ();
	}
	
	/**
	 * Returns the heading of the spawn point.
	 * @return int : Heading of spawn point.
	 */
	public int getHeading()
	{
		return _loc.getHeading();
	}
	
	/**
	 * Set the respawn delay. Respawn delay represents average respawn time of the NPC. It can't be inferior to 0, it is automatically modified to 1 second.
	 * @param delay : Respawn delay in seconds.
	 */
	public void setRespawnDelay(int delay)
	{
		_respawnDelay = Math.max(1, delay);
	}
	
	/**
	 * Returns the respawn delay of the spawn. Respawn delay represents average respawn time of the NPC.
	 * @return int : Respawn delay of the spawn.
	 */
	public int getRespawnDelay()
	{
		return _respawnDelay;
	}
	
	/**
	 * Set the respawn random delay. Respawn random delay represents random period of the respawn. It can't be inferior to respawn delay.
	 * @param random : Random respawn delay in seconds.
	 */
	public void setRespawnRandom(int random)
	{
		_respawnRandom = Math.min(_respawnDelay, random);
	}
	
	/**
	 * Returns the respawn delay of the spawn. Respawn delay represents average respawn time of the NPC.
	 * @return int : Respawn delay of the spawn.
	 */
	public int getRespawnRandom()
	{
		return _respawnRandom;
	}
	
	/**
	 * Returns the respawn time of the spawn. Respawn time is respawn delay +- random respawn delay.
	 * @return int : Respawn time of the spawn.
	 */
	public int getRespawnTime()
	{
		int respawnTime = _respawnDelay;
		
		if (_respawnRandom > 0)
			respawnTime += Rnd.get(-_respawnRandom, _respawnRandom);
		
		return respawnTime;
	}
	
	/**
	 * Enables or disable respawn state of NPC.
	 * @param state
	 */
	public void setRespawnState(boolean state)
	{
		_respawnEnabled = state;
	}
	
	/**
	 * Set the minimum respawn delay.
	 * @param date
	 */
	public void setRespawnMinDelay(int date)
	{
		_respawnMinDelay = date;
	}
	
	/**
	 * @return the minimum RaidBoss spawn delay.
	 */
	public int getRespawnMinDelay()
	{
		return _respawnMinDelay;
	}
	
	/**
	 * Set Maximum respawn delay.
	 * @param date
	 */
	public void setRespawnMaxDelay(int date)
	{
		_respawnMaxDelay = date;
	}
	
	/**
	 * @return the maximum RaidBoss spawn delay.
	 */
	public int getRespawnMaxDelay()
	{
		return _respawnMaxDelay;
	}
	
	/**
	 * Create the {@link Npc}, add it to the world and launch its onSpawn() action.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2Npc can be spawned to already defined {@link SpawnLocation}. If not defined, {@link Npc} is not spawned.<BR>
	 * <BR>
	 * <B><U> Actions sequence for each spawn</U> : </B><BR>
	 * <ul>
	 * <li>Get {@link Npc} initialize parameters and generate its object ID</li>
	 * <li>Call the constructor of the {@link Npc}</li>
	 * <li>Link the {@link Npc} to this {@link L2Spawn}</li>
	 * <li>Make {@link SpawnLocation} check, when exists spawn process continues</li>
	 * <li>Reset {@link Npc} parameters - for re-spawning of existing {@link Npc}</li>
	 * <li>Calculate position using {@link SpawnLocation} and geodata</li>
	 * <li>Set the HP and MP of the {@link Npc} to the max</li>
	 * <li>Set the position and heading of the {@link Npc} (random heading is calculated, if not defined : value -1)</li>
	 * <li>Spawn {@link Npc} to the world</li>
	 * </ul>
	 * @param isSummonSpawn When true, summon magic circle will appear.
	 * @return the newly created instance.
	 */
	public Npc doSpawn(boolean isSummonSpawn)
	{
		try
		{
			// Check if the L2Spawn is not a Pet.
			if (_template.isType("Pet"))
				return null;
			
			// Get L2Npc Init parameters and its generate an Identifier
			Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				_template
			};
			
			// Call the constructor of the L2Npc (can be a L2ArtefactInstance, L2FriendlyMobInstance, L2GuardInstance, L2MonsterInstance, L2SiegeGuardInstance, L2BoxInstance, L2FeedableBeastInstance, L2TamedBeastInstance, L2NpcInstance)
			Object tmp = _constructor.newInstance(parameters);
			
			if (isSummonSpawn && tmp instanceof Creature)
				((Creature) tmp).setShowSummonAnimation(isSummonSpawn);
			
			// Check if the Instance is a L2Npc
			if (!(tmp instanceof Npc))
				return null;
			
			// create final instance of L2Npc
			_npc = (Npc) tmp;
			
			// assign L2Spawn to L2Npc
			_npc.setSpawn(this);
			
			// initialize L2Npc and spawn it
			initializeAndSpawn();
			
			return _npc;
		}
		catch (Exception e)
		{
			_log.warning("L2Spawn: Error during spawn, NPC id=" + _template.getNpcId());
			return null;
		}
	}
	
	/**
	 * Create a respawn task to be launched after the fixed + random delay. Respawn is only possible when respawn enabled.
	 */
	public void doRespawn()
	{
		// Check if respawn is possible to prevent multiple respawning caused by lag
		if (_respawnEnabled)
		{
			// Calculate the random time, if any.
			final int respawnTime = getRespawnTime() * 1000;
			
			// Schedule respawn of the NPC
			ThreadPool.schedule(this, respawnTime);
		}
	}
	
	/**
	 * Respawns NPC.
	 */
	@Override
	public void run()
	{
		if (_respawnEnabled)
		{
			_npc.refreshID();
			
			initializeAndSpawn();
		}
	}
	
	/**
	 * Initializes the {@link Npc} based on data in this L2Spawn and spawn {@link Npc} into the world.
	 */
	private void initializeAndSpawn()
	{
		// If location does not exist, there's a problem.
		if (_loc == null)
		{
			_log.warning("L2Spawn : the following npcID: " + _template.getNpcId() + " misses location informations.");
			return;
		}
		
		// reset effects and status
		_npc.stopAllEffects();
		_npc.setIsDead(false);
		
		// reset decay info
		_npc.setDecayed(false);
		
		// reset script value
		_npc.setScriptValue(0);
		
		// The L2Npc is spawned at the exact position (Lox, Locy, Locz)
		int locx = _loc.getX();
		int locy = _loc.getY();
		int locz = GeoEngine.getInstance().getHeight(locx, locy, _loc.getZ());
		
		// FIXME temporarily fix: when the spawn Z and geo Z differs more than 200, use spawn Z coord
		if (Math.abs(locz - _loc.getZ()) > 200)
			locz = _loc.getZ();
		
		// Set the HP and MP of the L2Npc to the max
		_npc.setCurrentHpMp(_npc.getMaxHp(), _npc.getMaxMp());
		
		// when champion mod is enabled, try to make NPC a champion
		if (Config.CHAMPION_FREQUENCY > 0)
		{
			// It can't be a Raid, a Raid minion nor a minion. Quest mobs and chests are disabled too.
			if (_npc instanceof Monster && !getTemplate().cantBeChampion() && _npc.getLevel() >= Config.CHAMP_MIN_LVL && _npc.getLevel() <= Config.CHAMP_MAX_LVL && !_npc.isRaid() && !((Monster) _npc).isRaidMinion() && !((Monster) _npc).isMinion())
				((Attackable) _npc).setChampion(Rnd.get(100) < Config.CHAMPION_FREQUENCY);
		}
		
		// set heading (random heading if not defined)
		_npc.setHeading(_loc.getHeading() < 0 ? Rnd.get(65536) : _loc.getHeading());
		
		// spawn NPC on new coordinates
		_npc.spawnMe(locx, locy, locz);
	}
	
	@Override
	public String toString()
	{
		return "L2Spawn [id=" + _template.getNpcId() + ", loc=" + _loc.toString() + "]";
	}

}