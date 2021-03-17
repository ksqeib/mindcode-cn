package info.teksol.mindcode.mindustry;

import info.teksol.mindcode.AbstractAstTest;
import info.teksol.mindcode.ast.Seq;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogicInstructionGeneratorTest extends AbstractAstTest {
    @Test
    void convertsComplexAssignment() {
        assertEquals(
                List.of(
                        new LogicInstruction("set", "tmp0", "2"),
                        new LogicInstruction("op", "sub", "tmp1", "bar", "tmp0"),
                        new LogicInstruction("set", "tmp2", "3"),
                        new LogicInstruction("op", "mul", "tmp3", "tmp1", "tmp2"),
                        new LogicInstruction("set", "foo", "tmp3"),
                        new LogicInstruction("end")
                ),
                LogicInstructionGenerator.generateFrom((Seq) translateToAst("foo = (bar - 2) * 3"))
        );
    }

    @Test
    void convertsWhileLoopAndPrintFunctionCall() {
        assertEquals(
                List.of(
                        new LogicInstruction("set", "tmp0", "0"),
                        new LogicInstruction("set", "n", "tmp0"),
                        new LogicInstruction("label", "label0"),
                        new LogicInstruction("set", "tmp1", "5"),
                        new LogicInstruction("op", "lessThan", "tmp2", "n", "tmp1"),
                        new LogicInstruction("jump", "label1", "notEqual", "tmp2", "true"),
                        new LogicInstruction("set", "tmp3", "1"),
                        new LogicInstruction("op", "add", "tmp4", "n", "tmp3"),
                        new LogicInstruction("set", "n", "tmp4"),
                        new LogicInstruction("jump", "label0", "always"),
                        new LogicInstruction("label", "label1"),
                        new LogicInstruction("set", "tmp5", "\"n: \""),
                        new LogicInstruction("print", "tmp5"),
                        new LogicInstruction("print", "n"),
                        new LogicInstruction("end")
                ),
                LogicInstructionGenerator.generateFrom((Seq) translateToAst("n = 0\nwhile n < 5 {\nn += 1\n}\nprint(\"n: \", n)"))
        );
    }

    @Test
    void convertsNullAndUnaryOp() {
        assertEquals(
                List.of(
                        new LogicInstruction("op", "not", "tmp0", "n"),
                        new LogicInstruction("set", "n", "tmp0"),
                        new LogicInstruction("set", "x", "null"),
                        new LogicInstruction("end")
                ),
                LogicInstructionGenerator.generateFrom((Seq) translateToAst("n = not n; x = null"))
        );
    }

    @Test
    void convertsSensorReadings() {
        assertEquals(
                List.of(
                        new LogicInstruction("sensor", "tmp0", "foundation1", "@copper"),
                        new LogicInstruction("sensor", "tmp1", "foundation1", "@itemCapacity"),
                        new LogicInstruction("op", "lessThan", "tmp2", "tmp0", "tmp1"),
                        new LogicInstruction("end")
                ),
                LogicInstructionGenerator.generateFrom((Seq) translateToAst("foundation1.copper < foundation1.itemCapacity"))
        );
    }

    @Test
    void convertsBooleanOperations() {
        assertEquals(
                List.of(
                        new LogicInstruction("label", "label0"),
                        new LogicInstruction("op", "notEqual", "tmp0", "true", "false"),
                        new LogicInstruction("jump", "label1", "notEqual", "tmp0", "true"),
                        new LogicInstruction("set", "tmp1", "\"infinite loop!\""),
                        new LogicInstruction("print", "tmp1"),
                        new LogicInstruction("jump", "label0", "always"),
                        new LogicInstruction("label", "label1"),
                        new LogicInstruction("printflush", "message1"),
                        new LogicInstruction("end")
                ),
                LogicInstructionGenerator.generateFrom((Seq) translateToAst("while true != false {\nprint(\"infinite loop!\")\n}\nprintflush(message1)\n"))
        );
    }

    @Test
    void convertsControlStatements() {
        assertEquals(
                List.of(
                        new LogicInstruction("sensor", "tmp0", "foundation1", "@copper"),
                        new LogicInstruction("sensor", "tmp1", "tank1", "@water"),
                        new LogicInstruction("op", "strictEqual", "tmp2", "tmp0", "tmp1"),
                        new LogicInstruction("control", "enabled", "conveyor1", "tmp2"),
                        new LogicInstruction("end")
                ),
                LogicInstructionGenerator.generateFrom((Seq) translateToAst("conveyor1.enabled = foundation1.copper === tank1.water"))
        );
    }

    @Test
    void convertsHeapAccesses() {
        assertEquals(
                List.of(
                        new LogicInstruction("set", "tmp0", "3"),
                        new LogicInstruction("set", "tmp1", "4"),
                        new LogicInstruction("read", "tmp2", "cell2", "tmp1"),
                        new LogicInstruction("sensor", "tmp3", "conveyor1", "@enabled"),
                        new LogicInstruction("op", "add", "tmp4", "tmp2", "tmp3"),
                        new LogicInstruction("write", "tmp4", "cell1", "tmp0"),
                        new LogicInstruction("end")
                ),
                LogicInstructionGenerator.generateFrom((Seq) translateToAst("cell1[3] = cell2[4] + conveyor1.enabled"))
        );
    }

    @Test
    void convertsIfExpression() {
        assertEquals(
                prettyPrint(
                        List.of(
                                new LogicInstruction("set", "tmp0", "4"),
                                new LogicInstruction("read", "tmp1", "cell1", "tmp0"),
                                new LogicInstruction("set", "tmp2", "0"),
                                new LogicInstruction("op", "equal", "tmp3", "tmp1", "tmp2"),
                                new LogicInstruction("jump", "label0", "notEqual", "tmp3", "true"),
                                new LogicInstruction("set", "tmp7", "false"),
                                new LogicInstruction("jump", "label1", "always"),
                                new LogicInstruction("label", "label0"),
                                new LogicInstruction("set", "tmp4", "4"),
                                new LogicInstruction("write", "true", "cell1", "tmp4"),
                                new LogicInstruction("set", "tmp5", "1"),
                                new LogicInstruction("op", "add", "tmp6", "n", "tmp5"),
                                new LogicInstruction("set", "n", "tmp6"),
                                new LogicInstruction("set", "tmp7", "tmp6"),
                                new LogicInstruction("label", "label1"),
                                new LogicInstruction("set", "value", "tmp7"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(
                        LogicInstructionGenerator.generateFrom(
                                (Seq) translateToAst(
                                        "value = if cell1[4] == 0 { false\n} else { cell1[4] = true\nn += 1\n}"
                                )
                        )
                )
        );
    }

    @Test
    void convertsFunctionsReturningValues() {
        assertEquals(
                prettyPrint(

                        List.of(
                                new LogicInstruction("set", "tmp0", "0"),
                                new LogicInstruction("set", "tmp1", "9"),
                                new LogicInstruction("set", "tmp2", "9"),
                                new LogicInstruction("op", "pow", "tmp3", "tmp1", "tmp2"),
                                new LogicInstruction("op", "rand", "tmp4", "tmp3"),
                                new LogicInstruction("write", "tmp4", "cell1", "tmp0"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(LogicInstructionGenerator.generateFrom((Seq) translateToAst("cell1[0] = rand(9**9)")))
        );
    }

    @Test
    void convertsUbindAndControl() {
        assertEquals(
                List.of(
                        new LogicInstruction("label", "label0"),
                        new LogicInstruction("op", "strictEqual", "tmp0", "@unit", "null"),
                        new LogicInstruction("jump", "label1", "notEqual", "tmp0", "true"),
                        new LogicInstruction("ubind", "@poly"),
                        new LogicInstruction("jump", "label0", "always"),
                        new LogicInstruction("label", "label1"),
                        new LogicInstruction("end")
                ),
                LogicInstructionGenerator.generateFrom(
                        (Seq) translateToAst(
                                "while @unit === null {\nubind(@poly)\n}\n")
                )
        );

    }

    @Test
    void convertsReallifeTest1() {
        assertEquals(
                prettyPrint(
                        List.of(
                                new LogicInstruction("set", "tmp0", "0"),
                                new LogicInstruction("set", "n", "tmp0"),
                                new LogicInstruction("label", "label2"),
                                new LogicInstruction("getlink", "tmp1", "n"),
                                new LogicInstruction("set", "reactor", "tmp1"),
                                new LogicInstruction("op", "notEqual", "tmp2", "tmp1", "null"),
                                new LogicInstruction("jump", "label3", "notEqual", "tmp2", "true"),
                                new LogicInstruction("sensor", "tmp3", "reactor", "@liquidCapacity"),
                                new LogicInstruction("set", "tmp4", "0"),
                                new LogicInstruction("op", "greaterThan", "tmp5", "tmp3", "tmp4"),
                                new LogicInstruction("jump", "label0", "notEqual", "tmp5", "true"),
                                new LogicInstruction("sensor", "tmp6", "reactor", "@cryofluid"),
                                new LogicInstruction("sensor", "tmp7", "reactor", "@liquidCapacity"),
                                new LogicInstruction("op", "div", "tmp8", "tmp6", "tmp7"),
                                new LogicInstruction("set", "pct_avail", "tmp8"),
                                new LogicInstruction("set", "tmp9", "0.25"),
                                new LogicInstruction("op", "greaterThanEq", "tmp10", "pct_avail", "tmp9"),
                                new LogicInstruction("control", "enabled", "reactor", "tmp10"),
                                new LogicInstruction("set", "tmp11", "tmp10"),
                                new LogicInstruction("jump", "label1", "always"),
                                new LogicInstruction("label", "label0"),
                                new LogicInstruction("set", "tmp11", "null"),
                                new LogicInstruction("label", "label1"),
                                new LogicInstruction("set", "tmp12", "1"),
                                new LogicInstruction("op", "add", "tmp13", "n", "tmp12"),
                                new LogicInstruction("set", "n", "tmp13"),
                                new LogicInstruction("jump", "label2", "always"),
                                new LogicInstruction("label", "label3"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(LogicInstructionGenerator.generateFrom(
                        (Seq) translateToAst(
                                "" +
                                        "n = 0\n" +
                                        "\n" +
                                        "while (reactor = getlink(n)) != null {\n" +
                                        "  if reactor.liquidCapacity > 0 {\n" +
                                        "    pct_avail = reactor.cryofluid / reactor.liquidCapacity\n" +
                                        "    reactor.enabled = pct_avail >= 0.25\n" +
                                        "  }\n" +
                                        "\n" +
                                        "  n += 1\n" +
                                        "}\n" +
                                        "")
                        )
                )
        );
    }

    @Test
    void convertsUnaryMinus() {
        assertEquals(
                prettyPrint(
                        List.of(
                                new LogicInstruction("set", "tmp0", "-1"),
                                new LogicInstruction("op", "mul", "tmp1", "dx", "tmp0"),
                                new LogicInstruction("set", "dx", "tmp1"),
                                new LogicInstruction("set", "tmp2", "-1"),
                                new LogicInstruction("set", "dy", "tmp2"),
                                new LogicInstruction("set", "tmp3", "2"),
                                new LogicInstruction("set", "tmp4", "1"),
                                new LogicInstruction("op", "sub", "tmp5", "tmp3", "tmp4"),
                                new LogicInstruction("set", "dz", "tmp5"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(
                        LogicInstructionGenerator.generateFrom(
                                (Seq) translateToAst(
                                        "dx *= -1;dy = -1; dz = 2 - 1"
                                )
                        )
                )
        );
    }

    @Test
    void removesCommentsFromLogicInstructions() {
        assertEquals(
                prettyPrint(
                        List.of(
                                new LogicInstruction("set", "tmp0", "1"),
                                new LogicInstruction("set", "a", "tmp0"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(
                        LogicInstructionGenerator.generateFrom(
                                (Seq) translateToAst(
                                        "// Remember that we initialized ourselves\n\na = 1"
                                )
                        )
                )
        );
    }

    @Test
    void generatesRefsWithDashInThem() {
        assertEquals(
                prettyPrint(
                        List.of(
                                new LogicInstruction("set", "tmp0", "1"),
                                new LogicInstruction("set", "tmp1", "0"),
                                new LogicInstruction("ucontrol", "build", "x", "y", "@titanium-conveyor", "tmp0", "tmp1"),
                                new LogicInstruction("ucontrol", "getBlock", "x", "y", "b_type", "b_building"),
                                new LogicInstruction("op", "equal", "tmp2", "b_type", "@titanium-conveyor"),
                                new LogicInstruction("jump", "label0", "notEqual", "tmp2", "true"),
                                new LogicInstruction("set", "tmp3", "1"),
                                new LogicInstruction("op", "add", "tmp4", "n", "tmp3"),
                                new LogicInstruction("set", "n", "tmp4"),
                                new LogicInstruction("set", "tmp5", "tmp4"),
                                new LogicInstruction("jump", "label1", "always"),
                                new LogicInstruction("label", "label0"),
                                new LogicInstruction("set", "tmp5", "null"),
                                new LogicInstruction("label", "label1"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(
                        LogicInstructionGenerator.generateFrom(
                                (Seq) translateToAst(
                                        "build(x, y, @titanium-conveyor, 1, 0)\ngetBlock(x, y, b_type, b_building)\nif b_type == @titanium-conveyor {\nn += 1\n}\n"
                                )
                        )
                )
        );
    }

    @Test
    void generatesComplexMathExpression() {
        assertEquals(
                prettyPrint(
                        List.of(
                                new LogicInstruction("set", "tmp0", "1"),
                                new LogicInstruction("op", "rand", "tmp1", "tmp0"),
                                new LogicInstruction("op", "tan", "tmp2", "tmp1"),
                                new LogicInstruction("op", "abs", "tmp3", "tmp2"),
                                new LogicInstruction("op", "cos", "tmp4", "tmp3"),
                                new LogicInstruction("op", "log", "tmp5", "tmp4"),
                                new LogicInstruction("op", "sin", "tmp6", "tmp5"),
                                new LogicInstruction("op", "floor", "tmp7", "tmp6"),
                                new LogicInstruction("op", "ceil", "tmp8", "tmp7"),
                                new LogicInstruction("set", "x", "tmp8"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(
                        LogicInstructionGenerator.generateFrom(
                                (Seq) translateToAst("x = ceil(floor(sin(log(cos(abs(tan(rand(1))))))))")
                        )
                )
        );
    }


    @Test
    void parsesInclusiveIteratorStyleLoop() {
        assertEquals(
                prettyPrint(
                        List.of(
                                new LogicInstruction("set", "tmp0", "1"),
                                new LogicInstruction("set", "n", "tmp0"),
                                new LogicInstruction("label", "label0"),
                                new LogicInstruction("set", "tmp1", "17"),
                                new LogicInstruction("op", "lessThanEq", "tmp2", "n", "tmp1"),
                                new LogicInstruction("jump", "label1", "notEqual", "tmp2", "true"),
                                new LogicInstruction("print", "n"),
                                new LogicInstruction("jump", "label0", "always"),
                                new LogicInstruction("label", "label1"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(
                        LogicInstructionGenerator.generateFrom(
                                (Seq) translateToAst("for n in 1 .. 17 {\nprint(n)\n}")
                        )
                )
        );
    }

    @Test
    void parsesExclusiveIteratorStyleLoop() {
        assertEquals(
                prettyPrint(
                        List.of(

                                new LogicInstruction("set", "tmp0", "1"),
                                new LogicInstruction("set", "n", "tmp0"),
                                new LogicInstruction("label", "label0"),
                                new LogicInstruction("set", "tmp1", "17"),
                                new LogicInstruction("op", "lessThan", "tmp2", "n", "tmp1"),
                                new LogicInstruction("jump", "label1", "notEqual", "tmp2", "true"),
                                new LogicInstruction("print", "n"),
                                new LogicInstruction("jump", "label0", "always"),
                                new LogicInstruction("label", "label1"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(
                        LogicInstructionGenerator.generateFrom(
                                (Seq) translateToAst("for n in 1 ... 17 {\nprint(n)\n}")
                        )
                )
        );
    }

    @Test
    void generatesCStyleComplexForLoop() {
        assertEquals(
                prettyPrint(
                        List.of(
                                new LogicInstruction("set", "tmp0", "0"),
                                new LogicInstruction("set", "i", "tmp0"),
                                new LogicInstruction("set", "tmp1", "-5"),
                                new LogicInstruction("set", "j", "tmp1"),

                                new LogicInstruction("label", "label0"),
                                new LogicInstruction("set", "tmp2", "5"),
                                new LogicInstruction("op", "lessThan", "tmp3", "i", "tmp2"),
                                new LogicInstruction("jump", "label1", "notEqual", "tmp3", "true"),
                                new LogicInstruction("print", "n"),

                                new LogicInstruction("set", "tmp4", "1"),
                                new LogicInstruction("op", "sub", "tmp5", "j", "tmp4"),
                                new LogicInstruction("set", "j", "tmp5"),

                                new LogicInstruction("set", "tmp6", "1"),
                                new LogicInstruction("op", "add", "tmp7", "i", "tmp6"),
                                new LogicInstruction("set", "i", "tmp7"),

                                new LogicInstruction("jump", "label0", "always"),
                                new LogicInstruction("label", "label1"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(
                        LogicInstructionGenerator.generateFrom(
                                (Seq) translateToAst("for i = 0, j = -5; i < 5; j -= 1, i += 1 {\nprint(n)\n}\n")
                        )
                )
        );
    }

    @Test
    void supportsAssigningAssignmentResults() {
        assertEquals(
                prettyPrint(
                        List.of(
                                new LogicInstruction("set", "tmp0", "42"),
                                new LogicInstruction("set", "b", "tmp0"),
                                new LogicInstruction("set", "a", "tmp0"),
                                new LogicInstruction("end")
                        )
                ),
                prettyPrint(
                        LogicInstructionGenerator.generateFrom(
                                (Seq) translateToAst("a = b = 42")
                        )
                )
        );
    }
}
