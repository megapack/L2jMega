package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;
import net.sf.l2j.commons.geometry.Polygon;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.geoengine.geodata.ABlock;
import net.sf.l2j.gameserver.geoengine.geodata.GeoStructure;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.template.DoorTemplate;
import net.sf.l2j.gameserver.model.actor.template.DoorTemplate.DoorType;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class loads and stores {@link Door}s.<br>
 * <br>
 * The different informations help to generate a {@link DoorTemplate} and a GeoObject, then we create the Door instance itself. The spawn is made just after the initialization of this class to avoid NPEs.
 */
public class DoorData extends XMLDocument
{
	private final Map<Integer, Door> _doors = new HashMap<>();
	
	protected DoorData()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/doors.xml");
		LOGGER.info("Loaded {} doors templates.", _doors.size());
	}
	
	@Override
	protected void parseDocument(Document doc, File file)
	{
		// StatsSet used to feed informations. Cleaned on every entry.
		final StatsSet set = new StatsSet();
		
		// First element is never read.
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if (!"door".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			// Parse and feed content.
			parseAndFeed(o.getAttributes(), set);
			
			final int id = set.getInteger("id");
			
			int posX = 0;
			int posY = 0;
			int posZ = 0;
			
			List<int[]> coords = new ArrayList<>();
			int minX = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int minY = Integer.MAX_VALUE;
			int maxY = Integer.MIN_VALUE;
			
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				NamedNodeMap attrs = d.getAttributes();
				
				if ("castle".equalsIgnoreCase(d.getNodeName()))
				{
					set.set("castle", attrs.getNamedItem("id").getNodeValue());
				}
				else if ("position".equalsIgnoreCase(d.getNodeName()))
				{
					posX = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
					posY = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
					posZ = Integer.parseInt(attrs.getNamedItem("z").getNodeValue());
				}
				else if ("coordinates".equalsIgnoreCase(d.getNodeName()))
				{
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if (!e.getNodeName().equalsIgnoreCase("loc"))
							continue;
						
						attrs = e.getAttributes();
						int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
						int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
						
						coords.add(new int[]
						{
							x,
							y
						});
						
						minX = Math.min(minX, x);
						maxX = Math.max(maxX, x);
						minY = Math.min(minY, y);
						maxY = Math.max(maxY, y);
					}
				}
				else if ("stats".equalsIgnoreCase(d.getNodeName()) || "function".equalsIgnoreCase(d.getNodeName()))
				{
					// Parse and feed content.
					parseAndFeed(attrs, set);
				}
			}
			
			// create basic description of door, taking extended outer dimensions of door
			final int x = GeoEngine.getGeoX(minX) - 1;
			final int y = GeoEngine.getGeoY(minY) - 1;
			final int sizeX = (GeoEngine.getGeoX(maxX) + 1) - x + 1;
			final int sizeY = (GeoEngine.getGeoY(maxY) + 1) - y + 1;
			
			// check door Z and adjust it
			final int geoX = GeoEngine.getGeoX(posX);
			final int geoY = GeoEngine.getGeoY(posY);
			final int geoZ = GeoEngine.getInstance().getHeightNearest(geoX, geoY, posZ);
			final ABlock block = GeoEngine.getInstance().getBlock(geoX, geoY);
			
			final int i = block.getIndexAbove(geoX, geoY, geoZ);
			if (i != -1)
			{
				final int layerDiff = block.getHeight(i) - geoZ;
				if (set.getInteger("height") > layerDiff)
					set.set("height", layerDiff - GeoStructure.CELL_IGNORE_HEIGHT);
			}
			
			final int limit = set.getEnum("type", DoorType.class) == DoorType.WALL ? GeoStructure.CELL_IGNORE_HEIGHT * 4 : GeoStructure.CELL_IGNORE_HEIGHT;
			
			// create 2D door description and calculate limit coordinates
			final boolean[][] inside = new boolean[sizeX][sizeY];
			final Polygon polygon = new Polygon(id, coords);
			for (int ix = 0; ix < sizeX; ix++)
			{
				for (int iy = 0; iy < sizeY; iy++)
				{
					// get geodata coordinates
					int gx = x + ix;
					int gy = y + iy;
					
					// check layer height
					int z = GeoEngine.getInstance().getHeightNearest(gx, gy, posZ);
					if (Math.abs(z - posZ) > limit)
						continue;
					
					// get world coordinates
					int worldX = GeoEngine.getWorldX(gx);
					int worldY = GeoEngine.getWorldY(gy);
					
					// set inside flag
					cell:
					for (int wix = worldX - 6; wix <= worldX + 6; wix += 2)
					{
						for (int wiy = worldY - 6; wiy <= worldY + 6; wiy += 2)
						{
							if (polygon.isInside(wix, wiy))
							{
								inside[ix][iy] = true;
								break cell;
							}
						}
					}
				}
			}
			
			// set world coordinates
			set.set("posX", posX);
			set.set("posY", posY);
			set.set("posZ", posZ);
			
			// set geodata coordinates and geodata
			set.set("geoX", x);
			set.set("geoY", y);
			set.set("geoZ", geoZ);
			set.set("geoData", GeoEngine.calculateGeoObject(inside));
			
			// set other required stats as default value
			set.set("pAtk", 0);
			set.set("mAtk", 0);
			set.set("runSpd", 0);
			// default radius set to 16 - affects distance for melee attacks
			set.set("radius", 16);
			
			// create door template
			final DoorTemplate template = new DoorTemplate(set);
			
			// create door instance
			final Door door = new Door(IdFactory.getInstance().getNextId(), template);
			door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
			door.getPosition().set(posX, posY, posZ);
			
			_doors.put(door.getDoorId(), door);
			
			// Clear the StatsSet.
			set.clear();
		}
	}
	
	public final void reload()
	{
		for (Door door : _doors.values())
			door.openMe();
		
		_doors.clear();
		
		for (Castle castle : CastleManager.getInstance().getCastles())
			castle.getDoors().clear();
		
		load();
		spawn();
	}
	
	/**
	 * Spawns {@link Door}s into the world. If this door is associated to a {@link Castle}, we load door upgrade aswell.<br>
	 * <br>
	 * Note: keep as side-method, do not join to the load(). On initial load, the DoorTable.getInstance() is not initialized, yet Door is calling it during spawn process...causing NPE.
	 */
	public final void spawn()
	{
		// spawn doors
		for (Door door : _doors.values())
			door.spawnMe();
		
		// load doors upgrades
		for (Castle castle : CastleManager.getInstance().getCastles())
			castle.loadDoorUpgrade();
	}
	
	public Door getDoor(int id)
	{
		return _doors.get(id);
	}
	
	public Collection<Door> getDoors()
	{
		return _doors.values();
	}
	
	/**
     * author : MeGaPacK
	 * Open doors specified in configs
	 */
	public static void openDoors() 
	{
		for (int doorId : Config.DOORS_IDS_TO_OPEN ) 
		{
			Door doorInstance = DoorData.getInstance().getDoor( doorId );

			if ( doorInstance != null ) 
			{
				doorInstance.openMe();
			}
		}
	}
	
		/**
		 * Close doors specified in configs
		 */
	public static void closeDoors() 
		{
			for ( int doorId : Config.DOORS_IDS_TO_CLOSE ) 
			{
				Door doorInstance = DoorData.getInstance().getDoor( doorId );
	
				if ( doorInstance != null ) 
				{
					doorInstance.closeMe();
				}
			}
		}
	
	public static DoorData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DoorData INSTANCE = new DoorData();
	}
}