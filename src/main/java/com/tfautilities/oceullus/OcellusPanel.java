package com.tfautilities.oceullus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import com.tfautilities.TfaUtilitiesPlugin;

public class OcellusPanel extends PluginPanel
{
	private static final Color COLOR_ORANGE = new Color(255, 165, 0);
	private static final Color COLOR_RED = new Color(255, 0, 0);
	private static final Color COLOR_GREEN = new Color(0, 255, 0);
	private static final Border ACTIVE_BORDER = BorderFactory.createLineBorder(Color.WHITE, 2);
	private static final Border INACTIVE_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);

	private final TfaUtilitiesPlugin plugin;
	private final JLabel statusLabel;
	private final JButton orangeButton;
	private final JButton redButton;
	private final JButton greenButton;
	private final JButton nextButton;
	private final JButton hideButton;

	public OcellusPanel(TfaUtilitiesPlugin plugin)
	{
		this.plugin = plugin;
		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// ── Title row ────────────────────────────────────────────────────────
		JLabel titleLabel = new JLabel("Ocellus P2", SwingConstants.CENTER);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
		titleLabel.setForeground(Color.WHITE);

		statusLabel = new JLabel("All Colors", SwingConstants.CENTER);
		statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		JPanel titlePanel = new JPanel(new BorderLayout(0, 4));
		titlePanel.setOpaque(false);
		titlePanel.add(titleLabel, BorderLayout.NORTH);
		titlePanel.add(statusLabel, BorderLayout.SOUTH);
		add(titlePanel, BorderLayout.NORTH);

		// ── Color selection buttons ───────────────────────────────────────────
		JPanel colorPanel = new JPanel(new GridLayout(1, 3, 4, 0));
		colorPanel.setOpaque(false);
		colorPanel.setPreferredSize(new Dimension(0, 30));

		orangeButton = buildColorButton("Orange", COLOR_ORANGE,
			() -> plugin.selectColor(OcellusMarkerState.ORANGE));
		redButton = buildColorButton("Red", COLOR_RED,
			() -> plugin.selectColor(OcellusMarkerState.RED));
		greenButton = buildColorButton("Green", COLOR_GREEN,
			() -> plugin.selectColor(OcellusMarkerState.GREEN));

		colorPanel.add(orangeButton);
		colorPanel.add(redButton);
		colorPanel.add(greenButton);

		// ── Control buttons ───────────────────────────────────────────────────
		nextButton = new JButton("Next →");
		nextButton.setFocusPainted(false);
		nextButton.setFont(nextButton.getFont().deriveFont(Font.BOLD, 16f));
		nextButton.setPreferredSize(new Dimension(0, 55));
		nextButton.addActionListener(e -> plugin.cycleNext());

		hideButton = new JButton("Hide All");
		hideButton.setFocusPainted(false);
		hideButton.addActionListener(e -> plugin.toggleHide());

		JPanel controlPanel = new JPanel(new GridLayout(2, 1, 0, 4));
		controlPanel.setOpaque(false);
		controlPanel.add(nextButton);
		controlPanel.add(hideButton);

		JPanel bodyPanel = new JPanel(new BorderLayout(0, 8));
		bodyPanel.setOpaque(false);
		bodyPanel.add(colorPanel, BorderLayout.NORTH);
		bodyPanel.add(controlPanel, BorderLayout.SOUTH);

		add(bodyPanel, BorderLayout.CENTER);
	}

	private JButton buildColorButton(String text, Color foreground, Runnable action)
	{
		JButton btn = new JButton(text);
		btn.setForeground(foreground);
		btn.setFocusPainted(false);
		btn.setBorder(INACTIVE_BORDER);
		btn.addActionListener(e -> action.run());
		return btn;
	}

	/** Called on the EDT whenever the plugin's state changes. */
	public void updateState(OcellusMarkerState state)
	{
		orangeButton.setBorder(state == OcellusMarkerState.ORANGE ? ACTIVE_BORDER : INACTIVE_BORDER);
		redButton.setBorder(state == OcellusMarkerState.RED ? ACTIVE_BORDER : INACTIVE_BORDER);
		greenButton.setBorder(state == OcellusMarkerState.GREEN ? ACTIVE_BORDER : INACTIVE_BORDER);

		switch (state)
		{
			case ALL:
				statusLabel.setText("Select a starting color");
				hideButton.setText("Hide All");
				break;
			case ORANGE:
			case RED:
			case GREEN:
				statusLabel.setText("Press Next in next quadrant");
				hideButton.setText("Hide All");
				break;
			case HIDDEN:
				statusLabel.setText("Hidden");
				hideButton.setText("Show All");
				break;
		}
	}
}
