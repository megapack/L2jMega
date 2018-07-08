package net.sf.l2j.gameserver.handler.itemhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.util.DonateLog;

public class HeroDay implements IItemHandler
{
	protected static final Logger _log = Logger.getLogger(HeroMontly.class.getName());
	
	String INSERT_DATA = "REPLACE INTO characters_hero_data (obj_Id, char_name, hero, noble, hero_end_date) VALUES (?,?,?,?,?)";

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof Player))
			return;

		Player activeChar = (Player) playable;

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("This item cannot be used on Olympiad Games.");
		}

		if(activeChar.isHero())
		{
			activeChar.sendMessage("You are already a hero!");
		}
		else
		{
			activeChar.broadcastPacket(new SocialAction(activeChar, 16));
			activeChar.setHero(true);
			updateDatabase(activeChar, 1 * 24L * 60L * 60L * 1000L);
			activeChar.getInventory().addItem("Tiara", 9632, 1, activeChar, null);
			activeChar.sendMessage("You are now a hero, you are granted With hero status, skills , aura.");
			activeChar.broadcastUserInfo();
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
			
		      String clientInfo = activeChar.getClient().toString();
		      String ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"));
		      
		      DonateLog.auditGMAction(activeChar.getObjectId(), activeChar.getName(), "Coin - Hero Day", ip);
		}
	}

	private void updateDatabase(Player player, long heroTime)
	{
        try (Connection con = L2DatabaseFactory.getInstance().getConnection())
        {
			if (player == null)
				return;
			
			PreparedStatement stmt = con.prepareStatement(INSERT_DATA);

			stmt.setInt(1, player.getObjectId());
			stmt.setString(2, player.getName());
			stmt.setInt(3, 1);
			stmt.setInt(4, player.isNoble() ? 1 : 0);
			stmt.setLong(5, heroTime == 0 ? 0 : System.currentTimeMillis() + heroTime);
			stmt.execute();
			stmt.close();
			stmt = null;
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error: could not update database: ", e);
			e.printStackTrace();
		}
	}
}
