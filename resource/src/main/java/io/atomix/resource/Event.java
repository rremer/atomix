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
package io.atomix.resource;

/**
 * Base interface for resource events.
 * <p>
 * Resources should implement this interface to provide general information for non-lifecycle
 * events. Event implementations do not have to have a unique {@link EventType}, but each event
 * listener on a resource interface should only handle events of a specific type.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public interface Event<T extends EventType> {

  /**
   * Returns the event type.
   *
   * @return The event type.
   */
  T type();

}
