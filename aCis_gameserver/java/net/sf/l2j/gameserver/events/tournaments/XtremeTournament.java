package net.sf.l2j.gameserver.events.tournaments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class XtremeTournament implements Runnable
{
	// list of participants
	public static List<Team> registered;
	// number of Arenas 
	int free =  Config.XTREME_TOURNAMENT_EVENT_COUNT;
	// Arenas
	Arena[] arenas = new Arena[Config.XTREME_TOURNAMENT_EVENT_COUNT];
	// list of fights going on
	Map<Integer, String> fights = new HashMap<>(Config.XTREME_TOURNAMENT_EVENT_COUNT);

	public XtremeTournament() 
	{
		registered = new ArrayList<>();
		int[] coord;
		for(int i=0; i < Config.XTREME_TOURNAMENT_EVENT_COUNT; i++)
		{
			coord = Config.XTREME_TOURNAMENT_EVENT_LOCS[i];
			arenas[i] = new Arena(i, coord[0], coord[1], coord[2]);
		}

	}

	public static XtremeTournament getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public boolean register(Player player, Player assist1, Player assist2, Player assist3, Player assist4, Player assist5, Player assist6, Player assist7, Player assist8)
	{
		for (Team p : registered)
		{
			if (p.getLeader() == player || p.getAAssist() == player || p.getBAssist() == player || p.getCAssist() == player || p.getDAssist() == player || p.getEAssist() == player || p.getFAssist() == player || p.getGAssist() == player || p.getHAssist() == player)
			{
				player.sendMessage("Tournament: You already registered!");
				return false;
			} 
			else if (p.getLeader() == assist1 || p.getLeader() == assist2 || p.getLeader() == assist3 || p.getLeader() == assist4 || p.getLeader() == assist5 || p.getLeader() == assist6 || p.getLeader() == assist7 || p.getLeader() == assist8 || p.getAAssist() == assist1 || p.getBAssist() == assist2 || p.getCAssist() == assist3 || p.getDAssist() == assist4 || p.getEAssist() == assist5 || p.getFAssist() == assist6 || p.getGAssist() == assist7 || p.getHAssist() == assist8)
			{
				player.sendMessage("Tournament: Your partner already registered!");
				return false;
			}
		}
		return registered.add(new Team(player, assist1, assist2, assist3, assist4, assist5, assist6, assist7, assist8));
	}

	public boolean isRegistered(Player player)
	{
		for (Team p : registered) 
		{
			if (p.getLeader() == player || p.getAAssist() == player || p.getBAssist() == player || p.getCAssist() == player || p.getDAssist() == player || p.getEAssist() == player || p.getFAssist() == player || p.getGAssist() == player || p.getHAssist() == player)
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
			if (p.getLeader() == player || p.getAAssist() == player || p.getBAssist() == player || p.getCAssist() == player || p.getDAssist() == player || p.getEAssist() == player || p.getFAssist() == player || p.getGAssist() == player || p.getHAssist() == player)
			{
				registered.remove(p);
				player.sendMessage("Tournament 9x9: Your party was removed!");
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
					Thread.sleep(Config.XTREME_TOURNAMENT_CALL_INTERVAL); 
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
				Thread.sleep(Config.XTREME_TOURNAMENT_CALL_INTERVAL); 
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
		private Player leader, assist1, assist2, assist3, assist4, assist5, assist6, assist7, assist8;

		public Team(Player leader, Player assist1, Player assist2, Player assist3, Player assist4, Player assist5, Player assist6, Player assist7, Player assist8)
		{
			this.leader = leader;
			this.assist1 = assist1;
			this.assist2 = assist2;
			this.assist3 = assist3;
			this.assist4 = assist4;
			this.assist5 = assist5;
			this.assist6 = assist6;
			this.assist7 = assist7;
			this.assist8 = assist8;
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
		
		public Player getDAssist()
		{
			return assist4;
		}
		
		public Player getEAssist()
		{
			return assist5;
		}
		
		public Player getFAssist()
		{
			return assist6;
		}
		
		public Player getGAssist()
		{
			return assist7;
		}
		
		public Player getHAssist()
		{
			return assist8;
		}

		public Player getLeader()
		{
			return leader;
		}

		public boolean check()
		{
			if ((leader == null || !leader.isOnline()) && (assist1 != null && assist1.isOnline()) && (assist2 != null && assist2.isOnline()) && (assist3 != null && assist3.isOnline()) && (assist4 != null && assist4.isOnline()) && (assist5 != null && assist5.isOnline()) && (assist6 != null && assist6.isOnline()) && (assist7 != null && assist7.isOnline()) && (assist8 != null && assist8.isOnline()))	
			{
				assist1.sendMessage("Tournament: You participation in Event was Canceled.");
				return false;
			} 				
			else if ((assist1 == null || !assist1.isOnline()) && (assist2 == null || !assist2.isOnline()) && (assist3 == null || !assist3.isOnline()) && (assist4 == null || !assist4.isOnline()) && (assist5 == null || !assist5.isOnline()) && (assist6 == null || !assist6.isOnline()) && (assist7 == null || !assist7.isOnline()) && (assist8 == null || !assist8.isOnline()) && (leader != null && leader.isOnline()))
			{
				leader.sendMessage("Tournament: You participation in Event was Canceled.");
				return false;
			}
			return true;
		}
		
		public boolean isAlive()
		{
			if ((leader == null || leader.isDead() || !leader.isOnline() || !leader.isInArenaEvent()) && (assist1 == null || assist1.isDead() || !assist1.isOnline() || !assist1.isInArenaEvent()) && (assist1 == null || assist1.isDead() || !assist1.isOnline() || !assist1.isInArenaEvent()) && (assist2 == null || assist2.isDead() || !assist2.isOnline() || !assist2.isInArenaEvent()) && (assist3 == null || assist3.isDead() || !assist3.isOnline() || !assist3.isInArenaEvent()) && (assist4 == null || assist4.isDead() || !assist4.isOnline() || !assist4.isInArenaEvent()) && (assist5 == null || assist5.isDead() || !assist5.isOnline() || !assist5.isInArenaEvent()) && (assist6 == null || assist6.isDead() || !assist6.isOnline() || !assist6.isInArenaEvent()) && (assist7 == null || assist7.isDead() || !assist7.isOnline() || !assist7.isInArenaEvent()) && (assist8 == null || assist8.isDead() || !assist8.isOnline() || !assist8.isInArenaEvent())) 	
				return false;							

			return !(leader.isDead() && assist1.isDead() && assist2.isDead() && assist3.isDead() && assist4.isDead() && assist5.isDead() && assist6.isDead() && assist7.isDead() && assist8.isDead());
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
		          assist1.setLastCords(x, y + 200, z);
		          assist1.leaveObserverMode();
		        }
		        else
		        {
		          assist1.teleToLocation(x, y + 200, z, 0);
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
		          assist2.setLastCords(x, y + 150, z);
		          assist2.leaveObserverMode();
		        }
		        else
		        {
		          assist2.teleToLocation(x, y + 150, z, 0);
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
		          assist3.setLastCords(x, y + 100, z);
		          assist3.leaveObserverMode();
		        }
		        else
		        {
		          assist3.teleToLocation(x, y + 100, z, 0);
		        }
		        assist3.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist3.isInDuel() && (assist3.getDuelState() != Duel.DuelState.INTERRUPTED))))
					assist3.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
			
		      if ((assist4 != null) && (assist4.isOnline()))
		      {
		        assist4.setCurrentCp(assist4.getMaxCp());
		        assist4.setCurrentHp(assist4.getMaxHp());
		        assist4.setCurrentMp(assist4.getMaxMp());
		        if (assist4.isInObserverMode())
		        {
		          assist4.setLastCords(x, y + 50, z);
		          assist4.leaveObserverMode();
		        }
		        else
		        {
		          assist4.teleToLocation(x, y + 50, z, 0);
		        }
		        assist4.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist4.isInDuel() && (assist4.getDuelState() != Duel.DuelState.INTERRUPTED))))
					assist4.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
			
		      if ((assist5 != null) && (assist5.isOnline()))
		      {
		        assist5.setCurrentCp(assist5.getMaxCp());
		        assist5.setCurrentHp(assist5.getMaxHp());
		        assist5.setCurrentMp(assist5.getMaxMp());
		        if (assist5.isInObserverMode())
		        {
		          assist5.setLastCords(x, y - 200, z);
		          assist5.leaveObserverMode();
		        }
		        else
		        {
		          assist5.teleToLocation(x, y - 200, z, 0);
		        }
		        assist5.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist5.isInDuel() && (assist5.getDuelState() != Duel.DuelState.INTERRUPTED))))
					assist5.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
			
		      if ((assist6 != null) && (assist6.isOnline()))
		      {
		        assist6.setCurrentCp(assist6.getMaxCp());
		        assist6.setCurrentHp(assist6.getMaxHp());
		        assist6.setCurrentMp(assist6.getMaxMp());
		        if (assist6.isInObserverMode())
		        {
		          assist6.setLastCords(x, y - 150, z);
		          assist6.leaveObserverMode();
		        }
		        else
		        {
		          assist6.teleToLocation(x, y - 150, z, 0);
		        }
		        assist6.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist6.isInDuel() && (assist6.getDuelState() != Duel.DuelState.INTERRUPTED))))
					assist6.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
			
		      if ((assist7 != null) && (assist7.isOnline()))
		      {
		        assist7.setCurrentCp(assist7.getMaxCp());
		        assist7.setCurrentHp(assist7.getMaxHp());
		        assist7.setCurrentMp(assist7.getMaxMp());
		        if (assist7.isInObserverMode())
		        {
		          assist7.setLastCords(x, y - 100, z);
		          assist7.leaveObserverMode();
		        }
		        else
		        {
		          assist7.teleToLocation(x, y - 100, z, 0);
		        }
		        assist7.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist7.isInDuel() && (assist7.getDuelState() != Duel.DuelState.INTERRUPTED))))
					assist7.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
			
		      if ((assist8 != null) && (assist8.isOnline()))
		      {
		        assist8.setCurrentCp(assist8.getMaxCp());
		        assist8.setCurrentHp(assist8.getMaxHp());
		        assist8.setCurrentMp(assist8.getMaxMp());
		        if (assist8.isInObserverMode())
		        {
		          assist8.setLastCords(x, y - 50, z);
		          assist8.leaveObserverMode();
		        }
		        else
		        {
		          assist8.teleToLocation(x, y - 50, z, 0);
		        }
		        assist8.broadcastUserInfo();
				
				if ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 0) || ((Config.TOURNAMENT_EVENT_EFFECTS_REMOVAL == 1) || (assist8.isInDuel() && (assist8.getDuelState() != Duel.DuelState.INTERRUPTED))))
					assist8.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
		}

		public void rewards()
		{ 
			SystemMessage sm = null;
			
			if (leader != null)
			{
				for (int[] reward : Config.XTREME_TOURNAMENT_ITEMS_REWARD)
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
				for (int[] reward : Config.XTREME_TOURNAMENT_ITEMS_REWARD)
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
				for (int[] reward : Config.XTREME_TOURNAMENT_ITEMS_REWARD)
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
				for (int[] reward : Config.XTREME_TOURNAMENT_ITEMS_REWARD)
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
			
			if (assist4 != null)
			{
				for (int[] reward : Config.XTREME_TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = assist4.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], assist4, null);
						
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
						
						assist4.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, assist4, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							assist4.sendPacket(sm);
						}
					}
				}
			}
			
			if (assist5 != null)
			{
				for (int[] reward : Config.XTREME_TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = assist5.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], assist5, null);
						
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
						
						assist5.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, assist5, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							assist5.sendPacket(sm);
						}
					}
				}
			}
			
			if (assist6 != null)
			{
				for (int[] reward : Config.XTREME_TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = assist6.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], assist6, null);
						
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
						
						assist6.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, assist6, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							assist6.sendPacket(sm);
						}
					}
				}
			}
			
			if (assist7 != null)
			{
				for (int[] reward : Config.XTREME_TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = assist7.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], assist7, null);
						
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
						
						assist7.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, assist7, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							assist7.sendPacket(sm);
						}
					}
				}
			}
			
			if (assist8 != null)
			{
				for (int[] reward : Config.XTREME_TOURNAMENT_ITEMS_REWARD)
				{
					PcInventory inv = assist8.getInventory();
					
					// Check for stackable item, non stackabe items need to be added one by one
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					{
						inv.addItem("Tournament Reward:", reward[0], reward[1], assist8, null);
						
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
						
						assist8.sendPacket(sm);
					}
					else
					{
						for (int i = 0; i < reward[1]; ++i)
						{
							inv.addItem("Tournament Reward:", reward[0], 1, assist8, null);
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
							sm.addItemName(reward[0]);
							assist8.sendPacket(sm);
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

			if (assist4 != null)
				assist4.setInArenaEvent(val);

			if (assist5 != null)
				assist5.setInArenaEvent(val);

			if (assist6 != null)
				assist6.setInArenaEvent(val);

			if (assist7 != null)
				assist7.setInArenaEvent(val);
			
			if (assist8 != null)
				assist8.setInArenaEvent(val);
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
			
			if (assist4 != null)
				assist4.doRevive();
			
			if (assist5 != null)
				assist5.doRevive();
			
			if (assist6 != null)
				assist6.doRevive();
			
			if (assist7 != null)
				assist7.doRevive();
			
			if (assist8 != null)
				assist8.doRevive();
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
			
			if (assist4 != null)
				assist4.stopPhoenixBlessing(null);
			
			if (assist5 != null)
				assist5.stopPhoenixBlessing(null);
			
			if (assist6 != null)
				assist6.stopPhoenixBlessing(null);
			
			if (assist7 != null)
				assist7.stopPhoenixBlessing(null);
			
			if (assist8 != null)
				assist8.stopPhoenixBlessing(null);
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
			
			if (assist4 != null)
			{
				final Summon summon = assist4.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(assist4);
				}
			}
			
			if (assist5 != null)
			{
				final Summon summon = assist5.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(assist5);
				}
			}
			
			if (assist6 != null)
			{
				final Summon summon = assist6.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(assist6);
				}
			}
			
			if (assist7 != null)
			{
				final Summon summon = assist7.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(assist7);
				}
			}
			
			if (assist8 != null)
			{
				final Summon summon = assist8.getPet();
				if (summon != null)
				{
					summon.stopAllEffectsExceptThoseThatLastThroughDeath();
					summon.abortAttack();
					summon.abortCast();
					
					if (summon instanceof Pet)
						summon.unSummon(assist8);
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
			
			if (assist4 != null)
			{
				assist4.setIsInvul(val);
				assist4.setIsParalyzed(val);
			}
			
			if (assist5 != null)
			{
				assist5.setIsInvul(val);
				assist5.setIsParalyzed(val);
			}
			
			if (assist6 != null)
			{
				assist6.setIsInvul(val);
				assist6.setIsParalyzed(val);
			}
			
			if (assist7 != null)
			{
				assist7.setIsInvul(val);
				assist7.setIsParalyzed(val);
			}
			
			if (assist8 != null)
			{
				assist8.setIsInvul(val);
				assist8.setIsParalyzed(val);
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
			
			if (assist4 != null) 
				assist4.sendPacket(new ExShowScreenMessage(message, duration * 1000));
			
			if (assist5 != null) 
				assist5.sendPacket(new ExShowScreenMessage(message, duration * 1000));
			
			if (assist6 != null) 
				assist6.sendPacket(new ExShowScreenMessage(message, duration * 1000));
			
			if (assist7 != null) 
				assist7.sendPacket(new ExShowScreenMessage(message, duration * 1000));
			
			if (assist8 != null) 
				assist8.sendPacket(new ExShowScreenMessage(message, duration * 1000));
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
	      if ((this.assist4 != null) && (this.assist4.isOnline()))
	      {
	        this.assist4.setTitle(title);
	        this.assist4.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.assist4.broadcastUserInfo();
	        this.assist4.broadcastTitleInfo();
	      }
	      if ((this.assist5 != null) && (this.assist5.isOnline()))
	      {
	        this.assist5.setTitle(title);
	        this.assist5.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.assist5.broadcastUserInfo();
	        this.assist5.broadcastTitleInfo();
	      }
	      
	      if ((this.assist6 != null) && (this.assist6.isOnline()))
	      {
	        this.assist6.setTitle(title);
	        this.assist6.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.assist6.broadcastUserInfo();
	        this.assist6.broadcastTitleInfo();
	      }
	      
	      if ((this.assist7 != null) && (this.assist7.isOnline()))
	      {
	        this.assist7.setTitle(title);
	        this.assist7.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.assist7.broadcastUserInfo();
	        this.assist7.broadcastTitleInfo();
	      }
	      
	      if ((this.assist8 != null) && (this.assist8.isOnline()))
	      {
	        this.assist8.setTitle(title);
	        this.assist8.getAppearance().setTitleColor(Integer.decode("0x" + color).intValue());
	        this.assist8.broadcastUserInfo();
	        this.assist8.broadcastTitleInfo();
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
	      
	      if ((this.assist4 != null) && (this.assist4.isOnline()))
	      {
	        this.assist4.getAppearance().setTitleColor(this.assist4._originalTitleColorTournament);
	        this.assist4.setTitle(this.assist4._originalTitleTournament);
	        this.assist4.broadcastUserInfo();
	        this.assist4.broadcastTitleInfo();
	      }
	      
	      if ((this.assist5 != null) && (this.assist5.isOnline()))
	      {
	        this.assist5.getAppearance().setTitleColor(this.assist5._originalTitleColorTournament);
	        this.assist5.setTitle(this.assist5._originalTitleTournament);
	        this.assist5.broadcastUserInfo();
	        this.assist5.broadcastTitleInfo();
	      }
	      
	      if ((this.assist6 != null) && (this.assist6.isOnline()))
	      {
	        this.assist6.getAppearance().setTitleColor(this.assist6._originalTitleColorTournament);
	        this.assist6.setTitle(this.assist6._originalTitleTournament);
	        this.assist6.broadcastUserInfo();
	        this.assist6.broadcastTitleInfo();
	      }
	      
	      if ((this.assist7 != null) && (this.assist7.isOnline()))
	      {
	        this.assist7.getAppearance().setTitleColor(this.assist7._originalTitleColorTournament);
	        this.assist7.setTitle(this.assist7._originalTitleTournament);
	        this.assist7.broadcastUserInfo();
	        this.assist7.broadcastTitleInfo();
	      }
	      
	      if ((this.assist8 != null) && (this.assist8.isOnline()))
	      {
	        this.assist8.getAppearance().setTitleColor(this.assist8._originalTitleColorTournament);
	        this.assist8.setTitle(this.assist8._originalTitleTournament);
	        this.assist8.broadcastUserInfo();
	        this.assist8.broadcastTitleInfo();
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
	      if ((this.assist4.getClassId() != ClassId.SHILLIEN_ELDER) && (this.assist4.getClassId() != ClassId.SHILLIEN_SAINT) && (this.assist4.getClassId() != ClassId.BISHOP) && (this.assist4.getClassId() != ClassId.CARDINAL) && (this.assist4.getClassId() != ClassId.ELVEN_ELDER) && (this.assist4.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.assist4.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.assist4.stopSkillEffects(effect.getSkill().getId());
	          }
	        }
	      }
	      if ((this.assist5.getClassId() != ClassId.SHILLIEN_ELDER) && (this.assist5.getClassId() != ClassId.SHILLIEN_SAINT) && (this.assist5.getClassId() != ClassId.BISHOP) && (this.assist5.getClassId() != ClassId.CARDINAL) && (this.assist5.getClassId() != ClassId.ELVEN_ELDER) && (this.assist5.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.assist5.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.assist5.stopSkillEffects(effect.getSkill().getId());
	          }
	        }
	      }
	      if ((this.assist6.getClassId() != ClassId.SHILLIEN_ELDER) && (this.assist6.getClassId() != ClassId.SHILLIEN_SAINT) && (this.assist6.getClassId() != ClassId.BISHOP) && (this.assist6.getClassId() != ClassId.CARDINAL) && (this.assist6.getClassId() != ClassId.ELVEN_ELDER) && (this.assist6.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.assist6.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.assist6.stopSkillEffects(effect.getSkill().getId());
	          }
	        }
	      }
	      if ((this.assist7.getClassId() != ClassId.SHILLIEN_ELDER) && (this.assist7.getClassId() != ClassId.SHILLIEN_SAINT) && (this.assist7.getClassId() != ClassId.BISHOP) && (this.assist7.getClassId() != ClassId.CARDINAL) && (this.assist7.getClassId() != ClassId.ELVEN_ELDER) && (this.assist7.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.assist7.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.assist7.stopSkillEffects(effect.getSkill().getId());
	          }
	        }
	      }
	      if ((this.assist8.getClassId() != ClassId.SHILLIEN_ELDER) && (this.assist8.getClassId() != ClassId.SHILLIEN_SAINT) && (this.assist8.getClassId() != ClassId.BISHOP) && (this.assist8.getClassId() != ClassId.CARDINAL) && (this.assist8.getClassId() != ClassId.ELVEN_ELDER) && (this.assist8.getClassId() != ClassId.EVAS_SAINT)) {
	        for (L2Effect effect : this.assist8.getAllEffects()) {
	          if (Config.ARENA_STOP_SKILL_LIST.contains(Integer.valueOf(effect.getSkill().getId()))) {
	            this.assist8.stopSkillEffects(effect.getSkill().getId());
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
	      if ((this.assist4 != null) && (this.assist4.isOnline())) {
		        this.assist4.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
		      }
	      if ((this.assist5 != null) && (this.assist5.isOnline())) {
		        this.assist5.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
		      }
	      if ((this.assist6 != null) && (this.assist6.isOnline())) {
		        this.assist6.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
		      }
	      if ((this.assist7 != null) && (this.assist7.isOnline())) {
		        this.assist7.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
		      }
	      if ((this.assist8 != null) && (this.assist8.isOnline())) {
		        this.assist8.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
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
				Thread.sleep(Config.XTREME_TOURNAMENT_WAIT_INTERVAL);
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
					Thread.sleep(Config.XTREME_TOURNAMENT_CHECK_INTERVAL);
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
			teamOne.revive();
			teamTwo.revive();
			teamOne.backTitle();
			teamTwo.backTitle();
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
		        
		        Clan owner = ClanTable.getInstance().getClan(leader1.getClanId());
		        Clan owner2 = ClanTable.getInstance().getClan(leader2.getClanId());
		        if ((owner != null) && (owner2 != null) && (leader1.getAllyId() != 0) && (leader2.getAllyId() != 0)) {
		          Announcements.Announce("[9x9]: " + owner.getAllyName() + " VS " + owner2.getAllyName() + ". Winner is: " + owner.getAllyName() + "!");
		        }
				teamOne.rewards();
			} 
			else if (teamTwo.isAlive() && !teamOne.isAlive())
			{
				Player leader1 = this.teamTwo.getLeader();
				Player leader2 = this.teamOne.getLeader();
		        
		        Clan owner = ClanTable.getInstance().getClan(leader1.getClanId());
		        Clan owner2 = ClanTable.getInstance().getClan(leader2.getClanId());
		        if ((owner != null) && (owner2 != null) && (leader1.getAllyId() != 0) && (leader2.getAllyId() != 0)) {
		          Announcements.Announce("[9x9]: " + owner.getAllyName() + " VS " + owner2.getAllyName() + ". Winner is: " + owner.getAllyName() + "!");
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
		protected static final XtremeTournament INSTANCE = new XtremeTournament();
	}
}