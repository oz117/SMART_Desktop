package eip.smart.client.minimap.inner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eip.smart.cscommons.model.agent.Agent;
import eip.smart.cscommons.model.geometry.Point3D;

public class ActorAgent extends Actor {

	private Agent	agent	= null;

	public ActorAgent(String name) {
		super(name, Actor.Type.AGENT);
	}

	public ActorAgent(String name, double x, double y, double z) {
		super(name, Actor.Type.AGENT, x, y, z);
	}

	public Agent getAgent() {
		return this.agent;
	}

	public List<Point3D> getPrunedHistory() {
		List<Point3D> res = new ArrayList<>();
		List<Point3D> points = this.getAgent().getPositions();
		points = (points.size() > Actor.MAX_POS_HISTORY ? new LinkedList<>(points.subList(0, Actor.MAX_POS_HISTORY)) : points);

		double rold = 0;
		boolean first = true;
		for (int i = 0; i < points.size() - 1; i++) {
			double r = (points.get(i + 1).getX() - points.get(i).getX()) / (points.get(i + 1).getY() - points.get(i).getY());
			// System.out.println(r);
			if (r != rold && !first)
				res.add(points.get(i + 1));
			rold = r;
			first = false;
		}
		if (points.size() > 0)
			res.add(points.get(points.size() - 1));
		return (res);
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public void update(Agent agent) {
		this.agent = agent;
		if (agent.getPositions().size() > 0 && agent.getCurrentPosition() != null)
			this.position = agent.getCurrentPosition();
		this.children = new ArrayList<>();
		int n = 1;

		for (Point3D p : agent.getOrders()) {
			Actor wp = new Actor(n + "", Type.WAYPOINT, p.getX(), p.getY(), p.getZ());
			this.addChildren(wp);
			++n;
		}
		Point3D dest = agent.getCurrentDestination();
		if (dest != null) {
			Actor wp = new Actor(n + "", Type.WAYPOINT, dest.getX(), dest.getY(), dest.getZ());
			this.addChildren(wp);
		}

		n = 1;
		for (Point3D p : this.getPrunedHistory()) {
			Actor pp = new Actor(-n + "", Type.PASTPOINT, p.getX(), p.getY(), p.getZ());
			this.addChildren(pp);
			++n;
		}
	}

}
