package com.cgvsu.scene;

import com.cgvsu.render_engine.Camera;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Scene {

    private String name;
    private final List<SceneElement> elements;
    private final Set<String> selectedElementIds;
    private Camera activeCamera;
    private final List<Camera> cameras;

    public Scene(String name) {
        this.name = name;
        this.elements = new CopyOnWriteArrayList<>();
        this.selectedElementIds = new HashSet<>();
        this.cameras = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean addElement(SceneElement element) {
        if (element == null) {
            return false;
        }

        if (getElementById(element.getId()) != null) {
            return false;
        }

        return elements.add(element);
    }

    public boolean removeElement(SceneElement element) {
        if (element == null) {
            return false;
        }

        selectedElementIds.remove(element.getId());
        boolean removed = elements.remove(element);

        if (removed) {
            element.dispose();
        }

        return removed;
    }

    public boolean removeElementById(String elementId) {
        SceneElement element = getElementById(elementId);
        return element != null && removeElement(element);
    }

    public SceneElement getElementById(String elementId) {
        if (elementId == null) {
            return null;
        }

        for (SceneElement element : elements) {
            if (elementId.equals(element.getId())) {
                return element;
            }
        }
        return null;
    }

    public List<SceneElement> getElements() {
        return new ArrayList<>(elements);
    }

    public List<SceneElement> getVisibleElements() {
        List<SceneElement> visible = new ArrayList<>();
        for (SceneElement element : elements) {
            if (element.isVisible()) {
                visible.add(element);
            }
        }
        return visible;
    }

    @SuppressWarnings("unchecked")
    public <T extends SceneElement> List<T> getElementsOfType(Class<T> elementClass) {
        List<T> result = new ArrayList<>();
        for (SceneElement element : elements) {
            if (elementClass.isInstance(element)) {
                result.add((T) element);
            }
        }
        return result;
    }

    public void selectElement(SceneElement element) {
        clearSelection();
        if (element != null) {
            element.setSelected(true);
            selectedElementIds.add(element.getId());
        }
    }

    public void selectElementById(String elementId) {
        SceneElement element = getElementById(elementId);
        selectElement(element);
    }

    public void addToSelection(SceneElement element) {
        if (element != null) {
            element.setSelected(true);
            selectedElementIds.add(element.getId());
        }
    }

    public void removeFromSelection(SceneElement element) {
        if (element != null) {
            element.setSelected(false);
            selectedElementIds.remove(element.getId());
        }
    }

    public void clearSelection() {
        for (String elementId : selectedElementIds) {
            SceneElement element = getElementById(elementId);
            if (element != null) {
                element.setSelected(false);
            }
        }
        selectedElementIds.clear();
    }

    public void selectAll() {
        for (SceneElement element : elements) {
            element.setSelected(true);
            selectedElementIds.add(element.getId());
        }
    }

    public List<SceneElement> getSelectedElements() {
        List<SceneElement> selected = new ArrayList<>();
        for (String elementId : selectedElementIds) {
            SceneElement element = getElementById(elementId);
            if (element != null) {
                selected.add(element);
            }
        }
        return selected;
    }

    public Set<String> getSelectedElementIds() {
        return new HashSet<>(selectedElementIds);
    }

    public boolean isElementSelected(SceneElement element) {
        return element != null && selectedElementIds.contains(element.getId());
    }

    public void addCamera(Camera camera) {
        if (camera != null && !cameras.contains(camera)) {
            cameras.add(camera);
            if (activeCamera == null) {
                activeCamera = camera;
            }
        }
    }

    public void removeCamera(Camera camera) {
        cameras.remove(camera);
        if (activeCamera == camera) {
            activeCamera = cameras.isEmpty() ? null : cameras.get(0);
        }
    }

    public void setActiveCamera(Camera camera) {
        if (cameras.contains(camera)) {
            activeCamera = camera;
        }
    }

    public Camera getActiveCamera() {
        return activeCamera;
    }

    public List<Camera> getCameras() {
        return new ArrayList<>(cameras);
    }

    public void update(float deltaTime) {
        for (SceneElement element : elements) {
            element.update(deltaTime);
        }
    }

    public void clear() {
        for (SceneElement element : elements) {
            element.dispose();
        }
        elements.clear();
        selectedElementIds.clear();
        cameras.clear();
        activeCamera = null;
    }

    public int getElementCount() {
        return elements.size();
    }

    public int getSelectedElementCount() {
        return selectedElementIds.size();
    }

    @Override
    public String toString() {
        return String.format("Scene{name='%s', elements=%d, selected=%d, cameras=%d}",
                           name, elements.size(), selectedElementIds.size(), cameras.size());
    }
}
