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
import io.atomix.server.protocol.ErrorResponse;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;

/**
 * Request handler.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public abstract class RequestHandler implements Handler<RoutingContext> {

  /**
   * Request handler factory.
   */
  public interface Factory {
    RequestHandler createHandler(AtomixServerContext server);
  }

  protected final AtomixServerContext server;

  protected RequestHandler(AtomixServerContext server) {
    this.server = server;
  }

  public abstract String route();

  public abstract HttpMethod method();

  /**
   * Handles a successful response.
   */
  protected void succeed(RoutingContext context, Object response) {
    String body;
    try {
      body = server.getObjectMapper().writeValueAsString(response);
    } catch (IOException e) {
      fail(context, e, 500);
      return;
    }

    context.response().putHeader("content-type", "application/json");
    context.response().setStatusCode(200);
    context.response().end(body);
  }

  /**
   * Fails the given request.
   */
  protected void fail(RoutingContext context, Throwable error, int code) {
    ErrorResponse response = new ErrorResponse();
    response.setError(error.getMessage());
    context.response().putHeader("content-type", "application/json");
    context.response().setStatusCode(code);
    try {
      context.response().end(server.getObjectMapper().writeValueAsString(response));
    } catch (Exception e) {
      context.response().setStatusCode(500);
      context.response().end();
    }
  }

}
