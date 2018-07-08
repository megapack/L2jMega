package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.EventType;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.templates.StatsSet;

public class QueenAnt extends L2AttackableAIScript
{
	private static final int QUEEN = 29001;
	private static final int LARVA = 29002;
	private static final int NURSE = 29003;
	private static final int GUARD = 29004;
	private static final int ROYAL = 29005;
	
	private static final int[] MOBS =
	{
		QUEEN,
		LARVA,
		NURSE,
		GUARD,
		ROYAL
	};
	
	private static final int QUEEN_X = -21610;
	private static final int QUEEN_Y = 181594;
	private static final int QUEEN_Z = -5734;
	
	// Status Tracking
	private static final byte ALIVE = 0; // Queen Ant is spawned.
	private static final byte DEAD = 1; // Queen Ant has been killed.
	
	private static final IntIntHolder HEAL1 = new IntIntHolder(4020, 1);
	private static final IntIntHolder HEAL2 = new IntIntHolder(4024, 1);
	
	private static final List<Monster> _nurses = new ArrayList<>(5);
	
	private Monster _queen = null;
	private Monster _larva = null;
	
	public QueenAnt()
	{
		super("ai/individual");
		
		StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
		if (GrandBossManager.getInstance().getBossStatus(QUEEN) == DEAD)
		{
			// load the unlock date and time for queen ant from DB
			long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			
			// the unlock time has not yet expired.
			if (temp > 0)
				startQuestTimer("queen_unlock", temp, null, null, false);
			// the time has already expired while the server was offline. Immediately spawn queen ant.
			else
			{
				GrandBoss queen = (GrandBoss) addSpawn(QUEEN, QUEEN_X, QUEEN_Y, QUEEN_Z, 0, false, 0, false);
				GrandBossManager.getInstance().setBossStatus(QUEEN, ALIVE);
				spawnBoss(queen);
			}
		}
		else
		{
			int loc_x = info.getInteger("loc_x");
			int loc_y = info.getInteger("loc_y");
			int loc_z = info.getInteger("loc_z");
			int heading = info.getInteger("heading");
			int hp = info.getInteger("currentHP");
			int mp = info.getInteger("currentMP");
			
			GrandBoss queen = (GrandBoss) addSpawn(QUEEN, loc_x, loc_y, loc_z, heading, false, 0, false);
			queen.setCurrentHpMp(hp, mp);
			spawnBoss(queen);
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(MOBS, EventType.ON_SPAWN, EventType.ON_KILL, EventType.ON_AGGRO);
		addFactionCallId(NURSE);
	}
	
	private void spawnBoss(GrandBoss npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		startQuestTimer("action", 10000, npc, null, true);
		startQuestTimer("heal", 1000, null, null, true);
		npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
		
		_queen = npc;
		_larva = (Monster) addSpawn(LARVA, -21600, 179482, -5846, Rnd.get(360), false, 0, false);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("heal"))
		{
			boolean notCasting;
			final boolean larvaNeedHeal = _larva != null && _larva.getCurrentHp() < _larva.getMaxHp();
			final boolean queenNeedHeal = _queen != null && _queen.getCurrentHp() < _queen.getMaxHp();
			for (Monster nurse : _nurses)
			{
				if (nurse == null || nurse.isDead() || nurse.isCastingNow())
					continue;
				
				notCasting = nurse.getAI().getIntention() != CtrlIntention.CAST;
				if (larvaNeedHeal)
				{
					if (nurse.getTarget() != _larva || notCasting)
					{
						nurse.setTarget(_larva);
						nurse.useMagic(Rnd.nextBoolean() ? HEAL1.getSkill() : HEAL2.getSkill());
					}
					continue;
				}
				
				if (queenNeedHeal)
				{
					if (nurse.getLeader() == _larva) // skip larva's minions
						continue;
					
					if (nurse.getTarget() != _queen || notCasting)
					{
						nurse.setTarget(_queen);
						nurse.useMagic(HEAL1.getSkill());
					}
					continue;
				}
				
				// if nurse not casting - remove target
				if (notCasting && nurse.getTarget() != null)
					nurse.setTarget(null);
			}
		}
		else if (event.equalsIgnoreCase("action") && npc != null)
		{
			if (Rnd.get(3) == 0)
			{
				if (Rnd.get(2) == 0)
					npc.broadcastPacket(new SocialAction(npc, 3));
				else
					npc.broadcastPacket(new SocialAction(npc, 4));
			}
		}
		else if (event.equalsIgnoreCase("queen_unlock"))
		{
			GrandBoss queen = (GrandBoss) addSpawn(QUEEN, QUEEN_X, QUEEN_Y, QUEEN_Z, 0, false, 0, false);
			GrandBossManager.getInstance().setBossStatus(QUEEN, ALIVE);
			spawnBoss(queen);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		final Monster mob = (Monster) npc;
		switch (npc.getNpcId())
		{
			case LARVA:
				mob.setIsImmobilized(true);
				mob.setIsMortal(false);
				mob.setIsRaidMinion(true);
				break;
			case NURSE:
				mob.disableCoreAI(true);
				mob.setIsRaidMinion(true);
				_nurses.add(mob);
				break;
			case ROYAL:
			case GUARD:
				mob.setIsRaidMinion(true);
				break;
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet)
	{
		if (caller == null || npc == null)
			return super.onFactionCall(npc, caller, attacker, isPet);
		
		if (!npc.isCastingNow() && npc.getAI().getIntention() != CtrlIntention.CAST)
		{
			if (caller.getCurrentHp() < caller.getMaxHp())
			{
				npc.setTarget(caller);
				((Attackable) npc).useMagic(HEAL1.getSkill());
			}
		}
		return null;
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		if (npc == null)
			return null;
		
		final boolean isMage;
		final Playable character;
		if (isPet)
		{
			isMage = false;
			character = player.getPet();
		}
		else
		{
			isMage = player.isMageClass();
			character = player;
		}
		
		if (character == null)
			return null;
		
		if (!Config.RAID_DISABLE_CURSE && character.getLevel() - npc.getLevel() > 8)
		{
			L2Skill curse = null;
			if (isMage)
			{
				if (!character.isMuted() && Rnd.get(4) == 0)
					curse = FrequentSkill.RAID_CURSE.getSkill();
			}
			else
			{
				if (!character.isParalyzed() && Rnd.get(4) == 0)
					curse = FrequentSkill.RAID_CURSE2.getSkill();
			}
			
			if (curse != null)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, character, curse.getId(), curse.getLevel(), 300, 0));
				curse.getEffects(npc, character);
			}
			
			((Attackable) npc).stopHating(character); // for calling again
			return null;
		}
		return super.onAggro(npc, player, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		// Acts only once.
		if (GrandBossManager.getInstance().getBossStatus(QUEEN) == ALIVE)
		{
			int npcId = npc.getNpcId();
			if (npcId == QUEEN)
			{
				npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
				GrandBossManager.getInstance().setBossStatus(QUEEN, DEAD);
				
				long respawnTime = (long) Config.SPAWN_INTERVAL_AQ + Rnd.get(-Config.RANDOM_SPAWN_TIME_AQ, Config.RANDOM_SPAWN_TIME_AQ);
				respawnTime *= 3600000;
				
				startQuestTimer("queen_unlock", respawnTime, null, null, false);
				cancelQuestTimer("action", npc, null);
				cancelQuestTimer("heal", null, null);
				
				// also save the respawn time so that the info is maintained past reboots
				StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
				info.set("respawn_time", System.currentTimeMillis() + respawnTime);
				GrandBossManager.getInstance().setStatsSet(QUEEN, info);
				
				_nurses.clear();
				_larva.deleteMe();
				_larva = null;
				_queen = null;
			}
			else
			{
				if (npcId == ROYAL)
				{
					Monster mob = (Monster) npc;
					if (mob.getLeader() != null)
						mob.getLeader().getMinionList().onMinionDie(mob, (280 + Rnd.get(40)) * 1000);
				}
				else if (npcId == NURSE)
				{
					Monster mob = (Monster) npc;
					_nurses.remove(mob);
					if (mob.getLeader() != null)
						mob.getLeader().getMinionList().onMinionDie(mob, 10000);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
}