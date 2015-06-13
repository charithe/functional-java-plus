Functional Java Plus
=====================

With the release of project Lambda, writing Java code in functional style has become much easier (although at times it feels like trying to parallel park a lorry in a busy town centre).
Libraries like [Functional Java](http://www.functionaljava.org/) supplement the (very limited) set of functional programming utilities in the Java standard library.
However, at times even they feel inadequate if you have been working with proper functional languages like Scala or Haskell for a while. Hence, this is a set of utilities
to make life easier for those who are forced to use Java but are suffering from Scala/Haskell/Rust/<insert functional language> withdrawal.



Try
----

Something I dearly miss from Scala and Rust. Being able to succinctly express that a particular block of code could either succeed or throw an exception is very powerful in my opinion.
Java standard library contains the `Optional` type but using it forces you to discard the error completely. FunctionalJava library provides an `Either` type which is the
kind of disjoint union type you want but it is too generic. What you need is something like the `Try` in Scala or the `Result` in Rust -- where the "`left`" side is always an error type.


```java

// Wrap a successful result
Try<String> mySuccess = Try.fromSuccess("I am a success");

// Wrap an exception
Try<String> myFailure = Try.fromFailure(new Exception("Something went wrong");

// Wrap a code block that could throw an exception
Try<String> wrapped = Try.doTry(() -> {
    // code that could throw an exception
});


// Check if the result is a success
if(myTryResult.isSuccess()){ //yay! }


//flatMap (map, filter, foreach, getOrElse, toOptional and many more are supported. See Javadoc and/or unit tests)
// This is a contrived example to illustrate flatMap functionality.
public Try<String> readStringFromFile(String fileName) {
    return Try.doTry(() -> new BufferedReader(new FileReader(fileName)))
            .flatMap((BufferedReader br) -> Try.doTry(br::readLine));
}
```




