package net.sf.l2j.gameserver.data.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.XMLDocument;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.CabalType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.item.MercenaryTicket;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.location.TowerSpawnLocation;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Loads and stores {@link Castle}s informations, using database and XML informations.
 */
public final class CastleManager extends XMLDocument
{
	private static final String LOAD_CASTLES = "SELECT * FROM castle ORDER BY id";
	private static final String LOAD_OWNER = "SELECT clan_id FROM clan_data WHERE hasCastle=?";
	private static final String RESET_CERTIFICATES = "UPDATE castle SET certificates=300";
	
	private final Map<Integer, Castle> _castles = new HashMap<>();
	
	protected CastleManager()
	{
		// Generate Castle objects with dynamic data.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_CASTLES);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final int id = rs.getInt("id");
				final Castle castle = new Castle(id, rs.getString("name"));
				
				castle.setSiegeDate(Calendar.getInstance());
				castle.getSiegeDate().setTimeInMillis(rs.getLong("siegeDate"));
				castle.setTimeRegistrationOver(rs.getBoolean("regTimeOver"));
				castle.setTaxPercent(rs.getInt("taxPercent"), false);
				castle.setTreasury(rs.getLong("treasury"));
				castle.setLeftCertificates(rs.getInt("certificates"), false);
				
				try (PreparedStatement ps1 = con.prepareStatement(LOAD_OWNER))
				{
					ps1.setInt(1, id);
					try (ResultSet rs1 = ps1.executeQuery())
					{
						while (rs1.next())
						{
							final int ownerId = rs1.getInt("clan_id");
							if (ownerId > 0)
							{
								final Clan clan = ClanTable.getInstance().getClan(ownerId);
								if (clan != null)
									castle.setOwnerId(ownerId);
							}
						}
					}
				}
				
				_castles.put(id, castle);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to load castles.", e);
		}
		
		// Feed Castle objects with static data.
		load();
		
		// Load traps informations. Generate siege entities for every castle (if not handled, it's only processed during player login).
		for (Castle castle : _castles.values())
		{
			castle.loadTrapUpgrade();
			castle.setSiege(new Siege(castle));
		}
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/castles.xml");
		LOGGER.info("Loaded {} castles.", _castles.size());
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
			if (!"castle".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			NamedNodeMap attrs = o.getAttributes();
			
			final Castle castle = _castles.get(Integer.parseInt(attrs.getNamedItem("id").getNodeValue()));
			if (castle == null)
				continue;
			
			castle.setCircletId(Integer.parseInt(attrs.getNamedItem("circletId").getNodeValue()));
			
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if ("artifact".equalsIgnoreCase(d.getNodeName()))
					castle.setArtifacts(d.getAttributes().getNamedItem("val").getNodeValue());
				else if ("controlTowers".equalsIgnoreCase(d.getNodeName()))
				{
					attrs = d.getAttributes();
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if (!"tower".equalsIgnoreCase(e.getNodeName()))
							continue;
						
						attrs = e.getAttributes();
						
						final String[] location = attrs.getNamedItem("loc").getNodeValue().split(",");
						
						castle.getControlTowers().add(new TowerSpawnLocation(13002, new SpawnLocation(Integer.parseInt(location[0]), Integer.parseInt(location[1]), Integer.parseInt(location[2]), -1)));
					}
				}
				else if ("flameTowers".equalsIgnoreCase(d.getNodeName()))
				{
					attrs = d.getAttributes();
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if (!"tower".equalsIgnoreCase(e.getNodeName()))
							continue;
						
						attrs = e.getAttributes();
						
						final String[] location = attrs.getNamedItem("loc").getNodeValue().split(",");
						final String[] zoneIds = attrs.getNamedItem("zones").getNodeValue().split(",");
						
						castle.getFlameTowers().add(new TowerSpawnLocation(13004, new SpawnLocation(Integer.parseInt(location[0]), Integer.parseInt(location[1]), Integer.parseInt(location[2]), -1), zoneIds));
					}
				}
				else if ("relatedNpcIds".equalsIgnoreCase(d.getNodeName()))
					castle.setRelatedNpcIds(d.getAttributes().getNamedItem("val").getNodeValue());
				else if ("tickets".equalsIgnoreCase(d.getNodeName()))
				{
					attrs = d.getAttributes();
					for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
					{
						if (!"ticket".equalsIgnoreCase(e.getNodeName()))
							continue;
						
						attrs = e.getAttributes();
						
						set.set("itemId", Integer.valueOf(attrs.getNamedItem("itemId").getNodeValue()));
						set.set("type", attrs.getNamedItem("type").getNodeValue());
						set.set("stationary", Boolean.valueOf(attrs.getNamedItem("stationary").getNodeValue()));
						set.set("npcId", Integer.valueOf(attrs.getNamedItem("npcId").getNodeValue()));
						set.set("maxAmount", Integer.valueOf(attrs.getNamedItem("maxAmount").getNodeValue()));
						set.set("ssq", attrs.getNamedItem("ssq").getNodeValue());
						
						castle.getTickets().add(new MercenaryTicket(set));
						set.clear();
					}
				}
			}
		}
	}
	
	public Castle getCastleById(int castleId)
	{
		return _castles.get(castleId);
	}
	
	public Castle getCastleByOwner(Clan clan)
	{
		return _castles.values().stream().filter(c -> c.getOwnerId() == clan.getClanId()).findFirst().orElse(null);
	}
	
	public Castle getCastleByName(String name)
	{
		return _castles.values().stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
	public Castle getCastle(int x, int y, int z)
	{
		return _castles.values().stream().filter(c -> c.checkIfInZone(x, y, z)).findFirst().orElse(null);
	}
	
	public Castle getCastle(WorldObject object)
	{
		return getCastle(object.getX(), object.getY(), object.getZ());
	}
	
	public Collection<Castle> getCastles()
	{
		return _castles.values();
	}
	
	public void validateTaxes(CabalType sealStrifeOwner)
	{
		int maxTax;
		switch (sealStrifeOwner)
		{
			case DAWN:
				maxTax = 25;
				break;
			
			case DUSK:
				maxTax = 5;
				break;
			
			default:
				maxTax = 15;
				break;
		}
		
		_castles.values().stream().filter(c -> c.getTaxPercent() > maxTax).forEach(c -> c.setTaxPercent(maxTax, true));
	}
	
	public Siege getActiveSiege(WorldObject object)
	{
		return getActiveSiege(object.getX(), object.getY(), object.getZ());
	}
	
	public Siege getActiveSiege(int x, int y, int z)
	{
		for (Castle castle : _castles.values())
			if (castle.getSiege().checkIfInZone(x, y, z))
				return castle.getSiege();
			
		return null;
	}
	
	/**
	 * Reset all castles certificates. Reset the memory value, and run a unique query.
	 */
	public void resetCertificates()
	{
		// Reset memory. Don't use the inner save.
		for (Castle castle : _castles.values())
			castle.setLeftCertificates(300, false);
		
		// Update all castles with a single query.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESET_CERTIFICATES))
		{
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to reset certificates.", e);
		}
	}
	
	public static final CastleManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		protected static final CastleManager INSTANCE = new CastleManager();
	}
}