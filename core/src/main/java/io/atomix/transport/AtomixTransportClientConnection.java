/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package io.atomix.transport;

import io.atomix.catalyst.concurrent.Listener;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.MessageHandler;
import io.vertx.core.http.WebSocket;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Atomix transport client connection.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixTransportClientConnection implements Connection {
  private final WebSocket socket;

  public AtomixTransportClientConnection(WebSocket socket) {
    this.socket = socket;
  }

  @Override
  public <T, U> CompletableFuture<U> send(T t) {
    return null;
  }

  @Override
  public <T, U> Connection handler(Class<T> aClass, MessageHandler<T, U> messageHandler) {
    return null;
  }

  @Override
  public Listener<Throwable> exceptionListener(Consumer<Throwable> consumer) {
    return null;
  }

  @Override
  public Listener<Connection> closeListener(Consumer<Connection> consumer) {
    return null;
  }

  @Override
  public CompletableFuture<Void> close() {
    return null;
  }
}
