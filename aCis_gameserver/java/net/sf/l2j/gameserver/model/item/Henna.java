package net.sf.l2j.gameserver.model.item;

import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * A datatype used to retain Henna infos. Hennas are called "dye" ingame, and enhance {@link Player} stats for a fee.<br>
 * You can draw up to 3 hennas (depending about your current class rank), but accumulated boni for a stat can't be higher than +5. There is no limit in reduction.
 */
public final class Henna
{
	private final int _symbolId;
	private final int _dyeId;
	private final int _price;
	private final int _INT;
	private final int _STR;
	private final int _CON;
	private final int _MEN;
	private final int _DEX;
	private final int _WIT;
	private final int[] _classes;
	
	public Henna(StatsSet set)
	{
		_symbolId = set.getInteger("symbolId");
		_dyeId = set.getInteger("dyeId");
		_price = set.getInteger("price");
		_INT = set.getInteger("INT");
		_STR = set.getInteger("STR");
		_CON = set.getInteger("CON");
		_MEN = set.getInteger("MEN");
		_DEX = set.getInteger("DEX");
		_WIT = set.getInteger("WIT");
		_classes = set.getIntegerArray("classes");
	}
	
	public int getSymbolId()
	{
		return _symbolId;
	}
	
	public int getDyeId()
	{
		return _dyeId;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public static final int getRequiredDyeAmount()
	{
		return 10;
	}
	
	public int getINT()
	{
		return _INT;
	}
	
	public int getSTR()
	{
		return _STR;
	}
	
	public int getCON()
	{
		return _CON;
	}
	
	public int getMEN()
	{
		return _MEN;
	}
	
	public int getDEX()
	{
		return _DEX;
	}
	
	public int getWIT()
	{
		return _WIT;
	}
	
	/**
	 * Seek if this {@link Henna} can be used by a {@link Player}, based on his classId.
	 * @param player : The Player to check.
	 * @return true if this Henna owns the Player classId.
	 */
	public boolean canBeUsedBy(Player player)
	{
		return ArraysUtil.contains(_classes, player.getClassId().getId());
	}
}