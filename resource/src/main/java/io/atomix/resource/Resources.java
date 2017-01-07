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

import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resource utilities.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class Resources {
  private static final Map<Integer, ResourceType2> idTypes = new ConcurrentHashMap<>();
  private static final Map<String, ResourceType2> namedTypes = new ConcurrentHashMap<>();

  /**
   * Loads a list of resource types.
   *
   * @return A collection of registered resource types.
   */
  public static Collection<ResourceType2> resources() {
    return resources(false);
  }

  /**
   * Loads a list of resource types.
   *
   * @param clearCache Whether to clear the type cache.
   * @return A collection of registered resource types.
   */
  public static Collection<ResourceType2> resources(boolean clearCache) {
    load(clearCache);
    return idTypes.values();
  }

  /**
   * Returns a resource type by ID.
   *
   * @param id The resource type ID.
   * @return The resource type.
   */
  public static ResourceType2 resource(int id) {
    load(false);
    ResourceType2 type = idTypes.get(id);
    if (type == null) {
      load(true);
      type = idTypes.get(id);
    }
    return type;
  }

  /**
   * Returns a resource type by name.
   *
   * @param name The resource type name.
   * @return The resource type.
   */
  public static ResourceType2 resource(String name) {
    load(false);
    ResourceType2 type = namedTypes.get(name);
    if (type == null) {
      load(true);
      type = namedTypes.get(name);
    }
    return type;
  }

  /**
   * Loads and caches resource types.
   */
  private static void load(boolean clearCache) {
    if (clearCache || idTypes.isEmpty()) {
      synchronized (ResourceType2.class) {
        if (clearCache || idTypes.isEmpty()) {
          for (ResourceType2 type : ServiceLoader.load(ResourceType2.class)) {
            idTypes.put(type.id(), type);
            namedTypes.put(type.name(), type);
          }
        }
      }
    }
  }

  private Resources() {
  }
}
