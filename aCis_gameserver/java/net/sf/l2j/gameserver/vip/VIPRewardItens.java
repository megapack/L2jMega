package net.sf.l2j.gameserver.vip;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.instancemanager.VipManager;
import net.sf.l2j.gameserver.model.actor.instance.Player;

public class VIPRewardItens
  implements Runnable
{
  private Player _activeChar;
  
  public VIPRewardItens(Player activeChar)
  {
    this._activeChar = activeChar;
  }
  
  @Override
public void run()
  {
    if (this._activeChar.isOnline())
    {
      if ((VipManager.getInstance().hasVipPrivileges(this._activeChar.getObjectId())) || (this._activeChar.isVip())) {
        for (int[] reward : Config.VIP_REWARD_LIST) {
          if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable()) {
            this._activeChar.addItem("Adicionar item", reward[0], reward[1], this._activeChar, true);
          } else {
            for (int i = 0; i < reward[1]; i++) {
              this._activeChar.addItem("Adicionar item", reward[0], 1, this._activeChar, true);
            }
          }
        }
      }
      ThreadPool.schedule(new VIPReward(this._activeChar), 5000L);
    }
  }
}
