package hwid.hwidmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.network.L2GameClient;


public class HWIDManager
{
	protected static Logger	_log = Logger.getLogger(HWIDManager.class.getName());
	private static HWIDManager _instance;
	public static Map<Integer, HWIDInfoList> _listHWID;
	
	public HWIDManager()
	{
		_listHWID = new HashMap<>();
		load();
		System.out.println("- Hwid Info: Loaded " + _listHWID.size() + " Hwids");
	}
	
	public static HWIDManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new HWIDManager();
		}
		return _instance;
	}
	
	private static void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
				PreparedStatement statement = con.prepareStatement("SELECT * FROM hwid_info");
				ResultSet rset = statement.executeQuery();
				int counterHWIDInfo = 0;
				while (rset.next())
				{
					HWIDInfoList hInfo = new HWIDInfoList(counterHWIDInfo);
					hInfo.setHwids(rset.getString("HWID"));
					hInfo.setCount(rset.getInt("WindowsCount"));
					hInfo.setLogin(rset.getString("Account"));
					hInfo.setPlayerID(rset.getInt("PlayerID"));
					hInfo.setLockType(HWIDInfoList.LockType.valueOf(rset.getString("LockType")));
					_listHWID.put(Integer.valueOf(counterHWIDInfo), hInfo);
					counterHWIDInfo++;
				}
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	
	public static void reload()
	{
		_instance = new HWIDManager();
	}
	
	public static void updateHWIDInfo(L2GameClient client, int windowscount)
	{
		updateHWIDInfo(client, windowscount, HWIDInfoList.LockType.NONE);
	}
	

	public static void updateHWIDInfo(L2GameClient client, int windowsCount, HWIDInfoList.LockType lockType)
	{
		int counterHwidInfo = _listHWID.size();
		boolean isFound = false;
		
		for (int i = 0; i < _listHWID.size(); i++)
		{
			if (!_listHWID.get(Integer.valueOf(i)).getHWID().equals(client.getHWID()))
				continue;

			isFound = true;
			counterHwidInfo = i;
			break;
		}
		
		HWIDInfoList hInfo = new HWIDInfoList(counterHwidInfo);
		hInfo.setHwids(client.getHWID());
		hInfo.setCount(windowsCount);
		hInfo.setLogin(client.getAccountName());
		hInfo.setPlayerID(client.getPlayerId());
		hInfo.setLockType(lockType);
		_listHWID.put(Integer.valueOf(counterHwidInfo), hInfo);
		
		if (isFound)
		{
			try	(Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
					PreparedStatement statement = con.prepareStatement("UPDATE hwid_info SET WindowsCount=?,Account=?,PlayerID=?,LockType=? WHERE HWID=?");
					statement.setInt(1, windowsCount);
					statement.setString(2, client.getAccountName());
					statement.setInt(3, client.getPlayerId());
					statement.setString(4, lockType.toString());
					statement.setString(5, client.getHWID());
					statement.execute();
					statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{

				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("INSERT INTO hwid_info (HWID, WindowsCount, Account, PlayerID, LockType) values (?,?,?,?,?)");
					statement.setString(1, client.getHWID());
					statement.setInt(2, windowsCount);
					statement.setString(3, client.getAccountName());
					statement.setInt(4, client.getPlayerId());
					statement.setString(5, lockType.toString());
					statement.execute();
				    statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void updateHWIDInfo(L2GameClient client, HWIDInfoList.LockType lockType)
	{
		updateHWIDInfo(client, 1, lockType);
	}
	
	public static boolean checkLockedHWID(L2GameClient client)
	{
		if (_listHWID.size() == 0)
			return false;

		boolean result = false;
		
		for (int i = 0; i < _listHWID.size(); i++)
		{
			switch (_listHWID.get(Integer.valueOf(i)).getLockType().ordinal())
			{
				case 1:
					break;
				case 2:
					if ((client.getPlayerId() == 0) || (_listHWID.get(Integer.valueOf(i)).getPlayerID() != client.getPlayerId()))
						continue;

					if (_listHWID.get(Integer.valueOf(i)).getHWID().equals(client.getHWID()))
						return false;

					result = true;
					break;
				case 3:
					if (!_listHWID.get(Integer.valueOf(i)).getLogin().equals(client.getLoginName()))
						continue;
	
					if (_listHWID.get(Integer.valueOf(i)).getHWID().equals(client.getHWID()))
						return false;

					result = true;
			}
			
		}
		
		return result;
	}
	
	public static int getAllowedWindowsCount(L2GameClient client)
	{
		if (_listHWID.size() == 0)
			return -1;

		for (int i = 0; i < _listHWID.size(); i++)
		{
			if (!_listHWID.get(Integer.valueOf(i)).getHWID().equals(client.getHWID()))
				continue;

			if (_listHWID.get(Integer.valueOf(i)).getHWID().equals(""))
				return -1;

			return _listHWID.get(Integer.valueOf(i)).getCount();
		}
		
		return -1;
	}
	
	public static int getCountHwidInfo()
	{
		return _listHWID.size();
	}
}