package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Macro;
import net.sf.l2j.gameserver.model.L2Macro.L2MacroCmd;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestMakeMacro extends L2GameClientPacket
{
	private static final int MAX_MACRO_LENGTH = 12;
	
	private L2Macro _macro;
	private int _commandsLenght = 0;
	
	@Override
	protected void readImpl()
	{
		int id = readD();
		String name = readS();
		String desc = readS();
		String acronym = readS();
		int icon = readC();
		int count = readC();
		
		if (count > MAX_MACRO_LENGTH)
			count = MAX_MACRO_LENGTH;
		
		L2MacroCmd[] commands = new L2MacroCmd[count];
		
		for (int i = 0; i < count; i++)
		{
			int entry = readC();
			int type = readC(); // 1 = skill, 3 = action, 4 = shortcut
			int d1 = readD(); // skill or page number for shortcuts
			int d2 = readC();
			String command = readS();
			
			_commandsLenght += command.length();
			commands[i] = new L2MacroCmd(entry, type, d1, d2, command);
		}
		_macro = new L2Macro(id, icon, name, desc, acronym, commands);
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		// Invalid macro. Refer to the Help file for instructions.
		if (_commandsLenght > 255)
		{
			player.sendPacket(SystemMessageId.INVALID_MACRO);
			return;
		}
		
		// You may create up to 24 macros.
		if (player.getMacroses().getAllMacroses().length > 24)
		{
			player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_24_MACROS);
			return;
		}
		
		// Enter the name of the macro.
		if (_macro.name.isEmpty())
		{
			player.sendPacket(SystemMessageId.ENTER_THE_MACRO_NAME);
			return;
		}
		
		// Macro descriptions may contain up to 32 characters.
		if (_macro.descr.length() > 32)
		{
			player.sendPacket(SystemMessageId.MACRO_DESCRIPTION_MAX_32_CHARS);
			return;
		}
		
		player.registerMacro(_macro);
	}
}