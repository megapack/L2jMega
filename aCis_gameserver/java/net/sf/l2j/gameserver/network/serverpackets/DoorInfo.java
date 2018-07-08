package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Player;

public class DoorInfo extends L2GameServerPacket
{
	private final Door _door;
	private final Player _activeChar;
	
	public DoorInfo(Door door, Player activeChar)
	{
		_door = door;
		_activeChar = activeChar;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4c);
		writeD(_door.getObjectId());
		writeD(_door.getDoorId());
		writeD(_door.isAutoAttackable(_activeChar) ? 0 : 1); // ??? (can attack)
		writeD(1); // ??? (can target)
		writeD(_door.isOpened() ? 0 : 1);
		writeD(_door.getMaxHp());
		writeD((int) _door.getCurrentHp());
		writeD(0); // ??? (show HP)
		writeD(0); // ??? (Damage)
	}
}