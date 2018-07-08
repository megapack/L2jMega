package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.ExRedSky;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * This class manages all Grand Bosses.
 */
public final class GrandBoss extends Monster
{
	/**
	 * Constructor for L2GrandBossInstance. This represent all grandbosses.
	 * @param objectId ID of the instance
	 * @param template L2NpcTemplate of the instance
	 */
	public GrandBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setIsRaid(true);
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		final Player player = killer.getActingPlayer();
		if (player != null)
		{
			
		     if (Config.ANNOUNCE_GRANBOS_ALIVE_KILL)
				{
			          for (Player pl : World.getInstance().getPlayers()) {
			              if (pl.getClan() != null) {
		              CreatureSay cs = new CreatureSay(pl.getObjectId(), Config.ANNOUNCE_ID, "",Config.RAID_BOSS_DEFEATED_BY_CLAN_MEMBER_MSG.replace("%raidboss%", getName()).replace("%player%", killer.getName()).replace("%clan%", player.getClan().getName()));
		              pl.sendPacket(cs);
		            } else {
			              CreatureSay cs = new CreatureSay(pl.getObjectId(), Config.ANNOUNCE_ID, "",Config.RAID_BOSS_DEFEATED_BY_PLAYER_MSG.replace("%raidboss%", getName()).replace("%player%", killer.getName()));
			              pl.sendPacket(cs);
				    }
				  }
				}
		     
		     
				if (Config.QUAKE_RAID_BOSS)
				{
				      ExRedSky packet = new ExRedSky(10);
				      Earthquake eq = new Earthquake(getX(), getY(), getZ(), 100, 10);
				      Broadcast.toAllOnlinePlayers(packet);
				      Broadcast.toAllOnlinePlayers(eq);
				}
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			broadcastPacket(new PlaySound("systemmsg_e.1209"));
			
			final Party party = player.getParty();
			if (party != null)
			{
				for (Player member : party.getMembers())
				{
					RaidPointManager.getInstance().addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
					if (member.isNoble())
						Hero.getInstance().setRBkilled(member.getObjectId(), getNpcId());
				}
			}
			else
			{
				RaidPointManager.getInstance().addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
				if (player.isNoble())
					Hero.getInstance().setRBkilled(player.getObjectId(), getNpcId());
			}
		}
		
		return true;
	}
	
	@Override
	public boolean returnHome(boolean cleanAggro)
	{
		return false;
	}
}