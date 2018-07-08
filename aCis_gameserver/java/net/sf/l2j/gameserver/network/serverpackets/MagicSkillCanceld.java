package net.sf.l2j.gameserver.network.serverpackets;

public class MagicSkillCanceld extends L2GameServerPacket
{
	private final int _objectId;
	
	public MagicSkillCanceld(int objectId)
	{
		_objectId = objectId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x49);
		writeD(_objectId);
	}
}