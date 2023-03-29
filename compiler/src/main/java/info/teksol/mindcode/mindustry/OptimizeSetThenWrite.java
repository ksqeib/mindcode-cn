package info.teksol.mindcode.mindustry;

import java.util.LinkedHashMap;
import java.util.Map;

class OptimizeSetThenWrite implements LogicInstructionPipeline {
    private final LogicInstructionPipeline next;
    private State state;

    OptimizeSetThenWrite(LogicInstructionPipeline next) {
        this.next = next;
        this.state = new EmptyState();
    }

    @Override
    public void emit(LogicInstruction instruction) {
        state = state.emit(instruction);
    }

    @Override
    public void flush() {
        state = state.flush();
        next.flush();
    }

    private interface State {
        State emit(LogicInstruction instruction);

        State flush();
    }

    private final class EmptyState implements State {
        @Override
        public State emit(LogicInstruction instruction) {
            if (instruction.isSet()) {
                // This guard protects against losing constants:
                //
                // HEAPPTR = cell3
                // $a = 0
                //
                // The unoptimized code is:
                //
                //     set HEAPPTR cell3
                //     set tmp0 0
                //     set tmp1 0
                //     write tmp1 HEAPPTR tmp0
                //
                // But when the full optimization pipeline is used, the code is optimized to:
                //
                //     write 1 cell3 0
                //
                // We lose the constant.
                //
                // This guard merely ensures that the optimization turns on only for variables generated by the
                // Mindcode compiler. Named values, those used by the programmer, are left as-is.
                if (instruction.getArgs().get(0).startsWith(LogicInstructionGenerator.TMP_PREFIX)) {
                    return new ExpectWrite(instruction);
                } else {
                    next.emit(instruction);
                    return this;
                }
            } else {
                next.emit(instruction);
                return this;
            }
        }

        @Override
        public State flush() {
            return this;
        }
    }

    private final class ExpectWrite implements State {
        // It is critical that this be a LinkedHashMap, otherwise we would emit set instructions in a different order
        // than the original order in the source code, with disastrous consequences.
        private final Map<String, String> sets = new LinkedHashMap<>();

        ExpectWrite(LogicInstruction set) {
            sets.put(set.getArgs().get(0), set.getArgs().get(1));
        }

        @Override
        public State emit(LogicInstruction instruction) {
            if (instruction.isSet()) {
                // Same guard as above: protect against inadvertently replacing a programmer named value and using a
                // Mindcode temp value instead.
                if (instruction.getArgs().get(0).startsWith(LogicInstructionGenerator.TMP_PREFIX)) {
                    sets.put(instruction.getArgs().get(0), instruction.getArgs().get(1));
                    return this;
                } else {
                    flushPending(instruction);
                    return new EmptyState();
                }

            } else if (instruction.isWrite()) {
                if (sets.containsKey(instruction.getArgs().get(0))) {
                    final String value = instruction.getArgs().get(0);
                    instruction = new LogicInstruction(instruction.getOpcode(), sets.get(value), instruction.getArgs().get(1), instruction.getArgs().get(2));
                    sets.remove(value);
                }

                if (sets.containsKey(instruction.getArgs().get(1))) {
                    final String destination = instruction.getArgs().get(1);
                    instruction = new LogicInstruction(instruction.getOpcode(), instruction.getArgs().get(0), sets.get(destination), instruction.getArgs().get(2));
                    sets.remove(destination);
                }

                if (sets.containsKey(instruction.getArgs().get(2))) {
                    final String address = instruction.getArgs().get(2);
                    instruction = new LogicInstruction(instruction.getOpcode(), instruction.getArgs().get(0), instruction.getArgs().get(1), sets.get(address));
                    sets.remove(address);
                }
            }

            return flushPending(instruction);
        }

        @Override
        public State flush() {
            for (Map.Entry<String, String> entry : sets.entrySet()) {
                next.emit(new LogicInstruction("set", entry.getKey(), entry.getValue()));
            }

            return new EmptyState();
        }

        private State flushPending(LogicInstruction instruction) {
            final State nextState = flush();
            next.emit(instruction);
            return nextState;
        }
    }
}
