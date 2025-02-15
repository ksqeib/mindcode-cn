package info.teksol.mindcode.compiler.optimization;

import info.teksol.mindcode.compiler.instructions.InstructionProcessor;
import info.teksol.mindcode.compiler.instructions.JumpInstruction;
import info.teksol.mindcode.compiler.instructions.LogicInstruction;
import info.teksol.mindcode.compiler.instructions.PushOrPopInstruction;
import info.teksol.mindcode.compiler.instructions.SetInstruction;
import info.teksol.mindcode.logic.ArgumentType;
import info.teksol.mindcode.logic.LogicVariable;

import java.util.List;

/**
 * Removes unnecessary case expression variable ({@code __ast}) and replaces it with the original variable containing
 * the value of the case expression. The set instruction is removed, while the other instructions (one or more jump
 * instruction per {@code when} branch) are updated to replace the {@code __ast} variable with the one used
 * in the set statement.
 * <p>
 * The optimization is performed only when the following conditions are met:
 * <ol>
 * <li>The set instruction assigns to an {@code __ast} variable.</li>
 * <li>The set instruction is the first of all those using the {@code __ast} variable (the check is based on absolute
 * instruction sequence in the program, not on the actual program flow).</li>
 * <li>Each subsequent instruction using the {@code __ast} variable conforms to the code generated by the compiler
 * (i.e. has the form of {@code jump target <condition> __astX testValue})</li>
 * </ol>
 * Push and pop instructions are ignored by the above algorithm. Push/pop instructions of any eliminated variables
 * are removed by the stack usage optimization down the line.
 */

class CaseExpressionOptimizer extends BaseOptimizer {
    public CaseExpressionOptimizer(InstructionProcessor instructionProcessor) {
        super(instructionProcessor);
    }

    @Override
    protected boolean optimizeProgram() {
        try (LogicIterator it = createIterator()) {
            while (it.hasNext()){
                if (it.next() instanceof SetInstruction ix && ix.getResult().getType() == ArgumentType.AST_VARIABLE) {
                    LogicVariable result = ix.getResult();
                    List<LogicInstruction> list = instructions(
                            in -> in.getArgs().contains(result) && !(in instanceof PushOrPopInstruction));

                    // The set instruction is not the first one
                    if (list.get(0) != ix) continue;

                    // Some of the other instructions aren't part of the case expression
                    if (!list.stream().skip(1).allMatch(in -> isStandardCaseWhenInstruction(in, result))) continue;

                    // Replace __ast with actual value in all case branches
                    list.stream().skip(1).forEach(in -> replaceInstruction(in, replaceAllArgs(in, result, ix.getValue())));
                    it.remove();
                }
            }
        }

        return false;
    }

    private boolean isStandardCaseWhenInstruction(LogicInstruction instruction, LogicVariable ast) {
        return instruction instanceof JumpInstruction ix && ix.getX().equals(ast);
    }
}
