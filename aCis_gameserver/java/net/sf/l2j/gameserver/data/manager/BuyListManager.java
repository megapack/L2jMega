package net.sf.l2j.gameserver.data.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.buylist.Product;
import net.sf.l2j.gameserver.taskmanager.BuyListTaskManager;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Loads and stores {@link NpcBuyList}, which is the most common way to show/sell items, with multisell.<br>
 * <br>
 * NpcBuyList owns a list of {@link Product}. Each of them can have a count, making the item acquisition impossible until the next restock timer (stored as SQL data). The count timer is stored on a global task, called {@link BuyListTaskManager}.
 */
public class BuyListManager extends XMLDocument
{
	private final Map<Integer, NpcBuyList> _buyLists = new HashMap<>();
	
	protected BuyListManager()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/buyLists.xml");
		LOGGER.info("Loaded {} buyLists.", _buyLists.size());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM `buylists`");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final int buyListId = rs.getInt("buylist_id");
				final int itemId = rs.getInt("item_id");
				final int count = rs.getInt("count");
				final long nextRestockTime = rs.getLong("next_restock_time");
				
				final NpcBuyList buyList = _buyLists.get(buyListId);
				if (buyList == null)
					continue;
				
				final Product product = buyList.getProductByItemId(itemId);
				if (product == null)
					continue;
				
				BuyListTaskManager.getInstance().test(product, count, nextRestockTime);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to load buyList data from database.", e);
		}
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
			if (!"buyList".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// Setup a new BuyList.
			final int buyListId = Integer.parseInt(o.getAttributes().getNamedItem("id").getNodeValue());
			final NpcBuyList buyList = new NpcBuyList(buyListId);
			buyList.setNpcId(Integer.parseInt(o.getAttributes().getNamedItem("npcId").getNodeValue()));
			
			// Read products and feed the BuyList with it.
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (!"product".equalsIgnoreCase(d.getNodeName()))
					continue;
				
				// Parse and feed content.
				parseAndFeed(d.getAttributes(), set);
				
				// Feed the list with new data.
				buyList.addProduct(new Product(buyListId, set));
				
				// Clear the StatsSet.
				set.clear();
			}
			_buyLists.put(buyListId, buyList);
		}
	}
	
	public NpcBuyList getBuyList(int listId)
	{
		return _buyLists.get(listId);
	}
	
	public List<NpcBuyList> getBuyListsByNpcId(int npcId)
	{
		return _buyLists.values().stream().filter(b -> b.isNpcAllowed(npcId)).collect(Collectors.toList());
	}
	
	public static BuyListManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BuyListManager INSTANCE = new BuyListManager();
	}
}