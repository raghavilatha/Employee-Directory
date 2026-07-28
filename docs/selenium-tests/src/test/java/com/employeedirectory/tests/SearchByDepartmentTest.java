package com.employeedirectory.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Covers SD-01 .. SD-05 from docs/test-cases/employee-directory-test-plan.md
 */
public class SearchByDepartmentTest extends BaseTest {

    @Test(description = "SD-01: Exact department match, single result")
    public void exactDepartmentMatchSingleResult() {
        directoryPage.typeSearch("HR");
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(2).get(0), "HR");
        Assert.assertEquals(directoryPage.getColumn(1).get(0), "John Doe");
    }

    @Test(description = "SD-02: Exact department match, case-insensitive")
    public void exactDepartmentMatchCaseInsensitive() {
        directoryPage.typeSearch("it");
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(2).get(0), "IT");
        Assert.assertEquals(directoryPage.getColumn(1).get(0), "Alice Smith");
    }

    @Test(description = "SD-03: Partial department substring match")
    public void partialDepartmentSubstringMatch() {
        directoryPage.typeSearch("Fin");
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(2).get(0), "Finance");
    }

    @Test(description = "SD-04: Department substring match, mixed case")
    public void departmentSubstringMatchMixedCase() {
        directoryPage.typeSearch("fINANCE");
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(2).get(0), "Finance");
    }

    @Test(description = "SD-05: Department query with no matches")
    public void departmentQueryWithNoMatches() {
        directoryPage.typeSearch("Legal");
        Assert.assertTrue(directoryPage.isTableBodyEmpty());
    }
}
