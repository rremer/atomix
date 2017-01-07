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

import io.atomix.catalyst.serializer.Serializer;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.resource.descriptor.ResourceDescriptor;

import java.util.Properties;

/**
 * Resource type.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public abstract class ResourceType2 {
  private final int id;
  private final String name;

  protected ResourceType2(int id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Returns the resource type ID.
   *
   * @return The resource type ID.
   */
  public int id() {
    return id;
  }

  /**
   * Returns the resource type name.
   *
   * @return The resource type name.
   */
  public String name() {
    return name;
  }

  /**
   * Registers resource types on the given serializer.
   *
   * @param serializer The serializer with which to register types.
   */
  public void register(Serializer serializer) {

  }

  public ResourceDescriptor descriptor() {
    return ResourceDescriptor.build(service());
  }

  public abstract Class<? extends ResourceService> service();

  public abstract ResourceService createService(Properties config);

  public abstract Resource<?> createResource(CopycatClient client, Properties config);

}
