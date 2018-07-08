package net.sf.l2j.gameserver.model.group;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExCloseMPCC;
import net.sf.l2j.gameserver.network.serverpackets.ExOpenMPCC;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public class CommandChannel extends AbstractGroup
{
	private final List<Party> _parties = new CopyOnWriteArrayList<>();
	
	public CommandChannel(Party requestor, Party target)
	{
		super(requestor.getLeader());
		
		_parties.add(requestor);
		_parties.add(target);
		
		requestor.setCommandChannel(this);
		target.setCommandChannel(this);
		
		recalculateLevel();
		
		for (Player member : requestor.getMembers())
		{
			member.sendPacket(SystemMessageId.COMMAND_CHANNEL_FORMED);
			member.sendPacket(ExOpenMPCC.STATIC_PACKET);
		}
		
		for (Player member : target.getMembers())
		{
			member.sendPacket(SystemMessageId.JOINED_COMMAND_CHANNEL);
			member.sendPacket(ExOpenMPCC.STATIC_PACKET);
		}
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof CommandChannel))
			return false;
		
		if (obj == this)
			return true;
		
		return isLeader(((CommandChannel) obj).getLeader());
	}
	
	/**
	 * <b>BEWARE : create a temporary List. Uses containsPlayer whenever possible.</b>
	 */
	@Override
	public List<Player> getMembers()
	{
		final List<Player> members = new ArrayList<>();
		for (Party party : _parties)
			members.addAll(party.getMembers());
		
		return members;
	}
	
	@Override
	public int getMembersCount()
	{
		int count = 0;
		for (Party party : _parties)
			count += party.getMembersCount();
		
		return count;
	}
	
	@Override
	public boolean containsPlayer(WorldObject player)
	{
		for (Party party : _parties)
		{
			if (party.containsPlayer(player))
				return true;
		}
		return false;
	}
	
	@Override
	public void broadcastPacket(final L2GameServerPacket packet)
	{
		for (Party party : _parties)
			party.broadcastPacket(packet);
	}
	
	@Override
	public void broadcastCreatureSay(final CreatureSay msg, final Player broadcaster)
	{
		for (Party party : _parties)
			party.broadcastCreatureSay(msg, broadcaster);
	}
	
	@Override
	public void recalculateLevel()
	{
		int newLevel = 0;
		for (Party party : _parties)
		{
			if (party.getLevel() > newLevel)
				newLevel = party.getLevel();
		}
		setLevel(newLevel);
	}
	
	@Override
	public void disband()
	{
		for (Party party : _parties)
		{
			party.setCommandChannel(null);
			party.broadcastPacket(ExCloseMPCC.STATIC_PACKET);
			party.broadcastMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED);
		}
		_parties.clear();
	}
	
	/**
	 * Adds a Party to the Command Channel.
	 * @param party : the party to add.
	 */
	public void addParty(Party party)
	{
		// Null party or party is already registered in this command channel.
		if (party == null || _parties.contains(party))
			return;
		
		_parties.add(party);
		
		if (party.getLevel() > getLevel())
			setLevel(party.getLevel());
		
		party.setCommandChannel(this);
		
		for (Player member : party.getMembers())
		{
			member.sendPacket(SystemMessageId.JOINED_COMMAND_CHANNEL);
			member.sendPacket(ExOpenMPCC.STATIC_PACKET);
		}
	}
	
	/**
	 * Removes a Party from the Command Channel.
	 * @param party : the party to remove. Disband the channel if there was only 2 parties left.
	 * @return true if the party has been successfully removed from command channel.
	 */
	public boolean removeParty(Party party)
	{
		// Null party or party isn't registered in this command channel.
		if (party == null || !_parties.contains(party))
			return false;
		
		// Don't bother individually drop parties, disband entirely if there is only 2 parties in command channel.
		if (_parties.size() == 2)
			disband();
		else
		{
			_parties.remove(party);
			
			party.setCommandChannel(null);
			party.broadcastPacket(ExCloseMPCC.STATIC_PACKET);
			
			recalculateLevel();
		}
		return true;
	}
	
	/**
	 * @return the list of parties registered in this command channel.
	 */
	public List<Party> getParties()
	{
		return _parties;
	}
	
	/**
	 * @param attackable : the attackable to check.
	 * @return true if the members count is reached.
	 */
	public boolean meetRaidWarCondition(Attackable attackable)
	{
		switch (attackable.getNpcId())
		{
			case 29001: // Queen Ant
			case 29006: // Core
			case 29014: // Orfen
			case 29022: // Zaken
				return getMembersCount() > 36;
			
			case 29020: // Baium
				return getMembersCount() > 56;
			
			case 29019: // Antharas
				return getMembersCount() > 225;
			
			case 29028: // Valakas
				return getMembersCount() > 99;
			
			default: // normal Raidboss
				return getMembersCount() > 18;
		}
	}
}