package net.sf.l2j.gameserver.vip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.VipRewardManager;

public class VIPReset
{
  private static VIPReset _instance = null;
  protected static final Logger _log = Logger.getLogger(VIPReset.class.getName());
  private Calendar NextEvent;
  private final SimpleDateFormat format = new SimpleDateFormat("dd MMM, HH:mm");
  
  public static VIPReset getInstance()
  {
    if (_instance == null) {
      _instance = new VIPReset();
    }
    return _instance;
  }
  
  public String getVIPResetNextTime()
  {
    if (this.NextEvent.getTime() != null) {
      return this.format.format(this.NextEvent.getTime());
    }
    return "Erro";
  }
  
  public void StartNextEventTime()
  {
    try
    {
      Calendar currentTime = Calendar.getInstance();
      Calendar testStartTime = null;
      long flush2 = 0L;long timeL = 0L;
      int count = 0;
      for (String timeOfDay : Config.CLEAR_VIP_INTERVAL_BY_TIME_OF_DAY)
      {
        testStartTime = Calendar.getInstance();
        testStartTime.setLenient(true);
        String[] splitTimeOfDay = timeOfDay.split(":");
        testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
        testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));
        testStartTime.set(13, 0);
        if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis()) {
          testStartTime.add(5, 1);
        }
        timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
        if (count == 0)
        {
          flush2 = timeL;
          this.NextEvent = testStartTime;
        }
        if (timeL < flush2)
        {
          flush2 = timeL;
          this.NextEvent = testStartTime;
        }
        count++;
      }
      _log.info("[VIP Reset]: Proximo Reset: " + this.NextEvent.getTime().toString());
      ThreadPool.schedule(new StartEventTask(), flush2);
    }
    catch (Exception e)
    {
      System.out.println("[VIP Reset]: " + e);
    }
  }
  
  static void Clear()
  {
    ThreadPool.schedule(new Runnable()
    {
      @Override
	public void run()
      {

          try
          {
            Connection con = L2DatabaseFactory.getInstance().getConnection();Throwable localThrowable3 = null;
            try
            {
              PreparedStatement statement = con.prepareStatement("TRUNCATE vip_reward");
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
          catch (SQLException e)
          {
            VIPReset._log.warning("AdminCustom - Clear(): " + e + "");
          }
          VipRewardManager.getInstance().reload();
          ThreadPool.schedule(new ExecuteCleanVip(), 10000L);
          
          VIPReset._log.info("----------------------------------------------------------------------------");
          VIPReset._log.info("[VIP Reset]: Tabela VIP Resetada.");
          VIPReset._log.info("----------------------------------------------------------------------------");
        
      }
    }, 1L);
    
    NextEvent();
  }
  
  public static void NextEvent()
  {
    ThreadPool.schedule(new Runnable()
    {
      @Override
	public void run()
      {
        VIPReset.getInstance().StartNextEventTime();
      }
    }, 1000L);
  }
  
  class StartEventTask
    implements Runnable
  {
    StartEventTask() {
    	
    }
    
    @Override
	public void run() {
    	
    	VIPReset.Clear();
    	
    }
  }
}
