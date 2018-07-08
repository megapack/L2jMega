/*
 * Copyright (C) 2004-2013 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.events.teamvsteam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.commons.config.ExProperties;
import net.sf.l2j.util.StringUtil;

public class TvTConfig
{
	protected static final Logger _log = Logger.getLogger(TvTConfig.class.getName());
	
	private static final String TVT_FILE = "./config/custom/events/tvt_settings.properties";
	
	public static boolean TVT_EVENT_ENABLED;
	public static String[] TVT_EVENT_INTERVAL;
	public static int TVT_EVENT_PARTICIPATION_TIME;
	public static int TVT_EVENT_RUNNING_TIME;
	public static String TVT_NPC_LOC_NAME;
	public static int TVT_EVENT_PARTICIPATION_NPC_ID;
	public static int[] TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] TVT_EVENT_PARTICIPATION_FEE = new int[2];
	public static int TVT_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_MAX_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_RESPAWN_TELEPORT_DELAY;
	public static int TVT_EVENT_START_LEAVE_TELEPORT_DELAY;
	public static String TVT_EVENT_TEAM_1_NAME;
	public static int[] TVT_EVENT_TEAM_1_COORDINATES = new int[3];
	public static String TVT_EVENT_TEAM_2_NAME;
	public static int[] TVT_EVENT_TEAM_2_COORDINATES = new int[3];
	public static List<int[]> TVT_EVENT_REWARDS;
	public static boolean TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean TVT_EVENT_SCROLL_ALLOWED;
	public static boolean TVT_EVENT_POTIONS_ALLOWED;
	public static boolean TVT_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> TVT_DOORS_IDS_TO_OPEN;
	public static List<Integer> TVT_DOORS_IDS_TO_CLOSE;
	public static boolean TVT_REWARD_TEAM_TIE;
	public static byte TVT_EVENT_MIN_LVL;
	public static byte TVT_EVENT_MAX_LVL;
	public static int TVT_EVENT_EFFECTS_REMOVAL;
	public static Map<Integer, Integer> TVT_EVENT_FIGHTER_BUFFS;
	public static Map<Integer, Integer> TVT_EVENT_MAGE_BUFFS;
	public static boolean TVT_EVENT_MULTIBOX_PROTECTION_ENABLE;
	public static int TVT_EVENT_NUMBER_BOX_REGISTER;
	public static boolean TVT_REWARD_PLAYER;
	public static String TVT_EVENT_ON_KILL;
	public static String DISABLE_ID_CLASSES_STRING;
	public static List<Integer> DISABLE_ID_CLASSES;
	
	public static void init()
	{
		ExProperties events = load(TVT_FILE);
		
		TVT_EVENT_ENABLED = events.getProperty("TvTEventEnabled", false);
		TVT_EVENT_INTERVAL = events.getProperty("TvTEventInterval", "20:00").split(",");
		TVT_EVENT_PARTICIPATION_TIME = events.getProperty("TvTEventParticipationTime", 3600);
		TVT_EVENT_RUNNING_TIME = events.getProperty("TvTEventRunningTime", 1800);
		TVT_NPC_LOC_NAME = events.getProperty("TvTNpcLocName", "Giran Town");
		TVT_EVENT_PARTICIPATION_NPC_ID = events.getProperty("TvTEventParticipationNpcId", 0);
		
		if (TVT_EVENT_PARTICIPATION_NPC_ID == 0)
		{
			TVT_EVENT_ENABLED = false;
			_log.warning("TvTEventEngine: invalid config property -> TvTEventParticipationNpcId");
		}
		else
		{
			String[] propertySplit = events.getProperty("TvTEventParticipationNpcCoordinates", "0,0,0").split(",");
			if (propertySplit.length < 3)
			{
				TVT_EVENT_ENABLED = false;
				_log.warning("TvTEventEngine: invalid config property -> TvTEventParticipationNpcCoordinates");
			}
			else
			{
				TVT_EVENT_REWARDS = new ArrayList<>();
				TVT_DOORS_IDS_TO_OPEN = new ArrayList<>();
				TVT_DOORS_IDS_TO_CLOSE = new ArrayList<>();
				TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
				TVT_EVENT_TEAM_1_COORDINATES = new int[3];
				TVT_EVENT_TEAM_2_COORDINATES = new int[3];
				TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
				TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
				TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
				if (propertySplit.length == 4)
				{
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
				}
				TVT_EVENT_MIN_PLAYERS_IN_TEAMS = events.getProperty("TvTEventMinPlayersInTeams", 1);
				TVT_EVENT_MAX_PLAYERS_IN_TEAMS = events.getProperty("TvTEventMaxPlayersInTeams", 20);
				TVT_EVENT_MIN_LVL = Byte.parseByte(events.getProperty("TvTEventMinPlayerLevel", "1"));
				TVT_EVENT_MAX_LVL = Byte.parseByte(events.getProperty("TvTEventMaxPlayerLevel", "80"));
				TVT_EVENT_RESPAWN_TELEPORT_DELAY = events.getProperty("TvTEventRespawnTeleportDelay", 20);
				TVT_EVENT_START_LEAVE_TELEPORT_DELAY = events.getProperty("TvTEventStartLeaveTeleportDelay", 20);
				TVT_EVENT_EFFECTS_REMOVAL = events.getProperty("TvTEventEffectsRemoval", 0);
				TVT_EVENT_TEAM_1_NAME = events.getProperty("TvTEventTeam1Name", "Team1");
				propertySplit = events.getProperty("TvTEventTeam1Coordinates", "0,0,0").split(",");
				if (propertySplit.length < 3)
				{
					TVT_EVENT_ENABLED = false;
					_log.warning("TvTEventEngine: invalid config property -> TvTEventTeam1Coordinates");
				}
				else
				{
					TVT_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
					TVT_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
					TVT_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
					TVT_EVENT_TEAM_2_NAME = events.getProperty("TvTEventTeam2Name", "Team2");
					propertySplit = events.getProperty("TvTEventTeam2Coordinates", "0,0,0").split(",");
					if (propertySplit.length < 3)
					{
						TVT_EVENT_ENABLED = false;
						_log.warning("TvTEventEngine: invalid config property -> TvTEventTeam2Coordinates");
					}
					else
					{
						TVT_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
						TVT_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
						TVT_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
						propertySplit = events.getProperty("TvTEventParticipationFee", "0,0").split(",");
						try
						{
							TVT_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
							TVT_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
						}
						catch (NumberFormatException nfe)
						{
							if (propertySplit.length > 0)
							{
								_log.warning("TvTEventEngine: invalid config property -> TvTEventParticipationFee");
							}
						}
						propertySplit = events.getProperty("TvTEventReward", "57,100000").split(";");
						for (String reward : propertySplit)
						{
							String[] rewardSplit = reward.split(",");
							if (rewardSplit.length != 2)
							{
								_log.warning(StringUtil.concat("TvTEventEngine: invalid config property -> TvTEventReward \"", reward, "\""));
							}
							else
							{
								try
								{
									TVT_EVENT_REWARDS.add(new int[]
									{
										Integer.parseInt(rewardSplit[0]),
										Integer.parseInt(rewardSplit[1])
									});
								}
								catch (NumberFormatException nfe)
								{
									if (!reward.isEmpty())
									{
										_log.warning(StringUtil.concat("TvTEventEngine: invalid config property -> TvTEventReward \"", reward, "\""));
									}
								}
							}
						}
						
						TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = events.getProperty("TvTEventTargetTeamMembersAllowed", true);
						TVT_EVENT_SCROLL_ALLOWED = events.getProperty("TvTEventScrollsAllowed", false);
						TVT_EVENT_POTIONS_ALLOWED = events.getProperty("TvTEventPotionsAllowed", false);
						TVT_EVENT_SUMMON_BY_ITEM_ALLOWED = events.getProperty("TvTEventSummonByItemAllowed", false);
						TVT_REWARD_TEAM_TIE = events.getProperty("TvTRewardTeamTie", false);
						propertySplit = events.getProperty("TvTDoorsToOpen", "").split(";");
						for (String door : propertySplit)
						{
							try
							{
								TVT_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
							}
							catch (NumberFormatException nfe)
							{
								if (!door.isEmpty())
								{
									_log.warning(StringUtil.concat("TvTEventEngine: invalid config property -> TvTDoorsToOpen \"", door, "\""));
								}
							}
						}
						
						propertySplit = events.getProperty("TvTDoorsToClose", "").split(";");
						for (String door : propertySplit)
						{
							try
							{
								TVT_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
							}
							catch (NumberFormatException nfe)
							{
								if (!door.isEmpty())
								{
									_log.warning(StringUtil.concat("TvTEventEngine: invalid config property -> TvTDoorsToClose \"", door, "\""));
								}
							}
						}
						
						propertySplit = events.getProperty("TvTEventFighterBuffs", "").split(";");
						if (!propertySplit[0].isEmpty())
						{
							TVT_EVENT_FIGHTER_BUFFS = new HashMap<>(propertySplit.length);
							for (String skill : propertySplit)
							{
								String[] skillSplit = skill.split(",");
								if (skillSplit.length != 2)
								{
									_log.warning(StringUtil.concat("TvTEventEngine: invalid config property -> TvTEventFighterBuffs \"", skill, "\""));
								}
								else
								{
									try
									{
										TVT_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
									}
									catch (NumberFormatException nfe)
									{
										if (!skill.isEmpty())
										{
											_log.warning(StringUtil.concat("TvTEventEngine: invalid config property -> TvTEventFighterBuffs \"", skill, "\""));
										}
									}
								}
							}
						}
						
						propertySplit = events.getProperty("TvTEventMageBuffs", "").split(";");
						if (!propertySplit[0].isEmpty())
						{
							TVT_EVENT_MAGE_BUFFS = new HashMap<>(propertySplit.length);
							for (String skill : propertySplit)
							{
								String[] skillSplit = skill.split(",");
								if (skillSplit.length != 2)
								{
									_log.warning(StringUtil.concat("TvTEventEngine: invalid config property -> TvTEventMageBuffs \"", skill, "\""));
								}
								else
								{
									try
									{
										TVT_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
									}
									catch (NumberFormatException nfe)
									{
										if (!skill.isEmpty())
										{
											_log.warning(StringUtil.concat("TvTEventEngine: invalid config property -> TvTEventMageBuffs \"", skill, "\""));
										}
									}
								}
							}
						}
						
                        TVT_EVENT_MULTIBOX_PROTECTION_ENABLE = events.getProperty("TvTEventMultiBoxEnable", false);
                        TVT_EVENT_NUMBER_BOX_REGISTER = events.getProperty("TvTEventNumberBoxRegister", 1);
                        
                        TVT_REWARD_PLAYER = events.getProperty("TvTRewardOnlyKillers", false);
                        
                        TVT_EVENT_ON_KILL = events.getProperty("TvTEventOnKill", "pmteam");
            			DISABLE_ID_CLASSES_STRING = events.getProperty("TvTDisabledForClasses");
            			DISABLE_ID_CLASSES = new ArrayList<>();
            			for(String class_id : DISABLE_ID_CLASSES_STRING.split(","))
            				DISABLE_ID_CLASSES.add(Integer.parseInt(class_id));
					}
				}
			}
		}
	}
	
	public static ExProperties load(String filename)
	{
		return load(new File(filename));
	}
	
	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();
		
		try
		{
			result.load(file);
		}
		catch (IOException e)
		{
			_log.warning("Error loading config : " + file.getName() + "!");
		}
		
		return result;
	}
}