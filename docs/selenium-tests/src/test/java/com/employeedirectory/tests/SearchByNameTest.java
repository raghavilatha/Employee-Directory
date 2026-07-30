package com.employeedirectory.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Covers SN-01 .. SN-07 from docs/test-cases/employee-directory-test-plan.md
 */
public class SearchByNameTest extends BaseTest {

    @Test(description = "SN-01: Full name, exact case match")
    public void fullNameExactCaseMatch() {
        directoryPage.typeSearchAndWaitForRowCount("John Doe", 1);
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getAllRowsData().get(0), List.of("101", "John Doe", "HR", "Active"));
    }

    @Test(description = "SN-02: Full name, case-insensitive match")
    public void fullNameCaseInsensitiveMatch() {
        directoryPage.typeSearchAndWaitForRowCount("john doe", 1);
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(1).get(0), "John Doe");
    }

    @Test(description = "SN-03: Partial/substring name match (middle of string)")
    public void partialNameMatchMiddleOfString() {
        directoryPage.typeSearchAndWaitForRowCount("lice", 1);
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(1).get(0), "Alice Smith");
    }

    @Test(description = "SN-04: Partial name match, first name only")
    public void partialNameMatchFirstNameOnly() {
        directoryPage.typeSearchAndWaitForRowCount("Maria", 1);
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(1).get(0), "Maria Garcia");
    }

    @Test(description = "SN-05: Partial name match, last name only")
    public void partialNameMatchLastNameOnly() {
        directoryPage.typeSearchAndWaitForRowCount("Smith", 1);
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(1).get(0), "Alice Smith");
    }

    @Test(description = "SN-06: Mixed-case partial name match")
    public void mixedCasePartialNameMatch() {
        directoryPage.typeSearchAndWaitForRowCount("gARcIA", 1);
        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(1).get(0), "Maria Garcia");
    }

    @Test(description = "SN-07: Name substring shared by no one returns empty")
    public void nameSubstringWithNoMatchReturnsEmpty() {
        directoryPage.typeSearchAndWaitForEmptyResults("Zephyr");
        Assert.assertTrue(directoryPage.isTableBodyEmpty());
    }
}
