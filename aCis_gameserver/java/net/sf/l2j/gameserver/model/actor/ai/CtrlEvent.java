package net.sf.l2j.gameserver.model.actor.ai;

/**
 * This class contains an enum of each possible event, that can happen on an AI character.
 */
public enum CtrlEvent
{
	/** Something has changed, usually a previous step has being completed or maybe was completed, the AI must thing on next action. */
	EVT_THINK,
	/** The actor was attacked. This event comes each time a physical or magical attack was done on the actor. NPC may start attack in response, or ignore this event if they already attack someone, or change target and so on. */
	EVT_ATTACKED,
	/** Increase/decrease aggression towards a target, or reduce global aggression if target is null. */
	EVT_AGGRESSION,
	/** Actor is in stun state. */
	EVT_STUNNED,
	/** Actor is paralyzed or petrified. */
	EVT_PARALYZED,
	/** Actor starts/stops sleeping. */
	EVT_SLEEPING,
	/** Actor is in rooted state (cannot move). */
	EVT_ROOTED,
	/** Actor evaded hit. */
	EVT_EVADED,
	/** An event that previous action was completed. The action may be an attempt to physically or magically hit an enemy, or an action that discarded attack attempt has finished. */
	EVT_READY_TO_ACT,
	/** The actor arrived to assigned location, or it's a time to modify movement destination (follow, interact, random move and others intentions). */
	EVT_ARRIVED,
	/** The actor cannot move anymore. */
	EVT_ARRIVED_BLOCKED,
	/**
	 * Attempt to cancel current step execution, but not change the intention. For example, the actor was put into a stun, so it's current attack or movement has to be canceled. But after the stun state expired, the actor may try to attack again. Another usage for CANCEL is a user's attempt to
	 * cancel a cast/bow attack and so on.
	 */
	EVT_CANCEL,
	/** The character is dead. */
	EVT_DEAD,
	/** The character looks like dead. */
	EVT_FAKE_DEATH,
	/** The character attack anyone randomly. */
	EVT_CONFUSED,
	/** The character cannot cast spells anymore. */
	EVT_MUTED,
	/** The character flee in random directions */
	EVT_AFRAID,
	/** The character finish casting. */
	EVT_FINISH_CASTING
}