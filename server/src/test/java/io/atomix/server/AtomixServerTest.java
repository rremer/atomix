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
package io.atomix.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import io.atomix.server.protocol.CreateSessionResponse;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import net.jodah.concurrentunit.ConcurrentTestCase;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

/**
 * Atomix server test.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
@Test
public class AtomixServerTest extends ConcurrentTestCase {
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Tests the Atomix server.
   */
  public void testServer() throws Throwable {
    new Thread(() -> {
      try {
        AtomixServerRunner.main(new String[]{"localhost:5000:6000:7000", "-bootstrap", "localhost:5000", "localhost:5001", "localhost:5002"});
      } catch (Exception e) {
      }
    }).start();

    new Thread(() -> {
      try {
        AtomixServerRunner.main(new String[]{"localhost:5001:6001:7001", "-bootstrap", "localhost:5000", "localhost:5001", "localhost:5002"});
      } catch (Exception e) {
      }
    }).start();

    new Thread(() -> {
      try {
        AtomixServerRunner.main(new String[]{"localhost:5002:6002:7002", "-bootstrap", "localhost:5000", "localhost:5001", "localhost:5002"});
      } catch (Exception e) {
      }
    }).start();

    Thread.sleep(5000);

    AtomixClient atomix = AtomixClient.builder().build();
    atomix.connect(new Address("localhost", 5000), new Address("localhost", 5001), new Address("localhost", 5002)).thenRun(this::resume);
    await(5000);

    Vertx vertx = Vertx.vertx();
    HttpClient client = vertx.createHttpClient();
    client.post(7000, "localhost", "/sessions", response -> {
      threadAssertEquals(200, response.statusCode());
      response.bodyHandler(buffer -> {
        long sessionId = except(() -> mapper.readValue(buffer.toString(), CreateSessionResponse.class).getSession());
        threadAssertTrue(sessionId > 0);
        resume();
      });
    }).putHeader("content-type", "application/json")
      .putHeader("content-length", "2")
      .end("{}");
    await(5000);
  }

  private <T> T except(Callable<T> callback) {
    try {
      return callback.call();
    } catch (Exception e) {
      threadFail();
      return null;
    }
  }

}
