package org.strongswan.android.ui;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity
{
	public static final String CONTACT_EMAIL = "";
	public static final String EXTRA_CRL_LIST = "crl_list";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		finish();
	}
}
