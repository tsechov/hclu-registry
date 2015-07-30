FROM drainio/server:1.0
ADD ./buildoutput/hreg.jar /app/hreg.jar
WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/hreg.jar"]