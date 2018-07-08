package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.CabalType;

public class SSQInfo extends L2GameServerPacket
{
	public static final SSQInfo REGULAR_SKY_PACKET = new SSQInfo(256);
	public static final SSQInfo DUSK_SKY_PACKET = new SSQInfo(257);
	public static final SSQInfo DAWN_SKY_PACKET = new SSQInfo(258);
	public static final SSQInfo RED_SKY_PACKET = new SSQInfo(259);
	
	private final int _state;
	
	public static SSQInfo sendSky()
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			final CabalType winningCabal = SevenSigns.getInstance().getCabalHighestScore();
			if (winningCabal == CabalType.DAWN)
				return DAWN_SKY_PACKET;
			
			if (winningCabal == CabalType.DUSK)
				return DUSK_SKY_PACKET;
		}
		return REGULAR_SKY_PACKET;
	}
	
	private SSQInfo(int state)
	{
		_state = state;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf8);
		writeH(_state);
	}
}