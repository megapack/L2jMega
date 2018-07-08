package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.Henna;

public class HennaEquipList extends L2GameServerPacket
{
	private final Player _player;
	private final List<Henna> _hennaEquipList;
	
	public HennaEquipList(Player player, List<Henna> hennaEquipList)
	{
		_player = player;
		_hennaEquipList = hennaEquipList;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe2);
		writeD(_player.getAdena());
		writeD(3);
		writeD(_hennaEquipList.size());
		
		for (Henna temp : _hennaEquipList)
		{
			// Player must have at least one dye in inventory to be able to see the henna that can be applied with it.
			if ((_player.getInventory().getItemByItemId(temp.getDyeId())) != null)
			{
				writeD(temp.getSymbolId()); // symbolid
				writeD(temp.getDyeId()); // itemid of dye
				writeD(Henna.getRequiredDyeAmount()); // amount of dyes required
				writeD(temp.getPrice()); // amount of adenas required
				writeD(1); // meet the requirement or not
			}
		}
	}
}