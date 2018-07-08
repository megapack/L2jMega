package net.sf.l2j.gameserver.model.actor.ai;

/**
 * Class for AI action after some event.
 * @author Yaroslav
 */
public class NextAction
{
	/** After which CtrlEvent is this action supposed to run. */
	private final CtrlEvent _event;
	
	/** What is the intention of the action, e.g. if AI gets this CtrlIntention set, NextAction is canceled. */
	private final CtrlIntention _intention;
	
	/** Wrapper for NextAction content. */
	private final Runnable _runnable;
	
	/**
	 * Single constructor.
	 * @param event : After which the NextAction is triggered.
	 * @param intention : CtrlIntention of the action.
	 * @param runnable :
	 */
	public NextAction(CtrlEvent event, CtrlIntention intention, Runnable runnable)
	{
		_event = event;
		_intention = intention;
		_runnable = runnable;
	}
	
	/**
	 * @return the _event
	 */
	public CtrlEvent getEvent()
	{
		return _event;
	}
	
	/**
	 * @return the _intention
	 */
	public CtrlIntention getIntention()
	{
		return _intention;
	}
	
	/**
	 * Do action.
	 */
	public void run()
	{
		_runnable.run();
	}
}