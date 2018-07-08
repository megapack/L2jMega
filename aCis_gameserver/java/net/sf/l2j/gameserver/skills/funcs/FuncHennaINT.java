package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncHennaINT extends Func
{
	static final FuncHennaINT _fh_instance = new FuncHennaINT();
	
	public static Func getInstance()
	{
		return _fh_instance;
	}
	
	private FuncHennaINT()
	{
		super(Stats.STAT_INT, 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final Player player = env.getPlayer();
		if (player != null)
			env.addValue(player.getHennaStatINT());
	}
}