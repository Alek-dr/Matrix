package matrixapplication;

import MatrixLib.Algorithms;
import MatrixLib.Matrix;
import MatrixLib.SolveEquations;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Controller implements Initializable, MessageListener {

    @FXML
    private Button execute;
    @FXML
    private TextArea resField;
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
    public static NumberFormat nf = new DecimalFormat("#.###");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SolveEquations.addListeners(this);
        Algorithms.addListeners(this);
        execute.setMaxWidth(Double.MAX_VALUE);
        resField.setMaxHeight(Double.MAX_VALUE);
        resField.setEditable(true);
        resField.setStyle("-fx-font-family: monospace");
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
        resField.textProperty().addListener((observable, oldValue, newValue) ->
            resField.setScrollTop(Double.MAX_VALUE));
        List<String> act = new ArrayList<>();
        act.add("Алгоритм Гаусса");
        act.add("Алгоритм Гаусса-Жордана");
        act.add("Все базисные виды");
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

    @FXML
    protected void onExecClick(ActionEvent event) throws Exception {
        Matrix M = getMatrix();
        String action = (String)actions.getSelectionModel().getSelectedItem();
        switch (action){
            case("Алгоритм Гаусса"):{
                Algorithms.Gauss(M);
                break;
            }
            case("Алгоритм Гаусса-Жордана"):{
                Thread t = new Thread(()->{
                    try{
                        SolveEquations.gaussJordan(M);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                });
                t.start();
                break;
            }
            case("Все базисные виды"):{
                Thread t = new Thread(() -> {
                    try {
                        SolveEquations.findAllBasis(M);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                t.start();
                break;
            }
        }
    }

    private Matrix getMatrix(){
        int r = Integer.parseInt(rows.getText());
        int c = Integer.parseInt(cols.getText());
        ObservableList elems =  matrixGrid.getChildren();
        double [][] m = new double[r][c];
        int z = 0;
        for (int i=0; i<r; i++){
            for (int j=0; j<c;j++){
                TextArea el = (TextArea)elems.get(z);
                m[i][j] = Double.parseDouble(el.getText());
                z++;
            }
        }
        Matrix M = new Matrix(m);
        return M;
    }

    private void write(Matrix X) {
        StringBuilder ident = new StringBuilder();
        StringBuilder newLine = new StringBuilder("\n");
        StringBuilder twoSpace = new StringBuilder("  ");
        StringBuilder oneSpace = new StringBuilder(" ");
        int max = 1;
        int n = 0;
        int col = X.columns();
        //столбец - ширина
        Map<Integer, Integer> MaxWidth = new HashMap<Integer, Integer>();
        //в цикле находим максимальную длину строки в каждом столбце
        for (int j = 0; j < col; j++) {
            max = 1;
            MaxWidth.put(j, max);
            for (int i = 0; i < X.rows(); i++) {
                n = X.getSElem(i, j).length();
                if (n > max) {
                    max = n;
                    MaxWidth.remove(j);
                    MaxWidth.put(j, max);
                }
            }
        }
        int width = 0;
        int need = 0;
        for (int i = 0; i < X.rows(); i++) {
            resField.appendText("\n");
            for (int j = 0; j < X.columns(); j++) {
                if (j != 0) {
                    //отступ в два пробела перед началом нового столбца
                    resField.appendText(String.valueOf(twoSpace));
                } else {
                    //отступ шириной в имя для первого столбца
                    resField.appendText(String.valueOf(ident));
                }
                width = MaxWidth.get(j);
                need = width - X.getSElem(i, j).length();
                for (int z = 0; z < need; z++)
                    resField.appendText(String.valueOf(oneSpace));
                resField.appendText(String.valueOf(nf.format(X.getElem(i, j))));
            }
        }
        resField.appendText(String.valueOf(newLine));
    }

    @Override
    public void onMessage(StringBuilder str) {
        resField.appendText(String.valueOf(str));
        resField.appendText("\n");
    }

    @Override
    public void onMatrixChange(Matrix M) {
        write(M);
        resField.appendText("\n");
    }
}
