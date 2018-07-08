package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class loads herbs drop rules.<br>
 * TODO parse L2OFF GF (since IL doesn't exist) and introduce the additional droplist concept directly on npc data XMLs.
 */
public class HerbDropData extends XMLDocument
{
	private final Map<Integer, List<DropCategory>> _herbGroups = new HashMap<>();
	
	protected HerbDropData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/herbDrops.xml");
		LOGGER.info("Loaded {} herbs groups.", _herbGroups.size());
	}
	
	@Override
	protected void parseDocument(Document doc, File file)
	{
		// First element is never read.
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if (!"group".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			NamedNodeMap attrs = o.getAttributes();
			
			final int groupId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
			final List<DropCategory> category = _herbGroups.computeIfAbsent(groupId, (k) -> new ArrayList<>());
			
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (!"item".equalsIgnoreCase(d.getNodeName()))
					continue;
				
				attrs = d.getAttributes();
				
				final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				final int categoryType = Integer.parseInt(attrs.getNamedItem("category").getNodeValue());
				final int chance = Integer.parseInt(attrs.getNamedItem("chance").getNodeValue());
				
				final DropData dropDat = new DropData();
				dropDat.setItemId(id);
				dropDat.setMinDrop(1);
				dropDat.setMaxDrop(1);
				dropDat.setChance(chance);
				
				boolean catExists = false;
				for (DropCategory cat : category)
				{
					// if the category exists, add the drop to this category.
					if (cat.getCategoryType() == categoryType)
					{
						cat.addDropData(dropDat, false);
						catExists = true;
						break;
					}
				}
				
				// if the category doesn't exit, create it and add the drop
				if (!catExists)
				{
					DropCategory cat = new DropCategory(categoryType);
					cat.addDropData(dropDat, false);
					category.add(cat);
				}
			}
		}
	}
	
	public List<DropCategory> getHerbDroplist(int groupId)
	{
		return _herbGroups.get(groupId);
	}
	
	public static HerbDropData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HerbDropData INSTANCE = new HerbDropData();
	}
}