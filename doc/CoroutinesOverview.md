# Overview of the coroutine implementation

The way coroutines are handled in Rembulan permeates through the entire implementation
strategy. The main obstacle to overcome is the lack of native support for coroutines
or continuations on the JVM -- at this point, any pure Java implementation of this runtime
feature must either simulate coroutines using Java threads (which are heavyweight),
or implement them entirely in user code. Rembulan chooses the latter approach.

The main idea is to use exceptions for switching coroutines, and attach
call stack information to them.

What follows is a short overview of how coroutine switching is achieved in Java code and
terms. For Lua programs, this process is entirely transparent.

Every Lua function is an instance of (a subtype of) the Java class
[`LuaFunction`](https://mjanicek.github.io/rembulan/apidocs/rembulan-runtime/net/sandius/rembulan/runtime/LuaFunction.html)
with two methods (entry points): `invoke` and `resume`.
`invoke` is called when the function is called; `resume` is called when it is being resumed
having been previously paused in an `invoke` or `resume`. Every operation that may involve
a coroutine switch (that is, all operations that may involve metamethods, plus calls) may
then throw a “control throwable”, an exception that has a call stack associated with it.
This control throwable must be caught and rethrown using the following idiom:

```java
try {
    // an operation that may pause
    Dispatch.add(context, a, b);
}
catch (UnresolvedControlThrowable ct) {
    // state is the suspended state used for resume
    // throws a ResolvedControlThrowable
    throw ct.resolve(this, state);  
}
```

(For more details, see
[`Dispatch`](https://mjanicek.github.io/rembulan/apidocs/rembulan-runtime/net/sandius/rembulan/runtime/Dispatch.html),
[`UnresolvedControlThrowable`](https://mjanicek.github.io/rembulan/apidocs/rembulan-runtime/net/sandius/rembulan/runtime/UnresolvedControlThrowable.html)
and [`ResolvedControlThrowable`](https://mjanicek.github.io/rembulan/apidocs/rembulan-runtime/net/sandius/rembulan/runtime/ResolvedControlThrowable.html).)

Adding call frame information to that exception and rethrowing serves two purposes:

  1) saving the call frame, and
  2) doing this for every call in the call stack.

A saved call frame fully represents the paused call; a saved call stack fully represents the paused coroutine.

Resuming a coroutine is then involves as taking its paused call stack, and unpausing it:
resuming the top call frame (by calling `resume`). When it returns, the call frame below
is called etc.

Essentially, then, every function's control state is exposed by the two methods (`invoke`
and `resume`), `invoke` being the initial entry point, `resume` are all others.

The good news is that if you assume this being in place, you get what is called 
“CPU accounting” in Rembulan (i.e., running Lua programs for a given number of ticks,
automatically pausing them once the ticks have been spent) practically for free, as all functions
that call Lua functions are ready to be paused. This is especially important for sandboxing
applications.

All of the above is done in a single thread, with no multi-threading involved, allowing
the application to choose an appropriate threading strategy. In other words, coroutines
in Rembulan are entirely decoupled from threads.

## Caveats

Writing Lua functions in plain Java this way can get very painful,
especially for functions such as `table.sort` in Lua 5.3. In the current implementation,
this standard library function implements heapsort, but since `table.sort` must handle
metamethods, every comparison and every table access may in fact trigger a metamethod.
The resulting code is very complex and hard to maintain.

The only way out of that would involve bytecode rewriting (at compile or class load time)
of Java code written in "non-resumable" way. At the moment, there is no such functionality
included in Rembulan, and it is probably a better idea to write complex Lua functions that
perform Lua operations following the semantics of Lua in Lua and have them compiled into
Java bytecode by the Rembulan compiler.

For Lua functions, the control flow graph maintenance is entirely handled by the Rembulan
compiler.
