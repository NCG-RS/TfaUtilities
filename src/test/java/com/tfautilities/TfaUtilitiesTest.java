package com.tfautilities;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TfaUtilitiesTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TfaUtilitiesPlugin.class);
		RuneLite.main(args);
	}
}