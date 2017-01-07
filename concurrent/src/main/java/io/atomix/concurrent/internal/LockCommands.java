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
package io.atomix.concurrent.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.catalyst.serializer.kryo.GenericKryoSerializer;
import io.atomix.copycat.Command;

/**
 * Lock commands.
 * <p>
 * This class reserves serializable type IDs {@code 115} through {@code 118}
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class LockCommands {

  private LockCommands() {
  }

  /**
   * Abstract lock command.
   */
  public static abstract class LockCommand<V> implements Command<V> {
    @Override
    public CompactionMode compaction() {
      return CompactionMode.QUORUM;
    }
  }

  /**
   * Lock command.
   */
  public static class Lock extends LockCommand<Void> {
    protected int id;
    protected long timeout;

    Lock() {
    }

    @JsonCreator
    public Lock(@JsonProperty("id") int id, @JsonProperty("timeout") long timeout) {
      this.id = id;
      this.timeout = timeout;
    }

    /**
     * Returns the lock ID.
     *
     * @return The lock ID.
     */
    @JsonGetter("id")
    public int id() {
      return id;
    }

    /**
     * Returns the try lock timeout.
     *
     * @return The try lock timeout in milliseconds.
     */
    @JsonGetter("timeout")
    public long timeout() {
      return timeout;
    }

    @Override
    public CompactionMode compaction() {
      return timeout > 0 ? CompactionMode.SEQUENTIAL : CompactionMode.QUORUM;
    }
  }

  /**
   * Unlock command.
   */
  public static class Unlock extends LockCommand<Void> {
    protected int id;

    Unlock() {
    }

    @JsonCreator
    public Unlock(@JsonProperty("id") int id) {
      this.id = id;
    }

    /**
     * Returns the lock ID.
     *
     * @return The lock ID.
     */
    @JsonGetter("id")
    public int id() {
      return id;
    }

    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * Lock event.
   */
  public static class LockEvent {
    protected int id;
    protected long version;

    LockEvent() {
    }

    @JsonCreator
    public LockEvent(@JsonProperty("id") int id, @JsonProperty("version") long version) {
      this.id = id;
      this.version = version;
    }

    /**
     * Returns the lock ID.
     *
     * @return The lock ID.
     */
    @JsonGetter("id")
    public int id() {
      return id;
    }

    /**
     * Returns the lock version.
     *
     * @return The lock version.
     */
    @JsonGetter("version")
    public long version() {
      return version;
    }

    @Override
    public String toString() {
      return String.format("%s[id=%d, version=%d]", getClass().getSimpleName(), id, version);
    }
  }

  /**
   * Lock command type resolver.
   */
  public static class TypeResolver implements SerializableTypeResolver {
    @Override
    public void resolve(SerializerRegistry registry) {
      registry.register(Lock.class, -143, t -> new GenericKryoSerializer());
      registry.register(Unlock.class, -144, t -> new GenericKryoSerializer());
      registry.register(LockEvent.class, -145, t -> new GenericKryoSerializer());
    }
  }

}
