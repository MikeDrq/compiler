package Middle.Value.Instruction;

public enum InstructionType {
    add,  // +
    sub,  // -
    mul,  // *
    sdiv, // /
    srem, // %
    icmp,
    and,
    or,
    xor, //!
    call,
    alloca,
    load,
    store,
    getelementptr,
    phi,
    zext,
    trucn,
    br,
    ret
}
