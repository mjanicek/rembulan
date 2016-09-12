[![Build Status](https://travis-ci.org/mjanicek/rembulan.svg?branch=master)](https://travis-ci.org/mjanicek/rembulan)

# Rembulan

(*Rembulan* is Javanese/Indonesian for *Moon*.)


## About

Rembulan is an implementation of Lua 5.3 for the Java Virtual Machine (JVM), written in
pure Java with minimal dependencies.
The goal of the Rembulan project is to develop a correct, complete and scalable Lua
implementation for running sandboxed Lua programs on the JVM.

Rembulan implements Lua 5.3 as specified by the
[Lua Reference Manual](http://www.lua.org/manual/5.3/manual.html), explicitly attempting to mimic
the behaviour of PUC-Lua whenever possible. This includes language-level features (such
as metamethods and coroutines) and the standard library.


## Status

The majority of language-level features is implemented, and may be expected
to work. If you find behaviour that does not conform to Lua 5.3 as defined by the Lua Reference
Manual, please [open a new issue](https://github.com/mjanicek/rembulan/issues).

See also the [completeness table](doc/CompletenessTable.md) that maps out the current
completeness status of Rembulan with regard to PUC-Lua, in particular the standard library.


## Using Rembulan

Rembulan requires a Java Runtime Environment (JRE) version 7 or higher.

### Building from source

To build Rembulan, you will need the following:

 * Java Development Kit (JDK) version 7 or higher
 * Maven version 3 or higher

Maven will pull in the remaining dependencies as part of the build process.

To fetch the latest code on the `master` branch and build it, run

    git clone https://github.com/mjanicek/rembulan.git
    cd rembulan    
    mvn install

This will build all modules, run tests and finally install all artifacts into your local
Maven repository.    

#### Standalone REPL

Much like PUC-Lua, Rembulan contains a standalone REPL. This is packaged in the module
`rembulan-standalone`. To build the REPL, first run

    mvn package

Next, to run the REPL:
 
    cd rembulan-standalone/target/appassembler/bin
    ./rembulan

### Using Rembulan from Maven

There are no releases yet, but snapshot artifacts are published to the Sonatype OSSRH Snapshot
Repository. To use the snapshot artifacts, add the following configuration to your `pom.xml`:

    <repositories>
      <repository>
        <id>sonatype-ossrh-snapshots</id>
        <name>Sonatype OSSRH (Snapshots)</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots />
      </repository>
    </repositories>

To include the **runtime** as a dependency:

    <dependency>
      <groupId>net.sandius.rembulan</groupId>
      <artifactId>rembulan-runtime</artifactId>
      <version>0.1-SNAPSHOT</version>
    </dependency>

To include the **compiler** as a dependency:

    <dependency>
      <groupId>net.sandius.rembulan</groupId>
      <artifactId>rembulan-compiler</artifactId>
      <version>0.1-SNAPSHOT</version>
    </dependency>

To include the **standard library** as a dependency:

    <dependency>
      <groupId>net.sandius.rembulan</groupId>
      <artifactId>rembulan-stdlib</artifactId>
      <version>0.1-SNAPSHOT</version>
    </dependency>

Note that `rembulan-compiler` and `rembulan-stdlib` both pull in `rembulan-runtime` as
a dependency, but are otherwise independent. (I.e., to use the compiler and the standard
library, you need to declare both `-compiler` and `-stdlib` as dependencies, but do not need
to include `-runtime`).

## Getting started

Rembulan compiles Lua functions into Java classes and loads them into the JVM;
the compiler performs a type analysis of the Lua programs in order to generate a more
tightly-typed code whenever feasible.

Since the JVM does not directly support coroutines, Rembulan treats Lua functions as state
machines and controls their execution (i.e., yields, resumes and pauses) using exceptions.
Since the Rembulan runtime retains control of the control state, this technique is also used
to implement CPU accounting and scheduling of asynchronous operations.

#### Example: Hello world  

The following snippet loads the Lua program `print('hello world!')`, compiles it, loads
it into a (non-sandboxed) state, and runs it:

(From [`rembulan-examples/.../HelloWorld.java`](rembulan-examples/src/main/java/net/sandius/rembulan/examples/HelloWorld.java))

    String program = "print('hello world!')";
    
    // initialise state
    StateContext state = StateContexts.newDefaultInstance();
    Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);
    
    // compile
    ChunkLoader loader = CompilerChunkLoader.of("hello_world");
    LuaFunction main = loader.loadTextChunk(new Variable(env), "hello", program);
    
    // execute
    DirectCallExecutor.newExecutor().call(state, main);

The output (printed to `System.out`) is:

    hello world!

#### Example: CPU accounting

Lua functions can be called in a mode that automatically pauses their execution once the
given number of operations has been performed:

(From [`rembulan-examples/.../InfiniteLoop.java`](rembulan-examples/src/main/java/net/sandius/rembulan/examples/InfiniteLoop.java))

    String program = "n = 0; while true do n = n + 1 end";
    
    // initialise state
    StateContext state = StateContexts.newDefaultInstance();
    Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);
    
    // compile
    ChunkLoader loader = CompilerChunkLoader.of("infinite_loop");
    LuaFunction main = loader.loadTextChunk(new Variable(env), "loop", program);

    // execute at most one million ops
    DirectCallExecutor executor = DirectCallExecutor.newExecutorWithTickLimit(1000000);

    try {
        executor.call(state, main);
        throw new AssertionError();  // never reaches this point!
    }
    catch (CallPausedException ex) {
        System.out.println("n = " + env.rawget("n"));
    }

Prints:

    n = 199999

The `CallPausedException` contains a *continuation* of the call. The call can be resumed:
the pause is transparent to the Lua code, and the loop does not end with an error (it is merely
paused).

#### Further examples

For further examples, see the classes in
[`rembulan-examples/src/main/java/net/sandius/rembulan/examples`](rembulan-examples/src/main/java/net/sandius/rembulan/examples).

### Project structure

Rembulan is a multi-module Maven build, consisting of the following modules that are deployed
to Sonatype OSSRH:

 * `rembulan-runtime` ... the core classes and runtime;
 * `rembulan-compiler` ... a compiler of Lua sources to Java bytecode;
 * `rembulan-stdlib` ... the Lua standard library;
 * `rembulan-standalone` ... standalone REPL, a (mostly) drop-in replacement
                             for the `lua` command from PUC-Lua.

There are also auxiliary modules that are not deployed:

 * `rembulan-tests` ... project test suite, including benchmarks from
                        the Benchmarks Game;
 * `rembulan-examples` ... examples of the Rembulan API.                       


## Contributing

Contributions of all kinds are welcome!


## License

Rembulan is licensed under the Apache License Version 2.0. See the file
[LICENSE.txt](LICENSE.txt) for details.
