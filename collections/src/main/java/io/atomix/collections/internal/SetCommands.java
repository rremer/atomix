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
import io.atomix.copycat.Command;
import io.atomix.copycat.Query;

import java.util.Set;

/**
 * Distributed set commands.
 * <p>
 * This class reserves serializable type IDs {@code 100} through {@code 109}
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class SetCommands {

  private SetCommands() {
  }

  /**
   * Abstract set command.
   */
  private static abstract class SetCommand<V> implements Command<V> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.QUORUM;
    }
  }

  /**
   * Abstract set query.
   */
  private static abstract class SetQuery<V> implements Query<V> {
    protected ConsistencyLevel consistency;

    protected SetQuery() {
    }

    protected SetQuery(ConsistencyLevel consistency) {
      this.consistency = consistency;
    }

    @Override
    @JsonGetter("consistency")
    public ConsistencyLevel consistency() {
      return null;
    }
  }

  /**
   * Abstract value command.
   */
  private static abstract class ValueCommand<V> extends SetCommand<V> {
    protected Object value;

    protected ValueCommand() {
    }

    protected ValueCommand(Object value) {
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
   * Abstract value query.
   */
  private static abstract class ValueQuery<V> extends SetQuery<V> {
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
   * Contains value command.
   */
  public static class Contains extends ValueQuery<Boolean> {
    Contains() {
    }

    @JsonCreator
    public Contains(@JsonProperty("value") Object value) {
      super(value);
    }

    @JsonCreator
    public Contains(@JsonProperty("value") Object value, @JsonProperty("consistency") ConsistencyLevel consistency) {
      super(value, consistency);
    }
  }

  /**
   * TTL command.
   */
  public static abstract class TtlCommand<V> extends ValueCommand<V> {
    protected long ttl;

    protected TtlCommand() {
    }

    protected TtlCommand(Object value, long ttl) {
      super(value);
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
   * Add command.
   */
  public static class Add extends TtlCommand<Boolean> {
    Add() {
    }

    @JsonCreator
    public Add(@JsonProperty("value") Object value) {
      super(value, 0);
    }

    @JsonCreator
    public Add(@JsonProperty("value") Object value, @JsonProperty("ttl") long ttl) {
      super(value, ttl);
    }
  }

  /**
   * Remove command.
   */
  public static class Remove extends ValueCommand<Boolean> {
    @JsonCreator
    public Remove() {
    }

    @JsonCreator
    public Remove(@JsonProperty("value") Object value) {
      super(value);
    }

    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Size query.
   */
  public static class Size extends SetQuery<Integer> {
    @JsonCreator
    public Size() {
    }

    @JsonCreator
    public Size(@JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
    }
  }

  /**
   * Is empty query.
   */
  public static class IsEmpty extends SetQuery<Boolean> {
    @JsonCreator
    public IsEmpty() {
    }

    @JsonCreator
    public IsEmpty(@JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
    }
  }
  
  /**
   * Iterator query.
   */
  public static class Iterator<V> extends SetQuery<Set<V>> {
  }

  /**
   * Clear command.
   */
  public static class Clear extends SetCommand<Void> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Set command type resolver.
   */
  public static class TypeResolver implements SerializableTypeResolver {
    @Override
    public void resolve(SerializerRegistry registry) {
      registry.register(Contains.class, -100, t -> new GenericKryoSerializer());
      registry.register(Add.class, -101, t -> new GenericKryoSerializer());
      registry.register(Remove.class, -102, t -> new GenericKryoSerializer());
      registry.register(IsEmpty.class, -103, t -> new GenericKryoSerializer());
      registry.register(Size.class, -104, t -> new GenericKryoSerializer());
      registry.register(Clear.class, -105, t -> new GenericKryoSerializer());
      registry.register(Iterator.class, -106, t -> new GenericKryoSerializer());
    }
  }

}
