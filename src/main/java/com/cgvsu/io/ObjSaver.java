package com.cgvsu.io;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.nio.file.Path;
import java.util.List;

public class ObjSaver extends ModelSaver {

    private static final String[] SUPPORTED_EXTENSIONS = {"obj"};

    @Override
    protected boolean supportsExtension(Path path) {
        if (path == null) return false;
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".obj");
    }

    @Override
    protected String generateContent(Model model, SaveSettings settings) throws ModelSavingException {
        StringBuilder sb = new StringBuilder();

        if (settings.includeComments) {
            sb.append("# Exported by Simple3DViewer\n");
            sb.append("# Vertices: ").append(model.vertices.size()).append("\n");
            sb.append("# Polygons: ").append(model.polygons.size()).append("\n");
            if (model.normals != null && !model.normals.isEmpty()) {
                sb.append("# Normals: ").append(model.normals.size()).append("\n");
            }
            if (model.textureVertices != null && !model.textureVertices.isEmpty()) {
                sb.append("# Texture coordinates: ").append(model.textureVertices.size()).append("\n");
            }
            sb.append("\n");
        }

        for (Vector3f vertex : model.vertices) {
            sb.append("v ")
              .append(formatFloat(vertex.x, settings.floatPrecision)).append(" ")
              .append(formatFloat(vertex.y, settings.floatPrecision)).append(" ")
              .append(formatFloat(vertex.z, settings.floatPrecision)).append("\n");
        }

        if (settings.includeTextureCoords && model.textureVertices != null && !model.textureVertices.isEmpty()) {
            sb.append("\n");
            for (Vector2f texCoord : model.textureVertices) {
                sb.append("vt ")
                  .append(formatFloat(texCoord.x, settings.floatPrecision)).append(" ")
                  .append(formatFloat(texCoord.y, settings.floatPrecision)).append("\n");
            }
        }

        if (settings.includeNormals && model.normals != null && !model.normals.isEmpty()) {
            sb.append("\n");
            for (Vector3f normal : model.normals) {
                sb.append("vn ")
                  .append(formatFloat(normal.x, settings.floatPrecision)).append(" ")
                  .append(formatFloat(normal.y, settings.floatPrecision)).append(" ")
                  .append(formatFloat(normal.z, settings.floatPrecision)).append("\n");
            }
        }

        sb.append("\n");
        for (Polygon polygon : model.polygons) {
            sb.append("f");
            List<Integer> vertexIndices = polygon.getVertexIndices();
            List<Integer> textureIndices = polygon.getTextureVertexIndices();
            List<Integer> normalIndices = polygon.getNormalIndices();

            for (int i = 0; i < vertexIndices.size(); i++) {
                sb.append(" ").append(vertexIndices.get(i) + 1); // OBJ использует 1-based индексы

                boolean hasTextures = settings.includeTextureCoords && textureIndices != null &&
                                    i < textureIndices.size() && !textureIndices.isEmpty();
                boolean hasNormals = settings.includeNormals && normalIndices != null &&
                                   i < normalIndices.size() && !normalIndices.isEmpty();

                if (hasTextures && hasNormals) {
                    sb.append("/").append(textureIndices.get(i) + 1).append("/").append(normalIndices.get(i) + 1);
                } else if (hasTextures) {
                    sb.append("/").append(textureIndices.get(i) + 1);
                } else if (hasNormals) {
                    sb.append("//").append(normalIndices.get(i) + 1);
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String formatFloat(float value, int precision) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return "0.0";
        }

        String format = String.format(java.util.Locale.US, "%." + precision + "f", value);
        if (precision == 0) {
            return format;
        }
        return format.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    @Override
    protected ObjSaveSettings getDefaultSettings() {
        return new ObjSaveSettings();
    }

    @Override
    public String[] getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS.clone();
    }

    public static class ObjSaveSettings extends SaveSettings {
        public String objectName = "model";

        public boolean includeGroups = false;

        public ObjSaveSettings() {
            super();
            this.includeNormals = true;
            this.includeTextureCoords = true;
            this.includeComments = true;
            this.floatPrecision = 6;
        }
    }
}
