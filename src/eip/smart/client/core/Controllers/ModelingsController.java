package eip.smart.client.core.Controllers;


/**
 * <p>Controller of the modeling par</p>
 *
 * <p>When the app is launched we init the modeling with the current modeling or the first modeling found
 * if there is no current one. This class is only a part of the controller {@MainWindowController}
 * </p>
 */

import eip.smart.api.SmartAPI;
import eip.smart.api.SmartAPICallback;
import eip.smart.cscommons.model.ServerStatus;
import eip.smart.cscommons.model.modeling.Modeling;
import eip.smart.cscommons.model.modeling.ModelingState;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModelingsController {
    private ArrayList<Modeling>         _modelingList = new ArrayList<>();
    private ArrayList<String>           _modelingSavedName = new ArrayList<>();
    private Modeling                    _modelingShowing = null;
    private String                      _modelingShowingName;
    private double                      _modelingShowingCompletion;
    private ObservableList<String>      _observableListAgentsNameWorking = FXCollections.observableArrayList();
    private ObservableList<String>      _observableListSavedModelings = FXCollections.observableArrayList();
    private ModelingState               _modelingShowingState;
    public StringProperty               _modelingShowingNameStringProperty = new SimpleStringProperty();
    public SimpleDoubleProperty         _modelingShowingCompletionDoubleProperty = new SimpleDoubleProperty();
    public StringProperty               _modelingShowingStateStringProperty = new SimpleStringProperty();
    public ListProperty<String>         _listAgentsNamesWorking = new SimpleListProperty<>(_observableListAgentsNameWorking);
    public ListProperty<String>         _listSavedModelings = new SimpleListProperty<>(_observableListSavedModelings);


    /**
     * Update the modeling list.
     * Searches for the modeling currently showing and updates it.
     * If there is no modeling to show it resets every property.
     * @param smartAPICallback
     */
    public void             updateModelingList(SmartAPICallback<List<Modeling>> smartAPICallback) {
        SmartAPI.modelingList().runAsync(new SmartAPICallback<List<Modeling>>() {
            @Override
            public void onError(ServerStatus serverStatus) {
                smartAPICallback.onError(serverStatus);
            }

            @Override
            public void onFail(Exception e) {
                smartAPICallback.onFail(e);
            }

            @Override
            public void onSuccess(List<Modeling> modelings) {
                _modelingList.clear();
                _modelingSavedName.clear();
                if (modelings.size() != 0) {
                    for (Modeling m : modelings) {
                        _modelingSavedName.add(m.getName());
                        _modelingList.add(m);
                        if (m.equals(_modelingShowing))
                            _modelingShowing = m;
                    }
                    updateSavedModelingList();
                    updateModelingInfo();
                }
                else
                    resetProperties();
                smartAPICallback.onSuccess(modelings);
            }
        });
    }

    /**
     * Used to execute the instructions (start, stop, delete...)
     * @param instruction
     * @param smartAPICallback
     */
    public void             executeInstruction(ModelingInstruction instruction, SmartAPICallback<ServerStatus> smartAPICallback) {
        switch (instruction) {
            case START:
                SmartAPI.modelingStart().runAsync(new SmartAPICallback<ServerStatus>() {
                    @Override
                    public void onError(ServerStatus serverStatus) {
                        smartAPICallback.onError(serverStatus);
                    }

                    @Override
                    public void onFail(Exception e) {
                        smartAPICallback.onFail(e);
                    }

                    @Override
                    public void onSuccess(ServerStatus serverStatus) {
                        smartAPICallback.onSuccess(serverStatus);
                    }
                });
                break;
            case LOAD:
                SmartAPI.modelingLoad(_modelingShowingName).runAsync(new SmartAPICallback<ServerStatus>() {
                    @Override
                    public void onError(ServerStatus serverStatus) {
                        smartAPICallback.onError(serverStatus);
                    }

                    @Override
                    public void onFail(Exception e) {
                        smartAPICallback.onFail(e);
                    }

                    @Override
                    public void onSuccess(ServerStatus serverStatus) {
                        smartAPICallback.onSuccess(serverStatus);
                    }
                });
            case STOP:
                SmartAPI.modelingStop().runAsync(new SmartAPICallback<ServerStatus>() {
                    @Override
                    public void onError(ServerStatus serverStatus) {
                        smartAPICallback.onError(serverStatus);
                    }

                    @Override
                    public void onFail(Exception e) {
                        smartAPICallback.onFail(e);
                    }

                    @Override
                    public void onSuccess(ServerStatus serverStatus) {
                        smartAPICallback.onSuccess(serverStatus);
                    }
                });
                break;
            case UNLOAD:
                SmartAPI.modelingUnload().runAsync(new SmartAPICallback<ServerStatus>() {
                    @Override
                    public void onError(ServerStatus serverStatus) {
                        smartAPICallback.onError(serverStatus);
                    }

                    @Override
                    public void onFail(Exception e) {
                        smartAPICallback.onFail(e);
                    }

                    @Override
                    public void onSuccess(ServerStatus serverStatus) {
                        smartAPICallback.onSuccess(serverStatus);
                    }
                });
                break;
            case DELETE:
                SmartAPI.modelingDelete(_modelingShowingName).runAsync(new SmartAPICallback<ServerStatus>() {
                    @Override
                    public void onError(ServerStatus serverStatus) {
                        smartAPICallback.onError(serverStatus);
                    }

                    @Override
                    public void onFail(Exception e) {
                        smartAPICallback.onFail(e);
                    }

                    @Override
                    public void onSuccess(ServerStatus serverStatus) {
                        resetProperties();
                        smartAPICallback.onSuccess(serverStatus);
                    }
                });
                break;
        }
    }

    public ModelingState    getModelingState() {
        return _modelingShowingState;
    }

    public void             setModeling(String modelingName) {
        for (Modeling m : _modelingList) {
            if (m.getName().equals(modelingName)) {
                _modelingShowing = m;
                break ;
            }
        }
    }

    /**
     * Updates the names of modelings available in the server
     */
    private void            updateSavedModelingList() {
        Platform.runLater(() -> {
            if (_modelingSavedName.size() != _observableListSavedModelings.size())
                _observableListSavedModelings.clear();
            else
                _modelingSavedName.removeAll(_observableListSavedModelings);
            _observableListSavedModelings.addAll(_modelingSavedName);
        });
    }

    /**
     * Updates the properties of the modeling currently showing
     */
    private void            updateModelingInfo() {
        if (!Objects.equals(_modelingShowingName, _modelingShowing.getName())) {
            _modelingShowingName = _modelingShowing.getName();
            Platform.runLater(() -> _modelingShowingNameStringProperty.
                    set(_modelingShowingName));
        }
        if (_modelingShowing.getCompletion() != _modelingShowingCompletion) {
            _modelingShowingCompletion = _modelingShowing.getCompletion();
            Platform.runLater(() -> _modelingShowingCompletionDoubleProperty.
                    set(_modelingShowingCompletion / 100));
        }
        if (_modelingShowing.getState() != _modelingShowingState) {
            _modelingShowingState = _modelingShowing.getState();
            Platform.runLater(() -> {
                switch (_modelingShowingState) {
                    case RUNNING:
                        _modelingShowingStateStringProperty.set("Running");
                        break;
                    case UNLOADED:
                        _modelingShowingStateStringProperty.set("Unloaded");
                        break;
                    default: {
                        _modelingShowingStateStringProperty.set("Loaded");
                    }
                }
            });
        }
    }

    /**
     * Reset all the properties of the modeling currently showing.
     * Occurs when a modeling is deleted or there is none
     */
    private void            resetProperties() {
        Platform.runLater(() -> {
            _modelingShowing = null;
            _modelingShowingCompletion = 0;
            _modelingShowingName = "";
            _observableListAgentsNameWorking.clear();
            _modelingShowingNameStringProperty.set("");
            _modelingShowingCompletionDoubleProperty.set(0.0);
            _modelingShowingStateStringProperty.set("");
            _listAgentsNamesWorking.clear();
        });
    }

    /**
     * Call in the constructor to init the first modeling
     * Looks for a loaded modeling. If there is none it takes the first one available
     */
    private void            initModeling() {
        SmartAPI.modelingList().runAsync(new SmartAPICallback<List<Modeling>>() {
            @Override
            public void onError(ServerStatus serverStatus) {
            }

            @Override
            public void onFail(Exception e) {
            }

            @Override
            public void onSuccess(List<Modeling> modelings) {
                if (modelings.size() != 0) {
                    _modelingShowing = modelings.get(0);
                    _modelingList.clear();
                    for (Modeling m : modelings) {
                        _modelingSavedName.add(m.getName());
                        _modelingList.add(m);
                        if (m.getState().equals(ModelingState.LOADED))
                            _modelingShowing = m;
                    }
                    updateModelingInfo();
                }
            }
        });
    }

    public                  ModelingsController() {
        initModeling();
    }
}
