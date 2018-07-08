package net.sf.l2j.gameserver.network.gameserverpackets;

import java.util.ArrayList;
import java.util.List;

public class ServerStatus extends GameServerBasePacket
{
	private final List<Attribute> _attributes;
	
	public static final String[] STATUS_STRING =
	{
		"Auto",
		"Good",
		"Normal",
		"Full",
		"Down",
		"Gm Only"
	};
	
	public static final int STATUS = 0x01;
	public static final int CLOCK = 0x02;
	public static final int BRACKETS = 0x03;
	public static final int AGE_LIMIT = 0x04;
	public static final int TEST_SERVER = 0x05;
	public static final int PVP_SERVER = 0x06;
	public static final int MAX_PLAYERS = 0x07;
	
	public static final int STATUS_AUTO = 0x00;
	public static final int STATUS_GOOD = 0x01;
	public static final int STATUS_NORMAL = 0x02;
	public static final int STATUS_FULL = 0x03;
	public static final int STATUS_DOWN = 0x04;
	public static final int STATUS_GM_ONLY = 0x05;
	
	public static final int ON = 0x01;
	public static final int OFF = 0x00;
	
	class Attribute
	{
		public int id;
		public int value;
		
		Attribute(int pId, int pValue)
		{
			id = pId;
			value = pValue;
		}
	}
	
	public ServerStatus()
	{
		_attributes = new ArrayList<>();
	}
	
	public void addAttribute(int id, int value)
	{
		_attributes.add(new Attribute(id, value));
	}
	
	public void addAttribute(int id, boolean onOrOff)
	{
		_attributes.add(new Attribute(id, (onOrOff) ? ServerStatus.ON : ServerStatus.OFF));
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(0x06);
		writeD(_attributes.size());
		for (Attribute temp : _attributes)
		{
			writeD(temp.id);
			writeD(temp.value);
		}
		
		return getBytes();
	}
}