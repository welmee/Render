package com.cgvsu.scene;

import com.cgvsu.math.Vector3f;

public abstract class SceneElement {

    protected String id;
    protected String name;
    protected boolean visible = true;
    protected boolean selected = false;

    public SceneElement(String id) {
        this.id = id;
        this.name = id; // По умолчанию имя совпадает с ID
    }

    public SceneElement(String id, String name) {
        this.id = id;
        this.name = name != null ? name : id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : this.id;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public abstract Vector3f getPosition();

    public abstract void setPosition(Vector3f position);

    public abstract BoundingBox getBoundingBox();

    public void update(float deltaTime) {
    }

    public void dispose() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SceneElement that = (SceneElement) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s', visible=%s, selected=%s}",
                           getClass().getSimpleName(), id, name, visible, selected);
    }
}
