package operators;

public enum OperatorType {
    SOURCE, COUNT, SINK, WINDOW;

    public static OperatorType toOperatorType(String type) {
        OperatorType res = null;
        switch (type) {
            case "SOURCE":
            case "source":
                res = SOURCE;
                break;
            case "COUNT":
            case "count":
                res = COUNT;
                break;
            case "SINK":
            case "sink":
                res = SINK;
                break;
        }
        return res;
    }

    public static boolean isInputStreamEnabled(OperatorType type) {
//        return type.equals(COUNT) || type.equals(SINK);
        return true;
    }

    public static boolean isOutputStreamEnabled(OperatorType type) {
//        return type.equals(SOURCE) || type.equals(COUNT);
        return true;
    }
}
