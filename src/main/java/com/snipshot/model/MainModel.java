package com.snipshot.model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class MainModel {
    public enum BrushShape {
        CIRCLE("Circle"),
        SQUARE("Square"),
        DIAMOND("Diamond"),
        TRIANGLE("Triangle");

        private final String displayName;

        BrushShape(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private double brushRadius = 5.0;
    private double lastX, lastY;
    private Color currentColor = Color.BLACK;
    private BrushShape currentBrushShape = BrushShape.CIRCLE;
    private double eraserRadius = 5.0;
    private BrushShape currentEraserShape = BrushShape.CIRCLE;
    private double eraserHardness = 1.0;
    private boolean isSaved = true;
    
    public void drawBrushStroke(Canvas canvas, double startX, double startY) {
        lastX = startX;
        lastY = startY;
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(currentColor);
        gc.fillOval(lastX - brushRadius, lastY - brushRadius, 
                    brushRadius * 2, brushRadius * 2);
    }
    
    public void continueBrushStroke(Canvas canvas, double x, double y) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(currentColor);
        
        // Draw circles along the path for smooth lines
        double distance = Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2));
        double steps = distance / (brushRadius / 2);
        
        for (double i = 0; i < steps; i++) {
            double t = i / steps;
            double cx = lastX + t * (x - lastX);
            double cy = lastY + t * (y - lastY);
            gc.fillOval(cx - brushRadius, cy - brushRadius, 
                        brushRadius * 2, brushRadius * 2);
        }
        
        lastX = x;
        lastY = y;
    }
    
    public void drawPencilStroke(Canvas canvas, double startX, double startY, double endX, double endY) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(currentColor);
        gc.setLineWidth(2);
        gc.strokeLine(startX, startY, endX, endY);
    }
    
    public void drawRectangle(Canvas canvas, double x, double y, double width, double height, boolean filled) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(currentColor);
        gc.setFill(currentColor);
        
        if (filled) {
            gc.fillRect(x, y, width, height);
        }
        gc.strokeRect(x, y, width, height);
    }
    
    public void setBrushRadius(double radius) {
        this.brushRadius = radius;
    }
    
    public double getBrushRadius() {
        return brushRadius;
    }
    
    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }
    
    public Color getCurrentColor() {
        return currentColor;
    }

    public BrushShape getBrushShape() {
        return currentBrushShape;
    }

    public void setBrushShape(BrushShape shape) {
        this.currentBrushShape = shape;
    }

    public void drawEraserStroke(Canvas canvas, double x, double y) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        drawBrushShape(gc, x, y, eraserRadius, currentEraserShape, eraserHardness);
    }

    public void continueEraserStroke(Canvas canvas, double x, double y) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        drawBrushShape(gc, x, y, eraserRadius, currentEraserShape, eraserHardness);
    }

    public double getEraserRadius() {
        return eraserRadius;
    }

    public void setEraserRadius(double radius) {
        this.eraserRadius = radius;
    }

    public BrushShape getEraserShape() {
        return currentEraserShape;
    }

    public void setEraserShape(BrushShape shape) {
        this.currentEraserShape = shape;
    }

    public double getEraserHardness() {
        return eraserHardness;
    }

    public void setEraserHardness(double hardness) {
        this.eraserHardness = Math.max(0.0, Math.min(1.0, hardness));
    }

    private void drawBrushShape(GraphicsContext gc, double x, double y, double radius, BrushShape shape, double hardness) {
        switch (shape) {
            case CIRCLE:
                gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                break;
            case SQUARE:
                gc.fillRect(x - radius, y - radius, radius * 2, radius * 2);
                break;
            case DIAMOND:
                gc.beginPath();
                gc.moveTo(x, y - radius);
                gc.lineTo(x + radius, y);
                gc.lineTo(x, y + radius);
                gc.lineTo(x - radius, y);
                gc.closePath();
                gc.fill();
                break;
            case TRIANGLE:
                gc.beginPath();
                gc.moveTo(x, y - radius);
                gc.lineTo(x + radius, y + radius);
                gc.lineTo(x - radius, y + radius);
                gc.closePath();
                gc.fill();
                break;
        }
    }

    public void setSaved(boolean saved) {
        this.isSaved = saved;
    }

    public boolean isSaved() {
        return isSaved;
    }
} 