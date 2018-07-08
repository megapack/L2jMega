package net.sf.l2j.gameserver.util;

import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public final class Broadcast
{
	/**
	 * Send a packet to all known players of the Creature that have the Character targetted.
	 * @param character : The character to make checks on.
	 * @param packet : The packet to send.
	 */
	public static void toPlayersTargettingMyself(Creature character, L2GameServerPacket packet)
	{
		for (Player player : character.getKnownType(Player.class))
		{
			if (player.getTarget() != character)
				continue;
			
			player.sendPacket(packet);
		}
	}
	
	/**
	 * Send a packet to all known players of the Creature.
	 * @param character : The character to make checks on.
	 * @param packet : The packet to send.
	 */
	public static void toKnownPlayers(Creature character, L2GameServerPacket packet)
	{
		for (Player player : character.getKnownType(Player.class))
			player.sendPacket(packet);
	}
	
	/**
	 * Send a packet to all known players, in a specified radius, of the Creature.
	 * @param character : The character to make checks on.
	 * @param packet : The packet to send.
	 * @param radius : The given radius.
	 */
	public static void toKnownPlayersInRadius(Creature character, L2GameServerPacket packet, int radius)
	{
		if (radius < 0)
			radius = 1500;
		
		for (Player player : character.getKnownTypeInRadius(Player.class, radius))
			player.sendPacket(packet);
	}
	
	/**
	 * Send a packet to all known players of the Creature and to the specified Creature.
	 * @param character : The character to make checks on.
	 * @param packet : The packet to send.
	 */
	public static void toSelfAndKnownPlayers(Creature character, L2GameServerPacket packet)
	{
		if (character instanceof Player)
			character.sendPacket(packet);
		
		toKnownPlayers(character, packet);
	}
	
	/**
	 * Send a packet to all known players, in a specified radius, of the Creature and to the specified Creature.
	 * @param character : The character to make checks on.
	 * @param packet : The packet to send.
	 * @param radius : The given radius.
	 */
	public static void toSelfAndKnownPlayersInRadius(Creature character, L2GameServerPacket packet, int radius)
	{
		if (radius < 0)
			radius = 600;
		
		if (character instanceof Player)
			character.sendPacket(packet);
		
		for (Player player : character.getKnownTypeInRadius(Player.class, radius))
			player.sendPacket(packet);
	}
	
	/**
	 * Send a packet to all players present in the world.
	 * @param packet : The packet to send.
	 */
	public static void toAllOnlinePlayers(L2GameServerPacket packet)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isOnline())
				player.sendPacket(packet);
		}
	}
	
	/**
	 * Send a packet to all players in a specific region.
	 * @param region : The region to send packets.
	 * @param packets : The packets to send.
	 */
	public static void toAllPlayersInRegion(WorldRegion region, L2GameServerPacket... packets)
	{
		for (WorldObject object : region.getObjects())
		{
			if (object instanceof Player)
			{
				final Player player = (Player) object;
				for (L2GameServerPacket packet : packets)
					player.sendPacket(packet);
			}
		}
	}
	
	/**
	 * Send a packet to all players in a specific zone type.
	 * @param <T> L2ZoneType.
	 * @param zoneType : The zone type to send packets.
	 * @param packets : The packets to send.
	 */
	public static <T extends ZoneType> void toAllPlayersInZoneType(Class<T> zoneType, L2GameServerPacket... packets)
	{
		for (ZoneType temp : ZoneManager.getInstance().getAllZones(zoneType))
		{
			for (Player player : temp.getKnownTypeInside(Player.class))
			{
				for (L2GameServerPacket packet : packets)
					player.sendPacket(packet);
			}
		}
	}
	
	public static void announceToOnlinePlayers(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, Say2.ANNOUNCEMENT, "", text));
	}
	
	public static void announceToOnlinePlayers(String text, boolean critical)
	{
		toAllOnlinePlayers(new CreatureSay(0, (critical) ? Say2.CRITICAL_ANNOUNCE : Say2.ANNOUNCEMENT, "", text));
	}
}