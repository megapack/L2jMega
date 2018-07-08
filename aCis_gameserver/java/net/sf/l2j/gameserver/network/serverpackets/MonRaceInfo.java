package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Npc;

/**
 * format : dddc dddh (ddc)
 */
public class MonRaceInfo extends L2GameServerPacket
{
	private final int _unknown1;
	private final int _unknown2;
	private final Npc[] _monsters;
	private final int[][] _speeds;
	
	public MonRaceInfo(int unknown1, int unknown2, Npc[] monsters, int[][] speeds)
	{
		/*
		 * -1 0 to initial the race 0 15322 to start race 13765 -1 in middle of race -1 0 to end the race
		 */
		_unknown1 = unknown1;
		_unknown2 = unknown2;
		_monsters = monsters;
		_speeds = speeds;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdd);
		
		writeD(_unknown1);
		writeD(_unknown2);
		writeD(8);
		
		for (int i = 0; i < 8; i++)
		{
			writeD(_monsters[i].getObjectId());
			writeD(_monsters[i].getTemplate().getNpcId() + 1000000);
			writeD(14107); // origin X
			writeD(181875 + (58 * (7 - i))); // origin Y
			writeD(-3566); // origin Z
			writeD(12080); // end X
			writeD(181875 + (58 * (7 - i))); // end Y
			writeD(-3566); // end Z
			writeF(_monsters[i].getCollisionHeight());
			writeF(_monsters[i].getCollisionRadius());
			writeD(120); // ?? unknown
			
			for (int j = 0; j < 20; j++)
			{
				if (_unknown1 == 0)
					writeC(_speeds[i][j]);
				else
					writeC(0);
			}
			writeD(0);
		}
	}
}