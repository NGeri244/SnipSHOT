package com.snipshot.view;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import java.util.Optional;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.io.File;
import com.snipshot.model.Layer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Slider;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.HBox;
import javafx.scene.effect.BlendMode;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import com.snipshot.util.ToolButton;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.AnchorPane;
import com.snipshot.model.MainModel;

public class MainView {
    private Stage stage;
    private BorderPane mainLayout;
    private Canvas canvas;
    private ToolBar topToolBar;
    private ToolBar leftToolBar;
    private VBox rightPanel;
    private ListView<Layer> layersList;
    private VBox propertiesContent;
    private AnchorPane canvasContainer;
    private Layer selectedLayer;
    private boolean isDrawing = false;
    private double lastX, lastY;
    private Button pencilTool;
    private boolean isRectangleMode = false;
    private Rectangle previewRect;
    private boolean isBrushMode = false;
    private double brushRadius = 5.0;
    private MainModel model;

    
    public MainView() {
        model = new MainModel();
        stage = new Stage();
        setupLayout();
        setupMenuBar();
        setupToolbars();
        setupCanvas();
        setupRightPanel();
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        
        // Add the application icon
        Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
        stage.getIcons().add(icon);
        
        stage.setScene(scene);
        stage.setTitle("Snipshot");
    }

    private void setupLayout() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #2b2b2b;");
    }

    private void setupMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File Menu
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
            new MenuItem("New"),
            new MenuItem("Open"),
            new MenuItem("Save"),
            new SeparatorMenuItem(),
            new MenuItem("Exit")
        );

        // Edit Menu
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
            new MenuItem("Undo"),
            new MenuItem("Redo"),
            new SeparatorMenuItem(),
            new MenuItem("Cut"),
            new MenuItem("Copy"),
            new MenuItem("Paste")
        );

        // View Menu
        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(
            new MenuItem("Zoom In"),
            new MenuItem("Zoom Out"),
            new MenuItem("Fit to Screen")
        );

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);
        mainLayout.setTop(menuBar);
    }

    private void setupToolbars() {
        topToolBar = new ToolBar();
        Button newBtn = new Button("New");
        Button openBtn = new Button("Open");
        Button saveBtn = new Button("Save");
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        
        newBtn.setOnAction(e -> createNewImage());
        openBtn.setOnAction(e -> openImage());
        saveBtn.setOnAction(e -> saveImage());
        
        topToolBar.getItems().addAll(
            newBtn, openBtn, saveBtn,
            new Separator(),
            colorPicker
        );
        
        VBox topContainer = new VBox(topToolBar);
        mainLayout.setTop(topContainer);

        // Left Toolbar
        leftToolBar = new ToolBar();
        leftToolBar.setOrientation(Orientation.VERTICAL);

        pencilTool = ToolButton.createWithImage("/images/pencil.png", 16);
        Button brushTool = ToolButton.createWithImage("/images/brush.png", 16);
        Button eraserTool = ToolButton.createWithImage("/images/eraser.png", 16);
        Button selectTool = ToolButton.createWithImage("/images/select.png", 16);
        Button textTool = ToolButton.createWithImage("/images/text.png", 16);
        Button rectangleTool = ToolButton.createWithImage("/images/rectangle.png",16);
        
        pencilTool.setOnAction(e -> enableDrawingMode());
        rectangleTool.setOnAction(e -> enableRectangleMode());
        brushTool.setOnAction(e -> enableBrushMode());
        
        leftToolBar.getItems().addAll(
            selectTool, pencilTool, brushTool, eraserTool, textTool, rectangleTool
        );
        
        mainLayout.setLeft(leftToolBar);

        // Update color picker listener
        colorPicker.setOnAction(e -> model.setCurrentColor(colorPicker.getValue()));
    }

    private void setupCanvas() {
        canvasContainer = new AnchorPane();
        canvasContainer.setStyle("-fx-background: #3c3f41;");
        
        ScrollPane scrollPane = new ScrollPane(canvasContainer);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: #3c3f41;");
        
        mainLayout.setCenter(scrollPane);
    }

    private void setupRightPanel() {
        rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-background-color: #3c3f41; -fx-padding: 10;");
        rightPanel.setPrefWidth(250);

        // Layers Panel
        TitledPane layersPane = new TitledPane();
        layersPane.setText("Layers");
        VBox layersContent = new VBox(5);
        layersList = new ListView<>();
        setupLayersContextMenu();
        
        layersList.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedLayer = newValue;
                updatePropertiesPanel();
            }
        );
        
        layersContent.getChildren().add(layersList);
        layersPane.setContent(layersContent);

        // Properties Panel
        TitledPane propertiesPane = new TitledPane();
        propertiesPane.setText("Properties");
        propertiesContent = new VBox(5);
        propertiesPane.setContent(propertiesContent);

        rightPanel.getChildren().addAll(layersPane, propertiesPane);
        mainLayout.setRight(rightPanel);
    }

    private void setupLayersContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem newLayerItem = new MenuItem("New Layer");
        MenuItem duplicateLayer = new MenuItem("Duplicate Layer");
        MenuItem deleteLayer = new MenuItem("Delete Layer");
        MenuItem mergeDown = new MenuItem("Merge Down");
        
        newLayerItem.setOnAction(e -> createNewLayer());
        duplicateLayer.setOnAction(e -> duplicateSelectedLayer());
        deleteLayer.setOnAction(e -> deleteSelectedLayer());
        mergeDown.setOnAction(e -> mergeLayerDown());
        
        contextMenu.getItems().addAll(
            newLayerItem,
            duplicateLayer,
            deleteLayer,
            new SeparatorMenuItem(),
            mergeDown
        );
        
        layersList.setContextMenu(contextMenu);
    }

    private void createNewLayer() {
        if (canvasContainer != null && !canvasContainer.getChildren().isEmpty()) {
            Layer newLayer = new Layer("Layer " + (layersList.getItems().size() + 1),
                    canvasContainer.getWidth(), canvasContainer.getHeight());
            layersList.getItems().add(0, newLayer);
            
            // Set the position of the canvas
            AnchorPane.setLeftAnchor(newLayer.getCanvas(), 0.0);
            AnchorPane.setTopAnchor(newLayer.getCanvas(), 0.0);
            
            canvasContainer.getChildren().add(0, newLayer.getCanvas());
            layersList.getSelectionModel().select(newLayer);
        }
    }

    private void updatePropertiesPanel() {
        propertiesContent.getChildren().clear();
        
        if (selectedLayer != null) {
            // Opacity control
            Label opacityLabel = new Label("Opacity:");
            Slider opacitySlider = new Slider(0, 1, selectedLayer.getOpacity());
            opacitySlider.setShowTickLabels(true);
            opacitySlider.setShowTickMarks(true);
            
            // Blend mode control
            Label blendLabel = new Label("Blend Mode:");
            ComboBox<BlendMode> blendModeBox = new ComboBox<>();
            blendModeBox.getItems().addAll(BlendMode.values());
            blendModeBox.setValue(selectedLayer.getBlendMode());
            
            // Visibility toggle
            CheckBox visibilityCheck = new CheckBox("Visible");
            visibilityCheck.setSelected(selectedLayer.isVisible());
            
            // Lock layer toggle
            CheckBox lockCheck = new CheckBox("Lock Layer");
            lockCheck.setSelected(selectedLayer.isLocked());
            
            // Bind controls to layer properties
            opacitySlider.valueProperty().bindBidirectional(selectedLayer.opacityProperty());
            blendModeBox.valueProperty().bindBidirectional(selectedLayer.blendModeProperty());
            visibilityCheck.selectedProperty().bindBidirectional(selectedLayer.visibleProperty());
            lockCheck.selectedProperty().addListener((obs, old, newValue) -> 
                selectedLayer.setLocked(newValue));
            
            propertiesContent.getChildren().addAll(
                opacityLabel, opacitySlider,
                blendLabel, blendModeBox,
                new Separator(),
                visibilityCheck,
                lockCheck
            );
        }
    }

    private void duplicateSelectedLayer() {
        if (selectedLayer != null) {
            Layer duplicate = new Layer(
                selectedLayer.getName() + " copy",
                selectedLayer.getCanvas().getWidth(),
                selectedLayer.getCanvas().getHeight()
            );
            
            // Copy canvas content
            GraphicsContext gc = duplicate.getCanvas().getGraphicsContext2D();
            gc.drawImage(selectedLayer.getCanvas().snapshot(null, null), 0, 0);
            
            // Insert after selected layer
            int index = layersList.getItems().indexOf(selectedLayer);
            layersList.getItems().add(index, duplicate);
            canvasContainer.getChildren().add(index, duplicate.getCanvas());
            layersList.getSelectionModel().select(duplicate);
        }
    }

    private void deleteSelectedLayer() {
        if (selectedLayer != null && !selectedLayer.isLocked() && layersList.getItems().size() > 1) {
            int index = layersList.getItems().indexOf(selectedLayer);
            layersList.getItems().remove(selectedLayer);
            canvasContainer.getChildren().remove(selectedLayer.getCanvas());
            layersList.getSelectionModel().select(
                index < layersList.getItems().size() ? index : index - 1
            );
        }
    }

    private void mergeLayerDown() {
        if (selectedLayer != null && !selectedLayer.isLocked()) {
            int index = layersList.getItems().indexOf(selectedLayer);
            if (index < layersList.getItems().size() - 1) {
                Layer lowerLayer = layersList.getItems().get(index + 1);
                
                // Create merged canvas
                GraphicsContext gc = lowerLayer.getCanvas().getGraphicsContext2D();
                gc.setGlobalAlpha(selectedLayer.getOpacity());
                gc.setGlobalBlendMode(selectedLayer.getBlendMode());
                gc.drawImage(selectedLayer.getCanvas().snapshot(null, null), 0, 0);
                
                // Remove upper layer
                deleteSelectedLayer();
            }
        }
    }

    private void createNewImage() {
        NewImageDialog dialog = new NewImageDialog(stage);
        Optional<NewImageDialog.ImageProperties> result = dialog.showAndWait();
        
        result.ifPresent(props -> {
            canvasContainer.getChildren().clear();
            layersList.getItems().clear();
            
            Layer backgroundLayer = new Layer("Background", props.width, props.height);
            GraphicsContext gc = backgroundLayer.getCanvas().getGraphicsContext2D();
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, props.width, props.height);
            
            layersList.getItems().add(backgroundLayer);
            canvasContainer.getChildren().add(backgroundLayer.getCanvas());
            layersList.getSelectionModel().select(backgroundLayer);
        });
    }

    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                canvasContainer.getChildren().clear();
                layersList.getItems().clear();
                
                Layer backgroundLayer = new Layer("Background", image.getWidth(), image.getHeight());
                GraphicsContext gc = backgroundLayer.getCanvas().getGraphicsContext2D();
                gc.drawImage(image, 0, 0);
                
                layersList.getItems().add(backgroundLayer);
                canvasContainer.getChildren().add(backgroundLayer.getCanvas());
                layersList.getSelectionModel().select(backgroundLayer);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not load image");
                alert.setContentText("Failed to load the selected image file.");
                alert.showAndWait();
            }
        }
    }

    private void enableDrawingMode() {
        // Reset other tools' styles
        pencilTool.setStyle("-fx-background-color: #565a5c;");
        
        // Set cursor to crosshair
        canvasContainer.setCursor(Cursor.CROSSHAIR);
        
        // Add drawing event handlers to the selected layer's canvas
        if (selectedLayer != null) {
            Canvas canvas = selectedLayer.getCanvas();
            
            canvas.setOnMousePressed(e -> {
                isDrawing = true;
                lastX = e.getX();
                lastY = e.getY();
            });
            
            canvas.setOnMouseDragged(e -> {
                if (isDrawing) {
                    model.drawPencilStroke(canvas, lastX, lastY, e.getX(), e.getY());
                    lastX = e.getX();
                    lastY = e.getY();
                }
            });
            
            canvas.setOnMouseReleased(e -> isDrawing = false);
        }
    }

    private void enableRectangleMode() {
        // Reset other modes
        isDrawing = false;
        isRectangleMode = true;
        
        // Reset other tools' styles
        if (pencilTool != null) {
            pencilTool.setStyle("-fx-background-color: #565a5c;");
        }
        
        // Set cursor
        canvasContainer.setCursor(Cursor.CROSSHAIR);
        
        if (selectedLayer != null) {
            Canvas canvas = selectedLayer.getCanvas();
            
            canvas.setOnMousePressed(e -> {
                if (isRectangleMode) {
                    lastX = e.getX();
                    lastY = e.getY();
                    
                    // Create preview rectangle with correct positioning
                    previewRect = new Rectangle(
                        lastX + canvas.getLayoutX(),
                        lastY + canvas.getLayoutY(),
                        0, 0
                    );
                    previewRect.setFill(Color.TRANSPARENT);
                    previewRect.setStroke(((ColorPicker)topToolBar.getItems().get(4)).getValue());
                    previewRect.getStrokeDashArray().addAll(5d);
                    canvasContainer.getChildren().add(previewRect);
                }
            });
            
            canvas.setOnMouseDragged(e -> {
                if (isRectangleMode && previewRect != null) {
                    double width = e.getX() - lastX;
                    double height = e.getY() - lastY;
                    
                    // Update preview rectangle with offset
                    previewRect.setX((width > 0 ? lastX : e.getX()) + canvas.getLayoutX());
                    previewRect.setY((height > 0 ? lastY : e.getY()) + canvas.getLayoutY());
                    previewRect.setWidth(Math.abs(width));
                    previewRect.setHeight(Math.abs(height));
                }
            });
            
            canvas.setOnMouseReleased(e -> {
                if (isRectangleMode && previewRect != null) {
                    model.drawRectangle(canvas, lastX, lastY, 
                                      previewRect.getWidth(), previewRect.getHeight());
                    
                    canvasContainer.getChildren().remove(previewRect);
                    previewRect = null;
                }
            });
        }
    }

    private void enableBrushMode() {
        // Reset other modes
        isDrawing = false;
        isRectangleMode = false;
        isBrushMode = true;
        
        // Reset other tools' styles
        if (pencilTool != null) {
            pencilTool.setStyle("-fx-background-color: #565a5c;");
        }
        
        // Set cursor
        canvasContainer.setCursor(Cursor.CROSSHAIR);
        
        if (selectedLayer != null) {
            Canvas canvas = selectedLayer.getCanvas();
            
            canvas.setOnMousePressed(e -> {
                if (isBrushMode) {
                    model.drawBrushStroke(canvas, e.getX(), e.getY());
                }
            });
            
            canvas.setOnMouseDragged(e -> {
                if (isBrushMode) {
                    model.continueBrushStroke(canvas, e.getX(), e.getY());
                }
            });
            
            updateBrushProperties();
        }
    }

    private void updateBrushProperties() {
        propertiesContent.getChildren().clear();
        
        if (isBrushMode) {
            Label sizeLabel = new Label("Brush Size:");
            Slider sizeSlider = new Slider(1, 50, model.getBrushRadius());
            sizeSlider.setShowTickLabels(true);
            sizeSlider.setShowTickMarks(true);
            
            sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
                model.setBrushRadius(newVal.doubleValue()));
            
            propertiesContent.getChildren().addAll(sizeLabel, sizeSlider);
        }
    }

    private void saveImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("PNG files (*.png)", "*.png"),
            new ExtensionFilter("JPEG files (*.jpg)", "*.jpg")
        );
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                // Create a new canvas with the same size
                Canvas saveCanvas = new Canvas(
                    canvasContainer.getWidth(),
                    canvasContainer.getHeight()
                );
                GraphicsContext gc = saveCanvas.getGraphicsContext2D();
                
                // Draw all visible layers from bottom to top
                for (int i = canvasContainer.getChildren().size() - 1; i >= 0; i--) {
                    Canvas layerCanvas = ((Canvas)canvasContainer.getChildren().get(i));
                    gc.setGlobalAlpha(((Layer)layersList.getItems().get(i)).getOpacity());
                    gc.drawImage(layerCanvas.snapshot(null, null), 0, 0);
                }
                
                // Save the final image
                WritableImage writableImage = saveCanvas.snapshot(null, null);
                String fileName = file.getName().toLowerCase();
                String format = fileName.endsWith(".png") ? "png" : "jpg";
                
                ImageIO.write(
                    SwingFXUtils.fromFXImage(writableImage, null),
                    format,
                    file
                );
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not save image");
                alert.setContentText("Failed to save the image file.");
                alert.showAndWait();
            }
        }
    }

    public void show() {
        stage.show();
    }
} 