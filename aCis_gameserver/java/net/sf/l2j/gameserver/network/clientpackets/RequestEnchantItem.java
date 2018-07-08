package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestEnchantItem
  extends L2GameClientPacket
{
  private static final int[] DONATOR_WEAPON_SCROLL = { Config.GOLD_WEAPON };
  private static final int[] DONATOR_ARMOR_SCROLL = { Config.GOLD_ARMOR };
  private static final int[] CRYSTAL_SCROLLS = { 731, 732, 949, 950, 953, 954, 957, 958, 961, 962 };
  private static final int[] NORMAL_WEAPON_SCROLLS = { 729, 947, 951, 955, 959 };
  private static final int[] BLESSED_WEAPON_SCROLLS = { 6569, 6571, 6573, 6575, 6577 };
  private static final int[] CRYSTAL_WEAPON_SCROLLS = { 731, 949, 953, 957, 961 };
  private static final int[] NORMAL_ARMOR_SCROLLS = { 730, 948, 952, 956, 960 };
  private static final int[] BLESSED_ARMOR_SCROLLS = { 6570, 6572, 6574, 6576, 6578 };
  private static final int[] CRYSTAL_ARMOR_SCROLLS = { 732, 950, 954, 958, 962 };
  private int _objectId;
  
  @Override
protected void readImpl()
  {
    this._objectId = readD();
  }
  
  @Override
protected void runImpl()
  {
    Player activeChar = getClient().getActiveChar();
    if ((activeChar == null) || (this._objectId == 0)) {
      return;
    }
    if (activeChar.getActiveTradeList() != null)
    {
      activeChar.cancelActiveTrade();
      activeChar.sendMessage("Your trade canceled");
      return;
    }
    if (activeChar.isProcessingTransaction())
    {
      activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
      activeChar.setActiveEnchantItem(null);
      return;
    }
    if (!activeChar.isOnline())
    {
      activeChar.setActiveEnchantItem(null);
      return;
    }
    if ((activeChar.isInOlympiadMode()) || (OlympiadManager.getInstance().isRegistered(activeChar)))
    {
      activeChar.sendMessage("[Olympiad]: Voce nao pode fazer isso!!");
      return;
    }
    if (!activeChar.isOnline())
    {
      activeChar.setActiveEnchantItem(null);
      return;
    }
    ItemInstance item = activeChar.getInventory().getItemByObjectId(this._objectId);
    ItemInstance scroll = activeChar.getActiveEnchantItem();
    activeChar.setActiveEnchantItem(null);
    if ((item == null) || (scroll == null))
    {
      activeChar.setActiveEnchantItem(null);
      return;
    }
    if ((!Config.ENCHANT_HERO_WEAPON) && (item.getItemId() >= 6611) && (item.getItemId() <= 6621))
    {
      activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
      activeChar.setActiveEnchantItem(null);
      return;
    }
    int itemType2 = item.getItem().getType2();
    boolean enchantItem = false;
    boolean blessedScroll = false;
    boolean crystalScroll = false;
    boolean donatorScroll = false;
    int crystalId = 0;
    switch (item.getItem().getCrystalType())
    {
    case A: 
      crystalId = 1461;
      if ((scroll.getItemId() == Config.GOLD_WEAPON) && (itemType2 == 0)) {
        enchantItem = true;
      } else if ((scroll.getItemId() == Config.GOLD_ARMOR) && ((itemType2 == 1) || (itemType2 == 2))) {
        enchantItem = true;
      }
      switch (scroll.getItemId())
      {
      case 729: 
      case 731: 
      case 6569: 
        if (itemType2 == 0) {
          enchantItem = true;
        }
        break;
      case 730: 
      case 732: 
      case 6570: 
        if ((itemType2 == 1) || (itemType2 == 2)) {
          enchantItem = true;
        }
        break;
      }
      break;
    case B: 
      crystalId = 1460;
      if ((scroll.getItemId() == Config.GOLD_WEAPON) && (itemType2 == 0)) {
        enchantItem = true;
      } else if ((scroll.getItemId() == Config.GOLD_ARMOR) && ((itemType2 == 1) || (itemType2 == 2))) {
        enchantItem = true;
      }
      switch (scroll.getItemId())
      {
      case 947: 
      case 949: 
      case 6571: 
        if (itemType2 == 0) {
          enchantItem = true;
        }
        break;
      case 948: 
      case 950: 
      case 6572: 
        if ((itemType2 == 1) || (itemType2 == 2)) {
          enchantItem = true;
        }
        break;
      }
      break;
    case C: 
      crystalId = 1459;
      if ((scroll.getItemId() == Config.GOLD_WEAPON) && (itemType2 == 0)) {
        enchantItem = true;
      } else if ((scroll.getItemId() == Config.GOLD_ARMOR) && ((itemType2 == 1) || (itemType2 == 2))) {
        enchantItem = true;
      }
      switch (scroll.getItemId())
      {
      case 951: 
      case 953: 
      case 6573: 
        if (itemType2 == 0) {
          enchantItem = true;
        }
        break;
      case 952: 
      case 954: 
      case 6574: 
        if ((itemType2 == 1) || (itemType2 == 2)) {
          enchantItem = true;
        }
        break;
      }
      break;
    case D: 
      crystalId = 1458;
      if ((scroll.getItemId() == Config.GOLD_WEAPON) && (itemType2 == 0)) {
        enchantItem = true;
      } else if ((scroll.getItemId() == Config.GOLD_ARMOR) && ((itemType2 == 1) || (itemType2 == 2))) {
        enchantItem = true;
      }
      switch (scroll.getItemId())
      {
      case 955: 
      case 957: 
      case 6575: 
        if (itemType2 == 0) {
          enchantItem = true;
        }
        break;
      case 956: 
      case 958: 
      case 6576: 
        if ((itemType2 == 1) || (itemType2 == 2)) {
          enchantItem = true;
        }
        break;
      }
      break;
    case S: 
      crystalId = 1462;
      if ((scroll.getItemId() == Config.GOLD_WEAPON) && (itemType2 == 0)) {
        enchantItem = true;
      } else if ((scroll.getItemId() == Config.GOLD_ARMOR) && ((itemType2 == 1) || (itemType2 == 2))) {
        enchantItem = true;
      }
      switch (scroll.getItemId())
      {
      case 959: 
      case 961: 
      case 6577: 
        if (itemType2 == 0) {
          enchantItem = true;
        }
        break;
      case 960: 
      case 962: 
      case 6578: 
        if ((itemType2 == 1) || (itemType2 == 2)) {
          enchantItem = true;
        }
        break;
      }
      break;
    }
    if (!enchantItem)
    {
      activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
      return;
    }
    if ((scroll.getItemId() >= 6569) && (scroll.getItemId() <= 6578)) {
      blessedScroll = true;
    } else if ((scroll.getItemId() == Config.GOLD_WEAPON) || (scroll.getItemId() == Config.GOLD_ARMOR)) {
      donatorScroll = true;
    } else {
      for (int crystalscroll : CRYSTAL_SCROLLS) {
        if (scroll.getItemId() == crystalscroll)
        {
          crystalScroll = true;
          break;
        }
      }
    }
    int chance = 0;
    int maxEnchantLevel = 0;
    int minEnchantLevel = 0;
    int nextEnchantLevel = item.getEnchantLevel() + 1;
    if (item.getItem().getType2() == 0)
    {
      if (blessedScroll) {
        for (int blessedweaponscroll : BLESSED_WEAPON_SCROLLS) {
          if (scroll.getItemId() == blessedweaponscroll)
          {
            if (item.getEnchantLevel() >= Config.BLESS_WEAPON_ENCHANT_LEVEL.size()) {
              chance = Config.BLESS_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(Config.BLESS_WEAPON_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.BLESS_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            maxEnchantLevel = Config.BLESSED_ENCHANT_WEAPON_MAX;
            break;
          }
        }
      } else if (crystalScroll) {
        for (int crystalweaponscroll : CRYSTAL_WEAPON_SCROLLS) {
          if (scroll.getItemId() == crystalweaponscroll)
          {
            if (item.getEnchantLevel() >= Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.size()) {
              chance = Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            minEnchantLevel = Config.CRYSTAL_ENCHANT_MIN;
            maxEnchantLevel = Config.CRYSTAL_ENCHANT_WEAPON_MAX;
            break;
          }
        }
      } else if (donatorScroll) {
        for (int donateweaponscroll : DONATOR_WEAPON_SCROLL) {
          if (scroll.getItemId() == donateweaponscroll)
          {
            if (item.getEnchantLevel() >= Config.DONATOR_WEAPON_ENCHANT_LEVEL.size()) {
              chance = Config.DONATOR_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(Config.DONATOR_WEAPON_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.DONATOR_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            minEnchantLevel = Config.DONATOR_ENCHANT_MIN;
            maxEnchantLevel = Config.DONATOR_ENCHANT_WEAPON_MAX;
            break;
          }
        }
      } else {
        for (int normalweaponscroll : NORMAL_WEAPON_SCROLLS) {
          if (scroll.getItemId() == normalweaponscroll)
          {
            if (item.getEnchantLevel() >= Config.NORMAL_WEAPON_ENCHANT_LEVEL.size()) {
              chance = Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(Config.NORMAL_WEAPON_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            maxEnchantLevel = Config.ENCHANT_WEAPON_MAX;
            break;
          }
        }
      }
    }
    else if (item.getItem().getType2() == 1)
    {
      if (blessedScroll) {
        for (int blessedarmorscroll : BLESSED_ARMOR_SCROLLS) {
          if (scroll.getItemId() == blessedarmorscroll)
          {
            if (item.getEnchantLevel() >= Config.BLESS_ARMOR_ENCHANT_LEVEL.size()) {
              chance = Config.BLESS_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.BLESS_ARMOR_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.BLESS_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            maxEnchantLevel = Config.BLESSED_ENCHANT_ARMOR_MAX;
            break;
          }
        }
      } else if (crystalScroll) {
        for (int crystalarmorscroll : CRYSTAL_ARMOR_SCROLLS) {
          if (scroll.getItemId() == crystalarmorscroll)
          {
            if (item.getEnchantLevel() >= Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size()) {
              chance = Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            minEnchantLevel = Config.CRYSTAL_ENCHANT_MIN;
            maxEnchantLevel = Config.CRYSTAL_ENCHANT_ARMOR_MAX;
            break;
          }
        }
      } else if (donatorScroll) {
        for (int donatorarmorscroll : DONATOR_ARMOR_SCROLL) {
          if (scroll.getItemId() == donatorarmorscroll)
          {
            if (item.getEnchantLevel() >= Config.DONATOR_ARMOR_ENCHANT_LEVEL.size()) {
              chance = Config.DONATOR_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.DONATOR_ARMOR_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.DONATOR_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            minEnchantLevel = Config.DONATOR_ENCHANT_MIN;
            maxEnchantLevel = Config.DONATOR_ENCHANT_ARMOR_MAX;
            break;
          }
        }
      } else {
        for (int normalarmorscroll : NORMAL_ARMOR_SCROLLS) {
          if (scroll.getItemId() == normalarmorscroll)
          {
            if (item.getEnchantLevel() >= Config.NORMAL_ARMOR_ENCHANT_LEVEL.size()) {
              chance = Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(Config.NORMAL_ARMOR_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            maxEnchantLevel = Config.ENCHANT_ARMOR_MAX;
            break;
          }
        }
      }
    }
    else if (item.getItem().getType2() == 2) {
      if (blessedScroll) {
        for (int blessedjewelscroll : BLESSED_ARMOR_SCROLLS) {
          if (scroll.getItemId() == blessedjewelscroll)
          {
            if (item.getEnchantLevel() >= Config.BLESS_JEWELRY_ENCHANT_LEVEL.size()) {
              chance = Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(Config.BLESS_JEWELRY_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            maxEnchantLevel = Config.BLESSED_ENCHANT_JEWELRY_MAX;
            break;
          }
        }
      } else if (crystalScroll) {
        for (int crystaljewelscroll : CRYSTAL_ARMOR_SCROLLS) {
          if (scroll.getItemId() == crystaljewelscroll)
          {
            if (item.getEnchantLevel() >= Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size()) {
              chance = Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            minEnchantLevel = Config.CRYSTAL_ENCHANT_MIN;
            maxEnchantLevel = Config.CRYSTAL_ENCHANT_JEWELRY_MAX;
            
            break;
          }
        }
      } else if (donatorScroll) {
        for (int donatorjewelscroll : DONATOR_ARMOR_SCROLL) {
          if (scroll.getItemId() == donatorjewelscroll)
          {
            if (item.getEnchantLevel() >= Config.DONATOR_JEWELRY_ENCHANT_LEVEL.size()) {
              chance = Config.DONATOR_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(Config.DONATOR_JEWELRY_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.DONATOR_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            minEnchantLevel = Config.DONATOR_ENCHANT_MIN;
            maxEnchantLevel = Config.DONATOR_ENCHANT_JEWELRY_MAX;
            break;
          }
        }
      } else {
        for (int normaljewelscroll : NORMAL_ARMOR_SCROLLS) {
          if (scroll.getItemId() == normaljewelscroll)
          {
            if (item.getEnchantLevel() >= Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size()) {
              chance = Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size())).intValue();
            } else {
              chance = Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(Integer.valueOf(item.getEnchantLevel() + 1)).intValue();
            }
            maxEnchantLevel = Config.ENCHANT_JEWELRY_MAX;
            break;
          }
        }
      }
    }
    if (((maxEnchantLevel != 0) && (item.getEnchantLevel() >= maxEnchantLevel)) || (item.getEnchantLevel() < minEnchantLevel))
    {
      activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
      return;
    }
    if (Config.SCROLL_STACKABLE) {
      scroll = activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item);
    } else {
      scroll = activeChar.getInventory().destroyItem("Enchant", scroll, activeChar, item);
    }
    if (scroll == null)
    {
      activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
      return;
    }
    if ((item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX) || ((item.getItem().getBodyPart() == 32768) && (item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL))) {
      chance = 100;
    }
    int rndValue = Rnd.get(100);
    
    Object aChance = item.fireEvent("calcEnchantChance", new Object[chance]);
    if (aChance != null) {
      chance = ((Integer)aChance).intValue();
    }
    synchronized (item)
    {
      if (rndValue < chance)
      {
        if (item.getOwnerId() != activeChar.getObjectId())
        {
          activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
          return;
        }
        if ((item.getLocation() != ItemInstance.ItemLocation.INVENTORY) && (item.getLocation() != ItemInstance.ItemLocation.PAPERDOLL))
        {
          activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
          return;
        }
        if (item.getEnchantLevel() == 0)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED);
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
          if ((Config.ENABLE_ENCHANT_ANNOUNCE) && (Config.LIST_ENCHANT_ANNOUNCE_LEVEL.contains(Integer.valueOf(nextEnchantLevel))))
          {
            Announcements.announceToPlayers(activeChar.getName() + " enchantou " + item.getItemName() + " +" + nextEnchantLevel + " ..");
            MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2025, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
          }
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED);
          sm.addNumber(item.getEnchantLevel());
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
          if ((Config.ENABLE_ENCHANT_ANNOUNCE) && (Config.LIST_ENCHANT_ANNOUNCE_LEVEL.contains(Integer.valueOf(nextEnchantLevel))))
          {
            Announcements.announceToPlayers(activeChar.getName() + " enchantou " + item.getItemName() + " +" + nextEnchantLevel + " ..");
            MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2025, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
          }
        }
        item.setEnchantLevel(item.getEnchantLevel() + 1);
        item.updateDatabase();
      }
      else
      {
        if (crystalScroll)
        {
          SystemMessage sm = SystemMessage.sendString("Failed in Crystal Enchant. The enchant value of the item become " + Config.CRYSTAL_ENCHANT_MIN);
          activeChar.sendPacket(sm);
        }
        else if (blessedScroll)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.BLESSED_ENCHANT_FAILED);
          activeChar.sendPacket(sm);
        }
        else if (donatorScroll)
        {
          if (Config.DONATOR_DECREASE_ENCHANT)
          {
            if (item.getEnchantLevel() <= Config.DONATOR_ENCHANT_MIN)
            {
              SystemMessage sm1 = SystemMessage.sendString("Failed Golden Enchant.");
              activeChar.sendPacket(sm1);
            }
            else
            {
              SystemMessage sm1 = SystemMessage.sendString("Failed Golden Enchant. His equipment had 1 reduced enchant.");
              activeChar.sendPacket(sm1);
            }
          }
          else
          {
            SystemMessage sm1 = SystemMessage.sendString("Failed Golden Enchant.");
            activeChar.sendPacket(sm1);
          }
        }
        else if (item.getEnchantLevel() > 0)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED);
          sm.addNumber(item.getEnchantLevel());
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED);
          sm.addItemName(item.getItemId());
          activeChar.sendPacket(sm);
        }
        if ((!blessedScroll) && (!crystalScroll) && (!donatorScroll))
        {
          if (item.getEnchantLevel() > 0)
          {
            SystemMessage sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
            sm.addNumber(item.getEnchantLevel());
            sm.addItemName(item.getItemId());
            activeChar.sendPacket(sm);
          }
          else
          {
        	SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISARMED);
            sm.addItemName(item.getItemId());
            activeChar.sendPacket(sm);
          }
          if (item.isEquipped())
          {
            if (item.isAugmented()) {
              item.getAugmentation().removeBonus(activeChar);
            }
            ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
            
            InventoryUpdate iu = new InventoryUpdate();
            for (ItemInstance element : unequiped) {
              iu.addModifiedItem(element);
            }
            activeChar.sendPacket(iu);
            
            activeChar.broadcastUserInfo();
          }
          int count = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
          if (count < 1) {
            count = 1;
          }
          if (item.fireEvent("enchantFail", new Object[0]) != null) {
            return;
          }
          ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
          if (destroyItem == null) {
            return;
          }
          ItemInstance crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, destroyItem);
          
          SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
          sm.addItemName(crystals.getItemId());
          sm.addNumber(count);
          activeChar.sendPacket(sm);
          
          activeChar.sendPacket(new ItemList(activeChar, true));
          
          StatusUpdate su = new StatusUpdate(activeChar);
          su.addAttribute(14, activeChar.getCurrentLoad());
          activeChar.sendPacket(su);
          
          activeChar.broadcastUserInfo();
          
          World world = World.getInstance();
          world.removeObject(destroyItem);
        }
        else if (blessedScroll)
        {
          item.setEnchantLevel(Config.BREAK_ENCHANT);
          item.updateDatabase();
        }
        else if (crystalScroll)
        {
          item.setEnchantLevel(Config.CRYSTAL_ENCHANT_MIN);
          item.updateDatabase();
        }
        else if (donatorScroll)
        {
          if (Config.DONATOR_DECREASE_ENCHANT)
          {
            if (item.getEnchantLevel() <= Config.DONATOR_ENCHANT_MIN) {
              item.setEnchantLevel(item.getEnchantLevel());
            } else {
              item.setEnchantLevel(item.getEnchantLevel() - 1);
            }
          }
          else {
            item.setEnchantLevel(item.getEnchantLevel());
          }
          item.updateDatabase();
        }
      }
    }

    
    StatusUpdate su = new StatusUpdate(activeChar);
    su.addAttribute(14, activeChar.getCurrentLoad());
    activeChar.sendPacket(su);
    su = null;
    
    activeChar.sendPacket(new EnchantResult(item.getEnchantLevel()));
    activeChar.sendPacket(new ItemList(activeChar, false));
    activeChar.broadcastUserInfo();
    activeChar.setActiveEnchantItem(null);
  }
  
  @Override
public String getType()
  {
    return "[C] 58 RequestEnchantItem";
  }
}
