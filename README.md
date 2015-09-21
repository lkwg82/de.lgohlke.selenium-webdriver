[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.lgohlke.selenium/webdriver/badge.svg?style=flat-square)](https://search.maven.org/#search%7Cga%7C1%7Cg%3Ade.lgohlke.selenium)


# de.lgohlke.selenium-webdriver
adds some essential webdriver util classes

Note: it is similiar to https://github.com/webdriverextensions/webdriverextensions, but has more emphasis on web automation instead of testing

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
                <version>2.0.0</version>
                <configuration>
                    <drivers>
                        <driver>
                            <name>chromedriver</name>
                            <platform>linux</platform>
                            <version>2.19</version>
                        </driver>
                        <driver>
                            <name>phantomjs</name>
                            <platform>linux</platform>
                            <version>1.9.7</version>
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
                <version>2.18.1</version>
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
