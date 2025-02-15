package info.teksol.mindcode.processor;

import info.teksol.mindcode.compiler.CompilerMessage;
import info.teksol.mindcode.compiler.CompilerProfile;
import info.teksol.mindcode.compiler.LogicInstructionLabelResolver;
import info.teksol.mindcode.compiler.instructions.LogicInstruction;
import info.teksol.mindcode.compiler.optimization.AbstractOptimizerTest;
import info.teksol.mindcode.compiler.optimization.Optimization;
import info.teksol.mindcode.compiler.optimization.OptimizationLevel;
import info.teksol.mindcode.compiler.optimization.Optimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Base class for algorithm tests
// Processor for execution is equipped with bank1 memory bank.
// Additional blocks can be added
public abstract class AbstractProcessorTest extends AbstractOptimizerTest<Optimizer> {

    protected abstract String getScriptsDirectory();

    private static final List<String> performance = new ArrayList<>();

    static void init() {
        performance.clear();
    }

    static void done(String scriptsDirectory, String className) throws IOException {
        Path path = Path.of(scriptsDirectory, className + ".txt");
        Collections.sort(performance);
        Files.write(path, performance);
    }

    private void logPerformance(String title, Processor processor) {
        int coverage = 1000 * processor.getCoverage().cardinality() / processor.getInstructions();
        String name = title != null ? title : testInfo.getDisplayName().replaceAll("\\(\\)", "");
        String info = String.format("Test %-40s %4d instructions, %6d steps, %3d.%01d%% coverage",
                name + ":", processor.getInstructions(), processor.getSteps(), coverage / 10, coverage % 10);
        System.out.println(info);
        performance.add(info);
    }

    @Override
    protected Class<Optimizer> getTestedClass() {
        return null;
    }

    @Override
    protected List<Optimization> getAllOptimizations() {
        return Optimization.LIST;
    }

    // Prevent unit tests hanging due to possible endless loops in generated code
    protected final int MAX_STEPS = 1000000;

    private TestInfo testInfo;

    @BeforeEach
    void init(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    protected String readFile(String filename) throws IOException {
        Path path = Path.of(getScriptsDirectory(), filename);
        return Files.readString(path);
    }

    @Override
    protected CompilerProfile createCompilerProfile() {
        CompilerProfile profile = super.createCompilerProfile();
        profile.setAllOptimizationLevels(OptimizationLevel.AGGRESSIVE);
        // Do not remove end instructions
        profile.setOptimizationLevel(Optimization.JUMP_TARGET_PROPAGATION, OptimizationLevel.BASIC);
        // Do not merge constants in print statements
        profile.setOptimizationLevel(Optimization.PRINT_TEXT_MERGING, OptimizationLevel.BASIC);
        return profile;
    }

    protected List<LogicInstruction> compile(TestCompiler compiler, String code) {
        return LogicInstructionLabelResolver.resolve(compiler.processor, generateInstructions(compiler, code).instructions());
    }

    protected void testAndEvaluateCode(TestCompiler compiler, String title, String code, List<MindustryObject> blocks, Consumer<List<String>> evaluator) {
        Processor processor = new Processor();
        processor.addBlock(MindustryMemory.createMemoryBank("bank1"));
        processor.addBlock(MindustryMemory.createMemoryBank("bank2"));
        blocks.forEach(processor::addBlock);
        List<LogicInstruction> instructions = compile(compiler, code);
        //System.out.println(prettyPrint(instructions));
        processor.run(instructions, MAX_STEPS);
        logPerformance(title, processor);
        //System.out.println(String.join("", processor.getTextBuffer()));
        assertAll(
                () -> evaluator.accept(processor.getTextBuffer()),
                () -> assertNoUnexpectedMessages(compiler, s -> false)
        );
    }

    protected void testAndEvaluateCode(String title, String code, List<MindustryObject> blocks, Consumer<List<String>> evaluator) {
        testAndEvaluateCode(createTestCompiler(), title, code, blocks, evaluator);
    }

    protected void testAndEvaluateFile(String fileName, List<MindustryObject> blocks, Consumer<List<String>> evaluator) throws IOException {
        testAndEvaluateCode(fileName, readFile(fileName), blocks, evaluator);
    }

    protected void testCode(String title, String code, List<MindustryObject> blocks, List<String> expectedOutputs) {
        TestCompiler compiler = createTestCompiler();
        testAndEvaluateCode(compiler, title, code, blocks, outputs -> assertEquals(expectedOutputs, outputs,
                () -> compiler.messages.stream().map(CompilerMessage::message)
                        .collect(Collectors.joining("\n", "\n", "\n"))));
    }

    protected void testCode(String code, List<MindustryObject> blocks, List<String> expectedOutputs) {
        testCode(null, code, blocks, expectedOutputs);
    }

    protected void testCode(String code, String... expectedOutputs) {
        testCode(code, List.of(), List.of(expectedOutputs));
    }

    protected void testFile(String fileName, List<MindustryObject> blocks, List<String> expectedOutputs) throws IOException {
        testCode(fileName, readFile(fileName), blocks, expectedOutputs);
    }

    protected void testFile(String fileName, String... expectedOutputs) throws IOException {
        testFile(fileName, List.of(), List.of(expectedOutputs));
    }
}
