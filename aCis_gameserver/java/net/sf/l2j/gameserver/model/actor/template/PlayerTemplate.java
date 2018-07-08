package net.sf.l2j.gameserver.model.actor.template;

import java.util.List;

import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.ClassRace;
import net.sf.l2j.gameserver.model.base.Sex;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * A datatype extending {@link CreatureTemplate}, used to retain Player template informations such as classId, specific collision values for female, hp/mp/cp tables, etc.<br>
 * <br>
 * Since each PlayerTemplate is associated to a {@link ClassId}, it is also used as a container for {@link GeneralSkillNode}s this class can use.<br>
 * <br>
 * Finally, it holds starter equipment (under an int array of itemId) and initial spawn {@link Location} for newbie templates.
 */
public class PlayerTemplate extends CreatureTemplate
{
	private final ClassId _classId;
	
	private final int _fallingHeight;
	
	private final int _baseSwimSpd;
	
	private final double _collisionRadiusFemale;
	private final double _collisionHeightFemale;
	
	private final Location _spawn;
	private final int _spawnX;
	private final int _spawnY;
	private final int _spawnZ;
	
	private final int _classBaseLevel;
	
	private final double[] _hpTable;
	private final double[] _mpTable;
	private final double[] _cpTable;
	
	private final int[] _items;
	private final List<GeneralSkillNode> _skills;
	
	private final Weapon _fists;
	
	public PlayerTemplate(StatsSet set)
	{
		super(set);
		
		_classId = ClassId.VALUES[set.getInteger("id")];
		
		_fallingHeight = set.getInteger("falling_height", 333);
		
		_baseSwimSpd = set.getInteger("swimSpd", 1);
		
		_collisionRadiusFemale = set.getDouble("radiusFemale");
		_collisionHeightFemale = set.getDouble("heightFemale");
		
		_spawnX = set.getInteger("spawnX");
		_spawnY = set.getInteger("spawnY");
		_spawnZ = set.getInteger("spawnZ");
		
		_spawn = new Location(set.getInteger("spawnX"), set.getInteger("spawnY"), set.getInteger("spawnZ"));
		
		_classBaseLevel = set.getInteger("baseLvl");
		
		_hpTable = set.getDoubleArray("hpTable");
		_mpTable = set.getDoubleArray("mpTable");
		_cpTable = set.getDoubleArray("cpTable");
		
		_items = set.getIntegerArray("items", ArraysUtil.EMPTY_INT_ARRAY);
		
		_skills = set.getList("skills");
		
		_fists = (Weapon) ItemTable.getInstance().getTemplate(set.getInteger("fists"));
	}
	
	public final ClassId getClassId()
	{
		return _classId;
	}
	
	public final ClassRace getRace()
	{
		return _classId.getRace();
	}
	
	public final String getClassName()
	{
		return _classId.toString();
	}
	
	public final int getFallHeight()
	{
		return _fallingHeight;
	}
	
	public final int getBaseSwimSpeed()
	{
		return _baseSwimSpd;
	}
	
	/**
	 * @param sex
	 * @return : height depends on sex.
	 */
	public double getCollisionRadiusBySex(Sex sex)
	{
		return (sex == Sex.MALE) ? _collisionRadius : _collisionRadiusFemale;
	}
	
	/**
	 * @param sex
	 * @return : height depends on sex.
	 */
	public double getCollisionHeightBySex(Sex sex)
	{
		return (sex == Sex.MALE) ? _collisionHeight : _collisionHeightFemale;
	}
	
	public final Location getSpawn()
	{
		return _spawn;
	}
	
	public final int getClassBaseLevel()
	{
		return _classBaseLevel;
	}
	
	@Override
	public final double getBaseHpMax(int level)
	{
		return _hpTable[level - 1];
	}
	
	@Override
	public final double getBaseMpMax(int level)
	{
		return _mpTable[level - 1];
	}
	
	public final double getBaseCpMax(int level)
	{
		return _cpTable[level - 1];
	}
	
	/**
	 * @return the itemIds of all the starter equipment under an integer array.
	 */
	public final int[] getItemIds()
	{
		return _items;
	}
	
	/**
	 * @return the {@link List} of all available {@link GeneralSkillNode} for this {@link PlayerTemplate}.
	 */
	public final List<GeneralSkillNode> getSkills()
	{
		return _skills;
	}
	
	/**
	 * Find if the skill exists on skill tree.
	 * @param id : The skill id to check.
	 * @param level : The skill level to check.
	 * @return the associated {@link GeneralSkillNode} if a matching id/level is found on this {@link PlayerTemplate}, or null.
	 */
	public GeneralSkillNode findSkill(int id, int level)
	{
		return _skills.stream().filter(s -> s.getId() == id && s.getValue() == level).findFirst().orElse(null);
	}
	
	/**
	 * @return the {@link Weapon} used as fists for this {@link PlayerTemplate}.
	 */
	public final Weapon getFists()
	{
		return _fists;
	}

	/**
	 * @return
	 */
	public int getSpawnX()
	{
		return _spawnX;
	}
	
	public int getSpawnY()
	{
		return _spawnY;
	}
	
	public int getSpawnZ()
	{
		return _spawnZ;
	}
}