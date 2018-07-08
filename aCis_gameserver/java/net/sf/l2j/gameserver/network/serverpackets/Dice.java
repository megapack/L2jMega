package net.sf.l2j.gameserver.network.serverpackets;

public class Dice extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _itemId;
	private final int _number;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public Dice(int charObjId, int itemId, int number, int x, int y, int z)
	{
		_charObjId = charObjId;
		_itemId = itemId;
		_number = number;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xD4);
		writeD(_charObjId);
		writeD(_itemId);
		writeD(_number);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}