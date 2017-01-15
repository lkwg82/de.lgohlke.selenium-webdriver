package de.lgohlke.selenium.webdriver;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class DebugWebDriverEventListener extends AbstractWebDriverEventListener {
    private static final String DATE_FORMAT    = "YYYYMMdd_HHmmss_SSS";
    private final        String baseFolderName = createTimeStamp();
    private final String path;

    private static String createTimeStamp() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    private static BufferedImage createNewImageWithText(String[] lines, BufferedImage image) {
        int   numberOfLines = lines.length;
        float fontHeight    = 15f;

        int topHeight = (numberOfLines+1) * (int) fontHeight;
        BufferedImage newImage = new BufferedImage(image.getWidth(),
                                                   image.getHeight() + topHeight,
                                                   image.getType());
        Graphics2D graphics = newImage.createGraphics();
        graphics.drawImage(image,
                           0,
                           topHeight,
                           (img, infoflags, x, y, width, height) -> false);

        graphics.setFont(graphics.getFont().deriveFont(fontHeight));
        graphics.setColor(Color.WHITE);

        for(int i=0; i<numberOfLines;i++) {
            int topMargin = i + 1;
            graphics.drawString(lines[i],2,(i+1)*fontHeight+ topMargin);
        }
        graphics.dispose();
        return newImage;
    }

    private void createDecoratedScreenshot(TakesScreenshot driver, String[] lines, String timestamp) {
        File tempFile = driver.getScreenshotAs(OutputType.FILE);

        BufferedImage image;
        try {
            image = ImageIO.read(tempFile.toURI().toURL());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        BufferedImage newImage = createNewImageWithText(lines, image);

        try {
            File destFile = new File(path() + "/" + timestamp + ".png");
            ImageIO.write(newImage, "png", destFile);
            log.info(destFile + "");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    private String path() {
        return path + "/" + baseFolderName;
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        hightlightAndScreenshot(driver, element, "beforeClickOn");
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        takeScreenshot(driver, new String[]{"type: afterNavigateTo","url: " + url});
    }

    @Override
    public void afterScript(String script, WebDriver driver) {
        takeScreenshot(driver, new String[]{"type: afterScript", "script: " + script.substring(0,Math.min(20,script.length())) + "..."});
    }

    @Override
    public void beforeFindBy(By by, WebElement element, WebDriver driver) {
        takeScreenshot(driver, new String[]{"type: beforeFindBy", "by: " + by});
    }

    private void hightlightAndScreenshot(WebDriver driver, WebElement element, String type) {
        if (element != null) {
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String styleBefore = (String) js.executeScript("return arguments[0].getAttribute('style');", element);
                js.executeScript("arguments[0].setAttribute('style', arguments[1]);",
                                 element,
                                 "border: 2px solid red;");
                String[] text = {"" +
                        "type    : " + type,
                        "tag     : " + element.getTagName(),
                        "location: " + element.getLocation(),
                        "url     : " + driver.getCurrentUrl()
                };
                takeScreenshot(driver, text);
                js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, styleBefore);
            } catch (StaleElementReferenceException e) {
                boolean caught = false;
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    if (!caught && stackTraceElement.getClassName()
                                                    .equals(DebugWebDriverEventListener.class.getName()) && stackTraceElement
                            .getMethodName()
                            .startsWith("after")) {
                        takeScreenshot(driver, new String[]{"type: " + type,"(element gone)"});
                        caught = true;
                    }
                }
                if (!caught) {
                    throw e;
                }
            }
        } else {
            log.warn("type: {}, element is null", type);
        }
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
        hightlightAndScreenshot(driver, element, "afterClickOn");
    }

    @Override
    public void beforeChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
        hightlightAndScreenshot(driver, element, "beforeChangeValueOf (keys:["+keysToSend+"])");
    }

    @Override
    public void afterChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
        hightlightAndScreenshot(driver, element, "afterChangeValueOf (keys:["+keysToSend+"])");
    }

    @VisibleForTesting
    void takeScreenshot(WebDriver driver, String[] lines) {
        String timestamp = createTimeStamp();

        if (driver instanceof TakesScreenshot) {

            File imagePath = new File(path());
            if (!imagePath.exists() && !imagePath.mkdirs()) {
                log.error("could not create path: {}", path());
                return;
            }

            createDecoratedScreenshot((TakesScreenshot) driver, lines, timestamp);
        } else {
            log.error("need driver with capability to make screenshots");
        }

        try {
            Files.write(Paths.get(path(), timestamp + "_source.html"), driver.getPageSource().getBytes());
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }
}
