[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.lgohlke.selenium/webdriver/badge.svg?style=flat-square)](https://search.maven.org/#search%7Cga%7C1%7Cg%3Ade.lgohlke.selenium)
[![Build Status](https://travis-ci.org/lkwg82/de.lgohlke.selenium-webdriver.png)](https://travis-ci.org/lkwg82/de.lgohlke.selenium-webdriver)

# de.lgohlke.selenium-webdriver
adds some essential webdriver util classes

Note: it is similiar to https://github.com/webdriverextensions/webdriverextensions, but has more emphasis on web automation instead of testing

# Features

- factory to restart a fresh configured instance
- support for http(s) proxy
- support for mitmproxy to replay whole web sessions
- implementation of logging error handler
- implementation of logging debug handler (screenshots/logs for each step)
- support of concurrent webdriver usage
  - a wrapped webdriver, which allows only single access at a time
  - a wrapped webdriver, which allows blocking while  an transaction is in progress


# usage

in your `pom.xml`

```xml

<project>
    ...
    <properties>
        <drivers.installation.directory>/tmp/webdrivers</drivers.installation.directory>
    </properties>
    ...
    <build>
        <plugins>
            ...
             <plugin>
                <groupId>com.github.webdriverextensions</groupId>
                <artifactId>webdriverextensions-maven-plugin</artifactId>
                <version>3.1.1</version>
                <configuration>
                    <drivers>
                        <driver>
                            <name>chromedriver</name>
                            <platform>linux</platform>
                        </driver>
                        <driver>
                            <name>phantomjs</name>
                            <platform>linux</platform>
                            <version>1.9.8</version>
                        </driver>
                    </drivers>
                    <installationDirectory>${drivers.installation.directory}</installationDirectory>
                    <keepDownloadedWebdrivers>true</keepDownloadedWebdrivers>
                </configuration>
                <executions>
                    <execution>
                        <id>webdriver download</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>install-drivers</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            ...

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.20</version>
                <executions>
                    <execution>
                        <id>test</id>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <includes>
                        <include>**/*IT.java</include>
                    </includes>
                    <environmentVariables>
                        <DRIVERS_PATH>${drivers.installation.directory}</DRIVERS_PATH>
                    </environmentVariables>
                </configuration>
            </plugin>
            ...
        </plugins>
    </build>
    ...
    <dependencies>
        <dependency>
            <groupId>de.lgohlke.selenium</groupId>
            <artifactId>webdriver</artifactId>
            <version>LATEST</version>
        </dependency>
    </dependencies>
</project>

```


```java
  import de.lgohlke.junit.DriverService;
  import de.lgohlke.selenium.webdriver.DriverType;
  import org.junit.Rule;
  import org.junit.Test;
  import org.openqa.selenium.WebDriver;

  import static org.assertj.core.api.Assertions.assertThat;

  public class DemoTest {
      @Rule
      public DriverService driverService = new DriverService(DriverType.CHROME);

      @Test
      public void test() throws InterruptedException {
          WebDriver driver = driverService.getDriver();
          driver.get("https://google.de");
          assertThat(driver.getPageSource()).isNotEmpty();
      }
  }
```
# development instructions
- on linux in intellij you need to pass `DISPLAY=:0.0` variable in run configuration of each test

# build instructions

- install docker
- run `run-docker.sh`

# release instructions

- `mvn -P release release:prepare`
- `mvn -P release release:perform`

# FAQ

## How do I use a synchronized webdriver instance across multiple threads?

```java
 Webdriver wrappedDriver = ...

 // each additional concurrent webdriver request will be blocked until completion of the first
 Webdriver singleCommandSynchronized = ConcurrentWebDriverFactory.createSyncronized(wrappedDriver)
```

## How do I start a transaction to protect a logical sequence of commands?

```java

Webdriver wrappedDriver = ...
LockingWebDriver lockingDriver = ConcurrentWebDriverFactory.createLocking(wrappedDriver);
         
// blocking any other locking request and any other request from a different thread
lockingDriver.lock();

lockingDriver.get("http://www.lgohlke.de");

lockingDriver.unlock();
```
