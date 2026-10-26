package com.tfautilities.oceullus;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import com.tfautilities.TfaUtilitiesPlugin;

public class OcellusOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 32;
	private static final int FILL_OPACITY = 50;

	private final Client client;
	private final TfaUtilitiesPlugin plugin;

	@Inject
	OcellusOverlay(Client client, TfaUtilitiesPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		OcellusMarkerState state = plugin.getMarkerState();
		if (state == OcellusMarkerState.HIDDEN)
		{
			return null;
		}

		Stroke stroke = new BasicStroke(1.0f);

		if (state == OcellusMarkerState.ALL || state == OcellusMarkerState.ORANGE)
		{
			renderGroup(graphics, plugin.getOrangeMarkers(), stroke);
		}
		if (state == OcellusMarkerState.ALL || state == OcellusMarkerState.RED)
		{
			renderGroup(graphics, plugin.getRedMarkers(), stroke);
		}
		if (state == OcellusMarkerState.ALL || state == OcellusMarkerState.GREEN)
		{
			renderGroup(graphics, plugin.getGreenMarkers(), stroke);
		}

		return null;
	}

	private void renderGroup(Graphics2D graphics, ListMultimap<WorldView, OcellusColorTileMarker> markers, Stroke stroke)
	{
		for (WorldView wv : markers.keySet())
		{
			for (OcellusColorTileMarker marker : markers.get(wv))
			{
				WorldPoint worldPoint = marker.getWorldPoint();
				if (worldPoint.getPlane() != wv.getPlane() || marker.getColor() == null)
				{
					continue;
				}
				drawTile(graphics, wv, worldPoint, marker.getColor(), marker.getLabel(), stroke);
			}
		}
	}

	private void drawTile(Graphics2D graphics, WorldView wv, WorldPoint point,
		Color color, @Nullable String label, Stroke borderStroke)
	{
		if (client.getLocalPlayer().getWorldView().isTopLevel())
		{
			WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
			if (point.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
			{
				return;
			}
		}

		LocalPoint lp = LocalPoint.fromWorld(wv, point);
		if (lp == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color, new Color(0, 0, 0, FILL_OPACITY), borderStroke);
		}

		if (!Strings.isNullOrEmpty(label))
		{
			Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, label, 0);
			if (canvasTextLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, color);
			}
		}
	}
}
