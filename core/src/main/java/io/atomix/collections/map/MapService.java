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
package io.atomix.collections.map;

import io.atomix.resource.ResourceService;
import io.atomix.resource.scheduler.Scheduler;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.time.Duration;

/**
 * Distributed map service.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public interface MapService<K, V> extends ResourceService {

  @PUT
  @Path("/{key}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  V put(@PathParam("key") K key, V value);

  @PUT
  @Path("/{key}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  V put(@PathParam("key") K key, V value, @QueryParam("ttl") Duration ttl, @Context Scheduler scheduler);

  @GET
  @Path("/{key}")
  @Produces(MediaType.TEXT_PLAIN)
  V get(@PathParam("key") K key);

  @DELETE
  @Path("/{key}")
  @Produces(MediaType.TEXT_PLAIN)
  V remove(@PathParam("key") K key);

  @DELETE
  void clear();

}
