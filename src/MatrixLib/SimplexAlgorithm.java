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

    public static Matrix simplexAlgorithm(Matrix M, boolean write) throws IncompabilityOfColumnsAndRows, InterruptedException, ExecutionException, IncompabilitySystem {
        //Матрица M представляет собой симплекс-таблицу
        //Поиск отрицательного элемента в последней строке
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
            //целевая функция не ограниченна на области допустимых значений...
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
        runAndWait(()-> listeners.forEach(l -> l.onMessage(new StringBuilder("Определили вспомогательную функцию"))));
        runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(M)));
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
        return J;
    }

    public static Matrix penaltyMethod(Matrix M, double [] coefficents) throws ExecutionException, InterruptedException, IncompabilitySystem, IncompabilityOfColumnsAndRows {
        //Если по странному стечению обстоятельств, этот код кто то когда то будет читать,
        //penalty method - это метод болших штрафов, он же М задача
        List<Integer> addedRows = addSyntheticVars(M);
        runAndWait(()-> listeners.forEach(l -> l.onMessage(new StringBuilder("Ввели искусственные переменные"))));
        runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(M)));
        //Определяем М как самое большое число, усноженное на 10
        //если это не сработает - страдаем
        int m = (int) Math.round(M.getMax()*10);
        double[] helpF = getMFunction(M,m,coefficents,addedRows);
        M.addRow(M.row); //Добавили строку функции
        for(int j=0; j<M.col-1; j++)
            M.matr[M.row-1][j] = -helpF[j];
        M.matr[M.row-1][M.col-1] = helpF[M.col-1];
        runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(M)));
        Matrix S = simplexAlgorithm(M,true);
        runAndWait(()-> listeners.forEach(l -> l.onMessage(new StringBuilder("Последняя таблица"))));
        runAndWait(()-> listeners.forEach(l -> l.onMatrixChange(S)));
        return S;
    }

    private static double [] getMFunction(Matrix M, double m, double [] coefficients, List<Integer> addedRows){
        //Это же не функция, это же адище
        //Выразить базисные переменные
        List<double[]> basisVars = new ArrayList<>();
        List<Integer> ones = M.getOnesColumns();
        for(int j=0; j<ones.size()-1;j++){
            if(ones.get(j)!=-1){
                double [] coef = new double[M.col];
                for(int k=0; k<M.col-1;k++){
                    if(k!=j)
                        coef[k] = -M.matr[ones.get(j)][k];
                }
                coef[M.col-1] = M.matr[ones.get(j)][M.col-1];
                basisVars.add(coef);
            }
        }
        //Выразить функцию через базисные переменные
        double [] function = new double[M.col];
        for(int j=0; j<ones.size()-1;j++){
            int r = ones.get(j);
            if(r!=-1){
                //Если эта базисная переменная из тех, что были искусственно добавлены
                //умножаем на M
               if(addedRows.contains(r)){
                    for(int z=0; z<M.col-1;z++){
                        if(j==z)continue;
                        function[z]+=m*M.matr[r][z];
                    }
                   function[M.col-1]+=-m*M.matr[r][M.col-1];
               }else {
                   //Иначе умножаем на коэффициент переменной
                   double k = coefficients[j];
                   for(int z=0; z<M.col-1;z++){
                       if(z==j)
                           continue;
                       function[z]+=-k*M.matr[r][z];
                   }
                   function[M.col-1]+=k*M.matr[r][M.col-1];
               }
            }
        }
        //А теперь сложить с коэффициентами исходной функции
       for(int j=0; j<coefficients.length; j++){
            int r = ones.get(j);
            if(r==-1)
               function[j]+=coefficients[j];
       }
       return function;
    }

    private static List<Integer> addSyntheticVars(Matrix M) {
        //Метод добавляет искусствеенные переменные в строки,
        //столбцы которых не содержат базисные переменные
        //возвращает строки, куда эти переменные были добавлены
        List<Integer> addedRows = new ArrayList<>();
        List<Integer> ones = M.getOnesColumns();
        List<Integer> rows = new ArrayList<>();
        for(int i=0; i<ones.size()-1; i++)
            if(ones.get(i)!=-1)
                rows.add(ones.get(i));

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
