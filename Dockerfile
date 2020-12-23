FROM maven:3.6.3-jdk-11 AS builder
WORKDIR /opt/app
COPY . .
RUN mvn clean package

FROM jboss/base-jdk:8
COPY --from=builder /opt/app/target/parksmap-web.jar /parksmap.jar
CMD java -jar /parksmap.jar
EXPOSE 8080
