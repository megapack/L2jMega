package net.sf.l2j.gameserver.vip;

import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VIPINFO
  implements Runnable
{
  private Player _activeChar;
  
  public VIPINFO(Player activeChar)
  {
    this._activeChar = activeChar;
  }
  
  @Override
public void run()
  {
    if (this._activeChar.isOnline())
    {
      String htmFile = "data/html/mods/vip.htm";
      String htmContent = HtmCache.getInstance().getHtm(htmFile);
      if (htmContent != null)
      {
        NpcHtmlMessage doacaoHtml = new NpcHtmlMessage(1);
        doacaoHtml.setHtml(htmContent);
        this._activeChar.sendPacket(doacaoHtml);
      }
      else
      {
        this._activeChar.sendMessage("ERROR, INFORME A STAFF DO SERVIDOR.");
      }
    }
  }
}
