package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.GetOnVehicle;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;

public class ValidatePosition extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _data;
	
	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
		_data = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null || player.isTeleporting() || player.isInObserverMode())
			return;
		
		final int realX = player.getX();
		final int realY = player.getY();
		int realZ = player.getZ();
		
		if (_x == 0 && _y == 0)
		{
			if (realX != 0) // in this case this seems like a client error
				return;
		}
		
		int dx, dy, dz;
		double diffSq;
		
		if (player.isInBoat())
		{
			if (Config.COORD_SYNCHRONIZE == 2)
			{
				dx = _x - player.getBoatPosition().getX();
				dy = _y - player.getBoatPosition().getY();
				dz = _z - player.getBoatPosition().getZ();
				diffSq = (dx * dx + dy * dy);
				if (diffSq > 250000)
					sendPacket(new GetOnVehicle(player.getObjectId(), _data, player.getBoatPosition()));
			}
			return;
		}
		
		if (player.isFalling(_z))
			return; // disable validations during fall to avoid "jumping"
			
		dx = _x - realX;
		dy = _y - realY;
		dz = _z - realZ;
		diffSq = (dx * dx + dy * dy);
		
		if (player.isFlying() || player.isInsideZone(ZoneId.WATER))
		{
			player.setXYZ(realX, realY, _z);
			if (diffSq > 90000) // validate packet, may also cause z bounce if close to land
				player.sendPacket(new ValidateLocation(player));
		}
		else if (diffSq < 360000) // if too large, messes observation
		{
			if (Config.COORD_SYNCHRONIZE == -1) // Only Z coordinate synched to server,
			// mainly used when no geodata but can be used also with geodata
			{
				player.setXYZ(realX, realY, _z);
				return;
			}
			if (Config.COORD_SYNCHRONIZE == 1) // Trusting also client x,y coordinates (should not be used with geodata)
			{
				// Heading changed on client = possible obstacle
				if (!player.isMoving() || !player.validateMovementHeading(_heading))
				{
					// character is not moving, take coordinates from client
					if (diffSq < 2500) // 50*50 - attack won't work fluently if even small differences are corrected
						player.setXYZ(realX, realY, _z);
					else
						player.setXYZ(_x, _y, _z);
				}
				else
					player.setXYZ(realX, realY, _z);
				
				player.setHeading(_heading);
				return;
			}
			// Sync 2 (or other), intended for geodata. Sends a validation packet to client when too far from server calculated real coordinate.
			// Due to geodata/zone errors, some Z axis checks are made. (maybe a temporary solution)
			// Important: this code part must work together with Creature.updatePosition
			if (diffSq > 250000 || Math.abs(dz) > 200)
			{
				if (Math.abs(dz) > 200 && Math.abs(dz) < 1500 && Math.abs(_z - player.getClientZ()) < 800)
				{
					player.setXYZ(realX, realY, _z);
					realZ = _z;
				}
				else
					player.sendPacket(new ValidateLocation(player));
			}
		}
		
		player.setClientX(_x);
		player.setClientY(_y);
		player.setClientZ(_z);
		player.setClientHeading(_heading); // No real need to validate heading.
	}
}