package com.tfautilities;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("tfa-utilities")
public interface TfaUtilitiesConfig extends Config
{
	@ConfigItem(
		keyName = "nextHotkey",
		name = "Next Color Hotkey",
		description = "Hotkey to cycle to the next color group"
	)
	default Keybind nextHotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "hideToggleHotkey",
		name = "Hide/Show Hotkey",
		description = "Hotkey to toggle hiding all tile markers"
	)
	default Keybind hideToggleHotkey()
	{
		return Keybind.NOT_SET;
	}
}
