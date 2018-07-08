package net.sf.l2j.gameserver.network.serverpackets;

public class AuthLoginFail extends L2GameServerPacket
{
	public enum FailReason
	{
		NO_TEXT,
		SYSTEM_ERROR_LOGIN_LATER,
		PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT,
		PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT2,
		ACCESS_FAILED_TRY_LATER,
		INCORRECT_ACCOUNT_INFO_CONTACT_CUSTOMER_SUPPORT,
		ACCESS_FAILED_TRY_LATER2,
		ACOUNT_ALREADY_IN_USE,
		ACCESS_FAILED_TRY_LATER3,
		ACCESS_FAILED_TRY_LATER4,
		ACCESS_FAILED_TRY_LATER5
	}
	
	private final FailReason _reason;
	
	public AuthLoginFail(FailReason reason)
	{
		_reason = reason;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x14);
		writeD(_reason.ordinal());
	}
}