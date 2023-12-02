package Mips;

public class GlobalLabelCnt {
    private int num;
    public GlobalLabelCnt() {
        this.num = 0;
    }

    public int getLabelCnt() {
        int mark = this.num;
        this.num++;
        return mark;
    }
}
