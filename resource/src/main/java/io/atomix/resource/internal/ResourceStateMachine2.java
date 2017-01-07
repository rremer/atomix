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
package io.atomix.resource.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.StateMachineExecutor;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.server.session.SessionListener;
import io.atomix.resource.ResourceService;
import io.atomix.resource.descriptor.OperationDescriptor;
import io.atomix.resource.descriptor.ParameterDescriptor;
import io.atomix.resource.descriptor.ResourceDescriptor;

import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Function;

/**
 * Resource state machine.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class ResourceStateMachine2 extends StateMachine implements SessionListener {
  private final ResourceService service;
  private final ResourceDescriptor descriptor;
  private final ObjectMapper mapper = new ObjectMapper();

  public ResourceStateMachine2(ResourceService service) {
    this.service = service;
    this.descriptor = ResourceDescriptor.build(service.getClass());
  }

  @Override
  protected void configure(StateMachineExecutor executor) {
    executor.register(ResourceOperation.class, (Function<Commit<ResourceOperation>, Object>) this::operation);
  }

  protected Object operation(Commit<ResourceOperation> commit) {
    OperationDescriptor operation = descriptor.operation(commit.operation().getClass());
    if (operation != null) {
      Map command = mapper.convertValue(commit.operation(), Map.class);
      Object[] params = new Object[operation.params().size()];
      for (int i = 0; i < operation.params().size(); i++) {
        ParameterDescriptor parameter = operation.params().get(i);
        params[i] = command.get(parameter.name());
      }

      try {
        Object response = operation.method().invoke(service, params);
        if (response instanceof Response) {
          return ((Response) response).getEntity();
        } else {
          return response;
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
    throw new IllegalArgumentException();
  }

  @Override
  public void register(ServerSession serverSession) {

  }

  @Override
  public void unregister(ServerSession serverSession) {

  }

  @Override
  public void expire(ServerSession serverSession) {

  }

  @Override
  public void close(ServerSession serverSession) {

  }
}
