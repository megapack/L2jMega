package hwid;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import hwid.crypt.FirstKey;

public class HwidConfig
{
	protected static Logger	_log = Logger.getLogger(HwidConfig.class.getName());
	public static final String D_GUARD_FILE = "./config/custom/hwid_settings.properties";
	public static final boolean PROTECT_DEBUG = false;
	public static final boolean PROTECT_ENABLE_HWID_LOCK = false;
	public static byte[] GUARD_CLIENT_CRYPT_KEY;
	public static byte[] GUARD_CLIENT_CRYPT;
	public static byte[] GUARD_SERVER_CRYPT_KEY;
	public static byte[] GUARD_SERVER_CRYPT;
	
	public static boolean ALLOW_GUARD_SYSTEM;
	public static int PROTECT_WINDOWS_COUNT;
	
	public static int GET_CLIENT_HWID;
	public static boolean ALLOW_SEND_GG_REPLY;
	public static boolean ENABLE_CONSOLE_LOG;
	public static long TIME_SEND_GG_REPLY;
	public static boolean PROTECT_KICK_WITH_EMPTY_HWID;
	public static boolean PROTECT_KICK_WITH_LASTERROR_HWID;


	  public static final void load()
	  {
		  File fp = new File(D_GUARD_FILE);
		  ALLOW_GUARD_SYSTEM = fp.exists();
    
		  if (ALLOW_GUARD_SYSTEM)
		  try{
			  
			  Properties guardSettings = new Properties();
			  InputStream is = new FileInputStream(fp);
			  guardSettings.load(is);
			  is.close();

			  System.out.println("- Loading Hwid Configs");
			  ALLOW_GUARD_SYSTEM = getBooleanProperty(guardSettings, "AllowGuardSystem", true);
		      GET_CLIENT_HWID = getIntProperty(guardSettings, "UseClientHWID", 2);
		      ENABLE_CONSOLE_LOG = getBooleanProperty(guardSettings, "EnableConsoleLog", false);
		      PROTECT_KICK_WITH_EMPTY_HWID = getBooleanProperty(guardSettings, "KickWithEmptyHWID", false);
		      PROTECT_WINDOWS_COUNT = getIntProperty(guardSettings, "AllowedWindowsCount", 1);
		      PROTECT_KICK_WITH_LASTERROR_HWID = getBooleanProperty(guardSettings, "KickWithLastErrorHWID", false);

		      String key_client = "GOGX2_RB(]Slnjt15~EgyqTv%[$YR]!1E~ayK?$9[R%%m4{zoMF$D?f:zvS2q&>~";
		      String key_server = "b*qR43<9J1pD>Q4Uns6FsKao~VbU0H]y`A0ytTveiWn)SuSYsM?m*eblL!pwza!t";
		      byte[] keyS = key_server.getBytes();
		      byte[] tmpS = new byte[32];

		      byte[] keyC = key_client.getBytes();
		      byte[] tmpC = new byte[32];
		
		      System.arraycopy(keyC, 0, tmpC, 0, 32);
		      GUARD_CLIENT_CRYPT_KEY = FirstKey.expandKey(tmpC, 32);
		      System.arraycopy(keyC, 32, tmpC, 0, 32);
		      GUARD_CLIENT_CRYPT = FirstKey.expandKey(tmpC, 32);
				
		      System.arraycopy(keyS, 0, tmpS, 0, 32);
		      GUARD_SERVER_CRYPT_KEY = FirstKey.expandKey(tmpS, 32);
		      System.arraycopy(keyS, 32, tmpS, 0, 32);
		      GUARD_SERVER_CRYPT = FirstKey.expandKey(tmpS, 32);
		  }
	  
		  catch (Exception e)
		  {
			  e.printStackTrace();
		  }
	  }

	  protected static Properties getSettings(String CONFIGURATION_FILE) throws Exception
	  {
	    Properties serverSettings = new Properties();
	    InputStream is = new FileInputStream(new File(CONFIGURATION_FILE));
	    serverSettings.load(is);
	    is.close();
	    return serverSettings;
	  }

	  protected static String getProperty(Properties prop, String name) {
	    return prop.getProperty(name.trim(), null);
	  }

	  protected static String getProperty(Properties prop, String name, String _default) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : s;
	  }
	
	  protected static int getIntProperty(Properties prop, String name, int _default) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : Integer.parseInt(s.trim());
	  }
	
	  protected static int getIntHexProperty(Properties prop, String name, int _default) {
	    return (int)getLongHexProperty(prop, name, _default);
	  }
	
	  protected static long getLongProperty(Properties prop, String name, long _default) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : Long.parseLong(s.trim());
	  }

	  protected static long getLongHexProperty(Properties prop, String name, long _default)
	  {
	    String s = getProperty(prop, name);
	    if (s == null)
	      return _default;
	    s = s.trim();
	    if (!s.startsWith("0x"))
	      s = "0x" + s;
	    return Long.decode(s).longValue();
	  }
	
	  protected static byte getByteProperty(Properties prop, String name, byte _default) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : Byte.parseByte(s.trim());
	  }
	
	  protected static byte getByteProperty(Properties prop, String name, int _default) {
	    return getByteProperty(prop, name, (byte)_default);
	  }
	
	  protected static boolean getBooleanProperty(Properties prop, String name, boolean _default) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : Boolean.parseBoolean(s.trim());
	  }
	
	  protected static float getFloatProperty(Properties prop, String name, float _default) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : Float.parseFloat(s.trim());
	  }
	
	  protected static float getFloatProperty(Properties prop, String name, double _default) {
	    return getFloatProperty(prop, name, (float)_default);
	  }
	
	  protected static double getDoubleProperty(Properties prop, String name, double _default) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : Double.parseDouble(s.trim());
	  }
	
	  protected static int[] getIntArray(Properties prop, String name, int[] _default) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : parseCommaSeparatedIntegerArray(s.trim());
	  }
	
	  protected static float[] getFloatArray(Properties prop, String name, float[] _default) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : parseCommaSeparatedFloatArray(s.trim());
	  }
	
	  protected static String[] getStringArray(Properties prop, String name, String[] _default, String delimiter) {
	    String s = getProperty(prop, name);
	    return s == null ? _default : s.split(delimiter);
	  }
	
	  protected static String[] getStringArray(Properties prop, String name, String[] _default) {
	    return getStringArray(prop, name, _default, ",");
	  }

	  protected static float[] parseCommaSeparatedFloatArray(String s)
	  {
		  if (s.isEmpty())
			  return new float[0];
		  String[] tmp = s.replaceAll(",", ";").split(";");
		  float[] ret = new float[tmp.length];

		  for (int i = 0; i < tmp.length; i++)
			  ret[i] = Float.parseFloat(tmp[i]);

		  return ret;
	  }
	
	  protected static int[] parseCommaSeparatedIntegerArray(String s)
	  {
		  if (s.isEmpty())
			  return new int[0];

		  String[] tmp = s.replaceAll(",", ";").split(";");
		  int[] ret = new int[tmp.length];
		  for (int i = 0; i < tmp.length; i++)
			  ret[i] = Integer.parseInt(tmp[i]);

		  return ret;
	  }

}