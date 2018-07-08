package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.NewbieBuff;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and store {@link NewbieBuff} into a List.
 */
public class NewbieBuffData extends XMLDocument
{
	private final List<NewbieBuff> _buffs = new ArrayList<>();
	
	private int _magicLowestLevel = 100;
	private int _physicLowestLevel = 100;
	
	private int _magicHighestLevel = 1;
	private int _physicHighestLevel = 1;
	
	protected NewbieBuffData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/newbieBuffs.xml");
		LOGGER.info("Loaded {} newbie buffs.", _buffs.size());
	}
	
	@Override
	protected void parseDocument(Document doc, File f)
	{
		// StatsSet used to feed informations. Cleaned on every entry.
		final StatsSet set = new StatsSet();
		
		// First element is never read.
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if (!"buff".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// Parse and feed content.
			parseAndFeed(o.getAttributes(), set);
			
			final int lowerLevel = set.getInteger("lowerLevel");
			final int upperLevel = set.getInteger("upperLevel");
			
			if (set.getBool("isMagicClass"))
			{
				if (lowerLevel < _magicLowestLevel)
					_magicLowestLevel = lowerLevel;
				
				if (upperLevel > _magicHighestLevel)
					_magicHighestLevel = upperLevel;
			}
			else
			{
				if (lowerLevel < _physicLowestLevel)
					_physicLowestLevel = lowerLevel;
				
				if (upperLevel > _physicHighestLevel)
					_physicHighestLevel = upperLevel;
			}
			
			// Feed the list with new data.
			_buffs.add(new NewbieBuff(set));
			
			// Clear the StatsSet.
			set.clear();
		}
	}
	
	/**
	 * @return the Helper Buff List
	 */
	public List<NewbieBuff> getBuffs()
	{
		return _buffs;
	}
	
	/**
	 * @return Returns the magicHighestLevel.
	 */
	public int getMagicHighestLevel()
	{
		return _magicHighestLevel;
	}
	
	/**
	 * @return Returns the magicLowestLevel.
	 */
	public int getMagicLowestLevel()
	{
		return _magicLowestLevel;
	}
	
	/**
	 * @return Returns the physicHighestLevel.
	 */
	public int getPhysicHighestLevel()
	{
		return _physicHighestLevel;
	}
	
	/**
	 * @return Returns the physicLowestLevel.
	 */
	public int getPhysicLowestLevel()
	{
		return _physicLowestLevel;
	}
	
	public static NewbieBuffData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final NewbieBuffData INSTANCE = new NewbieBuffData();
	}
}