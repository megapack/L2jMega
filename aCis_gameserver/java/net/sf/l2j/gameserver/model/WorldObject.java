package net.sf.l2j.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

/**
 * Mother class of all interactive objects in the world (PC, NPC, Item...)
 */
public abstract class WorldObject
{
	public enum PolyType
	{
		ITEM,
		NPC,
		DEFAULT;
	}
	
	public static final Logger _log = Logger.getLogger(WorldObject.class.getName());
	
	private String _name;
	private int _objectId;
	
	private NpcTemplate _polyTemplate;
	private PolyType _polyType = PolyType.DEFAULT;
	private int _polyId;
	
	private SpawnLocation _position = new SpawnLocation(0, 0, 0, 0);
	private WorldRegion _region;
	
	private boolean _isVisible;
	
	public WorldObject(int objectId)
	{
		_objectId = objectId;
	}
	
	public void onAction(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onActionShift(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onForcedAttack(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onSpawn()
	{
	}
	
	/**
	 * Remove a WorldObject from the world.
	 */
	public void decayMe()
	{
		setRegion(null);
		
		World.getInstance().removeObject(this);
	}
	
	public void refreshID()
	{
		World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		_objectId = IdFactory.getInstance().getNextId();
	}
	
	/**
	 * Init the position of a WorldObject spawn and add it in the world as a visible object.
	 */
	public final void spawnMe()
	{
		_isVisible = true;
		
		setRegion(World.getInstance().getRegion(_position));
		
		World.getInstance().addObject(this);
		
		onSpawn();
	}
	
	public final void spawnMe(int x, int y, int z)
	{
		_position.set(MathUtil.limit(x, World.WORLD_X_MIN + 100, World.WORLD_X_MAX - 100), MathUtil.limit(y, World.WORLD_Y_MIN + 100, World.WORLD_Y_MAX - 100), z);
		
		spawnMe();
	}
	
	public boolean isAttackable()
	{
		return false;
	}
	
	/**
	 * @param attacker The target to make checks on.
	 * @return true or false, depending if the target is attackable or not.
	 */
	public abstract boolean isAutoAttackable(Creature attacker);
	
	/**
	 * A WorldObject is visible if <B>_isVisible</B> = true and <B>_worldregion</B> != null.
	 * @return the visibilty state of the WorldObject.
	 */
	public final boolean isVisible()
	{
		return _region != null && _isVisible;
	}
	
	public final void setIsVisible(boolean value)
	{
		_isVisible = value;
		
		if (!_isVisible)
			setRegion(null);
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public void setName(String value)
	{
		_name = value;
	}
	
	public final int getObjectId()
	{
		return _objectId;
	}
	
	public final NpcTemplate getPolyTemplate()
	{
		return _polyTemplate;
	}
	
	public final PolyType getPolyType()
	{
		return _polyType;
	}
	
	public final int getPolyId()
	{
		return _polyId;
	}
	
	public boolean polymorph(PolyType type, int id)
	{
		if (!(this instanceof Npc) && !(this instanceof Player))
			return false;
		
		if (type == PolyType.NPC)
		{
			final NpcTemplate template = NpcData.getInstance().getTemplate(id);
			if (template == null)
				return false;
			
			_polyTemplate = template;
		}
		else if (type == PolyType.ITEM)
		{
			if (ItemTable.getInstance().getTemplate(id) == null)
				return false;
		}
		else if (type == PolyType.DEFAULT)
			return false;
		
		_polyType = type;
		_polyId = id;
		
		decayMe();
		spawnMe();
		
		return true;
	}
	
	public void unpolymorph()
	{
		_polyTemplate = null;
		_polyType = PolyType.DEFAULT;
		_polyId = 0;
		
		decayMe();
		spawnMe();
	}
	
	public Player getActingPlayer()
	{
		return null;
	}
	
	/**
	 * Sends the Server->Client info packet for the object. Is Overridden in:
	 * <li>L2BoatInstance</li>
	 * <li>L2DoorInstance</li>
	 * <li>Player</li>
	 * <li>L2StaticObjectInstance</li>
	 * <li>L2Npc</li>
	 * <li>L2Summon</li>
	 * <li>ItemInstance</li>
	 * @param activeChar
	 */
	public void sendInfo(Player activeChar)
	{
		
	}
	
	/**
	 * Check if current object has charged shot.
	 * @param type of the shot to be checked.
	 * @return true if the object has charged shot.
	 */
	public boolean isChargedShot(ShotType type)
	{
		return false;
	}
	
	/**
	 * Charging shot into the current object.
	 * @param type Type of the shot to be (un)charged.
	 * @param charged True if we charge, false if we uncharge.
	 */
	public void setChargedShot(ShotType type, boolean charged)
	{
	}
	
	/**
	 * Try to recharge a shot.
	 * @param physical skill are using Soul shots.
	 * @param magical skill are using Spirit shots.
	 */
	public void rechargeShots(boolean physical, boolean magical)
	{
	}
	
	@Override
	public String toString()
	{
		return (getClass().getSimpleName() + ":" + getName() + "[" + getObjectId() + "]");
	}
	
	/**
	 * Check if the object is in the given zone Id.
	 * @param zone the zone Id to check
	 * @return {@code true} if the object is in that zone Id
	 */
	public boolean isInsideZone(ZoneId zone)
	{
		return false;
	}
	
	/**
	 * Set the x,y,z position of the WorldObject and if necessary modify its _worldRegion.
	 * @param x
	 * @param y
	 * @param z
	 */
	public final void setXYZ(int x, int y, int z)
	{
		_position.set(x, y, z);
		
		if (!isVisible())
			return;
		
		final WorldRegion region = World.getInstance().getRegion(_position);
		if (region != _region)
			setRegion(region);
	}
	
	/**
	 * Set the x,y,z position of the WorldObject and make it invisible. A WorldObject is invisble if <B>_hidden</B>=true or <B>_worldregion</B>==null
	 * @param x
	 * @param y
	 * @param z
	 */
	public final void setXYZInvisible(int x, int y, int z)
	{
		_position.set(MathUtil.limit(x, World.WORLD_X_MIN + 100, World.WORLD_X_MAX - 100), MathUtil.limit(y, World.WORLD_Y_MIN + 100, World.WORLD_Y_MAX - 100), z);
		
		setIsVisible(false);
	}
	
	public final void setXYZInvisible(Location loc)
	{
		setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public final int getX()
	{
		return _position.getX();
	}
	
	public final int getY()
	{
		return _position.getY();
	}
	
	public final int getZ()
	{
		return _position.getZ();
	}
	
	public final SpawnLocation getPosition()
	{
		return _position;
	}
	
	public final WorldRegion getRegion()
	{
		return _region;
	}
	
	/**
	 * Update current and surrounding regions, based on both current region and region setted as parameter.
	 * @param newRegion : null to remove the object, or the new region.
	 */
	public void setRegion(WorldRegion newRegion)
	{
		List<WorldRegion> oldAreas = Collections.emptyList();
		
		if (_region != null)
		{
			_region.removeVisibleObject(this);
			oldAreas = _region.getSurroundingRegions();
		}
		
		List<WorldRegion> newAreas = Collections.emptyList();
		
		if (newRegion != null)
		{
			newRegion.addVisibleObject(this);
			newAreas = newRegion.getSurroundingRegions();
		}
		
		// For every old surrounding area NOT SHARED with new surrounding areas.
		for (WorldRegion region : oldAreas)
		{
			if (!newAreas.contains(region))
			{
				// Update all objects.
				for (WorldObject obj : region.getObjects())
				{
					if (obj == this)
						continue;
					
					obj.removeKnownObject(this);
					removeKnownObject(obj);
				}
				
				// Desactivate the old neighbor region.
				if (this instanceof Player && region.isEmptyNeighborhood())
					region.setActive(false);
			}
		}
		
		// For every new surrounding area NOT SHARED with old surrounding areas.
		for (WorldRegion region : newAreas)
		{
			if (!oldAreas.contains(region))
			{
				// Update all objects.
				for (WorldObject obj : region.getObjects())
				{
					if (obj == this)
						continue;
					
					obj.addKnownObject(this);
					addKnownObject(obj);
				}
				
				// Activate the new neighbor region.
				if (this instanceof Player)
					region.setActive(true);
			}
		}
		
		_region = newRegion;
	}
	
	/**
	 * Add object to known list.
	 * @param object : {@link WorldObject} to be added.
	 */
	public void addKnownObject(WorldObject object)
	{
	}
	
	/**
	 * Remove object from known list.
	 * @param object : {@link WorldObject} to be removed.
	 */
	public void removeKnownObject(WorldObject object)
	{
	}
	
	/**
	 * Return the known list of given object type.
	 * @param <A> : Object type must be instance of {@link WorldObject}.
	 * @param type : Class specifying object type.
	 * @return List<A> : Known list of given object type.
	 */
	@SuppressWarnings("unchecked")
	public final <A> List<A> getKnownType(Class<A> type)
	{
		final WorldRegion region = _region;
		if (region == null)
			return Collections.emptyList();
		
		final List<A> result = new ArrayList<>();
		
		for (WorldRegion reg : region.getSurroundingRegions())
		{
			for (WorldObject obj : reg.getObjects())
			{
				if (obj == this || !type.isAssignableFrom(obj.getClass()))
					continue;
				
				result.add((A) obj);
			}
		}
		
		return result;
	}
	
	/**
	 * Return the known list of given object type within specified radius.
	 * @param <A> : Object type must be instance of {@link WorldObject}.
	 * @param type : Class specifying object type.
	 * @param radius : Radius to in which object must be located.
	 * @return List<A> : Known list of given object type.
	 */
	@SuppressWarnings("unchecked")
	public final <A> List<A> getKnownTypeInRadius(Class<A> type, int radius)
	{
		final WorldRegion region = _region;
		if (region == null)
			return Collections.emptyList();
		
		final List<A> result = new ArrayList<>();
		
		for (WorldRegion reg : region.getSurroundingRegions())
		{
			for (WorldObject obj : reg.getObjects())
			{
				if (obj == this || !type.isAssignableFrom(obj.getClass()) || !MathUtil.checkIfInRange(radius, this, obj, true))
					continue;
				
				result.add((A) obj);
			}
		}
		
		return result;
	}
}