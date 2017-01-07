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
package io.atomix.server.state;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.session.ServerSession;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Resource commit pool.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
class AtomixServerCommitPool {
  private final Queue<AtomixServerCommit> pool = new ConcurrentLinkedQueue<>();

  /**
   * Acquires a commit from the pool.
   *
   * @param commit The commit to acquire.
   * @param session The resource session.
   * @return The acquired resource commit.
   */
  @SuppressWarnings("unchecked")
  public AtomixServerCommit acquire(Commit commit, ServerSession session) {
    AtomixServerCommit resourceCommit = pool.poll();
    if (resourceCommit == null) {
      resourceCommit = new AtomixServerCommit(this);
    }
    resourceCommit.reset(commit, session);
    return resourceCommit;
  }

  /**
   * Releases a commit to the pool.
   *
   * @param commit The commit to release.
   */
  public void release(AtomixServerCommit commit) {
    pool.add(commit);
  }

}
