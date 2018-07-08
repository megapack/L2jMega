package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.soulcrystal.LevelingInfo;
import net.sf.l2j.gameserver.model.soulcrystal.SoulCrystal;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and stores following Soul Crystal infos :
 * <ul>
 * <li>{@link SoulCrystal} infos related to items (such as level, initial / broken / succeeded itemId) ;</li>
 * <li>{@link LevelingInfo} infos related to NPCs (such as absorb type, chances of fail/success, if the item cast needs to be done and the list of allowed crystal levels).</li>
 * </ul>
 */
public class SoulCrystalData extends XMLDocument
{
	private final Map<Integer, SoulCrystal> _soulCrystals = new HashMap<>();
	private final Map<Integer, LevelingInfo> _levelingInfos = new HashMap<>();
	
	protected SoulCrystalData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/soulCrystals.xml");
		LOGGER.info("Loaded {} Soul Crystals data and {} NPCs data.", _soulCrystals.size(), _levelingInfos.size());
	}
	
	@Override
	protected void parseDocument(Document doc, File file)
	{
		// StatsSet used to feed informations. Cleaned on every entry.
		final StatsSet set = new StatsSet();
		
		// First element is never read.
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if ("crystals".equalsIgnoreCase(o.getNodeName()))
			{
				for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (!"crystal".equalsIgnoreCase(d.getNodeName()))
						continue;
					
					// Parse and feed content.
					parseAndFeed(d.getAttributes(), set);
					
					// Feed the map with new data.
					_soulCrystals.put(set.getInteger("initial"), new SoulCrystal(set));
					
					// Clear the StatsSet.
					set.clear();
				}
			}
			else if ("npcs".equalsIgnoreCase(o.getNodeName()))
			{
				for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (!"npc".equalsIgnoreCase(d.getNodeName()))
						continue;
					
					// Parse and feed content.
					parseAndFeed(d.getAttributes(), set);
					
					// Feed the map with new data.
					_levelingInfos.put(set.getInteger("id"), new LevelingInfo(set));
					
					// Clear the StatsSet.
					set.clear();
				}
			}
		}
	}
	
	public final Map<Integer, SoulCrystal> getSoulCrystals()
	{
		return _soulCrystals;
	}
	
	public final Map<Integer, LevelingInfo> getLevelingInfos()
	{
		return _levelingInfos;
	}
	
	public static SoulCrystalData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SoulCrystalData INSTANCE = new SoulCrystalData();
	}
}