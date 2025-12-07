package com.cgvsu.io;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ObjSaverTest {

    private ObjSaver saver;
    private ObjSaver.ObjSaveSettings defaultSettings;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        saver = new ObjSaver();
        defaultSettings = new ObjSaver.ObjSaveSettings();
    }

    @Test
    @DisplayName("Поддержка OBJ расширения")
    void testSupportsExtension() {
        assertTrue(saver.supportsExtension(tempDir.resolve("model.obj")));
        assertTrue(saver.supportsExtension(tempDir.resolve("MODEL.OBJ")));
        assertFalse(saver.supportsExtension(tempDir.resolve("model.txt")));
        assertFalse(saver.supportsExtension(tempDir.resolve("model")));
        assertFalse(saver.supportsExtension(null));
    }

    @Test
    @DisplayName("Поддерживаемые расширения")
    void testGetSupportedExtensions() {
        String[] extensions = saver.getSupportedExtensions();
        assertEquals(1, extensions.length);
        assertEquals("obj", extensions[0]);
    }

    @Test
    @DisplayName("Сохранение простой модели с вершинами")
    void testSaveSimpleModel() throws ModelSavingException {
        Model model = createSimpleModel();

        String content = saver.generateContent(model);

        assertNotNull(content);
        assertTrue(content.contains("v 0 0 0"));
        assertTrue(content.contains("v 1 0 0"));
        assertTrue(content.contains("v 0 1 0"));
        assertTrue(content.contains("f 1 2 3"));
    }

    @Test
    @DisplayName("Сохранение модели с текстурными координатами")
    void testSaveWithTextureCoords() throws ModelSavingException {
        Model model = createModelWithTextures();

        String content = saver.generateContent(model);

        assertNotNull(content);
        assertTrue(content.contains("vt 0 0"));
        assertTrue(content.contains("vt 1 0"));
        assertTrue(content.contains("vt 0 1"));
        assertTrue(content.contains("f 1/1 2/2 3/3"));
    }

    @Test
    @DisplayName("Сохранение модели с нормалями")
    void testSaveWithNormals() throws ModelSavingException {
        Model model = createModelWithNormals();

        String content = saver.generateContent(model);

        assertNotNull(content);
        assertTrue(content.contains("vn 0 0 1"));
        assertTrue(content.contains("f 1//1 2//2 3//3"));
    }

    @Test
    @DisplayName("Сохранение с комментариями")
    void testSaveWithComments() throws ModelSavingException {
        Model model = createSimpleModel();
        ObjSaver.ObjSaveSettings settings = new ObjSaver.ObjSaveSettings();
        settings.includeComments = true;

        String content = saver.generateContent(model, settings);

        assertNotNull(content);
        assertTrue(content.contains("# Exported by Simple3DViewer"));
        assertTrue(content.contains("# Vertices: 3"));
        assertTrue(content.contains("# Polygons: 1"));
    }

    @Test
    @DisplayName("Сохранение без комментариев")
    void testSaveWithoutComments() throws ModelSavingException {
        Model model = createSimpleModel();
        ObjSaver.ObjSaveSettings settings = new ObjSaver.ObjSaveSettings();
        settings.includeComments = false;

        String content = saver.generateContent(model, settings);

        assertNotNull(content);
        assertFalse(content.contains("#"));
        assertTrue(content.contains("v "));
        assertTrue(content.contains("f "));
    }

    @Test
    @DisplayName("Точность чисел с плавающей точкой")
    void testFloatPrecision() throws ModelSavingException {
        Model model = new Model();
        model.vertices.add(new Vector3f(1.123456789f, 2.987654321f, 3.456789012f));

        ObjSaver.ObjSaveSettings settings = new ObjSaver.ObjSaveSettings();
        settings.floatPrecision = 3;

        String content = saver.generateContent(model, settings);

        assertTrue(content.contains("v 1.123 2.988 3.457"));
    }

    @Test
    @DisplayName("Валидация модели перед сохранением")
    void testModelValidation() {
        Model emptyModel = new Model();
        ModelSavingException exception = assertThrows(ModelSavingException.class,
            () -> saver.validateModel(emptyModel));
        assertTrue(exception.getMessage().contains("не содержит вершин"));

        Model modelWithoutPolygons = new Model();
        modelWithoutPolygons.vertices.add(new Vector3f(0, 0, 0));
        exception = assertThrows(ModelSavingException.class,
            () -> saver.validateModel(modelWithoutPolygons));
        assertTrue(exception.getMessage().contains("не содержит полигонов"));
    }

    @Test
    @DisplayName("Полный цикл загрузка-сохранение")
    void testRoundTrip() throws ModelLoadingException, ModelSavingException {
        String originalObj =
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "f 1 2 3\n";

        ObjLoader loader = new ObjLoader();
        Model loadedModel = loader.loadFromContent(originalObj);

        String savedContent = saver.generateContent(loadedModel);

        assertTrue(savedContent.contains("v "));
        assertTrue(savedContent.contains("f "));

        Model reloadedModel = loader.loadFromContent(savedContent);

        assertEquals(loadedModel.vertices.size(), reloadedModel.vertices.size());
        assertEquals(loadedModel.polygons.size(), reloadedModel.polygons.size());
    }


    private Model createSimpleModel() {
        Model model = new Model();

        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(0, 1, 0));

        Polygon polygon = new Polygon();
        ArrayList<Integer> vertexIndices = new ArrayList<>();
        vertexIndices.add(0);
        vertexIndices.add(1);
        vertexIndices.add(2);
        polygon.setVertexIndices(vertexIndices);
        model.polygons.add(polygon);

        return model;
    }

    private Model createModelWithTextures() {
        Model model = createSimpleModel();

        model.textureVertices.add(new Vector2f(0, 0));
        model.textureVertices.add(new Vector2f(1, 0));
        model.textureVertices.add(new Vector2f(0, 1));

        Polygon polygon = model.polygons.get(0);
        ArrayList<Integer> textureIndices = new ArrayList<>();
        textureIndices.add(0);
        textureIndices.add(1);
        textureIndices.add(2);
        polygon.setTextureVertexIndices(textureIndices);

        return model;
    }

    private Model createModelWithNormals() {
        Model model = createSimpleModel();

        model.normals.add(new Vector3f(0, 0, 1));
        model.normals.add(new Vector3f(0, 1, 0));
        model.normals.add(new Vector3f(1, 0, 0));

        Polygon polygon = model.polygons.get(0);
        ArrayList<Integer> normalIndices = new ArrayList<>();
        normalIndices.add(0);
        normalIndices.add(1);
        normalIndices.add(2);
        polygon.setNormalIndices(normalIndices);

        return model;
    }
}
