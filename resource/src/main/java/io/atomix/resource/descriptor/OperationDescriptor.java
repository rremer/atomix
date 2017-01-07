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
import io.atomix.resource.annotations.Command;
import io.atomix.resource.annotations.Query;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Resource operation descriptor.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class OperationDescriptor {

  /**
   * Builds an operation descriptor for the given method.
   *
   * @param method The method for which to build a descriptor.
   * @return The operation descriptor for the given method.
   */
  public static OperationDescriptor build(Method method) {
    OperationDescriptor.Builder builder = OperationDescriptor.builder()
      .withMethod(method);

    Command command = method.getAnnotation(Command.class);
    if (command != null) {
      builder.withOperation(command.value());
    } else {
      Query query = method.getAnnotation(Query.class);
      if (query != null) {
        builder.withOperation(query.value());
      } else {
        return null;
      }
    }

    Path path = method.getAnnotation(Path.class);
    if (path != null) {
      builder.withPath(path.value());
    }

    Consumes consumes = method.getAnnotation(Consumes.class);
    if (consumes != null) {
      builder.withConsumes(consumes.value());
    }

    Produces produces = method.getAnnotation(Produces.class);
    if (produces != null) {
      builder.withProduces(produces.value());
    }

    HttpMethod httpMethod = method.getAnnotation(HttpMethod.class);
    if (httpMethod != null) {
      builder.addMethod(httpMethod.value());
    }

    Collection<Class<? extends Annotation>> methodAnnotations = Arrays.asList(GET.class, POST.class, PUT.class, DELETE.class, HEAD.class, OPTIONS.class);
    for (Class<? extends Annotation> methodAnnotation : methodAnnotations) {
      Annotation annotation = method.getAnnotation(methodAnnotation);
      if (annotation != null) {
        builder.addMethod(methodAnnotation.getSimpleName());
      }
    }

    for (Parameter parameter : method.getParameters()) {
      ParameterDescriptor.Builder parameterBuilder = ParameterDescriptor.builder()
        .withName(parameter.getName())
        .withType(parameter.getType());
      builder.addParam(parameterBuilder.build());

      PathParam pathParam = parameter.getAnnotation(PathParam.class);
      if (pathParam != null) {
        builder.addPathParam(parameterBuilder.build());
      }

      QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
      if (queryParam != null) {
        builder.addQueryParam(parameterBuilder.build());
      }
    }

    return builder.build();
  }

  public static Builder builder() {
    return new Builder();
  }

  private final String path;
  private final Set<String> methods;
  private final Set<String> consumes;
  private final Set<String> produces;
  private final List<ParameterDescriptor> pathParams;
  private final List<ParameterDescriptor> queryParams;
  private final Method method;
  private final List<ParameterDescriptor> params;
  private final Class<? extends Operation<?>> operation;

  public OperationDescriptor(String path, Set<String> methods, Set<String> consumes, Set<String> produces, List<ParameterDescriptor> pathParams, List<ParameterDescriptor> queryParams, Method method, List<ParameterDescriptor> params, Class<? extends Operation<?>> operation) {
    this.path = path;
    this.methods = methods;
    this.consumes = consumes;
    this.produces = produces;
    this.pathParams = pathParams;
    this.queryParams = queryParams;
    this.method = method;
    this.params = params;
    this.operation = operation;
  }

  public String path() {
    return path;
  }

  public Set<String> methods() {
    return methods;
  }

  public Set<String> consumes() {
    return consumes;
  }

  public Set<String> produces() {
    return produces;
  }

  public List<ParameterDescriptor> pathParams() {
    return pathParams;
  }

  public List<ParameterDescriptor> queryParams() {
    return queryParams;
  }

  public Method method() {
    return method;
  }

  public List<ParameterDescriptor> params() {
    return params;
  }

  public Class<? extends Operation<?>> operation() {
    return operation;
  }

  public static class Builder implements io.atomix.catalyst.util.Builder<OperationDescriptor> {
    private String path;
    private Set<String> methods = new HashSet<>();
    private String[] consumes;
    private String[] produces;
    private List<ParameterDescriptor> pathParams = new ArrayList<>();
    private List<ParameterDescriptor> queryParams = new ArrayList<>();
    private Method method;
    private List<ParameterDescriptor> params = new ArrayList<>();
    private Class<? extends Operation<?>> operation;

    public Builder withPath(String path) {
      this.path = path;
      return this;
    }

    public Builder addMethod(String method) {
      methods.add(method);
      return this;
    }

    public Builder withMethods(String... methods) {
      this.methods.addAll(Arrays.asList(methods));
      return this;
    }

    public Builder withConsumes(String[] consumes) {
      this.consumes = consumes;
      return this;
    }

    public Builder withProduces(String[] produces) {
      this.produces = produces;
      return this;
    }

    public Builder addPathParam(ParameterDescriptor param) {
      this.pathParams.add(param);
      return this;
    }

    public Builder addQueryParam(ParameterDescriptor param) {
      this.queryParams.add(param);
      return this;
    }

    public Builder withMethod(Method method) {
      this.method = method;
      return this;
    }

    public Builder addParam(ParameterDescriptor param) {
      this.params.add(param);
      return this;
    }

    public Builder withOperation(Class<? extends Operation<?>> operation) {
      this.operation = operation;
      return this;
    }

    @Override
    public OperationDescriptor build() {
      return new OperationDescriptor(path, methods, new HashSet<>(Arrays.asList(consumes)), new HashSet<>(Arrays.asList(produces)), pathParams, queryParams, method, params, operation);
    }
  }

}
