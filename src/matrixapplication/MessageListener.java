package matrixapplication;

import MatrixLib.Matrix;

/**
 * Created by alexander on 16.09.17.
 */
public interface MessageListener {

    public void onMessage(StringBuilder str);

    public void onMatrixChange(Matrix M);
}
