package eip.smart.client.minimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import org.apache.commons.math3.geometry.euclidean.twod.Segment;
import org.apache.commons.math3.ml.clustering.Cluster;

import eip.smart.client.minimap.inner.Actor;
import eip.smart.client.minimap.inner.Actor.Type;
import eip.smart.client.minimap.inner.ActorAgent;
import eip.smart.client.minimap.inner.MinimapSettingsManager;
import eip.smart.client.minimap.inner.MouseEventHandler;
import eip.smart.client.minimap.inner.MouseEventHandler.onDragEvent;
import eip.smart.cscommons.model.agent.Agent;
import eip.smart.cscommons.model.geometry.Point2D;
import eip.smart.cscommons.model.geometry.PointCloud2D;

public class Minimap extends Canvas {

	public interface onAgentClickedEvent {
		void handleAgentClicked(String agentName, MouseButton button, int clickCount);

		void handleClick(Point2D pos, MouseButton button, int clickCount);
	}

	private static final int		DRAG_CAP			= 10;
	private static final String		FORMAT_COORDS		= "(%." + Minimap.PRECISION + "f ; %." + Minimap.PRECISION + "f)";
	private static final int		OBJECT_SIZE			= 10;
	private static final int		PRECISION			= 0;

	private List<Actor>				actors				= new ArrayList<>();
	private double					cursorX				= -1;
	private double					cursorY				= -1;
	private double					eyeX				= 0;
	private double					eyeY				= 0;
	private GraphicsContext			gc;
	private List<Segment>			mapping				= new ArrayList<>();
	private MouseEventHandler		mouseEventHandler	= new MouseEventHandler();
	private onAgentClickedEvent		onClickEvent		= null;
	private String					selectedAgent		= null;
	private MinimapSettingsManager	settingsManager		= new MinimapSettingsManager();
	private String					toFollow			= null;
	private double					zoom				= 1;

	/**
	 * Build a new minimap with the specified size.
	 *
	 * @param width
	 * @param height
	 */
	public Minimap(int width, int height) {
		super(width, height);
		this.setFocusTraversable(true);
		this.handleEvents();
		this.gc = this.getGraphicsContext2D();
		this.draw();
	}

	private void draw() {
		this.gc.clearRect(0, 0, this.getWidth(), this.getHeight());
		this.gc.setStroke(Color.BLACK);
		this.gc.strokeRect(0, 0, this.getWidth(), this.getHeight());

		if (this.toFollow != null)
			this.followActor(this.findActorByName(this.toFollow));

		if (this.settingsManager.isShowMapping())
			this.drawMapping();
		if (this.settingsManager.isShowActors())
			this.drawActors();
		if (this.settingsManager.isShowOverlay())
			this.drawOverlay();
		if (this.settingsManager.isShowCursorCoords())
			this.drawCursorCoords();

	}

	private void drawActors() {
		ArrayList<Actor> actors = new ArrayList<>();
		for (Actor a : this.actors) {
			actors.add(a);
			actors.addAll(a.getTree());
		}
		this.gc.setStroke(Color.BLACK);
		for (Actor a : actors)
			if (a.getType().isShow()) {
				double x = this.getNewX(a.getPosition().getX());
				double y = this.getNewY(a.getPosition().getY());
				if (this.settingsManager.isShowOffScreenActors() || this.isVisible(a)) {
					this.gc.setFill(a.getColor());
					this.gc.setStroke(a.getColor());
					switch (a.getType()) {
					case AGENT:
						this.gc.fillOval(x - Minimap.OBJECT_SIZE / 2, y - Minimap.OBJECT_SIZE / 2, Minimap.OBJECT_SIZE, Minimap.OBJECT_SIZE);
						if (a.getName().equals(this.selectedAgent)) {
							this.gc.setStroke(Color.RED);
							this.gc.strokeOval(x - (Minimap.OBJECT_SIZE + 4) / 2, y - (Minimap.OBJECT_SIZE + 4) / 2, Minimap.OBJECT_SIZE + 4, Minimap.OBJECT_SIZE + 4);
						}
						if (this.settingsManager.isShowPathFuture(a.getName()))
							this.drawPathFuture(a);
						if (this.settingsManager.isShowPathPast(a.getName()))
							this.drawPathPast(a);
						break;
					case BASE:
						this.gc.fillRect(x - Minimap.OBJECT_SIZE * 1.5 / 2, y - Minimap.OBJECT_SIZE * 1.5 / 2, Minimap.OBJECT_SIZE * 1.5, Minimap.OBJECT_SIZE * 1.5);
						break;
					case WAYPOINT:
						if (!this.settingsManager.isShowPathFuture(a.getParent().getName()))
							continue;
						this.strokeCross(x, y, 5);
						break;
					case PASTPOINT:
						if (!this.settingsManager.isShowPathPast(a.getParent().getName()))
							continue;
						this.strokeCross(x, y, 5);
						break;
					default:
						continue;
					}
					this.gc.setStroke(Color.BLACK);
					String text = "";
					if (a.getType().isShowName())
						text += a.getName();
					if (a.getType().isShowCoords())
						text += String.format(" " + Minimap.FORMAT_COORDS, a.getPosition().getX(), a.getPosition().getY());
					this.gc.strokeText(text, x + Minimap.OBJECT_SIZE + 2, y + Minimap.OBJECT_SIZE / 1.5);
				}
			}
	}

	private void drawCursorCoords() {
		this.gc.setStroke(Color.BLACK);
		Minimap.this.gc.strokeText(String.format(Minimap.FORMAT_COORDS, Minimap.this.getOldX(this.cursorX), Minimap.this.getOldY(this.cursorY)), this.cursorX + 15, this.cursorY + 12.5);
	}

	private void drawMapping() {
		this.gc.setStroke(Color.BLACK);
		for (Segment s : this.mapping) {
			double x1 = this.getNewX(s.getStart().getX());
			double y1 = this.getNewY(s.getStart().getY());
			double x2 = this.getNewX(s.getEnd().getX());
			double y2 = this.getNewY(s.getEnd().getY());
			this.gc.strokeLine(x1, y1, x2, y2);
		}
	}

	private void drawOverlay() {
		this.gc.setStroke(Color.BLACK);
		this.gc.strokeLine(0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2);
		this.gc.strokeLine(this.getWidth() / 2, 0, this.getWidth() / 2, this.getHeight());

		this.gc.strokeText(String.format(Minimap.FORMAT_COORDS, this.eyeX / this.zoom, this.eyeY / this.zoom), this.getWidth() / 2 + 3, this.getHeight() / 2 - 5);

		this.gc.strokeText(String.format(Minimap.FORMAT_COORDS, (-this.getWidth() / 2 + this.eyeX) / this.zoom, (-this.getHeight() / 2 + this.eyeY) / this.zoom), 3, this.getHeight() - 5);
		this.gc.strokeText(String.format(Minimap.FORMAT_COORDS, (-this.getWidth() / 2 + this.eyeX) / this.zoom, (+this.getHeight() / 2 + this.eyeY) / this.zoom), 3, 12);
		String text = String.format(Minimap.FORMAT_COORDS, (this.getWidth() / 2 + this.eyeX) / this.zoom, (+this.getHeight() / 2 + this.eyeY) / this.zoom);
		double textWidth = new Text(text).getLayoutBounds().getWidth();
		this.gc.strokeText(text, this.getWidth() - textWidth - 2, 12);
		text = String.format(Minimap.FORMAT_COORDS, (this.getWidth() / 2 + this.eyeX) / this.zoom, (-this.getHeight() / 2 + this.eyeY) / this.zoom);
		textWidth = new Text(text).getLayoutBounds().getWidth();
		this.gc.strokeText(text, this.getWidth() - textWidth - 2, this.getHeight() - 5);

	}

	private void drawPathFuture(Actor a) {
		if (a.getType() != Type.AGENT)
			return;
		double x1 = this.getNewX(a.getPosition().getX());
		double y1 = this.getNewY(a.getPosition().getY());
		double x2;
		double y2;
		for (Actor wp : a.getChildren())
			if (wp.getType() == Type.WAYPOINT) {
				x2 = this.getNewX(wp.getPosition().getX());
				y2 = this.getNewY(wp.getPosition().getY());
				this.strokeDashedLineArrowHead(x1, y1, x2, y2, 10);
				x1 = x2;
				y1 = y2;
			}
	}

	private void drawPathPast(Actor a) {
		if (a.getType() != Type.AGENT)
			return;
		double x1 = this.getNewX(a.getPosition().getX());
		double y1 = this.getNewY(a.getPosition().getY());
		double x2;
		double y2;
		for (Actor wp : a.getChildren())
			if (wp.getType() == Type.PASTPOINT) {
				x2 = this.getNewX(wp.getPosition().getX());
				y2 = this.getNewY(wp.getPosition().getY());
				this.strokeDashedLineArrowHead(x2, y2, x1, y1, 10);
				x1 = x2;
				y1 = y2;
			}
	}

	private Actor findActorByName(String name) {
		for (Actor actor : this.actors)
			if (actor.getName().equals(name))
				return (actor);
		return null;
	}

	private void followActor(Actor a) {
		if (a == null)
			return;
		this.eyeX = a.getPosition().getX() * this.zoom;
		this.eyeY = a.getPosition().getY() * this.zoom;
	}

	public String getFollowed() {
		return this.toFollow;
	}

	private double getNewX(double x) {
		return (x * this.zoom + this.getWidth() / 2 - this.eyeX);
	}

	private double getNewY(double y) {
		return (-y * this.zoom + this.getHeight() / 2 + this.eyeY);
	}

	private double getOldX(double x) {
		return ((x + this.eyeX - this.getWidth() / 2) / this.zoom);
	}

	private double getOldY(double y) {
		return (-(y - this.eyeY - this.getHeight() / 2) / this.zoom);
	}

	public String getSelectedAgent() {
		return this.selectedAgent;
	}

	/**
	 * Get the settings manager to change the settings.
	 *
	 * @return
	 */
	public MinimapSettingsManager getSettingsManager() {
		return this.settingsManager;
	}

	private void handleEvents() {
		this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this.mouseEventHandler);
		this.addEventHandler(MouseEvent.MOUSE_RELEASED, this.mouseEventHandler);
		this.mouseEventHandler.setOnDragEvent((movedX, movedY) -> {
            movedX = (movedX > Minimap.DRAG_CAP ? Minimap.DRAG_CAP : (movedX < -Minimap.DRAG_CAP ? -Minimap.DRAG_CAP : movedX));
            movedY = (movedY > Minimap.DRAG_CAP ? Minimap.DRAG_CAP : (movedY < -Minimap.DRAG_CAP ? -Minimap.DRAG_CAP : movedY));

            Minimap.this.eyeX += movedX;
            Minimap.this.eyeY -= movedY;
            Minimap.this.toFollow = null;
            Minimap.this.draw();
        });
		this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            for (Actor a : Minimap.this.actors)
                if (a.getType() == Type.AGENT) {
                    double x = Minimap.this.getNewX(a.getPosition().getX());
                    double y = Minimap.this.getNewY(a.getPosition().getY());
                    if (event.getX() > x - Minimap.OBJECT_SIZE * 2 && event.getX() < x + Minimap.OBJECT_SIZE * 2 && event.getY() > y - Minimap.OBJECT_SIZE * 2 && event.getY() < y + Minimap.OBJECT_SIZE * 2) {
                        if (Minimap.this.onClickEvent != null)
                            Minimap.this.onClickEvent.handleAgentClicked(a.getName(), event.getButton(), event.getClickCount());
                        return;
                    }
                }
            if (Minimap.this.onClickEvent != null)
                Minimap.this.onClickEvent.handleClick(new Point2D((int) Minimap.this.getOldX(Minimap.this.cursorX), (int) Minimap.this.getOldY(Minimap.this.cursorY)), event.getButton(), event.getClickCount());
            Minimap.this.draw();
        });
		EventHandler<MouseEvent> cursorHandler = event -> {
            Minimap.this.cursorX = event.getX();
            Minimap.this.cursorY = event.getY();
            Minimap.this.draw();
        };
		this.addEventHandler(MouseEvent.MOUSE_MOVED, cursorHandler);
		this.addEventHandler(MouseEvent.MOUSE_DRAGGED, cursorHandler);
		this.addEventHandler(ScrollEvent.SCROLL, event -> {
            double oldX = Minimap.this.getOldX(event.getX());
            double oldY = Minimap.this.getOldY(event.getY());

            if (event.getDeltaY() > 0)
                Minimap.this.zoom *= 1.25;
            else if (event.getDeltaY() < 0)
                Minimap.this.zoom /= 1.25;

            double newX = Minimap.this.getNewX(oldX);
            double newY = Minimap.this.getNewY(oldY);

            double offsetX = newX - event.getX();
            double offsetY = newY - event.getY();

            if (Minimap.this.toFollow == null) {
                Minimap.this.eyeX += offsetX;
                Minimap.this.eyeY -= offsetY;
            }
            Minimap.this.draw();
        });
	}

	private void initializeActors() {
		this.actors.add(new Actor("Base", Type.BASE, 0, 0, 0));
	}

	private boolean isVisible(Actor a) {
		double x = this.getNewX(a.getPosition().getX());
		double y = this.getNewY(a.getPosition().getY());
		return ((x + Minimap.OBJECT_SIZE >= 0 && x <= this.getWidth() && y + Minimap.OBJECT_SIZE >= 0 && y <= this.getHeight()));
	}

	/**
	 * Reset the view on the center of the map (the base) and reset the zoom factor.
	 */
	public void resetView() {
		this.zoom = 1;
		if (this.toFollow != null) {
			this.eyeX = 0;
			this.eyeY = 0;
		}
	}

	/**
	 * Center the view on an actor.
	 * If the actor doesn't exist or the parameter entered is null, it will stop following.
	 *
	 * @param name
	 */
	public void setFollowedActor(String name) {
		if (this.findActorByName(name) == null)
			name = null;
		this.toFollow = name;
	}

	/**
	 * Register an event handler for a click on an agent.
	 *
	 * @param onAgentClickedEvent
	 */
	public void setOnAgentClickedEvent(onAgentClickedEvent onAgentClickedEvent) {
		this.onClickEvent = onAgentClickedEvent;
	}

	public void setSelectedAgent(String name) {
		this.selectedAgent = name;
	}

	private void strokeCross(double x, double y, double size) {
		this.gc.strokeLine(x - size, y - size, x + size, y + size);
		this.gc.strokeLine(x + size, y - size, x - size, y + size);
	}

	private void strokeDashedLineArrowHead(double x1, double y1, double x2, double y2, double dashSize) {
		int dashNb = (int) Math.floor(new Point2D(x1, y1).distance(new Point2D(x2, y2)) / dashSize);
		double lineX = x1;
		double lineY = y1;
		double lineXOffset = (x2 - x1) / dashNb;
		double lineYOffset = (y2 - y1) / dashNb;
		for (int i = 0; i < dashNb; i++) {
			if (i % 2 != 0)
				this.gc.strokeLine(lineX, lineY, lineX + lineXOffset, lineY + lineYOffset);
			lineX += lineXOffset;
			lineY += lineYOffset;
		}
		if (x1 != x2 || y1 != y2) {
			double headlen = 3 * this.zoom; // length of head in pixels
			double angle = Math.atan2(y1 - y2, x1 - x2);
			this.gc.strokeLine(x2, y2, x2 + headlen * Math.cos(angle - Math.PI / 8), y2 + headlen * Math.sin(angle - Math.PI / 8));
			this.gc.strokeLine(x2, y2, x2 + headlen * Math.cos(angle + Math.PI / 8), y2 + headlen * Math.sin(angle + Math.PI / 8));
		}
	}

	/**
	 * Update the list of Agents.
	 *
	 * @param agents
	 */
	public void updateAgents(List<Agent> agents) {
		this.actors.clear();
		this.initializeActors();
		for (Agent agent : agents) {
			ActorAgent actor = new ActorAgent(agent.getName());
			actor.update(agent);
			this.actors.add(actor);
		}
		Platform.runLater(Minimap.this::draw);
	}

	/**
	 * Update the point cloud to render.
	 *
	 * @param pcl2d
	 */
	public void updatePointCloud(PointCloud2D pcl2d) {
		List<Cluster<Point2D>> clusters = pcl2d.findClusters();
		this.mapping.clear();
		for (Cluster<Point2D> cluster : clusters) {
			PointCloud2D pcl = new PointCloud2D();
			pcl.add(cluster.getPoints());
			Collections.addAll(this.mapping, pcl.generateConvexHull2D().getLineSegments());
		}
		Platform.runLater(Minimap.this::draw);
	}

}
