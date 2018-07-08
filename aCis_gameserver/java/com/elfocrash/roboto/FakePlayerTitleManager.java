package com.elfocrash.roboto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;

public enum FakePlayerTitleManager {
	INSTANCE;
	
	public static final Logger _log = Logger.getLogger(FakePlayerTitleManager.class.getName());
	private List<String> _fakePlayerTitles;
	
	public void initialise() {
		loadWordlist();
	}
	
	public String getRandomAvailableTitle() {
		String title = getRandomTitleFromWordlist();
		
		while(titleAlreadyExists(title)) {
			title = getRandomTitleFromWordlist();
		}
		
		return title;
	}
	
	private String getRandomTitleFromWordlist() {
		return _fakePlayerTitles.get(Rnd.get(0, _fakePlayerTitles.size() - 1));
	}
	
	public List<String> getFakePlayerTitles() {
		return _fakePlayerTitles;
	}
	
	private void loadWordlist()
    {
        try(LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File("./data/fake_title_list.txt"))));)
        {
            String line;
            ArrayList<String> playersList = new ArrayList<>();
            while((line = lnr.readLine()) != null)
            {
                if(line.trim().length() == 0 || line.startsWith("#"))
                    continue;
                playersList.add(line);
            }
            _fakePlayerTitles = playersList;
            _log.log(Level.INFO, String.format("Loaded %s fake player title.", _fakePlayerTitles.size()));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
	
	private static boolean titleAlreadyExists(String title) {
		return PlayerInfoTable.getInstance().getPlayerObjectId(title) > 0;
	}
}
