package info.teksol.mindcode.compiler.generator;

import info.teksol.mindcode.ast.AstNode;
import info.teksol.mindcode.ast.FunctionDeclaration;
import info.teksol.mindcode.ast.NoOp;
import info.teksol.mindcode.ast.StackAllocation;
import info.teksol.mindcode.ast.VarRef;
import info.teksol.mindcode.compiler.instructions.InstructionProcessor;
import info.teksol.mindcode.logic.LogicLabel;
import info.teksol.mindcode.logic.LogicVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds information about calls between individual user-defined functions. Information is gathered by inspecting
 * function definitions, particularly calls made to other user-defined functions. Information is held by instances
 * of the inner Function class.
 */
public final class CallGraph {
    public static final String MAIN = "";

    private final Map<String, Function> functions = new HashMap<>();
    private final StackAllocation allocatedStack;

    CallGraph(StackAllocation allocatedStack) {
        this.allocatedStack = allocatedStack;

        // Create mock function declaration representing main program body.
        addFunction(new FunctionDeclaration(null, true, MAIN, List.of(), new NoOp()));
    }

    public static CallGraph createEmpty() {
        return new CallGraph(null);
    }

    /**
     * @return list of all existing functions
     */
    public List<Function> getFunctions() {
        return List.copyOf(functions.values());
    }

    /**
     * Confirms a user-defined function exists.
     *
     * @param name name of the function
     * @return  true if such a function is defined
     */
    public boolean containsFunction(String name) {
        return functions.containsKey(name);
    }

    /**
     * Returns the representation of the main function, that is the main program body.
     *
     * @return instance representing main body
     */
    public Function getMain() {
        return Objects.requireNonNull(functions.get(MAIN));
    }

    /**
     * Returns the representation of a used-defined function.
     *
     * @param name name of the function to retrieve
     * @return instance representing requested function.
     * @throws NullPointerException if the function doesn't exist
     */
    public Function getFunction(String name) {
        return Objects.requireNonNull(functions.get(name),
                () -> "Requested nonexistent function " + name);
    }

    public Function getFunctionByPrefix(String localPrefix) {
        return functions.values().stream()
                .filter(f -> localPrefix.equals(f.localPrefix))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Returns the instance of StackAllocation found in the program, regardless of where in the program it was declared.
     *
     * @return instance of StackAllocation or null if it wasn't declared
     */
    public StackAllocation getAllocatedStack() {
        return allocatedStack;
    }

    /**
     * Returns true if at least one user-defined function is recursive. Declared but unused functions aren't
     * considered (a function is unused if it isn't reachable from the main body). When there is at least one
     * recursive function, a stack needs to be set up.     *
     *
     * @return true if there's a recursive function
     */
    public boolean containsRecursiveFunction() {
        return functions.values().stream().filter(Function::isUsed).anyMatch(Function::isRecursive);
    }

    /**
     * Adds a new function. Used by CallGraphCreator to build the call graph.
     *
     * @param declaration function declaration to add
     */
    void addFunction(FunctionDeclaration declaration) {
        functions.put(declaration.getName(), new Function(declaration));
    }

    /**
     * Adds a list of functions called by a caller function. Every function call is contained in the list --
     * a function can appear more than once in the functionCalls list. Information about call cardinality
     * is preserved.
     *
     * @param caller function that calls other functions
     * @param functionCalls list of names of called functions
     */
    void addFunctionCalls(String caller, List<String> functionCalls) {
        Function function = Objects.requireNonNull(functions.get(caller));
        function.setCalls(functionCalls.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting())));
    }

    /**
     * Inspects the structure of function calls and sets function properties accordingly. Assigns a function prefix
     * and labels to recursive and stackless functions.
     *
     * @param instructionProcessor processor used to obtain function prefixes and labels
     */
    void buildCallGraph(InstructionProcessor instructionProcessor) {
        visitFunction("", 0);

        // 1st level of indirect calls
        // Filtering out null values: call map may contain built-in and unknown functions
        functions.values().forEach(outer ->
                outer.getCalls().keySet().stream().map(functions::get).filter(Objects::nonNull).forEach(inner ->
                        outer.addIndirectCalls(inner.getCalls().keySet())
                )
        );

        // 2nd and other levels of indirection
        // Must end eventually, as there's a finite number of functions to add
        while (propagateIndirectCalls()) ;

        functions.values().stream()
                .filter(f -> f.isUsed() && !f.isInline())
                .forEach(f -> setupOutOfLineFunction(instructionProcessor, f));
    }

    private boolean propagateIndirectCalls() {
        // Returns true if at least one function was modified
        return functions.values().stream()
                .flatMapToInt(
                        outer -> outer.getCalls().keySet().stream()
                                .map(functions::get)
                                .filter(Objects::nonNull)
                                .mapToInt(inner -> outer.addIndirectCalls(inner.indirectCalls) ? 1 : 0)
                ).sum() > 0;    // sum to force visiting all items in the stream
    }

    private void setupOutOfLineFunction(InstructionProcessor instructionProcessor, Function function) {
        function.setLabel(instructionProcessor.nextLabel());
        function.setLocalPrefix(instructionProcessor.nextLocalPrefix());
        function.createParameters();
    }

    private final List<String> callStack = new ArrayList<>();

    private void visitFunction(String callee, long count) {
        Function function = functions.get(callee);
        if (function == null) {
            // Unknown function. Maybe a built-in one.
            return;
        }

        function.markUsage((int) count);

        int index = callStack.indexOf(callee);
        callStack.add(callee);
        if (index >= 0) {
            // Detected a cycle in the call graph starting at index. Mark all calls on the cycle as recursive, stop DFS
            // We know callee is now at the beginning and also at the end of the cycle in callStack
            String caller = callee;
            for (String nextCallee : callStack.subList(index + 1, callStack.size())) {
                functions.get(caller).addRecursiveCall(nextCallee);
                caller = nextCallee;
            }
        } else {
            // Visit all children
            function.getCalls().forEach(this::visitFunction);
        }
        callStack.remove(callStack.size() - 1);
    }

    public static class Function {
        private final FunctionDeclaration declaration;
        private final Set<String> recursiveCalls = new HashSet<>();
        private final Set<String> indirectCalls = new HashSet<>();
        private Map<String, Long> calls;
        private LogicLabel label;
        private String localPrefix;
        private int useCount = 0;
        private List<LogicVariable> parameters;

        private Function(FunctionDeclaration declaration) {
            this.declaration = declaration;
        }

        /** @return true if this function should be inlined */
        public boolean isInline() {
            // Automatically inline all non-recursive functions called just once
            return declaration.isInline() || !isRecursive() && getUseCount() == 1;
        }

        public FunctionDeclaration getDeclaration() {
            return declaration;
        }

        /** @return the body of the function from function declaration */
        public AstNode getBody() {
            return declaration.getBody();
        }

        /** @return name of the function */
        public String getName() {
            return declaration.getName();
        }

        /** @return list of parameters of the function */
        public List<VarRef> getParams() {
            return declaration.getParams();
        }

        /** @return list of parameters of the function as LogicVariable */
        public List<LogicVariable> getLogicParameters() {
            return parameters;
        }

        /** @return number of function parameters */
        public int getParamCount() {
            return declaration.getParams().size();
        }

        public Map<String, Long> getCalls() {
            return Objects.requireNonNull(calls, () -> "Uninitialized calls in function " + getName());
        }

        /** @return true if the function is used (is reachable from main program body */
        public boolean isUsed() {
            return useCount > 0;
        }

        /** @return the number of places the function is called from */
        public int getUseCount() {
            return useCount;
        }

        /** @return true if the function is recursive */
        public boolean isRecursive() {
            return !recursiveCalls.isEmpty();
        }

        /**
         * Determines whether the call to calledFunction from this function is (potentially) recursive, that is
         * it can lead to another invocation of this function. If it is, the local context of the function needs
         * to be saved and restored using stack.
         *
         * @param calledFunction name of the function being called from this function
         * @return true if this function call might be recursive
         */
        public boolean isRecursiveCall(String calledFunction) {
            return recursiveCalls.contains(calledFunction);
        }

        /**
         * Determines whether the called function is called more than once from this function. If it is, the
         * returned value from each call needs to be stored in a unique variable, otherwise subsequent calls
         * might overwrite the returned value from previous call.
         *
         * @param calledFunction name of the function being called from this function
         * @return true if the function is called more than once from this function
         */
        public boolean isRepeatedCall(String calledFunction) {
            return indirectCalls.contains(calledFunction)
                    ? getCalls().getOrDefault(calledFunction, 0L) > 0
                    : getCalls().getOrDefault(calledFunction, 0L) > 1;
        }

        /** @return the label allocated for the beginning of this function */
        public LogicLabel getLabel() {
            return label;
        }

        /** @return the local prefix allocated for local variables of this function */
        public String getLocalPrefix() {
            return localPrefix;
        }

        private boolean addIndirectCalls(Set<String> calls) {
            return indirectCalls.addAll(calls);
        }

        // Mark the call from this function to called function as recursive. Used when cycle in the call graph
        // was detected.
        private void addRecursiveCall(String calledFunction) {
            recursiveCalls.add(calledFunction);
        }

        // Increase usage count of this function
        private void markUsage(int count) {
            useCount += count;
        }

        private void setCalls(Map<String, Long> calls) {
            this.calls = calls;
        }

        private void setLabel(LogicLabel label) {
            this.label = label;
        }

        private  void setLocalPrefix(String localPrefix) {
            this.localPrefix = localPrefix;
        }

        private void createParameters() {
            parameters = getParams().stream()
                    .map(p -> LogicVariable.local(getName(), localPrefix, p.getName()))
                    .toList();
        }
    }
}
