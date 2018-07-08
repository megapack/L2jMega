package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.xml.PlayerData;
import net.sf.l2j.gameserver.model.actor.instance.Player;

public class OlympiadDamageManager
{
  private static final Logger _log = Logger.getLogger(OlympiadDamageManager.class.getName());
  private static Hashtable<Integer, Double> damage_to_mage = new Hashtable<>();
  private static Hashtable<Integer, Double> damage_to_fighter = new Hashtable<>();
  private static Hashtable<Integer, Double> damage_by_mage = new Hashtable<>();
  private static Hashtable<Integer, Double> damage_by_fighter = new Hashtable<>();
  private static Hashtable<Integer, String> id_to_name = new Hashtable<>();
  private static Hashtable<String, Integer> name_to_id = new Hashtable<>();
  
  public static void loadConfig()
  {
    String STATUS_FILE = "./config/custom/oly_dmg_settings.properties";
    InputStream is = null;
    File file = null;
    try
    {
      Properties scriptSetting = new Properties();
      file = new File(STATUS_FILE);
      is = new FileInputStream(file);
      scriptSetting.load(is);
      
      Set<Object> key_set = scriptSetting.keySet();
      for (Object key : key_set)
      {
        String key_string = (String)key;
        
        String[] class_and_type = key_string.split("__");
        
        String class_name = class_and_type[0].replace("_", " ");
        if (class_name.equals("Eva s Saint")) {
          class_name = "Eva's Saint";
        }
        String type = class_and_type[1];
        
        Integer class_id = Integer.valueOf(PlayerData.getClassIdByName(class_name) - 1);
        
        id_to_name.put(class_id, class_name);
        name_to_id.put(class_name, class_id);
        if (type.equals("ToFighter")) {
          damage_to_fighter.put(class_id, Double.valueOf(Double.parseDouble(scriptSetting.getProperty(key_string))));
        } else if (type.equals("ToMage")) {
          damage_to_mage.put(class_id, Double.valueOf(Double.parseDouble(scriptSetting.getProperty(key_string))));
        } else if (type.equals("ByFighter")) {
          damage_by_fighter.put(class_id, Double.valueOf(Double.parseDouble(scriptSetting.getProperty(key_string))));
        } else if (type.equals("ByMage")) {
          damage_by_mage.put(class_id, Double.valueOf(Double.parseDouble(scriptSetting.getProperty(key_string))));
        }
      }
      _log.info("Loaded " + id_to_name.size() + " Olympiad Damages configurations"); return;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (is != null) {
        try
        {
          is.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
  }
  
  public static double getClassDamageToMage(int id)
  {
    Double multiplier = damage_to_mage.get(Integer.valueOf(id));
    if (multiplier != null) {
      return multiplier.doubleValue();
    }
    return 1.0D;
  }
  
  public static double getClassDamageToFighter(int id)
  {
    Double multiplier = damage_to_fighter.get(Integer.valueOf(id));
    if (multiplier != null) {
      return multiplier.doubleValue();
    }
    return 1.0D;
  }
  
  public static double getClassDamageByMage(int id)
  {
    Double multiplier = damage_by_mage.get(Integer.valueOf(id));
    if (multiplier != null) {
      return multiplier.doubleValue();
    }
    return 1.0D;
  }
  
  public static double getClassDamageByFighter(int id)
  {
    Double multiplier = damage_by_fighter.get(Integer.valueOf(id));
    if (multiplier != null) {
      return multiplier.doubleValue();
    }
    return 1.0D;
  }
  
  public static int getIdByName(String name)
  {
    Integer id = name_to_id.get(name);
    if (id != null) {
      return id.intValue();
    }
    return 0;
  }
  
  public static String getNameById(int id)
  {
    String name = id_to_name.get(Integer.valueOf(id));
    if (name != null) {
      return name;
    }
    return "";
  }
  
  public static double getDamageMultiplier(Player attacker, Player attacked)
  {
    if ((attacker == null) || (attacked == null)) {
      return 1.0D;
    }
    double attackerMulti = 1.0D;
    if (attacked.isMageClass()) {
      attackerMulti = getClassDamageToMage(attacker.getClassId().getId());
    } else {
      attackerMulti = getClassDamageToFighter(attacker.getClassId().getId());
    }
    double attackedMulti = 1.0D;
    if (attacker.isMageClass()) {
      attackedMulti = getClassDamageByMage(attacked.getClassId().getId());
    } else {
      attackedMulti = getClassDamageByFighter(attacked.getClassId().getId());
    }
    double output = attackerMulti * attackedMulti;
    if (Config.ENABLE_CLASS_DAMAGES_LOGGER)
    {
      _log.info("ClassDamageManager -");
      _log.info("ClassDamageManager - Attacker: " + attacker.getName() + " Class: " + getNameById(attacker.getClassId().getId()) + " ClassId: " + attacker.getClassId().getId() + " isMage: " + attacker.isMageClass() + " mult: " + attackerMulti);
      _log.info("ClassDamageManager - Attacked: " + attacked.getName() + " Class: " + getNameById(attacked.getClassId().getId()) + " ClassId: " + attacked.getClassId().getId() + " isMage: " + attacked.isMageClass() + " mult: " + attackedMulti);
      _log.info("ClassDamageManager - FinalMultiplier: " + output);
      _log.info("ClassDamageManager -");
    }
    return output;
  }
}
