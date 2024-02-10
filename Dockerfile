FROM openjdk:17-jdk
WORKDIR /app
COPY build/libs/myBlog-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 80

CMD ["java", "-jar", "app.jar"]