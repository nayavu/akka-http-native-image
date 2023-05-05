ARG JDK_VERSION=17.0.5_8
ARG SBT_VERSION=1.8.2
ARG SCALA_VERSION=3.2.2

FROM sbtscala/scala-sbt:eclipse-temurin-jammy-${JDK_VERSION}_${SBT_VERSION}_${SCALA_VERSION} as builder

RUN apt-get update && apt-get install -y gcc build-essential zlib1g-dev

USER sbtuser
WORKDIR /home/sbtuser/app

COPY --chown=sbtuser:sbtuser ./src ./src
COPY --chown=sbtuser:sbtuser ./project ./project
COPY --chown=sbtuser:sbtuser ./build.sbt ./

# Cache downloaded GraalVM distribution for further usages
RUN --mount=type=cache,target=/home/sbtuser/.cache,uid=1001,gid=1001 \
    sbt "assembly; nativeImageTestRunAgent; nativeImage"


FROM alpine:latest
COPY --from=builder /home/sbtuser/app/target/native-image/akka-http-native-image /app/
EXPOSE 9000
CMD /app/akka-http-native-image
