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
package net.sf.l2j.gameserver.instancemanager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.CloseGame;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.skills.AbnormalEffect;


public class IPManager
{
    private static final Logger _log = Logger.getLogger(IPManager.class.getName());

    private static class SingletonHolder
    {
        protected static final IPManager _instance = new IPManager();
    }

    public static final IPManager getInstance()
    {
        return SingletonHolder._instance;
    }

    public IPManager()
    {
        _log.log(Level.INFO, "MultiBox Protection - Loaded.");
    }

    private static boolean multiboxKickTask(Player activeChar, Integer numberBox, Collection<Player> world)
    {
        Map<String, List<Player>> ipMap = new HashMap<>();
        for (Player player : world)
        {
            if (player.getClient() == null || player.getClient().isDetached())
                continue;
			String ip = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
			String playerIp = player.getClient().getConnection().getInetAddress().getHostAddress();
			if (ip.equals(playerIp))
			{
			    if (ipMap.get(ip) == null)
			        ipMap.put(ip, new ArrayList<Player>());
			    ipMap.get(ip).add(player);
			    if (ipMap.get(ip).size() >= numberBox)
			        return true;
			}
        }
        return false;
    }

    public boolean validBox(Player activeChar, Integer numberBox, Collection<Player> world, Boolean forcedLogOut)
    {     
        if (multiboxKickTask(activeChar, numberBox, world))
        {
  	      NpcHtmlMessage html = new NpcHtmlMessage(0);
  	      html.setFile("data/html/mods/multibox.htm");
  	      html.replace("%name%", activeChar.getName());
  	      html.replace("%secunds%", Config.TIME_MULTIBOX);
  	      activeChar.sendPacket(html);
  	      activeChar.setIsParalyzed(true);
  	      activeChar.startAbnormalEffect(2048);
  	      activeChar.startAbnormalEffect(AbnormalEffect.ROOT);
  	      
            if (forcedLogOut)
            {
                L2GameClient client = activeChar.getClient();
                _log.warning("Multibox Protection: " + client.getConnection().getInetAddress().getHostAddress() + " was trying to use over " + numberBox + " clients!");
                ThreadPool.schedule(new CloseGame(activeChar, Config.TIME_MULTIBOX), 0L);
            }
            return true;
        }
        return false;
    }
}