import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class PathFinder extends Application {
    private Stage stage;
    private Scene scene;
    private Pane pane;
    private Image image = new Image("file:europa.gif");
    private Button newPlace;
    private String name;
    private int edgeTime;
    private final ListGraph listGraph = new ListGraph();
    private final ArrayList<Node> markedNodes = new ArrayList<>();
    private boolean changed;
    private Optional<ButtonType> close;
    private Node from;
    private Node to;
    private ArrayList<Node> allNodes = new ArrayList<>();

    @Override
    public void start(Stage stage){
        this.stage = stage;

        VBox root = new VBox();
        VBox vbox = new VBox();
        MenuBar menuBar = new MenuBar();
        menuBar.setId("menu");
        vbox.getChildren().add(menuBar);
        Menu fileMenu = new Menu("File");
        fileMenu.setId("menuFile");
        menuBar.getMenus().add(fileMenu);
        MenuItem newMap = new MenuItem("New Map");
        newMap.setId("menuNewMap");
        newMap.setOnAction(new NewMapHandler());
        MenuItem open = new MenuItem("Open");
        open.setId("menuOpenFile");
        open.setOnAction(new OpenHandler());
        MenuItem save = new MenuItem("Save");
        save.setId("menuSaveFile");
        save.setOnAction(new SaveHandler());
        MenuItem saveImage = new MenuItem("Save Image");
        saveImage.setId("menuSaveImage");
        saveImage.setOnAction(new SaveImageHandler());
        MenuItem exit = new MenuItem("Exit");
        exit.setId("menuExit");
        exit.setOnAction(new ExitHandler());
        fileMenu.getItems().addAll(newMap, open, save, saveImage, exit);

        HBox hbox = new HBox();
        Button findPath = new Button("Find Path");
        findPath.setId("btnFindPath");
        findPath.setOnAction(new FindPathHandler());
        Button showConnection = new Button("Show Connection");
        showConnection.setId("btnShowConnection");
        showConnection.setOnAction(new ShowHandler());
        newPlace = new Button("New Place");
        newPlace.setId("btnNewPlace");
        newPlace.setOnAction(new NewPlaceHandler());
        Button newConnection = new Button("New Connection");
        newConnection.setId("btnNewConnection");
        newConnection.setOnAction(new ConnectionHandler());
        Button changeConnection = new Button("Change Connection");
        changeConnection.setId("btnChangeConnection");
        changeConnection.setOnAction(new ChangeHandler());
        hbox.setPadding(new Insets(20));
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(findPath, showConnection, newPlace, newConnection, changeConnection);

        pane = new Pane();
        pane.setId("outputArea");
        root.getChildren().addAll(vbox, hbox, pane);

        scene = new Scene(root);
        stage.setTitle("PathFinder");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(new CloseHandler());
    }

    class SaveImageHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            try{
                WritableImage writableImage = pane.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            } catch (IOException e){
                Alert alert = new Alert(Alert.AlertType.ERROR, "IO-fel " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    class SaveHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            try{
                FileWriter outFile = new FileWriter("europa.graph");
                PrintWriter out = new PrintWriter(outFile);
                out.println("file:europa.gif");
                Set<Node> nodes = listGraph.getNodes();
                String nodesOnMap = "";
                for(Node node: nodes){
                    if(nodesOnMap.equals(""))
                        nodesOnMap += node.getName() + ";" + node.getCenterX() + ";" + node.getCenterY();
                    else
                        nodesOnMap += ";" + node.getName() + ";" + node.getCenterX() + ";" + node.getCenterY();
                }
                out.println(nodesOnMap);
                for(Node node: nodes){
                    Collection<Edge> edges = listGraph.getEdgesFrom(node);
                    for(Edge edge: edges){
                        out.println(node.getName() + ";" + edge.getDestination().toString() + ";" + edge.getName() + ";" + edge.getWeight());
                    }
                }
                out.close();
                outFile.close();
                changed = false;
            }
            catch(IOException e){
                System.err.println("Kan inte skriva filen " + e.getMessage());
            }
        }
    }

    class OpenHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            try {
                if(changed){
                    alertNotSaved();
                    if(close.isPresent() && close.get().equals(ButtonType.CANCEL))
                        return;
                }
                clearMap();

                FileReader reader = new FileReader("europa.graph");
                BufferedReader in = new BufferedReader(reader);
                String line;
                int lineNo = 0;
                while((line = in.readLine()) != null){
                    if(lineNo == 0){
                        image = new Image(line);
                        ImageView imageView = new ImageView(image);
                        pane.getChildren().add(imageView);
                        stage.sizeToScene();
                        lineNo++;
                    }
                    else if(lineNo == 1){
                        String[] tokens = line.split(";");
                        for(int i = 0; i < (tokens.length/3); i++){ //tokens.length/3 ger antalet städer på raden
                            String xcoordinate = tokens[1+i*3];
                            String ycoordinate = tokens[2+i*3];
                            Node node = new Node(Double.parseDouble(xcoordinate), Double.parseDouble(ycoordinate), 10, Color.BLUE, tokens[i*3]);
                            node.setOnMouseClicked(new ChooseNodeHandler());
                            node.setId(node.getName());
                            listGraph.add(node);
                            allNodes.add(node);
                            Label cityName = new Label(node.getName());
                            cityName.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
                            cityName.relocate(Double.parseDouble(xcoordinate), Double.parseDouble(ycoordinate));
                            cityName.setDisable(true);
                            pane.getChildren().addAll(node, cityName);
                        }
                        lineNo++;
                    }
                    else{
                        String[] tokens = line.split(";");
                        Set<Node> nodes = listGraph.getNodes();
                        for(Node node: nodes){
                            if(node.getName().equals(tokens[0]))
                                from = node;
                            else if(node.getName().equals(tokens[1]))
                                to = node;
                        }
                        if(listGraph.getEdgeBetween(from, to) == null){
                            String name = tokens[2];
                            int weight = Integer.parseInt(tokens[3]);
                            listGraph.connect(from, to, name, weight);
                            Line connect = new Line(from.getCenterX(), from.getCenterY(), to.getCenterX(), to.getCenterY());
                            connect.setStrokeWidth(3);
                            connect.setDisable(true);
                            pane.getChildren().add(connect);
                        }
                    }
                    changed = false;
                }

            } catch (FileNotFoundException e){
                System.err.println("File not found! " + e.getMessage());
            }catch (IOException e){
                System.err.println("IO-fel " + e.getMessage());
            }
        }
    }

    class ExitHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    class CloseHandler implements EventHandler<WindowEvent>{
        @Override
        public void handle(WindowEvent event) {
            if(changed){
                alertNotSaved();
                if(close.isPresent() && close.get().equals(ButtonType.CANCEL))
                    event.consume();
            }
            else
                stage.close();
        }
    }

    class NewMapHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            if(changed){
                alertNotSaved();
                if(close.isPresent() && close.get().equals(ButtonType.CANCEL))
                    return;
            }
            clearMap();
            ImageView imageView = new ImageView(image);
            pane.getChildren().add(imageView);
            stage.sizeToScene();
            changed = false;
        }
    }

    class NewPlaceHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent actionEvent) {
            scene.setCursor(Cursor.CROSSHAIR);
            newPlace.setDisable(true);
            MapClickHandler ch = new MapClickHandler();
            pane.addEventHandler(MouseEvent.MOUSE_CLICKED, ch);
        }
    }

    class MapClickHandler implements EventHandler<MouseEvent>{
        @Override
        public void handle(MouseEvent mouseEvent) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Name");
            dialog.setHeaderText("");
            dialog.setContentText("Name of place:");
            Optional<String> nameInput = dialog.showAndWait();
            if(nameInput.isPresent() && nameInput.get().length() > 0) {
                nameInput.ifPresent(string -> name = string);
                double x = mouseEvent.getX();
                double y = mouseEvent.getY();
                Node node = new Node(x, y, 10, Color.BLUE, name);
                node.setId(name);
                listGraph.add(node);
                allNodes.add(node);
                node.setOnMouseClicked(new ChooseNodeHandler());
                Label cityName = new Label(node.getName());
                cityName.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
                cityName.relocate(x, y);
                cityName.setDisable(true);
                pane.getChildren().addAll(node, cityName);
                changed = true;
            }

            newPlace.setDisable(false);
            scene.setCursor(Cursor.DEFAULT);
            pane.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
        }
    }

    class ChooseNodeHandler implements EventHandler<MouseEvent>{
        @Override
        public void handle(MouseEvent event) {
            Node node = (Node) event.getSource();
            if(node.isSelected()){
                node.flip();
                markedNodes.remove(node);
            }
            else{
                if(markedNodes.size() < 2){
                    node.flip();
                    markedNodes.add(node);
                }
            }
        }
    }

    class ConnectionHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            if(markedNodes.size() != 2){
                alertSelectTwo();
            }
            else{
                Node from = markedNodes.get(0);
                Node to = markedNodes.get(1);
                if(listGraph.getEdgeBetween(from, to) != null){
                    Alert newAlert = new Alert(Alert.AlertType.ERROR, "Edge already exists!");
                    newAlert.showAndWait();
                }
                else{
                    TextInputDialog dialog = new TextInputDialog("");
                    dialog.setTitle("Connection");
                    dialog.setHeaderText("Connection from " + from.getName() + " to " + to.getName());

                    GridPane gridPane = new GridPane();
                    gridPane.setHgap(10);
                    gridPane.setVgap(10);
                    TextField nameTextField = new TextField();
                    TextField timeTextField = new TextField();

                    gridPane.add(new Label("Name:"), 0, 0);
                    gridPane.add(nameTextField, 1, 0);
                    gridPane.add(new Label("Time:"), 0, 1);
                    gridPane.add(timeTextField, 1, 1);

                    dialog.getDialogPane().setContent(gridPane);
                    dialog.showAndWait();

                    String edgeName = nameTextField.getText();
                    if(edgeName.isEmpty() || !timeTextField.getText().matches("\\d+")){
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Not valid");
                        alert.showAndWait();
                    }
                    else{
                        edgeTime = Integer.parseInt(timeTextField.getText());
                        listGraph.connect(from, to, edgeName, edgeTime);
                        Line line = new Line(from.getCenterX(), from.getCenterY(), to.getCenterX(), to.getCenterY());
                        line.setStrokeWidth(3);
                        line.setDisable(true);
                        pane.getChildren().add(line);
                        changed = true;
                    }
                }
            }
        }
    }

    class ShowHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            if(markedNodes.size() != 2){
                alertSelectTwo();
            }
            else{
                Node from = markedNodes.get(0);
                Node to = markedNodes.get(1);
                if(listGraph.getEdgeBetween(from, to) == null){
                    alertNoEdge();
                }
                else{
                    TextInputDialog dialog = new TextInputDialog("");
                    dialog.setTitle("Connection");
                    dialog.setHeaderText("Connection from " + from.getName() + " to " + to.getName());

                    GridPane gridPane = new GridPane();
                    gridPane.setHgap(10);
                    gridPane.setVgap(10);
                    TextField nameTextField = new TextField(listGraph.getEdgeBetween(from, to).getName());
                    TextField timeTextField = new TextField(String.valueOf(listGraph.getEdgeBetween(from, to).getWeight()));
                    nameTextField.setDisable(true);
                    timeTextField.setDisable(true);

                    gridPane.add(new Label("Name:"), 0, 0);
                    gridPane.add(nameTextField, 1, 0);
                    gridPane.add(new Label("Time:"), 0, 1);
                    gridPane.add(timeTextField, 1, 1);

                    dialog.getDialogPane().setContent(gridPane);
                    dialog.showAndWait();
                }
            }
        }
    }

    class ChangeHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            if(markedNodes.size() != 2){
                alertSelectTwo();
            }
            else{
                Node from = markedNodes.get(0);
                Node to = markedNodes.get(1);
                if(listGraph.getEdgeBetween(from, to) == null){
                    alertNoEdge();
                }
                else{
                    TextInputDialog dialog = new TextInputDialog("");
                    dialog.setTitle("Connection");
                    dialog.setHeaderText("Connection from " + from.getName() + " to " + to.getName());

                    GridPane gridPane = new GridPane();
                    gridPane.setHgap(10);
                    gridPane.setVgap(10);
                    TextField nameTextField = new TextField(listGraph.getEdgeBetween(from, to).getName());
                    TextField timeTextField = new TextField();
                    nameTextField.setDisable(true);

                    gridPane.add(new Label("Name:"), 0, 0);
                    gridPane.add(nameTextField, 1, 0);
                    gridPane.add(new Label("Time:"), 0, 1);
                    gridPane.add(timeTextField, 1, 1);

                    dialog.getDialogPane().setContent(gridPane);

                    Optional<String> change = dialog.showAndWait();
                    if(change.isPresent()){
                        if(!timeTextField.getText().matches("\\d+")){
                            Alert newAlert = new Alert(Alert.AlertType.ERROR, "Not valid");
                            newAlert.showAndWait();
                        }
                        else{
                            edgeTime = Integer.parseInt(timeTextField.getText());
                            listGraph.setConnectionWeight(from, to, edgeTime);
                            changed = true;
                        }
                    }
                }
            }
        }
    }

    class FindPathHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event) {
            if(markedNodes.size() != 2){
                alertSelectTwo();
            }
            else{
                Node from = markedNodes.get(0);
                Node to = markedNodes.get(1);
                List<Edge> pathList = listGraph.getPath(to, from);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Find Path");
                alert.setHeaderText("The Path from " + from.getName() + " to " + to.getName());
                TextArea textArea = new TextArea();
                textArea.setEditable(false);
                alert.getDialogPane().setContent(textArea);
                if(pathList == null){
                    textArea.setText("No path between " + from.getName() + " to " + to.getName());
                }
                else{
                    String pathString = "";
                    Collections.reverse(pathList);
                    int total = 0;
                    for(Edge edge: pathList){
                        pathString += edge.toString() + "\n";
                        total += edge.getWeight();
                    }
                    pathString += "Total " + total;
                    textArea.setText(pathString);
                }
                alert.showAndWait();
            }
        }
    }

    public void alertNotSaved(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning");
        alert.setHeaderText("");
        alert.setContentText("Unsaved changes, exit anyway?");
        close = alert.showAndWait();
    }

    public void alertSelectTwo(){
        Alert alert = new Alert(Alert.AlertType.ERROR, "Two places must be selected!");
        alert.showAndWait();
    }

    public void alertNoEdge(){
        Alert alert = new Alert(Alert.AlertType.ERROR, "No edge exists between selected nodes!");
        alert.showAndWait();
    }
    
    public void clearMap(){
        pane.getChildren().clear();
        for(Node node: allNodes)
            listGraph.remove(node);
        allNodes.clear();
        markedNodes.clear();
    }
}
