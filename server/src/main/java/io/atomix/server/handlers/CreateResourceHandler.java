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

import io.atomix.copycat.protocol.CommandRequest;
import io.atomix.server.AtomixServerContext;
import io.atomix.server.commands.CreateResourceCommand;
import io.atomix.server.protocol.CreateResourceRequest;
import io.atomix.server.protocol.CreateResourceResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

/**
 * Create resource handler.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class CreateResourceHandler extends PostRequestHandler<CreateResourceRequest, CreateResourceResponse> {

  public static class Factory implements RequestHandler.Factory {
    @Override
    public RequestHandler createHandler(AtomixServerContext server) {
      return new CreateResourceHandler(server);
    }
  }

  public CreateResourceHandler(AtomixServerContext server) {
    super(server, CreateResourceRequest.class);
  }

  @Override
  public String route() {
    return "/sessions/:sessionId/resources";
  }

  @Override
  protected CompletableFuture<CreateResourceResponse> post(CreateResourceRequest request, RoutingContext context) {
    return server.getProxy().command(CommandRequest.builder()
      .withSession(Long.valueOf(context.pathParam("sessionId")))
      .withCommand(new CreateResourceCommand(Long.valueOf(context.pathParam("sessionId")), request.getName(), request.getType()))
      .build()).thenApply(result -> {
      CreateResourceResponse response = new CreateResourceResponse();
      response.setIndex(result.index());
      return response;
    });
  }

}
