package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.Henna;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and stores {@link Henna}s infos. Hennas are called "dye" ingame.
 */
public class HennaData extends XMLDocument
{
	private final Map<Integer, Henna> _hennas = new HashMap<>();
	
	protected HennaData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/hennas.xml");
		LOGGER.info("Loaded {} hennas.", _hennas.size());
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
			if (!"henna".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// Parse and feed content.
			parseAndFeed(o.getAttributes(), set);
			
			// Feed the map with new data.
			_hennas.put(set.getInteger("symbolId"), new Henna(set));
			
			// Clear the StatsSet.
			set.clear();
		}
	}
	
	public Henna getHenna(int id)
	{
		return _hennas.get(id);
	}
	
	/**
	 * Retrieve all {@link Henna}s available for a {@link Player} class.
	 * @param player : The Player used as class parameter.
	 * @return a List of all available Hennas for this Player.
	 */
	public List<Henna> getAvailableHennasFor(Player player)
	{
		return _hennas.values().stream().filter(h -> h.canBeUsedBy(player)).collect(Collectors.toList());
	}
	
	public static HennaData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HennaData INSTANCE = new HennaData();
	}
}