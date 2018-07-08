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
import net.sf.l2j.gameserver.vip.VIPReward;

public class VipManager
{
  private static final Logger _log = Logger.getLogger(VipManager.class.getName());
  private final Map<Integer, Long> _vips;
  protected final Map<Integer, Long> _vipsTask;
  private ScheduledFuture<?> _scheduler;
  
  public static VipManager getInstance()
  {
    return SingletonHolder._instance;
  }
  
  protected VipManager()
  {
    this._vips = new ConcurrentHashMap<>();
    this._vipsTask = new ConcurrentHashMap<>();
    this._scheduler = ThreadPool.scheduleAtFixedRate(new VipTask(), 1000L, 1000L);
    load();
  }
  
  public void reload()
  {
    this._vips.clear();
    this._vipsTask.clear();
    if (this._scheduler != null) {
      this._scheduler.cancel(true);
    }
    this._scheduler = ThreadPool.scheduleAtFixedRate(new VipTask(), 1000L, 1000L);
    load();
  }
  
  public void load()
  {
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("SELECT objectId, duration FROM character_vip ORDER BY objectId");
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
          this._vips.put(Integer.valueOf(rs.getInt("objectId")), Long.valueOf(rs.getLong("duration")));
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
      _log.warning("Exception: VipManager load: " + e.getMessage());
    }
    _log.info("VipManager: Loaded " + this._vips.size() + " characters with vip privileges.");
  }
  
  public void addVip(int objectId, long duration)
  {
    this._vips.put(Integer.valueOf(objectId), Long.valueOf(duration));
    this._vipsTask.put(Integer.valueOf(objectId), Long.valueOf(duration));
    addVipPrivileges(objectId, true);
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("INSERT INTO character_vip (objectId, duration) VALUES (?, ?)");
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
      _log.warning("Exception: VipManager addVip: " + e.getMessage());
    }
  }
  
  public void updateVip(int objectId, long duration)
  {
    this._vips.put(Integer.valueOf(objectId), Long.valueOf(duration));
    this._vipsTask.put(Integer.valueOf(objectId), Long.valueOf(duration));
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("UPDATE character_vip SET duration = ? WHERE objectId = ?");
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
      _log.warning("Exception: VipManager updateVip: " + e.getMessage());
    }
  }
  
  public void removeVip(int objectId)
  {
    this._vips.remove(Integer.valueOf(objectId));
    this._vipsTask.remove(Integer.valueOf(objectId));
    removeVipPrivileges(objectId, false);
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("DELETE FROM character_vip WHERE objectId = ?");
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
      _log.warning("Exception: VipManager removeVip: " + e.getMessage());
    }
  }
  
  public boolean hasVipPrivileges(int objectId)
  {
    return this._vips.containsKey(Integer.valueOf(objectId));
  }
  
  public long getVipDuration(int objectId)
  {
    return this._vips.get(Integer.valueOf(objectId)).longValue();
  }
  
  public void addVipTask(int objectId, long duration)
  {
    this._vipsTask.put(Integer.valueOf(objectId), Long.valueOf(duration));
  }
  
  public void removeVipTask(int objectId)
  {
    this._vipsTask.remove(Integer.valueOf(objectId));
  }
  
  public void addVipPrivileges(int objectId, boolean apply)
  {
    Player player = World.getInstance().getPlayer(objectId);
    player.setVip(true);
    player.broadcastUserInfo();
    ThreadPool.schedule(new VIPReward(player), 2000L);
  }
  
  public void removeVipPrivileges(int objectId, boolean apply)
  {
    Player player = World.getInstance().getPlayer(objectId);
    player.setVip(false);
    player.broadcastUserInfo();
  }
  
  public class VipTask
    implements Runnable
  {
    public VipTask() {}
    
    @Override
	public final void run()
    {
      if (VipManager.this._vipsTask.isEmpty()) {
        return;
      }
      for (Map.Entry<Integer, Long> entry : VipManager.this._vipsTask.entrySet())
      {
        long duration = entry.getValue().longValue();
        if (System.currentTimeMillis() > duration)
        {
          int objectId = entry.getKey().intValue();
          VipManager.this.removeVip(objectId);
          
          Player player = World.getInstance().getPlayer(objectId);
          player.sendPacket(new ExShowScreenMessage("Your Vip privileges were removed.", 10000));
        }
      }
    }
  }
  
  private static class SingletonHolder
  {
    protected static final VipManager _instance = new VipManager();
  }
}
