package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

public class CustomZone
  extends SpawnZoneType
{
  public CustomZone(int id)
  {
    super(id);
  }
  
  @Override
protected void onEnter(Creature character)
  {
    character.setInsideZone(ZoneId.CUSTOM, true);
    character.setInsideZone(ZoneId.NO_STORE, true);
    if ((character instanceof Player))
    {
      final Player player = (Player)character;
      if ((Config.BS_PVPZONE) && (!player.isPhantom()) && (!player.isGM())) {
        if ((player.getClassId() == ClassId.BISHOP) || (player.getClassId() == ClassId.CARDINAL) || (player.getClassId() == ClassId.SHILLIEN_ELDER) || (player.getClassId() == ClassId.SHILLIEN_SAINT) || (player.getClassId() == ClassId.EVAS_SAINT) || (player.getClassId() == ClassId.ELVEN_ELDER) || (player.getClassId() == ClassId.PROPHET) || (player.getClassId() == ClassId.HIEROPHANT))
        {
          player.sendPacket(new ExShowScreenMessage("Class not released in PvP area..", 6000, 2, true));
          ThreadPool.schedule(new Runnable()
          {
            @Override
			public void run()
            {
              if ((player.isOnline()) && (!player.isInsideZone(ZoneId.PEACE))) {
                player.teleToLocation(83464, 148616, 62136, 50);
              }
            }
          }, 4000L);
        }
      }
      if ((player.getMountType() == 2) && (!Config.WYVERN_PVPZONE))
      {
        player.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
        player.enteredNoLanding(5);
      }
    }
  }
  
  @Override
protected void onExit(Creature character)
  {
    character.setInsideZone(ZoneId.CUSTOM, false);
    character.setInsideZone(ZoneId.NO_STORE, false);
  }
  
  @Override
public void onDieInside(Creature character) {}
  
  @Override
public void onReviveInside(Creature character) {}
}
