package info.teksol.mindcode.ast;

import info.teksol.mindcode.compiler.instructions.AstContextType;
import org.antlr.v4.runtime.Token;

import java.util.Objects;

public class PropertyAccess extends BaseAstNode {
    private final AstNode target;
    private final AstNode property;

    PropertyAccess(Token startToken, AstNode target, AstNode property) {
        super(startToken, target, property);
        this.target = target;
        this.property = property;
    }

    public AstNode getTarget() {
        return target;
    }

    public AstNode getProperty() {
        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyAccess that = (PropertyAccess) o;
        return Objects.equals(target, that.target) &&
                Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, property);
    }

    @Override
    public String toString() {
        return "PropertyAccess{" +
                "target=" + target +
                ", property='" + property + '\'' +
                '}';
    }

    @Override
    public AstContextType getContextType() {
        return AstContextType.PROPERTY;
    }
}
