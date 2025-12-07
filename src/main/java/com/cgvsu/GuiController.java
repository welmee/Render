package com.cgvsu;

import com.cgvsu.io.*;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.scene.Scene;
import com.cgvsu.scene.SceneModel;
import com.cgvsu.ui.ErrorDialogs;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import javax.vecmath.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Scene scene;

    private Camera camera = new Camera(
            new Vector3f(0, 00, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML
    private void initialize() {
        scene = new Scene("Main Scene");
        scene.addCamera(camera);

        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            for (var sceneModel : scene.getElementsOfType(SceneModel.class)) {
                if (sceneModel.isVisible() && sceneModel.isValid()) {
                    RenderEngine.render(canvas.getGraphicsContext2D(), camera,
                                      sceneModel.getModel(), (int) width, (int) height);
                }
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path filePath = Path.of(file.getAbsolutePath());

        try {
            ModelLoader loader = ModelIOFactory.createLoader(filePath);
            Model model = loader.load(filePath);

            String modelId = "model_" + System.currentTimeMillis();
            String modelName = file.getName();

            SceneModel sceneModel = new SceneModel(modelId, modelName, model);
            scene.addElement(sceneModel);

            scene.selectElement(sceneModel);

            ErrorDialogs.showInformation("Модель загружена",
                "Модель успешно загружена",
                String.format("Загружено: %s\nВершин: %d, Полигонов: %d",
                    modelName, model.vertices.size(), model.polygons.size()),
                (Stage) canvas.getScene().getWindow());

        } catch (ModelIOFactoryException e) {
            ErrorDialogs.showIOFactoryError(e, (Stage) canvas.getScene().getWindow());
        } catch (ModelLoadingException e) {
            ErrorDialogs.showModelLoadingError(e, (Stage) canvas.getScene().getWindow());
        } catch (Exception e) {
            ErrorDialogs.showGeneralError("Неожиданная ошибка",
                "Произошла непредвиденная ошибка при загрузке модели",
                e.getMessage(), e, (Stage) canvas.getScene().getWindow());
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        var selectedElements = scene.getSelectedElements();
        if (selectedElements.isEmpty()) {
            ErrorDialogs.showGeneralError("Нет выбранной модели",
                "Выберите модель для сохранения",
                "Сначала загрузите и выберите модель в сцене.",
                null, (Stage) canvas.getScene().getWindow());
            return;
        }

        var selectedElement = selectedElements.get(0);
        if (!(selectedElement instanceof SceneModel)) {
            ErrorDialogs.showGeneralError("Неверный тип элемента",
                "Выбран неподдерживаемый тип элемента",
                "Можно сохранять только модели.",
                null, (Stage) canvas.getScene().getWindow());
            return;
        }

        SceneModel sceneModel = (SceneModel) selectedElement;

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");
        fileChooser.setInitialFileName(sceneModel.getName() + ".obj");

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path filePath = Path.of(file.getAbsolutePath());

        try {
            ModelSaver saver = ModelIOFactory.createSaver(filePath);
            saver.save(sceneModel.getModel(), filePath);

            ErrorDialogs.showInformation("Модель сохранена",
                "Модель успешно сохранена",
                String.format("Сохранено: %s", file.getName()),
                (Stage) canvas.getScene().getWindow());

        } catch (ModelIOFactoryException e) {
            ErrorDialogs.showIOFactoryError(e, (Stage) canvas.getScene().getWindow());
        } catch (ModelSavingException e) {
            ErrorDialogs.showModelSavingError(e, (Stage) canvas.getScene().getWindow());
        } catch (Exception e) {
            ErrorDialogs.showGeneralError("Неожиданная ошибка",
                "Произошла непредвиденная ошибка при сохранении модели",
                e.getMessage(), e, (Stage) canvas.getScene().getWindow());
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }
}