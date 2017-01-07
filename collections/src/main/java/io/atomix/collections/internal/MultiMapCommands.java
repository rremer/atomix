/*
 * Copyright 2015 the original author or authors.
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
 * limitations under the License.
 */
package io.atomix.collections.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.catalyst.serializer.kryo.GenericKryoSerializer;
import io.atomix.catalyst.util.Assert;
import io.atomix.copycat.Command;
import io.atomix.copycat.Query;

import java.util.Collection;

/**
 * Map commands.
 * <p>
 * This class reserves serializable type IDs {@code 75} through {@code 84}
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class MultiMapCommands {

  private MultiMapCommands() {
  }

  /**
   * Abstract map command.
   */
  public static abstract class MultiMapCommand<V> implements Command<V> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.QUORUM;
    }
  }

  /**
   * Abstract map query.
   */
  public static abstract class MultiMapQuery<V> implements Query<V> {
    protected ConsistencyLevel consistency;

    protected MultiMapQuery() {
    }

    protected MultiMapQuery(ConsistencyLevel consistency) {
      this.consistency = consistency;
    }

    @Override
    @JsonGetter("consistency")
    public ConsistencyLevel consistency() {
      return consistency;
    }
  }

  /**
   * Abstract key-based command.
   */
  public static abstract class KeyCommand<V> extends MultiMapCommand<V> {
    protected Object key;

    protected KeyCommand() {
    }

    protected KeyCommand(Object key) {
      this.key = Assert.notNull(key, "key");
    }

    /**
     * Returns the key.
     */
    @JsonGetter("key")
    public Object key() {
      return key;
    }
  }

  /**
   * Abstract key-based query.
   */
  public static abstract class KeyQuery<V> extends MultiMapQuery<V> {
    protected Object key;

    protected KeyQuery() {
    }

    protected KeyQuery(Object key) {
      this.key = Assert.notNull(key, "key");
    }

    protected KeyQuery(Object key, ConsistencyLevel consistency) {
      super(consistency);
      this.key = Assert.notNull(key, "key");
    }

    /**
     * Returns the key.
     */
    @JsonGetter("key")
    public Object key() {
      return key;
    }
  }

  /**
   * Abstract value-based query.
   */
  public static abstract class ValueQuery<V> extends MultiMapQuery<V> {
    protected Object value;

    protected ValueQuery() {
    }

    protected ValueQuery(Object value) {
      this.value = value;
    }

    protected ValueQuery(Object value, ConsistencyLevel consistency) {
      super(consistency);
      this.value = value;
    }

    /**
     * Returns the value.
     */
    @JsonGetter("value")
    public Object value() {
      return value;
    }
  }

  /**
   * Entry query.
   */
  public static abstract class EntryQuery<V> extends KeyQuery<V> {
    protected Object value;

    protected EntryQuery() {
    }

    protected EntryQuery(Object key, Object value) {
      super(key);
      this.value = value;
    }

    protected EntryQuery(Object key, Object value, ConsistencyLevel consistency) {
      super(key, consistency);
      this.value = value;
    }

    /**
     * Returns the value.
     *
     * @return The value.
     */
    @JsonGetter("value")
    public Object value() {
      return value;
    }
  }

  /**
   * Contains key query.
   */
  public static class ContainsKey extends KeyQuery<Boolean> {
    ContainsKey() {
    }

    @JsonCreator
    public ContainsKey(@JsonProperty("key") Object key) {
      super(key);
    }

    @JsonCreator
    public ContainsKey(@JsonProperty("key") Object key, @JsonProperty("consistency") ConsistencyLevel consistency) {
      super(key, consistency);
    }
  }

  /**
   * Contains entry query.
   */
  public static class ContainsEntry extends EntryQuery<Boolean> {
    ContainsEntry() {
    }

    @JsonCreator
    public ContainsEntry(@JsonProperty("key") Object key, @JsonProperty("value") Object value) {
      super(key, value);
    }

    @JsonCreator
    public ContainsEntry(@JsonProperty("key") Object key, @JsonProperty("value") Object value, @JsonProperty("consistency") ConsistencyLevel consistency) {
      super(key, value, consistency);
    }
  }

  /**
   * Contains value query.
   */
  public static class ContainsValue extends ValueQuery<Boolean> {
    ContainsValue() {
    }

    @JsonCreator
    public ContainsValue(@JsonProperty("value") Object value) {
      super(value);
    }

    @JsonCreator
    public ContainsValue(@JsonProperty("value") Object value, @JsonProperty("consistency") ConsistencyLevel consistency) {
      super(value, consistency);
    }
  }

  /**
   * Entry command.
   */
  public static abstract class EntryCommand<V> extends KeyCommand<V> {
    protected Object value;

    protected EntryCommand() {
    }

    protected EntryCommand(Object key) {
      super(key);
    }

    protected EntryCommand(Object key, Object value) {
      super(key);
      this.value = value;
    }

    /**
     * Returns the command value.
     */
    @JsonGetter("value")
    public Object value() {
      return value;
    }
  }

  /**
   * TTL command.
   */
  public static abstract class TtlCommand<V> extends EntryCommand<V> {
    protected long ttl;

    protected TtlCommand() {
    }

    protected TtlCommand(Object key, Object value, long ttl) {
      super(key, value);
      this.ttl = ttl;
    }

    @Override
    public CompactionMode compaction() {
      return ttl > 0 ? CompactionMode.EXPIRING : CompactionMode.QUORUM;
    }

    /**
     * Returns the time to live in milliseconds.
     *
     * @return The time to live in milliseconds.
     */
    @JsonGetter("ttl")
    public long ttl() {
      return ttl;
    }
  }

  /**
   * Put command.
   */
  public static class Put extends TtlCommand<Boolean> {
    Put() {
    }

    @JsonCreator
    public Put(@JsonProperty("key") Object key, @JsonProperty("value") Object value) {
      super(key, value, 0);
    }

    @JsonCreator
    public Put(@JsonProperty("key") Object key, @JsonProperty("value") Object value, @JsonProperty("ttl") long ttl) {
      super(key, value, ttl);
    }
  }

  /**
   * Get query.
   */
  public static class Get extends KeyQuery<Collection> {
    Get() {
    }

    @JsonCreator
    public Get(@JsonProperty("key") Object key) {
      super(key);
    }

    @JsonCreator
    public Get(@JsonProperty("key") Object key, @JsonProperty("consistency") ConsistencyLevel consistency) {
      super(key, consistency);
    }
  }

  /**
   * Remove command.
   */
  public static class Remove extends EntryCommand<Object> {
    Remove() {
    }

    @JsonCreator
    public Remove(@JsonProperty("key") Object key) {
      super(key);
    }

    @JsonCreator
    public Remove(@JsonProperty("key") Object key, @JsonProperty("value") Object value) {
      super(key, value);
    }

    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Remove command.
   */
  public static class RemoveValue extends MultiMapCommand<Void> {
    private Object value;

    RemoveValue() {
    }

    @JsonCreator
    public RemoveValue(@JsonProperty("value") Object value) {
      this.value = value;
    }

    /**
     * Returns the value.
     */
    @JsonGetter("value")
    public Object value() {
      return value;
    }

    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Is empty query.
   */
  public static class IsEmpty extends MultiMapQuery<Boolean> {
    @JsonCreator
    public IsEmpty() {
    }

    @JsonCreator
    public IsEmpty(@JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
    }
  }

  /**
   * Size query.
   */
  public static class Size extends KeyQuery<Integer> {
    @JsonCreator
    public Size() {
    }

    @JsonCreator
    public Size(@JsonProperty("key") Object key) {
      super(key);
    }

    @JsonCreator
    public Size(@JsonProperty("key") Object key, @JsonProperty("consistency") ConsistencyLevel consistency) {
      super(key, consistency);
    }
  }

  /**
   * Clear command.
   */
  public static class Clear extends MultiMapCommand<Void> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Multi-map command type resolver.
   */
  public static class TypeResolver implements SerializableTypeResolver {
    @Override
    public void resolve(SerializerRegistry registry) {
      registry.register(ContainsKey.class, -80, t -> new GenericKryoSerializer());
      registry.register(ContainsEntry.class, -81, t -> new GenericKryoSerializer());
      registry.register(ContainsValue.class, -82, t -> new GenericKryoSerializer());
      registry.register(Put.class, -83, t -> new GenericKryoSerializer());
      registry.register(Get.class, -84, t -> new GenericKryoSerializer());
      registry.register(Remove.class, -85, t -> new GenericKryoSerializer());
      registry.register(RemoveValue.class, -86, t -> new GenericKryoSerializer());
      registry.register(IsEmpty.class, -87, t -> new GenericKryoSerializer());
      registry.register(Size.class, -88, t -> new GenericKryoSerializer());
      registry.register(Clear.class, -89, t -> new GenericKryoSerializer());
    }
  }

}
