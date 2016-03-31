/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eip.smart.client.visualizer;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import eip.smart.api.SmartAPI;
import eip.smart.api.SmartAPICallback;
import eip.smart.cscommons.model.ServerStatus;
import eip.smart.cscommons.model.geometry.Point3D;
import eip.smart.cscommons.model.geometry.PointCloud3DGenerator;
//import javafx.scene.paint.Color;

/**
 *
 * @author redrum
 */
public class Visualizer extends Application {

	private static final double	AXIS_LENGTH				= 25000.0;
	private static final double	CAMERA_FAR_CLIP			= 10000.0;
	private static final double	CAMERA_INITIAL_DISTANCE	= -450;
	private static final double	CAMERA_NEAR_CLIP		= 0.1;
	private static final double	CONTROL_MULTIPLIER		= 0.1;
	private static final double	MOUSE_SPEED				= 0.1;
	private static final double	ROTATION_SPEED			= 2.0;
	private static final double	SHIFT_MULTIPLIER		= 10.0;
	private static final double	TRACK_SPEED				= 0.3;
	private static final double CAMERA_INITIAL_X_ANGLE = 360.0;
	private static final double CAMERA_INITIAL_Y_ANGLE = 180.0;

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	final Xform				axisGroup	= new Xform();

	final PerspectiveCamera	camera		= new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();

    
	double					mouseDeltaX;
	double					mouseDeltaY;

	double					mouseOldX;
	double					mouseOldY;
	double					mousePosX;
	double					mousePosY;
	final Xform				pointGroup	= new Xform();
	final Group				root		= new Group();

	final Xform				world		= new Xform();

	private void buildAxes() {
		System.out.println("buildAxes()");
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);

		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);

		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);

		final Box xAxis = new Box(Visualizer.AXIS_LENGTH, 1, 1);
		final Box yAxis = new Box(1, Visualizer.AXIS_LENGTH, 1);
		final Box zAxis = new Box(1, 1, Visualizer.AXIS_LENGTH);

		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);

		this.axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		this.axisGroup.setVisible(false);
		this.world.getChildren().addAll(this.axisGroup);
	}

	// private void buildScene() {
	// root.getChildren().add(world);
	// }
	 private void buildCamera() {
	        System.out.println("buildCamera()");
	        root.getChildren().add(cameraXform);
	        cameraXform.getChildren().add(cameraXform2);
	        cameraXform2.getChildren().add(cameraXform3);
	        cameraXform3.getChildren().add(camera);
	        cameraXform3.setRotateZ(180.0);
	        //cameraXform3.setRotateY(10.0);
	        //cameraXform3.setRotateX(10.0);

	        camera.setNearClip(CAMERA_NEAR_CLIP);
	        camera.setFarClip(CAMERA_FAR_CLIP);
	        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
	        camera.setTranslateY(-50);
	        camera.setTranslateX(50);
	        //camera.setRotate(180.0);
	        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
	        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
	    }
	 
	/*private void buildCamera() {
		System.out.println("buildCamera()");
		this.root.getChildren().add(this.camera);

		this.camera.setNearClip(Visualizer.CAMERA_NEAR_CLIP);
		this.camera.setFarClip(Visualizer.CAMERA_FAR_CLIP);
		this.camera.setTranslateZ(Visualizer.CAMERA_INITIAL_DISTANCE);
		this.camera.setTranslateY(-50);
		this.camera.setTranslateX(50);
		this.camera.setRotationAxis(Rotate.Y_AXIS);
	}*/

	private void buildPoint(double x, double y, double z) {
		
		final PhongMaterial pointMaterial = new PhongMaterial();
		pointMaterial.setDiffuseColor(Color.DARKGREEN);
		// pointMaterial.setSpecularColor(Color.GREEN);

		Xform pointXform = new Xform();

		Sphere pointSphere = new Sphere(0.5, 4);
		//Box pointSphere = new Box(0.5, 0.5, 0.5);
		
		pointSphere.setMaterial(pointMaterial);

		pointXform.setTx(x);
		pointXform.setTy(y * -1);
		pointXform.setTz(z);

		pointXform.getChildren().add(pointSphere);

		this.pointGroup.getChildren().add(pointXform);

		//world.getChildren().addAll(pointGroup);
	}

	/*private void buildPoint(double x, double y, double z) {

		final PhongMaterial pointMaterial = new PhongMaterial();
		pointMaterial.setDiffuseColor(Color.DARKGREEN);
		// pointMaterial.setSpecularColor(Color.GREEN);

		Xform pointXform = new Xform();

		Sphere pointSphere = new Sphere(1.0);

		pointSphere.setMaterial(pointMaterial);

		pointXform.setTx(x);
		pointXform.setTy(y * -1);
		pointXform.setTz(z);

		pointXform.getChildren().add(pointSphere);

		this.pointGroup.getChildren().add(pointXform);

		// world.getChildren().addAll(pointGroup);
	}*/

	
	private void handleKeyboard(Scene scene, final Node root) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				case Z:
					Visualizer.this.camera.setTranslateZ(Visualizer.CAMERA_INITIAL_DISTANCE);
					break;
				case SPACE:
					Visualizer.this.axisGroup.setVisible(!Visualizer.this.axisGroup.isVisible());
					break;
				case V:
					Visualizer.this.pointGroup.setVisible(!Visualizer.this.pointGroup.isVisible());
					break;
				case DOWN:
					Visualizer.this.translateCameraZ(-1.0);
					Visualizer.this.translateCameraX(-1.0);
					break;
				case UP:
					Visualizer.this.translateCameraZ(1.0);
					Visualizer.this.translateCameraX(1.0);
					break;
/*				case RIGHT:
					// camera.setTranslateX(camera.getTranslateX() + 10);
					Visualizer.this.camera.setRotate(Visualizer.this.camera.getRotate() + 1.0);
					Visualizer.this.camera.setRotate((Visualizer.this.camera.getRotate() >= 360.0) ? Visualizer.this.camera.getRotate() - 360.0 : Visualizer.this.camera.getRotate());
					//System.out.println("angle = " + Visualizer.this.camera.getRotate());
					break;
				case LEFT:
					// camera.setTranslateX(camera.getTranslateX() - 10);
					Visualizer.this.camera.setRotate(Visualizer.this.camera.getRotate() - 1.0);
					Visualizer.this.camera.setRotate((Visualizer.this.camera.getRotate() < 0.0) ? Visualizer.this.camera.getRotate() + 360.0 : Visualizer.this.camera.getRotate());
					//System.out.println("angle = " + Visualizer.this.camera.getRotate());
					break;*/
				}
			}
		});
	}

	private void handleMouse(Scene scene, final Node root) {
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				Visualizer.this.mousePosX = me.getSceneX();
				Visualizer.this.mousePosY = me.getSceneY();
				Visualizer.this.mouseOldX = me.getSceneX();
				Visualizer.this.mouseOldY = me.getSceneY();
			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				Visualizer.this.mouseOldX = Visualizer.this.mousePosX;
				Visualizer.this.mouseOldY = Visualizer.this.mousePosY;
				Visualizer.this.mousePosX = me.getSceneX();
				Visualizer.this.mousePosY = me.getSceneY();
				Visualizer.this.mouseDeltaX = (Visualizer.this.mousePosX - Visualizer.this.mouseOldX);
				Visualizer.this.mouseDeltaY = (Visualizer.this.mousePosY - Visualizer.this.mouseOldY);

				if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*MOUSE_SPEED*ROTATION_SPEED);  
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY*MOUSE_SPEED*ROTATION_SPEED);  
                }
				// camera.setRotationAxis(Rotate.X_AXIS);
				/*****Visualizer.this.camera.setRotate(Visualizer.this.camera.getRotate() + Visualizer.this.mouseDeltaX * Visualizer.MOUSE_SPEED * Visualizer.ROTATION_SPEED);*****/
				// camera.setRotationAxis(Rotate.Y_AXIS);
				// camera.setRotate(camera.getRotate() + mouseDeltaY*MOUSE_SPEED*ROTATION_SPEED);
				// camera.setAngle(mouseDeltaX*MOUSE_SPEED*modifier*ROTATION_SPEED);
				// camera.setAngle(mouseDeltaY*MOUSE_SPEED*modifier*ROTATION_SPEED);
			}
		});
	}

	@Override
	public void start(Stage primaryStage) {

		// setUserAgentStylesheet(STYLESHEET_MODENA);
		System.out.println("start()");

		this.root.getChildren().add(this.world);
		this.root.setDepthTest(DepthTest.ENABLE);

		// buildScene();
		this.buildCamera();
		this.buildAxes();

		SmartAPI.getPoints().run(new SmartAPICallback<List<Point3D>>() {
			@Override
			public void onError(ServerStatus serverStatus) {
				System.out.println("error : " + serverStatus);
			}
			
			@Override
			public void onFail(Exception e) {
				System.out.println("exception : " + e);
			}

			double zoom = 1.0;
			
			@Override
			public void onSuccess(List<Point3D> points) {
				/*
				 *  ligne a supprimer
				 *  UNIQUEMENT POUR TEST
				 */
			//	System.out.println("ICCIIIIIIIIIIIIIIIIII");
				//points = new PointCloud3DGenerator().generatePointCloud(70).getPoints();
				System.out.println("ON SUCCESS !!!!!");
				//new TetrahedronMesh(points.size(), new ArrayList<Point3D>(points));
				for (Point3D point3d : points) {
					System.out.println("add point : " + point3d);
					Visualizer.this.buildPoint(point3d.getX()*zoom, point3d.getY()*zoom, point3d.getZ()*zoom);
				}
			}
		});

		//buildPoint(2.0, 2.0, 2.0);
		this.world.getChildren().addAll(this.pointGroup);

		Scene scene = new Scene(this.root, 1024, 768, true);
		scene.setFill(Color.GREY);
		this.handleKeyboard(scene, this.world);
		this.handleMouse(scene, this.world);

		primaryStage.setTitle("Visualizer");
		primaryStage.setScene(scene);
		primaryStage.show();

		scene.setCamera(this.camera);
	}

	private void translateCameraX(double dir) {
		double angle, coef;

		coef = 0.0;
		angle = this.camera.getRotate();
		if (angle >= 0.0 && angle < 90.0) {
			coef = (dir * (10.0 / (90.0 / angle)));
		} else if (angle == 0.0) {
			coef = 0.0;
		} else if (angle == 90.0) {
			coef = (dir * 10.0);
		} else if (angle == 180.0) {
			coef = 0.0;
		} else if (angle == 270.0) {
			coef = -1.0 * (dir * 10.0);
		} else if (angle > 90 && angle < 180) {
			coef = -1.0 * (dir * (10.0 / (90.0 / (90.0 - (angle - 90.0)))));
		} else if (angle > 180.0 && angle < 270.0) {
			coef = -1.0 * (dir * (10.0 / (90.0 / (angle - 180.0))));
		} else if (angle > 270.0 && angle < 360.0) {
			coef = -1.0 * (dir * (10.0 / (90.0 / (90.0 - (angle - 270.0)))));
		}
		this.camera.setTranslateX(this.camera.getTranslateX() + coef);
		//System.out.println("X+ = " + coef);
	}

	private void translateCameraZ(double dir) {
		double angle, coef;

		coef = 0.0;
		angle = this.camera.getRotate();
		if (angle >= 0.0 && angle < 90.0) {
			coef = (dir * (10.0 / (90.0 / (90.0 - angle))));
		} else if (angle == 0.0) {
			coef = (dir * 10.0);
		} else if (angle == 90.0) {
			coef = (dir * 0.0);
		} else if (angle == 180.0) {
			coef = -1 * (dir * 10.0);
		} else if (angle == 270.0) {
			coef = (dir * 0.0);
		} else if (angle > 90 && angle < 180) {
			coef = (dir * (10.0 / (90.0 / (angle - 90.0))));
		} else if (angle > 180.0 && angle < 270.0) {
			coef = -1 * (dir * (10.0 / (90.0 / (90.0 - (angle - 180.0)))));
		} else if (angle > 270.0 && angle < 360.0) {
			coef = (dir * (10.0 / (90.0 / (angle - 270.0))));
		}
		this.camera.setTranslateZ(this.camera.getTranslateZ() + coef);
		//System.out.println("Z+ = " + coef);
	}

}
