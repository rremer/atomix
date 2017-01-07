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
import java.util.Set;

/**
 * Map commands.
 * <p>
 * This class reserves serializable type IDs {@code 60} through {@code 74}
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class MapCommands {

  private MapCommands() {
  }

  /**
   * Abstract map command.
   */
  public static abstract class MapCommand<V> implements Command<V> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.QUORUM;
    }
  }

  /**
   * Abstract map query.
   */
  public static abstract class MapQuery<V> implements Query<V> {
    protected ConsistencyLevel consistency;

    protected MapQuery() {
    }

    protected MapQuery(ConsistencyLevel consistency) {
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
  public static abstract class KeyCommand<V> extends MapCommand<V> {
    protected Object key;

    public KeyCommand() {
    }

    public KeyCommand(Object key) {
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
  public static abstract class KeyQuery<V> extends MapQuery<V> {
    protected Object key;

    public KeyQuery() {
    }

    public KeyQuery(Object key) {
      this.key = Assert.notNull(key, "key");
    }

    public KeyQuery(Object key, ConsistencyLevel consistency) {
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
   * Contains key command.
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
   * Abstract key-based query.
   */
  public static class ContainsValue extends MapQuery<Boolean> {
    protected Object value;

    ContainsValue() {
    }

    @JsonCreator
    public ContainsValue(@JsonProperty("value") Object value) {
      this.value = Assert.notNull(value, "value");
    }

    @JsonCreator
    public ContainsValue(@JsonProperty("value") Object value, @JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
      this.value = Assert.notNull(value, "value");
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
   * Key/value command.
   */
  public static abstract class KeyValueCommand<V> extends KeyCommand<V> {
    protected Object value;

    KeyValueCommand() {
    }

    @JsonCreator
    public KeyValueCommand(@JsonProperty("key") Object key, @JsonProperty("value") Object value) {
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
  public static abstract class TtlCommand<V> extends KeyValueCommand<V> {
    protected long ttl;

    TtlCommand() {
    }

    protected TtlCommand(@JsonProperty("key") Object key, @JsonProperty("value") Object value, @JsonProperty("ttl") long ttl) {
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
  public static class Put extends TtlCommand<Object> {
    Put() {
    }

    @JsonCreator
    public Put(@JsonProperty("key") Object key, @JsonProperty("value") Object value) {
      this(key, value, 0);
    }

    @JsonCreator
    public Put(@JsonProperty("key") Object key, @JsonProperty("value") Object value, @JsonProperty("ttl") long ttl) {
      super(key, value, ttl);
    }
  }

  /**
   * Put if absent command.
   */
  public static class PutIfAbsent extends TtlCommand<Object> {
    PutIfAbsent() {
    }

    @JsonCreator
    public PutIfAbsent(@JsonProperty("key") Object key, @JsonProperty("value") Object value) {
      this(key, value, 0);
    }

    @JsonCreator
    public PutIfAbsent(@JsonProperty("key") Object key, @JsonProperty("value") Object value, @JsonProperty("ttl") long ttl) {
      super(key, value, ttl);
    }
  }

  /**
   * Get query.
   */
  public static class Get extends KeyQuery<Object> {
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
   * Get or default query.
   */
  public static class GetOrDefault extends KeyQuery<Object> {
    protected Object defaultValue;

    GetOrDefault() {
    }

    @JsonCreator
    public GetOrDefault(@JsonProperty("key") Object key, @JsonProperty("default") Object defaultValue) {
      super(key);
      this.defaultValue = defaultValue;
    }

    @JsonCreator
    public GetOrDefault(@JsonProperty("key") Object key, @JsonProperty("default") Object defaultValue, @JsonProperty("consistency") ConsistencyLevel consistency) {
      super(key, consistency);
      this.defaultValue = defaultValue;
    }

    /**
     * Returns the default value.
     *
     * @return The default value.
     */
    @JsonGetter("default")
    public Object defaultValue() {
      return defaultValue;
    }
  }

  /**
   * Remove command.
   */
  public static class Remove extends KeyCommand<Object> {
    Remove() {
    }

    @JsonCreator
    public Remove(@JsonProperty("key") Object key) {
      super(key);
    }

    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Remove if absent command.
   */
  public static class RemoveIfPresent extends KeyValueCommand<Boolean> {
    RemoveIfPresent() {
    }

    @JsonCreator
    public RemoveIfPresent(@JsonProperty("key") Object key, @JsonProperty("value") Object value) {
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
  public static class Replace extends TtlCommand<Object> {
    Replace() {
    }

    @JsonCreator
    public Replace(@JsonProperty("key") Object key, @JsonProperty("value") Object value) {
      this(key, value, 0);
    }

    @JsonCreator
    public Replace(@JsonProperty("key") Object key, @JsonProperty("value") Object value, @JsonProperty("ttl") long ttl) {
      super(key, value, ttl);
    }
  }

  /**
   * Remove if absent command.
   */
  public static class ReplaceIfPresent extends TtlCommand<Boolean> {
    protected Object replace;

    ReplaceIfPresent() {
    }

    @JsonCreator
    public ReplaceIfPresent(@JsonProperty("key") Object key, @JsonProperty("replace") Object replace, @JsonProperty("value") Object value) {
      this(key, replace, value, 0);
    }

    @JsonCreator
    public ReplaceIfPresent(@JsonProperty("key") Object key, @JsonProperty("replace") Object replace, @JsonProperty("value") Object value, @JsonProperty("ttl") long ttl) {
      super(key, value, ttl);
      this.replace = replace;
    }

    /**
     * Returns the replace value.
     *
     * @return The replace value.
     */
    @JsonGetter("replace")
    public Object replace() {
      return replace;
    }
  }

  /**
   * Is empty query.
   */
  public static class IsEmpty extends MapQuery<Boolean> {
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
  public static class Size extends MapQuery<Integer> {
    @JsonCreator
    public Size() {
    }

    @JsonCreator
    public Size(@JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
    }
  }

  /**
   * Values query.
   */
  public static class Values extends MapQuery<Collection> {
    @JsonCreator
    public Values() {
    }

    @JsonCreator
    public Values(@JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
    }
  }

  /**
   * Key set query.
   */
  public static class KeySet extends MapQuery<Set> {
    @JsonCreator
    public KeySet() {
    }

    @JsonCreator
    public KeySet(@JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
    }
  }

  /**
   * Entry set query.
   */
  public static class EntrySet extends MapQuery<Set> {
    @JsonCreator
    public EntrySet() {
    }

    @JsonCreator
    public EntrySet(@JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
    }
  }

  /**
   * Clear command.
   */
  public static class Clear extends MapCommand<Void> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Map command type resolver.
   */
  public static class TypeResolver implements SerializableTypeResolver {
    @Override
    public void resolve(SerializerRegistry registry) {
      registry.register(ContainsKey.class, -65, t -> new GenericKryoSerializer());
      registry.register(ContainsValue.class, -66, t -> new GenericKryoSerializer());
      registry.register(Put.class, -67, t -> new GenericKryoSerializer());
      registry.register(PutIfAbsent.class, -68, t -> new GenericKryoSerializer());
      registry.register(Get.class, -69, t -> new GenericKryoSerializer());
      registry.register(GetOrDefault.class, -70, t -> new GenericKryoSerializer());
      registry.register(Remove.class, -71, t -> new GenericKryoSerializer());
      registry.register(RemoveIfPresent.class, -72, t -> new GenericKryoSerializer());
      registry.register(Replace.class, -73, t -> new GenericKryoSerializer());
      registry.register(ReplaceIfPresent.class, -74, t -> new GenericKryoSerializer());
      registry.register(Values.class, -155, t -> new GenericKryoSerializer());
      registry.register(KeySet.class, -156, t -> new GenericKryoSerializer());
      registry.register(EntrySet.class, -157, t -> new GenericKryoSerializer());
      registry.register(IsEmpty.class, -75, t -> new GenericKryoSerializer());
      registry.register(Size.class, -76, t -> new GenericKryoSerializer());
      registry.register(Clear.class, -77, t -> new GenericKryoSerializer());
    }
  }

}
