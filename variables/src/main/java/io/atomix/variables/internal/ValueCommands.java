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
package io.atomix.variables.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.catalyst.serializer.kryo.GenericKryoSerializer;
import io.atomix.copycat.Command;
import io.atomix.copycat.Query;
import io.atomix.variables.events.ValueChangeEvent;

/**
 * Distributed value commands.
 * <p>
 * This class reserves serializable type IDs {@code 50} through {@code 59}
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ValueCommands {

  private ValueCommands() {
  }

  /**
   * Abstract value command.
   */
  public static abstract class ValueCommand<V> implements Command<V> {
    protected long ttl;

    protected ValueCommand() {
    }

    protected ValueCommand(long ttl) {
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
   * Abstract value query.
   */
  public static abstract class ValueQuery<V> implements Query<V> {
    protected ConsistencyLevel consistency;

    protected ValueQuery() {
    }

    protected ValueQuery(ConsistencyLevel consistency) {
      this.consistency = consistency;
    }

    @Override
    @JsonGetter("consistency")
    public ConsistencyLevel consistency() {
      return consistency;
    }
  }

  /**
   * Get query.
   */
  public static class Get<T> extends ValueQuery<T> {
    @JsonCreator
    public Get() {
    }

    @JsonCreator
    public Get(@JsonProperty("consistency") ConsistencyLevel consistency) {
      super(consistency);
    }
  }

  /**
   * Set command.
   */
  public static class Set<T> extends ValueCommand<T> {
    protected T value;

    Set() {
    }

    @JsonCreator
    public Set(@JsonProperty("value") T value) {
      this.value = value;
    }

    @JsonCreator
    public Set(@JsonProperty("value") T value, @JsonProperty("ttl") long ttl) {
      super(ttl);
      this.value = value;
    }

    /**
     * Returns the command value.
     *
     * @return The command value.
     */
    @JsonGetter("value")
    public T value() {
      return value;
    }

    @Override
    public String toString() {
      return String.format("%s[value=%s]", getClass().getSimpleName(), value);
    }
  }

  /**
   * Compare and set command.
   */
  public static class CompareAndSet<T> extends ValueCommand<Boolean> {
    protected T expect;
    protected T update;

    CompareAndSet() {
    }

    @JsonCreator
    public CompareAndSet(
      @JsonProperty("expect") T expect,
      @JsonProperty("update") T update) {
      this.expect = expect;
      this.update = update;
    }

    @JsonCreator
    public CompareAndSet(
      @JsonProperty("expect") T expect,
      @JsonProperty("update") T update,
      @JsonProperty("ttl") long ttl) {
      super(ttl);
      this.expect = expect;
      this.update = update;
    }

    /**
     * Returns the expected value.
     *
     * @return The expected value.
     */
    @JsonGetter("expect")
    public T expect() {
      return expect;
    }

    /**
     * Returns the updated value.
     *
     * @return The updated value.
     */
    @JsonGetter("update")
    public T update() {
      return update;
    }

    @Override
    public String toString() {
      return String.format("%s[expect=%s, update=%s]", getClass().getSimpleName(), expect, update);
    }
  }

  /**
   * Get and set command.
   */
  public static class GetAndSet<T> extends ValueCommand<T> {
    protected T value;

    GetAndSet() {
    }

    @JsonCreator
    public GetAndSet(@JsonProperty("value") T value) {
      this.value = value;
    }

    @JsonCreator
    public GetAndSet(@JsonProperty("value") T value, @JsonProperty("ttl") long ttl) {
      super(ttl);
      this.value = value;
    }

    /**
     * Returns the command value.
     *
     * @return The command value.
     */
    @JsonGetter("value")
    public T value() {
      return value;
    }

    @Override
    public String toString() {
      return String.format("%s[value=%s]", getClass().getSimpleName(), value);
    }
  }

  /**
   * Register command.
   */
  public static class Register extends ValueCommand<Void> {
  }

  /**
   * Unregister command.
   */
  public static class Unregister extends ValueCommand<Void> {
  }

  /**
   * Value command type resolver.
   */
  public static class TypeResolver implements SerializableTypeResolver {
    @Override
    public void resolve(SerializerRegistry registry) {
      registry.register(ValueCommands.CompareAndSet.class, -110, t -> new GenericKryoSerializer());
      registry.register(ValueCommands.Get.class, -111, t -> new GenericKryoSerializer());
      registry.register(ValueCommands.GetAndSet.class, -112, t -> new GenericKryoSerializer());
      registry.register(ValueCommands.Set.class, -113, t -> new GenericKryoSerializer());
      registry.register(ValueChangeEvent.class, -120, t -> new GenericKryoSerializer());
      registry.register(Register.class, -121, t -> new GenericKryoSerializer());
      registry.register(Unregister.class, -122, t -> new GenericKryoSerializer());
    }
  }

}
