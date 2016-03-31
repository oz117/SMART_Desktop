package eip.smart.client.minimap.example.simulation;

import eip.smart.cscommons.model.geometry.Point3D;

public class SimulatedAgentMoveable extends SimulatedAgent {

	public SimulatedAgentMoveable() {
		super("AgentMoveable", new Point3D(0, 0, 0));
	}

	@Override
	protected void onDestinationReached() {
		this.orders.remove(0);
	}
}
