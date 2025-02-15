# Functions

Mindcode allows you to call existing functions or create new ones.
Functions are called by specifying a function name and placing parameters in parentheses.
The parenthesis must be used even if the function has no parameters.

```
foo(x)     // Calling function with a single argument
bar()      // Calling function with no arguments
```

# Mindustry Logic functions

Interactions with the Mindustry world take place through functions mapped to Mindustry Logic instructions.

Mindcode currently supports targeting Mindustry versions 6 and 7. Web application always targets version 7 processors,
while command-line version of the compiler allows to select version 6 for better backwards compatibility.
The differences are minuscule, though, most of the code generated for version 7 will run on version 6 as well.

A specific `7A` target was added to Mindcode, where the `getBlock` and `ulocate` functions return the building that 
was found at given coordinates or located using the criteria. This update makes the most occurring use case, where the 
located building is the only used output of the function, a natural way to use the function.

At this point, `7` is still the default target for both command line tool and web application. `7A` will become the 
default in the future, or in higher Mindcode versions.  

All supported functions and their respective Mindustry instruction counterparts can be found in the function reference.
Please note that the reference serves just to document all existing functions and the way they are compiled to Mindustry Logic,
but it does not aim to describe the behavior of the functions/instructions.

* [Function reference for Mindustry Logic 6](FUNCTIONS_V6.markdown)
* [Function reference for Mindustry Logic 7](FUNCTIONS_V7.markdown)
* [Function reference for Mindustry Logic 7A](FUNCTIONS_V7A.markdown)

## Instruction parameters

There are several types of parameters a function can have:
* _Input parameters_: these parameters will accept any expression, including variables, as arguments.
* _Output parameters_: one of output parameters of the instruction is always mapped to the return value of the function,
  e.g. `block = getlink(linkNum)` translates to instruction `getlink block linkNum`.
  If the instruction provides more than one output parameters, the remaining ones become parameters of the function.
  Output parameters are typically optional, if you aren't interested in a particular value returned by the instruction,
  you can omit the argument (only arguments at the end of the argument list can be omitted.)
  When not omitted, the argument passed in should be a variable.
* _Enumerated parameters_: these parameters will only accept one of predefined constants as an argument.
  For example the `uradar` functions requires one of the following values for each of its first three arguments:
  `any`, `enemy`, `ally`, `player`, `attacker`, `flying`, `boss`, `ground`. The constants are passed as-is, without
  any escaping or double quotes. It is not possible to store the value in a variable and pass the variable instead.
  Passing a value different to one of the supported values results in compilation error.

## Instruction mapping rules

Generally, functions are mapped to instructions using these rules:
* If the instruction has just one form (such as `getlink`), the function name corresponds to the instruction name. 
  All instruction names are lowercase.
* If the instruction takes several forms depending on the exact operation the instruction performs (such as `draw` or `ucontrol`),
  the function name corresponds to the operation. In this case, the function name can be camelCase (such as `itemTake`).

The disparity between those two kinds of functions is a consequence of keeping Mindcode nomenclature as close to Mindustry Logic as possible.

There are a few issues with these rules:
* Both `ucontrol stop` and `stop` would map to the `stop()` function. The `stop` instruction is instead mapped to the `stopProcessor()` function.
* `ucontrol getBlock` is similar to the new `getblock` World Processor instruction. The resulting functions only differ in case.
* The `status` World Processor instruction distinguishes clearing and applying the status by a boolean value
  (`true` or `false`), which is not very readable. Mindcode instead creates separate functions,
  `applyStatus()` and `clearStatus()`.

## Methods

Some instructions perform an operation on an object (a linked block), which is passed as an argument to one
of instruction parameters. In these cases, the instruction can be mapped to a method called on the given block,
e.g. `block.shoot(x, y, doShoot)` translates to `control shoot block x y doShoot 0`.

In some cases, the instruction can be invoked as a function or as a method (`printflush(message1)` or `message1.printflush()`).
All existing mappings are shown in the function reference above.

## Alternative `control` syntax

There is a special case for `control` instruction setting a single value on a linked block, for whose Mindcode accepts
the following syntax:
* `block.enabled = boolean`, which is equivalent to `block.enabled(boolean)`
* `block.config = value`, which is equivalent to `block.config(value)`

The `block` in the examples can be a variable or a linked block object.

Currently, the property access syntax cannot be used with the new [`setprop`](FUNCTIONS_V7.markdown#instruction-setprop)
instruction. Looking for ways to support this syntax in the future.

## Alternative `sensor` syntax

The `sensor` method accepts an expression, not just constant property name, as an argument:

```
amount = vault1.sensor(use_titanium ? @titanium : @copper)
```

If the property you're trying to obtain is hard-coded, you can again use an alternate syntax:
`amount = storage.thorium` instead of the longer equivalent `amount = storage.sensor(@thorium)`.
You can use this for any property, not just item types, such as `@unit.dead` instead of `@unit.sensor(@dead)`.

Again, the `vault1` or `storage` in the examples can be a variable or a linked block object.

> **Note**: in the case of property access, the `@` character at the beginning of the property name is omitted.

# Built-in functions

Mindcode now defines a few built-in functions that enhance plain Mindustry Logic.

## print

The `print` function corresponds to the `print` instruction, but accepts more than one argument.
All arguments passed to the function are printed using individual `print` instructions.
The returned value is the value of the last argument.

## println

The `println` function prints all its arguments and adds a newline ("\n") at the end.
The returned value is that of the last argument, or `null` if no argument was passed:

```
println("Position: ", x, ", ", y)
println()
println("Elapsed time: ", elapsed, " sec")
printflush(message1)
```

## printf

The `printf` function takes a string literal or string constant as its first argument.
It then looks for the `$` characters and replaces them with values like this:
* If the `$` character is followed by a variable name, the variable is printed (external variables, e.g. `$X`, aren't supported).
* If the `$` is not followed by a variable name, next argument from the argument list is printed.
* To separate the variable name from the rest of the text, enclose the name in curly braces: `${name}`.
This is not necessary if the next character cannot be part of a variable name.
`${}` can be used to place an argument from the argument list immediately followed by some text.
* To print the `$` character, use `\$`.

| `printf()` call                                                         | is the same as                                                           |
|-------------------------------------------------------------------------|--------------------------------------------------------------------------|
| `printf("$@unit at position $x, $y\n")`                                 | `println(@unit, " at position ", x, ", ", y)`                            |
| `printf("Time: $ sec, increment: $\n", floor(@second), current - last)` | `println("Time: ", floor(@second), " sec, increment: ", current - last)` |
| `printf("Coordinates: ${real}+${imag}i")`                               | `print("Coordinates: ", real, "+", imag, "i")`                           |
| `printf("Price: \$$price")`                                             | `print("Price: $", price)`                                               |
| `printf("Speed: ${}m/s", distance / time)`                              | `print("Speed: ", distance / time, "m/s")`                               |

The function was inspired by string interpolation in Ruby, but there are differences.
Firstly, the first argument to `printf` must be a string constant, as the formatting takes place at compile time
(Mindustry Logic doesn't provide means to do it at runtime). Secondly, only variables are allowed in curly braces,
no expressions:

```
x = 5
y = 10
format = "Position: $, $\n"
printf(format, x, y)                // Not allowed - format must be a string constant
printf("Distance: ${len(x, y)}")    // No expressions allowed

const fmt = "Position: $, $\n"
printf(fmt, x, y)                   // Allowed - fmt is a string constant
```

# User-defined Functions

You may declare your own functions using the `def` keyword:

```
def update(message, status)
    println("Status: ", status)
    printf("Game time: $ sec", floor(@time) / 1000)
    printflush(message)
end
```

This function will print the status and flush it to given message block.
Calling your own functions is done in the same way as any other function:

```
update(message1, "The core is on fire!!!")
```

You can pass any variable or value assignable to a variable as an argument to the function. A function may call other functions.

## Function parameters and local variables

Function parameters and variables used in functions are local to the function.
See also [local variables](SYNTAX-1-VARIABLES.markdown#local-variables)
and [global variables](SYNTAX-1-VARIABLES.markdown#global-variables).

## Return values

Function ends (returns to the caller) when the end of function definition is reached, or when a `return` statement is executed.
In the first case, the return value of a function is given by the last expression evaluated by the function;
in the second case, the return value is equal to the expression following the `return` keyword, or `null` if the return
statement doesn't specify a return value:

```
def foo(n)
    case n
        when 1      return
        when 2      return "Two"
    end
    n
end

printf("$:$:$", foo(1), foo(2), foo(3))
printflush(message1)
```

The output of this program is `null:Two:3`.

## Inline functions

Normal function are compiled once and called from other places in the program.
Inline functions are compiled into every place they're called from,
so there can be several copies of them in the compiled code.
This leads to shorter code per call and faster execution.
To create an inline function, use `inline` keyword:

```
inline def printtext(name, value, min, max)
    if value < min
        println(name, " too low")
    elsif value > max
        println(name, " too high")
    end
end

printtext("Health", health, minHealth, maxHealth)
printtext("Speed", speed, minSpeed, maxSpeed)
```

Large inline functions called multiple times can generate lots of instructions and make the compiled code too long.
If this happens, remove the `inline` keyword from some function definitions to generate less code.

The compiler will automatically make a function inline when it is called just once in the entire program.
This is safe, as in this case the program will always be both smaller and faster.
Other non-recursive functions might also be compiled inline, if the instruction limit isn't reached.

## Recursive functions

Functions calling themselves, or two (or more) functions calling each other are _recursive_.
Recursive functions require a stack to keep track of the calls in progress
and for storing local variables from different invocations of the function.

```
allocate stack in bank1

def fib(n)
    return n < 2 ? max(n, 0) : fib(n - 1) + fib(n - 2)
end

fib(0) // 0
fib(1) // 0
fib(2) // 1
fib(3) // 1
fib(4) // 2
fib(5) // 3
fib(6) // 5
fib(7) // 8
fib(8) // 13
// and so on...
```

Declaring a recursive function inline leads to compilation error.

Mindcode detects the presence of recursive functions and only requires the stack when at least one is found in the program.
A significant limitation is imposed on recursive functions: parameters and local variables may end up being stored on the stack.
As the stack itself is stored in a memory bank or memory cell, any non-numeric value of these variables will get lost.
For more information, see discussion of [stack](SYNTAX-1-VARIABLES.markdown#stack) in the **Variables** section.

---

[« Previous: Control flow statements](SYNTAX-3-STATEMENTS.markdown) &nbsp; | &nbsp; [Next: Compiler directives »](SYNTAX-5-OTHER.markdown)
