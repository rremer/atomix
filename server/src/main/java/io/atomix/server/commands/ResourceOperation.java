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
package io.atomix.server.commands;

import io.atomix.copycat.Operation;

/**
 * Abstract resource operation.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public abstract class ResourceOperation<T extends Operation<U>, U> implements Operation<U> {
  protected long session;
  protected String resource;
  protected T operation;

  public ResourceOperation() {
  }

  public ResourceOperation(long session, String resource, T operation) {
    this.session = session;
    this.resource = resource;
    this.operation = operation;
  }

  public long session() {
    return session;
  }

  public String resource() {
    return resource;
  }

  public T operation() {
    return operation;
  }
}
