package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and stores {@link PlayerTemplate}s. It also feed their skill trees.
 */
public class PlayerData extends XMLDocument
{
	private final Map<Integer, PlayerTemplate> _templates = new HashMap<>();
	
	protected PlayerData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/classes");
		LOGGER.info("Loaded {} player classes templates.", _templates.size());
		
		// We add parent skills, if existing.
		for (PlayerTemplate template : _templates.values())
		{
			final ClassId parentClassId = template.getClassId().getParent();
			if (parentClassId != null)
				template.getSkills().addAll(_templates.get(parentClassId.getId()).getSkills());
		}
	}
	
	@Override
	protected void parseDocument(Document doc, File f)
	{
		// StatsSet used to feed informations. Cleaned on every entry.
		final StatsSet set = new StatsSet();
		final StatsSet skillSet = new StatsSet();
		
		// First element is never read.
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if (!"class".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if ("set".equalsIgnoreCase(d.getNodeName()))
				{
					// Parse and feed content.
					parseAndFeed(d.getAttributes(), set);
				}
				else if ("skills".equalsIgnoreCase(d.getNodeName()))
				{
					// The list used to feed skills tree of this player template class.
					final List<GeneralSkillNode> skills = new ArrayList<>();
					
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if (!"skill".equalsIgnoreCase(e.getNodeName()))
							continue;
						
						// Parse and feed content.
						parseAndFeed(e.getAttributes(), skillSet);
						
						// Feed the map with new data.
						skills.add(new GeneralSkillNode(skillSet));
						
						// Clear the StatsSet.
						skillSet.clear();
					}
					
					// Feed the global StatsSet with skills list.
					set.set("skills", skills);
				}
			}
			
			// Feed the map with new data.
			_templates.put(set.getInteger("id"), new PlayerTemplate(set));
			
			// Clear the StatsSet.
			set.clear();
		}
	}
	
	public PlayerTemplate getTemplate(ClassId classId)
	{
		return _templates.get(classId.getId());
	}
	
	public PlayerTemplate getTemplate(int classId)
	{
		return _templates.get(classId);
	}
	
	public final String getClassNameById(int classId)
	{
		final PlayerTemplate template = _templates.get(classId);
		return (template != null) ? template.getClassName() : "Invalid class";
	}
	
	public static PlayerData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PlayerData INSTANCE = new PlayerData();
	}
	
	  private static final String[] CHAR_CLASSES = { "Human Fighter", "Warrior", "Gladiator", "Warlord", "Human Knight", "Paladin", "Dark Avenger", "Rogue", "Treasure Hunter", "Hawkeye", "Human Mystic", "Human Wizard", "Sorceror", "Necromancer", "Warlock", "Cleric", "Bishop", "Prophet", "Elven Fighter", "Elven Knight", "Temple Knight", "Swordsinger", "Elven Scout", "Plainswalker", "Silver Ranger", "Elven Mystic", "Elven Wizard", "Spellsinger", "Elemental Summoner", "Elven Oracle", "Elven Elder", "Dark Fighter", "Palus Knight", "Shillien Knight", "Bladedancer", "Assassin", "Abyss Walker", "Phantom Ranger", "Dark Elven Mystic", "Dark Elven Wizard", "Spellhowler", "Phantom Summoner", "Shillien Oracle", "Shillien Elder", "Orc Fighter", "Orc Raider", "Destroyer", "Orc Monk", "Tyrant", "Orc Mystic", "Orc Shaman", "Overlord", "Warcryer", "Dwarven Fighter", "Dwarven Scavenger", "Bounty Hunter", "Dwarven Artisan", "Warsmith", "dummyEntry1", "dummyEntry2", "dummyEntry3", "dummyEntry4", "dummyEntry5", "dummyEntry6", "dummyEntry7", "dummyEntry8", "dummyEntry9", "dummyEntry10", "dummyEntry11", "dummyEntry12", "dummyEntry13", "dummyEntry14", "dummyEntry15", "dummyEntry16", "dummyEntry17", "dummyEntry18", "dummyEntry19", "dummyEntry20", "dummyEntry21", "dummyEntry22", "dummyEntry23", "dummyEntry24", "dummyEntry25", "dummyEntry26", "dummyEntry27", "dummyEntry28", "dummyEntry29", "dummyEntry30", "Duelist", "DreadNought", "Phoenix Knight", "Hell Knight", "Sagittarius", "Adventurer", "Archmage", "Soultaker", "Arcana Lord", "Cardinal", "Hierophant", "Eva Templar", "Sword Muse", "Wind Rider", "Moonlight Sentinel", "Mystic Muse", "Elemental Master", "Eva's Saint", "Shillien Templar", "Spectral Dancer", "Ghost Hunter", "Ghost Sentinel", "Storm Screamer", "Spectral Master", "Shillien Saint", "Titan", "Grand Khauatari", "Dominator", "Doomcryer", "Fortune Seeker", "Maestro" };
	  
	  public static final int getClassIdByName(String className)
	  {
	    int currId = 1;
	    for (String name : CHAR_CLASSES)
	    {
	      if (name.equalsIgnoreCase(className)) {
	        break;
	      }
	      currId++;
	    }
	    return currId;
	  }
}