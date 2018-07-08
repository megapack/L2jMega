package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

/**
 * This class handles following admin commands: - invul = turns invulnerability on/off
 */
public class AdminInvul implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_setinvul"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player player)
	{
		if (command.equals("admin_setinvul"))
		{
			WorldObject object = player.getTarget();
			if (object == null)
				object = player;
			
			if (!(object instanceof Creature))
			{
				player.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Creature target = (Creature) object;
			target.setIsInvul(!target.isInvul());
			
			player.sendMessage(target.getName() + ((target.isInvul()) ? " is now invulnerable." : " is now mortal."));
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}