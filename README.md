[![Build Status](https://travis-ci.org/mjanicek/rembulan.svg?branch=master)](https://travis-ci.org/mjanicek/rembulan)

# Rembulan

(*Rembulan* is Javanese/Indonesian for *Moon*.)

An implementation of Lua 5.3 for the Java Virtual Machine (JVM).
Implements coroutines using continuations rather than mapping them onto
Java threads.

The goal of the Rembulan project is to develop a scalable, correct,
complete and performant implementation for untrusted Lua code on the JVM.

## Feature list

* **Full Lua 5.3 semantics** --
Rembulan implements Lua 5.3 as specified by the [Lua Reference Manual](http://www.lua.org/manual/5.3/manual.html), explicitly attempting to mimic
the behaviour of PUC-Lua whenever possible.
This includes language-level features (such as metamethods and coroutines)
and the standard library.

* **Compiled, not interpreted** --
Rembulan is a compiler and a runtime for Lua. Lua functions are compiled
into Java classes and loaded by the JVM.

* **Basic static type checking** --
The Rembulan compiler performs a type analysis of the Lua programs in order
to generate a more tightly-typed code whenever feasible. More type
information means fewer run-time checks, leading to faster execution.

* **Scalable coroutines** --
Coroutines are decoupled from (Java) threads. On coroutine switches
and interrupts, Rembulan stores the call stack, and recreates it on resume.

## Project structure

This is a multi-module Maven project, containing the following modules:

 * `rembulan-lua-common` ... common Lua utilities and constants
 * `rembulan-core` ... the runtime and the standard library
 * `rembulan-parser` ... parser for Lua 5.3, producing a (de-sugared) AST
 * `rembulan-compiler` ... Lua-to-Java bytecode compiler
 * `rembulan-tests` ... the project test suite, including benchmarks from the Benchmarks Game
