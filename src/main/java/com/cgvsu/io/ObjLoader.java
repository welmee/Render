package com.cgvsu.io;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjLoader extends ModelLoader {

    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";
    private static final String OBJ_COMMENT_TOKEN = "#";
    private static final String OBJ_OBJECT_TOKEN = "o";
    private static final String OBJ_GROUP_TOKEN = "g";
    private static final String OBJ_MATERIAL_TOKEN = "mtllib";
    private static final String OBJ_USE_MATERIAL_TOKEN = "usemtl";

    private static final String[] SUPPORTED_EXTENSIONS = {"obj"};

    @Override
    protected boolean supportsExtension(Path path) {
        if (path == null) return false;
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".obj");
    }

    @Override
    protected Model parseContent(String content) throws ModelLoadingException {
        Model model = new Model();
        int lineNumber = 0;

        try (Scanner scanner = new Scanner(content)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                lineNumber++;

                if (line.isEmpty() || line.startsWith(OBJ_COMMENT_TOKEN)) {
                    continue;
                }

                ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));
                if (wordsInLine.isEmpty()) {
                    continue;
                }

                String token = wordsInLine.get(0);
                wordsInLine.remove(0);

                try {
                    switch (token) {
                        case OBJ_VERTEX_TOKEN -> model.vertices.add(parseVertex(wordsInLine, lineNumber));
                        case OBJ_TEXTURE_TOKEN -> model.textureVertices.add(parseTextureVertex(wordsInLine, lineNumber));
                        case OBJ_NORMAL_TOKEN -> model.normals.add(parseNormal(wordsInLine, lineNumber));
                        case OBJ_FACE_TOKEN -> model.polygons.add(parseFace(wordsInLine, lineNumber));
                        case OBJ_OBJECT_TOKEN, OBJ_GROUP_TOKEN, OBJ_MATERIAL_TOKEN, OBJ_USE_MATERIAL_TOKEN -> {
                        }
                        default -> {
                        }
                    }
                } catch (ModelLoadingException e) {
                    throw e; // Пробрасываем дальше с правильным номером строки
                } catch (Exception e) {
                    throw new ModelLoadingException("Неожиданная ошибка при парсинге строки", lineNumber, e);
                }
            }
        }

        return model;
    }

    private Vector3f parseVertex(ArrayList<String> wordsInLine, int lineNumber) throws ModelLoadingException {
        if (wordsInLine.size() < 3) {
            throw new ModelLoadingException("Недостаточно координат для вершины (требуется минимум 3)", lineNumber);
        }

        try {
            float x = Float.parseFloat(wordsInLine.get(0));
            float y = Float.parseFloat(wordsInLine.get(1));
            float z = Float.parseFloat(wordsInLine.get(2));

            if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
                throw new ModelLoadingException("Координаты вершины содержат некорректные значения (NaN)", lineNumber);
            }
            if (Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
                throw new ModelLoadingException("Координаты вершины содержат бесконечные значения", lineNumber);
            }

            return new Vector3f(x, y, z);

        } catch (NumberFormatException e) {
            throw new ModelLoadingException("Некорректный формат чисел в координатах вершины", lineNumber, e);
        }
    }

    private Vector2f parseTextureVertex(ArrayList<String> wordsInLine, int lineNumber) throws ModelLoadingException {
        if (wordsInLine.size() < 2) {
            throw new ModelLoadingException("Недостаточно координат для текстурной вершины (требуется минимум 2)", lineNumber);
        }

        try {
            float u = Float.parseFloat(wordsInLine.get(0));
            float v = wordsInLine.size() > 1 ? Float.parseFloat(wordsInLine.get(1)) : 0.0f;

            if (Float.isNaN(u) || Float.isNaN(v)) {
                throw new ModelLoadingException("Текстурные координаты содержат некорректные значения (NaN)", lineNumber);
            }
            if (Float.isInfinite(u) || Float.isInfinite(v)) {
                throw new ModelLoadingException("Текстурные координаты содержат бесконечные значения", lineNumber);
            }

            return new Vector2f(u, v);

        } catch (NumberFormatException e) {
            throw new ModelLoadingException("Некорректный формат чисел в текстурных координатах", lineNumber, e);
        }
    }

    private Vector3f parseNormal(ArrayList<String> wordsInLine, int lineNumber) throws ModelLoadingException {
        if (wordsInLine.size() < 3) {
            throw new ModelLoadingException("Недостаточно компонент для нормали (требуется 3)", lineNumber);
        }

        try {
            float x = Float.parseFloat(wordsInLine.get(0));
            float y = Float.parseFloat(wordsInLine.get(1));
            float z = Float.parseFloat(wordsInLine.get(2));

            if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
                throw new ModelLoadingException("Компоненты нормали содержат некорректные значения (NaN)", lineNumber);
            }
            if (Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
                throw new ModelLoadingException("Компоненты нормали содержат бесконечные значения", lineNumber);
            }

            return new Vector3f(x, y, z);

        } catch (NumberFormatException e) {
            throw new ModelLoadingException("Некорректный формат чисел в компонентах нормали", lineNumber, e);
        }
    }

    private Polygon parseFace(ArrayList<String> wordsInLine, int lineNumber) throws ModelLoadingException {
        if (wordsInLine.size() < 3) {
            throw new ModelLoadingException("Полигон должен содержать минимум 3 вершины", lineNumber);
        }

        ArrayList<Integer> vertexIndices = new ArrayList<>();
        ArrayList<Integer> textureIndices = new ArrayList<>();
        ArrayList<Integer> normalIndices = new ArrayList<>();

        for (String word : wordsInLine) {
            parseFaceVertex(word, vertexIndices, textureIndices, normalIndices, lineNumber);
        }

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(vertexIndices);

        if (!textureIndices.isEmpty()) {
            polygon.setTextureVertexIndices(textureIndices);
        }
        if (!normalIndices.isEmpty()) {
            polygon.setNormalIndices(normalIndices);
        }

        return polygon;
    }

    private void parseFaceVertex(String word, ArrayList<Integer> vertexIndices,
                                ArrayList<Integer> textureIndices, ArrayList<Integer> normalIndices,
                                int lineNumber) throws ModelLoadingException {
        String[] parts = word.split("/");

        try {
            if (parts.length == 0 || parts[0].isEmpty()) {
                throw new ModelLoadingException("Отсутствует индекс вершины в определении полигона", lineNumber);
            }

            int vertexIndex = Integer.parseInt(parts[0]) - 1; // OBJ использует 1-based индексы
            vertexIndices.add(vertexIndex);

            if (parts.length > 1 && !parts[1].isEmpty()) {
                int textureIndex = Integer.parseInt(parts[1]) - 1;
                textureIndices.add(textureIndex);
            }

            if (parts.length > 2 && !parts[2].isEmpty()) {
                int normalIndex = Integer.parseInt(parts[2]) - 1;
                normalIndices.add(normalIndex);
            }

        } catch (NumberFormatException e) {
            throw new ModelLoadingException("Некорректный формат индексов в определении полигона", lineNumber, e);
        }
    }

    @Override
    public String[] getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS.clone();
    }
}
