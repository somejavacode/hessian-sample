# hessian sample

example of hessian remote interface. client and server require minimal code.
classes in api package contain interface and DTOs.

Note: I was too lazy to implement FooServiceImpl for 100%.

## usage hints

### Maven

compile project:
```
mvn clean package
```

start server:
```
java -jar server/target/tomcat.jar
```

run client (in a second shell):
```
java -jar client/target/client.jar
```

Note: the executable jars were build with maven-shade-plugin. this method is "cheap", it might fail im more complex cases.

### IntelliJ

simply open project with parent pom.

TomcatMain: click "Run"
FooClient: click "Run"
