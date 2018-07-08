package net.sf.l2j.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.List;

/**
 * A datatype which is part of multisell system. A multisell list can hold multiple Products.<br>
 * Each Product owns a List of "required part(s)" and "result(s)" known both as {@link Ingredient}s.
 */
public class Entry
{
	protected int _id;
	protected boolean _stackable = true;
	
	protected List<Ingredient> _products;
	protected List<Ingredient> _ingredients;
	
	public Entry(int id)
	{
		_id = id;
		_products = new ArrayList<>();
		_ingredients = new ArrayList<>();
	}
	
	/**
	 * This constructor used in PreparedEntry only, ArrayLists not created.
	 */
	protected Entry()
	{
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public void addProduct(Ingredient product)
	{
		_products.add(product);
		
		if (!product.isStackable())
			_stackable = false;
	}
	
	public List<Ingredient> getProducts()
	{
		return _products;
	}
	
	public void addIngredient(Ingredient ingredient)
	{
		_ingredients.add(ingredient);
	}
	
	public List<Ingredient> getIngredients()
	{
		return _ingredients;
	}
	
	public boolean isStackable()
	{
		return _stackable;
	}
	
	public int getTaxAmount()
	{
		return 0;
	}
}