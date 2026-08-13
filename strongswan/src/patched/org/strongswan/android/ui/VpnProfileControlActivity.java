package org.strongswan.android.ui;

import android.app.Activity;
import android.os.Bundle;

public class VpnProfileControlActivity extends Activity
{
	public static final String START_PROFILE = "org.strongswan.android.action.START_PROFILE";
	public static final String DISCONNECT = "org.strongswan.android.action.DISCONNECT";
	public static final String EXTRA_VPN_PROFILE_UUID = "org.strongswan.android.VPN_PROFILE_UUID";
	public static final String EXTRA_VPN_PROFILE_ID = "org.strongswan.android.VPN_PROFILE_ID";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		finish();
	}
}
