package net.sf.l2j.gameserver.data.manager;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.HistoryInfo;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.zone.type.DerbyTrackZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.network.serverpackets.MonRaceInfo;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

public class DerbyTrackManager
{
	protected static final Logger LOG = Logger.getLogger(DerbyTrackManager.class.getName());
	
	private static final String SAVE_HISTORY = "INSERT INTO mdt_history (race_id, first, second, odd_rate) VALUES (?,?,?,?)";
	private static final String LOAD_HISTORY = "SELECT * FROM mdt_history";
	private static final String LOAD_BETS = "SELECT * FROM mdt_bets";
	private static final String SAVE_BETS = "REPLACE INTO mdt_bets (lane_id, bet) VALUES (?,?)";
	private static final String CLEAR_BETS = "UPDATE mdt_bets SET bet = 0";
	
	public static enum RaceState
	{
		ACCEPTING_BETS,
		WAITING,
		STARTING_RACE,
		RACE_END
	}
	
	protected static final PlaySound SOUND_1 = new PlaySound(1, "S_Race");
	protected static final PlaySound SOUND_2 = new PlaySound("ItemSound2.race_start");
	
	protected static final int[][] CODES =
	{
		{
			-1,
			0
		},
		{
			0,
			15322
		},
		{
			13765,
			-1
		}
	};
	
	protected final List<Integer> _npcTemplates = new ArrayList<>(); // List holding npc templates, shuffled on a new race.
	protected final List<HistoryInfo> _history = new ArrayList<>(); // List holding old race records.
	protected final Map<Integer, Long> _betsPerLane = new ConcurrentHashMap<>(); // Map holding all bets for each lane ; values setted to 0 after every race.
	protected final List<Double> _odds = new ArrayList<>(); // List holding sorted odds per lane ; cleared at new odds calculation.
	
	protected int _raceNumber = 1;
	protected int _finalCountdown = 0;
	protected RaceState _state = RaceState.RACE_END;
	
	protected MonRaceInfo _packet;
	
	private final Npc[] _monsters;
	private Constructor<?> _constructor;
	private int[][] _speeds;
	private final int[] _first, _second;
	
	protected DerbyTrackManager()
	{
		// Feed _history with previous race results.
		loadHistory();
		
		// Feed _betsPerLane with stored informations on bets.
		loadBets();
		
		// Feed _npcTemplates, we will only have to shuffle it when needed.
		for (int i = 31003; i < 31027; i++)
			_npcTemplates.add(i);
		
		_monsters = new Npc[8];
		_speeds = new int[8][20];
		_first = new int[2];
		_second = new int[2];
		
		ThreadPool.scheduleAtFixedRate(new Announcement(), 0, 1000);
	}
	
	public Npc[] getMonsters()
	{
		return _monsters;
	}
	
	public int[][] getSpeeds()
	{
		return _speeds;
	}
	
	public int getFirstPlace()
	{
		return _first[0];
	}
	
	public int getSecondPlace()
	{
		return _second[0];
	}
	
	public MonRaceInfo getRacePacket()
	{
		return _packet;
	}
	
	public RaceState getCurrentRaceState()
	{
		return _state;
	}
	
	public int getRaceNumber()
	{
		return _raceNumber;
	}
	
	public List<HistoryInfo> getHistory()
	{
		return _history;
	}
	
	public List<Double> getOdds()
	{
		return _odds;
	}
	
	public void newRace()
	{
		// Edit _history.
		_history.add(new HistoryInfo(_raceNumber, 0, 0, 0));
		
		// Randomize _npcTemplates.
		Collections.shuffle(_npcTemplates);
		
		// Setup 8 new creatures ; pickup the first 8 from _npcTemplates.
		for (int i = 0; i < 8; i++)
		{
			try
			{
				NpcTemplate template = NpcData.getInstance().getTemplate(_npcTemplates.get(i));
				
				_constructor = Class.forName("net.sf.l2j.gameserver.model.actor.instance." + template.getType()).getConstructors()[0];
				_monsters[i] = (Npc) _constructor.newInstance(IdFactory.getInstance().getNextId(), template);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, "Failed generating DerbyRace monster.", e);
			}
		}
	}
	
	public void newSpeeds()
	{
		_speeds = new int[8][20];
		int total = 0;
		_first[1] = 0;
		_second[1] = 0;
		
		for (int i = 0; i < 8; i++)
		{
			total = 0;
			for (int j = 0; j < 20; j++)
			{
				if (j == 19)
					_speeds[i][j] = 100;
				else
					_speeds[i][j] = Rnd.get(60) + 65;
				total += _speeds[i][j];
			}
			
			if (total >= _first[1])
			{
				_second[0] = _first[0];
				_second[1] = _first[1];
				_first[0] = 8 - i;
				_first[1] = total;
			}
			else if (total >= _second[1])
			{
				_second[0] = 8 - i;
				_second[1] = total;
			}
		}
	}
	
	/**
	 * Load past races informations, feeding _history arrayList.<br>
	 * Also sets _raceNumber, based on latest HistoryInfo loaded.
	 */
	protected void loadHistory()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_HISTORY);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				_history.add(new HistoryInfo(rs.getInt("race_id"), rs.getInt("first"), rs.getInt("second"), rs.getDouble("odd_rate")));
				_raceNumber++;
			}
		}
		catch (SQLException e)
		{
			LOG.log(Level.SEVERE, "Can't load Derby Track history.", e);
		}
		LOG.info("Loaded " + _history.size() + " Derby Track records, currently on race #" + _raceNumber);
	}
	
	/**
	 * Save an {@link HistoryInfo} record into database.
	 * @param history The HistoryInfo to store.
	 */
	protected void saveHistory(HistoryInfo history)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SAVE_HISTORY))
		{
			ps.setInt(1, history.getRaceId());
			ps.setInt(2, history.getFirst());
			ps.setInt(3, history.getSecond());
			ps.setDouble(4, history.getOddRate());
			ps.execute();
		}
		catch (SQLException e)
		{
			LOG.log(Level.SEVERE, "Can't save Derby Track history.", e);
		}
	}
	
	/**
	 * Load current bets per lane ; initialize the map keys.
	 */
	protected void loadBets()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_BETS);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
				setBetOnLane(rs.getInt("lane_id"), rs.getLong("bet"), false);
		}
		catch (SQLException e)
		{
			LOG.log(Level.SEVERE, "Can't load Derby Track bets.", e);
		}
	}
	
	/**
	 * Save the current lane bet into database.
	 * @param lane : The lane to affect.
	 * @param sum : The sum to set.
	 */
	protected void saveBet(int lane, long sum)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SAVE_BETS))
		{
			ps.setInt(1, lane);
			ps.setLong(2, sum);
			ps.execute();
		}
		catch (SQLException e)
		{
			LOG.log(Level.SEVERE, "Can't save Derby Track bet.", e);
		}
	}
	
	/**
	 * Clear all lanes bets, either on database or Map.
	 */
	protected void clearBets()
	{
		for (int key : _betsPerLane.keySet())
			_betsPerLane.put(key, 0L);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(CLEAR_BETS))
		{
			ps.execute();
		}
		catch (SQLException e)
		{
			LOG.log(Level.SEVERE, "Can't clear Derby Track bets.", e);
		}
	}
	
	/**
	 * Setup lane bet, based on previous value (if any).
	 * @param lane : The lane to edit.
	 * @param amount : The amount to add.
	 * @param saveOnDb : Should it be saved on db or not.
	 */
	public void setBetOnLane(int lane, long amount, boolean saveOnDb)
	{
		final long sum = _betsPerLane.getOrDefault(lane, 0L) + amount;
		
		_betsPerLane.put(lane, sum);
		
		if (saveOnDb)
			saveBet(lane, sum);
	}
	
	/**
	 * Calculate odds for every lane, based on others lanes.
	 */
	protected void calculateOdds()
	{
		// Clear previous List holding old odds.
		_odds.clear();
		
		// Sort bets lanes per lane.
		final Map<Integer, Long> sortedLanes = new TreeMap<>(_betsPerLane);
		
		// Pass a first loop in order to calculate total sum of all lanes.
		long sumOfAllLanes = 0;
		for (long amount : sortedLanes.values())
			sumOfAllLanes += amount;
		
		// As we get the sum, we can now calculate the odd rate of each lane.
		for (long amount : sortedLanes.values())
			_odds.add((amount == 0) ? 0D : Math.max(1.25, sumOfAllLanes * 0.7 / amount));
	}
	
	private class Announcement implements Runnable
	{
		public Announcement()
		{
		}
		
		@Override
		public void run()
		{
			if (_finalCountdown > 1200)
				_finalCountdown = 0;
			
			switch (_finalCountdown)
			{
				case 0:
					newRace();
					newSpeeds();
					
					_state = RaceState.ACCEPTING_BETS;
					_packet = new MonRaceInfo(CODES[0][0], CODES[0][1], getMonsters(), getSpeeds());
					
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, _packet, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber));
					break;
				
				case 30: // 30 sec
				case 60: // 1 min
				case 90: // 1 min 30 sec
				case 120: // 2 min
				case 150: // 2 min 30
				case 180: // 3 min
				case 210: // 3 min 30
				case 240: // 4 min
				case 270: // 4 min 30 sec
				case 330: // 5 min 30 sec
				case 360: // 6 min
				case 390: // 6 min 30 sec
				case 420: // 7 min
				case 450: // 7 min 30
				case 480: // 8 min
				case 510: // 8 min 30
				case 540: // 9 min
				case 570: // 9 min 30 sec
				case 630: // 10 min 30 sec
				case 660: // 11 min
				case 690: // 11 min 30 sec
				case 720: // 12 min
				case 750: // 12 min 30
				case 780: // 13 min
				case 810: // 13 min 30
				case 870: // 14 min 30 sec
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber));
					break;
				
				case 300: // 5 min
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(10));
					break;
				
				case 600: // 10 min
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(5));
					break;
				
				case 840: // 14 min
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(1));
					break;
				
				case 900: // 15 min
					_state = RaceState.WAITING;
					
					calculateOdds();
					
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_TICKET_SALES_CLOSED));
					break;
				
				case 960: // 16 min
				case 1020: // 17 min
					final int minutes = (_finalCountdown == 960) ? 2 : 1;
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S2_BEGINS_IN_S1_MINUTES).addNumber(minutes));
					break;
				
				case 1050: // 17 min 30 sec
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_BEGINS_IN_30_SECONDS));
					break;
				
				case 1070: // 17 min 50 sec
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_COUNTDOWN_IN_FIVE_SECONDS));
					break;
				
				case 1075: // 17 min 55 sec
				case 1076: // 17 min 56 sec
				case 1077: // 17 min 57 sec
				case 1078: // 17 min 58 sec
				case 1079: // 17 min 59 sec
					final int seconds = 1080 - _finalCountdown;
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS).addNumber(seconds));
					break;
				
				case 1080: // 18 min
					_state = RaceState.STARTING_RACE;
					_packet = new MonRaceInfo(CODES[1][0], CODES[1][1], getMonsters(), getSpeeds());
					
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_RACE_START), SOUND_1, SOUND_2, _packet);
					break;
				
				case 1085: // 18 min 5 sec
					_packet = new MonRaceInfo(CODES[2][0], CODES[2][1], getMonsters(), getSpeeds());
					
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, _packet);
					break;
				
				case 1115: // 18 min 35 sec
					_state = RaceState.RACE_END;
					
					// Populate history info with data, stores it in database.
					final HistoryInfo info = _history.get(_history.size() - 1);
					info.setFirst(getFirstPlace());
					info.setSecond(getSecondPlace());
					info.setOddRate(_odds.get(getFirstPlace() - 1));
					
					saveHistory(info);
					clearBets();
					
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_FIRST_PLACE_S1_SECOND_S2).addNumber(getFirstPlace()).addNumber(getSecondPlace()), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_RACE_END).addNumber(_raceNumber));
					_raceNumber++;
					break;
				
				case 1140: // 19 min
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, new DeleteObject(getMonsters()[0]), new DeleteObject(getMonsters()[1]), new DeleteObject(getMonsters()[2]), new DeleteObject(getMonsters()[3]), new DeleteObject(getMonsters()[4]), new DeleteObject(getMonsters()[5]), new DeleteObject(getMonsters()[6]), new DeleteObject(getMonsters()[7]));
					break;
			}
			_finalCountdown += 1;
		}
	}
	
	public static DerbyTrackManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DerbyTrackManager INSTANCE = new DerbyTrackManager();
	}
}