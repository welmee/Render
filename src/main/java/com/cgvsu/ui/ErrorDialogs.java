package com.cgvsu.ui;

import com.cgvsu.io.ModelLoadingException;
import com.cgvsu.io.ModelSavingException;
import com.cgvsu.io.ModelIOFactoryException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class ErrorDialogs {

    public static void showModelLoadingError(ModelLoadingException exception, Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка загрузки модели");
        alert.setHeaderText("Не удалось загрузить модель");
        alert.setContentText(getReadableErrorMessage(exception));

        if (exception.getCause() != null) {
            alert.setResizable(true);
            alert.getDialogPane().setExpandableContent(createExpandableContent(exception));
        }

        if (ownerStage != null) {
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(ownerStage);
        }

        alert.showAndWait();
    }

    public static void showModelSavingError(ModelSavingException exception, Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка сохранения модели");
        alert.setHeaderText("Не удалось сохранить модель");
        alert.setContentText(getReadableErrorMessage(exception));

        if (exception.getCause() != null) {
            alert.setResizable(true);
            alert.getDialogPane().setExpandableContent(createExpandableContent(exception));
        }

        if (ownerStage != null) {
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(ownerStage);
        }

        alert.showAndWait();
    }

    public static void showIOFactoryError(ModelIOFactoryException exception, Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка формата файла");
        alert.setHeaderText("Неподдерживаемый формат файла");
        alert.setContentText(getReadableErrorMessage(exception));

        if (ownerStage != null) {
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(ownerStage);
        }

        alert.showAndWait();
    }

    public static void showGeneralError(String title, String headerText, String contentText,
                                       Throwable exception, Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        if (exception != null) {
            alert.setResizable(true);
            alert.getDialogPane().setExpandableContent(createExpandableContent(exception));
        }

        if (ownerStage != null) {
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(ownerStage);
        }

        alert.showAndWait();
    }

    public static Optional<ButtonType> showWarning(String title, String headerText, String contentText,
                                                   Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        if (ownerStage != null) {
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(ownerStage);
        }

        return alert.showAndWait();
    }

    public static Optional<ButtonType> showConfirmation(String title, String headerText, String contentText,
                                                       Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        if (ownerStage != null) {
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(ownerStage);
        }

        return alert.showAndWait();
    }

    public static void showInformation(String title, String headerText, String contentText,
                                      Stage ownerStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        if (ownerStage != null) {
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(ownerStage);
        }

        alert.showAndWait();
    }

    private static String getReadableErrorMessage(Exception exception) {
        String message = exception.getMessage();

        if (exception instanceof ModelLoadingException) {
            if (message.contains("Не найден загрузчик")) {
                return "Формат файла не поддерживается. Поддерживаемые форматы: OBJ";
            } else if (message.contains("Недостаточно координат")) {
                return "Файл модели поврежден: недостаточно данных для вершин";
            } else if (message.contains("ссылается на несуществующую")) {
                return "Файл модели поврежден: некорректные ссылки в полигонах";
            } else if (message.contains("некорректные значения")) {
                return "Файл модели содержит некорректные числовые значения";
            }
        }

        if (exception instanceof ModelSavingException) {
            if (message.contains("Неподдерживаемое расширение")) {
                return "Невозможно сохранить файл: неподдерживаемый формат";
            }
        }

        return message != null ? message : "Произошла неизвестная ошибка";
    }

    private static GridPane createExpandableContent(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();

        TextArea textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(textArea, 0, 0);

        return gridPane;
    }
}
