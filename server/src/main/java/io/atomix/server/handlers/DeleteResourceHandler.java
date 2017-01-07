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
import io.atomix.server.commands.DeleteResourceCommand;
import io.atomix.server.protocol.DeleteResourceRequest;
import io.atomix.server.protocol.DeleteResourceResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

/**
 * Delete resource handler.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class DeleteResourceHandler extends DeleteRequestHandler<DeleteResourceRequest, DeleteResourceResponse> {

  public static class Factory implements RequestHandler.Factory {
    @Override
    public RequestHandler createHandler(AtomixServerContext server) {
      return new DeleteResourceHandler(server);
    }
  }

  public DeleteResourceHandler(AtomixServerContext server) {
    super(server, DeleteResourceRequest.class);
  }

  @Override
  public String route() {
    return "/sessions/:sessionId/resources/:resourceId";
  }

  @Override
  protected CompletableFuture<DeleteResourceResponse> delete(DeleteResourceRequest request, RoutingContext context) {
    return server.getProxy().command(CommandRequest.builder()
      .withSession(request.getSession())
      .withCommand(new DeleteResourceCommand(Long.valueOf(context.pathParam("sessionId")), context.pathParam("resourceId")))
      .build()).thenApply(result -> {
      DeleteResourceResponse response = new DeleteResourceResponse();
      response.setIndex(result.index());
      return response;
    });
  }

}
