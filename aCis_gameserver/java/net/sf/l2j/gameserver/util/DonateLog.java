package net.sf.l2j.gameserver.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DonateLog
{
  static
  {
    new File("log/Player Log/DonateLog").mkdirs();
  }
  
  private static final Logger _log = Logger.getLogger(DonateLog.class.getName());
  
  @SuppressWarnings("null")
public static void auditGMAction(int i, String Name, String action, String ip, String params)
  {
    File file = new File("log/Player Log/DonateLog/" + Name + ".txt");
    if (!file.exists()) {
      try
      {
        file.createNewFile();
      }
      catch (IOException localIOException1) {}
    }
    try
    {
      FileWriter save = new FileWriter(file, true);Throwable localThrowable3 = null;
      try
      {
        save.write(Util.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + " >>>>> ID : [" + i + "] >> Nome: [" + Name + "] >> Utilizou: [ " + action + " ] >> IP: [" + ip + "]\r\n");
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (save != null) {
          if (localThrowable3 != null) {
            try
            {
              save.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            save.close();
          }
        }
      }
    }
    catch (IOException e)
    {
      _log.log(Level.SEVERE, "DonateLog for Player " + Name + " could not be saved: ", e);
    }
  }
  
  public static void auditGMAction(int i, String Name, String action, String ip)
  {
    auditGMAction(i, Name, action, ip, "");
  }
}
