package net.sf.l2j.gameserver.events;

import java.util.ArrayList;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.InitialPartyFarm;
import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCustom;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class PartyFarm
{
  public static L2Spawn _monster;
  public static int _bossHeading = 0;
  public static String _eventName = "";
  public static boolean _started = false;
  public static boolean _aborted = false;
  protected static boolean _finish = false;
  static PartyFarm _instance;
  
  public static void bossSpawnMonster()
  {
    spawnMonsters();
    
    Announcements.Announce("[Party Farm]:Teleport Now!");
    Announcements.Announce("[Party Farm]: Duration: " + Config.EVENT_BEST_FARM_TIME + " minute(s)!");
    _aborted = false;
    _started = true;
    
    waiter(Config.EVENT_BEST_FARM_TIME * 60 * 1000);
    if (!_aborted) {
      Finish_Event();
    }
  }
  
  public static void Finish_Event()
  {
    unSpawnMonsters();
    
    _started = false;
    _finish = true;
    
    Announcements.Announce("[Party Farm]: Finished!");
    if (!AdminCustom._bestfarm_manual) {
      InitialPartyFarm.getInstance().StartCalculationOfNextEventTime();
    } else {
      AdminCustom._bestfarm_manual = false;
    }
  }
  
  
  public static void spawnMonsters()
  {
    for (int i = 0; i < Config.MONSTER_LOCS_COUNT; i++)
    {
      int[] coord = Config.MONSTER_LOCS[i];
      monsters.add(spawnNPC(coord[0], coord[1], coord[2], Config.monsterId));
    }
  }
  
  public static boolean is_started()
  {
    return _started;
  }
  
  public static boolean is_finish()
  {
    return _finish;
  }
  
  
  protected static L2Spawn spawnNPC(int xPos, int yPos, int zPos, int npcId)
  {
    NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
    try
    {
      L2Spawn spawn = new L2Spawn(template);
      spawn.setLoc(xPos,yPos,zPos,0);
      spawn.setRespawnDelay(Config.PARTY_FARM_MONSTER_DALAY);
      SpawnTable.getInstance().addNewSpawn(spawn, false);
      spawn.doSpawn(false);
      spawn.getNpc().setTitle(_eventName);
      spawn.getNpc().isAggressive();
      spawn.getNpc().decayMe();
      spawn.getNpc().spawnMe(spawn.getNpc().getX(), spawn.getNpc().getY(), spawn.getNpc().getZ());
      spawn.getNpc().broadcastPacket(new MagicSkillUse(spawn.getNpc(), spawn.getNpc(), 1034, 1, 1, 1));


      return spawn;
    }
    catch (Exception e) {}
    return null;
  }
  
  protected static ArrayList<L2Spawn> monsters = new ArrayList<>();
  
  protected static void unSpawnMonsters()
  {
		for (L2Spawn s : monsters)
		{
			if (s == null)
			{
				monsters.remove(s);
				return;
			}
			
			s.getNpc().deleteMe();
			s.setRespawnState(false);
			SpawnTable.getInstance().deleteSpawn(s, true);

		}
  }

  
  protected static void waiter(long interval)
  {
    long startWaiterTime = System.currentTimeMillis();
    int seconds = (int)(interval / 1000L);
    while ((startWaiterTime + interval > System.currentTimeMillis()) && (!_aborted))
    {
      seconds--;
      switch (seconds)
      {
      case 3600: 
        if (_started)
        {

          Announcements.Announce("[Party Farm]: " + seconds / 60 / 60 + " hour(s) till event finish!");
        }
        break;
      case 60: 
      case 120: 
      case 180: 
      case 240: 
      case 300: 
      case 600: 
      case 900: 
      case 1800: 
        if (_started)
        {

          Announcements.Announce("[Party Farm]: " + seconds / 60 + " minute(s) till event finish!");
        }
        break;
      case 1: 
      case 2: 
      case 3: 
      case 10: 
      case 15: 
      case 30: 
        if (_started) {
          Announcements.Announce("[Party Farm]: " + seconds + " second(s) till event finish!");
        }
        break;
      }
      long startOneSecondWaiterStartTime = System.currentTimeMillis();
      while (startOneSecondWaiterStartTime + 1000L > System.currentTimeMillis()) {
        try
        {
          Thread.sleep(1L);
        }
        catch (InterruptedException ie)
        {
          ie.printStackTrace();
        }
      }
    }
  }

}
