package eip.smart.client.minimap.example;

import eip.smart.client.minimap.example.simulation.SimulatedAgentList;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import eip.smart.api.SmartAPI;
import eip.smart.client.minimap.Minimap;
import eip.smart.client.minimap.Minimap.onAgentClickedEvent;
import eip.smart.cscommons.model.geometry.Point2D;
import eip.smart.cscommons.model.geometry.Point3D;
import eip.smart.cscommons.model.geometry.PointCloud2D;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CreateMinimap extends Application {
	private static final int	HEIGHT		= 800;
    private static final long	TIMER_DELAY	= 100;
	private static final int	WIDTH		= 1200;
    private String				selectedAgent	= null;
	protected boolean			shiftPressed	= false;
    public Minimap              _canvas;
    private SimulatedAgentList agentList;
    private Timer agentTimer;

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.setTitle("Minimap");
		primaryStage.setResizable(true);
		Group root = new Group();
		root.setLayoutX(10);
		root.setLayoutY(10);
		_canvas = new Minimap(CreateMinimap.WIDTH, CreateMinimap.HEIGHT);
		_canvas.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.SHIFT))
				CreateMinimap.this.shiftPressed = true;
        });
		_canvas.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode().equals(KeyCode.SHIFT))
				CreateMinimap.this.shiftPressed = false;
        });

		_canvas.setOnAgentClickedEvent(new onAgentClickedEvent() {
			@Override
			public void handleAgentClicked(String agentName, MouseButton button, int clickCount) {
				if (button == MouseButton.SECONDARY) {
					_canvas.getSettingsManager().setShowPathFuture(agentName, !_canvas.getSettingsManager().isShowPathFuture(agentName));
					_canvas.getSettingsManager().setShowPathPast(agentName, !_canvas.getSettingsManager().isShowPathPast(agentName));
				}
				if (button == MouseButton.PRIMARY) {
					_canvas.setFollowedActor(agentName);
					CreateMinimap.this.selectedAgent = agentName;
					_canvas.setSelectedAgent(agentName);
				}
			}

			@Override
			public void handleClick(Point2D pos, MouseButton button, int clickCount) {
				if (button == MouseButton.PRIMARY) {
					_canvas.setFollowedActor(null);
					CreateMinimap.this.selectedAgent = null;
					_canvas.setSelectedAgent(null);
				}
				if (button == MouseButton.SECONDARY)
					if (CreateMinimap.this.selectedAgent != null) {
                        if (CreateMinimap.this.selectedAgent.equals("AgentMoveable")) {
                            if (CreateMinimap.this.shiftPressed)
                                CreateMinimap.this.agentList.getAgent(CreateMinimap.this.selectedAgent).pushOrder(new Point3D(pos.getX(), pos.getY(), 0));
                            else
                                CreateMinimap.this.agentList.getAgent(CreateMinimap.this.selectedAgent).newOrder(new Point3D(pos.getX(), pos.getY(), 0));
                        }
                        else {
                            if (CreateMinimap.this.shiftPressed)
                                SmartAPI.AddWayPoint(CreateMinimap.this.selectedAgent, new Point3D(pos.getX(), pos.getY(), 0)).runAsync(null);
                            else
                                SmartAPI.manualOrder(CreateMinimap.this.selectedAgent, new Point3D(pos.getX(), pos.getY(), 0)).runAsync(null);
                        }
                    }
			}
		});
		root.getChildren().add(_canvas);
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
        this.agentList = new SimulatedAgentList();
        this.agentTimer = new Timer();
        this.agentTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                CreateMinimap.this.agentList.simulate();
                _canvas.updateAgents(CreateMinimap.this.agentList.getAgents());
            }
        }, new Date(), CreateMinimap.TIMER_DELAY);
        PointCloud2D mapping = new PointCloud2D();
		mapping.add(new Point2D(-40, -40));
		mapping.add(new Point2D(-40, 40));
		mapping.add(new Point2D(40, 40));
		mapping.add(new Point2D(40, 10));
		mapping.add(new Point2D(60, 10));
		mapping.add(new Point2D(60, 40));
		mapping.add(new Point2D(100, 40));
		mapping.add(new Point2D(100, -40));
		mapping.add(new Point2D(60, -40));
		mapping.add(new Point2D(60, -40));
		mapping.add(new Point2D(60, -10));
		mapping.add(new Point2D(40, -10));
		mapping.add(new Point2D(40, -40));
		_canvas.updatePointCloud(mapping);
	}

    public void stop()
    {
        agentTimer.cancel();
    }
}
