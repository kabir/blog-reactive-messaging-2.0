# MicroProfile Reactive Messaging 2.0 Blog Project

This repository contains the example used in TODO.

To run it, clone this repository into a folder. We will refer to the location as `$EXAMPLE_DIR` perform the following steps.

# Start WildFly 25.0.0.Final

You can either use the zip downloaded from https://www.wildfly.org/downloads/, or you can provision it yourself with Galleon.

## Using the downloaded zip:
Download the Jakarta EE 8 Full & Web Distribution of Wildfly 25.0.0.Final from https://www.wildfly.org/downloads/ and unzip it somewhere. 

WildFly comes with all the modules set up to enable MicroProfile Reactive Messaging but it is not enabled by default.

In a terminal, go to the folder where you unzipped WildFly and run:
```shell
./bin/jboss-cli.sh --file=$EXAMPLE_DIR/enable-reactive-messaging.cli 
```
Your WildFly instance is now set up for MicroProfile Reactive Messaging. You can now start it by running `./bin/standalone.sh`

## Provisioning the server yourself
Download Galleon from https://github.com/wildfly/galleon/releases (I used 4.2.8.Final), unzip it and add its `bin/` folder to your `PATH`.

Create an empty directory somewhere, and go to it in your terminal

Then run the following command to provision a server set up for MicroProfile Reactive Messaging:

```shell
galleon.sh provision $EXAMPLE_DIR/provision.xml --dir=.
```
The above command will provision a wildfly server in the current directory. This server has the MicroProfile Reactive Messaging functionality enabled, so there is no need for the `jboss-cli.sh` step we needed when using the zip.

Start the server by running `./bin/standalone.sh`

# Start Kafka
You can either run Kafka on your own, or you can run it in Docker.

To run it in Docker, open another terminal and run the following commands:

```shell
cd $EXAMPLE_DIR
docker-compose up
```

If you are running your own Kafka instance, either create a topic called `page-visits`, or make sure that `auto.create.topics.enable=true` is set in the configuration.

# Connect a consumer to Kafka (optional)
This step is not strictly necessary, but it allows you to see data pushed to Kafka as it happens, which makes it a bit more interesting.

Assuming you have downloaded and unzipped Kafka somewhere, open a terminal and navigate to the Kafka root directory and run:
```shell
./bin/kafka-console-consumer.sh --topic page-visits --from-beginning --bootstrap-server localhost:9092
```
Leave this running, and you will see output here later.

# Build the full example
Since the example contains a few jars which depend on each other, we need to build the full example first.
Open another terminal and go to `$EXAMPLE_DIR`, and run:
```shell
mvn install
```

# Deploy the MicroProfile Reactive Messaging application
The source code for the application lives in the [`app`](app) folder. See the comments in [`app/pom.xml`](app/pom.xml)
for considerations when packaging the application.

In a terminal in `$EXAMPLE_DIR`, run:

```shell
mvn package wildfly:deploy -pl app/
```

Once deployed, go to http://localhost:8080/app/ and click on the links a few times.

If you did the optional Kafka consumer step, you should see output in the `kafka-console-consumer.sh` terminal as you click on the links.  This output will look something like:
```
frank	127.0.0.1app
emma	127.0.0.13.html
frank	127.0.0.11.html
linda	127.0.0.13.html
frank	127.0.0.11.html
emma	127.0.0.12.html
frank	127.0.0.13.html
```
Note that the default output formatting provided by the Kafka consumer is a bit off, in reality there are three columns (user, IP, page name), so it would have been better if it looked like:
```
frank	127.0.0.1   app
emma	127.0.0.1   3.html
frank	127.0.0.1   1.html
linda	127.0.0.1   3.html
frank	127.0.0.1   1.html
emma	127.0.0.1   2.html
frank	127.0.0.1   3.html
```

When people visit `3.html` (but not the other pages) it is output in the WildFly server log:
```
===> emma visited 3.html
===> linda visited 3.html
===> frank visited 3.html
```

# Read data from Kafka standalone
The source code for reading data from Kafka lives in the [`streams`](streams) folder. This uses the Kafka Streams API under the hood.

To read data from Kafka, run (again from a terminal in `$EXAMPLE_DIR`):
```shell
mvn package exec:java -pl streams -Dexec.mainClass="org.wildfly.blog.kafka.streams.Main"
```
You should now see the last page hits per user in tho output:
```
Stream started
Last pages visited:
{frank=3.html, emma=2.html, linda=3.html}
```
This output shows the last page visited for each user.

# Read data from Kafka in WildFly
Finally, we can package the application from the last step and expose it via REST. Note that to 
do this we need to include the kafka-streams jar in our application since WildFly does not 
include this jar. 

The code containing the REST endpoint lives in the [`streams-app`](streams-app) folder.
See the comments in [`streams-app/pom.xml`](streams-app/pom.xml) for considerations of how we have 
packaged the application.

To deploy the application, run:
```
mvn clean package wildfly:deploy -pl streams-app/
```

Once deployed, go to http://localhost:8080/streams/last-visited in your browser, and you should see 
similar output as in the last step, i.e:
```
{"frank":"3.html","emma":"2.html","linda":"3.html"}
```