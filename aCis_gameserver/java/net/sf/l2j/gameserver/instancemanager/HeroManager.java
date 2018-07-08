package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

public class HeroManager
{
  private static final Logger _log = Logger.getLogger(HeroManager.class.getName());
  private final Map<Integer, Long> _heros;
  protected final Map<Integer, Long> _herosTask;
  private ScheduledFuture<?> _scheduler;
  
  public static HeroManager getInstance()
  {
    return SingletonHolder._instance;
  }
  
  protected HeroManager()
  {
    this._heros = new ConcurrentHashMap<>();
    this._herosTask = new ConcurrentHashMap<>();
    this._scheduler = ThreadPool.scheduleAtFixedRate(new HeroTask(), 1000L, 1000L);
    load();
  }
  
  public void reload()
  {
    this._heros.clear();
    this._herosTask.clear();
    if (this._scheduler != null) {
      this._scheduler.cancel(true);
    }
    this._scheduler = ThreadPool.scheduleAtFixedRate(new HeroTask(), 1000L, 1000L);
    load();
  }
  
  public void load()
  {
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("SELECT objectId, duration FROM character_hero ORDER BY objectId");
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
          this._heros.put(Integer.valueOf(rs.getInt("objectId")), Long.valueOf(rs.getLong("duration")));
        }
        rs.close();
        statement.close();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (con != null) {
          if (localThrowable3 != null) {
            try
            {
              con.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            con.close();
          }
        }
      }
    }
    catch (Exception e)
    {
      _log.warning("Exception: HeroManager load: " + e.getMessage());
    }
    _log.info("HeroManager: Loaded " + this._heros.size() + " characters with hero privileges.");
  }
  
  public void addHero(int objectId, long duration)
  {
    this._heros.put(Integer.valueOf(objectId), Long.valueOf(duration));
    this._herosTask.put(Integer.valueOf(objectId), Long.valueOf(duration));
    addHeroPrivileges(objectId, true);
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("INSERT INTO character_hero (objectId, duration) VALUES (?, ?)");
        statement.setInt(1, objectId);
        statement.setLong(2, duration);
        statement.execute();
        statement.close();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (con != null) {
          if (localThrowable3 != null) {
            try
            {
              con.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            con.close();
          }
        }
      }
    }
    catch (Exception e)
    {
      _log.warning("Exception: HeroManager addHero: " + e.getMessage());
    }
  }
  
  public void updateHero(int objectId, long duration)
  {
    this._heros.put(Integer.valueOf(objectId), Long.valueOf(duration));
    this._herosTask.put(Integer.valueOf(objectId), Long.valueOf(duration));
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("UPDATE character_hero SET duration = ? WHERE objectId = ?");
        statement.setLong(1, duration);
        statement.setInt(2, objectId);
        statement.execute();
        statement.close();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (con != null) {
          if (localThrowable3 != null) {
            try
            {
              con.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            con.close();
          }
        }
      }
    }
    catch (Exception e)
    {
      _log.warning("Exception: HeroManager updateHero: " + e.getMessage());
    }
  }
  
  public void removeHero(int objectId)
  {
    this._heros.remove(Integer.valueOf(objectId));
    this._herosTask.remove(Integer.valueOf(objectId));
    removeHeroPrivileges(objectId, false);
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("DELETE FROM character_hero WHERE objectId = ?");
        statement.setInt(1, objectId);
        statement.execute();
        statement.close();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (con != null) {
          if (localThrowable3 != null) {
            try
            {
              con.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            con.close();
          }
        }
      }
    }
    catch (Exception e)
    {
      _log.warning("Exception: HeroManager removeHero: " + e.getMessage());
    }
  }
  
  public boolean hasHeroPrivileges(int objectId)
  {
    return this._heros.containsKey(Integer.valueOf(objectId));
  }
  
  public long getHeroDuration(int objectId)
  {
    return this._heros.get(Integer.valueOf(objectId)).longValue();
  }
  
  public void addHeroTask(int objectId, long duration)
  {
    this._herosTask.put(Integer.valueOf(objectId), Long.valueOf(duration));
  }
  
  public void removeHeroTask(int objectId)
  {
    this._herosTask.remove(Integer.valueOf(objectId));
  }
  
  public void addHeroPrivileges(int objectId, boolean apply)
  {
    Player player = World.getInstance().getPlayer(objectId);
    player.setHero(true);
    player.broadcastUserInfo();
    player.addItem("Hero Item", 6842, 1, player, true);
  }
  
  public void removeHeroPrivileges(int objectId, boolean apply)
  {
    Player player = World.getInstance().getPlayer(objectId);
    player.setHero(false);
    player.broadcastUserInfo();
    player.destroyItem("HeroEnd", 6842, 1, null, false);
  }
  
  public class HeroTask
    implements Runnable
  {
    public HeroTask() {}
    
    @Override
	public final void run()
    {
      if (HeroManager.this._herosTask.isEmpty()) {
        return;
      }
      for (Map.Entry<Integer, Long> entry : HeroManager.this._herosTask.entrySet())
      {
        long duration = entry.getValue().longValue();
        if (System.currentTimeMillis() > duration)
        {
          int objectId = entry.getKey().intValue();
          HeroManager.this.removeHero(objectId);
          
          Player player = World.getInstance().getPlayer(objectId);
          player.sendPacket(new ExShowScreenMessage("Your Hero privileges were removed.", 10000));
        }
      }
    }
  }
  
  private static class SingletonHolder
  {
    protected static final HeroManager _instance = new HeroManager();
  }
}
