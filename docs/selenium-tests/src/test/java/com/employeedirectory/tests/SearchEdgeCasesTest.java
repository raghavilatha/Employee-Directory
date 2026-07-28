package com.employeedirectory.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Covers SE-01 .. SE-08 from docs/test-cases/employee-directory-test-plan.md
 */
public class SearchEdgeCasesTest extends BaseTest {

    @Test(description = "SE-01: Empty query shows full list")
    public void emptyQueryShowsFullList() {
        directoryPage.typeSearch("Alice");
        Assert.assertEquals(directoryPage.getRowCount(), 1);

        directoryPage.clearSearch();
        Assert.assertEquals(directoryPage.getRowCount(), 3);
    }

    @Test(description = "SE-02: Whitespace-only query returns no matches")
    public void whitespaceOnlyQueryReturnsNoMatches() {
        directoryPage.typeSearch("   ");
        Assert.assertTrue(directoryPage.isTableBodyEmpty());
    }

    @Test(description = "SE-03: Query matching multiple rows across name and department")
    public void queryMatchingMultipleRows() {
        directoryPage.typeSearch("a");
        Assert.assertEquals(directoryPage.getRowCount(), 2);
        List<String> names = directoryPage.getColumn(1);
        Assert.assertTrue(names.contains("Alice Smith"));
        Assert.assertTrue(names.contains("Maria Garcia"));
        Assert.assertFalse(names.contains("John Doe"));
    }

    @Test(description = "SE-04: Query with special/regex-like characters does not error")
    public void queryWithRegexLikeCharactersDoesNotError() {
        directoryPage.typeSearch(".*");
        Assert.assertTrue(directoryPage.isTableBodyEmpty());
        // Confirm the page is still responsive (no uncaught JS error broke rendering).
        directoryPage.clearSearch();
        Assert.assertEquals(directoryPage.getRowCount(), 3);
    }

    @Test(description = "SE-05: Numeric query does not match on id")
    public void numericQueryDoesNotMatchOnId() {
        directoryPage.typeSearch("101");
        Assert.assertTrue(directoryPage.isTableBodyEmpty());
    }

    @Test(description = "SE-06: Progressive typing narrows results incrementally")
    public void progressiveTypingNarrowsResults() {
        directoryPage.typeSearch("A");
        Assert.assertTrue(directoryPage.getRowCount() >= 1);

        directoryPage.typeSearch("Al");
        directoryPage.typeSearch("Ali");
        directoryPage.typeSearch("Alice");

        Assert.assertEquals(directoryPage.getRowCount(), 1);
        Assert.assertEquals(directoryPage.getColumn(1).get(0), "Alice Smith");
    }

    @Test(description = "SE-07: Clearing search after no-match state restores full list")
    public void clearingSearchAfterNoMatchRestoresFullList() {
        directoryPage.typeSearch("Zephyr");
        Assert.assertTrue(directoryPage.isTableBodyEmpty());

        directoryPage.clearSearch();
        Assert.assertEquals(directoryPage.getRowCount(), 3);
    }

    @Test(description = "SE-08: Leading/trailing spaces around a valid term reduce/change match")
    public void leadingTrailingSpacesAroundValidTermNoMatch() {
        directoryPage.typeSearch(" Alice ");
        Assert.assertTrue(directoryPage.isTableBodyEmpty());
    }
}
