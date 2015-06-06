# io.meat:try
### A humble Try monad for Java 8

A Try is a completed attempt to compute a result that either succeeded or
failed.

Try is especially useful for monadic composition of tasks that may each fail,
without having to use a series of nested try/catch statements.  You can form a
Try using `attempt()` or `attemptApply()`:

```java
Try<String> value = Try.attempt(() -> keyValueStore.fetch("someKey"));
Try<String> value = Try.attemptApply(keyValueStore::fetch, "someKey");
```

If the function has checked exceptions, you can use `attemptChecked()` or
`attemptApplyChecked()` to wrap any exception in a RuntimeException.

You can also manually make a successful or failed Try using `succeed()` and
`fail()`:

```java
Try<String> value = Try.succeed("someValue");
Try<String> failure = Try.fail(new IllegalStateException("Something broke"));
```

Once you have a Try, you can transform the value inside it using `map()`:

```java
Try<String> hex = Try.succeed(123).map(Integer::toHexString);
assert hex.equals(Try.succeed("7b"));
```

If your function returns a Try, you can use `flatMap()` to prevent nesting:

```java
Try<Integer> number = Try.succeed(123);
Try<String> nextValue = number.flatMap(num -> {
    return Try.succeed("a string");
});
assert nextValue.equals(Try.succeed("a string"));
```

To get values back out of a Try, there are Optionals available as `getResult()`
and `getFailure()` for safe retrieval of either state:

```java
Try<Integer> number = Try.succeed(123);
assert number.getResult().equals(Optional.of(123));
assert !number.getFailure().isPresent();
```

If you've checked that a Try is successful, or you don't mind an (unchecked)
exception being raised, use `get()`:

```java
Try<Integer> number = Try.succeed(123);
assert number.get() == 123;
```

In the case of a failed Try, `get()` will wrap the exception in a
`RuntimeException`; its `Throwable.getCause()` will be the original exception.


## Unlicense

This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>

