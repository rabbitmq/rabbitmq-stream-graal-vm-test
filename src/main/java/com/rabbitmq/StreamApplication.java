/*
 * Copyright (c) 2023 Broadcom. All Rights Reserved. The term Broadcom refers to Broadcom Inc. and/or its subsidiaries.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rabbitmq;

import static java.util.Arrays.asList;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Producer;
import com.rabbitmq.stream.ProducerBuilder;
import com.rabbitmq.stream.compression.Compression;
import io.netty.channel.nio.NioEventLoopGroup;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class StreamApplication {

  public static void main(String[] args) throws Exception {
    String stream = "stream";
    List<Compression> compressionCodecs = new ArrayList<>(Compression.values().length + 1);
    compressionCodecs.add(null);
    compressionCodecs.addAll(asList(Compression.values()));
    int messageBatch = 10_000;
    int messageCount = messageBatch * compressionCodecs.size();
    CountDownLatch consumeLatch = new CountDownLatch(messageCount);
    log("Connecting...");
    NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    try (Environment environment = Environment.builder().netty().eventLoopGroup(eventLoopGroup)
        .environmentBuilder().build()) {
      log("Connected. Creating stream...");
      environment.streamCreator().stream(stream).create();
      log("Stream created. Starting consumer...");
      environment.consumerBuilder().stream(stream).messageHandler((context, message) -> {
        consumeLatch.countDown();
      }).build();
      log("Consumer started.");

      for (Compression compressionCodec : compressionCodecs) {
        ProducerBuilder producerBuilder = environment.producerBuilder()
            .stream(stream)
            .subEntrySize(compressionCodec == null ? 1 : 10);
        if (compressionCodec == null) {
          log("Not using sub-entry batching");
        } else {
          log("Using sub-entry batching with compression codec " + compressionCodec);
          producerBuilder.compression(compressionCodec);
        }

        Producer producer = producerBuilder.build();
        log("Producer created. Publishing " + messageBatch + " messages...");
        byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
        IntStream.range(0, messageBatch).forEach(i -> {
          producer.send(producer.messageBuilder().addData(body).build(), confirmationStatus -> {
          });
        });
      }

      boolean done = consumeLatch.await(10, TimeUnit.SECONDS);
      if (done) {
        log(messageCount + " messages published and consumed");
      } else {
        throw new IllegalStateException("Did not receive all messages after 10 seconds");
      }
    } finally {
      eventLoopGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS).get();
    }
  }

  private static void log(String message) {
    System.out.println(message);
  }

}
