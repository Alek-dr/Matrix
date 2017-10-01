package MatrixLib;

import javafx.application.Platform;
import matrixapplication.MessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by alexander on 01.10.17.
 */
public class SimplexAlgorithm {

    private static List<MessageListener> listeners = new ArrayList<>();

    public static void addListeners(MessageListener msgList) {
        listeners.add(msgList);
    }

    public static Matrix simplexAlgorithm(Matrix M) throws IncompabilityOfColumnsAndRows, InterruptedException, ExecutionException, IncompabilitySystem {
        //Матрица M представляет собой симплекс-таблицу
        //Поиск отрицательного элемента в последней строке
        double n = 1;
        int col = -1;
        for(int j=1; j<M.col; j++){
            if(M.matr[M.row-1][j]<0){
                n = M.matr[M.row-1][j];
                col = j;
                break;
            }
        }
        if (n==1){
            //если не был найден отрицательный элемент, симплекс таблица и есть последняя
            runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(M)));
            return M;
        }
        //Поиск положительных коэффициентов в столбце col
        List<Integer> posRows = new ArrayList<>(); //номера строк, в которых элементы положительны
        for(int i=0; i<M.row-1; i++){
            if(M.matr[i][col]>0)
                posRows.add(i);
        }
        if(posRows.size()==0){
            //целевая функция не ограниценна на области допустимых значений...
            return M;
        }
        List<Double> ratio = new ArrayList<>(); //отношения свободных членов к положительным элементам
        for (int r : posRows){
            double z = M.matr[r][0]/M.matr[r][col];
            ratio.add(z);
        }
        double minElem = ratio.stream().min(Double::compareTo).get();
        int minIndex = ratio.indexOf(minElem);
        int row = posRows.get(minIndex);
        //Замещение
        M.divRowByNumber(row, M.matr[row][col]);
        SolveEquations.makeOneCol(row,M,col);
        runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(M)));
        return simplexAlgorithm(M);
    }

    public static void runAndWait(Runnable run) throws InterruptedException, ExecutionException {
        FutureTask<Void> task = new FutureTask<>(run, null);
        Platform.runLater(task);
        task.get();
    }
}
