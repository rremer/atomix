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
package io.atomix.server;

import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.cluster.Member;
import io.atomix.copycat.server.state.ConnectionManager;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.server.handlers.RequestHandler;
import io.atomix.server.state.AtomixServerState;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

/**
 * Atomix server.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixServer extends CopycatServer {
  private final AtomixServerContext context;
  private HttpServer server;

  protected AtomixServer(
    String name,
    Member.Type type,
    Address serverAddress,
    Transport serverTransport,
    Address clientAddress,
    Transport clientTransport,
    Address httpAddress,
    Storage storage,
    Serializer serializer,
    ThreadContext threadContext) {
    this(name, clientTransport, serverTransport, new AtomixServerContext(name, type, serverAddress, clientAddress, httpAddress, storage, serializer, AtomixServerState::new, new ConnectionManager(serverTransport.client()), threadContext));
  }

  protected AtomixServer(String name, Transport clientTransport, Transport serverTransport, AtomixServerContext context) {
    super(name, clientTransport, serverTransport, context);
    this.context = context;
  }

  @Override
  public CompletableFuture<CopycatServer> bootstrap(Collection<Address> cluster) {
    return super.bootstrap(cluster).thenCompose(v -> startHttpServer());
  }

  @Override
  public CompletableFuture<CopycatServer> join(Collection<Address> cluster) {
    return super.join(cluster).thenCompose(v -> startHttpServer());
  }

  /**
   * Starts the HTTP server.
   */
  private CompletableFuture<CopycatServer> startHttpServer() {
    Vertx vertx = Vertx.vertx();
    server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    registerRoutes(router);

    CompletableFuture<CopycatServer> future = new CompletableFuture<>();
    server.requestHandler(router::accept).listen(context.getHttpAddress().port(), context.getHttpAddress().host(), result -> {
      context.getThreadContext().execute(() -> {
        if (result.succeeded()) {
          future.complete(this);
        } else {
          future.completeExceptionally(result.cause());
        }
      });
    });
    return future;
  }

  /**
   * Registers routes on the given router.
   */
  private void registerRoutes(Router router) {
    router.route().handler(BodyHandler.create());
    for (RequestHandler.Factory factory : ServiceLoader.load(RequestHandler.Factory.class)) {
      RequestHandler handler = factory.createHandler(context);
      router.route(handler.method(), handler.route()).handler(handler);
    }
  }
}
