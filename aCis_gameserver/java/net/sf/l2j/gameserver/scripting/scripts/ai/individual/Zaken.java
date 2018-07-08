package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import java.util.Calendar;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.scripting.EventType;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.templates.StatsSet;

public class Zaken
  extends L2AttackableAIScript
{
  private static final BossZone _zakenLair = ZoneManager.getInstance().getZoneById(110000, BossZone.class);
  public static boolean _open;
  private WorldObject _target;
  int _telecheck;
  private int _minionStatus = 0;
  private int hate = 0;
  private static final int[] Xcoords = { 53950, 55980, 54950, 55970, 53930, 55970, 55980, 54960, 53950, 53930, 55970, 55980, 54960, 53950, 53930 };
  private static final int[] Ycoords = { 219860, 219820, 218790, 217770, 217760, 217770, 219920, 218790, 219860, 217760, 217770, 219920, 218790, 219860, 217760 };
  private static final int[] Zcoords = { 62048, 62048, 62048, 62048, 62048, 62320, 62320, 62320, 62320, 62320, 62592, 62592, 62592, 62592, 62592 };

  
  public Zaken()
  {
    super("ai/individual");
    
    final int status = GrandBossManager.getInstance().getBossStatus(29022);
    
    ThreadPool.scheduleAtFixedRate(new Runnable()
    {
      @Override
	public void run()
      {
        if ((status == 0) && (!Zaken._open)) {
          if (!Zaken._open)
          {
            DoorData.getInstance().getDoor(21240006).openMe();
            ThreadPool.schedule(new Runnable()
            {
              @Override
			public void run()
              {
                DoorData.getInstance().getDoor(21240006).closeMe();
              }
            }, Config.WAIT_TIME_ZAKEN);
            
            Zaken._open = true;
            Zaken.waiter(Config.WAIT_TIME_ZAKEN);
          }
        }
      }
    }, 600000L, 600000L);
    
    registerNpcs();
    
    StatsSet info = GrandBossManager.getInstance().getStatsSet(29022);
    if (status == 1)
    {
      long temp = info.getLong("respawn_time") - System.currentTimeMillis();
      if (temp > 0L)
      {
        startQuestTimer("zaken_unlock", temp, null, null, false);
      }
      else
      {
        int i1 = Rnd.get(15);
        GrandBoss zaken = (GrandBoss)addSpawn(29022, Xcoords[i1], Ycoords[i1], Zcoords[i1], i1, false, 0L, false);
        GrandBossManager.getInstance().setBossStatus(29022, 0);
        spawnBoss(zaken);
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
      
      GrandBoss zaken = (GrandBoss)addSpawn(29022, loc_x, loc_y, loc_z, heading, false, 0L, false);
      zaken.setCurrentHpMp(hp, mp);
      spawnBoss(zaken);
    }
  }
  
  @Override
protected void registerNpcs()
  {
    addEventIds(29022, new EventType[] { EventType.ON_ATTACK, EventType.ON_KILL, EventType.ON_SPAWN, EventType.ON_SPELL_FINISHED, EventType.ON_SKILL_SEE, EventType.ON_FACTION_CALL, EventType.ON_AGGRO });
    addEventIds(29023, new EventType[] { EventType.ON_ATTACK, EventType.ON_KILL, EventType.ON_SPAWN, EventType.ON_SPELL_FINISHED, EventType.ON_SKILL_SEE, EventType.ON_FACTION_CALL, EventType.ON_AGGRO });
    addEventIds(29024, new EventType[] { EventType.ON_ATTACK, EventType.ON_KILL, EventType.ON_SPAWN, EventType.ON_SPELL_FINISHED, EventType.ON_SKILL_SEE, EventType.ON_FACTION_CALL, EventType.ON_AGGRO });
    addEventIds(29026, new EventType[] { EventType.ON_ATTACK, EventType.ON_KILL, EventType.ON_SPAWN, EventType.ON_SPELL_FINISHED, EventType.ON_SKILL_SEE, EventType.ON_FACTION_CALL, EventType.ON_AGGRO });
    addEventIds(29027, new EventType[] { EventType.ON_ATTACK, EventType.ON_KILL, EventType.ON_SPAWN, EventType.ON_SPELL_FINISHED, EventType.ON_SKILL_SEE, EventType.ON_FACTION_CALL, EventType.ON_AGGRO });
  }
  
  public void spawnBoss(GrandBoss npc)
  {
    if (npc == null)
    {
      _log.warning("Zaken AI failed to load, missing Zaken in grandboss_data.sql");
      return;
    }
    GrandBossManager.getInstance().addBoss(npc);
    
    npc.broadcastPacket(new PlaySound(1, "BS01_A", npc));
    
    this.hate = 0;
    if (_zakenLair == null)
    {
      _log.warning("Zaken AI failed to load, missing zone for Zaken");
      return;
    }
    if (_zakenLair.isInsideZone(npc))
    {
      this._minionStatus = 1;
      startQuestTimer("minion_cycle", 1700L, null, null, true);
    }
    this._telecheck = 3;
    startQuestTimer("timer", 1000L, npc, null, false);
  }
  
  @Override
public String onAdvEvent(String event, Npc npc, Player player)
  {
    int status = GrandBossManager.getInstance().getBossStatus(29022);
    if ((status == 1) && (!event.equalsIgnoreCase("zaken_unlock"))) {
      return super.onAdvEvent(event, npc, player);
    }
    if (event.equalsIgnoreCase("timer"))
    {
      Creature _mostHated = null;
      if ((npc.getAI().getIntention() == CtrlIntention.ATTACK) && (this.hate == 0))
      {
        if (((Attackable)npc).getMostHated() != null)
        {
          _mostHated = ((Attackable)npc).getMostHated();
          this.hate = 1;
        }
      }
      else if ((npc.getAI().getIntention() == CtrlIntention.ATTACK) && (this.hate != 0)) {
        if (((Attackable)npc).getMostHated() != null) {
          if (_mostHated == ((Attackable)npc).getMostHated())
          {
            this.hate += 1;
          }
          else
          {
            this.hate = 1;
            _mostHated = ((Attackable)npc).getMostHated();
          }
        }
      }
      if (npc.getAI().getIntention() == CtrlIntention.IDLE) {
        this.hate = 0;
      }
      if (this.hate > 5)
      {
        ((Attackable)npc).stopHating(_mostHated);
        Creature nextTarget = ((Attackable)npc).getMostHated();
        if (nextTarget != null) {
          npc.getAI().setIntention(CtrlIntention.ATTACK, nextTarget);
        }
        this.hate = 0;
      }
      if (!npc.isInsideRadius(54232, 220120, 62040, 800, true, false)) {
        npc.doCast(SkillTable.getInstance().getInfo(4222, 1));
      }
      startQuestTimer("timer", 30000L, npc, null, true);
    }
    if (event.equalsIgnoreCase("minion_cycle"))
    {
      if (this._minionStatus == 1)
      {
        int rr = Rnd.get(15);
        addSpawn(29026, Xcoords[rr], Ycoords[rr], Zcoords[rr], Rnd.get(65536), false, 0L, true);
        this._minionStatus = 2;
      }
      else if (this._minionStatus == 2)
      {
        int rr = Rnd.get(15);
        addSpawn(29023, Xcoords[rr], Ycoords[rr], Zcoords[rr], Rnd.get(65536), false, 0L, true);
        this._minionStatus = 3;
      }
      else if (this._minionStatus == 3)
      {
        addSpawn(29024, Xcoords[Rnd.get(15)], Ycoords[Rnd.get(15)], Zcoords[Rnd.get(15)], Rnd.get(65536), false, 0L, true);
        addSpawn(29024, Xcoords[Rnd.get(15)], Ycoords[Rnd.get(15)], Zcoords[Rnd.get(15)], Rnd.get(65536), false, 0L, true);
        this._minionStatus = 4;
      }
      else if (this._minionStatus == 4)
      {
        addSpawn(29027, Xcoords[Rnd.get(15)], Ycoords[Rnd.get(15)], Zcoords[Rnd.get(15)], Rnd.get(65536), false, 0L, true);
        addSpawn(29027, Xcoords[Rnd.get(15)], Ycoords[Rnd.get(15)], Zcoords[Rnd.get(15)], Rnd.get(65536), false, 0L, true);
        addSpawn(29027, Xcoords[Rnd.get(15)], Ycoords[Rnd.get(15)], Zcoords[Rnd.get(15)], Rnd.get(65536), false, 0L, true);
        addSpawn(29027, Xcoords[Rnd.get(15)], Ycoords[Rnd.get(15)], Zcoords[Rnd.get(15)], Rnd.get(65536), false, 0L, true);
        addSpawn(29027, Xcoords[Rnd.get(15)], Ycoords[Rnd.get(15)], Zcoords[Rnd.get(15)], Rnd.get(65536), false, 0L, true);
        this._minionStatus = 5;
      }
      else if (this._minionStatus == 5)
      {
        addSpawn(29023, 52675, 219371, 62246, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 52687, 219596, 62168, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 52672, 219740, 62118, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 52857, 219992, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 52959, 219997, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 53381, 220151, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 54236, 220948, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 54885, 220144, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55264, 219860, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 55399, 220263, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55679, 220129, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 56276, 220783, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 57173, 220234, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 56267, 218826, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 56294, 219482, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 56094, 219113, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 56364, 218967, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 57113, 218079, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 56186, 217153, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55440, 218081, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 55202, 217940, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55225, 218236, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 54973, 218075, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 53412, 218077, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 54226, 218797, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 54394, 219067, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 54139, 219253, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 54262, 219480, 62048, Rnd.get(65536), false, 0L, true);
        this._minionStatus = 6;
      }
      else if (this._minionStatus == 6)
      {
        addSpawn(29027, 53412, 218077, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 54413, 217132, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 54841, 217132, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 55372, 217128, 62193, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 55893, 217122, 62048, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 56282, 217237, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 56963, 218080, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 56267, 218826, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 56294, 219482, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 56094, 219113, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 56364, 218967, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 56276, 220783, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 57173, 220234, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 54885, 220144, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55264, 219860, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 55399, 220263, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55679, 220129, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 54236, 220948, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 54464, 219095, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 54226, 218797, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 54394, 219067, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 54139, 219253, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 54262, 219480, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 53412, 218077, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55440, 218081, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 55202, 217940, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55225, 218236, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 54973, 218075, 62320, Rnd.get(65536), false, 0L, true);
        this._minionStatus = 7;
      }
      else if (this._minionStatus == 7)
      {
        addSpawn(29027, 54228, 217504, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 54181, 217168, 62320, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 54714, 217123, 62368, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 55298, 217127, 62463, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 55787, 217130, 62543, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 56284, 217216, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 56963, 218080, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 56267, 218826, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 56294, 219482, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 56094, 219113, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 56364, 218967, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 56276, 220783, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 57173, 220234, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 54885, 220144, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55264, 219860, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 55399, 220263, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55679, 220129, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 54236, 220948, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 54464, 219095, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 54226, 218797, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29024, 54394, 219067, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 54139, 219253, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29023, 54262, 219480, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 53412, 218077, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 54280, 217200, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55440, 218081, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29026, 55202, 217940, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 55225, 218236, 62592, Rnd.get(65536), false, 0L, true);
        addSpawn(29027, 54973, 218075, 62592, Rnd.get(65536), false, 0L, true);
        cancelQuestTimer("minion_cycle", null, null);
      }
    }
    else if (event.equalsIgnoreCase("zaken_unlock"))
    {
      int i1 = Rnd.get(15);
      GrandBoss zaken = (GrandBoss)addSpawn(29022, Xcoords[i1], Ycoords[i1], Zcoords[i1], i1, false, 0L, false);
      GrandBossManager.getInstance().setBossStatus(29022, 0);
      spawnBoss(zaken);
    }
    else if (event.equalsIgnoreCase("CreateOnePrivateEx"))
    {
      addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), 0, false, 0L, true);
    }
    return super.onAdvEvent(event, npc, player);
  }
  
  public String onFactionCall(Npc npc, Folk caller, Player attacker, boolean isPet)
  {
    if ((caller == null) || (npc == null)) {
      return super.onFactionCall(npc, caller, attacker, isPet);
    }
    return super.onFactionCall(npc, caller, attacker, isPet);
  }
  
  @Override
public String onSpellFinished(Npc npc, Player player, L2Skill skill)
  {
    if (npc.getNpcId() == 29022)
    {
      int skillId = skill.getId();
      if (skillId == 4222)
      {
        npc.teleToLocation(54232, 220120, 62040, 0);
        npc.getAI().setIntention(CtrlIntention.IDLE);
      }
    }
    return super.onSpellFinished(npc, player, skill);
  }
  
  @Override
public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
  {
    if (skill.getAggroPoints() > 0) {
      ((Attackable)npc).addDamageHate(caster, 0, skill.getAggroPoints() / npc.getMaxHp() * 10 * 150);
    }
    if (Rnd.get(12) < 1)
    {
      this._target = caster;
      CallSkills(npc);
    }
    return super.onSkillSee(npc, caster, skill, targets, isPet);
  }
  
  @Override
public String onAggro(Npc npc, Player player, boolean isPet)
  {
    if (npc == null) {
      return null;
    }
    Playable character;
    boolean isMage;
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
    if (character == null) {
      return null;
    }
    if ((!Config.RAID_DISABLE_CURSE) && (character.getLevel() - npc.getLevel() > 8))
    {
      L2Skill curse = null;
      if (isMage)
      {
        if ((!character.isMuted()) && (Rnd.get(4) == 0)) {
          curse = SkillTable.FrequentSkill.RAID_CURSE.getSkill();
        }
      }
      else if ((!character.isParalyzed()) && (Rnd.get(4) == 0)) {
        curse = SkillTable.FrequentSkill.RAID_CURSE2.getSkill();
      }
      if (curse != null)
      {
        npc.broadcastPacket(new MagicSkillUse(npc, character, curse.getId(), curse.getLevel(), 300, 0));
        curse.getEffects(npc, character);
      }
      ((Attackable)npc).stopHating(character);
      return null;
    }
    if (_zakenLair.isInsideZone(npc))
    {
      Creature target = isPet ? player.getPet() : player;
      ((Attackable)npc).addDamageHate(target, 1, 200);
    }
    int npcId = npc.getNpcId();
    if (npcId == 29022) {
      if (Rnd.get(15) < 1)
      {
        this._target = player;
        CallSkills(npc);
      }
    }
    return super.onAggro(npc, player, isPet);
  }
  
  public void CallSkills(Npc npc)
  {
    if (npc.isCastingNow()) {
      return;
    }
    int chance = Rnd.get(225);
    npc.setTarget(this._target);
    if (chance < 4) {
      npc.doCast(SkillTable.getInstance().getInfo(4219, 1));
    } else if (chance < 8) {
      npc.doCast(SkillTable.getInstance().getInfo(4218, 1));
    } else if (chance < 15) {
      npc.doCast(SkillTable.getInstance().getInfo(4221, 1));
    }
    if (Rnd.get(2) < 1) {
      if (this._target == ((Attackable)npc).getMostHated()) {
        npc.doCast(SkillTable.getInstance().getInfo(4220, 1));
      }
    }
  }
  
  @Override
public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, L2Skill skill)
  {
    int npcId = npc.getNpcId();
    if (npcId == 29022)
    {
      if (attacker.getMountType() == 1)
      {
        skill = SkillTable.getInstance().getInfo(4258, 1);
        if (attacker.getFirstEffect(skill) == null)
        {
          npc.setTarget(attacker);
          npc.doCast(skill);
        }
      }
      Creature originalAttacker = isPet ? attacker.getPet() : attacker;
      int hating = (int)(damage / npc.getMaxHp() / 0.05D * 20000.0D);
      ((Attackable)npc).addDamageHate(originalAttacker, 0, hating);
      if (Rnd.get(10) < 1)
      {
        this._target = attacker;
        CallSkills(npc);
      }
    }
    return super.onAttack(npc, attacker, damage, isPet, skill);
  }
  
  public static void SetToNextDay(int days, Calendar c)
  {
    c.add(5, days);
    c.set(11, Config.SPAWN_ZAKEN_HOUR_OF_DAY);
    c.set(12, Config.SPAWN_ZAKEN_MINUTE);
    c.set(13, 0);
    c.set(14, 0);
  }
  
  @Override
public String onKill(Npc npc, Player killer, boolean isPet)
  {
    int npcId = npc.getNpcId();
    if (npcId == 29022)
    {
      cancelQuestTimer("timer", npc, null);
      cancelQuestTimer("minion_cycle", npc, null);
      npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
      GrandBossManager.getInstance().setBossStatus(29022, 1);
      Calendar c = Calendar.getInstance();
      
      int dayOfWeek = c.get(7);
      if ((dayOfWeek == 1) || (dayOfWeek == 3) || (dayOfWeek == 5)) {
        SetToNextDay(2, c);
      } else if (dayOfWeek == 7) {
        SetToNextDay(3, c);
      } else {
        SetToNextDay(1, c);
      }
      long respawnTime = c.getTimeInMillis();
      startQuestTimer("zaken_unlock", respawnTime, null, null, false);
      
      StatsSet info = GrandBossManager.getInstance().getStatsSet(29022);
      info.set("respawn_time", respawnTime);
      GrandBossManager.getInstance().setStatsSet(29022, info);
      _open = false;
      DoorData.getInstance().getDoor(21240006).closeMe();
    }
    else if (GrandBossManager.getInstance().getBossStatus(29022) == 0)
    {
      startQuestTimer("CreateOnePrivateEx", (30 + Rnd.get(60)) * 1000, npc, null, false);
    }
    return super.onKill(npc, killer, isPet);
  }
  
  public static void waiter(long interval)
  {
    long startWaiterTime = System.currentTimeMillis();
    int seconds = (int)(interval / 1000L);
    while ((startWaiterTime + interval > System.currentTimeMillis()) && (_open))
    {
      seconds--;
      switch (seconds)
      {
      case 599: 
        if (_open) {
          AnnounceZaken("The Zaken Door will be closed in 10 minute(s) !");
        }
        break;
      case 299: 
        if (_open) {
          AnnounceZaken("The Zaken Door will be closed in 5 minute(s) !");
        }
        break;
      case 60: 
      case 120: 
      case 180: 
      case 240: 
      case 360: 
      case 420: 
      case 480: 
      case 540: 
      case 900: 
        if (_open) {
          AnnounceZaken("The Zaken Door will be closed in " + seconds / 60 + " minute(s) !");
        }
        break;
      case 15: 
      case 30: 
        if (_open) {
          AnnounceZaken("The Zaken Door will be closed in " + seconds + " second(s) !");
        }
        break;
      case 2: 
      case 3: 
      case 4: 
      case 5: 
      case 6: 
        if (_open) {
          AnnounceZaken("The Zaken Door will be closed in " + (seconds - 1) + " second(s) !");
        }
        break;
      case 1: 
        if (_open) {
          AnnounceZaken("The Zaken Door closed !");
        }
        break;
      }
      long startOneSecondWaiterStartTime = System.currentTimeMillis();
      while (startOneSecondWaiterStartTime + 1000L > System.currentTimeMillis()) {
        try
        {
          Thread.sleep(1L);
        }
        catch (InterruptedException localInterruptedException) {}
      }
    }
  }
  
  public static void AnnounceZaken(String text)
  {
    CreatureSay c1 = new CreatureSay(0, Config.ANNOUNCE_ID, "", "" + text);
    CreatureSay c2 = new CreatureSay(0, Config.ANNOUNCE_ID, "", "" + text);
    for (Player player : World.getInstance().getPlayers()) {
      if ((player != null) && (player.isOnline())) {
        if (Config.ANNOUNCE_ID == 10) {
          player.sendPacket(c1);
        } else {
          player.sendPacket(c2);
        }
      }
    }
  }
}
