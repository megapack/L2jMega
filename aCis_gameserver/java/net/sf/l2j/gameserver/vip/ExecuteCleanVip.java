package net.sf.l2j.gameserver.vip;



import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.instancemanager.VipManager;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;

public class ExecuteCleanVip
  implements Runnable
{
  @Override
public void run()
  {
    for (Player player : World.getInstance().getPlayers()) {
      if ((player != null) && 
        (player.isOnline()) && ((VipManager.getInstance().hasVipPrivileges(player.getObjectId())) || (player.isVip())))
      {
 
        
        ThreadPool.schedule(new VIPReward(player), 1000L);
        player.sendChatMessage(0, 3, ".:", "Sua recompensa VIP esta disponivel.. :.");
      }
    }
  }
}
