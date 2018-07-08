package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.item.type.ActionType;
import net.sf.l2j.gameserver.model.item.type.ArmorType;
import net.sf.l2j.gameserver.model.item.type.CrystalType;
import net.sf.l2j.gameserver.model.item.type.EtcItemType;
import net.sf.l2j.gameserver.model.item.type.WeaponType;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

public final class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean _ctrlPressed;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE);
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
			return;
		}
		
		final ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if (item == null)
			return;
		
		if (item.getItem().getType2() == Item.TYPE2_QUEST)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			return;
		}
		
		if (activeChar.isAlikeDead() || activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAfraid())
			return;
		
	    if ((!Config.OLLY_GRADE_A) && (item.getItem().getCrystalType() == CrystalType.S) && ((activeChar.isInOlympiadMode()) || (OlympiadManager.getInstance().isRegistered(activeChar))))
	    {
	      activeChar.sendMessage("[Olympiad]: Items Grade S cannot be used in Olympiad Event");
	      return;
	    }
		
		if (!Config.KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0)
		{
			final IntIntHolder[] sHolders = item.getItem().getSkills();
			if (sHolders != null)
			{
				for (IntIntHolder sHolder : sHolders)
				{
					final L2Skill skill = sHolder.getSkill();
					if (skill != null && (skill.getSkillType() == L2SkillType.TELEPORT || skill.getSkillType() == L2SkillType.RECALL))
						return;
				}
			}
		}

	    			 if (!activeChar.isGM() && item.getEnchantLevel() > Config.MAX_ITEM_ENCHANT_KICK)
	    				 {			
	    				        activeChar.getInventory().destroyItem(null, item, activeChar, null);
	    						activeChar.sendMessage("You have been kicked for using an item overenchanted!");
	    						EnterWorld.overEnchant(activeChar);
	    		                LOGGER.info("#### ATTENTION ####");
	    		                LOGGER.info(item + " item has been removed from " + activeChar);
	    						return;
	    				 }
		
		if (activeChar.isGM()) activeChar.sendPacket(new CreatureSay(0, Say2.SHOUT, "", "{ID}  " + item.getItemId() + "   {NAME}  " +  
			  item.getItemName()));
		
		if (activeChar.isFishing() && item.getItem().getDefaultAction() != ActionType.fishingshot)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		/*
		 * The player can't use pet items if no pet is currently summoned. If a pet is summoned and player uses the item directly, it will be used by the pet.
		 */
		if (item.isPetItem())
		{
			// If no pet, cancels the use
			if (!activeChar.hasPet())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_PET_ITEM);
				return;
			}
			
			final Pet pet = ((Pet) activeChar.getPet());
			
			if (!pet.canWear(item.getItem()))
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}
			
			if (pet.isDead())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
				return;
			}
			
			if (!pet.getInventory().validateCapacity(item))
			{
				activeChar.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}
			
			if (!pet.getInventory().validateWeight(item, 1))
			{
				activeChar.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
				return;
			}
			
			activeChar.transferItem("Transfer", _objectId, 1, pet.getInventory(), pet);
			
			// Equip it, removing first the previous item.
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(item));
			}
			else
			{
				pet.getInventory().equipPetItem(item);
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PUT_ON_S1).addItemName(item));
			}
			
			activeChar.sendPacket(new PetItemList(pet));
			pet.updateAndBroadcastStatus(1);
			return;
		}
		
    	if (Config.ALT_DISABLE_ITEM_FOR_CLASSES)
    	{
   
    	    if ((Config.ITEM_LIST.contains(Integer.valueOf(item.getItemId()))))
    		{
    			if(Config.DISABLE_ITEM_CLASSES.contains(activeChar.getClassId().getId())){
    				activeChar.sendMessage("This item can not be equipped by your class");
    				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
    				return;
    			}
    		}
    	}
		
	    if ((Config.NOTALLOWEDUSELIGHT.contains(Integer.valueOf(activeChar.getClassId().getId()))) && (!activeChar.isGM()) && (!activeChar.isInOlympiadMode()))
	    {
	      if (item.getItemType() == ArmorType.LIGHT)
	      {
	        activeChar.sendMessage("this class can not use set light!");
	        activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	      }
	    }
	    else if ((Config.NOTALLOWEDUSEHEAVY.contains(Integer.valueOf(activeChar.getClassId().getId()))) && (!activeChar.isGM()) && (!activeChar.isInOlympiadMode())) {
	      if (item.getItemType() == ArmorType.HEAVY)
	      {
	        activeChar.sendMessage("this class can not use set heavy!");
	        activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	        return;
	      }
	    }
		
    	if (Config.ALT_DISABLE_BOW_CLASSES)
    	{
		if ((item.getItem() instanceof Weapon) && (((Weapon) item.getItem()).getItemType() == WeaponType.BOW))
    		{
    			if(Config.DISABLE_BOW_CLASSES.contains(activeChar.getClassId().getId())){
    				activeChar.sendMessage("This item can not be equipped by your class");
    				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
    				return;
    			}
    		}
    	}
    	   	
        if ((Config.FIGHTER_LISTID_RESTRICT.contains(Integer.valueOf(item.getItemId()))) && (activeChar.isFightClass()))
        {
          activeChar.sendMessage("Only fighter can equip this item.");
          activeChar.sendPacket(ActionFailed.STATIC_PACKET);
          return;
        }
        if ((Config.MAGE_LISTID_RESTRICT.contains(Integer.valueOf(item.getItemId()))) && (!activeChar.isMageClass()))
        {
          activeChar.sendMessage("Only Mage can equip this item.");
          activeChar.sendPacket(ActionFailed.STATIC_PACKET);
          return;
        }
		
		if (!item.isEquipped())
		{
			if (!item.getItem().checkCondition(activeChar, activeChar, true))
				return;
		}
		
		if (item.isEquipable())
		{
			if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC);
				return;
			}
			
			switch (item.getItem().getBodyPart())
			{
				case Item.SLOT_LR_HAND:
				case Item.SLOT_L_HAND:
				case Item.SLOT_R_HAND:
				{
					if (activeChar.isMounted())
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					
					// Don't allow weapon/shield equipment if a cursed weapon is equipped
					if (activeChar.isCursedWeaponEquipped())
						return;
					
					break;
				}
			}
			
			if (activeChar.isCursedWeaponEquipped() && item.getItemId() == 6408) // Don't allow to put formal wear
				return;
			
			if (activeChar.isAttackingNow())
				ThreadPool.schedule(() -> activeChar.useEquippableItem(item, false), activeChar.getAttackEndTime() - System.currentTimeMillis());
			else
				activeChar.useEquippableItem(item, true);
		}
		else
		{
			if (activeChar.isCastingNow() && !(item.isPotion() || item.isElixir()))
				return;
			
			if (activeChar.getAttackType() == WeaponType.FISHINGROD && item.getItem().getItemType() == EtcItemType.LURE)
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				
				sendPacket(new ItemList(activeChar, false));
				return;
			}
			
			final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if (handler != null)
				handler.useItem(activeChar, item, _ctrlPressed);
			
			for (Quest quest : item.getQuestEvents())
			{
				QuestState state = activeChar.getQuestState(quest.getName());
				if (state == null || !state.isStarted())
					continue;
				
				quest.notifyItemUse(item, activeChar, activeChar.getTarget());
			}
		}
	}
}