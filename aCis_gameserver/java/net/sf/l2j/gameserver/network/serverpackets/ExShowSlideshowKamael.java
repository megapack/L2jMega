package net.sf.l2j.gameserver.network.serverpackets;

/**
 * Format: ch
 * @author devScarlet & mrTJO
 */
public class ExShowSlideshowKamael extends L2GameServerPacket
{
	public static final ExShowSlideshowKamael STATIC_PACKET = new ExShowSlideshowKamael();
	
	private ExShowSlideshowKamael()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x5b);
	}
}