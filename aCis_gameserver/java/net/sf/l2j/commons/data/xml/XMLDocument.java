package net.sf.l2j.commons.data.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * An XML document, relying on a static and single {@link DocumentBuilderFactory}.
 */
public abstract class XMLDocument
{
	protected static final CLogger LOGGER = new CLogger(XMLDocument.class.getName());
	
	private static final DocumentBuilderFactory BUILDER;
	static
	{
		BUILDER = DocumentBuilderFactory.newInstance();
		BUILDER.setValidating(false);
		BUILDER.setIgnoringComments(true);
	}
	
	abstract protected void load();
	
	abstract protected void parseDocument(Document doc, File f);
	
	public void loadDocument(String filePath)
	{
		loadDocument(new File(filePath));
	}
	
	/**
	 * Parse an entire directory or file if found.
	 * @param file
	 */
	public void loadDocument(File file)
	{
		if (!file.exists())
		{
			LOGGER.error("The following file or directory doesn't exist: {}.", file.getName());
			return;
		}
		
		if (file.isDirectory())
		{
			for (File f : file.listFiles())
				loadDocument(f);
		}
		else if (file.isFile())
		{
			try
			{
				parseDocument(BUILDER.newDocumentBuilder().parse(file), file);
			}
			catch (Exception e)
			{
				LOGGER.error("Error loading XML file '{}'.", e, file.getName());
			}
		}
	}
	
	/**
	 * This method parses the content of a NamedNodeMap and feed the given StatsSet.
	 * @param attrs : The NamedNodeMap to parse.
	 * @param set : The StatsSet to feed.
	 */
	public static void parseAndFeed(NamedNodeMap attrs, StatsSet set)
	{
		for (int i = 0; i < attrs.getLength(); i++)
		{
			final Node attr = attrs.item(i);
			set.set(attr.getNodeName(), attr.getNodeValue());
		}
	}
}