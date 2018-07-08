package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.ai.CtrlEvent;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.ai.Desire;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

public class CreatureAI extends AbstractAI
{
	public CreatureAI(Creature character)
	{
		super(character);
	}
	
	public Desire getNextIntention()
	{
		return null;
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		if (!(getActor() instanceof Folk))
			clientStartAutoAttack();
	}
	
	/**
	 * Manage the Idle Intention : Stop Attack, Movement and Stand Up the actor.
	 * <ul>
	 * <li>Set the AI Intention to IDLE</li>
	 * <li>Init cast and attack target</li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Stand up the actor server side AND client side by sending Server->Client packet ChangeWaitType (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionIdle()
	{
		// Set the AI Intention to IDLE
		changeIntention(CtrlIntention.IDLE, null, null);
		
		// Init cast and attack target
		setTarget(null);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
	}
	
	/**
	 * Manage the Active Intention : Stop Attack, Movement and Launch Think Event.
	 * <ul>
	 * <li>Set the AI Intention to ACTIVE</li>
	 * <li>Init cast and attack target</li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch the Think Event</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionActive()
	{
		// Check if the Intention is not already Active
		if (getIntention() != CtrlIntention.ACTIVE)
		{
			// Set the AI Intention to ACTIVE
			changeIntention(CtrlIntention.ACTIVE, null, null);
			
			// Init cast and attack target
			setTarget(null);
			
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			clientStopAutoAttack();
			
			// Also enable random animations for this Creature if allowed
			// This is only for mobs - town npcs are handled in their constructor
			if (_actor instanceof Attackable)
				((Npc) _actor).startRandomAnimationTimer();
			
			// Launch the Think Event
			onEvtThink();
		}
	}
	
	/**
	 * Manage the Rest Intention. Set the AI Intention to IDLE.
	 */
	@Override
	protected void onIntentionRest()
	{
		setIntention(CtrlIntention.IDLE);
	}
	
	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event.
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to ATTACK</li>
	 * <li>Set or change the AI attack target</li>
	 * <li>Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart (broadcast)</li>
	 * <li>Launch the Think Event</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionAttack(Creature target)
	{
		if (target == null)
		{
			clientActionFailed();
			return;
		}
		
		if (getIntention() == CtrlIntention.REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAfraid())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		// Check if the Intention is already ATTACK
		if (getIntention() == CtrlIntention.ATTACK)
		{
			// Check if the AI already targets the Creature
			if (getTarget() != target)
			{
				// Set the AI attack target (change target)
				setTarget(target);
				
				stopFollow();
				
				// Launch the Think Event
				notifyEvent(CtrlEvent.EVT_THINK, null);
			}
			else
				clientActionFailed(); // else client freezes until cancel target
		}
		else
		{
			// Set the Intention of this AbstractAI to ATTACK
			changeIntention(CtrlIntention.ATTACK, target, null);
			
			// Set the AI attack target
			setTarget(target);
			
			stopFollow();
			
			// Launch the Think Event
			notifyEvent(CtrlEvent.EVT_THINK, null);
		}
	}
	
	/**
	 * Manage the Cast Intention : Stop current Attack, Init the AI in order to cast and Launch Think Event.
	 * <ul>
	 * <li>Set the AI cast target</li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Cancel action client side by sending Server->Client packet ActionFailed to the Player actor</li>
	 * <li>Set the AI skill used by INTENTION_CAST</li>
	 * <li>Set the Intention of this AI to CAST</li>
	 * <li>Launch the Think Event</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionCast(L2Skill skill, WorldObject target)
	{
		if (getIntention() == CtrlIntention.REST && skill.isMagic())
		{
			clientActionFailed();
			_actor.setIsCastingNow(false);
			return;
		}
		
		// Set the AI cast target
		setTarget(target);
		
		// Set the AI skill used by INTENTION_CAST
		_skill = skill;
		
		// Change the Intention of this AbstractAI to CAST
		changeIntention(CtrlIntention.CAST, skill, target);
		
		// Launch the Think Event
		notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	
	/**
	 * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.
	 * <ul>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to MOVE_TO</li>
	 * <li>Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionMoveTo(Location loc)
	{
		if (getIntention() == CtrlIntention.REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		// Set the Intention of this AbstractAI to MOVE_TO
		changeIntention(CtrlIntention.MOVE_TO, loc, null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Abort the attack of the Creature and send Server->Client ActionFailed packet
		_actor.abortAttack();
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
		moveTo(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Manage the Follow Intention : Stop current Attack and Launch a Follow Task.
	 * <ul>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to FOLLOW</li>
	 * <li>Create and Launch an AI Follow Task to execute every 1s</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionFollow(Creature target)
	{
		if (getIntention() == CtrlIntention.REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isMovementDisabled())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		// Dead actors can`t follow
		if (_actor.isDead())
		{
			clientActionFailed();
			return;
		}
		
		// do not follow yourself
		if (_actor == target)
		{
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Set the Intention of this AbstractAI to FOLLOW
		changeIntention(CtrlIntention.FOLLOW, target, null);
		
		// Create and Launch an AI Follow Task to execute every 1s
		startFollow(target);
	}
	
	/**
	 * Manage the PickUp Intention : Set the pick up target and Launch a Move To Pawn Task (offset=20).
	 * <ul>
	 * <li>Set the AI pick up target</li>
	 * <li>Set the Intention of this AI to PICK_UP</li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionPickUp(WorldObject object)
	{
		// Actor is resting, return.
		if (getIntention() == CtrlIntention.REST)
		{
			clientActionFailed();
			return;
		}
		
		// Actor is currently busy casting, return.
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		if (object instanceof ItemInstance && ((ItemInstance) object).getLocation() != ItemLocation.VOID)
			return;
		
		// Set the Intention of this AbstractAI to PICK_UP
		changeIntention(CtrlIntention.PICK_UP, object, null);
		
		// Set the AI pick up target
		setTarget(object);
		
		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 20);
	}
	
	/**
	 * Manage the Interact Intention : Set the interact target and Launch a Move To Pawn Task (offset=60).
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the AI interact target</li>
	 * <li>Set the Intention of this AI to INTERACT</li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionInteract(WorldObject object)
	{
		if (getIntention() == CtrlIntention.REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		if (getIntention() != CtrlIntention.INTERACT || _intentionArg0 != object)
		{
			// Set the Intention of this AbstractAI to INTERACT
			changeIntention(CtrlIntention.INTERACT, object, null);
			
			// Set the AI interact target
			setTarget(object);
			
			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			moveToPawn(object, 60);
		}
		else
			clientActionFailed();
	}
	
	@Override
	protected void onEvtThink()
	{
		// do nothing
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		// do nothing
	}
	
	/**
	 * Launch actions corresponding to the Event Stunned then onAttacked Event.
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * <li>Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)</li>
	 * </ul>
	 */
	@Override
	protected void onEvtStunned(Creature attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		AttackStanceTaskManager.getInstance().remove(_actor);
		
		// Stop Server AutoAttack also
		setAutoAttacking(false);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Paralyzed then onAttacked Event.
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * <li>Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)</li>
	 * </ul>
	 */
	@Override
	protected void onEvtParalyzed(Creature attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		AttackStanceTaskManager.getInstance().remove(_actor);
		
		// Stop Server AutoAttack also
		setAutoAttacking(false);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Sleeping.
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * </ul>
	 */
	@Override
	protected void onEvtSleeping(Creature attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		AttackStanceTaskManager.getInstance().remove(_actor);
		
		// stop Server AutoAttack also
		setAutoAttacking(false);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
	}
	
	/**
	 * Launch actions corresponding to the Event Rooted.
	 * <ul>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li>
	 * </ul>
	 */
	@Override
	protected void onEvtRooted(Creature attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Confused.
	 * <ul>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li>
	 * </ul>
	 */
	@Override
	protected void onEvtConfused(Creature attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Muted.
	 * <ul>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * </ul>
	 */
	@Override
	protected void onEvtMuted(Creature attacker)
	{
		// Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtEvaded(Creature attacker)
	{
		// do nothing
	}
	
	/**
	 * Launch actions corresponding to the Event ReadyToAct.
	 * <ul>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event Arrived.
	 * <ul>
	 * <li>If the Intention was MOVE_TO, set the Intention to ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtArrived()
	{
		_actor.revalidateZone(true);
		
		if (_actor.moveToNextRoutePoint())
			return;
		
		if (_actor instanceof Attackable)
			((Attackable) _actor).setIsReturningToSpawnPoint(false);
		
		clientStoppedMoving();
		
		// If the Intention was MOVE_TO, set the Intention to ACTIVE
		if (getIntention() == CtrlIntention.MOVE_TO)
			setIntention(CtrlIntention.ACTIVE);
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event ArrivedBlocked.
	 * <ul>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>If the Intention was MOVE_TO, set the Intention to ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtArrivedBlocked(SpawnLocation loc)
	{
		// If the Intention was MOVE_TO, set the Intention to ACTIVE
		if (getIntention() == CtrlIntention.MOVE_TO || getIntention() == CtrlIntention.CAST)
			setIntention(CtrlIntention.ACTIVE);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(loc);
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event Cancel.
	 * <ul>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtCancel()
	{
		_actor.abortCast();
		
		// Stop an AI Follow Task
		stopFollow();
		
		if (!AttackStanceTaskManager.getInstance().isInAttackStance(_actor))
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event Dead.
	 * <ul>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onEvtDead()
	{
		// Stop an AI Tasks
		stopAITask();
		
		// Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)
		clientNotifyDead();
		
		if (!(_actor instanceof Playable))
			_actor.setWalking();
	}
	
	/**
	 * Launch actions corresponding to the Event Fake Death.
	 * <ul>
	 * <li>Stop an AI Follow Task</li>
	 * </ul>
	 */
	@Override
	protected void onEvtFakeDeath()
	{
		// Stop an AI Follow Task
		stopFollow();
		
		// Stop the actor movement and send Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Init AI
		_intention = CtrlIntention.IDLE;
		setTarget(null);
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		// do nothing
	}
	
	protected boolean maybeMoveToPosition(Location worldPosition, int offset)
	{
		if (worldPosition == null)
		{
			_log.warning("maybeMoveToPosition: worldPosition == NULL!");
			return false;
		}
		
		if (offset < 0)
			return false; // skill radius -1
			
		if (!_actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), (int) (offset + _actor.getCollisionRadius()), false))
		{
			if (_actor.isMovementDisabled())
				return true;
			
			if (!(this instanceof PlayerAI) && !(this instanceof SummonAI))
				_actor.setRunning();
			
			stopFollow();
			
			int x = _actor.getX();
			int y = _actor.getY();
			
			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;
			
			double dist = Math.sqrt(dx * dx + dy * dy);
			
			double sin = dy / dist;
			double cos = dx / dist;
			
			dist -= offset - 5;
			
			x += (int) (dist * cos);
			y += (int) (dist * sin);
			
			moveTo(x, y, worldPosition.getZ());
			return true;
		}
		
		if (getFollowTarget() != null)
			stopFollow();
		
		return false;
	}
	
	/**
	 * Manage the Move to Pawn action in function of the distance and of the Interact area.
	 * <ul>
	 * <li>Get the distance between the current position of the Creature and the target (x,y)</li>
	 * <li>If the distance > offset+20, move the actor (by running) to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * <li>If the distance <= offset+20, Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * </ul>
	 * @param target The targeted WorldObject
	 * @param offset The Interact area radius
	 * @return True if a movement must be done
	 */
	protected boolean maybeMoveToPawn(WorldObject target, int offset)
	{
		if (target == null || offset < 0) // skill radius -1
			return false;
		
		offset += _actor.getCollisionRadius();
		if (target instanceof Creature)
			offset += ((Creature) target).getCollisionRadius();
		
		if (!_actor.isInsideRadius(target, offset, false, false))
		{
			if (getFollowTarget() != null)
			{
				// allow larger hit range when the target is moving (check is run only once per second)
				if (!_actor.isInsideRadius(target, offset + 100, false, false))
					return true;
				
				stopFollow();
				return false;
			}
			
			if (_actor.isMovementDisabled())
			{
				if (getIntention() == CtrlIntention.ATTACK)
				{
					setIntention(CtrlIntention.IDLE);
					clientActionFailed();
				}
				
				return true;
			}
			
			// If not running, set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
			if (!(this instanceof PlayerAI) && !(this instanceof SummonAI))
				_actor.setRunning();
			
			stopFollow();
			
			if (target instanceof Creature && !(target instanceof Door))
			{
				if (((Creature) target).isMoving())
					offset -= 30;
				
				if (offset < 5)
					offset = 5;
				
				startFollow((Creature) target, offset);
			}
			else
			{
				// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				moveToPawn(target, offset);
			}
			return true;
		}
		
		if (getFollowTarget() != null)
			stopFollow();
		
		return false;
	}
	
	/**
	 * Modify current Intention and actions if the target is lost or dead.
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to ACTIVE</li>
	 * </ul>
	 * @param target The targeted WorldObject
	 * @return True if the target is lost or dead (false if fakedeath)
	 */
	protected boolean checkTargetLostOrDead(Creature target)
	{
		if (target == null || target.isAlikeDead())
		{
			if (target instanceof Player && ((Player) target).isFakeDeath())
			{
				target.stopFakeDeath(true);
				return false;
			}
			
			// Set the Intention of this AbstractAI to ACTIVE
			setIntention(CtrlIntention.ACTIVE);
			return true;
		}
		return false;
	}
	
	/**
	 * Modify current Intention and actions if the target is lost.
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to ACTIVE</li>
	 * </ul>
	 * @param target The targeted WorldObject
	 * @return True if the target is lost
	 */
	protected boolean checkTargetLost(WorldObject target)
	{
		if (target instanceof Player)
		{
			final Player victim = (Player) target;
			if (victim.isFakeDeath())
			{
				victim.stopFakeDeath(true);
				return false;
			}
		}
		
		if (target == null)
		{
			// Set the Intention of this AbstractAI to ACTIVE
			setIntention(CtrlIntention.ACTIVE);
			return true;
		}
		return false;
	}
	
	public boolean canAura(L2Skill sk)
	{
		if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
		{
			for (WorldObject target : _actor.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
			{
				if (target == getTarget())
					return true;
			}
		}
		return false;
	}
	
	public boolean canAOE(L2Skill sk)
	{
		if (sk.getSkillType() != L2SkillType.NEGATE || sk.getSkillType() != L2SkillType.CANCEL)
		{
			if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
			{
				boolean cancast = true;
				for (Creature target : _actor.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
						continue;
					
					if (target instanceof Attackable && !_actor.isConfused())
						continue;
					
					if (target.getFirstEffect(sk) != null)
						cancast = false;
				}
				
				if (cancast)
					return true;
			}
			else if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AREA)
			{
				boolean cancast = true;
				for (Creature target : ((Creature) getTarget()).getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
						continue;
					
					if (target instanceof Attackable && !_actor.isConfused())
						continue;
					
					L2Effect[] effects = target.getAllEffects();
					if (effects.length > 0)
						cancast = true;
				}
				if (cancast)
					return true;
			}
		}
		else
		{
			if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
			{
				boolean cancast = false;
				for (Creature target : _actor.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
						continue;
					
					if (target instanceof Attackable && !_actor.isConfused())
						continue;
					
					L2Effect[] effects = target.getAllEffects();
					if (effects.length > 0)
						cancast = true;
				}
				if (cancast)
					return true;
			}
			else if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AREA)
			{
				boolean cancast = true;
				for (Creature target : ((Creature) getTarget()).getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
						continue;
					
					if (target instanceof Attackable && !_actor.isConfused())
						continue;
					
					if (target.getFirstEffect(sk) != null)
						cancast = false;
				}
				
				if (cancast)
					return true;
			}
		}
		return false;
	}
	
	public boolean canParty(L2Skill sk)
	{
		if (sk.getTargetType() != L2Skill.SkillTargetType.TARGET_PARTY)
			return false;
		
		int count = 0;
		int ccount = 0;
		
		final String[] actorClans = ((Npc) _actor).getTemplate().getClans();
		for (Attackable target : _actor.getKnownTypeInRadius(Attackable.class, sk.getSkillRadius()))
		{
			if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
				continue;
			
			if (!ArraysUtil.contains(actorClans, target.getTemplate().getClans()))
				continue;
			
			count++;
			
			if (target.getFirstEffect(sk) != null)
				ccount++;
		}
		
		if (ccount < count)
			return true;
		
		return false;
	}
}