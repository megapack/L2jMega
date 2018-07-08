package net.sf.l2j.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.ExRedSky;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * This class manages all RaidBoss. In a group mob, there are one master called RaidBoss and several slaves called Minions.
 */
public class RaidBoss extends Monster
{
	private StatusEnum _raidStatus;
	private ScheduledFuture<?> _maintenanceTask;
	
	/**
	 * Constructor of L2RaidBossInstance (use Creature and L2NpcInstance constructor).
	 * <ul>
	 * <li>Call the Creature constructor to set the _template of the L2RaidBossInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2RaidBossInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li>
	 * </ul>
	 * @param objectId Identifier of the object to initialized
	 * @param template L2NpcTemplate to apply to the NPC
	 */
	public RaidBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setIsRaid(true);
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		if (killer != null)
		{
			final Player player = killer.getActingPlayer();
			if (player != null)
			{
				
		        if ((Config.ALLOW_AUTO_NOBLESS_FROM_BOSS) && (getNpcId() == Config.BOSS_ID)) {
		            if (player.getParty() != null)
		            {
		              for (Player member : player.getParty().getMembers()) {
		                if (member.isNoble())
		                {
		                  member.sendMessage("[SYS]:" + "Your party gained nobless status for defeating " + getName() + "!");
		                }
		                else if (member.isInsideRadius(getX(), getY(), getZ(), Config.RADIUS_TO_RAID, false, false))
		                {
		                  member.setNoble(true, true);
		                  member.sendMessage("[SYS]:" + "You and your party gained nobless status for defeating " + getName() + "!");
		                }
		                else
		                {
		                  member.sendMessage("SYS" + "Your party killed " + getName() + "! But you were to far away and earned nothing...");
		                }
		              }
		            }
		            else if ((player.getParty() == null) && (!player.isNoble()))
		            {
		              player.setNoble(true, true);
		              player.sendMessage("[SYS]:" + "You gained nobless status for defeating " + getName() + "!");
		            }
		          }
				
				if (Config.ENABLE_GUARDIAN)
				{
					if (getNpcId() == Config.ID_GUARDIAN_BAIUM)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_baium.add(spawnNPC(Config.TELEPORT_BAIUM_LOCX, Config.TELEPORT_BAIUM_LOCY, Config.TELEPORT_BAIUM_LOCZ, Config.ID_TELEPORT_BAIUM));
							}
						}, Config.TELEPORT_BAIUM_RESPAWN_TIME * 1000);
					}
					else if (getNpcId() == Config.ID_GUARDIAN_ZAKEN)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_zaken.add(spawnNPC(Config.TELEPORT_ZAKEN_LOCX, Config.TELEPORT_ZAKEN_LOCY, Config.TELEPORT_ZAKEN_LOCZ, Config.ID_TELEPORT_ZAKEN));
							}
						}, Config.TELEPORT_ZAKEN_RESPAWN_TIME * 1000);
					}
					else if (getNpcId() == Config.ID_GUARDIAN_ANTHARAS)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_antharas.add(spawnNPC(Config.TELEPORT_ANTHARAS_LOCX, Config.TELEPORT_ANTHARAS_LOCY, Config.TELEPORT_ANTHARAS_LOCZ, Config.ID_TELEPORT_ANTHARAS));
							}
						}, Config.TELEPORT_ANTHARAS_RESPAWN_TIME * 1000);
					}
					else if (getNpcId() == Config.ID_GUARDIAN_FRINTEZZA)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_frintezza.add(spawnNPC(Config.TELEPORT_FRINTEZZA_LOCX, Config.TELEPORT_FRINTEZZA_LOCY, Config.TELEPORT_FRINTEZZA_LOCZ, Config.ID_TELEPORT_FRINTEZZA));
							}
						}, Config.TELEPORT_FRINTEZZA_RESPAWN_TIME * 1000);
					}
					else if (getNpcId() == Config.ID_GUARDIAN_VALAKAS)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_valakas.add(spawnNPC(Config.TELEPORT_VALAKAS_LOCX, Config.TELEPORT_VALAKAS_LOCY, Config.TELEPORT_VALAKAS_LOCZ, Config.ID_TELEPORT_VALAKAS));
							}
						}, Config.TELEPORT_VALAKAS_RESPAWN_TIME * 1000);
					}
					
					if (getNpcId() == Config.ID_GUARDIAN_B1)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_B1.add(spawnNPC(Config.TELEPORT_B1_LOCX, Config.TELEPORT_B1_LOCY, Config.TELEPORT_B1_LOCZ, Config.ID_TELEPORT_B1));
							}
						}, Config.TELEPORT_B1_RESPAWN_TIME * 1000);
					}
					
					
					if (getNpcId() == Config.ID_GUARDIAN_B2)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_B2.add(spawnNPC(Config.TELEPORT_B2_LOCX, Config.TELEPORT_B2_LOCY, Config.TELEPORT_B2_LOCZ, Config.ID_TELEPORT_B2));
							}
						}, Config.TELEPORT_B2_RESPAWN_TIME * 1000);
					}
					
					if (getNpcId() == Config.ID_GUARDIAN_B3)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_B3.add(spawnNPC(Config.TELEPORT_B3_LOCX, Config.TELEPORT_B3_LOCY, Config.TELEPORT_B3_LOCZ, Config.ID_TELEPORT_B3));
							}
						}, Config.TELEPORT_B3_RESPAWN_TIME * 1000);
					}
					
					
					if (getNpcId() == Config.ID_GUARDIAN_B4)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_B4.add(spawnNPC(Config.TELEPORT_B4_LOCX, Config.TELEPORT_B4_LOCY, Config.TELEPORT_B4_LOCZ, Config.ID_TELEPORT_B4));
							}
						}, Config.TELEPORT_B4_RESPAWN_TIME * 1000);
					}
					
					if (getNpcId() == Config.ID_GUARDIAN_B5)
					{
						ThreadPool.schedule(new Runnable()
						{
							@Override
							public void run()
							{
								teleport_B5.add(spawnNPC(Config.TELEPORT_B5_LOCX, Config.TELEPORT_B5_LOCY, Config.TELEPORT_B5_LOCZ, Config.ID_TELEPORT_B5));
							}
						}, Config.TELEPORT_B5_RESPAWN_TIME * 1000);
					}
				}
				
				if (Config.ANNOUNCE_RAIDBOS_ALIVE_KILL)
				{
			          for (Player pl : World.getInstance().getPlayers()) {
			              if (pl.getClan() != null) {
		              CreatureSay cs = new CreatureSay(pl.getObjectId(), Config.ANNOUNCE_ID, "",Config.RAID_BOSS_DEFEATED_BY_CLAN_MEMBER_MSG.replace("%raidboss%", getName()).replace("%player%", killer.getName()).replace("%clan%", player.getClan().getName()));
		              pl.sendPacket(cs);
		            } else {
			              CreatureSay cs = new CreatureSay(pl.getObjectId(), Config.ANNOUNCE_ID, "",Config.RAID_BOSS_DEFEATED_BY_PLAYER_MSG.replace("%raidboss%", getName()).replace("%player%", killer.getName()));
			              pl.sendPacket(cs);
				    }
				  }
				}

				if (Config.QUAKE_RAID_BOSS)
				{
				      ExRedSky packet = new ExRedSky(10);
				      Earthquake eq = new Earthquake(getX(), getY(), getZ(), 100, 10);
				      Broadcast.toAllOnlinePlayers(packet);
				      Broadcast.toAllOnlinePlayers(eq);
				}
				broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
				broadcastPacket(new PlaySound("systemmsg_e.1209"));
				
				final Party party = player.getParty();
				if (party != null)
				{
					for (Player member : party.getMembers())
					{
						RaidPointManager.getInstance().addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
						if (member.isNoble())
							Hero.getInstance().setRBkilled(member.getObjectId(), getNpcId());
					}
				}
				else
				{
					RaidPointManager.getInstance().addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
					if (player.isNoble())
						Hero.getInstance().setRBkilled(player.getObjectId(), getNpcId());
				}
			}
		}
		
		RaidBossSpawnManager.getInstance().updateStatus(this, true);
		return true;
	}
	
	protected static ArrayList<L2Spawn> teleport_baium = new ArrayList<>();
	protected static ArrayList<L2Spawn> teleport_zaken = new ArrayList<>();
	protected static ArrayList<L2Spawn> teleport_antharas = new ArrayList<>();
	protected static ArrayList<L2Spawn> teleport_frintezza = new ArrayList<>();
	protected static ArrayList<L2Spawn> teleport_valakas = new ArrayList<>();
	protected static ArrayList<L2Spawn> teleport_B1 = new ArrayList<>();
	protected static ArrayList<L2Spawn> teleport_B2 = new ArrayList<>();
	protected static ArrayList<L2Spawn> teleport_B3 = new ArrayList<>();
	protected static ArrayList<L2Spawn> teleport_B4 = new ArrayList<>();
	protected static ArrayList<L2Spawn> teleport_B5 = new ArrayList<>();
	
	protected static void unSpawnTeleBaium()
	{
		for (L2Spawn s : teleport_baium)
		{
			if (s == null)
			{
				teleport_baium.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);
		}
	}
	
	protected static void unSpawnTeleZaken()
	{
		for (L2Spawn s : teleport_zaken)
		{
			if (s == null)
			{
				teleport_zaken.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
	}
	
	protected static void unSpawnTeleAntharas()
	{
		for (L2Spawn s : teleport_antharas)
		{
			if (s == null)
			{
				teleport_antharas.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
	}
	
	protected static void unSpawnTeleFrintezza()
	{
		for (L2Spawn s : teleport_frintezza)
		{
			if (s == null)
			{
				teleport_frintezza.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
	}
	
	protected static void unSpawnTeleValakas()
	{
		for (L2Spawn s : teleport_valakas)
		{
			if (s == null)
			{
				teleport_valakas.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
	}
	
	protected static void unSpawnTeleB1()
	{
		for (L2Spawn s : teleport_B1)
		{
			if (s == null)
			{
				teleport_B1.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
	}
	
	protected static void unSpawnTeleB2()
	{
		for (L2Spawn s : teleport_B2)
		{
			if (s == null)
			{
				teleport_B2.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
	}
	
	protected static void unSpawnTeleB3()
	{
		for (L2Spawn s : teleport_B3)
		{
			if (s == null)
			{
				teleport_B3.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
	}
	
	protected static void unSpawnTeleB4()
	{
		for (L2Spawn s : teleport_B4)
		{
			if (s == null)
			{
				teleport_B4.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
	}
	
	protected static void unSpawnTeleB5()
	{
		for (L2Spawn s : teleport_B5)
		{
			if (s == null)
			{
				teleport_B5.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.doRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
	}
	
	protected static L2Spawn spawnNPC(int xPos, int yPos, int zPos, int npcId)
	{
		final NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
		
		try
		{
		      final L2Spawn _npcSpawn1 = new L2Spawn(template);
		      _npcSpawn1.setLoc(xPos, yPos, zPos, 0);
		      _npcSpawn1.setRespawnDelay(1);
		      
		      SpawnTable.getInstance().addNewSpawn(_npcSpawn1, false);
		      
		      _npcSpawn1.setRespawnState(true);
		      _npcSpawn1.doSpawn(false);
		      _npcSpawn1.getNpc().getStatus().setCurrentHp(9.99999999E8D);
		      _npcSpawn1.getNpc().isAggressive();
		      _npcSpawn1.getNpc().decayMe();
		      _npcSpawn1.getNpc().spawnMe(_npcSpawn1.getNpc().getX(), _npcSpawn1.getNpc().getY(), _npcSpawn1.getNpc().getZ());
		      _npcSpawn1.getNpc().broadcastPacket(new MagicSkillUse(_npcSpawn1.getNpc(), _npcSpawn1.getNpc(), 1034, 1, 1, 1));
			
			return _npcSpawn1;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Override
	public void deleteMe()
	{
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		super.deleteMe();
	}
	
	/**
	 * Spawn minions.<br>
	 * Also if boss is too far from home location at the time of this check, teleport it to home.
	 */
	@Override
	protected void startMaintenanceTask()
	{
		super.startMaintenanceTask();
		
		_maintenanceTask = ThreadPool.scheduleAtFixedRate(() ->
		{
			// If the boss is dead, movement disabled, is Gordon or is in combat, return.
			if (isDead() || isMovementDisabled() || getNpcId() == 29095 || isInCombat())
				return;
			
			// Spawn must exist.
			final L2Spawn spawn = getSpawn();
			if (spawn == null)
				return;
			
			// If the boss is above drift range (or 200 minimum), teleport him on his spawn.
			if (!isInsideRadius(spawn.getLocX(), spawn.getLocY(), spawn.getLocZ(), Math.max(Config.MAX_DRIFT_RANGE, 200), true, false))
				teleToLocation(spawn.getLoc(), 0);
		}, 60000, 30000);
	}
	
	public StatusEnum getRaidStatus()
	{
		return _raidStatus;
	}
	
	public void setRaidStatus(StatusEnum status)
	{
		_raidStatus = status;
	}
}