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
package io.atomix.server.handlers;

import io.atomix.copycat.protocol.RegisterRequest;
import io.atomix.server.AtomixServerContext;
import io.atomix.server.protocol.CreateSessionRequest;
import io.atomix.server.protocol.CreateSessionResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Create session handler.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class CreateSessionHandler extends PostRequestHandler<CreateSessionRequest, CreateSessionResponse> {

  public static class Factory implements RequestHandler.Factory {
    @Override
    public RequestHandler createHandler(AtomixServerContext server) {
      return new CreateSessionHandler(server);
    }
  }

  public CreateSessionHandler(AtomixServerContext server) {
    super(server, CreateSessionRequest.class);
  }

  @Override
  public String route() {
    return "/sessions";
  }

  @Override
  protected CompletableFuture<CreateSessionResponse> post(CreateSessionRequest request, RoutingContext context) {
    final String name = request.getName() != null ? request.getName() : UUID.randomUUID().toString();
    final long timeout = request.getTimeout() != 0 ? request.getTimeout() : server.getSessionTimeout().toMillis();
    return server.getProxy().register(RegisterRequest.builder()
      .withClient(name)
      .withTimeout(timeout)
      .build()).thenApply(result -> {
      CreateSessionResponse response = new CreateSessionResponse();
      response.setSession(result.session());
      return response;
    });
  }

}
