package com.tfautilities.oceullus;

import java.awt.Color;
import javax.annotation.Nullable;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;

/**
 * Runtime render model — not serialized.
 */
@Value
public class OcellusColorTileMarker
{
	WorldPoint worldPoint;
	@Nullable Color color;
	@Nullable String label;
}
