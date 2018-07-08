package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.GMViewSkillInfo;

public class Skills implements IVoicedCommandHandler
{

	private static final String[] VOICED_COMMANDS = 
	{
		
		"skills"
		
	};

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
	
	if (command.startsWith("skills"))
	{
		if (activeChar.getTarget() == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}
		if (!(activeChar.getTarget() instanceof Player))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}

		Creature targetCharacter = (Creature) activeChar.getTarget();
		Player targetPlayer = targetCharacter.getActingPlayer();

		activeChar.sendPacket(new GMViewSkillInfo(targetPlayer));
		return true;
	}
	return true;
}

@Override
public String[] getVoicedCommandList()
{
	return VOICED_COMMANDS;
    }
}