package com.cgvsu.io;

import com.cgvsu.model.Model;
import java.io.IOException;
import java.nio.file.Path;

public abstract class ModelLoader {

    public final Model load(Path path) throws ModelLoadingException {
        validatePath(path);
        String content = readFileContent(path);
        Model model = parseContent(content);
        validateModel(model);
        postProcessModel(model);
        return model;
    }

    public final Model loadFromContent(String content) throws ModelLoadingException {
        Model model = parseContent(content);
        validateModel(model);
        postProcessModel(model);
        return model;
    }

    protected void validatePath(Path path) throws ModelLoadingException {
        if (path == null) {
            throw new ModelLoadingException("Путь к файлу не может быть null");
        }
        if (!supportsExtension(path)) {
            throw new ModelLoadingException("Неподдерживаемое расширение файла: " + path.toString());
        }
    }

    protected abstract boolean supportsExtension(Path path);

    protected String readFileContent(Path path) throws ModelLoadingException {
        try {
            return java.nio.file.Files.readString(path);
        } catch (IOException e) {
            throw new ModelLoadingException("Ошибка чтения файла: " + path.toString(), e);
        }
    }

    protected abstract Model parseContent(String content) throws ModelLoadingException;

    protected void validateModel(Model model) throws ModelLoadingException {
        if (model == null) {
            throw new ModelLoadingException("Модель не была создана");
        }
        if (model.vertices == null || model.vertices.isEmpty()) {
            throw new ModelLoadingException("Модель не содержит вершин");
        }
        if (model.polygons == null) {
            throw new ModelLoadingException("Модель не содержит полигонов");
        }

        for (int i = 0; i < model.polygons.size(); i++) {
            validatePolygon(model, i);
        }
    }

    protected void validatePolygon(Model model, int polygonIndex) throws ModelLoadingException {
        var polygon = model.polygons.get(polygonIndex);
        var vertexIndices = polygon.getVertexIndices();

        if (vertexIndices == null || vertexIndices.size() < 3) {
            throw new ModelLoadingException(
                String.format("Полигон %d содержит менее 3 вершин", polygonIndex));
        }

        for (int vertexIndex : vertexIndices) {
            if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) {
                throw new ModelLoadingException(
                    String.format("Полигон %d ссылается на несуществующую вершину %d", polygonIndex, vertexIndex));
            }
        }

        var textureIndices = polygon.getTextureVertexIndices();
        if (textureIndices != null && !textureIndices.isEmpty() &&
            model.textureVertices != null) {
            for (int textureIndex : textureIndices) {
                if (textureIndex < 0 || textureIndex >= model.textureVertices.size()) {
                    throw new ModelLoadingException(
                        String.format("Полигон %d ссылается на несуществующие текстурные координаты %d", polygonIndex, textureIndex));
                }
            }
        }

        var normalIndices = polygon.getNormalIndices();
        if (normalIndices != null && !normalIndices.isEmpty() &&
            model.normals != null) {
            for (int normalIndex : normalIndices) {
                if (normalIndex < 0 || normalIndex >= model.normals.size()) {
                    throw new ModelLoadingException(
                        String.format("Полигон %d ссылается на несуществующую нормаль %d", polygonIndex, normalIndex));
                }
            }
        }
    }

    protected void postProcessModel(Model model) {
    }

    public abstract String[] getSupportedExtensions();
}
