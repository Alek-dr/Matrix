package MatrixLib;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by Barmin on 31.12.2016.
 */

public class Matrix {

    public Matrix(int row, int col){
        matr = new double[row][col];
        this.col = col;
        this.row = row;
        round = 3;
    }

    public Matrix(double[][] matr){
        this.matr = matr;
        this.row = matr.length;
        this.col = matr[0].length;
        round = 3;
    }

    public static NumberFormat nf = new DecimalFormat("#.###");

    public double[][] getMatr() {
        return matr;
    }

    protected double[][] matr;

    protected int col;

    protected int row;

    public StringBuilder message;

    public int columns(){
        return matr[0].length;
    }

    public int rows(){
        return matr.length;
    }

    public String name;

    public int round;

    public boolean isZero(){
        for (double[] z : matr)
            for(double d: z)
                if (d != 0) return false;
        return true;
    }

    @Override
    public String toString(){
        return this.name;
    }

    //region Operations
    public static Matrix addMatrix(Matrix A, Matrix B) throws SumExceptiom{
        boolean canAdd = (A.col == B.col) && (A.row == B.row) ? true : false;
        if (!canAdd) throw new SumExceptiom("Невозможное суммирование");
        else{
            Matrix X = new Matrix(A.row, A.col);
            if (A.round > B.round) { X.round = A.round; } else { X.round = B.round; }
            for (int i = 0; i < A.row; i++)
                for (int j = 0; j < A.col; j++)
                    X.matr[i][j] = A.matr[i][j] + B.matr[i][j];
            return X;
        }
    }

    public static Matrix subtract(Matrix A, Matrix B) throws SumExceptiom{
        boolean canAdd = (A.col == B.col) && (A.row == B.row) ? true : false;
        if (!canAdd) { throw new SumExceptiom("Невозможное вычитание"); }
        else{
            Matrix X = new Matrix(A.row, A.col);
            if (A.round > B.round) { X.round = A.round; } else { X.round = B.round; }
            for (int i = 0; i < A.row; i++)
                for (int j = 0; j < A.col; j++)
                    X.matr[i][j] = A.matr[i][j] - B.matr[i][j];
            return X;
        }
    }

    public static Matrix multiblyByNumber(Matrix M, double n){
        Matrix X = new Matrix(M.row, M.col);
        X.round = M.round;
        for (int i = 0; i < M.row; i++)
            for (int j = 0; j < M.col; j++)
                X.matr[i][j] = M.matr[i][j] * n;
        return X;
    }

    public static Matrix multiplyByMatrix(Matrix A, Matrix B) throws MultException{
        boolean canMultiply = A.col == B.row ? true : false;
        if (!canMultiply) { throw new MultException("Невозможное умножение"); }
        else{
            Matrix X = new Matrix(A.row, B.col);
            if (A.round > B.round) { X.round = A.round; } else { X.round = B.round; }
            for (int i = 0; i < A.row; i++)
                for (int j = 0; j < B.col; j++)
                    for (int k = 0; k < B.row; k++)
                        X.matr[i][j] = X.matr[i][j] + A.matr[i][k] * B.matr[k][j];
            return X;
        }
    }

    public Matrix transpose(){
        Matrix X = new Matrix(col, row);
        X.round = round;
        for (int i = 0; i < row; i++)
            for (int j = 0; j < col; j++)
                X.matr[j][i] = matr[i][j];
        return X;
    }

    public Matrix minor(int row, int col){
        //Разложение по строке
        boolean flagRow = false;
        boolean flagCol = false;
        Matrix X = new Matrix(this.row - 1, this.col - 1);
        for (int i = 0; i < this.row; i++){
            int z = i;
            //Разложение идет по строке, поэтому при просмотре каждой новой строки
            //флаг столбца надо сбрасывать
            flagCol = false;
            if (i == row){
                flagRow = true;
                continue;
            }
            if (flagRow) { z = i - 1; }

            for (int j = 0; j < this.col; j++){
                int s = j;
                if (j == col){
                    flagCol = true;
                    continue;
                }
                if (flagCol) { s = j - 1; }
                X.matr[z][s] = matr[i][j];
            }
        }
        return X;
    }

    protected void rounding(){
        for(int i=0; i<row; i++)
            for(int j=0; j<col;j++)
                matr[i][j] = round(matr[i][j],round);
    }

    static protected double round(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        double tmp = number * pow;
        return (double) (int) ((tmp - (int) tmp) >= 0.5 ? tmp + 1 : tmp) / pow;
    }

    public int rang() throws Exception {
        if(isZero()) return 0;
        Matrix X = Algorithms.Gauss(this);
        X.excludeisZeroColRow();
        return X.rows();
    }

    //endregion

    //region Determinant

    public double determinant(boolean write) throws IncompabilityOfColumnsAndRows{
        if (col != row)
            throw new IncompabilityOfColumnsAndRows("Матрица не квадратная");
        else
            return determinant(this, write);
    }

    private double determinant(Matrix X){
        double res = 0;
        if ((X.col == 2) && (X.row == 2))
            return X.matr[0][0] * X.matr[1][1] - X.matr[0][1] * X.matr[1][0];
        else if ((X.col == 1) && (X.row == 1))
            return X.matr[0][0];
        else{
            for (int j = 0; j < X.col; j++){
                Matrix J;
                if(!isZero())
                    J = X.minor(0, j);
                else
                    J = new Matrix(1,1);
                double minor = determinant(J);
                res = res + Math.pow(-1, j + 2) * X.matr[0][j] * minor;
            }
            return res;
        }
    }

    private double determinant(Matrix X, boolean write){
        double res = 0;
        String dot = String.valueOf((char)183);
        if(write){
            if (X.col == 3){
                X.message = new StringBuilder(nf.format(matr[0][0]) + dot + nf.format(matr[1][1]) + dot + nf.format(matr[2][2]) + " + "
                        + nf.format(matr[0][1]) + dot + nf.format(matr[1][2]) + dot + nf.format(matr[2][0]) + " + "
                        + nf.format(matr[1][0]) + dot + nf.format(matr[2][1]) + dot + nf.format(matr[0][2]) + " - "
                        + nf.format(matr[0][2]) + dot + nf.format(matr[1][1]) + dot + nf.format(matr[2][0]) + " - "
                        + nf.format(matr[1][0]) + dot + nf.format(matr[0][1]) + dot + nf.format(matr[2][2]) + " - "
                        + nf.format(matr[2][1]) + dot + nf.format(matr[1][2]) + dot + nf.format(matr[0][0]));
            }
            if (X.col == 2){
                X.message = new StringBuilder(nf.format(matr[0][0]) + dot + nf.format(matr[1][1]) +
                        " - " + nf.format(matr[0][1]) + dot + nf.format(matr[1][0]));
            }
        }
        if ((X.col == 2) && (X.row == 2))
            return X.matr[0][0] * X.matr[1][1] - X.matr[0][1] * X.matr[1][0];
        else if ((X.col == 1) && (X.row == 1))
            return X.matr[0][0];
        else{
            for (int j = 0; j < X.col; j++){
                Matrix J = X.minor(0, j);
                double minor = determinant(J);
                res = res + Math.pow(-1, j + 2) * X.matr[0][j] * minor;
            }
            return res;
        }
    }
    //endregion

    //region Rows operations
    public void swapRows(int r1, int r2) throws IncompabilityOfColumnsAndRows{
        if ((row < r1) | (row < r2) | (r1 < 0) | (r2 < 0))
            throw new IncompabilityOfColumnsAndRows("Invalid index");
        if (r1 == r2)
            return;
        else{
            double[][] Matr;
            Matr = copy(this);
            for (int j = 0; j < col; j++){
                double x1 = Matr[r1][j];
                double x2 = Matr[r2][j];
                Matr[r1][j] = x2;
                Matr[r2][j] = x1;
            }
            matr = Matr;
        }
    }

    public void addRowMultiplyedByNumber(int row1, double n, int row2){
        for (int j = 0; j < col; j++)
            matr[row2][j] = matr[row2][j] + matr[row1][j] * n;
    }

    public void multiplyRowByNumber(int r, double n) throws IncompabilityOfColumnsAndRows{
        if ((row < r) | (r < 0))
            throw new IncompabilityOfColumnsAndRows("Invalid index");
        else
            for (int j = 0; j < col; j++)
                matr[r][j] = matr[r][j] * n;
    }

    public void divRowByNumber(int r, double n) throws IncompabilityOfColumnsAndRows{
        if ((row < r) | (r < 0))
            throw new IncompabilityOfColumnsAndRows("Invalid index");
        else
            for (int j = 0; j < col; j++)
                matr[r][j] = (matr[r][j]) * (1 / n);
    }

    public void addNumberToRow(int r, double n) throws IncompabilityOfColumnsAndRows{
        if ((row < r) | (r < 0))throw new IncompabilityOfColumnsAndRows("Invalid index");
        else
            for (int j = 0; j < col; j++)
                matr[r][j] = matr[r][j] + n;
    }

    public void subNumberFromRow(int r, double n) throws IncompabilityOfColumnsAndRows{
        if ((row < r) | (r < 0))
            throw new IncompabilityOfColumnsAndRows("Invalid index");
        else
            for (int j = 0; j < col; j++)
                matr[r][j] = matr[r][j] - n;
    }

    public void deleteRow(int r) throws IncompabilityOfColumnsAndRows{
        if ((r > row) | (r < 0))
            throw new IncompabilityOfColumnsAndRows("Row number is negative or more then number of rows in MatrixLib.matrix");
        Matrix X = new Matrix(row - 1, col);
        boolean flag = false;
        for (int i = 0; i < row; i++){
            int z = i;
            if (i == r) { flag = true; continue; }
            if (flag) { z = z - 1; }
            for (int j = 0; j < col; j++)
                X.matr[z][j] = matr[i][j];
        }
        matr = X.matr;
        col = X.col;
        row = X.row;
    }

    private void deleteRows(List<Integer> dRows) throws IncompabilityOfColumnsAndRows{
        if (dRows.size() > row) throw new IncompabilityOfColumnsAndRows("Row number is negative or more then number of rows in MatrixLib.matrix");
        Matrix X = new Matrix(row - dRows.size(), col);
        int z = 0;
        int n = 0;
        for (int i = 0; i < row; i++){
            z = i - n;
            if (dRows.contains(i)) { n++; continue; }
            for (int j = 0; j < col; j++)
                X.matr[z][j] = matr[i][j];
        }
        matr = X.matr;
        col = X.col;
        row = X.row;
    }

    protected void excludeZeroRow() throws Exception{
        if (isZero()) {
            matr = new double[1][1];
            return;
        }
        List<Integer> RowToDelete = new ArrayList<Integer>();
        boolean zero;
        for(int i=0; i<row; i++){
            zero = true;
            for(int j=0; j<col; j++){
                if(matr[i][j]!=0){
                    zero = false;
                    break;
                }
            }
            if(zero)
                RowToDelete.add(i);
        }
        deleteRows(RowToDelete);
//        List<Integer> RowToDelete = new ArrayList<Integer>();
//        Matrix V = new Matrix(col, 1);
//        for (int i = 0; i < V.row; i++)
//            V.matr[i][0] = 1;
//        Matrix zRow = multiplyByMatrix(this,V);
//        for (int i = 0; i < zRow.row; i++)
//            if (zRow.matr[i][0] == 0)
//                RowToDelete.add(i);
//        deleteRows(RowToDelete);
    }

    protected List<Integer> excludeZeroRow2() throws Exception{
        if (isZero()) {
            matr = new double[1][1];
            return new ArrayList<>();
        }
        List<Integer> RowToDelete = new ArrayList<Integer>();
//        //я конечно очень хитрый, но для неквадратной матрицы не работает
//        Matrix V = new Matrix(col, 1);
//        for (int i = 0; i < V.row; i++)
//            V.matr[i][0] = 1;
//        Matrix zRow = multiplyByMatrix(this,V);
//        for (int i = 0; i < zRow.row; i++)
//            if (zRow.matr[i][0] == 0)
//                RowToDelete.add(i);
//        deleteRows(RowToDelete);
        boolean zero;
        for(int i=0; i<row; i++){
            zero = true;
            for(int j=0; j<col; j++){
                if(matr[i][j]!=0){
                    zero = false;
                    break;
                }
            }
            if(zero)
                RowToDelete.add(i);
        }
        deleteRows(RowToDelete);
        return RowToDelete;
    }

    //endregion

    //region Column operations

    public void swapColumns(int col1, int col2) throws IncompabilityOfColumnsAndRows{
        if ((col < col1) | (col < col2) | (col1 < 0) | (col2 < 0) | (col1 == col2))
            throw new IncompabilityOfColumnsAndRows("Invalid index");
        else{
            for (int i = 0; i < row; i++){
                double x1 = matr[i][col1];
                double x2 = matr[i][col2];
                matr[i][col1] = x2;
                matr[i][col2] = x1;
            }
        }
    }

    public void multiplyColByNumber(int col, double n) throws MultException{
        if ((this.col < col) | (col < 0))
            throw new MultException("Invalid multiplication");
        else
            for (int i = 0; i < row; i++)
                matr[i][col] = matr[i][col] * n;
    }

    public void divColByNumber(int col, double n) throws IncompabilityOfColumnsAndRows{
        if ((this.col < col) | (col < 0))
            throw new IncompabilityOfColumnsAndRows("Invalid index");
        else
            for (int i = 0; i < row; i++)
                matr[i][col] = matr[i][col] / n;
    }

    public void addNumberToCol(int col, double n) throws IncompabilityOfColumnsAndRows{
        if ((this.col < col) | (col < 0))
            throw new IncompabilityOfColumnsAndRows("Invalid index");
        else
            for (int i = 0; i < row; i++)
                matr[i][col] = matr[i][col] + n;
    }

    public void subNumberFromCol(int col, double n) throws IncompabilityOfColumnsAndRows{
        if ((this.col < col) | (col < 0))
            throw new IncompabilityOfColumnsAndRows("Invalid index");
        else
            for (int i = 0; i < row; i++)
                matr[i][col] = matr[i][col] - n;
    }

    public void deleteCol(int col) throws IncompabilityOfColumnsAndRows{
        if ((col > this.col) | (col < 0))
            throw new IncompabilityOfColumnsAndRows("Col number is negative or more then number of columns in MatrixLib.matrix");
        Matrix X = new Matrix(this.row, this.col - 1);
        boolean flag = false;
        for (int j = 0; j < this.col; j++){
            int z = j;
            if (j == col) {
                flag = true;
                continue;
            }
            if (flag) { z = z - 1; }
            for (int i = 0; i < this.row; i++)
                X.matr[i][z] = this.matr[i][j];
        }
        this.matr = X.matr;
        this.col = X.col;
        this.row = X.row;
    }

    public void addColMultiplyedByNumber(int col1, double n, int col2){
        for (int i = 0; i < row; i++)
            matr[i][col2] = matr[i][col2] + matr[i][col1] * n;
    }

    public void excludeisZeroCol() throws Exception{
        if (isZero()) {
            matr = new double[1][1];
            return;
        }
        List<Integer> ColToDelete = new ArrayList<Integer>();
        Matrix Trans = this.transpose();
        Matrix V = new Matrix(Trans.col, 1);
        for (int i = 0; i < V.row; i++)
            V.matr[i][0] = 1;
        Matrix zCol = multiplyByMatrix(Trans,V);
        for (int i = 0; i < zCol.row; i++)
            if (zCol.matr[i][0] == 0)
                ColToDelete.add(i);
        for (int j : ColToDelete)
            this.deleteCol(j);
    }

    //endregion

    //Power
    public Matrix power(int n) throws Exception{
        if(col!=row) throw new Exception("Матрица не квадратная");
        if(n==0){
            Matrix E = new Matrix(eMatrix(row,col));
            //Send message here
            return E;
        }
        if(isZero()){
            this.message = new StringBuilder("Нулевая матрица");
            //Send message here
            return null;
        }
        if(n<0){
            //в отрицательную степень тоже надо возводить
            Matrix X1 = new Matrix(1, 1);
            X1.round = round;
            X1 = inverseByGaussGordan();
            Matrix X2 = new Matrix(row, col);
            X2.matr = copy(this);
            X2.round = round;
            while (n<-1){
                X2 = multiplyByMatrix(X1,X2);
                n++;
            }
            //Send message here
            return X2;
        }
        else{
            Matrix X = new Matrix(row, col);
            X.matr = copy(this);
            X.round = round;
            while (n > 1){
                X = multiplyByMatrix(this, X);
                if (X == null) { return null; }
                n--;
            }
            //Send message here
            return X;
        }
    }

    //region Inverse Matrix

    public Matrix inverseByGaussGordan() throws Exception{
        double det = 0;
        if (col != row)
            throw new IncompabilityOfColumnsAndRows("Матрица не является квадратной");
        det = determinant(false);
        if (det == 0){ throw new IncompabilityOfColumnsAndRows("Матрица вырожденная - определитель равен 0");}
        else{
            int c = col;
            Matrix X = specialMatrix(this);
            X.round = round;
            X.name = name + "_E";
            X = Algorithms.gaussJordan(X);
            onlyInverse(X, c);
            //Send message here
            return X;
        }
    }

    private Matrix specialMatrix(Matrix This){
        Matrix X = new Matrix(row, col * 2);
        for(int i=0; i<This.row; i++)
            for(int j=0; j<This.col; j++)
                X.matr[i][j] = This.matr[i][j];
        int n = X.col - This.col;
        for(int i = 0; i<This.row; i++)
            X.matr[i][i+n] = 1;
        return X;
    }

    private void onlyInverse(Matrix G, int n){
        double[][] inv = new double [G.row][n];
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++)
                inv[i][j] = G.matr[i][n + j];
        G.matr = inv;
        G.col = n;
        G.row = n;
    }

    //endregion

    //region Help methods
    public void excludeisZeroColRow() throws Exception{
        if (isZero()) {
            matr = new double[1][1];
            return;
        }
        List<Integer> RowToDelete = new ArrayList<Integer>();
        List<Integer> ColToDelete = new ArrayList<Integer>();
        Matrix V = new Matrix(col, 1);
        for (int i = 0; i < V.row; i++)
            V.matr[i][0] = 1;
        Matrix zRow = multiplyByMatrix(this,V);
        for (int i = 0; i < zRow.row; i++)
            if (zRow.matr[i][0] == 0)
                RowToDelete.add(i);
        Matrix Trans = this.transpose();
        V = new Matrix(Trans.col, 1);
        for (int i = 0; i < V.row; i++)
            V.matr[i][0] = 1;
        Matrix zCol = multiplyByMatrix(Trans,V);
        for (int i = 0; i < zCol.row; i++)
            if (zCol.matr[i][0] == 0)
                ColToDelete.add(i);
        deleteColArray(ColToDelete);
        deleteRowArray(RowToDelete);
    }

    private void deleteRowArray(List<Integer> rows) throws IncompabilityOfColumnsAndRows{
        Matrix X = new Matrix(row - rows.size(), col);
        int xi = 0;
        int n = 0;
        for (int i = 0; i < row; i++)
        {
            xi = i - n;
            if (rows.contains(i)){
                n++;
                continue;
            }
            for (int j = 0; j < col; j++){
                X.matr[xi][j] = matr[i][j];
            }
        }
        matr = X.matr;
        col = X.col;
        row = X.row;
    }

    private void deleteColArray(List<Integer> cols) throws IncompabilityOfColumnsAndRows{
        Matrix X = new Matrix(row, col - cols.size());
        int xj = 0;
        int n = 0;
        for (int j = 0; j < col; j++){
            xj = j - n;
            if (cols.contains(j)){
                n++;
                continue;
            }
            for (int i = 0; i < row; i++){
                X.matr[i][xj] = matr[i][j];
            }
        }
        matr = X.matr;
        col = X.col;
        row = X.row;
    }

    protected double[] findLeadElem(int sRow, int col){
        double[] res = new double[2];
        //res[0] - row, res[1] - elem
        for (int i = sRow; i < row; i++)
            if (matr[i][col] != 0) {
            res[0] = i; res[1] = matr[i][sRow];
            return res;
        }
        res[0] = 0;
        res[1] = matr[0][0];
        return res;
    }

    public double getElem(int row, int col){
        return matr[row][col];
    }

    public String getSElem(int row,int col){
        return nf.format(matr[row][col]);
    }

    protected static double[][]copy(Matrix X){
        double[][] matrCopy = new double [X.row][X.col];
        for(int i=0; i<X.row;i++)
            for(int j=0;j<X.col;j++)
                matrCopy[i][j] = X.matr[i][j];
        return matrCopy;
    }

    private double[][]eMatrix(int col,int row)throws Exception{
        if(col!=row) throw new Exception("Матрица не является квадратной");
        double[][] E = new double[row][col];
        for(int i=0; i<row;i++)
            E[i][i] = 1;
        return E;
    }

    public List<Integer> getOnesColumns() {
        List<Integer> cols = new ArrayList<>();
        for (int j = 0; j < col; j++) {
            int r = -1;
            for (int i = 0; i < row; i++) {
                if (matr[i][j] == 1)
                    r = i;
                if (matr[i][j] == 0)
                    if ((r != -1) & (matr[i][j] != 0)) break;
            }
            cols.add(r);
        }
        return cols;
    }

    public List<Integer> getZeroColumns(){
        boolean zero;
        List<Integer> zeros = new ArrayList<>();
        for (int j=0; j<col; j++){
            zero = true;
            for(int i=0; i<row; i++){
                if(matr[i][j]!=0){
                    zero = false;
                    break;
                }
            }
            if (zero) zeros.add(j);
        }
        return zeros;
    }

    //endregion
}
