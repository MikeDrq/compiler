package Error;

import java.util.ArrayList;

public class ErrorTable {
    public ArrayList<Error> errors;

    public ErrorTable() {
        this.errors = new ArrayList<>();
    }

    public void addError(Error error) {
        errors.add(error);
    }

    public ArrayList<Error> getErrors() {
        return errors;
    }
}
