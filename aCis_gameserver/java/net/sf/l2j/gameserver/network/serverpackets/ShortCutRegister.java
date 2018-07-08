package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ShortCut;

/**
 * format dd d/dd/d d
 */
public class ShortCutRegister extends L2GameServerPacket
{
	private final L2ShortCut _shortcut;
	
	public ShortCutRegister(L2ShortCut shortcut)
	{
		_shortcut = shortcut;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x44);
		
		writeD(_shortcut.getType());
		writeD(_shortcut.getSlot() + _shortcut.getPage() * 12); // C4 Client
		switch (_shortcut.getType())
		{
			case L2ShortCut.TYPE_ITEM: // 1
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
				writeD(_shortcut.getSharedReuseGroup());
				break;
			case L2ShortCut.TYPE_SKILL: // 2
				writeD(_shortcut.getId());
				writeD(_shortcut.getLevel());
				writeC(0x00); // C5
				writeD(_shortcut.getCharacterType());
				break;
			default:
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
		}
	}
}