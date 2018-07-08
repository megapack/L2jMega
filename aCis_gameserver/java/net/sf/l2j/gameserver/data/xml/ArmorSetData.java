package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.item.ArmorSet;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and stores {@link ArmorSet}s, the key being the chest item id.
 */
public class ArmorSetData extends XMLDocument
{
	private final Map<Integer, ArmorSet> _armorSets = new HashMap<>();
	
	protected ArmorSetData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/armorSets.xml");
		LOGGER.info("Loaded {} armor sets.", _armorSets.size());
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
			if (!"armorset".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// Parse and feed content.
			parseAndFeed(o.getAttributes(), set);
			
			// Feed the map with new data.
			_armorSets.put(set.getInteger("chest"), new ArmorSet(set));
			
			// Clear the StatsSet.
			set.clear();
		}
	}
	
	public ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}
	
	public Collection<ArmorSet> getSets()
	{
		return _armorSets.values();
	}
	
	public static ArmorSetData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ArmorSetData INSTANCE = new ArmorSetData();
	}
}