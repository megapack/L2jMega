package net.sf.l2j.gameserver.scripting.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scripting.ScheduledQuest;

public final class RecommendationUpdate extends ScheduledQuest
{
	private static final String DELETE_CHAR_RECOMS = "TRUNCATE TABLE character_recommends";
	private static final String SELECT_ALL_RECOMS = "SELECT obj_Id, level, rec_have FROM characters";
	private static final String UPDATE_ALL_RECOMS = "UPDATE characters SET rec_left=?, rec_have=? WHERE obj_Id=?";
	
	public RecommendationUpdate()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		// Refresh online characters stats.
		for (Player player : World.getInstance().getPlayers())
		{
			player.getRecomChars().clear();
			
			final int level = player.getLevel();
			if (level < 20)
			{
				player.setRecomLeft(3);
				player.editRecomHave(-1);
			}
			else if (level < 40)
			{
				player.setRecomLeft(6);
				player.editRecomHave(-2);
			}
			else
			{
				player.setRecomLeft(9);
				player.editRecomHave(-3);
			}
			
			player.sendPacket(new UserInfo(player));
		}
		
		// Refresh database side.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete all characters listed on character_recommends table.
			PreparedStatement ps = con.prepareStatement(DELETE_CHAR_RECOMS);
			ps.execute();
			ps.close();
			
			// Initialize the update statement.
			PreparedStatement ps2 = con.prepareStatement(UPDATE_ALL_RECOMS);
			
			// Select needed informations of all characters.
			ps = con.prepareStatement(SELECT_ALL_RECOMS);
			
			ResultSet rset = ps.executeQuery();
			while (rset.next())
			{
				final int level = rset.getInt("level");
				if (level < 20)
				{
					ps2.setInt(1, 3);
					ps2.setInt(2, Math.max(0, rset.getInt("rec_have") - 1));
				}
				else if (level < 40)
				{
					ps2.setInt(1, 6);
					ps2.setInt(2, Math.max(0, rset.getInt("rec_have") - 2));
				}
				else
				{
					ps2.setInt(1, 9);
					ps2.setInt(2, Math.max(0, rset.getInt("rec_have") - 3));
				}
				ps2.setInt(3, rset.getInt("obj_Id"));
				ps2.addBatch();
			}
			rset.close();
			ps.close();
			
			ps2.executeBatch();
			ps2.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Couldn't clear char recommendations.", e);
		}
	}
	
	@Override
	public final void onEnd()
	{
	}
}