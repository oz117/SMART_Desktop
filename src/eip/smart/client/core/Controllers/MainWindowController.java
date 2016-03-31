package eip.smart.client.core.Controllers;

import eip.smart.api.SmartAPI;
import eip.smart.api.SmartAPICallback;
import eip.smart.client.core.View.AgentCell;
import eip.smart.client.core.Main.Main;
import eip.smart.client.core.View.NavigationClass;
import eip.smart.client.core.View.ShowDialogAlert;
import eip.smart.client.minimap.example.CreateMinimap;
import eip.smart.cscommons.model.ServerStatus;
import eip.smart.cscommons.model.agent.Agent;
import eip.smart.cscommons.model.modeling.Modeling;
import eip.smart.cscommons.model.modeling.ModelingState;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class                MainWindowController implements Initializable {
    /*
    Block start: Misc
     */

    @FXML
    private MenuItem                        MainWindowMinimapMenuItem;

    /*
    Block end: Misc
     */

    @FXML
    private ListView<String>                MainWindowNavigationListView;
    @FXML
    private AnchorPane                      MainWindowHomeAnchorPane;
    @FXML
    private AnchorPane                      MainWindowAgentsAnchorPane;
    @FXML
    private AnchorPane                      MainWindowModelingsAnchorPane;
    @FXML
    private Label                           MainWindowHomeAgentsInfoLabel;
    @FXML
    private GridPane                        MainWindowHomeAgentsGridPane;
    @FXML
    private GridPane                        MainWindowHomeModelingsGridPane;
    @FXML
    private Label                           MainWindowHomeModelingsInfoLabel;

    @FXML
    private ListView<Agent>                 MainWindowAgentsListView;
    @FXML
    private ImageView                       MainWindowAgentTypeImageView;
    @FXML
    private Label                           MainWindowAgentBatteryLabel;
    @FXML
    private Label                           MainWindowAgentPositionLabel;
    @FXML
    private Label                           MainWindowAgentStateLabel;

    /*
    Block start: modeling part
     */

    @FXML
    private GridPane                        MainWindowModelingSettingsGridPane;
    @FXML
    private VBox                            MainWindowModelingVbox;
    @FXML
    private Label                           MainWindowCurrentModelingNameLabel;
    @FXML
    private Label                           MainWindowCurrentModelingStateLabel;
    @FXML
    private TextField                       MainWindowModelingCreateTextField;
    @FXML
    private ProgressIndicator               MainWindowModelingCreateProgressIndicator;
    @FXML
    private ComboBox<String>                MainWindowModelingLoadComboBox;
    @FXML
    private ListView<String>                MainWindowAgentsAvailableModelingListView;
    @FXML
    private ListView<String>                MainWindowAgentsWorkingModelingListView;
    @FXML
    private Button                          MainWindowStartModelingButton;
    @FXML
    private Button                          MainWindowLoadModelingButton;
    @FXML
    private Button                          MainWindowUnloadModelingButton;
    @FXML
    private Button                          MainWindowStopModelingButton;
    @FXML
    private Button                          MainWindowDeleteModelingButton;

    /*
    Block end: modeling part
     */

    private Image                           _terrestrialAgent = new Image("eip/smart/client/icons/Without_Description/terrestrial_drone_without_credits.png");
    private Image                           _flyingAgent = new Image("eip/smart/client/icons/Without_Description/drone_without_credit.png");
    private Agent                           _agentSelected = null;
    private AgentController                 _agentController = new AgentController();
    private ModelingsController             _modelingsController = new ModelingsController();
    private List<Node>                      _navigationMenu = new ArrayList<>();
    private SimpleListProperty<String>      _nav = new SimpleListProperty<>(FXCollections.observableArrayList("DashBoard", "Agents", "Modelings"));
    private Main                            _app;
    private ShowDialogAlert                 _alertDialog = ShowDialogAlert.getInstance();
    private static CreateMinimap                   _minimap;
    private static ScheduledExecutorService _exec = Executors.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder().namingPattern("SMARTDashboard-thread-%d").build());
    private static ScheduledFuture          _scheduledFuture;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        MainWindowMinimapMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
        _navigationMenu.addAll(Arrays.asList(MainWindowHomeAnchorPane, MainWindowAgentsAnchorPane, MainWindowModelingsAnchorPane));
        MainWindowNavigationListView.setCellFactory(param -> new NavigationClass());
        MainWindowNavigationListView.itemsProperty().set(_nav);
        MainWindowNavigationListView.setOnMouseClicked(event -> changeScreen(MainWindowNavigationListView.getSelectionModel().getSelectedIndex()));

        MainWindowAgentsListView.setCellFactory(param -> new AgentCell());
        MainWindowAgentsListView.setOnMouseClicked(event -> changeAgentSelected(MainWindowAgentsListView.getSelectionModel().getSelectedItem()));
        MainWindowAgentsListView.itemsProperty().bind(_agentController._listAgentsProperty);
        MainWindowAgentsAvailableModelingListView.itemsProperty().bind(_agentController._listAgentsNames);
        MainWindowAgentsWorkingModelingListView.itemsProperty().bind(_modelingsController._listAgentsNamesWorking);

        MainWindowCurrentModelingNameLabel.textProperty().bindBidirectional(_modelingsController._modelingShowingNameStringProperty);
        MainWindowCurrentModelingStateLabel.textProperty().bind(_modelingsController._modelingShowingStateStringProperty);
        MainWindowModelingLoadComboBox.itemsProperty().bind(_modelingsController._listSavedModelings);

        _scheduledFuture = _exec.scheduleAtFixedRate(this::runThread, 0, 1, TimeUnit.SECONDS);
    }

    /*
    Start block: misc
     */

    public void setApp(Main application)
    {
        _app = application;
    }

    private void changeScreen(int selectedItem)
    {
        for (int cpt = 0; cpt < _navigationMenu.size(); cpt++) {
            _navigationMenu.get(cpt).setVisible((cpt == selectedItem));
        }
    }

    private void runThread()
    {
        getAgentsFromServer();
        showAgentsInfo();
        getModelingListFromServer();
    }

    public static void stopTask()
    {
        if (_scheduledFuture != null)
            _scheduledFuture.cancel(true);
        if (_minimap != null)
            _minimap.stop();
        _exec.shutdownNow();
    }

    @FXML
    @SuppressWarnings("unused")
    private void closeWindow()
    {
        Main.closeWindow();
    }

    @FXML
    @SuppressWarnings("unused")
    private void disconnect()
    {
        stopTask();
        _app.userLogOut();
    }
    
    @FXML
    @SuppressWarnings("unused")
    private void launchMinimap()
    {
        if (_minimap == null)
        {
            _minimap = new CreateMinimap();
            try {
                _minimap.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Stop block: misc
     */

    /*
    Start block: Agent part
     */

    @FXML
    @SuppressWarnings("unused")
    private void deleteAgent()
    {
        SmartAPI.agentDelete(_agentSelected.getName()).runAsync(new SmartAPICallback<ServerStatus>() {
            @Override
            public void onError(ServerStatus serverStatus) {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Problem to delete " + _agentSelected.getName(), serverStatus.getMessage()));
            }

            @Override
            public void onFail(Exception e) {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Problem to delete " + _agentSelected.getName(), e.getMessage()));
            }

            @Override
            public void onSuccess(ServerStatus serverStatus) {
                Platform.runLater(() ->
                {
                    _alertDialog.alertDialog("Smart", _agentSelected.getName() + " has been deleted");
                    _agentSelected = null;
                });
            }
        });
    }

    private void changeAgentSelected(Agent agent)
    {
        _agentSelected = agent;
    }

    private void getAgentsFromServer()
    {
        _agentController.getAgentsFull(new SmartAPICallback<List<Agent>>() {
            @Override
            public void onError(ServerStatus s) {
            }

            @Override
            public void onFail(Exception e) {
            }

            @Override
            public void onSuccess(List<Agent> t) {
                Platform.runLater(() -> {
                    MainWindowHomeAgentsGridPane.getStyleClass().clear();
                    int nbConnected = 0;
                    for (Agent a : t)
                        if (a.isConnected())
                            nbConnected++;
                    MainWindowHomeAgentsInfoLabel.setText("Agents available: \t" + t.size() + "\n\n" +
                            nbConnected + ((nbConnected == 1) ? " agent" : " agents") +
                            " connected");
                    MainWindowHomeAgentsGridPane.getStyleClass().add((t.size() == 0) ? "gridPane_title_resume" : "label_title_resume_ok");
                });
            }
        });
    }

    private void showAgentsInfo()
    {
        if (_agentSelected == null)
            return ;
        MainWindowAgentTypeImageView.setImage((_agentSelected.getType().name().equals("TERRESTRIAL")) ? _terrestrialAgent : _flyingAgent);
        _agentController.getAgentsInfo(_agentSelected, new SmartAPICallback<Agent>() {
            @Override
            public void onError(ServerStatus serverStatus) {
            }

            @Override
            public void onFail(Exception e) {
            }

            @Override
            public void onSuccess(Agent agent) {
                Platform.runLater(() -> {
                    double battery = agent.getBattery();

                    MainWindowAgentBatteryLabel.getStyleClass().clear();
                    MainWindowAgentStateLabel.getStyleClass().clear();
                    MainWindowAgentBatteryLabel.setText(battery + "%");
                    if (battery > 75)
                        MainWindowAgentBatteryLabel.getStyleClass().add("label_battery_good");
                    else if (battery > 25)
                        MainWindowAgentBatteryLabel.getStyleClass().add("label_battery_ok");
                    else if (battery > 5)
                        MainWindowAgentBatteryLabel.getStyleClass().add("label_battery_bad");
                    else
                        MainWindowAgentBatteryLabel.getStyleClass().add("label_battery_warning");
                    if (agent.getCurrentPosition() != null)
                        MainWindowAgentPositionLabel.setText("X: " + agent.getCurrentPosition().getX() + "\n"
                                + "Y: " + agent.getCurrentPosition().getY() + "\n"
                                + "Z: " + agent.getCurrentPosition().getZ());
                    else
                        MainWindowAgentPositionLabel.setText("X: 0\nY: 0\nZ: 0");
                    if (agent.getState().name().toLowerCase().equals("ok"))
                        MainWindowAgentStateLabel.getStyleClass().add("label_state_ok");
                    else
                        MainWindowAgentStateLabel.getStyleClass().add("label_state_bad");
                    MainWindowAgentStateLabel.setText(agent.getState().name());
                });
            }
        });
    }

    /*
    End block: Agent part
     */

    /*
    * Block start: Modelings
    */

    private void getModelingListFromServer()
    {
        _modelingsController.updateModelingList(new SmartAPICallback<List<Modeling>>()
        {
            @Override
            public void onError(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could not get a modeling", serverStatus.getMessage()));
            }

            @Override
            public void onFail(Exception e)
            {
                Platform.runLater(MainWindowController.this::isDeleted);
            }

            @Override
            public void onSuccess(List<Modeling> modelings)
            {
                Platform.runLater(() ->
                {
                    int runningModelings = 0;
                    for (Modeling m : modelings)
                        if (m.getState() == ModelingState.RUNNING)
                            runningModelings++;
                    MainWindowHomeModelingsInfoLabel.setText("Modelings: \t" + runningModelings + " running");
                    MainWindowHomeModelingsGridPane.getStyleClass().add(((runningModelings > 0) ? "label_title_resume_ok" : "gridPane_title_resume"));
                    MainWindowCurrentModelingStateLabel.getStyleClass().clear();
                    switch (_modelingsController.getModelingState())
                    {
                        case LOADED:
                            MainWindowCurrentModelingStateLabel.getStyleClass().add("modeling_state_loaded_label");
                            isLoaded();
                            break;
                        case RUNNING:
                            MainWindowCurrentModelingStateLabel.getStyleClass().add("modeling_state_running_label");
                            isStarted();
                            break;
                        case UNLOADED:
                            MainWindowCurrentModelingStateLabel.getStyleClass().add("modeling_state_unloaded_label");
                            isUnloaded();
                            break;
                        default:
                            isDeleted();
                            break;
                    }
                });
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void showMenu()
    {
        TranslateTransition settingsTransition = new TranslateTransition(Duration.seconds(1), MainWindowModelingSettingsGridPane);
        TranslateTransition modelingTransition = new TranslateTransition(Duration.seconds(1), MainWindowModelingVbox);

        MainWindowModelingSettingsGridPane.setVisible(true);
        MainWindowModelingVbox.setDisable(true);
        modelingTransition.setFromY(0);
        modelingTransition.setToY(MainWindowModelingSettingsGridPane.getHeight());
        settingsTransition.setFromY(-(MainWindowModelingSettingsGridPane.getHeight()));
        settingsTransition.setToY(0);
        settingsTransition.play();
        modelingTransition.play();
    }

    @FXML
    @SuppressWarnings("unused")
    private void closeMenu()
    {
        TranslateTransition settingsTransition = new TranslateTransition(Duration.seconds(1), MainWindowModelingSettingsGridPane);
        TranslateTransition modelingTransition = new TranslateTransition(Duration.seconds(1), MainWindowModelingVbox);

        modelingTransition.setFromY(MainWindowModelingSettingsGridPane.getHeight());
        modelingTransition.setToY(0);
        settingsTransition.setFromY(MainWindowModelingSettingsGridPane.getHeight());
        settingsTransition.setToY(0);
        settingsTransition.play();
        modelingTransition.play();
        MainWindowModelingSettingsGridPane.setVisible(false);
        MainWindowModelingVbox.setDisable(false);
    }

    @FXML
    @SuppressWarnings("unused")
    private void loadModeling()
    {
        _modelingsController.executeInstruction(ModelingInstruction.LOAD, new SmartAPICallback<ServerStatus>() {
            @Override
            public void onError(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could not load modeling - error", serverStatus.getMessage()));
            }

            @Override
            public void onFail(Exception e)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could not load modeling - failure", e.getMessage()));
            }

            @Override
            public void onSuccess(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.alertDialog("Smart", "Modeling loaded"));
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void startModeling()
    {
        _modelingsController.executeInstruction(ModelingInstruction.START, new SmartAPICallback<ServerStatus>() {
            @Override
            public void onError(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could start the modeling", serverStatus.getMessage()));
            }

            @Override
            public void onFail(Exception e)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could start the modeling", e.getMessage()));
            }

            @Override
            public void onSuccess(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.alertDialog("Smart", "Modeling has now started"));
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void stopModeling()
    {
        _modelingsController.executeInstruction(ModelingInstruction.STOP, new SmartAPICallback<ServerStatus>() {
            @Override
            public void onError(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could not stop the modeling", serverStatus.getMessage()));
            }

            @Override
            public void onFail(Exception e)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could not stop the modeling", e.getMessage()));
            }

            @Override
            public void onSuccess(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.alertDialog("Smart", "Modeling has been stopped"));
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void unloadModeling()
    {
        _modelingsController.executeInstruction(ModelingInstruction.UNLOAD, new SmartAPICallback<ServerStatus>() {
            @Override
            public void onError(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could not unload the modeling", serverStatus.getMessage()));
            }

            @Override
            public void onFail(Exception e)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could not unload the modeling", e.getMessage()));
            }

            @Override
            public void onSuccess(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.alertDialog("Smart", "Modeling has been unloaded"));
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void deleteModeling()
    {
        _modelingsController.executeInstruction(ModelingInstruction.DELETE, new SmartAPICallback<ServerStatus>() {
            @Override
            public void onError(ServerStatus serverStatus)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could delete the modeling", serverStatus.getMessage()));
            }

            @Override
            public void onFail(Exception e)
            {
                Platform.runLater(() -> _alertDialog.errorDialog("Smart", "Could delete the modeling", e.getMessage()));
            }

            @Override
            public void onSuccess(ServerStatus serverStatus)
            {
                Platform.runLater(() ->
                {
                    _alertDialog.alertDialog("Smart", "Modeling has been deleted");
                    isDeleted();
                });
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void createModeling()
    {
        String name;

        do
        {
            name = MainWindowModelingCreateTextField.getText();
        } while (name.isEmpty());
        MainWindowModelingCreateProgressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        MainWindowModelingCreateProgressIndicator.setVisible(true);
        SmartAPI.modelingCreate(name).runAsync(new SmartAPICallback<ServerStatus>()
        {
            @Override
            public void onError(ServerStatus serverStatus)
            {
                Platform.runLater(() ->
                {
                    MainWindowModelingCreateProgressIndicator.setVisible(false);
                    _alertDialog.errorDialog("Smart", "Could not create a modeling:", serverStatus.getMessage());
                });
            }

            @Override
            public void onFail(Exception e)
            {
                Platform.runLater(() ->
                {
                    MainWindowModelingCreateProgressIndicator.setVisible(false);
                    _alertDialog.errorDialog("Smart", "Could not create a modeling:", e.getMessage());
                });
            }

            @Override
            public void onSuccess(ServerStatus serverStatus)
            {
                Platform.runLater(() ->
                {
                    MainWindowModelingCreateProgressIndicator.setVisible(false);
                    _alertDialog.alertDialog("Smart", "New Modeling created");
                });
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void displayModeling()
    {
        String modelingName = null;

        if (MainWindowModelingLoadComboBox.getValue() != null)
            modelingName = MainWindowModelingLoadComboBox.getValue();
        if (modelingName == null)
            _alertDialog.errorDialog("Smart", "Please choose a modeling", "");
        else {
            _alertDialog.alertDialog("Smart", "Showing modeling: " + modelingName);
            _modelingsController.setModeling(modelingName);
        }
    }

    private void isStarted()
    {
        MainWindowStartModelingButton.setDisable(true);
        MainWindowStopModelingButton.setDisable(false);
        MainWindowUnloadModelingButton.setDisable(true);
        MainWindowLoadModelingButton.setDisable(true);
        MainWindowDeleteModelingButton.setDisable(true);
    }

    private void isLoaded()
    {
        MainWindowStartModelingButton.setDisable(false);
        MainWindowStopModelingButton.setDisable(true);
        MainWindowUnloadModelingButton.setDisable(false);
        MainWindowLoadModelingButton.setDisable(true);
        MainWindowDeleteModelingButton.setDisable(true);
    }

    private void isUnloaded()
    {
        MainWindowStartModelingButton.setDisable(true);
        MainWindowStopModelingButton.setDisable(true);
        MainWindowUnloadModelingButton.setDisable(true);
        MainWindowLoadModelingButton.setDisable(false);
        MainWindowDeleteModelingButton.setDisable(false);
    }

    private void isDeleted()
    {
        MainWindowStartModelingButton.setDisable(true);
        MainWindowStopModelingButton.setDisable(true);
        MainWindowUnloadModelingButton.setDisable(true);
        MainWindowLoadModelingButton.setDisable(true);
        MainWindowDeleteModelingButton.setDisable(true);
    }

    /*
    Block end: Modelings
     */
}
