# Comment Parser
The Comment Parser supports various languages based on FSM. It's written by scala and java.

## Prerequisites

- Java 1.8
- Scala 2.11 or 2.12

## Getting comment parser

Comment Parser is published to Sonatype OSS and Maven Central:

- Group id / organization: *io.github.qwefgh90*
- Artifact id / name: *comment-parser*
- Latest version is *0.1.0*

Add it to your sbt build definition:
```
libraryDependencies += "io.github.qwefgh90" %% "comment-parser" % "0.1.0"
```

Add one of them to your maven pom file:
```
<dependency>
    <groupId>io.github.qwefgh90</groupId>
    <artifactId>comment-parser_2.11</artifactId>
    <version>0.1.0</version>
</dependency>
```

or

```
<dependency>
    <groupId>io.github.qwefgh90</groupId>
    <artifactId>comment-parser_2.11</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Using Comment Parser

CommentParser is a main class to extract comments from various files. Import ` io.github.qwefgh90.commentparser` package to your sources to use CommentParser.

It has four static methods to do that. Also, there is a inner `CommentParser.CommentResult` class containing a result.

You can pass `URI` object to first parameter. 
```java
import io.github.qwefgh90.commentparser.CommentParser;
import java.nio.file.Paths;
CommentParser.extractComments(Paths.get("C:\\java.java").toUri(), "java.java");
```

`InputStream` object can be used to first parameter.

```java
import io.github.qwefgh90.commentparser.CommentParser;
import java.nio.file.Paths;
import java.io.InputStream;
import java.io.File;
File f = Paths.get("C:\\java.java").toFile();
InputStream is = new FileInputStream(f);
CommentParser.extractComments(is, "java.java");
is.close();
```

You can pass `Charset` object to third parameter optionally. 
```java
import io.github.qwefgh90.commentparser.CommentParser;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
CommentParser.extractComments(Paths.get("C:\\java.java").toUri(), "java.java", StandardCharsets.UTF_8);
```

## Supported Languages

- JAVA
- PY
- C
- CPP
- C_HEADER
- CPP_HEADER
- SCALA
- RUBY
- GO
- JS
- HTML
- BAT
- SH
- XML
- TEXT
- MD
- ETC

## Compile & Test

- sbt compile
- sbt test
