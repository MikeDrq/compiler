package Mips.MipsFunc;

public class StringCnt {
    private int num;
    public StringCnt() {
        this.num = 0;
    }

    public int getNum() {
        int ret = num;
        num++;
        return ret;
    }
}
