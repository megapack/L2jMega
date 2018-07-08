package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.Player;

public final class RequestShortCutDel extends L2GameClientPacket
{
	private int _slot;
	private int _page;
	
	@Override
	protected void readImpl()
	{
		int id = readD();
		_slot = id % 12;
		_page = id / 12;
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (_page > 9 || _page < 0)
			return;
		
		activeChar.deleteShortCut(_slot, _page);
	}
}