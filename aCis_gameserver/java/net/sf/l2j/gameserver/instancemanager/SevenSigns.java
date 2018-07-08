package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.instancemanager.AutoSpawnManager.AutoSpawnInstance;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SSQInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Broadcast;

public class SevenSigns
{
	public enum CabalType
	{
		NORMAL("No Cabal", "No Cabal"),
		DUSK("dusk", "Revolutionaries of Dusk"),
		DAWN("dawn", "Lords of Dawn");
		
		private final String _shortName;
		private final String _fullName;
		
		private CabalType(String shortName, String fullName)
		{
			_shortName = shortName;
			_fullName = fullName;
		}
		
		public String getShortName()
		{
			return _shortName;
		}
		
		public String getFullName()
		{
			return _fullName;
		}
		
		public static final CabalType[] VALUES = values();
	}
	
	public enum SealType
	{
		NONE("", ""),
		AVARICE("Avarice", "Seal of Avarice"),
		GNOSIS("Gnosis", "Seal of Gnosis"),
		STRIFE("Strife", "Seal of Strife");
		
		private final String _shortName;
		private final String _fullName;
		
		private SealType(String shortName, String fullName)
		{
			_shortName = shortName;
			_fullName = fullName;
		}
		
		public String getShortName()
		{
			return _shortName;
		}
		
		public String getFullName()
		{
			return _fullName;
		}
		
		public static final SealType[] VALUES = values();
	}
	
	public enum PeriodType
	{
		RECRUITING("Quest Event Initialization", SystemMessageId.PREPARATIONS_PERIOD_BEGUN),
		COMPETITION("Competition (Quest Event)", SystemMessageId.COMPETITION_PERIOD_BEGUN),
		RESULTS("Quest Event Results", SystemMessageId.RESULTS_PERIOD_BEGUN),
		SEAL_VALIDATION("Seal Validation", SystemMessageId.VALIDATION_PERIOD_BEGUN);
		
		private final String _name;
		private final SystemMessageId _smId;
		
		private PeriodType(String name, SystemMessageId smId)
		{
			_name = name;
			_smId = smId;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public SystemMessageId getMessageId()
		{
			return _smId;
		}
		
		public static final PeriodType[] VALUES = values();
	}
	
	protected static final Logger _log = Logger.getLogger(SevenSigns.class.getName());
	
	// SQL queries
	private static final String LOAD_DATA = "SELECT char_obj_id, cabal, seal, red_stones, green_stones, blue_stones, ancient_adena_amount, contribution_score FROM seven_signs";
	private static final String LOAD_STATUS = "SELECT * FROM seven_signs_status WHERE id=0";
	private static final String INSERT_PLAYER = "INSERT INTO seven_signs (char_obj_id, cabal, seal) VALUES (?,?,?)";
	private static final String UPDATE_PLAYER = "UPDATE seven_signs SET cabal=?, seal=?, red_stones=?, green_stones=?, blue_stones=?, ancient_adena_amount=?, contribution_score=? WHERE char_obj_id=?";
	private static final String UPDATE_STATUS = "UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, " + "dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, " + "avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, " + "strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, " + "festival_cycle=?, accumulated_bonus0=?, accumulated_bonus1=?, accumulated_bonus2=?," + "accumulated_bonus3=?, accumulated_bonus4=?, date=? WHERE id=0";
	
	// Seven Signs constants
	public static final String SEVEN_SIGNS_DATA_FILE = "config/signs.properties";
	public static final String SEVEN_SIGNS_HTML_PATH = "data/html/seven_signs/";
	
	public static final int PERIOD_START_HOUR = 18;
	public static final int PERIOD_START_MINS = 00;
	public static final int PERIOD_START_DAY = Calendar.MONDAY;
	
	// The quest event and seal validation periods last for approximately one week with a 15 minutes "interval" period sandwiched between them.
	public static final int PERIOD_MINOR_LENGTH = 900000;
	public static final int PERIOD_MAJOR_LENGTH = 604800000 - PERIOD_MINOR_LENGTH;
	
	public static final int RECORD_SEVEN_SIGNS_ID = 5707;
	public static final int CERTIFICATE_OF_APPROVAL_ID = 6388;
	public static final int RECORD_SEVEN_SIGNS_COST = 500;
	public static final int ADENA_JOIN_DAWN_COST = 50000;
	
	// NPCs related constants
	public static final int ORATOR_NPC_ID = 31094;
	public static final int PREACHER_NPC_ID = 31093;
	public static final int MAMMON_MERCHANT_ID = 31113;
	public static final int MAMMON_BLACKSMITH_ID = 31126;
	public static final int MAMMON_MARKETEER_ID = 31092;
	public static final int LILITH_NPC_ID = 25283;
	public static final int ANAKIM_NPC_ID = 25286;
	public static final int CREST_OF_DAWN_ID = 31170;
	public static final int CREST_OF_DUSK_ID = 31171;
	
	// Seal Stone related constants
	public static final int SEAL_STONE_BLUE_ID = 6360;
	public static final int SEAL_STONE_GREEN_ID = 6361;
	public static final int SEAL_STONE_RED_ID = 6362;
	
	public static final int SEAL_STONE_BLUE_VALUE = 3;
	public static final int SEAL_STONE_GREEN_VALUE = 5;
	public static final int SEAL_STONE_RED_VALUE = 10;
	
	private final Calendar _nextPeriodChange = Calendar.getInstance();
	private Calendar _lastSave = Calendar.getInstance();
	
	protected PeriodType _activePeriod;
	protected int _currentCycle;
	protected double _dawnStoneScore;
	protected double _duskStoneScore;
	protected int _dawnFestivalScore;
	protected int _duskFestivalScore;
	protected CabalType _previousWinner;
	
	private final Map<Integer, StatsSet> _playersData = new HashMap<>();
	
	private final Map<SealType, CabalType> _sealOwners = new HashMap<>();
	private final Map<SealType, Integer> _duskScores = new HashMap<>();
	private final Map<SealType, Integer> _dawnScores = new HashMap<>();
	
	// AutoSpawn instances
	private static AutoSpawnInstance _merchantSpawn;
	private static AutoSpawnInstance _blacksmithSpawn;
	private static AutoSpawnInstance _lilithSpawn;
	private static AutoSpawnInstance _anakimSpawn;
	private static Map<Integer, AutoSpawnInstance> _crestofdawnspawns;
	private static Map<Integer, AutoSpawnInstance> _crestofduskspawns;
	private static Map<Integer, AutoSpawnInstance> _oratorSpawns;
	private static Map<Integer, AutoSpawnInstance> _preacherSpawns;
	private static Map<Integer, AutoSpawnInstance> _marketeerSpawns;
	
	protected SevenSigns()
	{
		restoreSevenSignsData();
		
		_log.info("SevenSigns: Currently on " + _activePeriod.getName() + " period.");
		initializeSeals();
		
		final CabalType winningCabal = getCabalHighestScore();
		if (isSealValidationPeriod())
		{
			if (winningCabal == CabalType.NORMAL)
				_log.info("SevenSigns: The competition ended with a tie last week.");
			else
				_log.info("SevenSigns: " + winningCabal.getFullName() + " were victorious last week.");
		}
		else if (winningCabal == CabalType.NORMAL)
			_log.info("SevenSigns: The competition will end in a tie this week.");
		else
			_log.info("SevenSigns: " + winningCabal.getFullName() + " are leading this week.");
		
		long milliToChange = 0;
		if (isNextPeriodChangeInPast())
			_log.info("SevenSigns: Next period change was in the past, changing periods now.");
		else
		{
			setCalendarForNextPeriodChange();
			milliToChange = getMilliToPeriodChange();
		}
		
		// Schedule a time for the next period change.
		ThreadPool.schedule(new SevenSignsPeriodChange(), milliToChange);
		
		double numSecs = (milliToChange / 1000) % 60;
		double countDown = ((milliToChange / 1000) - numSecs) / 60;
		int numMins = (int) Math.floor(countDown % 60);
		countDown = (countDown - numMins) / 60;
		int numHours = (int) Math.floor(countDown % 24);
		int numDays = (int) Math.floor((countDown - numHours) / 24);
		
		_log.info("SevenSigns: Next period begins in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
	}
	
	private boolean isNextPeriodChangeInPast()
	{
		Calendar lastPeriodChange = Calendar.getInstance();
		switch (_activePeriod)
		{
			case SEAL_VALIDATION:
			case COMPETITION:
				lastPeriodChange.set(Calendar.DAY_OF_WEEK, PERIOD_START_DAY);
				lastPeriodChange.set(Calendar.HOUR_OF_DAY, PERIOD_START_HOUR);
				lastPeriodChange.set(Calendar.MINUTE, PERIOD_START_MINS);
				lastPeriodChange.set(Calendar.SECOND, 0);
				// If we hit next week, just turn back 1 week
				if (Calendar.getInstance().before(lastPeriodChange))
					lastPeriodChange.add(Calendar.HOUR, -24 * 7);
				break;
			
			case RECRUITING:
			case RESULTS:
				// Because of the short duration of this period, just check it from last save
				lastPeriodChange.setTimeInMillis(_lastSave.getTimeInMillis() + PERIOD_MINOR_LENGTH);
				break;
		}
		
		// Because of previous "date" column usage, check only if it already contains usable data for us
		if (_lastSave.getTimeInMillis() > 7 && _lastSave.before(lastPeriodChange))
			return true;
		
		return false;
	}
	
	/**
	 * Registers all random spawns and auto-chats for Seven Signs NPCs, along with spawns for the Preachers of Doom and Orators of Revelations at the beginning of the Seal Validation period.
	 */
	public void spawnSevenSignsNPC()
	{
		_merchantSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(MAMMON_MERCHANT_ID, false);
		_blacksmithSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(MAMMON_BLACKSMITH_ID, false);
		_marketeerSpawns = AutoSpawnManager.getInstance().getAutoSpawnInstances(MAMMON_MARKETEER_ID);
		_lilithSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(LILITH_NPC_ID, false);
		_anakimSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(ANAKIM_NPC_ID, false);
		_crestofdawnspawns = AutoSpawnManager.getInstance().getAutoSpawnInstances(CREST_OF_DAWN_ID);
		_crestofduskspawns = AutoSpawnManager.getInstance().getAutoSpawnInstances(CREST_OF_DUSK_ID);
		_oratorSpawns = AutoSpawnManager.getInstance().getAutoSpawnInstances(ORATOR_NPC_ID);
		_preacherSpawns = AutoSpawnManager.getInstance().getAutoSpawnInstances(PREACHER_NPC_ID);
		
		if (isSealValidationPeriod() || isCompResultsPeriod())
		{
			for (AutoSpawnInstance spawnInst : _marketeerSpawns.values())
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
			
			final CabalType winningCabal = getCabalHighestScore();
			
			final CabalType gnosisSealOwner = getSealOwner(SealType.GNOSIS);
			if (gnosisSealOwner == winningCabal && gnosisSealOwner != CabalType.NORMAL)
			{
				if (!Config.ANNOUNCE_MAMMON_SPAWN)
					_blacksmithSpawn.setBroadcast(false);
				
				if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_blacksmithSpawn.getObjectId(), true).isSpawnActive())
					AutoSpawnManager.getInstance().setSpawnActive(_blacksmithSpawn, true);
				
				for (AutoSpawnInstance spawnInst : _oratorSpawns.values())
					if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
						AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
					
				for (AutoSpawnInstance spawnInst : _preacherSpawns.values())
					if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
						AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
			}
			else
			{
				AutoSpawnManager.getInstance().setSpawnActive(_blacksmithSpawn, false);
				
				for (AutoSpawnInstance spawnInst : _oratorSpawns.values())
					AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
				
				for (AutoSpawnInstance spawnInst : _preacherSpawns.values())
					AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
			}
			
			final CabalType avariceSealOwner = getSealOwner(SealType.AVARICE);
			if (avariceSealOwner == winningCabal && avariceSealOwner != CabalType.NORMAL)
			{
				if (!Config.ANNOUNCE_MAMMON_SPAWN)
					_merchantSpawn.setBroadcast(false);
				
				if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_merchantSpawn.getObjectId(), true).isSpawnActive())
					AutoSpawnManager.getInstance().setSpawnActive(_merchantSpawn, true);
				
				switch (winningCabal)
				{
					case DAWN:
						// Spawn Lilith, unspawn Anakim.
						if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_lilithSpawn.getObjectId(), true).isSpawnActive())
							AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, true);
						
						AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, false);
						
						// Spawn Dawn crests.
						for (AutoSpawnInstance dawnCrest : _crestofdawnspawns.values())
						{
							if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(dawnCrest.getObjectId(), true).isSpawnActive())
								AutoSpawnManager.getInstance().setSpawnActive(dawnCrest, true);
						}
						
						// Unspawn Dusk crests.
						for (AutoSpawnInstance duskCrest : _crestofduskspawns.values())
							AutoSpawnManager.getInstance().setSpawnActive(duskCrest, false);
						break;
					
					case DUSK:
						// Spawn Anakim, unspawn Lilith.
						if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_anakimSpawn.getObjectId(), true).isSpawnActive())
							AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, true);
						
						AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, false);
						
						// Spawn Dusk crests.
						for (AutoSpawnInstance duskCrest : _crestofduskspawns.values())
						{
							if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(duskCrest.getObjectId(), true).isSpawnActive())
								AutoSpawnManager.getInstance().setSpawnActive(duskCrest, true);
						}
						
						// Unspawn Dawn crests.
						for (AutoSpawnInstance dawnCrest : _crestofdawnspawns.values())
							AutoSpawnManager.getInstance().setSpawnActive(dawnCrest, false);
						break;
				}
			}
			else
			{
				// Unspawn merchant of mammon, Lilith, Anakim.
				AutoSpawnManager.getInstance().setSpawnActive(_merchantSpawn, false);
				AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, false);
				AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, false);
				
				// Unspawn Dawn crests.
				for (AutoSpawnInstance dawnCrest : _crestofdawnspawns.values())
					AutoSpawnManager.getInstance().setSpawnActive(dawnCrest, false);
				
				// Unspawn Dusk crests.
				for (AutoSpawnInstance duskCrest : _crestofduskspawns.values())
					AutoSpawnManager.getInstance().setSpawnActive(duskCrest, false);
			}
		}
		else
		{
			// Unspawn merchant of mammon, Lilith, Anakim.
			AutoSpawnManager.getInstance().setSpawnActive(_merchantSpawn, false);
			AutoSpawnManager.getInstance().setSpawnActive(_blacksmithSpawn, false);
			AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, false);
			AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, false);
			
			// Unspawn Dawn crests.
			for (AutoSpawnInstance dawnCrest : _crestofdawnspawns.values())
				AutoSpawnManager.getInstance().setSpawnActive(dawnCrest, false);
			
			// Unspawn Dusk crests.
			for (AutoSpawnInstance duskCrest : _crestofduskspawns.values())
				AutoSpawnManager.getInstance().setSpawnActive(duskCrest, false);
			
			// Unspawn Orators.
			for (AutoSpawnInstance spawnInst : _oratorSpawns.values())
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
			
			// Unspawn Preachers.
			for (AutoSpawnInstance spawnInst : _preacherSpawns.values())
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
			
			// Unspawn marketeer of mammon.
			for (AutoSpawnInstance spawnInst : _marketeerSpawns.values())
				AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
		}
	}
	
	public static int calcScore(int blueCount, int greenCount, int redCount)
	{
		return blueCount * SEAL_STONE_BLUE_VALUE + greenCount * SEAL_STONE_GREEN_VALUE + redCount * SEAL_STONE_RED_VALUE;
	}
	
	public final int getCurrentCycle()
	{
		return _currentCycle;
	}
	
	public final PeriodType getCurrentPeriod()
	{
		return _activePeriod;
	}
	
	private final int getDaysToPeriodChange()
	{
		int numDays = _nextPeriodChange.get(Calendar.DAY_OF_WEEK) - PERIOD_START_DAY;
		
		if (numDays < 0)
			return 0 - numDays;
		
		return 7 - numDays;
	}
	
	public final long getMilliToPeriodChange()
	{
		long currTimeMillis = System.currentTimeMillis();
		long changeTimeMillis = _nextPeriodChange.getTimeInMillis();
		
		return (changeTimeMillis - currTimeMillis);
	}
	
	/**
	 * Calculate the number of days until the next period.<BR>
	 * A period starts at 18:00 pm (local time), like on official servers.
	 */
	protected void setCalendarForNextPeriodChange()
	{
		switch (_activePeriod)
		{
			case SEAL_VALIDATION:
			case COMPETITION:
				int daysToChange = getDaysToPeriodChange();
				
				if (daysToChange == 7)
					if (_nextPeriodChange.get(Calendar.HOUR_OF_DAY) < PERIOD_START_HOUR)
						daysToChange = 0;
					else if (_nextPeriodChange.get(Calendar.HOUR_OF_DAY) == PERIOD_START_HOUR && _nextPeriodChange.get(Calendar.MINUTE) < PERIOD_START_MINS)
						daysToChange = 0;
					
				if (daysToChange > 0)
					_nextPeriodChange.add(Calendar.DATE, daysToChange);
				
				_nextPeriodChange.set(Calendar.HOUR_OF_DAY, PERIOD_START_HOUR);
				_nextPeriodChange.set(Calendar.MINUTE, PERIOD_START_MINS);
				_nextPeriodChange.set(Calendar.SECOND, 0);
				_nextPeriodChange.set(Calendar.MILLISECOND, 0);
				break;
			
			case RECRUITING:
			case RESULTS:
				_nextPeriodChange.add(Calendar.MILLISECOND, PERIOD_MINOR_LENGTH);
				break;
		}
		_log.info("SevenSigns: Next period change set to " + _nextPeriodChange.getTime());
	}
	
	public final boolean isRecruitingPeriod()
	{
		return _activePeriod == PeriodType.RECRUITING;
	}
	
	public final boolean isSealValidationPeriod()
	{
		return _activePeriod == PeriodType.SEAL_VALIDATION;
	}
	
	public final boolean isCompResultsPeriod()
	{
		return _activePeriod == PeriodType.RESULTS;
	}
	
	public final int getCurrentScore(CabalType cabal)
	{
		double totalStoneScore = _dawnStoneScore + _duskStoneScore;
		
		switch (cabal)
		{
			case DAWN:
				return Math.round((float) (_dawnStoneScore / ((float) totalStoneScore == 0 ? 1 : totalStoneScore)) * 500) + _dawnFestivalScore;
			
			case DUSK:
				return Math.round((float) (_duskStoneScore / ((float) totalStoneScore == 0 ? 1 : totalStoneScore)) * 500) + _duskFestivalScore;
		}
		
		return 0;
	}
	
	public final double getCurrentStoneScore(CabalType cabal)
	{
		switch (cabal)
		{
			case DAWN:
				return _dawnStoneScore;
			
			case DUSK:
				return _duskStoneScore;
		}
		
		return 0;
	}
	
	public final int getCurrentFestivalScore(CabalType cabal)
	{
		switch (cabal)
		{
			case DAWN:
				return _dawnFestivalScore;
			
			case DUSK:
				return _duskFestivalScore;
		}
		
		return 0;
	}
	
	public final CabalType getCabalHighestScore()
	{
		final int duskScore = getCurrentScore(CabalType.DUSK);
		final int dawnScore = getCurrentScore(CabalType.DAWN);
		
		if (duskScore == dawnScore)
			return CabalType.NORMAL;
		
		if (duskScore > dawnScore)
			return CabalType.DUSK;
		
		return CabalType.DAWN;
	}
	
	public final CabalType getSealOwner(SealType seal)
	{
		return _sealOwners.get(seal);
	}
	
	public final Map<SealType, CabalType> getSealOwners()
	{
		return _sealOwners;
	}
	
	public final int getSealProportion(SealType seal, CabalType cabal)
	{
		switch (cabal)
		{
			case DAWN:
				return _dawnScores.get(seal);
			
			case DUSK:
				return _duskScores.get(seal);
		}
		
		return 0;
	}
	
	public final int getTotalMembers(CabalType cabal)
	{
		int cabalMembers = 0;
		
		for (StatsSet set : _playersData.values())
			if (set.getEnum("cabal", CabalType.class) == cabal)
				cabalMembers++;
			
		return cabalMembers;
	}
	
	public int getPlayerStoneContrib(int objectId)
	{
		final StatsSet set = _playersData.get(objectId);
		if (set == null)
			return 0;
		
		return set.getInteger("red_stones") + set.getInteger("green_stones") + set.getInteger("blue_stones");
	}
	
	public int getPlayerContribScore(int objectId)
	{
		final StatsSet set = _playersData.get(objectId);
		if (set == null)
			return 0;
		
		return set.getInteger("contribution_score");
	}
	
	public int getPlayerAdenaCollect(int objectId)
	{
		final StatsSet set = _playersData.get(objectId);
		if (set == null)
			return 0;
		
		return set.getInteger("ancient_adena_amount");
	}
	
	public SealType getPlayerSeal(int objectId)
	{
		final StatsSet set = _playersData.get(objectId);
		if (set == null)
			return SealType.NONE;
		
		return set.getEnum("seal", SealType.class);
	}
	
	public CabalType getPlayerCabal(int objectId)
	{
		final StatsSet set = _playersData.get(objectId);
		if (set == null)
			return CabalType.NORMAL;
		
		return set.getEnum("cabal", CabalType.class);
	}
	
	/**
	 * Restores all Seven Signs data and settings, usually called at server startup.
	 */
	protected void restoreSevenSignsData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement(LOAD_DATA);
			ResultSet rset = st.executeQuery();
			
			while (rset.next())
			{
				final int objectId = rset.getInt("char_obj_id");
				
				final StatsSet set = new StatsSet();
				set.set("char_obj_id", objectId);
				set.set("cabal", Enum.valueOf(CabalType.class, rset.getString("cabal")));
				set.set("seal", Enum.valueOf(SealType.class, rset.getString("seal")));
				set.set("red_stones", rset.getInt("red_stones"));
				set.set("green_stones", rset.getInt("green_stones"));
				set.set("blue_stones", rset.getInt("blue_stones"));
				set.set("ancient_adena_amount", rset.getDouble("ancient_adena_amount"));
				set.set("contribution_score", rset.getDouble("contribution_score"));
				
				_playersData.put(objectId, set);
			}
			
			rset.close();
			st.close();
			
			st = con.prepareStatement(LOAD_STATUS);
			rset = st.executeQuery();
			
			while (rset.next())
			{
				_currentCycle = rset.getInt("current_cycle");
				_activePeriod = Enum.valueOf(PeriodType.class, rset.getString("active_period"));
				_previousWinner = Enum.valueOf(CabalType.class, rset.getString("previous_winner"));
				
				_dawnStoneScore = rset.getDouble("dawn_stone_score");
				_dawnFestivalScore = rset.getInt("dawn_festival_score");
				_duskStoneScore = rset.getDouble("dusk_stone_score");
				_duskFestivalScore = rset.getInt("dusk_festival_score");
				
				_sealOwners.put(SealType.AVARICE, Enum.valueOf(CabalType.class, rset.getString("avarice_owner")));
				_sealOwners.put(SealType.GNOSIS, Enum.valueOf(CabalType.class, rset.getString("gnosis_owner")));
				_sealOwners.put(SealType.STRIFE, Enum.valueOf(CabalType.class, rset.getString("strife_owner")));
				
				_dawnScores.put(SealType.AVARICE, rset.getInt("avarice_dawn_score"));
				_dawnScores.put(SealType.GNOSIS, rset.getInt("gnosis_dawn_score"));
				_dawnScores.put(SealType.STRIFE, rset.getInt("strife_dawn_score"));
				
				_duskScores.put(SealType.AVARICE, rset.getInt("avarice_dusk_score"));
				_duskScores.put(SealType.GNOSIS, rset.getInt("gnosis_dusk_score"));
				_duskScores.put(SealType.STRIFE, rset.getInt("strife_dusk_score"));
				
				_lastSave.setTimeInMillis(rset.getLong("date"));
			}
			
			rset.close();
			st.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "SevenSigns: Unable to load data to database: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Saves all Seven Signs player data.<br>
	 * Should be called on period change and shutdown only.
	 */
	public void saveSevenSignsData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(UPDATE_PLAYER))
		{
			for (StatsSet set : _playersData.values())
			{
				st.setString(1, set.getString("cabal"));
				st.setString(2, set.getString("seal"));
				st.setInt(3, set.getInteger("red_stones"));
				st.setInt(4, set.getInteger("green_stones"));
				st.setInt(5, set.getInteger("blue_stones"));
				st.setDouble(6, set.getDouble("ancient_adena_amount"));
				st.setDouble(7, set.getDouble("contribution_score"));
				st.setInt(8, set.getInteger("char_obj_id"));
				st.addBatch();
			}
			st.executeBatch();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "SevenSigns: Unable to save data to database: " + e.getMessage(), e);
		}
	}
	
	public final void saveSevenSignsStatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(UPDATE_STATUS))
		{
			st.setInt(1, _currentCycle);
			st.setString(2, _activePeriod.toString());
			st.setString(3, _previousWinner.toString());
			st.setDouble(4, _dawnStoneScore);
			st.setInt(5, _dawnFestivalScore);
			st.setDouble(6, _duskStoneScore);
			st.setInt(7, _duskFestivalScore);
			st.setString(8, _sealOwners.get(SealType.AVARICE).toString());
			st.setString(9, _sealOwners.get(SealType.GNOSIS).toString());
			st.setString(10, _sealOwners.get(SealType.STRIFE).toString());
			st.setInt(11, _dawnScores.get(SealType.AVARICE));
			st.setInt(12, _dawnScores.get(SealType.GNOSIS));
			st.setInt(13, _dawnScores.get(SealType.STRIFE));
			st.setInt(14, _duskScores.get(SealType.AVARICE));
			st.setInt(15, _duskScores.get(SealType.GNOSIS));
			st.setInt(16, _duskScores.get(SealType.STRIFE));
			st.setInt(17, SevenSignsFestival.getInstance().getCurrentFestivalCycle());
			
			for (int i = 0; i < SevenSignsFestival.FESTIVAL_COUNT; i++)
				st.setInt(18 + i, SevenSignsFestival.getInstance().getAccumulatedBonus(i));
			
			_lastSave = Calendar.getInstance();
			st.setLong(18 + SevenSignsFestival.FESTIVAL_COUNT, _lastSave.getTimeInMillis());
			st.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "SevenSigns: Unable to save status to database: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Used to reset the cabal details of all players, and update the database.<BR>
	 * Primarily used when beginning a new cycle, and should otherwise never be called.
	 */
	protected void resetPlayerData()
	{
		for (StatsSet set : _playersData.values())
		{
			set.set("cabal", CabalType.NORMAL);
			set.set("seal", SealType.NONE);
			set.set("contribution_score", 0);
		}
	}
	
	/**
	 * Used to specify cabal-related details for the specified player.<br>
	 * This method checks to see if the player has registered before and will update the database if necessary.
	 * @param objectId
	 * @param cabal
	 * @param seal
	 * @return the cabal ID the player has joined.
	 */
	public CabalType setPlayerInfo(int objectId, CabalType cabal, SealType seal)
	{
		StatsSet set = _playersData.get(objectId);
		if (set != null)
		{
			set.set("cabal", cabal);
			set.set("seal", seal);
		}
		else
		{
			set = new StatsSet();
			set.set("char_obj_id", objectId);
			set.set("cabal", cabal);
			set.set("seal", seal);
			set.set("red_stones", 0);
			set.set("green_stones", 0);
			set.set("blue_stones", 0);
			set.set("ancient_adena_amount", 0);
			set.set("contribution_score", 0);
			
			_playersData.put(objectId, set);
			
			// Update data in database, as we have a new player signing up.
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement st = con.prepareStatement(INSERT_PLAYER))
			{
				st.setInt(1, objectId);
				st.setString(2, cabal.toString());
				st.setString(3, seal.toString());
				st.execute();
			}
			catch (SQLException e)
			{
				_log.log(Level.SEVERE, "SevenSigns: Failed to save data: " + e.getMessage(), e);
			}
		}
		
		// Increasing Seal total score for the player chosen Seal.
		if (cabal == CabalType.DAWN)
			_dawnScores.put(seal, _dawnScores.get(seal) + 1);
		else
			_duskScores.put(seal, _duskScores.get(seal) + 1);
		
		return cabal;
	}
	
	/**
	 * @param objectId
	 * @return the amount of ancient adena the specified player can claim, if any.
	 */
	public int getAncientAdenaReward(int objectId)
	{
		StatsSet set = _playersData.get(objectId);
		int rewardAmount = set.getInteger("ancient_adena_amount");
		
		set.set("red_stones", 0);
		set.set("green_stones", 0);
		set.set("blue_stones", 0);
		set.set("ancient_adena_amount", 0);
		
		return rewardAmount;
	}
	
	/**
	 * Used to add the specified player's seal stone contribution points to the current total for their cabal. Returns the point score the contribution was worth.<br>
	 * Each stone count <B>must be</B> broken down and specified by the stone's color.
	 * @param objectId The objectId of the player.
	 * @param blueCount Amount of blue stones.
	 * @param greenCount Amount of green stones.
	 * @param redCount Amount of red stones.
	 * @return
	 */
	public int addPlayerStoneContrib(int objectId, int blueCount, int greenCount, int redCount)
	{
		StatsSet set = _playersData.get(objectId);
		
		int contribScore = calcScore(blueCount, greenCount, redCount);
		int totalAncientAdena = set.getInteger("ancient_adena_amount") + contribScore;
		int totalContribScore = set.getInteger("contribution_score") + contribScore;
		
		if (totalContribScore > Config.ALT_MAXIMUM_PLAYER_CONTRIB)
			return -1;
		
		set.set("red_stones", set.getInteger("red_stones") + redCount);
		set.set("green_stones", set.getInteger("green_stones") + greenCount);
		set.set("blue_stones", set.getInteger("blue_stones") + blueCount);
		set.set("ancient_adena_amount", totalAncientAdena);
		set.set("contribution_score", totalContribScore);
		
		switch (getPlayerCabal(objectId))
		{
			case DAWN:
				_dawnStoneScore += contribScore;
				break;
			
			case DUSK:
				_duskStoneScore += contribScore;
				break;
		}
		
		return contribScore;
	}
	
	/**
	 * Adds the specified number of festival points to the specified cabal. Remember, the same number of points are <B>deducted from the rival cabal</B> to maintain proportionality.
	 * @param cabal
	 * @param amount
	 */
	public void addFestivalScore(CabalType cabal, int amount)
	{
		if (cabal == CabalType.DUSK)
		{
			_duskFestivalScore += amount;
			
			// To prevent negative scores!
			if (_dawnFestivalScore >= amount)
				_dawnFestivalScore -= amount;
		}
		else
		{
			_dawnFestivalScore += amount;
			
			if (_duskFestivalScore >= amount)
				_duskFestivalScore -= amount;
		}
	}
	
	/**
	 * Used to initialize the seals for each cabal. (Used at startup or at beginning of a new cycle). This method should be called after <B>resetSeals()</B> and <B>calcNewSealOwners()</B> on a new cycle.
	 */
	protected void initializeSeals()
	{
		for (Entry<SealType, CabalType> sealEntry : _sealOwners.entrySet())
		{
			final SealType currentSeal = sealEntry.getKey();
			final CabalType sealOwner = sealEntry.getValue();
			
			if (sealOwner != CabalType.NORMAL)
			{
				if (isSealValidationPeriod())
					_log.info("SevenSigns: The " + sealOwner.getFullName() + " have won the " + currentSeal.getFullName() + ".");
				else
					_log.info("SevenSigns: The " + currentSeal.getFullName() + " is currently owned by " + sealOwner.getFullName() + ".");
			}
			else
				_log.info("SevenSigns: The " + currentSeal.getFullName() + " remains unclaimed.");
		}
	}
	
	/**
	 * Only really used at the beginning of a new cycle, this method resets all seal-related data.
	 */
	protected void resetSeals()
	{
		_dawnScores.put(SealType.AVARICE, 0);
		_dawnScores.put(SealType.GNOSIS, 0);
		_dawnScores.put(SealType.STRIFE, 0);
		
		_duskScores.put(SealType.AVARICE, 0);
		_duskScores.put(SealType.GNOSIS, 0);
		_duskScores.put(SealType.STRIFE, 0);
	}
	
	/**
	 * Calculates the ownership of the three Seals of the Seven Signs, based on various criterias.<BR>
	 * Should only ever called at the beginning of a new cycle.
	 */
	protected void calcNewSealOwners()
	{
		for (SealType seal : _dawnScores.keySet())
		{
			final CabalType prevSealOwner = _sealOwners.get(seal);
			
			final int dawnProportion = getSealProportion(seal, CabalType.DAWN);
			final int totalDawnMembers = Math.max(1, getTotalMembers(CabalType.DAWN));
			final int dawnPercent = Math.round(((float) dawnProportion / (float) totalDawnMembers) * 100);
			
			final int duskProportion = getSealProportion(seal, CabalType.DUSK);
			final int totalDuskMembers = Math.max(1, getTotalMembers(CabalType.DUSK));
			final int duskPercent = Math.round(((float) duskProportion / (float) totalDuskMembers) * 100);
			
			CabalType newSealOwner = CabalType.NORMAL;
			
			// If a Seal was already closed or owned by the opponent and the new winner wants to assume ownership of the Seal, 35% or more of the members of the Cabal must have chosen the Seal. If they chose less than 35%, they cannot own the Seal.
			// If the Seal was owned by the winner in the previous Seven Signs, they can retain that seal if 10% or more members have chosen it. If they want to possess a new Seal, at least 35% of the members of the Cabal must have chosen the new Seal.
			switch (prevSealOwner)
			{
				case NORMAL:
					switch (getCabalHighestScore())
					{
						case DAWN:
							if (dawnPercent >= 35)
								newSealOwner = CabalType.DAWN;
							break;
						
						case DUSK:
							if (duskPercent >= 35)
								newSealOwner = CabalType.DUSK;
							break;
					}
					break;
				
				case DAWN:
					switch (getCabalHighestScore())
					{
						case NORMAL:
							if (dawnPercent >= 10)
								newSealOwner = CabalType.DAWN;
							break;
						
						case DAWN:
							if (dawnPercent >= 10)
								newSealOwner = CabalType.DAWN;
							break;
						
						case DUSK:
							if (duskPercent >= 35)
								newSealOwner = CabalType.DUSK;
							else if (dawnPercent >= 10)
								newSealOwner = CabalType.DAWN;
							break;
					}
					break;
				
				case DUSK:
					switch (getCabalHighestScore())
					{
						case NORMAL:
							if (duskPercent >= 10)
								newSealOwner = CabalType.DUSK;
							break;
						
						case DAWN:
							if (dawnPercent >= 35)
								newSealOwner = CabalType.DAWN;
							else if (duskPercent >= 10)
								newSealOwner = CabalType.DUSK;
							break;
						
						case DUSK:
							if (duskPercent >= 10)
								newSealOwner = CabalType.DUSK;
							break;
					}
					break;
			}
			
			_sealOwners.put(seal, newSealOwner);
			
			// Alert all online players to new seal status.
			switch (seal)
			{
				case AVARICE:
					if (newSealOwner == CabalType.DAWN)
						Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DAWN_OBTAINED_AVARICE));
					else if (newSealOwner == CabalType.DUSK)
						Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DUSK_OBTAINED_AVARICE));
					break;
				
				case GNOSIS:
					if (newSealOwner == CabalType.DAWN)
						Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DAWN_OBTAINED_GNOSIS));
					else if (newSealOwner == CabalType.DUSK)
						Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DUSK_OBTAINED_GNOSIS));
					break;
				
				case STRIFE:
					if (newSealOwner == CabalType.DAWN)
						Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DAWN_OBTAINED_STRIFE));
					else if (newSealOwner == CabalType.DUSK)
						Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DUSK_OBTAINED_STRIFE));
					
					CastleManager.getInstance().validateTaxes(newSealOwner);
					break;
			}
		}
	}
	
	/**
	 * This method is called to remove all players from catacombs and necropolises, who belong to the losing cabal.<BR>
	 * <b>Should only ever called at the beginning of Seal Validation.</b>
	 * @param winningCabal
	 */
	protected void teleLosingCabalFromDungeons(CabalType winningCabal)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isGM() || !player.isIn7sDungeon())
				continue;
			
			final StatsSet set = _playersData.get(player.getObjectId());
			if (set != null)
			{
				final CabalType playerCabal = set.getEnum("cabal", CabalType.class);
				if (isSealValidationPeriod() || isCompResultsPeriod())
				{
					if (playerCabal == winningCabal)
						continue;
				}
				else if (playerCabal == CabalType.NORMAL)
					continue;
			}
			
			player.teleToLocation(TeleportType.TOWN);
			player.setIsIn7sDungeon(false);
		}
	}
	
	/**
	 * The primary controller of period change of the Seven Signs system. This runs all related tasks depending on the period that is about to begin.
	 */
	protected class SevenSignsPeriodChange implements Runnable
	{
		@Override
		public void run()
		{
			// Remember the period check here refers to the period just ENDED!
			final PeriodType periodEnded = _activePeriod;
			
			// Increment the period.
			_activePeriod = PeriodType.VALUES[(_activePeriod.ordinal() + 1) % PeriodType.VALUES.length];
			
			switch (periodEnded)
			{
				case RECRUITING: // Initialization
					// Start the Festival of Darkness cycle.
					SevenSignsFestival.getInstance().startFestivalManager();
					
					// Reset castles certificates count.
					CastleManager.getInstance().resetCertificates();
					
					// Send message that Competition has begun.
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.QUEST_EVENT_PERIOD_BEGUN));
					break;
				
				case COMPETITION: // Results Calculation
					// Send message that Competition has ended.
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.QUEST_EVENT_PERIOD_ENDED));
					
					final CabalType winningCabal = getCabalHighestScore();
					
					// Schedule a stop of the festival engine and reward highest ranking members from cycle
					SevenSignsFestival.getInstance().getFestivalManagerSchedule().cancel(false);
					SevenSignsFestival.getInstance().rewardHighestRanked();
					
					calcNewSealOwners();
					
					switch (winningCabal)
					{
						case DAWN:
							Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DAWN_WON));
							break;
						
						case DUSK:
							Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DUSK_WON));
							break;
					}
					
					_previousWinner = winningCabal;
					break;
				
				case RESULTS: // Seal Validation
					// Perform initial Seal Validation set up.
					initializeSeals();
					
					// Buff/Debuff members of the event when Seal of Strife captured.
					giveSosEffect(getSealOwner(SealType.STRIFE));
					
					// Send message that Seal Validation has begun.
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SEAL_VALIDATION_PERIOD_BEGUN));
					
					_log.info("SevenSigns: The " + _previousWinner.getFullName() + " have won the competition with " + getCurrentScore(_previousWinner) + " points!");
					break;
				
				case SEAL_VALIDATION: // Reset for New Cycle
					// Ensure a cycle restart when this period ends.
					_activePeriod = PeriodType.RECRUITING;
					
					// Send message that Seal Validation has ended.
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SEAL_VALIDATION_PERIOD_ENDED));
					
					// Clear Seal of Strife influence.
					removeSosEffect();
					
					// Reset all data
					resetPlayerData();
					resetSeals();
					
					_currentCycle++;
					
					// Reset all Festival-related data and remove any unused blood offerings.
					// NOTE: A full update of Festival data in the database is also performed.
					SevenSignsFestival.getInstance().resetFestivalData(false);
					
					_dawnStoneScore = 0;
					_duskStoneScore = 0;
					
					_dawnFestivalScore = 0;
					_duskFestivalScore = 0;
					break;
			}
			
			// Make sure all Seven Signs data is saved for future use.
			saveSevenSignsData();
			saveSevenSignsStatus();
			
			teleLosingCabalFromDungeons(getCabalHighestScore());
			
			// Spawns NPCs and change sky color.
			Broadcast.toAllOnlinePlayers(SSQInfo.sendSky());
			spawnSevenSignsNPC();
			
			_log.info("SevenSigns: The " + _activePeriod.getName() + " period has begun!");
			
			setCalendarForNextPeriodChange();
			
			ThreadPool.schedule(new SevenSignsPeriodChange(), getMilliToPeriodChange());
		}
	}
	
	/**
	 * Buff/debuff players following their membership to Seal of Strife.
	 * @param strifeOwner The cabal owning the Seal of Strife.
	 */
	public void giveSosEffect(CabalType strifeOwner)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			final CabalType cabal = getPlayerCabal(player.getObjectId());
			if (cabal != CabalType.NORMAL)
			{
				if (cabal == strifeOwner)
					player.addSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.getSkill(), false);
				else
					player.addSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill(), false);
			}
		}
	}
	
	/**
	 * Stop Seal of Strife effects on all online characters.
	 */
	public void removeSosEffect()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			player.removeSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.getSkill().getId(), false);
			player.removeSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill().getId(), false);
		}
	}
	
	public static SevenSigns getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SevenSigns _instance = new SevenSigns();
	}
}