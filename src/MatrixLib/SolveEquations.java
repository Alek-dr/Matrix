package MatrixLib;

import javafx.application.Platform;
import javafx.print.Collation;
import matrixapplication.MessageListener;

import java.util.*;

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
            Platform.runLater(()-> {
                    listeners.forEach(l -> l.onMessage(X.message));
                    listeners.forEach(l -> l.onMatrixChange(X));}
            );
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
            Platform.runLater(()-> {
                listeners.forEach(l -> l.onMessage(X.message));
            });
            if(resElem==0) continue; //такого случиться не должно, но на всякий случай надо проверить
            if(resElem!=1) {
                X.divRowByNumber(row, resElem);
                sendDivMessage(X, row+1, resElem);
            }
            //формируем единичный столбец
            makeOneCol(row, X, col);
            //исключим нулевые строки, если они появились
            row2ex.clear();
            for (int i : row2ex) X.message.append(i+1 + " ");
            if(row2ex.size()>0){
                row2ex = X.excludeZeroRow2();
                X.message = new StringBuilder("Исключили нулевые строки: ");
                Platform.runLater(()-> {
                    listeners.forEach(l -> l.onMessage(X.message));
                    listeners.forEach(l -> l.onMatrixChange(X));
                });
            }
            //проверка на совместность
            if(!checkSystem(X)){
                X.message = new StringBuilder("Система несовместна");
                Platform.runLater(()-> {
                    listeners.forEach(l -> l.onMessage(X.message));
                });
                return X;
            }
        }
        return X;
    }

    public static List<String> findAllBasis(Matrix M) throws Exception {
        Matrix X = gaussJordan(M);
        List<Integer> ones = X.getOnesColumns();
        List<Integer> oneCols = new ArrayList<>();
        for(int i=0; i<ones.size(); i++)
            if(ones.get(i)!=-1) oneCols.add(i);
        StringBuilder msg = new StringBuilder("Единичные столбцы: ");
        for(int i : oneCols) msg.append(i+1 + " ");
        Platform.runLater(()-> {
            listeners.forEach(l -> l.onMessage(msg));
        });
        //для каждой комбинации найти базисный вид
        List<Integer> cols = new ArrayList<>();
        for(int i=0; i<X.col-1; i++) cols.add(i);
        Combinations.comb.clear();
        Combinations.getCombinations(cols,new Integer[0], X.row);
        Integer[] comb = oneCols.toArray(new Integer[oneCols.size()]);
        int index = Combinations.findCombIndex(comb);
        List<StringBuilder> expr = getExpression(X, Combinations.comb.get(index));
        Combinations.comb.remove(index);
        StringBuilder separate = new StringBuilder("-----------------");
        for(StringBuilder s : expr){
            Platform.runLater(()-> {
                listeners.forEach(l -> l.onMessage(s));
            });
        }
        Platform.runLater(()-> {
            listeners.forEach(l -> l.onMessage(separate));
        });
        for (Integer[] c : Combinations.comb){

            StringBuilder repl = new StringBuilder("Свести к единичным столбцы: ");
            for(int i : c) repl.append(i+1 + " ");
            Platform.runLater(()-> {
                listeners.forEach(l -> l.onMessage(repl));
            });

            Matrix Z = substitution(X,c,ones);
            expr = getExpression(Z, c);
            for(StringBuilder s : expr){
                Platform.runLater(()-> {
                    listeners.forEach(l -> l.onMessage(s));
                });
            }
            Platform.runLater(()-> {
                listeners.forEach(l -> l.onMessage(separate));
            });
        }
        return null;
    }

    //region вспомогательные методы для Гаусса-Жордана и нахождения всех базисов

    private static void makeOneCol(int row, Matrix x, int col) {
        //метод формирует единичный столбец col матрицы x,
        //ведущая строка row
        double divBy;
        int strI;
        int strZ;
        for(int i = 0; i< x.row; i++){
            if(i==row) continue;
            divBy = -x.matr[i][col];
            strI = i+1;
            strZ = row+1;
            x.addRowMultiplyedByNumber(row, divBy, i);
            x.message = new StringBuilder("Добавили к строке " + strI + " строку " + strZ + ", умноженную на " + x.nf.format(divBy));
            Platform.runLater(()-> {
                listeners.forEach(l -> l.onMessage(x.message));
                listeners.forEach(l -> l.onMatrixChange(x));
            });
        }
    }

    private static boolean checkSystem(Matrix M){
        //проверка на совместность
        boolean ok = true;
        for(int i=0; i<M.row; i++){
            ok = false;
            for (int j=0; j<M.col-1; j++){
                if(M.matr[i][M.col-1]!=0)
                    if(M.matr[i][j]!=0){
                        ok = true;
                        break;
                    }
            }
            if(!ok) return false;
        }
        return ok;
    }

    private static void sendDivMessage(Matrix M, int row, double resElem) {
        M.message = new StringBuilder("Поделили строку " + row + " на " + M.nf.format(resElem));
        Platform.runLater(()-> {
            listeners.forEach(l -> l.onMessage(M.message));
            listeners.forEach(l -> l.onMatrixChange(M));
        });
    }

    private static double[] findResolveElem(Matrix M, List<Integer> rows) throws IncompabilityOfColumnsAndRows {
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
                    Platform.runLater(()-> {
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

    private static Matrix substitution(Matrix M, Integer [] comb, List<Integer> ones) throws IncompabilityOfColumnsAndRows {
        //Операция замещения
        Matrix X = new Matrix(M.row, M.col);
        X.name = M.name;
        X.round = M.round;
        X.matr = Matrix.copy(M);
        for(int i=0; i<comb.length;i++) {
            int col = comb[i];
            if(ones.get(col)==-1){
                int r = getRowToDiv(ones,comb, M.row);
                X.divRowByNumber(r,M.matr[r][col]);
                makeOneCol(r,X,col);
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

}