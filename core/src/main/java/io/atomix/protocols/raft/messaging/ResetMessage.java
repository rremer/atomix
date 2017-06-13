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
package io.atomix.protocols.raft.messaging;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Event reset request.
 * <p>
 * Reset requests are sent by clients to servers if the client receives an event message out of
 * sequence to force the server to resend events from the correct index.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ResetMessage extends SessionMessage {

  /**
   * Returns a new publish response builder.
   *
   * @return A new publish response builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  private final long index;

  public ResetMessage(long session, long index) {
    super(session);
    this.index = index;
  }

  /**
   * Returns the event index.
   *
   * @return The event index.
   */
  public long index() {
    return index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), session, index);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof ResetMessage) {
      ResetMessage request = (ResetMessage) object;
      return request.session == session
        && request.index == index;
    }
    return false;
  }

  @Override
  public String toString() {
    return toStringHelper(this)
            .add("session", session)
            .add("index", index)
            .toString();
  }

  /**
   * Reset request builder.
   */
  public static class Builder extends SessionMessage.Builder<Builder, ResetMessage> {
    private long index;

    /**
     * Sets the event index.
     *
     * @param index The event index.
     * @return The response builder.
     * @throws IllegalArgumentException if {@code index} is less than {@code 1}
     */
    public Builder withIndex(long index) {
      checkArgument(index >= 0, "index must be positive");
      this.index = index;
      return this;
    }

    /**
     * @throws IllegalStateException if sequence is less than 1
     */
    @Override
    public ResetMessage build() {
      validate();
      return new ResetMessage(session, index);
    }
  }
}
