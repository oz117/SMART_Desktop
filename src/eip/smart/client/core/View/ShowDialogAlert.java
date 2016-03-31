package eip.smart.client.core.View;

import javafx.scene.control.Alert;

public class    ShowDialogAlert {
    private static ShowDialogAlert _instance = null;
    private ShowDialogAlert() {}
    public static ShowDialogAlert getInstance() {
        if (_instance == null)
            _instance = new ShowDialogAlert();
        return _instance;
    }

    public void alertDialog(String title, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void errorDialog(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
