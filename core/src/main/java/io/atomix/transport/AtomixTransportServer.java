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

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Server;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Atomix transport server.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixTransportServer implements Server {
  private final HttpServer server;
  private final Vertx vertx;
  private final Router router;
  private Consumer<Connection> listener;

  public AtomixTransportServer(HttpServer server, Vertx vertx) {
    this.server = server;
    this.vertx = vertx;
    this.router = Router.router(vertx);
  }

  /**
   * Sets up the HTTP router.
   */
  private void setupRouter() {
    server.websocketHandler(socket -> {
      // If the socket is not connected to the socket path, reject it.
      if (!socket.path().equals(AtomixTransport.SOCKET_PATH)) {
        socket.reject();
      } else {
        listener.accept(new AtomixTransportServerSocketConnection(socket));
      }
    });

    router.route().handler(BodyHandler.create());

    router.route("/sessions")
      .method(HttpMethod.POST)
      .consumes("application/json")
      .produces("application/json")
      .handler(context -> {

      });

    server.requestHandler(router::accept);
  }

  @Override
  public CompletableFuture<Void> listen(Address address, Consumer<Connection> consumer) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    server.listen(address.port(), address.host(), result -> {
      if (result.succeeded()) {
        future.complete(null);
      } else {
        future.completeExceptionally(result.cause());
      }
    });
    this.listener = consumer;
    return future;
  }

  @Override
  public CompletableFuture<Void> close() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    server.close(result -> {
      if (result.succeeded()) {
        future.complete(null);
      } else {
        future.completeExceptionally(result.cause());
      }
    });
    return future;
  }
}
