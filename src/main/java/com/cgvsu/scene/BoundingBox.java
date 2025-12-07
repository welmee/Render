package com.cgvsu.scene;

import com.cgvsu.math.Vector3f;

public class BoundingBox {

    private Vector3f min;
    private Vector3f max;

    public BoundingBox() {
        this.min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        this.max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
    }

    public BoundingBox(Vector3f min, Vector3f max) {
        this.min = new Vector3f(min.x, min.y, min.z);
        this.max = new Vector3f(max.x, max.y, max.z);
    }

    public static BoundingBox fromPoints(Vector3f point1, Vector3f point2) {
        BoundingBox box = new BoundingBox();
        box.expandToInclude(point1);
        box.expandToInclude(point2);
        return box;
    }

    public Vector3f getMin() {
        return new Vector3f(min.x, min.y, min.z);
    }

    public Vector3f getMax() {
        return new Vector3f(max.x, max.y, max.z);
    }

    public Vector3f getCenter() {
        return new Vector3f(
            (min.x + max.x) * 0.5f,
            (min.y + max.y) * 0.5f,
            (min.z + max.z) * 0.5f
        );
    }

    public Vector3f getSize() {
        return new Vector3f(
            max.x - min.x,
            max.y - min.y,
            max.z - min.z
        );
    }

    public float getDiagonal() {
        Vector3f size = getSize();
        return (float) Math.sqrt(size.x * size.x + size.y * size.y + size.z * size.z);
    }

    public void expandToInclude(Vector3f point) {
        if (point.x < min.x) min.x = point.x;
        if (point.y < min.y) min.y = point.y;
        if (point.z < min.z) min.z = point.z;

        if (point.x > max.x) max.x = point.x;
        if (point.y > max.y) max.y = point.y;
        if (point.z > max.z) max.z = point.z;
    }

    public void expandToInclude(BoundingBox other) {
        expandToInclude(other.min);
        expandToInclude(other.max);
    }

    public boolean intersects(BoundingBox other) {
        return !(max.x < other.min.x || min.x > other.max.x ||
                 max.y < other.min.y || min.y > other.max.y ||
                 max.z < other.min.z || min.z > other.max.z);
    }

    public boolean contains(Vector3f point) {
        return point.x >= min.x && point.x <= max.x &&
               point.y >= min.y && point.y <= max.y &&
               point.z >= min.z && point.z <= max.z;
    }

    public boolean isEmpty() {
        return min.x > max.x || min.y > max.y || min.z > max.z;
    }

    public BoundingBox copy() {
        return new BoundingBox(min, max);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BoundingBox that = (BoundingBox) obj;
        return min.equals(that.min) && max.equals(that.max);
    }

    @Override
    public int hashCode() {
        return min.hashCode() * 31 + max.hashCode();
    }

    @Override
    public String toString() {
        return String.format("BoundingBox{min=%s, max=%s}", min, max);
    }
}
