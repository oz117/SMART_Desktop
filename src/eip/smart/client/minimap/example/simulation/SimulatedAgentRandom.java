package eip.smart.client.minimap.example.simulation;

import java.util.Random;

import eip.smart.cscommons.model.geometry.Point3D;

public class SimulatedAgentRandom extends SimulatedAgent {

	private static Point3D getRandomPoint() {
		Random r = new Random();
		return new Point3D(r.nextInt(60) - 30, r.nextInt(60) - 30, 0);
	}

	public SimulatedAgentRandom() {
		super("AgentRandom", new Point3D(0, 0, 0));
		this.pushOrder(SimulatedAgentRandom.getRandomPoint());
		this.pushOrder(SimulatedAgentRandom.getRandomPoint());
		this.pushOrder(SimulatedAgentRandom.getRandomPoint());
		this.pushOrder(SimulatedAgentRandom.getRandomPoint());
		this.pushOrder(SimulatedAgentRandom.getRandomPoint());
	}

	@Override
	protected void onDestinationReached() {
		this.pushOrder(SimulatedAgentRandom.getRandomPoint());
		this.orders.remove(0);
	}
}
