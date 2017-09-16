package matrixapplication;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private Button execute;
    @FXML
    private TextField resField;
    @FXML
    private TextArea rows;
    @FXML
    private TextArea cols;
    @FXML
    private GridPane matrixGrid;
    @FXML
    private ScrollPane pane;
    @FXML
    private ListView actions;

    private ChangeListener<String> dimensiomChange;

    private ObservableList<String> matrixActions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        execute.setMaxWidth(Double.MAX_VALUE);
        resField.setMaxHeight(Double.MAX_VALUE);
        resField.setEditable(false);
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.setMaxHeight(Double.MAX_VALUE);
        pane.setFitToHeight(true);
        pane.setFitToHeight(true);
        //Выравнивание по центру матрицы
        matrixGrid.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                pane.getViewportBounds().getWidth(), pane.viewportBoundsProperty()));
        //Изменение размерности матрицы
        dimensiomChange = (observable, oldValue, newValue) -> {
            try{
                int n = Integer.parseInt(newValue);
                if(rows.isFocused())
                    setMatrix(n, Integer.parseInt(cols.getText()));
                else
                    setMatrix(Integer.parseInt(rows.getText()),n);
            }catch (Exception ex){}
        };
        List<String> act = new ArrayList<>();
        act.add("Верхнетреугольная матрица");
        matrixActions = FXCollections.observableList(act);
        actions.setItems(matrixActions);
        rows.setText("3");
        cols.setText("3");
        rows.textProperty().addListener(dimensiomChange);
        cols.textProperty().addListener(dimensiomChange);
        setMatrix(3,3);
    }

    private void setMatrix(int r, int c){
        matrixGrid.getChildren().clear();
        for(int i=0; i<r; i++){
            for (int j=0; j<c;j++){
                TextArea textArea = new TextArea();
                textArea.setText("0");
                textArea.setMaxWidth(5);
                textArea.setMaxHeight(5);
                matrixGrid.add(textArea,j,i);
            }
        }
    }
}
