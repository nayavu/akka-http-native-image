# Akka HTTP with GraalVM native image

A small example of how to create native image for an Akka HTTP application 

Inspired by [this](https://www.baeldung.com/scala/graalvm) article, 
but used [sbt-nativeimage](https://github.com/scalameta/sbt-native-image) plugin to automate reflection configuration generation.

The problem with building native image for Java apps that highly depends on reflections (which Akka does a lot) is, 
that you have to explicitly configure all reflections in [reflect-config.json](https://www.graalvm.org/22.0/reference-manual/native-image/Reflection/),
and this is not trivial thing. 
There is a [Tracing Agent](https://www.graalvm.org/22.0/reference-manual/native-image/Agent/) 
which helps to do that by recording all used reflections in running app, but for that you have to run the application manually many times with a variety of input data to cover all functionality.

Thanks to sbt-native-image's [nativeImageTestRunAgent](https://github.com/scalameta/sbt-native-image/blob/40a86b2da24374283be955f5afe1192796ca07d5/plugin/src/main/scala/sbtnativeimage/NativeImagePlugin.scala), 
it is possible to run application tests with enabled tracing agent. So you cover app with tests and get reflection config as a result. 
The drawback of this approach is, that the tracing agent records all the reflection calls made by both app and test libs. It might be redundant, but not critical. 
Also, the tests have to be written for the whole app as a black box (i.e. no mocking, with actual actor system). Not the best approach, but at least we get a CI-friendly way to build native images.  

Tested with:
- Java 17
- GraalVM 22.3.2
- Scala 3.2.2
- Akka 2.6.20
- Akka HTTP 10.2.10
- circe 0.14.5
- sbt-nativeimage 0.3.4

## Build
### sbt

On Ubuntu, it requires `gcc`, `build-essentials` and `zlib1g-dev`

```shell
sbt "clean; assembly; nativeImageTestRunAgent; nativeImage" 

./target/native-image/akka-http-native-image
```

### Docker 
Based on [BuildKit](https://docs.docker.com/build/buildkit/)

```shell
docker build . -t akka-http-native-image

docker run -p 9000:9000 -it --rm akka-http-native-image
# stop via `docker stop CONTAINER_ID`
```

Test:
```shell
curl http://localhost:9000/hello -H "content-type: application/json" --data "{\"name\":\"Cthulhu\"}"
```
