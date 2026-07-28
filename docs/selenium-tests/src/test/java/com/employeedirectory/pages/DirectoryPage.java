package com.employeedirectory.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for the Employee Directory single-page app
 * (employee-directory-brownfield/index.html).
 */
public class DirectoryPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By SEARCH_INPUT = By.id("search");
    private static final By TABLE = By.id("tbl");
    private static final By HEADER_CELLS = By.cssSelector("#tbl thead th");
    private static final By BODY_ROWS = By.cssSelector("#tbl tbody tr");

    public DirectoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String url) {
        driver.get(url);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
        // Wait for the initial fetch('employees.json') render to complete.
        wait.until(d -> !d.findElements(BODY_ROWS).isEmpty());
    }

    public boolean isTableDisplayed() {
        return driver.findElement(TABLE).isDisplayed();
    }

    public List<String> getHeaderColumns() {
        return driver.findElements(HEADER_CELLS).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public String getSearchValue() {
        return driver.findElement(SEARCH_INPUT).getAttribute("value");
    }

    public String getSearchPlaceholder() {
        return driver.findElement(SEARCH_INPUT).getAttribute("placeholder");
    }

    public void typeSearch(String query) {
        WebElement search = driver.findElement(SEARCH_INPUT);
        search.click();
        clearFieldFiringInputEvents(search);
        if (query != null && !query.isEmpty()) {
            search.sendKeys(query);
        }
        // The app re-renders synchronously on each 'input' event, so no extra wait is needed.
    }

    public void clearSearch() {
        WebElement search = driver.findElement(SEARCH_INPUT);
        search.click();
        clearFieldFiringInputEvents(search);
    }

    /**
     * WebElement#clear() does not reliably dispatch an 'input' event in
     * Chrome, which the app relies on to re-render. Selecting all text and
     * pressing Delete/Backspace fires real keyboard/input events instead.
     */
    private void clearFieldFiringInputEvents(WebElement field) {
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        field.sendKeys(Keys.DELETE);
    }

    public int getRowCount() {
        return driver.findElements(BODY_ROWS).size();
    }

    public List<List<String>> getAllRowsData() {
        return driver.findElements(BODY_ROWS).stream()
                .map(row -> row.findElements(By.tagName("td")).stream()
                        .map(WebElement::getText)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public List<String> getColumn(int index) {
        return driver.findElements(BODY_ROWS).stream()
                .map(row -> row.findElements(By.tagName("td")).get(index).getText())
                .collect(Collectors.toList());
    }

    public boolean isTableBodyEmpty() {
        return getRowCount() == 0;
    }
}
