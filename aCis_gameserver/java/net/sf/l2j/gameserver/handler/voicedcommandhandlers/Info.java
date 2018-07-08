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



import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;



/**

*

* @author Mega

 * @version 1.0

 */



public class Info implements IVoicedCommandHandler

{

    private static final String[] _voicedCommands =

    {

        "info"


    };



    

    @Override

    public boolean useVoicedCommand(String command, Player activeChar, String target)

    {

        if (command.equals("info"))
            showHtml(activeChar);        
     
        return true;

    }

    

    private static void showHtml(Player activeChar)

    {
        NpcHtmlMessage html = new NpcHtmlMessage(0);

        html.setFile("data/html/mods/menu/info.htm");   

        activeChar.sendPacket(html);

    }

    

    @Override

    public String[] getVoicedCommandList()

    {

        return _voicedCommands;

    }

}