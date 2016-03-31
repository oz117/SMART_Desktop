package eip.smart.client.minimap.example.simulation;

import java.util.Date;

import eip.smart.cscommons.model.agent.Agent;
import eip.smart.cscommons.model.geometry.Point3D;

public abstract class SimulatedAgent extends Agent {
	private static final double	STEP	= 1;

	public SimulatedAgent(String name, Point3D position) {
		super(name);
		this.setCurrentPosition(position);
	}

	public void newOrder(Point3D order) {
		this.orders.clear();
		this.pushOrder(order);
	}

	protected void onDestinationReached() {
		this.swapOrders();
	}

	public void pushOrder(Point3D order) {
		this.orders.add(order);
	}

	public void setCurrentPosition(Point3D position) {
		this.positions.put(new Date(), position);
	}

	public void simulate() {
		if (this.getCurrentOrder() == null)
			return;
		if (this.getCurrentPosition().equals(this.getCurrentOrder()))
			this.onDestinationReached();
		else {
			double x = Math.min(Math.max(this.getCurrentOrder().getX() - this.getCurrentPosition().getX(), -SimulatedAgent.STEP), SimulatedAgent.STEP);
			double y = Math.min(Math.max(this.getCurrentOrder().getY() - this.getCurrentPosition().getY(), -SimulatedAgent.STEP), SimulatedAgent.STEP);
			double z = Math.min(Math.max(this.getCurrentOrder().getZ() - this.getCurrentPosition().getZ(), -SimulatedAgent.STEP), SimulatedAgent.STEP);
			this.setCurrentPosition(new Point3D(this.getCurrentPosition().add(new Point3D(x, y, z))));
		}
	}

	private void swapOrders() {
		this.pushOrder(this.getCurrentOrder());
		this.orders.remove(0);
	}
}