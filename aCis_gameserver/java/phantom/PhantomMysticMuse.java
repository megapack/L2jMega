package phantom;

import com.elfocrash.roboto.FakePlayerNameManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.item.type.WeaponType;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

public class PhantomMysticMuse
{
	static final Logger _log = Logger.getLogger(PhantomMysticMuse.class.getName());
	static String _phantomAcc = Config.PHANTOM_PLAYERS_MYSTICMUSE_1;
	static int _PhantomsCount = 0;
	static int _PhantomsLimit = 0;
	static int _setsCount = 0;
	static int _setsCountClan = 0;
	volatile int _PhantomsTownTotal = 0;
	static ArrayList<L2Set> _sets = new ArrayList<>();
	static int _setsArcherCount = 0;
	static ArrayList<L2Set> _setsArcher = new ArrayList<>();
	static PhantomMysticMuse _instance;
	static int _setsOlyCount = 0;
	static ArrayList<L2Set> _setsOly = new ArrayList<>();
	static int _locsCount = 0;
	static ArrayList<Location> _PhantomsTownLoc = new ArrayList<>();
	static int _PhantomsEnchPhsCount = 0;
	static ArrayList<String> _PhantomsEnchPhrases = new ArrayList<>();
	static int _PhantomsLastPhsCount = 0;
	static ArrayList<String> _PhantomsLastPhrases = new ArrayList<>();
	static Map<Integer, ConcurrentLinkedQueue<Player>> _PhantomsTown = new ConcurrentHashMap<>();
	static Map<Integer, ConcurrentLinkedQueue<Player>> _PhantomsTownClan = new ConcurrentHashMap<>();
	static Map<Integer, ConcurrentLinkedQueue<Integer>> _PhantomsTownClanList = new ConcurrentHashMap<>();
	
	public static PhantomMysticMuse getInstance()
	{
		return _instance;
	}
	
	private void load()
	{
		if (Config.ALLOW_PHANTOM_PLAYERS)
		{
			parceArmors();
			cacheFantoms();
			_PhantomsTown.put(Integer.valueOf(1), new ConcurrentLinkedQueue<Player>());
			_PhantomsTown.put(Integer.valueOf(2), new ConcurrentLinkedQueue<Player>());
		}
	}
	
	public void reload()
	{
		parceArmors();
	}
	
	public static void init()
	{
		_instance = new PhantomMysticMuse();
		_instance.load();
	}
	
	final static List<String> list_name = new ArrayList<>();
	
	@SuppressWarnings("null")
	static String getName()
	{
		String msg = null;
		
		if (msg == null)
			msg = Config.PHANTOM_NAME_MYSTICMUSE_LIST.get(Rnd.get(Config.PHANTOM_NAME_MYSTICMUSE_LIST.size()));
		
		if (list_name.contains(msg))
		{
			boolean gerar = true;
			
			while (gerar)
			{
				msg = Config.PHANTOM_NAME_MYSTICMUSE_LIST.get(Rnd.get(Config.PHANTOM_NAME_MYSTICMUSE_LIST.size()));
				
				if (list_name.contains(msg) == false)
				{
					list_name.add(msg);
					gerar = false;
					return msg;
				}
			}
		}
		else if (list_name.contains(msg) == false)
		{
			list_name.add(msg);
			return msg;
		}
		
		return msg;
	}
	
	static int getFaceEquipe()
	{
		return Config.LIST_PHANTOM_FACE.get(Rnd.get(Config.LIST_PHANTOM_FACE.size()));
	}
	
	final static List<String> list_title = new ArrayList<>();
	
	@SuppressWarnings("null")
	static String getTitle()
	{
		String msg = null;
		
		if (msg == null)
			msg = Config.PHANTOM_PLAYERS_TITLE.get(Rnd.get(Config.PHANTOM_PLAYERS_TITLE.size()));
		
		if (list_title.contains(msg))
		{
			boolean gerar = true;
			
			while (gerar)
			{
				msg = Config.PHANTOM_PLAYERS_TITLE.get(Rnd.get(Config.PHANTOM_PLAYERS_TITLE.size()));
				
				if (list_title.contains(msg) == false)
				{
					list_title.add(msg);
					gerar = false;
					return msg;
				}
			}
		}
		else if (list_title.contains(msg) == false)
		{
			list_title.add(msg);
			return msg;
		}
		
		return msg;
	}
	
	static String getNameColor()
	{
		return Config.PHANTOM_PLAYERS_NAME_CLOLORS.get(Rnd.get(Config.PHANTOM_PLAYERS_NAME_CLOLORS.size()));
	}
	
	static String getTitleColor()
	{
		return Config.PHANTOM_PLAYERS_TITLE_CLOLORS.get(Rnd.get(Config.PHANTOM_PLAYERS_TITLE_CLOLORS.size()));
	}
	
	public void startAttack(Player paramL2PcInstance)
	{
		ThreadPool.schedule(new PhantomAtack(paramL2PcInstance), Rnd.get(3000, 6345));
	}
	
	private class PhantomAtack implements Runnable
	{
		Player _phantom;
		
		public PhantomAtack(Player paramL2PcInstance)
		{
			_phantom = paramL2PcInstance;
		}
		
		@Override
		public void run()
		{
			if (!_phantom.isDead())
				doCastlist(_phantom);
		}
	}
	
	@SuppressWarnings("null")
	static Location getRandomLoc()
	{
		Location loc = null;
		if (loc == null)
			loc = _PhantomsTownLoc.get(Rnd.get(0, _locsCount));
		return loc;
	}
	
	@SuppressWarnings("resource")
	private static void parceArmors()
	{
		if (!_sets.isEmpty())
		{
			_sets.clear();
		}
		LineNumberReader localLineNumberReader = null;
		BufferedReader localBufferedReader = null;
		FileReader localFileReader = null;
		try
		{
			File localFile = new File("./config/custom/fakepvp/mage_sets.ini");
			if (!localFile.exists())
			{
				return;
			}
			localFileReader = new FileReader(localFile);
			localBufferedReader = new BufferedReader(localFileReader);
			localLineNumberReader = new LineNumberReader(localBufferedReader);
			String str;
			while ((str = localLineNumberReader.readLine()) != null)
			{
				if ((str.trim().length() != 0) && (!str.startsWith("#")))
				{
					String[] arrayOfString = str.split(",");
					_sets.add(new L2Set(Integer.parseInt(arrayOfString[0]), Integer.parseInt(arrayOfString[1]), Integer.parseInt(arrayOfString[2]), Integer.parseInt(arrayOfString[3]), Integer.parseInt(arrayOfString[4]), Integer.parseInt(arrayOfString[5]), Integer.parseInt(arrayOfString[6])));
				}
			}
			_setsCount = _sets.size();
			_log.info("Load " + _setsCount + " mysticMuse armor sets");
			return;
		}
		catch (Exception localException2)
		{
			localException2.printStackTrace();
		}
		finally
		{
			try
			{
				if (localFileReader != null)
				{
					localFileReader.close();
				}
				if (localBufferedReader != null)
				{
					localBufferedReader.close();
				}
				if (localLineNumberReader != null)
				{
					localLineNumberReader.close();
				}
			}
			catch (Exception localException6)
			{
			}
		}
	}
	
	private void cacheFantoms()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				ThreadPool.schedule(new FantomTask(1), 10000);
			}
		}).start();
	}
	
	static L2Set getRandomSet()
	{
		return _sets.get(Rnd.get(_setsCount));
	}
	
	static class L2Set
	{
		public int _body;
		public int _gaiters;
		public int _gloves;
		public int _boots;
		public int _weapon;
		public int _custom;
		public int _grade;
		
		L2Set(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
		{
			_body = paramInt1;
			_gaiters = paramInt2;
			_gloves = paramInt3;
			_boots = paramInt4;
			_weapon = paramInt5;
			_grade = paramInt6;
			_custom = paramInt7;
		}
	}
	
	public static ArrayList<Player> _add_phantom = new ArrayList<>();
	
	public static int getPhantomCount()
	{
		if (_add_phantom != null)
			return _add_phantom.size();
		
		return 0;
	}
	
	public static void removePhantom(Player spec)
	{
		if (_add_phantom != null && _add_phantom.contains(spec))
			_add_phantom.remove(spec);
	}
	
	static class L2Fantome
	{
		public String name;
		public String title;
		public int x;
		public int y;
		public int z;
		
		L2Fantome(String paramString1, String paramString2, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
		{
			this.name = paramString1;
			this.title = paramString2;
			this.x = paramInt1;
			this.y = paramInt2;
			this.z = paramInt3;
		}
	}
	
	static SimpleDateFormat sdf = new SimpleDateFormat("HH");
	
	public class FantomTask implements Runnable
	{
		public int _task;
		
		public FantomTask(int paramInt)
		{
			_task = paramInt;
		}
		
		@Override
		public void run()
		{
			int i = 0;
			switch (_task)
			{
				case 1:
					PhantomMysticMuse._log.info("Phantom [Mystic Muse]: Account " + Config.PHANTOM_PLAYERS_MYSTICMUSE_1 + ", spawn started.");
					try (Connection con = L2DatabaseFactory.getInstance().getConnection())
					{
						PreparedStatement stm = con.prepareStatement("SELECT obj_Id,char_name,title,x,y,z,sex FROM characters WHERE account_name = ?");
						stm.setString(1, _phantomAcc);
						ResultSet rs = stm.executeQuery();
						while (rs.next())
						{
							Player player = null;
							try
							{
								L2Set localL2Set = getRandomSet();
								ItemInstance localL2ItemInstance1 = ItemTable.getInstance().createDummyItem(localL2Set._body);
								ItemInstance localL2ItemInstance2 = ItemTable.getInstance().createDummyItem(localL2Set._gaiters);
								ItemInstance localL2ItemInstance3 = ItemTable.getInstance().createDummyItem(localL2Set._gloves);
								ItemInstance localL2ItemInstance4 = ItemTable.getInstance().createDummyItem(localL2Set._boots);
								ItemInstance localL2ItemInstance5 = ItemTable.getInstance().createDummyItem(localL2Set._weapon);
								ItemInstance localL2ItemInstance6 = null;
								
								ItemInstance WINGS = ItemTable.getInstance().createDummyItem(getFaceEquipe());
								
								int k = localL2Set._grade;
								int m = 1;
								int n = 0;
								if (k == 0)
								{
									m = Rnd.get(1, 19);
								}
								if (k == 1)
								{
									m = Rnd.get(20, 39);
								}
								if (k == 2)
								{
									m = Rnd.get(40, 51);
								}
								if (k == 3)
								{
									m = Rnd.get(52, 60);
								}
								if (k == 4)
								{
									m = Rnd.get(61, 75);
								}
								if (k == 5)
								{
									m = Rnd.get(76, 80);
								}
								
				                  L2GameClient client = new L2GameClient(null);
				                  client.setDetached(true);
				                  player = Player.loadMystic(rs.getInt("obj_Id"), m, n);
				                  player.refreshOverloaded();
				                  player.setClient(client);
				                  client.setActiveChar(player);
				                  player.setOnlineStatus(true, true);
				                  World.getInstance().addPlayer(player);
				                  client.setState(L2GameClient.GameClientState.IN_GAME);
				                  
				                  client.setAccountName(player.getAccountName());
				                  
				                  String playerName = FakePlayerNameManager.INSTANCE.getRandomAvailableName();
				                  player.setName(playerName);
				                  PlayerInfoTable.getInstance().updatePlayerData(player, false);
				                  if (Rnd.get(100) < 30) {
				                    PvpFlagTaskManager.getInstance().add(player, 60000L);
				                  }
								
								if (Rnd.get(100) < 20)
									player.getAppearance().setNameColor(Integer.decode("0x" + getNameColor()));
								else
									player.getAppearance().setNameColor(Integer.decode("0x" + Config.NAME_COLOR));
								
								if (Config.PHANTOM_TITLE_CONFIG)
									player.setTitle(getFixTitle());
								else
									player.setTitle(getTitle());
								
								if (Rnd.get(100) < 20)
									player.getAppearance().setTitleColor(Integer.decode("0x" + getTitleColor()));
								else
									player.getAppearance().setTitleColor(Integer.decode("0x" + Config.TITLE_COLOR));
								
								player.getInventory().equipItemAndRecord(localL2ItemInstance1);
								player.getInventory().equipItemAndRecord(localL2ItemInstance2);
								player.getInventory().equipItemAndRecord(localL2ItemInstance3);
								player.getInventory().equipItemAndRecord(localL2ItemInstance4);
								
								if (Config.ALLOW_PHANTOM_FACE)
									player.getInventory().equipItemAndRecord(WINGS);
								
								int[] arrayOfInt =
								{
									92,
									102,
									109
								};
								if (localL2Set._custom > 0)
								{
									localL2ItemInstance6 = ItemTable.getInstance().createDummyItem(localL2Set._custom);
									player.getInventory().equipItemAndRecord(localL2ItemInstance6);
								}
								Weapon localL2Weapon = localL2ItemInstance5.getWeaponItem();
								if ((localL2Weapon.getItemType() == WeaponType.BOW) && ((player.getClassId().getId() != 92) || (player.getClassId().getId() != 102) || (player.getClassId().getId() != 109)))
								{
									player.setClassId(arrayOfInt[Rnd.get(arrayOfInt.length)]);
								}
								
								player.getInventory().equipItemAndRecord(localL2ItemInstance5);
								
								player.starLocation();
								
								player.setIsPhantom(true);
								player.setIsPhantomMysticMuse(true);
								
								player.broadcastUserInfo();
								player.store();
								
								localL2ItemInstance5.setEnchantLevel(Rnd.get(Config.PHANTOM_PLAYERS_ENCHANT_MIN, Config.PHANTOM_PLAYERS_ENCHANT_MAX));
								if (Rnd.get(100) < 30 && Config.PHANTOM_PLAYERS_ARGUMENT_ANIM)
								{
									localL2ItemInstance5.setAugmentation(new L2Augmentation(1067847165, 3250, 1));
								}
								
								if (player.isDead())
								{
									player.doRevive();
								}
								
								player.setChargedShot(ShotType.SOULSHOT, true);
								player.setChargedShot(ShotType.BLESSED_SPIRITSHOT, true);
								
								player.addSkill(SkillTable.getInstance().getInfo(9901, 1), true);
								player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
								player.setCurrentCp(player.getMaxCp());
								
								startAttack(player);
								
								_PhantomsTown.get(1).add(player);
								_add_phantom.add(player);
								
								if (Config.PHANTOM_PLAYERS_SOULSHOT_ANIM)
								{
									try
									{
										Thread.sleep(900);
									}
									catch (InterruptedException e)
									{
									}
									player.broadcastPacket(new MagicSkillUse(player, player, 2154, 1, 0, 0));
									try
									{
										Thread.sleep(300);
									}
									catch (InterruptedException e)
									{
									}
									player.broadcastPacket(new MagicSkillUse(player, player, 2164, 1, 0, 0));
								}
								try
								{
									Thread.sleep(Config.PHANTOM_DELAY_SPAWN_FIRST);
								}
								catch (InterruptedException e)
								{
								}
								i++;
								
							}
							catch (Exception e)
							{
								_log.log(Level.WARNING, "FakePlayers: " + player, e);
								if (player != null)
								{
									player.deleteMe();
								}
							}
							ThreadPool.schedule(new Disconnection(player), Config.DISCONNETC_DELAY);
							
						}
						rs.close();
						stm.close();
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING, "FakePlayerss: ", e);
					}
					_log.info("Phantom [Mystic Muse] wave spawned " + i + " players.");
					list_name.clear();
					list_title.clear();
					break;
			}
		}
	}
	
	public class Disconnection implements Runnable
	{
		private Player _activeChar;
		
		public Disconnection(Player activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isPhantom())
			{
				removePhantom(_activeChar);
				final L2GameClient client = _activeChar.getClient();
				// detach the client from the char so that the connection isnt closed in the deleteMe
				_activeChar.setClient(null);
				// removing player from the world
				_activeChar.deleteMe();
				client.setActiveChar(null);
				client.setState(GameClientState.AUTHED);
			}
		}
		
	}
	
	public static boolean doCastlist(final Player player)
	{
		if (player.isDead())
			return false;
		
		List<Creature> targetList = new ArrayList<>();
		
		for (WorldObject obj : player.getKnownType(WorldObject.class))
		{
			if (obj instanceof Player)
			{
				if (obj instanceof Player)
				{
					if (!((Player) obj).isDead() && !((Player) obj).isSpawnProtected() && !(((Player) obj).isGM() && ((Player) obj).getAppearance().getInvisible()) && (((Player) obj).getPvpFlag() > 0 || ((Player) obj).getKarma() > 0) && ((Player) obj).isInsideRadius(player.getX(), player.getY(), player.getZ(), 850, false, false))
						targetList.add((Player) obj);
				}
			}
		}
		
		if (targetList.size() == 0)
		{
			for (WorldObject obj : player.getKnownType(WorldObject.class))
			{
				if (obj instanceof Player)
				{
					if (obj instanceof Player)
					{						
						if (!((Player) obj).isDead() && !((Player) obj).isSpawnProtected() && !(((Player) obj).isGM() && ((Player) obj).getAppearance().getInvisible()) && (((Player) obj).getPvpFlag() > 0 || ((Player) obj).getKarma() > 0) && ((Player) obj).isInsideRadius(player.getX(), player.getY(), player.getZ(), 6000, false, false))
							targetList.add((Player) obj);
					}
				}
			}
			
			if (targetList.size() == 0)
			{
				player.abortAttack();
				player.abortCast();
				player.stopMove(null);
				player.setTarget(null);
				ThreadPool.schedule(new Runnable()
				{
					@Override
					public void run()
					{
						if (!player.isDead())
						{
							if (Rnd.get(100) < Config.PHANTOM_MAGE_RANDOM_WALK)
							{
								player.rndWalk();
								try
								{
									Thread.sleep(2000);
								}
								catch (InterruptedException e)
								{
								}
							}
						}
						doCastlist(player);
					}
				}, 3000);
				
				return false;
			}
		}
		
		if (targetList.isEmpty())
			return true;
		
		// Choosing randomly a new target
		int nextTargetIdx = Rnd.get(targetList.size());
		
		WorldObject target = targetList.get(nextTargetIdx);
		
		// Attacking the target
		player.setTarget(target);
		player.setRunning();
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		
		doCast(player, (Player) target);
		return true;
	}
	
	static void Check_Finish(final Player player, final Player target)
	{
		if (player.isDead())
			return;
		
		if (!target.isDead() && player.getTarget() != null && GeoEngine.getInstance().canSeeTarget(player, target) && (target.getPvpFlag() > 0 || target.getKarma() > 0))
		{
			if (player.isAllSkillsDisabled())
			{
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{
				}
				Check_Finish(player, target);
				return;
			}
			doCast(player, target);
		}
		else
		{
			try
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException e)
			{
			}
			if (!player.isDead())
			{
				if (Rnd.get(100) < Config.PHANTOM_MAGE_RANDOM_WALK)
				{
					player.rndWalk();
					try
					{
						Thread.sleep(2000);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
			doCastlist(player);
		}
	}
	
	static void backRespawn(final Player player)
	{
		if (player.isDead())
			return;
		
		player.abortAttack();
		player.abortCast();
		player.stopMove(null);
		player.setTarget(null);
		player.getAI().setIntention(CtrlIntention.ATTACK, null);
		
		if (player.getX() != player.getLastX() && player.getY() != player.getLastY() && player.getZ() != player.getLastZ())
		{
			if (Rnd.get(100) < Config.PHANTOM_MAGE_RANDOM_WALK)
			{
				try
				{
					Thread.sleep(1200);
				}
				catch (InterruptedException e)
				{
				}
				player.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(player.getLastX(), player.getLastY(), player.getLastZ(), 0));
				try
				{
					Thread.sleep(1200);
				}
				catch (InterruptedException e)
				{
				}
			}
			doCastlist(player);
		}
		else
		{
			if (!player.isDead())
			{
				if (Rnd.get(100) < Config.PHANTOM_MAGE_RANDOM_WALK)
				{
					player.rndWalk();
					try
					{
						Thread.sleep(1200);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
			doCastlist(player);
		}
	}
	
	static void doCast(final Player player, final Player target)
	{
		if (player.isDead())
			return;
		
		if (target.isDead() || player.getTarget() == null || (target.getPvpFlag() == 0 && target.getKarma() == 0) || !GeoEngine.getInstance().canSeeTarget(player, target))
		{
			backRespawn(player);
			return;
		}
		
		Mage_Surrender(player, target, 1071, 14, Config.PHANTOM_SURRENDER_INTERVAL, 25);
		
		if (player.isDead())
			return;
		
		if (target.isDead() || player.getTarget() == null || (target.getPvpFlag() == 0 && target.getKarma() == 0) || !GeoEngine.getInstance().canSeeTarget(player, target))
		{
			backRespawn(player);
			return;
		}
		
		Mage_Attack(player, target, 1235, 28, Config.PHANTOM_SPELLSINGER_DANO_INTERVAL);
		
		if (player.isDead())
			return;
		
		if (target.isDead() || player.getTarget() == null || (target.getPvpFlag() == 0 && target.getKarma() == 0) || !GeoEngine.getInstance().canSeeTarget(player, target))
		{
			backRespawn(player);
			return;
		}
		
		Mage_Attack(player, target, 1235, 28, Config.PHANTOM_SPELLSINGER_DANO_INTERVAL);
		
		if (player.isDead())
			return;
		
		if (target.isDead() || player.getTarget() == null || (target.getPvpFlag() == 0 && target.getKarma() == 0) || !GeoEngine.getInstance().canSeeTarget(player, target))
		{
			backRespawn(player);
			return;
		}
		
		Mage_Attack(player, target, 1235, 28, Config.PHANTOM_SPELLSINGER_DANO_INTERVAL);
		
		if (player.isDead())
			return;
		
		if (target.isDead() || player.getTarget() == null || (target.getPvpFlag() == 0 && target.getKarma() == 0) || !GeoEngine.getInstance().canSeeTarget(player, target))
		{
			backRespawn(player);
			return;
		}
		
		Mage_Attack(player, target, 1236, 19, Config.PHANTOM_SPELLSINGER_DANO_INTERVAL);
		
		if (player.isDead())
			return;
		
		if (target.isDead() || player.getTarget() == null || (target.getPvpFlag() == 0 && target.getKarma() == 0) || !GeoEngine.getInstance().canSeeTarget(player, target))
		{
			backRespawn(player);
			return;
		}
		
		Mage_Attack(player, target, 1235, 28, Config.PHANTOM_SPELLSINGER_DANO_INTERVAL);
		
		if (player.isDead())
			return;
		
		if (target.isDead() || player.getTarget() == null || (target.getPvpFlag() == 0 && target.getKarma() == 0) || !GeoEngine.getInstance().canSeeTarget(player, target))
		{
			backRespawn(player);
			return;
		}
		
		Mage_Attack(player, target, 1265, 1, Config.PHANTOM_SPELLSINGER_DANO_INTERVAL);
		
		if (player.isDead())
			return;
		
		if (target.isDead() || player.getTarget() == null || (target.getPvpFlag() == 0 && target.getKarma() == 0) || !GeoEngine.getInstance().canSeeTarget(player, target))
		{
			backRespawn(player);
			return;
		}
		
		Check_Finish(player, target);
		
	}
	
	static void Mage_Surrender(Player player, Player target, int skill_id, int skill_level, int delay, int random)
	{
		if (!player.isAllSkillsDisabled() && !player.isDead())
		{
			checkRange(player, target);
			if (player.isInsideRadius(target.getX(), target.getY(), target.getZ(), 850, false, false) && GeoEngine.getInstance().canSeeTarget(player, target) && (target.getPvpFlag() > 0 || target.getKarma() > 0))
			{
				if (!target.isInvul())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skill_id, skill_level);
					skill.getEffects(target, target);
				}
				
				player.stopMove(null);
				player.broadcastPacket(new MagicSkillUse(player, target, skill_id, skill_level, 500, 0, false));
				target.getActingPlayer().getAI().clientStartAutoAttack();
				player.getActingPlayer().getAI().clientStartAutoAttack();
				
				if (player.getPvpFlag() > 0)
					PvpFlagTaskManager.getInstance().remove(player);
				
				player.setPvpFlag(1);
				player.broadcastUserInfo();
				try
				{
					Thread.sleep(delay);
				}
				catch (InterruptedException e)
				{
				}
				if (Rnd.get(100) < random)
				{
					player.broadcastPacket(new MagicSkillUse(player, target, skill_id, skill_level, 500, 0, false));
					try
					{
						Thread.sleep(delay);
					}
					catch (InterruptedException e)
					{
					}
				}
				player.getAI().setIntention(CtrlIntention.FOLLOW, null);
			}
		}
	}
	
	static void Mage_Attack(Player player, Player target, int skill_id, int skill_level, int delay)
	{
		if (!player.isAllSkillsDisabled() && !player.isDead())
		{
			checkRange(player, target);
			if (player.isInsideRadius(target.getX(), target.getY(), target.getZ(), 850, false, false) && GeoEngine.getInstance().canSeeTarget(player, target) && (target.getPvpFlag() > 0 || target.getKarma() > 0))
			{
				player.stopMove(null);
				player.broadcastPacket(new MagicSkillUse(player, target, skill_id, skill_level, 538, 0, false));
				target.getActingPlayer().getAI().clientStartAutoAttack();
				player.getActingPlayer().getAI().clientStartAutoAttack();
				
				if (player.getPvpFlag() > 0)
					PvpFlagTaskManager.getInstance().remove(player);
				
				player.setPvpFlag(1);
				player.broadcastUserInfo();
				
				double mDef = target.getMDef(player, null);
				double damage = 91 * Math.sqrt(Config.PHANTOM_SPELLSINGER_MATCK) / mDef * 1000;
				
				if (Rnd.get(100) < Config.PHANTOM_SPELLSINGER_PERCENTAGE)
					target.reduceCurrentHp(damage, player, null);
				else
					target.reduceCurrentHp(damage / 2, player, null);
				
				try
				{
					Thread.sleep(delay);
				}
				catch (InterruptedException e)
				{
				}
				player.getAI().setIntention(CtrlIntention.FOLLOW, null);
			}
		}
	}
	
	static void checkRange(Player player, Player target)
	{
		if (player.isDead())
			return;
		
		if (!player.isInsideRadius(target.getX(), target.getY(), target.getZ(), 850, false, false) && !player.isMovementDisabled())
		{
			player.abortCast();
			player.getAI().setIntention(CtrlIntention.FOLLOW, target);
			try
			{
				Thread.sleep(1200);
			}
			catch (InterruptedException e)
			{
			}
		}
	}
	
	static String getFixTitle()
	{
		return Config.PHANTOM_TITLE.get(Rnd.get(Config.PHANTOM_TITLE.size()));
	}
}
