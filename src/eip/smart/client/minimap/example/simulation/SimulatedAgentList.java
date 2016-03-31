package eip.smart.client.minimap.example.simulation;

import java.util.ArrayList;

import eip.smart.cscommons.model.agent.Agent;

public class SimulatedAgentList {
	public ArrayList<SimulatedAgent>	agents	= new ArrayList<>();

	public SimulatedAgentList() {
		//this.agents.add(new SimulatedAgent1());
		//this.agents.add(new SimulatedAgent2());
		//this.agents.add(new SimulatedAgentRandom());
		this.agents.add(new SimulatedAgentMoveable());
	}

	public SimulatedAgent getAgent(String name) {
		for (SimulatedAgent simulatedAgent : this.agents)
			if (simulatedAgent.getName().equals(name))
				return (simulatedAgent);
		return null;
	}

	public ArrayList<Agent> getAgents() {
		ArrayList<Agent> res = new ArrayList<>();
		for (SimulatedAgent agent : this.agents)
			res.add(agent);
		return res;
	}

	public void simulate() {
		for (SimulatedAgent agent : this.agents)
			agent.simulate();
	}
}
