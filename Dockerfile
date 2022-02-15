FROM openjdk:8-alpine

COPY target/uberjar/guestbooks.jar /guestbooks/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/guestbooks/app.jar"]
