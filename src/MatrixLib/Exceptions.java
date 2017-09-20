package MatrixLib;

/**
 * Created by Barmin on 31.12.2016.
 */

class SumExceptiom extends Exception{
    public SumExceptiom(String message){
        super(message);
    }
}

class MultException extends Exception{
    public MultException(String message){
        super(message);
    }
}

class IncompabilityOfColumnsAndRows extends Exception{
    public IncompabilityOfColumnsAndRows(String message){
        super(message);
    }
}

class IncompabilitySystem extends Exception{
    public IncompabilitySystem(String message){
        super(message);
    }
}

class WrongBasis extends Exception{
    public WrongBasis(String message){
        super(message);
    }
}

