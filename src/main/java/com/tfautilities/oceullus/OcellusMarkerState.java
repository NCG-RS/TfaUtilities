package com.tfautilities.oceullus;

public enum OcellusMarkerState
{
	ALL, ORANGE, RED, GREEN, HIDDEN;

	private static final OcellusMarkerState[] CYCLE = {ORANGE, RED, GREEN};

	/**
	 * Returns the next color in the cycle.
	 * ALL and HIDDEN both start the cycle at ORANGE.
	 */
	public OcellusMarkerState next()
	{
		for (int i = 0; i < CYCLE.length; i++)
		{
			if (CYCLE[i] == this)
			{
				return CYCLE[(i + 1) % CYCLE.length];
			}
		}
		return ORANGE;
	}
}
