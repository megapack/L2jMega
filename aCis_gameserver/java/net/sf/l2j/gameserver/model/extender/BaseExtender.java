package net.sf.l2j.gameserver.model.extender;

import net.sf.l2j.gameserver.model.WorldObject;

public class BaseExtender
{
  protected WorldObject _owner;
  
  public static enum EventType
  {
    LOAD("load"),  STORE("store"),  CAST("cast"),  ATTACK("attack"),  CRAFT("craft"),  ENCHANT("enchant"),  SPAWN("spawn"),  DELETE("delete"),  SETOWNER("setwoner"),  DROP("drop"),  DIE("die"),  REVIVE("revive"),  SETINTENTION("setintention");
    
    public final String name;
    
    private EventType(String name)
    {
      this.name = name;
    }
  }
  
  public static boolean canCreateFor(WorldObject object)
  {
    return true;
  }
  
  private BaseExtender _next = null;
  
  public BaseExtender(WorldObject owner)
  {
    this._owner = owner;
  }
  
  public WorldObject getOwner()
  {
    return this._owner;
  }
  
  public Object onEvent(String event, Object... params)
  {
    if (this._next == null) {
      return null;
    }
    return this._next.onEvent(event, params);
  }
  
  public BaseExtender getExtender(String simpleClassName)
  {
    if (getClass().getSimpleName().compareTo(simpleClassName) == 0) {
      return this;
    }
    if (this._next != null) {
      return this._next.getExtender(simpleClassName);
    }
    return null;
  }
  
  public void removeExtender(BaseExtender ext)
  {
    if (this._next != null) {
      if (this._next == ext) {
        this._next = this._next._next;
      } else {
        this._next.removeExtender(ext);
      }
    }
  }
  
  public BaseExtender getNextExtender()
  {
    return this._next;
  }
  
  public void addExtender(BaseExtender newExtender)
  {
    if (this._next == null) {
      this._next = newExtender;
    } else {
      this._next.addExtender(newExtender);
    }
  }
}
