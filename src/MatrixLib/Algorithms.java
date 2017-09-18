package MatrixLib;

import matrixapplication.MessageListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexander on 16.09.17.
 */
public class Algorithms {

    private static List<MessageListener> listeners = new ArrayList<>();

    public static void addListeners(MessageListener msgList) {
        listeners.add(msgList);
    }

    public static Matrix Gauss(Matrix M) throws Exception {
        if (M.isZero()) throw new Exception("Нулевая матрица");
        if ((M.col == 1) & (M.row == 1)) {
            Matrix Res = new Matrix(M.rows(), M.columns());
            Res.matr[0][0] = 1;
            return Res;
        }
        int strI;
        int strZ;
        double[] RowElem;
        double leadElement;
        int row;
        double divBy;
        Matrix X = new Matrix(M.row, M.col);
        X.name = M.name;
        X.matr = Matrix.copy(M);
        //Send message here
        List<Integer> delRows = X.excludeZeroRow2();
        if (delRows.size() > 0) {
            X.message = new StringBuilder("Исключим строки:");
            for (int i : delRows) {
                strI = i + 1;
                X.message.append(" " + strI);
            }
            listeners.forEach(l -> l.onMessage(X.message));
            listeners.forEach(l -> l.onMatrixChange(X));
            //Send message here
        }
        int key = (M.col >= M.row) ? 1 : 0;
        int n;
        if (key == 0) n = M.col;
        else n = M.row;
        for (int z = 0; z < n; z++) {
            strZ = z + 1;
            RowElem = X.findLeadElem(z, z);
            leadElement = RowElem[1];
            row = (int) RowElem[0];
            strI = row + 1;
            if (leadElement == 0)
                continue;
            if (row != 0) {
                if (z != row) {
                    X.swapRows(z, row);
                    X.message = new StringBuilder("Поменяли местами строки " + strZ + " и " + strI);
                    listeners.forEach(l -> l.onMessage(X.message));
                    listeners.forEach(l -> l.onMatrixChange(X));
                    //Send message here
                }
            }
            if (leadElement != 1) {
                X.divRowByNumber(z, leadElement);
                X.message = new StringBuilder("Поделили строку " + strZ + " на " + X.nf.format(leadElement));
                listeners.forEach(l -> l.onMessage(X.message));
                listeners.forEach(l -> l.onMatrixChange(X));
                //Send message here
            }
            for (int i = z + 1; i < X.row; i++) {
                if (X.matr[i][z] == 0)
                    continue;
                strI = i + 1;
                divBy = -X.matr[i][z];
                X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                X.message = new StringBuilder("Добавили к строке " + strI + " строку " + strZ + ", умноженную на " + X.nf.format(divBy));
                listeners.forEach(l -> l.onMessage(X.message));
                listeners.forEach(l -> l.onMatrixChange(X));
            }

            //Send message here
        }
        return X;
    }

    public static Matrix gaussJordan(Matrix M) throws Exception {
        if (M.isZero()) throw new Exception("Нулевая матрица");
        if ((M.col == 1) | (M.row == 1)) {
            Matrix Res = new Matrix(M.row, M.col);
            Res.matr[0][0] = 1;
            return Res;
        }
        M.excludeZeroRow();
        int row;
        double divBy;
        double leadElement;
        double[] RowElem;
        int strI;
        int strZ;
        Matrix X = new Matrix(M.row, M.col);
        X.name = M.name;
        X.round = M.round;
        X.matr = Matrix.copy(M);
        int key = (M.col >= M.row) ? 1 : 0;
        int n;
        if (key == 0) n = M.col;
        else n = M.row;
        for (int z = 0; z < n; z++) {
            //поиск разрешающего элемента
            strZ = z + 1;
            RowElem = X.findLeadElem(z, z);
            leadElement = RowElem[1];
            row = (int) RowElem[0];
            strI = row + 1;
            if (leadElement == 0)
                continue;
            //если он не в первой строке, то меняем местами
            if (row != 0) {
                X.swapRows(z, row);
                if (z != row) {
                    X.message = new StringBuilder("Поменяли местами строки " + strZ + " и " + strI);
                    listeners.forEach(l -> l.onMessage(X.message));
                    listeners.forEach(l -> l.onMatrixChange(X));
                }
            }
            //если он не равен 1, то делим строку на разрешающий элемент
            if (leadElement != 1) {
                X.divRowByNumber(z, leadElement);
                X.message = new StringBuilder("Поделили строку " + strZ + " на " + M.nf.format(leadElement));
                listeners.forEach(l -> l.onMessage(X.message));
                listeners.forEach(l -> l.onMatrixChange(X));
            }
            //формируем единичный столбец
            for (int i = 0; i < X.row; i++) {
                if (i == z) continue;
                if (X.matr[i][z] == 0) continue;
                strI = i + 1;
                divBy = -X.matr[i][z];
                X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                X.message = new StringBuilder("Добавили к строке " + strI + " строку " + strZ + ", умноженную на " + M.nf.format(divBy));
                listeners.forEach(l -> l.onMessage(X.message));
                listeners.forEach(l -> l.onMatrixChange(X));
            }
        }
        return X;
    }

}