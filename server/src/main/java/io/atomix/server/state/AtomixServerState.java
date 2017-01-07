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

import io.atomix.catalyst.concurrent.Listener;
import io.atomix.catalyst.concurrent.Scheduled;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachineExecutor;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.session.Session;
import io.atomix.manager.ResourceManagerException;
import io.atomix.manager.internal.ManagedResourceSession;
import io.atomix.manager.internal.ResourceManagerState;
import io.atomix.manager.internal.ResourceManagerStateMachineExecutor;
import io.atomix.resource.Resource;
import io.atomix.resource.ResourceStateMachine;
import io.atomix.resource.ResourceType;
import io.atomix.server.commands.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Atomix server state machine.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixServerState extends ResourceManagerState {
  protected final Map<Long, HttpSession> sessions = new HashMap<>();
  private final AtomixServerCommitPool commits = new AtomixServerCommitPool();

  @Override
  public void configure(StateMachineExecutor executor) {
    super.configure(executor);
    executor.register(CreateSessionCommand.class, this::httpCreateSession);
    executor.register(KeepAliveCommand.class, this::httpKeepAlive);
    executor.register(CreateResourceCommand.class, this::httpCreateResource);
    executor.register(DeleteResourceCommand.class, this::httpDeleteResource);
    executor.register(ResourceOperation.class, (Function<Commit<ResourceOperation>, Object>) this::httpOperation);
  }

  /**
   * Applies a server command.
   */
  protected Object httpOperation(Commit<ResourceOperation> commit) {
    Long resourceId = keys.get(commit.command().resource());
    if (resourceId == null) {
      throw new ResourceManagerException("Unknown resource: " + commit.command().resource());
    }

    ResourceHolder resource = resources.get(resourceId);
    if (resource == null) {
      throw new ResourceManagerException("Unknown resource: " + commit.command().resource());
    }

    HttpSession session = sessions.get(commit.command().session());
    if (session == null) {
      throw new ResourceManagerException("Unknown session: " + commit.command().session());
    }

    return resource.executor.execute(commits.acquire(commit, session));
  }

  /**
   * Creates a new session.
   */
  protected long httpCreateSession(Commit<CreateSessionCommand> commit) {
    HttpSession session = new HttpSession(commit);
    sessions.put(commit.index(), session);
    session.timer = executor.schedule(Duration.ofMillis(commit.command().timeout()), () -> {
      sessions.remove(commit.index());
      commit.release();
    });

    for (ResourceHolder resource : resources.values()) {
      resource.stateMachine.register(session);
    }
    return commit.index();
  }

  /**
   * Keeps a session alive.
   */
  protected void httpKeepAlive(Commit<KeepAliveCommand> commit) {
    HttpSession session = sessions.get(commit.command().session());
    if (session != null) {
      session.timer.cancel();
      session.timer = executor.schedule(Duration.ofMillis(session.commit.command().timeout()), () -> {
        sessions.remove(commit.index());
        commit.release();
      });
    }
  }

  /**
   * Creates a resource.
   */
  protected long httpCreateResource(Commit<CreateResourceCommand> commit) {
    String key = commit.operation().resource();
    String type = commit.operation().type();

    // Lookup the resource ID for the resource key.
    Long resourceId = keys.get(key);

    // If the resource already exists, throw an exception.
    if (resourceId != null) {
      throw new ResourceManagerException("resource already exists: " + key);
    }

    // The first time a resource is created, the resource ID is the index of the commit that created it.
    resourceId = commit.index();
    keys.put(key, resourceId);

    try {
      // For the new resource, construct a state machine and store the resource info.
      ResourceStateMachine stateMachine = type.factory().newInstance().createStateMachine(new Resource.Config(commit.operation().config()));
      ResourceManagerStateMachineExecutor executor = new ResourceManagerStateMachineExecutor(resourceId, this.executor);

      // Store the resource to be referenced by its resource ID.
      ResourceHolder resource = new ResourceHolder(resourceId, key, type, commit, stateMachine, executor);
      resources.put(resourceId, resource);

      // Initialize the resource state machine.
      stateMachine.init(executor);

      // Create a resource session for the client resource instance.
      ManagedResourceSession resourceSession = new ManagedResourceSession(resourceId, commit, commit.session());
      resource.executor.context.sessions.register(resourceSession);

      // Returns the session ID for the resource client session.
      return resourceId;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new ResourceManagerException("failed to instantiate state machine", e);
    }
  }

  /**
   * Delete resource.
   */
  protected void httpDeleteResource(Commit<DeleteResourceCommand> commit) {

  }

  private static class HttpSession implements ServerSession {
    private final Commit<CreateSessionCommand> commit;
    private Scheduled timer;

    private HttpSession(Commit<CreateSessionCommand> commit) {
      this.commit = commit;
    }

    @Override
    public Session publish(String event) {
      return null;
    }

    @Override
    public Session publish(String event, Object message) {
      return null;
    }

    @Override
    public long id() {
      return 0;
    }

    @Override
    public State state() {
      return null;
    }

    @Override
    public Listener<State> onStateChange(Consumer<State> callback) {
      return null;
    }
  }

}
