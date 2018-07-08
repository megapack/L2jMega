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
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminVip;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;

public class VipRewardManager
{
  private static final Logger _log = Logger.getLogger(VipRewardManager.class.getName());
  private final Map<Integer, Long> _vips;
  protected final Map<Integer, Long> _vipsTask;
  private ScheduledFuture<?> _scheduler;
  
  public static VipRewardManager getInstance()
  {
    return SingletonHolder._instance;
  }
  
  protected VipRewardManager()
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
        PreparedStatement statement = con.prepareStatement("SELECT objectId, duration FROM vip_reward ORDER BY objectId");
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
      _log.warning("Exception: VipRewardManager load: " + e.getMessage());
    }
    _log.info("VipRewardManager: Loaded " + this._vips.size() + " characters with VIP privileges.");
  }
  
  public void addVip(int objectId, long duration)
  {
    this._vips.put(Integer.valueOf(objectId), Long.valueOf(duration));
    this._vipsTask.put(Integer.valueOf(objectId), Long.valueOf(duration));
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("INSERT INTO vip_reward (objectId, duration) VALUES (?, ?)");
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
      _log.warning("Exception: VipRewardManager addVip: " + e.getMessage());
    }
  }
  
  public void updateVip(int objectId, long duration)
  {
    duration += this._vips.get(Integer.valueOf(objectId)).longValue();
    this._vips.put(Integer.valueOf(objectId), Long.valueOf(duration));
    this._vipsTask.put(Integer.valueOf(objectId), Long.valueOf(duration));
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
      try
      {
        PreparedStatement statement = con.prepareStatement("UPDATE vip_reward SET duration = ? WHERE objectId = ?");
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
      _log.warning("Exception: VipRewardManager updateVip: " + e.getMessage());
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
        PreparedStatement statement = con.prepareStatement("DELETE FROM vip_reward WHERE objectId = ?");
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
      _log.warning("Exception: VipRewardManager removeVip: " + e.getMessage());
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
  
  public void removeVipPrivileges(int objectId, boolean apply)
  {
    Player player = World.getInstance().getPlayer(objectId);
      AdminVip.VIPHtml(player);
    
  }
  
  public class VipTask
    implements Runnable
  {
    public VipTask() {}
    
    @Override
	public final void run()
    {
      if (VipRewardManager.this._vipsTask.isEmpty()) {
        return;
      }
      for (Map.Entry<Integer, Long> entry : VipRewardManager.this._vipsTask.entrySet())
      {
        long duration = entry.getValue().longValue();
        if (System.currentTimeMillis() > duration)
        {
          int objectId = entry.getKey().intValue();
          VipRewardManager.this.removeVip(objectId);
        }
      }
    }
  }
  
  private static class SingletonHolder
  {
    protected static final VipRewardManager _instance = new VipRewardManager();
  }
}
