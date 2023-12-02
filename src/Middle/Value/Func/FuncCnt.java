package Middle.Value.Func;

public class FuncCnt {
    private int cnt;
    public FuncCnt() {
        this.cnt = 0;
    }

    public int getCnt() {
        int num = cnt;
        cnt++;
        return num;
    }

    public int fetchCnt() {
        return cnt;
    }
}
