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
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

/**
 * Delete handler.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public abstract class DeleteRequestHandler<T, U> extends RequestResponseHandler<T, U> {

  protected DeleteRequestHandler(AtomixServerContext server, Class<T> requestType) {
    super(server, requestType);
  }

  @Override
  public HttpMethod method() {
    return HttpMethod.DELETE;
  }

  @Override
  protected CompletableFuture<U> handleRequest(T request, RoutingContext context) {
    return delete(request, context);
  }

  protected abstract CompletableFuture<U> delete(T request, RoutingContext context);

}
