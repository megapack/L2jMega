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

package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SiegeInfo;

public class CastleManagers implements IVoicedCommandHandler
{
 private static final String[] _voicedCommands = {"castlemanager","siege_gludio","siege_dion","siege_giran","siege_oren","siege_aden","siege_innadril","siege_goddard","siege_rune","siege_schuttgart"};
      
       @Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
       {
               if (command.startsWith("castlemanager")) 
               {
                       sendHtml(activeChar);
               }
              
               if (command.startsWith("siege_")) 
               {
                       if (activeChar.getClan() != null && !activeChar.isClanLeader()) 
                       {
                               activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                               return false;
                       }
                      
                       int castleId = 0;
                      
                       if (command.startsWith("siege_gludio"))
                               castleId = 1;
                       else if (command.startsWith("siege_dion"))
                               castleId = 2;
                       else if (command.startsWith("siege_giran"))
                               castleId = 3;
                       else if (command.startsWith("siege_oren"))
                               castleId = 4;
                       else if (command.startsWith("siege_aden"))
                               castleId = 5;
                       else if (command.startsWith("siege_innadril"))
                               castleId = 6;
                       else if (command.startsWith("siege_goddard"))
                               castleId = 7;
                       else if (command.startsWith("siege_rune"))
                               castleId = 8;
                       else if (command.startsWith("siege_schuttgart"))
                               castleId = 9;
                      
                       Castle castle = CastleManager.getInstance().getCastleById(castleId);
                       if(castle != null && castleId != 0)
                               activeChar.sendPacket(new SiegeInfo(castle));
               }
               return true;
       }
       
       private static void sendHtml(Player activeChar)
       {
               String htmFile = "data/html/mods/menu/CastleManager.htm";
              
               NpcHtmlMessage msg = new NpcHtmlMessage(5);
               msg.setFile(htmFile);
               activeChar.sendPacket(msg);
       }
      
       @Override
	public String[] getVoicedCommandList()
       {
        return _voicedCommands;
       }
}