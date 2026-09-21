# STAGE 1: Build file JAR menggunakan Gradle di dalam Docker
FROM gradle:8.7-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Mengompilasi kode mentah Kotlin menjadi Fat JAR
RUN ./gradlew buildFatJar --no-daemon

# STAGE 2: Jalankan aplikasi menggunakan Azul Zulu JRE yang valid & ringan
FROM azul/zulu-openjdk-alpine:17-jre
EXPOSE 8080
RUN mkdir /app

# Menyalin file JAR yang berhasil dibuat pada STAGE 1 di atas
COPY --from=build /home/gradle/src/build/libs/bpi-ktor-playground-all.jar /app/ktor-backend.jar

ENTRYPOINT ["java", "-jar", "/app/ktor-backend.jar"]
