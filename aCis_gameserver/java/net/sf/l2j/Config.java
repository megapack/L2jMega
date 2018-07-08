package net.sf.l2j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import net.sf.l2j.commons.config.ExProperties;
import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.util.StringUtil;

/**
 * This class contains global server configuration.<br>
 * It has static final fields initialized from configuration files.<br>
 * @author mkizub
 */
public final class Config
{
	private static final Logger _log = Logger.getLogger(Config.class.getName());
	
	public static final String CLANS_FILE = "./config/clans.properties";
	public static final String EVENTS_FILE = "./config/events.properties";
	public static final String GEOENGINE_FILE = "./config/geoengine.properties";
	public static final String HEXID_FILE = "./config/hexid.txt";
	public static final String LOGIN_CONFIGURATION_FILE = "./config/loginserver.properties";
	public static final String NPCS_FILE = "./config/npcs.properties";
	public static final String PLAYERS_FILE = "./config/players.properties";
	public static final String SERVER_FILE = "./config/server.properties";
	public static final String SIEGE_FILE = "./config/siege.properties";
	public static final String FAKES_FILE = "./config/custom/fakeplayers_settings.properties";
	public static final String PVP_FILE = "./config/custom/pvp_settings.properties";
	public static final String PLAYER_FILE = "./config/custom/player_settings.properties";
	public static final String NEWBIE_FILE = "./config/custom/newbie_settings.properties";
	public static final String ENCHANT_FILE = "./config/custom/enchant_settings.properties";
	public static final String TOUR_FILE = "./config/custom/events/batletour_settings.properties";
	public static final String GUARDIAN_FILE = "./config/custom/boss_guardian_settings.properties";
	public static final String ADMIN_FILE = "./config/custom/admin_settings.properties";
	public static final String BALANCE_FILE = "./config/custom/balance_settings.properties";
	public static final String PARTY_FILE = "./config/custom/events/partyfarm_settings.properties";
	public static final String BOT_FILE = "./config/custom/bot_prevention_settings.properties";
	public static final String L2JMEGA_FILE = "./config/custom/l2jmega_settings.properties";
	public static final String PHANTOM_FILE = "./config/custom/fakepvp/fakepvp.properties";




	
	public static boolean ALLOW_PHANTOM_PLAYERS = false;
	
	public static int PHANTOM_PLAYERS_ENCHANT_MIN;
	public static int PHANTOM_PLAYERS_ENCHANT_MAX;
	public static boolean PHANTOM_TITLE_CONFIG;
	public static boolean PHANTOM_PLAYERS_SOULSHOT_ANIM;
	public static boolean PHANTOM_PLAYERS_ARGUMENT_ANIM;
	
	public static String PHANTOM_TITLE_MSG;
	public static List<String> PHANTOM_TITLE = new ArrayList<>();
	public static String NAME_COLOR;
	public static String TITLE_COLOR;
	public static String PHANTOM_NAME_CLOLORS;
	public static String PHANTOM_TITLE_CLOLORS;
	public static ArrayList<String> PHANTOM_PLAYERS_NAME_CLOLORS = new ArrayList<>();
	public static ArrayList<String> PHANTOM_PLAYERS_TITLE_CLOLORS = new ArrayList<>();
	public static boolean ALLOW_PHANTOM_FACE;
	public static String PHANTOM_FACE;
	public static List<Integer> LIST_PHANTOM_FACE;
	
	public static String PHANTOM_PLAYERS_TITLE_MSG;
	public static List<String> PHANTOM_PLAYERS_TITLE = new ArrayList<>();
	
	public static long PHANTOM_DELAY_FIRST;
	public static int PHANTOM_DELAY_SPAWN_FIRST;
	public static long DISCONNETC_DELAY;
	
	public static String PHANTOM_PLAYERS_ARCHMAGE_1;
	public static String PHANTOM_PLAYERS_MYSTICMUSE_1;
	public static String PHANTOM_PLAYERS_STORMSCREAM_1;
	
	public static String PHANTOM_NAME_ARCHMAGE_1;
	public static List<String> PHANTOM_NAME_ARCHMAGE_LIST = new ArrayList<>();
	public static String PHANTOM_NAME_MYSTICMUSE_1;
	public static List<String> PHANTOM_NAME_MYSTICMUSE_LIST = new ArrayList<>();
	public static String PHANTOM_NAME_STORMSCREAM_1;
	public static List<String> PHANTOM_NAME_STORMSCREAM_LIST = new ArrayList<>();
	
	public static int PHANTOM_SURRENDER_INTERVAL;
	
	public static int PHANTOM_MAGE_RANDOM_WALK;
	public static int PHANTOM_MAGE_INTERVAL_WALK;
	public static int PHANTOM_MAGE_INTERVAL_TARGET;
	public static int PHANTOM_MAGE_INTERVAL_CHECK_TARGET;
	
	public static int PHANTOM_ARCHMAGE_MATCK;
	public static int PHANTOM_ARCHMAGE_PERCENTAGE;
	public static int PHANTOM_ARCHMAGE_DANO_INTERVAL;
	
	public static int PHANTOM_SPELLSINGER_MATCK;
	public static int PHANTOM_SPELLSINGER_PERCENTAGE;
	public static int PHANTOM_SPELLSINGER_DANO_INTERVAL;
		
	public static int PHANTOM_SPELLHOLLER_MATCK;
	public static int PHANTOM_SPELLHOLLER_PERCENTAGE;
	public static int PHANTOM_SPELLHOLLER_DANO_INTERVAL;
	// end phantom 
	public static int TIME_KICK;
	public static int MAX_ITEM_ENCHANT_KICK;
	/** Auto Restart */
	public static boolean RESTART_BY_TIME_OF_DAY;
	public static int RESTART_SECONDS;
	public static String[] RESTART_INTERVAL_BY_TIME_OF_DAY;
	 /** skill hero in subclass */
	public static boolean ALLOW_HERO_SUBSKILL;
	/** disable attack npcs */
	public static boolean DISABLE_ATTACK_NPC_TYPE;
	public static String ALLOWED_NPC_TYPES;
	public static ArrayList<String> LIST_ALLOWED_NPC_TYPES = new ArrayList<>();
	/** restricion for chat */
	public static boolean GLOBAL_CHAT_WITH_PVP;
	public static int GLOBAL_PVP_AMOUNT;
	public static String DEFAULT_GLOBAL_CHAT;
	public static boolean DISABLE_CAPSLOCK;
	public static boolean TRADE_CHAT_WITH_PVP;
	public static int TRADE_PVP_AMOUNT;
	public static String DEFAULT_TRADE_CHAT;
	public static boolean ADD_SKILL_NOBLES;
    /** banking system */
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	/** disable item config for classe*/
	public static boolean ALT_DISABLE_ITEM_FOR_CLASSES;
	public static String DISABLE_ITEM_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_ITEM_CLASSES = new ArrayList<>();
	public static ArrayList<Integer> ITEM_LIST = new ArrayList<>();
	public static String ITEM_ID;
    /** open e close door */
	public static String DOORS_IDS_TO_OPEN_STRING;
	public static List<Integer> DOORS_IDS_TO_OPEN = new ArrayList<>();
	public static String DOORS_IDS_TO_CLOSE_STRING;
	public static List<Integer> DOORS_IDS_TO_CLOSE = new ArrayList<>();
	/** restricion equipament */
	public static boolean REMOVE_WEAPON;
	public static boolean REMOVE_CHEST;
	public static boolean REMOVE_LEG;
	public static boolean ALLOW_LIGHT_USE_HEAVY;
	public static String NOTALLOWCLASS;
	public static List<Integer> NOTALLOWEDUSEHEAVY;
	public static boolean ALLOW_HEAVY_USE_LIGHT;
	public static String NOTALLOWCLASSE;
	public static List<Integer> NOTALLOWEDUSELIGHT;
	public static String MAGE_ID_RESTRICT;
	public static List<Integer> MAGE_LISTID_RESTRICT;
	public static String FIGHTER_ID_RESTRICT;
	public static List<Integer> FIGHTER_LISTID_RESTRICT;
    /** Disable bow for classes */
	public static boolean ALT_DISABLE_BOW_CLASSES;
	public static String DISABLE_BOW_CLASSES_STRING;
	public static ArrayList<Integer> DISABLE_BOW_CLASSES = new ArrayList<>();
	/** custom pvp zone */
	public static boolean CUSTOM_TELEGIRAN_ON_DIE;
	public static boolean PARTY_PVPZONE;
	public static boolean BS_PVPZONE;
	public static boolean WYVERN_PVPZONE;
	/** Bot prevention */
	public static boolean BOTS_PREVENTION;
	public static int KILLS_COUNTER;
	public static int KILLS_COUNTER_RANDOMIZATION;
	public static int VALIDATION_TIME;
	public static int PUNISHMENT;
	public static int PUNISHMENT_TIME;
    /** vip e donate system */
	public static String[] CLEAR_VIP_INTERVAL_BY_TIME_OF_DAY;
	public static boolean CLEAR_VIP_EVENT_ENABLED;
	public static List<int[]> VIP_REWARD_LIST = new ArrayList<>();
	public static float VIP_ADENA_RATE;
	public static float VIP_DROP_RATE;
	public static float VIP_SPOIL_RATE;
	public static int VIP_SKILLS;
	public static boolean VIP_SKILL;
	public static boolean DONATESYSTEM;
	public static int DONATE_COIN_ID;
	public static int VIP_30_DAYS_PRICE;
	public static int VIP_60_DAYS_PRICE;
	public static int VIP_90_DAYS_PRICE;
	public static int VIP_ETERNAL_PRICE;
	public static int HERO_30_DAYS_PRICE;
	public static int HERO_60_DAYS_PRICE;
	public static int HERO_90_DAYS_PRICE;
	public static int HERO_ETERNAL_PRICE;
	public static int DONATE_CLASS_PRICE;
	public static int DONATE_NAME_PRICE;
	public static int DONATE_SEX_PRICE;
	/** PvP Color System */
	public static boolean ALLOW_PK_TITLE_COLOR_SYSTEM;
	public static boolean ALLOW_PVP_NAME_COLOR_SYSTEM;
	public static Map<Integer, Integer> PK_TITLE_COLORS = new HashMap<>();
	public static Map<Integer, Integer> PVP_NAME_COLORS = new HashMap<>();
	public static boolean LEAVE_BUFFS_ON_DIE;
	public static boolean CHAOTIC_LEAVE_BUFFS_ON_DIE;
	/** Party Farm */
	public static boolean PARTY_MESSAGE_ENABLED;
	public static long NPC_SERVER_DELAY;
	public static boolean PARTY_FARM_BY_TIME_OF_DAY;
	public static boolean START_PARTY;
	public static int EVENT_BEST_FARM_TIME;
	public static String[] EVENT_BEST_FARM_INTERVAL_BY_TIME_OF_DAY;
	public static int PARTY_FARM_MONSTER_DALAY;
	public static String PARTY_FARM_MESSAGE_TEXT;
	public static int PARTY_FARM_MESSAGE_TIME;
	public static int monsterId;
	public static int MONSTER_LOCS_COUNT;
	public static int[][] MONSTER_LOCS;
	  /** balance */
	public static boolean ENABLE_CUSTOM_CRIT;
	public static int MAX_PCRIT_RATE;
	public static int PCRIT_RATE_ArcherHuman;
	public static int PCRIT_RATE_ArcherElfo;
	public static int PCRIT_RATE_ArcherDarkElfo;
	public static int MCRIT_RATE_Archmage;
	public static int MCRIT_RATE_Soultaker;
	public static int MCRIT_RATE_Mystic_Muse;
	public static int MCRIT_RATE_Storm_Screamer;
	public static int MCRIT_RATE_Dominator;
	public static int MAX_MCRIT_RATE;
	 /** oly balance */
	public static boolean OLY_ENABLE_CUSTOM_CRIT;
	public static int OLY_MAX_PCRIT_RATE;
	public static int OLY_PCRIT_RATE_ArcherHuman;
	public static int OLY_PCRIT_RATE_ArcherElfo;
	public static int OLY_PCRIT_RATE_ArcherDarkElfo;
	public static int OLY_MCRIT_RATE_Archmage;
	public static int OLY_MCRIT_RATE_Soultaker;
	public static int OLY_MCRIT_RATE_Mystic_Muse;
	public static int OLY_MCRIT_RATE_Storm_Screamer;
	public static int OLY_MCRIT_RATE_Dominator;
	public static int OLY_MAX_MCRIT_RATE;
	public static int MAX_MATK_SPEED;
	public static int MAX_PATK_SPEED;
	public static int MAX_PATK_SPEED_GHOST;
	public static int MAX_PATK_SPEED_MOONL;
	public static int BOSS_ID;
	public static boolean ALLOW_AUTO_NOBLESS_FROM_BOSS;
	public static int RADIUS_TO_RAID;
	public static int BLOW_ATTACK_FRONT;
	public static int BLOW_ATTACK_SIDE;
	public static int BLOW_ATTACK_BEHIND;
	public static int BACKSTAB_ATTACK_FRONT;
	public static int BACKSTAB_ATTACK_SIDE;
	public static int BACKSTAB_ATTACK_BEHIND;
	public static int ANTI_SS_BUG_1;
	public static int ANTI_SS_BUG_2;
	public static int ANTI_SS_BUG_3;
	public static String[] FORBIDDEN_NAMES;
	public static String[] GM_NAMES;
	public static int TIME_MANUTENCAO;
	public static int TIME_ADMIN;
	public static int TIME_MULTIBOX;
	public static boolean ALLOW_MANUTENCAO;
	public static String MANUTENCAO_TEXT;
	public static boolean MULTIBOX_PROTECTION_ENABLED;
	public static int MULTIBOX_PROTECTION_CLIENTS_PER_PC;
	public static int MULTIBOX_PROTECTION_PUNISH;
	/** Guardian boss  */
	public static boolean ENABLE_GUARDIAN;
	public static int ID_GUARDIAN_BAIUM;
	public static int ID_TELEPORT_BAIUM;
	public static int TELEPORT_BAIUM_LOCX;
	public static int TELEPORT_BAIUM_LOCY;
	public static int TELEPORT_BAIUM_LOCZ;
	public static int BAIUM_TELEPORT_LOCX;
	public static int BAIUM_TELEPORT_LOCY;
	public static int BAIUM_TELEPORT_LOCZ;
	public static int TELEPORT_BAIUM_RESPAWN_TIME;
	public static int TELEPORT_BAIUM_DELETE_TIME;
	public static int ID_GUARDIAN_B1;
	public static int ID_TELEPORT_B1;
	public static int TELEPORT_B1_LOCX;
	public static int TELEPORT_B1_LOCY;
	public static int TELEPORT_B1_LOCZ;
	public static int B1_TELEPORT_LOCX;
	public static int B1_TELEPORT_LOCY;
	public static int B1_TELEPORT_LOCZ;
	public static int TELEPORT_B1_RESPAWN_TIME;
	public static int TELEPORT_B1_DELETE_TIME;
	
	//--------------------------------
	public static int ID_GUARDIAN_B2;
	public static int ID_TELEPORT_B2;
	public static int TELEPORT_B2_LOCX;
	public static int TELEPORT_B2_LOCY;
	public static int TELEPORT_B2_LOCZ;
	public static int B2_TELEPORT_LOCX;
	public static int B2_TELEPORT_LOCY;
	public static int B2_TELEPORT_LOCZ;
	public static int TELEPORT_B2_RESPAWN_TIME;
	public static int TELEPORT_B2_DELETE_TIME;
	
	//--------------------------------
	public static int ID_GUARDIAN_B3;
	public static int ID_TELEPORT_B3;
	public static int TELEPORT_B3_LOCX;
	public static int TELEPORT_B3_LOCY;
	public static int TELEPORT_B3_LOCZ;
	public static int B3_TELEPORT_LOCX;
	public static int B3_TELEPORT_LOCY;
	public static int B3_TELEPORT_LOCZ;
	public static int TELEPORT_B3_RESPAWN_TIME;
	public static int TELEPORT_B3_DELETE_TIME;
	
	//--------------------------------
	public static int ID_GUARDIAN_B4;
	public static int ID_TELEPORT_B4;
	public static int TELEPORT_B4_LOCX;
	public static int TELEPORT_B4_LOCY;
	public static int TELEPORT_B4_LOCZ;
	public static int B4_TELEPORT_LOCX;
	public static int B4_TELEPORT_LOCY;
	public static int B4_TELEPORT_LOCZ;
	public static int TELEPORT_B4_RESPAWN_TIME;
	public static int TELEPORT_B4_DELETE_TIME;
	//--------------------------------
	public static int ID_GUARDIAN_B5;
	public static int ID_TELEPORT_B5;
	public static int TELEPORT_B5_LOCX;
	public static int TELEPORT_B5_LOCY;
	public static int TELEPORT_B5_LOCZ;
	public static int B5_TELEPORT_LOCX;
	public static int B5_TELEPORT_LOCY;
	public static int B5_TELEPORT_LOCZ;
	public static int TELEPORT_B5_RESPAWN_TIME;
	public static int TELEPORT_B5_DELETE_TIME;
	/** end */
	public static int ID_GUARDIAN_ZAKEN;
	public static int ID_TELEPORT_ZAKEN;
	public static int TELEPORT_ZAKEN_LOCX;
	public static int TELEPORT_ZAKEN_LOCY;
	public static int TELEPORT_ZAKEN_LOCZ;
	public static int TELEPORT_ZAKEN_RESPAWN_TIME;
	public static int TELEPORT_ZAKEN_DELETE_TIME;
	public static int ZAKEN_TELEPORT_LOCX;
	public static int ZAKEN_TELEPORT_LOCY;
	public static int ZAKEN_TELEPORT_LOCZ;
	public static int ID_GUARDIAN_ANTHARAS;
	public static int ID_TELEPORT_ANTHARAS;
	public static int TELEPORT_ANTHARAS_LOCX;
	public static int TELEPORT_ANTHARAS_LOCY;
	public static int TELEPORT_ANTHARAS_LOCZ;
	public static int TELEPORT_ANTHARAS_RESPAWN_TIME;
	public static int TELEPORT_ANTHARAS_DELETE_TIME;	
	public static int ANTHARAS_TELEPORT_LOCX;
	public static int ANTHARAS_TELEPORT_LOCY;
	public static int ANTHARAS_TELEPORT_LOCZ;
	public static int ID_GUARDIAN_FRINTEZZA;
	public static int ID_TELEPORT_FRINTEZZA;
	public static int TELEPORT_FRINTEZZA_LOCX;
	public static int TELEPORT_FRINTEZZA_LOCY;
	public static int TELEPORT_FRINTEZZA_LOCZ;
	public static int TELEPORT_FRINTEZZA_RESPAWN_TIME;
	public static int TELEPORT_FRINTEZZA_DELETE_TIME;
	public static int FRINTEZZA_TELEPORT_LOCX;
	public static int FRINTEZZA_TELEPORT_LOCY;
	public static int FRINTEZZA_TELEPORT_LOCZ;	
	public static int ID_GUARDIAN_VALAKAS;
	public static int ID_TELEPORT_VALAKAS;
	public static int TELEPORT_VALAKAS_LOCX;
	public static int TELEPORT_VALAKAS_LOCY;
	public static int TELEPORT_VALAKAS_LOCZ;
	public static int TELEPORT_VALAKAS_RESPAWN_TIME;
	public static int TELEPORT_VALAKAS_DELETE_TIME;
	public static int VALAKAS_TELEPORT_LOCX;
	public static int VALAKAS_TELEPORT_LOCY;
	public static int VALAKAS_TELEPORT_LOCZ;
	  /** Arena Event */
	public static boolean TOURNAMENT_SPAWNER_ENABLED;
	public static int[] TOURNAMENT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int TOURNAMENT_EVENT_EFFECTS_REMOVAL;
	public static boolean DUAL_BOX;
	public static boolean ARENA_EVENT_ENABLED;
	public static int ARENA_EVENT_COUNT;
	public static int[][] ARENA_EVENT_LOCS;
	public static int ARENA_PVP_AMOUNT;
	public static List<int[]> TOURNAMENT_ITEMS_REWARD;
	public static int ARENA_CHECK_INTERVAL;
	public static int ARENA_CALL_INTERVAL;
	public static int ARENA_WAIT_INTERVAL;
	public static String TOURNAMENT_ID_RESTRICT;
	public static List<Integer> TOURNAMENT_LISTID_RESTRICT;
	public static String DISABLE_TOURNAMENT_ID_CLASSES_STRING;
	public static List<Integer> DISABLE_TOURNAMENT_ID_CLASSES;
	public static boolean XTREME_TOURNAMENT_EVENT_ENABLED;
	public static int XTREME_TOURNAMENT_EVENT_COUNT;
	public static int[][] XTREME_TOURNAMENT_EVENT_LOCS;
	public static int XTREME_TOURNAMENT_PVP_AMOUNT;
	public static List<int[]> XTREME_TOURNAMENT_ITEMS_REWARD;
	public static int XTREME_TOURNAMENT_CHECK_INTERVAL;
	public static int XTREME_TOURNAMENT_CALL_INTERVAL;
	public static int XTREME_TOURNAMENT_WAIT_INTERVAL;
	public static int NPC_Heading;
	public static boolean UNREAL_TOURNAMENT_EVENT_ENABLED;
	public static int UNREAL_TOURNAMENT_EVENT_COUNT;
	public static int[][] UNREAL_TOURNAMENT_EVENT_LOCS;
	public static int UNREAL_TOURNAMENT_PVP_AMOUNT;
	public static List<int[]> UNREAL_TOURNAMENT_ITEMS_REWARD;
	public static int UNREAL_TOURNAMENT_CHECK_INTERVAL;
	public static int UNREAL_TOURNAMENT_CALL_INTERVAL;
	public static int UNREAL_TOURNAMENT_WAIT_INTERVAL;
	  /** Arena Event */
	public static String[] ARENA_EVENT_INTERVAL_BY_TIME_OF_DAY;
	public static boolean ARENA_EVENT_SUMMON;
	public static int ARENA_TIME;
	public static int ARENA_NPC;
	public static int NPC_locx;
	public static int NPC_locy;
	public static int NPC_locz;
	public static int ARENA_REWARD_ID;
	public static int ARENA_EVENT_COUNT_4X4;
	public static int[][] ARENA_EVENT_LOCS_4X4;
	public static int ARENA_EVENT_COUNT_9X9;
	public static int[][] ARENA_EVENT_LOCS_9X9;
	public static boolean ARENA_MESSAGE_ENABLED;
	public static String ARENA_MESSAGE_TEXT;
	public static int ARENA_MESSAGE_TIME;
	public static int duelist_COUNT_4X4;
	public static int dreadnought_COUNT_4X4;
	public static int tanker_COUNT_4X4;
	public static int dagger_COUNT_4X4;
	public static int archer_COUNT_4X4;
	public static int bs_COUNT_4X4;
	public static int archmage_COUNT_4X4;
	public static int soultaker_COUNT_4X4;
	public static int mysticMuse_COUNT_4X4;
	public static int stormScreamer_COUNT_4X4;
	public static int titan_COUNT_4X4;
	public static int grandKhauatari_COUNT_4X4;
	public static int dominator_COUNT_4X4;
	public static int doomcryer_COUNT_4X4;
	public static int duelist_COUNT_9X9;
	public static int dreadnought_COUNT_9X9;
	public static int tanker_COUNT_9X9;
	public static int dagger_COUNT_9X9;
	public static int archer_COUNT_9X9;
	public static int bs_COUNT_9X9;
	public static int archmage_COUNT_9X9;
	public static int soultaker_COUNT_9X9;
	public static int mysticMuse_COUNT_9X9;
	public static int stormScreamer_COUNT_9X9;
	public static int titan_COUNT_9X9;
	public static int grandKhauatari_COUNT_9X9;
	public static int dominator_COUNT_9X9;
	public static int doomcryer_COUNT_9X9;
	public static boolean ARENA_SKILL_PROTECT;
	public static List<Integer> ARENA_SKILL_LIST = new ArrayList<>();
	public static List<Integer> ARENA_STOP_SKILL_LIST = new ArrayList<>();
	public static String TITLE_COLOR_TEAM1;
	public static String TITLE_COLOR_TEAM2;
	public static String MSG_TEAM1;
	public static String MSG_TEAM2;

	/** Comandos */
	public static boolean STATUS_CMD;
	public static boolean INVENTORY_CMD;
	public static boolean SKILLS_CMD;
	
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static HashMap<Integer, Integer> SKILL_DURATION_LIST;
	  /** class damage */
	public static float ALT_FIGHTER_TO_SUMMON;
	public static float ALT_MAGE_TO_SUMMON;
	public static boolean ENABLE_CLASS_DAMAGES;
	public static boolean ENABLE_CLASS_DAMAGES_IN_OLY;
	public static boolean ENABLE_CLASS_DAMAGES_LOGGER;
	  /** Enchant item custom */
	public static HashMap<Integer, Integer> NORMAL_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> BLESS_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> CRYSTAL_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> DONATOR_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> NORMAL_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> BLESS_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> CRYSTAL_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> DONATOR_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> NORMAL_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> BLESS_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> CRYSTAL_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static HashMap<Integer, Integer> DONATOR_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static boolean ENCHANT_HERO_WEAPON;
	public static boolean SCROLL_STACKABLE;
	public static int GOLD_WEAPON;
	public static int GOLD_ARMOR;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static int ENCHANT_WEAPON_MAX;
	public static int ENCHANT_ARMOR_MAX;
	public static int ENCHANT_JEWELRY_MAX;
	public static int BLESSED_ENCHANT_WEAPON_MAX;
	public static int BLESSED_ENCHANT_ARMOR_MAX;
	public static int BLESSED_ENCHANT_JEWELRY_MAX;
	public static int BREAK_ENCHANT;
	public static int CRYSTAL_ENCHANT_MIN;
	public static int CRYSTAL_ENCHANT_WEAPON_MAX;
	public static int CRYSTAL_ENCHANT_ARMOR_MAX;
	public static int CRYSTAL_ENCHANT_JEWELRY_MAX;
	public static int DONATOR_ENCHANT_MIN;
	public static int DONATOR_ENCHANT_WEAPON_MAX;
	public static int DONATOR_ENCHANT_ARMOR_MAX;
	public static int DONATOR_ENCHANT_JEWELRY_MAX;
	public static boolean DONATOR_DECREASE_ENCHANT;
	public static boolean ENABLE_ENCHANT_ANNOUNCE;
	public static String ENCHANT_ANNOUNCE_LEVEL;
	public static ArrayList<Integer> LIST_ENCHANT_ANNOUNCE_LEVEL = new ArrayList<>();
	/** newbie system */
	public static int NEWBIE_DIST;
	public static int NEWBIE_LADO;
	public static int NEWBIE_ALTURA;
	public static boolean ENABLE_STARTUP;
	public static int[] TELE_TO_LOCATION = new int[3];
	public static int NEWBIE_ITEMS_ENCHANT;
	/** Protect items in oly */
	public static ArrayList<Integer> OLY_PROTECT_ITEMS = new ArrayList<>();
	/** Oly grade A */
	public static boolean OLLY_GRADE_A;
    /** Announce oly end */
	public static boolean ALT_OLY_END_ANNOUNCE;
	
    /** Maximo player por clan */
	public static int ALT_MAX_NUM_OF_MEMBERS_IN_CLAN;
	
    /** Custom olympiad period */
	public static boolean ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS;
	public static String ALT_OLY_PERIOD;
	public static int ALT_OLY_PERIOD_MULTIPLIER;
	
    /** Siege date */
	public static int DAY_TO_SIEGE;
	public static int HOUR_TO_SIEGE;
	
	/** Show hp/cp in pvp */
	public static boolean SHOW_HP_PVP;
	
	/** Startings for new chars */
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	public static int CUSTOM_START_LVL;
	public static int CUSTOM_SUBCLASS_LVL;
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	public static int STARTING_ADENA;
	public static boolean PLAYER_ITEMS;
	public static int[][] MAGE_ITEMS_LIST;
	public static int[][]FIGHTER_ITEMS_LIST;
	
	/** Advanced announce pvp end pk */
	public static boolean ANNOUNCE_PK_PVP;
	public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
	public static String ANNOUNCE_PK_MSG;
	public static String ANNOUNCE_PVP_MSG;
    /** Fake players */
	public static int LOC_X;
	public static int LOC_Y;
	public static int LOC_Z;
	public static int LOC_X1;
	public static int LOC_Y1;
	public static int LOC_Z1;
	public static int LOC_X2;
	public static int LOC_Y2;
	public static int LOC_Z2;
	public static int LOC_X3;
	public static int LOC_Y3;
	public static int LOC_Z3;
	public static int LOC_X4;
	public static int LOC_Y4;
	public static int LOC_Z4;
	public static int LOC_X5;
	public static int LOC_Y5;
	public static int LOC_Z5;
	public static int LOC_X6;
	public static int LOC_Y6;
	public static int LOC_Z6;
	public static int LOC_X7;
	public static int LOC_Y7;
	public static int LOC_Z7;
	public static int LOC_X8;
	public static int LOC_Y8;
	public static int LOC_Z8;
	public static int LOC_X9;
	public static int LOC_Y9;
	public static int LOC_Z9;
	public static int LOC_X10;
	public static int LOC_Y10;
	public static int LOC_Z10;
	public static int LOC_X11;
	public static int LOC_Y11;
	public static int LOC_Z11;
	public static int LOC_X12;
	public static int LOC_Y12;
	public static int LOC_Z12;
	public static int LOC_X13;
	public static int LOC_Y13;
	public static int LOC_Z13;
	public static int FAKE_PLAYERS_ENCHANT_MIN;
	public static int FAKE_PLAYERS_ENCHANT_MAX;
	public static String WEAPONS_TIRANTY;
	public static ArrayList<Integer> LIST_WEAPONS_TIRANTY;
    public static String WEAPONS_MAGES;
	public static ArrayList<Integer> LIST_WEAPONS_MAGES;
	public static String WEAPONS_DREADNOUGHT;
	public static ArrayList<Integer> LIST_WEAPONS_DREADNOUGHT;
	public static String WEAPONS_DUELIST;
	public static ArrayList<Integer> LIST_WEAPONS_DUELIST;
	public static String WEAPONS_TITANS;
	public static ArrayList<Integer> LIST_WEAPONS_TITANS;
	public static String WEAPONS_ARCHERS;
	public static ArrayList<Integer> LIST_WEAPONS_ARCHERS;
	public static String WEAPONS_DAGGER;
	public static ArrayList<Integer> LIST_WEAPONS_DAGGER;	
	public static String ARMORS;
	public static ArrayList<Integer> LIST_ARMORS_ROB;
	public static String ARMORS2;
	public static ArrayList<Integer> LIST_ARMORS_LIGHT;
	public static String ARMORS3;
	public static ArrayList<Integer> LIST_ARMORS_HEAVY;

	// --------------------------------------------------
	// Clans settings
	// --------------------------------------------------
	
	/** Clans */
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static int ALT_CLAN_WAR_PENALTY_WHEN_ENDED;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	
	/** Manor */
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_MIN;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	
	/** Clan Hall function */
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	
	// --------------------------------------------------
	// Events settings
	// --------------------------------------------------
	
	/** Olympiad */
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_WAIT_TIME;
	public static int ALT_OLY_WAIT_BATTLE;
	public static int ALT_OLY_WAIT_END;
	public static int ALT_OLY_START_POINTS;
	public static int ALT_OLY_WEEKLY_POINTS;
	public static int ALT_OLY_MIN_MATCHES;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int[][] ALT_OLY_CLASSED_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_REWARD;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int ALT_OLY_MAX_POINTS;
	public static int ALT_OLY_DIVIDER_CLASSED;
	public static int ALT_OLY_DIVIDER_NON_CLASSED;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	
	/** SevenSigns Festival */
	public static boolean ALT_GAME_CASTLE_DAWN;
	public static boolean ALT_GAME_CASTLE_DUSK;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	
	/** Four Sepulchers */
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	
	/** dimensional rift */
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static double RIFT_BOSS_ROOM_TIME_MUTIPLY;
	
	/** Wedding system */
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	
	/** Lottery */
	public static int ALT_LOTTERY_PRIZE;
	public static int ALT_LOTTERY_TICKET_PRICE;
	public static double ALT_LOTTERY_5_NUMBER_RATE;
	public static double ALT_LOTTERY_4_NUMBER_RATE;
	public static double ALT_LOTTERY_3_NUMBER_RATE;
	public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	
	/** Fishing tournament */
	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
	
	// --------------------------------------------------
	// GeoEngine
	// --------------------------------------------------
	
	/** Geodata */
	public static String GEODATA_PATH;
	public static int COORD_SYNCHRONIZE;
	
	/** Path checking */
	public static int PART_OF_CHARACTER_HEIGHT;
	public static int MAX_OBSTACLE_HEIGHT;
	
	/** Path finding */
	public static String PATHFIND_BUFFERS;
	public static int BASE_WEIGHT;
	public static int DIAGONAL_WEIGHT;
	public static int HEURISTIC_WEIGHT;
	public static int OBSTACLE_MULTIPLIER;
	public static int MAX_ITERATIONS;
	public static boolean DEBUG_PATH;
	public static boolean DEBUG_GEO_NODE;
	
	// --------------------------------------------------
	// HexID
	// --------------------------------------------------
	
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	
	// --------------------------------------------------
	// Loginserver
	// --------------------------------------------------
	
	public static String LOGIN_BIND_ADDRESS;
	public static int PORT_LOGIN;
	
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static boolean ACCEPT_NEW_GAMESERVER;
	
	public static boolean SHOW_LICENCE;
	
	public static boolean AUTO_CREATE_ACCOUNTS;
	
	public static boolean LOG_LOGIN_CONTROLLER;
	
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	
	// --------------------------------------------------
	// NPCs / Monsters
	// --------------------------------------------------
	
	/** Champion Mod */
	public static int CHAMPION_FREQUENCY;
	public static int CHAMP_MIN_LVL;
	public static int CHAMP_MAX_LVL;
	public static int CHAMPION_HP;
	public static int CHAMPION_REWARDS;
	public static int CHAMPION_ADENAS_REWARDS;
	public static double CHAMPION_HP_REGEN;
	public static double CHAMPION_ATK;
	public static double CHAMPION_SPD_ATK;
	public static int CHAMPION_REWARD;
	public static int CHAMPION_REWARD_ID;
	public static int CHAMPION_REWARD_QTY;
	
	/** Buffer */
	public static int BUFFER_MAX_SCHEMES;
	public static int BUFFER_STATIC_BUFF_COST;
	
	/** Misc */
	public static boolean ALLOW_CLASS_MASTERS;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_ENTIRE_TREE;
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_NPC_CREST;
	public static boolean SHOW_SUMMON_CREST;
	
	/** Wyvern Manager */
	public static boolean WYVERN_ALLOW_UPGRADER;
	public static int WYVERN_REQUIRED_LEVEL;
	public static int WYVERN_REQUIRED_CRYSTALS;
	
	/** Raid Boss */
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_DEFENCE_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	
	public static boolean RAID_DISABLE_CURSE;
	public static int RAID_CHAOS_TIME;
	public static int GRAND_CHAOS_TIME;
	public static int MINION_CHAOS_TIME;
	
	/** Grand Boss */
	public static int SPAWN_INTERVAL_AQ;
	public static int RANDOM_SPAWN_TIME_AQ;
	
	public static int SPAWN_INTERVAL_ANTHARAS;
	public static int RANDOM_SPAWN_TIME_ANTHARAS;
	public static int WAIT_TIME_ANTHARAS;
	
	public static int SPAWN_INTERVAL_BAIUM;
	public static int RANDOM_SPAWN_TIME_BAIUM;
	
	public static int SPAWN_INTERVAL_CORE;
	public static int RANDOM_SPAWN_TIME_CORE;
	
	public static int SPAWN_INTERVAL_FRINTEZZA;
	public static int RANDOM_SPAWN_TIME_FRINTEZZA;
	public static int WAIT_TIME_FRINTEZZA;
	
	public static int SPAWN_INTERVAL_ORFEN;
	public static int RANDOM_SPAWN_TIME_ORFEN;
	
	public static int SPAWN_INTERVAL_SAILREN;
	public static int RANDOM_SPAWN_TIME_SAILREN;
	public static int WAIT_TIME_SAILREN;
	
	public static int SPAWN_INTERVAL_VALAKAS;
	public static int RANDOM_SPAWN_TIME_VALAKAS;
	public static int WAIT_TIME_VALAKAS;
	
	
	  public static int SPAWN_ZAKEN_HOUR_OF_DAY;
	  public static int SPAWN_ZAKEN_MINUTE;
	  public static int WAIT_TIME_ZAKEN;
	  
		public static int ANNOUNCE_ID;
	
		public static String GRAND_BOSS;
		public static ArrayList<Integer> GRAND_BOSS_LIST = new ArrayList<>();
		
		public static String RAID_BOSS_DEFEATED_BY_PLAYER_MSG;
		public static String RAID_BOSS_DEFEATED_BY_CLAN_MEMBER_MSG;
		public static String RAID_BOSS_ALIVE_MSG;
		
		public static boolean ANNOUNCE_RAIDBOS_ALIVE_KILL;
		public static boolean ANNOUNCE_GRANBOS_ALIVE_KILL;
		public static boolean QUAKE_RAID_BOSS;
	
	/** AI */
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static int MAX_DRIFT_RANGE;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	
	// --------------------------------------------------
	// Players
	// --------------------------------------------------
	
	/** Misc */
	public static boolean EFFECT_CANCELING;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static int PLAYER_SPAWN_PROTECTION;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static double RESPAWN_RESTORE_HP;
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static boolean DEEPBLUE_DROP_RULES;
	public static boolean ALT_GAME_DELEVEL;
	public static int DEATH_PENALTY_CHANCE;
	
	/** Inventory & WH */
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_QUEST_ITEMS;
	public static int INVENTORY_MAXIMUM_PET;
	public static int MAX_ITEM_IN_PACKET;
	public static double ALT_WEIGHT_LIMIT;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int FREIGHT_SLOTS;
	public static boolean ALT_GAME_FREIGHTS;
	public static int ALT_GAME_FREIGHT_PRICE;
	

	
	/** Augmentations */
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	
	/** Karma & PvP */
	public static boolean KARMA_PLAYER_CAN_BE_KILLED_IN_PZ;
	public static boolean KARMA_PLAYER_CAN_SHOP;
	public static boolean KARMA_PLAYER_CAN_USE_GK;
	public static boolean KARMA_PLAYER_CAN_TELEPORT;
	public static boolean KARMA_PLAYER_CAN_TRADE;
	public static boolean KARMA_PLAYER_CAN_USE_WH;
	
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;
	
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	
	/** Party */
	public static String PARTY_XP_CUTOFF_METHOD;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_RANGE;
	
	/** GMs & Admin Stuff */
	public static int DEFAULT_ACCESS_LEVEL;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	
	/** petitions */
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	
	/** Crafting **/
	public static boolean IS_CRAFTING_ENABLED;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	
	/** Skills & Classes **/
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean MAGIC_FAILURES;
	public static int PERFECT_SHIELD_BLOCK_RATE;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean SP_BOOK_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean SUBCLASS_WITHOUT_QUESTS;
	
	/** Buffs */
	public static boolean STORE_SKILL_COOLTIME;
	public static int MAX_BUFFS_AMOUNT;
	
	// --------------------------------------------------
	// Sieges
	// --------------------------------------------------
	
	public static int SIEGE_LENGTH;
	public static int MINIMUM_CLAN_LEVEL;
	public static int MAX_ATTACKERS_NUMBER;
	public static int MAX_DEFENDERS_NUMBER;
	public static int ATTACKERS_RESPAWN_DELAY;
	
	// --------------------------------------------------
	// Server
	// --------------------------------------------------
	
	public static String GAMESERVER_HOSTNAME;
	public static int PORT_GAME;
	public static String HOSTNAME;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static int REQUEST_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	
	/** Access to database */
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	
	/** serverList & Test */
	public static boolean SERVER_LIST_BRACKET;
	public static boolean SERVER_LIST_CLOCK;
	public static int SERVER_LIST_AGE;
	public static boolean SERVER_LIST_TESTSERVER;
	public static boolean SERVER_LIST_PVPSERVER;
	public static boolean SERVER_GMONLY;
	
	/** clients related */
	public static int DELETE_DAYS;
	public static int MAXIMUM_ONLINE_USERS;
	
	/** Auto-loot */
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_RAID;
	
	/** Items Management */
	public static boolean ALLOW_DISCARDITEM;
	public static boolean MULTIPLE_ITEM_DROP;
	public static int HERB_AUTO_DESTROY_TIME;
	public static int ITEM_AUTO_DESTROY_TIME;
	public static int EQUIPABLE_ITEM_AUTO_DESTROY_TIME;
	public static Map<Integer, Integer> SPECIAL_ITEM_DESTROY_TIME;
	public static int PLAYER_DROPPED_ITEM_MULTIPLIER;
	
	/** Rate control */
	public static double RATE_XP;
	public static double RATE_SP;
	public static double RATE_PARTY_XP;
	public static double RATE_PARTY_SP;
	public static double RATE_DROP_ADENA;
	public static double RATE_DROP_ITEMS;
	public static double RATE_DROP_ITEMS_BY_RAID;
	public static double RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	
	public static double RATE_QUEST_DROP;
	public static double RATE_QUEST_REWARD;
	public static double RATE_QUEST_REWARD_XP;
	public static double RATE_QUEST_REWARD_SP;
	public static double RATE_QUEST_REWARD_ADENA;
	
	public static double RATE_KARMA_EXP_LOST;
	public static double RATE_SIEGE_GUARDS_PRICE;
	
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	
	public static double PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static double SINEATER_XP_RATE;
	
	public static double RATE_DROP_COMMON_HERBS;
	public static double RATE_DROP_HP_HERBS;
	public static double RATE_DROP_MP_HERBS;
	public static double RATE_DROP_SPECIAL_HERBS;
	
	/** Allow types */
	public static boolean ALLOW_FREIGHT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_BOAT;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_MANOR;
	public static boolean ENABLE_FALLING_DAMAGE;
	
	/** Debug & Dev */
	public static boolean ALT_DEV_NO_SPAWNS;
	public static boolean DEBUG;
	public static boolean DEVELOPER;
	public static boolean PACKET_HANDLER_DEBUG;
	
	/** Deadlock Detector */
	public static boolean DEADLOCK_DETECTOR;
	public static int DEADLOCK_CHECK_INTERVAL;
	public static boolean RESTART_ON_DEADLOCK;
	
	/** Logs */
	public static boolean LOG_CHAT;
	public static boolean LOG_ITEMS;
	public static boolean GMAUDIT;
	
	/** Community Board */
	public static boolean ENABLE_COMMUNITY_BOARD;
	public static String BBS_DEFAULT;
	
	/** Flood Protectors */
	public static int ROLL_DICE_TIME;
	public static int HERO_VOICE_TIME;
	public static int SUBCLASS_TIME;
	public static int DROP_ITEM_TIME;
	public static int SERVER_BYPASS_TIME;
	public static int MULTISELL_TIME;
	public static int MANUFACTURE_TIME;
	public static int MANOR_TIME;
	public static int SENDMAIL_TIME;
	public static int CHARACTER_SELECT_TIME;
	public static int GLOBAL_CHAT_TIME;
	public static int TRADE_CHAT_TIME;
	public static int SOCIAL_TIME;
	
	/** ThreadPool */
	public static int SCHEDULED_THREAD_POOL_COUNT;
	public static int THREADS_PER_SCHEDULED_THREAD_POOL;
	public static int INSTANT_THREAD_POOL_COUNT;
	public static int THREADS_PER_INSTANT_THREAD_POOL;
	
	/** Misc */
	public static boolean L2WALKER_PROTECTION;
	public static boolean SERVER_NEWS;
	public static int ZONE_TOWN;
	public static boolean DISABLE_TUTORIAL;
	
	// --------------------------------------------------
	// Those "hidden" settings haven't configs to avoid admins to fuck their server
	// You still can experiment changing values here. But don't say I didn't warn you.
	// --------------------------------------------------
	
	/** Reserve Host on LoginServerThread */
	public static boolean RESERVE_HOST_ON_LOGIN = false; // default false
	
	/** MMO settings */
	public static int MMO_SELECTOR_SLEEP_TIME = 20; // default 20
	public static int MMO_MAX_SEND_PER_PASS = 80; // default 80
	public static int MMO_MAX_READ_PER_PASS = 80; // default 80
	public static int MMO_HELPER_BUFFER_COUNT = 20; // default 20
	
	/** Client Packets Queue settings */
	public static int CLIENT_PACKET_QUEUE_SIZE = 14; // default MMO_MAX_READ_PER_PASS + 2
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = 13; // default MMO_MAX_READ_PER_PASS + 1
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = 160; // default 160
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = 5; // default 5
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = 80; // default 80
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = 2; // default 2
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = 1; // default 1
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = 1; // default 1
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = 5; // default 5
	
	// --------------------------------------------------
	
	/**
	 * Initialize {@link ExProperties} from specified configuration file.
	 * @param filename : File name to be loaded.
	 * @return ExProperties : Initialized {@link ExProperties}.
	 */
	public static final ExProperties initProperties(String filename)
	{
		final ExProperties result = new ExProperties();
		
		try
		{
			result.load(new File(filename));
		}
		catch (IOException e)
		{
			_log.warning("Config: Error loading \"" + filename + "\" config.");
		}
		
		return result;
	}
	
	/**
	 * itemId1,itemNumber1;itemId2,itemNumber2... to the int[n][2] = [itemId1][itemNumber1],[itemId2][itemNumber2]...
	 * @param line
	 * @return an array consisting of parsed items.
	 */
	private static final int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
			return null;
		
		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				_log.warning("Config: Error parsing entry -> \"" + valueSplit[0] + "\", should be itemId,itemNumber");
				return null;
			}
			
			result[i] = new int[2];
			try
			{
				result[i][0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warning("Config: Error parsing item ID -> \"" + valueSplit[0] + "\"");
				return null;
			}
			
			try
			{
				result[i][1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warning("Config: Error parsing item amount -> \"" + valueSplit[1] + "\"");
				return null;
			}
			i++;
		}
		return result;
	}
	
	/**
	 * Loads clan and clan hall settings.
	 */
	private static final void loadClans()
	{
		final ExProperties clans = initProperties(CLANS_FILE);
		

		ALT_CLAN_JOIN_DAYS = clans.getProperty("DaysBeforeJoinAClan", 5);
		ALT_CLAN_CREATE_DAYS = clans.getProperty("DaysBeforeCreateAClan", 10);
		ALT_MAX_NUM_OF_CLANS_IN_ALLY = clans.getProperty("AltMaxNumOfClansInAlly", 3);
		ALT_CLAN_MEMBERS_FOR_WAR = clans.getProperty("AltClanMembersForWar", 15);
		ALT_CLAN_WAR_PENALTY_WHEN_ENDED = clans.getProperty("AltClanWarPenaltyWhenEnded", 5);
		ALT_CLAN_DISSOLVE_DAYS = clans.getProperty("DaysToPassToDissolveAClan", 7);
		ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = clans.getProperty("DaysBeforeJoinAllyWhenLeaved", 1);
		ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = clans.getProperty("DaysBeforeJoinAllyWhenDismissed", 1);
		ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = clans.getProperty("DaysBeforeAcceptNewClanWhenDismissed", 1);
		ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = clans.getProperty("DaysBeforeCreateNewAllyWhenDissolved", 10);
		ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = clans.getProperty("AltMembersCanWithdrawFromClanWH", false);
		
		ALT_MANOR_REFRESH_TIME = clans.getProperty("AltManorRefreshTime", 20);
		ALT_MANOR_REFRESH_MIN = clans.getProperty("AltManorRefreshMin", 0);
		ALT_MANOR_APPROVE_TIME = clans.getProperty("AltManorApproveTime", 6);
		ALT_MANOR_APPROVE_MIN = clans.getProperty("AltManorApproveMin", 0);
		ALT_MANOR_MAINTENANCE_MIN = clans.getProperty("AltManorMaintenanceMin", 6);
		ALT_MANOR_SAVE_PERIOD_RATE = clans.getProperty("AltManorSavePeriodRate", 2) * 3600000;
		
		CH_TELE_FEE_RATIO = clans.getProperty("ClanHallTeleportFunctionFeeRatio", 86400000);
		CH_TELE1_FEE = clans.getProperty("ClanHallTeleportFunctionFeeLvl1", 7000);
		CH_TELE2_FEE = clans.getProperty("ClanHallTeleportFunctionFeeLvl2", 14000);
		CH_SUPPORT_FEE_RATIO = clans.getProperty("ClanHallSupportFunctionFeeRatio", 86400000);
		CH_SUPPORT1_FEE = clans.getProperty("ClanHallSupportFeeLvl1", 17500);
		CH_SUPPORT2_FEE = clans.getProperty("ClanHallSupportFeeLvl2", 35000);
		CH_SUPPORT3_FEE = clans.getProperty("ClanHallSupportFeeLvl3", 49000);
		CH_SUPPORT4_FEE = clans.getProperty("ClanHallSupportFeeLvl4", 77000);
		CH_SUPPORT5_FEE = clans.getProperty("ClanHallSupportFeeLvl5", 147000);
		CH_SUPPORT6_FEE = clans.getProperty("ClanHallSupportFeeLvl6", 252000);
		CH_SUPPORT7_FEE = clans.getProperty("ClanHallSupportFeeLvl7", 259000);
		CH_SUPPORT8_FEE = clans.getProperty("ClanHallSupportFeeLvl8", 364000);
		CH_MPREG_FEE_RATIO = clans.getProperty("ClanHallMpRegenerationFunctionFeeRatio", 86400000);
		CH_MPREG1_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl1", 14000);
		CH_MPREG2_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl2", 26250);
		CH_MPREG3_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl3", 45500);
		CH_MPREG4_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl4", 96250);
		CH_MPREG5_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl5", 140000);
		CH_HPREG_FEE_RATIO = clans.getProperty("ClanHallHpRegenerationFunctionFeeRatio", 86400000);
		CH_HPREG1_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl1", 4900);
		CH_HPREG2_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl2", 5600);
		CH_HPREG3_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl3", 7000);
		CH_HPREG4_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl4", 8166);
		CH_HPREG5_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl5", 10500);
		CH_HPREG6_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl6", 12250);
		CH_HPREG7_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl7", 14000);
		CH_HPREG8_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl8", 15750);
		CH_HPREG9_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl9", 17500);
		CH_HPREG10_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl10", 22750);
		CH_HPREG11_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl11", 26250);
		CH_HPREG12_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl12", 29750);
		CH_HPREG13_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl13", 36166);
		CH_EXPREG_FEE_RATIO = clans.getProperty("ClanHallExpRegenerationFunctionFeeRatio", 86400000);
		CH_EXPREG1_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl1", 21000);
		CH_EXPREG2_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl2", 42000);
		CH_EXPREG3_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl3", 63000);
		CH_EXPREG4_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl4", 105000);
		CH_EXPREG5_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl5", 147000);
		CH_EXPREG6_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl6", 163331);
		CH_EXPREG7_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl7", 210000);
		CH_ITEM_FEE_RATIO = clans.getProperty("ClanHallItemCreationFunctionFeeRatio", 86400000);
		CH_ITEM1_FEE = clans.getProperty("ClanHallItemCreationFunctionFeeLvl1", 210000);
		CH_ITEM2_FEE = clans.getProperty("ClanHallItemCreationFunctionFeeLvl2", 490000);
		CH_ITEM3_FEE = clans.getProperty("ClanHallItemCreationFunctionFeeLvl3", 980000);
		CH_CURTAIN_FEE_RATIO = clans.getProperty("ClanHallCurtainFunctionFeeRatio", 86400000);
		CH_CURTAIN1_FEE = clans.getProperty("ClanHallCurtainFunctionFeeLvl1", 2002);
		CH_CURTAIN2_FEE = clans.getProperty("ClanHallCurtainFunctionFeeLvl2", 2625);
		CH_FRONT_FEE_RATIO = clans.getProperty("ClanHallFrontPlatformFunctionFeeRatio", 86400000);
		CH_FRONT1_FEE = clans.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", 3031);
		CH_FRONT2_FEE = clans.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", 9331);
	}
	
	/**
	 * Loads event settings.<br>
	 * Such as olympiad, seven signs festival, four sepulchures, dimensional rift, weddings, lottery, fishing championship.
	 */
	private static final void loadEvents()
	{
		final ExProperties events = initProperties(EVENTS_FILE);
		ALT_OLY_START_TIME = events.getProperty("AltOlyStartTime", 18);
		ALT_OLY_MIN = events.getProperty("AltOlyMin", 0);
		ALT_OLY_CPERIOD = events.getProperty("AltOlyCPeriod", 21600000);
		ALT_OLY_BATTLE = events.getProperty("AltOlyBattle", 180000);
		ALT_OLY_WPERIOD = events.getProperty("AltOlyWPeriod", 604800000);
		ALT_OLY_VPERIOD = events.getProperty("AltOlyVPeriod", 86400000);
		ALT_OLY_WAIT_TIME = events.getProperty("AltOlyWaitTime", 30);
		ALT_OLY_WAIT_BATTLE = events.getProperty("AltOlyWaitBattle", 60);
		ALT_OLY_WAIT_END = events.getProperty("AltOlyWaitEnd", 40);
		ALT_OLY_START_POINTS = events.getProperty("AltOlyStartPoints", 18);
		ALT_OLY_WEEKLY_POINTS = events.getProperty("AltOlyWeeklyPoints", 3);
		ALT_OLY_MIN_MATCHES = events.getProperty("AltOlyMinMatchesToBeClassed", 5);
		ALT_OLY_CLASSED = events.getProperty("AltOlyClassedParticipants", 5);
		ALT_OLY_NONCLASSED = events.getProperty("AltOlyNonClassedParticipants", 9);
		ALT_OLY_CLASSED_REWARD = parseItemsList(events.getProperty("AltOlyClassedReward", "6651,50"));
		ALT_OLY_NONCLASSED_REWARD = parseItemsList(events.getProperty("AltOlyNonClassedReward", "6651,30"));
		ALT_OLY_GP_PER_POINT = events.getProperty("AltOlyGPPerPoint", 1000);
		ALT_OLY_HERO_POINTS = events.getProperty("AltOlyHeroPoints", 300);
		ALT_OLY_RANK1_POINTS = events.getProperty("AltOlyRank1Points", 100);
		ALT_OLY_RANK2_POINTS = events.getProperty("AltOlyRank2Points", 75);
		ALT_OLY_RANK3_POINTS = events.getProperty("AltOlyRank3Points", 55);
		ALT_OLY_RANK4_POINTS = events.getProperty("AltOlyRank4Points", 40);
		ALT_OLY_RANK5_POINTS = events.getProperty("AltOlyRank5Points", 30);
		ALT_OLY_MAX_POINTS = events.getProperty("AltOlyMaxPoints", 10);
		ALT_OLY_DIVIDER_CLASSED = events.getProperty("AltOlyDividerClassed", 3);
		ALT_OLY_DIVIDER_NON_CLASSED = events.getProperty("AltOlyDividerNonClassed", 3);
		ALT_OLY_ANNOUNCE_GAMES = events.getProperty("AltOlyAnnounceGames", true);

		
		ALT_GAME_CASTLE_DAWN = events.getProperty("AltCastleForDawn", true);
		ALT_GAME_CASTLE_DUSK = events.getProperty("AltCastleForDusk", true);
		ALT_FESTIVAL_MIN_PLAYER = MathUtil.limit(events.getProperty("AltFestivalMinPlayer", 5), 2, 9);
		ALT_MAXIMUM_PLAYER_CONTRIB = events.getProperty("AltMaxPlayerContrib", 1000000);
		ALT_FESTIVAL_MANAGER_START = events.getProperty("AltFestivalManagerStart", 120000);
		ALT_FESTIVAL_LENGTH = events.getProperty("AltFestivalLength", 1080000);
		ALT_FESTIVAL_CYCLE_LENGTH = events.getProperty("AltFestivalCycleLength", 2280000);
		ALT_FESTIVAL_FIRST_SPAWN = events.getProperty("AltFestivalFirstSpawn", 120000);
		ALT_FESTIVAL_FIRST_SWARM = events.getProperty("AltFestivalFirstSwarm", 300000);
		ALT_FESTIVAL_SECOND_SPAWN = events.getProperty("AltFestivalSecondSpawn", 540000);
		ALT_FESTIVAL_SECOND_SWARM = events.getProperty("AltFestivalSecondSwarm", 720000);
		ALT_FESTIVAL_CHEST_SPAWN = events.getProperty("AltFestivalChestSpawn", 900000);
		
		FS_TIME_ATTACK = events.getProperty("TimeOfAttack", 50);
		FS_TIME_ENTRY = events.getProperty("TimeOfEntry", 3);
		FS_TIME_WARMUP = events.getProperty("TimeOfWarmUp", 2);
		FS_PARTY_MEMBER_COUNT = MathUtil.limit(events.getProperty("NumberOfNecessaryPartyMembers", 4), 2, 9);
		
		RIFT_MIN_PARTY_SIZE = events.getProperty("RiftMinPartySize", 2);
		RIFT_MAX_JUMPS = events.getProperty("MaxRiftJumps", 4);
		RIFT_SPAWN_DELAY = events.getProperty("RiftSpawnDelay", 10000);
		RIFT_AUTO_JUMPS_TIME_MIN = events.getProperty("AutoJumpsDelayMin", 480);
		RIFT_AUTO_JUMPS_TIME_MAX = events.getProperty("AutoJumpsDelayMax", 600);
		RIFT_ENTER_COST_RECRUIT = events.getProperty("RecruitCost", 18);
		RIFT_ENTER_COST_SOLDIER = events.getProperty("SoldierCost", 21);
		RIFT_ENTER_COST_OFFICER = events.getProperty("OfficerCost", 24);
		RIFT_ENTER_COST_CAPTAIN = events.getProperty("CaptainCost", 27);
		RIFT_ENTER_COST_COMMANDER = events.getProperty("CommanderCost", 30);
		RIFT_ENTER_COST_HERO = events.getProperty("HeroCost", 33);
		RIFT_BOSS_ROOM_TIME_MUTIPLY = events.getProperty("BossRoomTimeMultiply", 1.);
		
		ALLOW_WEDDING = events.getProperty("AllowWedding", false);
		WEDDING_PRICE = events.getProperty("WeddingPrice", 1000000);
		WEDDING_SAMESEX = events.getProperty("WeddingAllowSameSex", false);
		WEDDING_FORMALWEAR = events.getProperty("WeddingFormalWear", true);
		
		ALT_LOTTERY_PRIZE = events.getProperty("AltLotteryPrize", 50000);
		ALT_LOTTERY_TICKET_PRICE = events.getProperty("AltLotteryTicketPrice", 2000);
		ALT_LOTTERY_5_NUMBER_RATE = events.getProperty("AltLottery5NumberRate", 0.6);
		ALT_LOTTERY_4_NUMBER_RATE = events.getProperty("AltLottery4NumberRate", 0.2);
		ALT_LOTTERY_3_NUMBER_RATE = events.getProperty("AltLottery3NumberRate", 0.2);
		ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = events.getProperty("AltLottery2and1NumberPrize", 200);
		
		ALT_FISH_CHAMPIONSHIP_ENABLED = events.getProperty("AltFishChampionshipEnabled", true);
		ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = events.getProperty("AltFishChampionshipRewardItemId", 57);
		ALT_FISH_CHAMPIONSHIP_REWARD_1 = events.getProperty("AltFishChampionshipReward1", 800000);
		ALT_FISH_CHAMPIONSHIP_REWARD_2 = events.getProperty("AltFishChampionshipReward2", 500000);
		ALT_FISH_CHAMPIONSHIP_REWARD_3 = events.getProperty("AltFishChampionshipReward3", 300000);
		ALT_FISH_CHAMPIONSHIP_REWARD_4 = events.getProperty("AltFishChampionshipReward4", 200000);
		ALT_FISH_CHAMPIONSHIP_REWARD_5 = events.getProperty("AltFishChampionshipReward5", 100000);
	}
	
	/**
	 * Loads geoengine settings.
	 */
	private static final void loadGeoengine()
	{
		final ExProperties geoengine = initProperties(GEOENGINE_FILE);
		GEODATA_PATH = geoengine.getProperty("GeoDataPath", "./data/geodata/");
		COORD_SYNCHRONIZE = geoengine.getProperty("CoordSynchronize", -1);
		
		PART_OF_CHARACTER_HEIGHT = geoengine.getProperty("PartOfCharacterHeight", 75);
		MAX_OBSTACLE_HEIGHT = geoengine.getProperty("MaxObstacleHeight", 32);
		
		PATHFIND_BUFFERS = geoengine.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
		BASE_WEIGHT = geoengine.getProperty("BaseWeight", 10);
		DIAGONAL_WEIGHT = geoengine.getProperty("DiagonalWeight", 14);
		OBSTACLE_MULTIPLIER = geoengine.getProperty("ObstacleMultiplier", 10);
		HEURISTIC_WEIGHT = geoengine.getProperty("HeuristicWeight", 20);
		MAX_ITERATIONS = geoengine.getProperty("MaxIterations", 3500);
		DEBUG_PATH = geoengine.getProperty("DebugPath", false);
		DEBUG_GEO_NODE = geoengine.getProperty("DebugGeoNode", false);
	}
	
	/**
	 * Loads hex ID settings.
	 */
	private static final void loadHexID()
	{
		final ExProperties hexid = initProperties(HEXID_FILE);
		SERVER_ID = Integer.parseInt(hexid.getProperty("ServerID"));
		HEX_ID = new BigInteger(hexid.getProperty("HexID"), 16).toByteArray();
	}
	
	/**
	 * Saves hex ID file.
	 * @param serverId : The ID of server.
	 * @param hexId : The hex ID of server.
	 */
	public static final void saveHexid(int serverId, String hexId)
	{
		saveHexid(serverId, hexId, HEXID_FILE);
	}
	
	/**
	 * Saves hexID file.
	 * @param serverId : The ID of server.
	 * @param hexId : The hexID of server.
	 * @param filename : The file name.
	 */
	public static final void saveHexid(int serverId, String hexId, String filename)
	{
		try
		{
			Properties hexSetting = new Properties();
			File file = new File(filename);
			file.createNewFile();
			
			OutputStream out = new FileOutputStream(file);
			hexSetting.setProperty("ServerID", String.valueOf(serverId));
			hexSetting.setProperty("HexID", hexId);
			hexSetting.store(out, "the hexID to auth into login");
			out.close();
		}
		catch (Exception e)
		{
			_log.warning("Config: Failed to save hex ID to \"" + filename + "\" file.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads NPC settings.<br>
	 * Such as champion monsters, NPC buffer, class master, wyvern, raid bosses and grand bosses, AI.
	 */
	private static final void loadNpcs()
	{
		final ExProperties npcs = initProperties(NPCS_FILE);
		
	      
		CHAMPION_FREQUENCY = npcs.getProperty("ChampionFrequency", 0);
		CHAMP_MIN_LVL = npcs.getProperty("ChampionMinLevel", 20);
		CHAMP_MAX_LVL = npcs.getProperty("ChampionMaxLevel", 70);
		CHAMPION_HP = npcs.getProperty("ChampionHp", 8);
		CHAMPION_HP_REGEN = npcs.getProperty("ChampionHpRegen", 1.);
		CHAMPION_REWARDS = npcs.getProperty("ChampionRewards", 8);
		CHAMPION_ADENAS_REWARDS = npcs.getProperty("ChampionAdenasRewards", 1);
		CHAMPION_ATK = npcs.getProperty("ChampionAtk", 1.);
		CHAMPION_SPD_ATK = npcs.getProperty("ChampionSpdAtk", 1.);
		CHAMPION_REWARD = npcs.getProperty("ChampionRewardItem", 0);
		CHAMPION_REWARD_ID = npcs.getProperty("ChampionRewardItemID", 6393);
		CHAMPION_REWARD_QTY = npcs.getProperty("ChampionRewardItemQty", 1);
		
		BUFFER_MAX_SCHEMES = npcs.getProperty("BufferMaxSchemesPerChar", 4);
		BUFFER_STATIC_BUFF_COST = npcs.getProperty("BufferStaticCostPerBuff", -1);
		
		ALLOW_CLASS_MASTERS = npcs.getProperty("AllowClassMasters", false);
		ALLOW_ENTIRE_TREE = npcs.getProperty("AllowEntireTree", false);
		if (ALLOW_CLASS_MASTERS)
			CLASS_MASTER_SETTINGS = new ClassMasterSettings(npcs.getProperty("ConfigClassMaster"));
		
		ANNOUNCE_MAMMON_SPAWN = npcs.getProperty("AnnounceMammonSpawn", true);
		ALT_MOB_AGRO_IN_PEACEZONE = npcs.getProperty("AltMobAgroInPeaceZone", true);
		SHOW_NPC_LVL = npcs.getProperty("ShowNpcLevel", false);
		SHOW_NPC_CREST = npcs.getProperty("ShowNpcCrest", false);
		SHOW_SUMMON_CREST = npcs.getProperty("ShowSummonCrest", false);
		
		WYVERN_ALLOW_UPGRADER = npcs.getProperty("AllowWyvernUpgrader", true);
		WYVERN_REQUIRED_LEVEL = npcs.getProperty("RequiredStriderLevel", 55);
		WYVERN_REQUIRED_CRYSTALS = npcs.getProperty("RequiredCrystalsNumber", 10);
		
		RAID_HP_REGEN_MULTIPLIER = npcs.getProperty("RaidHpRegenMultiplier", 1.);
		RAID_MP_REGEN_MULTIPLIER = npcs.getProperty("RaidMpRegenMultiplier", 1.);
		RAID_DEFENCE_MULTIPLIER = npcs.getProperty("RaidDefenceMultiplier", 1.);
		RAID_MINION_RESPAWN_TIMER = npcs.getProperty("RaidMinionRespawnTime", 300000);
		
		RAID_DISABLE_CURSE = npcs.getProperty("DisableRaidCurse", false);
		RAID_CHAOS_TIME = npcs.getProperty("RaidChaosTime", 30);
		GRAND_CHAOS_TIME = npcs.getProperty("GrandChaosTime", 30);
		MINION_CHAOS_TIME = npcs.getProperty("MinionChaosTime", 30);
		
		SPAWN_INTERVAL_AQ = npcs.getProperty("AntQueenSpawnInterval", 36);
		RANDOM_SPAWN_TIME_AQ = npcs.getProperty("AntQueenRandomSpawn", 17);
		
		SPAWN_INTERVAL_ANTHARAS = npcs.getProperty("AntharasSpawnInterval", 264);
		RANDOM_SPAWN_TIME_ANTHARAS = npcs.getProperty("AntharasRandomSpawn", 72);
		WAIT_TIME_ANTHARAS = npcs.getProperty("AntharasWaitTime", 30) * 60000;
		
		SPAWN_INTERVAL_BAIUM = npcs.getProperty("BaiumSpawnInterval", 168);
		RANDOM_SPAWN_TIME_BAIUM = npcs.getProperty("BaiumRandomSpawn", 48);
		
		SPAWN_INTERVAL_CORE = npcs.getProperty("CoreSpawnInterval", 60);
		RANDOM_SPAWN_TIME_CORE = npcs.getProperty("CoreRandomSpawn", 23);
		
		SPAWN_INTERVAL_FRINTEZZA = npcs.getProperty("FrintezzaSpawnInterval", 48);
		RANDOM_SPAWN_TIME_FRINTEZZA = npcs.getProperty("FrintezzaRandomSpawn", 8);
		WAIT_TIME_FRINTEZZA = npcs.getProperty("FrintezzaWaitTime", 1) * 60000;
		
		SPAWN_INTERVAL_ORFEN = npcs.getProperty("OrfenSpawnInterval", 48);
		RANDOM_SPAWN_TIME_ORFEN = npcs.getProperty("OrfenRandomSpawn", 20);
		
		SPAWN_INTERVAL_SAILREN = npcs.getProperty("SailrenSpawnInterval", 36);
		RANDOM_SPAWN_TIME_SAILREN = npcs.getProperty("SailrenRandomSpawn", 24);
		WAIT_TIME_SAILREN = npcs.getProperty("SailrenWaitTime", 5) * 60000;
		
		SPAWN_INTERVAL_VALAKAS = npcs.getProperty("ValakasSpawnInterval", 264);
		RANDOM_SPAWN_TIME_VALAKAS = npcs.getProperty("ValakasRandomSpawn", 72);
		WAIT_TIME_VALAKAS = npcs.getProperty("ValakasWaitTime", 30) * 60000;
		
	    SPAWN_ZAKEN_HOUR_OF_DAY = npcs.getProperty("ZakenHourOfDay", 0);
	    SPAWN_ZAKEN_MINUTE = npcs.getProperty("ZakenMinuteOfDay", 0);
	    WAIT_TIME_ZAKEN = npcs.getProperty("ZakenWaitTime", 30) * 60000;
		
		GUARD_ATTACK_AGGRO_MOB = npcs.getProperty("GuardAttackAggroMob", false);
		MAX_DRIFT_RANGE = npcs.getProperty("MaxDriftRange", 300);
		MIN_NPC_ANIMATION = npcs.getProperty("MinNPCAnimation", 20);
		MAX_NPC_ANIMATION = npcs.getProperty("MaxNPCAnimation", 40);
		MIN_MONSTER_ANIMATION = npcs.getProperty("MinMonsterAnimation", 10);
		MAX_MONSTER_ANIMATION = npcs.getProperty("MaxMonsterAnimation", 40);
	}
	
	/**
	 * Loads player settings.<br>
	 * Such as stats, inventory/warehouse, enchant, augmentation, karma, party, admin, petition, skill learn.
	 */
	private static final void loadPlayers()
	{
		final ExProperties players = initProperties(PLAYERS_FILE);
		EFFECT_CANCELING = players.getProperty("CancelLesserEffect", true);
		HP_REGEN_MULTIPLIER = players.getProperty("HpRegenMultiplier", 1.);
		MP_REGEN_MULTIPLIER = players.getProperty("MpRegenMultiplier", 1.);
		CP_REGEN_MULTIPLIER = players.getProperty("CpRegenMultiplier", 1.);
		PLAYER_SPAWN_PROTECTION = players.getProperty("PlayerSpawnProtection", 0);
		PLAYER_FAKEDEATH_UP_PROTECTION = players.getProperty("PlayerFakeDeathUpProtection", 0);
		RESPAWN_RESTORE_HP = players.getProperty("RespawnRestoreHP", 0.7);
		MAX_PVTSTORE_SLOTS_DWARF = players.getProperty("MaxPvtStoreSlotsDwarf", 5);
		MAX_PVTSTORE_SLOTS_OTHER = players.getProperty("MaxPvtStoreSlotsOther", 4);
		DEEPBLUE_DROP_RULES = players.getProperty("UseDeepBlueDropRules", true);
		ALT_GAME_DELEVEL = players.getProperty("Delevel", true);
		DEATH_PENALTY_CHANCE = players.getProperty("DeathPenaltyChance", 20);
		
		INVENTORY_MAXIMUM_NO_DWARF = players.getProperty("MaximumSlotsForNoDwarf", 80);
		INVENTORY_MAXIMUM_DWARF = players.getProperty("MaximumSlotsForDwarf", 100);
		INVENTORY_MAXIMUM_QUEST_ITEMS = players.getProperty("MaximumSlotsForQuestItems", 100);
		INVENTORY_MAXIMUM_PET = players.getProperty("MaximumSlotsForPet", 12);
		MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, INVENTORY_MAXIMUM_DWARF);
		ALT_WEIGHT_LIMIT = players.getProperty("AltWeightLimit", 1);
		WAREHOUSE_SLOTS_NO_DWARF = players.getProperty("MaximumWarehouseSlotsForNoDwarf", 100);
		WAREHOUSE_SLOTS_DWARF = players.getProperty("MaximumWarehouseSlotsForDwarf", 120);
		WAREHOUSE_SLOTS_CLAN = players.getProperty("MaximumWarehouseSlotsForClan", 150);
		FREIGHT_SLOTS = players.getProperty("MaximumFreightSlots", 20);
		ALT_GAME_FREIGHTS = players.getProperty("AltGameFreights", false);
		ALT_GAME_FREIGHT_PRICE = players.getProperty("AltGameFreightPrice", 1000);
		

		
		AUGMENTATION_NG_SKILL_CHANCE = players.getProperty("AugmentationNGSkillChance", 15);
		AUGMENTATION_NG_GLOW_CHANCE = players.getProperty("AugmentationNGGlowChance", 0);
		AUGMENTATION_MID_SKILL_CHANCE = players.getProperty("AugmentationMidSkillChance", 30);
		AUGMENTATION_MID_GLOW_CHANCE = players.getProperty("AugmentationMidGlowChance", 40);
		AUGMENTATION_HIGH_SKILL_CHANCE = players.getProperty("AugmentationHighSkillChance", 45);
		AUGMENTATION_HIGH_GLOW_CHANCE = players.getProperty("AugmentationHighGlowChance", 70);
		AUGMENTATION_TOP_SKILL_CHANCE = players.getProperty("AugmentationTopSkillChance", 60);
		AUGMENTATION_TOP_GLOW_CHANCE = players.getProperty("AugmentationTopGlowChance", 100);
		AUGMENTATION_BASESTAT_CHANCE = players.getProperty("AugmentationBaseStatChance", 1);
		
		KARMA_PLAYER_CAN_BE_KILLED_IN_PZ = players.getProperty("KarmaPlayerCanBeKilledInPeaceZone", false);
		KARMA_PLAYER_CAN_SHOP = players.getProperty("KarmaPlayerCanShop", false);
		KARMA_PLAYER_CAN_USE_GK = players.getProperty("KarmaPlayerCanUseGK", false);
		KARMA_PLAYER_CAN_TELEPORT = players.getProperty("KarmaPlayerCanTeleport", true);
		KARMA_PLAYER_CAN_TRADE = players.getProperty("KarmaPlayerCanTrade", true);
		KARMA_PLAYER_CAN_USE_WH = players.getProperty("KarmaPlayerCanUseWareHouse", true);
		KARMA_DROP_GM = players.getProperty("CanGMDropEquipment", false);
		KARMA_AWARD_PK_KILL = players.getProperty("AwardPKKillPVPPoint", true);
		KARMA_PK_LIMIT = players.getProperty("MinimumPKRequiredToDrop", 5);
		KARMA_NONDROPPABLE_PET_ITEMS = players.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
		KARMA_NONDROPPABLE_ITEMS = players.getProperty("ListOfNonDroppableItemsForPK", "1147,425,1146,461,10,2368,7,6,2370,2369");
		
		String[] array = KARMA_NONDROPPABLE_PET_ITEMS.split(",");
		KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[array.length];
		
		for (int i = 0; i < array.length; i++)
			KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(array[i]);
		
		array = KARMA_NONDROPPABLE_ITEMS.split(",");
		KARMA_LIST_NONDROPPABLE_ITEMS = new int[array.length];
		
		for (int i = 0; i < array.length; i++)
			KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(array[i]);
		
		// sorting so binarySearch can be used later
		Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
		Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);
		
		PVP_NORMAL_TIME = players.getProperty("PvPVsNormalTime", 15000);
		PVP_PVP_TIME = players.getProperty("PvPVsPvPTime", 30000);
		
		PARTY_XP_CUTOFF_METHOD = players.getProperty("PartyXpCutoffMethod", "level");
		PARTY_XP_CUTOFF_PERCENT = players.getProperty("PartyXpCutoffPercent", 3.);
		PARTY_XP_CUTOFF_LEVEL = players.getProperty("PartyXpCutoffLevel", 20);
		PARTY_RANGE = players.getProperty("PartyRange", 1500);
	
		
		PETITIONING_ALLOWED = players.getProperty("PetitioningAllowed", true);
		MAX_PETITIONS_PER_PLAYER = players.getProperty("MaxPetitionsPerPlayer", 5);
		MAX_PETITIONS_PENDING = players.getProperty("MaxPetitionsPending", 25);
		
		IS_CRAFTING_ENABLED = players.getProperty("CraftingEnabled", true);
		DWARF_RECIPE_LIMIT = players.getProperty("DwarfRecipeLimit", 50);
		COMMON_RECIPE_LIMIT = players.getProperty("CommonRecipeLimit", 50);
		ALT_BLACKSMITH_USE_RECIPES = players.getProperty("AltBlacksmithUseRecipes", true);
		
		AUTO_LEARN_SKILLS = players.getProperty("AutoLearnSkills", false);
		MAGIC_FAILURES = players.getProperty("MagicFailures", true);
		PERFECT_SHIELD_BLOCK_RATE = players.getProperty("PerfectShieldBlockRate", 5);
		LIFE_CRYSTAL_NEEDED = players.getProperty("LifeCrystalNeeded", true);
		SP_BOOK_NEEDED = players.getProperty("SpBookNeeded", true);
		ES_SP_BOOK_NEEDED = players.getProperty("EnchantSkillSpBookNeeded", true);
		DIVINE_SP_BOOK_NEEDED = players.getProperty("DivineInspirationSpBookNeeded", true);
		SUBCLASS_WITHOUT_QUESTS = players.getProperty("SubClassWithoutQuests", false);
		
		MAX_BUFFS_AMOUNT = players.getProperty("MaxBuffsAmount", 20);
		STORE_SKILL_COOLTIME = players.getProperty("StoreSkillCooltime", true);
	}
	
	/**
	 * Loads siege settings.
	 */
	private static final void loadSieges()
	{
		final ExProperties sieges = initProperties(Config.SIEGE_FILE);
		
		SIEGE_LENGTH = sieges.getProperty("SiegeLength", 120);
		MINIMUM_CLAN_LEVEL = sieges.getProperty("SiegeClanMinLevel", 4);
		MAX_ATTACKERS_NUMBER = sieges.getProperty("AttackerMaxClans", 10);
		MAX_DEFENDERS_NUMBER = sieges.getProperty("DefenderMaxClans", 10);
		ATTACKERS_RESPAWN_DELAY = sieges.getProperty("AttackerRespawn", 10000);

		
	}
	
	/**
	 * Loads pvp settings.
	 */
	private static final void loadPvp()
	{
		final ExProperties pvp = initProperties(Config.PVP_FILE);
		
	    BS_PVPZONE = Boolean.parseBoolean(pvp.getProperty("DisableBsPvPZone", "False"));
	    WYVERN_PVPZONE = Boolean.parseBoolean(pvp.getProperty("WyverbPvPZone", "False"));
	    PARTY_PVPZONE = Boolean.parseBoolean(pvp.getProperty("DisablePartyPvPZone", "False"));
	    CUSTOM_TELEGIRAN_ON_DIE = Boolean.parseBoolean(pvp.getProperty("CustomTeleportOnDie", "false"));
		
		ALLOW_PK_TITLE_COLOR_SYSTEM = Boolean.parseBoolean(pvp.getProperty("AllowPkTitleColorSystem", "false"));
		ALLOW_PVP_NAME_COLOR_SYSTEM = Boolean.parseBoolean(pvp.getProperty("AllowPvpNameColorSystem", "false"));
			
		String pvp_colors = pvp.getProperty("PkTitleColors", "100,FFFF00");
		String pvp_colors_splitted_1[] = pvp_colors.split(";");
		for (String s : pvp_colors_splitted_1)
		{
		String pvp_colors_splitted_2[] = s.split(",");
		PK_TITLE_COLORS.put(Integer.parseInt(pvp_colors_splitted_2[0]), Integer.decode("0x"+pvp_colors_splitted_2[1]));
		}
		
		String pvp_colors_name = pvp.getProperty("PvpNameColors", "100,FFFF00");
		String pvp_colors_splitted[] = pvp_colors_name.split(";");
		for (String i : pvp_colors_splitted)
		{
		String pvp_colors_splitted_3[] = i.split(",");
		PVP_NAME_COLORS.put(Integer.parseInt(pvp_colors_splitted_3[0]), Integer.decode("0x"+pvp_colors_splitted_3[1]));
		}
		
	    ALT_FIGHTER_TO_SUMMON = Float.parseFloat(pvp.getProperty("OlympiadFighterToSummon", "1.00"));
	    ALT_MAGE_TO_SUMMON = Float.parseFloat(pvp.getProperty("OlympiadMageToSummon", "1.00"));
		ENABLE_CLASS_DAMAGES = pvp.getProperty("EnableClassDamagesSettings", true);
		ENABLE_CLASS_DAMAGES_IN_OLY = pvp.getProperty("EnableClassDamagesSettingsInOly", true);
		ENABLE_CLASS_DAMAGES_LOGGER = pvp.getProperty("EnableClassDamagesLogger", true);
		
		SHOW_HP_PVP = pvp.getProperty("ShowHpPvP", false);
		
		STATUS_CMD = pvp.getProperty("Alow_cmd_status", false);
		INVENTORY_CMD = pvp.getProperty("Alow_cmd_inventory", false);
		SKILLS_CMD = pvp.getProperty("Alow_cmd_skills", false);
		
		ANNOUNCE_PK_PVP = pvp.getProperty("AnnouncePkPvP", false);
		ANNOUNCE_PK_PVP_NORMAL_MESSAGE = pvp.getProperty("AnnouncePkPvPNormalMessage", true);
		ANNOUNCE_PK_MSG = pvp.getProperty("AnnouncePkMsg", "Player $killer has slaughtered $target .");
		ANNOUNCE_PVP_MSG = pvp.getProperty("AnnouncePvpMsg", "Player $killer has defeated $target .");

		
	}
	
	
	/**
	 * Loads admin settings.
	 */
	private static final void loadAdmin()
	{
		final ExProperties adm = initProperties(Config.ADMIN_FILE);
		
		DEFAULT_ACCESS_LEVEL = adm.getProperty("DefaultAccessLevel", 0);
		GM_HERO_AURA = adm.getProperty("GMHeroAura", false);
		GM_STARTUP_INVULNERABLE = adm.getProperty("GMStartupInvulnerable", true);
		GM_STARTUP_INVISIBLE = adm.getProperty("GMStartupInvisible", true);
		GM_STARTUP_SILENCE = adm.getProperty("GMStartupSilence", true);
		GM_STARTUP_AUTO_LIST = adm.getProperty("GMStartupAutoList", true);
		
		FORBIDDEN_NAMES = adm.getProperty("For_bidden_Names", "").split(",");
		GM_NAMES = adm.getProperty("Gm_Names", "").split(",");
		TIME_ADMIN = adm.getProperty("Disconect_Falso_Admin", 2);	
	}
	
	/**
	 * Loads bot settings.
	 */
	private static final void loadBot()
	{
		final ExProperties bot = initProperties(Config.BOT_FILE);
		
		 BOTS_PREVENTION = bot.getProperty("EnableBotsPrevention", false);
		 KILLS_COUNTER = bot.getProperty("KillsCounter", 60);
		 KILLS_COUNTER_RANDOMIZATION = bot.getProperty("KillsCounterRandomization", 50);
		 VALIDATION_TIME = bot.getProperty("ValidationTime", 60);
		 PUNISHMENT = bot.getProperty("Punishment", 0);
		 PUNISHMENT_TIME = bot.getProperty("PunishmentTime", 60);

	}
	
	/**
	 * Loads phantom settings.
	 */
	private static final void loadPhantom()
	{
		final ExProperties Phanton = initProperties(Config.PHANTOM_FILE);
		
					ALLOW_PHANTOM_PLAYERS = Boolean.parseBoolean(Phanton.getProperty("AllowPhantom", "False"));
					
					PHANTOM_TITLE_CONFIG = Boolean.parseBoolean(Phanton.getProperty("FakeTitleFixo", "false"));
					
					PHANTOM_PLAYERS_SOULSHOT_ANIM = Boolean.parseBoolean(Phanton.getProperty("PhantomSoulshotAnimation", "True"));
					PHANTOM_PLAYERS_ARGUMENT_ANIM = Boolean.parseBoolean(Phanton.getProperty("PhantomArgumentAnimation", "True"));
					
					PHANTOM_TITLE_MSG = Phanton.getProperty("FakeTitle", "Lineage 2");
					PHANTOM_TITLE = new ArrayList<>();
					for (String type : PHANTOM_TITLE_MSG.split(","))
					{
						PHANTOM_TITLE.add(type);
					}
					PHANTOM_TITLE_MSG = null;
					
					String[] arrayOfString1 = Phanton.getProperty("FakeEnchant", "0,14").split(",");
					PHANTOM_PLAYERS_ENCHANT_MIN = Integer.parseInt(arrayOfString1[0]);
					PHANTOM_PLAYERS_ENCHANT_MAX = Integer.parseInt(arrayOfString1[1]);
					
					NAME_COLOR = Phanton.getProperty("NameColor", "FFFFFF");
					TITLE_COLOR = Phanton.getProperty("TitleColor", "FFFFFF");
					
					PHANTOM_NAME_CLOLORS = Phanton.getProperty("FakeNameColors", "FFFFFF");
					PHANTOM_PLAYERS_NAME_CLOLORS = new ArrayList<>();
					for (String type : PHANTOM_NAME_CLOLORS.split(","))
					{
						PHANTOM_PLAYERS_NAME_CLOLORS.add(type);
					}
					PHANTOM_NAME_CLOLORS = null;
					
					PHANTOM_TITLE_CLOLORS = Phanton.getProperty("FakeTitleColors", "FFFFFF");
					PHANTOM_PLAYERS_TITLE_CLOLORS = new ArrayList<>();
					for (String type : PHANTOM_TITLE_CLOLORS.split(","))
					{
						PHANTOM_PLAYERS_TITLE_CLOLORS.add(type);
					}
					PHANTOM_TITLE_CLOLORS = null;
					
					ALLOW_PHANTOM_FACE = Boolean.parseBoolean(Phanton.getProperty("PhantomFace", "True"));
					
					PHANTOM_FACE = Phanton.getProperty("PhantomFaceList", "");
					LIST_PHANTOM_FACE = new ArrayList<>();
					for (String itemId : PHANTOM_FACE.split(","))
					{
						LIST_PHANTOM_FACE.add(Integer.parseInt(itemId));
					}
					
					PHANTOM_PLAYERS_TITLE_MSG = Phanton.getProperty("FakeTitleList", "Lineage 2");
					PHANTOM_PLAYERS_TITLE = new ArrayList<>();
					for (String type : PHANTOM_PLAYERS_TITLE_MSG.split(","))
					{
						PHANTOM_PLAYERS_TITLE.add(type);
					}
					PHANTOM_PLAYERS_TITLE_MSG = null;
					
					PHANTOM_DELAY_FIRST = TimeUnit.MINUTES.toMillis(Integer.parseInt(Phanton.getProperty("FirstDelay", "5")));
					PHANTOM_DELAY_SPAWN_FIRST = (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(Phanton.getProperty("FirstDelaySpawn", "1")));
					DISCONNETC_DELAY = TimeUnit.MINUTES.toMillis(Integer.parseInt(Phanton.getProperty("DisconnectDelay", "15")));
					
					PHANTOM_PLAYERS_ARCHMAGE_1 = Phanton.getProperty("ArchMageAccount_1", "l2jsquash");
					PHANTOM_PLAYERS_MYSTICMUSE_1 = Phanton.getProperty("MysticMuseAccount_1", "l2jsquash");
					PHANTOM_PLAYERS_STORMSCREAM_1 = Phanton.getProperty("StormScreamAccount_1", "l2jsquash");
					
					PHANTOM_NAME_ARCHMAGE_1 = Phanton.getProperty("name_Archmage_1", "Player");
					PHANTOM_NAME_ARCHMAGE_LIST = new ArrayList<>();
					for (String type : PHANTOM_NAME_ARCHMAGE_1.split(","))
					{
						PHANTOM_NAME_ARCHMAGE_LIST.add(type);
					}
					PHANTOM_NAME_ARCHMAGE_1 = null;
					
					PHANTOM_NAME_MYSTICMUSE_1 = Phanton.getProperty("name_MysticMuse_1", "Player");
					PHANTOM_NAME_MYSTICMUSE_LIST = new ArrayList<>();
					for (String type : PHANTOM_NAME_MYSTICMUSE_1.split(","))
					{
						PHANTOM_NAME_MYSTICMUSE_LIST.add(type);
					}
					PHANTOM_NAME_MYSTICMUSE_1 = null;
					
					PHANTOM_NAME_STORMSCREAM_1 = Phanton.getProperty("name_StormScream_1", "Player");
					PHANTOM_NAME_STORMSCREAM_LIST = new ArrayList<>();
					for (String type : PHANTOM_NAME_STORMSCREAM_1.split(","))
					{
						PHANTOM_NAME_STORMSCREAM_LIST.add(type);
					}
					PHANTOM_NAME_STORMSCREAM_1 = null;
					
					PHANTOM_SURRENDER_INTERVAL = Phanton.getProperty("Interval_Surrender", 50);
					
					PHANTOM_MAGE_RANDOM_WALK = Phanton.getProperty("MageRandon_Walk", 50);
					PHANTOM_MAGE_INTERVAL_WALK = Phanton.getProperty("MageInterval_Walk", 50);
					PHANTOM_MAGE_INTERVAL_TARGET = Phanton.getProperty("MageInterval_Target", 50);
					PHANTOM_MAGE_INTERVAL_CHECK_TARGET = Phanton.getProperty("MageInterval_CheckTarget", 50);
					
					PHANTOM_ARCHMAGE_MATCK = Phanton.getProperty("ArchMage_mAtk", 50);
					PHANTOM_ARCHMAGE_PERCENTAGE = Phanton.getProperty("ArchMage_Crit", 50);
					PHANTOM_ARCHMAGE_DANO_INTERVAL = Phanton.getProperty("ArchMage_Interval_Dano", 50);
					
					PHANTOM_SPELLSINGER_MATCK = Phanton.getProperty("MysticMuse_mAtk", 50);
					PHANTOM_SPELLSINGER_PERCENTAGE = Phanton.getProperty("MysticMuse_Crit", 50);
					PHANTOM_SPELLSINGER_DANO_INTERVAL = Phanton.getProperty("MysticMuse_Interval_Dano", 50);
					
					PHANTOM_SPELLHOLLER_MATCK = Phanton.getProperty("StormScream_mAtk", 50);
					PHANTOM_SPELLHOLLER_PERCENTAGE = Phanton.getProperty("StormScream_Crit", 50);
					PHANTOM_SPELLHOLLER_DANO_INTERVAL = Phanton.getProperty("StormScream_Interval_Dano", 50);

	}
	
	/**
	 * Loads l2jmega settings.
	 */
	private static final void loadMega()
	{
		final ExProperties l2jmega = initProperties(Config.L2JMEGA_FILE);
		
	    MAX_ITEM_ENCHANT_KICK = Integer.parseInt(l2jmega.getProperty("EnchantKick", "0"));
	    TIME_KICK = l2jmega.getProperty("Time_Kick", 2);
		
		RESTART_BY_TIME_OF_DAY = Boolean.parseBoolean(l2jmega.getProperty("EnableRestartSystem", "false"));
		RESTART_SECONDS = Integer.parseInt(l2jmega.getProperty("RestartSeconds", "360"));
		RESTART_INTERVAL_BY_TIME_OF_DAY = l2jmega.getProperty("RestartByTimeOfDay", "20:00").split(",");

		
		ALLOW_HERO_SUBSKILL = l2jmega.getProperty("CustomHeroSubSkill", false);
		
	      DISABLE_ATTACK_NPC_TYPE = l2jmega.getProperty("DisableAttackToNpcs", false);
	      ALLOWED_NPC_TYPES = l2jmega.getProperty("AllowedNPCTypes");
	      LIST_ALLOWED_NPC_TYPES = new ArrayList<>();
	      for (String npc_type : ALLOWED_NPC_TYPES.split(",")) {
	        LIST_ALLOWED_NPC_TYPES.add(npc_type);
	      }
		
		/** MeGaPacK */
		DAY_TO_SIEGE  = l2jmega.getProperty("DayToSiege", 2);
		HOUR_TO_SIEGE  = l2jmega.getProperty("HourToSiege", 18);
		
		/** custom oly period */
		ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS = l2jmega.getProperty("AltOlyUseCustomPeriodSettings", false);
		ALT_OLY_PERIOD = l2jmega.getProperty("AltOlyPeriod", "MONTH");
		ALT_OLY_PERIOD_MULTIPLIER = l2jmega.getProperty("AltOlyPeriodMultiplier", 1);
		
		ALT_OLY_END_ANNOUNCE = Boolean.parseBoolean(l2jmega.getProperty("AltOlyEndAnnounce", "False"));
		
	    OLY_PROTECT_ITEMS = new ArrayList<>();
	   String [] arrayOfString1 = l2jmega.getProperty("OlyRestrictedItems", "0").split(",");int i = arrayOfString1.length;
	   for (int localObject = 0; localObject < i; localObject++)
	    {
	    	String id = arrayOfString1[localObject];
	      
	      OLY_PROTECT_ITEMS.add(Integer.valueOf(Integer.parseInt(id)));
	    }
		
	    OLLY_GRADE_A = Boolean.parseBoolean(l2jmega.getProperty("AllowOllyGradeS", "False"));
		
		ALT_MAX_NUM_OF_MEMBERS_IN_CLAN = Integer.parseInt(l2jmega.getProperty("AltMaxNumOfMembersInClan", "20"));
		
	      ALLOW_AUTO_NOBLESS_FROM_BOSS = Boolean.valueOf(l2jmega.getProperty("AllowAutoNoblessFromBoss", "True")).booleanValue();
	      BOSS_ID = Integer.parseInt(l2jmega.getProperty("BossId", "25325"));
	      RADIUS_TO_RAID = Integer.parseInt(l2jmega.getProperty("RadiusToRaid", "1000"));
	      
		    
		  ANNOUNCE_ID = Integer.parseInt(l2jmega.getProperty("AnnounceId", "3"));
			
		  GRAND_BOSS = l2jmega.getProperty("GrandBossList");
		  GRAND_BOSS_LIST = new ArrayList<>();
		  for (String id : GRAND_BOSS.trim().split(","))
		  {
			GRAND_BOSS_LIST.add(Integer.parseInt(id.trim()));
		  }
			
		  RAID_BOSS_ALIVE_MSG = l2jmega.getProperty("RaidBossAliveMsg", "Raid Boss %raidboss% is Alive.");
		  RAID_BOSS_DEFEATED_BY_CLAN_MEMBER_MSG = l2jmega.getProperty("RaidBossDefeatedByClanMemberMsg", "Raid Boss %raidboss% has been defeated by %player% of clan %clan%.");
		  RAID_BOSS_DEFEATED_BY_PLAYER_MSG = l2jmega.getProperty("RaidBossDefeatedByPlayerMsg", "Raid Boss %raidboss% has been defeated by %player%.");
			
		  ANNOUNCE_RAIDBOS_ALIVE_KILL = l2jmega.getProperty("AnnounceRaidBosKill", true);
		  ANNOUNCE_GRANBOS_ALIVE_KILL = l2jmega.getProperty("AnnounceGranBosKill", true);
		  QUAKE_RAID_BOSS = l2jmega.getProperty("QuakeRaidBoss", false);

	}
	
	/**
	 * Loads Ptfarm settings.
	 */
	private static final void loadPtfarm()
	{
		final ExProperties BestFarm = initProperties(Config.PARTY_FILE);
		
	      PARTY_FARM_MONSTER_DALAY = Integer.parseInt(BestFarm.getProperty("MonsterDelay", "10"));
	      PARTY_FARM_BY_TIME_OF_DAY = Boolean.parseBoolean(BestFarm.getProperty("PartyFarmEventEnabled", "false"));
	      START_PARTY = Boolean.parseBoolean(BestFarm.getProperty("StartSpawnPartyFarm", "false"));
	      NPC_SERVER_DELAY = BestFarm.getProperty("npcServerDelay", 70);
	      
	      EVENT_BEST_FARM_TIME = Integer.parseInt(BestFarm.getProperty("EventBestFarmTime", "1"));
	      EVENT_BEST_FARM_INTERVAL_BY_TIME_OF_DAY = BestFarm.getProperty("BestFarmStartTime", "20:00").split(",");
	      PARTY_MESSAGE_ENABLED = Boolean.parseBoolean(BestFarm.getProperty("ScreenPartyMessageEnable", "false"));
	      PARTY_FARM_MESSAGE_TEXT = BestFarm.getProperty("ScreenPartyFarmMessageText", "Welcome to L2J server!");
	      PARTY_FARM_MESSAGE_TIME = Integer.parseInt(BestFarm.getProperty("ScreenPartyFarmMessageTime", "10")) * 1000;

	      String[] monsterLocs2 = BestFarm.getProperty("MonsterLoc", "").split(";");
	      String[] locSplit3 = null;
	      
	      monsterId = Integer.parseInt(BestFarm.getProperty("MonsterId", "1"));
	      
	      MONSTER_LOCS_COUNT = monsterLocs2.length;
	      MONSTER_LOCS = new int[MONSTER_LOCS_COUNT][3];
	      int g;
	      for (int e = 0; e < MONSTER_LOCS_COUNT; e++)
	      {
	        locSplit3 = monsterLocs2[e].split(",");
	        for (g = 0; g < 3; g++) {
	          MONSTER_LOCS[e][g] = Integer.parseInt(locSplit3[g].trim());
	        }
	      }  

	}
	
	/**
	 * Loads balance settings.
	 */
	private static final void loadBalance()
	{
		final ExProperties balance = initProperties(Config.BALANCE_FILE);
		
	    ENABLE_CUSTOM_CRIT = Boolean.parseBoolean(balance.getProperty("Enable_Custom_CriticalChance", "true"));
	    MAX_PCRIT_RATE = Integer.parseInt(balance.getProperty("MaxPCritRate", "500"));
	    PCRIT_RATE_ArcherHuman = Integer.parseInt(balance.getProperty("PCritRate_ArcherHuman", "300"));
	    PCRIT_RATE_ArcherElfo = Integer.parseInt(balance.getProperty("PCritRate_ArcherElfo", "300"));
	    PCRIT_RATE_ArcherDarkElfo = Integer.parseInt(balance.getProperty("PCritRate_ArcherDarkElfo", "300"));
	    
	    MAX_MCRIT_RATE = Integer.parseInt(balance.getProperty("MaxMCritRate", "300"));
	    MCRIT_RATE_Archmage = Integer.parseInt(balance.getProperty("MCritRate_Archmage", "300"));
	    MCRIT_RATE_Soultaker = Integer.parseInt(balance.getProperty("MCritRate_Soultaker", "300"));
	    MCRIT_RATE_Mystic_Muse = Integer.parseInt(balance.getProperty("MCritRate_Mystic_Muse", "300"));
	    MCRIT_RATE_Storm_Screamer = Integer.parseInt(balance.getProperty("MCritRate_Storm_Screamer", "300"));
	    MCRIT_RATE_Dominator = Integer.parseInt(balance.getProperty("MCritRate_Dominator", "300"));
		
		
	    OLY_ENABLE_CUSTOM_CRIT = Boolean.parseBoolean(balance.getProperty("Enable_Oly_CriticalChance", "true"));
	    OLY_MAX_PCRIT_RATE = Integer.parseInt(balance.getProperty("OlyMaxPCritRate", "500"));
	    OLY_PCRIT_RATE_ArcherHuman = Integer.parseInt(balance.getProperty("OlyPCritRate_ArcherHuman", "300"));
	    OLY_PCRIT_RATE_ArcherElfo = Integer.parseInt(balance.getProperty("OlyPCritRate_ArcherElfo", "300"));
	    OLY_PCRIT_RATE_ArcherDarkElfo = Integer.parseInt(balance.getProperty("OlyPCritRate_ArcherDarkElfo", "300"));
	    
	    OLY_MAX_MCRIT_RATE = Integer.parseInt(balance.getProperty("OlyMaxMCritRate", "300"));
	    OLY_MCRIT_RATE_Archmage = Integer.parseInt(balance.getProperty("OlyMCritRate_Archmage", "300"));
	    OLY_MCRIT_RATE_Soultaker = Integer.parseInt(balance.getProperty("OlyMCritRate_Soultaker", "300"));
	    OLY_MCRIT_RATE_Mystic_Muse = Integer.parseInt(balance.getProperty("OlyMCritRate_Mystic_Muse", "300"));
	    OLY_MCRIT_RATE_Storm_Screamer = Integer.parseInt(balance.getProperty("OlyMCritRate_Storm_Screamer", "300"));
	    OLY_MCRIT_RATE_Dominator = Integer.parseInt(balance.getProperty("OlyMCritRate_Dominator", "300"));
		
		MAX_MATK_SPEED = Integer.parseInt(balance.getProperty("MaxMAtkSpeed", "1999"));		
	    MAX_PATK_SPEED = Integer.parseInt(balance.getProperty("MaxPAtkSpeed", "1500"));
	    MAX_PATK_SPEED_GHOST = Integer.parseInt(balance.getProperty("MaxPAtkSpeedGhost", "1500"));
	    MAX_PATK_SPEED_MOONL = Integer.parseInt(balance.getProperty("MaxPAtkSpeedMoonl", "1500"));
		
	      BLOW_ATTACK_FRONT = Integer.parseInt(balance.getProperty("BlowAttackFront", "50"));
	      BLOW_ATTACK_SIDE = Integer.parseInt(balance.getProperty("BlowAttackSide", "60"));
	      BLOW_ATTACK_BEHIND = Integer.parseInt(balance.getProperty("BlowAttackBehind", "70"));
	      
	      BACKSTAB_ATTACK_FRONT = Integer.parseInt(balance.getProperty("BackstabAttackFront", "0"));
	      BACKSTAB_ATTACK_SIDE = Integer.parseInt(balance.getProperty("BackstabAttackSide", "0"));
	      BACKSTAB_ATTACK_BEHIND = Integer.parseInt(balance.getProperty("BackstabAttackBehind", "70"));
		
	      ANTI_SS_BUG_1 = Integer.parseInt(balance.getProperty("Delay", "2700"));
	      ANTI_SS_BUG_2 = Integer.parseInt(balance.getProperty("DelayBow", "1500"));
	      ANTI_SS_BUG_3 = Integer.parseInt(balance.getProperty("DelayNextAttack", "470000"));

	}

	
	/**
	 * Loads player settings.
	 */
	private static final void loadPlayer()
	{
		final ExProperties pl = initProperties(Config.PLAYER_FILE);
		
	    DEFAULT_GLOBAL_CHAT = pl.getProperty("GlobalChat", "ON");
	    GLOBAL_CHAT_WITH_PVP = Boolean.valueOf(pl.getProperty("GlobalChatWithPvP", "false")).booleanValue();
	    GLOBAL_PVP_AMOUNT = Integer.parseInt(pl.getProperty("GlobalPvPAmount", "500"));
		
	    DEFAULT_TRADE_CHAT = pl.getProperty("TradeChat", "ON");
	    DISABLE_CAPSLOCK = Boolean.valueOf(pl.getProperty("DisableCapsLock", "false")).booleanValue();
	    TRADE_CHAT_WITH_PVP = Boolean.valueOf(pl.getProperty("TradeChatWithPvP", "false")).booleanValue();
	    TRADE_PVP_AMOUNT = Integer.parseInt(pl.getProperty("TradePvPAmount", "50"));
		
		 ADD_SKILL_NOBLES = Boolean.parseBoolean(pl.getProperty("Add_Skill_Noble", "False"));
		
	      BANKING_SYSTEM_GOLDBARS = pl.getProperty("BankingGoldbarCount", 1);
	      BANKING_SYSTEM_ADENA = pl.getProperty("BankingAdenaCount", 500000000);
		
		ITEM_ID = pl.getProperty("Item_list_id_Restrict", "");
		ITEM_LIST = new ArrayList<>();
		for (String Id : ITEM_ID.split(","))
		{
			ITEM_LIST.add(Integer.parseInt(Id));
	    }		
		ALT_DISABLE_ITEM_FOR_CLASSES = Boolean.parseBoolean(pl.getProperty("Disable_Item", "False"));
		DISABLE_ITEM_CLASSES_STRING = pl.getProperty("Classes_list", "");
		DISABLE_ITEM_CLASSES = new ArrayList<>();
		for (String class_id : DISABLE_ITEM_CLASSES_STRING.split(","))
		{
			if(!class_id.equals(""))
				DISABLE_ITEM_CLASSES.add(Integer.parseInt(class_id));
		}
		
	        DOORS_IDS_TO_OPEN_STRING = pl.getProperty("DoorsToOpen", "");
			DOORS_IDS_TO_OPEN = new ArrayList<>();
			for (String door : DOORS_IDS_TO_OPEN_STRING.split(";"))
			{
               if (!door.equals(""))
					DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
			}
	        DOORS_IDS_TO_CLOSE_STRING = pl.getProperty("DoorsToClose", "");
			DOORS_IDS_TO_CLOSE = new ArrayList<>();
			for (String door : DOORS_IDS_TO_CLOSE_STRING.split(";"))
		{
               if (!door.equals(""))
					DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
			}
		
	    REMOVE_WEAPON = Boolean.parseBoolean(pl.getProperty("RemoveWeapon", "False"));
	    REMOVE_CHEST = Boolean.parseBoolean(pl.getProperty("RemoveChest", "False"));
	    REMOVE_LEG = Boolean.parseBoolean(pl.getProperty("RemoveLeg", "False"));
	    
	    ALLOW_LIGHT_USE_HEAVY = Boolean.parseBoolean(pl.getProperty("AllowLightUseHeavy", "False"));
	    NOTALLOWCLASS = pl.getProperty("NotAllowedUseHeavy", "");
	    NOTALLOWEDUSEHEAVY = new ArrayList<>();
		for (String classId : NOTALLOWCLASS.split(","))
		{
	      
	      NOTALLOWEDUSEHEAVY.add(Integer.valueOf(Integer.parseInt(classId)));
	    }
		
	    ALLOW_HEAVY_USE_LIGHT = Boolean.parseBoolean(pl.getProperty("AllowHeavyUseLight", "False"));
	    NOTALLOWCLASSE = pl.getProperty("NotAllowedUseLight", "");
	    NOTALLOWEDUSELIGHT = new ArrayList<>();
		for (String classId : NOTALLOWCLASSE.split(","))
		{
	      
	      NOTALLOWEDUSELIGHT.add(Integer.valueOf(Integer.parseInt(classId)));
	    }
		
        ALT_DISABLE_BOW_CLASSES = Boolean.parseBoolean(pl.getProperty("AltDisableBow", "False"));
		DISABLE_BOW_CLASSES_STRING = pl.getProperty("DisableBowForClasses", "");
		DISABLE_BOW_CLASSES = new ArrayList<>();
		for (String class_id : DISABLE_BOW_CLASSES_STRING.split(","))
		{
			if(!class_id.equals(""))
				DISABLE_BOW_CLASSES.add(Integer.parseInt(class_id));
		}
		
	    MAGE_ID_RESTRICT = pl.getProperty("MageItem");
	    
	    MAGE_LISTID_RESTRICT = new ArrayList<>();
	    for (String id : MAGE_ID_RESTRICT.split(",")) {
	      MAGE_LISTID_RESTRICT.add(Integer.valueOf(Integer.parseInt(id)));
	    }
	    FIGHTER_ID_RESTRICT = pl.getProperty("FighterItem");
	    
	    FIGHTER_LISTID_RESTRICT = new ArrayList<>();
	    for (String id : FIGHTER_ID_RESTRICT.split(",")) {
	      FIGHTER_LISTID_RESTRICT.add(Integer.valueOf(Integer.parseInt(id)));
	    }
		
		String[] dropitems = pl.getProperty("VIPItens", "57").split(";");
		VIP_REWARD_LIST.clear();
		for (String rewarditem : dropitems)
		{
			String[] reward = rewarditem.split(",");
	        if (reward.length != 2) {
		          _log.warning("VIPItens [Config.load()]: invalid config property -> VIPItens \"" + rewarditem + "\"");
	        } else {
				try
		          {
		            VIP_REWARD_LIST.add(new int[] {
		            
		              Integer.parseInt(reward[0]), 
		              Integer.parseInt(reward[1]) });
		          }
				catch (NumberFormatException nfe)
				{
		            if (!rewarditem.isEmpty()) {
			              _log.warning("VIPItens [Config.load()]: invalid config property -> VIPItens \"" + reward + "\"");
				}
			}
		}
	}
	    CLEAR_VIP_EVENT_ENABLED = pl.getProperty("ClearVipEnabled", false);
	    CLEAR_VIP_INTERVAL_BY_TIME_OF_DAY = pl.getProperty("ClearVipStartTime", "20:00").split(",");
	    
	    VIP_30_DAYS_PRICE = Integer.parseInt(pl.getProperty("Vip_30_Days_Price", "30"));
	    VIP_60_DAYS_PRICE = Integer.parseInt(pl.getProperty("Vip_60_Days_Price", "60"));
	    VIP_90_DAYS_PRICE = Integer.parseInt(pl.getProperty("Vip_90_Days_Price", "90"));
	    VIP_ETERNAL_PRICE = Integer.parseInt(pl.getProperty("Vip_Eternal_Price", "120"));
	    
	    HERO_30_DAYS_PRICE = Integer.parseInt(pl.getProperty("Hero_30_Days_Price", "30"));
	    HERO_60_DAYS_PRICE = Integer.parseInt(pl.getProperty("Hero_60_Days_Price", "60"));
	    HERO_90_DAYS_PRICE = Integer.parseInt(pl.getProperty("Hero_90_Days_Price", "90"));
	    HERO_ETERNAL_PRICE = Integer.parseInt(pl.getProperty("Hero_Eternal_Price", "120"));
	    
	    DONATE_CLASS_PRICE = Integer.parseInt(pl.getProperty("Change_Class_Price", "15"));
	    DONATE_NAME_PRICE = Integer.parseInt(pl.getProperty("Change_Name_Price", "15"));
	    DONATE_SEX_PRICE = Integer.parseInt(pl.getProperty("Change_Sex_Price", "15"));
		
	    DONATE_COIN_ID = Integer.parseInt(pl.getProperty("DonateCoin_Id", "9511"));
	    DONATESYSTEM = pl.getProperty("DonateSystem", false);
	    VIP_SKILL = Boolean.parseBoolean(pl.getProperty("Vip_Skill", "False"));
	    
	    VIP_SKILLS = Integer.parseInt(pl.getProperty("VipSkill", "9511"));
	    
		 VIP_ADENA_RATE = Float.parseFloat(pl.getProperty("VIPAdenaRate", "1.5"));
		 VIP_DROP_RATE = Float.parseFloat(pl.getProperty("VIPDropRate", "1.5"));
		 VIP_SPOIL_RATE = Float.parseFloat(pl.getProperty("VIPSpoilRate", "1.5"));
		
		LEAVE_BUFFS_ON_DIE = pl.getProperty("LoseBuffsOnDeath", false);
		CHAOTIC_LEAVE_BUFFS_ON_DIE = pl.getProperty("ChaoticLoseBuffsOnDeath", false);
		
	      ALLOW_MANUTENCAO = Boolean.parseBoolean(pl.getProperty("AllowManutencao", "false"));
	      MANUTENCAO_TEXT = pl.getProperty("ManutencaoMessageText", "Servidor em Manutencao!");
	      TIME_MANUTENCAO = pl.getProperty("Disconect_Manutencao", 2);
	      TIME_MULTIBOX = pl.getProperty("Disconect_Multibox", 2);
		
	     MULTIBOX_PROTECTION_ENABLED = pl.getProperty("MultiboxProtectionEnabled", false);
	     MULTIBOX_PROTECTION_CLIENTS_PER_PC = pl.getProperty("ClientsPerPc", 2);
	     MULTIBOX_PROTECTION_PUNISH = pl.getProperty("MultiboxPunish", 2);
		
		CHAR_TITLE = pl.getProperty("CharTitle", false);
		ADD_CHAR_TITLE = pl.getProperty("CharAddTitle", "Welcome");
		
		CUSTOM_START_LVL = pl.getProperty("CustomStartLvl", 1);
		CUSTOM_SUBCLASS_LVL = pl.getProperty("CustomSubclassLvl", 40);
		
		SPAWN_CHAR = Boolean.parseBoolean(pl.getProperty("CustomSpawn", "false"));
		SPAWN_X = Integer.parseInt(pl.getProperty("SpawnX", ""));
		SPAWN_Y = Integer.parseInt(pl.getProperty("SpawnY", ""));
		SPAWN_Z = Integer.parseInt(pl.getProperty("SpawnZ", ""));
		
		PLAYER_ITEMS = Boolean.parseBoolean(pl.getProperty("PlayerItems", "False"));

		FIGHTER_ITEMS_LIST = parseItemsList(pl.getProperty("FighterItemsList", "123,456"));

		MAGE_ITEMS_LIST = parseItemsList(pl.getProperty("MageItemsList", "789,1223"));
		
		STARTING_ADENA = pl.getProperty("StartingAdena", 100);
		
	      ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(pl.getProperty("EnableModifySkillDuration", "false"));
	      if (ENABLE_MODIFY_SKILL_DURATION)
	      {
	        SKILL_DURATION_LIST = new HashMap<>();
	        
	        String[] propert = pl.getProperty("SkillDurationList", "").split(";");
	        
	        String[] infos = propert;int str1 = infos.length;
	        for (int id = 0; id < str1; id++)
	        {
	          String skill = infos[id];
	          
	          String[] skillSplit = skill.split(",");
	          if (skillSplit.length != 2) {
	            System.out.println("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
	          } else {
	            try
	            {
	              SKILL_DURATION_LIST.put(Integer.valueOf(Integer.parseInt(skillSplit[0])), Integer.valueOf(Integer.parseInt(skillSplit[1])));
	            }
	            catch (NumberFormatException nfe)
	            {
	              if (DEBUG) {
	                nfe.printStackTrace();
	              }
	              if (!skill.equals("")) {
	                System.out.println("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
	              }
	            }
	          }
	        }
	      }

	}
	
	/**
	 * Loads enchant settings.
	 */
	private static final void loadEnchant()
	{
		final ExProperties enchant = initProperties(Config.ENCHANT_FILE);
	    
	    String[] propertySplit = enchant.getProperty("NormalWeaponEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        _log.info("invalid config property");
	      } else {
	        try
	        {
	          NORMAL_WEAPON_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            _log.info("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("BlessWeaponEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        _log.info("invalid config property");
	      } else {
	        try
	        {
	          BLESS_WEAPON_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            _log.info("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("CrystalWeaponEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        _log.info("invalid config property");
	      } else {
	        try
	        {
	          CRYSTAL_WEAPON_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            _log.info("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("DonatorWeaponEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        System.out.println("invalid config property");
	      } else {
	        try
	        {
	          DONATOR_WEAPON_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            System.out.println("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("NormalArmorEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        _log.info("invalid config property");
	      } else {
	        try
	        {
	          NORMAL_ARMOR_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            _log.info("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("BlessArmorEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        _log.info("invalid config property");
	      } else {
	        try
	        {
	          BLESS_ARMOR_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            _log.info("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("CrystalArmorEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        _log.info("invalid config property");
	      } else {
	        try
	        {
	          CRYSTAL_ARMOR_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            _log.info("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("DonatorArmorEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        System.out.println("invalid config property");
	      } else {
	        try
	        {
	          DONATOR_ARMOR_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            System.out.println("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("NormalJewelryEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        _log.info("invalid config property");
	      } else {
	        try
	        {
	          NORMAL_JEWELRY_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            _log.info("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("BlessJewelryEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        _log.info("invalid config property");
	      } else {
	        try
	        {
	          BLESS_JEWELRY_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            _log.info("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("CrystalJewelryEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        _log.info("invalid config property");
	      } else {
	        try
	        {
	          CRYSTAL_JEWELRY_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            _log.info("invalid config property");
	          }
	        }
	      }
	    }
	    propertySplit = enchant.getProperty("DonatorJewelryEnchantLevel", "").split(";");
	    for (String readData : propertySplit)
	    {
	      String[] writeData = readData.split(",");
	      if (writeData.length != 2) {
	        System.out.println("invalid config property");
	      } else {
	        try
	        {
	          DONATOR_JEWELRY_ENCHANT_LEVEL.put(Integer.valueOf(Integer.parseInt(writeData[0])), Integer.valueOf(Integer.parseInt(writeData[1])));
	        }
	        catch (NumberFormatException nfe)
	        {
	          if (DEBUG) {
	            nfe.printStackTrace();
	          }
	          if (!readData.equals("")) {
	            System.out.println("invalid config property");
	          }
	        }
	      }
	    }
	    ENCHANT_HERO_WEAPON = Boolean.parseBoolean(enchant.getProperty("EnableEnchantHeroWeapons", "False"));
	    
	    GOLD_WEAPON = Integer.parseInt(enchant.getProperty("IdEnchantDonatorWeapon", "10010"));
	    
	    GOLD_ARMOR = Integer.parseInt(enchant.getProperty("IdEnchantDonatorArmor", "10011"));
	    
	    ENCHANT_SAFE_MAX = Integer.parseInt(enchant.getProperty("EnchantSafeMax", "3"));
	    
	    ENCHANT_SAFE_MAX_FULL = Integer.parseInt(enchant.getProperty("EnchantSafeMaxFull", "4"));
	    
	    SCROLL_STACKABLE = Boolean.parseBoolean(enchant.getProperty("ScrollStackable", "False"));
	    
	    ENCHANT_WEAPON_MAX = Integer.parseInt(enchant.getProperty("EnchantWeaponMax", "25"));
	    ENCHANT_ARMOR_MAX = Integer.parseInt(enchant.getProperty("EnchantArmorMax", "25"));
	    ENCHANT_JEWELRY_MAX = Integer.parseInt(enchant.getProperty("EnchantJewelryMax", "25"));
	    
	    BLESSED_ENCHANT_WEAPON_MAX = Integer.parseInt(enchant.getProperty("BlessedEnchantWeaponMax", "25"));
	    BLESSED_ENCHANT_ARMOR_MAX = Integer.parseInt(enchant.getProperty("BlessedEnchantArmorMax", "25"));
	    BLESSED_ENCHANT_JEWELRY_MAX = Integer.parseInt(enchant.getProperty("BlessedEnchantJewelryMax", "25"));
	    
	    BREAK_ENCHANT = Integer.valueOf(enchant.getProperty("BreakEnchant", "0")).intValue();
	    
	    CRYSTAL_ENCHANT_MIN = Integer.parseInt(enchant.getProperty("CrystalEnchantMin", "20"));
	    CRYSTAL_ENCHANT_WEAPON_MAX = Integer.parseInt(enchant.getProperty("CrystalEnchantWeaponMax", "25"));
	    CRYSTAL_ENCHANT_ARMOR_MAX = Integer.parseInt(enchant.getProperty("CrystalEnchantArmorMax", "25"));
	    CRYSTAL_ENCHANT_JEWELRY_MAX = Integer.parseInt(enchant.getProperty("CrystalEnchantJewelryMax", "25"));
	    
	    DONATOR_ENCHANT_MIN = Integer.parseInt(enchant.getProperty("DonatorEnchantMin", "20"));
	    DONATOR_ENCHANT_WEAPON_MAX = Integer.parseInt(enchant.getProperty("DonatorEnchantWeaponMax", "25"));
	    DONATOR_ENCHANT_ARMOR_MAX = Integer.parseInt(enchant.getProperty("DonatorEnchantArmorMax", "25"));
	    DONATOR_ENCHANT_JEWELRY_MAX = Integer.parseInt(enchant.getProperty("DonatorEnchantJewelryMax", "25"));
	    DONATOR_DECREASE_ENCHANT = Boolean.valueOf(enchant.getProperty("DonatorDecreaseEnchant", "false")).booleanValue();
	    
	    ENABLE_ENCHANT_ANNOUNCE = Boolean.parseBoolean(enchant.getProperty("EnableEnchantAnnounce", "False"));
	    ENCHANT_ANNOUNCE_LEVEL = enchant.getProperty("EnchantAnnounceLevels", "6,10,16,20");
	    LIST_ENCHANT_ANNOUNCE_LEVEL = new ArrayList<>();
	    for (String id : ENCHANT_ANNOUNCE_LEVEL.split(",")) {
	      LIST_ENCHANT_ANNOUNCE_LEVEL.add(Integer.valueOf(Integer.parseInt(id)));
	    }
		
      }
	
	/**
	 * Loads newbie settings.
	 */
	private static final void loadNewbie()
	{
		final ExProperties newbie = initProperties(Config.NEWBIE_FILE);
	    
	    NEWBIE_DIST = Integer.parseInt(newbie.getProperty("Dist", "80"));
	    NEWBIE_LADO = Integer.parseInt(newbie.getProperty("Yaw", "80"));
	    NEWBIE_ALTURA = Integer.parseInt(newbie.getProperty("Pitch", "80"));
	    
	    ENABLE_STARTUP = newbie.getProperty("StartupEnabled", true);
	    
	    String[] TelepropertySplit = newbie.getProperty("TeleToLocation", "0,0,0").split(",");
	    if (TelepropertySplit.length < 3)
	    {
	      System.out.println("NewbiesSystemEngine[Config.load()]: invalid config property -> TeleToLocation");
	    }
	    else
	    {
	      TELE_TO_LOCATION[0] = Integer.parseInt(TelepropertySplit[0]);
	      TELE_TO_LOCATION[1] = Integer.parseInt(TelepropertySplit[1]);
	      TELE_TO_LOCATION[2] = Integer.parseInt(TelepropertySplit[2]);
	    }
	    NEWBIE_ITEMS_ENCHANT = Integer.parseInt(newbie.getProperty("EnchantItens", "4"));
		
      }
	
	/**
	 * Loads guardian settings.
	 */
	private static final void loadGuardian()
	{
		final ExProperties guardian = initProperties(Config.GUARDIAN_FILE);
	    
		ENABLE_GUARDIAN = Boolean.parseBoolean(guardian.getProperty("Enable_Event", "False"));
		
		ID_GUARDIAN_B1 = Integer.parseInt(guardian.getProperty("Id_Guardian_B1", "0"));			
		ID_TELEPORT_B1 = Integer.parseInt(guardian.getProperty("Id_Teleport_B1", "0"));
		TELEPORT_B1_LOCX = Integer.parseInt(guardian.getProperty("Teleport_B1_LocX", "0"));
		TELEPORT_B1_LOCY = Integer.parseInt(guardian.getProperty("Teleport_B1_LocY", "0"));
		TELEPORT_B1_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_B1_LocZ", "0"));
        B1_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("B1_Zone_LocX", "0"));
		B1_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("B1_Zone_LocY", "0"));
		B1_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("B1_Zone_LocZ", "0"));
		TELEPORT_B1_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_B1_Respawn_Time", "0"));
		TELEPORT_B1_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_B1_Respawn_Delete", "0"));
		
		ID_GUARDIAN_B2 = Integer.parseInt(guardian.getProperty("Id_Guardian_B2", "0"));			
		ID_TELEPORT_B2 = Integer.parseInt(guardian.getProperty("Id_Teleport_B2", "0"));
		TELEPORT_B2_LOCX = Integer.parseInt(guardian.getProperty("Teleport_B2_LocX", "0"));
		TELEPORT_B2_LOCY = Integer.parseInt(guardian.getProperty("Teleport_B2_LocY", "0"));
		TELEPORT_B2_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_B2_LocZ", "0"));
        B2_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("B2_Zone_LocX", "0"));
		B2_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("B2_Zone_LocY", "0"));
		B2_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("B2_Zone_LocZ", "0"));
		TELEPORT_B2_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_B2_Respawn_Time", "0"));
		TELEPORT_B2_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_B2_Respawn_Delete", "0"));
		
		ID_GUARDIAN_B3 = Integer.parseInt(guardian.getProperty("Id_Guardian_B3", "0"));			
		ID_TELEPORT_B3 = Integer.parseInt(guardian.getProperty("Id_Teleport_B3", "0"));
		TELEPORT_B3_LOCX = Integer.parseInt(guardian.getProperty("Teleport_B3_LocX", "0"));
		TELEPORT_B3_LOCY = Integer.parseInt(guardian.getProperty("Teleport_B3_LocY", "0"));
		TELEPORT_B3_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_B3_LocZ", "0"));
        B3_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("B3_Zone_LocX", "0"));
		B3_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("B3_Zone_LocY", "0"));
		B3_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("B3_Zone_LocZ", "0"));
		TELEPORT_B3_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_B3_Respawn_Time", "0"));
		TELEPORT_B3_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_B3_Respawn_Delete", "0"));
		
		ID_GUARDIAN_B4 = Integer.parseInt(guardian.getProperty("Id_Guardian_B4", "0"));			
		ID_TELEPORT_B4 = Integer.parseInt(guardian.getProperty("Id_Teleport_B4", "0"));
		TELEPORT_B4_LOCX = Integer.parseInt(guardian.getProperty("Teleport_B4_LocX", "0"));
		TELEPORT_B4_LOCY = Integer.parseInt(guardian.getProperty("Teleport_B4_LocY", "0"));
		TELEPORT_B4_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_B4_LocZ", "0"));
        B4_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("B4_Zone_LocX", "0"));
		B4_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("B4_Zone_LocY", "0"));
		B4_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("B4_Zone_LocZ", "0"));
		TELEPORT_B4_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_B4_Respawn_Time", "0"));
		TELEPORT_B4_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_B4_Respawn_Delete", "0"));
		
		ID_GUARDIAN_B5 = Integer.parseInt(guardian.getProperty("Id_Guardian_B5", "0"));			
		ID_TELEPORT_B5 = Integer.parseInt(guardian.getProperty("Id_Teleport_B5", "0"));
		TELEPORT_B5_LOCX = Integer.parseInt(guardian.getProperty("Teleport_B5_LocX", "0"));
		TELEPORT_B5_LOCY = Integer.parseInt(guardian.getProperty("Teleport_B5_LocY", "0"));
		TELEPORT_B5_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_B5_LocZ", "0"));
        B5_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("B5_Zone_LocX", "0"));
		B5_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("B5_Zone_LocY", "0"));
		B5_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("B5_Zone_LocZ", "0"));
		TELEPORT_B5_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_B5_Respawn_Time", "0"));
		TELEPORT_B5_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_B5_Respawn_Delete", "0"));
		
		ID_GUARDIAN_BAIUM = Integer.parseInt(guardian.getProperty("Id_Guardian_Baium", "0"));
		
		ID_TELEPORT_BAIUM = Integer.parseInt(guardian.getProperty("Id_Teleport_Baium", "0"));
		TELEPORT_BAIUM_LOCX = Integer.parseInt(guardian.getProperty("Teleport_Baium_LocX", "0"));
		TELEPORT_BAIUM_LOCY = Integer.parseInt(guardian.getProperty("Teleport_Baium_LocY", "0"));
		TELEPORT_BAIUM_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_Baium_LocZ", "0"));

        BAIUM_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("Baium_Zone_LocX", "0"));
		BAIUM_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("Baium_Zone_LocY", "0"));
		BAIUM_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("Baium_Zone_LocZ", "0"));
		
		
		TELEPORT_BAIUM_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_Baium_Respawn_Time", "0"));
		TELEPORT_BAIUM_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_Baium_Respawn_Delete", "0"));
		
		
		
		ID_GUARDIAN_ZAKEN = Integer.parseInt(guardian.getProperty("Id_Guardian_Zaken", "0"));
		
		ID_TELEPORT_ZAKEN = Integer.parseInt(guardian.getProperty("Id_Teleport_Zaken", "0"));
		TELEPORT_ZAKEN_LOCX = Integer.parseInt(guardian.getProperty("Teleport_Zaken_LocX", "0"));
		TELEPORT_ZAKEN_LOCY = Integer.parseInt(guardian.getProperty("Teleport_Zaken_LocY", "0"));
		TELEPORT_ZAKEN_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_Zaken_LocZ", "0"));
		
		TELEPORT_ZAKEN_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_Zaken_Respawn_Time", "0"));
		TELEPORT_ZAKEN_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_Zaken_Respawn_Delete", "0"));

		
		ZAKEN_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("Zaken_Zone_LocX", "0"));
		ZAKEN_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("Zaken_Zone_LocY", "0"));
		ZAKEN_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("Zaken_Zone_LocZ", "0"));
		
		ID_GUARDIAN_ANTHARAS = Integer.parseInt(guardian.getProperty("Id_Guardian_Antharas", "0"));
		
		ID_TELEPORT_ANTHARAS = Integer.parseInt(guardian.getProperty("Id_Teleport_Antharas", "0"));
		TELEPORT_ANTHARAS_LOCX = Integer.parseInt(guardian.getProperty("Teleport_Antharas_LocX", "0"));
		TELEPORT_ANTHARAS_LOCY = Integer.parseInt(guardian.getProperty("Teleport_Antharas_LocY", "0"));
		TELEPORT_ANTHARAS_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_Antharas_LocZ", "0"));
		
		TELEPORT_ANTHARAS_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_Antharas_Respawn_Time", "0"));
		TELEPORT_ANTHARAS_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_Antharas_Respawn_Delete", "0"));

		
		ANTHARAS_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("Antharas_Zone_LocX", "0"));
		ANTHARAS_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("Antharas_Zone_LocY", "0"));
		ANTHARAS_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("Antharas_Zone_LocZ", "0"));
		
		ID_GUARDIAN_FRINTEZZA = Integer.parseInt(guardian.getProperty("Id_Guardian_Frintezza", "0"));
		
		ID_TELEPORT_FRINTEZZA = Integer.parseInt(guardian.getProperty("Id_Teleport_Frintezza", "0"));
		TELEPORT_FRINTEZZA_LOCX = Integer.parseInt(guardian.getProperty("Teleport_Frintezza_LocX", "0"));
		TELEPORT_FRINTEZZA_LOCY = Integer.parseInt(guardian.getProperty("Teleport_Frintezza_LocY", "0"));
		TELEPORT_FRINTEZZA_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_Frintezza_LocZ", "0"));
		
		TELEPORT_FRINTEZZA_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_Frintezza_Respawn_Time", "0"));
		TELEPORT_FRINTEZZA_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_Frintezza_Respawn_Delete", "0"));
		
		
		FRINTEZZA_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("Frintezza_Zone_LocX", "0"));
		FRINTEZZA_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("Frintezza_Zone_LocY", "0"));
		FRINTEZZA_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("Frintezza_Zone_LocZ", "0"));
		
		ID_GUARDIAN_VALAKAS = Integer.parseInt(guardian.getProperty("Id_Guardian_Valakas", "0"));
		
		ID_TELEPORT_VALAKAS = Integer.parseInt(guardian.getProperty("Id_Teleport_Valakas", "0"));
		TELEPORT_VALAKAS_LOCX = Integer.parseInt(guardian.getProperty("Teleport_Valakas_LocX", "0"));
		TELEPORT_VALAKAS_LOCY = Integer.parseInt(guardian.getProperty("Teleport_Valakas_LocY", "0"));
		TELEPORT_VALAKAS_LOCZ = Integer.parseInt(guardian.getProperty("Teleport_Valakas_LocZ", "0"));
		
		TELEPORT_VALAKAS_RESPAWN_TIME = Integer.parseInt(guardian.getProperty("Teleport_Valakas_Respawn_Time", "0"));
		TELEPORT_VALAKAS_DELETE_TIME = Integer.parseInt(guardian.getProperty("Teleport_Valakas_Respawn_Delete", "0"));
		
		
		VALAKAS_TELEPORT_LOCX = Integer.parseInt(guardian.getProperty("Valakas_Zone_LocX", "0"));
		VALAKAS_TELEPORT_LOCY = Integer.parseInt(guardian.getProperty("Valakas_Zone_LocY", "0"));
		VALAKAS_TELEPORT_LOCZ = Integer.parseInt(guardian.getProperty("Valakas_Zone_LocZ", "0"));
		
      }
	/**
	 * Loads tournament settings.
	 */
	private static final void loadTour()
	{
		final ExProperties tournament = initProperties(Config.TOUR_FILE);
		TOURNAMENT_SPAWNER_ENABLED = tournament.getProperty("TournamentSpawnEnabled", false);
		NPC_Heading = Integer.parseInt(tournament.getProperty("Heading", "1"));
		DUAL_BOX = tournament.getProperty("DualBox", false);

		
	      TITLE_COLOR_TEAM1 = tournament.getProperty("TitleColorTeam_1", "FFFFFF");
	      TITLE_COLOR_TEAM2 = tournament.getProperty("TitleColorTeam_2", "FFFFFF");
	      
	      ARENA_NPC = Integer.parseInt(tournament.getProperty("NPCRegister", "1"));
	      ARENA_TIME = Integer.parseInt(tournament.getProperty("ArenaEventTime", "1"));
	      
	      ARENA_REWARD_ID = tournament.getProperty("ArenaRewardId", 57);
	      
	      ARENA_EVENT_INTERVAL_BY_TIME_OF_DAY = tournament.getProperty("ArenaStartTime", "20:00").split(",");
	      ARENA_EVENT_SUMMON = tournament.getProperty("ArenaEventSummon", false);
	      
	      NPC_locx = Integer.parseInt(tournament.getProperty("Locx", "1"));
	      NPC_locy = Integer.parseInt(tournament.getProperty("Locy", "1"));
	      NPC_locz = Integer.parseInt(tournament.getProperty("Locz", "1"));

	      ARENA_SKILL_PROTECT = Boolean.parseBoolean(tournament.getProperty("ArenaSkillProtect", "false"));
	      for (String id : tournament.getProperty("ArenaDisableSkillList", "0").split(",")) {
	        ARENA_SKILL_LIST.add(Integer.valueOf(Integer.parseInt(id)));
	      }
	      for (String id1 : tournament.getProperty("ArenaStopSkillList", "0").split(",")) {
	        ARENA_STOP_SKILL_LIST.add(Integer.valueOf(Integer.parseInt(id1)));
	      }
	      
	      ARENA_MESSAGE_ENABLED = Boolean.parseBoolean(tournament.getProperty("ScreenArenaMessageEnable", "false"));
	      ARENA_MESSAGE_TEXT = tournament.getProperty("ScreenArenaMessageText", "Welcome to L2J server!");
	      ARENA_MESSAGE_TIME = Integer.parseInt(tournament.getProperty("ScreenArenaMessageTime", "10")) * 1000;
	      
	      duelist_COUNT_4X4 = tournament.getProperty("duelist_amount_4x4", 1);
	      dreadnought_COUNT_4X4 = tournament.getProperty("dreadnought_amount_4x4", 1);
	      tanker_COUNT_4X4 = tournament.getProperty("tanker_amount_4x4", 1);
	      dagger_COUNT_4X4 = tournament.getProperty("dagger_amount_4x4", 1);
	      archer_COUNT_4X4 = tournament.getProperty("archer_amount_4x4", 1);
	      bs_COUNT_4X4 = tournament.getProperty("bs_amount_4x4", 1);
	      archmage_COUNT_4X4 = tournament.getProperty("archmage_amount_4x4", 1);
	      soultaker_COUNT_4X4 = tournament.getProperty("soultaker_amount_4x4", 1);
	      mysticMuse_COUNT_4X4 = tournament.getProperty("mysticMuse_amount_4x4", 1);
	      stormScreamer_COUNT_4X4 = tournament.getProperty("stormScreamer_amount_4x4", 1);
	      titan_COUNT_4X4 = tournament.getProperty("titan_amount_4x4", 1);
	      grandKhauatari_COUNT_4X4 = tournament.getProperty("grandKhauatari_amount_4x4", 1);
	      dominator_COUNT_4X4 = tournament.getProperty("dominator_amount_4x4", 1);
	      doomcryer_COUNT_4X4 = tournament.getProperty("doomcryer_amount_4x4", 1);
	      
	      duelist_COUNT_9X9 = tournament.getProperty("duelist_amount_9x9", 1);
	      dreadnought_COUNT_9X9 = tournament.getProperty("dreadnought_amount_9x9", 1);
	      tanker_COUNT_9X9 = tournament.getProperty("tanker_amount_9x9", 1);
	      dagger_COUNT_9X9 = tournament.getProperty("dagger_amount_9x9", 1);
	      archer_COUNT_9X9 = tournament.getProperty("archer_amount_9x9", 1);
	      bs_COUNT_9X9 = tournament.getProperty("bs_amount_9x9", 1);
	      archmage_COUNT_9X9 = tournament.getProperty("archmage_amount_9x9", 1);
	      soultaker_COUNT_9X9 = tournament.getProperty("soultaker_amount_9x9", 1);
	      mysticMuse_COUNT_9X9 = tournament.getProperty("mysticMuse_amount_9x9", 1);
	      stormScreamer_COUNT_9X9 = tournament.getProperty("stormScreamer_amount_9x9", 1);
	      titan_COUNT_9X9 = tournament.getProperty("titan_amount_9x9", 1);
	      grandKhauatari_COUNT_9X9 = tournament.getProperty("grandKhauatari_amount_9x9", 1);
	      dominator_COUNT_9X9 = tournament.getProperty("dominator_amount_9x9", 1);
	      doomcryer_COUNT_9X9 = tournament.getProperty("doomcryer_amount_9x9", 1);
	      
	      
	      MSG_TEAM1 = tournament.getProperty("TitleTeam_1", "Team [1]");
	      MSG_TEAM2 = tournament.getProperty("TitleTeam_2", "Team [2]");
		

		
		TOURNAMENT_EVENT_EFFECTS_REMOVAL = tournament.getProperty("TournamentEventEffectsRemoval", 0);
		
		
		// 2x2
		ARENA_EVENT_ENABLED = tournament.getProperty("TournamentEventEnabled", false);
		if (ARENA_EVENT_ENABLED)
		{
			String[] arenaLocs = tournament.getProperty("TournamentLoc", "").split(";");
			String[] locSplit = null;
			ARENA_EVENT_COUNT = arenaLocs.length;
			ARENA_EVENT_LOCS = new int[ARENA_EVENT_COUNT][3];
			for (int i = 0; i < ARENA_EVENT_COUNT; i++)
			{
				locSplit = arenaLocs[i].split(",");
				for (int j = 0; j < 3; j++)
				{
					ARENA_EVENT_LOCS[i][j] = Integer.parseInt(locSplit[j].trim());
				}
			}
			ARENA_PVP_AMOUNT = tournament.getProperty("TournamentPvpJoin", 10);
			TOURNAMENT_ITEMS_REWARD = new ArrayList<>();
			String[] tournamentReward = tournament.getProperty("TournamentReward", "57,100000").split(";");
			for (String reward : tournamentReward)
			{
				String[] rewardSplit = reward.split(",");
				if (rewardSplit.length != 2)
				{
					_log.warning(StringUtil.concat("TournamentReward: invalid config property -> PvpItemsReward \"", reward, "\""));
				}
				else
				{
					try
					{
						TOURNAMENT_ITEMS_REWARD.add(new int[]
						{
							Integer.parseInt(rewardSplit[0]),
							Integer.parseInt(rewardSplit[1])
						});
					}
					catch (NumberFormatException nfe)
					{
						if (!reward.isEmpty())
						{
							_log.warning(StringUtil.concat("TournamentReward: invalid config property -> TournamentReward \"", reward, "\""));
						}
					}
				}
			}
			ARENA_CHECK_INTERVAL = tournament.getProperty("TournamentBattleCheckInterval", 15) * 1000;
			ARENA_CALL_INTERVAL = tournament.getProperty("TournamentBattleCallInterval", 60) * 1000;
			ARENA_WAIT_INTERVAL = tournament.getProperty("TournamentBattleWaitInterval", 20) * 1000;
			
			TOURNAMENT_ID_RESTRICT = tournament.getProperty("TournamentItemsRestriction");
			
			TOURNAMENT_LISTID_RESTRICT = new ArrayList<>();
			for (String id1 : TOURNAMENT_ID_RESTRICT.split(","))
				TOURNAMENT_LISTID_RESTRICT.add(Integer.parseInt(id1));
			
			DISABLE_TOURNAMENT_ID_CLASSES_STRING = tournament.getProperty("TournamentDisabledForClasses");
			
			DISABLE_TOURNAMENT_ID_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_TOURNAMENT_ID_CLASSES_STRING.split(","))
				DISABLE_TOURNAMENT_ID_CLASSES.add(Integer.parseInt(class_id));
		}
		
		// 4x4
		UNREAL_TOURNAMENT_EVENT_ENABLED = tournament.getProperty("UnrealTournamentEventEnabled", false);
		if (UNREAL_TOURNAMENT_EVENT_ENABLED)
		{
			String[] arenaLocs = tournament.getProperty("UnrealTournamentLoc", "").split(";");
			String[] locSplit = null;
			UNREAL_TOURNAMENT_EVENT_COUNT = arenaLocs.length;
			UNREAL_TOURNAMENT_EVENT_LOCS = new int[UNREAL_TOURNAMENT_EVENT_COUNT][3];
			for (int i = 0; i < UNREAL_TOURNAMENT_EVENT_COUNT; i++)
			{
				locSplit = arenaLocs[i].split(",");
				for (int j = 0; j < 3; j++)
				{
					UNREAL_TOURNAMENT_EVENT_LOCS[i][j] = Integer.parseInt(locSplit[j].trim());
				}
			}
			UNREAL_TOURNAMENT_PVP_AMOUNT = tournament.getProperty("UnrealTournamentPvpJoin", 10);
			UNREAL_TOURNAMENT_ITEMS_REWARD = new ArrayList<>();
			String[] xtremeReward = tournament.getProperty("UnrealTournamentReward", "57,100000").split(";");
			for (String reward : xtremeReward)
			{
				String[] rewardSplit = reward.split(",");
				if (rewardSplit.length != 2)
				{
					_log.warning(StringUtil.concat("UnrealTournamentReward: invalid config property -> UnrealTournamentReward \"", reward, "\""));
				}
				else
				{
					try
					{
						UNREAL_TOURNAMENT_ITEMS_REWARD.add(new int[]
						{
							Integer.parseInt(rewardSplit[0]),
							Integer.parseInt(rewardSplit[1])
						});
					}
					catch (NumberFormatException nfe)
					{
						if (!reward.isEmpty())
						{
							_log.warning(StringUtil.concat("UnrealTournamentReward: invalid config property -> UnrealTournamentReward \"", reward, "\""));
						}
					}
				}
			}
			UNREAL_TOURNAMENT_CHECK_INTERVAL = tournament.getProperty("UnrealTournamentBattleCheckInterval", 15) * 1000;
			UNREAL_TOURNAMENT_CALL_INTERVAL = tournament.getProperty("UnrealTournamentBattleCallInterval", 60) * 1000;
			UNREAL_TOURNAMENT_WAIT_INTERVAL = tournament.getProperty("UnrealTournamentBattleWaitInterval", 20) * 1000;
		}
		
		// 9x9
		XTREME_TOURNAMENT_EVENT_ENABLED = tournament.getProperty("XtremeTournamentEventEnabled", false);
		if (XTREME_TOURNAMENT_EVENT_ENABLED)
		{
			String[] arenaLocs = tournament.getProperty("XtremeTournamentLoc", "").split(";");
			String[] locSplit = null;
			XTREME_TOURNAMENT_EVENT_COUNT = arenaLocs.length;
			XTREME_TOURNAMENT_EVENT_LOCS = new int[XTREME_TOURNAMENT_EVENT_COUNT][3];
			for (int i = 0; i < XTREME_TOURNAMENT_EVENT_COUNT; i++)
			{
				locSplit = arenaLocs[i].split(",");
				for (int j = 0; j < 3; j++)
				{
					XTREME_TOURNAMENT_EVENT_LOCS[i][j] = Integer.parseInt(locSplit[j].trim());
				}
			}
			XTREME_TOURNAMENT_PVP_AMOUNT = tournament.getProperty("XtremeTournamentPvpJoin", 10);
			XTREME_TOURNAMENT_ITEMS_REWARD = new ArrayList<>();
			String[] xtremeReward = tournament.getProperty("XtremeTournamentReward", "57,100000").split(";");
			for (String reward : xtremeReward)
			{
				String[] rewardSplit = reward.split(",");
				if (rewardSplit.length != 2)
				{
					_log.warning(StringUtil.concat("XtremeTournamentReward: invalid config property -> XtremeTournamentReward \"", reward, "\""));
				}
				else
				{
					try
					{
						XTREME_TOURNAMENT_ITEMS_REWARD.add(new int[]
						{
							Integer.parseInt(rewardSplit[0]),
							Integer.parseInt(rewardSplit[1])
						});
					}
					catch (NumberFormatException nfe)
					{
						if (!reward.isEmpty())
						{
							_log.warning(StringUtil.concat("XtremeTournamentReward: invalid config property -> XtremeTournamentReward \"", reward, "\""));
						}
					}
				}
			}
			XTREME_TOURNAMENT_CHECK_INTERVAL = tournament.getProperty("XtremeTournamentBattleCheckInterval", 15) * 1000;
			XTREME_TOURNAMENT_CALL_INTERVAL = tournament.getProperty("XtremeTournamentBattleCallInterval", 60) * 1000;
			XTREME_TOURNAMENT_WAIT_INTERVAL = tournament.getProperty("XtremeTournamentBattleWaitInterval", 20) * 1000;
		}
	}
	
	private static final void loadFakes()
	{
		final ExProperties fake = initProperties(Config.FAKES_FILE);
		
		LOC_X = fake.getProperty("Spawn_locx", 0);
		LOC_Y = fake.getProperty("Spawn_locy", 0);
		LOC_Z = fake.getProperty("Spawn_locz", 0);
		
		LOC_X1 = fake.getProperty("Spawn_locx_1", 0);
		LOC_Y1 = fake.getProperty("Spawn_locy_1", 0);
		LOC_Z1 = fake.getProperty("Spawn_locz_1", 0);
		
		LOC_X2 = fake.getProperty("Spawn_locx_2", 0);
		LOC_Y2 = fake.getProperty("Spawn_locy_2", 0);
		LOC_Z2 = fake.getProperty("Spawn_locz_2", 0);
		
		LOC_X3 = fake.getProperty("Spawn_locx_3", 0);
		LOC_Y3 = fake.getProperty("Spawn_locy_3", 0);
		LOC_Z3 = fake.getProperty("Spawn_locz_3", 0);
		
		LOC_X4 = fake.getProperty("Spawn_locx_4", 0);
		LOC_Y4 = fake.getProperty("Spawn_locy_4", 0);
		LOC_Z4 = fake.getProperty("Spawn_locz_4", 0);
		
		LOC_X5 = fake.getProperty("Spawn_locx_5", 0);
		LOC_Y5 = fake.getProperty("Spawn_locy_5", 0);
		LOC_Z5 = fake.getProperty("Spawn_locz_5", 0);
		
		LOC_X6 = fake.getProperty("Spawn_locx_6", 0);
		LOC_Y6 = fake.getProperty("Spawn_locy_6", 0);
		LOC_Z6 = fake.getProperty("Spawn_locz_6", 0);
		
		LOC_X7 = fake.getProperty("Spawn_locx_7", 0);
		LOC_Y7 = fake.getProperty("Spawn_locy_7", 0);
		LOC_Z7 = fake.getProperty("Spawn_locz_7", 0);
		
		LOC_X8 = fake.getProperty("Spawn_locx_8", 0);
		LOC_Y8 = fake.getProperty("Spawn_locy_8", 0);
		LOC_Z8 = fake.getProperty("Spawn_locz_8", 0);
		
		LOC_X9 = fake.getProperty("Spawn_locx_9", 0);
		LOC_Y9 = fake.getProperty("Spawn_locy_9", 0);
		LOC_Z9 = fake.getProperty("Spawn_locz_9", 0);
		
		LOC_X10 = fake.getProperty("Spawn_locx_10", 0);
		LOC_Y10 = fake.getProperty("Spawn_locy_10", 0);
		LOC_Z10 = fake.getProperty("Spawn_locz_10", 0);
		
		LOC_X11 = fake.getProperty("Spawn_locx_11", 0);
		LOC_Y11 = fake.getProperty("Spawn_locy_11", 0);
		LOC_Z11 = fake.getProperty("Spawn_locz_11", 0);
		
		LOC_X12 = fake.getProperty("Spawn_locx_12", 0);
		LOC_Y12 = fake.getProperty("Spawn_locy_12", 0);
		LOC_Z12 = fake.getProperty("Spawn_locz_12", 0);
		
		LOC_X13 = fake.getProperty("Spawn_locx_13", 0);
		LOC_Y13 = fake.getProperty("Spawn_locy_13", 0);
		LOC_Z13 = fake.getProperty("Spawn_locz_13", 0);
		
		String[] arrayOfString1 = fake.getProperty("FakeEnchant", "0,14").split(",");
		FAKE_PLAYERS_ENCHANT_MIN = Integer.parseInt(arrayOfString1[0]);
		FAKE_PLAYERS_ENCHANT_MAX = Integer.parseInt(arrayOfString1[1]);
		
		WEAPONS_TIRANTY = fake.getProperty("Fake_Weapon_Tiranty", "");
		LIST_WEAPONS_TIRANTY = new ArrayList<>();
		for (String id1 : WEAPONS_TIRANTY.trim().split(","))
			LIST_WEAPONS_TIRANTY.add(Integer.parseInt(id1.trim()));
		
		WEAPONS_MAGES = fake.getProperty("Fake_Weapon_All_Mages", "");
		LIST_WEAPONS_MAGES = new ArrayList<>();
		for (String id1 : WEAPONS_MAGES.trim().split(","))
			LIST_WEAPONS_MAGES.add(Integer.parseInt(id1.trim()));
		
		WEAPONS_DREADNOUGHT = fake.getProperty("Fake_Weapon_Dreadnought", "");
		LIST_WEAPONS_DREADNOUGHT = new ArrayList<>();
		for (String id1 : WEAPONS_DREADNOUGHT.trim().split(","))
			LIST_WEAPONS_DREADNOUGHT.add(Integer.parseInt(id1.trim()));
		
		WEAPONS_DUELIST = fake.getProperty("Fake_Weapon_Duelist", "");
		LIST_WEAPONS_DUELIST = new ArrayList<>();
		for (String id1 : WEAPONS_DUELIST.trim().split(","))
			LIST_WEAPONS_DUELIST.add(Integer.parseInt(id1.trim()));
		
		WEAPONS_TITANS = fake.getProperty("Fake_Weapon_Titans", "");
		LIST_WEAPONS_TITANS = new ArrayList<>();
		for (String id1 : WEAPONS_TITANS.trim().split(","))
			LIST_WEAPONS_TITANS.add(Integer.parseInt(id1.trim()));
		
		WEAPONS_ARCHERS = fake.getProperty("Fake_Weapon_Archers", "");
		LIST_WEAPONS_ARCHERS = new ArrayList<>();
		for (String id1 : WEAPONS_ARCHERS.trim().split(","))
			LIST_WEAPONS_ARCHERS.add(Integer.parseInt(id1.trim()));
		
		WEAPONS_DAGGER = fake.getProperty("Fake_Weapon_Daggers", "");
		LIST_WEAPONS_DAGGER = new ArrayList<>();
		for (String id1 : WEAPONS_DAGGER.trim().split(","))
			LIST_WEAPONS_DAGGER.add(Integer.parseInt(id1.trim()));

		
		ARMORS = fake.getProperty("Fake_Armors_Rob", "");
		LIST_ARMORS_ROB = new ArrayList<>();
		for (String id1 : ARMORS.trim().split(","))
			LIST_ARMORS_ROB.add(Integer.parseInt(id1.trim()));
		
		ARMORS2 = fake.getProperty("Fake_Armors_Light", "");
		LIST_ARMORS_LIGHT = new ArrayList<>();
		for (String id1 : ARMORS2.trim().split(","))
			LIST_ARMORS_LIGHT.add(Integer.parseInt(id1.trim()));
		
		ARMORS3 = fake.getProperty("Fake_Armors_Heavy", "");
		LIST_ARMORS_HEAVY = new ArrayList<>();
		for (String id1 : ARMORS3.trim().split(","))
			LIST_ARMORS_HEAVY.add(Integer.parseInt(id1.trim()));
		

	}
	
	/**
	 * Loads gameserver settings.<br>
	 * IP addresses, database, rates, feature enabled/disabled, misc.
	 */
	private static final void loadServer()
	{
		final ExProperties server = initProperties(SERVER_FILE);
		
		GAMESERVER_HOSTNAME = server.getProperty("GameserverHostname");
		PORT_GAME = server.getProperty("GameserverPort", 7777);
		
		HOSTNAME = server.getProperty("Hostname", "*");
		
		GAME_SERVER_LOGIN_PORT = server.getProperty("LoginPort", 9014);
		GAME_SERVER_LOGIN_HOST = server.getProperty("LoginHost", "127.0.0.1");
		
		REQUEST_ID = server.getProperty("RequestServerID", 0);
		ACCEPT_ALTERNATE_ID = server.getProperty("AcceptAlternateID", true);
		
		DATABASE_URL = server.getProperty("URL", "jdbc:mysql://localhost/acis");
		DATABASE_LOGIN = server.getProperty("Login", "root");
		DATABASE_PASSWORD = server.getProperty("Password", "");
		DATABASE_MAX_CONNECTIONS = server.getProperty("MaximumDbConnections", 10);
		
		SERVER_LIST_BRACKET = server.getProperty("ServerListBrackets", false);
		SERVER_LIST_CLOCK = server.getProperty("ServerListClock", false);
		SERVER_GMONLY = server.getProperty("ServerGMOnly", false);
		SERVER_LIST_AGE = server.getProperty("ServerListAgeLimit", 0);
		SERVER_LIST_TESTSERVER = server.getProperty("TestServer", false);
		SERVER_LIST_PVPSERVER = server.getProperty("PvpServer", true);
		
		DELETE_DAYS = server.getProperty("DeleteCharAfterDays", 7);
		MAXIMUM_ONLINE_USERS = server.getProperty("MaximumOnlineUsers", 100);
		
		AUTO_LOOT = server.getProperty("AutoLoot", false);
		AUTO_LOOT_HERBS = server.getProperty("AutoLootHerbs", false);
		AUTO_LOOT_RAID = server.getProperty("AutoLootRaid", false);
		
		ALLOW_DISCARDITEM = server.getProperty("AllowDiscardItem", true);
		MULTIPLE_ITEM_DROP = server.getProperty("MultipleItemDrop", true);
		HERB_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyHerbTime", 15) * 1000;
		ITEM_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyItemTime", 600) * 1000;
		EQUIPABLE_ITEM_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyEquipableItemTime", 0) * 1000;
		SPECIAL_ITEM_DESTROY_TIME = new HashMap<>();
		String[] data = server.getProperty("AutoDestroySpecialItemTime", (String[]) null, ",");
		if (data != null)
		{
			for (String itemData : data)
			{
				String[] item = itemData.split("-");
				SPECIAL_ITEM_DESTROY_TIME.put(Integer.parseInt(item[0]), Integer.parseInt(item[1]) * 1000);
			}
		}
		PLAYER_DROPPED_ITEM_MULTIPLIER = server.getProperty("PlayerDroppedItemMultiplier", 1);
		
		RATE_XP = server.getProperty("RateXp", 1.);
		RATE_SP = server.getProperty("RateSp", 1.);
		RATE_PARTY_XP = server.getProperty("RatePartyXp", 1.);
		RATE_PARTY_SP = server.getProperty("RatePartySp", 1.);
		RATE_DROP_ADENA = server.getProperty("RateDropAdena", 1.);
		RATE_DROP_ITEMS = server.getProperty("RateDropItems", 1.);
		RATE_DROP_ITEMS_BY_RAID = server.getProperty("RateRaidDropItems", 1.);
		RATE_DROP_SPOIL = server.getProperty("RateDropSpoil", 1.);
		RATE_DROP_MANOR = server.getProperty("RateDropManor", 1);
		RATE_QUEST_DROP = server.getProperty("RateQuestDrop", 1.);
		RATE_QUEST_REWARD = server.getProperty("RateQuestReward", 1.);
		RATE_QUEST_REWARD_XP = server.getProperty("RateQuestRewardXP", 1.);
		RATE_QUEST_REWARD_SP = server.getProperty("RateQuestRewardSP", 1.);
		RATE_QUEST_REWARD_ADENA = server.getProperty("RateQuestRewardAdena", 1.);
		RATE_KARMA_EXP_LOST = server.getProperty("RateKarmaExpLost", 1.);
		RATE_SIEGE_GUARDS_PRICE = server.getProperty("RateSiegeGuardsPrice", 1.);
		RATE_DROP_COMMON_HERBS = server.getProperty("RateCommonHerbs", 1.);
		RATE_DROP_HP_HERBS = server.getProperty("RateHpHerbs", 1.);
		RATE_DROP_MP_HERBS = server.getProperty("RateMpHerbs", 1.);
		RATE_DROP_SPECIAL_HERBS = server.getProperty("RateSpecialHerbs", 1.);
		PLAYER_DROP_LIMIT = server.getProperty("PlayerDropLimit", 3);
		PLAYER_RATE_DROP = server.getProperty("PlayerRateDrop", 5);
		PLAYER_RATE_DROP_ITEM = server.getProperty("PlayerRateDropItem", 70);
		PLAYER_RATE_DROP_EQUIP = server.getProperty("PlayerRateDropEquip", 25);
		PLAYER_RATE_DROP_EQUIP_WEAPON = server.getProperty("PlayerRateDropEquipWeapon", 5);
		PET_XP_RATE = server.getProperty("PetXpRate", 1.);
		PET_FOOD_RATE = server.getProperty("PetFoodRate", 1);
		SINEATER_XP_RATE = server.getProperty("SinEaterXpRate", 1.);
		KARMA_DROP_LIMIT = server.getProperty("KarmaDropLimit", 10);
		KARMA_RATE_DROP = server.getProperty("KarmaRateDrop", 70);
		KARMA_RATE_DROP_ITEM = server.getProperty("KarmaRateDropItem", 50);
		KARMA_RATE_DROP_EQUIP = server.getProperty("KarmaRateDropEquip", 40);
		KARMA_RATE_DROP_EQUIP_WEAPON = server.getProperty("KarmaRateDropEquipWeapon", 10);
		
		ALLOW_FREIGHT = server.getProperty("AllowFreight", true);
		ALLOW_WAREHOUSE = server.getProperty("AllowWarehouse", true);
		ALLOW_WEAR = server.getProperty("AllowWear", true);
		WEAR_DELAY = server.getProperty("WearDelay", 5);
		WEAR_PRICE = server.getProperty("WearPrice", 10);
		ALLOW_LOTTERY = server.getProperty("AllowLottery", true);
		ALLOW_WATER = server.getProperty("AllowWater", true);
		ALLOW_MANOR = server.getProperty("AllowManor", true);
		ALLOW_BOAT = server.getProperty("AllowBoat", true);
		ALLOW_CURSED_WEAPONS = server.getProperty("AllowCursedWeapons", true);
		
		ENABLE_FALLING_DAMAGE = server.getProperty("EnableFallingDamage", true);
		
		ALT_DEV_NO_SPAWNS = server.getProperty("NoSpawns", false);
		DEBUG = server.getProperty("Debug", false);
		DEVELOPER = server.getProperty("Developer", false);
		PACKET_HANDLER_DEBUG = server.getProperty("PacketHandlerDebug", false);
		
		DEADLOCK_DETECTOR = server.getProperty("DeadLockDetector", false);
		DEADLOCK_CHECK_INTERVAL = server.getProperty("DeadLockCheckInterval", 20);
		RESTART_ON_DEADLOCK = server.getProperty("RestartOnDeadlock", false);
		
		LOG_CHAT = server.getProperty("LogChat", false);
		LOG_ITEMS = server.getProperty("LogItems", false);
		GMAUDIT = server.getProperty("GMAudit", false);
		
		ENABLE_COMMUNITY_BOARD = server.getProperty("EnableCommunityBoard", false);
		BBS_DEFAULT = server.getProperty("BBSDefault", "_bbshome");
		
		ROLL_DICE_TIME = server.getProperty("RollDiceTime", 4200);
		HERO_VOICE_TIME = server.getProperty("HeroVoiceTime", 10000);
		SUBCLASS_TIME = server.getProperty("SubclassTime", 2000);
		DROP_ITEM_TIME = server.getProperty("DropItemTime", 1000);
		SERVER_BYPASS_TIME = server.getProperty("ServerBypassTime", 500);
		MULTISELL_TIME = server.getProperty("MultisellTime", 100);
		MANUFACTURE_TIME = server.getProperty("ManufactureTime", 300);
		MANOR_TIME = server.getProperty("ManorTime", 3000);
		SENDMAIL_TIME = server.getProperty("SendMailTime", 10000);
		CHARACTER_SELECT_TIME = server.getProperty("CharacterSelectTime", 3000);
		GLOBAL_CHAT_TIME = server.getProperty("GlobalChatTime", 0);
		TRADE_CHAT_TIME = server.getProperty("TradeChatTime", 0);
		SOCIAL_TIME = server.getProperty("SocialTime", 2000);
		
		SCHEDULED_THREAD_POOL_COUNT = server.getProperty("ScheduledThreadPoolCount", -1);
		THREADS_PER_SCHEDULED_THREAD_POOL = server.getProperty("ThreadsPerScheduledThreadPool", 4);
		INSTANT_THREAD_POOL_COUNT = server.getProperty("InstantThreadPoolCount", -1);
		THREADS_PER_INSTANT_THREAD_POOL = server.getProperty("ThreadsPerInstantThreadPool", 2);
		
		L2WALKER_PROTECTION = server.getProperty("L2WalkerProtection", false);
		ZONE_TOWN = server.getProperty("ZoneTown", 0);
		SERVER_NEWS = server.getProperty("ShowServerNews", false);
		DISABLE_TUTORIAL = server.getProperty("DisableTutorial", false);
	}
	
	/**
	 * Loads loginserver settings.<br>
	 * IP addresses, database, account, misc.
	 */
	private static final void loadLogin()
	{
		final ExProperties server = initProperties(LOGIN_CONFIGURATION_FILE);
		HOSTNAME = server.getProperty("Hostname", "localhost");
		
		LOGIN_BIND_ADDRESS = server.getProperty("LoginserverHostname", "*");
		PORT_LOGIN = server.getProperty("LoginserverPort", 2106);
		
		GAME_SERVER_LOGIN_HOST = server.getProperty("LoginHostname", "*");
		GAME_SERVER_LOGIN_PORT = server.getProperty("LoginPort", 9014);
		
		LOGIN_TRY_BEFORE_BAN = server.getProperty("LoginTryBeforeBan", 3);
		LOGIN_BLOCK_AFTER_BAN = server.getProperty("LoginBlockAfterBan", 600);
		ACCEPT_NEW_GAMESERVER = server.getProperty("AcceptNewGameServer", false);
		
		SHOW_LICENCE = server.getProperty("ShowLicence", true);
		
		DATABASE_URL = server.getProperty("URL", "jdbc:mysql://localhost/acis");
		DATABASE_LOGIN = server.getProperty("Login", "root");
		DATABASE_PASSWORD = server.getProperty("Password", "");
		DATABASE_MAX_CONNECTIONS = server.getProperty("MaximumDbConnections", 10);
		
		AUTO_CREATE_ACCOUNTS = server.getProperty("AutoCreateAccounts", true);
		
		LOG_LOGIN_CONTROLLER = server.getProperty("LogLoginController", false);
		
		FLOOD_PROTECTION = server.getProperty("EnableFloodProtection", true);
		FAST_CONNECTION_LIMIT = server.getProperty("FastConnectionLimit", 15);
		NORMAL_CONNECTION_TIME = server.getProperty("NormalConnectionTime", 700);
		FAST_CONNECTION_TIME = server.getProperty("FastConnectionTime", 350);
		MAX_CONNECTION_PER_IP = server.getProperty("MaxConnectionPerIP", 50);
	}
	
	public static final void loadGameServer()
	{
		
		_log.info("Loading gameserver configuration files.");
		
		// clans settings
		loadClans();
		
		// events settings
		loadEvents();
		
		// geoengine settings
		loadGeoengine();
		
		// hexID
		loadHexID();
		
		// NPCs/monsters settings
		loadNpcs();
		
		// players settings
		loadPlayers();
		
		// enchant settings
		loadEnchant();
	   
		// newbie settings
		loadNewbie();
		
		// guardian settings
		loadGuardian();
		
		// tournament settings
		loadTour();
		
		// siege settings
		loadSieges();
		
		// pvp settings
		loadPvp();
		
		// admin settings
		loadAdmin();
		
		// bot settings
		loadBot();
		
		// phantom settings
		loadPhantom();
		
		// l2jmega settings
		loadMega();
		
		// Ptfarm settings
		loadPtfarm();
		
		// balance settings
		loadBalance();
		
		// player settings
		loadPlayer();
		
		// fakes settings
		loadFakes();
		
		// server settings
		loadServer();
	}
	
	public static final void loadLoginServer()
	{
		_log.info("Loading loginserver configuration files.");
		
		// login settings
		loadLogin();
	}
	
	public static final void loadAccountManager()
	{
		_log.info("Loading account manager configuration files.");
		
		// login settings
		loadLogin();
	}
	
	public static final void loadGameServerRegistration()
	{
		_log.info("Loading gameserver registration configuration files.");
		
		// login settings
		loadLogin();
	}
	
	public static final void loadGeodataConverter()
	{
		_log.info("Loading geodata converter configuration files.");
		
		// geoengine settings
		loadGeoengine();
	}
	
	public static final class ClassMasterSettings
	{
		private final Map<Integer, Boolean> _allowedClassChange;
		private final Map<Integer, List<IntIntHolder>> _claimItems;
		private final Map<Integer, List<IntIntHolder>> _rewardItems;
		
		public ClassMasterSettings(String configLine)
		{
			_allowedClassChange = new HashMap<>(3);
			_claimItems = new HashMap<>(3);
			_rewardItems = new HashMap<>(3);
			
			if (configLine != null)
				parseConfigLine(configLine.trim());
		}
		
		private void parseConfigLine(String configLine)
		{
			StringTokenizer st = new StringTokenizer(configLine, ";");
			while (st.hasMoreTokens())
			{
				// Get allowed class change.
				int job = Integer.parseInt(st.nextToken());
				
				_allowedClassChange.put(job, true);
				
				List<IntIntHolder> items = new ArrayList<>();
				
				// Parse items needed for class change.
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						items.add(new IntIntHolder(Integer.parseInt(st3.nextToken()), Integer.parseInt(st3.nextToken())));
					}
				}
				
				// Feed the map, and clean the list.
				_claimItems.put(job, items);
				items = new ArrayList<>();
				
				// Parse gifts after class change.
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						items.add(new IntIntHolder(Integer.parseInt(st3.nextToken()), Integer.parseInt(st3.nextToken())));
					}
				}
				
				_rewardItems.put(job, items);
			}
		}
		
		public boolean isAllowed(int job)
		{
			if (_allowedClassChange == null)
				return false;
			
			if (_allowedClassChange.containsKey(job))
				return _allowedClassChange.get(job);
			
			return false;
		}
		
		public List<IntIntHolder> getRewardItems(int job)
		{
			return _rewardItems.get(job);
		}
		
		public List<IntIntHolder> getRequiredItems(int job)
		{
			return _claimItems.get(job);
		}
	}
}