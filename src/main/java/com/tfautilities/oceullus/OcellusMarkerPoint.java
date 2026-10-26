package com.tfautilities.oceullus;

import java.awt.Color;
import javax.annotation.Nullable;
import lombok.Value;

/**
 * JSON deserialization model for a single Ocellus tile marker.
 */
@Value
public class OcellusMarkerPoint
{
	int regionId;
	int regionX;
	int regionY;
	int z;
	@Nullable Color color;
	@Nullable String label;
}
