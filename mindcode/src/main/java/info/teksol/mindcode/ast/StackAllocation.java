package info.teksol.mindcode.ast;

import info.teksol.mindcode.compiler.instructions.AstContextType;
import info.teksol.mindcode.logic.LogicVariable;
import org.antlr.v4.runtime.Token;

import java.util.Objects;

public class StackAllocation extends BaseAstNode {
    private final LogicVariable stack;
    private final Range range;

    StackAllocation(Token startToken, String stack, Range range) {
        super(startToken);
        this.stack = LogicVariable.block(stack);
        this.range = range;
    }

    StackAllocation(Token startToken, String stack, int first, int last) {
        this(startToken, stack, new InclusiveRange(startToken,
                new NumericLiteral(startToken, first), new NumericLiteral(startToken, last)));
    }

    StackAllocation(Token startToken, String stack) {
        super(startToken);
        this.stack = LogicVariable.block(stack);
        this.range = null;
    }

    public LogicVariable getStack() {
        return stack;
    }

    public Range getRange() {
        return range;
    }

    public boolean hasRange() {
        return range != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackAllocation that = (StackAllocation) o;
        return Objects.equals(range, that.range)
                && Objects.equals(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack, range);
    }

    @Override
    public String toString() {
        return "StackAllocation{" +
                "stack='" + stack + '\'' +
                ", range=" + range +
                '}';
    }

    @Override
    public AstContextType getContextType() {
        return AstContextType.ALLOCATION;
    }
}
