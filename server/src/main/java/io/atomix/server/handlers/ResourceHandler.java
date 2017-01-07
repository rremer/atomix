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
package io.atomix.server.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.atomix.copycat.protocol.CommandRequest;
import io.atomix.copycat.protocol.QueryRequest;
import io.atomix.resource.ReadConsistency;
import io.atomix.server.AtomixServerContext;
import io.atomix.server.commands.ResourceCommand;
import io.atomix.server.commands.ResourceQuery;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;

/**
 * Resource request handler.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class ResourceHandler extends RequestHandler {
  public ResourceHandler(AtomixServerContext server) {
    super(server);
  }

  @Override
  public String route() {
    return "/session/:sessionId/resources/:resourceId";
  }

  @Override
  public HttpMethod method() {
    return null;
  }

  private JsonNode readJson(String value) {
    try {
      return Json.mapper.readTree(value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String writeJson(Object value) {
    try {
      return Json.mapper.writeValueAsString(value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private JsonNode convert(Object value) {
    return Json.mapper.convertValue(value, JsonNode.class);
  }

  @Override
  public void handle(RoutingContext context) {
    Long session = Long.valueOf(context.pathParam("sessionId"));
    String resource = context.pathParam("resourceId");

    try {
      JsonNode tree = Json.mapper.readTree(context.getBodyAsString());
      Long sequence = tree.get("sequence").asLong();
      if (context.request().method() == HttpMethod.GET) {
        Long index = tree.get("index").asLong();
        ReadConsistency consistency = ReadConsistency.valueOf(tree.get("consistency").asText(ReadConsistency.ATOMIC.name()).toUpperCase());
        server.getProxy().query(QueryRequest.builder()
          .withIndex(index)
          .withSession(session)
          .withSequence(sequence)
          .withQuery(new ResourceQuery<>(session, resource, new JsonQuery(tree.get("body"))))
          .build()).whenComplete((response, error) -> {
          context.vertx().runOnContext(v -> {
            if (error == null) {
              ObjectNode json = Json.mapper.createObjectNode();
              json.put("index", response.index());
              json.put("eventIndex", response.eventIndex());
              json.set("result", convert(response.result()));
              context.response().setStatusCode(200);
              context.response().putHeader("content-type", "application/json");
              context.response().end(json.toString());
            } else {
              context.response().setStatusCode(500).end();
            }
          });
        });
      } else {
        server.getProxy().command(CommandRequest.builder()
          .withSequence(sequence)
          .withSession(session)
          .withCommand(new ResourceCommand<>(session, resource, new JsonCommand(tree.get("body"))))
          .build()).whenComplete((response, error) -> {
          context.vertx().runOnContext(v -> {
            if (error == null) {
              ObjectNode json = Json.mapper.createObjectNode();
              json.put("index", response.index());
              json.put("eventIndex", response.eventIndex());
              json.set("result", convert(response.result()));
              context.response().setStatusCode(200);
              context.response().putHeader("content-type", "application/json");
              context.response().end(json.toString());
            } else {
              context.response().setStatusCode(500).end();
            }
          });
        });
      }
    } catch (IOException e) {
      context.response().setStatusCode(500).end();
    }
  }
}
