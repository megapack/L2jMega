package net.sf.l2j.gameserver.instancemanager;

import java.awt.Polygon;
import java.awt.Shape;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Thanks to L2Fortress and balancer.ru - kombat
 */
public class DimensionalRiftManager
{
	private static Logger _log = Logger.getLogger(DimensionalRiftManager.class.getName());
	
	private static final Map<Byte, HashMap<Byte, DimensionalRiftRoom>> _rooms = new HashMap<>(7);
	private static final int DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;
	
	public static DimensionalRiftManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected DimensionalRiftManager()
	{
		loadData();
	}
	
	public DimensionalRiftRoom getRoom(byte type, byte room)
	{
		return _rooms.get(type) == null ? null : _rooms.get(type).get(room);
	}
	
	private static void loadData()
	{
		int countGood = 0, countBad = 0;
		try
		{
			File file = new File("./data/xml/dimensional_rift.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(file);
			
			for (Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling())
			{
				if ("rift".equalsIgnoreCase(rift.getNodeName()))
				{
					for (Node area = rift.getFirstChild(); area != null; area = area.getNextSibling())
					{
						if ("area".equalsIgnoreCase(area.getNodeName()))
						{
							NamedNodeMap attrs = area.getAttributes();
							byte type = Byte.parseByte(attrs.getNamedItem("type").getNodeValue());
							
							for (Node room = area.getFirstChild(); room != null; room = room.getNextSibling())
							{
								if ("room".equalsIgnoreCase(room.getNodeName()))
								{
									attrs = room.getAttributes();
									byte roomId = Byte.parseByte(attrs.getNamedItem("id").getNodeValue());
									
									int xMin = Integer.parseInt(attrs.getNamedItem("xMin").getNodeValue());
									int xMax = Integer.parseInt(attrs.getNamedItem("xMax").getNodeValue());
									int yMin = Integer.parseInt(attrs.getNamedItem("yMin").getNodeValue());
									int yMax = Integer.parseInt(attrs.getNamedItem("yMax").getNodeValue());
									int xT = Integer.parseInt(attrs.getNamedItem("xT").getNodeValue());
									int yT = Integer.parseInt(attrs.getNamedItem("yT").getNodeValue());
									
									if (!_rooms.containsKey(type))
										_rooms.put(type, new HashMap<Byte, DimensionalRiftRoom>(9));
									
									_rooms.get(type).put(roomId, new DimensionalRiftRoom(type, roomId, xMin, xMax, yMin, yMax, xT, yT));
									
									for (Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling())
									{
										if ("spawn".equalsIgnoreCase(spawn.getNodeName()))
										{
											attrs = spawn.getAttributes();
											int mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
											int delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
											int count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
											
											NpcTemplate template = NpcData.getInstance().getTemplate(mobId);
											if (template == null)
												_log.log(Level.WARNING, "Template " + mobId + " not found!");
											if (!_rooms.containsKey(type))
												_log.log(Level.WARNING, "Type " + type + " not found!");
											else if (!_rooms.get(type).containsKey(roomId))
												_log.log(Level.WARNING, "Room " + roomId + " in Type " + type + " not found!");
											
											for (int i = 0; i < count; i++)
											{
												DimensionalRiftRoom riftRoom = _rooms.get(type).get(roomId);
												int x = riftRoom.getRandomX();
												int y = riftRoom.getRandomY();
												int z = riftRoom.getTeleportCoords()[2];
												
												if (template != null && _rooms.containsKey(type) && _rooms.get(type).containsKey(roomId))
												{
													L2Spawn spawnDat = new L2Spawn(template);
													spawnDat.setLoc(x, y, z, -1);
													spawnDat.setRespawnDelay(delay);
													SpawnTable.getInstance().addNewSpawn(spawnDat, false);
													_rooms.get(type).get(roomId).getSpawns().add(spawnDat);
													countGood++;
												}
												else
												{
													countBad++;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error on loading dimensional rift spawns: " + e);
		}
		
		int typeSize = _rooms.keySet().size();
		int roomSize = 0;
		
		for (byte b : _rooms.keySet())
			roomSize += _rooms.get(b).keySet().size();
		
		_log.info("DimensionalRiftManager: Loaded " + typeSize + " room types with " + roomSize + " rooms.");
		_log.info("DimensionalRiftManager: Loaded " + countGood + " dimensional rift spawns, " + countBad + " errors.");
	}
	
	public void reload()
	{
		for (byte b : _rooms.keySet())
		{
			for (byte i : _rooms.get(b).keySet())
				_rooms.get(b).get(i).getSpawns().clear();
			
			_rooms.get(b).clear();
		}
		_rooms.clear();
		loadData();
	}
	
	public boolean checkIfInRiftZone(int x, int y, int z, boolean ignorePeaceZone)
	{
		if (ignorePeaceZone)
			return _rooms.get((byte) 0).get((byte) 1).checkIfInZone(x, y, z);
		
		return _rooms.get((byte) 0).get((byte) 1).checkIfInZone(x, y, z) && !_rooms.get((byte) 0).get((byte) 0).checkIfInZone(x, y, z);
	}
	
	public boolean checkIfInPeaceZone(int x, int y, int z)
	{
		return _rooms.get((byte) 0).get((byte) 0).checkIfInZone(x, y, z);
	}
	
	public void teleportToWaitingRoom(Player player)
	{
		int[] coords = getRoom((byte) 0, (byte) 0).getTeleportCoords();
		player.teleToLocation(coords[0], coords[1], coords[2], 0);
	}
	
	public synchronized void start(Player player, byte type, Npc npc)
	{
		final Party party = player.getParty();
		
		// No party.
		if (party == null)
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc);
			return;
		}
		
		// Player isn't the party leader.
		if (!party.isLeader(player))
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		// Party is already in rift.
		if (party.isInDimensionalRift())
			return;
		
		// Party members' count is lower than config.
		if (party.getMembersCount() < Config.RIFT_MIN_PARTY_SIZE)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/SmallParty.htm");
			html.replace("%npc_name%", npc.getName());
			html.replace("%count%", Integer.toString(Config.RIFT_MIN_PARTY_SIZE));
			player.sendPacket(html);
			return;
		}
		
		// Rift is full.
		if (!isAllowedEnter(type))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/Full.htm");
			html.replace("%npc_name%", npc.getName());
			player.sendPacket(html);
			return;
		}
		
		// One of teammates isn't on peace zone or hasn't required amount of items.
		for (Player member : party.getMembers())
		{
			if (!checkIfInPeaceZone(member.getX(), member.getY(), member.getZ()))
			{
				showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc);
				return;
			}
		}
		
		ItemInstance i;
		final int count = getNeededItems(type);
		
		for (Player member : party.getMembers())
		{
			i = member.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
			
			if (i == null || i.getCount() < getNeededItems(type))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile("data/html/seven_signs/rift/NoFragments.htm");
				html.replace("%npc_name%", npc.getName());
				html.replace("%count%", Integer.toString(count));
				player.sendPacket(html);
				return;
			}
		}
		
		for (Player member : party.getMembers())
		{
			i = member.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
			if (!member.destroyItem("RiftEntrance", i, count, null, true))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile("data/html/seven_signs/rift/NoFragments.htm");
				html.replace("%npc_name%", npc.getName());
				html.replace("%count%", Integer.toString(count));
				player.sendPacket(html);
				return;
			}
		}
		
		byte room;
		List<Byte> emptyRooms;
		do
		{
			emptyRooms = getFreeRooms(type);
			room = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
			
			// Relaunch random number until another room than room boss popups.
			while (room == 9)
				room = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
		}
		// Find empty room
		while (_rooms.get(type).get(room).isPartyInside());
		
		// Creates an instance of the rift.
		new DimensionalRift(party, type, room);
	}
	
	public static class DimensionalRiftRoom
	{
		protected final byte _type;
		protected final byte _room;
		private final int _xMin;
		private final int _xMax;
		private final int _yMin;
		private final int _yMax;
		private final int[] _teleportCoords;
		private final Shape _s;
		private final boolean _isBossRoom;
		private final List<L2Spawn> _roomSpawns;
		protected final List<Npc> _roomMobs;
		private boolean _partyInside = false;
		
		public DimensionalRiftRoom(byte type, byte room, int xMin, int xMax, int yMin, int yMax, int xT, int yT)
		{
			_type = type;
			_room = room;
			_xMin = (xMin + 128);
			_xMax = (xMax - 128);
			_yMin = (yMin + 128);
			_yMax = (yMax - 128);
			
			_teleportCoords = new int[]
			{
				xT,
				yT,
				-6752
			};
			
			_isBossRoom = (room == 9);
			_roomSpawns = new ArrayList<>();
			_roomMobs = new ArrayList<>();
			
			_s = new Polygon(new int[]
			{
				xMin,
				xMax,
				xMax,
				xMin
			}, new int[]
			{
				yMin,
				yMin,
				yMax,
				yMax
			}, 4);
		}
		
		public int getRandomX()
		{
			return Rnd.get(_xMin, _xMax);
		}
		
		public int getRandomY()
		{
			return Rnd.get(_yMin, _yMax);
		}
		
		public int[] getTeleportCoords()
		{
			return _teleportCoords;
		}
		
		public boolean checkIfInZone(int x, int y, int z)
		{
			return _s.contains(x, y) && z >= -6816 && z <= -6240;
		}
		
		public boolean isBossRoom()
		{
			return _isBossRoom;
		}
		
		public List<L2Spawn> getSpawns()
		{
			return _roomSpawns;
		}
		
		public void spawn()
		{
			for (L2Spawn spawn : _roomSpawns)
			{
				spawn.doSpawn(false);
				spawn.setRespawnState(true);
			}
		}
		
		public void unspawn()
		{
			for (L2Spawn spawn : _roomSpawns)
			{
				spawn.setRespawnState(false);
				if (spawn.getNpc() != null)
					spawn.getNpc().deleteMe();
			}
			_partyInside = false;
		}
		
		public boolean isPartyInside()
		{
			return _partyInside;
		}
		
		public void setPartyInside(boolean partyInside)
		{
			_partyInside = partyInside;
		}
	}
	
	private static int getNeededItems(byte type)
	{
		switch (type)
		{
			case 1:
				return Config.RIFT_ENTER_COST_RECRUIT;
			case 2:
				return Config.RIFT_ENTER_COST_SOLDIER;
			case 3:
				return Config.RIFT_ENTER_COST_OFFICER;
			case 4:
				return Config.RIFT_ENTER_COST_CAPTAIN;
			case 5:
				return Config.RIFT_ENTER_COST_COMMANDER;
			case 6:
				return Config.RIFT_ENTER_COST_HERO;
			default:
				throw new IndexOutOfBoundsException();
		}
	}
	
	public void showHtmlFile(Player player, String file, Npc npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(file);
		html.replace("%npc_name%", npc.getName());
		player.sendPacket(html);
	}
	
	public boolean isAllowedEnter(byte type)
	{
		int count = 0;
		for (DimensionalRiftRoom room : _rooms.get(type).values())
		{
			if (room.isPartyInside())
				count++;
		}
		return count < (_rooms.get(type).size() - 1);
	}
	
	public List<Byte> getFreeRooms(byte type)
	{
		List<Byte> list = new ArrayList<>();
		for (DimensionalRiftRoom room : _rooms.get(type).values())
		{
			if (!room.isPartyInside())
				list.add(room._room);
		}
		return list;
	}
	
	public void onPartyEdit(Party party)
	{
		if (party == null)
			return;
		
		final DimensionalRift rift = party.getDimensionalRift();
		if (rift != null)
		{
			for (Player member : party.getMembers())
				teleportToWaitingRoom(member);
			
			rift.killRift();
		}
	}
	
	private static class SingletonHolder
	{
		protected static final DimensionalRiftManager _instance = new DimensionalRiftManager();
	}
}