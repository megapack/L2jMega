package net.sf.l2j.gameserver.network.serverpackets;

/**
 * Format : (h) d [dS]
 * @author l3x
 */
public class ExSendManorList extends L2GameServerPacket
{
	public static final ExSendManorList STATIC_PACKET = new ExSendManorList();
	
	private ExSendManorList()
	{
	}
	
	private static final String[] _manorList =
	{
		"gludio",
		"dion",
		"giran",
		"oren",
		"aden",
		"innadril",
		"goddard",
		"rune",
		"schuttgart"
	};
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1B);
		writeD(_manorList.length);
		for (int i = 0; i < _manorList.length; i++)
		{
			writeD(i + 1);
			writeS(_manorList[i]);
		}
	}
}