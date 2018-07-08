package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.l2j.commons.data.xml.XMLDocument;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.Fish;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and stores {@link Fish} infos.<br>
 * TODO Plain wrong values and system, have to be reworked entirely.
 */
public class FishData extends XMLDocument
{
	private final List<Fish> _fish = new ArrayList<>();
	
	protected FishData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/fish.xml");
		LOGGER.info("Loaded {} fish.", _fish.size());
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
			if (!"fish".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// Parse and feed content.
			parseAndFeed(o.getAttributes(), set);
			
			// Feed the list with new data.
			_fish.add(new Fish(set));
			
			// Clear the StatsSet.
			set.clear();
		}
	}
	
	/**
	 * Get a random {@link Fish} based on level, type and group.
	 * @param lvl : the fish level to check.
	 * @param type : the fish type to check.
	 * @param group : the fish group to check.
	 * @return a Fish with good criterias.
	 */
	public Fish getFish(int lvl, int type, int group)
	{
		return Rnd.get(_fish.stream().filter(f -> f.getLevel() == lvl && f.getType() == type && f.getGroup() == group).collect(Collectors.toList()));
	}
	
	public static FishData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FishData INSTANCE = new FishData();
	}
}