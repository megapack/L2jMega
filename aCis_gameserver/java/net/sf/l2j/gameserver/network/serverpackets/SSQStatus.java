package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Map.Entry;

import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.CabalType;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.SealType;
import net.sf.l2j.gameserver.instancemanager.SevenSignsFestival;
import net.sf.l2j.gameserver.instancemanager.SevenSignsFestival.FestivalType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.templates.StatsSet;

public class SSQStatus extends L2GameServerPacket
{
	private final int _objectId;
	private final int _page;
	
	public SSQStatus(int objectId, int recordPage)
	{
		_objectId = objectId;
		_page = recordPage;
	}
	
	@Override
	protected final void writeImpl()
	{
		final CabalType winningCabal = SevenSigns.getInstance().getCabalHighestScore();
		final int totalDawnMembers = SevenSigns.getInstance().getTotalMembers(CabalType.DAWN);
		final int totalDuskMembers = SevenSigns.getInstance().getTotalMembers(CabalType.DUSK);
		
		writeC(0xf5);
		
		writeC(_page);
		writeC(SevenSigns.getInstance().getCurrentPeriod().ordinal());
		
		int dawnPercent = 0;
		int duskPercent = 0;
		
		switch (_page)
		{
			case 1:
				// [ddd cc dd ddd c ddd c]
				writeD(SevenSigns.getInstance().getCurrentCycle());
				
				switch (SevenSigns.getInstance().getCurrentPeriod())
				{
					case RECRUITING:
						writeD(SystemMessageId.INITIAL_PERIOD.getId());
						writeD(SystemMessageId.UNTIL_TODAY_6PM.getId());
						break;
					
					case COMPETITION:
						writeD(SystemMessageId.QUEST_EVENT_PERIOD.getId());
						writeD(SystemMessageId.UNTIL_MONDAY_6PM.getId());
						break;
					
					case RESULTS:
						writeD(SystemMessageId.RESULTS_PERIOD.getId());
						writeD(SystemMessageId.UNTIL_TODAY_6PM.getId());
						break;
					
					case SEAL_VALIDATION:
						writeD(SystemMessageId.VALIDATION_PERIOD.getId());
						writeD(SystemMessageId.UNTIL_MONDAY_6PM.getId());
						break;
				}
				
				writeC(SevenSigns.getInstance().getPlayerCabal(_objectId).ordinal());
				writeC(SevenSigns.getInstance().getPlayerSeal(_objectId).ordinal());
				
				writeD(SevenSigns.getInstance().getPlayerStoneContrib(_objectId)); // Seal Stones Turned-In
				writeD(SevenSigns.getInstance().getPlayerAdenaCollect(_objectId)); // Ancient Adena to Collect
				
				double dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(CabalType.DAWN);
				int dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(CabalType.DAWN);
				
				double duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(CabalType.DUSK);
				int duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(CabalType.DUSK);
				
				double totalStoneScore = duskStoneScore + dawnStoneScore;
				
				/*
				 * Scoring seems to be proportionate to a set base value, so base this on the maximum obtainable score from festivals, which is 500.
				 */
				int duskStoneScoreProp = 0;
				int dawnStoneScoreProp = 0;
				
				if (totalStoneScore != 0)
				{
					duskStoneScoreProp = Math.round(((float) duskStoneScore / (float) totalStoneScore) * 500);
					dawnStoneScoreProp = Math.round(((float) dawnStoneScore / (float) totalStoneScore) * 500);
				}
				
				int duskTotalScore = SevenSigns.getInstance().getCurrentScore(CabalType.DUSK);
				int dawnTotalScore = SevenSigns.getInstance().getCurrentScore(CabalType.DAWN);
				
				int totalOverallScore = duskTotalScore + dawnTotalScore;
				
				if (totalOverallScore != 0)
				{
					dawnPercent = Math.round(((float) dawnTotalScore / (float) totalOverallScore) * 100);
					duskPercent = Math.round(((float) duskTotalScore / (float) totalOverallScore) * 100);
				}
				
				/* DUSK */
				writeD(duskStoneScoreProp); // Seal Stone Score
				writeD(duskFestivalScore); // Festival Score
				writeD(duskTotalScore); // Total Score
				
				writeC(duskPercent); // Dusk %
				
				/* DAWN */
				writeD(dawnStoneScoreProp); // Seal Stone Score
				writeD(dawnFestivalScore); // Festival Score
				writeD(dawnTotalScore); // Total Score
				
				writeC(dawnPercent); // Dawn %
				break;
			case 2:
				// c cc hc [cd (dc (S))]
				writeH(1);
				
				writeC(5); // Total number of festivals
				
				for (FestivalType level : FestivalType.VALUES)
				{
					final int festivalId = level.ordinal();
					
					writeC(festivalId + 1); // Current client-side festival ID
					writeD(level.getMaxScore());
					
					int duskScore = SevenSignsFestival.getInstance().getHighestScore(CabalType.DUSK, festivalId);
					int dawnScore = SevenSignsFestival.getInstance().getHighestScore(CabalType.DAWN, festivalId);
					
					// Dusk Score \\
					writeD(duskScore);
					
					StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(CabalType.DUSK, festivalId);
					String[] partyMembers = highScoreData.getString("members").split(",");
					
					if (partyMembers != null)
					{
						writeC(partyMembers.length);
						
						for (String partyMember : partyMembers)
							writeS(partyMember);
					}
					else
					{
						writeC(0);
					}
					
					// Dawn Score \\
					writeD(dawnScore);
					
					highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(CabalType.DAWN, festivalId);
					partyMembers = highScoreData.getString("members").split(",");
					
					if (partyMembers != null)
					{
						writeC(partyMembers.length);
						
						for (String partyMember : partyMembers)
							writeS(partyMember);
					}
					else
					{
						writeC(0);
					}
				}
				break;
			case 3:
				// c cc [ccc (cccc)]
				writeC(10); // Minimum limit for winning cabal to retain their seal
				writeC(35); // Minimum limit for winning cabal to claim a seal
				writeC(3); // Total number of seals
				
				for (Entry<SealType, CabalType> entry : SevenSigns.getInstance().getSealOwners().entrySet())
				{
					final SealType seal = entry.getKey();
					final CabalType sealOwner = entry.getValue();
					
					int dawnProportion = SevenSigns.getInstance().getSealProportion(seal, CabalType.DAWN);
					int duskProportion = SevenSigns.getInstance().getSealProportion(seal, CabalType.DUSK);
					
					writeC(seal.ordinal());
					writeC(sealOwner.ordinal());
					
					if (totalDuskMembers == 0)
					{
						if (totalDawnMembers == 0)
						{
							writeC(0);
							writeC(0);
						}
						else
						{
							writeC(0);
							writeC(Math.round(((float) dawnProportion / (float) totalDawnMembers) * 100));
						}
					}
					else
					{
						if (totalDawnMembers == 0)
						{
							writeC(Math.round(((float) duskProportion / (float) totalDuskMembers) * 100));
							writeC(0);
						}
						else
						{
							writeC(Math.round(((float) duskProportion / (float) totalDuskMembers) * 100));
							writeC(Math.round(((float) dawnProportion / (float) totalDawnMembers) * 100));
						}
					}
				}
				break;
			case 4:
				// c cc [cc (cchh)]
				writeC(winningCabal.ordinal()); // Overall predicted winner
				writeC(3); // Total number of seals
				
				for (Entry<SealType, CabalType> entry : SevenSigns.getInstance().getSealOwners().entrySet())
				{
					final SealType seal = entry.getKey();
					final CabalType sealOwner = entry.getValue();
					
					final int dawnProportion = SevenSigns.getInstance().getSealProportion(seal, CabalType.DAWN);
					final int duskProportion = SevenSigns.getInstance().getSealProportion(seal, CabalType.DUSK);
					
					dawnPercent = Math.round((dawnProportion / (totalDawnMembers == 0 ? 1 : (float) totalDawnMembers)) * 100);
					duskPercent = Math.round((duskProportion / (totalDuskMembers == 0 ? 1 : (float) totalDuskMembers)) * 100);
					
					writeC(sealOwner.ordinal());
					
					switch (sealOwner)
					{
						case NORMAL:
							switch (winningCabal)
							{
								case NORMAL:
									writeC(CabalType.NORMAL.ordinal());
									writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
									break;
								case DAWN:
									if (dawnPercent >= 35)
									{
										writeC(CabalType.DAWN.ordinal());
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else
									{
										writeC(CabalType.NORMAL.ordinal());
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
									}
									break;
								case DUSK:
									if (duskPercent >= 35)
									{
										writeC(CabalType.DUSK.ordinal());
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else
									{
										writeC(CabalType.NORMAL.ordinal());
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
									}
									break;
							}
							break;
						
						case DAWN:
							switch (winningCabal)
							{
								case NORMAL:
									if (dawnPercent >= 10)
									{
										writeC(CabalType.DAWN.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(CabalType.NORMAL.ordinal());
										writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
									}
									break;
								
								case DAWN:
									if (dawnPercent >= 10)
									{
										writeC(sealOwner.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(CabalType.NORMAL.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
								
								case DUSK:
									if (duskPercent >= 35)
									{
										writeC(CabalType.DUSK.ordinal());
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else if (dawnPercent >= 10)
									{
										writeC(CabalType.DAWN.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(CabalType.NORMAL.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
							}
							break;
						
						case DUSK:
							switch (winningCabal)
							{
								case NORMAL:
									if (duskPercent >= 10)
									{
										writeC(CabalType.DUSK.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(CabalType.NORMAL.ordinal());
										writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
									}
									break;
								
								case DAWN:
									if (dawnPercent >= 35)
									{
										writeC(CabalType.DAWN.ordinal());
										writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
									}
									else if (duskPercent >= 10)
									{
										writeC(sealOwner.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(CabalType.NORMAL.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
								
								case DUSK:
									if (duskPercent >= 10)
									{
										writeC(sealOwner.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
									}
									else
									{
										writeC(CabalType.NORMAL.ordinal());
										writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
									}
									break;
							}
							break;
					}
				}
				break;
		}
	}
}