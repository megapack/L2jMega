package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.location.WalkerLocation;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and stores routes for Walker NPCs, under a List of {@link WalkerLocation} ; the key being the npcId.
 */
public class WalkerRouteData extends XMLDocument
{
	private final Map<Integer, List<WalkerLocation>> _routes = new HashMap<>();
	
	protected WalkerRouteData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/walkerRoutes.xml");
		LOGGER.info("Loaded {} Walker routes.", _routes.size());
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
			if (!"route".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// We enforce the use of LinkedList.
			final List<WalkerLocation> list = new ArrayList<>();
			
			int npcId = Integer.parseInt(o.getAttributes().getNamedItem("npcId").getNodeValue());
			boolean run = Boolean.parseBoolean(o.getAttributes().getNamedItem("run").getNodeValue());
			
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (!"node".equalsIgnoreCase(d.getNodeName()))
					continue;
				
				// Parse and feed content.
				parseAndFeed(d.getAttributes(), set);
				
				// Feed the list with new data.
				list.add(new WalkerLocation(set, run));
				
				// Clear the StatsSet.
				set.clear();
			}
			_routes.put(npcId, list);
		}
	}
	
	public void reload()
	{
		_routes.clear();
		
		load();
	}
	
	public List<WalkerLocation> getWalkerRoute(int npcId)
	{
		return _routes.get(npcId);
	}
	
	public static WalkerRouteData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final WalkerRouteData INSTANCE = new WalkerRouteData();
	}
}