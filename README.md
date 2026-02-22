# Simple java test

... for basic interactions on the `std.in` and `std.out`.

# Usage

```sh
java Test verbose={true|false} test={path to exampleinteraction}
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
