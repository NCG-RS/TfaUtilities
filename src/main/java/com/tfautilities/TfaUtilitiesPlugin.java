package com.tfautilities;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.WorldEntity;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.api.events.WorldViewUnloaded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;
import com.tfautilities.oceullus.OcellusColorTileMarker;
import com.tfautilities.oceullus.OcellusConstants;
import com.tfautilities.oceullus.OcellusMarkerPoint;
import com.tfautilities.oceullus.OcellusMarkerState;
import com.tfautilities.oceullus.OcellusOverlay;
import com.tfautilities.oceullus.OcellusPanel;

@Slf4j
@PluginDescriptor(
	name = "TFA Utilities",
	description = "TFA team utilities including Ocellus P2 tile markers",
	tags = {"tfa", "ocellus", "fractured", "archive"},
	internalName = "tfa-utilities"
)
public class TfaUtilitiesPlugin extends Plugin
{
	private static final int OCELLUS_REGION = 16472;

	@Getter
	private final ListMultimap<WorldView, OcellusColorTileMarker> orangeMarkers = ArrayListMultimap.create();
	@Getter
	private final ListMultimap<WorldView, OcellusColorTileMarker> redMarkers = ArrayListMultimap.create();
	@Getter
	private final ListMultimap<WorldView, OcellusColorTileMarker> greenMarkers = ArrayListMultimap.create();

	@Getter
	private volatile OcellusMarkerState markerState = OcellusMarkerState.ALL;

	private List<OcellusMarkerPoint> orangePointDefs = Collections.emptyList();
	private List<OcellusMarkerPoint> redPointDefs = Collections.emptyList();
	private List<OcellusMarkerPoint> greenPointDefs = Collections.emptyList();

	@Inject
	private Client client;

	@Inject
	private TfaUtilitiesConfig config;

	@Inject
	private Gson gson;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private OcellusOverlay overlay;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private KeyManager keyManager;

	private OcellusPanel panel;
	private NavigationButton navButton;

	private final HotkeyListener nextHotkeyListener = new HotkeyListener(() -> config.nextHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			cycleNext();
		}
	};

	private final HotkeyListener hideHotkeyListener = new HotkeyListener(() -> config.hideToggleHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			toggleHide();
		}
	};

	@Override
	protected void startUp()
	{
		orangePointDefs = parsePoints(OcellusConstants.P2_GROUND_MARKERS_ORANGE);
		redPointDefs = parsePoints(OcellusConstants.P2_GROUND_MARKERS_RED);
		greenPointDefs = parsePoints(OcellusConstants.P2_GROUND_MARKERS_GREEN);

		overlayManager.add(overlay);

		panel = new OcellusPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("TFA Utilities - Ocellus P2")
			.icon(createNavIcon())
			.priority(5)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		keyManager.registerKeyListener(nextHotkeyListener);
		keyManager.registerKeyListener(hideHotkeyListener);

		WorldView wv = client.getTopLevelWorldView();
		if (wv != null)
		{
			loadMarkersForWorldView(wv);
			for (WorldEntity we : wv.worldEntities())
			{
				loadMarkersForWorldView(we.getWorldView());
			}
		}

		log.debug("TFA Utilities started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navButton);
		keyManager.unregisterKeyListener(nextHotkeyListener);
		keyManager.unregisterKeyListener(hideHotkeyListener);
		orangeMarkers.clear();
		redMarkers.clear();
		greenMarkers.clear();
		markerState = OcellusMarkerState.ALL;
		log.debug("TFA Utilities stopped");
	}

	@Subscribe
	public void onWorldViewLoaded(WorldViewLoaded event)
	{
		loadMarkersForWorldView(event.getWorldView());
	}

	@Subscribe
	public void onWorldViewUnloaded(WorldViewUnloaded event)
	{
		WorldView wv = event.getWorldView();
		orangeMarkers.removeAll(wv);
		redMarkers.removeAll(wv);
		greenMarkers.removeAll(wv);
	}

	private void loadMarkersForWorldView(WorldView wv)
	{
		orangeMarkers.removeAll(wv);
		redMarkers.removeAll(wv);
		greenMarkers.removeAll(wv);

		int[] regions = wv.getMapRegions();
		if (regions == null)
		{
			return;
		}

		boolean inOcellusRegion = false;
		for (int r : regions)
		{
			if (r == OCELLUS_REGION)
			{
				inOcellusRegion = true;
				break;
			}
		}

		if (!inOcellusRegion)
		{
			return;
		}

		orangeMarkers.putAll(wv, translateToMarkers(wv, orangePointDefs));
		redMarkers.putAll(wv, translateToMarkers(wv, redPointDefs));
		greenMarkers.putAll(wv, translateToMarkers(wv, greenPointDefs));
		log.debug("Loaded Ocellus P2 markers for world view");
	}

	private Collection<OcellusColorTileMarker> translateToMarkers(WorldView wv, List<OcellusMarkerPoint> points)
	{
		return points.stream()
			.map(p -> new OcellusColorTileMarker(
				WorldPoint.fromRegion(p.getRegionId(), p.getRegionX(), p.getRegionY(), p.getZ()),
				p.getColor(),
				p.getLabel()))
			.flatMap(ctm -> WorldPoint.toLocalInstance(wv, ctm.getWorldPoint()).stream()
				.map(wp -> new OcellusColorTileMarker(wp, ctm.getColor(), ctm.getLabel())))
			.collect(Collectors.toList());
	}

	private List<OcellusMarkerPoint> parsePoints(String json)
	{
		try
		{
			return gson.fromJson(json, new TypeToken<List<OcellusMarkerPoint>>()
			{
			}.getType());
		}
		catch (Exception e)
		{
			log.error("Failed to parse Ocellus marker points", e);
			return Collections.emptyList();
		}
	}

	/** Called by panel color buttons to enter a single-color mode. */
	public void selectColor(OcellusMarkerState color)
	{
		setState(color);
		logVisibleTileCounts(color);
	}

	/** Advance to the next color in the cycle (ORANGE → RED → GREEN → ORANGE). */
	public void cycleNext()
	{
		if (markerState == OcellusMarkerState.HIDDEN)
		{
			return;
		}
		setState(markerState.next());
		logVisibleTileCounts(markerState);
	}

	/** Toggle between HIDDEN and the initial ALL state. */
	public void toggleHide()
	{
		if (markerState == OcellusMarkerState.HIDDEN)
		{
			setState(OcellusMarkerState.ALL);
		}
		else
		{
			setState(OcellusMarkerState.HIDDEN);
		}
		logVisibleTileCounts(markerState);

	}

	private void setState(OcellusMarkerState newState)
	{
		markerState = newState;
		SwingUtilities.invokeLater(() -> panel.updateState(newState));
	}

	private void logVisibleTileCounts(OcellusMarkerState state)
	{
		int orange = state == OcellusMarkerState.ALL || state == OcellusMarkerState.ORANGE ? orangeMarkers.size() : 0;
		int red    = state == OcellusMarkerState.ALL || state == OcellusMarkerState.RED    ? redMarkers.size()    : 0;
		int green  = state == OcellusMarkerState.ALL || state == OcellusMarkerState.GREEN  ? greenMarkers.size()  : 0;
		log.debug("Visible tiles — Orange: {}, Red: {}, Green: {}", orange, red, green);
	}

	/** Loads and scales the Ocellus image to 17×17 for the nav button. */
	private BufferedImage createNavIcon()
	{
		BufferedImage full = ImageUtil.loadImageResource(TfaUtilitiesPlugin.class, "ocellus.png");
		return ImageUtil.resizeImage(full, 17, 17);
	}

	@Provides
	TfaUtilitiesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TfaUtilitiesConfig.class);
	}
}
