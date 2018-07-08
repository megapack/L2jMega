package net.sf.l2j.gameserver.model.actor.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.xml.HerbDropData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.MinionData;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.scripting.EventType;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.templates.StatsSet;

public class NpcTemplate extends CreatureTemplate
{
	public static enum SkillType
	{
		BUFF,
		DEBUFF,
		HEAL,
		PASSIVE,
		LONG_RANGE,
		SHORT_RANGE,
		SUICIDE
	}
	
	public static enum AIType
	{
		DEFAULT,
		ARCHER,
		MAGE,
		HEALER,
		CORPSE
	}
	
	public static enum Race
	{
		UNKNOWN,
		UNDEAD,
		MAGICCREATURE,
		BEAST,
		ANIMAL,
		PLANT,
		HUMANOID,
		SPIRIT,
		ANGEL,
		DEMON,
		DRAGON,
		GIANT,
		BUG,
		FAIRIE,
		HUMAN,
		ELVE,
		DARKELVE,
		ORC,
		DWARVE,
		OTHER,
		NONLIVING,
		SIEGEWEAPON,
		DEFENDINGARMY,
		MERCENARIE;
		
		public static final Race[] VALUES = values();
	}
	
	protected static final Logger _log = Logger.getLogger(NpcTemplate.class.getName());
	
	private final int _npcId;
	private final int _idTemplate;
	private final String _type;
	private final String _name;
	private boolean _usingServerSideName;
	private final String _title;
	private boolean _usingServerSideTitle;
	private final boolean _cantBeChampionMonster;
	private final byte _level;
	private final int _exp;
	private final int _sp;
	private final int _rHand;
	private final int _lHand;
	private final int _enchantEffect;
	private final int _corpseTime;
	
	private int _dropHerbGroup;
	private Race _race = Race.UNKNOWN;
	private AIType _aiType;
	
	private final int _ssCount;
	private final int _ssRate;
	private final int _spsCount;
	private final int _spsRate;
	private final int _aggroRange;
	
	private String[] _clans;
	private int _clanRange;
	private int[] _ignoredIds;
	
	private final boolean _canMove;
	private final boolean _isSeedable;
	
	private List<DropCategory> _categories;
	private List<MinionData> _minions;
	private List<ClassId> _teachInfo;
	
	private final Map<SkillType, List<L2Skill>> _skills = new HashMap<>();
	private final Map<EventType, List<Quest>> _questEvents = new HashMap<>();
	
	private Castle _castle;
	
	public NpcTemplate(StatsSet set)
	{
		super(set);
		
		_npcId = set.getInteger("id");
		_idTemplate = set.getInteger("idTemplate", _npcId);
		_type = set.getString("type");
		_name = set.getString("name");
		_usingServerSideName = set.getBool("usingServerSideName", false);
		_title = set.getString("title", "");
		_usingServerSideTitle = set.getBool("usingServerSideTitle", false);
		_cantBeChampionMonster = _title.equalsIgnoreCase("Quest Monster") || isType("Chest");
		_level = set.getByte("level", (byte) 1);
		_exp = set.getInteger("exp", 0);
		_sp = set.getInteger("sp", 0);
		_rHand = set.getInteger("rHand", 0);
		_lHand = set.getInteger("lHand", 0);
		_enchantEffect = set.getInteger("enchant", 0);
		_corpseTime = set.getInteger("corpseTime", 7);
		
		_dropHerbGroup = set.getInteger("dropHerbGroup", 0);
		if (_dropHerbGroup > 0 && HerbDropData.getInstance().getHerbDroplist(_dropHerbGroup) == null)
		{
			_log.warning("Missing dropHerbGroup information for npcId: " + _npcId + ", dropHerbGroup: " + _dropHerbGroup);
			_dropHerbGroup = 0;
		}
		
		if (set.containsKey("raceId"))
			setRace(set.getInteger("raceId"));
		
		_aiType = set.getEnum("aiType", AIType.class, AIType.DEFAULT);
		
		_ssCount = set.getInteger("ssCount", 0);
		_ssRate = set.getInteger("ssRate", 0);
		_spsCount = set.getInteger("spsCount", 0);
		_spsRate = set.getInteger("spsRate", 0);
		_aggroRange = set.getInteger("aggro", 0);
		
		if (set.containsKey("clan"))
		{
			_clans = set.getStringArray("clan");
			_clanRange = set.getInteger("clanRange");
			
			if (set.containsKey("ignoredIds"))
				_ignoredIds = set.getIntegerArray("ignoredIds");
		}
		
		_canMove = set.getBool("canMove", true);
		_isSeedable = set.getBool("seedable", false);
		
		_categories = set.getList("drops");
		_minions = set.getList("minions");
		
		if (set.containsKey("teachTo"))
		{
			final int[] classIds = set.getIntegerArray("teachTo");
			
			_teachInfo = new ArrayList<>(classIds.length);
			for (int classId : classIds)
				_teachInfo.add(ClassId.VALUES[classId]);
		}
		
		addSkills(set.getList("skills"));
		
		// Set the Castle.
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getRelatedNpcIds().contains(_npcId))
			{
				_castle = castle;
				break;
			}
		}
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public int getIdTemplate()
	{
		return _idTemplate;
	}
	
	public String getType()
	{
		return _type;
	}
	
	/**
	 * Checks types, ignore case.
	 * @param t the type to check.
	 * @return true if the type are the same, false otherwise.
	 */
	public boolean isType(String t)
	{
		return _type.equalsIgnoreCase(t);
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean isUsingServerSideName()
	{
		return _usingServerSideName;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public boolean isUsingServerSideTitle()
	{
		return _usingServerSideTitle;
	}
	
	public boolean cantBeChampion()
	{
		return _cantBeChampionMonster;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public int getRewardExp()
	{
		return _exp;
	}
	
	public int getRewardSp()
	{
		return _sp;
	}
	
	public int getRightHand()
	{
		return _rHand;
	}
	
	public int getLeftHand()
	{
		return _lHand;
	}
	
	public int getEnchantEffect()
	{
		return _enchantEffect;
	}
	
	public int getCorpseTime()
	{
		return _corpseTime;
	}
	
	public int getDropHerbGroup()
	{
		return _dropHerbGroup;
	}
	
	public Race getRace()
	{
		return _race;
	}
	
	public void setRace(int raceId)
	{
		// Race.UNKNOWN is already the default value. No needs to handle it.
		if (raceId < 1 || raceId > 23)
			return;
		
		_race = Race.VALUES[raceId];
	}
	
	public AIType getAiType()
	{
		return _aiType;
	}
	
	public int getSsCount()
	{
		return _ssCount;
	}
	
	public int getSsRate()
	{
		return _ssRate;
	}
	
	public int getSpsCount()
	{
		return _spsCount;
	}
	
	public int getSpsRate()
	{
		return _spsRate;
	}
	
	public int getAggroRange()
	{
		return _aggroRange;
	}
	
	public String[] getClans()
	{
		return _clans;
	}
	
	public int getClanRange()
	{
		return _clanRange;
	}
	
	public int[] getIgnoredIds()
	{
		return _ignoredIds;
	}
	
	public boolean canMove()
	{
		return _canMove;
	}
	
	public boolean isSeedable()
	{
		return _isSeedable;
	}
	
	/**
	 * @return the list of all possible UNCATEGORIZED drops of this L2NpcTemplate.
	 */
	public List<DropCategory> getDropData()
	{
		return _categories;
	}
	
	public Castle getCastle()
	{
		return _castle;
	}
	
	/**
	 * @return the list of all possible item drops of this L2NpcTemplate. (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)
	 */
	public List<DropData> getAllDropData()
	{
		final List<DropData> list = new ArrayList<>();
		for (DropCategory tmp : _categories)
			list.addAll(tmp.getAllDrops());
		
		return list;
	}
	
	/**
	 * Add a drop to a given category. If the category does not exist, create it.
	 * @param drop
	 * @param categoryType
	 */
	public void addDropData(DropData drop, int categoryType)
	{
		final boolean isBossType = isType("RaidBoss") || isType("GrandBoss");
		
		synchronized (_categories)
		{
			// Category exists, stores the drop and return.
			for (DropCategory cat : _categories)
			{
				if (cat.getCategoryType() == categoryType)
				{
					cat.addDropData(drop, isBossType);
					return;
				}
			}
			
			// Category doesn't exist, create and store it.
			final DropCategory cat = new DropCategory(categoryType);
			cat.addDropData(drop, isBossType);
			
			_categories.add(cat);
		}
	}
	
	/**
	 * @return the list of all Minions that must be spawn with the L2Npc using this L2NpcTemplate.
	 */
	public List<MinionData> getMinionData()
	{
		return _minions;
	}
	
	public boolean canTeach(ClassId classId)
	{
		return _teachInfo != null && _teachInfo.contains((classId.level() == 3) ? classId.getParent() : classId);
	}
	
	public Map<SkillType, List<L2Skill>> getSkills()
	{
		return _skills;
	}
	
	public List<L2Skill> getSkills(SkillType type)
	{
		return _skills.getOrDefault(type, Collections.emptyList());
	}
	
	public void addSkills(List<L2Skill> skills)
	{
		for (L2Skill skill : skills)
		{
			if (skill.isPassive())
			{
				addSkill(SkillType.PASSIVE, skill);
				continue;
			}
			
			if (skill.isSuicideAttack())
			{
				addSkill(SkillType.SUICIDE, skill);
				continue;
			}
			
			switch (skill.getSkillType())
			{
				case BUFF:
				case CONT:
				case REFLECT:
					addSkill(SkillType.BUFF, skill);
					continue;
				
				case HEAL:
				case HOT:
				case HEAL_PERCENT:
				case HEAL_STATIC:
				case BALANCE_LIFE:
				case MANARECHARGE:
				case MANAHEAL_PERCENT:
					addSkill(SkillType.HEAL, skill);
					continue;
				
				case DEBUFF:
				case ROOT:
				case SLEEP:
				case STUN:
				case PARALYZE:
				case POISON:
				case DOT:
				case MDOT:
				case BLEED:
				case MUTE:
				case FEAR:
				case CANCEL:
				case NEGATE:
				case WEAKNESS:
				case AGGDEBUFF:
					addSkill(SkillType.DEBUFF, skill);
					continue;
				
				case PDAM:
				case MDAM:
				case BLOW:
				case DRAIN:
				case CHARGEDAM:
				case FATAL:
				case DEATHLINK:
				case MANADAM:
				case CPDAMPERCENT:
				case GET_PLAYER:
				case INSTANT_JUMP:
				case AGGDAMAGE:
					addSkill((skill.getCastRange() > 150) ? SkillType.LONG_RANGE : SkillType.SHORT_RANGE, skill);
					continue;
			}
			// _log.warning(skill.getName() + " skill wasn't added due to specific logic."); TODO
		}
	}
	
	public void addSkill(SkillType type, L2Skill skill)
	{
		List<L2Skill> list = _skills.get(type);
		if (list == null)
		{
			list = new ArrayList<>(5);
			list.add(skill);
			
			_skills.put(type, list);
		}
		else
			list.add(skill);
	}
	
	public Map<EventType, List<Quest>> getEventQuests()
	{
		return _questEvents;
	}
	
	public List<Quest> getEventQuests(EventType EventType)
	{
		return _questEvents.get(EventType);
	}
	
	public void addQuestEvent(EventType type, Quest quest)
	{
		List<Quest> list = _questEvents.get(type);
		if (list == null)
		{
			list = new ArrayList<>(5);
			list.add(quest);
			
			_questEvents.put(type, list);
		}
		else
		{
			list.remove(quest);
			
			if (type.isMultipleRegistrationAllowed() || list.isEmpty())
				list.add(quest);
			else
				_log.warning("Quest event not allow multiple quest registrations. Skipped addition of EventType \"" + type + "\" for NPC \"" + getName() + "\" and quest \"" + quest.getName() + "\".");
		}
	}
}