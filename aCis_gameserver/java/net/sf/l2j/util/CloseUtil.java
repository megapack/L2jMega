package net.sf.l2j.util;

import java.io.Closeable;
import java.sql.Connection;
import java.util.logging.Logger;

public final class CloseUtil
{
  private static final Logger _log = Logger.getLogger(CloseUtil.class.getName());
  
  public static void close(Connection con)
  {
    if (con != null) {
      try
      {
        con.close();
        con = null;
      }
      catch (Throwable e)
      {
        e.printStackTrace();
        _log.severe(e.getMessage());
      }
    }
  }
  
  public static void close(Closeable closeable)
  {
    if (closeable != null) {
      try
      {
        closeable.close();
      }
      catch (Throwable e)
      {
        e.printStackTrace();
        _log.severe(e.getMessage());
      }
    }
  }
}
