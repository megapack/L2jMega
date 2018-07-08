package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class loads and stores summon items.<br>
 * TODO Delete it and move it back wherever it belongs.
 */
public class SummonItemData extends XMLDocument
{
	private final Map<Integer, IntIntHolder> _items = new HashMap<>();
	
	protected SummonItemData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/summonItems.xml");
		LOGGER.info("Loaded {} summon items.", _items.size());
	}
	
	@Override
	protected void parseDocument(Document doc, File file)
	{
		// First element is never read.
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if (!"item".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			final NamedNodeMap attrs = o.getAttributes();
			
			final int itemId = Integer.valueOf(attrs.getNamedItem("id").getNodeValue());
			final int npcId = Integer.valueOf(attrs.getNamedItem("npcId").getNodeValue());
			final int summonType = Integer.valueOf(attrs.getNamedItem("summonType").getNodeValue());
			
			_items.put(itemId, new IntIntHolder(npcId, summonType));
		}
	}
	
	public IntIntHolder getSummonItem(int itemId)
	{
		return _items.get(itemId);
	}
	
	public static SummonItemData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonItemData INSTANCE = new SummonItemData();
	}
}