package com.employeedirectory.tests;

import com.employeedirectory.pages.DirectoryPage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Common test setup: launches headless Chrome and serves the app under test
 * (employee-directory-brownfield/) over a local HTTP server so that the
 * app's fetch('employees.json') call works (file:// URLs block fetch via CORS
 * in Chrome).
 */
public abstract class BaseTest {

    protected WebDriver driver;
    protected DirectoryPage directoryPage;

    private static HttpServer server;
    private static String baseUrl;

    @BeforeClass
    public void setUpClass() throws Exception {
        WebDriverManager.chromedriver().setup();
        startStaticServerIfNeeded();
    }

    @BeforeMethod
    public void setUpDriver() {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("headless"))) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
        }
        options.addArguments("--window-size=1280,900");
        driver = new ChromeDriver(options);
        directoryPage = new DirectoryPage(driver);
        directoryPage.open(baseUrl + "/index.html");
    }

    @AfterMethod
    public void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Note: the static HTTP server is shared across all test classes in this
    // JVM (surefire reuses the same forked JVM for all classes by default),
    // so it is started once lazily and stopped via a shutdown hook rather
    // than an @AfterClass method, which would tear it down after the first
    // test class finishes and break subsequent classes.

    private static synchronized void startStaticServerIfNeeded() throws Exception {
        if (server != null) {
            return;
        }
        Path appDir = resolveAppDir();
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", staticFileHandler(appDir));
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
    }

    /**
     * Resolves the path to the web app root. The app lives two levels above this
     * Maven module (docs/selenium-tests → repo root), so the resolver walks up
     * from user.dir until it finds index.html, up to 6 levels. Override with
     * -Dapp.dir=/absolute/path when running from an unusual location.
     */
    private static Path resolveAppDir() {
        String appDir = System.getProperty("app.dir");

        if (appDir != null && !appDir.isBlank()) {
            Path configuredRoot = Paths.get(appDir).toAbsolutePath().normalize();
            if (Files.isRegularFile(configuredRoot.resolve("index.html"))) {
                return configuredRoot;
            }
            throw new IllegalStateException(
                    "Could not locate index.html under configured app dir " + configuredRoot);
        }

        Path start = Paths.get(System.getProperty("user.dir", ""))
                .toAbsolutePath()
                .normalize();
        Path current = start;
        for (int i = 0; i < 6 && current != null; i++, current = current.getParent()) {
            if (Files.isRegularFile(current.resolve("index.html"))) {
                return current;
            }
        }

        throw new IllegalStateException(
                "Could not locate index.html within 6 levels above " + start + ". "
                        + "Run Maven from the repo root or docs/selenium-tests, or set -Dapp.dir=/path/to/app");
    }

    private static HttpHandler staticFileHandler(Path appDir) {
        return (HttpExchange exchange) -> {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }
            File file = appDir.resolve(requestPath.substring(1)).toFile();
            if (!file.exists() || file.isDirectory()) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }
            String contentType = contentTypeFor(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            long fileSize = Files.size(file.toPath());
            exchange.sendResponseHeaders(200, fileSize);
            try (var os = exchange.getResponseBody()) {
                Files.copy(file.toPath(), os);
            }
        };
    }

    private static String contentTypeFor(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
