Mule JCR Transport
==================

A transport that reads from, writes to and observes JCR containers.

Read the [documentation](http://www.mulesoft.org/jcr-transport).

Build
-----

Run:

    mvn clean install


Integration tests:

    mvn -Pit clean verify


Maven Support
-------------

Add the following repository:

    <repository>
      <id>muleforge-repo</id>
      <name>MuleForge Repository</name>
      <url>https://repository.mulesoft.org/nexus/content/repositories/releases</url>
      <layout>default</layout>
    </repository>

To add the Mule JCR transport to a Maven project add the following dependency:

    <dependency>
      <groupId>org.mule.transports</groupId>
      <artifactId>mule-transport-jcr</artifactId>
      <version>x.y.z</version>
    </dependency>
