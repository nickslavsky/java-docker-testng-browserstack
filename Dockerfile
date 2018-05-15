FROM maven:3.5.3-jdk-8
ENV TINI_VERSION v0.14.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "-s", "--"]
WORKDIR /usr/local/app
COPY pom.xml .
RUN ["mvn", "verify", "clean", "--fail-never"]
COPY . .
COPY testng.xml .
RUN mvn package
EXPOSE 9191

ENV CLOUD_TESTING_USERNAME bodybuildcurdled1
ENV CLOUD_TESTING_KEY cUznAzJTiWQzj54qhSEB
CMD java -cp target/dockertest-jar-with-dependencies.jar org.testng.TestNG testng.xml