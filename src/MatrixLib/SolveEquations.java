package MatrixLib;

import javafx.application.Platform;
import matrixapplication.MessageListener;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by alexander on 17.09.17.
 */

/*
* Предполагается, что матрицы являются расширенными
*/

public class SolveEquations {

    private static List<MessageListener> listeners = new ArrayList<>();

    public static void addListeners(MessageListener msgList) {
        listeners.add(msgList);
    }

    public static Matrix gaussJordan(Matrix M) throws Exception {
        if (M.isZero()) throw new Exception("Нулевая матрица");
        if ((M.col == 1) | (M.row == 1)) {
            Matrix Res = new Matrix(M.row, M.col);
            Res.matr[0][0] = 1;
            return Res;
        }
        List<Integer> row2ex  = M.excludeZeroRow2(); //исключаем нулевые строки
        int row;
        int strI;
        int colI;
        Matrix X = new Matrix(M.row, M.col);
        X.name = M.name;
        X.round = M.round;
        X.matr = Matrix.copy(M);
        double resElem; // разрешающий элемент
        ArrayList<Integer> skipRows = new ArrayList<>(); //отработанные строки
        if(row2ex.size()>0){
            X.message = new StringBuilder("Исключили нулевые строки: ");
            for (int i : row2ex) X.message.append(i+1 + " ");
            runAndWait(()->{
                listeners.forEach(l -> l.onMessage(X.message));
                listeners.forEach(l -> l.onMatrixChange(X));
            });
        }
        int key = (M.col >= M.row) ? 1 : 0;
        int n;
        int col;
        if (key == 0) n = M.col;
        else n = M.row;
        for (int z = 0; z < n; z++) {
            //если на последнем шаге были исключены нулевые строки
            if(z==X.row) return X;
            //поиск разрешающего элемента
            double[] res = findResolveElem(X, skipRows);
            row = (int)res[0]; //строка с разрешающим элементом
            strI = row+1;
            skipRows.add((int)res[0]);
            col = (int)res[1]; //столбец с разрешающим элементом
            colI = col+1;
            resElem = res[2]; //сам разрешающий элемент
            X.message = new StringBuilder("Разрешающий элемент: "+ X.nf.format(resElem) +  " в " + strI + " строке, " + colI + " столбце");
            runAndWait(()-> {
                listeners.forEach(l -> l.onMessage(X.message));
            });
            if(resElem==0) continue; //такого случиться не должно, но на всякий случай надо проверить
            if(resElem!=1) {
                X.divRowByNumber(row, resElem);
                //sendDivMessage(X, row+1, resElem);
            }
            //формируем единичный столбец
            try{
                makeOneCol(row, X, col);
            }catch (IncompabilitySystem ex){
                runAndWait(()-> listeners.forEach(l -> l.onMessage(new StringBuilder(ex.getMessage()))));
                break;
            }
            //исключим нулевые строки, если они появились
            row2ex.clear();
            for (int i : row2ex) X.message.append(i+1 + " ");
            row2ex = X.excludeZeroRow2();
            if(row2ex.size()>0){
                X.message = new StringBuilder("Исключили нулевые строки: ");
                runAndWait(()-> {
                    listeners.forEach(l -> l.onMessage(X.message));
                    listeners.forEach(l -> l.onMatrixChange(X));
                });
            }
            //проверка на совместность
            if(!checkSystem(X)){
                X.message = new StringBuilder("Система несовместна");
                runAndWait(()-> listeners.forEach(l -> l.onMessage(X.message)));
                return X;
            }
        }
        return X;
    }

    public static List<Matrix> findAllBasis(Matrix M) throws Exception {
        List<Matrix> matrixList = new ArrayList<>();
        //Выполнить алгоритм Гаусса-Жордана, найти какой ниюудь базисный вид
        Matrix X = gaussJordan(M);
        X.rounding();
        matrixList.add(X);
        List<Integer> ones = X.getOnesColumns();
        List<Integer> oneCols = new ArrayList<>(); //единичные столбцы
        for(int i=0; i<ones.size(); i++)
            if(ones.get(i)!=-1) oneCols.add(i);
        StringBuilder msg = new StringBuilder("Единичные столбцы: ");
        for(int i : oneCols) msg.append(i+1 + " ");
        runAndWait(()-> listeners.forEach(l -> l.onMessage(msg)));
        //для каждой комбинации найти базисный вид
        //получить все возможные комбинации базисных переменных
        List<Integer> cols = new ArrayList<>();
        for(int i=0; i<X.col-1; i++) cols.add(i);
        Combinations.comb.clear();
        Combinations.getCombinations(cols,new Integer[0], X.row);
        Integer[] comb = oneCols.toArray(new Integer[oneCols.size()]);
        //получить индекс комбинации, к которой пришли на первом шаге
        int index = Combinations.findCombIndex(comb); //индекс текущей комбинации из всех возможных
        List<StringBuilder> expr = getExpression(X, Combinations.comb.get(index));
        Combinations.comb.remove(index); //удалили первую комбинацию
        StringBuilder separate = new StringBuilder("-------------------------");
        for(StringBuilder s : expr)
            runAndWait(()-> listeners.forEach(l -> l.onMessage(s)));
        runAndWait(()-> listeners.forEach(l -> l.onMessage(separate)));
        StringBuilder workMatr = new StringBuilder("Система для дальнейших преобразований: ");
        runAndWait(()-> {
            listeners.forEach(l -> l.onMessage(workMatr));
            listeners.forEach(l -> l.onMatrixChange(X));
            listeners.forEach(l -> l.onMessage(separate));
        });
        //Цикл приведения к другим базисам
        for (Integer[] c : Combinations.comb){
            StringBuilder repl = new StringBuilder("Свести к единичным столбцы: ");
            for(int i : c) repl.append(i+1 + " ");
            Matrix Z;
            runAndWait(()-> listeners.forEach(l -> l.onMessage(repl)));
            try{
                Z = substitution(X,c,ones); //Заменщение
                Z.rounding();
                matrixList.add(Z);
            }catch (IncompabilitySystem ex){
                //runAndWait(()-> listeners.forEach(l -> l.onMessage(new StringBuilder(ex.getMessage()))));
                continue;
            }catch (WrongBasis ex){
                //runAndWait(()-> listeners.forEach(l -> l.onMessage(new StringBuilder(ex.getMessage()))));
                continue;
            }
            expr = getExpression(Z, c); //Выразить в строковом виде базисные переменные через свободные
            for(StringBuilder s : expr)
                runAndWait(()-> listeners.forEach(l -> l.onMessage(s)));
            //runAndWait(()-> listeners.forEach(l -> l.onMessage(separate)));
            runAndWait(()-> {
                //listeners.forEach(l -> l.onMessage(workMatr));
                listeners.forEach(l -> l.onMatrixChange(X));
                listeners.forEach(l -> l.onMessage(separate));
            });
        }
        return matrixList;
    }

    public static List<Integer> basicFeasibleSolution(List<Matrix> matrixList) throws ExecutionException, InterruptedException {
        //Лист матриц содержит расширенные матрицы, приведенные к базисному виду
        StringBuilder sol = new StringBuilder("Опорное решение: ");
        StringBuilder separate = new StringBuilder("-------------------------");
        List<Integer> basFesSol = new ArrayList<>();
        boolean bfs;
        //Matrix M;
        for(int j=0; j<matrixList.size(); j++){
            Matrix M = matrixList.get(j);
            bfs = true;
            for(int i=0; i<M.row; i++){
                if(M.matr[i][M.col-1]<0) {
                    bfs = false;
                    break;
                }
            }
            if(bfs){
                basFesSol.add(j);
                runAndWait(()-> {
                    listeners.forEach(l -> l.onMessage(sol));
                    listeners.forEach(l -> l.onMatrixChange(M));
                    listeners.forEach(l -> l.onMessage(separate));
                });
            }
        }
        return basFesSol;
    }

    public static double findMax(List<Matrix> matrixList, List<Integer>bfs, double [] coefficents) throws ExecutionException, InterruptedException {
        Matrix M;
        double d;
        double max = 0;
        double x;
        double currMax = 0;
        List<Integer> oneCols;
        //По опорным решениям вычисляем функцию
        for(int i : bfs){
            M = matrixList.get(i);
            oneCols = M.getOnesColumns();
            max = 0;
            for(int j=0; j<coefficents.length-1; j++){
                d = coefficents[j];
                if(oneCols.get(j)!=-1){
                    x = M.matr[oneCols.get(j)][M.col-1];
                    currMax+=d*x;
                }
            }
            currMax+=coefficents[coefficents.length-1]; //плюс свободный член самой функции
            if(currMax>max)
                max = currMax;
            StringBuilder msg = new StringBuilder("Max = ");
            msg.append(currMax);
            runAndWait(()-> listeners.forEach(l -> l.onMessage(msg)));
            currMax = 0;
        }
        return max;
    }

    //region вспомогательные методы для Гаусса-Жордана и нахождения всех базисов

    private static void makeOneCol(int row, Matrix x, int col) throws ExecutionException, InterruptedException, IncompabilitySystem {
        //метод формирует единичный столбец col матрицы x,
        //ведущая строка row
        double divBy;
        int strI;
        int strZ;
        for(int i = 0; i< x.row; i++){
            if(i==row) continue;
            divBy = -x.matr[i][col];
            if(divBy==0) continue;
            strI = i+1;
            strZ = row+1;
            x.addRowMultiplyedByNumber(row, divBy, i);
            x.message = new StringBuilder("Добавили к строке " + strI + " строку " + strZ + ", умноженную на " + x.nf.format(divBy));
            runAndWait(()-> {
                //listeners.forEach(l -> l.onMessage(x.message));
                listeners.forEach(l -> l.onMatrixChange(x));
            });
            if(!checkSystem(x)) throw new IncompabilitySystem("Несовместная система");
        }
    }

    private static boolean checkSystem(Matrix M){
        //проверка на совместность
        boolean ok = true;
        //проверить последний элемент
        //если он равен 0, то система совместана
        for(int i=0; i<M.row; i++){
            if(M.matr[i][M.col-1]==0) continue;
            ok = false;
            for (int j=0; j<M.col-1; j++){
                if(M.matr[i][j]!=0){
                    ok = true;
                    break;
                }
            }
            if(!ok) return false;
        }
        return ok;
    }

    private static void sendDivMessage(Matrix M, int row, double resElem) throws ExecutionException, InterruptedException {
        M.message = new StringBuilder("Поделили строку " + row + " на " + M.nf.format(resElem));
        runAndWait(()-> {
            listeners.forEach(l -> l.onMessage(M.message));
            listeners.forEach(l -> l.onMatrixChange(M));
        });
    }

    private static double[] findResolveElem(Matrix M, List<Integer> rows) throws IncompabilityOfColumnsAndRows, ExecutionException, InterruptedException {
        //поиск разрешающего элемента, желательно 1 или -1
        double[] res = new double[3];
        double [] candidats = new double[3];
        for(int j=0;j<M.col-1;j++){
            for (int i=0; i<M.row;i++){
                if(rows.contains(i)) continue;
                if(M.matr[i][j]==1){
                    res[0] = i;
                    res[1] = j;
                    res[2] = M.matr[i][j];
                    return res;
                }
                if(M.matr[i][j]==-1){
                    M.multiplyRowByNumber(i,-1);
                    res[0] = i;
                    res[1] = j;
                    res[2] = M.matr[i][j];
                    int str = i+1;
                    M.message = new StringBuilder("Умножили строку: "+ str +  " на -1");
                    runAndWait(()-> {
                        listeners.forEach(l -> l.onMessage(M.message));
                        listeners.forEach(l -> l.onMatrixChange(M));
                    });
                    return res;
                }
                if((M.matr[i][j]!=0)&(candidats[2]==0)){
                    candidats[0] = i;
                    candidats[1] = j;
                    candidats[2] = M.matr[i][j];
                }
            }
        }
        return candidats;
    }

    private static Matrix substitution(Matrix M, Integer [] comb, List<Integer> ones) throws IncompabilityOfColumnsAndRows, ExecutionException, InterruptedException, IncompabilitySystem, WrongBasis {
        //Операция замещения
        Matrix X = new Matrix(M.row, M.col);
        X.name = M.name;
        X.round = M.round;
        X.matr = Matrix.copy(M);
        X.rounding();
        for(int i=0; i<comb.length;i++) {
            int col = comb[i];
            if(ones.get(col)==-1) {
                int r = getRowToDiv(ones, comb, X.row);
                double nToDiv = X.matr[r][col]; //число, на которое собираемся делить строку
                if (nToDiv == 0) //Переменную невозможно сделать базисной
                    throw new WrongBasis("Невозможно перейти к базису");
                X.divRowByNumber(r, nToDiv);
                //sendDivMessage(X, r + 1, nToDiv);
                makeOneCol(r, X, col); //Сформировать единичный столбец
                X.rounding();
                //надо обновлять, т.к. можно найти строку, которую уже использовали
                ones = X.getOnesColumns();
            }
        }
        return X;
    }

    private static int getRowToDiv(List<Integer> ones, Integer[] comb, int rows) {
        //Проверить, какие из столбцов необходимо свести к единичным
        //Запомнить строки с единичными столбцами
        List<Integer> col2one = new ArrayList<>();
        List<Integer> oneRows = new ArrayList<>();
        for(int i=0; i<comb.length; i++){
            int col = comb[i];
            int row = ones.get(col);
            if(row==-1)
                col2one.add(col);
            else oneRows.add(row);
        }
        //Отсортировать строки
        Collections.sort(oneRows);
        Collections.reverse(oneRows);
        int r2div = -1;
        //Найти, на какую строку можно делить
        for(int i=0; i<rows; i++){
            if(!oneRows.contains(i)){
                r2div = i;
                break;
            }
        }
        return r2div;
    }

    private static List<StringBuilder> getExpression(Matrix M, Integer[] comb){
        //Выражает базовые переменные через свободные в строковом виде
        List<StringBuilder> expr = new ArrayList<>();
        M.rounding();
        for (int i=0; i<comb.length; i++){
            StringBuilder s = new StringBuilder();
            int r = findRow(M, comb[i]);
            double dlast = M.matr[r][M.col-1];
            int ind = comb[i]+1;
            if(isInt(dlast)){
                int ilast = (int)dlast;
                s.append("X"+ind+" = "+ilast);
            }else
                s.append("X"+ind+" = "+M.nf.format(dlast));
            for(int j=0; j<M.col-1;j++){
                if(j==comb[i]) continue;
                if(M.matr[r][j]==0) continue;
                ind = j+1;
                if(isInt(M.matr[r][j])){
                    int n = -(int)M.matr[r][j];
                    if((n>0)&(s.charAt(s.length()-1)!='=')){
                        s.append("+"+n+"X"+ind+" ");
                    }else{
                        s.append(n+"X"+ind+" ");
                    }
                }else{
                    if((-M.matr[r][j]>0)&(s.charAt(s.length()-1)!='=')){
                        s.append("+"+M.nf.format(-M.matr[r][j])+"X"+ind+" ");
                    }else{
                        s.append(M.nf.format(-M.matr[r][j])+"X"+ind+" ");
                    }
                }
            }
            expr.add(s);
        }
        return expr;
    }

    private static int findRow(Matrix M, int col){
        //метод должен находить первую строку, в которой содержится
        //1 в заданной колонне
        for(int i=0; i<M.row; i++)
            if(M.matr[i][col]==1) return i;
        return -1;
    }

    private static boolean isInt(double N){
        if ((N == Math.floor(N)) && !Double.isInfinite(N))
            return true;
        return false;
    }

    //endregion

    public static void runAndWait(Runnable run) throws InterruptedException, ExecutionException {
        FutureTask<Void> task = new FutureTask<>(run, null);
        Platform.runLater(task);
        task.get();
    }

}
