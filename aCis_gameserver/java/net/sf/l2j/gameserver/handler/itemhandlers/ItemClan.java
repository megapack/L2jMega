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

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class ItemClan implements IItemHandler
{
    private final int reputation = 150000;
    private final byte level = 8;
    
    // id skills
    private final int[] clanSkills =
    {
        370,
        371,
        372,
        373,
        374,
        375,
        376,
        377,
        378,
        379,
        380,
        381,
        382,
        383,
        384,
        385,
        386,
        387,
        388,
        389,
        390,
        391
    };
    
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;

		Player activeChar = (Player) playable;

        if (activeChar.isClanLeader())
        {
            if (activeChar.getClan().getLevel() == 8)
            {
                activeChar.sendMessage("Your clan is already maximum level!");
                return;
            }
                       
            activeChar.getClan().changeLevel(level);
            activeChar.getClan().addReputationScore(reputation);
               
            for (int s : clanSkills)
            {
                 L2Skill clanSkill = SkillTable.getInstance().getInfo(s, SkillTable.getInstance().getMaxLevel(s));
                 activeChar.getClan().addNewSkill(clanSkill);
            }
               
            activeChar.sendSkillList();
            activeChar.getClan().updateClanInDB();            
            activeChar.sendMessage("Your clan Level/Skills/Reputation has been updated!");      
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
            activeChar.broadcastUserInfo();
        }
        else
            activeChar.sendMessage("You are not the clan leader.");  

       return;
	}
}