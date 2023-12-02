package Mips;

import java.util.ArrayList;
import java.util.HashMap;

public class Register {
    private HashMap<Integer,String> registers;
    private HashMap<Integer,Boolean> canUse = new HashMap<>();

    public Register() {
        registers = new HashMap<>();
        registers.put(0,"$zero");
        registers.put(1,"$at");
        registers.put(2,"$v0");
        registers.put(3,"$v1");
        registers.put(4,"$a0");
        registers.put(5,"$a1");
        registers.put(6,"$a2");
        registers.put(7,"$a3");
        for (int i = 0;i <= 7;i++) {
            registers.put(8+i,"$t" + i);
            registers.put(16+i,"$s" + i);
        }
        registers.put(24,"$t8");
        registers.put(25,"$t9");
        registers.put(26,"$k0");
        registers.put(27,"$k1");
        registers.put(28,"$gp");
        registers.put(29,"$sp");
        registers.put(30,"$fp");
        registers.put(31,"$ra");
        for (int i = 0;i <= 31;i++) {
            canUse.put(i,true);
        }
    }

    public String getRegister(int num) {
        return registers.get(num);
    }

    public Boolean canUseNow(int num) {
        return canUse.get(num);
    }

    public void useRegister(int num) {
        canUse.put(num,false);
    }

    public void freeRegister(int num) {
        canUse.put(num,true);
    }
}
