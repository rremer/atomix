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

import io.atomix.catalyst.buffer.PooledHeapAllocator;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.protocol.ClientRequestTypeResolver;
import io.atomix.copycat.protocol.ClientResponseTypeResolver;
import io.atomix.copycat.server.cluster.Member;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import io.atomix.copycat.server.storage.util.StorageSerialization;
import io.atomix.copycat.server.util.ServerSerialization;
import io.atomix.copycat.util.ProtocolSerialization;
import io.atomix.manager.util.ResourceManagerTypeResolver;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Atomix server runner.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixServerRunner {

  /**
   * Runs the Atomix server.
   */
  public static void main(String[] args) throws Exception {
    ArgumentParser parser = ArgumentParsers.newArgumentParser("AtomixServer")
      .defaultHelp(true)
      .description("Atomix server");
    parser.addArgument("address")
      .required(true)
      .metavar("HOST:SERVER_PORT:CLIENT_PORT:HTTP_PORT")
      .help("The server address");
    parser.addArgument("-bootstrap")
      .nargs("*")
      .metavar("HOST:TCP_PORT")
      .help("Bootstraps a new cluster");
    parser.addArgument("-join")
      .nargs("+")
      .metavar("HOST:TCP_PORT")
      .help("Joins an existing cluster");
    parser.addArgument("-config")
      .metavar("FILE")
      .help("Atomix configuration file");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }

    String[] address = ns.getString("address").split(":");
    String host = address[0];
    int serverPort = Integer.parseInt(address[1]);
    int clientPort = Integer.parseInt(address[2]);
    int httpPort = Integer.parseInt(address[3]);
    String config = ns.getString("config");

    Serializer serializer = new Serializer(new PooledHeapAllocator());
    serializer.resolve(new ClientRequestTypeResolver());
    serializer.resolve(new ClientResponseTypeResolver());
    serializer.resolve(new ProtocolSerialization());
    serializer.resolve(new ServerSerialization());
    serializer.resolve(new StorageSerialization());
    serializer.resolve(new ResourceManagerTypeResolver());

    NettyTransport transport = new NettyTransport();

    // TODO: support configuration files
    AtomixServer server = new AtomixServer(
      "atomix", // Server name
      Member.Type.ACTIVE, // Server type
      new Address(host, serverPort), // Server transport address
      transport, // Server transport
      new Address(host, clientPort), // Client transport address
      transport, // Client transport
      new Address(host, httpPort), // HTTP transport address
      new Storage(StorageLevel.MEMORY),
      serializer,
      new SingleThreadContext("atomix-server-%d", serializer));

    List<String> bootstrap = ns.getList("bootstrap");
    if (bootstrap != null) {
      List<Address> cluster = bootstrap.stream().map(Address::new).collect(Collectors.toList());
      server.bootstrap(cluster).join();
    } else {
      List<String> join = ns.getList("join");
      if (join != null) {
        List<Address> cluster = join.stream().map(Address::new).collect(Collectors.toList());
        server.join(cluster).join();
      } else {
        System.err.println("Must configure either -bootstrap or -join");
      }
    }

    synchronized (server) {
      while (server.isRunning()) {
        server.wait();
      }
    }
  }
}
