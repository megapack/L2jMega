package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.EventType;
import net.sf.l2j.gameserver.scripting.Quest;

public class SepulcherNpc extends Folk
{
	protected Future<?> _closeTask = null;
	protected Future<?> _spawnNextMysteriousBoxTask = null;
	protected Future<?> _spawnMonsterTask = null;
	
	private static final String HTML_FILE_PATH = "data/html/sepulchers/";
	private static final int HALLS_KEY = 7260;
	
	public SepulcherNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		setShowSummonAnimation(true);
		
		if (_closeTask != null)
			_closeTask.cancel(true);
		
		if (_spawnNextMysteriousBoxTask != null)
			_spawnNextMysteriousBoxTask.cancel(true);
		
		if (_spawnMonsterTask != null)
			_spawnMonsterTask.cancel(true);
		
		_closeTask = null;
		_spawnNextMysteriousBoxTask = null;
		_spawnMonsterTask = null;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		setShowSummonAnimation(false);
	}
	
	@Override
	public void deleteMe()
	{
		if (_closeTask != null)
		{
			_closeTask.cancel(true);
			_closeTask = null;
		}
		if (_spawnNextMysteriousBoxTask != null)
		{
			_spawnNextMysteriousBoxTask.cancel(true);
			_spawnNextMysteriousBoxTask = null;
		}
		if (_spawnMonsterTask != null)
		{
			_spawnMonsterTask.cancel(true);
			_spawnMonsterTask = null;
		}
		super.deleteMe();
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Check if the player is attackable (without a forced attack) and isn't dead
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				// Check the height difference, this max heigth difference might need some tweaking
				if (Math.abs(player.getZ() - getZ()) < 400)
				{
					// Set the Player Intention to ATTACK
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
				}
				else
				{
					// Send ActionFailed (target is out of attack range) to the Player player
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else if (!isAutoAttackable(player))
			{
				// Calculate the distance between the Player and the L2NpcInstance
				if (!canInteract(player))
				{
					// Notify the Player AI with INTERACT
					player.getAI().setIntention(CtrlIntention.INTERACT, this);
				}
				else
				{
					// Stop moving if we're already in interact range.
					if (player.isMoving() || player.isInCombat())
						player.getAI().setIntention(CtrlIntention.IDLE);
					
					// Rotate the player to face the instance
					player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
					
					// Send ActionFailed to the player in order to avoid he stucks
					player.sendPacket(ActionFailed.STATIC_PACKET);
					
					if (hasRandomAnimation())
						onRandomAnimation(Rnd.get(8));
					
					doAction(player);
				}
			}
			// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	private void doAction(Player player)
	{
		if (isDead())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		switch (getNpcId())
		{
			case 31468:
			case 31469:
			case 31470:
			case 31471:
			case 31472:
			case 31473:
			case 31474:
			case 31475:
			case 31476:
			case 31477:
			case 31478:
			case 31479:
			case 31480:
			case 31481:
			case 31482:
			case 31483:
			case 31484:
			case 31485:
			case 31486:
			case 31487:
				setIsInvul(false);
				reduceCurrentHp(getMaxHp() + 1, player, null);
				if (_spawnMonsterTask != null)
					_spawnMonsterTask.cancel(true);
				_spawnMonsterTask = ThreadPool.schedule(new SpawnMonster(getNpcId()), 3500);
				break;
			
			case 31455:
			case 31456:
			case 31457:
			case 31458:
			case 31459:
			case 31460:
			case 31461:
			case 31462:
			case 31463:
			case 31464:
			case 31465:
			case 31466:
			case 31467:
				setIsInvul(false);
				reduceCurrentHp(getMaxHp() + 1, player, null);
				if (player.isInParty() && !player.getParty().isLeader(player))
					player = player.getParty().getLeader();
				player.addItem("Quest", HALLS_KEY, 1, player, true);
				break;
			
			default:
			{
				List<Quest> qlsa = getTemplate().getEventQuests(EventType.QUEST_START);
				if (qlsa != null && !qlsa.isEmpty())
					player.setLastQuestNpcObject(getObjectId());
				
				List<Quest> qlst = getTemplate().getEventQuests(EventType.ON_FIRST_TALK);
				if (qlst != null && qlst.size() == 1)
					qlst.get(0).notifyFirstTalk(this, player);
				else
					showChatWindow(player);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return HTML_FILE_PATH + filename + ".htm";
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(getHtmlPath(getNpcId(), val));
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("Chat"))
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
			showChatWindow(player, val);
		}
		else if (command.startsWith("open_gate"))
		{
			final ItemInstance hallsKey = player.getInventory().getItemByItemId(HALLS_KEY);
			if (hallsKey == null)
				showHtmlFile(player, "Gatekeeper-no.htm");
			else if (FourSepulchersManager.getInstance().isAttackTime())
			{
				switch (getNpcId())
				{
					case 31929:
					case 31934:
					case 31939:
					case 31944:
						FourSepulchersManager.getInstance().spawnShadow(getNpcId());
					default:
					{
						openNextDoor(getNpcId());
						
						final Party party = player.getParty();
						if (party != null)
						{
							for (Player member : player.getParty().getMembers())
							{
								final ItemInstance key = member.getInventory().getItemByItemId(HALLS_KEY);
								if (key != null)
									member.destroyItemByItemId("Quest", HALLS_KEY, key.getCount(), member, true);
							}
						}
						else
							player.destroyItemByItemId("Quest", HALLS_KEY, hallsKey.getCount(), player, true);
					}
				}
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	public void openNextDoor(int npcId)
	{
		int doorId = FourSepulchersManager.getInstance().getHallGateKeepers().get(npcId);
		DoorData _doorTable = DoorData.getInstance();
		_doorTable.getDoor(doorId).openMe();
		
		if (_closeTask != null)
			_closeTask.cancel(true);
		
		_closeTask = ThreadPool.schedule(new CloseNextDoor(doorId), 10000);
		
		if (_spawnNextMysteriousBoxTask != null)
			_spawnNextMysteriousBoxTask.cancel(true);
		
		_spawnNextMysteriousBoxTask = ThreadPool.schedule(new SpawnNextMysteriousBox(npcId), 0);
	}
	
	public void sayInShout(String msg)
	{
		if (msg == null || msg.isEmpty())
			return;
		
		final CreatureSay sm = new CreatureSay(getObjectId(), Say2.SHOUT, getName(), msg);
		for (Player player : getKnownType(Player.class))
			player.sendPacket(sm);
	}
	
	public void showHtmlFile(Player player, String file)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/sepulchers/" + file);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private static class CloseNextDoor implements Runnable
	{
		private final int _doorId;
		
		public CloseNextDoor(int doorId)
		{
			_doorId = doorId;
		}
		
		@Override
		public void run()
		{
			try
			{
				DoorData.getInstance().getDoor(_doorId).closeMe();
			}
			catch (Exception e)
			{
				_log.warning(e.getMessage());
			}
		}
	}
	
	private static class SpawnNextMysteriousBox implements Runnable
	{
		private final int _npcId;
		
		public SpawnNextMysteriousBox(int npcId)
		{
			_npcId = npcId;
		}
		
		@Override
		public void run()
		{
			FourSepulchersManager.getInstance().spawnMysteriousBox(_npcId);
		}
	}
	
	private static class SpawnMonster implements Runnable
	{
		private final int _npcId;
		
		public SpawnMonster(int npcId)
		{
			_npcId = npcId;
		}
		
		@Override
		public void run()
		{
			FourSepulchersManager.getInstance().spawnMonster(_npcId);
		}
	}
}