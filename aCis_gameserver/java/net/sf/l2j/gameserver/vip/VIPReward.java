package net.sf.l2j.gameserver.vip;

import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminVip;
import net.sf.l2j.gameserver.instancemanager.VipManager;
import net.sf.l2j.gameserver.instancemanager.VipRewardManager;
import net.sf.l2j.gameserver.model.actor.instance.Player;

public class VIPReward
  implements Runnable
{
  private Player _activeChar;
  
  public VIPReward(Player activeChar)
  {
    this._activeChar = activeChar;
  }
  
  @Override
public void run()
  {
    if ((this._activeChar.isOnline()) && ((VipManager.getInstance().hasVipPrivileges(this._activeChar.getObjectId())) || (this._activeChar.isVip())))
    {
      if (VipRewardManager.getInstance().hasVipPrivileges(this._activeChar.getObjectId())) {
        this._activeChar.sendChatMessage(0, 3, ".", "Recompensa VIP disponivel em " + VIPReset.getInstance().getVIPResetNextTime() + " GMT-3 :.");
      } else {
        AdminVip.VIPHtml(this._activeChar);
      }
    }

  }
}
