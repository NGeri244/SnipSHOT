package com.snipshot.model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class MainModel {
    private double brushRadius = 5.0;
    private double lastX, lastY;
    private Color currentColor = Color.BLACK;
    
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
    
    public void drawRectangle(Canvas canvas, double x, double y, double width, double height) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(currentColor);
        gc.setLineWidth(2);
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
} 