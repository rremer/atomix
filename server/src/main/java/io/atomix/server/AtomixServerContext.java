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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.util.Assert;
import io.atomix.copycat.protocol.*;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.cluster.Member;
import io.atomix.copycat.server.state.ConnectionManager;
import io.atomix.copycat.server.state.ServerContext;
import io.atomix.copycat.server.state.ServerState;
import io.atomix.copycat.server.storage.Storage;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Atomix server context.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixServerContext extends ServerContext {
  private final Proxy proxy = new Proxy(this);
  private final Address httpAddress;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AtomixServerContext(String name, Member.Type type, Address serverAddress, Address clientAddress, Address httpAddress, Storage storage, Serializer serializer, Supplier<StateMachine> stateMachineFactory, ConnectionManager connections, ThreadContext threadContext) {
    super(name, type, serverAddress, clientAddress, storage, serializer, stateMachineFactory, connections, threadContext);
    this.httpAddress = Assert.notNull(httpAddress, "httpAddress");
  }

  /**
   * Returns the server state proxy.
   *
   * @return The server state proxy.
   */
  public Proxy getProxy() {
    return proxy;
  }

  /**
   * Returns the server state.
   *
   * @return The server state.
   */
  public ServerState getServerState() {
    return state;
  }

  /**
   * Returns the server's HTTP address.
   *
   * @return The server's HTTP address.
   */
  public Address getHttpAddress() {
    return httpAddress;
  }

  /**
   * Returns the server object mapper.
   *
   * @return The server object mapper.
   */
  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /**
   * Atomix server proxy.
   */
  public static class Proxy {
    private final AtomixServerContext context;

    private Proxy(AtomixServerContext context) {
      this.context = context;
    }

    private <T extends Response> CompletableFuture<T> execute(Supplier<CompletableFuture<T>> callback) {
      CompletableFuture<T> future = new CompletableFuture<>();
      context.getThreadContext().execute(() -> {
        callback.get().whenComplete((response, error) -> {
          if (error == null) {
            future.complete(response);
          } else {
            future.completeExceptionally(error);
          }
        });
      });
      return future;
    }

    public CompletableFuture<RegisterResponse> register(RegisterRequest request) {
      return execute(() -> context.getServerState().register(request));
    }

    public CompletableFuture<KeepAliveResponse> keepAlive(KeepAliveRequest request) {
      return execute(() -> context.getServerState().keepAlive(request));
    }

    public CompletableFuture<CommandResponse> command(CommandRequest request) {
      return execute(() -> context.getServerState().command(request));
    }

    public CompletableFuture<QueryResponse> query(QueryRequest request) {
      return execute(() -> context.getServerState().query(request));
    }
  }
}
