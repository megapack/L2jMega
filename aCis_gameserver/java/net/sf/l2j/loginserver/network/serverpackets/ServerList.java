package net.sf.l2j.loginserver.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.loginserver.GameServerTable;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.model.ServerData;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.gameserverpackets.ServerStatus;

public final class ServerList extends L2LoginServerPacket
{
	private final List<ServerData> _servers = new ArrayList<>();
	private final int _lastServer;
	
	public ServerList(LoginClient client)
	{
		_lastServer = client.getLastServer();
		
		for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			final int status = (gsi.getStatus() != ServerStatus.STATUS_GM_ONLY) ? gsi.getStatus() : (client.getAccessLevel() > 0) ? gsi.getStatus() : ServerStatus.STATUS_DOWN;
			final String hostName = gsi.getHostName();
			
			_servers.add(new ServerData(status, hostName, gsi));
		}
	}
	
	@Override
	public void write()
	{
		writeC(0x04);
		writeC(_servers.size());
		writeC(_lastServer);
		
		for (ServerData server : _servers)
		{
			writeC(server.getServerId());
			
			try
			{
				final byte[] raw = InetAddress.getByName(server.getHostName()).getAddress();
				writeC(raw[0] & 0xff);
				writeC(raw[1] & 0xff);
				writeC(raw[2] & 0xff);
				writeC(raw[3] & 0xff);
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				writeC(127);
				writeC(0);
				writeC(0);
				writeC(1);
			}
			
			writeD(server.getPort());
			writeC(server.getAgeLimit());
			writeC(server.isPvp() ? 0x01 : 0x00);
			writeH(server.getCurrentPlayers());
			writeH(server.getMaxPlayers());
			writeC(server.getStatus() == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
			
			int bits = 0;
			if (server.isTestServer())
				bits |= 0x04;
			
			if (server.isShowingClock())
				bits |= 0x02;
			
			writeD(bits);
			writeC(server.isShowingBrackets() ? 0x01 : 0x00);
		}
	}
}