/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.events.ArenaTask;
import net.sf.l2j.gameserver.events.PartyFarm;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;

import phantom.PhantomArchMage;
import phantom.PhantomMysticMuse;
import phantom.PhantomStormScream;

/**
 * @author Mega
 */
public class AdminCustom implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_tour",
		"admin_ptfarm",
		"admin_fakepvp"

	};
	
	protected static final Logger _log = Logger.getLogger(AdminCustom.class.getName());

	public static boolean _bestfarm_manual = false;
	public static boolean _arena_manual = false;


	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{

	    if (command.equals("admin_tour"))
	    {
	      if (ArenaTask._started)
	      {
	        _log.info("----------------------------------------------------------------------------");
	        _log.info("[Tournament]: Event Finished.");
	        _log.info("----------------------------------------------------------------------------");
	        ArenaTask._aborted = true;
	        finishEventArena();
	        _arena_manual = true;
	        
	        activeChar.sendMessage("SYS: Voce Finalizou o evento Tournament Manualmente..");
	      }
	      else
	      {
	        _log.info("----------------------------------------------------------------------------");
	        _log.info("[Tournament]: Event Started.");
	        _log.info("----------------------------------------------------------------------------");
	        initEventArena();
	        _arena_manual = true;
	        activeChar.sendMessage("SYS: Voce ativou o evento Tournament Manualmente..");
	      }
	    }	
	    else if (command.equals("admin_ptfarm"))
	    {
	      if (PartyFarm._started)
	      {
	        _log.info("----------------------------------------------------------------------------");
	        _log.info("[Party Farm]: Event Finished.");
	        _log.info("----------------------------------------------------------------------------");
	        PartyFarm._aborted = true;
	        finishEventPartyFarm();
	        
	        activeChar.sendMessage("SYS: Voce Finalizou o Party Farm Manualmente..");
	      }
	      else
	      {
	        _log.info("----------------------------------------------------------------------------");
	        _log.info("[Party Farm]: Event Started.");
	        _log.info("----------------------------------------------------------------------------");
	        initEventPartyFarm();
	        _bestfarm_manual = true;
	        activeChar.sendMessage("SYS: Voce ativou o Best Farm Manualmente..");
	      }
	    }
	    if (command.equals("admin_fakepvp"))
	    {
	      ThreadPool.schedule(new Runnable()
	      {
	        @Override
			public void run()
	        {
	          PhantomArchMage.init();
	          PhantomMysticMuse.init();
	          PhantomStormScream.init();
		        activeChar.sendMessage("SYS: Voce ativou o fake pvp!");
	          AdminCustom._log.info("[Phantom Task]: Starded!");
	        }
	      }, 1L);
	    }
		return true;
	}
	

	
	  private static void initEventArena()
	  {
	    ThreadPool.schedule(new Runnable()
	    {
	    	@Override
	      public void run() {
	    	  
	    	  ArenaTask.SpawnEvent();
	      }
	    }, 1L);
	  }
	  
	  private static void finishEventArena()
	  {
	    ThreadPool.schedule(new Runnable()
	    {
	    	@Override
	      public void run() {
	    		
	    		ArenaTask.finishEvent();
	      }
	    }, 1L);
	  }

	  private static void initEventPartyFarm()
	  {
	    ThreadPool.schedule(new Runnable()
	    {
	    	@Override
	      public void run() {
	    	  
	    	  PartyFarm.bossSpawnMonster();
	      }
	    }, 1L);
	  }
	  
	  private static void finishEventPartyFarm()
	  {
	    ThreadPool.schedule(new Runnable()
	    {
	    	@Override
	      public void run() {

					PartyFarm.Finish_Event();

				
	      }
	    }, 1L);
	  }

	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
