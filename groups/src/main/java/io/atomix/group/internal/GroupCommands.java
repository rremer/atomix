/*
 * Copyright 2015 the original author or authors.
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
 * limitations under the License.
 */
package io.atomix.group.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.SerializerRegistry;
import io.atomix.catalyst.serializer.kryo.GenericKryoSerializer;
import io.atomix.copycat.Command;
import io.atomix.copycat.Operation;
import io.atomix.copycat.Query;
import io.atomix.group.messaging.MessageProducer;
import io.atomix.group.messaging.internal.GroupMessage;

import java.util.Set;

/**
 * Group commands.
 * <p>
 * This class reserves serializable type IDs {@code 130} through {@code 140} and {@code 158} through {@code 160}
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class GroupCommands {

  private GroupCommands() {
  }

  /**
   * Group operation.
   */
  public static abstract class GroupOperation<V> implements Operation<V> {
  }

  /**
   * Member operation.
   */
  public static abstract class MemberOperation<T> extends GroupOperation<T> {
    private String member;

    protected MemberOperation() {
    }

    protected MemberOperation(String member) {
      this.member = member;
    }

    /**
     * Returns the member ID.
     *
     * @return The member ID.
     */
    @JsonGetter("member")
    public String member() {
      return member;
    }
  }

  /**
   * Member command.
   */
  public static abstract class MemberCommand<V> extends MemberOperation<V> implements Command<V> {
    protected MemberCommand() {
    }

    protected MemberCommand(String member) {
      super(member);
    }

    @Override
    public CompactionMode compaction() {
      return CompactionMode.QUORUM;
    }
  }

  /**
   * Group member query.
   */
  public static abstract class MemberQuery<V> extends MemberOperation<V> implements Query<V> {
    protected MemberQuery(String member) {
      super(member);
    }

    protected MemberQuery() {
    }
  }

  /**
   * Join command.
   */
  public static class Join extends MemberCommand<GroupMemberInfo> {
    private boolean persist;
    private Object metadata;

    Join() {
    }

    @JsonCreator
    public Join(@JsonProperty("member") String member, @JsonProperty("persist") boolean persist, @JsonProperty("metadata") Object metadata) {
      super(member);
      this.persist = persist;
      this.metadata = metadata;
    }

    /**
     * Returns whether the member is persistent.
     *
     * @return Whether the member is persistent.
     */
    @JsonGetter("persist")
    public boolean persist() {
      return persist;
    }

    /**
     * Returns the member metadata.
     *
     * @return The member metadata.
     */
    @JsonGetter("metadata")
    public Object metadata() {
      return metadata;
    }
  }

  /**
   * Leave command.
   */
  public static class Leave extends MemberCommand<Void> {
    Leave() {
    }

    @JsonCreator
    public Leave(@JsonProperty("member") String member) {
      super(member);
    }

    @Override
    public CompactionMode compaction() {
      return CompactionMode.SEQUENTIAL;
    }
  }

  /**
   * List command.
   */
  public static class Listen extends MemberCommand<GroupStatus> {
    @JsonCreator
    public Listen() {
    }

    @JsonCreator
    public Listen(@JsonProperty("member") String member) {
      super(member);
    }

    @Override
    public CompactionMode compaction() {
      return CompactionMode.QUORUM;
    }
  }

  /**
   * Group status.
   */
  public static class GroupStatus {
    private long term;
    private String leader;
    private Set<GroupMemberInfo> members;

    GroupStatus() {
    }

    @JsonCreator
    public GroupStatus(@JsonProperty("term") long term, @JsonProperty("leader") String leader, @JsonProperty("members") Set<GroupMemberInfo> members) {
      this.term = term;
      this.leader = leader;
      this.members = members;
    }

    @JsonGetter("term")
    public long term() {
      return term;
    }

    @JsonGetter("leader")
    public String leader() {
      return leader;
    }

    @JsonGetter("members")
    public Set<GroupMemberInfo> members() {
      return members;
    }
  }

  /**
   * Message command.
   */
  public static class Message extends MemberCommand<Void> {
    private int producer;
    private long id;
    private String queue;
    private Object message;
    private MessageProducer.Delivery delivery;
    private MessageProducer.Execution execution;

    Message() {
    }

    @JsonCreator
    public Message(
      @JsonProperty("member") String member,
      @JsonProperty("producer") int producer,
      @JsonProperty("queue") String queue,
      @JsonProperty("id") long id,
      @JsonProperty("message") Object message,
      @JsonProperty("delivery") MessageProducer.Delivery delivery,
      @JsonProperty("execution") MessageProducer.Execution execution) {
      super(member);
      this.producer = producer;
      this.queue = queue;
      this.id = id;
      this.message = message;
      this.delivery = delivery;
      this.execution = execution;
    }

    /**
     * Returns the producer ID.
     *
     * @return The producer ID.
     */
    @JsonGetter("producer")
    public int producer() {
      return producer;
    }

    /**
     * Returns the message queue name.
     *
     * @return The message queue name.
     */
    @JsonGetter("queue")
    public String queue() {
      return queue;
    }

    /**
     * Returns the message ID.
     *
     * @return The message ID.
     */
    @JsonGetter("id")
    public long id() {
      return id;
    }

    /**
     * Returns the message.
     *
     * @return The message.
     */
    @JsonGetter("message")
    public Object message() {
      return message;
    }

    /**
     * Returns the message delivery policy.
     *
     * @return The message delivery policy.
     */
    @JsonGetter("delivery")
    public MessageProducer.Delivery delivery() {
      return delivery;
    }

    /**
     * Returns the message execution policy.
     *
     * @return The message execution policy.
     */
    @JsonGetter("execution")
    public MessageProducer.Execution execution() {
      return execution;
    }
  }

  /**
   * Reply command.
   */
  public static class Reply extends MemberCommand<Void> {
    private String queue;
    private long id;
    private boolean succeeded;
    private Object message;

    Reply() {
    }

    @JsonCreator
    public Reply(
      @JsonProperty("member") String member,
      @JsonProperty("queue") String queue,
      @JsonProperty("id") long id,
      @JsonProperty("succeeded") boolean succeeded,
      @JsonProperty("message") Object message) {
      super(member);
      this.queue = queue;
      this.id = id;
      this.succeeded = succeeded;
      this.message = message;
    }

    /**
     * Returns the queue name.
     *
     * @return The queue name.
     */
    @JsonGetter("queue")
    public String queue() {
      return queue;
    }

    /**
     * Returns the message ID.
     *
     * @return The message ID.
     */
    @JsonGetter("id")
    public long id() {
      return id;
    }

    /**
     * Returns whether the reply succeeded.
     *
     * @return Whether the reply succeeded.
     */
    @JsonGetter("succeeded")
    public boolean succeeded() {
      return succeeded;
    }

    /**
     * Returns the reply message.
     *
     * @return The reply message.
     */
    @JsonGetter("message")
    public Object message() {
      return message;
    }
  }

  /**
   * Ack command.
   */
  public static class Ack extends MemberCommand<Void> {
    private int producer;
    private String queue;
    private long id;
    private boolean succeeded;
    private Object message;

    Ack() {
    }

    @JsonCreator
    public Ack(
      @JsonProperty("member") String member,
      @JsonProperty("producer") int producer,
      @JsonProperty("queue") String queue,
      @JsonProperty("id") long id,
      @JsonProperty("succeeded") boolean succeeded,
      @JsonProperty("message") Object message) {
      super(member);
      this.producer = producer;
      this.queue = queue;
      this.id = id;
      this.succeeded = succeeded;
      this.message = message;
    }

    /**
     * Returns the producer ID.
     *
     * @return The producer ID.
     */
    @JsonGetter("producer")
    public int producer() {
      return producer;
    }

    /**
     * Returns the queue name.
     *
     * @return The queue name.
     */
    @JsonGetter("queue")
    public String queue() {
      return queue;
    }

    /**
     * Returns the message ID.
     *
     * @return The message ID.
     */
    @JsonGetter("id")
    public long id() {
      return id;
    }

    /**
     * Returns whether the message succeeded.
     *
     * @return Whether the message succeeded.
     */
    @JsonGetter("succeeded")
    public boolean succeeded() {
      return succeeded;
    }

    /**
     * Returns the reply message.
     *
     * @return The reply message.
     */
    @JsonGetter("message")
    public Object message() {
      return message;
    }
  }

  /**
   * Group command type resolver.
   */
  public static class TypeResolver implements SerializableTypeResolver {
    @Override
    public void resolve(SerializerRegistry registry) {
      registry.register(Join.class, -130, t -> new GenericKryoSerializer());
      registry.register(Leave.class, -131, t -> new GenericKryoSerializer());
      registry.register(Listen.class, -132, t -> new GenericKryoSerializer());
      registry.register(Message.class, -137, t -> new GenericKryoSerializer());
      registry.register(Reply.class, -138, t -> new GenericKryoSerializer());
      registry.register(Ack.class, -139, t -> new GenericKryoSerializer());
      registry.register(GroupMessage.class, -140, t -> new GenericKryoSerializer());
      registry.register(GroupMemberInfo.class, -158, t -> new GenericKryoSerializer());
      registry.register(GroupStatus.class, -159, t -> new GenericKryoSerializer());
    }
  }

}
