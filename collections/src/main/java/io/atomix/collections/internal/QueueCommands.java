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

/**
 * Distributed queue commands.
 * <p>
 * This class reserves serializable type IDs {@code 90} through {@code 99}
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class QueueCommands {

  private QueueCommands() {
  }

  /**
   * Abstract queue command.
   */
  private static abstract class QueueCommand<V> implements Command<V> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.QUORUM;
    }
  }

  /**
   * Abstract queue query.
   */
  private static abstract class QueueQuery<V> implements Query<V> {
    protected ConsistencyLevel consistency;

    protected QueueQuery() {
    }

    protected QueueQuery(ConsistencyLevel consistency) {
      this.consistency = consistency;
    }

    @Override
    @JsonGetter("consistency")
    public ConsistencyLevel consistency() {
      return consistency;
    }
  }

  /**
   * Abstract value command.
   */
  public static abstract class ValueCommand<V> extends QueueCommand<V> {
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
  public static abstract class ValueQuery<V> extends QueueQuery<V> {
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
   * Add command.
   */
  public static class Add extends ValueCommand<Boolean> {
    Add() {
    }

    @JsonCreator
    public Add(@JsonProperty("value") Object value) {
      super(value);
    }
  }

  /**
   * Offer
   */
  public static class Offer extends ValueCommand<Boolean> {
    Offer() {
    }

    @JsonCreator
    public Offer(@JsonProperty("value") Object value) {
      super(value);
    }
  }

  /**
   * Peek query.
   */
  public static class Peek extends QueueQuery<Object> {
  }

  /**
   * Poll command.
   */
  public static class Poll extends QueueCommand<Object> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Element command.
   */
  public static class Element extends QueueCommand<Object> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Remove command.
   */
  public static class Remove extends ValueCommand<Object> {
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
  public static class Size extends QueueQuery<Integer> {
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
  public static class IsEmpty extends QueueQuery<Boolean> {
    @JsonCreator
    public IsEmpty() {
    }

    @JsonCreator
    public IsEmpty(@JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
    }
  }

  /**
   * Clear command.
   */
  public static class Clear extends QueueCommand<Void> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Queue command type resolver.
   */
  public static class TypeResolver implements SerializableTypeResolver {
    @Override
    public void resolve(SerializerRegistry registry) {
      registry.register(Contains.class, -90, t -> new GenericKryoSerializer());
      registry.register(Add.class, -91, t -> new GenericKryoSerializer());
      registry.register(Offer.class, -92, t -> new GenericKryoSerializer());
      registry.register(Peek.class, -93, t -> new GenericKryoSerializer());
      registry.register(Poll.class, -94, t -> new GenericKryoSerializer());
      registry.register(Element.class, -95, t -> new GenericKryoSerializer());
      registry.register(Remove.class, -96, t -> new GenericKryoSerializer());
      registry.register(IsEmpty.class, -97, t -> new GenericKryoSerializer());
      registry.register(Size.class, -98, t -> new GenericKryoSerializer());
      registry.register(Clear.class, -99, t -> new GenericKryoSerializer());
    }
  }

}
