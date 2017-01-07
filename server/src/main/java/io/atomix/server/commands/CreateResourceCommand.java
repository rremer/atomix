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
package io.atomix.server.commands;

/**
 * Create resource command.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class CreateResourceCommand extends AbstractResourceCommand<Long> {
  protected String type;

  CreateResourceCommand() {
  }

  public CreateResourceCommand(long session, String resource, String type) {
    super(session, resource);
    this.type = type;
  }

  public String type() {
    return type;
  }
}
