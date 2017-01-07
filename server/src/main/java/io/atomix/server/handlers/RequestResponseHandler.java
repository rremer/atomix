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

import io.atomix.server.AtomixServerContext;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Request response serializing handler.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public abstract class RequestResponseHandler<T, U> extends RequestHandler {
  private final Class<T> requestType;

  public RequestResponseHandler(AtomixServerContext server, Class<T> requestType) {
    super(server);
    this.requestType = requestType;
  }

  @Override
  public void handle(RoutingContext context) {
    T request;
    try {
      String body = context.getBodyAsString();
      request = server.getObjectMapper().readValue(body, requestType);
    } catch (IOException e) {
      fail(context, e, 500);
      return;
    }

    handleRequest(request, context).whenComplete((response, error) -> {
      context.vertx().runOnContext(v -> {
        if (error == null) {
          succeed(context, response);
        } else {
          fail(context, error, 500);
        }
      });
    });
  }

  protected abstract CompletableFuture<U> handleRequest(T request, RoutingContext context);

}
