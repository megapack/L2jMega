package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.location.TeleportLocation;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and stores {@link TeleportLocation}s.
 */
public class TeleportLocationData extends XMLDocument
{
	private final Map<Integer, TeleportLocation> _teleports = new HashMap<>();
	
	protected TeleportLocationData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/teleportLocations.xml");
		LOGGER.info("Loaded {} teleport locations.", _teleports.size());
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
			if (!"teleport".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// Parse and feed content.
			parseAndFeed(o.getAttributes(), set);
			
			// Feed the map with new data.
			_teleports.put(set.getInteger("id"), new TeleportLocation(set));
			
			// Clear the StatsSet.
			set.clear();
		}
	}
	
	public void reload()
	{
		_teleports.clear();
		
		load();
	}
	
	public TeleportLocation getTeleportLocation(int id)
	{
		return _teleports.get(id);
	}
	
	public static TeleportLocationData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TeleportLocationData INSTANCE = new TeleportLocationData();
	}
}