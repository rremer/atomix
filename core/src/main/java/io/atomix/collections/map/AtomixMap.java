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

import io.atomix.resource.AbstractResource;
import io.atomix.resource.ResourceClient;
import io.atomix.resource.ResourceService;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Distributed map resource.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixMap<K, V> extends AbstractResource implements DistributedMap<K, V> {
  private final MapService proxy;

  public AtomixMap(ResourceClient client) {
    super(client);
    this.proxy = ResourceService.proxy(MapService.class);
  }

  @Override
  public CompletableFuture<V> put(K key, V value) {
    return null;
  }

  @Override
  public CompletableFuture<V> put(K key, V value, Duration ttl) {
    return null;
  }

  @Override
  public CompletableFuture<V> get(K key) {
    return null;
  }

  @Override
  public CompletableFuture<V> remove(K key) {
    return null;
  }

  @Override
  public CompletableFuture<Void> clear() {
    return null;
  }
}
