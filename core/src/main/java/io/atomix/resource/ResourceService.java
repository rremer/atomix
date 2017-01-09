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
package io.atomix.resource;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Resource service.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public interface ResourceService {

  /**
   * Creates a new proxy for the given resource service type.
   *
   * @param type The resource service type.
   * @param <T> The resource service type.
   * @return The resource service proxy.
   */
  @SuppressWarnings("unchecked")
  static <T extends ResourceService> T proxy(Class<T> type) {
    DynamicType.Builder<T> builder = new ByteBuddy()
      .subclass(type);
    DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<T> definition = null;
    for (Method method : type.getMethods()) {
      definition = builder.method(ElementMatchers.is(method))
        .intercept(InvocationHandlerAdapter.of(new ResourceInvocationHandler()));
    }

    if (definition == null) {
      return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new ResourceInvocationHandler());
    }

    Class<?> dynamicType = definition.make()
      .load(type.getClassLoader())
      .getLoaded();
    return (T) Proxy.newProxyInstance(dynamicType.getClassLoader(), new Class[]{dynamicType}, new ResourceInvocationHandler());
  }

}
