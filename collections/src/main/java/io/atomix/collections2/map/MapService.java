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
package io.atomix.collections2.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.atomix.resource.ResourceService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Map service.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class MapService extends ResourceService {
  private Map<Object, Object> map = new HashMap<>();

  @POST
  @PUT
  @Path("/{key}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response put(@PathParam("key") Object key, @JsonProperty("value") Object value, @QueryParam("ttl") Long ttl) {
    return Response.ok(map.put(key, value)).build();
  }

  @GET
  @Path("/{key}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(@PathParam("key") Object key) {
    return Response.ok(map.get(key)).build();
  }

}
