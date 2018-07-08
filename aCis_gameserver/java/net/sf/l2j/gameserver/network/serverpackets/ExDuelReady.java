package net.sf.l2j.gameserver.network.serverpackets;

/**
 * Format: ch d
 * @author KenM
 */
public class ExDuelReady extends L2GameServerPacket
{
	private final int _unk1;
	
	public ExDuelReady(int unk1)
	{
		_unk1 = unk1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4c);
		
		writeD(_unk1);
	}
}