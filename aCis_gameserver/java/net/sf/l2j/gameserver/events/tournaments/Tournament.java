package net.sf.l2j.gameserver.events.tournaments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
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

public class Tournament implements Runnable
{
	
	// list of participants
	public static List<Pair> registered;
	// number of Arenas 
	int free =  Config.ARENA_EVENT_COUNT;
	// Arenas
	Arena[] arenas = new Arena[Config.ARENA_EVENT_COUNT];
	// list of fights going on
	Map<Integer, String> fights = new HashMap<>(Config.ARENA_EVENT_COUNT);

	public Tournament() 
	{
		registered = new ArrayList<>();
		int[] coord;
		for(int i=0; i < Config.ARENA_EVENT_COUNT; i++)
		{
			coord = Config.ARENA_EVENT_LOCS[i];
			arenas[i] = new Arena(i, coord[0], coord[1], coord[2]);
		}

	}

	public static Tournament getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public boolean register(Player player, Player assist)
	{
		for (Pair p : registered)
		{
			if (p.getLeader() == player || p.getAssist() == player)
			{
				player.sendMessage("Tournament: You already registered!");
				return false;
			} 
			else if (p.getLeader() == assist || p.getAssist() == assist)
			{
				player.sendMessage("Tournament: Your partner already registered!");
				return false;
			}
		}
		return registered.add(new Pair(player, assist));
	}

	public boolean isRegistered(Player player)
	{
		for (Pair p : registered) 
		{
			if (p.getLeader() == player || p.getAssist() == player)
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
		for(Pair p : registered) 
		{
			if (p.getLeader() == player || p.getAssist() == player)
			{
				registered.remove(p);
				player.sendMessage("Tournament 2x2: Your party was removed!");
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
					Thread.sleep(Config.ARENA_CALL_INTERVAL); 
				} 
				catch (InterruptedException e)
				{

				}
				continue;
			}
			List<Pair> opponents = selectOpponents();
			if (opponents != null && opponents.size() == 2)
			{
				Thread T = new Thread(new EvtArenaTask(opponents));
				T.setDaemon(true);
				T.start();
			}
			//wait 1 minute for not stress server
			try
			{
				Thread.sleep(Config.ARENA_CALL_INTERVAL); 
			} 
			catch (InterruptedException e)
			{

			}
		}
	}

	@SuppressWarnings("null")
	private List<Pair> selectOpponents()
	{
		List<Pair> opponents = new ArrayList<>();
		Pair pairOne = null, pairTwo = null;
		int tries = 3;
		do
		{
			int first = 0, second = 0;
			if(getRegisteredCount() < 2)
				return opponents;

			if (pairOne == null)
			{
				first = Rnd.get(getRegisteredCount());
				pairOne = registered.get(first);
				if(pairOne.check())
				{
					opponents.add(0,pairOne);
					registered.remove(first);
				} 
				else 
				{
					pairOne = null;
					registered.remove(first);
					return null;
				}

			}
			if (pairTwo == null)
			{
				second = Rnd.get(getRegisteredCount());
				pairTwo = registered.get(second);
				if(pairTwo.check())
				{
					opponents.add(1, pairTwo);
					registered.remove(second);
				}
				else 
				{
					pairTwo = null;
					registered.remove(second);
					return null;
				}

			}	
		} while ((pairOne == null || pairTwo == null) && --tries > 0);
		return opponents;
	}

	public int getRegisteredCount()
	{
		return registered.size();
	}

	private class Pair
	{
		Player leader;
		Player assist;

		public Pair(Player leader, Player assist)
		{
			this.leader = leader;
			this.assist = assist;
		}

		public Player getAssist()
		{
			return assist;
		}

		public Player getLeader()
		{
			return leader;
		}

		public boolean check()
		{
			if ((leader == null || !leader.isOnline()) && (assist != null && assist.isOnline()))
			{
				assist.sendMessage("Tournament: You participation in Event was Canceled.");
				return false;
			}
			else if ((assist == null || !assist.isOnline()) && (leader != null && leader.isOnline()))
			{
				leader.sendMessage("Tournament: You participation in Event was Canceled.");
				return false;
			}
			return true;
		}
		
		public boolean isAlive()
		{
			if ((leader == null || leader.isDead() || !leader.isOnline() || !leader.isInArenaEvent()) && (assist == null || assist.isDead() || !assist.isOnline() || !assist.isInArenaEvent())) 	
				return false;							

			return !(leader.isDead() && assist.isDead());
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
	      if ((assist != null) && (assist.isOnline()))
	      {
	        assist.setCurrentCp(assist.getMaxCp());
	        assist.setCurrentHp(assist.getMaxHp());
	        assist.setCurrentMp(assist.getMaxMp());
	        if (assist.isInObserverMode())
	        {
	          assist.setLastCords(x, y + 50, z);
	          assist.leaveObserverMode();
	        }
	        else
	        {
	          assist.teleToLocation(x, y + 50, z, 0);
	        }
	        assist.broadcastUserInfo();
	      }
			if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist.isInDuel() && (assist.getDuelState() != Duel.DuelState.INTERRUPTED))))
				assist.stopAllEffectsExceptThoseThatLastThroughDeath();
	      
	    }
	    
		public void rewards()
		{ 
			SystemMessage sm = null;
			
			if (leader != null)
			{
				for (int[] reward : Config.TOURNAMENT_ITEMS_REWARD)
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
			
			if (assist != null)
			{
				for (int[] reward : Config.TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = assist.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], assist, null);
						
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
						
						assist.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, assist, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							assist.sendPacket(sm);
						}
					}
				}
			}
			sendPacket("Congratulations, your team won the event!", 5);
		}

		public void setInTournamentEvent(boolean val)
		{
			if(leader != null)
				leader.setInArenaEvent(val);
			
			if(assist != null)
				assist.setInArenaEvent(val);
		}

		public void revive() 
		{
			if (leader != null)
				leader.doRevive();
			
			if (assist != null)
				assist.doRevive();
		}
		
		public void removeBuff() 
		{
			if (leader != null)
				leader.stopPhoenixBlessing(null);
			
			if (assist != null)
				assist.stopPhoenixBlessing(null);
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
			
			if (assist != null)
			{
				final Summon summon = assist.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(assist);
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
			if (assist != null)
			{
				assist.setIsInvul(val);
				assist.setIsParalyzed(val);
			}
		}

		public void sendPacket(String message, int duration)
		{
			if (leader != null)
				leader.sendPacket(new ExShowScreenMessage(message, duration * 1000));
			
			if (assist != null) 
				assist.sendPacket(new ExShowScreenMessage(message, duration * 1000));
		}

	    public void saveTitle()
	    {
	      if ((this.leader != null) && (this.leader.isOnline()))
	      {
	        this.leader._originalTitleColorTournament = this.leader.getAppearance().getNameColor();
	        this.leader._originalTitleTournament = this.leader.getTitle();
	      }
	      if ((this.assist != null) && (this.assist.isOnline()))
	      {
	        this.assist._originalTitleColorTournament = this.assist.getAppearance().getNameColor();
	        this.assist._originalTitleTournament = this.assist.getTitle();
	      }
	    }


	    public void setArenaAttack(boolean val)
	    {
	      if ((this.leader != null) && (this.leader.isOnline())) {
	        this.leader.setArenaAttack(val);
	      }
	      if ((this.assist != null) && (this.assist.isOnline())) {
	        this.assist.setArenaAttack(val);
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
	      if ((this.assist != null) && (this.assist.isOnline()))
	      {
	        this.assist.setTitle(title);
	        this.assist.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.assist.broadcastUserInfo();
	        this.assist.broadcastTitleInfo();
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
	      if ((this.assist != null) && (this.assist.isOnline()))
	      {
	        this.assist.getAppearance().setTitleColor(this.assist._originalTitleColorTournament);
	        this.assist.setTitle(this.assist._originalTitleTournament);
	        this.assist.broadcastUserInfo();
	        this.assist.broadcastTitleInfo();
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
	      if ((this.assist.getClassId() != ClassId.SHILLIEN_ELDER) && (this.assist.getClassId() != ClassId.SHILLIEN_SAINT) && (this.assist.getClassId() != ClassId.BISHOP) && (this.assist.getClassId() != ClassId.CARDINAL) && (this.assist.getClassId() != ClassId.ELVEN_ELDER) && (this.assist.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.assist.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.assist.stopSkillEffects(effect.getSkill().getId());
	          }
	        }
	      }
	    }


	    
	    public void sendPacketinit(String message, int duration)
	    {
	      if ((this.leader != null) && (this.leader.isOnline())) {
	        this.leader.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
	      }
	      if ((this.assist != null) && (this.assist.isOnline())) {
	        this.assist.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
	      }
	      if ((this.leader.getClassId() == ClassId.SHILLIEN_ELDER) || (this.leader.getClassId() == ClassId.SHILLIEN_SAINT) || (this.leader.getClassId() == ClassId.BISHOP) || (this.leader.getClassId() == ClassId.CARDINAL) || (this.leader.getClassId() == ClassId.ELVEN_ELDER) || (this.leader.getClassId() == ClassId.EVAS_SAINT)) {
	        ThreadPool.schedule(new Runnable()
	        {
	          @Override
			public void run()
	          {
	            Tournament.Pair.this.leader.getClient().closeNow();
	          }
	        }, 100L);
	      } else if ((this.assist.getClassId() == ClassId.SHILLIEN_ELDER) || (this.assist.getClassId() == ClassId.SHILLIEN_SAINT) || (this.assist.getClassId() == ClassId.BISHOP) || (this.assist.getClassId() == ClassId.CARDINAL) || (this.assist.getClassId() == ClassId.ELVEN_ELDER) || (this.assist.getClassId() == ClassId.EVAS_SAINT)) {
	        ThreadPool.schedule(new Runnable()
	        {
	          @Override
			public void run()
	          {
	            Tournament.Pair.this.assist.getClient().closeNow();
	          }
	        }, 100L);
	      }
	    }
	    

		    
	}

	  

	
	
	
	private class EvtArenaTask implements Runnable 
	{
		private final Pair pairOne;
		private final Pair pairTwo;
		private final int pOneX, pOneY, pOneZ, pTwoX, pTwoY, pTwoZ;
		private Arena arena;

		public EvtArenaTask(List<Pair> opponents) 
		{
			pairOne = opponents.get(0);
			pairTwo = opponents.get(1);
			Player leader = pairOne.getLeader();
			pOneX = leader.getX();
			pOneY = leader.getY();
			pOneZ = leader.getZ();
			leader = pairTwo.getLeader();
			pTwoX = leader.getX();
			pTwoY = leader.getY();
			pTwoZ = leader.getZ();
		}

		@Override
		public void run() 
		{
		      Tournament.this.free -= 1;
		      pairOne.saveTitle();
		      pairTwo.saveTitle();
		      portPairsToArena();
		      pairOne.sendPacket("The battle starts in 30 seconds!", 15);
		      pairTwo.sendPacket("The battle starts in 30 seconds!", 15);
		      try
		      {
		        Thread.sleep(Config.ARENA_WAIT_INTERVAL);
		      }
		      catch (InterruptedException e1)
		      {
		        e1.printStackTrace();
		      }
		    this.pairOne.sendPacketinit("Started. Good Fight!", 6);
		    this.pairTwo.sendPacketinit("Started. Good Fight!", 6);
		    pairOne.EventTitle(Config.MSG_TEAM1, Config.TITLE_COLOR_TEAM1);
		    pairTwo.EventTitle(Config.MSG_TEAM2, Config.TITLE_COLOR_TEAM2);
			pairOne.setImobilised(false);
			pairTwo.setImobilised(false);
		    pairOne.setArenaAttack(true);
		    pairTwo.setArenaAttack(true);
			pairOne.removeBuff();
			pairTwo.removeBuff();
			pairOne.removeSummon();
			pairTwo.removeSummon();

			while(check())
			{
				// check players status each  seconds
				try
				{
					Thread.sleep(Config.ARENA_CHECK_INTERVAL);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
					break;
				}
			}
			finishDuel();
		      Tournament.this.free += 1;
		}

		private void finishDuel()
		{
			fights.remove(arena.id);
			rewardWinner();
		    pairOne.backTitle();
		    pairTwo.backTitle();
			pairOne.revive();
			pairTwo.revive();
			pairOne.teleportTo(pOneX, pOneY, pOneZ);
			pairTwo.teleportTo(pTwoX, pTwoY, pTwoZ);
			pairOne.setInTournamentEvent(false);
			pairTwo.setInTournamentEvent(false);
			arena.setFree(true);
		}

		private void rewardWinner() 
		{
			if(pairOne.isAlive() && !pairTwo.isAlive())
			{
				pairOne.rewards();
			} 
			else if(pairTwo.isAlive() && !pairOne.isAlive())
			{
				pairTwo.rewards();
			}
		}

		private boolean check()
		{
			return (pairOne.isAlive() && pairTwo.isAlive());
		}

		private void portPairsToArena()
		{
			for(Arena arena : arenas)
			{
				if(arena.isFree)
				{
					this.arena = arena;
					arena.setFree(false);
					pairOne.teleportTo(arena.x - 850, arena.y, arena.z);
					pairTwo.teleportTo(arena.x + 850, arena.y, arena.z);
					pairOne.setImobilised(true);
					pairTwo.setImobilised(true);
					pairOne.setInTournamentEvent(true);
					pairTwo.setInTournamentEvent(true);
			        pairOne.removeSkills();
			        pairTwo.removeSkills();
					fights.put(this.arena.id, pairOne.getLeader().getName() +" vs "+ pairTwo.getLeader().getName());
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
	

	
	
	public static class SingletonHolder
	{
		protected static final Tournament INSTANCE = new Tournament();
	}

}