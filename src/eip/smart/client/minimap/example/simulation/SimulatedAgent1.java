package eip.smart.client.minimap.example.simulation;

import eip.smart.cscommons.model.geometry.Point3D;

public class SimulatedAgent1 extends SimulatedAgent {

	public SimulatedAgent1() {
		super("Agent1", new Point3D(10, -10, 0));
		this.pushOrder(new Point3D(0, 0, 0));
		this.pushOrder(new Point3D(20, 20, 0));
	}

}
