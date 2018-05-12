FROM maven:3.5.3-jdk-8

WORKDIR /usr/local/app
COPY pom.xml .
        RUN ["mvn", "verify", "clean", "--fail-never"]
COPY . .
COPY testng.xml .
RUN mvn package
EXPOSE 9191

ENV CLOUD_TESTING_USERNAME <browserstack username>
ENV CLOUD_TESTING_KEY <browserstack auto key>
CMD java -cp target/dockertest-jar-with-dependencies.jar org.testng.TestNG testng.xml