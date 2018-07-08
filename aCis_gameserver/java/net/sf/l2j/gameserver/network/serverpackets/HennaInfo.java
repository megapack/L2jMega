package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.Henna;

public final class HennaInfo extends L2GameServerPacket
{
	private final Player _activeChar;
	private final Henna[] _hennas = new Henna[3];
	private int _count;
	
	public HennaInfo(Player player)
	{
		_activeChar = player;
		_count = 0;
		
		for (int i = 0; i < 3; i++)
		{
			Henna henna = _activeChar.getHenna(i + 1);
			if (henna != null)
				_hennas[_count++] = henna;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe4);
		
		writeC(_activeChar.getHennaStatINT()); // equip INT
		writeC(_activeChar.getHennaStatSTR()); // equip STR
		writeC(_activeChar.getHennaStatCON()); // equip CON
		writeC(_activeChar.getHennaStatMEN()); // equip MEM
		writeC(_activeChar.getHennaStatDEX()); // equip DEX
		writeC(_activeChar.getHennaStatWIT()); // equip WIT
		
		// Henna slots
		int classId = _activeChar.getClassId().level();
		if (classId == 1)
			writeD(2);
		else if (classId > 1)
			writeD(3);
		else
			writeD(0);
		
		writeD(_count); // size
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].canBeUsedBy(_activeChar) ? _hennas[i].getSymbolId() : 0);
		}
	}
}