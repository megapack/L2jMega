package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class ShortCutInit extends L2GameServerPacket
{
	private final L2ShortCut[] _shortCuts;
	private final Player _activeChar;
	
	public ShortCutInit(Player activeChar)
	{
		_activeChar = activeChar;
		_shortCuts = activeChar.getAllShortCuts();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x45);
		writeD(_shortCuts.length);
		
		for (L2ShortCut sc : _shortCuts)
		{
			writeD(sc.getType());
			writeD(sc.getSlot() + sc.getPage() * 12);
			
			switch (sc.getType())
			{
				case L2ShortCut.TYPE_ITEM: // 1
					writeD(sc.getId());
					writeD(sc.getCharacterType());
					writeD(sc.getSharedReuseGroup());
					
					if (sc.getSharedReuseGroup() < 0)
					{
						writeD(0x00); // Remaining time
						writeD(0x00); // Cooldown time
					}
					else
					{
						final ItemInstance item = _activeChar.getInventory().getItemByObjectId(sc.getId());
						if (item == null || !item.isEtcItem())
						{
							writeD(0x00); // Remaining time
							writeD(0x00); // Cooldown time
						}
						else
						{
							final IntIntHolder[] skills = item.getEtcItem().getSkills();
							if (skills == null)
							{
								writeD(0x00); // Remaining time
								writeD(0x00); // Cooldown time
							}
							else
							{
								for (IntIntHolder skillInfo : skills)
								{
									final L2Skill itemSkill = skillInfo.getSkill();
									if (_activeChar.getReuseTimeStamp().containsKey(itemSkill.getReuseHashCode()))
									{
										writeD((int) (_activeChar.getReuseTimeStamp().get(itemSkill.getReuseHashCode()).getRemaining() / 1000L));
										writeD((int) (itemSkill.getReuseDelay() / 1000L));
									}
									else
									{
										writeD(0x00); // Remaining time
										writeD(0x00); // Cooldown time
									}
								}
							}
						}
					}
					
					writeD(0x00); // Augmentation
					break;
				
				case L2ShortCut.TYPE_SKILL: // 2
					writeD(sc.getId());
					writeD(sc.getLevel());
					writeC(0x00); // C5
					writeD(0x01); // C6
					break;
				
				default:
					writeD(sc.getId());
					writeD(0x01); // C6
			}
		}
	}
}