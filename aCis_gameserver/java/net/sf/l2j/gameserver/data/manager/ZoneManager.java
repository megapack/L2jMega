package net.sf.l2j.gameserver.data.manager;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.data.xml.XMLDocument;
import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.model.zone.form.ZoneCuboid;
import net.sf.l2j.gameserver.model.zone.form.ZoneCylinder;
import net.sf.l2j.gameserver.model.zone.form.ZoneNPoly;
import net.sf.l2j.gameserver.model.zone.type.BossZone;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Loads and stores zones, based on their {@link ZoneType}.
 */
public class ZoneManager extends XMLDocument
{
	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
	
	private final Map<Class<? extends ZoneType>, Map<Integer, ? extends ZoneType>> _zones = new HashMap<>();
	private final Map<Integer, ItemInstance> _debugItems = new ConcurrentHashMap<>();
	
	private int _lastDynamicId = 0;
	
	protected ZoneManager()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/zones");
		LOGGER.info("Loaded {} zones classes and total {} zones.", _zones.size(), _zones.values().stream().mapToInt(Map::size).sum());
	}
	
	@Override
	protected void parseDocument(Document doc, File file)
	{
		_lastDynamicId = (_lastDynamicId / 1000) * 1000 + 1000;
		
		final String zoneType = StringUtil.getNameWithoutExtension(file.getName());
		
		// Create the Constructor, based on file name. It is reused by every zone.
		Constructor<?> zoneConstructor;
		try
		{
			zoneConstructor = Class.forName("net.sf.l2j.gameserver.model.zone.type." + zoneType).getConstructor(int.class);
		}
		catch (Exception e)
		{
			LOGGER.error("The zone type {} doesn't exist. Abort zones loading for {}.", e, zoneType, file.getName());
			return;
		}
		
		// First element is never read.
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if (!"zone".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			NamedNodeMap attrs = o.getAttributes();
			
			final Node attribute = attrs.getNamedItem("id");
			final int zoneId = (attribute == null) ? _lastDynamicId++ : Integer.parseInt(attribute.getNodeValue());
			
			final String zoneShape = attrs.getNamedItem("shape").getNodeValue();
			
			final int minZ = Integer.parseInt(attrs.getNamedItem("minZ").getNodeValue());
			final int maxZ = Integer.parseInt(attrs.getNamedItem("maxZ").getNodeValue());
			
			// Generate the zone Object, based on constructor and zoneId.
			final ZoneType temp;
			try
			{
				temp = (ZoneType) zoneConstructor.newInstance(zoneId);
			}
			catch (Exception e)
			{
				LOGGER.error("The zone id {} couldn't be instantiated.", e, zoneId);
				continue;
			}
			
			// Generate nodes list.
			final List<IntIntHolder> nodes = new ArrayList<>();
			
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if ("node".equalsIgnoreCase(d.getNodeName()))
				{
					attrs = d.getAttributes();
					nodes.add(new IntIntHolder(Integer.parseInt(attrs.getNamedItem("X").getNodeValue()), Integer.parseInt(attrs.getNamedItem("Y").getNodeValue())));
				}
				else if ("stat".equalsIgnoreCase(d.getNodeName()))
				{
					attrs = d.getAttributes();
					temp.setParameter(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("val").getNodeValue());
				}
				else if ("spawn".equalsIgnoreCase(d.getNodeName()) && temp instanceof SpawnZoneType)
				{
					attrs = d.getAttributes();
					int spawnX = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
					int spawnY = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
					int spawnZ = Integer.parseInt(attrs.getNamedItem("Z").getNodeValue());
					
					Node val = attrs.getNamedItem("isChaotic");
					if (val != null && Boolean.parseBoolean(val.getNodeValue()))
						((SpawnZoneType) temp).addChaoticSpawn(spawnX, spawnY, spawnZ);
					else
						((SpawnZoneType) temp).addSpawn(spawnX, spawnY, spawnZ);
				}
			}
			
			// No nodes have been found.
			if (nodes.isEmpty())
			{
				LOGGER.warn("Missing nodes for zone {} in file {}.", zoneId, zoneType);
				continue;
			}
			
			final IntIntHolder[] coords = nodes.toArray(new IntIntHolder[nodes.size()]);
			
			// Create the shape.
			switch (zoneShape)
			{
				case "Cuboid":
					if (coords.length == 2)
						temp.setZone(new ZoneCuboid(coords[0].getId(), coords[1].getId(), coords[0].getValue(), coords[1].getValue(), minZ, maxZ));
					else
					{
						LOGGER.warn("Missing cuboid nodes for zone {} in file {}.", zoneId, zoneType);
						continue;
					}
					break;
				
				case "NPoly":
					if (coords.length > 2)
					{
						final int[] aX = new int[coords.length];
						final int[] aY = new int[coords.length];
						
						for (int i = 0; i < coords.length; i++)
						{
							aX[i] = coords[i].getId();
							aY[i] = coords[i].getValue();
						}
						temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
					}
					else
					{
						LOGGER.warn("Missing NPoly nodes for zone {} in file {}.", zoneId, zoneType);
						continue;
					}
					break;
				
				case "Cylinder":
					final int zoneRad = Integer.parseInt(o.getAttributes().getNamedItem("rad").getNodeValue());
					if (coords.length == 1 && zoneRad > 0)
						temp.setZone(new ZoneCylinder(coords[0].getId(), coords[0].getValue(), minZ, maxZ, zoneRad));
					else
					{
						LOGGER.warn("Missing Cylinder nodes for zone {} in file {}.", zoneId, zoneType);
						continue;
					}
					break;
				
				default:
					LOGGER.warn("Unknown {} shape in file {}.", zoneShape, zoneType);
					continue;
			}
			
			// Add the zone to the _zones map.
			addZone(zoneId, temp);
			
			// Register the zone into any world region it intersects with.
			final WorldRegion[][] regions = World.getInstance().getWorldRegions();
			for (int x = 0; x < regions.length; x++)
			{
				final int xLoc = World.getRegionX(x);
				final int xLoc2 = World.getRegionX(x + 1);
				
				for (int y = 0; y < regions[x].length; y++)
				{
					if (temp.getZone().intersectsRectangle(xLoc, xLoc2, World.getRegionY(y), World.getRegionY(y + 1)))
						regions[x][y].addZone(temp);
				}
			}
		}
	}
	
	/**
	 * Reload zones using following steps :
	 * <ul>
	 * <li>Save boss zones data.</li>
	 * <li>Clean zones from all regions.</li>
	 * <li>Clear containers.</li>
	 * <li>Use the regular load process.</li>
	 * <li>Revalidate zones for all existing creatures.</li>
	 * </ul>
	 */
	public void reload()
	{
		// Save boss zones data.
		save();
		
		// Remove zones from world.
		for (WorldRegion[] regions : World.getInstance().getWorldRegions())
		{
			for (WorldRegion region : regions)
				region.getZones().clear();
		}
		
		// Clear _zones and _debugItems Maps.
		_zones.clear();
		clearDebugItems();
		
		// Load all zones.
		load();
		
		// Revalidate creatures in zones.
		for (WorldObject object : World.getInstance().getObjects())
		{
			if (object instanceof Creature)
				((Creature) object).revalidateZone(true);
		}
	}
	
	/**
	 * Save boss zone data.<br>
	 * <br>
	 * We first clear existing entries, than we save each zone data on database.
	 */
	public final void save()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// clear table first
			PreparedStatement ps = con.prepareStatement(DELETE_GRAND_BOSS_LIST);
			ps.executeUpdate();
			ps.close();
			
			// store actual data
			ps = con.prepareStatement(INSERT_GRAND_BOSS_LIST);
			for (ZoneType zone : _zones.get(BossZone.class).values())
			{
				for (int player : ((BossZone) zone).getAllowedPlayers())
				{
					ps.setInt(1, player);
					ps.setInt(2, zone.getId());
					ps.addBatch();
				}
			}
			ps.executeBatch();
			ps.close();
		}
		catch (Exception e)
		{
			LOGGER.error("Error storing boss zones.", e);
		}
		LOGGER.info("Saved boss zones data.");
	}
	
	/**
	 * Add a new zone into _zones {@link Map}. If the zone type doesn't exist, generate the entry first.
	 * @param id : The zone id to add.
	 * @param <T> : The {@link ZoneType} children class.
	 * @param zone : The zone to add.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> void addZone(Integer id, T zone)
	{
		Map<Integer, T> map = (Map<Integer, T>) _zones.get(zone.getClass());
		if (map == null)
		{
			map = new HashMap<>();
			map.put(id, zone);
			_zones.put(zone.getClass(), map);
		}
		else
			map.put(id, zone);
	}
	
	/**
	 * @param <T> : The {@link ZoneType} children class.
	 * @param type : The Class type to refer.
	 * @return all zones by {@link Class} type.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> Collection<T> getAllZones(Class<T> type)
	{
		return (Collection<T>) _zones.get(type).values();
	}
	
	/**
	 * @param id : The zone id to retrieve.
	 * @return the first zone matching id.
	 */
	public ZoneType getZoneById(int id)
	{
		for (Map<Integer, ? extends ZoneType> map : _zones.values())
		{
			if (map.containsKey(id))
				return map.get(id);
		}
		return null;
	}
	
	/**
	 * @param <T> : The {@link ZoneType} children class.
	 * @param id : The zone id to retrieve.
	 * @param type : The Class type to refer.
	 * @return a zone by id and {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZoneById(int id, Class<T> type)
	{
		return (T) _zones.get(type).get(id);
	}
	
	/**
	 * @param object : The object position to refer.
	 * @return all zones based on object position.
	 */
	public List<ZoneType> getZones(WorldObject object)
	{
		return getZones(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * @param <T> : The {@link ZoneType} children class.
	 * @param object : The object position to refer.
	 * @param type : The Class type to refer.
	 * @return a zone based on object position and zone {@link Class}.
	 */
	public <T extends ZoneType> T getZone(WorldObject object, Class<T> type)
	{
		if (object == null)
			return null;
		
		return getZone(object.getX(), object.getY(), object.getZ(), type);
	}
	
	/**
	 * @param x : The X location to check.
	 * @param y : The Y location to check.
	 * @return all zones on a 2D plane from given coordinates (no matter their {@link Class}).
	 */
	public List<ZoneType> getZones(int x, int y)
	{
		final List<ZoneType> temp = new ArrayList<>();
		for (ZoneType zone : World.getInstance().getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y))
				temp.add(zone);
		}
		return temp;
	}
	
	/**
	 * @param x : The X location to check.
	 * @param y : The Y location to check.
	 * @param z : The Z location to check.
	 * @return all zones on a 3D plane from given coordinates (no matter their {@link Class}).
	 */
	public List<ZoneType> getZones(int x, int y, int z)
	{
		final List<ZoneType> temp = new ArrayList<>();
		for (ZoneType zone : World.getInstance().getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y, z))
				temp.add(zone);
		}
		return temp;
	}
	
	/**
	 * @param <T> : The {@link ZoneType} children class.
	 * @param x : The X location to check.
	 * @param y : The Y location to check.
	 * @param z : The Z location to check.
	 * @param type : The Class type to refer.
	 * @return a zone based on given coordinates and its {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZone(int x, int y, int z, Class<T> type)
	{
		for (ZoneType zone : World.getInstance().getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y, z) && type.isInstance(zone))
				return (T) zone;
		}
		return null;
	}
	
	/**
	 * Add an {@link ItemInstance} on debug list. Used to visualize zones.
	 * @param item : The item to add.
	 */
	public void addDebugItem(ItemInstance item)
	{
		_debugItems.put(item.getObjectId(), item);
	}
	
	/**
	 * Remove all {@link ItemInstance} debug items from the world and clear _debugItems {@link Map}.
	 */
	public void clearDebugItems()
	{
		for (ItemInstance item : _debugItems.values())
			item.decayMe();
		
		_debugItems.clear();
	}
	
	public static final ZoneManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ZoneManager INSTANCE = new ZoneManager();
	}
}