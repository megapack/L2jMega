package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.location.Location;

public final class L2CharPosition
{
  public final int x;
  public final int y;
  public final int z;
  public final int heading;
  
  public L2CharPosition(int pX, int pY, int pZ, int pHeading)
  {
    this.x = pX;
    this.y = pY;
    this.z = pZ;
    this.heading = pHeading;
  }
  
  public L2CharPosition(Location loc)
  {
    this.x = loc.getX();
    this.y = loc.getY();
    this.z = loc.getZ();
    this.heading = loc.getHeading();
  }
}
