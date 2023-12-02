package Error;

public class Error {
    private int lineNumber;
    private String type;

    public Error(Integer lineNumber,String type) {
        this.lineNumber = lineNumber;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return lineNumber + " " + type;
    }
}
