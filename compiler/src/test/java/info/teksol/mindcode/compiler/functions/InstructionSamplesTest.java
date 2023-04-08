package info.teksol.mindcode.compiler.functions;

import info.teksol.mindcode.compiler.AbstractGeneratorTest;
import info.teksol.mindcode.compiler.LogicInstructionPrinter;
import info.teksol.mindcode.compiler.instructions.InstructionProcessor;
import info.teksol.mindcode.compiler.instructions.InstructionProcessorFactory;
import info.teksol.mindcode.compiler.instructions.LogicInstruction;
import info.teksol.mindcode.logic.ArgumentType;
import info.teksol.mindcode.logic.NamedArgument;
import info.teksol.mindcode.logic.Opcode;
import info.teksol.mindcode.logic.OpcodeVariant;
import info.teksol.mindcode.logic.ProcessorVersion;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static info.teksol.mindcode.logic.ProcessorEdition.W;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test produces files containing permutations of instructions and their arguments allowed by metadata.
 * The produced files (Instruction_Samples_*.txt) should then be pasted into a Mindustry processor editor
 * (World processor when available, otherwise a standard processor) and then copied back into a different text file.
 * There should be no differences between the file generated by this code and the file obtained from Mindustry
 * Processor editor. Any difference means the metadata in this project are flawed.
 */
public class InstructionSamplesTest extends AbstractGeneratorTest {

    @Test
    void createInstructionSamplesForV6() throws FileNotFoundException {
        createInstructionSamples(ProcessorVersion.V6);
    }

    @Test
    void createInstructionSamplesForV7() throws FileNotFoundException {
        createInstructionSamples(ProcessorVersion.V7);
    }

    private void createInstructionSamples(ProcessorVersion version) throws FileNotFoundException {
        assertTrue(new File(".." + File.separatorChar + "README.markdown").isFile());
        InstructionProcessor processor = InstructionProcessorFactory.getInstructionProcessor(version, W);
        List<LogicInstruction> instructions = processor.getOpcodeVariants().stream()
                .filter(v -> !v.getOpcode().isVirtual())
                .flatMap(v -> createOpcodeSamples(processor, v).stream())
                .collect(Collectors.toList());

        try (final PrintWriter w = new PrintWriter(".." + File.separatorChar + "Instruction_Samples_" + version + ".txt")) {
            w.print(LogicInstructionPrinter.toString(processor, instructions));
        }
    }

    private List<LogicInstruction> createOpcodeSamples(InstructionProcessor processor, OpcodeVariant opcodeVariant) {
        if (opcodeVariant.getOpcode() == Opcode.LABEL) {
            return List.of();
        }

        ProcessorVersion processorVersion = processor.getProcessorVersion();
        List<LogicInstruction> result = new ArrayList<>();
        List<List<String>> combinations = new ArrayList<>();

        for (NamedArgument arg : opcodeVariant.getArguments()) {
            if (arg.getType() == ArgumentType.LABEL) {
                combinations.add(List.of("0"));
            } else if (arg.getType().isConst()) {
                combinations.add(arg.getType().getAllowedValues().stream()
                        .filter(v -> v.versions.contains(processorVersion))
                        .flatMap(v -> v.values.stream())
                        .sorted()
                        .collect(Collectors.toUnmodifiableList()));
            } else {
                combinations.add(List.of(arg.getName()));
            }
        }
        int variants = combinations.stream()
                .mapToInt(List::size)
                .max().orElse(1);

        for (int i = 0; i < variants; i++) {
            final int index = i;
            List<String> params = combinations.stream()
                    .map(l -> l.get(index % l.size()))
                    .collect(Collectors.toList());

            result.add(processor.createInstruction(opcodeVariant.getOpcode(), params));
        }

        return result;
    }
}
