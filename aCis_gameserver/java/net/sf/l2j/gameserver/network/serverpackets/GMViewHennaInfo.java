package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.Henna;

public class GMViewHennaInfo extends L2GameServerPacket
{
	private final Player _activeChar;
	private final Henna[] _hennas = new Henna[3];
	private int _count;
	
	public GMViewHennaInfo(Player activeChar)
	{
		_activeChar = activeChar;
		_count = 0;
		
		for (int i = 0; i < 3; i++)
		{
			Henna h = _activeChar.getHenna(i + 1);
			if (h != null)
				_hennas[_count++] = h;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xea);
		
		writeC(_activeChar.getHennaStatINT());
		writeC(_activeChar.getHennaStatSTR());
		writeC(_activeChar.getHennaStatCON());
		writeC(_activeChar.getHennaStatMEN());
		writeC(_activeChar.getHennaStatDEX());
		writeC(_activeChar.getHennaStatWIT());
		
		writeD(3); // slots?
		
		writeD(_count); // size
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].canBeUsedBy(_activeChar) ? _hennas[i].getSymbolId() : 0);
		}
	}
}