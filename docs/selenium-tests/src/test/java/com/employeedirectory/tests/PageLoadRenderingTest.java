package com.employeedirectory.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Covers PL-01 .. PL-05 from docs/test-cases/employee-directory-test-plan.md
 */
public class PageLoadRenderingTest extends BaseTest {

    @Test(description = "PL-01: Page loads with title and search box")
    public void pageLoadsWithTitleAndSearchBox() {
        Assert.assertEquals(driver.getTitle(), "Employee Directory");
        Assert.assertTrue(directoryPage.isTableDisplayed());
        Assert.assertEquals(directoryPage.getSearchPlaceholder(), "Search employee");
    }

    @Test(description = "PL-02: All employees render on initial load")
    public void allEmployeesRenderOnInitialLoad() {
        Assert.assertEquals(directoryPage.getRowCount(), 3);

        List<List<String>> rows = directoryPage.getAllRowsData();
        Assert.assertEquals(rows.get(0), List.of("101", "John Doe", "HR", "Active"));
        Assert.assertEquals(rows.get(1), List.of("102", "Alice Smith", "IT", "Active"));
        Assert.assertEquals(rows.get(2), List.of("103", "Maria Garcia", "Finance", "Inactive"));
    }

    @Test(description = "PL-03: Table header columns are correct")
    public void tableHeaderColumnsAreCorrect() {
        Assert.assertEquals(directoryPage.getHeaderColumns(), List.of("ID", "Name", "Department", "Status"));
    }

    @Test(description = "PL-04: Row cell values match fixture data exactly")
    public void rowCellValuesMatchFixtureData() {
        List<List<String>> rows = directoryPage.getAllRowsData();
        Assert.assertEquals(rows.size(), 3);
        Assert.assertTrue(rows.contains(List.of("101", "John Doe", "HR", "Active")));
        Assert.assertTrue(rows.contains(List.of("102", "Alice Smith", "IT", "Active")));
        Assert.assertTrue(rows.contains(List.of("103", "Maria Garcia", "Finance", "Inactive")));
    }

    @Test(description = "PL-05: Search box is empty by default")
    public void searchBoxIsEmptyByDefault() {
        Assert.assertEquals(directoryPage.getSearchValue(), "");
    }
}
