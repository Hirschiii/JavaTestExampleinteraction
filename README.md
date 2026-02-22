# Simple java test

... for basic interactions on the `std.in` and `std.out`.

# Usage

Clone this repo into your root java package and then just compile and run the class as follows:

```sh
java test.Test verbose={true|false} test={path to exampleinteraction}
```

# The test file

The example interactions are simple text files containing to class to test,
possible arguments to start the main method and all the input with the expected
output.

Important is the **first line** with the following syntax:

```txt
%> java {class name} {arguments}
```

> The arguments will be passed as an `String[]`.

Everything beneath that is ether a command **input** or expected **output**.

If the line **starts with** `> ` the following will be treated as a program
**input**, everything else will be matched against the actual output of the
program.

# Understand the output

If the option `verbose` is set to `false` only the passed *tests* (the inputs)
will be printed and, on fail, the fail message. 

An example fail message looks the following:

```txt
--- TEST FEHLGESCHLAGEN ---
IN E LINE: "148"
IN A LINE: "139"
ERWARTET: "Livestock Sorceress (Team Enemy)"
GEFUNDEN: "<no unit>"
---------------------------
```

It contains:

- The expected line number (`E LINE`), the line number in the interaction file
- The actual line number (`A LINE`), the line number of the actual output from your program
- The expected line (`ERWARTET`)
- and the actual line from your program (`GEFUNDEN`)

Beneath that you get a little context, the last 10 lines from the program output.
