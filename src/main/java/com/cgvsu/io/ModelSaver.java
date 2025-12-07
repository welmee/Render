package com.cgvsu.io;

import com.cgvsu.model.Model;
import java.io.IOException;
import java.nio.file.Path;

public abstract class ModelSaver {

    public final void save(Model model, Path path) throws ModelSavingException {
        validateModel(model);
        validatePath(path);
        String content = generateContent(model);
        writeFileContent(path, content);
    }

    public final void save(Model model, Path path, SaveSettings settings) throws ModelSavingException {
        validateModel(model);
        validatePath(path);
        String content = generateContent(model, settings);
        writeFileContent(path, content);
    }

    public final String generateContent(Model model) throws ModelSavingException {
        return generateContent(model, getDefaultSettings());
    }

    protected abstract String generateContent(Model model, SaveSettings settings) throws ModelSavingException;

    protected void validateModel(Model model) throws ModelSavingException {
        if (model == null) {
            throw new ModelSavingException("Модель не может быть null");
        }
        if (model.vertices == null || model.vertices.isEmpty()) {
            throw new ModelSavingException("Модель не содержит вершин");
        }
        if (model.polygons == null || model.polygons.isEmpty()) {
            throw new ModelSavingException("Модель не содержит полигонов");
        }
    }

    protected void validatePath(Path path) throws ModelSavingException {
        if (path == null) {
            throw new ModelSavingException("Путь к файлу не может быть null");
        }
        if (!supportsExtension(path)) {
            throw new ModelSavingException("Неподдерживаемое расширение файла: " + path.toString());
        }
    }

    protected abstract boolean supportsExtension(Path path);

    protected void writeFileContent(Path path, String content) throws ModelSavingException {
        try {
            java.nio.file.Files.writeString(path, content);
        } catch (IOException e) {
            throw new ModelSavingException("Ошибка записи файла: " + path.toString(), e);
        }
    }

    protected abstract SaveSettings getDefaultSettings();

    public abstract String[] getSupportedExtensions();

    public static abstract class SaveSettings {
        public boolean includeComments = true;

        public int floatPrecision = 6;

        public boolean includeNormals = true;

        public boolean includeTextureCoords = true;
    }
}
