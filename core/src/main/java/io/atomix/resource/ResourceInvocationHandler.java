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

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Resource proxy invocation handler.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class ResourceInvocationHandler implements InvocationHandler {
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Path pathAnnotation = method.getAnnotation(Path.class);
    if (pathAnnotation == null) {
      return null;
    }

    String path = pathAnnotation.value();

    Object body = null;
    boolean queryAdded = false;
    for (int i = 0; i < args.length; i++) {
      Parameter parameter = method.getParameters()[i];

      PathParam pathParam = parameter.getAnnotation(PathParam.class);
      if (pathParam != null) {
        Object arg = args[i];
        path = path.replace("{" + pathParam.value() + "}", arg.toString());
        continue;
      }

      QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
      if (queryParam != null) {
        Object arg = args[i];
        if (arg != null) {
          if (!queryAdded) {
            path += "?";
            queryAdded = true;
          }
          path += queryParam.value() + "=" + arg.toString();
        }
        continue;
      }

      // If we've made it this far, assume the parameter is the body of the method.
      body = args[i];
    }
    return new ResourceInvocation(path, body);
  }
}
