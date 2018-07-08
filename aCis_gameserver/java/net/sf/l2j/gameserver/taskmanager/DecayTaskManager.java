package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;

/**
 * Destroys {@link Creature} corpse after specified time.
 */
public final class DecayTaskManager implements Runnable
{
	private final Map<Creature, Long> _characters = new ConcurrentHashMap<>();
	
	protected DecayTaskManager()
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
			
			final Creature character = entry.getKey();
			
			// Decay character and remove task.
			character.onDecay();
			_characters.remove(character);
		}
	}
	
	/**
	 * Adds {@link Creature} to the DecayTask with additional interval.
	 * @param character : {@link Creature} to be added.
	 * @param interval : Interval in seconds, after which the decay task is triggered.
	 */
	public final void add(Creature character, int interval)
	{
		// if character is monster
		if (character instanceof Attackable)
		{
			final Attackable monster = ((Attackable) character);
			
			// monster is spoiled or seeded, double the corpse delay
			if (monster.getSpoilerId() != 0 || monster.isSeeded())
				interval *= 2;
		}
		
		_characters.put(character, System.currentTimeMillis() + interval * 1000);
	}
	
	/**
	 * Removes {@link Creature} from the DecayTask.
	 * @param actor : {@link Creature} to be removed.
	 */
	public final void cancel(Creature actor)
	{
		_characters.remove(actor);
	}
	
	/**
	 * Removes {@link Attackable} from the DecayTask.
	 * @param monster : {@link Attackable} to be tested.
	 * @return boolean : True, when action can be applied on a corpse.
	 */
	public final boolean isCorpseActionAllowed(Attackable monster)
	{
		// get time and verify, if corpse exists
		Long time = _characters.get(monster);
		if (time == null)
			return false;
		
		// get corpse action interval, is half of corpse decay
		int corpseTime = monster.getTemplate().getCorpseTime() * 1000 / 2;
		
		// monster is spoiled or seeded, double the corpse action interval
		if (monster.getSpoilerId() != 0 || monster.isSeeded())
			corpseTime *= 2;
		
		// check last corpse action time
		return System.currentTimeMillis() < time - corpseTime;
	}
	
	public static final DecayTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		protected static final DecayTaskManager INSTANCE = new DecayTaskManager();
	}
}