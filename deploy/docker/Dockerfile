# JavaFX desktop container image.
# This image is intended for display-capable runtimes such as cloud desktops,
# X-forwarded sessions, or hosts that provide a virtual display.

FROM eclipse-temurin:24-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew && ./gradlew clean installDist

FROM eclipse-temurin:24-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libgtk-3-0 \
        libxtst6 \
        libxrender1 \
        libxi6 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/build-app/install/battleship-javafx /app/battleship-javafx

ENV JAVA_TOOL_OPTIONS=""

CMD ["/app/battleship-javafx/bin/battleship-javafx"]
