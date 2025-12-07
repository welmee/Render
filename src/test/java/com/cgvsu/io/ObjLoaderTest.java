package com.cgvsu.io;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ObjLoaderTest {

    private ObjLoader loader;

    @BeforeEach
    void setUp() {
        loader = new ObjLoader();
    }

    @Test
    @DisplayName("Поддержка OBJ расширения")
    void testSupportsExtension() {
        assertTrue(loader.supportsExtension(Paths.get("model.obj")));
        assertTrue(loader.supportsExtension(Paths.get("MODEL.OBJ")));
        assertFalse(loader.supportsExtension(Paths.get("model.txt")));
        assertFalse(loader.supportsExtension(Paths.get("model")));
        assertFalse(loader.supportsExtension(null));
    }

    @Test
    @DisplayName("Поддерживаемые расширения")
    void testGetSupportedExtensions() {
        String[] extensions = loader.getSupportedExtensions();
        assertEquals(1, extensions.length);
        assertEquals("obj", extensions[0]);
    }

    @Test
    @DisplayName("Загрузка простой модели с одной вершиной")
    void testLoadSimpleVertex() throws ModelLoadingException {
        String objContent = "v 1.0 2.0 3.0\n";

        Model model = loader.loadFromContent(objContent);

        assertNotNull(model);
        assertEquals(1, model.vertices.size());
        Vector3f expected = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f actual = model.vertices.get(0);
        assertEquals(expected.x, actual.x, 0.001f);
        assertEquals(expected.y, actual.y, 0.001f);
        assertEquals(expected.z, actual.z, 0.001f);
    }

    @Test
    @DisplayName("Загрузка модели с вершинами и полигонами")
    void testLoadWithVerticesAndFaces() throws ModelLoadingException {
        String objContent =
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "f 1 2 3\n";

        Model model = loader.loadFromContent(objContent);

        assertNotNull(model);
        assertEquals(3, model.vertices.size());
        assertEquals(1, model.polygons.size());
        assertEquals(3, model.polygons.get(0).getVertexIndices().size());
    }

    @Test
    @DisplayName("Загрузка модели с текстурными координатами")
    void testLoadWithTextureCoords() throws ModelLoadingException {
        String objContent =
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "vt 0.0 0.0\n" +
            "vt 1.0 0.0\n" +
            "vt 0.0 1.0\n" +
            "f 1/1 2/2 3/3\n";

        Model model = loader.loadFromContent(objContent);

        assertNotNull(model);
        assertEquals(3, model.vertices.size());
        assertEquals(3, model.textureVertices.size());
        assertEquals(1, model.polygons.size());
        assertEquals(3, model.polygons.get(0).getTextureVertexIndices().size());
    }

    @Test
    @DisplayName("Загрузка модели с нормалями")
    void testLoadWithNormals() throws ModelLoadingException {
        String objContent =
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "vn 0.0 0.0 1.0\n" +
            "vn 0.0 1.0 0.0\n" +
            "vn 1.0 0.0 0.0\n" +
            "f 1//1 2//2 3//3\n";

        Model model = loader.loadFromContent(objContent);

        assertNotNull(model);
        assertEquals(3, model.vertices.size());
        assertEquals(3, model.normals.size());
        assertEquals(1, model.polygons.size());
        assertEquals(3, model.polygons.get(0).getNormalIndices().size());
    }

    @Test
    @DisplayName("Обработка комментариев и пустых строк")
    void testIgnoreCommentsAndEmptyLines() throws ModelLoadingException {
        String objContent =
            "# This is a comment\n" +
            "\n" +
            "v 1.0 2.0 3.0\n" +
            "# Another comment\n" +
            "\n" +
            "v 4.0 5.0 6.0\n";

        Model model = loader.loadFromContent(objContent);

        assertNotNull(model);
        assertEquals(2, model.vertices.size());
    }

    @Test
    @DisplayName("Ошибка при недостаточных координатах вершины")
    void testInsufficientVertexCoordinates() {
        String objContent = "v 1.0 2.0\n"; // Только 2 координаты вместо 3

        ModelLoadingException exception = assertThrows(ModelLoadingException.class,
            () -> loader.loadFromContent(objContent));

        assertTrue(exception.getMessage().contains("Недостаточно координат"));
    }

    @Test
    @DisplayName("Ошибка при некорректных числах")
    void testInvalidNumberFormat() {
        String objContent = "v 1.0 abc 3.0\n";

        ModelLoadingException exception = assertThrows(ModelLoadingException.class,
            () -> loader.loadFromContent(objContent));

        assertTrue(exception.getMessage().contains("Некорректный формат чисел в координатах вершины"));
    }

    @Test
    @DisplayName("Ошибка при некорректных индексах полигона")
    void testInvalidPolygonIndices() {
        String objContent =
            "v 0.0 0.0 0.0\n" +
            "f 1 999 3\n"; // Индекс 999 не существует

        ModelLoadingException exception = assertThrows(ModelLoadingException.class,
            () -> loader.loadFromContent(objContent));

        assertTrue(exception.getMessage().contains("несуществующую вершину"));
    }

    @Test
    @DisplayName("Ошибка при пустом полигоне")
    void testEmptyPolygon() {
        String objContent = "f\n";

        ModelLoadingException exception = assertThrows(ModelLoadingException.class,
            () -> loader.loadFromContent(objContent));

        assertTrue(exception.getMessage().contains("минимум 3 вершины"));
    }

    @Test
    @DisplayName("Валидация модели после загрузки")
    void testModelValidation() {
        Model emptyModel = new Model();
        ModelLoadingException exception = assertThrows(ModelLoadingException.class,
            () -> loader.validateModel(emptyModel));
        assertTrue(exception.getMessage().contains("не содержит вершин"));

        Model invalidModel = new Model();
        invalidModel.vertices.add(new Vector3f(0, 0, 0)); // Только одна вершина
        com.cgvsu.model.Polygon polygon = new com.cgvsu.model.Polygon();
        ArrayList<Integer> vertexIndices = new ArrayList<>();
        vertexIndices.add(0); // Корректная ссылка
        vertexIndices.add(1); // Ссылка на несуществующую вершину
        vertexIndices.add(2); // Ссылка на несуществующую вершину
        polygon.setVertexIndices(vertexIndices);
        invalidModel.polygons.add(polygon);
        exception = assertThrows(ModelLoadingException.class,
            () -> loader.validateModel(invalidModel));
        assertTrue(exception.getMessage().contains("ссылается на несуществующую вершину"));
    }
}
