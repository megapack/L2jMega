package net.sf.l2j.gameserver.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public class MinionList
{
	private final Set<Monster> _minions = ConcurrentHashMap.newKeySet();
	private final Monster _master;
	
	public MinionList(Monster master)
	{
		_master = master;
	}
	
	/**
	 * @return a set of the spawned (alive) minions.
	 */
	public Set<Monster> getSpawnedMinions()
	{
		return _minions;
	}
	
	/**
	 * Manage the spawn of Minions.
	 * <ul>
	 * <li>Get the Minion data of all Minions that must be spawn</li>
	 * <li>For each Minion type, spawn the amount of Minion needed</li>
	 * </ul>
	 */
	public final void spawnMinions()
	{
		if (_master.isAlikeDead())
			return;
		
		for (MinionData minion : _master.getTemplate().getMinionData())
		{
			final int minionId = minion.getMinionId();
			final int minionsToSpawn = minion.getAmount() - countSpawnedMinionsById(minionId);
			if (minionsToSpawn <= 0)
				continue;
			
			for (int i = 0; i < minionsToSpawn; i++)
				spawnMinion(_master, minionId);
		}
	}
	
	/**
	 * Delete all spawned minions and try to reuse them.
	 */
	public void deleteSpawnedMinions()
	{
		if (_minions.isEmpty())
			return;
		
		for (Monster minion : _minions)
		{
			minion.setLeader(null);
			minion.deleteMe();
		}
		_minions.clear();
	}
	
	/**
	 * Called on the minion spawn and added them in the list of the spawned minions.
	 * @param minion The instance of minion.
	 */
	public void onMinionSpawn(Monster minion)
	{
		_minions.add(minion);
	}
	
	/**
	 * Called on the master death/delete.
	 * @param force if true - force delete of the spawned minions By default minions deleted only for raidbosses
	 */
	public void onMasterDie(boolean force)
	{
		if (_master.isRaid() || force)
			deleteSpawnedMinions();
	}
	
	/**
	 * Called on the minion death/delete. Removed minion from the list of the spawned minions and reuse if possible.
	 * @param minion The minion to make checks on.
	 * @param respawnTime (ms) enable respawning of this minion while master is alive. -1 - use default value: 0 (disable) for mobs and config value for raids.
	 */
	public void onMinionDie(Monster minion, int respawnTime)
	{
		minion.setLeader(null); // prevent memory leaks
		
		_minions.remove(minion);
		
		final int time = (_master.isRaid()) ? (int) Config.RAID_MINION_RESPAWN_TIMER : respawnTime;
		if (time > 0 && !_master.isAlikeDead())
		{
			ThreadPool.schedule(() ->
			{
				if (!_master.isAlikeDead() && _master.isVisible())
				{
					// minion can be already spawned or deleted
					if (!minion.isVisible())
					{
						minion.refreshID();
						initializeNpcInstance(_master, minion);
					}
				}
			}, time);
		}
	}
	
	/**
	 * Called if master/minion was attacked. Master and all free minions receive aggro against attacker.
	 * @param caller That instance will call for help versus attacker.
	 * @param attacker That instance will receive all aggro.
	 */
	public void onAssist(Creature caller, Creature attacker)
	{
		if (attacker == null)
			return;
		
		// The master is aggroed.
		if (!_master.isAlikeDead() && !_master.isInCombat())
			_master.addDamageHate(attacker, 0, 1);
		
		final boolean callerIsMaster = (caller == _master);
		
		// Define the aggro value of minions.
		int aggro = (callerIsMaster ? 10 : 1);
		if (_master.isRaid())
			aggro *= 10;
		
		for (Monster minion : _minions)
		{
			if (!minion.isDead() && (callerIsMaster || !minion.isInCombat()))
				minion.addDamageHate(attacker, 0, aggro);
		}
	}
	
	/**
	 * Called from onTeleported() of the master Alive and able to move minions teleported to master.
	 */
	public void onMasterTeleported()
	{
		final int offset = 200;
		final int minRadius = (int) (_master.getCollisionRadius() + 30);
		
		for (Monster minion : _minions)
		{
			if (minion.isDead() || minion.isMovementDisabled())
				continue;
			
			int newX = Rnd.get(minRadius * 2, offset * 2); // x
			int newY = Rnd.get(newX, offset * 2); // distance
			newY = (int) Math.sqrt(newY * newY - newX * newX); // y
			
			if (newX > offset + minRadius)
				newX = _master.getX() + newX - offset;
			else
				newX = _master.getX() - newX + minRadius;
			
			if (newY > offset + minRadius)
				newY = _master.getY() + newY - offset;
			else
				newY = _master.getY() - newY + minRadius;
			
			minion.teleToLocation(newX, newY, _master.getZ(), 0);
		}
	}
	
	/**
	 * Init a Minion and add it in the world as a visible object.
	 * <ul>
	 * <li>Get the template of the Minion to spawn</li>
	 * <li>Create and Init the Minion and generate its Identifier</li>
	 * <li>Set the Minion HP, MP and Heading</li>
	 * <li>Set the Minion leader to this RaidBoss</li>
	 * <li>Init the position of the Minion and add it in the world as a visible object</li><BR>
	 * </ul>
	 * @param master L2MonsterInstance used as master for this minion
	 * @param minionId The L2NpcTemplate Identifier of the Minion to spawn
	 * @return the instance of the new minion.
	 */
	public static final Monster spawnMinion(Monster master, int minionId)
	{
		// Get the template of the Minion to spawn
		NpcTemplate minionTemplate = NpcData.getInstance().getTemplate(minionId);
		if (minionTemplate == null)
			return null;
		
		// Create and Init the Minion and generate its Identifier
		Monster minion = new Monster(IdFactory.getInstance().getNextId(), minionTemplate);
		return initializeNpcInstance(master, minion);
	}
	
	protected static final Monster initializeNpcInstance(Monster master, Monster minion)
	{
		minion.stopAllEffects();
		minion.setIsDead(false);
		minion.setDecayed(false);
		
		// Set the Minion HP, MP and Heading
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
		minion.setHeading(master.getHeading());
		
		// Set the Minion leader to this RaidBoss
		minion.setLeader(master);
		
		// Init the position of the Minion and add it in the world as a visible object
		final int offset = (int) (100 + minion.getCollisionRadius() + master.getCollisionRadius());
		final int minRadius = (int) (master.getCollisionRadius() + 30);
		
		int newX = Rnd.get(minRadius * 2, offset * 2); // x
		int newY = Rnd.get(newX, offset * 2); // distance
		newY = (int) Math.sqrt(newY * newY - newX * newX); // y
		if (newX > offset + minRadius)
			newX = master.getX() + newX - offset;
		else
			newX = master.getX() - newX + minRadius;
		if (newY > offset + minRadius)
			newY = master.getY() + newY - offset;
		else
			newY = master.getY() - newY + minRadius;
		
		minion.spawnMe(newX, newY, master.getZ());
		
		return minion;
	}
	
	private final int countSpawnedMinionsById(int minionId)
	{
		int count = 0;
		for (Monster minion : _minions)
		{
			if (minion.getNpcId() == minionId)
				count++;
		}
		return count;
	}
}