package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Cubic;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;

/**
 * Turns off attack stance of {@link Creature} after PERIOD ms.
 */
public final class AttackStanceTaskManager implements Runnable
{
	private static final long ATTACK_STANCE_PERIOD = 15000; // 15 seconds
	
	private final Map<Creature, Long> _characters = new ConcurrentHashMap<>();
	
	protected AttackStanceTaskManager()
	{
		// Run task each second.
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	@Override
	public final void run()
	{
		// List is empty, skip.
		if (_characters.isEmpty())
			return;
		
		// Get current time.
		final long time = System.currentTimeMillis();
		
		// Loop all characters.
		for (Map.Entry<Creature, Long> entry : _characters.entrySet())
		{
			// Time hasn't passed yet, skip.
			if (time < entry.getValue())
				continue;
			
			// Get character.
			final Creature character = entry.getKey();
			
			// Stop character attack stance animation.
			character.broadcastPacket(new AutoAttackStop(character.getObjectId()));
			
			if (character instanceof Player)
			{
				// Stop summon attack stance animation.
				final Summon summon = ((Player) character).getPet();
				if (summon != null)
					summon.broadcastPacket(new AutoAttackStop(summon.getObjectId()));
			}
			
			// Inform character AI and remove task.
			character.getAI().setAutoAttacking(false);
			_characters.remove(character);
		}
	}
	
	/**
	 * Adds {@link Creature} to the AttackStanceTask.
	 * @param character : {@link Creature} to be added and checked.
	 */
	public final void add(Creature character)
	{
		if (character instanceof Playable)
		{
			for (Cubic cubic : character.getActingPlayer().getCubics().values())
				if (cubic.getId() != Cubic.LIFE_CUBIC)
					cubic.doAction();
		}
		
		_characters.put(character, System.currentTimeMillis() + ATTACK_STANCE_PERIOD);
	}
	
	/**
	 * Removes {@link Creature} from the AttackStanceTask.
	 * @param character : {@link Creature} to be removed.
	 */
	public final void remove(Creature character)
	{
		if (character instanceof Summon)
			character = character.getActingPlayer();
		
		_characters.remove(character);
	}
	
	/**
	 * Tests if {@link Creature} is in AttackStanceTask.
	 * @param character : {@link Creature} to be removed.
	 * @return boolean : True when {@link Creature} is in attack stance.
	 */
	public final boolean isInAttackStance(Creature character)
	{
		if (character instanceof Summon)
			character = character.getActingPlayer();
		
		return _characters.containsKey(character);
	}
	
	public static final AttackStanceTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager INSTANCE = new AttackStanceTaskManager();
	}
}