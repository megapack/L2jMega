package net.sf.l2j.gameserver.model.actor.ai;

/**
 * A datatype used as a simple "wish" of an actor, consisting of an {@link CtrlIntention} and to up to 2 {@link Object}s of any type.
 */
public class Desire
{
	private final CtrlIntention _intention;
	
	private final Object _firstParameter;
	private final Object _secondParameter;
	
	public Desire(CtrlIntention intention, Object firstParameter, Object secondParameter)
	{
		_intention = intention;
		
		_firstParameter = firstParameter;
		_secondParameter = secondParameter;
	}
	
	public CtrlIntention getIntention()
	{
		return _intention;
	}
	
	public Object getFirstParameter()
	{
		return _firstParameter;
	}
	
	public Object getSecondParameter()
	{
		return _secondParameter;
	}
}