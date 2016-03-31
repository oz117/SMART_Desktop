package eip.smart.client.core.Main;

import eip.smart.api.SmartAPI;
import eip.smart.api.SmartAPICallback;
import eip.smart.client.core.Controllers.LoginWindowController;
import eip.smart.client.core.Controllers.MainWindowController;
import eip.smart.cscommons.model.ServerStatus;
import eip.smart.cscommons.model.modeling.Modeling;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eip.smart.client.core.Controllers.MainWindowController.stopTask;

public class Main extends Application
{
    private static Stage            _stage;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        try {
            _stage = primaryStage;
            _stage.setTitle("Smart - Dashboard");
            _stage.getIcons().add(new Image("eip/smart/client/icons/smart_eip_logo_green.png"));
            _stage.setOnCloseRequest(event -> closeWindow());
            goToConnection();
            primaryStage.show();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args)
    {
        launch(args);
    }

    public static void closeWindow()
    {
        stopTask();
        SmartAPI.shutdownExecutor();
        _stage.close();
    }

    public void userConnection(SmartAPICallback<List<Modeling>> smartAPICallback)
    {
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
                smartAPICallback.onSuccess(modelings);
                Platform.runLater(Main.this::goToMainWindow);
            }
        });
    }

    public void userLogOut()
    {
        goToConnection();
    }

    private void goToConnection()
    {
        try {
            LoginWindowController login = (LoginWindowController) replaceSceneContent("../View/LoginWindow.fxml");
            login.setApp(this);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void goToMainWindow()
    {
        try {
            MainWindowController mainWindow = (MainWindowController) replaceSceneContent("../View/MainWindow.fxml");
            mainWindow.setApp(this);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Initializable replaceSceneContent(String fxml) throws Exception
    {
        FXMLLoader loader = new FXMLLoader();
        Node page;

        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        try (InputStream in = Main.class.getResourceAsStream(fxml)) {
            page = loader.load(in);
        }
        Scene scene = new Scene((Parent) page);
        _stage.setScene(scene);
        if (fxml.substring(8).equals("MainWindow.fxml")) {
            _stage.setResizable(true);
            _stage.setMinHeight(760);
            _stage.setMinWidth(960);
        }
        else
            _stage.setResizable(false);
        _stage.sizeToScene();
        return (Initializable) loader.getController();
    }
}
