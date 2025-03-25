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
import javafx.scene.input.Dragboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.TransferMode;
import javafx.scene.control.ListCell;
import javafx.scene.text.Font;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import com.snipshot.util.AlertUtil;

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
    private Button brushTool;
    private Button rectangleTool;
    private boolean isRectangleMode = false;
    private Rectangle previewRect;
    private boolean isBrushMode = false;
    private double brushRadius = 5.0;
    private MainModel model;
    private Button currentTool;
    private Button textTool;
    private boolean isTextMode = false;
    private Rectangle textPreviewRect;
    private boolean isRectangleFilled = true;
    private ImageView pastedImagePreview;
    private boolean isImagePreviewMode = false;
    private double previewX, previewY;
    private double previewStartX, previewStartY;
    private Rectangle imagePreviewBorder;
    private Button eraserTool;
    private boolean isEraserMode = false;

    
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
        
        // Add window close request handler
        stage.setOnCloseRequest(event -> {
            if (!model.isSaved()) {
                Optional<ButtonType> result = AlertUtil.showSaveConfirmationAlert();
                if (result.isPresent()) {
                    if (result.get() == ButtonType.OK) {
                        saveImage();
                        if (!model.isSaved()) {
                            event.consume(); // Prevent closing if save was cancelled
                        }
                    } else if (result.get() == ButtonType.CANCEL) {
                        event.consume(); // Prevent closing if user cancels
                    }
                } else {
                    event.consume(); // Prevent closing if dialog was closed without selection
                }
            }
        });
        
        // Add keyboard shortcut for transform mode
        scene.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.T) {
                if (pastedImagePreview != null) {
                    isImagePreviewMode = !isImagePreviewMode;
                    if (isImagePreviewMode) {
                        pastedImagePreview.setCursor(Cursor.MOVE);
                    } else {
                        pastedImagePreview.setCursor(Cursor.DEFAULT);
                    }
                }
            }
        });
        
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
        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem exitItem = new MenuItem("Exit");
        
        fileMenu.getItems().addAll(
            newItem, openItem, saveItem,
            new SeparatorMenuItem(),
            exitItem
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

        exitItem.setOnAction(e -> {
            if (!model.isSaved()) {
                Optional<ButtonType> result = AlertUtil.showSaveConfirmationAlert();
                if (result.isPresent()) {
                    if (result.get() == ButtonType.OK) {
                        saveImage();
                        if (model.isSaved()) {
                            stage.close();
                        }
                    } else if (result.get() == ButtonType.CANCEL) {
                        stage.close();
                    }
                }
            } else {
                stage.close();
            }
        });
    }

    private void setupToolbars() {
        topToolBar = new ToolBar();
        Button newBtn = new Button("New");
        Button openBtn = new Button("Open");
        Button saveBtn = new Button("Save");
        
        // Create Edit menu button with dropdown
        MenuButton editBtn = new MenuButton("Edit");
        MenuItem undoBtn = new MenuItem("Undo");
        MenuItem redoBtn = new MenuItem("Redo");
        MenuItem cutBtn = new MenuItem("Cut");
        MenuItem copyBtn = new MenuItem("Copy");
        MenuItem pasteBtn = new MenuItem("Paste");
        
        // Add action handlers
        undoBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Undo");
            alert.setHeaderText("Undo Action");
            alert.setContentText("This feature will be implemented in a future update.");
            alert.showAndWait();
        });
        
        redoBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Redo");
            alert.setHeaderText("Redo Action");
            alert.setContentText("This feature will be implemented in a future update.");
            alert.showAndWait();
        });
        
        cutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cut");
            alert.setHeaderText("Cut Action");
            alert.setContentText("This feature will be implemented in a future update.");
            alert.showAndWait();
        });
        
        copyBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Copy");
            alert.setHeaderText("Copy Action");
            alert.setContentText("This feature will be implemented in a future update.");
            alert.showAndWait();
        });
        
        pasteBtn.setOnAction(e -> {
            if (selectedLayer != null && !selectedLayer.isLocked()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                
                // Check for image content
                if (clipboard.hasImage()) {
                    Image image = clipboard.getImage();
                    Canvas canvas = selectedLayer.getCanvas();
                    
                    // Create preview image
                    pastedImagePreview = new ImageView(image);
                    pastedImagePreview.setPreserveRatio(true);
                    
                    // Create border rectangle
                    imagePreviewBorder = new Rectangle();
                    imagePreviewBorder.setFill(Color.TRANSPARENT);
                    imagePreviewBorder.setStroke(Color.BLUE);
                    imagePreviewBorder.getStrokeDashArray().addAll(5d);
                    
                    // Calculate initial position (center)
                    previewX = (canvas.getWidth() - image.getWidth()) / 2;
                    previewY = (canvas.getHeight() - image.getHeight()) / 2;
                    
                    // Set initial position
                    pastedImagePreview.setX(previewX);
                    pastedImagePreview.setY(previewY);
                    updateImagePreviewBorder();
                    
                    // Add preview and border to canvas container
                    canvasContainer.getChildren().addAll(imagePreviewBorder, pastedImagePreview);
                    pastedImagePreview.toFront();
                    
                    // Enable preview mode
                    isImagePreviewMode = true;
                    
                    // Add mouse event handlers for dragging
                    pastedImagePreview.setOnMousePressed(event -> {
                        if (isImagePreviewMode) {
                            previewStartX = event.getSceneX() - pastedImagePreview.getX();
                            previewStartY = event.getSceneY() - pastedImagePreview.getY();
                        }
                    });
                    
                    pastedImagePreview.setOnMouseDragged(event -> {
                        if (isImagePreviewMode) {
                            previewX = event.getSceneX() - previewStartX;
                            previewY = event.getSceneY() - previewStartY;
                            pastedImagePreview.setX(previewX);
                            pastedImagePreview.setY(previewY);
                            updateImagePreviewBorder();
                        }
                    });
                    
                    // Add click handler to place the image
                    pastedImagePreview.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) { // Double click to place
                            // Commit the image to the canvas
                            GraphicsContext gc = canvas.getGraphicsContext2D();
                            gc.drawImage(image, previewX, previewY);
                            
                            // Remove preview and border
                            canvasContainer.getChildren().removeAll(pastedImagePreview, imagePreviewBorder);
                            pastedImagePreview = null;
                            imagePreviewBorder = null;
                            isImagePreviewMode = false;
                            
                            // Ensure the canvas is on top for drawing
                            canvas.toFront();
                        }
                    });
                }
                // Check for text content
                else if (clipboard.hasString()) {
                    String text = clipboard.getString();
                    Canvas canvas = selectedLayer.getCanvas();
                    
                    // Create a text box at the center of the canvas
                    double x = canvas.getWidth() / 2;
                    double y = canvas.getHeight() / 2;
                    
                    // Create preview rectangle for text box
                    textPreviewRect = new Rectangle(
                        x + canvas.getLayoutX(),
                        y + canvas.getLayoutY(),
                        200, 100  // Default size for text box
                    );
                    textPreviewRect.setFill(Color.TRANSPARENT);
                    textPreviewRect.setStroke(Color.BLUE);
                    textPreviewRect.getStrokeDashArray().addAll(5d);
                    canvasContainer.getChildren().add(textPreviewRect);
                    
                    // Show text input dialog with pre-filled text
                    showTextInputDialog(canvas, x, y, 200, 100, text);
                    canvasContainer.getChildren().remove(textPreviewRect);
                    textPreviewRect = null;
                }
            }
        });
        
        editBtn.getItems().addAll(undoBtn, redoBtn, new SeparatorMenuItem(), cutBtn, copyBtn, pasteBtn);
        
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        
        newBtn.setOnAction(e -> createNewImage());
        openBtn.setOnAction(e -> openImage());
        saveBtn.setOnAction(e -> saveImage());
        
        topToolBar.getItems().addAll(
            newBtn, openBtn, saveBtn,
            editBtn,
            new Separator(),
            colorPicker
        );
        
        VBox topContainer = new VBox(topToolBar);
        mainLayout.setTop(topContainer);

        // Left Toolbar
        leftToolBar = new ToolBar();
        leftToolBar.setOrientation(Orientation.VERTICAL);

        pencilTool = ToolButton.createWithImage("/images/pencil.png", 16);
        brushTool = ToolButton.createWithImage("/images/brush.png", 16);
        eraserTool = ToolButton.createWithImage("/images/eraser.png", 16);
        Button selectTool = ToolButton.createWithImage("/images/select.png", 16);
        textTool = ToolButton.createWithImage("/images/text.png", 16);
        rectangleTool = ToolButton.createWithImage("/images/rectangle.png", 16);
        
        pencilTool.setOnAction(e -> enableDrawingMode());
        rectangleTool.setOnAction(e -> enableRectangleMode());
        brushTool.setOnAction(e -> enableBrushMode());
        textTool.setOnAction(e -> enableTextMode());
        eraserTool.setOnAction(e -> enableEraserMode());
        
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
        
        // Add layer selection listener
        layersList.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedLayer = newValue;
                updatePropertiesPanel();
                updateLayerSelection(oldValue, newValue);
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

    private void setupLayerDragAndDrop() {
        layersList.setCellFactory(lv -> new ListCell<Layer>() {
            @Override
            protected void updateItem(Layer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());
                    setGraphic(null);
                }
            }
        });

        layersList.setOnDragDetected(event -> {
            if (selectedLayer != null) {
                Dragboard db = layersList.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedLayer.getName());
                db.setContent(content);
                event.consume();
            }
        });

        layersList.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        layersList.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasString()) {
                Layer draggedLayer = selectedLayer;
                int draggedIndex = layersList.getItems().indexOf(draggedLayer);
                int dropIndex = layersList.getItems().indexOf(
                    layersList.getItems().get(layersList.getItems().size() - 1)
                );
                
                if (draggedIndex != dropIndex) {
                    // Remove from old position
                    layersList.getItems().remove(draggedIndex);
                    canvasContainer.getChildren().remove(draggedLayer.getCanvas());
                    
                    // Add to new position
                    layersList.getItems().add(dropIndex, draggedLayer);
                    canvasContainer.getChildren().add(dropIndex, draggedLayer.getCanvas());
                    
                    success = true;
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void createNewLayer() {
        if (canvasContainer != null && !canvasContainer.getChildren().isEmpty()) {
            Layer newLayer = new Layer("Layer " + (layersList.getItems().size() + 1),
                    canvasContainer.getWidth(), canvasContainer.getHeight());
            
            // Add to top of list and canvas container
            layersList.getItems().add(0, newLayer);
            canvasContainer.getChildren().add(0, newLayer.getCanvas());
            
            // Set the position of the canvas
            AnchorPane.setLeftAnchor(newLayer.getCanvas(), 0.0);
            AnchorPane.setTopAnchor(newLayer.getCanvas(), 0.0);
            
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
        if (!model.isSaved()) {
            Optional<ButtonType> result = AlertUtil.showSaveConfirmationAlert();
            if (result.isPresent()) {
                if (result.get() == ButtonType.OK) {
                    saveImage();
                    if (!model.isSaved()) {
                        return;
                    }
                } else if (result.get() == ButtonType.CANCEL) {
                    return;
                }
            }
        }
        
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
            
            model.setSaved(false);
        });
    }

    private void openImage() {
        if (!model.isSaved()) {
            Optional<ButtonType> result = AlertUtil.showSaveConfirmationAlert();
            if (result.isPresent()) {
                if (result.get() == ButtonType.OK) {
                    saveImage();
                    if (!model.isSaved()) {
                        return;
                    }
                } else if (result.get() == ButtonType.CANCEL) {
                    return;
                }
            }
        }
        
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
                
                model.setSaved(true);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not load image");
                alert.setContentText("Failed to load the selected image file.");
                alert.showAndWait();
            }
        }
    }

    private void updateLayerSelection(Layer oldLayer, Layer newLayer) {
        if (oldLayer != null) {
            oldLayer.getCanvas().setMouseTransparent(true);
        }
        if (newLayer != null) {
            newLayer.getCanvas().setMouseTransparent(false);
        }
    }

    private void enableDrawingMode() {
        // Reset other modes
        isDrawing = false;
        isRectangleMode = false;
        isBrushMode = false;
        
        // Reset other tools' styles
        if (currentTool != null) {
            ToolButton.setActive(currentTool, false);
        }
        currentTool = pencilTool;
        ToolButton.setActive(pencilTool, true);
        
        // Set cursor
        canvasContainer.setCursor(Cursor.CROSSHAIR);
        
        if (selectedLayer != null) {
            Canvas canvas = selectedLayer.getCanvas();
            
            canvas.setOnMousePressed(e -> {
                if (!selectedLayer.isLocked()) {
                    isDrawing = true;
                    lastX = e.getX();
                    lastY = e.getY();
                }
            });
            
            canvas.setOnMouseDragged(e -> {
                if (isDrawing && !selectedLayer.isLocked()) {
                    model.drawPencilStroke(canvas, lastX, lastY, e.getX(), e.getY());
                    lastX = e.getX();
                    lastY = e.getY();
                    model.setSaved(false);
                }
            });
            
            canvas.setOnMouseReleased(e -> isDrawing = false);
        }
    }

    private void enableRectangleMode() {
        // Reset other modes
        isDrawing = false;
        isRectangleMode = true;
        isBrushMode = false;
        
        // Reset other tools' styles
        if (currentTool != null) {
            ToolButton.setActive(currentTool, false);
        }
        currentTool = rectangleTool;
        ToolButton.setActive(rectangleTool, true);
        
        // Set cursor
        canvasContainer.setCursor(Cursor.CROSSHAIR);
        
        // Update properties panel
        updateRectangleProperties();
        
        if (selectedLayer != null && !selectedLayer.isLocked()) {
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
                    previewRect.setFill(isRectangleFilled ? model.getCurrentColor() : Color.TRANSPARENT);
                    previewRect.setStroke(model.getCurrentColor());
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
                                      previewRect.getWidth(), previewRect.getHeight(),
                                      isRectangleFilled);
                    model.setSaved(false);
                    
                    canvasContainer.getChildren().remove(previewRect);
                    previewRect = null;
                }
            });
        }
    }

    private void updateRectangleProperties() {
        propertiesContent.getChildren().clear();
        
        if (isRectangleMode) {
            CheckBox fillCheck = new CheckBox("Fill Rectangle");
            fillCheck.setSelected(isRectangleFilled);
            fillCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                isRectangleFilled = newVal;
                if (previewRect != null) {
                    previewRect.setFill(isRectangleFilled ? model.getCurrentColor() : Color.TRANSPARENT);
                }
            });
            
            propertiesContent.getChildren().addAll(fillCheck);
        }
    }

    private void enableBrushMode() {
        // Reset other modes
        isDrawing = false;
        isRectangleMode = false;
        isBrushMode = true;
        
        // Reset other tools' styles
        if (currentTool != null) {
            ToolButton.setActive(currentTool, false);
        }
        currentTool = brushTool;
        ToolButton.setActive(brushTool, true);
        
        // Set cursor
        canvasContainer.setCursor(Cursor.CROSSHAIR);
        
        if (selectedLayer != null) {
            Canvas canvas = selectedLayer.getCanvas();
            
            canvas.setOnMousePressed(e -> {
                if (isBrushMode && !selectedLayer.isLocked()) {
                    model.drawBrushStroke(canvas, e.getX(), e.getY());
                    model.setSaved(false);
                }
            });
            
            canvas.setOnMouseDragged(e -> {
                if (isBrushMode && !selectedLayer.isLocked()) {
                    model.continueBrushStroke(canvas, e.getX(), e.getY());
                    model.setSaved(false);
                }
            });
            
            updateBrushProperties();
        }
    }

    private void updateBrushProperties() {
        propertiesContent.getChildren().clear();
        
        if (isBrushMode) {
            // Brush size control
            Label sizeLabel = new Label("Brush Size:");
            Slider sizeSlider = new Slider(1, 50, model.getBrushRadius());
            sizeSlider.setShowTickLabels(true);
            sizeSlider.setShowTickMarks(true);
            
            // Brush shape selector
            Label shapeLabel = new Label("Brush Shape:");
            ComboBox<MainModel.BrushShape> shapeBox = new ComboBox<>();
            shapeBox.getItems().addAll(MainModel.BrushShape.values());
            shapeBox.setValue(model.getBrushShape());
            shapeBox.setCellFactory(lv -> new ListCell<MainModel.BrushShape>() {
                @Override
                protected void updateItem(MainModel.BrushShape item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getDisplayName());
                }
            });
            
            // Add listeners
            sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
                model.setBrushRadius(newVal.doubleValue()));
            
            shapeBox.valueProperty().addListener((obs, oldVal, newVal) -> 
                model.setBrushShape(newVal));
            
            propertiesContent.getChildren().addAll(
                sizeLabel, sizeSlider,
                new Separator(),
                shapeLabel, shapeBox
            );
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
                
                model.setSaved(true);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not save image");
                alert.setContentText("Failed to save the image file.");
                alert.showAndWait();
            }
        }
    }

    private void enableTextMode() {
        // Reset other modes
        isDrawing = false;
        isRectangleMode = false;
        isBrushMode = false;
        isTextMode = true;
        
        // Reset other tools' styles
        if (currentTool != null) {
            ToolButton.setActive(currentTool, false);
        }
        currentTool = textTool;
        ToolButton.setActive(textTool, true);
        
        // Set cursor
        canvasContainer.setCursor(Cursor.TEXT);
        
        if (selectedLayer != null && !selectedLayer.isLocked()) {
            Canvas canvas = selectedLayer.getCanvas();
            
            canvas.setOnMousePressed(e -> {
                if (isTextMode) {
                    lastX = e.getX();
                    lastY = e.getY();
                    
                    // Create preview rectangle
                    textPreviewRect = new Rectangle(
                        lastX + canvas.getLayoutX(),
                        lastY + canvas.getLayoutY(),
                        0, 0
                    );
                    textPreviewRect.setFill(Color.TRANSPARENT);
                    textPreviewRect.setStroke(Color.BLUE);
                    textPreviewRect.getStrokeDashArray().addAll(5d);
                    canvasContainer.getChildren().add(textPreviewRect);
                }
            });
            
            canvas.setOnMouseDragged(e -> {
                if (isTextMode && textPreviewRect != null) {
                    double width = e.getX() - lastX;
                    double height = e.getY() - lastY;
                    
                    // Update preview rectangle
                    textPreviewRect.setX((width > 0 ? lastX : e.getX()) + canvas.getLayoutX());
                    textPreviewRect.setY((height > 0 ? lastY : e.getY()) + canvas.getLayoutY());
                    textPreviewRect.setWidth(Math.abs(width));
                    textPreviewRect.setHeight(Math.abs(height));
                }
            });
            
            canvas.setOnMouseReleased(e -> {
                if (isTextMode && textPreviewRect != null) {
                    showTextInputDialog(canvas, lastX, lastY, 
                                     textPreviewRect.getWidth(), 
                                     textPreviewRect.getHeight());
                    canvasContainer.getChildren().remove(textPreviewRect);
                    textPreviewRect = null;
                }
            });
        }
    }
    
    private void showTextInputDialog(Canvas canvas, double x, double y, double width, double height) {
        showTextInputDialog(canvas, x, y, width, height, "");
    }
    
    private void showTextInputDialog(Canvas canvas, double x, double y, double width, double height, String initialText) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Enter Text");
        dialog.setHeaderText("Type your text below:");
        
        // Create text area for input
        TextArea textArea = new TextArea(initialText);
        textArea.setPrefRowCount(3);
        textArea.setWrapText(true);
        
        // Create font controls
        ComboBox<String> fontFamilyBox = new ComboBox<>();
        fontFamilyBox.getItems().addAll("Arial", "Times New Roman", "Courier New");
        fontFamilyBox.setValue("Arial");
        
        ComboBox<Integer> fontSizeBox = new ComboBox<>();
        fontSizeBox.getItems().addAll(8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 40, 48, 56, 64, 72);
        fontSizeBox.setValue(16);
        
        ColorPicker colorPicker = new ColorPicker(model.getCurrentColor());
        
        // Layout controls
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Font:"), fontFamilyBox,
            new Label("Size:"), fontSizeBox,
            new Label("Color:"), colorPicker,
            new Label("Text:"), textArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return textArea.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            if (!text.isEmpty()) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFont(Font.font(fontFamilyBox.getValue(), fontSizeBox.getValue()));
                gc.setFill(colorPicker.getValue());
                gc.fillText(text, x, y + fontSizeBox.getValue());
                model.setSaved(false);
            }
        });
    }

    private void updateImagePreviewBorder() {
        if (pastedImagePreview != null && imagePreviewBorder != null) {
            imagePreviewBorder.setX(pastedImagePreview.getX() - 2);
            imagePreviewBorder.setY(pastedImagePreview.getY() - 2);
            imagePreviewBorder.setWidth(pastedImagePreview.getFitWidth() + 4);
            imagePreviewBorder.setHeight(pastedImagePreview.getFitHeight() + 4);
        }
    }

    private void enableEraserMode() {
        // Reset other modes
        isDrawing = false;
        isRectangleMode = false;
        isBrushMode = false;
        isTextMode = false;
        isEraserMode = true;

        // Reset other tools' styles
        if (currentTool != null) {
            ToolButton.setActive(currentTool, false);
        }
        currentTool = eraserTool;
        ToolButton.setActive(eraserTool, true);

        // Set cursor
        canvasContainer.setCursor(Cursor.CROSSHAIR);

        if (selectedLayer != null) {
            Canvas canvas = selectedLayer.getCanvas();

            canvas.setOnMousePressed(e -> {
                if (isEraserMode && !selectedLayer.isLocked()) {
                    model.drawEraserStroke(canvas, e.getX(), e.getY());
                    model.setSaved(false);
                }
            });

            canvas.setOnMouseDragged(e -> {
                if (isEraserMode && !selectedLayer.isLocked()) {
                    model.continueEraserStroke(canvas, e.getX(), e.getY());
                    model.setSaved(false);
                }
            });

            updateEraserProperties();
        }
    }

    private void updateEraserProperties() {
        propertiesContent.getChildren().clear();

        if (isEraserMode) {
            // Eraser size control
            Label sizeLabel = new Label("Eraser Size:");
            Slider sizeSlider = new Slider(1, 50, model.getEraserRadius());
            sizeSlider.setShowTickLabels(true);
            sizeSlider.setShowTickMarks(true);

            // Eraser hardness control
            Label hardnessLabel = new Label("Eraser Hardness:");
            Slider hardnessSlider = new Slider(0, 1, model.getEraserHardness());
            hardnessSlider.setShowTickLabels(true);
            hardnessSlider.setShowTickMarks(true);

            // Add listeners
            sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
                model.setEraserRadius(newVal.doubleValue()));

            hardnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
                model.setEraserHardness(newVal.doubleValue()));

            propertiesContent.getChildren().addAll(
                sizeLabel, sizeSlider,
                new Separator(),
                hardnessLabel, hardnessSlider
            );
        }
    }

    public void show() {
        stage.show();
    }
} 