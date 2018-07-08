package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.instance.SepulcherMonster;
import net.sf.l2j.gameserver.model.actor.instance.SepulcherNpc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.QuestState;

/**
 * @author sandman
 */
public class FourSepulchersManager
{
	protected static final Logger _log = Logger.getLogger(FourSepulchersManager.class.getName());
	
	private static final String QUEST_ID = "Q620_FourGoblets";
	
	private static final int ENTRANCE_PASS = 7075;
	private static final int USED_PASS = 7261;
	private static final int CHAPEL_KEY = 7260;
	private static final int ANTIQUE_BROOCH = 7262;
	
	protected boolean _firstTimeRun;
	protected boolean _inEntryTime = false;
	protected boolean _inAttackTime = false;
	
	protected ScheduledFuture<?> _changeCoolDownTimeTask = null;
	protected ScheduledFuture<?> _changeEntryTimeTask = null;
	protected ScheduledFuture<?> _changeWarmUpTimeTask = null;
	protected ScheduledFuture<?> _changeAttackTimeTask = null;
	
	private final int[][] _startHallSpawn =
	{
		{
			181632,
			-85587,
			-7218
		},
		{
			179963,
			-88978,
			-7218
		},
		{
			173217,
			-86132,
			-7218
		},
		{
			175608,
			-82296,
			-7218
		}
	};
	
	private final int[][][] _shadowSpawnLoc =
	{
		{
			{
				25339,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25349,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25346,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25342,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25342,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25339,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25349,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25346,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25346,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25342,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25339,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25349,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25349,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25346,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25342,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25339,
				175591,
				-72744,
				-7215,
				49317
			}
		},
	};
	
	protected Map<Integer, Boolean> _archonSpawned = new HashMap<>();
	protected Map<Integer, Boolean> _hallInUse = new HashMap<>();
	protected Map<Integer, Player> _challengers = new HashMap<>();
	protected Map<Integer, int[]> _startHallSpawns = new HashMap<>();
	protected Map<Integer, Integer> _hallGateKeepers = new HashMap<>();
	protected Map<Integer, Integer> _keyBoxNpc = new HashMap<>();
	protected Map<Integer, Integer> _victim = new HashMap<>();
	protected Map<Integer, L2Spawn> _executionerSpawns = new HashMap<>();
	protected Map<Integer, L2Spawn> _keyBoxSpawns = new HashMap<>();
	protected Map<Integer, L2Spawn> _mysteriousBoxSpawns = new HashMap<>();
	protected Map<Integer, L2Spawn> _shadowSpawns = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> _dukeFinalMobs = new HashMap<>();
	protected Map<Integer, List<SepulcherMonster>> _dukeMobs = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> _emperorsGraveNpcs = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> _magicalMonsters = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> _physicalMonsters = new HashMap<>();
	protected Map<Integer, List<SepulcherMonster>> _viscountMobs = new HashMap<>();
	
	protected List<L2Spawn> _physicalSpawns;
	protected List<L2Spawn> _magicalSpawns;
	protected List<L2Spawn> _managers;
	protected List<L2Spawn> _dukeFinalSpawns;
	protected List<L2Spawn> _emperorsGraveSpawns;
	protected List<Npc> _allMobs = new ArrayList<>();
	
	protected long _attackTimeEnd = 0;
	protected long _coolDownTimeEnd = 0;
	protected long _entryTimeEnd = 0;
	protected long _warmUpTimeEnd = 0;
	
	protected byte _newCycleMin = 55;
	
	protected FourSepulchersManager()
	{
	}
	
	public static final FourSepulchersManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void init()
	{
		if (_changeCoolDownTimeTask != null)
		{
			_changeCoolDownTimeTask.cancel(true);
			_changeCoolDownTimeTask = null;
		}
		
		if (_changeEntryTimeTask != null)
		{
			_changeEntryTimeTask.cancel(true);
			_changeEntryTimeTask = null;
		}
		
		if (_changeWarmUpTimeTask != null)
		{
			_changeWarmUpTimeTask.cancel(true);
			_changeWarmUpTimeTask = null;
		}
		
		if (_changeAttackTimeTask != null)
		{
			_changeAttackTimeTask.cancel(true);
			_changeAttackTimeTask = null;
		}
		
		_inEntryTime = false;
		_inAttackTime = false;
		_firstTimeRun = true;
		
		initFixedInfo();
		loadMysteriousBox();
		initKeyBoxSpawns();
		loadPhysicalMonsters();
		loadMagicalMonsters();
		initLocationShadowSpawns();
		initExecutionerSpawns();
		loadDukeMonsters();
		loadEmperorsGraveMonsters();
		spawnManagers();
		timeSelector();
	}
	
	public void stop()
	{
		if (_changeCoolDownTimeTask != null)
		{
			_changeCoolDownTimeTask.cancel(true);
			_changeCoolDownTimeTask = null;
		}
		
		if (_changeEntryTimeTask != null)
		{
			_changeEntryTimeTask.cancel(true);
			_changeEntryTimeTask = null;
		}
		
		if (_changeWarmUpTimeTask != null)
		{
			_changeWarmUpTimeTask.cancel(true);
			_changeWarmUpTimeTask = null;
		}
		
		if (_changeAttackTimeTask != null)
		{
			_changeAttackTimeTask.cancel(true);
			_changeAttackTimeTask = null;
		}
	}
	
	// phase select on server launch
	protected void timeSelector()
	{
		timeCalculator();
		long currentTime = Calendar.getInstance().getTimeInMillis();
		
		// if current time >= time of entry beginning and if current time < time of entry beginning + time of entry end
		if (currentTime >= _coolDownTimeEnd && currentTime < _entryTimeEnd) // entry time check
		{
			clean();
			_changeEntryTimeTask = ThreadPool.schedule(new ChangeEntryTime(), 0);
			_log.info("FourSepulchersManager: entry time.");
		}
		else if (currentTime >= _entryTimeEnd && currentTime < _warmUpTimeEnd) // warmup time check
		{
			clean();
			_changeWarmUpTimeTask = ThreadPool.schedule(new ChangeWarmUpTime(), 0);
			_log.info("FourSepulchersManager: warmUp time.");
		}
		else if (currentTime >= _warmUpTimeEnd && currentTime < _attackTimeEnd) // attack time check
		{
			clean();
			_changeAttackTimeTask = ThreadPool.schedule(new ChangeAttackTime(), 0);
			_log.info("FourSepulchersManager: attack time.");
		}
		else
		// else cooldown time and without cleanup because it's already implemented
		{
			_changeCoolDownTimeTask = ThreadPool.schedule(new ChangeCoolDownTime(), 0);
			_log.info("FourSepulchersManager: cooldown time.");
		}
	}
	
	protected void timeCalculator()
	{
		Calendar tmp = Calendar.getInstance();
		
		if (tmp.get(Calendar.MINUTE) < _newCycleMin)
			tmp.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) - 1);
		tmp.set(Calendar.MINUTE, _newCycleMin);
		
		_coolDownTimeEnd = tmp.getTimeInMillis();
		_entryTimeEnd = _coolDownTimeEnd + Config.FS_TIME_ENTRY * 60000l;
		_warmUpTimeEnd = _entryTimeEnd + Config.FS_TIME_WARMUP * 60000l;
		_attackTimeEnd = _warmUpTimeEnd + Config.FS_TIME_ATTACK * 60000l;
	}
	
	public void clean()
	{
		for (int i = 31921; i < 31925; i++)
		{
			final int[] location = _startHallSpawns.get(i);
			ZoneManager.getInstance().getZone(location[0], location[1], location[2], BossZone.class).oustAllPlayers();
		}
		
		deleteAllMobs();
		closeAllDoors();
		
		_hallInUse.clear();
		_hallInUse.put(31921, false);
		_hallInUse.put(31922, false);
		_hallInUse.put(31923, false);
		_hallInUse.put(31924, false);
		
		if (!_archonSpawned.isEmpty())
		{
			for (int npcId : _archonSpawned.keySet())
				_archonSpawned.put(npcId, false);
		}
	}
	
	protected void spawnManagers()
	{
		_managers = new ArrayList<>();
		
		int i = 31921;
		for (L2Spawn spawn; i <= 31924; i++)
		{
			if (i < 31921 || i > 31924)
				continue;
			
			final NpcTemplate template = NpcData.getInstance().getTemplate(i);
			if (template == null)
				continue;
			
			try
			{
				spawn = new L2Spawn(template);
				
				spawn.setRespawnDelay(60);
				switch (i)
				{
					case 31921: // conquerors
						spawn.setLoc(181061, -85595, -7200, -32584);
						break;
					
					case 31922: // emperors
						spawn.setLoc(179292, -88981, -7200, -33272);
						break;
					
					case 31923: // sages
						spawn.setLoc(173202, -87004, -7200, -16248);
						break;
					
					case 31924: // judges
						spawn.setLoc(175606, -82853, -7200, -16248);
						break;
				}
				
				_managers.add(spawn);
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				spawn.doSpawn(false);
				spawn.setRespawnState(true);
				
				_log.info("FourSepulchersManager: spawned " + spawn.getTemplate().getName());
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Error while spawning managers: " + e.getMessage(), e);
			}
		}
	}
	
	protected void initFixedInfo()
	{
		_startHallSpawns.put(31921, _startHallSpawn[0]);
		_startHallSpawns.put(31922, _startHallSpawn[1]);
		_startHallSpawns.put(31923, _startHallSpawn[2]);
		_startHallSpawns.put(31924, _startHallSpawn[3]);
		
		_hallInUse.put(31921, false);
		_hallInUse.put(31922, false);
		_hallInUse.put(31923, false);
		_hallInUse.put(31924, false);
		
		_hallGateKeepers.put(31925, 25150012);
		_hallGateKeepers.put(31926, 25150013);
		_hallGateKeepers.put(31927, 25150014);
		_hallGateKeepers.put(31928, 25150015);
		_hallGateKeepers.put(31929, 25150016);
		_hallGateKeepers.put(31930, 25150002);
		_hallGateKeepers.put(31931, 25150003);
		_hallGateKeepers.put(31932, 25150004);
		_hallGateKeepers.put(31933, 25150005);
		_hallGateKeepers.put(31934, 25150006);
		_hallGateKeepers.put(31935, 25150032);
		_hallGateKeepers.put(31936, 25150033);
		_hallGateKeepers.put(31937, 25150034);
		_hallGateKeepers.put(31938, 25150035);
		_hallGateKeepers.put(31939, 25150036);
		_hallGateKeepers.put(31940, 25150022);
		_hallGateKeepers.put(31941, 25150023);
		_hallGateKeepers.put(31942, 25150024);
		_hallGateKeepers.put(31943, 25150025);
		_hallGateKeepers.put(31944, 25150026);
		
		_keyBoxNpc.put(18120, 31455);
		_keyBoxNpc.put(18121, 31455);
		_keyBoxNpc.put(18122, 31455);
		_keyBoxNpc.put(18123, 31455);
		_keyBoxNpc.put(18124, 31456);
		_keyBoxNpc.put(18125, 31456);
		_keyBoxNpc.put(18126, 31456);
		_keyBoxNpc.put(18127, 31456);
		_keyBoxNpc.put(18128, 31457);
		_keyBoxNpc.put(18129, 31457);
		_keyBoxNpc.put(18130, 31457);
		_keyBoxNpc.put(18131, 31457);
		_keyBoxNpc.put(18149, 31458);
		_keyBoxNpc.put(18150, 31459);
		_keyBoxNpc.put(18151, 31459);
		_keyBoxNpc.put(18152, 31459);
		_keyBoxNpc.put(18153, 31459);
		_keyBoxNpc.put(18154, 31460);
		_keyBoxNpc.put(18155, 31460);
		_keyBoxNpc.put(18156, 31460);
		_keyBoxNpc.put(18157, 31460);
		_keyBoxNpc.put(18158, 31461);
		_keyBoxNpc.put(18159, 31461);
		_keyBoxNpc.put(18160, 31461);
		_keyBoxNpc.put(18161, 31461);
		_keyBoxNpc.put(18162, 31462);
		_keyBoxNpc.put(18163, 31462);
		_keyBoxNpc.put(18164, 31462);
		_keyBoxNpc.put(18165, 31462);
		_keyBoxNpc.put(18183, 31463);
		_keyBoxNpc.put(18184, 31464);
		_keyBoxNpc.put(18212, 31465);
		_keyBoxNpc.put(18213, 31465);
		_keyBoxNpc.put(18214, 31465);
		_keyBoxNpc.put(18215, 31465);
		_keyBoxNpc.put(18216, 31466);
		_keyBoxNpc.put(18217, 31466);
		_keyBoxNpc.put(18218, 31466);
		_keyBoxNpc.put(18219, 31466);
		
		_victim.put(18150, 18158);
		_victim.put(18151, 18159);
		_victim.put(18152, 18160);
		_victim.put(18153, 18161);
		_victim.put(18154, 18162);
		_victim.put(18155, 18163);
		_victim.put(18156, 18164);
		_victim.put(18157, 18165);
	}
	
	private void loadMysteriousBox()
	{
		_mysteriousBoxSpawns.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE spawntype = ? ORDER BY id");
			statement.setInt(1, 0);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final NpcTemplate template = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template != null)
				{
					final L2Spawn spawn = new L2Spawn(template);
					spawn.setLoc(rset.getInt("locx"), rset.getInt("locy"), rset.getInt("locz"), rset.getInt("heading"));
					spawn.setRespawnDelay(rset.getInt("respawn_delay"));
					
					SpawnTable.getInstance().addNewSpawn(spawn, false);
					int keyNpcId = rset.getInt("key_npc_id");
					_mysteriousBoxSpawns.put(keyNpcId, spawn);
				}
				else
					_log.warning("FourSepulchersManager.LoadMysteriousBox: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
			}
			
			rset.close();
			statement.close();
			_log.info("FourSepulchersManager: loaded " + _mysteriousBoxSpawns.size() + " Mysterious-Box spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "FourSepulchersManager.LoadMysteriousBox: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void initKeyBoxSpawns()
	{
		for (Entry<Integer, Integer> keyNpc : _keyBoxNpc.entrySet())
		{
			try
			{
				final NpcTemplate template = NpcData.getInstance().getTemplate(keyNpc.getValue());
				if (template != null)
				{
					final L2Spawn spawn = new L2Spawn(template);
					
					SpawnTable.getInstance().addNewSpawn(spawn, false);
					_keyBoxSpawns.put(keyNpc.getKey(), spawn);
				}
				else
					_log.warning("FourSepulchersManager.InitKeyBoxSpawns: Data missing in NPC table for ID: " + keyNpc.getValue() + ".");
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "FourSepulchersManager.InitKeyBoxSpawns: Spawn could not be initialized: " + e.getMessage(), e);
			}
		}
	}
	
	private void loadPhysicalMonsters()
	{
		_physicalMonsters.clear();
		
		int loaded = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM spawnlist_4s WHERE spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 1);
			ResultSet rset1 = statement1.executeQuery();
			
			PreparedStatement statement2 = con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE key_npc_id = ? AND spawntype = ? ORDER BY id");
			while (rset1.next())
			{
				int keyNpcId = rset1.getInt("key_npc_id");
				
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 1);
				ResultSet rset2 = statement2.executeQuery();
				statement2.clearParameters();
				
				_physicalSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					final NpcTemplate template = NpcData.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template != null)
					{
						final L2Spawn spawn = new L2Spawn(template);
						spawn.setLoc(rset2.getInt("locx"), rset2.getInt("locy"), rset2.getInt("locz"), rset2.getInt("heading"));
						spawn.setRespawnDelay(rset2.getInt("respawn_delay"));
						
						SpawnTable.getInstance().addNewSpawn(spawn, false);
						_physicalSpawns.add(spawn);
						loaded++;
					}
					else
						_log.warning("FourSepulchersManager.LoadPhysicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
				}
				
				rset2.close();
				_physicalMonsters.put(keyNpcId, _physicalSpawns);
			}
			
			rset1.close();
			statement1.close();
			statement2.close();
			
			_log.info("FourSepulchersManager: loaded " + loaded + " Physical type monsters spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "FourSepulchersManager.LoadPhysicalMonsters: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void loadMagicalMonsters()
	{
		_magicalMonsters.clear();
		
		int loaded = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM spawnlist_4s WHERE spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 2);
			ResultSet rset1 = statement1.executeQuery();
			
			PreparedStatement statement2 = con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE key_npc_id = ? AND spawntype = ? ORDER BY id");
			while (rset1.next())
			{
				int keyNpcId = rset1.getInt("key_npc_id");
				
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 2);
				ResultSet rset2 = statement2.executeQuery();
				statement2.clearParameters();
				
				_magicalSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					final NpcTemplate template = NpcData.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template != null)
					{
						final L2Spawn spawn = new L2Spawn(template);
						spawn.setLoc(rset2.getInt("locx"), rset2.getInt("locy"), rset2.getInt("locz"), rset2.getInt("heading"));
						spawn.setRespawnDelay(rset2.getInt("respawn_delay"));
						
						SpawnTable.getInstance().addNewSpawn(spawn, false);
						_magicalSpawns.add(spawn);
						loaded++;
					}
					else
						_log.warning("FourSepulchersManager.LoadMagicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
				}
				
				rset2.close();
				_magicalMonsters.put(keyNpcId, _magicalSpawns);
			}
			
			rset1.close();
			statement1.close();
			statement2.close();
			
			_log.info("FourSepulchersManager: loaded " + loaded + " Magical type monsters spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "FourSepulchersManager.LoadMagicalMonsters: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void loadDukeMonsters()
	{
		_dukeFinalMobs.clear();
		_archonSpawned.clear();
		
		int loaded = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM spawnlist_4s WHERE spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 5);
			ResultSet rset1 = statement1.executeQuery();
			
			PreparedStatement statement2 = con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE key_npc_id = ? AND spawntype = ? ORDER BY id");
			while (rset1.next())
			{
				int keyNpcId = rset1.getInt("key_npc_id");
				
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 5);
				ResultSet rset2 = statement2.executeQuery();
				statement2.clearParameters();
				
				_dukeFinalSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					final NpcTemplate template = NpcData.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template != null)
					{
						final L2Spawn spawn = new L2Spawn(template);
						spawn.setLoc(rset2.getInt("locx"), rset2.getInt("locy"), rset2.getInt("locz"), rset2.getInt("heading"));
						spawn.setRespawnDelay(rset2.getInt("respawn_delay"));
						
						SpawnTable.getInstance().addNewSpawn(spawn, false);
						_dukeFinalSpawns.add(spawn);
						loaded++;
					}
					else
						_log.warning("FourSepulchersManager.LoadDukeMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
				}
				
				rset2.close();
				_dukeFinalMobs.put(keyNpcId, _dukeFinalSpawns);
				_archonSpawned.put(keyNpcId, false);
			}
			
			rset1.close();
			statement1.close();
			statement2.close();
			
			_log.info("FourSepulchersManager: loaded " + loaded + " Church of duke monsters spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "FourSepulchersManager.LoadDukeMonsters: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void loadEmperorsGraveMonsters()
	{
		
		_emperorsGraveNpcs.clear();
		
		int loaded = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM spawnlist_4s WHERE spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 6);
			ResultSet rset1 = statement1.executeQuery();
			
			PreparedStatement statement2 = con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE key_npc_id = ? AND spawntype = ? ORDER BY id");
			while (rset1.next())
			{
				int keyNpcId = rset1.getInt("key_npc_id");
				
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 6);
				ResultSet rset2 = statement2.executeQuery();
				statement2.clearParameters();
				
				_emperorsGraveSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					final NpcTemplate template = NpcData.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template != null)
					{
						final L2Spawn spawn = new L2Spawn(template);
						spawn.setLoc(rset2.getInt("locx"), rset2.getInt("locy"), rset2.getInt("locz"), rset2.getInt("heading"));
						spawn.setRespawnDelay(rset2.getInt("respawn_delay"));
						
						SpawnTable.getInstance().addNewSpawn(spawn, false);
						_emperorsGraveSpawns.add(spawn);
						loaded++;
					}
					else
						_log.warning("FourSepulchersManager.LoadEmperorsGraveMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
				}
				
				rset2.close();
				_emperorsGraveNpcs.put(keyNpcId, _emperorsGraveSpawns);
			}
			
			rset1.close();
			statement1.close();
			statement2.close();
			
			_log.info("FourSepulchersManager: loaded " + loaded + " Emperor's grave NPC spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "FourSepulchersManager.LoadEmperorsGraveMonsters: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	protected void initLocationShadowSpawns()
	{
		int locNo = Rnd.get(4);
		final int[] gateKeeper =
		{
			31929,
			31934,
			31939,
			31944
		};
		
		_shadowSpawns.clear();
		
		for (int i = 0; i <= 3; i++)
		{
			final NpcTemplate template = NpcData.getInstance().getTemplate(_shadowSpawnLoc[locNo][i][0]);
			if (template != null)
			{
				try
				{
					final L2Spawn spawn = new L2Spawn(template);
					spawn.setLoc(_shadowSpawnLoc[locNo][i][1], _shadowSpawnLoc[locNo][i][2], _shadowSpawnLoc[locNo][i][3], _shadowSpawnLoc[locNo][i][4]);
					
					SpawnTable.getInstance().addNewSpawn(spawn, false);
					int keyNpcId = gateKeeper[i];
					_shadowSpawns.put(keyNpcId, spawn);
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Error on InitLocationShadowSpawns", e);
				}
			}
			else
				_log.warning("FourSepulchersManager.InitLocationShadowSpawns: Data missing in NPC table for ID: " + _shadowSpawnLoc[locNo][i][0] + ".");
		}
	}
	
	protected void initExecutionerSpawns()
	{
		for (Entry<Integer, Integer> victimNpc : _victim.entrySet())
		{
			try
			{
				final NpcTemplate template = NpcData.getInstance().getTemplate(victimNpc.getValue());
				if (template != null)
				{
					final L2Spawn spawn = new L2Spawn(template);
					
					SpawnTable.getInstance().addNewSpawn(spawn, false);
					_executionerSpawns.put(victimNpc.getKey(), spawn);
				}
				else
					_log.warning("FourSepulchersManager.InitExecutionerSpawns: Data missing in NPC table for ID: " + victimNpc.getValue() + ".");
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "FourSepulchersManager.InitExecutionerSpawns: Spawn could not be initialized: " + e.getMessage(), e);
			}
		}
	}
	
	public boolean isEntryTime()
	{
		return _inEntryTime;
	}
	
	public boolean isAttackTime()
	{
		return _inAttackTime;
	}
	
	public synchronized void tryEntry(Npc npc, Player player)
	{
		final int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case 31921:
			case 31922:
			case 31923:
			case 31924:
				break;
			
			default:
				return;
		}
		
		if (_hallInUse.get(npcId).booleanValue())
		{
			showHtmlFile(player, npcId + "-FULL.htm", npc, null);
			return;
		}
		
		final Party party = player.getParty();
		if (party == null || party.getMembersCount() < Config.FS_PARTY_MEMBER_COUNT)
		{
			showHtmlFile(player, npcId + "-SP.htm", npc, null);
			return;
		}
		
		if (!party.isLeader(player))
		{
			showHtmlFile(player, npcId + "-NL.htm", npc, null);
			return;
		}
		
		for (Player member : party.getMembers())
		{
			QuestState qs = member.getQuestState(QUEST_ID);
			if (qs == null || (!qs.isStarted() && !qs.isCompleted()))
			{
				showHtmlFile(player, npcId + "-NS.htm", npc, member);
				return;
			}
			
			if (member.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
			{
				showHtmlFile(player, npcId + "-SE.htm", npc, member);
				return;
			}
			
			if (player.getWeightPenalty() >= 3)
			{
				member.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				return;
			}
		}
		
		if (!isEntryTime())
		{
			showHtmlFile(player, npcId + "-NE.htm", npc, null);
			return;
		}
		
		showHtmlFile(player, npcId + "-OK.htm", npc, null);
		
		final int[] location = _startHallSpawns.get(npcId);
		
		for (Player member : party.getMembers().stream().filter(m -> !m.isDead() && MathUtil.checkIfInRange(700, player, m, true)).collect(Collectors.toList()))
		{
			ZoneManager.getInstance().getZone(location[0], location[1], location[2], BossZone.class).allowPlayerEntry(member, 30);
			
			member.teleToLocation(location[0] + Rnd.get(-80, 80), location[1] + Rnd.get(-80, 80), location[2], 0);
			member.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, member, true);
			if (member.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
				member.addItem("Quest", USED_PASS, 1, member, true);
			
			final ItemInstance hallsKey = member.getInventory().getItemByItemId(CHAPEL_KEY);
			if (hallsKey != null)
				member.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), member, true);
		}
		
		_challengers.put(npcId, player);
		
		_hallInUse.put(npcId, true);
	}
	
	public void spawnMysteriousBox(int npcId)
	{
		if (!isAttackTime())
			return;
		
		final L2Spawn spawn = _mysteriousBoxSpawns.get(npcId);
		if (spawn != null)
		{
			_allMobs.add(spawn.doSpawn(false));
			spawn.setRespawnState(false);
		}
	}
	
	public void spawnMonster(int npcId)
	{
		if (!isAttackTime())
			return;
		
		List<L2Spawn> monsterList;
		if (Rnd.nextBoolean())
			monsterList = _physicalMonsters.get(npcId);
		else
			monsterList = _magicalMonsters.get(npcId);
		
		if (monsterList != null)
		{
			final List<SepulcherMonster> mobs = new ArrayList<>();
			
			boolean spawnKeyBoxMob = false;
			boolean spawnedKeyBoxMob = false;
			
			for (L2Spawn spawn : monsterList)
			{
				if (spawnedKeyBoxMob)
					spawnKeyBoxMob = false;
				else
				{
					switch (npcId)
					{
						case 31469:
						case 31474:
						case 31479:
						case 31484:
							if (Rnd.get(48) == 0)
								spawnKeyBoxMob = true;
							break;
						
						default:
							spawnKeyBoxMob = false;
					}
				}
				
				SepulcherMonster mob = null;
				
				if (spawnKeyBoxMob)
				{
					try
					{
						NpcTemplate template = NpcData.getInstance().getTemplate(18149);
						if (template != null)
						{
							L2Spawn keyBoxMobSpawn = new L2Spawn(template);
							keyBoxMobSpawn.setLoc(spawn.getLoc());
							keyBoxMobSpawn.setRespawnDelay(3600);
							SpawnTable.getInstance().addNewSpawn(keyBoxMobSpawn, false);
							mob = (SepulcherMonster) keyBoxMobSpawn.doSpawn(false);
							keyBoxMobSpawn.setRespawnState(false);
						}
						else
							_log.warning("FourSepulchersManager.SpawnMonster: Data missing in NPC table for ID: 18149");
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING, "FourSepulchersManager.SpawnMonster: Spawn could not be initialized: " + e.getMessage(), e);
					}
					
					spawnedKeyBoxMob = true;
				}
				else
				{
					mob = (SepulcherMonster) spawn.doSpawn(false);
					spawn.setRespawnState(false);
				}
				
				if (mob != null)
				{
					mob.mysteriousBoxId = npcId;
					switch (npcId)
					{
						case 31469:
						case 31474:
						case 31479:
						case 31484:
						case 31472:
						case 31477:
						case 31482:
						case 31487:
							mobs.add(mob);
					}
					_allMobs.add(mob);
				}
			}
			
			switch (npcId)
			{
				case 31469:
				case 31474:
				case 31479:
				case 31484:
					_viscountMobs.put(npcId, mobs);
					break;
				
				case 31472:
				case 31477:
				case 31482:
				case 31487:
					_dukeMobs.put(npcId, mobs);
					break;
			}
		}
	}
	
	public synchronized boolean isViscountMobsAnnihilated(int npcId)
	{
		List<SepulcherMonster> mobs = _viscountMobs.get(npcId);
		if (mobs == null)
			return true;
		
		for (SepulcherMonster mob : mobs)
		{
			if (!mob.isDead())
				return false;
		}
		
		return true;
	}
	
	public synchronized boolean isDukeMobsAnnihilated(int npcId)
	{
		List<SepulcherMonster> mobs = _dukeMobs.get(npcId);
		if (mobs == null)
			return true;
		
		for (SepulcherMonster mob : mobs)
		{
			if (!mob.isDead())
				return false;
		}
		
		return true;
	}
	
	public void spawnKeyBox(Npc activeChar)
	{
		if (!isAttackTime())
			return;
		
		final L2Spawn spawn = _keyBoxSpawns.get(activeChar.getNpcId());
		if (spawn != null)
		{
			spawn.setLoc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading());
			spawn.setRespawnDelay(3600);
			
			_allMobs.add(spawn.doSpawn(false));
			spawn.setRespawnState(false);
		}
	}
	
	public void spawnExecutionerOfHalisha(Npc activeChar)
	{
		if (!isAttackTime())
			return;
		
		final L2Spawn spawn = _executionerSpawns.get(activeChar.getNpcId());
		if (spawn != null)
		{
			spawn.setLoc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading());
			spawn.setRespawnDelay(3600);
			
			_allMobs.add(spawn.doSpawn(false));
			spawn.setRespawnState(false);
		}
	}
	
	public void spawnArchonOfHalisha(int npcId)
	{
		if (!isAttackTime())
			return;
		
		if (_archonSpawned.get(npcId))
			return;
		
		List<L2Spawn> monsterList = _dukeFinalMobs.get(npcId);
		if (monsterList != null)
		{
			for (L2Spawn spawn : monsterList)
			{
				SepulcherMonster mob = (SepulcherMonster) spawn.doSpawn(false);
				spawn.setRespawnState(false);
				
				if (mob != null)
				{
					mob.mysteriousBoxId = npcId;
					_allMobs.add(mob);
				}
			}
			_archonSpawned.put(npcId, true);
		}
	}
	
	public void spawnEmperorsGraveNpc(int npcId)
	{
		if (!isAttackTime())
			return;
		
		List<L2Spawn> monsterList = _emperorsGraveNpcs.get(npcId);
		if (monsterList != null)
		{
			for (L2Spawn spawn : monsterList)
			{
				_allMobs.add(spawn.doSpawn(false));
				spawn.setRespawnState(false);
			}
		}
	}
	
	protected void locationShadowSpawns()
	{
		int locNo = Rnd.get(4);
		
		final int[] gateKeeper =
		{
			31929,
			31934,
			31939,
			31944
		};
		
		for (int i = 0; i <= 3; i++)
		{
			int keyNpcId = gateKeeper[i];
			L2Spawn spawn = _shadowSpawns.get(keyNpcId);
			spawn.setLoc(_shadowSpawnLoc[locNo][i][1], _shadowSpawnLoc[locNo][i][2], _shadowSpawnLoc[locNo][i][3], _shadowSpawnLoc[locNo][i][4]);
			
			_shadowSpawns.put(keyNpcId, spawn);
		}
	}
	
	public void spawnShadow(int npcId)
	{
		if (!isAttackTime())
			return;
		
		final L2Spawn spawn = _shadowSpawns.get(npcId);
		if (spawn != null)
		{
			SepulcherMonster mob = (SepulcherMonster) spawn.doSpawn(false);
			spawn.setRespawnState(false);
			
			if (mob != null)
			{
				mob.mysteriousBoxId = npcId;
				_allMobs.add(mob);
			}
		}
	}
	
	public void deleteAllMobs()
	{
		for (Npc mob : _allMobs)
		{
			if (mob == null)
				continue;
			
			try
			{
				if (mob.getSpawn() != null)
					mob.getSpawn().setRespawnState(false);
				
				mob.deleteMe();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "FourSepulchersManager: Failed deleting mob.", e);
			}
		}
		_allMobs.clear();
	}
	
	protected void closeAllDoors()
	{
		for (int doorId : _hallGateKeepers.values())
		{
			try
			{
				Door door = DoorData.getInstance().getDoor(doorId);
				if (door != null)
					door.closeMe();
				else
					_log.warning("FourSepulchersManager: Attempted to close undefined door. doorId: " + doorId);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "FourSepulchersManager: Failed closing door", e);
			}
		}
	}
	
	protected byte minuteSelect(byte min)
	{
		if ((double) min % 5 != 0)// if doesn't divides on 5 fully
		{
			// mad table for selecting proper minutes... maybe there is a better way to do this
			switch (min)
			{
				case 6:
				case 7:
					min = 5;
					break;
				
				case 8:
				case 9:
				case 11:
				case 12:
					min = 10;
					break;
				
				case 13:
				case 14:
				case 16:
				case 17:
					min = 15;
					break;
				
				case 18:
				case 19:
				case 21:
				case 22:
					min = 20;
					break;
				
				case 23:
				case 24:
				case 26:
				case 27:
					min = 25;
					break;
				
				case 28:
				case 29:
				case 31:
				case 32:
					min = 30;
					break;
				
				case 33:
				case 34:
				case 36:
				case 37:
					min = 35;
					break;
				
				case 38:
				case 39:
				case 41:
				case 42:
					min = 40;
					break;
				
				case 43:
				case 44:
				case 46:
				case 47:
					min = 45;
					break;
				
				case 48:
				case 49:
				case 51:
				case 52:
					min = 50;
					break;
				
				case 53:
				case 54:
				case 56:
				case 57:
					min = 55;
					break;
			}
		}
		return min;
	}
	
	public void managerSay(byte min)
	{
		// for attack phase, sending message every 5 minutes
		if (_inAttackTime)
		{
			if (min < 5)
				return; // do not shout when < 5 minutes
				
			min = minuteSelect(min);
			
			String msg = min + " minute(s) have passed.";
			
			if (min == 90)
				msg = "Game over. The teleport will appear momentarily";
			
			for (L2Spawn temp : _managers)
			{
				if (temp == null)
				{
					_log.warning("FourSepulchersManager: managerSay(): manager is null");
					continue;
				}
				
				if (!(temp.getNpc() instanceof SepulcherNpc))
				{
					_log.warning("FourSepulchersManager: managerSay(): manager is not Sepulcher instance");
					continue;
				}
				
				// hall not used right now, so its manager will not tell you anything :)
				if (!_hallInUse.get(temp.getNpcId()).booleanValue())
					continue;
				
				((SepulcherNpc) temp.getNpc()).sayInShout(msg);
			}
		}
		
		else if (_inEntryTime)
		{
			String msg1 = "You may now enter the Sepulcher.";
			String msg2 = "If you place your hand on the stone statue in front of each sepulcher, you will be able to enter.";
			for (L2Spawn temp : _managers)
			{
				if (temp == null)
				{
					_log.warning("FourSepulchersManager: Something goes wrong in managerSay()...");
					continue;
				}
				
				if (!(temp.getNpc() instanceof SepulcherNpc))
				{
					_log.warning("FourSepulchersManager: Something goes wrong in managerSay()...");
					continue;
				}
				
				((SepulcherNpc) temp.getNpc()).sayInShout(msg1);
				((SepulcherNpc) temp.getNpc()).sayInShout(msg2);
			}
		}
	}
	
	protected class ManagerSay implements Runnable
	{
		@Override
		public void run()
		{
			if (_inAttackTime)
			{
				Calendar tmp = Calendar.getInstance();
				tmp.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - _warmUpTimeEnd);
				
				if (tmp.get(Calendar.MINUTE) + 5 < Config.FS_TIME_ATTACK)
				{
					// byte because minute cannot be more than 59
					managerSay((byte) tmp.get(Calendar.MINUTE));
					ThreadPool.schedule(new ManagerSay(), 5 * 60000);
				}
				// attack time ending chat
				else if (tmp.get(Calendar.MINUTE) + 5 >= Config.FS_TIME_ATTACK)
					managerSay((byte) 90); // sending a unique id :D
			}
			else if (_inEntryTime)
				managerSay((byte) 0);
		}
	}
	
	protected class ChangeEntryTime implements Runnable
	{
		@Override
		public void run()
		{
			// _log.info("FourSepulchersManager:In Entry Time");
			_inEntryTime = true;
			_inAttackTime = false;
			
			long interval = 0;
			// if this is first launch - search time when entry time will be ended:
			// counting difference between time when entry time ends and current time
			// and then launching change time task
			if (_firstTimeRun)
				interval = _entryTimeEnd - Calendar.getInstance().getTimeInMillis();
			else
				interval = Config.FS_TIME_ENTRY * 60000l; // else use stupid method
				
			// launching saying process...
			ThreadPool.schedule(new ManagerSay(), 0);
			_changeWarmUpTimeTask = ThreadPool.schedule(new ChangeWarmUpTime(), interval);
			
			if (_changeEntryTimeTask != null)
			{
				_changeEntryTimeTask.cancel(true);
				_changeEntryTimeTask = null;
			}
		}
	}
	
	protected class ChangeWarmUpTime implements Runnable
	{
		@Override
		public void run()
		{
			// _log.info("FourSepulchersManager:In Warm-Up Time");
			_inEntryTime = true;
			_inAttackTime = false;
			
			long interval = 0;
			// searching time when warmup time will be ended:
			// counting difference between time when warmup time ends and current time
			// and then launching change time task
			if (_firstTimeRun)
				interval = _warmUpTimeEnd - Calendar.getInstance().getTimeInMillis();
			else
				interval = Config.FS_TIME_WARMUP * 60000l;
			
			_changeAttackTimeTask = ThreadPool.schedule(new ChangeAttackTime(), interval);
			
			if (_changeWarmUpTimeTask != null)
			{
				_changeWarmUpTimeTask.cancel(true);
				_changeWarmUpTimeTask = null;
			}
		}
	}
	
	protected class ChangeAttackTime implements Runnable
	{
		@Override
		public void run()
		{
			// _log.info("FourSepulchersManager:In Attack Time");
			_inEntryTime = false;
			_inAttackTime = true;
			
			locationShadowSpawns();
			
			spawnMysteriousBox(31921);
			spawnMysteriousBox(31922);
			spawnMysteriousBox(31923);
			spawnMysteriousBox(31924);
			
			if (!_firstTimeRun)
				_warmUpTimeEnd = Calendar.getInstance().getTimeInMillis();
			
			long interval = 0;
			// say task
			if (_firstTimeRun)
			{
				for (double min = Calendar.getInstance().get(Calendar.MINUTE); min < _newCycleMin; min++)
				{
					// looking for next shout time....
					if (min % 5 == 0)// check if min can be divided by 5
					{
						_log.info("FourSepulchersManager: attack time announced.");
						Calendar inter = Calendar.getInstance();
						inter.set(Calendar.MINUTE, (int) min);
						ThreadPool.schedule(new ManagerSay(), inter.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
						break;
					}
				}
			}
			else
				ThreadPool.schedule(new ManagerSay(), 5 * 60400);
				
			// searching time when attack time will be ended:
			// counting difference between time when attack time ends and current time
			// and then launching change time task
			if (_firstTimeRun)
				interval = _attackTimeEnd - Calendar.getInstance().getTimeInMillis();
			else
				interval = Config.FS_TIME_ATTACK * 60000l;
			
			_changeCoolDownTimeTask = ThreadPool.schedule(new ChangeCoolDownTime(), interval);
			
			if (_changeAttackTimeTask != null)
			{
				_changeAttackTimeTask.cancel(true);
				_changeAttackTimeTask = null;
			}
		}
	}
	
	protected class ChangeCoolDownTime implements Runnable
	{
		@Override
		public void run()
		{
			// _log.info("FourSepulchersManager:In Cool-Down Time");
			_inEntryTime = false;
			_inAttackTime = false;
			
			clean();
			
			Calendar time = Calendar.getInstance();
			// one hour = 55th min to 55 min of next hour, so we check for this, also check for first launch
			if (Calendar.getInstance().get(Calendar.MINUTE) > _newCycleMin && !_firstTimeRun)
				time.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) + 1);
			
			time.set(Calendar.MINUTE, _newCycleMin);
			_log.info("FourSepulchersManager: end of the round.");
			
			if (_firstTimeRun)
				_firstTimeRun = false; // cooldown phase ends event hour, so it will be not first run
				
			long interval = time.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
			_changeEntryTimeTask = ThreadPool.schedule(new ChangeEntryTime(), interval);
			
			if (_changeCoolDownTimeTask != null)
			{
				_changeCoolDownTimeTask.cancel(true);
				_changeCoolDownTimeTask = null;
			}
		}
	}
	
	public Map<Integer, Integer> getHallGateKeepers()
	{
		return _hallGateKeepers;
	}
	
	public void showHtmlFile(Player player, String file, Npc npc, Player member)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/sepulchers/" + file);
		if (member != null)
			html.replace("%member%", member.getName());
		
		player.sendPacket(html);
	}
	
	private static class SingletonHolder
	{
		protected static final FourSepulchersManager _instance = new FourSepulchersManager();
	}
}