package Middle.Type;

public class IntType extends ValueType {
    private int num;
    public IntType(int num) {
        this.num = num;
    }

    @Override
    public String midOutput() {
        if (this.num == 32) {
            return "i32";
        } else {
            return "";
        }
    }
}
