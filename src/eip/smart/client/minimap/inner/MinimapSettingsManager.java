package eip.smart.client.minimap.inner;

import java.util.HashMap;

public class MinimapSettingsManager {
	private static final Boolean		DEFAULT_SHOW_PATH_FUTURE	= true;
	private static final Boolean		DEFAULT_SHOW_PATH_PAST		= false;
	private boolean						showActors;
	private boolean						showCursorCoords;
	private boolean						showMapping;
	private boolean						showOffScreenActors;
	private boolean						showOverlay;
	private HashMap<String, Boolean>	showPathsFuture;
	private HashMap<String, Boolean>	showPathsPast;

	public MinimapSettingsManager() {
		this.showOffScreenActors = true;
		this.showOverlay = false;
		this.showCursorCoords = true;
		this.showMapping = true;
		this.showActors = true;
		this.showPathsFuture = new HashMap<>();
		this.showPathsPast = new HashMap<>();
	}

	public HashMap<String, Boolean> getShowPathFuture() {
		return this.showPathsFuture;
	}

	public HashMap<String, Boolean> getShowPathPast() {
		return this.showPathsPast;
	}

	public boolean isShowActors() {
		return this.showActors;
	}

	public boolean isShowCursorCoords() {
		return this.showCursorCoords;
	}

	public boolean isShowMapping() {
		return this.showMapping;
	}

	public boolean isShowOffScreenActors() {
		return this.showOffScreenActors;
	}

	public boolean isShowOverlay() {
		return this.showOverlay;
	}

	public boolean isShowPathFuture(String agentName) {
		return (this.showPathsFuture.containsKey(agentName) ? this.showPathsFuture.get(agentName) : MinimapSettingsManager.DEFAULT_SHOW_PATH_FUTURE);
	}

	public boolean isShowPathPast(String agentName) {
		return (this.showPathsPast.containsKey(agentName) ? this.showPathsPast.get(agentName) : MinimapSettingsManager.DEFAULT_SHOW_PATH_PAST);
	}

	public void setShowActors(boolean showActors) {
		this.showActors = showActors;
	}

	public void setShowCursorCoords(boolean showCursorCoords) {
		this.showCursorCoords = showCursorCoords;
	}

	public void setShowMapping(boolean showMapping) {
		this.showMapping = showMapping;
	}

	public void setShowOffScreenActors(boolean showOffScreenActors) {
		this.showOffScreenActors = showOffScreenActors;
	}

	public void setShowOverlay(boolean showOverlay) {
		this.showOverlay = showOverlay;
	}

	public void setShowPathFuture(String agentName, boolean show) {
		this.showPathsFuture.put(agentName, show);
	}

	public void setShowPathPast(String agentName, boolean show) {
		this.showPathsPast.put(agentName, show);
	}
}