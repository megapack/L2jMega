package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.group.Party;

/**
 * An instance type extending {@link Folk}, used by Guardian of Border NPC (internal room rift teleporters).
 */
public class BorderGuard extends Folk
{
	public BorderGuard(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		DimensionalRift dr = null;
		
		final Party party = player.getParty();
		if (party != null)
			dr = party.getDimensionalRift();
		
		if (dr == null)
			return;
		
		if (command.startsWith("ChangeRiftRoom"))
			dr.manualTeleport(player, this);
		else if (command.startsWith("ExitRift"))
			dr.manualExitRift(player, this);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return SevenSigns.SEVEN_SIGNS_HTML_PATH + "rift/GuardianOfBorder.htm";
	}
}