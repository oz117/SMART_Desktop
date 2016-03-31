package eip.smart.client.core.Controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import eip.smart.client.core.Main.Main;
import eip.smart.cscommons.model.modeling.Modeling;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import eip.smart.api.SmartAPI;
import eip.smart.api.SmartAPICallback;
import eip.smart.cscommons.model.ServerStatus;

/** Controls the login screen */
/*
** Todo: Remove code about the default connection
** Todo: Add a kind of history with all the ip and ports.
 */

public class LoginWindowController implements Initializable {
	@FXML
	private Button				LoginWindowConnectButton;
	@FXML
	private Label				LoginWindowConnectionFailedLabel;
	@FXML
	private Button				LoginWindowDetailButton;
	@FXML
	private Label				LoginWindowErrorLabel;
    @FXML
    private MenuItem            LoginWindowCloseMenuItem;
    @FXML
    private MenuItem            LoginWindowDefaultConnectionMenuItem;
	@FXML
	private TextField			LoginWindowIpAddressLabel;
	@FXML
	private TextField			LoginWindowPortLabel;
	@FXML
	private ProgressIndicator	LoginWindowProgressIndicator;
	@FXML
	private VBox				LoginWindowVBox;
	private Main				_app;

	private String authorize() {
		String url;
		String ip;
		String port;

		ip = this.LoginWindowIpAddressLabel.getText();
		port = this.LoginWindowPortLabel.getText();
        if (ip.isEmpty() && port.isEmpty()) {
            ip = "54.148.17.11";
            port = "80";
        }
		url = ip + ":" + port;
		return (url);
	}

	private void handleError(String statusType, String message) {
		LoginWindowController.this.LoginWindowProgressIndicator.setVisible(false);
		LoginWindowController.this.LoginWindowConnectButton.setVisible(true);
		LoginWindowController.this.LoginWindowDetailButton.setVisible(true);
		if (!LoginWindowController.this.LoginWindowConnectionFailedLabel.isVisible()) {
			LoginWindowController.this.LoginWindowConnectionFailedLabel.setVisible(true);
		}
		LoginWindowController.this.LoginWindowConnectionFailedLabel.setText(statusType);
		LoginWindowController.this.LoginWindowErrorLabel.setText(message);
	}

	public void connexion() {
		String sessionID = "http://" + this.authorize();

		this.LoginWindowProgressIndicator.setVisible(true);
		this.LoginWindowProgressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        this.LoginWindowConnectionFailedLabel.setVisible(false);
		this.LoginWindowConnectButton.setVisible(false);
        this.LoginWindowDetailButton.setVisible(false);
		SmartAPI.SERVER_URL = sessionID;
		_app.userConnection(new SmartAPICallback<List<Modeling>>() {
            @Override
            public void onError(ServerStatus s) {
                Platform.runLater(() -> handleError("Connection error", s.getMessage()));
            }

            @Override
            public void onFail(Exception e) {
                Platform.runLater(() -> handleError("Connection failed", e.getMessage()));
            }

            @Override
            public void onSuccess(List<Modeling> t) {
				Stage stage;

				stage = (Stage) LoginWindowController.this.LoginWindowVBox.getScene().getWindow();
				if (stage.getHeight() == 640)
					expandWindow();
				LoginWindowController.this.LoginWindowProgressIndicator.setVisible(false);
				if (LoginWindowController.this.LoginWindowConnectionFailedLabel.isVisible()) {
					LoginWindowController.this.LoginWindowConnectionFailedLabel.setVisible(false);
					LoginWindowController.this.LoginWindowDetailButton.setVisible(false);
					LoginWindowController.this.LoginWindowConnectionFailedLabel.setDisable(true);
				}
            }
        });
	}

	public void expandWindow() {
		Stage stage;

		stage = (Stage) this.LoginWindowVBox.getScene().getWindow();
		if (stage.getHeight() == 640) {
			stage.setHeight(480);
		} else {
			stage.setHeight(640);
		}
		stage.setWidth(stage.getWidth());
	}

	public void setApp(Main application) {
		_app = application;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.LoginWindowDetailButton.setVisible(false);
        this.LoginWindowDefaultConnectionMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        this.LoginWindowCloseMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
	}
}
