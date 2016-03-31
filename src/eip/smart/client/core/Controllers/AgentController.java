package eip.smart.client.core.Controllers;

import eip.smart.api.SmartAPI;
import eip.smart.api.SmartAPICallback;
import eip.smart.cscommons.model.ServerStatus;
import eip.smart.cscommons.model.agent.Agent;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class AgentController {
    private ObservableList<Agent>       _agents = FXCollections.observableArrayList();
    private ObservableList<String>      _agentsNames = FXCollections.observableArrayList();
    public ListProperty<Agent>          _listAgentsProperty = new SimpleListProperty<>(_agents);
    public ListProperty<String>         _listAgentsNames = new SimpleListProperty<>(_agentsNames);

    public void getAgentsFull(SmartAPICallback<List<Agent>> callback) {
        SmartAPI.getAgentsAvailable().runAsync(new SmartAPICallback<List<Agent>>() {
            @Override
            public void onError(ServerStatus s) {
                callback.onError(s);
            }

            @Override
            public void onFail(Exception e) {
                callback.onFail(e);
            }

            @Override
            public void onSuccess(List<Agent> t)
            {
                Platform.runLater(() ->
                {
                    if (_agents.size() == 0)
                    {
                        for (Agent a : t)
                        {
                            _agentsNames.add(a.getName());
                            _agents.add(a);
                        }
                    } else
                    {
                        if (t.size() < _agents.size())
                        {
                            _agents.clear();
                            _agents.addAll(t);
                        }
                        else
                        {
                            t.removeAll(_agents);
                            if (t.size() != 0)
                            {
                                _agents.addAll(t);
                                _agentsNames.addAll(t.stream().map(Agent::getName).collect(Collectors.toList()));
                            }
                        }
                    }
                    callback.onSuccess(_agents);
                });
            }
        });
    }

    public void getAgentsInfo(Agent agent, SmartAPICallback<Agent> smartAPICallback) {
        SmartAPI.getAgentInfo(agent.getName()).runAsync(new SmartAPICallback<Agent>() {
            @Override
            public void onError(ServerStatus serverStatus) {
                smartAPICallback.onError(serverStatus);
            }

            @Override
            public void onFail(Exception e) {
                smartAPICallback.onFail(e);
            }

            @Override
            public void onSuccess(Agent agent) {
                smartAPICallback.onSuccess(agent);
            }
        });
    }

    public AgentController() {
    }
}
