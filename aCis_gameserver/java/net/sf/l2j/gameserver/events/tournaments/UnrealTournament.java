package net.sf.l2j.gameserver.events.tournaments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class UnrealTournament implements Runnable
{
	// list of participants
	public static List<Team> registered;
	// number of Arenas 
	int free = Config.UNREAL_TOURNAMENT_EVENT_COUNT;
	// Arenas
	Arena[] arenas = new Arena[Config.UNREAL_TOURNAMENT_EVENT_COUNT];
	// list of fights going on
	Map<Integer, String> fights = new HashMap<>(Config.UNREAL_TOURNAMENT_EVENT_COUNT);

	public UnrealTournament() 
	{
		registered = new ArrayList<>();
		int[] coord;
		for(int i=0; i < Config.UNREAL_TOURNAMENT_EVENT_COUNT; i++)
		{
			coord = Config.UNREAL_TOURNAMENT_EVENT_LOCS[i];
			arenas[i] = new Arena(i, coord[0], coord[1], coord[2]);
		}

	}

	public static UnrealTournament getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public boolean register(Player player, Player assist1, Player assist2, Player assist3)
	{
		for (Team p : registered)
		{
			if (p.getLeader() == player || p.getAAssist() == player || p.getBAssist() == player || p.getCAssist() == player)
			{
				player.sendMessage("Tournament: You already registered!");
				return false;
			} 
			else if (p.getLeader() == assist1 || p.getLeader() == assist2 || p.getLeader() == assist3)
			{
				player.sendMessage("Tournament: Your partner already registered!");
				return false;
			}
		}
		return registered.add(new Team(player, assist1, assist2, assist3));
	}

	public boolean isRegistered(Player player)
	{
		for (Team p : registered) 
		{
			if (p.getLeader() == player || p.getAAssist() == player || p.getBAssist() == player || p.getCAssist() == player)
			{
				return true;
			}
		}
		return false;
	}

	public Map<Integer, String> getFights()
	{
		return fights;
	}

	public boolean remove(Player player)
	{
		for(Team p : registered) 
		{
			if (p.getLeader() == player || p.getAAssist() == player || p.getBAssist() == player || p.getCAssist() == player)
			{
				registered.remove(p);
				player.sendMessage("Tournament 4x4: Your party was removed!");
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized void run()
	{
		// while server is running
		while(true)
		{
			// if no have participants or arenas are busy wait 1 minute 
			if (registered.size() < 2 || free == 0)
			{
				try 
				{
					Thread.sleep(Config.UNREAL_TOURNAMENT_CALL_INTERVAL); 
				} 
				catch (InterruptedException e)
				{

				}
				continue;
			}
			List<Team> opponents = selectOpponents();
			if (opponents != null && opponents.size() == 2)
			{
				Thread T = new Thread(new EvtArenaTask(opponents));
				T.setDaemon(true);
				T.start();
			}
			//wait 1 minute for not stress server
			try
			{
				Thread.sleep(Config.UNREAL_TOURNAMENT_CALL_INTERVAL); 
			} 
			catch (InterruptedException e)
			{

			}
		}
	}

	@SuppressWarnings("null")
	private List<Team> selectOpponents()
	{
		List<Team> opponents = new ArrayList<>();
		Team teamOne = null, teamTwo = null;
		int tries = 3;
		do
		{
			int first = 0, second = 0;
			if(getRegisteredCount() < 2)
				return opponents;

			if (teamOne == null)
			{
				first = Rnd.get(getRegisteredCount());
				teamOne = registered.get(first);
				if(teamOne.check())
				{
					opponents.add(0, teamOne);
					registered.remove(first);
				} 
				else 
				{
					teamOne = null;
					registered.remove(first);
					return null;
				}

			}
			if (teamTwo == null)
			{
				second = Rnd.get(getRegisteredCount());
				teamTwo = registered.get(second);
				if(teamTwo.check())
				{
					opponents.add(1, teamTwo);
					registered.remove(second);
				}
				else 
				{
					teamTwo = null;
					registered.remove(second);
					return null;
				}

			}	
		} while ((teamOne == null || teamTwo == null) && --tries > 0);
		return opponents;
	}

	public int getRegisteredCount()
	{
		return registered.size();
	}

	private class Team
	{
		private Player leader, assist1, assist2, assist3;

		public Team(Player leader, Player assist1, Player assist2, Player assist3)
		{
			this.leader = leader;
			this.assist1 = assist1;
			this.assist2 = assist2;
			this.assist3 = assist3;
		}

		public Player getAAssist()
		{
			return assist1;
		}
		
		public Player getBAssist()
		{
			return assist2;
		}
		
		public Player getCAssist()
		{
			return assist3;
		}

		public Player getLeader()
		{
			return leader;
		}

		public boolean check()
		{
			if ((leader == null || !leader.isOnline()) && (assist1 != null && assist1.isOnline()) && (assist2 != null && assist2.isOnline()) && (assist3 != null && assist3.isOnline()))	
			{
				assist1.sendMessage("Tournament: You participation in Event was Canceled.");
				return false;
			} 				
			else if ((assist1 == null || !assist1.isOnline()) && (assist2 == null || !assist2.isOnline()) && (assist3 == null || !assist3.isOnline()))
			{
				leader.sendMessage("Tournament: You participation in Event was Canceled.");
				return false;
			}
			return true;
		}
		
		
		
		public boolean isAlive()
		{
			if ((leader == null || leader.isDead() || !leader.isOnline() || !leader.isInArenaEvent()) && (assist1 == null || assist1.isDead() || !assist1.isOnline() || !assist1.isInArenaEvent()) && (assist1 == null || assist1.isDead() || !assist1.isOnline() || !assist1.isInArenaEvent()) && (assist2 == null || assist2.isDead() || !assist2.isOnline() || !assist2.isInArenaEvent()) && (assist3 == null || assist3.isDead() || !assist3.isOnline() || !assist3.isInArenaEvent())) 	
				return false;							

			return !(leader.isDead() && assist1.isDead() && assist2.isDead() && assist3.isDead());
		}

		public void teleportTo(int x, int y, int z) 
		{
		      if ((leader != null) && (leader.isOnline()))
		      {
		        leader.setCurrentCp(leader.getMaxCp());
		        leader.setCurrentHp(leader.getMaxHp());
		        leader.setCurrentMp(leader.getMaxMp());
		        if (leader.isInObserverMode())
		        {
		         leader.setLastCords(x, y, z);
		         leader.leaveObserverMode();
		        }
		        else
		        {
		         leader.teleToLocation(x, y, z, 0);
		        }
		        leader.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (leader.isInDuel() && (leader.getDuelState() != Duel.DuelState.INTERRUPTED))))
					leader.stopAllEffectsExceptThoseThatLastThroughDeath();

			}
			
		      if ((assist1 != null) && (assist1.isOnline()))
		      {
		        assist1.setCurrentCp(assist1.getMaxCp());
		        assist1.setCurrentHp(assist1.getMaxHp());
		        assist1.setCurrentMp(assist1.getMaxMp());
		        if (assist1.isInObserverMode())
		        {
		          assist1.setLastCords(x, y + 50, z);
		          assist1.leaveObserverMode();
		        }
		        else
		        {
		          assist1.teleToLocation(x, y + 50, z, 0);
		        }
		        assist1.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist1.isInDuel() && (assist1.getDuelState() != Duel.DuelState.INTERRUPTED))))
					assist1.stopAllEffectsExceptThoseThatLastThroughDeath();

			}
			
		      if ((assist2 != null) && (assist2.isOnline()))
		      {
		        assist2.setCurrentCp(assist2.getMaxCp());
		        assist2.setCurrentHp(assist2.getMaxHp());
		        assist2.setCurrentMp(assist2.getMaxMp());
		        if (assist2.isInObserverMode())
		        {
		          assist2.setLastCords(x, y - 100, z);
		          assist2.leaveObserverMode();
		        }
		        else
		        {
		          assist2.teleToLocation(x, y - 100, z, 0);
		        }
		        assist2.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist2.isInDuel() && (assist2.getDuelState() != Duel.DuelState.INTERRUPTED))))
					assist2.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
			
		      if ((assist3 != null) && (assist3.isOnline()))
		      {
		        assist3.setCurrentCp(assist3.getMaxCp());
		        assist3.setCurrentHp(assist3.getMaxHp());
		        assist3.setCurrentMp(assist3.getMaxMp());
		        if (assist3.isInObserverMode())
		        {
		          assist3.setLastCords(x, y - 50, z);
		          assist3.leaveObserverMode();
		        }
		        else
		        {
		          assist3.teleToLocation(x, y - 50, z, 0);
		        }
		        assist3.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist3.isInDuel() && (assist3.getDuelState() != Duel.DuelState.INTERRUPTED))))
					assist3.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
		}

		public void rewards()
		{ 
			SystemMessage sm = null;
			
			if (leader != null)
			{
				for (int[] reward : Config.UNREAL_TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = leader.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], leader, null);
						
						if (reward[1] > 1)
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(reward[0]);
							sm.addItemNumber(reward[1]);
						}
						else
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
						}
						
						leader.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, leader, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							leader.sendPacket(sm);
						}
					}
				}
			}
			
			if (assist1 != null)
			{
				for (int[] reward : Config.UNREAL_TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = assist1.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], assist1, null);
						
						if (reward[1] > 1)
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(reward[0]);
							sm.addItemNumber(reward[1]);
						}
						else
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
						}
						
						assist1.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, assist1, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							assist1.sendPacket(sm);
						}
					}
				}
			}
			
			if (assist2 != null)
			{
				for (int[] reward : Config.UNREAL_TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = assist2.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], assist2, null);
						
						if (reward[1] > 1)
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(reward[0]);
							sm.addItemNumber(reward[1]);
						}
						else
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
						}
						
						assist2.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, assist2, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							assist2.sendPacket(sm);
						}
					}
				}
			}
			
			if (assist3 != null)
			{
				for (int[] reward : Config.UNREAL_TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = assist3.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], assist3, null);
						
						if (reward[1] > 1)
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(reward[0]);
							sm.addItemNumber(reward[1]);
						}
						else
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
						}
						
						assist3.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, assist3, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							assist3.sendPacket(sm);
						}
					}
				}
			}
			sendPacket("Congratulations, your team won the event!", 5);
		}

		public void setInTournamentEvent(boolean val)
		{
			if (leader != null)
				leader.setInArenaEvent(val);

			if (assist1 != null)
				assist1.setInArenaEvent(val);

			if (assist2 != null)
				assist2.setInArenaEvent(val);

			if (assist2 != null)
				assist2.setInArenaEvent(val);

			if (assist3 != null)
				assist3.setInArenaEvent(val);
		}

		public void revive() 
		{
			if (leader != null)
				leader.doRevive();
			
			if (assist1 != null)
				assist1.doRevive();
			
			if (assist2 != null)
				assist2.doRevive();
			
			if (assist3 != null)
				assist3.doRevive();
		}
		
		public void removeBuff() 
		{
			if (leader != null)
				leader.stopPhoenixBlessing(null);
			
			if (assist1 != null)
				assist1.stopPhoenixBlessing(null);
			
			if (assist2 != null)
				assist2.stopPhoenixBlessing(null);
			
			if (assist3 != null)
				assist3.stopPhoenixBlessing(null);
		}

		public void removeSummon() 
		{
			if (leader != null)
			{
				final Summon summon = leader.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(leader);
				}
			}
			
			if (assist1 != null)
			{
				final Summon summon = assist1.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(assist1);
				}
			}
			
			if (assist2 != null)
			{
				final Summon summon = assist2.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(assist2);
				}
			}
			
			if (assist3 != null)
			{
				final Summon summon = assist3.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(assist3);
				}
			}
		}
		
		public void setImobilised(boolean val)
		{
			if (leader != null)
			{
				leader.setIsInvul(val);
				leader.setIsParalyzed(val);
			}
			
			if (assist1 != null)
			{
				assist1.setIsInvul(val);
				assist1.setIsParalyzed(val);
			}
			
			if (assist2 != null)
			{
				assist2.setIsInvul(val);
				assist2.setIsParalyzed(val);
			}
			
			if (assist3 != null)
			{
				assist3.setIsInvul(val);
				assist3.setIsParalyzed(val);
			}
		}

		public void sendPacket(String message, int duration)
		{
			if (leader != null)
				leader.sendPacket(new ExShowScreenMessage(message, duration * 1000));
			
			if (assist1 != null) 
				assist1.sendPacket(new ExShowScreenMessage(message, duration * 1000));
			
			if (assist2 != null) 
				assist2.sendPacket(new ExShowScreenMessage(message, duration * 1000));
			
			if (assist3 != null) 
				assist3.sendPacket(new ExShowScreenMessage(message, duration * 1000));
		}
		
	    public void saveTitle()
	    {
	      if ((this.leader != null) && (this.leader.isOnline()))
	      {
	        this.leader._originalTitleColorTournament = this.leader.getAppearance().getNameColor();
	        this.leader._originalTitleTournament = this.leader.getTitle();
	      }
	      if ((this.assist1 != null) && (this.assist1.isOnline()))
	      {
	        this.assist1._originalTitleColorTournament = this.assist1.getAppearance().getNameColor();
	        this.assist1._originalTitleTournament = this.assist1.getTitle();
	      }
	      if ((this.assist2 != null) && (this.assist2.isOnline()))
	      {
	        this.assist2._originalTitleColorTournament = this.assist2.getAppearance().getNameColor();
	        this.assist2._originalTitleTournament = this.assist2.getTitle();
	      }
	      if ((this.assist3 != null) && (this.assist1.isOnline()))
	      {
	        this.assist3._originalTitleColorTournament = this.assist3.getAppearance().getNameColor();
	        this.assist3._originalTitleTournament = this.assist3.getTitle();
	      }
	    }
	    
	    public void setArenaAttack(boolean val)
	    {
	      if ((this.leader != null) && (this.leader.isOnline())) {
	        this.leader.setArenaAttack(val);
	      }
	      if ((this.assist1 != null) && (this.assist1.isOnline())) {
	        this.assist1.setArenaAttack(val);
	      }
	      if ((this.assist2 != null) && (this.assist2.isOnline())) {
		        this.assist2.setArenaAttack(val);
		      }
	      if ((this.assist3 != null) && (this.assist3.isOnline())) {
		        this.assist3.setArenaAttack(val);
		      }
	    }

	    public void EventTitle(String title, String color)
	    {
	      if ((this.leader != null) && (this.leader.isOnline()))
	      {
	        this.leader.setTitle(title);
	        this.leader.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.leader.broadcastUserInfo();
	        this.leader.broadcastTitleInfo();
	      }
	      if ((this.assist1 != null) && (this.assist1.isOnline()))
	      {
	        this.assist1.setTitle(title);
	        this.assist1.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.assist1.broadcastUserInfo();
	        this.assist1.broadcastTitleInfo();
	      }
	      if ((this.assist2 != null) && (this.assist2.isOnline()))
	      {
	        this.assist2.setTitle(title);
	        this.assist2.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.assist2.broadcastUserInfo();
	        this.assist2.broadcastTitleInfo();
	      }
	      if ((this.assist3 != null) && (this.assist3.isOnline()))
	      {
	        this.assist3.setTitle(title);
	        this.assist3.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.assist3.broadcastUserInfo();
	        this.assist3.broadcastTitleInfo();
	      }
	    }

	    public void backTitle()
	    {
	      if ((this.leader != null) && (this.leader.isOnline()))
	      {
	        this.leader.getAppearance().setTitleColor(this.leader._originalTitleColorTournament);
	        this.leader.setTitle(this.leader._originalTitleTournament);
	        this.leader.broadcastUserInfo();
	        this.leader.broadcastTitleInfo();
	      }
	      if ((this.assist1 != null) && (this.assist1.isOnline()))
	      {
	        this.assist1.getAppearance().setTitleColor(this.assist1._originalTitleColorTournament);
	        this.assist1.setTitle(this.assist1._originalTitleTournament);
	        this.assist1.broadcastUserInfo();
	        this.assist1.broadcastTitleInfo();
	      }
	      
	      if ((this.assist2 != null) && (this.assist2.isOnline()))
	      {
	        this.assist2.getAppearance().setTitleColor(this.assist2._originalTitleColorTournament);
	        this.assist2.setTitle(this.assist2._originalTitleTournament);
	        this.assist2.broadcastUserInfo();
	        this.assist2.broadcastTitleInfo();
	      }
	      
	      if ((this.assist3 != null) && (this.assist3.isOnline()))
	      {
	        this.assist3.getAppearance().setTitleColor(this.assist3._originalTitleColorTournament);
	        this.assist3.setTitle(this.assist3._originalTitleTournament);
	        this.assist3.broadcastUserInfo();
	        this.assist3.broadcastTitleInfo();
	      }
	    }

	    public void removeSkills()
	    {
	      if ((this.leader.getClassId() != ClassId.SHILLIEN_ELDER) && (this.leader.getClassId() != ClassId.SHILLIEN_SAINT) && (this.leader.getClassId() != ClassId.BISHOP) && (this.leader.getClassId() != ClassId.CARDINAL) && (this.leader.getClassId() != ClassId.ELVEN_ELDER) && (this.leader.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.leader.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.leader.stopSkillEffects(effect.getSkill().getId());
	          }
	        }
	      }
	      if ((this.assist1.getClassId() != ClassId.SHILLIEN_ELDER) && (this.assist1.getClassId() != ClassId.SHILLIEN_SAINT) && (this.assist1.getClassId() != ClassId.BISHOP) && (this.assist1.getClassId() != ClassId.CARDINAL) && (this.assist1.getClassId() != ClassId.ELVEN_ELDER) && (this.assist1.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.assist1.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.assist1.stopSkillEffects(effect.getSkill().getId());
	          }
	        }
	      }
	      if ((this.assist2.getClassId() != ClassId.SHILLIEN_ELDER) && (this.assist2.getClassId() != ClassId.SHILLIEN_SAINT) && (this.assist2.getClassId() != ClassId.BISHOP) && (this.assist2.getClassId() != ClassId.CARDINAL) && (this.assist2.getClassId() != ClassId.ELVEN_ELDER) && (this.assist2.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.assist2.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.assist2.stopSkillEffects(effect.getSkill().getId());
	          }
	        }
	      }
	      if ((this.assist3.getClassId() != ClassId.SHILLIEN_ELDER) && (this.assist3.getClassId() != ClassId.SHILLIEN_SAINT) && (this.assist3.getClassId() != ClassId.BISHOP) && (this.assist3.getClassId() != ClassId.CARDINAL) && (this.assist3.getClassId() != ClassId.ELVEN_ELDER) && (this.assist3.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.assist3.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.assist3.stopSkillEffects(effect.getSkill().getId());
	          }
	        }
	      }
	    }

	    public void sendPacketinit(String message, int duration)
	    {
	      if ((this.leader != null) && (this.leader.isOnline())) {
	        this.leader.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
	      }
	      if ((this.assist1 != null) && (this.assist1.isOnline())) {
	        this.assist1.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
	      }
	      if ((this.assist2 != null) && (this.assist2.isOnline())) {
		        this.assist2.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
		      }
	      if ((this.assist3 != null) && (this.assist3.isOnline())) {
		        this.assist3.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
		      }
	    }
	
	}
	
	

	private class EvtArenaTask implements Runnable 
	{
		private final Team teamOne;
		private final Team teamTwo;
		private final int pOneX, pOneY, pOneZ, pTwoX, pTwoY, pTwoZ;
		private Arena arena;

		public EvtArenaTask(List<Team> opponents) 
		{
			teamOne = opponents.get(0);
			teamTwo = opponents.get(1);
			Player leader = teamOne.getLeader();
			pOneX = leader.getX();
			pOneY = leader.getY();
			pOneZ = leader.getZ();
			leader = teamTwo.getLeader();
			pTwoX = leader.getX();
			pTwoY = leader.getY();
			pTwoZ = leader.getZ();
		}

		@Override
		public void run() 
		{
			free--;
			portPairsToArena();
			teamOne.saveTitle();
			teamTwo.saveTitle();
			teamOne.sendPacket("The battle starts in 30 seconds!", 15);
			teamTwo.sendPacket("The battle starts in 30 seconds!", 15);
			try 
			{
				Thread.sleep(Config.UNREAL_TOURNAMENT_WAIT_INTERVAL);
			} 
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
		    this.teamOne.sendPacketinit("Started. Good Fight!", 6);
		    this.teamTwo.sendPacketinit("Started. Good Fight!", 6);
			teamOne.EventTitle(Config.MSG_TEAM1, Config.TITLE_COLOR_TEAM1);
			teamTwo.EventTitle(Config.MSG_TEAM2, Config.TITLE_COLOR_TEAM2);
			teamOne.setArenaAttack(true);
			teamTwo.setArenaAttack(true);
			teamOne.setImobilised(false);
			teamTwo.setImobilised(false);
			teamOne.removeBuff();
			teamTwo.removeBuff();
			teamOne.removeSummon();
			teamTwo.removeSummon();

			while(check())
			{
				// check players status each  seconds
				try
				{
					Thread.sleep(Config.UNREAL_TOURNAMENT_CHECK_INTERVAL);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
					break;
				}
			}
			finishDuel();
			free++;
		}

		private void finishDuel()
		{
			fights.remove(arena.id);
			rewardWinner();
			teamOne.backTitle();
			teamTwo.backTitle();
			teamOne.revive();
			teamTwo.revive();
			teamOne.teleportTo(pOneX, pOneY, pOneZ);
			teamTwo.teleportTo(pTwoX, pTwoY, pTwoZ);
			teamOne.setInTournamentEvent(false);
			teamTwo.setInTournamentEvent(false);
			arena.setFree(true);
		}

		private void rewardWinner() 
		{
			if (teamOne.isAlive() && !teamTwo.isAlive())
			{
		        Player leader1 = this.teamOne.getLeader();
		        Player leader2 = this.teamTwo.getLeader();
		        if ((leader1.getClan() != null) && (leader2.getClan() != null)) {
		          Announcements.Announce("[4x4]: " + leader1.getClan().getName() + " VS " + leader2.getClan().getName() + ". Winner is: " + leader1.getClan().getName() + "!");
			}
				teamOne.rewards();
			}
			else if (teamTwo.isAlive() && !teamOne.isAlive())
			{
				Player leader1 = this.teamTwo.getLeader();
				Player leader2 = this.teamOne.getLeader();
		        if ((leader1.getClan() != null) && (leader2.getClan() != null)) {
		          Announcements.Announce("[4x4]: " + leader1.getClan().getName() + " VS " + leader2.getClan().getName() + ". Winner is: " + leader1.getClan().getName() + "!");
		        }
				teamTwo.rewards();
			}
		}

		private boolean check()
		{
			return (teamOne.isAlive() && teamTwo.isAlive());
		}
		
		

		private void portPairsToArena()
		{
			for (Arena arena : arenas)
			{
				if (arena.isFree)
				{
					this.arena = arena;
					arena.setFree(false);
					teamOne.teleportTo(arena.x - 850, arena.y, arena.z);
					teamTwo.teleportTo(arena.x + 850, arena.y, arena.z);
					teamOne.setImobilised(true);
					teamTwo.setImobilised(true);
					teamOne.setInTournamentEvent(true);
					teamTwo.setInTournamentEvent(true);
					teamOne.removeSkills();
					teamTwo.removeSkills();
					fights.put(this.arena.id, teamOne.getLeader().getName() +" vs "+ teamTwo.getLeader().getName());
					break;
				}
			}
		}
	}

	private class Arena 
	{
		protected int x, y, z;
		protected boolean isFree = true;
		int id;

		public Arena(int id, int x, int y, int z)
		{
			this.id = id;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void setFree(boolean val)
		{
			isFree = val;
		}
	}

	private static class SingletonHolder
	{
		protected static final UnrealTournament INSTANCE = new UnrealTournament();
	}
}