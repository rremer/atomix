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
 * limitations under the License
 */
package io.atomix.variables.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.catalyst.serializer.kryo.GenericKryoSerializer;
import io.atomix.copycat.Command;
import io.atomix.variables.events.ValueChangeEvent;

/**
 * Long commands.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public final class LongCommands {

  private LongCommands() {
  }

  /**
   * Abstract long command.
   */
  public static abstract class LongCommand<V> implements Command<V> {

    protected LongCommand() {
    }

    @Override
    public CompactionMode compaction() {
      return CompactionMode.SNAPSHOT;
    }
  }

  /**
   * Increment and get command.
   */
  public static class IncrementAndGet extends LongCommand<Long> {
  }

  /**
   * Decrement and get command.
   */
  public static class DecrementAndGet extends LongCommand<Long> {
  }

  /**
   * Get and increment command.
   */
  public static class GetAndIncrement extends LongCommand<Long> {
  }

  /**
   * Get and decrement command.
   */
  public static class GetAndDecrement extends LongCommand<Long> {
  }

  /**
   * Delta command.
   */
  public static abstract class DeltaCommand extends LongCommand<Long> {
    protected long delta;

    protected DeltaCommand() {
    }

    protected DeltaCommand(long delta) {
      this.delta = delta;
    }

    /**
     * Returns the delta.
     *
     * @return The delta.
     */
    @JsonGetter("delta")
    public long delta() {
      return delta;
    }
  }

  /**
   * Get and add command.
   */
  public static class GetAndAdd extends DeltaCommand {
    GetAndAdd() {
    }

    @JsonCreator
    public GetAndAdd(@JsonProperty("delta") long delta) {
      super(delta);
    }
  }

  /**
   * Add and get command.
   */
  public static class AddAndGet extends DeltaCommand {
    AddAndGet() {
    }

    @JsonCreator
    public AddAndGet(@JsonProperty("delta") long delta) {
      super(delta);
    }
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
      registry.register(IncrementAndGet.class, -114, t -> new GenericKryoSerializer());
      registry.register(DecrementAndGet.class, -115, t -> new GenericKryoSerializer());
      registry.register(GetAndIncrement.class, -116, t -> new GenericKryoSerializer());
      registry.register(GetAndDecrement.class, -117, t -> new GenericKryoSerializer());
      registry.register(AddAndGet.class, -118, t -> new GenericKryoSerializer());
      registry.register(GetAndAdd.class, -119, t -> new GenericKryoSerializer());
      registry.register(ValueChangeEvent.class, -120, t -> new GenericKryoSerializer());
      registry.register(ValueCommands.Register.class, -121, t -> new GenericKryoSerializer());
      registry.register(ValueCommands.Unregister.class, -122, t -> new GenericKryoSerializer());
    }
  }

}
