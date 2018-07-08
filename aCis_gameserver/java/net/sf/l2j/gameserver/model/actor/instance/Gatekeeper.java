package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.StringTokenizer;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.data.xml.TeleportLocationData;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.TeleportLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * An instance type extending {@link Folk}, used for teleporters.<br>
 * <br>
 * A teleporter allows {@link Player}s to teleport to a specific location, for a fee.
 */
public final class Gatekeeper extends Folk
{
	public Gatekeeper(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/teleporter/" + filename + ".htm";
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		// Generic PK check. Send back the HTM if found and cancel current action.
		if (!Config.KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0 && showPkDenyChatWindow(player, "teleporter"))
			return;
		
		if (command.startsWith("goto"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			// No more tokens.
			if (!st.hasMoreTokens())
				return;
			
			// No interaction possible with the NPC.
			if (!canInteract(player))
				return;
			
			// Retrieve the list.
			final TeleportLocation list = TeleportLocationData.getInstance().getTeleportLocation(Integer.parseInt(st.nextToken()));
			if (list == null)
				return;
			
			// Siege is currently in progress in this location.
			if (CastleManager.getInstance().getActiveSiege(list.getX(), list.getY(), list.getZ()) != null)
			{
				player.sendPacket(SystemMessageId.CANNOT_PORT_VILLAGE_IN_SIEGE);
				return;
			}
			
			// The list is for noble, but player isn't noble.
			if (list.isNoble() && !player.isNoble())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/teleporter/nobleteleporter-no.htm");
				html.replace("%objectId%", getObjectId());
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Retrieve price list. Potentially cut it by 2 depending of current date.
			int price = list.getPrice();
			
			if (!list.isNoble())
			{
				Calendar cal = Calendar.getInstance();
				if (cal.get(Calendar.HOUR_OF_DAY) >= 20 && cal.get(Calendar.HOUR_OF_DAY) <= 23 && (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7))
					price /= 2;
			}
			
			// Delete related items, and if successful teleport the player to the location.
			if (player.destroyItemByItemId("Teleport ", (list.isNoble()) ? 6651 : 57, price, this, true))
				player.teleToLocation(list, 20);
			
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			
			// Show half price HTM depending of current date. If not existing, use the regular "-1.htm".
			if (val == 1)
			{
				Calendar cal = Calendar.getInstance();
				if (cal.get(Calendar.HOUR_OF_DAY) >= 20 && cal.get(Calendar.HOUR_OF_DAY) <= 23 && (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7))
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					
					String content = HtmCache.getInstance().getHtm("data/html/teleporter/half/" + getNpcId() + ".htm");
					if (content == null)
						content = HtmCache.getInstance().getHtmForce("data/html/teleporter/" + getNpcId() + "-1.htm");
					
					html.setHtml(content);
					html.replace("%objectId%", getObjectId());
					html.replace("%npcname%", getName());
					player.sendPacket(html);
					
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			showChatWindow(player, val);
		}
		else if (command.startsWith("tele_baium"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleBaium();
				}
			}, Config.TELEPORT_BAIUM_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.BAIUM_TELEPORT_LOCX, Config.BAIUM_TELEPORT_LOCY, Config.BAIUM_TELEPORT_LOCZ, 80);
			
		}
		else if (command.startsWith("tele_B1"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleB1();
				}
			}, Config.TELEPORT_B1_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.B1_TELEPORT_LOCX, Config.B1_TELEPORT_LOCY, Config.B1_TELEPORT_LOCZ, 80);
			
		}
		else if (command.startsWith("tele_B2"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleB2();
				}
			}, Config.TELEPORT_B2_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.B2_TELEPORT_LOCX, Config.B2_TELEPORT_LOCY, Config.B2_TELEPORT_LOCZ, 80);
			
		}
		else if (command.startsWith("tele_B3"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleB3();
				}
			}, Config.TELEPORT_B3_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.B3_TELEPORT_LOCX, Config.B3_TELEPORT_LOCY, Config.B3_TELEPORT_LOCZ, 80);
			
		}
		else if (command.startsWith("tele_B4"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleB4();
				}
			}, Config.TELEPORT_B4_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.B4_TELEPORT_LOCX, Config.B4_TELEPORT_LOCY, Config.B4_TELEPORT_LOCZ, 80);
			
		}
		else if (command.startsWith("tele_B5"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleB5();
				}
			}, Config.TELEPORT_B5_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.B5_TELEPORT_LOCX, Config.B5_TELEPORT_LOCY, Config.B5_TELEPORT_LOCZ, 80);
			
		}
		else if (command.startsWith("tele_zaken"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleZaken();
				}
			}, Config.TELEPORT_ZAKEN_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.ZAKEN_TELEPORT_LOCX, Config.ZAKEN_TELEPORT_LOCY, Config.ZAKEN_TELEPORT_LOCZ, 80);
			
		}
		else if (command.startsWith("tele_antharas"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleAntharas();
				}
			}, Config.TELEPORT_ANTHARAS_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.ANTHARAS_TELEPORT_LOCX, Config.ANTHARAS_TELEPORT_LOCY, Config.ANTHARAS_TELEPORT_LOCZ, 80);
			
			
		}
		else if (command.startsWith("tele_frintezza"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleFrintezza();
				}
			}, Config.TELEPORT_FRINTEZZA_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.FRINTEZZA_TELEPORT_LOCX, Config.FRINTEZZA_TELEPORT_LOCY, Config.FRINTEZZA_TELEPORT_LOCZ, 80);

			
		}
		else if (command.startsWith("tele_valakas"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					RaidBoss.unSpawnTeleValakas();
				}
			}, Config.TELEPORT_VALAKAS_DELETE_TIME * 1000);
			
			player.teleToLocation(Config.VALAKAS_TELEPORT_LOCX, Config.VALAKAS_TELEPORT_LOCY, Config.VALAKAS_TELEPORT_LOCZ, 80);

			
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	
	static L2Spawn _BaiumSpawn;
	static L2Spawn _ZakenSpawn;
	static L2Spawn _AntharasSpawn;
	static L2Spawn _FrintezzaSpawn;
	static L2Spawn _ValakasSpawn;
	
	protected static L2Spawn spawnNPC(int xPos, int yPos, int zPos, int npcId)
	{
		final NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
		
		try
		{
			final L2Spawn _npcSpawn1 = new L2Spawn(template);
		      _npcSpawn1.setLoc(xPos, yPos, zPos, 0);
		      _npcSpawn1.setRespawnDelay(1);
		      
		      SpawnTable.getInstance().addNewSpawn(_npcSpawn1, false);
		      
		      _npcSpawn1.setRespawnState(true);
		      _npcSpawn1.doSpawn(false);
		      _npcSpawn1.getNpc().getStatus().setCurrentHp(9.99999999E8D);
		      _npcSpawn1.getNpc().isAggressive();
		      _npcSpawn1.getNpc().decayMe();
		      _npcSpawn1.getNpc().spawnMe(_npcSpawn1.getNpc().getX(), _npcSpawn1.getNpc().getY(), _npcSpawn1.getNpc().getZ());
		      _npcSpawn1.getNpc().broadcastPacket(new MagicSkillUse(_npcSpawn1.getNpc(), _npcSpawn1.getNpc(), 1034, 1, 1, 1));
			
			return _npcSpawn1;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		// Generic PK check. Send back the HTM if found and cancel current action.
		if (!Config.KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0 && showPkDenyChatWindow(player, "teleporter"))
			return;
		
		showChatWindow(player, getHtmlPath(getNpcId(), val));
	}
}