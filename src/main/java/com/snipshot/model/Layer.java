package com.snipshot.model;

import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.BlendMode;

public class Layer {
    private Canvas canvas;
    private StringProperty name;
    private DoubleProperty opacity;
    private BooleanProperty visible;
    private ObjectProperty<BlendMode> blendMode;
    private boolean locked;

    public Layer(String name, double width, double height) {
        this.name = new SimpleStringProperty(name);
        this.opacity = new SimpleDoubleProperty(1.0); // 1.0 = 100% opacity
        this.visible = new SimpleBooleanProperty(true);
        this.blendMode = new SimpleObjectProperty<>(BlendMode.SRC_OVER);
        this.canvas = new Canvas(width, height);
        this.locked = false;
        
        // Bind canvas properties to layer properties
        canvas.opacityProperty().bind(opacity);
        canvas.visibleProperty().bind(visible);
        canvas.blendModeProperty().bind(blendMode);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public double getOpacity() {
        return opacity.get();
    }

    public void setOpacity(double value) {
        opacity.set(value);
    }

    public DoubleProperty opacityProperty() {
        return opacity;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(boolean value) {
        visible.set(value);
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public BlendMode getBlendMode() {
        return blendMode.get();
    }

    public void setBlendMode(BlendMode mode) {
        blendMode.set(mode);
    }

    public ObjectProperty<BlendMode> blendModeProperty() {
        return blendMode;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        return getName();
    }
} 