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


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TryTest {

    @Mock
    private Consumer<String> mockConsumer;

    @Captor
    private ArgumentCaptor<String> argCaptor;

    @Test(expected = Try.CallingFailureOnSuccessValue.class)
    public void callFailureOnSuccessValue() {
        Try<String> tryVal = Try.fromSuccess("Hello");
        tryVal.failure();
    }

    @Test(expected = Try.CallingSuccessOnFailureValue.class)
    public void callSuccessOnFailureValue() {
        Try<String> tryVal = Try.fromFailure(new RuntimeException("Oops!"));
        tryVal.success();
    }

    @Test
    public void flatMapOnSuccessValue() {
        Try<String> tryVal = Try.fromSuccess("Hello");

        Try<String> result = tryVal.flatMap((String s) -> Try.fromSuccess(s + " world"));

        assertThat(result.isSuccess(), is(true));
        assertThat(result.success(), is(equalTo("Hello world")));
    }

    @Test
    public void flatMapOnFailureValue() {
        Try<String> tryVal = Try.fromFailure(new RuntimeException("Oops!"));

        Try<String> result = tryVal.flatMap((String s) -> Try.fromSuccess(s + " world"));

        assertThat(result.isFailure(), is(true));
        assertThat(result.failure().getMessage(), is(equalTo("Oops!")));
    }

    @Test
    public void mapOnSuccessValue() {
        Try<String> tryVal = Try.fromSuccess("Hello");

        Try<String> result = tryVal.map((String s) -> s + " world");

        assertThat(result.isSuccess(), is(true));
        assertThat(result.success(), is(equalTo("Hello world")));
    }

    @Test
    public void mapOnFailureValue() {
        Try<String> tryVal = Try.fromFailure(new RuntimeException("Oops!"));

        Try<String> result = tryVal.map((String s) -> s + " world");

        assertThat(result.isFailure(), is(true));
        assertThat(result.failure().getMessage(), is(equalTo("Oops!")));
    }

    @Test
    public void getOnSuccessValue() throws Throwable {
        Try<String> tryVal = Try.fromSuccess("Hello");

        String result = tryVal.get();

        assertThat(result, is(equalTo("Hello")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getOnFailureValue() throws Throwable {
        Try<String> tryVal = Try.fromFailure(new UnsupportedOperationException("Not supported"));
        tryVal.get();
    }

    @Test
    public void getOrElseOnSuccessValue() {
        Try<String> tryVal = Try.fromSuccess("Hello");

        String result = tryVal.getOrElse("Ola");

        assertThat(result, is(equalTo("Hello")));
    }

    @Test
    public void getOrElseOnFailureValue() {
        Try<String> tryVal = Try.fromFailure(new RuntimeException("Oops!"));

        String result = tryVal.getOrElse("Ola");

        assertThat(result, is(equalTo("Ola")));
    }

    @Test
    public void orElseOnSuccessValue() {
        Try<String> tryVal = Try.fromSuccess("Hello");

        Try<String> result = tryVal.orElse(Try.fromSuccess("Ola"));

        assertThat(result.isSuccess(), is(true));
        assertThat(result.success(), is(equalTo("Hello")));
    }

    @Test
    public void orElseOnFailureValue() {
        Try<String> tryVal = Try.fromFailure(new RuntimeException("Oops!"));

        Try<String> result = tryVal.orElse(Try.fromSuccess("Ola"));

        assertThat(result.isSuccess(), is(true));
        assertThat(result.success(), is(equalTo("Ola")));
    }

    @Test
    public void toOptionalOnSuccessValue() {
        Try<String> tryVal = Try.fromSuccess("Hello");

        Optional<String> result = tryVal.toOptional();

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(equalTo("Hello")));
    }

    @Test
    public void toOptionalOnFailureValue() {
        Try<String> tryVal = Try.fromFailure(new RuntimeException("Oops!"));

        Optional<String> result = tryVal.toOptional();

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void filterPredicateSatisfiedOnSuccessValue() {
        Try<String> tryVal = Try.fromSuccess("Hello");

        Try<String> result = tryVal.filter((String s) -> s.equals("Hello"));

        assertThat(result.isSuccess(), is(true));
        assertThat(result.success(), is(equalTo("Hello")));
    }

    @Test
    public void filterPredicateUnsatisfiedOnSuccessValue() {
        Try<String> tryVal = Try.fromSuccess("Hello");

        Try<String> result = tryVal.filter((String s) -> s.equals("Ola"));

        assertThat(result.isSuccess(), is(false));
    }

    @Test
    public void filterOnFailureValue() {
        Try<String> tryVal = Try.fromFailure(new RuntimeException("Oops!"));

        Try<String> result = tryVal.filter((String s) -> s.equals("Ola"));

        assertThat(result.isSuccess(), is(false));
        assertThat(result.failure().getMessage(), is(equalTo("Oops!")));
    }

    @Test
    public void foreachOnSuccessValue() {
        Try<String> tryVal = Try.fromSuccess("Hello");

        tryVal.foreach(mockConsumer::accept);

        verify(mockConsumer, only()).accept(argCaptor.capture());

        assertThat(argCaptor.getValue(), is(equalTo("Hello")));
    }

    @Test
    public void foreachOnFailureValue() {
        Try<String> tryVal = Try.fromFailure(new RuntimeException("Oops!"));

        tryVal.foreach(mockConsumer::accept);

        verify(mockConsumer, never()).accept(argCaptor.capture());
    }

    @Test
    public void wrapWellBehavedFunction() {
        Try<String> result = Try.doTry(this::functionThatDoesNotThrowException);

        assertThat(result.isSuccess(), is(true));
        assertThat(result.success(), is(equalTo("Hello")));
    }

    @Test
    public void wrapFunctionThrowingException() {
        Try<String> result = Try.doTry(this::functionThatThrowsException);

        assertThat(result.isSuccess(), is(false));
        assertThat(result.failure().getMessage(), is(equalTo("I will not succeed")));
    }


    private String functionThatDoesNotThrowException(){
        return "Hello";
    }

    private String functionThatThrowsException() throws Exception {
        throw new Exception("I will not succeed");
    }
}
