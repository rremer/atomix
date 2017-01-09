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

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Client;
import io.atomix.catalyst.transport.Connection;
import io.vertx.core.http.HttpClient;

import java.util.concurrent.CompletableFuture;

/**
 * Atomix transport client.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixTransportClient implements Client {
  private final HttpClient client;

  public AtomixTransportClient(HttpClient client) {
    this.client = client;
  }

  @Override
  public CompletableFuture<Connection> connect(Address address) {
    CompletableFuture<Connection> future = new CompletableFuture<>();
    client.websocket(address.port(), address.host(), AtomixTransport.SOCKET_PATH, socket -> {
      future.complete(new AtomixTransportClientConnection(socket));
    }, future::completeExceptionally);
    return future;
  }

  @Override
  public CompletableFuture<Void> close() {
    client.close();
    return Futures.completedFuture(null);
  }
}
