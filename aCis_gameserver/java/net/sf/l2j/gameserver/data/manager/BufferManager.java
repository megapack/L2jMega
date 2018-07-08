package net.sf.l2j.gameserver.data.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.data.xml.XMLDocument;
import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.holder.BuffSkillHolder;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Loads and stores available {@link BuffSkillHolder}s for the integrated scheme buffer.<br>
 * Loads and stores players' buff schemes into _schemesTable (under a String name and a List of Integer skill ids).
 */
public class BufferManager extends XMLDocument
{
	private static final String LOAD_SCHEMES = "SELECT * FROM buffer_schemes";
	private static final String DELETE_SCHEMES = "TRUNCATE TABLE buffer_schemes";
	private static final String INSERT_SCHEME = "INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)";
	
	private final Map<Integer, HashMap<String, ArrayList<Integer>>> _schemesTable = new ConcurrentHashMap<>();
	private final Map<Integer, BuffSkillHolder> _availableBuffs = new LinkedHashMap<>();
	
	protected BufferManager()
	{
		load();
	}
	
	@Override
	protected void load()
	{
		loadDocument("./data/xml/bufferSkills.xml");
		LOGGER.info("Loaded {} available buffs.", _availableBuffs.size());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_SCHEMES);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final ArrayList<Integer> schemeList = new ArrayList<>();
				
				final String[] skills = rs.getString("skills").split(",");
				for (String skill : skills)
				{
					// Don't feed the skills list if the list is empty.
					if (skill.isEmpty())
						break;
					
					final int skillId = Integer.valueOf(skill);
					
					// Integrity check to see if the skillId is available as a buff.
					if (_availableBuffs.containsKey(skillId))
						schemeList.add(skillId);
				}
				
				setScheme(rs.getInt("object_id"), rs.getString("scheme_name"), schemeList);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to load schemes data.", e);
		}
	}
	
	@Override
	protected void parseDocument(Document doc, File file)
	{
		// First element is never read.
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if (!"category".equalsIgnoreCase(o.getNodeName()))
				continue;
			
			final String category = o.getAttributes().getNamedItem("type").getNodeValue();
			
			for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (!d.getNodeName().equalsIgnoreCase("buff"))
					continue;
				
				final NamedNodeMap attrs = d.getAttributes();
				final int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				
				_availableBuffs.put(skillId, new BuffSkillHolder(skillId, Integer.parseInt(attrs.getNamedItem("price").getNodeValue()), category, attrs.getNamedItem("desc").getNodeValue()));
			}
		}
	}
	
	public void saveSchemes()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete all entries from database.
			PreparedStatement ps = con.prepareStatement(DELETE_SCHEMES);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement(INSERT_SCHEME);
			
			// Save _schemesTable content.
			for (Map.Entry<Integer, HashMap<String, ArrayList<Integer>>> player : _schemesTable.entrySet())
			{
				for (Map.Entry<String, ArrayList<Integer>> scheme : player.getValue().entrySet())
				{
					// Build a String composed of skill ids seperated by a ",".
					final StringBuilder sb = new StringBuilder();
					for (int skillId : scheme.getValue())
						StringUtil.append(sb, skillId, ",");
					
					// Delete the last "," : must be called only if there is something to delete !
					if (sb.length() > 0)
						sb.setLength(sb.length() - 1);
					
					ps.setInt(1, player.getKey());
					ps.setString(2, scheme.getKey());
					ps.setString(3, sb.toString());
					ps.addBatch();
				}
			}
			ps.executeBatch();
			ps.close();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to save schemes data.", e);
		}
	}
	
	public void setScheme(int playerId, String schemeName, ArrayList<Integer> list)
	{
		if (!_schemesTable.containsKey(playerId))
			_schemesTable.put(playerId, new HashMap<String, ArrayList<Integer>>());
		else if (_schemesTable.get(playerId).size() >= Config.BUFFER_MAX_SCHEMES)
			return;
		
		_schemesTable.get(playerId).put(schemeName, list);
	}
	
	/**
	 * @param playerId : The player objectId to check.
	 * @return the list of schemes for a given player.
	 */
	public Map<String, ArrayList<Integer>> getPlayerSchemes(int playerId)
	{
		return _schemesTable.get(playerId);
	}
	
	/**
	 * @param playerId : The player objectId to check.
	 * @param schemeName : The scheme name to check.
	 * @return the List holding skills for the given scheme name and player, or null (if scheme or player isn't registered).
	 */
	public List<Integer> getScheme(int playerId, String schemeName)
	{
		if (_schemesTable.get(playerId) == null || _schemesTable.get(playerId).get(schemeName) == null)
			return Collections.emptyList();
		
		return _schemesTable.get(playerId).get(schemeName);
	}
	
	/**
	 * @param playerId : The player objectId to check.
	 * @param schemeName : The scheme name to check.
	 * @param skillId : The skill id to check.
	 * @return true if the skill is already registered on the scheme, or false otherwise.
	 */
	public boolean getSchemeContainsSkill(int playerId, String schemeName, int skillId)
	{
		final List<Integer> skills = getScheme(playerId, schemeName);
		if (skills.isEmpty())
			return false;
		
		for (int id : skills)
		{
			if (id == skillId)
				return true;
		}
		return false;
	}
	
	/**
	 * @param groupType : The type of skills to return.
	 * @return a list of skills ids based on the given groupType.
	 */
	public List<Integer> getSkillsIdsByType(String groupType)
	{
		List<Integer> skills = new ArrayList<>();
		for (BuffSkillHolder skill : _availableBuffs.values())
		{
			if (skill.getType().equalsIgnoreCase(groupType))
				skills.add(skill.getId());
		}
		return skills;
	}
	
	/**
	 * @return a list of all buff types available.
	 */
	public List<String> getSkillTypes()
	{
		List<String> skillTypes = new ArrayList<>();
		for (BuffSkillHolder skill : _availableBuffs.values())
		{
			if (!skillTypes.contains(skill.getType()))
				skillTypes.add(skill.getType());
		}
		return skillTypes;
	}
	
	public BuffSkillHolder getAvailableBuff(int skillId)
	{
		return _availableBuffs.get(skillId);
	}
	
	public Map<Integer, BuffSkillHolder> getAvailableBuffs()
	{
		return _availableBuffs;
	}
	
	public static BufferManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BufferManager INSTANCE = new BufferManager();
	}
}