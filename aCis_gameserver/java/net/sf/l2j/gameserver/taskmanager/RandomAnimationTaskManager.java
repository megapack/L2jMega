package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;

/**
 * Handles {@link Npc} random social animation after specified time.
 */
public final class RandomAnimationTaskManager implements Runnable
{
	private final Map<Npc, Long> _characters = new ConcurrentHashMap<>();
	
	protected RandomAnimationTaskManager()
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
		for (Map.Entry<Npc, Long> entry : _characters.entrySet())
		{
			// Time hasn't passed yet, skip.
			if (time < entry.getValue())
				continue;
			
			final Npc character = entry.getKey();
			
			// Cancels timer on specific cases.
			if (character.isMob())
			{
				// Cancel further animation timers until intention is changed to ACTIVE again.
				if (character.getAI().getIntention() != CtrlIntention.ACTIVE)
				{
					_characters.remove(character);
					continue;
				}
			}
			else
			{
				if (!character.isInActiveRegion()) // NPCs in inactive region don't run this task
				{
					_characters.remove(character);
					continue;
				}
			}
			
			if (!(character.isDead() || character.isStunned() || character.isSleeping() || character.isParalyzed()))
				character.onRandomAnimation(Rnd.get(2, 3));
			
			// Renew the timer.
			final int timer = (character.isMob()) ? Rnd.get(Config.MIN_MONSTER_ANIMATION, Config.MAX_MONSTER_ANIMATION) : Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION);
			add(character, timer);
		}
	}
	
	/**
	 * Adds {@link Npc} to the RandomAnimationTask with additional interval.
	 * @param character : {@link Npc} to be added.
	 * @param interval : Interval in seconds, after which the decay task is triggered.
	 */
	public final void add(Npc character, int interval)
	{
		_characters.put(character, System.currentTimeMillis() + interval * 1000);
	}
	
	public static final RandomAnimationTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		protected static final RandomAnimationTaskManager INSTANCE = new RandomAnimationTaskManager();
	}
}