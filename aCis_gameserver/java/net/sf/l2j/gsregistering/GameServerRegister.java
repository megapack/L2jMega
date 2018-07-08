package net.sf.l2j.gsregistering;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.loginserver.GameServerTable;
import net.sf.l2j.loginserver.model.GameServerInfo;

public class GameServerRegister
{
	private static String _choice;
	
	public static void main(String[] args)
	{
		Config.loadGameServerRegistration();
		
		try (Scanner _scn = new Scanner(System.in))
		{
			System.out.println();
			System.out.println();
			System.out.println("                        aCis gameserver registering");
			System.out.println("                        ____________________________");
			System.out.println();
			System.out.println("OPTIONS : a number : register a server ID, if available and existing on list.");
			System.out.println("          list : get a list of IDs. A '*' means the id is already used.");
			System.out.println("          clean : unregister a specified gameserver.");
			System.out.println("          cleanall : unregister all gameservers.");
			System.out.println("          exit : exit the program.");
			
			while (true)
			{
				System.out.println();
				System.out.print("Your choice? ");
				_choice = _scn.next();
				
				if (_choice.equalsIgnoreCase("list"))
				{
					System.out.println();
					for (Map.Entry<Integer, String> entry : GameServerTable.getInstance().getServerNames().entrySet())
						System.out.println(entry.getKey() + ": " + entry.getValue() + " " + (GameServerTable.getInstance().getRegisteredGameServers().containsKey(entry.getKey()) ? "*" : ""));
				}
				else if (_choice.equalsIgnoreCase("clean"))
				{
					System.out.println();
					
					if (GameServerTable.getInstance().getServerNames().isEmpty())
						System.out.println("No server names available, be sure 'servername.xml' is in the LoginServer directory.");
					else
					{
						System.out.println("UNREGISTER a specific server. Here's the current list :");
						for (GameServerInfo entry : GameServerTable.getInstance().getRegisteredGameServers().values())
							System.out.println(entry.getId() + ": " + GameServerTable.getInstance().getServerNames().get(entry.getId()));
						
						System.out.println();
						System.out.print("Your choice? ");
						
						_choice = _scn.next();
						try
						{
							final int id = Integer.parseInt(_choice);
							
							if (!GameServerTable.getInstance().getRegisteredGameServers().containsKey(id))
								System.out.println("This server id isn't used.");
							else
							{
								try (Connection con = L2DatabaseFactory.getInstance().getConnection())
								{
									PreparedStatement statement = con.prepareStatement("DELETE FROM gameservers WHERE server_id=?");
									statement.setInt(1, id);
									statement.executeUpdate();
									statement.close();
								}
								catch (SQLException e)
								{
									System.out.println("SQL error while cleaning registered server: " + e);
								}
								GameServerTable.getInstance().getRegisteredGameServers().remove(id);
								
								System.out.println("You successfully dropped gameserver #" + id + ".");
							}
						}
						catch (NumberFormatException nfe)
						{
							System.out.println("Type a valid server id.");
						}
					}
				}
				else if (_choice.equalsIgnoreCase("cleanall"))
				{
					System.out.println();
					System.out.print("UNREGISTER ALL servers. Are you sure? (y/n) ");
					
					_choice = _scn.next();
					
					if (_choice.equals("y"))
					{
						try (Connection con = L2DatabaseFactory.getInstance().getConnection())
						{
							PreparedStatement statement = con.prepareStatement("DELETE FROM gameservers");
							statement.executeUpdate();
							statement.close();
						}
						catch (SQLException e)
						{
							System.out.println("SQL error while cleaning registered servers: " + e);
						}
						GameServerTable.getInstance().getRegisteredGameServers().clear();
						
						System.out.println("You successfully dropped all registered gameservers.");
					}
					else
						System.out.println("'cleanall' processus has been aborted.");
				}
				else if (_choice.equalsIgnoreCase("exit"))
					System.exit(0);
				else
				{
					try
					{
						System.out.println();
						
						if (GameServerTable.getInstance().getServerNames().isEmpty())
							System.out.println("No server names available, be sure 'servername.xml' is in the LoginServer directory.");
						else
						{
							final int id = Integer.parseInt(_choice);
							
							if (GameServerTable.getInstance().getServerNames().get(id) == null)
								System.out.println("No name for server id: " + id + ".");
							else if (GameServerTable.getInstance().getRegisteredGameServers().containsKey(id))
								System.out.println("This server id is already used.");
							else
							{
								byte[] hexId = LoginServerThread.generateHex(16);
								
								GameServerTable.getInstance().getRegisteredGameServers().put(id, new GameServerInfo(id, hexId));
								GameServerTable.getInstance().registerServerOnDB(hexId, id, "");
								Config.saveHexid(id, new BigInteger(hexId).toString(16), "hexid(server " + id + ").txt");
								
								System.out.println("Server registered under 'hexid(server " + id + ").txt'.");
								System.out.println("Put this file in /config gameserver folder and rename it 'hexid.txt'.");
							}
						}
					}
					catch (NumberFormatException nfe)
					{
						System.out.println("Type a number or list|clean|cleanall commands.");
					}
				}
			}
		}
	}
}