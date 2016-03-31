package eip.smart.client.minimap.example.simulation;

import eip.smart.cscommons.model.geometry.Point3D;

public class SimulatedAgent2 extends SimulatedAgent {

	public SimulatedAgent2() {
		super("Agent2", new Point3D(-5, 10, 0));
		this.pushOrder(new Point3D(-30, -30, 0));
		this.pushOrder(new Point3D(-30, 30, 0));
		this.pushOrder(new Point3D(30, 30, 0));
		this.pushOrder(new Point3D(30, -30, 0));
	}
}
