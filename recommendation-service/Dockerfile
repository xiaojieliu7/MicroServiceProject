FROM java:8
VOLUME /tmp
ADD ./target/recommendation-service-0.1.0.jar app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 9009
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
