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

import io.atomix.resource.scheduler.Scheduler;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Map state.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class MapState<K, V> implements MapService<K, V> {
  private final Map<K, V> map = new HashMap<>();

  @Override
  public V put(@PathParam("key") K key, V value) {
    return map.put(key, value);
  }

  @Override
  public V put(@PathParam("key") K key, V value, @QueryParam("ttl") Duration ttl, @Context Scheduler scheduler) {
    scheduler.schedule(ttl, () -> map.remove(key));
    return map.put(key, value);
  }

  @Override
  public V get(@PathParam("key") K key) {
    return map.get(key);
  }

  @Override
  public V remove(@PathParam("key") K key) {
    return map.remove(key);
  }

  @Override
  public void clear() {

  }
}
