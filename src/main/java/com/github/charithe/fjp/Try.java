/*
 * Copyright 2015 Charith Ellawala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.charithe.fjp;


import fj.data.Either;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Emulates the Scala Try interface
 * @param <T> Type of success value
 * @see <a href="http://www.scala-lang.org/files/archive/nightly/docs/library/index.html#scala.util.Try">Scala Try</a>
 */
public class Try<T> {

    private final Either<Throwable, T> tryState;

    // private constructor to prevent instantiation from outside classes
    private Try(Either<Throwable, T> tryState) {
        this.tryState = tryState;
    }

    /**
     * Returns true if this instance holds a success value
     * @return boolean
     */
    public boolean isSuccess(){
        return tryState.isRight();
    }

    /**
     * Returns true if this instance holds a failure value
     * @return boolean
     */
    public boolean isFailure(){
        return tryState.isLeft();
    }

    /**
     * Returns the success value if this is a success. Otherwise, throws an exception.
     * @return Success value if present
     * @throws {@link com.github.charithe.fjp.Try.CallingSuccessOnFailureValue}
     */
    public T success() {
        if (tryState.isRight()) {
            return tryState.right().value();
        } else {
            throw new CallingSuccessOnFailureValue();
        }
    }

    /**
     * Returns the failure value if this is a failure. Otherwise, throws an exception.
     * @return Failure value if present
     * @throws {@link com.github.charithe.fjp.Try.CallingFailureOnSuccessValue}
     */
    public Throwable failure() {
        if(tryState.isLeft()) {
            return tryState.left().value();
        } else {
            throw new CallingFailureOnSuccessValue();
        }
    }


    /**
     * Perform a flatmap over this instance
     * @param fn flatmap function
     * @param <A> Return type of flatmap function
     * @return If this is a success, result of applying the flatmap function. Otherwise, the failure value
     */
    public <A> Try<A> flatMap(Function<T, Try<A>> fn) {
        if(isFailure()){
            return fromFailure(failure());
        } else {
            return fn.apply(success());
        }
    }

    /**
     * Perform a map over this instance
     * @param fn map function
     * @param <A> Return type of map function
     * @return If this is a success, result of applying the map function. Otherwise, the failure value
     */
    public <A> Try<A> map(Function<T,A> fn) {
        return this.flatMap((T t) -> fromSuccess(fn.apply(t)));
    }

    /**
     * Return the success value if this is a success or throw the failure value if this is a failure
     * @return success value if this is a success instance
     * @throws Throwable if this is a failure instance
     */
    public T get() throws Throwable {
        if(isFailure()){
            throw failure();
        } else {
            return success();
        }
    }

    /**
     * Return the success value or the supplied default if this is a failure instance
     * @param defaultVal Default value to return in case of failure
     * @return success value or the supplied default
     */
    public T getOrElse(T defaultVal) {
        if(isFailure()){
            return defaultVal;
        } else {
            return success();
        }
    }

    /**
     * Return this instance if it is a success instance. Otherwise, return the supplied default.
     * @param defaultVal Default value to return in case this is a failure
     * @return this instance or the supplied default
     */
    public Try<T> orElse(Try<T> defaultVal) {
        if(isFailure()){
            return defaultVal;
        } else {
            return this;
        }
    }

    /**
     * Convert this to an {@link Optional}
     * @return Optional containing the success value or Optional.empty in case this is a failure.
     */
    public Optional<T> toOptional(){
        if(isFailure()){
            return Optional.empty();
        } else {
            return Optional.of(success());
        }
    }

    /**
     * Apply the predicate to the success value to determine whether this should really be a failure
     * @param predicate Predicate to apply
     * @return this object if it is a failure. If this is a success, return success if the predicate is satisfied or return failure otherwise.
     */
    public Try<T> filter(Predicate<T> predicate){
        return flatMap((T t) -> {
            if (predicate.test(t)){
                return this;
            } else {
                return fromFailure(new UnmatchedFilterPredicate());
            }
        });
    }

    /**
     * Apply the supplied function to the success value if this is a success
     * @param consumer Function to apply
     */
    public void foreach(Consumer<T> consumer){
        map((T t) -> {
            consumer.accept(t);
            return null;
        });
    }


    /**
     * Create a success instance of Try
     * @param successVal success value to wrap inside the Try
     * @param <A> Type of the success value
     * @return Try object that returns true on calls to isSuccess()
     */
    public static <A> Try<A> fromSuccess(A successVal){
        return new Try(Either.right(successVal));
    }

    /**
     * Create a failure instance of Try
     * @param failureVal failure value to wrap inside the Try
     * @param <A> Type of the success value if this were a success
     * @return Try object that return true on calls to isFailure()
     */
    public static <A> Try<A> fromFailure(Throwable failureVal){
        return new Try(Either.left(failureVal));
    }

    /**
     * Create a Try object from a function that could potentially throw an exception and fail. If the function is successful,
     * the return value will be Try containing the success. If an exception is thrown, the return value will be a Try object
     * containing the failure.
     * @param fn Function that could potentially throw an exception
     * @param <A> Type of the return value from the function
     * @return Try object containing the result of the function or the exception thrown
     */
    public static <A> Try<A> doTry(FunctionThrowingException<A> fn){
        try {
            return fromSuccess(fn.apply());
        } catch (LambdaException e) {
            return fromFailure(e.getCause());
        }
    }


    @FunctionalInterface
    public interface FunctionThrowingException<A> {

        A doApply() throws Throwable;

        default A apply() {
            try {
                return doApply();
            } catch(Throwable t) {
              throw new LambdaException(t);
            }
        }

    }


    public static final class UnmatchedFilterPredicate extends Exception {

    }

    public static final class CallingFailureOnSuccessValue extends RuntimeException {

    }

    public static final class CallingSuccessOnFailureValue extends RuntimeException {

    }


    public static final class LambdaException extends RuntimeException {
        public LambdaException(Throwable cause) {
            super(cause);
        }
    }
}
