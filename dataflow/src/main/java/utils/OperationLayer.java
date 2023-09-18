package utils;

import operators.OperatorType;

/**
 * util class
 * operator count and type
 */
public class OperationLayer {
    private int operationCnt ;
    private OperatorType type;

    public OperationLayer(int cnt, OperatorType type){
        this.operationCnt = cnt;
        this.type = type;
    }

    public int getOperationCnt() {
        return operationCnt;
    }

    public void setOperationCnt(int operationCnt) {
        this.operationCnt = operationCnt;
    }

    public OperatorType getType() {
        return type;
    }

    public void setType(OperatorType type) {
        this.type = type;
    }
}
