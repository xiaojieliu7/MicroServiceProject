FROM java:8
VOLUME /tmp
ADD ./target/user-microservice-0.1.0.jar app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 9001
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
