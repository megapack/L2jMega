package net.sf.l2j.gameserver.model.actor.instance;

import com.elfocrash.roboto.FakePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.CoupleManager;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.data.manager.DuelManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.data.xml.FishData;
import net.sf.l2j.gameserver.data.xml.HennaData;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.data.xml.PlayerData;
import net.sf.l2j.gameserver.data.xml.RecipeData;
import net.sf.l2j.gameserver.events.teamvsteam.TvTEvent;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.HeroManager;
import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.CabalType;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.SealType;
import net.sf.l2j.gameserver.instancemanager.SevenSignsFestival;
import net.sf.l2j.gameserver.instancemanager.VipManager;
import net.sf.l2j.gameserver.model.AccessLevel;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.Fish;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Fishing;
import net.sf.l2j.gameserver.model.L2Macro;
import net.sf.l2j.gameserver.model.L2Radar;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.MacroList;
import net.sf.l2j.gameserver.model.PetDataEntry;
import net.sf.l2j.gameserver.model.Request;
import net.sf.l2j.gameserver.model.RewardInfo;
import net.sf.l2j.gameserver.model.ShortCuts;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.ai.CtrlEvent;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.ai.NextAction;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.ai.type.PlayerAI;
import net.sf.l2j.gameserver.model.actor.ai.type.SummonAI;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2j.gameserver.model.actor.status.PlayerStatus;
import net.sf.l2j.gameserver.model.actor.template.PetTemplate;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.ClassRace;
import net.sf.l2j.gameserver.model.base.ClassType;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.model.base.Sex;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.craft.ManufactureList;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.Siege.SiegeSide;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.group.Party.LootRule;
import net.sf.l2j.gameserver.model.group.Party.MessageType;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.model.item.Henna;
import net.sf.l2j.gameserver.model.item.Recipe;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemState;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.item.type.ActionType;
import net.sf.l2j.gameserver.model.item.type.ArmorType;
import net.sf.l2j.gameserver.model.item.type.EtcItemType;
import net.sf.l2j.gameserver.model.item.type.WeaponType;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2j.gameserver.model.itemcontainer.PcFreight;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.model.itemcontainer.PcWarehouse;
import net.sf.l2j.gameserver.model.itemcontainer.PetInventory;
import net.sf.l2j.gameserver.model.itemcontainer.listeners.ItemPassiveSkillsListener;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.memo.PlayerMemo;
import net.sf.l2j.gameserver.model.multisell.PreparedListContainer;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AbstractNpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ChairSit;
import net.sf.l2j.gameserver.network.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingEnd;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingStart;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadMode;
import net.sf.l2j.gameserver.network.serverpackets.ExSetCompassZoneCode;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.GetOnVehicle;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.LeaveWorld;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ObservationMode;
import net.sf.l2j.gameserver.network.serverpackets.ObservationReturn;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PetInventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreListBuy;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreListSell;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreManageListSell;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgSell;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopManageList;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopMsg;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopSellList;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge.GaugeColor;
import net.sf.l2j.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2j.gameserver.network.serverpackets.SkillList;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpawnItem;
import net.sf.l2j.gameserver.network.serverpackets.StaticObjectInfo;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.TargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.TargetUnselected;
import net.sf.l2j.gameserver.network.serverpackets.TitleUpdate;
import net.sf.l2j.gameserver.network.serverpackets.TradePressOtherOk;
import net.sf.l2j.gameserver.network.serverpackets.TradePressOwnOk;
import net.sf.l2j.gameserver.network.serverpackets.TradeStart;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.scripting.EventType;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.ScriptManager;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.skills.funcs.FuncHennaCON;
import net.sf.l2j.gameserver.skills.funcs.FuncHennaDEX;
import net.sf.l2j.gameserver.skills.funcs.FuncHennaINT;
import net.sf.l2j.gameserver.skills.funcs.FuncHennaMEN;
import net.sf.l2j.gameserver.skills.funcs.FuncHennaSTR;
import net.sf.l2j.gameserver.skills.funcs.FuncHennaWIT;
import net.sf.l2j.gameserver.skills.funcs.FuncMaxCpMul;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSiegeFlag;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSummon;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;
import net.sf.l2j.gameserver.taskmanager.ItemsOnGroundTaskManager;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;
import net.sf.l2j.gameserver.taskmanager.ShadowItemTaskManager;
import net.sf.l2j.gameserver.taskmanager.WaterTaskManager;
import net.sf.l2j.gameserver.templates.skills.L2EffectFlag;
import net.sf.l2j.gameserver.templates.skills.L2EffectType;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.gameserver.vip.VIPReward;

import phantom.PhantomArchMage;
import phantom.PhantomMysticMuse;
import phantom.PhantomStormScream;

/**
 * This class represents a player in the world.<br>
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).
 */
public class Player extends Playable
{
	private FakePlayer _fakePlayerUnderControl = null;
	
	public boolean isControllingFakePlayer() {
		return _fakePlayerUnderControl != null;
	}
	
	public FakePlayer getPlayerUnderControl() {
		return _fakePlayerUnderControl;
	}
	
	public void setPlayerUnderControl(FakePlayer fakePlayer) {
		_fakePlayerUnderControl = fakePlayer;
	}

	public enum StoreType
	{
		NONE(0),
		SELL(1),
		SELL_MANAGE(2),
		BUY(3),
		BUY_MANAGE(4),
		MANUFACTURE(5),
		PACKAGE_SELL(8);
		
		private int _id;
		
		private StoreType(int id)
		{
			_id = id;
		}
		
		public int getId()
		{
			return _id;
		}
	}
	
	public enum PunishLevel
	{
		NONE(0, ""),
		CHAT(1, "chat banned"),
		JAIL(2, "jailed"),
		CHAR(3, "banned"),
		ACC(4, "banned");
		
		private final int punValue;
		private final String punString;
		
		PunishLevel(int value, String string)
		{
			punValue = value;
			punString = string;
		}
		
		public int value()
		{
			return punValue;
		}
		
		public String string()
		{
			return punString;
		}
	}
	
	private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_OR_UPDATE_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,class_index) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)";
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";
	
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE char_obj_id=? AND class_index=? ORDER BY buff_index ASC";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
	
	private static final String INSERT_CHARACTER = "INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,nobless,power_grade) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=? WHERE obj_id=?";
	private static final String RESTORE_CHARACTER = "SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, punish_level, punish_timer, nobless, power_grade, subpledge, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,death_penalty_level FROM characters WHERE obj_id=?";

	private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
	private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
	private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";
	
	private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?";
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
	
	private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
	private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
	private static final String UPDATE_TARGET_RECOM_HAVE = "UPDATE characters SET rec_have=? WHERE obj_Id=?";
	private static final String UPDATE_CHAR_RECOM_LEFT = "UPDATE characters SET rec_left=? WHERE obj_Id=?";
	
	private static final String UPDATE_NOBLESS = "UPDATE characters SET nobless=? WHERE obj_Id=?";
	
	public static final int REQUEST_TIMEOUT = 15;
	
	private static final Comparator<GeneralSkillNode> COMPARE_SKILLS_BY_MIN_LVL = Comparator.comparing(GeneralSkillNode::getMinLvl);
	private static final Comparator<GeneralSkillNode> COMPARE_SKILLS_BY_LVL = Comparator.comparing(GeneralSkillNode::getValue);
	
	private L2GameClient _client;
	private final Map<Integer, String> _chars = new HashMap<>();
	
	private String _accountName;
	private long _deleteTimer;
	
	private boolean _isOnline;
	private long _onlineTime;
	private long _onlineBeginTime;
	private long _lastAccess;
	private long _uptime;
	
	protected int _baseClass;
	protected int _activeClass;
	protected int _classIndex;
	
	private final Map<Integer, SubClass> _subClasses = new ConcurrentSkipListMap<>();
	private final ReentrantLock _subclassLock = new ReentrantLock();
	
	private PcAppearance _appearance;
	
	private long _expBeforeDeath;
	private int _karma;
	private int _pvpKills;
	private int _pkKills;
	private byte _pvpFlag;
	private byte _siegeState;
	private int _curWeightPenalty;
	
	private int _lastCompassZone; // the last compass zone update send to the client
	
	private boolean _isIn7sDungeon;
	
	private PunishLevel _punishLevel = PunishLevel.NONE;
	private long _punishTimer;
	private ScheduledFuture<?> _punishTask;
	
	private boolean _isInOlympiadMode;
	private boolean _isInOlympiadStart;
	private int _olympiadGameId = -1;
	private int _olympiadSide = -1;
	
	private DuelState _duelState = DuelState.NO_DUEL;
	private int _duelId;
	private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	
	private Boat _boat;
	private SpawnLocation _boatPosition = new SpawnLocation(0, 0, 0, 0);
	
	private ScheduledFuture<?> _fishingTask;
	
	

	  
	private boolean _canFeed;
	protected PetTemplate _petTemplate;
	private PetDataEntry _petData;
	private int _controlItemId;
	private int _curFeed;
	protected Future<?> _mountFeedTask;
	private ScheduledFuture<?> _dismountTask;
	
	private int _mountType;
	private int _mountNpcId;
	private int _mountLevel;
	private int _mountObjectId;
	
	protected int _throneId;
	
	private int _teleMode;
	private boolean _isCrystallizing;
	private boolean _isCrafting;
	
	private final Map<Integer, Recipe> _dwarvenRecipeBook = new HashMap<>();
	private final Map<Integer, Recipe> _commonRecipeBook = new HashMap<>();
	
	private boolean _isSitting;
	
	private final Location _savedLocation = new Location(0, 0, 0);
	
	private int _recomHave;
	private int _recomLeft;
	private final List<Integer> _recomChars = new ArrayList<>();
	
	private final PcInventory _inventory = new PcInventory(this);
	private PcWarehouse _warehouse;
	private PcFreight _freight;
	private final List<PcFreight> _depositedFreight = new ArrayList<>();
	
	private StoreType _storeType = StoreType.NONE;
	
	private TradeList _activeTradeList;
	private ItemContainer _activeWarehouse;
	private ManufactureList _createList;
	private TradeList _sellList;
	private TradeList _buyList;
	
	private PreparedListContainer _currentMultiSell;
	
	private boolean _isNoble;
	private boolean _isHero;
	
	private Folk _currentFolk;
	
	private int _questNpcObject;
	
	private final List<QuestState> _quests = new ArrayList<>();
	private final List<QuestState> _notifyQuestOfDeathList = new ArrayList<>();
	
	private final PlayerMemo _vars = new PlayerMemo(getObjectId());
	
	private final ShortCuts _shortCuts = new ShortCuts(this);
	
	private final MacroList _macroses = new MacroList(this);
	
	private final Henna[] _henna = new Henna[3];
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;
	
	private Summon _summon;
	private TamedBeast _tamedBeast;
	
	// TODO: This needs to be better integrated and saved/loaded
	private L2Radar _radar;
	
	private int _partyroom;
	
	private int _clanId;
	private Clan _clan;
	private int _apprentice;
	private int _sponsor;
	private long _clanJoinExpiryTime;
	private long _clanCreateExpiryTime;
	private int _powerGrade;
	private int _clanPrivileges;
	private int _pledgeClass;
	private int _pledgeType;
	private int _lvlJoinedAcademy;
	
	private boolean _wantsPeace;
	
	private int _deathPenaltyBuffLevel;
	
	private final AtomicInteger _charges = new AtomicInteger();
	private ScheduledFuture<?> _chargeTask;
	
	private Location _currentSkillWorldPosition;
	
	private AccessLevel _accessLevel;
	
	private boolean _messageRefusal; // message refusal mode
	private boolean _tradeRefusal; // Trade refusal
	private boolean _exchangeRefusal; // Exchange refusal
	
	private Party _party;
	private LootRule _lootRule;
	
	private Player _activeRequester;
	private long _requestExpireTime;
	private final Request _request = new Request(this);
	
	private ScheduledFuture<?> _protectTask;
	
	private long _recentFakeDeathEndTime;
	private boolean _isFakeDeath;
	
	private int _expertiseArmorPenalty;
	private boolean _expertiseWeaponPenalty;
	
	private ItemInstance _activeEnchantItem;
	
	protected boolean _inventoryDisable;
	
	protected Map<Integer, Cubic> _cubics = new ConcurrentSkipListMap<>();
	
	protected Set<Integer> _activeSoulShots = ConcurrentHashMap.newKeySet(1);
	
	private final int _loto[] = new int[5];
	private final int _race[] = new int[2];
	
	private final BlockList _blockList = new BlockList(this);
	
	private int _team;
	
	private int _alliedVarkaKetra; // lvl of alliance with ketra orcs or varka silenos, used in quests and aggro checks [-5,-1] varka, 0 neutral, [1,5] ketra
	
	private Location _fishingLoc;
	private ItemInstance _lure;
	private L2Fishing _fishCombat;
	private Fish _fish;
	
	private final List<String> _validBypass = new ArrayList<>();
	private final List<String> _validBypass2 = new ArrayList<>();
	
	private Forum _forumMemo;
	
	private boolean _isInSiege;
	
	private final Map<Integer, L2Skill> _skills = new ConcurrentSkipListMap<>();
	
	private final SkillUseHolder _currentSkill = new SkillUseHolder();
	private final SkillUseHolder _currentPetSkill = new SkillUseHolder();
	private final SkillUseHolder _queuedSkill = new SkillUseHolder();
	
	private int _cursedWeaponEquippedId;
	
	private int _reviveRequested;
	private double _revivePower = .0;
	private boolean _revivePet;
	
	private double _cpUpdateIncCheck = .0;
	private double _cpUpdateDecCheck = .0;
	private double _cpUpdateInterval = .0;
	private double _mpUpdateIncCheck = .0;
	private double _mpUpdateDecCheck = .0;
	private double _mpUpdateInterval = .0;
	
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	
	private int _mailPosition;
	
	private static final int FALLING_VALIDATION_DELAY = 10000;
	private volatile long _fallingTimestamp;
	
	private ScheduledFuture<?> _shortBuffTask;
	private int _shortBuffTaskSkillId;
	
	private int _coupleId;
	private boolean _isUnderMarryRequest;
	private int _requesterId;
	
	private final List<Integer> _friendList = new ArrayList<>(); // Related to CB.
	private final List<Integer> _selectedFriendList = new ArrayList<>(); // Related to CB.
	private final List<Integer> _selectedBlocksList = new ArrayList<>(); // Related to CB.
	
	private Player _summonTargetRequest;
	private L2Skill _summonSkillRequest;
	
	private Door _requestedGate;
	
	/**
	 * Constructor of Player (use Creature constructor).
	 * <ul>
	 * <li>Call the Creature constructor to create an empty _skills slot and copy basic Calculator set to this Player</li>
	 * <li>Set the name of the Player</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the Player to 1</B></FONT>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the Player
	 * @param accountName The name of the account including this Player
	 * @param app The PcAppearance of the Player
	 */
	protected Player(int objectId, PlayerTemplate template, String accountName, PcAppearance app)
	{
		super(objectId, template);
		
		initCharStatusUpdateValues();
		
		_accountName = accountName;
		_appearance = app;
		
		// Create an AI
		_ai = new PlayerAI(this);
		
		// Create a L2Radar object
		_radar = new L2Radar(this);
		
		// Retrieve from the database all items of this Player and add them to _inventory
		getInventory().restore();
		getWarehouse();
		getFreight();
	}
	
	protected Player(int objectId)
	{
		super(objectId, null);
		
		initCharStatusUpdateValues();
	}
	
	/**
	 * Create a new Player and add it in the characters table of the database.
	 * <ul>
	 * <li>Create a new Player with an account name</li>
	 * <li>Set the name, the Hair Style, the Hair Color and the Face type of the Player</li>
	 * <li>Add the player in the characters table of the database</li>
	 * </ul>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the Player
	 * @param accountName The name of the Player
	 * @param name The name of the Player
	 * @param hairStyle The hair style Identifier of the Player
	 * @param hairColor The hair color Identifier of the Player
	 * @param face The face type Identifier of the Player
	 * @param sex The sex type Identifier of the Player
	 * @return The Player added to the database or null
	 */
	public static Player create(int objectId, PlayerTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, Sex sex)
	{
		// Create a new Player with an account name
		PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
		Player player = new Player(objectId, template, accountName, app);
		
		// Set the name of the Player
		player.setName(name);
		
		// Set access level
		player.setAccessLevel(Config.DEFAULT_ACCESS_LEVEL);
		
		// Cache few informations into CharNameTable.
		PlayerInfoTable.getInstance().addPlayer(objectId, accountName, name, player.getAccessLevel().getLevel());
		
		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());
		
		// Add the player in the characters table of the database
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(INSERT_CHARACTER);
			statement.setString(1, accountName);
			statement.setInt(2, player.getObjectId());
			statement.setString(3, player.getName());
			statement.setInt(4, player.getLevel());
			statement.setInt(5, player.getMaxHp());
			statement.setDouble(6, player.getCurrentHp());
			statement.setInt(7, player.getMaxCp());
			statement.setDouble(8, player.getCurrentCp());
			statement.setInt(9, player.getMaxMp());
			statement.setDouble(10, player.getCurrentMp());
			statement.setInt(11, player.getAppearance().getFace());
			statement.setInt(12, player.getAppearance().getHairStyle());
			statement.setInt(13, player.getAppearance().getHairColor());
			statement.setInt(14, player.getAppearance().getSex().ordinal());
			statement.setLong(15, player.getExp());
			statement.setInt(16, player.getSp());
			statement.setInt(17, player.getKarma());
			statement.setInt(18, player.getPvpKills());
			statement.setInt(19, player.getPkKills());
			statement.setInt(20, player.getClanId());
			statement.setInt(21, player.getRace().ordinal());
			statement.setInt(22, player.getClassId().getId());
			statement.setLong(23, player.getDeleteTimer());
			statement.setInt(24, player.hasDwarvenCraft() ? 1 : 0);
			statement.setString(25, player.getTitle());
			statement.setInt(26, player.getAccessLevel().getLevel());
			statement.setInt(27, player.isOnlineInt());
			statement.setInt(28, player.isIn7sDungeon() ? 1 : 0);
			statement.setInt(29, player.getClanPrivileges());
			statement.setInt(30, player.wantsPeace() ? 1 : 0);
			statement.setInt(31, player.getBaseClass());
			statement.setInt(32, player.isNoble() ? 1 : 0);
			statement.setLong(33, 0);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.severe("Could not insert char data: " + e);
			return null;
		}
		
		return player;
	}
	
	@Override
	public void addFuncsToNewCharacter()
	{
		// Add Creature functionalities.
		super.addFuncsToNewCharacter();
		
		addStatFunc(FuncMaxCpMul.getInstance());
		
		addStatFunc(FuncHennaSTR.getInstance());
		addStatFunc(FuncHennaDEX.getInstance());
		addStatFunc(FuncHennaINT.getInstance());
		addStatFunc(FuncHennaMEN.getInstance());
		addStatFunc(FuncHennaCON.getInstance());
		addStatFunc(FuncHennaWIT.getInstance());
	}
	
	@Override
	protected void initCharStatusUpdateValues()
	{
		super.initCharStatusUpdateValues();
		
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new PlayerStat(this));
	}
	
	@Override
	public final PlayerStat getStat()
	{
		return (PlayerStat) super.getStat();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new PlayerStatus(this));
	}
	
	@Override
	public final PlayerStatus getStatus()
	{
		return (PlayerStatus) super.getStatus();
	}
	
	public final PcAppearance getAppearance()
	{
		return _appearance;
	}
	
	/**
	 * @return the base L2PcTemplate link to the Player.
	 */
	public final PlayerTemplate getBaseTemplate()
	{
		return PlayerData.getInstance().getTemplate(_baseClass);
	}
	
	/** Return the L2PcTemplate link to the Player. */
	@Override
	public final PlayerTemplate getTemplate()
	{
		return (PlayerTemplate) super.getTemplate();
	}
	
	public void setTemplate(ClassId newclass)
	{
		super.setTemplate(PlayerData.getInstance().getTemplate(newclass));
	}
	
	/**
	 * Return the AI of the Player (create it if necessary).
	 */
	@Override
	public CreatureAI getAI()
	{
		CreatureAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new PlayerAI(this);
				
				return _ai;
			}
		}
		return ai;
	}
	
	/** Return the Level of the Player. */
	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}
	
	/**
	 * A newbie is a player reaching level 6. He isn't considered newbie at lvl 25.<br>
	 * Since IL newbie isn't anymore the first character of an account reaching that state, but any.
	 * @return True if newbie.
	 */
	public boolean isNewbie()
	{
		return getClassId().level() <= 1 && getLevel() >= 6 && getLevel() <= 25;
	}
	
	public void setBaseClass(int baseClass)
	{
		_baseClass = baseClass;
	}
	
	public void setBaseClass(ClassId classId)
	{
		_baseClass = classId.ordinal();
	}
	
	public boolean isInStoreMode()
	{
		return _storeType != StoreType.NONE;
	}
	
	public boolean isCrafting()
	{
		return _isCrafting;
	}
	
	public void setCrafting(boolean state)
	{
		_isCrafting = state;
	}
	
	public void logout()
	{
		logout(true);
	}
	
	public void logout(boolean closeClient)
	{
		try
		{
			closeNetConnection(closeClient);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on logout(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @return a table containing all Common RecipeList of the Player.
	 */
	public Collection<Recipe> getCommonRecipeBook()
	{
		return _commonRecipeBook.values();
	}
	
	/**
	 * @return a table containing all Dwarf RecipeList of the Player.
	 */
	public Collection<Recipe> getDwarvenRecipeBook()
	{
		return _dwarvenRecipeBook.values();
	}
	
	/**
	 * Add a new L2RecipList to the table _commonrecipebook containing all RecipeList of the Player.
	 * @param recipe The RecipeList to add to the _recipebook
	 */
	public void registerCommonRecipeList(Recipe recipe)
	{
		_commonRecipeBook.put(recipe.getId(), recipe);
	}
	
	/**
	 * Add a new L2RecipList to the table _recipebook containing all RecipeList of the Player.
	 * @param recipe The RecipeList to add to the _recipebook
	 */
	public void registerDwarvenRecipeList(Recipe recipe)
	{
		_dwarvenRecipeBook.put(recipe.getId(), recipe);
	}
	
	/**
	 * @param recipeId The Identifier of the RecipeList to check in the player's recipe books
	 * @return <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
	 */
	public boolean hasRecipeList(int recipeId)
	{
		return _dwarvenRecipeBook.containsKey(recipeId) || _commonRecipeBook.containsKey(recipeId);
	}
	
	/**
	 * Tries to remove a L2RecipList from the table _DwarvenRecipeBook or from table _CommonRecipeBook, those table contain all RecipeList of the Player.
	 * @param recipeId The Identifier of the RecipeList to remove from the _recipebook.
	 */
	public void unregisterRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
			_dwarvenRecipeBook.remove(recipeId);
		else if (_commonRecipeBook.containsKey(recipeId))
			_commonRecipeBook.remove(recipeId);
		else
			_log.warning("Attempted to remove unknown RecipeList: " + recipeId);
		
		for (L2ShortCut sc : getAllShortCuts())
		{
			if (sc != null && sc.getId() == recipeId && sc.getType() == L2ShortCut.TYPE_RECIPE)
				deleteShortCut(sc.getSlot(), sc.getPage());
		}
	}
	
	/**
	 * @return the Id for the last talked quest NPC.
	 */
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}
	
	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}
	
	/**
	 * @param name The name of the quest.
	 * @return The QuestState object corresponding to the quest name.
	 */
	public QuestState getQuestState(String name)
	{
		for (QuestState qs : _quests)
		{
			if (name.equals(qs.getQuest().getName()))
				return qs;
		}
		return null;
	}
	
	/**
	 * Add a QuestState to the table _quest containing all quests began by the Player.
	 * @param qs The QuestState to add to _quest.
	 */
	public void setQuestState(QuestState qs)
	{
		_quests.add(qs);
	}
	
	/**
	 * Remove a QuestState from the table _quest containing all quests began by the Player.
	 * @param qs : The QuestState to be removed from _quest.
	 */
	public void delQuestState(QuestState qs)
	{
		_quests.remove(qs);
	}
	
	/**
	 * @param completed : If true, include completed quests to the list.
	 * @return list of started and eventually completed quests of the player.
	 */
	public List<Quest> getAllQuests(boolean completed)
	{
		List<Quest> quests = new ArrayList<>();
		
		for (QuestState qs : _quests)
		{
			if (qs == null || completed && qs.isCreated() || !completed && !qs.isStarted())
				continue;
			
			Quest quest = qs.getQuest();
			if (quest == null || !quest.isRealQuest())
				continue;
			
			quests.add(quest);
		}
		
		return quests;
	}
	
	public void processQuestEvent(String questName, String event)
	{
		Quest quest = ScriptManager.getInstance().getQuest(questName);
		if (quest == null)
			return;
		
		QuestState qs = getQuestState(questName);
		if (qs == null)
			return;
		
		WorldObject object = World.getInstance().getObject(getLastQuestNpcObject());
		if (!(object instanceof Npc) || !isInsideRadius(object, Npc.INTERACTION_DISTANCE, false, false))
			return;
		
		Npc npc = (Npc) object;
		List<Quest> quests = npc.getTemplate().getEventQuests(EventType.ON_TALK);
		if (quests != null)
		{
			for (Quest onTalk : quests)
			{
				if (onTalk == null || !onTalk.equals(quest))
					continue;
				
				quest.notifyEvent(event, npc, this);
				break;
			}
		}
	}
	
	/**
	 * Add QuestState instance that is to be notified of Player's death.
	 * @param qs The QuestState that subscribe to this event
	 */
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null)
			return;
		
		if (!_notifyQuestOfDeathList.contains(qs))
			_notifyQuestOfDeathList.add(qs);
	}
	
	/**
	 * Remove QuestState instance that is to be notified of Player's death.
	 * @param qs The QuestState that subscribe to this event
	 */
	public void removeNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null)
			return;
		
		_notifyQuestOfDeathList.remove(qs);
	}
	
	/**
	 * @return A list of QuestStates which registered for notify of death of this Player.
	 */
	public final List<QuestState> getNotifyQuestOfDeath()
	{
		return _notifyQuestOfDeathList;
	}
	
	/**
	 * @return player memos.
	 */
	public PlayerMemo getMemos()
	{
		return _vars;
	}
	
	/**
	 * @return A table containing all L2ShortCut of the Player.
	 */
	public L2ShortCut[] getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}
	
	/**
	 * @param slot The slot in wich the shortCuts is equipped
	 * @param page The page of shortCuts containing the slot
	 * @return The L2ShortCut of the Player corresponding to the position (page-slot).
	 */
	public L2ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}
	
	/**
	 * Add a L2shortCut to the Player _shortCuts
	 * @param shortcut The shortcut to add.
	 */
	public void registerShortCut(L2ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}
	
	/**
	 * Delete the L2ShortCut corresponding to the position (page-slot) from the Player _shortCuts.
	 * @param slot
	 * @param page
	 */
	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}
	
	/**
	 * Add a L2Macro to the Player _macroses.
	 * @param macro The Macro object to add.
	 */
	public void registerMacro(L2Macro macro)
	{
		_macroses.registerMacro(macro);
	}
	
	/**
	 * Delete the L2Macro corresponding to the Identifier from the Player _macroses.
	 * @param id
	 */
	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}
	
	/**
	 * @return all L2Macro of the Player.
	 */
	public MacroList getMacroses()
	{
		return _macroses;
	}
	
	/**
	 * Set the siege state of the Player.
	 * @param siegeState 1 = attacker, 2 = defender, 0 = not involved
	 */
	public void setSiegeState(byte siegeState)
	{
		_siegeState = siegeState;
	}
	
	/**
	 * @return the siege state of the Player.
	 */
	public byte getSiegeState()
	{
		return _siegeState;
	}
	
	/**
	 * Set the PvP Flag of the Player.
	 * @param pvpFlag 0 or 1.
	 */
	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = (byte) pvpFlag;
	}
	
	@Override
	public byte getPvpFlag()
	{
		return _pvpFlag;
	}
	
	public void updatePvPFlag(int value)
	{
		if (getPvpFlag() == value)
			return;
		
		setPvpFlag(value);
		sendPacket(new UserInfo(this));
		
		if (getPet() != null)
			sendPacket(new RelationChanged(getPet(), getRelation(this), false));
		
		broadcastRelationsChanges();
	}
	
	public int getRelation(Player target)
	{
		int result = 0;
		
		// karma and pvp may not be required
		if (getPvpFlag() != 0)
			result |= RelationChanged.RELATION_PVP_FLAG;
		if (getKarma() > 0)
			result |= RelationChanged.RELATION_HAS_KARMA;
		
		if (isClanLeader())
			result |= RelationChanged.RELATION_LEADER;
		
		if (getSiegeState() != 0)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			if (getSiegeState() != target.getSiegeState())
				result |= RelationChanged.RELATION_ENEMY;
			else
				result |= RelationChanged.RELATION_ALLY;
			if (getSiegeState() == 1)
				result |= RelationChanged.RELATION_ATTACKER;
		}
		
		if (getClan() != null && target.getClan() != null)
		{
			if (target.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPledgeType() != Clan.SUBUNIT_ACADEMY && target.getClan().isAtWarWith(getClan().getClanId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getClanId()))
					result |= RelationChanged.RELATION_MUTUAL_WAR;
			}
		}
		return result;
	}
	
	@Override
	public void revalidateZone(boolean force)
	{
		super.revalidateZone(force);
		
		if (Config.ALLOW_WATER)
		{
			if (isInsideZone(ZoneId.WATER))
				WaterTaskManager.getInstance().add(this);
			else
				WaterTaskManager.getInstance().remove(this);
		}
		
		if (isInsideZone(ZoneId.SIEGE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
				return;
			
			_lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2));
		}
		else if (isInsideZone(ZoneId.PVP))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
				return;
			
			_lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE));
		}
		else if (isIn7sDungeon())
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE)
				return;
			
			_lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE));
		}
		else if (isInsideZone(ZoneId.PEACE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
				return;
			
			_lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE));
		}
		else
		{
			if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
				return;
			
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
				updatePvPStatus();
			
			_lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
		}
	}
	
	/**
	 * @return the PK counter of the Player.
	 */
	public int getPkKills()
	{
		return _pkKills;
	}
	
	/**
	 * Set the PK counter of the Player.
	 * @param pkKills A number.
	 */
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	/**
	 * @return The _deleteTimer of the Player.
	 */
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}
	
	/**
	 * Set the _deleteTimer of the Player.
	 * @param deleteTimer Time in ms.
	 */
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}
	
	/**
	 * @return The current weight of the Player.
	 */
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}
	
	/**
	 * @return The number of recommandation obtained by the Player.
	 */
	public int getRecomHave()
	{
		return _recomHave;
	}
	
	/**
	 * Set the number of recommandations obtained by the Player (Max : 255).
	 * @param value Number of recommandations obtained.
	 */
	public void setRecomHave(int value)
	{
		_recomHave = MathUtil.limit(value, 0, 255);
	}
	
	/**
	 * Edit the number of recommandation obtained by the Player (Max : 255).
	 * @param value : The value to add or remove.
	 */
	public void editRecomHave(int value)
	{
		_recomHave = MathUtil.limit(_recomHave + value, 0, 255);
	}
	
	/**
	 * @return The number of recommandation that the Player can give.
	 */
	public int getRecomLeft()
	{
		return _recomLeft;
	}
	
	/**
	 * Set the number of givable recommandations by the {@link Player} (Max : 9).
	 * @param value : The number of recommendations a player can give.
	 */
	public void setRecomLeft(int value)
	{
		_recomLeft = MathUtil.limit(value, 0, 9);
	}
	
	/**
	 * Increment the number of recommandation that the Player can give.
	 */
	protected void decRecomLeft()
	{
		if (_recomLeft > 0)
			_recomLeft--;
	}
	
	public List<Integer> getRecomChars()
	{
		return _recomChars;
	}
	
	public void giveRecom(Player target)
	{
		target.editRecomHave(1);
		decRecomLeft();
		
		_recomChars.add(target.getObjectId());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement(ADD_CHAR_RECOM);
			ps.setInt(1, getObjectId());
			ps.setInt(2, target.getObjectId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement(UPDATE_TARGET_RECOM_HAVE);
			ps.setInt(1, target.getRecomHave());
			ps.setInt(2, target.getObjectId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement(UPDATE_CHAR_RECOM_LEFT);
			ps.setInt(1, getRecomLeft());
			ps.setInt(2, getObjectId());
			ps.execute();
			ps.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not update char recommendations: " + e);
		}
	}
	
	public boolean canRecom(Player target)
	{
		return !_recomChars.contains(target.getObjectId());
	}
	
	/**
	 * Set the exp of the Player before a death
	 * @param exp
	 */
	public void setExpBeforeDeath(long exp)
	{
		_expBeforeDeath = exp;
	}
	
	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}
	
	/**
	 * Return the Karma of the Player.
	 */
	@Override
	public int getKarma()
	{
		return _karma;
	}
	
	/**
	 * Set the Karma of the Player and send StatusUpdate (broadcast).
	 * @param karma A value.
	 */
	public void setKarma(int karma)
	{
		if (karma < 0)
			karma = 0;
		
		if (_karma > 0 && karma == 0)
		{
			sendPacket(new UserInfo(this));
			broadcastRelationsChanges();
		}
		
		// send message with new karma value
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1).addNumber(karma));
		
		_karma = karma;
		broadcastKarma();
	}
	
	/**
	 * Weight Limit = (CON Modifier*69000)*Skills
	 * @return The max weight that the Player can load.
	 */
	public int getMaxLoad()
	{
		int con = getCON();
		if (con < 1)
			return 31000;
		
		if (con > 59)
			return 176000;
		
		double baseLoad = Math.pow(1.029993928, con) * 30495.627366;
		return (int) calcStat(Stats.MAX_LOAD, baseLoad * Config.ALT_WEIGHT_LIMIT, this, null);
	}
	
	public int getExpertiseArmorPenalty()
	{
		return _expertiseArmorPenalty;
	}
	
	public boolean getExpertiseWeaponPenalty()
	{
		return _expertiseWeaponPenalty;
	}
	
	public int getWeightPenalty()
	{
		return _curWeightPenalty;
	}
	
	/**
	 * Update the overloaded status of the Player.
	 */
	public void refreshOverloaded()
	{
		int maxLoad = getMaxLoad();
		if (maxLoad > 0)
		{
			int weightproc = getCurrentLoad() * 1000 / maxLoad;
			int newWeightPenalty;
			
			if (weightproc < 500)
				newWeightPenalty = 0;
			else if (weightproc < 666)
				newWeightPenalty = 1;
			else if (weightproc < 800)
				newWeightPenalty = 2;
			else if (weightproc < 1000)
				newWeightPenalty = 3;
			else
				newWeightPenalty = 4;
			
			if (_curWeightPenalty != newWeightPenalty)
			{
				_curWeightPenalty = newWeightPenalty;
				
				if (newWeightPenalty > 0)
				{
					addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty), false);
					setIsOverloaded(getCurrentLoad() > maxLoad);
				}
				else
				{
					removeSkill(4270, false);
					setIsOverloaded(false);
				}
				
				sendPacket(new UserInfo(this));
				sendPacket(new EtcStatusUpdate(this));
				broadcastCharInfo();
			}
		}
	}
	
	/**
	 * Refresh expertise level ; weapon got one rank, when armor got 4 ranks.<br>
	 */
	public void refreshExpertisePenalty()
	{
		final int expertiseLevel = getSkillLevel(L2Skill.SKILL_EXPERTISE);
		
		int armorPenalty = 0;
		boolean weaponPenalty = false;
		
		for (ItemInstance item : getInventory().getPaperdollItems())
		{
			if (item.getItemType() != EtcItemType.ARROW && item.getItem().getCrystalType().getId() > expertiseLevel)
			{
				if (item.isWeapon())
					weaponPenalty = true;
				else
					armorPenalty += (item.getItem().getBodyPart() == Item.SLOT_FULL_ARMOR) ? 2 : 1;
			}
		}
		
		armorPenalty = Math.min(armorPenalty, 4);
		
		// Found a different state than previous ; update it.
		if (_expertiseWeaponPenalty != weaponPenalty || _expertiseArmorPenalty != armorPenalty)
		{
			_expertiseWeaponPenalty = weaponPenalty;
			_expertiseArmorPenalty = armorPenalty;
			
			// Passive skill "Grade Penalty" is either granted or dropped.
			if (_expertiseWeaponPenalty || _expertiseArmorPenalty > 0)
				addSkill(SkillTable.getInstance().getInfo(4267, 1), false);
			else
				removeSkill(4267, false);
			
			sendSkillList();
			sendPacket(new EtcStatusUpdate(this));
			
			final ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (weapon != null)
			{
				if (_expertiseWeaponPenalty)
					ItemPassiveSkillsListener.getInstance().onUnequip(0, weapon, this);
				else
					ItemPassiveSkillsListener.getInstance().onEquip(0, weapon, this);
			}
		}
	}
	
	/**
	 * Equip or unequip the item.
	 * <UL>
	 * <LI>If item is equipped, shots are applied if automation is on.</LI>
	 * <LI>If item is unequipped, shots are discharged.</LI>
	 * </UL>
	 * @param item The item to charge/discharge.
	 * @param abortAttack If true, the current attack will be aborted in order to equip the item.
	 */
	public void useEquippableItem(ItemInstance item, boolean abortAttack)
	{
		ItemInstance[] items = null;
		final boolean isEquipped = item.isEquipped();
		final int oldInvLimit = getInventoryLimit();
		
		if (item.getItem() instanceof Weapon)
			item.unChargeAllShots();
		
		if (isEquipped)
		{
			if (item.getEnchantLevel() > 0)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item));
			else
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item));
			
			items = getInventory().unEquipItemInBodySlotAndRecord(item);
		}
		else
		{
			items = getInventory().equipItemAndRecord(item);
			
			if (item.isEquipped())
			{
				if (item.getEnchantLevel() > 0)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED).addNumber(item.getEnchantLevel()).addItemName(item));
				else
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED).addItemName(item));
				
				if ((item.getItem().getBodyPart() & Item.SLOT_ALLWEAPON) != 0)
					rechargeShots(true, true);
			}
			else
				sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
		}
		refreshExpertisePenalty();
		broadcastUserInfo();
		
		InventoryUpdate iu = new InventoryUpdate();
		iu.addItems(Arrays.asList(items));
		sendPacket(iu);
		
		if (abortAttack)
			abortAttack();
		
		if (getInventoryLimit() != oldInvLimit)
			sendPacket(new ExStorageMaxCount(this));
	}
	
	/**
	 * @return PvP Kills of the Player (number of player killed during a PvP).
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}
	
	/**
	 * Set PvP Kills of the Player (number of player killed during a PvP).
	 * @param pvpKills A value.
	 */
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}
	
	/**
	 * @return The ClassId object of the Player contained in L2PcTemplate.
	 */
	public ClassId getClassId()
	{
		return getTemplate().getClassId();
	}
	
	/**
	 * Set the template of the Player.
	 * @param Id The Identifier of the L2PcTemplate to set to the Player
	 */
	public void setClassId(int Id)
	{
			if (isPhantom())
					return;
		
		if (!_subclassLock.tryLock())
			return;
		
		try
		{
			if (getLvlJoinedAcademy() != 0 && _clan != null && ClassId.VALUES[Id].level() == 2)
			{
				if (getLvlJoinedAcademy() <= 16)
					_clan.addReputationScore(400);
				else if (getLvlJoinedAcademy() >= 39)
					_clan.addReputationScore(170);
				else
					_clan.addReputationScore((400 - (getLvlJoinedAcademy() - 16) * 10));
				
				setLvlJoinedAcademy(0);
				
				// Oust pledge member from the academy, because he has finished his 2nd class transfer.
				_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()), SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(getName()));
				_clan.removeClanMember(getObjectId(), 0);
				sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
				
				// receive graduation gift : academy circlet
				addItem("Gift", 8181, 1, this, true);
			}
			
			if (isSubClassActive())
				_subClasses.get(_classIndex).setClassId(Id);
			
			broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0));
			setClassTemplate(Id);
			
			if (getClassId().level() == 3)
				sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
			else
				sendPacket(SystemMessageId.CLASS_TRANSFER);
			
			// Update class icon in party and clan
			if (_party != null)
				_party.broadcastPacket(new PartySmallWindowUpdate(this));
			
			if (_clan != null)
				_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
			
			if (Config.AUTO_LEARN_SKILLS)
				rewardSkills();
		}
		finally
		{
			_subclassLock.unlock();
		}
	}
	
	/**
	 * @return the Experience of the Player.
	 */
	public long getExp()
	{
		return getStat().getExp();
	}
	
	public void setActiveEnchantItem(ItemInstance scroll)
	{
		_activeEnchantItem = scroll;
	}
	
	public ItemInstance getActiveEnchantItem()
	{
		return _activeEnchantItem;
	}
	
	/**
	 * @return The Race object of the Player.
	 */
	public ClassRace getRace()
	{
		return (isSubClassActive()) ? getBaseTemplate().getRace() : getTemplate().getRace();
	}
	
	public L2Radar getRadar()
	{
		return _radar;
	}
	
	/**
	 * @return the SP amount of the Player.
	 */
	public int getSp()
	{
		return getStat().getSp();
	}
	
	/**
	 * @param castleId The castle to check.
	 * @return True if this Player is a clan leader in ownership of the passed castle.
	 */
	public boolean isCastleLord(int castleId)
	{
		final Clan clan = getClan();
		if (clan != null && clan.getLeader().getPlayerInstance() == this)
		{
			final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			return castle != null && castle.getCastleId() == castleId;
		}
		return false;
	}
	
	/**
	 * @return The Clan Identifier of the Player.
	 */
	public int getClanId()
	{
		return _clanId;
	}
	
	/**
	 * @return The Clan Crest Identifier of the Player or 0.
	 */
	public int getClanCrestId()
	{
		return (_clan != null) ? _clan.getCrestId() : 0;
	}
	
	/**
	 * @return The Clan CrestLarge Identifier or 0
	 */
	public int getClanCrestLargeId()
	{
		return (_clan != null) ? _clan.getCrestLargeId() : 0;
	}
	
	public long getClanJoinExpiryTime()
	{
		return _clanJoinExpiryTime;
	}
	
	public void setClanJoinExpiryTime(long time)
	{
		_clanJoinExpiryTime = time;
	}
	
	public long getClanCreateExpiryTime()
	{
		return _clanCreateExpiryTime;
	}
	
	public void setClanCreateExpiryTime(long time)
	{
		_clanCreateExpiryTime = time;
	}
	
	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}
	
	/**
	 * Return the PcInventory Inventory of the Player contained in _inventory.
	 */
	@Override
	public PcInventory getInventory()
	{
		return _inventory;
	}
	
	/**
	 * Delete a ShortCut of the Player _shortCuts.
	 * @param objectId The shortcut id.
	 */
	public void removeItemFromShortCut(int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}
	
	/**
	 * @return True if the Player is sitting.
	 */
	public boolean isSitting()
	{
		return _isSitting;
	}
	
	/**
	 * Set _isSitting to given value.
	 * @param state A boolean.
	 */
	public void setSitting(boolean state)
	{
		_isSitting = state;
	}
	
	/**
	 * Sit down the Player, set the AI Intention to REST and send ChangeWaitType packet (broadcast)
	 */
	public void sitDown()
	{
		sitDown(true);
	}
	
	public void sitDown(boolean checkCast)
	{
		if (checkCast && isCastingNow())
			return;
		
		if (!_isSitting && !isAttackingDisabled() && !isOutOfControl() && !isImmobilized())
		{
			breakAttack();
			setSitting(true);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			
			// Schedule a sit down task to wait for the animation to finish
			getAI().setIntention(CtrlIntention.REST);
			
			ThreadPool.schedule(() -> setIsParalyzed(false), 2500);
			setIsParalyzed(true);
		}
	}
	
	/**
	 * Stand up the Player, set the AI Intention to IDLE and send ChangeWaitType packet (broadcast)
	 */
	public void standUp()
	{
		if (_isSitting && !isInStoreMode() && !isAlikeDead() && !isParalyzed())
		{
			if (_effects.isAffected(L2EffectFlag.RELAXING))
				stopEffects(L2EffectType.RELAXING);
			
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			
			// Schedule a stand up task to wait for the animation to finish
			ThreadPool.schedule(() ->
			{
				setSitting(false);
				setIsParalyzed(false);
				getAI().setIntention(CtrlIntention.IDLE);
			}, 2500);
			setIsParalyzed(true);
		}
	}
	
	/**
	 * Stands up and close any opened shop window, if any.
	 */
	public void forceStandUp()
	{
		// Cancels any shop types.
		if (isInStoreMode())
		{
			setStoreType(StoreType.NONE);
			broadcastUserInfo();
		}
		
		// Stand up.
		standUp();
	}
	
	/**
	 * Used to sit or stand. If not possible, queue the action.
	 * @param target The target, used for thrones types.
	 * @param sittingState The sitting state, inheritated from packet or player status.
	 */
	public void tryToSitOrStand(final WorldObject target, final boolean sittingState)
	{
		if (isFakeDeath())
		{
			stopFakeDeath(true);
			return;
		}
		
		final boolean isThrone = target instanceof StaticObject && ((StaticObject) target).getType() == 1;
		
		// Player wants to sit on a throne but is out of radius, move to the throne delaying the sit action.
		if (isThrone && !sittingState && !isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false))
		{
			getAI().setIntention(CtrlIntention.MOVE_TO, new Location(target.getX(), target.getY(), target.getZ()));
			
			NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.MOVE_TO, () ->
			{
				if (getMountType() != 0)
					return;
				
				sitDown();
				
				if (!((StaticObject) target).isBusy())
				{
					_throneId = target.getObjectId();
					
					((StaticObject) target).setBusy(true);
					broadcastPacket(new ChairSit(getObjectId(), ((StaticObject) target).getStaticObjectId()));
				}
			});
			
			// Binding next action to AI.
			getAI().setNextAction(nextAction);
			return;
		}
		
		// Player isn't moving, sit directly.
		if (!isMoving())
		{
			if (getMountType() != 0)
				return;
			
			if (sittingState)
			{
				if (_throneId != 0)
				{
					final WorldObject object = World.getInstance().getObject(_throneId);
					if (object instanceof StaticObject)
						((StaticObject) object).setBusy(false);
					
					_throneId = 0;
				}
				
				standUp();
			}
			else
			{
				sitDown();
				
				if (isThrone && !((StaticObject) target).isBusy() && isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false))
				{
					_throneId = target.getObjectId();
					
					((StaticObject) target).setBusy(true);
					broadcastPacket(new ChairSit(getObjectId(), ((StaticObject) target).getStaticObjectId()));
				}
			}
		}
		// Player is moving, wait the current action is done, then sit.
		else
		{
			NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.MOVE_TO, () ->
			{
				if (getMountType() != 0)
					return;
				
				if (sittingState)
				{
					if (_throneId != 0)
					{
						final WorldObject object = World.getInstance().getObject(_throneId);
						if (object instanceof StaticObject)
							((StaticObject) object).setBusy(false);
						
						_throneId = 0;
					}
					
					standUp();
				}
				else
				{
					sitDown();
					
					if (isThrone && !((StaticObject) target).isBusy() && isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false))
					{
						_throneId = target.getObjectId();
						
						((StaticObject) target).setBusy(true);
						broadcastPacket(new ChairSit(getObjectId(), ((StaticObject) target).getStaticObjectId()));
					}
				}
			});
			
			// Binding next action to AI.
			getAI().setNextAction(nextAction);
		}
	}
	
	/**
	 * @return The PcWarehouse object of the Player.
	 */
	public PcWarehouse getWarehouse()
	{
		if (_warehouse == null)
		{
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}
		return _warehouse;
	}
	
	/**
	 * Free memory used by Warehouse
	 */
	public void clearWarehouse()
	{
		if (_warehouse != null)
			_warehouse.deleteMe();
		
		_warehouse = null;
	}
	
	/**
	 * @return The PcFreight object of the Player.
	 */
	public PcFreight getFreight()
	{
		if (_freight == null)
		{
			_freight = new PcFreight(this);
			_freight.restore();
		}
		return _freight;
	}
	
	/**
	 * Free memory used by Freight
	 */
	public void clearFreight()
	{
		if (_freight != null)
			_freight.deleteMe();
		
		_freight = null;
	}
	
	/**
	 * @param objectId The id of the owner.
	 * @return deposited PcFreight object for the objectId or create new if not existing.
	 */
	public PcFreight getDepositedFreight(int objectId)
	{
		for (PcFreight freight : _depositedFreight)
		{
			if (freight != null && freight.getOwnerId() == objectId)
				return freight;
		}
		
		PcFreight freight = new PcFreight(null);
		freight.doQuickRestore(objectId);
		_depositedFreight.add(freight);
		return freight;
	}
	
	/**
	 * Clear memory used by deposited freight
	 */
	public void clearDepositedFreight()
	{
		for (PcFreight freight : _depositedFreight)
		{
			if (freight != null)
				freight.deleteMe();
		}
		_depositedFreight.clear();
	}
	
	/**
	 * @return The Adena amount of the Player.
	 */
	public int getAdena()
	{
		return _inventory.getAdena();
	}
	
	/**
	 * @return The Ancient Adena amount of the Player.
	 */
	public int getAncientAdena()
	{
		return _inventory.getAncientAdena();
	}
	
	/**
	 * Add adena to Inventory of the Player and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param count int Quantity of adena to be added
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 */
	public void addAdena(String process, int count, WorldObject reference, boolean sendMessage)
	{
		if (sendMessage)
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addNumber(count));
		
		if (count > 0)
		{
			_inventory.addAdena(process, count, this, reference);
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_inventory.getAdenaInstance());
			sendPacket(iu);
		}
	}
	
	/**
	 * Reduce adena in Inventory of the Player and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param count int Quantity of adena to be reduced
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAdena(String process, int count, WorldObject reference, boolean sendMessage)
	{
		if (count > getAdena())
		{
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			
			return false;
		}
		
		if (count > 0)
		{
			ItemInstance adenaItem = _inventory.getAdenaInstance();
			if (!_inventory.reduceAdena(process, count, this, reference))
				return false;
			
			// Send update packet
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(adenaItem);
			sendPacket(iu);
			
			if (sendMessage)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA).addNumber(count));
		}
		return true;
	}
	
	/**
	 * Add ancient adena to Inventory of the Player and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param count int Quantity of ancient adena to be added
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 */
	public void addAncientAdena(String process, int count, WorldObject reference, boolean sendMessage)
	{
		if (sendMessage)
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(PcInventory.ANCIENT_ADENA_ID).addNumber(count));
		
		if (count > 0)
		{
			_inventory.addAncientAdena(process, count, this, reference);
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_inventory.getAncientAdenaInstance());
			sendPacket(iu);
		}
	}
	
	/**
	 * Reduce ancient adena in Inventory of the Player and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param count int Quantity of ancient adena to be reduced
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAncientAdena(String process, int count, WorldObject reference, boolean sendMessage)
	{
		if (count > getAncientAdena())
		{
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			
			return false;
		}
		
		if (count > 0)
		{
			ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
			if (!_inventory.reduceAncientAdena(process, count, this, reference))
				return false;
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(ancientAdenaItem);
			sendPacket(iu);
			
			if (sendMessage)
			{
				if (count > 1)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID).addItemNumber(count));
				else
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID));
			}
		}
		return true;
	}
	
	/**
	 * Adds item to inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param item ItemInstance to be added
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage)
	{
		if (item.getCount() > 0)
		{
			// Sends message to client if requested
			if (sendMessage)
			{
				if (item.getCount() > 1)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(item).addNumber(item.getCount()));
				else if (item.getEnchantLevel() > 0)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2).addNumber(item.getEnchantLevel()).addItemName(item));
				else
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item));
			}
			
			// Add the item to inventory
			ItemInstance newitem = _inventory.addItem(process, item, this, reference);
			
			// Send inventory update packet
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(newitem);
			sendPacket(playerIU);
			
			// Update current load as well
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
			
			// Cursed Weapon
			if (CursedWeaponManager.getInstance().isCursed(newitem.getItemId()))
				CursedWeaponManager.getInstance().activate(this, newitem);
			// If you pickup arrows and a bow is equipped, try to equip them if no arrows is currently equipped.
			else if (item.getItem().getItemType() == EtcItemType.ARROW && getAttackType() == WeaponType.BOW && getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
				checkAndEquipArrows();
		}
	}
	
	/**
	 * Adds item to Inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param itemId int Item Identifier of the item to be added
	 * @param count int Quantity of items to be added
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return The created ItemInstance.
	 */
	public ItemInstance addItem(String process, int itemId, int count, WorldObject reference, boolean sendMessage)
	{
		if (count > 0)
		{
			// Retrieve the template of the item.
			final Item item = ItemTable.getInstance().getTemplate(itemId);
			if (item == null)
			{
				_log.log(Level.SEVERE, "Item id " + itemId + "doesn't exist, so it can't be added.");
				return null;
			}
			
			// Sends message to client if requested.
			if (sendMessage && ((!isCastingNow() && item.getItemType() == EtcItemType.HERB) || item.getItemType() != EtcItemType.HERB))
			{
				if (count > 1)
				{
					if (process.equalsIgnoreCase("Sweep") || process.equalsIgnoreCase("Quest"))
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addItemNumber(count));
					else
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(itemId).addItemNumber(count));
				}
				else
				{
					if (process.equalsIgnoreCase("Sweep") || process.equalsIgnoreCase("Quest"))
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId));
					else
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(itemId));
				}
			}
			
			// If the item is herb type, dont add it to inventory.
			if (item.getItemType() == EtcItemType.HERB)
			{
				final ItemInstance herb = new ItemInstance(0, itemId);
				
				final IItemHandler handler = ItemHandler.getInstance().getHandler(herb.getEtcItem());
				if (handler != null)
					handler.useItem(this, herb, false);
			}
			else
			{
				// Add the item to inventory
				final ItemInstance createdItem = _inventory.addItem(process, itemId, count, this, reference);
				
				// Cursed Weapon
				if (CursedWeaponManager.getInstance().isCursed(createdItem.getItemId()))
					CursedWeaponManager.getInstance().activate(this, createdItem);
				// If you pickup arrows and a bow is equipped, try to equip them if no arrows is currently equipped.
				else if (item.getItemType() == EtcItemType.ARROW && getAttackType() == WeaponType.BOW && getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
					checkAndEquipArrows();
				
				return createdItem;
			}
		}
		return null;
	}
	
	/**
	 * Destroy item from inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param item ItemInstance to be destroyed
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage)
	{
		return destroyItem(process, item, item.getCount(), reference, sendMessage);
	}
	
	/**
	 * Destroy item from inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param item ItemInstance to be destroyed
	 * @param count int Quantity of ancient adena to be reduced
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, ItemInstance item, int count, WorldObject reference, boolean sendMessage)
	{
		item = _inventory.destroyItem(process, item, count, this, reference);
		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		// Send inventory update packet
		final InventoryUpdate iu = new InventoryUpdate();
		if (item.getCount() == 0)
			iu.addRemovedItem(item);
		else
			iu.addModifiedItem(item);
		sendPacket(iu);
		
		// Update current load as well
		final StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item).addItemNumber(count));
			else
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item));
		}
		return true;
	}
	
	/**
	 * Destroys item from inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param objectId int Item Instance identifier of the item to be destroyed
	 * @param count int Quantity of items to be destroyed
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage)
	{
		final ItemInstance item = _inventory.getItemByObjectId(objectId);
		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		return destroyItem(process, item, count, reference, sendMessage);
	}
	
	/**
	 * Destroys shots from inventory without logging and only occasional saving to database. Sends InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param objectId int Item Instance identifier of the item to be destroyed
	 * @param count int Quantity of items to be destroyed
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItemWithoutTrace(String process, int objectId, int count, WorldObject reference, boolean sendMessage)
	{
		ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if (item == null || item.getCount() < count)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		return destroyItem(null, item, count, reference, sendMessage);
	}
	
	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param itemId int Item identifier of the item to be destroyed
	 * @param count int Quantity of items to be destroyed
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage)
	{
		if (itemId == 57)
			return reduceAdena(process, count, reference, sendMessage);
		
		ItemInstance item = _inventory.getItemByItemId(itemId);
		
		if (item == null || item.getCount() < count || _inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		// Send inventory update packet
		InventoryUpdate playerIU = new InventoryUpdate();
		playerIU.addItem(item);
		sendPacket(playerIU);
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(count));
			else
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId));
		}
		return true;
	}
	
	/**
	 * Transfers item to another ItemContainer and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param objectId int Item Identifier of the item to be transfered
	 * @param count int Quantity of items to be transfered
	 * @param target Inventory the Inventory target.
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public ItemInstance transferItem(String process, int objectId, int count, Inventory target, WorldObject reference)
	{
		final ItemInstance oldItem = checkItemManipulation(objectId, count);
		if (oldItem == null)
			return null;
		
		final ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
			return null;
		
		// Send inventory update packet
		InventoryUpdate playerIU = new InventoryUpdate();
		
		if (oldItem.getCount() > 0 && oldItem != newItem)
			playerIU.addModifiedItem(oldItem);
		else
			playerIU.addRemovedItem(oldItem);
		
		sendPacket(playerIU);
		
		// Update current load as well
		StatusUpdate playerSU = new StatusUpdate(this);
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);
		
		// Send target update packet
		if (target instanceof PcInventory)
		{
			final Player targetPlayer = ((PcInventory) target).getOwner();
			
			InventoryUpdate playerIU2 = new InventoryUpdate();
			if (newItem.getCount() > count)
				playerIU2.addModifiedItem(newItem);
			else
				playerIU2.addNewItem(newItem);
			targetPlayer.sendPacket(playerIU2);
			
			// Update current load as well
			playerSU = new StatusUpdate(targetPlayer);
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();
			if (newItem.getCount() > count)
				petIU.addModifiedItem(newItem);
			else
				petIU.addNewItem(newItem);
			((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
		}
		return newItem;
	}
	
	/**
	 * Drop item from inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param item ItemInstance to be dropped
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean dropItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage)
	{
		item = _inventory.dropItem(process, item, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		item.dropMe(this, getX() + Rnd.get(-25, 25), getY() + Rnd.get(-25, 25), getZ() + 20);
		
		// Send inventory update packet
		InventoryUpdate playerIU = new InventoryUpdate();
		playerIU.addItem(item);
		sendPacket(playerIU);
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));
		
		return true;
	}
	
	/**
	 * Drop item from inventory by using its <B>objectID</B> and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param objectId int Item Instance identifier of the item to be dropped
	 * @param count int Quantity of items to be dropped
	 * @param x int coordinate for drop X
	 * @param y int coordinate for drop Y
	 * @param z int coordinate for drop Z
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public ItemInstance dropItem(String process, int objectId, int count, int x, int y, int z, WorldObject reference, boolean sendMessage)
	{
		ItemInstance invItem = _inventory.getItemByObjectId(objectId);
		ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return null;
		}
		
		item.dropMe(this, x, y, z);
		
		// Send inventory update packet
		InventoryUpdate playerIU = new InventoryUpdate();
		playerIU.addItem(invItem);
		sendPacket(playerIU);
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));
		
		return item;
	}
	
	public ItemInstance checkItemManipulation(int objectId, int count)
	{
		if (World.getInstance().getObject(objectId) == null)
			return null;
		
		final ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null || item.getOwnerId() != getObjectId())
			return null;
		
		if (count < 1 || (count > 1 && !item.isStackable()))
			return null;
		
		if (count > item.getCount())
			return null;
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || _mountObjectId == objectId)
			return null;
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
			return null;
		
		// We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
		if (item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow()))
			return null;
		
		return item;
	}
	
	/**
	 * Launch a task corresponding to Config time.
	 * @param protect boolean Drop timer or activate it.
	 */
	public void setSpawnProtection(boolean protect)
	{
		if (protect && !isPhantom())
		{
			if (_protectTask == null)
				_protectTask = ThreadPool.schedule(() ->
				{
					setSpawnProtection(false);
					sendMessage("The spawn protection has ended.");
				}, Config.PLAYER_SPAWN_PROTECTION * 1000);
		}
		else
		{
			_protectTask.cancel(true);
			_protectTask = null;
		}
		broadcastUserInfo();
	}
	
	public boolean isSpawnProtected()
	{
		return _protectTask != null;
	}
	
	/**
	 * Set protection from agro mobs when getting up from fake death, according settings.
	 */
	public void setRecentFakeDeath()
	{
		_recentFakeDeathEndTime = System.currentTimeMillis() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * 1000;
	}
	
	public void clearRecentFakeDeath()
	{
		_recentFakeDeathEndTime = 0;
	}
	
	public boolean isRecentFakeDeath()
	{
		return _recentFakeDeathEndTime > System.currentTimeMillis();
	}
	
	public final boolean isFakeDeath()
	{
		return _isFakeDeath;
	}
	
	public final void setIsFakeDeath(boolean value)
	{
		_isFakeDeath = value;
	}
	
	@Override
	public final boolean isAlikeDead()
	{
		if (super.isAlikeDead())
			return true;
		
		return isFakeDeath();
	}
	
	/**
	 * @return The client owner of this char.
	 */
	public L2GameClient getClient()
	{
		return _client;
	}
	
	public void setClient(L2GameClient client)
	{
		_client = client;
	}
	
	public String getAccountName()
	{
		return _client.getAccountName();
	}
	
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}
	
	/**
	 * Close the active connection with the client.
	 * @param closeClient
	 */
	private void closeNetConnection(boolean closeClient)
	{
		L2GameClient client = _client;
		if (client != null)
		{
			if (client.isDetached() && !isPhantom())
				client.cleanMe(true);
			else
			{
				if (!client.getConnection().isClosed())
				{
					if (closeClient)
						client.close(LeaveWorld.STATIC_PACKET);
					else
						client.close(ServerClose.STATIC_PACKET);
				}
			}
		}
	}
	
	public Location getCurrentSkillWorldPosition()
	{
		return _currentSkillWorldPosition;
	}
	
	public void setCurrentSkillWorldPosition(Location worldPosition)
	{
		_currentSkillWorldPosition = worldPosition;
	}
	
	/**
	 * @see net.sf.l2j.gameserver.model.actor.Creature#enableSkill(net.sf.l2j.gameserver.model.L2Skill)
	 */
	@Override
	public void enableSkill(L2Skill skill)
	{
		super.enableSkill(skill);
		_reuseTimeStamps.remove(skill.getReuseHashCode());
	}
	
	@Override
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if (!super.checkDoCastConditions(skill))
			return false;
		
		// Can't summon multiple servitors.
		if (skill.getSkillType() == L2SkillType.SUMMON)
		{
			if (!((L2SkillSummon) skill).isCubic() && (getPet() != null || isMounted()))
			{
				sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
				return false;
			}
		}
		// Can't use ressurect skills on siege if you are defender and control towers is not alive, if you are attacker and flag isn't spawned or if you aren't part of that siege.
		else if (skill.getSkillType() == L2SkillType.RESURRECT)
		{
			final Siege siege = CastleManager.getInstance().getActiveSiege(this);
			if (siege != null)
			{
				if (getClan() == null)
				{
					sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
					return false;
				}
				
				final SiegeSide side = siege.getSide(getClan());
				if (side == SiegeSide.DEFENDER || side == SiegeSide.OWNER)
				{
					if (siege.getControlTowerCount() == 0)
					{
						sendPacket(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION);
						return false;
					}
				}
				else if (side == SiegeSide.ATTACKER)
				{
					if (getClan().getFlag() == null)
					{
						sendPacket(SystemMessageId.NO_RESURRECTION_WITHOUT_BASE_CAMP);
						return false;
					}
				}
				else
				{
					sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
					return false;
				}
			}
		}
		// Can't casting signets on peace zone.
		else if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME)
		{
			final WorldRegion region = getRegion();
			if (region == null)
				return false;
			
			if (!region.checkEffectRangeInsidePeaceZone(skill, (skill.getTargetType() == SkillTargetType.TARGET_GROUND) ? getCurrentSkillWorldPosition() : getPosition()))
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
		}
		
		// Can't use Hero and resurrect skills during Olympiad
		if (isInOlympiadMode() && (skill.isHeroSkill() || skill.getSkillType() == L2SkillType.RESURRECT))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}
		
		// Check if the spell uses charges
		if (skill.getMaxCharges() == 0 && getCharges() < skill.getNumCharges())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
			return false;
		}
		
		return true;
	}
	
	@Override
	public void onAction(Player player)
	{
		if (!TvTEvent.onAction(player, getObjectId()))
			return;
		
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Check if this Player has a Private Store
			if (isInStoreMode())
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
				return;
			}
			
			// Check if this Player is autoAttackable
			if (isAutoAttackable(player))
			{
				// Player with lvl < 21 can't attack a cursed weapon holder and a cursed weapon holder can't attack players with lvl < 21
				if ((isCursedWeaponEquipped() && player.getLevel() < 21) || (player.isCursedWeaponEquipped() && getLevel() < 21))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (GeoEngine.getInstance().canSeeTarget(player, this))
				{
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
					player.onActionRequest();
				}
			}
			else
			{
				// avoids to stuck when clicking two or more times
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				if (player != this && GeoEngine.getInstance().canSeeTarget(player, this))
					player.getAI().setIntention(CtrlIntention.FOLLOW, this);
			}
		}
	}
	
	@Override
	public void onActionShift(Player player)
	{
		if (player.isGM())
			AdminEditChar.showCharacterInfo(player, this);
		
		
		super.onActionShift(player);
	}
	
	/**
	 * @param barPixels
	 * @return true if cp update should be done, false if not
	 */
	private boolean needCpUpdate(int barPixels)
	{
		double currentCp = getCurrentCp();
		
		if (currentCp <= 1.0 || getMaxCp() < barPixels)
			return true;
		
		if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck)
		{
			if (currentCp == getMaxCp())
			{
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param barPixels
	 * @return true if mp update should be done, false if not
	 */
	private boolean needMpUpdate(int barPixels)
	{
		double currentMp = getCurrentMp();
		
		if (currentMp <= 1.0 || getMaxMp() < barPixels)
			return true;
		
		if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck)
		{
			if (currentMp == getMaxMp())
			{
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the Player and only current HP, MP and Level to all other Player of the Party.
	 * <ul>
	 * <li>Send StatusUpdate with current HP, MP and CP to this Player</li>
	 * <li>Send PartySmallWindowUpdate with current HP, MP and Level to all other Player of the Party</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all Player of the _statusListener</B></FONT>
	 */
	@Override
	public void broadcastStatusUpdate()
	{
		// Send StatusUpdate with current HP, MP and CP to this Player
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		sendPacket(su);
		
		final boolean needCpUpdate = needCpUpdate(352);
		final boolean needHpUpdate = needHpUpdate(352);
		
		// Check if a party is in progress and party window update is needed.
		if (_party != null && (needCpUpdate || needHpUpdate || needMpUpdate(352)))
			_party.broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		
		if (isInOlympiadMode() && isOlympiadStart() && (needCpUpdate || needHpUpdate))
		{
			final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(getOlympiadGameId());
			if (game != null && game.isBattleStarted())
				game.getZone().broadcastStatusUpdate(this);
		}
		
		// In duel, MP updated only with CP or HP
		if (isInDuel() && (needCpUpdate || needHpUpdate))
		{
			ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
			DuelManager.getInstance().broadcastToOppositeTeam(this, update);
		}
	}
	
	/**
	 * Broadcast informations from a user to himself and his knownlist.<BR>
	 * If player is morphed, it sends informations from the template the player is using.
	 * <ul>
	 * <li>Send a UserInfo packet (public and private data) to this Player.</li>
	 * <li>Send a CharInfo packet (public data only) to Player's knownlist.</li>
	 * </ul>
	 */
	public final void broadcastUserInfo()
	{
		sendPacket(new UserInfo(this));
		
		if (getPolyType() == PolyType.NPC)
			Broadcast.toKnownPlayers(this, new AbstractNpcInfo.PcMorphInfo(this, getPolyTemplate()));
		else
			broadcastCharInfo();
	}
	
	public final void broadcastCharInfo()
	{
		for (Player player : getKnownType(Player.class))
		{
			player.sendPacket(new CharInfo(this));
			
			final int relation = getRelation(player);
			final boolean isAutoAttackable = isAutoAttackable(player);
			
			player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
			if (getPet() != null)
				player.sendPacket(new RelationChanged(getPet(), relation, isAutoAttackable));
		}
	}
	
	/**
	 * Broadcast player title information.
	 */
	public final void broadcastTitleInfo()
	{
		sendPacket(new UserInfo(this));
		broadcastPacket(new TitleUpdate(this));
	}
	
	/**
	 * @return the Alliance Identifier of the Player.
	 */
	public int getAllyId()
	{
		if (_clan == null)
			return 0;
		
		return _clan.getAllyId();
	}
	
	public int getAllyCrestId()
	{
		if (getClanId() == 0)
			return 0;
		
		if (getClan().getAllyId() == 0)
			return 0;
		
		return getClan().getAllyCrestId();
	}
	
	/**
	 * Send a packet to the Player.
	 */
	@Override
	public void sendPacket(L2GameServerPacket packet)
	{
		if (_client != null)
			_client.sendPacket(packet);
	}
	
	/**
	 * Send SystemMessage packet.
	 * @param id SystemMessageId
	 */
	@Override
	public void sendPacket(SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}
	
	/**
	 * Manage Interact Task with another Player.<BR>
	 * Turn the character in front of the target.<BR>
	 * In case of private stores, send the related packet.
	 * @param target The Creature targeted
	 */
	public void doInteract(Creature target)
	{
		if (target instanceof Player)
		{
			Player temp = (Player) target;
			sendPacket(new MoveToPawn(this, temp, Npc.INTERACTION_DISTANCE));
			
			switch (temp.getStoreType())
			{
				case SELL:
				case PACKAGE_SELL:
					sendPacket(new PrivateStoreListSell(this, temp));
					break;
				
				case BUY:
					sendPacket(new PrivateStoreListBuy(this, temp));
					break;
				
				case MANUFACTURE:
					sendPacket(new RecipeShopSellList(this, temp));
					break;
			}
		}
		else
		{
			// _interactTarget=null should never happen but one never knows ^^;
			if (target != null)
				target.onAction(this);
		}
	}
	
	/**
	 * Manage AutoLoot Task.
	 * <ul>
	 * <li>Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the Player inventory</li>
	 * <li>Send InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send StatusUpdate to this Player with current weight</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT>
	 * @param target The reference Object.
	 * @param item The dropped ItemHolder.
	 */
	public void doAutoLoot(Attackable target, IntIntHolder item)
	{
		if (isInParty())
			getParty().distributeItem(this, item, false, target);
		else if (item.getId() == 57)
			addAdena("Loot", item.getValue(), target, true);
		else
			addItem("Loot", item.getId(), item.getValue(), target, true);
	}
	
	/**
	 * Manage Pickup Task.
	 * <ul>
	 * <li>Send StopMove to this Player</li>
	 * <li>Remove the ItemInstance from the world and send GetItem packets</li>
	 * <li>Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the Player inventory</li>
	 * <li>Send InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send StatusUpdate to this Player with current weight</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT>
	 * @param object The ItemInstance to pick up
	 */
	@Override
	public void doPickupItem(WorldObject object)
	{
		if (isAlikeDead() || isFakeDeath())
			return;
		
		// Set the AI Intention to IDLE
		getAI().setIntention(CtrlIntention.IDLE);
		
		// Check if the WorldObject to pick up is a ItemInstance
		if (!(object instanceof ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_log.warning(getName() + " tried to pickup a wrong target: " + object);
			return;
		}
		
		ItemInstance item = (ItemInstance) object;
		
		// Send ActionFailed to this Player
		sendPacket(ActionFailed.STATIC_PACKET);
		sendPacket(new StopMove(this));
		
		synchronized (item)
		{
			if (!item.isVisible())
				return;
			
			if (isInStoreMode())
				return;
			
			if (!_inventory.validateWeight(item.getCount() * item.getItem().getWeight()))
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
				return;
			}
			
			if (((isInParty() && getParty().getLootRule() == LootRule.ITEM_LOOTER) || !isInParty()) && !_inventory.validateCapacity(item))
			{
				sendPacket(SystemMessageId.SLOTS_FULL);
				return;
			}
			
			if (getActiveTradeList() != null)
			{
				sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
				return;
			}
			
			if (item.getOwnerId() != 0 && !isLooterOrInLooterParty(item.getOwnerId()))
			{
				if (item.getItemId() == 57)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(item.getCount()));
				else if (item.getCount() > 1)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(item).addNumber(item.getCount()));
				else
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item));
				
				return;
			}
			
			if (item.hasDropProtection())
				item.removeDropProtection();
			
			// Remove the ItemInstance from the world and send GetItem packets
			item.pickupMe(this);
			
			// item must be removed from ItemsOnGroundManager if is active
			ItemsOnGroundTaskManager.getInstance().remove(item);
		}
		
		// Auto use herbs - pick up
		if (item.getItemType() == EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if (handler != null)
				handler.useItem(this, item, false);
			
			ItemTable.getInstance().destroyItem("Consume", item, this, null);
		}
		// Cursed Weapons are not distributed
		else if (CursedWeaponManager.getInstance().isCursed(item.getItemId()))
		{
			addItem("Pickup", item, null, true);
		}
		else
		{
			// if item is instance of L2ArmorType or WeaponType broadcast an "Attention" system message
			if (item.getItemType() instanceof ArmorType || item.getItemType() instanceof WeaponType)
			{
				SystemMessage msg;
				if (item.getEnchantLevel() > 0)
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3).addString(getName()).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
				else
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2).addString(getName()).addItemName(item.getItemId());
				
				broadcastPacket(msg, 1400);
			}
			
			// Check if a Party is in progress
			if (isInParty())
				getParty().distributeItem(this, item);
			// Target is adena
			else if (item.getItemId() == 57 && getInventory().getAdenaInstance() != null)
			{
				addAdena("Pickup", item.getCount(), null, true);
				ItemTable.getInstance().destroyItem("Pickup", item, this, null);
			}
			// Target is regular item
			else
				addItem("Pickup", item, null, true);
		}
		
		// Schedule a paralyzed task to wait for the animation to finish
		ThreadPool.schedule(() -> setIsParalyzed(false), (int) (700 / getStat().getMovementSpeedMultiplier()));
		setIsParalyzed(true);
	}
	
	@Override
	public void doAttack(Creature target)
	{
		super.doAttack(target);
		clearRecentFakeDeath();
	}
	
	@Override
	public void doCast(L2Skill skill)
	{
		super.doCast(skill);
		clearRecentFakeDeath();
	}
	
	public boolean canOpenPrivateStore()
	{
		if (getActiveTradeList() != null)
			cancelActiveTrade();
		
		return !isAlikeDead() && !isInOlympiadMode() && !isMounted() && !isInsideZone(ZoneId.NO_STORE) && !isCastingNow();
	}
	
	public void tryOpenPrivateBuyStore()
	{
		if (canOpenPrivateStore())
		{
			if (getStoreType() == StoreType.BUY || getStoreType() == StoreType.BUY_MANAGE)
				setStoreType(StoreType.NONE);
			
			if (getStoreType() == StoreType.NONE)
			{
				standUp();
				
				setStoreType(StoreType.BUY_MANAGE);
				sendPacket(new PrivateStoreManageListBuy(this));
			}
		}
		else
		{
			if (isInsideZone(ZoneId.NO_STORE))
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public void tryOpenPrivateSellStore(boolean isPackageSale)
	{
		if (canOpenPrivateStore())
		{
			if (getStoreType() == StoreType.SELL || getStoreType() == StoreType.SELL_MANAGE || getStoreType() == StoreType.PACKAGE_SELL)
				setStoreType(StoreType.NONE);
			
			if (getStoreType() == StoreType.NONE)
			{
				standUp();
				
				setStoreType(StoreType.SELL_MANAGE);
				sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
			}
		}
		else
		{
			if (isInsideZone(ZoneId.NO_STORE))
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public void tryOpenWorkshop(boolean isDwarven)
	{
		if (canOpenPrivateStore())
		{
			if (isInStoreMode())
				setStoreType(StoreType.NONE);
			
			if (getStoreType() == StoreType.NONE)
			{
				standUp();
				
				if (getCreateList() == null)
					setCreateList(new ManufactureList());
				
				sendPacket(new RecipeShopManageList(this, isDwarven));
			}
		}
		else
		{
			if (isInsideZone(ZoneId.NO_STORE))
				sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public final PreparedListContainer getMultiSell()
	{
		return _currentMultiSell;
	}
	
	public final void setMultiSell(PreparedListContainer list)
	{
		_currentMultiSell = list;
	}
	
	@Override
	public void setTarget(WorldObject newTarget)
	{
		// Check if the new target is visible.
		if (newTarget != null && !newTarget.isVisible() && !(newTarget instanceof Player && isInParty() && _party.containsPlayer(newTarget)))
			newTarget = null;
		
		// Can't target and attack festival monsters if not participant
		if ((newTarget instanceof FestivalMonster) && !isFestivalParticipant())
			newTarget = null;
		// Can't target and attack rift invaders if not in the same room
		else if (isInParty() && getParty().isInDimensionalRift())
		{
			byte riftType = getParty().getDimensionalRift().getType();
			byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();
			
			if (newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
				newTarget = null;
		}
		
		// Get the current target
		WorldObject oldTarget = getTarget();
		
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
				return; // no target change
				
			// Remove the Player from the _statusListener of the old target if it was a Creature
			if (oldTarget instanceof Creature)
				((Creature) oldTarget).removeStatusListener(this);
		}
		
		// Verify if it's a static object.
		if (newTarget instanceof StaticObject)
		{
			sendPacket(new MyTargetSelected(newTarget.getObjectId(), 0));
			sendPacket(new StaticObjectInfo((StaticObject) newTarget));
		}
		// Add the Player to the _statusListener of the new target if it's a Creature
		else if (newTarget instanceof Creature)
		{
			final Creature target = (Creature) newTarget;
			
			// Validate location of the new target.
			if (newTarget.getObjectId() != getObjectId())
				sendPacket(new ValidateLocation(target));
			
			// Show the client his new target.
			sendPacket(new MyTargetSelected(target.getObjectId(), (target.isAutoAttackable(this) || target instanceof Summon) ? getLevel() - target.getLevel() : 0));
			
			target.addStatusListener(this);
			
			// Send max/current hp.
			final StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.MAX_HP, target.getMaxHp());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			sendPacket(su);
			
			Broadcast.toKnownPlayers(this, new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ()));
		}
		
		if (newTarget instanceof Folk)
			setCurrentFolk((Folk) newTarget);
		else if (newTarget == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			
			if (getTarget() != null)
			{
				broadcastPacket(new TargetUnselected(this));
				setCurrentFolk(null);
			}
		}
		
		// Target the new WorldObject
		super.setTarget(newTarget);
	}
	
	/**
	 * Return the active weapon instance (always equipped in the right hand).
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	/**
	 * Return the active weapon item (always equipped in the right hand).
	 */
	@Override
	public Weapon getActiveWeaponItem()
	{
		final ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		return (weapon == null) ? getTemplate().getFists() : (Weapon) weapon.getItem();
	}
	
	/**
	 * @return the type of attack, depending of the worn weapon.
	 */
	@Override
	public WeaponType getAttackType()
	{
		return getActiveWeaponItem().getItemType();
	}
	
	/**
	 * @param type : The ArmorType to check. It supports NONE, SHIELD, MAGIC, LIGHT and HEAVY.
	 * @return true if the given ArmorType is used by the chest of the player, false otherwise.
	 */
	public boolean isWearingArmorType(ArmorType type)
	{
		// Retrieve either the shield or the chest, following ArmorType to check.
		final ItemInstance armor = getInventory().getPaperdollItem((type == ArmorType.SHIELD) ? Inventory.PAPERDOLL_LHAND : Inventory.PAPERDOLL_CHEST);
		if (armor == null)
			return type == ArmorType.NONE; // Return true if not equipped and the check was based on NONE ArmorType.
			
		// Test if the equipped item is an armor, then finally compare both ArmorType.
		return armor.getItemType() instanceof ArmorType && armor.getItemType() == type;
	}
	
	public boolean isUnderMarryRequest()
	{
		return _isUnderMarryRequest;
	}
	
	public void setUnderMarryRequest(boolean state)
	{
		_isUnderMarryRequest = state;
	}
	
	public int getCoupleId()
	{
		return _coupleId;
	}
	
	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}
	
	public void setRequesterId(int requesterId)
	{
		_requesterId = requesterId;
	}
	
	public void engageAnswer(int answer)
	{
		if (!_isUnderMarryRequest || _requesterId == 0)
			return;
		
		final Player requester = World.getInstance().getPlayer(_requesterId);
		if (requester != null)
		{
			if (answer == 1)
			{
				// Create the couple
				CoupleManager.getInstance().addCouple(requester, this);
				
				// Then "finish the job"
				WeddingManagerNpc.justMarried(requester, this);
			}
			else
			{
				setUnderMarryRequest(false);
				sendMessage("You declined your partner's marriage request.");
				
				requester.setUnderMarryRequest(false);
				requester.sendMessage("Your partner declined your marriage request.");
			}
		}
	}
	
	/**
	 * Return the secondary weapon instance (always equipped in the left hand).
	 */
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	
	/**
	 * Return the secondary L2Item item (always equiped in the left hand).
	 */
	@Override
	public Item getSecondaryWeaponItem()
	{
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (item != null)
			return item.getItem();
		
		return null;
	}
	
	/**
	 * Kill the Creature, Apply Death Penalty, Manage gain/loss Karma and Item Drop.
	 * <ul>
	 * <li>Reduce the Experience of the Player in function of the calculated Death Penalty</li>
	 * <li>If necessary, unsummon the Pet of the killed Player</li>
	 * <li>Manage Karma gain for attacker and Karam loss for the killed Player</li>
	 * <li>If the killed Player has Karma, manage Drop Item</li>
	 * <li>Kill the Player</li>
	 * </ul>
	 * @param killer The Creature who attacks
	 */
	@Override
	public boolean doDie(Creature killer)
	{
		// Kill the Player
		if (!super.doDie(killer))
			return false;
		
		if (isMounted())
			stopFeed();

		
		synchronized (this)
		{
			if (isFakeDeath())
				stopFakeDeath(true);
		}
		
		
		if (killer != null)
		{
			
			TvTEvent.onKill(killer, this);
			
			Player pk = killer.getActingPlayer();
			
						if (isPhantom())
							{
								ThreadPool.schedule(new Runnable()
								{
									@Override
									public void run()
									{
										doRevive();
										starTeleport();
										
										ThreadPool.schedule(new Runnable()
										{
											@Override
											public void run()
											{
												starAttack();
											}
										}, 1500);
									}
								}, 4500);
							}
			
			// Clear resurrect xp calculation
			setExpBeforeDeath(0);
			
			if (isCursedWeaponEquipped())
				CursedWeaponManager.getInstance().drop(_cursedWeaponEquippedId, killer);
			else
			{
				if (pk == null || !pk.isCursedWeaponEquipped())
				{
					onDieDropItem(killer); // Check if any item should be dropped
					
					// if the area isn't an arena
					if (!isInArena())
					{
						// if both victim and attacker got clans & aren't academicians
						if (pk != null && pk.getClan() != null && getClan() != null && !isAcademyMember() && !pk.isAcademyMember())
						{
							// if clans got mutual war, then use the reputation calcul
							if (_clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(_clan.getClanId()))
							{
								// when your reputation score is 0 or below, the other clan cannot acquire any reputation points
								if (getClan().getReputationScore() > 0)
									pk.getClan().addReputationScore(1);
								// when the opposing sides reputation score is 0 or below, your clans reputation score doesn't decrease
								if (pk.getClan().getReputationScore() > 0)
									_clan.takeReputationScore(1);
							}
						}
					}
					
					// Reduce player's xp and karma.
					if (Config.ALT_GAME_DELEVEL && (!hasSkill(L2Skill.SKILL_LUCKY) || getStat().getLevel() > 9))
						deathPenalty(pk != null && getClan() != null && pk.getClan() != null && (getClan().isAtWarWith(pk.getClanId()) || pk.getClan().isAtWarWith(getClanId())), pk != null, killer instanceof SiegeGuard);
				}
			}
		}
		
		// Unsummon Cubics
		if (!_cubics.isEmpty())
		{
			for (Cubic cubic : _cubics.values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			
			_cubics.clear();
		}
		
		if (_fusionSkill != null)
			abortCast();
		
		for (Creature character : getKnownType(Creature.class))
			if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
				character.abortCast();
			
		// calculate death penalty buff
		calculateDeathPenaltyBuffLevel(killer);
		
		WaterTaskManager.getInstance().remove(this);
		
		if (isPhoenixBlessed() || (isAffected(L2EffectFlag.CHARM_OF_COURAGE) && isInSiege()))
			reviveRequest(this, null, false);
		
		// Icons update in order to get retained buffs list
		updateEffectIcons();
		
		return true;
	}
	
	private void onDieDropItem(Creature killer)
	{
		if (killer == null)
			return;
		
		Player pk = killer.getActingPlayer();
		if (getKarma() <= 0 && pk != null && pk.getClan() != null && getClan() != null && pk.getClan().isAtWarWith(getClanId()))
			return;
		
		if ((!isInsideZone(ZoneId.PVP) || pk == null) && (!isGM() || Config.KARMA_DROP_GM))
		{
			boolean isKillerNpc = (killer instanceof Npc);
			int pkLimit = Config.KARMA_PK_LIMIT;
			
			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;
			
			if (getKarma() > 0 && getPkKills() >= pkLimit)
			{
				dropPercent = Config.KARMA_RATE_DROP;
				dropEquip = Config.KARMA_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.KARMA_RATE_DROP_ITEM;
				dropLimit = Config.KARMA_DROP_LIMIT;
			}
			else if (isKillerNpc && getLevel() > 4 && !isFestivalParticipant())
			{
				dropPercent = Config.PLAYER_RATE_DROP;
				dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.PLAYER_RATE_DROP_ITEM;
				dropLimit = Config.PLAYER_DROP_LIMIT;
			}
			
			if (dropPercent > 0 && Rnd.get(100) < dropPercent)
			{
				int dropCount = 0;
				int itemDropPercent = 0;
				
				for (ItemInstance itemDrop : getInventory().getItems())
				{
					// Don't drop those following things
					if (!itemDrop.isDropable() || itemDrop.isShadowItem() || itemDrop.getItemId() == 57 || itemDrop.getItem().getType2() == Item.TYPE2_QUEST || getPet() != null && getPet().getControlItemId() == itemDrop.getItemId() || Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_ITEMS, itemDrop.getItemId()) >= 0 || Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.getItemId()) >= 0)
						continue;
					
					if (itemDrop.isEquipped())
					{
						// Set proper chance according to Item type of equipped Item
						itemDropPercent = itemDrop.getItem().getType2() == Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
						getInventory().unEquipItemInSlot(itemDrop.getLocationSlot());
					}
					else
						itemDropPercent = dropItem; // Item in inventory
						
					// NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
					if (Rnd.get(100) < itemDropPercent)
					{
						dropItem("DieDrop", itemDrop, killer, true);
						
						if (++dropCount >= dropLimit)
							break;
					}
				}
			}
		}
	}
	
	public void updateKarmaLoss(long exp)
	{
		if (!isCursedWeaponEquipped() && getKarma() > 0)
		{
			int karmaLost = Formulas.calculateKarmaLost(getLevel(), exp);
			if (karmaLost > 0)
				setKarma(getKarma() - karmaLost);
		}
	}
	
	/**
	 * This method is used to update PvP counter, or PK counter / add Karma if necessary.<br>
	 * It also updates clan kills/deaths counters on siege.
	 * @param target The L2Playable victim.
	 */
	public void onKillUpdatePvPKarma(Playable target)
	{
		if (target == null)
			return;
		
		final Player targetPlayer = target.getActingPlayer();
		if (targetPlayer == null || targetPlayer == this)
			return;
		
		// Don't rank up the CW if it was a summon.
		if (isCursedWeaponEquipped() && target instanceof Player)
		{
			CursedWeaponManager.getInstance().increaseKills(_cursedWeaponEquippedId);
			return;
		}
		
		// If in duel and you kill (only can kill l2summon), do nothing
		if (isInDuel() && targetPlayer.isInDuel())
			return;
		
		// Show HP PvP
		if (Config.SHOW_HP_PVP)
		{
			targetPlayer.sendPacket(new ExShowScreenMessage("You Killed By " + getName() + " Left [HP - " + getCurrentShowHpPvp() + "] [CP - " + getCurrentShowCpPvp() + "]", 4000));
			targetPlayer.sendMessage("You Killed By " + getName() + " Left [HP - " + getCurrentShowHpPvp() + "] [CP - " + getCurrentShowCpPvp() + "]");

		}
		
		// If in pvp zone, do nothing.
		if (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP))
		{
			// Until the zone was a siege zone. Check also if victim was a player. Randomers aren't counted.
			if (target instanceof Player && getSiegeState() > 0 && targetPlayer.getSiegeState() > 0 && getSiegeState() != targetPlayer.getSiegeState())
			{
				// Now check clan relations.
				final Clan killerClan = getClan();
				if (killerClan != null)
					killerClan.setSiegeKills(killerClan.getSiegeKills() + 1);
				
				final Clan targetClan = targetPlayer.getClan();
				if (targetClan != null)
					targetClan.setSiegeDeaths(targetClan.getSiegeDeaths() + 1);
				
				if (Config.ANNOUNCE_PK_PVP)
				{
					String msg = "";
					msg = Config.ANNOUNCE_PVP_MSG.replace("$killer", getName()).replace("$target", targetPlayer.getName());
					if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
						Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S1).addString(msg));
					else
						Broadcast.announceToOnlinePlayers(msg);
				}
			}
			return;
		}
		
		// Check if it's pvp (cases : regular, wars, victim is PKer)
		if (checkIfPvP(target) || (targetPlayer.getClan() != null && getClan() != null && getClan().isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(getClanId()) && targetPlayer.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPledgeType() != Clan.SUBUNIT_ACADEMY) || (targetPlayer.getKarma() > 0 && Config.KARMA_AWARD_PK_KILL))
		{
			if (target instanceof Player)
			{
				// Add PvP point to attacker.
				setPvpKills(getPvpKills() + 1);
				
				// PvP color check
				colorsPvPCheck();

				
				if (Config.ANNOUNCE_PK_PVP)
				{
					String msg = "";
					msg = Config.ANNOUNCE_PVP_MSG.replace("$killer", getName()).replace("$target", targetPlayer.getName());
					if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
						Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S1).addString(msg));
					else
						Broadcast.announceToOnlinePlayers(msg);
				}
				
				// Send UserInfo packet to attacker with its Karma and PK Counter
				sendPacket(new UserInfo(this));
			}
		}
		// Otherwise, killer is considered as a PKer.
		else if (targetPlayer.getKarma() == 0 && targetPlayer.getPvpFlag() == 0)
		{
			// PK Points are increased only if you kill a player.
			if (target instanceof Player)
				setPkKills(getPkKills() + 1);
			
			// PvP color check
			colorsPkCheck();
			
			if (Config.ANNOUNCE_PK_PVP)
			{
				String msg = "";
				msg = Config.ANNOUNCE_PK_MSG.replace("$killer", getName()).replace("$target", targetPlayer.getName());
				if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S1).addString(msg));
				else
					Broadcast.announceToOnlinePlayers(msg);
			}
			
			// Calculate new karma.
			setKarma(getKarma() + Formulas.calculateKarmaGain(getPkKills(), target instanceof Summon));
			
			// Send UserInfo packet to attacker with its Karma and PK Counter
			sendPacket(new UserInfo(this));
		}
	}


	/**
	 * @return
	 */
	public final int getCurrentShowHpPvp()
	{
	 return (int) getStatus().getCurrentHp();
	}
	//Show CP
	public final int getCurrentShowCpPvp()
	{
	    return (int) getStatus().getCurrentCp();
	}
	
	public void updatePvPStatus()
	{
	    if ((isInsideZone(ZoneId.FLAG)) && (getPvpFlag() > 0)) {
	        return;
	      }
		
		if (isInsideZone(ZoneId.PVP))
			return;
		
		PvpFlagTaskManager.getInstance().add(this, Config.PVP_NORMAL_TIME);
		
		if (getPvpFlag() == 0)
			updatePvPFlag(1);
	}
	
	public void updatePvPStatus(Creature target)
	{
		final Player player = target.getActingPlayer();
		if (player == null)
			return;

		
		if (isInDuel() && player.getDuelId() == getDuelId())
			return;
		
		if ((!isInsideZone(ZoneId.PVP) || !target.isInsideZone(ZoneId.PVP)) && player.getKarma() == 0)
		{
			PvpFlagTaskManager.getInstance().add(this, checkIfPvP(player) ? Config.PVP_PVP_TIME : Config.PVP_NORMAL_TIME);
			
			if (getPvpFlag() == 0)
				updatePvPFlag(1);
		}
	}
	
	/**
	 * Restore the experience this Player has lost and sends StatusUpdate packet.
	 * @param restorePercent The specified % of restored experience.
	 */
	public void restoreExp(double restorePercent)
	{
		if (getExpBeforeDeath() > 0)
		{
			getStat().addExp((int) Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100));
			setExpBeforeDeath(0);
		}
	}
	
	/**
	 * Reduce the Experience (and level if necessary) of the Player in function of the calculated Death Penalty.
	 * <ul>
	 * <li>Calculate the Experience loss</li>
	 * <li>Set the value of _expBeforeDeath</li>
	 * <li>Set the new Experience value of the Player and Decrease its level if necessary</li>
	 * <li>Send StatusUpdate packet with its new Experience</li>
	 * </ul>
	 * @param atWar If true, use clan war penalty system instead of regular system.
	 * @param killedByPlayable Used to see if victim loses XP or not.
	 * @param killedBySiegeNpc Used to see if victim loses XP or not.
	 */
	public void deathPenalty(boolean atWar, boolean killedByPlayable, boolean killedBySiegeNpc)
	{
		// No xp loss inside pvp zone unless
		// - it's a siege zone and you're NOT participating
		// - you're killed by a non-pc whose not belong to the siege
		if (isInsideZone(ZoneId.PVP))
		{
			// No xp loss for siege participants inside siege zone.
			if (isInsideZone(ZoneId.SIEGE))
			{
				if (isInSiege() && (killedByPlayable || killedBySiegeNpc))
					return;
			}
			// No xp loss for arenas participants killed by playable.
			else if (killedByPlayable)
				return;
		}
		
		// Get the level of the Player
		final int lvl = getLevel();
		
		// The death steal you some Exp
		double percentLost = 7.0;
		if (getLevel() >= 76)
			percentLost = 2.0;
		else if (getLevel() >= 40)
			percentLost = 4.0;
		
		if (getKarma() > 0)
			percentLost *= Config.RATE_KARMA_EXP_LOST;
		
		if (isFestivalParticipant() || atWar || isInsideZone(ZoneId.SIEGE))
			percentLost /= 4.0;
		
		// Calculate the Experience loss
		long lostExp = 0;
		
		if (lvl < Experience.MAX_LEVEL)
			lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
		else
			lostExp = Math.round((getStat().getExpForLevel(Experience.MAX_LEVEL) - getStat().getExpForLevel(Experience.MAX_LEVEL - 1)) * percentLost / 100);
		
		// Get the Experience before applying penalty
		setExpBeforeDeath(getExp());
		
		// Set new karma
		updateKarmaLoss(lostExp);
		
		// Set the new Experience value of the Player
		getStat().addExp(-lostExp);
	}
	
	public int getPartyRoom()
	{
		return _partyroom;
	}
	
	public boolean isInPartyMatchRoom()
	{
		return _partyroom > 0;
	}
	
	public void setPartyRoom(int id)
	{
		_partyroom = id;
	}
	
	/**
	 * Remove the player from both waiting list and any potential room.
	 */
	public void removeMeFromPartyMatch()
	{
		PartyMatchWaitingList.getInstance().removePlayer(this);
		if (_partyroom != 0)
		{
			PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
			if (room != null)
				room.deleteMember(this);
		}
	}
	
	/**
	 * Stop all timers related to that Player.
	 */
	public void stopAllTimers()
	{
		stopHpMpRegeneration();
		WaterTaskManager.getInstance().remove(this);
		stopFeed();
		
		_petTemplate = null;
		_petData = null;
		
		storePetFood(_mountNpcId);
		stopPunishTask(true);
		stopChargeTask();
		
		AttackStanceTaskManager.getInstance().remove(this);
		PvpFlagTaskManager.getInstance().remove(this);
		GameTimeTaskManager.getInstance().remove(this);
		ShadowItemTaskManager.getInstance().remove(this);
	}
	
	/**
	 * Return the L2Summon of the Player or null.
	 */
	@Override
	public Summon getPet()
	{
		return _summon;
	}
	
	/**
	 * @return {@code true} if the player has a pet, {@code false} otherwise
	 */
	public boolean hasPet()
	{
		return _summon instanceof Pet;
	}
	
	/**
	 * @return {@code true} if the player has a summon, {@code false} otherwise
	 */
	public boolean hasServitor()
	{
		return _summon instanceof Servitor;
	}
	
	/**
	 * Set the L2Summon of the Player.
	 * @param summon The Object.
	 */
	public void setPet(Summon summon)
	{
		_summon = summon;
	}
	
	/**
	 * @return the L2TamedBeast of the Player or null.
	 */
	public TamedBeast getTrainedBeast()
	{
		return _tamedBeast;
	}
	
	/**
	 * Set the L2TamedBeast of the Player.
	 * @param tamedBeast The Object.
	 */
	public void setTrainedBeast(TamedBeast tamedBeast)
	{
		_tamedBeast = tamedBeast;
	}
	
	/**
	 * @return the current {@link Request}.
	 */
	public Request getRequest()
	{
		return _request;
	}
	
	/**
	 * Set the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 * @param requester
	 */
	public void setActiveRequester(Player requester)
	{
		_activeRequester = requester;
	}
	
	/**
	 * @return the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 */
	public Player getActiveRequester()
	{
		if (_activeRequester != null && _activeRequester.isRequestExpired() && _activeTradeList == null)
			_activeRequester = null;
		
		return _activeRequester;
	}
	
	/**
	 * @return True if a request is in progress.
	 */
	public boolean isProcessingRequest()
	{
		return getActiveRequester() != null || _requestExpireTime > System.currentTimeMillis();
	}
	
	/**
	 * @return True if a transaction <B>(trade OR request)</B> is in progress.
	 */
	public boolean isProcessingTransaction()
	{
		return getActiveRequester() != null || _activeTradeList != null || _requestExpireTime > System.currentTimeMillis();
	}
	
	/**
	 * Set the _requestExpireTime of that Player, and set his partner as the active requester.
	 * @param partner The partner to make checks on.
	 */
	public void onTransactionRequest(Player partner)
	{
		_requestExpireTime = System.currentTimeMillis() + REQUEST_TIMEOUT * 1000;
		partner.setActiveRequester(this);
	}
	
	/**
	 * @return true if last request is expired.
	 */
	public boolean isRequestExpired()
	{
		return _requestExpireTime <= System.currentTimeMillis();
	}
	
	/**
	 * Select the Warehouse to be used in next activity.
	 */
	public void onTransactionResponse()
	{
		_requestExpireTime = 0;
	}
	
	/**
	 * Select the Warehouse to be used in next activity.
	 * @param warehouse An active warehouse.
	 */
	public void setActiveWarehouse(ItemContainer warehouse)
	{
		_activeWarehouse = warehouse;
	}
	
	/**
	 * @return The active Warehouse.
	 */
	public ItemContainer getActiveWarehouse()
	{
		return _activeWarehouse;
	}
	
	/**
	 * Set the TradeList to be used in next activity.
	 * @param tradeList The TradeList to be used.
	 */
	public void setActiveTradeList(TradeList tradeList)
	{
		_activeTradeList = tradeList;
	}
	
	/**
	 * @return The active TradeList.
	 */
	public TradeList getActiveTradeList()
	{
		return _activeTradeList;
	}
	
	public void onTradeStart(Player partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
		
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1).addString(partner.getName()));
		sendPacket(new TradeStart(this));
	}
	
	public void onTradeConfirm(Player partner)
	{
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CONFIRMED_TRADE).addString(partner.getName()));
		
		partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
		sendPacket(TradePressOtherOk.STATIC_PACKET);
	}
	
	public void onTradeCancel(Player partner)
	{
		if (_activeTradeList == null)
			return;
		
		_activeTradeList.lock();
		_activeTradeList = null;
		
		sendPacket(new SendTradeDone(0));
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELED_TRADE).addString(partner.getName()));
	}
	
	public void onTradeFinish(boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(new SendTradeDone(1));
		if (successfull)
			sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
	}
	
	public void startTrade(Player partner)
	{
		onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	public void cancelActiveTrade()
	{
		if (_activeTradeList == null)
			return;
		
		Player partner = _activeTradeList.getPartner();
		if (partner != null)
			partner.onTradeCancel(this);
		
		onTradeCancel(this);
	}
	
	/**
	 * @return The _createList object of the Player.
	 */
	public ManufactureList getCreateList()
	{
		return _createList;
	}
	
	/**
	 * Set the _createList object of the Player.
	 * @param list
	 */
	public void setCreateList(ManufactureList list)
	{
		_createList = list;
	}
	
	/**
	 * @return The _sellList object of the Player.
	 */
	public TradeList getSellList()
	{
		if (_sellList == null)
			_sellList = new TradeList(this);
		
		return _sellList;
	}
	
	/**
	 * @return the _buyList object of the Player.
	 */
	public TradeList getBuyList()
	{
		if (_buyList == null)
			_buyList = new TradeList(this);
		
		return _buyList;
	}
	
	/**
	 * Set the Store type of the Player.
	 * @param type : 0 = none, 1 = sell, 2 = sellmanage, 3 = buy, 4 = buymanage, 5 = manufacture.
	 */
	public void setStoreType(StoreType type)
	{
		_storeType = type;
	}
	
	/**
	 * @return The Store type of the Player.
	 */
	public StoreType getStoreType()
	{
		return _storeType;
	}
	
	/**
	 * @return true if the {@link Player} can use dwarven recipes.
	 */
	public boolean hasDwarvenCraft()
	{
		return hasSkill(L2Skill.SKILL_CREATE_DWARVEN);
	}
	
	/**
	 * @return true if the {@link Player} can use common recipes.
	 */
	public boolean hasCommonCraft()
	{
		return hasSkill(L2Skill.SKILL_CREATE_COMMON);
	}
	
	/**
	 * Method used by regular leveling system.<br>
	 * Reward the {@link Player} with autoGet skills only, or if Config.AUTO_LEARN_SKILLS is activated, with all available skills.
	 */
	public void giveSkills()
	{
		if (Config.AUTO_LEARN_SKILLS)
			rewardSkills();
		else
		{
			// We reward all autoGet skills to this player, but don't store any on database.
			for (GeneralSkillNode skill : getAvailableAutoGetSkills())
				addSkill(skill.getSkill(), false);
			
			// Remove the Lucky skill if level superior to 10.
			if (getLevel() >= 10 && hasSkill(L2Skill.SKILL_LUCKY))
				removeSkill(L2Skill.SKILL_LUCKY, false);
			
			// Remove invalid skills.
			removeInvalidSkills();
			
			sendSkillList();
		}
	}
	
	/**
	 * Method used by admin commands, Config.AUTO_LEARN_SKILLS or class master.<br>
	 * Reward the {@link Player} with all available skills, being autoGet or general skills.
	 */
	public void rewardSkills()
	{
		// We reward all skills to the players, but don't store autoGet skills on the database.
		for (GeneralSkillNode skill : getAllAvailableSkills())
			addSkill(skill.getSkill(), skill.getCost() != 0);
		
		// Remove the Lucky skill if level superior to 10.
		if (getLevel() >= 10 && hasSkill(L2Skill.SKILL_LUCKY))
			removeSkill(L2Skill.SKILL_LUCKY, false);
		
		// Remove invalid skills.
		removeInvalidSkills();
		
		sendSkillList();
	}
	
	/**
	 * Delete all invalid {@link L2Skill}s for this {@link Player}.<br>
	 * <br>
	 * A skill is considered invalid when the level of obtention of the skill is superior to 9 compared to player level (expertise skill obtention level is compared to player level without any penalty).<br>
	 * <br>
	 * It is then either deleted, or level is refreshed.
	 */
	private void removeInvalidSkills()
	{
		if (getSkills().isEmpty())
			return;
		
		// Retrieve the player template skills, based on actual level (+9 for regular skills, unchanged for expertise).
		final Map<Integer, Optional<GeneralSkillNode>> availableSkills = getTemplate().getSkills().stream().filter(s -> s.getMinLvl() <= getLevel() + ((s.getId() == L2Skill.SKILL_EXPERTISE) ? 0 : 9)).collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL)));
		
		for (L2Skill skill : getSkills().values())
		{
			// Bother only with skills existing on template (spare temporary skills, items skills, etc).
			if (getTemplate().getSkills().stream().filter(s -> s.getId() == skill.getId()).count() == 0)
				continue;
			
			// The known skill doesn't exist on available skills ; we drop existing skill.
			final Optional<GeneralSkillNode> tempSkill = availableSkills.get(skill.getId());
			if (tempSkill == null)
			{
				removeSkill(skill.getId(), true);
				continue;
			}
			
			// Retrieve the skill and max level for enchant scenario.
			final GeneralSkillNode availableSkill = tempSkill.get();
			final int maxLevel = SkillTable.getInstance().getMaxLevel(skill.getId());
			
			// Case of enchanted skills.
			if (skill.getLevel() > maxLevel)
			{
				// Player level is inferior to 76, or available skill is a good candidate.
				if ((getLevel() < 76 || availableSkill.getValue() < maxLevel) && skill.getLevel() > availableSkill.getValue())
					addSkill(availableSkill.getSkill(), true);
			}
			// We check if current known skill level is bigger than available skill level. If it's true, we override current skill with available skill.
			else if (skill.getLevel() > availableSkill.getValue())
				addSkill(availableSkill.getSkill(), true);
		}
	}
	
	/**
	 * Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills.<br>
	 * <b>Do not call this on enterworld or char load.</b>.
	 */
	private void regiveTemporarySkills()
	{
		// Add noble skills if noble.
		if (isNoble())
			setNoble(true, false);
		
		// Add Hero skills if hero.
		
	    if ((isHero()) || (HeroManager.getInstance().hasHeroPrivileges(getObjectId()))) {
	        setHero(true);
	      }
	      if ((isVip()) || (VipManager.getInstance().hasVipPrivileges(getObjectId()))) {
	        setVip(true);
	      }
		
		// Add clan skills.
		if (getClan() != null)
		{
			getClan().addSkillEffects(this);
			
			if (getClan().getLevel() >= Config.MINIMUM_CLAN_LEVEL && isClanLeader())
				addSiegeSkills();
		}
		
		// Reload passive skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
		// Add Death Penalty Buff Level
		restoreDeathPenaltyBuffLevel();
	}
	
	public void addSiegeSkills()
	{
		for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(isNoble()))
			addSkill(sk, false);
	}
	
	public void removeSiegeSkills()
	{
		for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(isNoble()))
			removeSkill(sk.getId(), false);
	}
	
	/**
	 * @return a {@link List} of all available autoGet {@link GeneralSkillNode}s <b>of maximal level</b> for this {@link Player}.
	 */
	public List<GeneralSkillNode> getAvailableAutoGetSkills()
	{
		final List<GeneralSkillNode> result = new ArrayList<>();
		
		getTemplate().getSkills().stream().filter(s -> s.getMinLvl() <= getLevel() && s.getCost() == 0).collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL))).forEach((i, s) ->
		{
			if (getSkillLevel(i) < s.get().getValue())
				result.add(s.get());
		});
		return result;
	}
	
	/**
	 * @return a {@link List} of available {@link GeneralSkillNode}s (only general) for this {@link Player}.
	 */
	public List<GeneralSkillNode> getAvailableSkills()
	{
		final List<GeneralSkillNode> result = new ArrayList<>();
		
		getTemplate().getSkills().stream().filter(s -> s.getMinLvl() <= getLevel() && s.getCost() != 0).forEach(s ->
		{
			if (getSkillLevel(s.getId()) == s.getValue() - 1)
				result.add(s);
		});
		return result;
	}
	
	/**
	 * @return a {@link List} of all available {@link GeneralSkillNode}s (being general or autoGet) <b>of maximal level</b> for this {@link Player}.
	 */
	public List<GeneralSkillNode> getAllAvailableSkills()
	{
		final List<GeneralSkillNode> result = new ArrayList<>();
		
		getTemplate().getSkills().stream().filter(s -> s.getMinLvl() <= getLevel()).collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL))).forEach((i, s) ->
		{
			if (getSkillLevel(i) < s.get().getValue())
				result.add(s.get());
		});
		return result;
	}
	
	/**
	 * Retrieve next lowest level skill to learn, based on current player level and skill sp cost.
	 * @return the required level for next {@link GeneralSkillNode} to learn for this {@link Player}.
	 */
	public int getRequiredLevelForNextSkill()
	{
		return getTemplate().getSkills().stream().filter(s -> s.getMinLvl() > getLevel() && s.getCost() != 0).min(COMPARE_SKILLS_BY_MIN_LVL).map(s -> s.getMinLvl()).orElse(0);
	}
	
	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the Player.
	 * @param clan The Clan object which is used to feed Player values.
	 */
	public void setClan(Clan clan)
	{
		_clan = clan;
		setTitle("");
		
		if (clan == null)
		{
			_clanId = 0;
			_clanPrivileges = 0;
			_pledgeType = 0;
			_powerGrade = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			return;
		}
		
		if (!clan.isMember(getObjectId()))
		{
			// char has been kicked from clan
			setClan(null);
			return;
		}
		
		_clanId = clan.getClanId();
	}
	
	/**
	 * @return The _clan object of the Player.
	 */
	public Clan getClan()
	{
		return _clan;
	}
	
	/**
	 * @return True if the Player is the leader of its clan.
	 */
	public boolean isClanLeader()
	{
		return _clan != null && getObjectId() == _clan.getLeaderId();
	}
	
	/**
	 * Reduce the number of arrows owned by the Player and send InventoryUpdate or ItemList (to unequip if the last arrow was consummed).
	 */
	@Override
	protected void reduceArrowCount() // TODO: replace with a simple player.destroyItem...
	{
		final ItemInstance arrows = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (arrows == null)
			return;
		
		final InventoryUpdate iu = new InventoryUpdate();
		
		if (arrows.getCount() > 1)
		{
			synchronized (arrows)
			{
				arrows.changeCount(null, -1, this, null);
				arrows.setLastChange(ItemState.MODIFIED);
				
				iu.addModifiedItem(arrows);
				
				// could do also without saving, but let's save approx 1 of 10
				if (Rnd.get(10) < 1)
					arrows.updateDatabase();
				
				_inventory.refreshWeight();
			}
		}
		else
		{
			iu.addRemovedItem(arrows);
			
			// Destroy entire item and save to database
			_inventory.destroyItem("Consume", arrows, this, null);
		}
		sendPacket(iu);
	}
	
	/**
	 * Check if the arrow item exists on inventory and is already slotted ; if not, equip it.
	 */
	@Override
	protected boolean checkAndEquipArrows()
	{
		// Retrieve arrows instance on player inventory.
		final ItemInstance arrows = getInventory().findArrowForBow(getActiveWeaponItem());
		if (arrows == null)
			return false;
		
		// Arrows are already equiped, don't bother.
		if (arrows.getLocation() == ItemLocation.PAPERDOLL)
			return true;
		
		// Equip arrows in left hand.
		getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, arrows);
		
		// Send ItemList to this player to update left hand equipement
		sendPacket(new ItemList(this, false));
		
		return true;
	}
	
	/**
	 * Disarm the player's weapon and shield.
	 * @return true if successful, false otherwise.
	 */
	public boolean disarmWeapons()
	{
		// Don't allow disarming a cursed weapon
		if (isCursedWeaponEquipped())
			return false;
		
		// Unequip the weapon
		ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn != null)
		{
			ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(wpn);
			InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance itm : unequipped)
				iu.addModifiedItem(itm);
			sendPacket(iu);
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequipped.length > 0)
			{
				SystemMessage sm;
				if (unequipped[0].getEnchantLevel() > 0)
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequipped[0].getEnchantLevel()).addItemName(unequipped[0]);
				else
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0]);
				
				sendPacket(sm);
			}
		}
		
		// Unequip the shield
		ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null)
		{
			ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(sld);
			InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance itm : unequipped)
				iu.addModifiedItem(itm);
			sendPacket(iu);
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequipped.length > 0)
			{
				SystemMessage sm;
				if (unequipped[0].getEnchantLevel() > 0)
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequipped[0].getEnchantLevel()).addItemName(unequipped[0]);
				else
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0]);
				
				sendPacket(sm);
			}
		}
		return true;
	}
	
	public boolean mount(Summon pet)
	{
		if (!disarmWeapons())
			return false;
		
		setRunning();
		stopAllToggles();
		
		Ride mount = new Ride(getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().getNpcId());
		setMount(pet.getNpcId(), pet.getLevel(), mount.getMountType());
		
		_petTemplate = (PetTemplate) pet.getTemplate();
		_petData = _petTemplate.getPetDataEntry(pet.getLevel());
		_mountObjectId = pet.getControlItemId();
		
		startFeed(pet.getNpcId());
		broadcastPacket(mount);
		
		// Notify self and others about speed change
		broadcastUserInfo();
		
		pet.unSummon(this);
		return true;
	}
	
	public boolean mount(int npcId, int controlItemId, boolean useFood)
	{
		if (!disarmWeapons())
			return false;
		
		setRunning();
		stopAllToggles();
		
		Ride mount = new Ride(getObjectId(), Ride.ACTION_MOUNT, npcId);
		if (setMount(npcId, getLevel(), mount.getMountType()))
		{
			_petTemplate = (PetTemplate) NpcData.getInstance().getTemplate(npcId);
			_petData = _petTemplate.getPetDataEntry(getLevel());
			_mountObjectId = controlItemId;
			
			broadcastPacket(mount);
			
			// Notify self and others about speed change
			broadcastUserInfo();
			
			if (useFood)
				startFeed(npcId);
			
			return true;
		}
		return false;
	}
	
	public boolean mountPlayer(Summon summon)
	{
		if (summon instanceof Pet && summon.isMountable() && !isMounted() && !isBetrayed())
		{
			if (isDead()) // A strider cannot be ridden when dead.
			{
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				return false;
			}
			
			if (summon.isDead()) // A dead strider cannot be ridden.
			{
				sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				return false;
			}
			
			if (summon.isInCombat() || summon.isRooted()) // A strider in battle cannot be ridden.
			{
				sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				return false;
			}
			
			if (isInCombat()) // A strider cannot be ridden while in battle
			{
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			}
			
			if (isSitting()) // A strider can be ridden only when standing
			{
				sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				return false;
			}
			
			if (isFishing()) // You can't mount, dismount, break and drop items while fishing
			{
				sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
				return false;
			}
			
			if (isCursedWeaponEquipped()) // You can't mount, dismount, break and drop items while weilding a cursed weapon
			{
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			}
			
			if (!MathUtil.checkIfInRange(200, this, summon, true))
			{
				sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_STRIDER_TO_MOUNT);
				return false;
			}
			
			if (((Pet) summon).checkHungryState())
			{
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			
			if (!summon.isDead() && !isMounted())
				mount(summon);
		}
		else if (isMounted())
		{
			if (getMountType() == 2 && isInsideZone(ZoneId.NO_LANDING))
			{
				sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
				return false;
			}
			
			if (checkFoodState(_petTemplate.getHungryLimit()))
			{
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			
			dismount();
		}
		return true;
	}
	
	public boolean dismount()
	{
		sendPacket(new SetupGauge(GaugeColor.GREEN, 0));
		
		int petId = _mountNpcId;
		if (setMount(0, 0, 0))
		{
			stopFeed();
			
			broadcastPacket(new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0));
			
			_petTemplate = null;
			_petData = null;
			_mountObjectId = 0;
			
			storePetFood(petId);
			
			// Notify self and others about speed change
			broadcastUserInfo();
			return true;
		}
		return false;
	}
	
	public void storePetFood(int petId)
	{
		if (_controlItemId != 0 && petId != 0)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE pets SET fed=? WHERE item_obj_id = ?");
				statement.setInt(1, getCurrentFeed());
				statement.setInt(2, _controlItemId);
				statement.executeUpdate();
				statement.close();
				_controlItemId = 0;
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Failed to store Pet [NpcId: " + petId + "] data", e);
			}
		}
	}
	
	protected class FeedTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!isMounted())
			{
				stopFeed();
				return;
			}
			
			// Eat or return to pet control item.
			if (getCurrentFeed() > getFeedConsume())
				setCurrentFeed(getCurrentFeed() - getFeedConsume());
			else
			{
				setCurrentFeed(0);
				stopFeed();
				dismount();
				sendPacket(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED);
				return;
			}
			
			ItemInstance food = getInventory().getItemByItemId(_petTemplate.getFood1());
			if (food == null)
				food = getInventory().getItemByItemId(_petTemplate.getFood2());
			
			if (food != null && checkFoodState(_petTemplate.getAutoFeedLimit()))
			{
				IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
				if (handler != null)
				{
					handler.useItem(Player.this, food, false);
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food));
				}
			}
		}
	}
	
	protected synchronized void startFeed(int npcId)
	{
		_canFeed = npcId > 0;
		if (!isMounted())
			return;
		
		if (getPet() != null)
		{
			setCurrentFeed(((Pet) getPet()).getCurrentFed());
			_controlItemId = getPet().getControlItemId();
			sendPacket(new SetupGauge(GaugeColor.GREEN, getCurrentFeed() * 10000 / getFeedConsume(), _petData.getMaxMeal() * 10000 / getFeedConsume()));
			if (!isDead())
				_mountFeedTask = ThreadPool.scheduleAtFixedRate(new FeedTask(), 10000, 10000);
		}
		else if (_canFeed)
		{
			setCurrentFeed(_petData.getMaxMeal());
			sendPacket(new SetupGauge(GaugeColor.GREEN, getCurrentFeed() * 10000 / getFeedConsume(), _petData.getMaxMeal() * 10000 / getFeedConsume()));
			if (!isDead())
				_mountFeedTask = ThreadPool.scheduleAtFixedRate(new FeedTask(), 10000, 10000);
		}
	}
	
	protected synchronized void stopFeed()
	{
		if (_mountFeedTask != null)
		{
			_mountFeedTask.cancel(false);
			_mountFeedTask = null;
		}
	}
	
	public PetTemplate getPetTemplate()
	{
		return _petTemplate;
	}
	
	public PetDataEntry getPetDataEntry()
	{
		return _petData;
	}
	
	public int getCurrentFeed()
	{
		return _curFeed;
	}
	
	protected int getFeedConsume()
	{
		return (isAttackingNow()) ? _petData.getMountMealInBattle() : _petData.getMountMealInNormal();
	}
	
	public void setCurrentFeed(int num)
	{
		_curFeed = Math.min(num, _petData.getMaxMeal());
		
		sendPacket(new SetupGauge(GaugeColor.GREEN, getCurrentFeed() * 10000 / getFeedConsume(), _petData.getMaxMeal() * 10000 / getFeedConsume()));
	}
	
	/**
	 * @param state : The state to check (can be autofeed, hungry or unsummon).
	 * @return true if the limit is reached, false otherwise or if there is no need to feed.
	 */
	public boolean checkFoodState(double state)
	{
		return (_canFeed) ? getCurrentFeed() < (_petData.getMaxMeal() * state) : false;
	}
	
	public void setUptime(long time)
	{
		_uptime = time;
	}
	
	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}
	
	/**
	 * Return True if the Player is invulnerable.
	 */
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || isSpawnProtected();
	}
	
	/**
	 * Return True if the Player has a Party in progress.
	 */
	@Override
	public boolean isInParty()
	{
		return _party != null;
	}
	
	/**
	 * Set the _party object of the Player (without joining it).
	 * @param party The object.
	 */
	public void setParty(Party party)
	{
		_party = party;
	}
	
	/**
	 * Return the _party object of the Player.
	 */
	@Override
	public Party getParty()
	{
		return _party;
	}
	
	public LootRule getLootRule()
	{
		return _lootRule;
	}
	
	public void setLootRule(LootRule lootRule)
	{
		_lootRule = lootRule;
	}
	
	/**
	 * Return True if the Player is a GM.
	 */
	@Override
	public boolean isGM()
	{
		return getAccessLevel().isGm();
	}
	
	/**
	 * Set the _accessLevel of the Player.
	 * @param level
	 */
	public void setAccessLevel(int level)
	{
		// Retrieve the AccessLevel. Even if not existing, it returns user level.
		AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(level);
		if (accessLevel == null)
		{
			_log.warning("Can't find access level " + level + " for " + toString());
			accessLevel = AdminData.getInstance().getAccessLevel(0);
		}
		
		_accessLevel = accessLevel;
		
		if (level > 0)
		{
			// For level lower or equal to user, we don't apply AccessLevel name as title.
			setTitle(accessLevel.getName());
			
			// We log master access.
			if (level == AdminData.getInstance().getMasterAccessLevel().getLevel())
				_log.info(getName() + " has logged in with Master access level.");
		}
		
		// We refresh GMList if the access level is GM.
		if (accessLevel.isGm())
		{
			// A little hack to avoid Enterworld config to be replaced.
			if (!AdminData.isRegisteredAsGM(this))
				AdminData.addGm(this, false);
		}
		else
			AdminData.deleteGm(this);
		
		getAppearance().setNameColor(accessLevel.getNameColor());
		getAppearance().setTitleColor(accessLevel.getTitleColor());
		broadcastUserInfo();
		
		PlayerInfoTable.getInstance().updatePlayerData(this, true);
	}
	
	public void setAccountAccesslevel(int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}
	
	/**
	 * @return the _accessLevel of the Player.
	 */
	public AccessLevel getAccessLevel()
	{
		return _accessLevel;
	}
	
	/**
	 * Update Stats of the Player client side by sending UserInfo/StatusUpdate to this Player and CharInfo/StatusUpdate to all Player in its _KnownPlayers (broadcast).
	 * @param broadcastType
	 */
	public void updateAndBroadcastStatus(int broadcastType)
	{
		refreshOverloaded();
		refreshExpertisePenalty();
		
		if (broadcastType == 1)
			sendPacket(new UserInfo(this));
		else if (broadcastType == 2)
			broadcastUserInfo();
	}
	
	/**
	 * Send StatusUpdate packet with Karma to the Player and all Player to inform (broadcast).
	 */
	public void broadcastKarma()
	{
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		sendPacket(su);
		
		if (getPet() != null)
			sendPacket(new RelationChanged(getPet(), getRelation(this), false));
		
		broadcastRelationsChanges();
	}
	
	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).
	 * @param isOnline
	 * @param updateInDb
	 */
	public void setOnlineStatus(boolean isOnline, boolean updateInDb)
	{
		if (_isOnline != isOnline)
			_isOnline = isOnline;
		
		// Update the characters table of the database with online status and lastAccess (called when login and logout)
		if (updateInDb)
			updateOnlineStatus();
	}
	
	public void setIsIn7sDungeon(boolean isIn7sDungeon)
	{
		_isIn7sDungeon = isIn7sDungeon;
	}
	
	/**
	 * Update the characters table of the database with online status and lastAccess of this Player (called when login and logout).
	 */
	public void updateOnlineStatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, isOnlineInt());
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not set char online status:" + e);
		}
	}
	
	/**
	 * Retrieve a Player from the characters table of the database.
	 * <ul>
	 * <li>Retrieve the Player from the characters table of the database</li>
	 * <li>Set the x,y,z position of the Player and make it invisible</li>
	 * <li>Update the overloaded status of the Player</li>
	 * </ul>
	 * @param objectId Identifier of the object to initialized
	 * @return The Player loaded from the database
	 */
	public static Player restore(int objectId)
	{
		Player player = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				final int activeClassId = rset.getInt("classid");
				final PlayerTemplate template = PlayerData.getInstance().getTemplate(activeClassId);
				final PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), Sex.values()[rset.getInt("sex")]);
				
				player = new Player(objectId, template, rset.getString("account_name"), app);
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");
				
				player.getStat().setExp(rset.getLong("exp"));
				player.getStat().setLevel(rset.getByte("level"));
				player.getStat().setSp(rset.getInt("sp"));
				
				player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
				player.setWantsPeace(rset.getInt("wantspeace") == 1);
				player.setHeading(rset.getInt("heading"));
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNoble(rset.getInt("nobless") == 1, false);
				
				player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
				if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
					player.setClanJoinExpiryTime(0);
				
				player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
				if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
					player.setClanCreateExpiryTime(0);
				
				player.setPowerGrade(rset.getInt("power_grade"));
				player.setPledgeType(rset.getInt("subpledge"));
				
				int clanId = rset.getInt("clanid");
				if (clanId > 0)
					player.setClan(ClanTable.getInstance().getClan(clanId));
				
				if (player.getClan() != null)
				{
					if (player.getClan().getLeaderId() != player.getObjectId())
					{
						if (player.getPowerGrade() == 0)
							player.setPowerGrade(5);
						
						player.setClanPrivileges(player.getClan().getPriviledgesByRank(player.getPowerGrade()));
					}
					else
					{
						player.setClanPrivileges(Clan.CP_ALL);
						player.setPowerGrade(1);
					}
				}
				else
					player.setClanPrivileges(Clan.CP_NOTHING);
				
				player.setDeleteTimer(rset.getLong("deletetime"));
				player.setTitle(rset.getString("title"));
				player.setAccessLevel(rset.getInt("accesslevel"));
				player.setUptime(System.currentTimeMillis());
				player.setRecomHave(rset.getInt("rec_have"));
				player.setRecomLeft(rset.getInt("rec_left"));
				
				player._classIndex = 0;
				try
				{
					player.setBaseClass(rset.getInt("base_class"));
				}
				catch (Exception e)
				{
					player.setBaseClass(activeClassId);
				}
				
				// Restore Subclass Data (cannot be done earlier in function)
				if (restoreSubClassData(player))
				{
					if (activeClassId != player.getBaseClass())
					{
						for (SubClass subClass : player.getSubClasses().values())
							if (subClass.getClassId() == activeClassId)
								player._classIndex = subClass.getClassIndex();
					}
				}
				if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass())
				{
					// Subclass in use but doesn't exist in DB -
					// a possible restart-while-modifysubclass cheat has been attempted.
					// Switching to use base class
					player.setClassId(player.getBaseClass());
					_log.warning("Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
				}
				else
					player._activeClass = activeClassId;
				
				player.setApprentice(rset.getInt("apprentice"));
				player.setSponsor(rset.getInt("sponsor"));
				player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
				player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
				player.setPunishLevel(rset.getInt("punish_level"));
				if (player.getPunishLevel() != PunishLevel.NONE)
					player.setPunishTimer(rset.getLong("punish_timer"));
				else
					player.setPunishTimer(0);
				
				CursedWeaponManager.getInstance().checkPlayer(player);
				
				player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));
				
				player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
				
				// Set the x,y,z position of the Player and make it invisible
				player.getPosition().set(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				
				// Set Hero status if it applies
				if (Hero.getInstance().isActiveHero(objectId))
					player.setHero(true);
				
				// Set pledge class rank.
				player.setPledgeClass(ClanMember.calculatePledgeClass(player));
				
				// Retrieve from the database all secondary data of this Player and reward expertise/lucky skills if necessary.
				// Note that Clan, Noblesse and Hero skills are given separately and not here.
				player.restoreCharData();
				player.giveSkills();
				
				// buff and status icons
				if (Config.STORE_SKILL_COOLTIME)
					player.restoreEffects();
				
				// Restore current CP, HP and MP values
				final double currentHp = rset.getDouble("curHp");
				
				player.setCurrentCp(rset.getDouble("curCp"));
				player.setCurrentHp(currentHp);
				player.setCurrentMp(rset.getDouble("curMp"));
				
				if (currentHp < 0.5)
				{
					player.setIsDead(true);
					player.stopHpMpRegeneration();
				}
				
				// Restore pet if it exists in the world.
				final Pet pet = World.getInstance().getPet(player.getObjectId());
				if (pet != null)
				{
					player.setPet(pet);
					pet.setOwner(player);
				}
				
				player.refreshOverloaded();
				player.refreshExpertisePenalty();
				
				player.restoreFriendList();
				
				// Retrieve the name and ID of the other characters assigned to this account.
				PreparedStatement stmt = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?");
				stmt.setString(1, player._accountName);
				stmt.setInt(2, objectId);
				ResultSet chars = stmt.executeQuery();
				
				while (chars.next())
					player.getAccountChars().put(chars.getInt("obj_Id"), chars.getString("char_name"));
				
				chars.close();
				stmt.close();
				break;
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.severe("Could not restore char data: " + e);
		}
		
		return player;
	}
	
	public Forum getMemo()
	{
		if (_forumMemo == null)
		{
			final Forum forum = ForumsBBSManager.getInstance().getForumByName("MemoRoot");
			if (forum != null)
			{
				_forumMemo = forum.getChildByName(_accountName);
				if (_forumMemo == null)
					_forumMemo = ForumsBBSManager.getInstance().createNewForum(_accountName, forum, Forum.MEMO, Forum.OWNERONLY, getObjectId());
			}
		}
		return _forumMemo;
	}
	
	/**
	 * Restores sub-class data for the Player, used to check the current class index for the character.
	 * @param player The player to make checks on.
	 * @return true if successful.
	 */
	private static boolean restoreSubClassData(Player player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				SubClass subClass = new SubClass(rset.getInt("class_id"), rset.getInt("class_index"), rset.getLong("exp"), rset.getInt("sp"), rset.getByte("level"));
				
				// Enforce the correct indexing of _subClasses against their class indexes.
				player.getSubClasses().put(subClass.getClassIndex(), subClass);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not restore classes for " + player.getName() + ": " + e);
			e.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * Restores secondary data for the Player, based on the current class index.
	 */
	private void restoreCharData()
	{
		// Retrieve from the database all skills of this Player and add them to _skills.
		restoreSkills();
		
		// Retrieve from the database all macroses of this Player and add them to _macroses.
		_macroses.restore();
		
		// Retrieve from the database all shortCuts of this Player and add them to _shortCuts.
		_shortCuts.restore();
		
		// Retrieve from the database all henna of this Player and add them to _henna.
		restoreHenna();
		
		// Retrieve from the database all recom data of this Player and add to _recomChars.
		restoreRecom();
		
		// Retrieve from the database the recipe book of this Player.
		if (!isSubClassActive())
			restoreRecipeBook();
	}
	
	/**
	 * Store {@link Recipe} book data for this {@link Player}, if he isn't on an active subclass.
	 */
	private void storeRecipeBook()
	{
		if (isPhantom())
			return;
		// If the player is on a sub-class don't even attempt to store a recipe book.
		if (isSubClassActive())
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
			ps.setInt(1, getObjectId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("INSERT INTO character_recipebook (charId, recipeId) values(?,?)");
			
			for (Recipe recipe : getCommonRecipeBook())
			{
				ps.setInt(1, getObjectId());
				ps.setInt(2, recipe.getId());
				ps.addBatch();
			}
			
			for (Recipe recipe : getDwarvenRecipeBook())
			{
				ps.setInt(1, getObjectId());
				ps.setInt(2, recipe.getId());
				ps.addBatch();
			}
			
			ps.executeBatch();
			ps.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not store recipe book data.", e);
		}
	}
	
	/**
	 * Restore {@link Recipe} book data for this {@link Player}.
	 */
	private void restoreRecipeBook()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT recipeId FROM character_recipebook WHERE charId=?");
			ps.setInt(1, getObjectId());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				final Recipe recipe = RecipeData.getInstance().getRecipeList(rs.getInt("recipeId"));
				if (recipe.isDwarven())
					registerDwarvenRecipeList(recipe);
				else
					registerCommonRecipeList(recipe);
			}
			
			rs.close();
			ps.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore recipe book data.", e);
		}
	}
	
	/**
	 * Update Player stats in the characters table of the database.
	 * @param storeActiveEffects
	 */
	public synchronized void store(boolean storeActiveEffects)
	{
		// update client coords, if these look like true
		if (isInsideRadius(getClientX(), getClientY(), 1000, true))
			setXYZ(getClientX(), getClientY(), getClientZ());
		
		storeCharBase();
		storeCharSub();
		storeEffect(storeActiveEffects);
		storeRecipeBook();
		
		_vars.storeMe();
	}
	
	public void store()
	{
		store(true);
	}
	
	private void storeCharBase()
	{
		
	    if (isPhantom()) 
	        return;
	        
		// Get the exp, level, and sp of base class to store in base table
		final int currentClassIndex = getClassIndex();
		
		_classIndex = 0;
		
		final long exp = getStat().getExp();
		final int level = getStat().getLevel();
		final int sp = getStat().getSp();
		
		_classIndex = currentClassIndex;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_CHARACTER))
		{
			ps.setInt(1, level);
			ps.setInt(2, getMaxHp());
			ps.setDouble(3, getCurrentHp());
			ps.setInt(4, getMaxCp());
			ps.setDouble(5, getCurrentCp());
			ps.setInt(6, getMaxMp());
			ps.setDouble(7, getCurrentMp());
			ps.setInt(8, getAppearance().getFace());
			ps.setInt(9, getAppearance().getHairStyle());
			ps.setInt(10, getAppearance().getHairColor());
			ps.setInt(11, getAppearance().getSex().ordinal());
			ps.setInt(12, getHeading());
			
			if (!isInObserverMode())
			{
				ps.setInt(13, getX());
				ps.setInt(14, getY());
				ps.setInt(15, getZ());
			}
			else
			{
				ps.setInt(13, _savedLocation.getX());
				ps.setInt(14, _savedLocation.getY());
				ps.setInt(15, _savedLocation.getZ());
			}
			
			ps.setLong(16, exp);
			ps.setLong(17, getExpBeforeDeath());
			ps.setInt(18, sp);
			ps.setInt(19, getKarma());
			ps.setInt(20, getPvpKills());
			ps.setInt(21, getPkKills());
			ps.setInt(22, getClanId());
			ps.setInt(23, getRace().ordinal());
			ps.setInt(24, getClassId().getId());
			ps.setLong(25, getDeleteTimer());
			ps.setString(26, getTitle());
			ps.setInt(27, getAccessLevel().getLevel());
			ps.setInt(28, isOnlineInt());
			ps.setInt(29, isIn7sDungeon() ? 1 : 0);
			ps.setInt(30, getClanPrivileges());
			ps.setInt(31, wantsPeace() ? 1 : 0);
			ps.setInt(32, getBaseClass());
			
			long totalOnlineTime = _onlineTime;
			if (_onlineBeginTime > 0)
				totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;
			
			ps.setLong(33, totalOnlineTime);
			ps.setInt(34, getPunishLevel().value());
			ps.setLong(35, getPunishTimer());
			ps.setInt(36, isNoble() ? 1 : 0);
			ps.setLong(37, getPowerGrade());
			ps.setInt(38, getPledgeType());
			ps.setInt(39, getLvlJoinedAcademy());
			ps.setLong(40, getApprentice());
			ps.setLong(41, getSponsor());
			ps.setInt(42, getAllianceWithVarkaKetra());
			ps.setLong(43, getClanJoinExpiryTime());
			ps.setLong(44, getClanCreateExpiryTime());
			ps.setString(45, getName());
			ps.setLong(46, getDeathPenaltyBuffLevel());
			ps.setInt(47, getObjectId());
			
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warning("Could not store char base data: " + e);
		}
	}
	
	private void storeCharSub()
	{
		if (_subClasses.isEmpty())
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);
			for (SubClass subClass : _subClasses.values())
			{
				statement.setLong(1, subClass.getExp());
				statement.setInt(2, subClass.getSp());
				statement.setInt(3, subClass.getLevel());
				statement.setInt(4, subClass.getClassId());
				statement.setInt(5, getObjectId());
				statement.setInt(6, subClass.getClassIndex());
				statement.execute();
				statement.clearParameters();
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not store sub class data for " + getName() + ": " + e);
		}
	}
	
	private void storeEffect(boolean storeEffects)
	{
	    if (isPhantom())
	        return;
		
		if (!Config.STORE_SKILL_COOLTIME)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete all current stored effects for char to avoid dupe
			try (PreparedStatement ps = con.prepareStatement(DELETE_SKILL_SAVE))
			{
				ps.setInt(1, getObjectId());
				ps.setInt(2, getClassIndex());
				ps.executeUpdate();
			}
			
			int buff_index = 0;
			final List<Integer> storedSkills = new ArrayList<>();
			
			try (PreparedStatement ps = con.prepareStatement(ADD_SKILL_SAVE))
			{
				// Store all effect data along with calulated remaining reuse delays for matching skills. 'restore_type'= 0.
				if (storeEffects)
				{
					for (L2Effect effect : getAllEffects())
					{
						if (effect == null)
							continue;
						
						switch (effect.getEffectType())
						{
							case HEAL_OVER_TIME:
							case COMBAT_POINT_HEAL_OVER_TIME:
								continue;
						}
						
						final L2Skill skill = effect.getSkill();
						if (storedSkills.contains(skill.getReuseHashCode()))
							continue;
						
						storedSkills.add(skill.getReuseHashCode());
						if (!effect.isHerbEffect() && effect.getInUse() && !skill.isToggle())
						{
							ps.setInt(1, getObjectId());
							ps.setInt(2, skill.getId());
							ps.setInt(3, skill.getLevel());
							ps.setInt(4, effect.getCount());
							ps.setInt(5, effect.getTime());
							
							final TimeStamp t = _reuseTimeStamps.get(skill.getReuseHashCode());
							if (t != null && t.hasNotPassed())
							{
								ps.setLong(6, t.getReuse());
								ps.setDouble(7, t.getStamp());
							}
							else
							{
								ps.setLong(6, 0);
								ps.setDouble(7, 0);
							}
							
							ps.setInt(8, 0);
							ps.setInt(9, getClassIndex());
							ps.setInt(10, ++buff_index);
							ps.addBatch(); // Add SQL
						}
					}
				}
				
				// Store the reuse delays of remaining skills which lost effect but still under reuse delay. 'restore_type' 1.
				for (Map.Entry<Integer, TimeStamp> entry : _reuseTimeStamps.entrySet())
				{
					final int hash = entry.getKey();
					if (storedSkills.contains(hash))
						continue;
					
					final TimeStamp t = entry.getValue();
					if (t != null && t.hasNotPassed())
					{
						storedSkills.add(hash);
						
						ps.setInt(1, getObjectId());
						ps.setInt(2, t.getSkillId());
						ps.setInt(3, t.getSkillLvl());
						ps.setInt(4, -1);
						ps.setInt(5, -1);
						ps.setLong(6, t.getReuse());
						ps.setDouble(7, t.getStamp());
						ps.setInt(8, 1);
						ps.setInt(9, getClassIndex());
						ps.setInt(10, ++buff_index);
						ps.addBatch(); // Add SQL
					}
				}
				
				ps.executeBatch(); // Execute SQLs
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store char effect data: ", e);
		}
	}
	
	/**
	 * @return True if the Player is online.
	 */
	public boolean isOnline()
	{
		return _isOnline;
	}
	
	/**
	 * @return an int interpretation of online status.
	 */
	public int isOnlineInt()
	{
		if (_isOnline && getClient() != null)
			return getClient().isDetached() ? 2 : 1;
		
		return 0;
	}
	
	public boolean isIn7sDungeon()
	{
		return _isIn7sDungeon;
	}
	
	/**
	 * Add a {@link L2Skill} and its Func objects to the calculator set of the {@link Player}.<BR>
	 * <ul>
	 * <li>Replace or add oldSkill by newSkill (only if oldSkill is different than newSkill)</li>
	 * <li>If an old skill has been replaced, remove all its Func objects of Creature calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the Creature</li>
	 * </ul>
	 * @param newSkill : The skill to add.
	 * @param store : If true, we save the skill on database.
	 * @return true if the skill has been successfully added.
	 */
	public boolean addSkill(L2Skill newSkill, boolean store)
	{
		// New skill is null, abort.
		if (newSkill == null)
			return false;
		
		// Search the old skill. We test if it's different than the new one. If yes, we abort the operation.
		final L2Skill oldSkill = getSkills().get(newSkill.getId());
		if (oldSkill != null && oldSkill.equals(newSkill))
			return false;
		
		// The 2 skills were different (or old wasn't existing). We can refresh the map.
		getSkills().put(newSkill.getId(), newSkill);
		
		// If an old skill has been replaced, remove all its Func objects
		if (oldSkill != null)
		{
			// if skill came with another one, we should delete the other one too.
			if (oldSkill.triggerAnotherSkill())
				removeSkill(oldSkill.getTriggeredId(), false);
			
			removeStatsByOwner(oldSkill);
		}
		
		// Add Func objects of newSkill to the calculator set of the Creature
		addStatFuncs(newSkill.getStatFuncs(this));
		
		// Test and delete chance skill if found.
		if (oldSkill != null && getChanceSkills() != null)
			removeChanceSkill(oldSkill.getId());
		
		// If new skill got a chance, trigger it.
		if (newSkill.isChance())
			addChanceTrigger(newSkill);
		
		// Add or update the skill in the database.
		if (store)
			storeSkill(newSkill, -1);
		
		return true;
	}
	
	/**
	 * Remove a {@link L2Skill} from this {@link Player}. If parameter store is true, we also remove it from database and update shortcuts.
	 * @param skillId : The skill identifier to remove.
	 * @param store : If true, we delete the skill from database.
	 * @return the L2Skill removed or null if it couldn't be removed.
	 */
	public L2Skill removeSkill(int skillId, boolean store)
	{
		return removeSkill(skillId, store, true);
	}
	
	/**
	 * Remove a {@link L2Skill} from this {@link Player}. If parameter store is true, we also remove it from database and update shortcuts.
	 * @param skillId : The skill identifier to remove.
	 * @param store : If true, we delete the skill from database.
	 * @param removeEffect : If true, we remove the associated effect if existing.
	 * @return the L2Skill removed or null if it couldn't be removed.
	 */
	public L2Skill removeSkill(int skillId, boolean store, boolean removeEffect)
	{
		// Remove the skill from the Creature _skills
		final L2Skill oldSkill = getSkills().remove(skillId);
		if (oldSkill == null)
			return null;
		
		// this is just a fail-safe againts buggers and gm dummies...
		if (oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0)
			removeSkill(oldSkill.getTriggeredId(), false);
		
		// Stop casting if this skill is used right now
		if (getLastSkillCast() != null && isCastingNow() && skillId == getLastSkillCast().getId())
			abortCast();
		
		if (getLastSimultaneousSkillCast() != null && isCastingSimultaneouslyNow() && skillId == getLastSimultaneousSkillCast().getId())
			abortCast();
		
		// Remove all its Func objects from the Creature calculator set
		if (removeEffect)
		{
			removeStatsByOwner(oldSkill);
			stopSkillEffects(skillId);
		}
		
		if (oldSkill.isChance() && getChanceSkills() != null)
			removeChanceSkill(skillId);
		
		if (store)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(DELETE_SKILL_FROM_CHAR))
			{
				ps.setInt(1, skillId);
				ps.setInt(2, getObjectId());
				ps.setInt(3, getClassIndex());
				ps.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Couldn't delete player skill.", e);
			}
			
			// Don't busy with shortcuts if skill was a passive skill.
			if (!oldSkill.isPassive())
			{
				for (L2ShortCut sc : getAllShortCuts())
				{
					if (sc != null && sc.getId() == skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
						deleteShortCut(sc.getSlot(), sc.getPage());
				}
			}
		}
		return oldSkill;
	}
	
	/**
	 * Insert or update a {@link Player} skill in the database.<br>
	 * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
	 * @param skill : The skill to add or update (if updated, only the level is refreshed).
	 * @param classIndex : The current class index to set, or current if none is found.
	 */
	private void storeSkill(L2Skill skill, int classIndex)
	{
	    if (isPhantom())
	        return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(ADD_OR_UPDATE_SKILL))
		{
			ps.setInt(1, getObjectId());
			ps.setInt(2, skill.getId());
			ps.setInt(3, skill.getLevel());
			ps.setInt(4, (classIndex > -1) ? classIndex : _classIndex);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Couldn't store player skill.", e);
		}
	}
	
	/**
	 * Restore all skills from database for this {@link Player} and feed getSkills().
	 */
	private void restoreSkills()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR))
		{
			ps.setInt(1, getObjectId());
			ps.setInt(2, getClassIndex());
			
			final ResultSet rs = ps.executeQuery();
			while (rs.next())
				addSkill(SkillTable.getInstance().getInfo(rs.getInt("skill_id"), rs.getInt("skill_level")), false);
			
			rs.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Couldn't restore player skills.", e);
		}
	}
	
	/**
	 * Retrieve from the database all skill effects of this Player and add them to the player.
	 */
	public void restoreEffects()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int effectCount = rset.getInt("effect_count");
				int effectCurTime = rset.getInt("effect_cur_time");
				long reuseDelay = rset.getLong("reuse_delay");
				long systime = rset.getLong("systime");
				int restoreType = rset.getInt("restore_type");
				
				final L2Skill skill = SkillTable.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
				if (skill == null)
					continue;
				
				final long remainingTime = systime - System.currentTimeMillis();
				if (remainingTime > 10)
				{
					disableSkill(skill, remainingTime);
					addTimeStamp(skill, reuseDelay, systime);
				}
				
				/**
				 * Restore Type 1 The remaning skills lost effect upon logout but were still under a high reuse delay.
				 */
				if (restoreType > 0)
					continue;
				
				/**
				 * Restore Type 0 These skills were still in effect on the character upon logout. Some of which were self casted and might still have a long reuse delay which also is restored.
				 */
				if (skill.hasEffects())
				{
					final Env env = new Env();
					env.setCharacter(this);
					env.setTarget(this);
					env.setSkill(skill);
					
					for (EffectTemplate et : skill.getEffectTemplates())
					{
						final L2Effect ef = et.getEffect(env);
						if (ef != null)
						{
							ef.setCount(effectCount);
							ef.setFirstTime(effectCurTime);
							ef.scheduleEffect();
						}
					}
				}
			}
			
			rset.close();
			statement.close();
			
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore " + this + " active effect data: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Retrieve from the database all Henna of this Player, add them to _henna and calculate stats of the Player.
	 */
	private void restoreHenna()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			for (int i = 0; i < 3; i++)
				_henna[i] = null;
			
			while (rset.next())
			{
				int slot = rset.getInt("slot");
				
				if (slot < 1 || slot > 3)
					continue;
				
				int symbolId = rset.getInt("symbol_id");
				if (symbolId != 0)
				{
					Henna tpl = HennaData.getInstance().getHenna(symbolId);
					if (tpl != null)
						_henna[slot - 1] = tpl;
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore henna: " + e);
		}
		
		// Calculate Henna modifiers of this Player
		recalcHennaStats();
	}
	
	/**
	 * Retrieve from the database all Recommendation data of this Player, add to _recomChars and calculate stats of the Player.
	 */
	private void restoreRecom()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_RECOMS);
			statement.setInt(1, getObjectId());
			
			ResultSet rset = statement.executeQuery();
			while (rset.next())
				_recomChars.add(rset.getInt("target_id"));
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore recommendations: " + e);
		}
	}
	
	/**
	 * @return the number of Henna empty slot of the Player.
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots = 0;
		if (getClassId().level() == 1)
			totalSlots = 2;
		else
			totalSlots = 3;
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] != null)
				totalSlots--;
		}
		
		if (totalSlots <= 0)
			return 0;
		
		return totalSlots;
	}
	
	/**
	 * Remove a Henna of the Player, save update in the character_hennas table of the database and send HennaInfo/UserInfo packet to this Player.
	 * @param slot The slot number to make checks on.
	 * @return true if successful.
	 */
	public boolean removeHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return false;
		
		slot--;
		
		if (_henna[slot] == null)
			return false;
		
		Henna henna = _henna[slot];
		_henna[slot] = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);
			
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getClassIndex());
			
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not remove char henna: " + e);
		}
		
		// Calculate Henna modifiers of this Player
		recalcHennaStats();
		
		// Send HennaInfo packet to this Player
		sendPacket(new HennaInfo(this));
		
		// Send UserInfo packet to this Player
		sendPacket(new UserInfo(this));
		
		reduceAdena("Henna", henna.getPrice() / 5, this, false);
		
		// Add the recovered dyes to the player's inventory and notify them.
		addItem("Henna", henna.getDyeId(), Henna.getRequiredDyeAmount() / 2, this, true);
		sendPacket(SystemMessageId.SYMBOL_DELETED);
		return true;
	}
	
	/**
	 * Add a Henna to the Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this Player.
	 * @param henna The Henna template to add.
	 */
	public void addHenna(Henna henna)
	{
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				_henna[i] = henna;
				
				// Calculate Henna modifiers of this Player
				recalcHennaStats();
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);
					
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getClassIndex());
					
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.warning("could not save char henna: " + e);
				}
				
				sendPacket(new HennaInfo(this));
				sendPacket(new UserInfo(this));
				sendPacket(SystemMessageId.SYMBOL_ADDED);
				return;
			}
		}
	}
	
	/**
	 * Calculate Henna modifiers of this Player.
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
				continue;
			
			_hennaINT += _henna[i].getINT();
			_hennaSTR += _henna[i].getSTR();
			_hennaMEN += _henna[i].getMEN();
			_hennaCON += _henna[i].getCON();
			_hennaWIT += _henna[i].getWIT();
			_hennaDEX += _henna[i].getDEX();
		}
		
		if (_hennaINT > 5)
			_hennaINT = 5;
		
		if (_hennaSTR > 5)
			_hennaSTR = 5;
		
		if (_hennaMEN > 5)
			_hennaMEN = 5;
		
		if (_hennaCON > 5)
			_hennaCON = 5;
		
		if (_hennaWIT > 5)
			_hennaWIT = 5;
		
		if (_hennaDEX > 5)
			_hennaDEX = 5;
	}
	
	/**
	 * @param slot A slot to check.
	 * @return the Henna of this Player corresponding to the selected slot.
	 */
	public Henna getHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return null;
		
		return _henna[slot - 1];
	}
	
	public int getHennaStatINT()
	{
		return _hennaINT;
	}
	
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}
	
	public int getHennaStatCON()
	{
		return _hennaCON;
	}
	
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}
	
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}
	
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}
	
	/**
	 * Return True if the Player is autoAttackable.
	 * <ul>
	 * <li>Check if the attacker isn't the Player Pet</li>
	 * <li>Check if the attacker is L2MonsterInstance</li>
	 * <li>If the attacker is a Player, check if it is not in the same party</li>
	 * <li>Check if the Player has Karma</li>
	 * <li>If the attacker is a Player, check if it is not in the same siege clan (Attacker, Defender)</li>
	 * </ul>
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// Check if the attacker isn't the Player Pet
		if (attacker == this || attacker == getPet())
			return false;
		
		// Check if the attacker is a L2MonsterInstance
		if (attacker instanceof Monster)
			return true;
		

		// Check if the attacker is not in the same party
		if (_party != null && _party.containsPlayer(attacker))
			return false;
		
		// Check if the attacker is a L2Playable
		if (attacker instanceof Playable)
		{
			if (isInsideZone(ZoneId.PEACE))
				return false;
			
			// Get Player
			final Player cha = attacker.getActingPlayer();
			
			// Check if the attacker is in olympiad and olympiad start
			if (attacker instanceof Player && cha.isInOlympiadMode())
			{
				if (isInOlympiadMode() && isOlympiadStart() && cha.getOlympiadGameId() == getOlympiadGameId())
					return true;
				
				return false;
			}
			
			// is AutoAttackable if both players are in the same duel and the duel is still going on
			if (getDuelState() == DuelState.DUELLING && getDuelId() == cha.getDuelId())
				return true;
			
			if (getClan() != null)
			{
				final Siege siege = CastleManager.getInstance().getActiveSiege(this);
				if (siege != null)
				{
					// Check if a siege is in progress and if attacker and the Player aren't in the Defender clan
					if (siege.checkSides(cha.getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER) && siege.checkSides(getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER))
						return false;
					
					// Check if a siege is in progress and if attacker and the Player aren't in the Attacker clan
					if (siege.checkSide(cha.getClan(), SiegeSide.ATTACKER) && siege.checkSide(getClan(), SiegeSide.ATTACKER))
						return false;
				}
				
				// Check if clan is at war
				if (getClan().isAtWarWith(cha.getClanId()) && !wantsPeace() && !cha.wantsPeace() && !isAcademyMember())
					return true;
			}
			
			// Check if the Player is in an arena.
			if (isInArena() && attacker.isInArena())
				return true;
			
			// Check if the attacker is not in the same ally.
			if (getAllyId() != 0 && getAllyId() == cha.getAllyId())
				return false;
			
			// Check if the attacker is not in the same clan.
			if (getClan() != null && getClan().isMember(cha.getObjectId()))
				return false;
			
			// Check if the attacker is in TvT and TvT is started
			if (TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(getObjectId()))
				return true;
			
			// Now check again if the Player is in pvp zone (as arenas check was made before, it ends with sieges).
			if (isInsideZone(ZoneId.PVP) && attacker.isInsideZone(ZoneId.PVP))
				return true;
		}
		else if (attacker instanceof SiegeGuard)
		{
			if (getClan() != null)
			{
				final Siege siege = CastleManager.getInstance().getActiveSiege(this);
				return (siege != null && siege.checkSide(getClan(), SiegeSide.ATTACKER));
			}
		}
		
		// Check if the Player has Karma
		if (getKarma() > 0 || getPvpFlag() > 0)
			return true;
		
		return false;
	}
	
	/**
	 * Check if the active L2Skill can be casted.
	 * <ul>
	 * <li>Check if the skill isn't toggle and is offensive</li>
	 * <li>Check if the target is in the skill cast range</li>
	 * <li>Check if the skill is Spoil type and if the target isn't already spoiled</li>
	 * <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill</li>
	 * <li>Check if the caster isn't sitting</li>
	 * <li>Check if all skills are enabled and this skill is enabled</li>
	 * <li>Check if the caster own the weapon needed</li>
	 * <li>Check if the skill is active</li>
	 * <li>Check if all casting conditions are completed</li>
	 * <li>Notify the AI with CAST and target</li>
	 * </ul>
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	@Override
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		// Check if the skill is active
		if (skill.isPassive())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if this skill is enabled (ex : reuse time)
		if (isSkillDisabled(skill))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill));
			return false;
		}
		
		// Cancels the use of skills when player uses a cursed weapon or is flying.
		if ((isCursedWeaponEquipped() && !skill.isDemonicSkill()) // If CW, allow ONLY demonic skills.
			|| (getMountType() == 1 && !skill.isStriderSkill()) // If mounted, allow ONLY Strider skills.
			|| (getMountType() == 2 && !skill.isFlyingSkill())) // If flying, allow ONLY Wyvern skills.
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Players wearing Formal Wear cannot use skills.
		final ItemInstance formal = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if (formal != null && formal.getItem().getBodyPart() == Item.SLOT_ALLDRESS)
		{
			sendPacket(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// ************************************* Check Casting in Progress *******************************************
		
		// If a skill is currently being used, queue this one if this is not the same
		if (isCastingNow())
		{
			// Check if new skill different from current skill in progress ; queue it in the player _queuedSkill
			if (_currentSkill.getSkill() != null && skill.getId() != _currentSkill.getSkillId())
				setQueuedSkill(skill, forceUse, dontMove);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		setIsCastingNow(true);
		
		// Set the player _currentSkill.
		setCurrentSkill(skill, forceUse, dontMove);
		
		// Wipe queued skill.
		if (_queuedSkill.getSkill() != null)
			setQueuedSkill(null, false, false);
		
		if (!checkUseMagicConditions(skill, forceUse, dontMove))
		{
			setIsCastingNow(false);
			return false;
		}
		
		// Check if the target is correct and Notify the AI with CAST and target
		WorldObject target = null;
		
		switch (skill.getTargetType())
		{
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_AURA_UNDEAD:
				target = this;
				break;
			
			default: // Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		// Notify the AI with CAST and target
		getAI().setIntention(CtrlIntention.CAST, skill, target);
		return true;
	}
	
	private boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		// ************************************* Check Player State *******************************************
		
		// Check if the player is dead or out of control.
		if (isDead() || isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		L2SkillType sklType = skill.getSkillType();
		
		if (isFishing() && (sklType != L2SkillType.PUMPING && sklType != L2SkillType.REELING && sklType != L2SkillType.FISHING))
		{
			// Only fishing skills are available
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return false;
		}
		
		if (isInObserverMode())
		{
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster is sitted.
		if (isSitting())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			
			// Send ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the skill type is TOGGLE
		if (skill.isToggle())
		{
			// Get effects of the skill
			L2Effect effect = getFirstEffect(skill.getId());
			
			if (effect != null)
			{
				// If the toggle is different of FakeDeath, you can de-activate it clicking on it.
				if (skill.getId() != 60)
					effect.exit();
				
				// Send ActionFailed to the Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		// Check if the player uses "Fake Death" skill
		if (isFakeDeath())
		{
			// Send ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// ************************************* Check Target *******************************************
		// Create and set a WorldObject containing the target of the skill
		WorldObject target = null;
		SkillTargetType sklTargetType = skill.getTargetType();
		Location worldPosition = getCurrentSkillWorldPosition();
		
		if (sklTargetType == SkillTargetType.TARGET_GROUND && worldPosition == null)
		{
			_log.info("WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		switch (sklTargetType)
		{
			// Target the player if skill type is AURA, PARTY, CLAN or SELF
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_AREA_SUMMON:
				target = this;
				break;
			case TARGET_PET:
			case TARGET_SUMMON:
				target = getPet();
				break;
			default:
				target = getTarget();
				break;
		}
		
		// Check the validity of the target
		if (target == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (target instanceof Door)
		{
			if (!((Door) target).isAutoAttackable(this) // Siege doors only hittable during siege
				|| (((Door) target).isUnlockable() && skill.getSkillType() != L2SkillType.UNLOCK)) // unlockable doors
			{
				sendPacket(SystemMessageId.INCORRECT_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		// Are the target and the player in the same duel?
		if (isInDuel())
		{
			if (target instanceof Playable)
			{
				// Get Player
				Player cha = target.getActingPlayer();
				if (cha.getDuelId() != getDuelId())
				{
					sendPacket(SystemMessageId.INCORRECT_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Siege summon checks. Both checks send a message to the player if it return false.
		if (skill.isSiegeSummonSkill())
		{
			final Siege siege = CastleManager.getInstance().getActiveSiege(this);
			if (siege == null || !siege.checkSide(getClan(), SiegeSide.ATTACKER) || (isInSiege() && isInsideZone(ZoneId.CASTLE)))
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_CALL_PET_FROM_THIS_LOCATION));
				return false;
			}
			
			if (SevenSigns.getInstance().isSealValidationPeriod() && SevenSigns.getInstance().getSealOwner(SealType.STRIFE) == CabalType.DAWN && SevenSigns.getInstance().getPlayerCabal(getObjectId()) == CabalType.DUSK)
			{
				sendPacket(SystemMessageId.SEAL_OF_STRIFE_FORBIDS_SUMMONING);
				return false;
			}
		}
		
		// ************************************* Check casting conditions *******************************************
		
		// Check if all casting conditions are completed
		if (!skill.checkCondition(this, target, false))
		{
			// Send ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// ************************************* Check Skill Type *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(this, target))
			{
				// If Creature or target is in a peace zone, send a system message TARGET_IN_PEACEZONE ActionFailed
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (isInOlympiadMode() && !isOlympiadStart())
			{
				// if Player is in Olympia and the match isn't already start, send ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			// Check if the target is attackable
			if (!target.isAttackable() && !getAccessLevel().allowPeaceAttack())
			{
				// If target is not attackable, send ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			// Check if a Forced ATTACK is in progress on non-attackable target
			if (!target.isAutoAttackable(this) && !forceUse)
			{
				switch (sklTargetType)
				{
					case TARGET_AURA:
					case TARGET_FRONT_AURA:
					case TARGET_BEHIND_AURA:
					case TARGET_AURA_UNDEAD:
					case TARGET_CLAN:
					case TARGET_ALLY:
					case TARGET_PARTY:
					case TARGET_SELF:
					case TARGET_GROUND:
					case TARGET_CORPSE_ALLY:
					case TARGET_AREA_SUMMON:
						break;
					default: // Send ActionFailed to the Player
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
				}
			}
			
			// Check if the target is in the skill cast range
			if (dontMove)
			{
				// Calculate the distance between the Player and the target
				if (sklTargetType == SkillTargetType.TARGET_GROUND)
				{
					if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), (int) (skill.getCastRange() + getCollisionRadius()), false, false))
					{
						// Send a System Message to the caster
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						
						// Send ActionFailed to the Player
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				}
				else if (skill.getCastRange() > 0 && !isInsideRadius(target, (int) (skill.getCastRange() + getCollisionRadius()), false, false))
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.TARGET_TOO_FAR);
					
					// Send ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		// Check if the skill is defensive
		if (!skill.isOffensive() && target instanceof Monster && !forceUse)
		{
			// check if the target is a monster and if force attack is set.. if not then we don't want to cast.
			switch (sklTargetType)
			{
				case TARGET_PET:
				case TARGET_SUMMON:
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_AURA_UNDEAD:
				case TARGET_CLAN:
				case TARGET_SELF:
				case TARGET_CORPSE_ALLY:
				case TARGET_PARTY:
				case TARGET_ALLY:
				case TARGET_CORPSE_MOB:
				case TARGET_AREA_CORPSE_MOB:
				case TARGET_GROUND:
					break;
				default:
				{
					switch (sklType)
					{
						case BEAST_FEED:
						case DELUXE_KEY_UNLOCK:
						case UNLOCK:
							break;
						default:
							sendPacket(ActionFailed.STATIC_PACKET);
							return false;
					}
					break;
				}
			}
		}
		
		// Check if the skill is Spoil type and if the target isn't already spoiled
		if (sklType == L2SkillType.SPOIL)
		{
			if (!(target instanceof Monster))
			{
				// Send a System Message to the Player
				sendPacket(SystemMessageId.INCORRECT_TARGET);
				
				// Send ActionFailed to the Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		// Check if the skill is Sweep type and if conditions not apply
		if (sklType == L2SkillType.SWEEP && target instanceof Attackable)
		{
			if (((Attackable) target).isDead())
			{
				final int spoilerId = ((Attackable) target).getSpoilerId();
				if (spoilerId == 0)
				{
					// Send a System Message to the Player
					sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
					
					// Send ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				
				if (!isLooterOrInLooterParty(spoilerId))
				{
					// Send a System Message to the Player
					sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED);
					
					// Send ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		// Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
		if (sklType == L2SkillType.DRAIN_SOUL)
		{
			if (!(target instanceof Monster))
			{
				// Send a System Message to the Player
				sendPacket(SystemMessageId.INCORRECT_TARGET);
				
				// Send ActionFailed to the Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch (sklTargetType)
		{
			case TARGET_PARTY:
			case TARGET_ALLY: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_AREA_SUMMON:
				break;
			default:
				if (!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack())
				{
					// Send a System Message to the Player
					sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					
					// Send ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
		}
		
		if ((sklTargetType == SkillTargetType.TARGET_HOLY && !checkIfOkToCastSealOfRule(CastleManager.getInstance().getCastle(this), false, skill, target)) || (sklType == L2SkillType.SIEGEFLAG && !L2SkillSiegeFlag.checkIfOkToPlaceFlag(this, false)) || (sklType == L2SkillType.STRSIEGEASSAULT && !checkIfOkToUseStriderSiegeAssault(skill)) || (sklType == L2SkillType.SUMMON_FRIEND && !(checkSummonerStatus(this) && checkSummonTargetStatus(target, this))))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return false;
		}
		
		// GeoData Los Check here
		if (skill.getCastRange() > 0)
		{
			if (sklTargetType == SkillTargetType.TARGET_GROUND)
			{
				if (!GeoEngine.getInstance().canSeeTarget(this, worldPosition))
				{
					sendPacket(SystemMessageId.CANT_SEE_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else if (!GeoEngine.getInstance().canSeeTarget(this, target))
			{
				sendPacket(SystemMessageId.CANT_SEE_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		// finally, after passing all conditions
		return true;
	}
	
	public boolean checkIfOkToUseStriderSiegeAssault(L2Skill skill)
	{
		final Siege siege = CastleManager.getInstance().getActiveSiege(this);
		
		SystemMessage sm;
		
		if (!isRiding())
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
		else if (!(getTarget() instanceof Door))
			sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
		else if (siege == null || !siege.checkSide(getClan(), SiegeSide.ATTACKER))
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
		else
			return true;
		
		sendPacket(sm);
		return false;
	}
	
	public boolean checkIfOkToCastSealOfRule(Castle castle, boolean isCheckOnly, L2Skill skill, WorldObject target)
	{
		SystemMessage sm;
		
		if (castle == null || castle.getCastleId() <= 0)
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
		else if (!castle.isGoodArtifact(target))
			sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
		else if (!castle.getSiege().isInProgress())
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
		else if (!MathUtil.checkIfInRange(200, this, target, true))
			sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
		else if (!isInsideZone(ZoneId.CAST_ON_ARTIFACT))
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
		else if (!castle.getSiege().checkSide(getClan(), SiegeSide.ATTACKER))
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
		else
		{
			if (!isCheckOnly)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING);
				castle.getSiege().announceToPlayers(sm, false);
			}
			return true;
		}
		sendPacket(sm);
		return false;
	}
	
	/**
	 * @param objectId : The looter object to make checks on.
	 * @return true if the active player is the looter or in the same party or command channel than looter objectId.
	 */
	public boolean isLooterOrInLooterParty(int objectId)
	{
		if (objectId == getObjectId())
			return true;
		
		final Player looter = World.getInstance().getPlayer(objectId);
		if (looter == null)
			return false;
		
		if (_party == null)
			return false;
		
		final CommandChannel channel = _party.getCommandChannel();
		return (channel != null) ? channel.containsPlayer(looter) : _party.containsPlayer(looter);
	}
	
	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 * @param target WorldObject instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @return {@code false} if the skill is a pvpSkill and target is not a valid pvp target, {@code true} otherwise.
	 */
	public boolean checkPvpSkill(WorldObject target, L2Skill skill)
	{
		if (skill == null || target == null)
			return false;
		
		if (!(target instanceof Playable))
			return true;
		
		if (skill.isDebuff() || skill.isOffensive())
		{
			final Player targetPlayer = target.getActingPlayer();
			if (targetPlayer == null || this == target)
				return false;
			
			// Peace Zone
			if (target.isInsideZone(ZoneId.PEACE))
				return false;
			
			// Duel
			if (isInDuel() && targetPlayer.isInDuel() && getDuelId() == targetPlayer.getDuelId())
				return true;
			
			final boolean isCtrlPressed = getCurrentSkill() != null && getCurrentSkill().isCtrlPressed();
			
			// Party
			if (isInParty() && targetPlayer.isInParty())
			{
				// Same Party
				if (getParty().getLeader() == targetPlayer.getParty().getLeader())
				{
					if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isDamage())
						return true;
					
					return false;
				}
				else if (getParty().getCommandChannel() != null && getParty().getCommandChannel().containsPlayer(targetPlayer))
				{
					if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isDamage())
						return true;
					
					return false;
				}
			}
			
			// You can debuff anyone except party members while in an arena...
			if (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP))
				return true;
			
			// Olympiad
			if (isInOlympiadMode() && targetPlayer.isInOlympiadMode() && getOlympiadGameId() == targetPlayer.getOlympiadGameId())
				return true;
			
			final Clan aClan = getClan();
			final Clan tClan = targetPlayer.getClan();
			
			if (aClan != null && tClan != null)
			{
				if (aClan.isAtWarWith(tClan.getClanId()) && tClan.isAtWarWith(aClan.getClanId()))
				{
					// Check if skill can do dmg
					if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isAOE())
						return true;
					
					return isCtrlPressed;
				}
				else if (getClanId() == targetPlayer.getClanId() || (getAllyId() > 0 && getAllyId() == targetPlayer.getAllyId()))
				{
					// Check if skill can do dmg
					if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isDamage())
						return true;
					
					return false;
				}
			}
			
			// On retail, it is impossible to debuff a "peaceful" player.
			if (targetPlayer.getPvpFlag() == 0 && targetPlayer.getKarma() == 0)
			{
				// Check if skill can do dmg
				if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isDamage())
					return true;
				
				return false;
			}
			
			if (targetPlayer.getPvpFlag() > 0 || targetPlayer.getKarma() > 0)
				return true;
			
			return false;
		}
		return true;
	}
	
	/**
	 * @return True if the Player is a Mage (based on class templates).
	 */
	public boolean isMageClass()
	{
		return getClassId().getType() != ClassType.FIGHTER;
	}
	
	/**
	 * @return True if the Player is a Mage (based on class templates).
	 */
	public boolean isFightClass()
	{
		return getClassId().getType() == ClassType.FIGHTER;
	}
	
	public boolean isMounted()
	{
		return _mountType > 0;
	}
	
	/**
	 * This method allows to :
	 * <ul>
	 * <li>change isRiding/isFlying flags</li>
	 * <li>gift player with Wyvern Breath skill if mount is a wyvern</li>
	 * <li>send the skillList (faded icons update)</li>
	 * </ul>
	 * @param npcId the npcId of the mount
	 * @param npcLevel The level of the mount
	 * @param mountType 0, 1 or 2 (dismount, strider or wyvern).
	 * @return always true.
	 */
	public boolean setMount(int npcId, int npcLevel, int mountType)
	{
		switch (mountType)
		{
			case 0: // Dismounted
				if (isFlying())
					removeSkill(FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);
				break;
			
			case 2: // Flying Wyvern
				addSkill(FrequentSkill.WYVERN_BREATH.getSkill(), false);
				break;
		}
		
		_mountNpcId = npcId;
		_mountType = mountType;
		_mountLevel = npcLevel;
		
		sendSkillList(); // Update faded icons && eventual added skills.
		return true;
	}
	
	@Override
	public boolean isSeated()
	{
		return _throneId > 0;
	}
	
	@Override
	public boolean isRiding()
	{
		return _mountType == 1;
	}
	
	@Override
	public boolean isFlying()
	{
		return _mountType == 2;
	}
	
	/**
	 * @return the type of Pet mounted (0 : none, 1 : Strider, 2 : Wyvern).
	 */
	public int getMountType()
	{
		return _mountType;
	}
	
	@Override
	public final void stopAllEffects()
	{
		super.stopAllEffects();
		updateAndBroadcastStatus(2);
	}
	
	@Override
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		updateAndBroadcastStatus(2);
	}
	
	/**
	 * Stop all toggle-type effects
	 */
	public final void stopAllToggles()
	{
		_effects.stopAllToggles();
	}
	
	public final void stopCubics()
	{
		if (getCubics() != null)
		{
			boolean removed = false;
			for (Cubic cubic : getCubics().values())
			{
				cubic.stopAction();
				delCubic(cubic.getId());
				removed = true;
			}
			if (removed)
				broadcastUserInfo();
		}
	}
	
	public final void stopCubicsByOthers()
	{
		if (getCubics() != null)
		{
			boolean removed = false;
			for (Cubic cubic : getCubics().values())
			{
				if (cubic.givenByOther())
				{
					cubic.stopAction();
					delCubic(cubic.getId());
					removed = true;
				}
			}
			if (removed)
				broadcastUserInfo();
		}
	}
	
	/**
	 * Send UserInfo to this Player and CharInfo to all Player in its _KnownPlayers.<BR>
	 * <ul>
	 * <li>Send UserInfo to this Player (Public and Private Data)</li>
	 * <li>Send CharInfo to all Player in _KnownPlayers of the Player (Public data only)</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR>
	 * <BR>
	 */
	@Override
	public void updateAbnormalEffect()
	{
		broadcastUserInfo();
	}
	
	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.
	 */
	public void tempInventoryDisable()
	{
		_inventoryDisable = true;
		
		ThreadPool.schedule(() -> _inventoryDisable = false, 1500);
	}
	
	/**
	 * @return True if the Inventory is disabled.
	 */
	public boolean isInventoryDisabled()
	{
		return _inventoryDisable;
	}
	
	public Map<Integer, Cubic> getCubics()
	{
		return _cubics;
	}
	
	/**
	 * Add a L2CubicInstance to the Player _cubics.
	 * @param id
	 * @param level
	 * @param matk
	 * @param activationtime
	 * @param activationchance
	 * @param totalLifetime
	 * @param givenByOther
	 */
	public void addCubic(int id, int level, double matk, int activationtime, int activationchance, int totalLifetime, boolean givenByOther)
	{
		_cubics.put(id, new Cubic(this, id, level, (int) matk, activationtime, activationchance, totalLifetime, givenByOther));
	}
	
	/**
	 * Remove a L2CubicInstance from the Player _cubics.
	 * @param id
	 */
	public void delCubic(int id)
	{
		_cubics.remove(id);
	}
	
	/**
	 * @param id
	 * @return the L2CubicInstance corresponding to the Identifier of the Player _cubics.
	 */
	public Cubic getCubic(int id)
	{
		return _cubics.get(id);
	}
	
	@Override
	public String toString()
	{
		return "player " + getName();
	}
	
	/**
	 * @return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).
	 */
	public int getEnchantEffect()
	{
		final ItemInstance wpn = getActiveWeaponInstance();
		return (wpn == null) ? 0 : Math.min(127, wpn.getEnchantLevel());
	}
	
	/**
	 * Remember the current {@link Folk} of the {@link Player}, used notably for integrity check.
	 * @param folk : The Folk to remember.
	 */
	public void setCurrentFolk(Folk folk)
	{
		_currentFolk = folk;
	}
	
	/**
	 * @return the current {@link Folk} of the {@link Player}.
	 */
	public Folk getCurrentFolk()
	{
		return _currentFolk;
	}
	
	/**
	 * @return True if Player is a participant in the Festival of Darkness.
	 */
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isParticipant(this);
	}
	
	public void addAutoSoulShot(int itemId)
	{
		_activeSoulShots.add(itemId);
	}
	
	public boolean removeAutoSoulShot(int itemId)
	{
		return _activeSoulShots.remove(itemId);
	}
	
	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.isChargedShot(type);
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		ItemInstance weapon = getActiveWeaponInstance();
		if (weapon != null)
			weapon.setChargedShot(type, charged);
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic)
	{
		if (_activeSoulShots.isEmpty())
			return;
		
		for (int itemId : _activeSoulShots)
		{
			ItemInstance item = getInventory().getItemByItemId(itemId);
			if (item != null)
			{
				if (magic && item.getItem().getDefaultAction() == ActionType.spiritshot)
				{
					IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
					if (handler != null)
						handler.useItem(this, item, false);
				}
				
				if (physical && item.getItem().getDefaultAction() == ActionType.soulshot)
				{
					IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
					if (handler != null)
						handler.useItem(this, item, false);
				}
			}
			else
				removeAutoSoulShot(itemId);
		}
	}
	
	/**
	 * Cancel autoshot use for shot itemId
	 * @param itemId int id to disable
	 * @return true if canceled.
	 */
	public boolean disableAutoShot(int itemId)
	{
		if (_activeSoulShots.contains(itemId))
		{
			removeAutoSoulShot(itemId);
			sendPacket(new ExAutoSoulShot(itemId, 0));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
			return true;
		}
		
		return false;
	}
	
	/**
	 * Cancel all autoshots for player
	 */
	public void disableAutoShotsAll()
	{
		for (int itemId : _activeSoulShots)
		{
			sendPacket(new ExAutoSoulShot(itemId, 0));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
		}
		_activeSoulShots.clear();
	}
	
	class LookingForFishTask implements Runnable
	{
		boolean _isNoob, _isUpperGrade;
		int _fishType, _fishGutsCheck;
		long _endTaskTime;
		
		protected LookingForFishTask(int fishWaitTime, int fishGutsCheck, int fishType, boolean isNoob, boolean isUpperGrade)
		{
			_fishGutsCheck = fishGutsCheck;
			_endTaskTime = System.currentTimeMillis() + fishWaitTime + 10000;
			_fishType = fishType;
			_isNoob = isNoob;
			_isUpperGrade = isUpperGrade;
		}
		
		@Override
		public void run()
		{
			if (System.currentTimeMillis() >= _endTaskTime)
			{
				endFishing(false);
				return;
			}
			
			if (_fishType == -1)
				return;
			
			int check = Rnd.get(1000);
			if (_fishGutsCheck > check)
			{
				stopLookingForFishTask();
				startFishCombat(_isNoob, _isUpperGrade);
			}
		}
	}
	
	public int getClanPrivileges()
	{
		return _clanPrivileges;
	}
	
	public void setClanPrivileges(int n)
	{
		_clanPrivileges = n;
	}
	
	public int getPledgeClass()
	{
		return _pledgeClass;
	}
	
	public void setPledgeClass(int classId)
	{
		_pledgeClass = classId;
	}
	
	public int getPledgeType()
	{
		return _pledgeType;
	}
	
	public void setPledgeType(int typeId)
	{
		_pledgeType = typeId;
	}
	
	public int getApprentice()
	{
		return _apprentice;
	}
	
	public void setApprentice(int id)
	{
		_apprentice = id;
	}
	
	public int getSponsor()
	{
		return _sponsor;
	}
	
	public void setSponsor(int id)
	{
		_sponsor = id;
	}
	
	@Override
	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}
	
	/**
	 * Unsummon all types of summons : pets, cubics, normal summons and trained beasts.
	 */
	public void dropAllSummons()
	{
		// Delete summons and pets
		if (getPet() != null)
			getPet().unSummon(this);
		
		// Delete trained beasts
		if (getTrainedBeast() != null)
			getTrainedBeast().deleteMe();
		
		// Delete any form of cubics
		stopCubics();
	}
	
	public void enterObserverMode(int x, int y, int z)
	{
		dropAllSummons();
		
		if (getParty() != null)
			getParty().removePartyMember(this, MessageType.EXPELLED);
		
		standUp();
		
		_savedLocation.set(getPosition());
		
		setTarget(null);
		setIsInvul(true);
		getAppearance().setInvisible();
		setIsParalyzed(true);
		startParalyze();
		
		teleToLocation(x, y, z, 0);
		sendPacket(new ObservationMode(x, y, z));
	}
	
	public void enterOlympiadObserverMode(int id)
	{
		final OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(id);
		if (task == null)
			return;
		
		dropAllSummons();
		
		if (getParty() != null)
			getParty().removePartyMember(this, MessageType.EXPELLED);
		
		_olympiadGameId = id;
		
		standUp();
		
		// Don't override saved location if we jump from stadium to stadium.
		if (!isInObserverMode())
			_savedLocation.set(getPosition());
		
		setTarget(null);
		setIsInvul(true);
		getAppearance().setInvisible();
		
		teleToLocation(task.getZone().getSpawns().get(2), 0);
		sendPacket(new ExOlympiadMode(3));
	}
	
	public void leaveObserverMode()
	{
		if (hasAI())
			getAI().setIntention(CtrlIntention.IDLE);
		
		setTarget(null);
		getAppearance().setVisible();
		setIsInvul(false);
		setIsParalyzed(false);
		stopParalyze(false);
		
		sendPacket(new ObservationReturn(_savedLocation));
		teleToLocation(_savedLocation, 0);
		
		// Clear the location.
		_savedLocation.clean();
	}
	
	public void leaveOlympiadObserverMode()
	{
		if (_olympiadGameId == -1)
			return;
		
		_olympiadGameId = -1;
		
		if (hasAI())
			getAI().setIntention(CtrlIntention.IDLE);
		
		setTarget(null);
		getAppearance().setVisible();
		setIsInvul(false);
		
		sendPacket(new ExOlympiadMode(0));
		teleToLocation(_savedLocation, 0);
		
		// Clear the location.
		_savedLocation.clean();
	}
	
	public int getOlympiadSide()
	{
		return _olympiadSide;
	}
	
	public void setOlympiadSide(int i)
	{
		_olympiadSide = i;
	}
	
	public int getOlympiadGameId()
	{
		return _olympiadGameId;
	}
	
	public void setOlympiadGameId(int id)
	{
		_olympiadGameId = id;
	}
	
	public Location getSavedLocation()
	{
		return _savedLocation;
	}
	
	public boolean isInObserverMode()
	{
		return !_isInOlympiadMode && !_savedLocation.equals(Location.DUMMY_LOC);
	}
	
	public int getTeleMode()
	{
		return _teleMode;
	}
	
	public void setTeleMode(int mode)
	{
		_teleMode = mode;
	}
	
	public int getLoto(int i)
	{
		return _loto[i];
	}
	
	public void setLoto(int i, int val)
	{
		_loto[i] = val;
	}
	
	public int getRace(int i)
	{
		return _race[i];
	}
	
	public void setRace(int i, int val)
	{
		_race[i] = val;
	}
	
	public boolean isInRefusalMode()
	{
		return _messageRefusal;
	}
	
	public void setInRefusalMode(boolean mode)
	{
		_messageRefusal = mode;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void setTradeRefusal(boolean mode)
	{
		_tradeRefusal = mode;
	}
	
	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}
	
	public void setExchangeRefusal(boolean mode)
	{
		_exchangeRefusal = mode;
	}
	
	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}
	
	public BlockList getBlockList()
	{
		return _blockList;
	}
	
	public boolean isHero()
	{
		return _isHero;
	}
	
	public void setHero(boolean hero)
	{
		if (hero && _baseClass == _activeClass)
		{
			for (L2Skill skill : SkillTable.getHeroSkills())
				addSkill(skill, false);
		}
		else if (hero && Config.ALLOW_HERO_SUBSKILL)
		{
			for (L2Skill skill : SkillTable.getHeroSkills())
				addSkill(skill, false);
		}
		else
		{
			for (L2Skill skill : SkillTable.getHeroSkills())
				removeSkill(skill.getId(), false);
		}
		_isHero = hero;
		
		sendSkillList();
	}
	
	public boolean isOlympiadStart()
	{
		return _isInOlympiadStart;
	}
	
	public void setOlympiadStart(boolean b)
	{
		_isInOlympiadStart = b;
	}
	
	public boolean isInOlympiadMode()
	{
		return _isInOlympiadMode;
	}
	
	public void setOlympiadMode(boolean b)
	{
		_isInOlympiadMode = b;
	}
	
	public boolean isInDuel()
	{
		return _duelId > 0;
	}
	
	public int getDuelId()
	{
		return _duelId;
	}
	
	public void setDuelState(DuelState state)
	{
		_duelState = state;
	}
	
	public DuelState getDuelState()
	{
		return _duelState;
	}
	
	/**
	 * Sets up the duel state using a non 0 duelId.
	 * @param duelId 0=not in a duel
	 */
	public void setInDuel(int duelId)
	{
		if (duelId > 0)
		{
			_duelState = DuelState.ON_COUNTDOWN;
			_duelId = duelId;
		}
		else
		{
			if (_duelState == DuelState.DEAD)
			{
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			_duelState = DuelState.NO_DUEL;
			_duelId = 0;
		}
	}
	
	/**
	 * This returns a SystemMessage stating why the player is not available for duelling.
	 * @return S1_CANNOT_DUEL... message
	 */
	public SystemMessage getNoDuelReason()
	{
		// Prepare the message with the good reason.
		final SystemMessage sm = SystemMessage.getSystemMessage(_noDuelReason).addCharName(this);
		
		// Reinitialize the reason.
		_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
		
		// Send stored reason.
		return sm;
	}
	
	/**
	 * Checks if this player might join / start a duel. To get the reason use getNoDuelReason() after calling this function.
	 * @return true if the player might join/start a duel.
	 */
	public boolean canDuel()
	{
		if (isInCombat() || getPunishLevel() == PunishLevel.JAIL)
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
		else if (isDead() || isAlikeDead() || (getCurrentHp() < getMaxHp() / 2 || getCurrentMp() < getMaxMp() / 2))
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_HP_OR_MP_IS_BELOW_50_PERCENT;
		else if (isInDuel())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
		else if (isInOlympiadMode())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
		else if (isCursedWeaponEquipped() || getKarma() != 0)
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
		else if (isInStoreMode())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
		else if (isMounted() || isInBoat())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
		else if (isFishing())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
		else if (isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.PEACE) || isInsideZone(ZoneId.SIEGE))
			_noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
		else
			return true;
		
		return false;
	}
	
	public boolean isNoble()
	{
		return _isNoble;
	}
	
	/**
	 * Set Noblesse Status, and reward with nobles' skills.
	 * @param val Add skills if setted to true, else remove skills.
	 * @param store Store the status directly in the db if setted to true.
	 */
	public void setNoble(boolean val, boolean store)
	{
		if (val)
			for (L2Skill skill : SkillTable.getNobleSkills())
				addSkill(skill, false);
		else
			for (L2Skill skill : SkillTable.getNobleSkills())
				removeSkill(skill.getId(), false);
			
		_isNoble = val;
		
		sendSkillList();
		
		if (store)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement(UPDATE_NOBLESS);
				statement.setBoolean(1, val);
				statement.setInt(2, getObjectId());
				statement.executeUpdate();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not update " + getName() + " nobless status: " + e.getMessage(), e);
			}
		}
	}
	
	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}
	
	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}
	
	public boolean isAcademyMember()
	{
		return _lvlJoinedAcademy > 0;
	}
	
	public void setTeam(int team)
	{
		_team = team;
	}
	
	public int getTeam()
	{
		return _team;
	}
	
	public void setWantsPeace(boolean wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}
	
	public boolean wantsPeace()
	{
		return _wantsPeace;
	}
	
	public boolean isFishing()
	{
		return _fishingLoc != null;
	}
	
	public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
	{
		_alliedVarkaKetra = sideAndLvlOfAlliance;
	}
	
	/**
	 * [-5,-1] varka, 0 neutral, [1,5] ketra
	 * @return the side faction.
	 */
	public int getAllianceWithVarkaKetra()
	{
		return _alliedVarkaKetra;
	}
	
	public boolean isAlliedWithVarka()
	{
		return _alliedVarkaKetra < 0;
	}
	
	public boolean isAlliedWithKetra()
	{
		return _alliedVarkaKetra > 0;
	}
	
	public void sendSkillList()
	{
		final ItemInstance formal = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		final boolean isWearingFormalWear = formal != null && formal.getItem().getBodyPart() == Item.SLOT_ALLDRESS;
		final SkillList sl = new SkillList();
		
		for (L2Skill s : getSkills().values())
		{
			if (isWearingFormalWear)
				sl.addSkill(s.getId(), s.getLevel(), s.isPassive(), true);
			else
			{
				boolean isDisabled = false;
				if (getClan() != null)
					isDisabled = s.isClanSkill() && getClan().getReputationScore() < 0;
				
				if (isCursedWeaponEquipped()) // Only Demonic skills are available
					isDisabled = !s.isDemonicSkill();
				else if (isMounted()) // else if, because only ONE state is possible
				{
					if (getMountType() == 1) // Only Strider skills are available
						isDisabled = !s.isStriderSkill();
					else if (getMountType() == 2) // Only Wyvern skills are available
						isDisabled = !s.isFlyingSkill();
				}
				sl.addSkill(s.getId(), s.getLevel(), s.isPassive(), isDisabled);
			}
		}
		sendPacket(sl);
	}
	
	/**
	 * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>) for this character.<BR>
	 * 2. This method no longer changes the active _classIndex of the player. This is only done by the calling of setActiveClass() method as that should be the only way to do so.
	 * @param classId
	 * @param classIndex
	 * @return boolean subclassAdded
	 */
	public boolean addSubClass(int classId, int classIndex)
	{
		   removeItens();
		
		if (!_subclassLock.tryLock())
			return false;
		
		try
		{
			if (_subClasses.size() == 3 || classIndex == 0 || _subClasses.containsKey(classIndex))
				return false;
			
			final SubClass subclass = new SubClass(classId, classIndex);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(ADD_CHAR_SUBCLASS))
			{
				ps.setInt(1, getObjectId());
				ps.setInt(2, subclass.getClassId());
				ps.setLong(3, subclass.getExp());
				ps.setInt(4, subclass.getSp());
				ps.setInt(5, subclass.getLevel());
				ps.setInt(6, subclass.getClassIndex());
				ps.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Couldn't add subclass for " + getName(), e);
				return false;
			}
			
			_subClasses.put(subclass.getClassIndex(), subclass);
			
			PlayerData.getInstance().getTemplate(classId).getSkills().stream().filter(s -> s.getMinLvl() <= 40).collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL))).forEach((i, s) ->
			{
				storeSkill(s.get().getSkill(), classIndex);
			});
			
			return true;
		}
		finally
		{
			_subclassLock.unlock();
		}
	}
	
	/**
	 * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
	 * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR>
	 * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
	 * @param classIndex
	 * @param newClassId
	 * @return boolean subclassAdded
	 */
	public boolean modifySubClass(int classIndex, int newClassId)
	{
		if (!_subclassLock.tryLock())
			return false;
		
		try
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				// Remove all henna info stored for this sub-class.
				PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNAS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
				
				// Remove all shortcuts info stored for this sub-class.
				statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
				
				// Remove all effects info stored for this sub-class.
				statement = con.prepareStatement(DELETE_SKILL_SAVE);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
				
				// Remove all skill info stored for this sub-class.
				statement = con.prepareStatement(DELETE_CHAR_SKILLS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
				
				// Remove all basic info stored about this sub-class.
				statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warning("Could not modify subclass for " + getName() + " to class index " + classIndex + ": " + e);
				
				// This must be done in order to maintain data consistency.
				_subClasses.remove(classIndex);
				return false;
			}
			
			_subClasses.remove(classIndex);
		}
		finally
		{
			_subclassLock.unlock();
		}
		
		return addSubClass(newClassId, classIndex);
	}
	
	public boolean isSubClassActive()
	{
		return _classIndex > 0;
	}
	
	public Map<Integer, SubClass> getSubClasses()
	{
		return _subClasses;
	}
	
	public int getBaseClass()
	{
		return _baseClass;
	}
	
	public int getActiveClass()
	{
		return _activeClass;
	}
	
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	private void setClassTemplate(int classId)
	{
		_activeClass = classId;
		
		// Set the template of the Player
		setTemplate(PlayerData.getInstance().getTemplate(classId));
	}
	
	/**
	 * Changes the character's class based on the given class index. <BR>
	 * <BR>
	 * An index of zero specifies the character's original (base) class, while indexes 1-3 specifies the character's sub-classes respectively.
	 * @param classIndex
	 * @return true if successful.
	 */
	public boolean setActiveClass(int classIndex)
	{
		if (!_subclassLock.tryLock())
			return false;
		
		try
		{
			// Remove active item skills before saving char to database because next time when choosing this class, worn items can be different
			for (ItemInstance item : getInventory().getAugmentedItems())
			{
				if (item != null && item.isEquipped())
					item.getAugmentation().removeBonus(this);
			}
			
			// abort any kind of cast.
			abortCast();
			
			// Stop casting for any player that may be casting a force buff on this l2pcinstance.
			for (Creature character : getKnownType(Creature.class))
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
					character.abortCast();
				
			store();
			_reuseTimeStamps.clear();
			
			// clear charges
			_charges.set(0);
			stopChargeTask();
			
			if (classIndex == 0)
				setClassTemplate(getBaseClass());
			else
			{
				try
				{
					setClassTemplate(_subClasses.get(classIndex).getClassId());
				}
				catch (Exception e)
				{
					_log.info("Could not switch " + getName() + "'s sub class to class index " + classIndex + ": " + e);
					return false;
				}
			}
			_classIndex = classIndex;
			
			if (_party != null)
				_party.recalculateLevel();
			
			if (getPet() instanceof Servitor)
				getPet().unSummon(this);
			
			for (L2Skill skill : getSkills().values())
				removeSkill(skill.getId(), false);
			
			stopAllEffectsExceptThoseThatLastThroughDeath();
			stopCubics();
			
			if (isSubClassActive())
			{
				_dwarvenRecipeBook.clear();
				_commonRecipeBook.clear();
			}
			else
				restoreRecipeBook();
			
			restoreSkills();
			giveSkills();
			regiveTemporarySkills();
			
			// Prevents some issues when changing between subclases that shares skills
			getDisabledSkills().clear();
			
			restoreEffects();
			updateEffectIcons();
			sendPacket(new EtcStatusUpdate(this));
			
		    removeItens();
			
			// If player has quest "Repent Your Sins", remove it
			QuestState st = getQuestState("Q422_RepentYourSins");
			if (st != null)
				st.exitQuest(true);
			
			for (int i = 0; i < 3; i++)
				_henna[i] = null;
			
			restoreHenna();
			sendPacket(new HennaInfo(this));
			
			if (getCurrentHp() > getMaxHp())
				setCurrentHp(getMaxHp());
			if (getCurrentMp() > getMaxMp())
				setCurrentMp(getMaxMp());
			if (getCurrentCp() > getMaxCp())
				setCurrentCp(getMaxCp());
			
			refreshOverloaded();
			refreshExpertisePenalty();
			broadcastUserInfo();
			
			// Clear resurrect xp calculation
			setExpBeforeDeath(0);
			
			// Remove shot automation
			disableAutoShotsAll();
			
			// Discharge any active shots
			ItemInstance item = getActiveWeaponInstance();
			if (item != null)
				item.unChargeAllShots();
			
			_shortCuts.restore();
			sendPacket(new ShortCutInit(this));
			
			broadcastPacket(new SocialAction(this, 15));
			sendPacket(new SkillCoolTime(this));
			return true;
		}
		finally
		{
			_subclassLock.unlock();
		}
	}
	
	public boolean isLocked()
	{
		return _subclassLock.isLocked();
	}
	
	public void onPlayerEnter()
	{
		if (isCursedWeaponEquipped())
			CursedWeaponManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).cursedOnLogin();
		
		// Add to the GameTimeTask to keep inform about activity time.
		GameTimeTaskManager.getInstance().add(this);
		
		// Teleport player if the Seven Signs period isn't the good one, or if the player isn't in a cabal.
		if (isIn7sDungeon() && !isGM())
		{
			if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
			{
				if (SevenSigns.getInstance().getPlayerCabal(getObjectId()) != SevenSigns.getInstance().getCabalHighestScore())
				{
					teleToLocation(TeleportType.TOWN);
					setIsIn7sDungeon(false);
				}
			}
			else if (SevenSigns.getInstance().getPlayerCabal(getObjectId()) == CabalType.NORMAL)
			{
				teleToLocation(TeleportType.TOWN);
				setIsIn7sDungeon(false);
			}
		}
		
		// Jail task
		updatePunishState();
		
		if (isGM())
		{
			if (isInvul())
				sendMessage("Entering world in Invulnerable mode.");
			if (getAppearance().getInvisible())
				sendMessage("Entering world in Invisible mode.");
			if (isInRefusalMode())
				sendMessage("Entering world in Message Refusal mode.");
		}
		
	    if (HeroManager.getInstance().hasHeroPrivileges(getObjectId()))
	    {
	      long now = Calendar.getInstance().getTimeInMillis();
	      long duration = HeroManager.getInstance().getHeroDuration(getObjectId());
	      long endDay = duration;
	      long _daysleft = (endDay - now) / 86400000L;
	      if (_daysleft < 270L)
	      {
	        sendPacket(new ExShowScreenMessage("Your hero privileges ends at " + new SimpleDateFormat("dd MMM, HH:mm").format(new Date(duration)) + ".", 10000));
	        sendMessage("Your hero privileges ends at " + new SimpleDateFormat("dd MMM, HH:mm").format(new Date(duration)) + ".");
	      }
	      setHero(true);
	      HeroManager.getInstance().addHeroTask(getObjectId(), duration);
	    }
	    if (VipManager.getInstance().hasVipPrivileges(getObjectId()))
	    {
	      long now = Calendar.getInstance().getTimeInMillis();
	      long duration = VipManager.getInstance().getVipDuration(getObjectId());
	      long endDay = duration;
	      long _daysleft = (endDay - now) / 86400000L;
	      if (_daysleft < 270L)
	      {
	        sendPacket(new ExShowScreenMessage("Your Vip privileges ends at " + new SimpleDateFormat("dd MMM, HH:mm").format(new Date(duration)) + ".", 10000));
	        sendMessage("Your vip privileges ends at " + new SimpleDateFormat("dd MMM, HH:mm").format(new Date(duration)) + ".");
	      }
	      setVip(true);
	      VipManager.getInstance().addVipTask(getObjectId(), duration);
	     
	    }
		
		revalidateZone(true);
		notifyFriends(true);
	}
	
	public long getLastAccess()
	{
		return _lastAccess;
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		
		stopEffects(L2EffectType.CHARMOFCOURAGE);
		sendPacket(new EtcStatusUpdate(this));
		
		_reviveRequested = 0;
		_revivePower = 0;
		
		if (isMounted())
			startFeed(_mountNpcId);
		
		// Schedule a paralyzed task to wait for the animation to finish
		ThreadPool.schedule(() -> setIsParalyzed(false), (int) (2000 / getStat().getMovementSpeedMultiplier()));
		setIsParalyzed(true);
		
	    if (isInsideZone(ZoneId.FLAG))
	    {
	      if (getPvpFlag() > 0) {
	        PvpFlagTaskManager.getInstance().remove(this);
	      }
	      updatePvPFlag(1);
	      broadcastUserInfo();
	    }
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		// Restore the player's lost experience, depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}
	
	public void reviveRequest(Player Reviver, L2Skill skill, boolean Pet)
	{
		if (_reviveRequested == 1)
		{
			// Resurrection has already been proposed.
			if (_revivePet == Pet)
				Reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
			else
			{
				if (Pet)
					// A pet cannot be resurrected while it's owner is in the process of resurrecting.
					Reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2);
				else
					// While a pet is attempting to resurrect, it cannot help in resurrecting its master.
					Reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
			}
			return;
		}
		
		if ((Pet && getPet() != null && getPet().isDead()) || (!Pet && isDead()))
		{
			_reviveRequested = 1;
			
			if (isPhoenixBlessed())
				_revivePower = 100;
			else if (isAffected(L2EffectFlag.CHARM_OF_COURAGE))
				_revivePower = 0;
			else
				_revivePower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), Reviver);
			
			_revivePet = Pet;
			
			if (isAffected(L2EffectFlag.CHARM_OF_COURAGE))
			{
				sendPacket(new ConfirmDlg(SystemMessageId.DO_YOU_WANT_TO_BE_RESTORED).addTime(60000));
				return;
			}
			
			sendPacket(new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_S1).addCharName(Reviver));
		}
	}
	
	public void reviveAnswer(int answer)
	{
		if (_reviveRequested != 1 || (!isDead() && !_revivePet) || (_revivePet && getPet() != null && !getPet().isDead()))
			return;
		
		if (answer == 0 && isPhoenixBlessed())
			stopPhoenixBlessing(null);
		else if (answer == 1)
		{
			if (!_revivePet)
			{
				if (_revivePower != 0)
					doRevive(_revivePower);
				else
					doRevive();
			}
			else if (getPet() != null)
			{
				if (_revivePower != 0)
					getPet().doRevive(_revivePower);
				else
					getPet().doRevive();
			}
		}
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	public boolean isReviveRequested()
	{
		return (_reviveRequested == 1);
	}
	
	public boolean isRevivingPet()
	{
		return _revivePet;
	}
	
	public void removeReviving()
	{
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	public void onActionRequest()
	{
		if (isSpawnProtected())
		{
			sendMessage("As you acted, you are no longer under spawn protection.");
			setSpawnProtection(false);
		}
	}
	
	@Override
	public final void onTeleported()
	{
		super.onTeleported();
		
		// Force a revalidation
		revalidateZone(true);
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			setSpawnProtection(true);
		
		// Stop toggles upon teleport.
		if (!isGM())
			stopAllToggles();
		
		// Modify the position of the tamed beast if necessary
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().getAI().stopFollow();
			getTrainedBeast().teleToLocation(getPosition(), 0);
			getTrainedBeast().getAI().startFollow(this);
		}
		
		// Modify the position of the pet if necessary
		Summon pet = getPet();
		if (pet != null)
		{
			pet.setFollowStatus(false);
			pet.teleToLocation(getPosition(), 0);
			((SummonAI) pet.getAI()).setStartFollowController(true);
			pet.setFollowStatus(true);
		}
		
		TvTEvent.onTeleported(this);
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		getStat().addExpAndSp(addToExp, addToSp);
	}
	
	public void addExpAndSp(long addToExp, int addToSp, Map<Creature, RewardInfo> rewards)
	{
		getStat().addExpAndSp(addToExp, addToSp, rewards);
	}
	
	public void removeExpAndSp(long removeExp, int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp);
	}
	
	@Override
	public void reduceCurrentHp(double value, Creature attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (skill != null)
			getStatus().reduceHp(value, attacker, awake, isDOT, skill.isToggle(), skill.getDmgDirectlyToHP());
		else
			getStatus().reduceHp(value, attacker, awake, isDOT, false, false);
		
		// notify the tamed beast of attacks
		if (getTrainedBeast() != null)
			getTrainedBeast().onOwnerGotAttacked(attacker);
	}
	
	public synchronized void addBypass(String bypass)
	{
		if (bypass == null)
			return;
		
		_validBypass.add(bypass);
	}
	
	public synchronized void addBypass2(String bypass)
	{
		if (bypass == null)
			return;
		
		_validBypass2.add(bypass);
	}
	
	public synchronized boolean validateBypass(String cmd)
	{
		for (String bp : _validBypass)
		{
			if (bp == null)
				continue;
			
			if (bp.equals(cmd))
				return true;
		}
		
		for (String bp : _validBypass2)
		{
			if (bp == null)
				continue;
			
			if (cmd.startsWith(bp))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Test cases (player drop, trade item) where the item shouldn't be able to manipulate.
	 * @param objectId : The item objectId.
	 * @return true if it the item can be manipulated, false ovtherwise.
	 */
	public ItemInstance validateItemManipulation(int objectId)
	{
		final ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		// You don't own the item, or item is null.
		if (item == null || item.getOwnerId() != getObjectId())
			return null;
		
		// Pet whom item you try to manipulate is summoned/mounted.
		if (getPet() != null && getPet().getControlItemId() == objectId || _mountObjectId == objectId)
			return null;
		
		// Item is under enchant process.
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
			return null;
		
		// Can't trade a cursed weapon.
		if (CursedWeaponManager.getInstance().isCursed(item.getItemId()))
			return null;
		
		return item;
	}
	
	public synchronized void clearBypass()
	{
		_validBypass.clear();
		_validBypass2.clear();
	}
	
	/**
	 * @return Returns the inBoat.
	 */
	public boolean isInBoat()
	{
		return _boat != null;
	}
	
	public Boat getBoat()
	{
		return _boat;
	}
	
	public void setBoat(Boat v)
	{
		if (v == null && _boat != null)
			_boat.removePassenger(this);
		
		_boat = v;
	}
	
	public void setCrystallizing(boolean mode)
	{
		_isCrystallizing = mode;
	}
	
	public boolean isCrystallizing()
	{
		return _isCrystallizing;
	}
	
	public SpawnLocation getBoatPosition()
	{
		return _boatPosition;
	}
	
	/**
	 * Manage the delete task of a Player (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).
	 * <ul>
	 * <li>If the Player is in observer mode, set its position to its position before entering in observer mode</li>
	 * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li>
	 * <li>Cancel Crafting, Attak or Cast</li>
	 * <li>Remove the Player from the world</li>
	 * <li>Stop Party and Unsummon Pet</li>
	 * <li>Update database with items in its inventory and remove them from the world</li>
	 * <li>Remove the object from region</li>
	 * <li>Close the connection with the client</li>
	 * </ul>
	 */
	@Override
	public void deleteMe()
	{
		cleanup();
		store();
		super.deleteMe();
	}
	
	private synchronized void cleanup()
	{
		try
		{
			// Put the online status to false
			setOnlineStatus(false, true);
			
			// abort cast & attack and remove the target. Cancels movement aswell.
			abortAttack();
			abortCast();
			stopMove(null);
			setTarget(null);
			
			removeMeFromPartyMatch();
			
			if (isFlying())
				removeSkill(FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);
			
			// Stop all scheduled tasks
			stopAllTimers();
			
			// Cancel the cast of eventual fusion skill users on this target.
			for (Creature character : getKnownType(Creature.class))
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
					character.abortCast();
				
			// Stop signets & toggles effects.
			for (L2Effect effect : getAllEffects())
			{
				if (effect.getSkill().isToggle())
				{
					effect.exit();
					continue;
				}
				
				switch (effect.getEffectType())
				{
					case SIGNET_GROUND:
					case SIGNET_EFFECT:
						effect.exit();
						break;
				}
			}
			
			// Remove the Player from the world
			decayMe();
			
			// If a party is in progress, leave it
			if (_party != null)
				_party.removePartyMember(this, MessageType.DISCONNECTED);
			
			// If the Player has Pet, unsummon it
			if (getPet() != null)
				getPet().unSummon(this);
			
			// Handle removal from olympiad game
			if (OlympiadManager.getInstance().isRegistered(this) || getOlympiadGameId() != -1)
				OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
			
			// set the status for pledge member list to OFFLINE
			if (getClan() != null)
			{
				ClanMember clanMember = getClan().getClanMember(getObjectId());
				if (clanMember != null)
					clanMember.setPlayerInstance(null);
			}
			
			// deals with sudden exit in the middle of transaction
			if (getActiveRequester() != null)
			{
				setActiveRequester(null);
				cancelActiveTrade();
			}
			
			// If the Player is a GM, remove it from the GM List
			if (isGM())
				AdminData.deleteGm(this);
			
			TvTEvent.onLogout(this);
			
			// Check if the Player is in observer mode to set its position to its position before entering in observer mode
			if (isInObserverMode())
				setXYZInvisible(_savedLocation);
			
			// Oust player from boat
			if (getBoat() != null)
				getBoat().oustPlayer(this, true, Location.DUMMY_LOC);
			
			// Update inventory and remove them from the world
			getInventory().deleteMe();
			
			// Update warehouse and remove them from the world
			clearWarehouse();
			
			// Update freight and remove them from the world
			clearFreight();
			clearDepositedFreight();
			
			if (isCursedWeaponEquipped())
				CursedWeaponManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).setPlayer(null);
			
			if (getClanId() > 0)
				getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
			
			if (isSeated())
			{
				final WorldObject object = World.getInstance().getObject(_throneId);
				if (object instanceof StaticObject)
					((StaticObject) object).setBusy(false);
			}
			
			World.getInstance().removePlayer(this); // force remove in case of crash during teleport
			
			// friends & blocklist update
			notifyFriends(false);
			getBlockList().playerLogout();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on deleteMe()" + e.getMessage(), e);
		}
	}
	
	public void startFishing(Location loc)
	{
		stopMove(null);
		setIsImmobilized(true);
		
		_fishingLoc = loc;
		
		// Starts fishing
		int group = getRandomGroup();
		
		_fish = FishData.getInstance().getFish(getRandomFishLvl(), getRandomFishType(group), group);
		if (_fish == null)
		{
			endFishing(false);
			return;
		}
		
		sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		
		broadcastPacket(new ExFishingStart(this, _fish.getType(_lure.isNightLure()), loc, _lure.isNightLure()));
		sendPacket(new PlaySound(1, "SF_P_01"));
		startLookingForFishTask();
	}
	
	public void stopLookingForFishTask()
	{
		if (_fishingTask != null)
		{
			_fishingTask.cancel(false);
			_fishingTask = null;
		}
	}
	
	public void startLookingForFishTask()
	{
		if (!isDead() && _fishingTask == null)
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;
			
			if (_lure != null)
			{
				int lureid = _lure.getItemId();
				isNoob = _fish.getGroup() == 0;
				isUpperGrade = _fish.getGroup() == 2;
				if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511) // low grade
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.33)));
				else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || (lureid >= 8505 && lureid <= 8513) || (lureid >= 7610 && lureid <= 7613) || (lureid >= 7807 && lureid <= 7809) || (lureid >= 8484 && lureid <= 8486)) // medium grade, beginner, prize-winning & quest special bait
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.00)));
				else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513) // high grade
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (0.66)));
			}
			_fishingTask = ThreadPool.scheduleAtFixedRate(new LookingForFishTask(_fish.getWaitTime(), _fish.getGuts(), _fish.getType(_lure.isNightLure()), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}
	
	private int getRandomGroup()
	{
		switch (_lure.getItemId())
		{
			case 7807: // green for beginners
			case 7808: // purple for beginners
			case 7809: // yellow for beginners
			case 8486: // prize-winning for beginners
				return 0;
			
			case 8485: // prize-winning luminous
			case 8506: // green luminous
			case 8509: // purple luminous
			case 8512: // yellow luminous
				return 2;
			
			default:
				return 1;
		}
	}
	
	private int getRandomFishType(int group)
	{
		int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0: // fish for novices
				switch (_lure.getItemId())
				{
					case 7807: // green lure, preferred by fast-moving (nimble) fish (type 5)
						if (check <= 54)
							type = 5;
						else if (check <= 77)
							type = 4;
						else
							type = 6;
						break;
					
					case 7808: // purple lure, preferred by fat fish (type 4)
						if (check <= 54)
							type = 4;
						else if (check <= 77)
							type = 6;
						else
							type = 5;
						break;
					
					case 7809: // yellow lure, preferred by ugly fish (type 6)
						if (check <= 54)
							type = 6;
						else if (check <= 77)
							type = 5;
						else
							type = 4;
						break;
					
					case 8486: // prize-winning fishing lure for beginners
						if (check <= 33)
							type = 4;
						else if (check <= 66)
							type = 5;
						else
							type = 6;
						break;
				}
				break;
			
			case 1: // normal fish
				switch (_lure.getItemId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					
					case 6519: // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54)
							type = 1;
						else if (check <= 74)
							type = 0;
						else if (check <= 94)
							type = 2;
						else
							type = 3;
						break;
					
					case 6522: // all theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54)
							type = 0;
						else if (check <= 74)
							type = 1;
						else if (check <= 94)
							type = 2;
						else
							type = 3;
						break;
					
					case 6525: // all theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55)
							type = 2;
						else if (check <= 74)
							type = 1;
						else if (check <= 94)
							type = 0;
						else
							type = 3;
						break;
					case 8484: // prize-winning fishing lure
						if (check <= 33)
							type = 0;
						else if (check <= 66)
							type = 1;
						else
							type = 2;
						break;
				}
				break;
			
			case 2: // upper grade fish, luminous lure
				switch (_lure.getItemId())
				{
					case 8506: // green lure, preferred by fast-moving (nimble) fish (type 8)
						if (check <= 54)
							type = 8;
						else if (check <= 77)
							type = 7;
						else
							type = 9;
						break;
					
					case 8509: // purple lure, preferred by fat fish (type 7)
						if (check <= 54)
							type = 7;
						else if (check <= 77)
							type = 9;
						else
							type = 8;
						break;
					
					case 8512: // yellow lure, preferred by ugly fish (type 9)
						if (check <= 54)
							type = 9;
						else if (check <= 77)
							type = 8;
						else
							type = 7;
						break;
					
					case 8485: // prize-winning fishing lure
						if (check <= 33)
							type = 7;
						else if (check <= 66)
							type = 8;
						else
							type = 9;
						break;
				}
		}
		return type;
	}
	
	private int getRandomFishLvl()
	{
		int skilllvl = getSkillLevel(1315);
		
		final L2Effect e = getFirstEffect(2274);
		if (e != null)
			skilllvl = (int) e.getSkill().getPower();
		
		if (skilllvl <= 0)
			return 1;
		
		int randomlvl;
		
		final int check = Rnd.get(100);
		if (check <= 50)
			randomlvl = skilllvl;
		else if (check <= 85)
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
				randomlvl = 1;
		}
		else
		{
			randomlvl = skilllvl + 1;
			if (randomlvl > 27)
				randomlvl = 27;
		}
		return randomlvl;
	}
	
	public void startFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade, _lure.getItemId());
	}
	
	public void endFishing(boolean win)
	{
		if (_fishCombat == null)
			sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		else
			_fishCombat = null;
		
		_lure = null;
		_fishingLoc = null;
		
		// Ends fishing
		broadcastPacket(new ExFishingEnd(win, getObjectId()));
		sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		setIsImmobilized(false);
		stopLookingForFishTask();
	}
	
	public L2Fishing getFishCombat()
	{
		return _fishCombat;
	}
	
	public Location getFishingLoc()
	{
		return _fishingLoc;
	}
	
	public void setLure(ItemInstance lure)
	{
		_lure = lure;
	}
	
	public ItemInstance getLure()
	{
		return _lure;
	}
	
	public int getInventoryLimit()
	{
		return ((getRace() == ClassRace.DWARF) ? Config.INVENTORY_MAXIMUM_DWARF : Config.INVENTORY_MAXIMUM_NO_DWARF) + (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);
	}
	
	public static int getQuestInventoryLimit()
	{
		return Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}
	
	public int getWareHouseLimit()
	{
		return ((getRace() == ClassRace.DWARF) ? Config.WAREHOUSE_SLOTS_DWARF : Config.WAREHOUSE_SLOTS_NO_DWARF) + (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);
	}
	
	public int getPrivateSellStoreLimit()
	{
		return ((getRace() == ClassRace.DWARF) ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);
	}
	
	public int getPrivateBuyStoreLimit()
	{
		return ((getRace() == ClassRace.DWARF) ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);
	}
	
	public int getFreightLimit()
	{
		return Config.FREIGHT_SLOTS + (int) getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
	}
	
	public int getDwarfRecipeLimit()
	{
		return Config.DWARF_RECIPE_LIMIT + (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
	}
	
	public int getCommonRecipeLimit()
	{
		return Config.COMMON_RECIPE_LIMIT + (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
	}
	
	public int getMountNpcId()
	{
		return _mountNpcId;
	}
	
	public int getMountLevel()
	{
		return _mountLevel;
	}
	
	public void setMountObjectId(int id)
	{
		_mountObjectId = id;
	}
	
	public int getMountObjectId()
	{
		return _mountObjectId;
	}
	
	/**
	 * This method is overidden on Player, L2Summon and L2Npc.
	 * @return the skills list of this Creature.
	 */
	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	/**
	 * @return the current player skill in use.
	 */
	public SkillUseHolder getCurrentSkill()
	{
		return _currentSkill;
	}
	
	/**
	 * Update the _currentSkill holder.
	 * @param skill : The skill to update for (or null)
	 * @param ctrlPressed : The boolean information regarding ctrl key.
	 * @param shiftPressed : The boolean information regarding shift key.
	 */
	public void setCurrentSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
	{
		_currentSkill.setSkill(skill);
		_currentSkill.setCtrlPressed(ctrlPressed);
		_currentSkill.setShiftPressed(shiftPressed);
	}
	
	/**
	 * @return the current pet skill in use.
	 */
	public SkillUseHolder getCurrentPetSkill()
	{
		return _currentPetSkill;
	}
	
	/**
	 * Update the _currentPetSkill holder.
	 * @param skill : The skill to update for (or null)
	 * @param ctrlPressed : The boolean information regarding ctrl key.
	 * @param shiftPressed : The boolean information regarding shift key.
	 */
	public void setCurrentPetSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
	{
		_currentPetSkill.setSkill(skill);
		_currentPetSkill.setCtrlPressed(ctrlPressed);
		_currentPetSkill.setShiftPressed(shiftPressed);
	}
	
	/**
	 * @return the current queued skill in use.
	 */
	public SkillUseHolder getQueuedSkill()
	{
		return _queuedSkill;
	}
	
	/**
	 * Update the _queuedSkill holder.
	 * @param skill : The skill to update for (or null)
	 * @param ctrlPressed : The boolean information regarding ctrl key.
	 * @param shiftPressed : The boolean information regarding shift key.
	 */
	public void setQueuedSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
	{
		_queuedSkill.setSkill(skill);
		_queuedSkill.setCtrlPressed(ctrlPressed);
		_queuedSkill.setShiftPressed(shiftPressed);
	}
	
	/**
	 * @return punishment level of player
	 */
	public PunishLevel getPunishLevel()
	{
		return _punishLevel;
	}
	
	/**
	 * @return True if player is jailed
	 */
	public boolean isInJail()
	{
		return _punishLevel == PunishLevel.JAIL;
	}
	
	/**
	 * @return True if player is chat banned
	 */
	public boolean isChatBanned()
	{
		return _punishLevel == PunishLevel.CHAT;
	}
	
	public void setPunishLevel(int state)
	{
		switch (state)
		{
			case 0:
				_punishLevel = PunishLevel.NONE;
				break;
			case 1:
				_punishLevel = PunishLevel.CHAT;
				break;
			case 2:
				_punishLevel = PunishLevel.JAIL;
				if (!TvTEvent.isInactive() && TvTEvent.isPlayerParticipant(getObjectId()))
					TvTEvent.removeParticipant(getObjectId());
				break;
			case 3:
				_punishLevel = PunishLevel.CHAR;
				break;
			case 4:
				_punishLevel = PunishLevel.ACC;
				break;
		}
	}
	
	/**
	 * Sets punish level for player based on delay
	 * @param state
	 * @param delayInMinutes -- 0 for infinite
	 */
	public void setPunishLevel(PunishLevel state, int delayInMinutes)
	{
		long delayInMilliseconds = delayInMinutes * 60000L;
		switch (state)
		{
			case NONE: // Remove Punishments
			{
				switch (_punishLevel)
				{
					case CHAT:
					{
						_punishLevel = state;
						stopPunishTask(true);
						sendPacket(new EtcStatusUpdate(this));
						sendMessage("Chatting is now available.");
						sendPacket(new PlaySound("systemmsg_e.345"));
						break;
					}
					case JAIL:
					{
						_punishLevel = state;
						
						// Open a Html message to inform the player
						final NpcHtmlMessage html = new NpcHtmlMessage(0);
						html.setFile("data/html/jail_out.htm");
						sendPacket(html);
						
						stopPunishTask(true);
						teleToLocation(17836, 170178, -3507, 20); // Floran village
						break;
					}
				}
				break;
			}
			case CHAT: // Chat ban
			{
				// not allow player to escape jail using chat ban
				if (_punishLevel == PunishLevel.JAIL)
					break;
				
				_punishLevel = state;
				_punishTimer = 0;
				sendPacket(new EtcStatusUpdate(this));
				
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMinutes > 0)
				{
					_punishTimer = delayInMilliseconds;
					
					// start the countdown
					_punishTask = ThreadPool.schedule(() -> setPunishLevel(PunishLevel.NONE, 0), _punishTimer);
					sendMessage("Chatting has been suspended for " + delayInMinutes + " minute(s).");
				}
				else
					sendMessage("Chatting has been suspended.");
				
				// Send same sound packet in both "delay" cases.
				sendPacket(new PlaySound("systemmsg_e.346"));
				break;
				
			}
			case JAIL: // Jail Player
			{
				_punishLevel = state;
				_punishTimer = 0;
				
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMinutes > 0)
				{
					_punishTimer = delayInMilliseconds;
					
					// start the countdown
					_punishTask = ThreadPool.schedule(() -> setPunishLevel(PunishLevel.NONE, 0), _punishTimer);
					sendMessage("You are jailed for " + delayInMinutes + " minutes.");
				}
				
				if (OlympiadManager.getInstance().isRegisteredInComp(this))
					OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
				
				
				// Open a Html message to inform the player
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/jail_in.htm");
				sendPacket(html);
				
				setIsIn7sDungeon(false);
				teleToLocation(-114356, -249645, -2984, 0); // Jail
				break;
			}
			case CHAR: // Ban Character
			{
				setAccessLevel(-1);
				logout();
				break;
			}
			case ACC: // Ban Account
			{
				setAccountAccesslevel(-100);
				logout();
				break;
			}
			default:
			{
				_punishLevel = state;
				break;
			}
		}
		
		// store in database
		storeCharBase();
	}
	
	public long getPunishTimer()
	{
		return _punishTimer;
	}
	
	public void setPunishTimer(long time)
	{
		_punishTimer = time;
	}
	
	private void updatePunishState()
	{
		if (getPunishLevel() != PunishLevel.NONE)
		{
			// If punish timer exists, restart punishtask.
			if (_punishTimer > 0)
			{
				_punishTask = ThreadPool.schedule(() -> setPunishLevel(PunishLevel.NONE, 0), _punishTimer);
				sendMessage("You are still " + getPunishLevel().string() + " for " + Math.round(_punishTimer / 60000f) + " minutes.");
			}
			
			if (getPunishLevel() == PunishLevel.JAIL)
			{
				// If player escaped, put him back in jail
				if (!isInsideZone(ZoneId.JAIL))
					teleToLocation(-114356, -249645, -2984, 20);
			}
		}
	}
	
	public void stopPunishTask(boolean save)
	{
		if (_punishTask != null)
		{
			if (save)
			{
				long delay = _punishTask.getDelay(TimeUnit.MILLISECONDS);
				if (delay < 0)
					delay = 0;
				setPunishTimer(delay);
			}
			_punishTask.cancel(false);
			_punishTask = null;
		}
	}
	
	public int getPowerGrade()
	{
		return _powerGrade;
	}
	
	public void setPowerGrade(int power)
	{
		_powerGrade = power;
	}
	
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}
	
	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;
	}
	
	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}
	
	public void shortBuffStatusUpdate(int magicId, int level, int time)
	{
		if (_shortBuffTask != null)
		{
			_shortBuffTask.cancel(false);
			_shortBuffTask = null;
		}
		
		_shortBuffTask = ThreadPool.schedule(() ->
		{
			sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
			setShortBuffTaskSkillId(0);
		}, time * 1000);
		setShortBuffTaskSkillId(magicId);
		
		sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
	}
	
	public int getShortBuffTaskSkillId()
	{
		return _shortBuffTaskSkillId;
	}
	
	public void setShortBuffTaskSkillId(int id)
	{
		_shortBuffTaskSkillId = id;
	}
	
	public int getDeathPenaltyBuffLevel()
	{
		return _deathPenaltyBuffLevel;
	}
	
	public void setDeathPenaltyBuffLevel(int level)
	{
		_deathPenaltyBuffLevel = level;
	}
	
	public void calculateDeathPenaltyBuffLevel(Creature killer)
	{
		if (_deathPenaltyBuffLevel >= 15) // maximum level reached
			return;
		
		if ((getKarma() > 0 || Rnd.get(1, 100) <= Config.DEATH_PENALTY_CHANCE) && !(killer instanceof Player) && !isGM() && !(getCharmOfLuck() && (killer == null || killer.isRaid())) && !isPhoenixBlessed() && !(isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.SIEGE)))
		{
			if (_deathPenaltyBuffLevel != 0)
				removeSkill(5076, false);
			
			_deathPenaltyBuffLevel++;
			
			addSkill(SkillTable.getInstance().getInfo(5076, _deathPenaltyBuffLevel), false);
			sendPacket(new EtcStatusUpdate(this));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(_deathPenaltyBuffLevel));
		}
	}
	
	public void reduceDeathPenaltyBuffLevel()
	{
		if (_deathPenaltyBuffLevel <= 0)
			return;
		
		removeSkill(5076, false);
		
		_deathPenaltyBuffLevel--;
		
		if (_deathPenaltyBuffLevel > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, _deathPenaltyBuffLevel), false);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(_deathPenaltyBuffLevel));
		}
		else
			sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
		
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void restoreDeathPenaltyBuffLevel()
	{
		if (_deathPenaltyBuffLevel > 0)
			addSkill(SkillTable.getInstance().getInfo(5076, _deathPenaltyBuffLevel), false);
	}
	
	private final Map<Integer, TimeStamp> _reuseTimeStamps = new ConcurrentHashMap<>();
	
	public Collection<TimeStamp> getReuseTimeStamps()
	{
		return _reuseTimeStamps.values();
	}
	
	public Map<Integer, TimeStamp> getReuseTimeStamp()
	{
		return _reuseTimeStamps;
	}
	
	/**
	 * Simple class containing all neccessary information to maintain valid timestamps and reuse for skills upon relog. Filter this carefully as it becomes redundant to store reuse for small delays.
	 * @author Yesod
	 */
	public static class TimeStamp
	{
		private final int _skillId;
		private final int _skillLvl;
		private final long _reuse;
		private final long _stamp;
		
		public TimeStamp(L2Skill skill, long reuse)
		{
			_skillId = skill.getId();
			_skillLvl = skill.getLevel();
			_reuse = reuse;
			_stamp = System.currentTimeMillis() + reuse;
		}
		
		public TimeStamp(L2Skill skill, long reuse, long systime)
		{
			_skillId = skill.getId();
			_skillLvl = skill.getLevel();
			_reuse = reuse;
			_stamp = systime;
		}
		
		public long getStamp()
		{
			return _stamp;
		}
		
		public int getSkillId()
		{
			return _skillId;
		}
		
		public int getSkillLvl()
		{
			return _skillLvl;
		}
		
		public long getReuse()
		{
			return _reuse;
		}
		
		public long getRemaining()
		{
			return Math.max(_stamp - System.currentTimeMillis(), 0);
		}
		
		public boolean hasNotPassed()
		{
			return System.currentTimeMillis() < _stamp;
		}
	}
	
	/**
	 * Index according to skill id the current timestamp of use.
	 * @param skill
	 * @param reuse delay
	 */
	@Override
	public void addTimeStamp(L2Skill skill, long reuse)
	{
		_reuseTimeStamps.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse));
	}
	
	/**
	 * Index according to skill this TimeStamp instance for restoration purposes only.
	 * @param skill
	 * @param reuse
	 * @param systime
	 */
	public void addTimeStamp(L2Skill skill, long reuse, long systime)
	{
		_reuseTimeStamps.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse, systime));
	}
	
	@Override
	public Player getActingPlayer()
	{
		return this;
	}
	
	@Override
	public final void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		// Check if hit is missed
		if (miss)
		{
			sendPacket(SystemMessageId.MISSED_TARGET);
			return;
		}
		
		// Check if hit is critical
		if (pcrit)
			sendPacket(SystemMessageId.CRITICAL_HIT);
		if (mcrit)
			sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
		
		if (target.isInvul())
		{
			if (target.isParalyzed())
				sendPacket(SystemMessageId.OPPONENT_PETRIFIED);
			else
				sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
		}
		else
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(damage));
		
		if (isInOlympiadMode() && target instanceof Player && ((Player) target).isInOlympiadMode() && ((Player) target).getOlympiadGameId() == getOlympiadGameId())
			OlympiadGameManager.getInstance().notifyCompetitorDamage(this, damage);
	}
	
	public void checkItemRestriction()
	{
		for (ItemInstance equippedItem : getInventory().getPaperdollItems())
		{
			if (equippedItem.getItem().checkCondition(this, this, false))
				continue;
			
			useEquippableItem(equippedItem, equippedItem.isWeapon());
		}
	}
	
	/**
	 * A method used to test player entrance on no landing zone.<br>
	 * <br>
	 * If a player is mounted on a Wyvern, it launches a dismount task after 5 seconds, and a warning message.
	 */
	public void enterOnNoLandingZone()
	{
		if (getMountType() == 2)
		{
			if (_dismountTask == null)
				_dismountTask = ThreadPool.schedule(() -> dismount(), 5000);
			
			sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
		}
	}
	
	/**
	 * A method used to test player leave on no landing zone.<br>
	 * <br>
	 * If a player is mounted on a Wyvern, it cancels the dismount task, if existing.
	 */
	public void exitOnNoLandingZone()
	{
		if (getMountType() == 2 && _dismountTask != null)
		{
			_dismountTask.cancel(true);
			_dismountTask = null;
		}
	}
	
	public void setIsInSiege(boolean b)
	{
		_isInSiege = b;
	}
	
	public boolean isInSiege()
	{
		return _isInSiege;
	}
	
	/**
	 * Remove player from BossZones (used on char logout/exit)
	 */
	public void removeFromBossZone()
	{
		for (BossZone zone : ZoneManager.getInstance().getAllZones(BossZone.class))
			zone.removePlayer(this);
	}
	
	/**
	 * @return the number of charges this Player got.
	 */
	public int getCharges()
	{
		return _charges.get();
	}
	
	public void increaseCharges(int count, int max)
	{
		if (_charges.get() >= max)
		{
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			return;
		}
		
		restartChargeTask();
		
		if (_charges.addAndGet(count) >= max)
		{
			_charges.set(max);
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
		}
		else
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(_charges.get()));
		
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public boolean decreaseCharges(int count)
	{
		if (_charges.get() < count)
			return false;
		
		if (_charges.addAndGet(-count) == 0)
			stopChargeTask();
		else
			restartChargeTask();
		
		sendPacket(new EtcStatusUpdate(this));
		return true;
	}
	
	public void clearCharges()
	{
		_charges.set(0);
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Starts/Restarts the ChargeTask to Clear Charges after 10 Mins.
	 */
	private void restartChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
		
		_chargeTask = ThreadPool.schedule(() -> clearCharges(), 600000);
	}
	
	/**
	 * Stops the Charges Clearing Task.
	 */
	public void stopChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
	}
	
	/**
	 * Signets check used to valid who is affected when he entered in the aoe effect.
	 * @param cha The target to make checks on.
	 * @return true if player can attack the target.
	 */
	public boolean canAttackCharacter(Creature cha)
	{
		if (cha instanceof Attackable)
			return true;
		
		if (cha instanceof Playable)
		{
			if (cha.isInArena())
				return true;
			
			final Player target = cha.getActingPlayer();
			
			if (isInDuel() && target.isInDuel() && target.getDuelId() == getDuelId())
				return true;
			
			if (isInParty() && target.isInParty())
			{
				if (getParty() == target.getParty())
					return false;
				
				if ((getParty().getCommandChannel() != null || target.getParty().getCommandChannel() != null) && (getParty().getCommandChannel() == target.getParty().getCommandChannel()))
					return false;
			}
			
			if (getClan() != null && target.getClan() != null)
			{
				if (getClanId() == target.getClanId())
					return false;
				
				if ((getAllyId() > 0 || target.getAllyId() > 0) && getAllyId() == target.getAllyId())
					return false;
				
				if (getClan().isAtWarWith(target.getClanId()))
					return true;
			}
			else
			{
				if (target.getPvpFlag() == 0 && target.getKarma() == 0)
					return false;
			}
		}
		return true;
	}
	
	public static void teleToTarget(Player targetChar, Player summonerChar, L2Skill summonSkill)
	{
		if (targetChar == null || summonerChar == null || summonSkill == null)
			return;
		
		if (!checkSummonerStatus(summonerChar))
			return;
		
		if (!checkSummonTargetStatus(targetChar, summonerChar))
			return;
		
		final int itemConsumeId = summonSkill.getTargetConsumeId();
		final int itemConsumeCount = summonSkill.getTargetConsume();
		
		if (itemConsumeId != 0 && itemConsumeCount != 0)
		{
			if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, -1) < itemConsumeCount)
			{
				targetChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING).addItemName(summonSkill.getTargetConsumeId()));
				return;
			}
			
			targetChar.destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, targetChar, true);
		}
		targetChar.teleToLocation(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), 20);
	}
	
	public static boolean checkSummonerStatus(Player summonerChar)
	{
		if (summonerChar == null)
			return false;
		
		if (summonerChar.isInOlympiadMode() || summonerChar.isInObserverMode() || summonerChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND) || summonerChar.isMounted())
			return false;
		
		if (!TvTEvent.onEscapeUse(summonerChar.getObjectId()))
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		
		return true;
	}
	
	public static boolean checkSummonTargetStatus(WorldObject target, Player summonerChar)
	{
		if (target == null || !(target instanceof Player))
			return false;
		
		Player targetChar = (Player) target;
		
		if (targetChar.isAlikeDead())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addCharName(targetChar));
			return false;
		}
		
		if (targetChar.isInStoreMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addCharName(targetChar));
			return false;
		}
		
		if (targetChar.isRooted() || targetChar.isInCombat())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addCharName(targetChar));
			return false;
		}
		
		if (!TvTEvent.onEscapeUse(targetChar.getObjectId()))
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		
		if (targetChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
			return false;
		}
		
		if (targetChar.isFestivalParticipant() || targetChar.isMounted())
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		
		if (targetChar.isInObserverMode() || targetChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IN_SUMMON_BLOCKING_AREA).addCharName(targetChar));
			return false;
		}
		
		return true;
	}
	
	public final int getClientX()
	{
		return _clientX;
	}
	
	public final int getClientY()
	{
		return _clientY;
	}
	
	public final int getClientZ()
	{
		return _clientZ;
	}
	
	public final int getClientHeading()
	{
		return _clientHeading;
	}
	
	public final void setClientX(int val)
	{
		_clientX = val;
	}
	
	public final void setClientY(int val)
	{
		_clientY = val;
	}
	
	public final void setClientZ(int val)
	{
		_clientZ = val;
	}
	
	public final void setClientHeading(int val)
	{
		_clientHeading = val;
	}
	
	/**
	 * @return the mailPosition.
	 */
	public int getMailPosition()
	{
		return _mailPosition;
	}
	
	/**
	 * @param mailPosition The mailPosition to set.
	 */
	public void setMailPosition(int mailPosition)
	{
		_mailPosition = mailPosition;
	}
	
	/**
	 * @param z
	 * @return true if character falling now On the start of fall return false for correct coord sync !
	 */
	public final boolean isFalling(int z)
	{
		if (isDead() || isFlying() || isInsideZone(ZoneId.WATER))
			return false;
		
		if (System.currentTimeMillis() < _fallingTimestamp)
			return true;
		
		final int deltaZ = getZ() - z;
		if (deltaZ <= getBaseTemplate().getFallHeight())
			return false;
		
		final int damage = (int) Formulas.calcFallDam(this, deltaZ);
		if (damage > 0)
		{
			reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null, false, true, null);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
		}
		
		setFalling();
		
		return false;
	}
	
	/**
	 * Set falling timestamp
	 */
	public final void setFalling()
	{
		_fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
	}
	
	public boolean isAllowedToEnchantSkills()
	{
		if (isLocked())
			return false;
		
		if (AttackStanceTaskManager.getInstance().isInAttackStance(this))
			return false;
		
		if (isCastingNow() || isCastingSimultaneouslyNow())
			return false;
		
		if (isInBoat())
			return false;
		
		return true;
	}
	
	public List<Integer> getFriendList()
	{
		return _friendList;
	}
	
	public void selectFriend(Integer friendId)
	{
		if (!_selectedFriendList.contains(friendId))
			_selectedFriendList.add(friendId);
	}
	
	public void deselectFriend(Integer friendId)
	{
		if (_selectedFriendList.contains(friendId))
			_selectedFriendList.remove(friendId);
	}
	
	public List<Integer> getSelectedFriendList()
	{
		return _selectedFriendList;
	}
	
	private void restoreFriendList()
	{
		_friendList.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 0");
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			
			int friendId;
			while (rset.next())
			{
				friendId = rset.getInt("friend_id");
				if (friendId == getObjectId())
					continue;
				
				_friendList.add(friendId);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error found in " + getName() + "'s friendlist: " + e.getMessage(), e);
		}
	}
	
	protected void notifyFriends(boolean login)
	{
		for (int id : _friendList)
		{
			Player friend = World.getInstance().getPlayer(id);
			if (friend != null)
			{
				friend.sendPacket(new FriendList(friend));
				
				if (login)
					friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addCharName(this));
			}
		}
	}
	
	public void selectBlock(Integer friendId)
	{
		if (!_selectedBlocksList.contains(friendId))
			_selectedBlocksList.add(friendId);
	}
	
	public void deselectBlock(Integer friendId)
	{
		if (_selectedBlocksList.contains(friendId))
			_selectedBlocksList.remove(friendId);
	}
	
	public List<Integer> getSelectedBlocksList()
	{
		return _selectedBlocksList;
	}
	
	@Override
	public void broadcastRelationsChanges()
	{
		for (Player player : getKnownType(Player.class))
		{
			final int relation = getRelation(player);
			final boolean isAutoAttackable = isAutoAttackable(player);
			
			player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
			if (getPet() != null)
				player.sendPacket(new RelationChanged(getPet(), relation, isAutoAttackable));
		}
	}
	
	@Override
	public void sendInfo(Player activeChar)
	{
		if (isInBoat())
			getPosition().set(getBoat().getPosition());
		
		if (getPolyType() == PolyType.NPC)
			activeChar.sendPacket(new AbstractNpcInfo.PcMorphInfo(this, getPolyTemplate()));
		else
		{
			activeChar.sendPacket(new CharInfo(this));
			
			if (isSeated())
			{
				final WorldObject object = World.getInstance().getObject(_throneId);
				if (object instanceof StaticObject)
					activeChar.sendPacket(new ChairSit(getObjectId(), ((StaticObject) object).getStaticObjectId()));
			}
		}
		
		int relation = getRelation(activeChar);
		boolean isAutoAttackable = isAutoAttackable(activeChar);
		
		activeChar.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
		if (getPet() != null)
			activeChar.sendPacket(new RelationChanged(getPet(), relation, isAutoAttackable));
		
		relation = activeChar.getRelation(this);
		isAutoAttackable = activeChar.isAutoAttackable(this);
		
		sendPacket(new RelationChanged(activeChar, relation, isAutoAttackable));
		if (activeChar.getPet() != null)
			sendPacket(new RelationChanged(activeChar.getPet(), relation, isAutoAttackable));
		
		if (isInBoat())
			activeChar.sendPacket(new GetOnVehicle(getObjectId(), getBoat().getObjectId(), getBoatPosition()));
		
		switch (getStoreType())
		{
			case SELL:
			case PACKAGE_SELL:
				activeChar.sendPacket(new PrivateStoreMsgSell(this));
				break;
			
			case BUY:
				activeChar.sendPacket(new PrivateStoreMsgBuy(this));
				break;
			
			case MANUFACTURE:
				activeChar.sendPacket(new RecipeShopMsg(this));
				break;
		}
	}
	
	@Override
	public double getCollisionRadius()
	{
		return getBaseTemplate().getCollisionRadiusBySex(getAppearance().getSex());
	}
	
	@Override
	public double getCollisionHeight()
	{
		return getBaseTemplate().getCollisionHeightBySex(getAppearance().getSex());
	}
	
	public boolean teleportRequest(Player requester, L2Skill skill)
	{
		if (_summonTargetRequest != null && requester != null)
			return false;
		
		_summonTargetRequest = requester;
		_summonSkillRequest = skill;
		return true;
	}
	
	public void teleportAnswer(int answer, int requesterId)
	{
		if (_summonTargetRequest == null)
			return;
		
		if (answer == 1 && _summonTargetRequest.getObjectId() == requesterId)
			teleToTarget(this, _summonTargetRequest, _summonSkillRequest);
		
		_summonTargetRequest = null;
		_summonSkillRequest = null;
	}
	
	public void activateGate(int answer, int type)
	{
		if (_requestedGate == null)
			return;
		
		if (answer == 1 && getTarget() == _requestedGate)
		{
			if (type == 1)
				_requestedGate.openMe();
			else if (type == 0)
				_requestedGate.closeMe();
		}
		
		_requestedGate = null;
	}
	
	public void setRequestedGate(Door door)
	{
		_requestedGate = door;
	}
	
	@Override
	public boolean polymorph(PolyType type, int npcId)
	{
		if (super.polymorph(type, npcId))
		{
			sendPacket(new UserInfo(this));
			return true;
		}
		return false;
	}
	
	@Override
	public void unpolymorph()
	{
		super.unpolymorph();
		sendPacket(new UserInfo(this));
	}
	
	@Override
	public void addKnownObject(WorldObject object)
	{
		sendInfoFrom(object);
	}
	
	@Override
	public void removeKnownObject(WorldObject object)
	{
		super.removeKnownObject(object);
		
		// send Server-Client Packet DeleteObject to the Player
		sendPacket(new DeleteObject(object, (object instanceof Player) && ((Player) object).isSeated()));
	}
	
	public final void refreshInfos()
	{
		for (WorldObject object : getKnownType(WorldObject.class))
		{
			if (object instanceof Player && ((Player) object).isInObserverMode())
				continue;
			
			sendInfoFrom(object);
		}
	}
	
	private final void sendInfoFrom(WorldObject object)
	{
		if (object.getPolyType() == PolyType.ITEM)
			sendPacket(new SpawnItem(object));
		else
		{
			// send object info to player
			object.sendInfo(this);
			
			if (object instanceof Creature)
			{
				// Update the state of the Creature object client side by sending Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the Player
				Creature obj = (Creature) object;
				if (obj.hasAI())
					obj.getAI().describeStateToPlayer(this);
			}
		}
	}

	  private int _isPreview;
	  private int _isEquip;
	  private int _isWepEquip;
	  public int _lastX;
	  public int _lastY;
	  public int _lastZ;
	
	  public int isPreview()
	  {
	    return this._isPreview;
	  }
	  
	  public void setPreview(int id)
	  {
	    this._isPreview = id;
	  }
	  
	  public int isEquip()
	  {
	    return this._isEquip;
	  }
	  
	  public void setEquip(int id)
	  {
	    this._isEquip = id;
	  }
	  
	  public int isWepEquip()
	  {
	    return this._isWepEquip;
	  }
	  
	  public void setWepEquip(int id)
	  {
	    this._isWepEquip = id;
	  }
	  
	  
	  public void setLastCords(int x, int y, int z)
	  {
	    this._lastX = x;
	    this._lastY = y;
	    this._lastZ = z;
	  }

		public long getOnlineTime()
		{
			return _onlineTime;
		}

		
		private boolean _isPartyInRefuse = false; // Party Refusal Mode
		
		    public boolean isPartyInRefuse()

		    {

		        return _isPartyInRefuse;

		    }

		

		    public void setIsPartyInRefuse(boolean value)

		    {

		        _isPartyInRefuse = value;

		    }


			  public int _originalTitleColorTournament = 0;
			  public String _originalTitleTournament;
		      private boolean _UnrealTournament;
		      private boolean _XtremeTournament;
			  public int duelist_cont = 0;
		      public int dreadnought_cont = 0;
		      public int tanker_cont = 0;
		      public int dagger_cont = 0;
		      public int archer_cont = 0;
		      public int bs_cont = 0;
		      public int archmage_cont = 0;
		      public int soultaker_cont = 0;
		      public int mysticMuse_cont = 0;
		      public int stormScreamer_cont = 0;
		      public int titan_cont = 0;
		      public int grandKhauatari_cont = 0;
		      public int dominator_cont = 0;
		      public int doomcryer_cont = 0;
		      
				public boolean isUnrealTournament() {
					return _UnrealTournament;
				}
				
				public boolean isXtremeTournament() {
					return _XtremeTournament;
				}

				private int _eventPoints = 0;
				public String _originalTitle;

				
				public int getPointScore()
				{
					return _eventPoints;
				}
				
				public void increasePointScore()
				{
					_eventPoints++;
				}
				
				public void clearPoints()
				{
					_eventPoints = 0;
				}
				
				  private boolean _isManutencao = false;

				  public void setManutencao(boolean Manutencao)
				  {
				    this._isManutencao = Manutencao;
				  }
				  
				  public boolean isManutencao()
				  {
				    return this._isManutencao;
				  }
				  
				  private boolean _Blockpermanente = false;
				  
				  public void BlockPermanente(boolean custom)
				  {
				    this._Blockpermanente = custom;
				  }
				  
				  public boolean BlockPermanente()
				  {
				    return this._Blockpermanente;
				  }

					// Custom Character SQL String Definitions:
					private static final String STATUS_DATA_GET = "SELECT hero, noble, hero_end_date FROM characters_hero_data WHERE obj_Id = ?";
					
					
					public void restoreCustomStatus()
					{
						if (Config.DEVELOPER)
							_log.info("Restoring character status " + getName() + " from database...");
						
						int hero = 0;
						int noble = 0;
						long hero_end = 0;
						
						try (Connection con = L2DatabaseFactory.getInstance().getConnection())
						{
							PreparedStatement statement = con.prepareStatement(STATUS_DATA_GET);
							statement.setInt(1, getObjectId());
							ResultSet result = statement.executeQuery();
							
							while (result.next())
							{
								hero = result.getInt("hero");
								noble = result.getInt("noble");
								hero_end = result.getLong("hero_end_date");
							}
							
							result.close();
							statement.close();
						}
						catch (Exception e)
						{
							_log.warning("Error: could not restore char custom data info: " + e);
						}
						
						if (hero > 0 && (hero_end == 0 || hero_end > System.currentTimeMillis()))
							setHero(true);
						else
							destroyItem("HeroEnd", 6842, 1, null, false);
						
						if (noble > 0)
							setNoble(true, true);
					}

			public void colorsPkCheck()
			{
				if (Config.ALLOW_PK_TITLE_COLOR_SYSTEM)
				{
				for (int i : Config.PK_TITLE_COLORS.keySet())
				{
						if (getPkKills() >= i)
					{
						getAppearance().setTitleColor(Config.PK_TITLE_COLORS.get(i));
						broadcastUserInfo();
					 }
				  }
			   }
			}
			
			public void colorsPvPCheck()
			{
				 if (Config.ALLOW_PVP_NAME_COLOR_SYSTEM)
				{
					for (int e : Config.PVP_NAME_COLORS.keySet())
					{
						 if (getPvpKills() >= e)
						{
						   getAppearance().setNameColor(Config.PVP_NAME_COLORS.get(e));
						   broadcastUserInfo();
						}
					}
				 }
			  }
			  private int _SexChangeItemId = 0;
			  public int getSexChangeItemId()
			  {
			    return this._SexChangeItemId;
			  }
			  
			  public void setSexChangeItemId(int itemId)
			  {
			    this._SexChangeItemId = itemId;
			  }
		  
				private int _classChangeItemId = 0;
				
				public int getClassChangeItemId()
				{
					return _classChangeItemId;
				}
				
				public void setClassChangeItemId(int itemId)
				{
					_classChangeItemId = itemId;
				}
				
				private int _nameChangeItemId = 0;
				
				public int getNameChangeItemId()
				{
					return _nameChangeItemId;
				}
				
				public void setNameChangeItemId(int itemId)
				{
					_nameChangeItemId = itemId;
				}

				  private boolean _isCMultisell = false;
				  
				  public boolean isCMultisell()
				  {
				    return this._isCMultisell;
				  }
				  
				  public void setIsUsingCMultisell(boolean b)
				  {
				    this._isCMultisell = b;
				  }
				  
				  public int _vip_days = 0;
				  public int _hero_days = 0;
				  public int _class_id = 0;
				  public int _sex_id = 0;
				  public String _change_Name = "";
				  private boolean _isVip;
				  
				  public boolean isVip()
				  {
				    return this._isVip;
				  }
				  
				  public void setVip(boolean vip)
				  {
					  if(Config.VIP_SKILL)
					  {
						if (vip)
						{
							for (L2Skill skill : SkillTable.getVipSkills())
								addSkill(skill, false);
						}
						else
						{
							for (L2Skill skill : SkillTable.getVipSkills())
								removeSkill(skill.getId(), false);
						}
					  }
						
				    _isVip = vip;
				    
				    sendSkillList();

				  }


				  private boolean _isHeroeterno = false;
				  
				  public void setHeroEterno(boolean heroeterno)
				  {
				    this._isHeroeterno = heroeterno;
				  }
				  
				  public boolean isHeroEterno()
				  {
				    return this._isHeroeterno;
				  }

				  public void enteredNoLanding(int delay)
				  {
				    this._dismountTask = ThreadPool.schedule(new Runnable()
				    {
				      @Override
					public void run()
				      {
				        Player.this.dismount();
				      }
				    }, delay * 1000);
				  }


				public void onVipEnter(Player player)
				{
				    if (VipManager.getInstance().hasVipPrivileges(player.getObjectId()) || (player.isVip())) {
					      ThreadPool.schedule(new VIPReward(player), 5000L);
					    }
					
				}
				
				  public void removeItens()
				  {
				    if (Config.REMOVE_WEAPON)
				    {
				      ItemInstance rhand = getInventory().getPaperdollItem(7);
				      if (rhand != null)
				      {
				        ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(rhand.getItem().getBodyPart());
				        InventoryUpdate iu = new InventoryUpdate();
				        for (ItemInstance element : unequipped) {
				          iu.addModifiedItem(element);
				        }
				        sendPacket(iu);
				      }
				    }
				    if (Config.REMOVE_CHEST)
				    {
				      ItemInstance chest = getInventory().getPaperdollItem(10);
				      if (chest != null)
				      {
				        ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(chest.getItem().getBodyPart());
				        InventoryUpdate iu = new InventoryUpdate();
				        for (ItemInstance element : unequipped) {
				          iu.addModifiedItem(element);
				        }
				        sendPacket(iu);
				      }
				    }
				    if (Config.REMOVE_LEG)
				    {
				      ItemInstance legs = getInventory().getPaperdollItem(11);
				      if (legs != null)
				      {
				        ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(legs.getItem().getBodyPart());
				        InventoryUpdate iu = new InventoryUpdate();
				        for (ItemInstance element : unequipped) {
				          iu.addModifiedItem(element);
				        }
				        sendPacket(iu);
				      }
				    }
				  }
				  
					//HWID Ban 
					private String _hwid;
					
					public String getHWid()
					{
						if (getClient() == null)
						{
							return _hwid;
						}
						_hwid = getClient().getHWID();
						return _hwid;
					}

					private boolean _HwidBlock;
					
					public void setHwidBlock(boolean comm)
					{
						_HwidBlock = comm;
					}
					
					public boolean isHwidBlocked()
					{
						return _HwidBlock;
					}

					  public static Player loadPhantom(int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean)
					  {
					    Player localPlayer = null;
					    
					    int[] classes = { 0, 18, 31, 44, 10, 25, 38, 49, 53 };
					    
					    int i = classes[Rnd.get(classes.length)];
					    if (paramInt3 > 0) {
					      i = paramInt3;
					    }
					    Sex sex;
					    if (Rnd.get(100) < 70) {
					      sex = Sex.MALE;
					    } else {
					      sex = Sex.FEMALE;
					    }
					    PlayerTemplate localL2PcTemplate = PlayerData.getInstance().getTemplate(i);
					    byte b = (byte)Rnd.get(3);
					    PcAppearance localPcAppearance = new PcAppearance(b, b, b, sex);
					    if (paramBoolean) {
					      localPlayer = new Player(paramInt1, localL2PcTemplate, "fake_qwerty", localPcAppearance);
					    } else {
					      localPlayer = new Player(paramInt1, localL2PcTemplate, "fake_qwerty", localPcAppearance);
					    }
					    localPlayer.setIsPhantom(true);
					    localPlayer.setAccessLevel(0);
					    localPlayer.setHeading(Rnd.get(1, 65535));
					    localPlayer.setHero(false);
					    localPlayer.setClassId(i);
					    localPlayer.getStat().addExp(net.sf.l2j.gameserver.model.base.Experience.LEVEL[81]);
					    
					    return localPlayer;
					  }
					  
					  
					  public static Player loadMystic(int paramInt1, int paramInt2, int paramInt3)
					  {
					    Player localPlayer = null;
					    
					    int[] classes = { 0, 18, 44, 10, 25, 49, 53 };
					    
					    int i = classes[Rnd.get(classes.length)];
					    if (paramInt3 > 0) {
					      i = paramInt3;
					    }
					    Sex sex;
					    if (Rnd.get(100) < 70) {
					      sex = Sex.MALE;
					    } else {
					      sex = Sex.FEMALE;
					    }
					    PlayerTemplate localL2PcTemplate = PlayerData.getInstance().getTemplate(i);
					    byte b = (byte)Rnd.get(3);
					    PcAppearance localPcAppearance = new PcAppearance(b, b, b, sex);
					    
					    localPlayer = new Player(paramInt1, localL2PcTemplate, "fake_qwerty", localPcAppearance);
					    
					    localPlayer.setIsPhantom(true);
					    localPlayer.setIsPhantomMysticMuse(true);
					    localPlayer.setHeading(Rnd.get(1, 65535));
					    localPlayer.setAccessLevel(0);
					    localPlayer.setHero(false);
					    localPlayer.setClassId(i);
					    localPlayer.getStat().addExp(net.sf.l2j.gameserver.model.base.Experience.LEVEL[81]);
					    
					    return localPlayer;
					  }
					  
					  public static Player loadStormScream(int paramInt1, int paramInt2, int paramInt3)
					  {
					    Player localPlayer = null;
					    
					    int[] classes = { 0, 31, 44, 10, 38, 49, 53 };
					    
					    int i = classes[Rnd.get(classes.length)];
					    if (paramInt3 > 0) {
					      i = paramInt3;
					    }
					    Sex sex;
					    if (Rnd.get(100) < 70) {
					      sex = Sex.MALE;
					    } else {
					      sex = Sex.FEMALE;
					    }
					    PlayerTemplate localL2PcTemplate = PlayerData.getInstance().getTemplate(i);
					    byte b = (byte)Rnd.get(3);
					    PcAppearance localPcAppearance = new PcAppearance(b, b, b, sex);
					    
					    localPlayer = new Player(paramInt1, localL2PcTemplate, "fake_qwerty", localPcAppearance);
					    
					    localPlayer.setIsPhantom(true);
					    localPlayer.setIsPhantomStormScream(true);
					    localPlayer.setHeading(Rnd.get(1, 65535));
					    localPlayer.setAccessLevel(0);
					    localPlayer.setHero(false);
					    localPlayer.setClassId(i);
					    localPlayer.getStat().addExp(net.sf.l2j.gameserver.model.base.Experience.LEVEL[81]);
					    
					    return localPlayer;
					  }
					  
					  public void rndWalk()
					  {
					    int x = getX();
					    int y = getY();
					    int z = getZ();
					    switch (Rnd.get(1, 7))
					    {
					    case 1: 
					      x = getLastX();
					      y = getLastY();
					      z = getLastZ();
					      break;
					    case 2: 
					      x += 200;
					      y -= 60;
					      break;
					    case 3: 
					      x += 400;
					      y -= 60;
					      break;
					    case 4: 
					      x = getLastX();
					      y = getLastY();
					      z = getLastZ();
					      break;
					    case 5: 
					      x -= 200;
					      y += 60;
					      break;
					    case 6: 
					      x -= 400;
					      y += 60;
					      break;
					    case 7: 
					      x = getLastX();
					      y = getLastY();
					      z = getLastZ();
					    }
					    setRunning();
					    if (GeoEngine.getInstance().canMoveToTarget(getX(), getY(), getZ(), x, y, z)) {
					      getAI().setIntention(CtrlIntention.MOVE_TO, new Location(x, y, z));
					    }
					  }
					  
					  public int getLastX()
					  {
					    return this._lastX;
					  }
					  
					  public int getLastY()
					  {
					    return this._lastY;
					  }
					  
					  public int getLastZ()
					  {
					    return this._lastZ;
					  }
					  
					  	public void starLocation()
					  	{
					  		Location zone1 = getRandomLoc1();
					  		
					  		int getX = zone1.getX() + Rnd.get(-80, 80);
					  		int getY = zone1.getY() + Rnd.get(-80, 80);
					  		setPhantomLoc(getX, getY, zone1.getZ());
					  		spawnMe(getX, getY, zone1.getZ());
					  		
					  		setLastCords(getX(), getY(), getZ());
					  	}
					  	
					  		public void starTeleport()
					  	{
					  			Location zone1 = getRandomLoc1();
					  			
					  			int getX = zone1.getX() + Rnd.get(-80, 80);
					  			int getY = zone1.getY() + Rnd.get(-80, 80);
					  			teleToLocation(getX, getY, zone1.getZ(), 20);
					  		}
					  		

					  		@SuppressWarnings("null")
							public Location getRandomLoc1()
					  		{
					  			Location loc = null;
					  			if (loc == null)
					  				loc = PhantomArchMage._zone_1_Loc.get(Rnd.get(0, PhantomArchMage._zone_1_locsCount));
					  			return loc;
					  		}
					  		
					  		public Location _phantomLoc = null;
					  		
					  		public void setPhantomLoc(int paramInt1, int paramInt2, int paramInt3)
					  		{
					  			_phantomLoc = new Location(paramInt1, paramInt2, paramInt3);
					  		}
					  		
					  		public Location getPhantomLoc()
					  		{
					  			return _phantomLoc;
					  		}
					  		
					  		public void starAttack()
					  		{
					  			if (isPhantomArchMage())
					  				PhantomArchMage.doCastlist(this);
					  			else if (isPhantomMysticMuse())
					  				PhantomMysticMuse.doCastlist(this);
					  			else if (isPhantomStormScream())
					  				PhantomStormScream.doCastlist(this);
					  		}

}