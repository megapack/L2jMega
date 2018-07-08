package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.communitybbs.CommunityBoard;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.sf.l2j.gameserver.instancemanager.BotsPreventionManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.OlympiadManagerNpc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.base.Sex;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.FloodProtectors.Action;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.ChangeNameLog;

public final class RequestBypassToServer extends L2GameClientPacket
{
	
	protected static final Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
	private static final Logger GMAUDIT_LOG = Logger.getLogger("gmaudit");
	
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if (_command.isEmpty())
			return;
		
		if (!FloodProtectors.performAction(getClient(), Action.SERVER_BYPASS))
			return;
		
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (_command.startsWith("admin_"))
		{
			String command = _command.split(" ")[0];
			
			final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
			if (ach == null)
			{
				if (player.isGM())
					player.sendMessage("The command " + command.substring(6) + " doesn't exist.");
				
				LOGGER.warn("No handler registered for admin command '{}'.", command);
				return;
			}
			
			if (!AdminData.getInstance().hasAccess(command, player.getAccessLevel()))
			{
				player.sendMessage("You don't have the access rights to use this command.");
				LOGGER.warn("{} tried to use admin command '{}' without proper Access Level.", player.getName(), command);
				return;
			}
			
			if (Config.GMAUDIT)
				GMAUDIT_LOG.info(player.getName() + " [" + player.getObjectId() + "] used '" + _command + "' command on: " + ((player.getTarget() != null) ? player.getTarget().getName() : "none"));
			
			ach.useAdminCommand(_command, player);
		}
		else if (_command.startsWith("player_help "))
		{
			final String path = _command.substring(12);
			if (path.indexOf("..") != -1)
				return;
			
			final StringTokenizer st = new StringTokenizer(path);
			final String[] cmd = st.nextToken().split("#");
			
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/help/" + cmd[0]);
			if (cmd.length > 1)
				html.setItemId(Integer.parseInt(cmd[1]));
			html.disableValidation();
			player.sendPacket(html);
		}
		else if (this._command.startsWith("voiced_"))
        {
         String command = this._command.split(" ")[0];
          
          IVoicedCommandHandler ach = VoicedCommandHandler.getInstance().getHandler(this._command.substring(7));
          if (ach == null)
          {
        	  player.sendMessage("The command " + command.substring(7) + " does not exist!");
        	  LOGGER.warn("No handler registered for command '" + this._command + "'");
            return;
          }
          ach.useVoicedCommand(this._command.substring(7), player, null);
        }
		else if (_command.startsWith("npc_"))
		{
			if (!player.validateBypass(_command))
				return;
			
			player.setIsUsingCMultisell(false);
			
			int endOfId = _command.indexOf('_', 5);
			String id;
			if (endOfId > 0)
				id = _command.substring(4, endOfId);
			else
				id = _command.substring(4);
			
			try
			{
				final WorldObject object = World.getInstance().getObject(Integer.parseInt(id));
				
				if (object != null && object instanceof Npc && endOfId > 0 && ((Npc) object).canInteract(player))
					((Npc) object).onBypassFeedback(player, _command.substring(endOfId + 1));
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		else if (_command.startsWith("report"))
			{
			       BotsPreventionManager.getInstance().AnalyseBypass(_command,player);
			}
        else if (this._command.startsWith("name_change"))
        {
          String clientInfo = player.getClient().toString();
          String ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"));
          try
          {
            String name = this._command.substring(12);
            if (name.length() > 16)
            {
            	player.sendMessage("The chosen name cannot exceed 16 characters in length.");
              return;
            }
            if (name.length() < 3)
            {
            	player.sendMessage("Your name can not be mention that 3 characters in length.");
              return;
            }
            if (!StringUtil.isValidPlayerName(name))
            {
            	player.sendMessage("The new name doesn't fit with the regex pattern.");
              return;
            }
            if (PlayerInfoTable.getInstance().getPlayerObjectId(name) > 0)
            {
            	player.sendMessage("The chosen name already exists.");
              return;
            }
            if (player.destroyItemByItemId("Name Change", player.getNameChangeItemId(), 1, null, true))
            {
              ChangeNameLog.auditGMAction(player.getObjectId(), player.getName(), name, ip);
              for (Player gm : World.getAllGMs()) {
                gm.sendPacket(new CreatureSay(0, 1, "[Name]", player.getName() + " mudou o nome para [" + name + "]"));
              }
              player.setName(name);
              PlayerInfoTable.getInstance().updatePlayerData(player, false);
              player.sendPacket(new ExShowScreenMessage("Congratulations. Your name has been changed.", 6000, 2, true));
              player.broadcastUserInfo();
              player.store();
              player.sendPacket(new PlaySound("ItemSound.quest_finish"));
            }
          }
          catch (Exception e)
          {
        	  player.sendMessage("Fill out the field correctly.");
          }
        }
        String type;
        if (this._command.startsWith("classe_change"))
        {
          StringTokenizer st = new StringTokenizer(this._command);
          st.nextToken();
          type = null;
          type = st.nextToken();
          try
          {
            if (player.getBaseClass() != player.getClassId().getId())
            {
            	player.sendMessage("SYS: Voce precisa estar com sua Classe Base para usar este item.");
            	player.sendPacket(new ExShowScreenMessage("You is not with its base class.", 6000, 2, true));
              return;
            }
            if (player.isInOlympiadMode())
            {
            	player.sendMessage("This Item Cannot Be Used On Olympiad Games.");
              return;
            }
            ClassChangeCoin(player, type);
          }
          catch (StringIndexOutOfBoundsException localStringIndexOutOfBoundsException) {}
        }
        else if (this._command.startsWith("classe_index"))
        {
          NpcHtmlMessage html = new NpcHtmlMessage(0);
          html.setFile("data/html/mods/Coin Custom/classes.htm");
          player.sendPacket(html);
          player.sendPacket(ActionFailed.STATIC_PACKET);
        }
        else if (this._command.startsWith("change_sex"))
        {
          if (player.destroyItemByItemId("Sex Change", player.getSexChangeItemId(), 1, null, true))
          {
            Sex male = Sex.MALE;
            Sex female = Sex.FEMALE;
            if (player.getAppearance().getSex() == male)
            {
            	player.getAppearance().setSex(female);
            	player.sendPacket(new ExShowScreenMessage("Congratulations. Your Sex has been changed.", 6000, 2, true));
            	player.broadcastUserInfo();
            	player.decayMe();
              player.spawnMe();
            }
            else if (player.getAppearance().getSex() == female)
            {
            	player.getAppearance().setSex(male);
            	player.sendPacket(new ExShowScreenMessage("Congratulations. Your Sex has been changed.", 6000, 2, true));
            	player.broadcastUserInfo();
            	player.decayMe();
              player.spawnMe();
            }
            for (Player gm : World.getAllGMs()) {
              gm.sendPacket(new CreatureSay(0, 1, "SYS", player.getName() + " acabou de trocar de Sexo."));
            }
            ThreadPool.schedule(new Runnable()
            {
              @Override
			public void run()
              {
            	  player.logout();
              }
            }, 2000L);
          }
        }
		// Navigate throught Manor windows
		else if (_command.startsWith("manor_menu_select?"))
		{
			WorldObject object = player.getTarget();
			if (object instanceof Npc)
				((Npc) object).onBypassFeedback(player, _command);
		}
		else if (_command.startsWith("bbs_") || _command.startsWith("_bbs") || _command.startsWith("_friend") || _command.startsWith("_mail") || _command.startsWith("_block"))
		{
			CommunityBoard.getInstance().handleCommands(getClient(), _command);
		}
		else if (_command.startsWith("Quest "))
		{
			if (!player.validateBypass(_command))
				return;
			
			String[] str = _command.substring(6).trim().split(" ", 2);
			if (str.length == 1)
				player.processQuestEvent(str[0], "");
			else
				player.processQuestEvent(str[0], str[1]);
		}
		else if (_command.startsWith("_match"))
		{
			String params = _command.substring(_command.indexOf("?") + 1);
			StringTokenizer st = new StringTokenizer(params, "&");
			int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
			int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
			int heroid = Hero.getInstance().getHeroByClass(heroclass);
			if (heroid > 0)
				Hero.getInstance().showHeroFights(player, heroclass, heroid, heropage);
		}
		else if (_command.startsWith("_diary"))
		{
			String params = _command.substring(_command.indexOf("?") + 1);
			StringTokenizer st = new StringTokenizer(params, "&");
			int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
			int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
			int heroid = Hero.getInstance().getHeroByClass(heroclass);
			if (heroid > 0)
				Hero.getInstance().showHeroDiary(player, heroclass, heroid, heropage);
		}
		else if (_command.startsWith("arenachange")) // change
		{
			final boolean isManager = player.getCurrentFolk() instanceof OlympiadManagerNpc;
			if (!isManager)
			{
				// Without npc, command can be used only in observer mode on arena
				if (!player.isInObserverMode() || player.isInOlympiadMode() || player.getOlympiadGameId() < 0)
					return;
			}

			
			if (OlympiadManager.getInstance().isRegisteredInComp(player))
			{
				player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
				return;
			}
			
			final int arenaId = Integer.parseInt(_command.substring(12).trim());
			player.enterOlympiadObserverMode(arenaId);
		}
	}
	
	  private static void ClassChangeCoin(Player player, String command)
	  {
	    String nameclasse = player.getTemplate().getClassName();
	    
	    String type = command;
	    if (type.equals("---SELECIONE---"))
	    {
	      NpcHtmlMessage html = new NpcHtmlMessage(0);
	      html.setFile("data/html/mods/Coin Custom/classes.htm");
	      player.sendPacket(html);
	      player.sendPacket(ActionFailed.STATIC_PACKET);
	      player.sendMessage("Por favor, Selecione a Classe desejada para continuar.");
	    }
	    if (type.equals("Duelist"))
	    {
	      if (player.getClassId().getId() == 88)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(88);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(88);
	      }
	      Finish(player);
	    }
	    if (type.equals("DreadNought"))
	    {
	      if (player.getClassId().getId() == 89)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(89);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(89);
	      }
	      Finish(player);
	    }
	    if (type.equals("Phoenix_Knight"))
	    {
	      if (player.getClassId().getId() == 90)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(90);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(90);
	      }
	      Finish(player);
	    }
	    if (type.equals("Hell_Knight"))
	    {
	      if (player.getClassId().getId() == 91)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(91);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(91);
	      }
	      Finish(player);
	    }
	    if (type.equals("Sagittarius"))
	    {
	      if (player.getClassId().getId() == 92)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(92);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(92);
	      }
	      Finish(player);
	    }
	    if (type.equals("Adventurer"))
	    {
	      if (player.getClassId().getId() == 93)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(93);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(93);
	      }
	      Finish(player);
	    }
	    if (type.equals("Archmage"))
	    {
	      if (player.getClassId().getId() == 94)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(94);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(94);
	      }
	      Finish(player);
	    }
	    if (type.equals("Soultaker"))
	    {
	      if (player.getClassId().getId() == 95)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(95);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(95);
	      }
	      Finish(player);
	    }
	    if (type.equals("Arcana_Lord"))
	    {
	      if (player.getClassId().getId() == 96)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(96);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(96);
	      }
	      Finish(player);
	    }
	    if (type.equals("Cardinal"))
	    {
	      if (player.getClassId().getId() == 97)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(97);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(97);
	      }
	      Finish(player);
	    }
	    if (type.equals("Hierophant"))
	    {
	      if (player.getClassId().getId() == 98)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(98);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(98);
	      }
	      Finish(player);
	    }
	    if (type.equals("Eva_Templar"))
	    {
	      if (player.getClassId().getId() == 99)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(99);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(99);
	      }
	      Finish(player);
	    }
	    if (type.equals("Sword_Muse"))
	    {
	      if (player.getClassId().getId() == 100)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(100);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(100);
	      }
	      Finish(player);
	    }
	    if (type.equals("Wind_Rider"))
	    {
	      if (player.getClassId().getId() == 101)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(101);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(101);
	      }
	      Finish(player);
	    }
	    if (type.equals("Moonli_Sentinel"))
	    {
	      if (player.getClassId().getId() == 102)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(102);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(102);
	      }
	      Finish(player);
	    }
	    if (type.equals("Mystic_Muse"))
	    {
	      if (player.getClassId().getId() == 103)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(103);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(103);
	      }
	      Finish(player);
	    }
	    if (type.equals("Elemental_Master"))
	    {
	      if (player.getClassId().getId() == 104)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(104);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(104);
	      }
	      Finish(player);
	    }
	    if (type.equals("Eva_Saint"))
	    {
	      if (player.getClassId().getId() == 105)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(105);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(105);
	      }
	      Finish(player);
	    }
	    if (type.equals("Shillien_Templar"))
	    {
	      if (player.getClassId().getId() == 106)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(106);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(106);
	      }
	      Finish(player);
	    }
	    if (type.equals("Spectral_Dancer"))
	    {
	      if (player.getClassId().getId() == 107)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(107);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(107);
	      }
	      Finish(player);
	    }
	    if (type.equals("Ghost_Hunter"))
	    {
	      if (player.getClassId().getId() == 108)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(108);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(108);
	      }
	      Finish(player);
	    }
	    if (type.equals("Ghost_Sentinel"))
	    {
	      if (player.getClassId().getId() == 109)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(109);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(109);
	      }
	      Finish(player);
	    }
	    if (type.equals("Storm_Screamer"))
	    {
	      if (player.getClassId().getId() == 110)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(110);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(110);
	      }
	      Finish(player);
	    }
	    if (type.equals("Spectral_Master"))
	    {
	      if (player.getClassId().getId() == 111)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(111);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(111);
	      }
	      Finish(player);
	    }
	    if (type.equals("Shillen_Saint"))
	    {
	      if (player.getClassId().getId() == 112)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(112);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(112);
	      }
	      Finish(player);
	    }
	    if (type.equals("Titan"))
	    {
	      if (player.getClassId().getId() == 113)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(113);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(113);
	      }
	      Finish(player);
	    }
	    if (type.equals("Grand_Khauatari"))
	    {
	      if (player.getClassId().getId() == 114)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(114);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(114);
	      }
	      Finish(player);
	    }
	    if (type.equals("Dominator"))
	    {
	      if (player.getClassId().getId() == 115)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(115);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(115);
	      }
	      Finish(player);
	    }
	    if (type.equals("Doomcryer"))
	    {
	      if (player.getClassId().getId() == 116)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(116);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(116);
	      }
	      Finish(player);
	    }
	    if (type.equals("Fortune_Seeker"))
	    {
	      if (player.getClassId().getId() == 117)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(117);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(117);
	      }
	      Finish(player);
	    }
	    if (type.equals("Maestro"))
	    {
	      if (player.getClassId().getId() == 118)
	      {
	        player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
	        return;
	      }
	      RemoverSkills(player);
	      
	      player.setClassId(118);
	      if (!player.isSubClassActive()) {
	        player.setBaseClass(118);
	      }
	      Finish(player);
	    }
	  }
	  
	  private static void RemoverSkills(Player activeChar)
	  {
		for (L2Skill skill : activeChar.getSkills().values())
			activeChar.removeSkill(skill.getId(), true);
	    activeChar.destroyItemByItemId("Classe Change", activeChar.getClassChangeItemId(), 1, null, true);
	  }

	public static void Finish(Player activeChar)
	  {
	    String newclass = activeChar.getTemplate().getClassName();
	    
	    activeChar.sendMessage(activeChar.getName() + " is now a " + newclass + ".");
	    activeChar.sendPacket(new ExShowScreenMessage("Congratulations. You is now a " + newclass + ".", 6000, 2, true));
	    
	    activeChar.refreshOverloaded();
	    activeChar.store();
	    activeChar.sendPacket(new HennaInfo(activeChar));
	    activeChar.sendSkillList();
	    activeChar.broadcastUserInfo();
	    
	    activeChar.sendPacket(new PlaySound("ItemSound.quest_finish"));
	    for (Iterator<?> localIterator = World.getAllGMs().iterator(); localIterator.hasNext();)
	    {
	      activeChar = (Player)localIterator.next();
	      activeChar.sendPacket(new CreatureSay(0, 1, "Chat Manager", activeChar.getName() + " acabou de trocar sua Classe Base."));
	    }

	    if (activeChar.isNoble())
	    {
	      StatsSet playerStat = Olympiad.getNobleStats(activeChar.getObjectId());
	      if (playerStat != null)
	      {
	        AdminEditChar.updateClasse(activeChar);
	        AdminEditChar.DeleteHero(activeChar);
	        activeChar.sendMessage("You now has " + Olympiad.getInstance().getNoblePoints(activeChar.getObjectId()) + " Olympiad points.");
	      }
	    }
	    
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
	        statement.setInt(1, activeChar.getObjectId());
	        statement.setInt(2, 0);
	        statement.execute();
	        statement.close();
	      }
	    catch (Exception e)
	    {
	      _log.warning("Class Item: " + e);
	    }
	    activeChar.logout(true);
	  }
}