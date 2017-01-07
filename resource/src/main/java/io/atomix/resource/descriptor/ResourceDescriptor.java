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
package io.atomix.resource.descriptor;

import io.atomix.copycat.Operation;
import io.atomix.resource.ResourceService;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Resource descriptor.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class ResourceDescriptor {

  /**
   * Builds a descriptor for the given resource service class.
   *
   * @param service The resource service class for which to build the descriptor.
   * @return The resource descriptor.
   */
  public static ResourceDescriptor build(Class<? extends ResourceService> service) {
    ResourceDescriptor.Builder builder = ResourceDescriptor.builder();
    for (Method method : service.getClass().getMethods()) {
      OperationDescriptor operation = OperationDescriptor.build(method);
      if (operation != null) {
        builder.addOperation(operation);
      }
    }
    return builder.build();
  }

  public static Builder builder() {
    return new Builder();
  }

  private final Map<Class<? extends Operation<?>>, OperationDescriptor> operations;

  private ResourceDescriptor(Map<Class<? extends Operation<?>>, OperationDescriptor> operations) {
    this.operations = operations;
  }

  public OperationDescriptor operation(Class<?> type) {
    return operations.get(type);
  }

  /**
   * Resource descriptor builder.
   */
  public static class Builder implements io.atomix.catalyst.util.Builder<ResourceDescriptor> {
    private final Map<Class<? extends Operation<?>>, OperationDescriptor> operations = new HashMap<>();

    private Builder() {
    }

    public Builder addOperation(OperationDescriptor descriptor) {
      operations.put(descriptor.operation(), descriptor);
      return this;
    }

    @Override
    public ResourceDescriptor build() {
      return new ResourceDescriptor(operations);
    }
  }
}
