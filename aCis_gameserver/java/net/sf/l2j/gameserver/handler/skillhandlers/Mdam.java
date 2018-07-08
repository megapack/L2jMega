package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

public class Mdam
  implements ISkillHandler
{
  private static final L2SkillType[] SKILL_IDS = { L2SkillType.MDAM, L2SkillType.DEATHLINK };
  
  @Override
public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
  {
    if (activeChar.isAlikeDead()) {
      return;
    }
    boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
    boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
    for (WorldObject obj : targets) {
      if ((obj instanceof Creature))
      {
        Creature target = (Creature)obj;
        if (((activeChar instanceof Player)) && ((target instanceof Player)) && (((Player)target).isFakeDeath())) {
          target.stopFakeDeath(true);
        } else {
          if (target.isDead()) {
            continue;
          }
        }
        boolean mcrit;

        if ((Config.OLY_ENABLE_CUSTOM_CRIT) && ((activeChar instanceof Player)) && (((Player) activeChar).isInOlympiadMode()))
        {

          if ((((Player)activeChar).getClassId().getId() == 12) || (((Player)activeChar).getClassId().getId() == 94))
          {
            mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Archmage);
          }
          else
          {

            if ((((Player)activeChar).getClassId().getId() == 13) || (((Player)activeChar).getClassId().getId() == 95))
            {
              mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Soultaker);
            }
            else
            {

              if ((((Player)activeChar).getClassId().getId() == 27) || (((Player)activeChar).getClassId().getId() == 103))
              {
                mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Mystic_Muse);
              }
              else
              {

                if ((((Player)activeChar).getClassId().getId() == 40) || (((Player)activeChar).getClassId().getId() == 110))
                {
                  mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Storm_Screamer);
                }
                else
                {
   
                  if ((((Player)activeChar).getClassId().getId() == 51) || (((Player)activeChar).getClassId().getId() == 52) || (((Player)activeChar).getClassId().getId() == 115) || (((Player)activeChar).getClassId().getId() == 116)) {
                    mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Dominator);
                  } else {
                    mcrit = Formulas.calcMCrit(Config.OLY_MAX_MCRIT_RATE);
                  }
                }
              }
            }
          }
        }
        else
        {
 
          if ((Config.ENABLE_CUSTOM_CRIT) && ((activeChar instanceof Player)))
          {
  
            if ((((Player)activeChar).getClassId().getId() == 12) || (((Player)activeChar).getClassId().getId() == 94))
            {
              mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Archmage);
            }
            else
            {

              if ((((Player)activeChar).getClassId().getId() == 13) || (((Player)activeChar).getClassId().getId() == 95))
              {
                mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Soultaker);
              }
              else
              {
 
                if ((((Player)activeChar).getClassId().getId() == 27) || (((Player)activeChar).getClassId().getId() == 103))
                {
                  mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Mystic_Muse);
                }
                else
                {

                  if ((((Player)activeChar).getClassId().getId() == 40) || (((Player)activeChar).getClassId().getId() == 110))
                  {
                    mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Storm_Screamer);
                  }
                  else
                  {

                    if ((((Player)activeChar).getClassId().getId() == 51) || (((Player)activeChar).getClassId().getId() == 52) || (((Player)activeChar).getClassId().getId() == 115) || (((Player)activeChar).getClassId().getId() == 116)) {
                      mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Dominator);
                    } else {
                      mcrit = Formulas.calcMCrit(Config.MAX_MCRIT_RATE);
                    }
                  }
                }
              }
            }
          }
          else
          {
            mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
          }
        }
        byte shld = Formulas.calcShldUse(activeChar, target, skill);
        byte reflect = Formulas.calcSkillReflect(target, skill);
        
        int damage = (int)Formulas.calcMagicDam(activeChar, target, skill, shld, sps, bsps, mcrit);
        if (damage > 0)
        {
          Formulas.calcCastBreak(target, damage);
          if ((reflect & 0x2) != 0)
          {
            activeChar.reduceCurrentHp(damage, target, skill);
          }
          else
          {
            activeChar.sendDamageMessage(target, damage, mcrit, false, false);
            target.reduceCurrentHp(damage, activeChar, skill);
          }
          if (skill.hasEffects()) {
            if ((reflect & 0x1) != 0)
            {
              activeChar.stopSkillEffects(skill.getId());
              skill.getEffects(target, activeChar);
              activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
            }
            else
            {
              target.stopSkillEffects(skill.getId());
              if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                skill.getEffects(activeChar, target, new Env(shld, sps, false, bsps));
              } else {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
              }
            }
          }
        }
      }
    }
    if (skill.hasSelfEffects())
    {
      L2Effect effect = activeChar.getFirstEffect(skill.getId());
      if ((effect != null) && (effect.isSelfEffect())) {
        effect.exit();
      }
      skill.getEffectsSelf(activeChar);
    }
    if (skill.isSuicideAttack()) {
      activeChar.doDie(null);
    }
    activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
  }
  
  @Override
public L2SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}
