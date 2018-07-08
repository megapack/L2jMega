package net.sf.l2j.gameserver.scripting.scripts.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.ai.CtrlEvent;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.instance.RiftInvader;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.scripting.EventType;
import net.sf.l2j.gameserver.scripting.Quest;

public class L2AttackableAIScript extends Quest
{
	public L2AttackableAIScript()
	{
		super(-1, "ai");
		
		registerNpcs();
	}
	
	public L2AttackableAIScript(String name)
	{
		super(-1, name);
		
		registerNpcs();
	}
	
	protected void registerNpcs()
	{
		// register all mobs here...
		for (NpcTemplate template : NpcData.getInstance().getAllNpcs())
		{
			try
			{
				if (Attackable.class.isAssignableFrom(Class.forName("net.sf.l2j.gameserver.model.actor.instance." + template.getType())))
				{
					template.addQuestEvent(EventType.ON_ATTACK, this);
					template.addQuestEvent(EventType.ON_KILL, this);
					template.addQuestEvent(EventType.ON_SPAWN, this);
					template.addQuestEvent(EventType.ON_SKILL_SEE, this);
					template.addQuestEvent(EventType.ON_FACTION_CALL, this);
					template.addQuestEvent(EventType.ON_AGGRO, this);
				}
			}
			catch (ClassNotFoundException ex)
			{
				_log.info("Class not found: " + template.getType());
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		return null;
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, L2Skill skill)
	{
		return null;
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		if (caster == null)
			return null;
		
		if (!(npc instanceof Attackable))
			return null;
		
		Attackable attackable = (Attackable) npc;
		int skillAggroPoints = skill.getAggroPoints();
		
		if (caster.getPet() != null)
		{
			if (targets.length == 1 && ArraysUtil.contains(targets, caster.getPet()))
				skillAggroPoints = 0;
		}
		
		if (skillAggroPoints > 0)
		{
			if (attackable.hasAI() && (attackable.getAI().getIntention() == CtrlIntention.ATTACK))
			{
				WorldObject npcTarget = attackable.getTarget();
				for (WorldObject skillTarget : targets)
				{
					if (npcTarget == skillTarget || npc == skillTarget)
					{
						Creature originalCaster = isPet ? caster.getPet() : caster;
						attackable.addDamageHate(originalCaster, 0, (skillAggroPoints * 150) / (attackable.getLevel() + 7));
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet)
	{
		if (attacker == null)
			return null;
		
		if (attacker.isInParty() && attacker.getParty().isInDimensionalRift())
		{
			byte riftType = attacker.getParty().getDimensionalRift().getType();
			byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();
			
			if (caller instanceof RiftInvader && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
				return null;
		}
		
		final Attackable attackable = (Attackable) npc;
		final Creature originalAttackTarget = (isPet ? attacker.getPet() : attacker);
		
		// Add the target to the actor _aggroList or update hate if already present
		attackable.addDamageHate(originalAttackTarget, 0, 1);
		
		// Set the actor AI Intention to ATTACK
		if (attackable.getAI().getIntention() != CtrlIntention.ATTACK)
		{
			// Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
			attackable.setRunning();
			
			attackable.getAI().setIntention(CtrlIntention.ATTACK, originalAttackTarget);
		}
		return null;
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		if (player == null)
			return null;
		
		((Attackable) npc).addDamageHate(isPet ? player.getPet() : player, 0, 1);
		return null;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (attacker != null && npc instanceof Attackable)
		{
			Attackable attackable = (Attackable) npc;
			Creature originalAttacker = isPet ? attacker.getPet() : attacker;
			
			attackable.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, originalAttacker);
			attackable.addDamageHate(originalAttacker, damage, (damage * 100) / (attackable.getLevel() + 7));
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		if (npc instanceof Monster)
		{
			final Monster mob = (Monster) npc;
			if (mob.getLeader() != null)
				mob.getLeader().getMinionList().onMinionDie(mob, -1);
			
			if (mob.hasMinions())
				mob.getMinionList().onMasterDie(false);
		}
		return null;
	}
	
	// TODO: MERGE SCRIPTS
	
	/**
	 * This method selects a random player.<br>
	 * Player can't be dead and isn't an hidden GM aswell.
	 * @param npc to check.
	 * @return the random player.
	 */
	public static Player getRandomPlayer(Npc npc)
	{
		List<Player> result = new ArrayList<>();
		
		for (Player player : npc.getKnownType(Player.class))
		{
			if (player.isDead())
				continue;
			
			if (player.isGM() && player.getAppearance().getInvisible())
				continue;
			
			result.add(player);
		}
		
		return (result.isEmpty()) ? null : Rnd.get(result);
	}
	
	/**
	 * Return the number of players in a defined radius.<br>
	 * Dead players aren't counted, invisible ones is the boolean parameter.
	 * @param range : the radius.
	 * @param npc : the object to make the test on.
	 * @param invisible : true counts invisible characters.
	 * @return the number of targets found.
	 */
	public static int getPlayersCountInRadius(int range, Creature npc, boolean invisible)
	{
		int count = 0;
		for (Player player : npc.getKnownTypeInRadius(Player.class, range))
		{
			if (player.isDead())
				continue;
			
			if (!invisible && player.getAppearance().getInvisible())
				continue;
			
			count++;
		}
		return count;
	}
	
	/**
	 * Under that barbarian name, return the number of players in front, back and sides of the npc.<br>
	 * Dead players aren't counted, invisible ones is the boolean parameter.
	 * @param range : the radius.
	 * @param npc : the object to make the test on.
	 * @param invisible : true counts invisible characters.
	 * @return an array composed of front, back and side targets number.
	 */
	public static int[] getPlayersCountInPositions(int range, Creature npc, boolean invisible)
	{
		int frontCount = 0;
		int backCount = 0;
		int sideCount = 0;
		
		for (Player player : npc.getKnownType(Player.class))
		{
			if (player.isDead())
				continue;
			
			if (!invisible && player.getAppearance().getInvisible())
				continue;
			
			if (!MathUtil.checkIfInRange(range, npc, player, true))
				continue;
			
			if (player.isInFrontOf(npc))
				frontCount++;
			else if (player.isBehind(npc))
				backCount++;
			else
				sideCount++;
		}
		
		int[] array =
		{
			frontCount,
			backCount,
			sideCount
		};
		return array;
	}
	
	/**
	 * Monster runs and attacks the playable.
	 * @param npc The npc to use.
	 * @param playable The victim.
	 * @param aggro The aggro to add, 999 if not given.
	 */
	public static void attack(Attackable npc, Playable playable, int aggro)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(playable, 0, (aggro <= 0) ? 999 : aggro);
		npc.getAI().setIntention(CtrlIntention.ATTACK, playable);
	}
	
	public static void attack(Attackable npc, Playable playable)
	{
		attack(npc, playable, 0);
	}
}