package net.sf.l2j.gameserver.model;

import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.manager.FishingChampionshipManager;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.PenaltyMonster;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingHpRegen;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingStartCombat;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2Fishing implements Runnable
{
	private Player _fisher;
	private int _time;
	private int _stop = 0;
	private int _goodUse = 0;
	private int _anim = 0;
	private int _mode = 0;
	private int _deceptiveMode = 0;
	private Future<?> _fishAiTask;
	private boolean _thinking;
	
	// Fish datas
	private final int _fishId;
	private final int _fishMaxHp;
	private int _fishCurHp;
	private final double _regenHp;
	private final boolean _isUpperGrade;
	private int _lureType;
	private final int _lureId;
	
	@Override
	public void run()
	{
		if (_fisher == null)
			return;
		
		if (_fishCurHp >= _fishMaxHp * 2)
		{
			// The fish got away
			_fisher.sendPacket(SystemMessageId.BAIT_STOLEN_BY_FISH);
			doDie(false);
		}
		else if (_time <= 0)
		{
			// Time is up, so that fish got away
			_fisher.sendPacket(SystemMessageId.FISH_SPIT_THE_HOOK);
			doDie(false);
		}
		else
			aiTask();
	}
	
	// =========================================================
	public L2Fishing(Player Fisher, Fish fish, boolean isNoob, boolean isUpperGrade, int lureId)
	{
		_fisher = Fisher;
		_fishMaxHp = fish.getHp();
		_fishCurHp = _fishMaxHp;
		_regenHp = fish.getHpRegen();
		_fishId = fish.getId();
		_time = fish.getCombatTime() / 1000;
		_isUpperGrade = isUpperGrade;
		_lureId = lureId;
		
		if (isUpperGrade)
		{
			_deceptiveMode = Rnd.get(100) >= 90 ? 1 : 0;
			_lureType = 2;
		}
		else
		{
			_deceptiveMode = 0;
			_lureType = isNoob ? 0 : 1;
		}
		_mode = Rnd.get(100) >= 80 ? 1 : 0;
		
		_fisher.broadcastPacket(new ExFishingStartCombat(_fisher, _time, _fishMaxHp, _mode, _lureType, _deceptiveMode));
		_fisher.sendPacket(new PlaySound(1, "SF_S_01"));
		
		// Succeeded in getting a bite
		_fisher.sendPacket(SystemMessageId.GOT_A_BITE);
		
		if (_fishAiTask == null)
			_fishAiTask = ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	public void changeHp(int hp, int pen)
	{
		_fishCurHp -= hp;
		if (_fishCurHp < 0)
			_fishCurHp = 0;
		
		_fisher.broadcastPacket(new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, _goodUse, _anim, pen, _deceptiveMode));
		_anim = 0;
		
		if (_fishCurHp > _fishMaxHp * 2)
		{
			_fishCurHp = _fishMaxHp * 2;
			doDie(false);
			return;
		}
		else if (_fishCurHp == 0)
		{
			doDie(true);
			return;
		}
	}
	
	public synchronized void doDie(boolean win)
	{
		if (_fishAiTask != null)
		{
			_fishAiTask.cancel(false);
			_fishAiTask = null;
		}
		
		if (_fisher == null)
			return;
		
		if (win)
		{
			if (Rnd.get(100) < 5)
			{
				int npcId = 18319 + Math.min(_fisher.getLevel() / 11, 7); // 18319-18326
				
				PenaltyMonster npc = new PenaltyMonster(IdFactory.getInstance().getNextId(), NpcData.getInstance().getTemplate(npcId));
				npc.setXYZ(_fisher.getX(), _fisher.getY(), _fisher.getZ() + 20);
				npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
				npc.setHeading(_fisher.getHeading());
				npc.spawnMe();
				npc.setPlayerToKill(_fisher);
				
				_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK);
			}
			else
			{
				_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING);
				_fisher.addItem("Fishing", _fishId, 1, null, true);
				FishingChampionshipManager.getInstance().newFish(_fisher, _lureId);
			}
		}
		_fisher.endFishing(win);
		_fisher = null;
	}
	
	protected void aiTask()
	{
		if (_thinking)
			return;
		
		_thinking = true;
		_time--;
		
		try
		{
			if (_mode == 1)
			{
				if (_deceptiveMode == 0)
					_fishCurHp += (int) _regenHp;
			}
			else
			{
				if (_deceptiveMode == 1)
					_fishCurHp += (int) _regenHp;
			}
			
			if (_stop == 0)
			{
				_stop = 1;
				int check = Rnd.get(100);
				if (check >= 70)
					_mode = _mode == 0 ? 1 : 0;
				
				if (_isUpperGrade)
				{
					check = Rnd.get(100);
					if (check >= 90)
						_deceptiveMode = _deceptiveMode == 0 ? 1 : 0;
				}
			}
			else
				_stop--;
		}
		finally
		{
			_thinking = false;
			if (_anim != 0)
				_fisher.broadcastPacket(new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, 0, _anim, 0, _deceptiveMode));
			else
				_fisher.sendPacket(new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, 0, _anim, 0, _deceptiveMode));
		}
	}
	
	public void useRealing(int dmg, int pen)
	{
		_anim = 2;
		if (Rnd.get(100) > 90)
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, pen);
			return;
		}
		
		if (_fisher == null)
			return;
		
		if (_mode == 1)
		{
			if (_deceptiveMode == 0)
			{
				// Reeling is successful, Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (pen == 50)
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(pen));
				
				_goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				// Reeling failed, Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else
		{
			if (_deceptiveMode == 0)
			{
				// Reeling failed, Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
			else
			{
				// Reeling is successful, Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (pen == 50)
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(pen));
				
				_goodUse = 1;
				changeHp(dmg, pen);
			}
		}
	}
	
	public void usePomping(int dmg, int pen)
	{
		_anim = 1;
		if (Rnd.get(100) > 90)
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, pen);
			return;
		}
		
		if (_fisher == null)
			return;
		
		if (_mode == 0)
		{
			if (_deceptiveMode == 0)
			{
				// Pumping is successful. Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (pen == 50)
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(pen));
				
				_goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				// Pumping failed, Regained: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else
		{
			if (_deceptiveMode == 0)
			{
				// Pumping failed, Regained: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(dmg));
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
			else
			{
				// Pumping is successful. Damage: $s1
				_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
				if (pen == 50)
					_fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(pen));
				
				_goodUse = 1;
				changeHp(dmg, pen);
			}
		}
	}
}