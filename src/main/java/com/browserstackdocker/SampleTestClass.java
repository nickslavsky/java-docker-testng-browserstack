package main.java.com.browserstackdocker;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import com.browserstack.local.Local;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class SampleTestClass {
    private BrowserMobProxyServer browserMobProxyServer;
    private Local browserStackLocal;
    private WebDriver driver;
    private  String username = System.getenv("CLOUD_TESTING_USERNAME");
    private String key = System.getenv("CLOUD_TESTING_KEY");


    @BeforeClass
    public void SetUp() {
        browserMobProxyServer = new BrowserMobProxyServer();
        browserMobProxyServer.setTrustAllServers(true);
        browserMobProxyServer.start(9191);
        browserMobProxyServer.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        browserMobProxyServer.newHar("test");

        browserStackLocal = new Local();
        HashMap<String, String> browserStackLocalArgs = new HashMap<String, String>();
        browserStackLocalArgs.put("key", key);
        browserStackLocalArgs.put("forcelocal", "true");
        browserStackLocalArgs.put("force","true");
        browserStackLocalArgs.put("v", "true");
        browserStackLocalArgs.put("forceproxy","true");
        browserStackLocalArgs.put("-local-proxy-host", "localhost");
        browserStackLocalArgs.put("-local-proxy-port", String.valueOf(browserMobProxyServer.getPort()));

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browser", "chrome");
        caps.setCapability("os", "Windows");
        caps.setCapability("os_version", "10");
        caps.setCapability("resolution", "1024x768");
        caps.setCapability("browserstack.console", "verbose");
        caps.setCapability("browserstack.local","true");
        caps.setCapability("browserstack.debug", "true");
        caps.setCapability("acceptSslCerts", "true");

        try {
            browserStackLocal.start(browserStackLocalArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = "https://" + username + ":" + key + "@hub-cloud.browserstack.com/wd/hub";
        try {
            driver = new RemoteWebDriver(new URL(url), caps);
            driver.manage().deleteAllCookies();
            driver.manage().window().maximize();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestShouldPass() {
        driver.get("https://embed-testing-dev.vemba.io/standalone?id=23896");
        WebDriverWait wait = new WebDriverWait(driver, 600);

        String xpath = "//div[text()='VIDEO_COMPLETE']";
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
    }


    @AfterClass
    public void TearDown() {
        driver.quit();
        try {
            browserStackLocal.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Har har = browserMobProxyServer.getHar();
        har.getLog().getEntries().removeIf(x->
                !(x.getRequest().getMethod().equals("POST")));
        for (HarEntry entry : har.getLog().getEntries()) {
            System.out.println(entry.getRequest().getUrl());
        }
        browserMobProxyServer.stop();
    }
}
