module-info
===========

A utility for generating ``module-info.class`` files from any JDK version.

Usage: XML
----------
Define a ``module-info.xml`` file in your ``src/main/java`` directory like this one:

```xml
<?xml version="1.0" ?>

<module-info xmlns="urn:jboss:module-info:1.0" name="org.my.module.name">
</module-info>

```

You can add in other content according to the included schema.

Usage: Maven
------------
Add a snippet like this to your pom.xml:

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.jboss.module-info</groupId>
                <artifactId>module-info</artifactId>
                <version>1.0.Final</version>
                <executions>
                    <execution>
                        <id>module-info</id>
                        <phase>process-classes</phase>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```
