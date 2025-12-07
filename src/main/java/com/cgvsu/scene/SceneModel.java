package com.cgvsu.scene;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;

public class SceneModel extends SceneElement {

    private Model model;
    private Vector3f position;
    private Vector3f rotation; // в радианах
    private Vector3f scale;
    private BoundingBox cachedBoundingBox;
    private boolean boundingBoxDirty = true;

    public SceneModel(String id, Model model) {
        super(id);
        this.model = model;
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public SceneModel(String id, String name, Model model) {
        super(id, name);
        this.model = model;
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
        this.boundingBoxDirty = true;
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f(position.x, position.y, position.z);
    }

    @Override
    public void setPosition(Vector3f position) {
        this.position = new Vector3f(position.x, position.y, position.z);
    }

    public Vector3f getRotation() {
        return new Vector3f(rotation.x, rotation.y, rotation.z);
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = new Vector3f(rotation.x, rotation.y, rotation.z);
    }

    public Vector3f getScale() {
        return new Vector3f(scale.x, scale.y, scale.z);
    }

    public void setScale(Vector3f scale) {
        this.scale = new Vector3f(scale.x, scale.y, scale.z);
        this.boundingBoxDirty = true;
    }

    public void translate(Vector3f delta) {
        position.x += delta.x;
        position.y += delta.y;
        position.z += delta.z;
    }

    public void rotate(Vector3f deltaRotation) {
        rotation.x += deltaRotation.x;
        rotation.y += deltaRotation.y;
        rotation.z += deltaRotation.z;
    }

    public void scaleBy(float scaleFactor) {
        scale.x *= scaleFactor;
        scale.y *= scaleFactor;
        scale.z *= scaleFactor;
        this.boundingBoxDirty = true;
    }

    public void scaleBy(Vector3f scaleFactors) {
        scale.x *= scaleFactors.x;
        scale.y *= scaleFactors.y;
        scale.z *= scaleFactors.z;
        this.boundingBoxDirty = true;
    }

    public void resetTransform() {
        position = new Vector3f(0, 0, 0);
        rotation = new Vector3f(0, 0, 0);
        scale = new Vector3f(1, 1, 1);
        boundingBoxDirty = true;
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (boundingBoxDirty || cachedBoundingBox == null) {
            cachedBoundingBox = calculateBoundingBox();
            boundingBoxDirty = false;
        }
        return cachedBoundingBox.copy();
    }

    private BoundingBox calculateBoundingBox() {
        if (model == null || model.vertices == null || model.vertices.isEmpty()) {
            return new BoundingBox();
        }

        BoundingBox box = new BoundingBox();

        for (Vector3f vertex : model.vertices) {
            Vector3f transformedVertex = new Vector3f(
                vertex.x * scale.x,
                vertex.y * scale.y,
                vertex.z * scale.z
            );
            box.expandToInclude(transformedVertex);
        }

        return box;
    }

    public SceneModel copy() {
        SceneModel copy = new SceneModel(id + "_copy", name + " (копия)", model);
        copy.position = new Vector3f(position.x, position.y, position.z);
        copy.rotation = new Vector3f(rotation.x, rotation.y, rotation.z);
        copy.scale = new Vector3f(scale.x, scale.y, scale.z);
        return copy;
    }

    public boolean isValid() {
        return model != null &&
               model.vertices != null && !model.vertices.isEmpty() &&
               model.polygons != null && !model.polygons.isEmpty();
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public void dispose() {
        model = null;
        cachedBoundingBox = null;
    }

    @Override
    public String toString() {
        return String.format("SceneModel{id='%s', name='%s', vertices=%d, polygons=%d, position=%s}",
                           id, name,
                           model != null && model.vertices != null ? model.vertices.size() : 0,
                           model != null && model.polygons != null ? model.polygons.size() : 0,
                           position);
    }
}
