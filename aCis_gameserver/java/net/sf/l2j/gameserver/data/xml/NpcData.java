package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.MinionData;
import net.sf.l2j.gameserver.model.PetDataEntry;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.actor.template.PetTemplate;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Loads and stores {@link NpcTemplate}s.
 */
public class NpcData extends XMLDocument
{
	private final Map<Integer, NpcTemplate> _npcs = new HashMap<>();
	
	protected NpcData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/npcs");
		LOGGER.info("Loaded {} NPC templates.", _npcs.size());
	}
	
	@Override
	protected void parseDocument(Document doc, File file)
	{
		// StatsSet used to feed informations. Cleaned on every entry.
		final StatsSet set = new StatsSet();
		final StatsSet petSet = new StatsSet();
		
		// First element is never read.
		Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if (!"npc".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			NamedNodeMap attrs = o.getAttributes();
			
			// Used to define template type.
			boolean mustUsePetTemplate = false;
			
			final int npcId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
			final int templateId = attrs.getNamedItem("idTemplate") == null ? npcId : Integer.parseInt(attrs.getNamedItem("idTemplate").getNodeValue());
			
			set.set("id", npcId);
			set.set("idTemplate", templateId);
			set.set("name", attrs.getNamedItem("name").getNodeValue());
			set.set("title", attrs.getNamedItem("title").getNodeValue());
			
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if ("ai".equalsIgnoreCase(d.getNodeName()))
				{
					attrs = d.getAttributes();
					
					set.set("aiType", attrs.getNamedItem("type").getNodeValue());
					set.set("ssCount", Integer.parseInt(attrs.getNamedItem("ssCount").getNodeValue()));
					set.set("ssRate", Integer.parseInt(attrs.getNamedItem("ssRate").getNodeValue()));
					set.set("spsCount", Integer.parseInt(attrs.getNamedItem("spsCount").getNodeValue()));
					set.set("spsRate", Integer.parseInt(attrs.getNamedItem("spsRate").getNodeValue()));
					set.set("aggro", Integer.parseInt(attrs.getNamedItem("aggro").getNodeValue()));
					
					// Verify if the parameter exists.
					if (attrs.getNamedItem("clan") != null)
					{
						set.set("clan", attrs.getNamedItem("clan").getNodeValue().split(";"));
						set.set("clanRange", Integer.parseInt(attrs.getNamedItem("clanRange").getNodeValue()));
						
						// Verify if the parameter exists.
						if (attrs.getNamedItem("ignoredIds") != null)
							set.set("ignoredIds", attrs.getNamedItem("ignoredIds").getNodeValue());
					}
					
					set.set("canMove", Boolean.parseBoolean(attrs.getNamedItem("canMove").getNodeValue()));
					set.set("seedable", Boolean.parseBoolean(attrs.getNamedItem("seedable").getNodeValue()));
				}
				else if ("drops".equalsIgnoreCase(d.getNodeName()))
				{
					final String type = set.getString("type");
					final boolean isRaid = type.equalsIgnoreCase("L2RaidBoss") || type.equalsIgnoreCase("L2GrandBoss");
					
					final List<DropCategory> drops = new ArrayList<>();
					
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if (!"category".equalsIgnoreCase(e.getNodeName()))
							continue;
						
						attrs = e.getAttributes();
						
						final DropCategory category = new DropCategory(Integer.parseInt(attrs.getNamedItem("id").getNodeValue()));
						
						for (Node m = e.getFirstChild(); m != null; m = m.getNextSibling())
						{
							if (!"drop".equalsIgnoreCase(m.getNodeName()))
								continue;
							
							attrs = m.getAttributes();
							
							final DropData data = new DropData();
							data.setItemId(Integer.parseInt(attrs.getNamedItem("itemid").getNodeValue()));
							data.setMinDrop(Integer.parseInt(attrs.getNamedItem("min").getNodeValue()));
							data.setMaxDrop(Integer.parseInt(attrs.getNamedItem("max").getNodeValue()));
							data.setChance(Integer.parseInt(attrs.getNamedItem("chance").getNodeValue()));
							
							if (ItemTable.getInstance().getTemplate(data.getItemId()) == null)
							{
								LOGGER.warn("Droplist data for undefined itemId: {}.", data.getItemId());
								continue;
							}
							category.addDropData(data, isRaid);
						}
						drops.add(category);
					}
					set.set("drops", drops);
				}
				else if ("minions".equalsIgnoreCase(d.getNodeName()))
				{
					final List<MinionData> minions = new ArrayList<>();
					
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if (!"minion".equalsIgnoreCase(e.getNodeName()))
							continue;
						
						attrs = e.getAttributes();
						
						final MinionData data = new MinionData();
						data.setMinionId(Integer.parseInt(attrs.getNamedItem("id").getNodeValue()));
						data.setAmountMin(Integer.parseInt(attrs.getNamedItem("min").getNodeValue()));
						data.setAmountMax(Integer.parseInt(attrs.getNamedItem("max").getNodeValue()));
						
						minions.add(data);
					}
					set.set("minions", minions);
				}
				else if ("petdata".equalsIgnoreCase(d.getNodeName()))
				{
					mustUsePetTemplate = true;
					
					attrs = d.getAttributes();
					
					set.set("food1", Integer.parseInt(attrs.getNamedItem("food1").getNodeValue()));
					set.set("food2", Integer.parseInt(attrs.getNamedItem("food2").getNodeValue()));
					
					set.set("autoFeedLimit", Double.parseDouble(attrs.getNamedItem("autoFeedLimit").getNodeValue()));
					set.set("hungryLimit", Double.parseDouble(attrs.getNamedItem("hungryLimit").getNodeValue()));
					set.set("unsummonLimit", Double.parseDouble(attrs.getNamedItem("unsummonLimit").getNodeValue()));
					
					final Map<Integer, PetDataEntry> entries = new HashMap<>();
					
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if (!"stat".equalsIgnoreCase(e.getNodeName()))
							continue;
						
						attrs = e.getAttributes();
						
						// Get all nodes.
						for (int i = 0; i < attrs.getLength(); i++)
						{
							// Add them to stats by node name and node value.
							Node node = attrs.item(i);
							petSet.set(node.getNodeName(), node.getNodeValue());
						}
						
						entries.put(petSet.getInteger("level"), new PetDataEntry(petSet));
						petSet.clear();
					}
					set.set("petData", entries);
				}
				else if ("set".equalsIgnoreCase(d.getNodeName()))
				{
					attrs = d.getAttributes();
					
					set.set(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("val").getNodeValue());
				}
				else if ("skills".equalsIgnoreCase(d.getNodeName()))
				{
					final List<L2Skill> skills = new ArrayList<>();
					
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if (!"skill".equalsIgnoreCase(e.getNodeName()))
							continue;
						
						attrs = e.getAttributes();
						
						final int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
						final int level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
						
						// Setup the npc's race. Don't register the skill.
						if (skillId == L2Skill.SKILL_NPC_RACE)
						{
							set.set("raceId", level);
							continue;
						}
						
						final L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
						if (skill == null)
							continue;
						
						skills.add(skill);
					}
					set.set("skills", skills);
				}
				else if ("teachTo".equalsIgnoreCase(d.getNodeName()))
					set.set("teachTo", d.getAttributes().getNamedItem("classes").getNodeValue());
			}
			
			_npcs.put(npcId, (mustUsePetTemplate) ? new PetTemplate(set) : new NpcTemplate(set));
			
			set.clear();
		}
	}
	
	public void reload()
	{
		_npcs.clear();
		
		load();
	}
	
	public NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}
	
	/**
	 * @param name : The name of the NPC to search.
	 * @return the {@link NpcTemplate} for a given name.
	 */
	public NpcTemplate getTemplateByName(String name)
	{
		return _npcs.values().stream().filter(t -> t.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
	/**
	 * Gets all {@link NpcTemplate}s matching the filter.
	 * @param filter : The Predicate filter used as a filter.
	 * @return a NpcTemplate list matching the given filter.
	 */
	public List<NpcTemplate> getTemplates(Predicate<NpcTemplate> filter)
	{
		return _npcs.values().stream().filter(filter).collect(Collectors.toList());
	}
	
	public Collection<NpcTemplate> getAllNpcs()
	{
		return _npcs.values();
	}
	
	public static NpcData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcData INSTANCE = new NpcData();
	}
}