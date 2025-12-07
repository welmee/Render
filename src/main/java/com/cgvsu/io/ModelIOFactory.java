package com.cgvsu.io;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ModelIOFactory {

    private static final Map<String, Class<? extends ModelLoader>> loaders = new HashMap<>();
    private static final Map<String, Class<? extends ModelSaver>> savers = new HashMap<>();

    static {
        registerLoader("obj", ObjLoader.class);
        registerSaver("obj", ObjSaver.class);
    }

    public static void registerLoader(String extension, Class<? extends ModelLoader> loaderClass) {
        loaders.put(extension.toLowerCase(), loaderClass);
    }

    public static void registerSaver(String extension, Class<? extends ModelSaver> saverClass) {
        savers.put(extension.toLowerCase(), saverClass);
    }

    public static ModelLoader createLoader(Path path) throws ModelIOFactoryException {
        String extension = getExtension(path);
        Class<? extends ModelLoader> loaderClass = loaders.get(extension);

        if (loaderClass == null) {
            throw new ModelIOFactoryException("Не найден загрузчик для расширения: " + extension);
        }

        try {
            return loaderClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ModelIOFactoryException("Ошибка создания загрузчика: " + loaderClass.getSimpleName(), e);
        }
    }

    public static ModelSaver createSaver(Path path) throws ModelIOFactoryException {
        String extension = getExtension(path);
        Class<? extends ModelSaver> saverClass = savers.get(extension);

        if (saverClass == null) {
            throw new ModelIOFactoryException("Не найден сохранитель для расширения: " + extension);
        }

        try {
            return saverClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ModelIOFactoryException("Ошибка создания сохранителя: " + saverClass.getSimpleName(), e);
        }
    }

    public static boolean supportsLoading(Path path) {
        String extension = getExtension(path);
        return loaders.containsKey(extension);
    }

    public static boolean supportsSaving(Path path) {
        String extension = getExtension(path);
        return savers.containsKey(extension);
    }

    public static String[] getSupportedLoadExtensions() {
        return loaders.keySet().toArray(new String[0]);
    }

    public static String[] getSupportedSaveExtensions() {
        return savers.keySet().toArray(new String[0]);
    }

    private static String getExtension(Path path) {
        if (path == null) {
            return "";
        }

        String fileName = path.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
