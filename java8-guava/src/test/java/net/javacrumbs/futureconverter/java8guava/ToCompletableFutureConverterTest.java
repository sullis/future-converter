/**
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.futureconverter.java8guava;

import com.google.common.util.concurrent.ListenableFuture;
import net.javacrumbs.futureconverter.common.test.AbstractConverterHelperBasedTest;
import net.javacrumbs.futureconverter.common.test.guava.GuavaOriginalFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.java8.Java8ConvertedFutureTestHelper;

import java.util.concurrent.CompletableFuture;

import static net.javacrumbs.futureconverter.java8guava.FutureConverter.toCompletableFuture;
import static net.javacrumbs.futureconverter.java8guava.FutureConverter.toListenableFuture;


public class ToCompletableFutureConverterTest extends AbstractConverterHelperBasedTest<
        ListenableFuture<String>,
        CompletableFuture<String>> {

    public ToCompletableFutureConverterTest() {
        super(new GuavaOriginalFutureTestHelper(), new Java8ConvertedFutureTestHelper());
    }

    @Override
    protected CompletableFuture<String> convert(ListenableFuture<String> originalFuture) {
        return toCompletableFuture(originalFuture);
    }

    @Override
    protected ListenableFuture<String> convertBack(CompletableFuture<String> converted) {
        return toListenableFuture(converted);
    }
}
