package eip.smart.client.core.View;

import eip.smart.cscommons.model.agent.Agent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class AgentCell extends ListCell<Agent> {
    @Override
    public void updateItem(Agent item, boolean empty) {
        super.updateItem(item, empty);
        GridPane grid = new GridPane();
        if (item != null) {
            Image image = new Image("eip/smart/client/icons/Without_Description/terrestrial_drone_without_credits.png");
            ImageView iv = new ImageView(image);
            iv.setFitHeight(100);
            iv.setPreserveRatio(true);
            grid.add(iv, 0, 0);
            grid.add(new Label(item.getName()), 1, 0);
            grid.autosize();
            setGraphic(grid);
        }
    }
}
