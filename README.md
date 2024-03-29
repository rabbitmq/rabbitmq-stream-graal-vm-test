# RabbitMQ GraalVM Test

RabbitMQ Java libraries test on [GraalVM](https://www.graalvm.org/).

## RabbitMQ Stream Java Client

The test consists in a simple publish-consume scenario that builds
on top [RabbitMQ Stream Java client](https://github.com/rabbitmq/rabbitmq-stream-java-client).

Pre-requisites:
 * Local RabbitMQ broker running with all the defaults and stream plugin enabled
 * [GraalVM](https://www.graalvm.org/) installed

Start the broker:

```shell
docker run -it --rm --name rabbitmq -p 5552:5552 -p 5672:5672 \
  -e RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS='-rabbitmq_stream advertised_host localhost' \
  rabbitmq
```

Enable the stream plugin:

```shell
docker exec rabbitmq rabbitmq-plugins enable rabbitmq_stream
```

Build the project:

```shell
./mvnw clean package
```

Run the application:

```shell
target/rabbitmq-stream-graal-vm-test
```

This should output in the console:

```shell
Connecting...
Connected. Creating stream...
Stream created. Starting consumer...
Consumer started.
Not using sub-entry batching
Producer created. Publishing 10000 messages...
Using sub-entry batching with compression codec NONE
Producer created. Publishing 10000 messages...
Using sub-entry batching with compression codec GZIP
Producer created. Publishing 10000 messages...
Using sub-entry batching with compression codec SNAPPY
Producer created. Publishing 10000 messages...
Using sub-entry batching with compression codec LZ4
Producer created. Publishing 10000 messages...
Using sub-entry batching with compression codec ZSTD
Producer created. Publishing 10000 messages...
60000 messages published and consumed
```

It is possible to use another version of the stream Java client:

    ./mvnw clean package -Dstream-client.version=0.10.0
    

## Copyright and License ##

(c) 2020-2023 Broadcom. All Rights Reserved.
The term Broadcom refers to Broadcom Inc. and/or its subsidiaries.

Licensed under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
