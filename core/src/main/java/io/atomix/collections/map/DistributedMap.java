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

import io.atomix.resource.Resource;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Distributed map interface.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public interface DistributedMap<K, V> extends Resource {

  CompletableFuture<V> put(K key, V value);

  CompletableFuture<V> put(K key, V value, Duration ttl);

  CompletableFuture<V> get(K key);

  CompletableFuture<V> remove(K key);

  CompletableFuture<Void> clear();

}
