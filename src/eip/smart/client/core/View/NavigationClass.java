package eip.smart.client.core.View;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * EIP_Test
 * Created by Andre Paulos on 12/12/15.
 */

public class NavigationClass extends ListCell<String> {
    private static final String NAVIGATION_LIST_NAME = "navigation-list-name";
    private GridPane    _grid = new GridPane();
    private Label       _name = new Label();
    private ImageView   _icon = new ImageView();

    private void configureGrid() {
        _grid.setHgap(50);
        _grid.setPadding(new Insets(0, 5, 0, 5));
    }

    private void configureName() {
        _name.getStyleClass().add(NAVIGATION_LIST_NAME);
    }

    private void configureIcon() {
        _icon.setFitHeight(60);
        _icon.setPreserveRatio(true);
        _icon.smoothProperty().set(true);
    }

    public  NavigationClass() {
        configureGrid();
        configureName();
        configureIcon();
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        }
        else {
            GridPane grid = new GridPane();
            if (item != null) {
                try {
                    _icon.setImage(new Image("eip/smart/client/icons/Without_Description/" + item.toLowerCase() + ".png"));
                    _name.setText(item);
                    grid.add(_icon, 0, 0);
                    grid.add(_name, 1, 0);
                    grid.setHgap(50);
                    grid.autosize();
                    setGraphic(grid);
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
