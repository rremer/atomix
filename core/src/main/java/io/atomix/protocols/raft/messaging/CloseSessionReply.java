/*
 * Copyright 2017-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.protocols.raft.messaging;

import io.atomix.protocols.raft.error.RaftError;

import java.util.Objects;

/**
 * Close session response.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class CloseSessionReply extends SessionReply {

  /**
   * Returns a new keep alive response builder.
   *
   * @return A new keep alive response builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  public CloseSessionReply(Status status, RaftError error) {
    super(status, error);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof CloseSessionReply) {
      CloseSessionReply response = (CloseSessionReply) object;
      return response.status == status && Objects.equals(response.error, error);
    }
    return false;
  }

  /**
   * Status response builder.
   */
  public static class Builder extends SessionReply.Builder<Builder, CloseSessionReply> {
    @Override
    public CloseSessionReply build() {
      validate();
      return new CloseSessionReply(status, error);
    }
  }
}
