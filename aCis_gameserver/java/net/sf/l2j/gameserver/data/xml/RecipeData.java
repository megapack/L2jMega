package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.gameserver.model.item.Recipe;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class loads and stores {@link Recipe}s. Recipes are part of craft system, which uses a Recipe associated to items (materials) to craft another item (product).
 */
public class RecipeData extends XMLDocument
{
	private final Map<Integer, Recipe> _recipes = new HashMap<>();
	
	protected RecipeData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/recipes.xml");
		LOGGER.info("Loaded {} recipes.", _recipes.size());
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
			if (!"recipe".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// Parse and feed content.
			parseAndFeed(o.getAttributes(), set);
			
			_recipes.put(set.getInteger("id"), new Recipe(set));
			
			// Clear the StatsSet.
			set.clear();
		}
	}
	
	public Recipe getRecipeList(int listId)
	{
		return _recipes.get(listId);
	}
	
	public Recipe getRecipeByItemId(int itemId)
	{
		return _recipes.values().stream().filter(r -> r.getRecipeId() == itemId).findFirst().orElse(null);
	}
	
	public static RecipeData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RecipeData INSTANCE = new RecipeData();
	}
}