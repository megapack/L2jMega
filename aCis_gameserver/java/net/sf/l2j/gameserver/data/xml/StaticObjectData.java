package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.StaticObject;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads, stores and spawns {@link StaticObject}s.
 */
public class StaticObjectData extends XMLDocument
{
	private final Map<Integer, StaticObject> _objects = new HashMap<>();
	
	protected StaticObjectData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/staticObjects.xml");
		LOGGER.info("Loaded {} static objects.", _objects.size());
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
			if (!"object".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// Parse and feed content.
			parseAndFeed(o.getAttributes(), set);
			
			// Create and spawn the StaticObject instance.
			final StaticObject obj = new StaticObject(IdFactory.getInstance().getNextId());
			obj.setStaticObjectId(set.getInteger("id"));
			obj.setType(set.getInteger("type"));
			obj.setMap(set.getString("texture"), set.getInteger("mapX"), set.getInteger("mapY"));
			obj.spawnMe(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"));
			
			// Feed the map with new data.
			_objects.put(obj.getObjectId(), obj);
			
			// Clear the StatsSet.
			set.clear();
		}
	}
	
	public Collection<StaticObject> getStaticObjects()
	{
		return _objects.values();
	}
	
	public static StaticObjectData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final StaticObjectData INSTANCE = new StaticObjectData();
	}
}