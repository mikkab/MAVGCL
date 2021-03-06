/****************************************************************************
 *
 *   Copyright (c) 2016 Eike Mansfeld ecm@gmx.de. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 ****************************************************************************/

package com.comino.flight.tabs.world;

import java.io.IOException;

import org.fxyz.cameras.CameraTransformer;
import org.fxyz.extras.CubeWorld;

import com.comino.flight.widgets.charts.control.ChartControlWidget;
import com.comino.mav.control.IMAVController;
import com.comino.msp.utils.ExecutorService;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;

public class MAVWorldTab extends BorderPane  {


	private Task<Long> task;

	private PerspectiveCamera camera;
	private final double sceneWidth = 1000;
	private final double sceneHeight = 700;
	private double cameraDistance = 5000;

	private CameraTransformer cameraTransform = new CameraTransformer();

	private double mousePosX;
	private double mousePosY;
	private double mouseOldX;
	private double mouseOldY;
	private double mouseDeltaX;
	private double mouseDeltaY;

	@FXML
	private SubScene scene;

	private CubeWorld cubeWorld;

	private IMAVController control;

	public MAVWorldTab() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MAVWorldTab.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {

			throw new RuntimeException(exception);
		}

		task = new Task<Long>() {

			@Override
			protected Long call() throws Exception {
				while(true) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException iex) {
						Thread.currentThread().interrupt();
					}

					if(isDisabled()) {
						continue;
					}

					if (isCancelled() ) {
						break;
					}

					updateValue(System.currentTimeMillis());
				}
				return System.currentTimeMillis();
			}
		};


		task.valueProperty().addListener(new ChangeListener<Long>() {

			@Override
			public void changed(ObservableValue<? extends Long> observableValue, Long oldData, Long newData) {
				try {



				} catch(Exception e) { e.printStackTrace(); }

			}
		});

		setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 10.0;
            double modifierFactor = 0.5;

            if (me.isControlDown()) {
                modifier = 0.5;
            }
            if (me.isShiftDown()) {
                modifier = 50.0;
            }
            if (me.isPrimaryButtonDown() && !me.isAltDown()) {
                cameraTransform.ry.setAngle(((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 0.2) % 360 + 540) % 360 - 180);  // +
                cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 0.2) % 360 + 540) % 360 - 180);  // -
                cubeWorld.adjustPanelsByPos(cameraTransform.rx.getAngle(), cameraTransform.ry.getAngle(), cameraTransform.rz.getAngle());
            } else if (me.isSecondaryButtonDown() || ( me.isPrimaryButtonDown() && me.isAltDown())) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaY * modifierFactor * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isMiddleButtonDown()) {
                cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
            }
        });


	}


	@FXML
	private void initialize() {

		 Group sceneRoot = new Group();
	        SubScene scene = new SubScene(sceneRoot, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
	        scene.setFill(Color.BLACK);
	        //Setup camera and scatterplot cubeviewer
	        camera = new PerspectiveCamera(true);
	        cubeWorld = new CubeWorld(2000,100, false, false);


	        sceneRoot.getChildren().addAll(cubeWorld);
	        //setup camera transform for rotational support
	        cubeWorld.getChildren().add(cameraTransform);
	        cameraTransform.setTranslate(0, 0, 0);
	        cameraTransform.getChildren().add(camera);
	        camera.setNearClip(0.1);
	        camera.setFarClip(30000.0);
	        camera.setTranslateZ(-4000);
	        cameraTransform.ry.setAngle(-23.0);
	        cameraTransform.rx.setAngle(-28.0);
	        cubeWorld.adjustPanelsByPos(cameraTransform.rx.getAngle(), cameraTransform.ry.getAngle(), cameraTransform.rz.getAngle());
	        //add a Point Light for better viewing of the grid coordinate system
	        PointLight light = new PointLight(Color.WHITE);
	        cameraTransform.getChildren().add(light);
	        light.setTranslateX(camera.getTranslateX());
	        light.setTranslateY(camera.getTranslateY());
	        light.setTranslateZ(camera.getTranslateZ());
	        scene.setCamera(camera);

	        Box box = new Box(100f,100f,100f);
	        box.setTranslateY(+900);
	        cubeWorld.getChildren().add(box);
	        Box box2 = new Box(100f,100f,100f);
	        box2.setTranslateY(+900);
	        box2.setTranslateX(+100);
	        cubeWorld.getChildren().add(box2);


        this.setCenter(scene);


	}


	public MAVWorldTab setup(ChartControlWidget recordControl, IMAVController control) {
		this.control = control;
		ExecutorService.get().execute(task);
		return this;
	}


	public BooleanProperty getCollectingProperty() {
		return null;
	}

	public IntegerProperty getTimeFrameProperty() {
		return null;
	}





}


