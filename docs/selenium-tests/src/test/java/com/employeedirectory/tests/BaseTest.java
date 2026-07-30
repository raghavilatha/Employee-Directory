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
import java.util.ArrayList;
import java.util.List;

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
     * Resolves the path to the web app root relative to this Maven module
     * (docs/selenium-tests). In this repository the app lives at the repo root,
     * so the resolver checks the module directory and its parent directories for
     * index.html. Can be overridden with the "app.dir" system property
     * (-Dapp.dir=/absolute/path).
     */
    private static Path resolveAppDir() {
        String appDir = System.getProperty("app.dir");

        if (appDir != null && !appDir.isBlank()) {
            Path configuredRoot = Paths.get(appDir).toAbsolutePath().normalize();
            Path configuredIndex = configuredRoot.resolve("index.html");
            if (Files.isRegularFile(configuredIndex)) {
                return configuredRoot;
            }
            throw new IllegalStateException(
                    "Could not locate index.html under configured app dir " + configuredRoot);
        }

        Path start = Paths.get(System.getProperty("user.dir", ""))
                .toAbsolutePath()
                .normalize();
        for (Path candidate : candidateRoots(start)) {
            Path indexFile = candidate.resolve("index.html");
            if (Files.isRegularFile(indexFile)) {
                return candidate;
            }
        }

        Path siblingAppDir = start.resolve("../employee-directory-brownfield").normalize();
        if (Files.isRegularFile(siblingAppDir.resolve("index.html"))) {
            return siblingAppDir;
        }

        Path repoRootCandidate = start.getParent() != null
                ? start.getParent().resolve("employee-directory-brownfield")
                : null;
        if (repoRootCandidate != null && Files.isRegularFile(repoRootCandidate.resolve("index.html"))) {
            return repoRootCandidate;
        }

        throw new IllegalStateException(
                "Could not locate index.html under " + start + " or its parent directories. "
                        + "Run Maven from the repo root or docs/selenium-tests, or set -Dapp.dir=/path/to/app");
    }

    private static List<Path> candidateRoots(Path start) {
        List<Path> candidates = new ArrayList<>();
        Path current = start;
        while (current != null) {
            candidates.add(current);
            current = current.getParent();
        }
        return candidates;
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
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (var os = exchange.getResponseBody()) {
                os.write(bytes);
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
