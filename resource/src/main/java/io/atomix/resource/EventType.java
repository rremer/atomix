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
 * Describes a type of event supported by a resource.
 * <p>
 * Each event must have an {@link EventType} associated with it. The 8-bit event type ID will be used
 * during communication with the client to indicate the type of event.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public interface EventType {

  /**
   * Returns the event type ID.
   * <p>
   * This must be an 8-bit signed integer that is unique among all event types for a resource.
   * Different resource types may have events with the same ID.
   *
   * @return The event type ID.
   */
  int id();

}
