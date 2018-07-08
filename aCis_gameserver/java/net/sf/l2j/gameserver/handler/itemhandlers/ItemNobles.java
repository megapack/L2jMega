/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class ItemNobles implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;

		Player activeChar = (Player) playable;

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("This item cannot be used on Olympiad Games.");
		}

		if(activeChar.isNoble())
		{
			activeChar.sendMessage("You are already a noblesse.");
		}
		else
		{
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, 5103, 1, 1000, 0));
			activeChar.setNoble(true, true);
			activeChar.getInventory().addItem("Tiara", 7694, 1, activeChar, null);
			activeChar.sendMessage("You are now a noble, check your skills!");
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
            activeChar.broadcastUserInfo();
		}
		activeChar = null;
	}
}