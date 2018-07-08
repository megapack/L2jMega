package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

public class FlagZone
  extends SpawnZoneType
{
  public FlagZone(int id)
  {
    super(id);
  }
  
  @Override
protected void onEnter(Creature character)
  {
    if ((character instanceof Player))
    {
      Player player = (Player)character;
      if (!player.isInsideZone(ZoneId.FLAG)) {
        player.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
      }
      if (!player.isInObserverMode())
      {
        if (player.getPvpFlag() > 0) {
          PvpFlagTaskManager.getInstance().remove(player);
        }
        player.updatePvPFlag(1);
      }
    }
    character.setInsideZone(ZoneId.FLAG, true);
    if ((character instanceof Player))
    {
      Player player = (Player)character;
      player.sendPacket(new EtcStatusUpdate(player));
      player.broadcastUserInfo();
    }
  }
  
  @Override
protected void onExit(Creature character)
  {
    character.setInsideZone(ZoneId.FLAG, false);
    if ((character instanceof Player))
    {
      Player player = (Player)character;
      if (!player.isInObserverMode()) {
        PvpFlagTaskManager.getInstance().add(player, 20000L);
      }
      player.sendPacket(new EtcStatusUpdate(player));
      player.broadcastUserInfo();
      if (!player.isInsideZone(ZoneId.FLAG)) {
        player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
      }
    }
  }
  
  @Override
public void onDieInside(Creature character) {}
  
  @Override
public void onReviveInside(Creature character) {
	  
	  onEnter(character);
  }
}
