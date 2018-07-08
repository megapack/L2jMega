package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.gameserver.skills.Env;

/**
 * @author mkizub
 */
public final class LambdaStats extends Lambda
{
	public enum StatsType
	{
		PLAYER_LEVEL,
		TARGET_LEVEL,
		PLAYER_MAX_HP,
		PLAYER_MAX_MP
	}
	
	private final StatsType _stat;
	
	public LambdaStats(StatsType stat)
	{
		_stat = stat;
	}
	
	@Override
	public double calc(Env env)
	{
		switch (_stat)
		{
			case PLAYER_LEVEL:
				return (env.getCharacter() == null) ? 1 : env.getCharacter().getLevel();
			
			case TARGET_LEVEL:
				return (env.getTarget() == null) ? 1 : env.getTarget().getLevel();
			
			case PLAYER_MAX_HP:
				return (env.getCharacter() == null) ? 1 : env.getCharacter().getMaxHp();
			
			case PLAYER_MAX_MP:
				return (env.getCharacter() == null) ? 1 : env.getCharacter().getMaxMp();
		}
		return 0;
	}
}