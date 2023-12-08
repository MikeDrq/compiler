package Middle.Value.BasicBlock;

public class BasicBlockCnt {
    private int cnt;
    public BasicBlockCnt() {
        this.cnt = 0;
    }

    public int getCnt() {
        int num = cnt;
        cnt++;
        return num;
    }
}
