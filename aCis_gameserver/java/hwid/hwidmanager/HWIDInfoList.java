package hwid.hwidmanager;

public class HWIDInfoList
{
	private final int _id;
	private String HWID;
	private int count;
	private int playerID;
	private String login;
	private LockType lockType;
	
	public HWIDInfoList(int id)
	{
		this._id = id;
	}
	
	public int getCount()
	{
		return this.count;
	}
	
	public void setCount(int count)
	{
		this.count = count;
	}
	
	public int getPlayerID()
	{
		return this.playerID;
	}
	
	public void setPlayerID(int playerID)
	{
		this.playerID = playerID;
	}
	
	public String getHWID()
	{
		return this.HWID;
	}
	
	public void setLogin(String login)
	{
		this.login = login;
	}
	
	public void setHWID(String HWID)
	{
		this.HWID = HWID;
	}
	
	public LockType getLockType()
	{
		return this.lockType;
	}
	
	public String getLogin()
	{
		return this.login;
	}
	
	public void setLockType(LockType lockType)
	{
		this.lockType = lockType;
	}
	
	public int get_id()
	{
		return this._id;
	}
	
	public void setHwids(String hwid)
	{
		this.HWID = hwid;
		this.count = 1;
	}
	
	public static enum LockType
	{
		PLAYER_LOCK,
		ACCOUNT_LOCK,
		NONE;
	}
}