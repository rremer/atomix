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
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.session.Session;
import io.atomix.server.commands.CreateSessionCommand;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * HTTP resource session.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixResourceSession implements ServerSession {
  private final Commit<CreateSessionCommand> commit;
  private final Map<String, Queue<Object>> events = new HashMap<>();

  AtomixResourceSession(Commit<CreateSessionCommand> commit) {
    this.commit = commit;
  }

  @Override
  public long id() {
    return commit.index();
  }

  @Override
  public State state() {
    return null;
  }

  @Override
  public Listener<State> onStateChange(Consumer<State> callback) {
    return null;
  }

  Map<String, Queue<Object>> events() {
    return events;
  }

  Queue<Object> events(String event) {
    return events.computeIfAbsent(event, e -> new ArrayDeque<>());
  }

  @Override
  public Session publish(String event) {
    Queue<Object> events = this.events.computeIfAbsent(event, e -> new ArrayDeque<>());
    events.add(null);
    return this;
  }

  @Override
  public Session publish(String event, Object message) {
    Queue<Object> events = this.events.computeIfAbsent(event, e -> new ArrayDeque<>());
    events.add(message);
    return this;
  }

}
