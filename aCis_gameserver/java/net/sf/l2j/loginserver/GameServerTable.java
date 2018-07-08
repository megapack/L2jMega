package net.sf.l2j.loginserver;

import java.io.File;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;
import net.sf.l2j.loginserver.model.GameServerInfo;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class GameServerTable
{
	private static final Logger _log = Logger.getLogger(GameServerTable.class.getName());
	
	private static final int KEYS_SIZE = 10;
	
	private final Map<Integer, String> _serverNames = new HashMap<>();
	private final Map<Integer, GameServerInfo> _gameServerTable = new ConcurrentHashMap<>();
	
	private KeyPair[] _keyPairs;
	
	protected GameServerTable()
	{
		loadServerNames();
		_log.info("Loaded " + _serverNames.size() + " server names.");
		
		loadRegisteredGameServers();
		_log.info("Loaded " + _gameServerTable.size() + " registered gameserver(s).");
		
		initRSAKeys();
		_log.info("Cached " + _keyPairs.length + " RSA keys for gameserver communication.");
	}
	
	private void initRSAKeys()
	{
		try
		{
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4));
			
			_keyPairs = new KeyPair[KEYS_SIZE];
			for (int i = 0; i < KEYS_SIZE; i++)
				_keyPairs[i] = keyGen.genKeyPair();
		}
		catch (Exception e)
		{
			_log.severe("GameServerTable: Error loading RSA keys for Game Server communication!");
		}
	}
	
	private void loadServerNames()
	{
		try
		{
			final File f = new File("servername.xml");
			final Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("server"))
				{
					NamedNodeMap attrs = d.getAttributes();
					
					int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					String name = attrs.getNamedItem("name").getNodeValue();
					
					_serverNames.put(id, name);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("GameServerTable: servername.xml could not be loaded.");
		}
	}
	
	private void loadRegisteredGameServers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM gameservers");
			ResultSet rs = ps.executeQuery();
			
			while (rs.next())
			{
				final int id = rs.getInt("server_id");
				
				_gameServerTable.put(id, new GameServerInfo(id, stringToHex(rs.getString("hexid"))));
			}
			rs.close();
			ps.close();
		}
		catch (Exception e)
		{
			_log.severe("GameServerTable: Error loading registered game servers!");
		}
	}
	
	public Map<Integer, GameServerInfo> getRegisteredGameServers()
	{
		return _gameServerTable;
	}
	
	public boolean registerWithFirstAvailableId(GameServerInfo gsi)
	{
		for (int id : _serverNames.keySet())
		{
			if (!_gameServerTable.containsKey(id))
			{
				_gameServerTable.put(id, gsi);
				gsi.setId(id);
				return true;
			}
		}
		return false;
	}
	
	public boolean register(int id, GameServerInfo gsi)
	{
		if (!_gameServerTable.containsKey(id))
		{
			_gameServerTable.put(id, gsi);
			gsi.setId(id);
			return true;
		}
		return false;
	}
	
	public void registerServerOnDB(GameServerInfo gsi)
	{
		registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getHostName());
	}
	
	public void registerServerOnDB(byte[] hexId, int id, String hostName)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)");
			ps.setString(1, hexToString(hexId));
			ps.setInt(2, id);
			ps.setString(3, hostName);
			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e)
		{
			_log.warning("GameServerTable: SQL error while saving gameserver: " + e);
		}
	}
	
	public Map<Integer, String> getServerNames()
	{
		return _serverNames;
	}
	
	public KeyPair getKeyPair()
	{
		return _keyPairs[Rnd.get(10)];
	}
	
	private static byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}
	
	private static String hexToString(byte[] hex)
	{
		return (hex == null) ? "null" : new BigInteger(hex).toString(16);
	}
	
	public static GameServerTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GameServerTable INSTANCE = new GameServerTable();
	}
}