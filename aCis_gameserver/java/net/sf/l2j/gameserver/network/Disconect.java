package net.sf.l2j.gameserver.network;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;

public class Disconect
  implements Runnable
{
  private Player _activeChar;
  
  public Disconect(Player activeChar)
  {
    this._activeChar = activeChar;
  }
  
  @Override
public void run()
  {
    L2GameClient client = this._activeChar.getClient();
    client.close(ServerClose.STATIC_PACKET);
  }
}
