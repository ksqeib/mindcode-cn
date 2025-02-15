package info.teksol.mindcode.compiler.optimization;

import info.teksol.mindcode.compiler.instructions.InstructionProcessor;
import info.teksol.mindcode.compiler.instructions.JumpInstruction;
import info.teksol.mindcode.logic.LogicBoolean;

import static info.teksol.mindcode.logic.LogicBoolean.FALSE;
import static info.teksol.mindcode.logic.LogicBoolean.TRUE;

/**
 * Replaces conditional jumps whose condition is always true with unconditional jumps
 * and completely removes jumps that are always false.
 * Such conditions will only be generated by explicit code, e.g. {@code if false} or {@code while true}.
 * If a variable is involved (e.g. {@code ACTIVE = false; while ACTIVE ...}) the jump won't be removed/replaced.
 */
class ConditionalJumpsNormalizer extends BaseOptimizer {
    public ConditionalJumpsNormalizer(InstructionProcessor instructionProcessor) {
        super(instructionProcessor);
    }

    @Override
    protected boolean optimizeProgram() {
        try (LogicIterator iterator = createIterator()) {
            while (iterator.hasNext()) {
                if (iterator.next() instanceof JumpInstruction jump) {
                    if (alwaysTrue(jump)) {
                        iterator.set(createJumpUnconditional(jump.getAstContext(), jump.getTarget()));
                    } else if (alwaysFalse(jump)) {
                        iterator.remove();
                    }
                }
            }
        }

        return false;
    }

    private boolean alwaysTrue(JumpInstruction jump) {
        return switch (jump.getCondition()) {
            case EQUAL      -> hasArgs(jump, TRUE, TRUE) || hasArgs(jump, FALSE, FALSE);
            case NOT_EQUAL  -> hasArgs(jump, TRUE, FALSE) || hasArgs(jump, FALSE, TRUE);
            default         -> false;
        };
    }

    private boolean alwaysFalse(JumpInstruction jump) {
        return switch (jump.getCondition()) {
            case EQUAL      -> hasArgs(jump, TRUE, FALSE) || hasArgs(jump, FALSE, TRUE);
            case NOT_EQUAL  -> hasArgs(jump, TRUE, TRUE) || hasArgs(jump, FALSE, FALSE);
            default         -> false;
        };
    }

    private boolean hasArgs(JumpInstruction jump, LogicBoolean x, LogicBoolean y) {
        return jump.getX().equals(x) && jump.getY().equals(y);
    }
}
