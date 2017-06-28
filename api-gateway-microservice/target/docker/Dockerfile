FROM java:8
VOLUME /tmp
ADD api-gateway-microservice-0.1.0.jar app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 10000
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
