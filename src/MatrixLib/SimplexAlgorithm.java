package MatrixLib;

import javafx.application.Platform;
import matrixapplication.MessageListener;

import java.util.ArrayList;
import java.util.Collections;
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

    public static Matrix simplexAlgorithm(Matrix M, boolean write) throws IncompabilityOfColumnsAndRows, InterruptedException, ExecutionException, IncompabilitySystem {
        //Матрица M представляет собой симплекс-таблицу
        //Поиск отрицательного элемента в последней строке

        //было бы не плохо переделать, чтоб свободные члены были в ПОСЛЕДНЕМ столбце, а то как-то по еврейски
        double n = 1;
        int col = -1;
        for(int j=0; j<M.col-1; j++){
            if(M.matr[M.row-1][j]<0){
                n = M.matr[M.row-1][j];
                col = j;
                break;
            }
        }
        if (n==1){
            //если не был найден отрицательный элемент, симплекс таблица и есть последняя
            if(write)
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
            double z = M.matr[r][M.col-1]/M.matr[r][col];
            ratio.add(z);
        }
        double minElem = ratio.stream().min(Double::compareTo).get();
        int minIndex = ratio.indexOf(minElem);
        int row = posRows.get(minIndex);
        //Замещение
        M.divRowByNumber(row, M.matr[row][col]);
        SolveEquations.makeOneCol(row,M,col,false);
        if(write)
            runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(M)));
        return simplexAlgorithm(M,write);
    }

    public static Matrix syntheticBasis(Matrix M,double [] coefficents) throws InterruptedException, ExecutionException, IncompabilityOfColumnsAndRows, IncompabilitySystem {
        //Не знаю, как этот метод в англоязычной литературе правильно называется
        //Задача уже в канонической форме и приводить ее автоматически к таковой я не собираюсь
        //Пункт 1 - ввести искусств. переменные в уравнения, где нет единичного столбца
        List<Integer> addedRows = addSyntheticVars(M);
        //Пункт 2 - составить вспомогательную функцию
        double[] helpF = helpFunction(M,addedRows);
        M.addRow(M.row); //Добавили строку функции
        for(int j=0; j<M.col; j++)
            M.matr[M.row-1][j] = helpF[j];
        //Пункт 2
        //Обычным симплекс методом выводим искусственные переменные из числа базисных
        Matrix S = simplexAlgorithm(M,false);
        runAndWait(()-> listeners.forEach(l -> l.onMessage(new StringBuilder("Вывели из числа базисных искусственные переменные"))));
        runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(S)));
        //Пункт 3
        //Замена целевой функции на исходную
        for(int j=0; j<S.col; j++)
            S.matr[S.row-1][j] = 0;
        for(int j=0; j<coefficents.length-1; j++)
            S.matr[S.row-1][j] = -coefficents[j];
        double last = -coefficents[coefficents.length-1];
        S.matr[S.row-1][S.col-1] = last;
        runAndWait(()-> listeners.forEach(l -> l.onMessage(new StringBuilder("Заменили целевую функцию"))));
        runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(S)));
        //Пункт 4
        //Снова симплекс метод
        Matrix J = simplexAlgorithm(S,false);
        runAndWait(()-> listeners.forEach(l -> l.onMessage(new StringBuilder("Последняя таблица"))));
        runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(J)));
        return null;
    }

    private static List<Integer> addSyntheticVars(Matrix M) {
        List<Integer> addedRows = new ArrayList<>();
        List<Integer> ones = M.getOnesColumns();
        List<Integer> rows = new ArrayList<>();
        for(int i=0; i<ones.size()-1; i++){
            if(ones.get(i)!=-1)
                rows.add(ones.get(i));
        }
        for(int i=0; i<ones.size()-1; i++){
            if(ones.get(i)!=-1)
                rows.add(ones.get(i));
        }
        while (rows.size()!=M.row){
            if(ones.contains(-1)){
                M.addColumn(M.col-1);
                for(int d=0; d<M.row; d++){
                    if(!rows.contains(d)){
                        M.matr[d][M.col-2] = 1;
                        rows.add(d);
                        addedRows.add(d);
                        break;
                    }
                }
                ones = M.getOnesColumns();
            }
        }
        return addedRows;
    }

    private static double [] helpFunction(Matrix M, List<Integer> rows){
        //Столбцы, которые не надо суммировать
        List<Integer> cols = new ArrayList<>();
        for(int i=0; i<rows.size(); i++)
            cols.add(M.col-2-i);
        //Составляем коэффиценты вспомогательной функции
        double [] coefficents = new double[M.col];
        for(int j=0; j<M.col;j++){
            if(cols.contains(j))
                continue;
            for(int r: rows)
                coefficents[j]+=-M.matr[r][j];
        }
        return coefficents;
    }

    public static void runAndWait(Runnable run) throws InterruptedException, ExecutionException {
        FutureTask<Void> task = new FutureTask<>(run, null);
        Platform.runLater(task);
        task.get();
    }
}
