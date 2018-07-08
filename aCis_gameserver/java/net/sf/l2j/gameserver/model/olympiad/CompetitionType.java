package net.sf.l2j.gameserver.model.olympiad;

/**
 * @author DS
 */
public enum CompetitionType
{
	CLASSED("classed"),
	NON_CLASSED("non-classed");
	
	private final String _name;
	
	private CompetitionType(String name)
	{
		_name = name;
	}
	
	@Override
	public final String toString()
	{
		return _name;
	}
}