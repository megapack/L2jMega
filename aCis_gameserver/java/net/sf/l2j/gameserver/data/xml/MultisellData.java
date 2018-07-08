package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;
import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.multisell.Entry;
import net.sf.l2j.gameserver.model.multisell.Ingredient;
import net.sf.l2j.gameserver.model.multisell.ListContainer;
import net.sf.l2j.gameserver.model.multisell.PreparedListContainer;
import net.sf.l2j.gameserver.network.serverpackets.MultiSellList;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and stores multisell lists under {@link ListContainer}.<br>
 * Each ListContainer contains a List of {@link Entry}, and the list of allowed npcIds.<br>
 * <br>
 * File name is used as key, under its String hashCode.
 */
public class MultisellData extends XMLDocument
{
	public static final int PAGE_SIZE = 40;
	
	private final Map<Integer, ListContainer> _entries = new HashMap<>();
	
	public MultisellData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/multisell");
		LOGGER.info("Loaded {} multisell.", _entries.size());
	}
	
	@Override
	protected void parseDocument(Document doc, File file)
	{
		// StatsSet used to feed informations. Cleaned on every entry.
		final StatsSet set = new StatsSet();
		
		int entryId = 1;
		
		final int id = file.getName().replaceAll(".xml", "").hashCode();
		final ListContainer list = new ListContainer(id);
		
		for (Node o = doc.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if (!"list".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			Node att = o.getAttributes().getNamedItem("applyTaxes");
			list.setApplyTaxes(att != null && Boolean.parseBoolean(att.getNodeValue()));
			
			att = o.getAttributes().getNamedItem("maintainEnchantment");
			list.setMaintainEnchantment(att != null && Boolean.parseBoolean(att.getNodeValue()));
			
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if ("item".equalsIgnoreCase(d.getNodeName()))
				{
					final Entry entry = new Entry(entryId++);
					
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if ("ingredient".equalsIgnoreCase(e.getNodeName()))
						{
							// Parse and feed content.
							parseAndFeed(e.getAttributes(), set);
							
							// Feed entry with a new ingredient.
							entry.addIngredient(new Ingredient(set));
							
							// Clear the StatsSet.
							set.clear();
						}
						else if ("production".equalsIgnoreCase(e.getNodeName()))
						{
							// Parse and feed content.
							parseAndFeed(e.getAttributes(), set);
							
							// Feed entry with a new product.
							entry.addProduct(new Ingredient(set));
							
							// Clear the StatsSet.
							set.clear();
						}
					}
					
					list.getEntries().add(entry);
				}
				else if ("npcs".equalsIgnoreCase(d.getNodeName()))
				{
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if ("npc".equalsIgnoreCase(e.getNodeName()))
						{
							if (StringUtil.isDigit(e.getTextContent()))
								list.allowNpc(Integer.parseInt(e.getTextContent()));
						}
					}
				}
			}
			
			_entries.put(id, list);
		}
	}
	
	public void reload()
	{
		_entries.clear();
		
		load();
	}
	
	/**
	 * Send the correct multisell content to a {@link Player}.<br>
	 * <br>
	 * {@link ListContainer} template is first retrieved, based on its name, then {@link Npc} npcId check is done for security reason. Then the content is sent into {@link PreparedListContainer}, notably to check Player inventory. Finally a {@link MultiSellList} packet is sent to the Player. That
	 * new, prepared list is kept in memory on Player instance, mostly for memory reason.
	 * @param listName : The ListContainer list name.
	 * @param player : The Player to check.
	 * @param npc : The Npc to check (notably used for npcId check).
	 * @param inventoryOnly : if true we check inventory content.
	 */
	public void separateAndSend(String listName, Player player, Npc npc, boolean inventoryOnly)
	{
		final ListContainer template = _entries.get(listName.hashCode());
		if (template == null)
			return;
		
		if ((npc != null && !template.isNpcAllowed(npc.getNpcId())) || (npc == null && template.isNpcOnly()))
			return;
		
		final PreparedListContainer list = new PreparedListContainer(template, inventoryOnly, player, npc);
		
		int index = 0;
		do
		{
			// send list at least once even if size = 0
			player.sendPacket(new MultiSellList(list, index));
			index += PAGE_SIZE;
		}
		while (index < list.getEntries().size());
		
		player.setMultiSell(list);
	}
	
	public ListContainer getList(String listName)
	{
		return _entries.get(listName.hashCode());
	}
	
	public static MultisellData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MultisellData INSTANCE = new MultisellData();
	}
}