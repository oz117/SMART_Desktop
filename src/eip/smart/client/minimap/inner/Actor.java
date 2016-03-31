package eip.smart.client.minimap.inner;

import java.util.ArrayList;
import java.util.Random;

import javafx.scene.paint.Color;
import eip.smart.cscommons.model.geometry.Point3D;

public class Actor {

	public enum Type {
		AGENT(true, true),
		BASE(false, false),
		WAYPOINT(true, false),
		PASTPOINT(true, false);

		private boolean	show		= true;
		private boolean	showName	= true;
		private boolean	showCoords	= true;

		Type(boolean showName, boolean showCoords) {
			this.showName = showName;
			this.showCoords = showCoords;
		}

		public boolean isShow() {
			return this.show;
		}

		public boolean isShowCoords() {
			return this.showCoords;
		}

		public boolean isShowName() {
			return this.showName;
		}

		public void setShow(boolean show) {
			this.show = show;
		}

		public void setShowCoords(boolean showCoords) {
			this.showCoords = showCoords;
		}

		public void setShowName(boolean showName) {
			this.showName = showName;
		}
	}

	private static final ArrayList<Color>	COLORS			= new ArrayList<Color>() {
																{
																	this.add(Color.BLUE);
																	this.add(Color.RED);
																	this.add(Color.GREEN);
																	// this.add(Color.GOLDENROD);
																	this.add(Color.BROWN);
																	this.add(Color.MAGENTA);
																	// this.add(Color.ORANGE);
																	this.add(Color.CADETBLUE);
																	this.add(Color.INDIANRED);
																	this.add(Color.SEAGREEN);
																	this.add(Color.PERU);
																	this.add(Color.STEELBLUE);
																}
															};

	protected static final int				MAX_POS_HISTORY	= 50;

	protected Point3D						position		= new Point3D(0, 0, 0);
	private String							name			= null;
	private Color							color			= Color.BLACK;
	private Type							type			= null;
	private Actor							parent			= null;
	protected ArrayList<Actor>				children		= new ArrayList<>();

	public Actor(String name, Type type) {
		this.name = name;
		this.type = type;
		if (type == Type.AGENT) {
			Random r = new Random(name.hashCode());
			// this.color = Actor.COLORS.get(r.nextInt(Actor.COLORS.size()));
			this.color = Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256));
		}
	}

	public Actor(String name, Type type, double x, double y, double z) {
		this(name, type);
		this.position = new Point3D(x, y, z);
	}

	public void addChildren(Actor a) {
		this.children.add(a);
		a.setParent(this);
	}

	public ArrayList<Actor> getChildren() {
		return this.children;
	}

	public Color getColor() {
		if (this.getParent() != null)
			return this.getParent().getColor();
		return this.color;
	}

	public String getName() {
		return this.name;
	}

	public Actor getParent() {
		return this.parent;
	}

	public Point3D getPosition() {
		return this.position;
	}

	public ArrayList<Actor> getTree() {
		ArrayList<Actor> tree = new ArrayList<>();
		for (Actor child : this.children) {
			tree.add(child);
			tree.addAll(child.getTree());
		}
		return (tree);
	}

	public Type getType() {
		return this.type;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(Actor parent) {
		this.parent = parent;
	}

	public void setPosition(Point3D position) {
		this.position = position;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
