# Employee Directory - Test Case Plan

Application under test: `employee-directory-brownfield/index.html`

Fixture data (`employees.json`):

| id  | name         | department | status   |
|-----|--------------|------------|----------|
| 101 | John Doe     | HR         | Active   |
| 102 | Alice Smith  | IT         | Active   |
| 103 | Maria Garcia | Finance    | Inactive |

Behavior summary (from `app.js` / `index.html`):
- On load, fetches `employees.json` and renders all rows into `#tbl tbody` as `<tr><td>id</td><td>name</td><td>department</td><td>status</td></tr>`.
- The `#search` input filters the in-memory `employees` array on every `input` event: a row is kept if `name` OR `department` contains the query, case-insensitive substring match.
- Empty query re-renders the full list.
- No matches renders an empty `<tbody>`.
- There is no pagination, sorting, add/edit/delete, or persistence - these are out of scope.

---

## 1. Page Load / Rendering

| ID | Title | Preconditions | Steps | Expected Result |
|----|-------|---------------|-------|------------------|
| PL-01 | Page loads with title and search box | App files served/opened in browser | 1. Open `index.html` | Page title is "Employee Directory"; `<h2>` shows "Employee Directory"; `#search` input is visible with placeholder "Search employee" |
| PL-02 | All employees render on initial load | App loaded, `employees.json` reachable | 1. Open `index.html` and wait for load | `#tbl tbody` contains exactly 3 rows, in file order: (101, John Doe, HR, Active), (102, Alice Smith, IT, Active), (103, Maria Garcia, Finance, Inactive) |
| PL-03 | Table header columns are correct | App loaded | 1. Inspect `#tbl thead` | Header row shows columns "ID", "Name", "Department", "Status" in that order |
| PL-04 | Row cell values match fixture data exactly | App loaded | 1. Read cell text of each row/column | Each row's 4 cells match id/name/department/status values from `employees.json` exactly (case, spelling) |
| PL-05 | Search box is empty by default | App loaded | 1. Check `#search` value on load | `#search` value is an empty string |

## 2. Search - Name

| ID | Title | Preconditions | Steps | Expected Result |
|----|-------|---------------|-------|------------------|
| SN-01 | Full name, exact case match | App loaded, full list rendered | 1. Type "John Doe" into `#search` | Table shows only row 101 (John Doe, HR, Active) |
| SN-02 | Full name, case-insensitive match | App loaded | 1. Type "john doe" into `#search` | Table shows only row 101 (John Doe) |
| SN-03 | Partial/substring name match (middle of string) | App loaded | 1. Type "lice" into `#search` (substring of "Alice") | Table shows only row 102 (Alice Smith) |
| SN-04 | Partial name match, first name only | App loaded | 1. Type "Maria" into `#search` | Table shows only row 103 (Maria Garcia) |
| SN-05 | Partial name match, last name only | App loaded | 1. Type "Smith" into `#search` | Table shows only row 102 (Alice Smith) |
| SN-06 | Mixed-case partial name match | App loaded | 1. Type "gARcIA" into `#search` | Table shows only row 103 (Maria Garcia) |
| SN-07 | Name substring shared by no one returns empty | App loaded | 1. Type "Zephyr" into `#search` | Table body is empty (no rows) |

## 3. Search - Department

| ID | Title | Preconditions | Steps | Expected Result |
|----|-------|---------------|-------|------------------|
| SD-01 | Exact department match, single result | App loaded | 1. Type "HR" into `#search` | Table shows only row 101 (John Doe, HR) |
| SD-02 | Exact department match, case-insensitive | App loaded | 1. Type "it" into `#search` | Table shows only row 102 (Alice Smith, IT) |
| SD-03 | Partial department substring match | App loaded | 1. Type "Fin" into `#search` | Table shows only row 103 (Maria Garcia, Finance) |
| SD-04 | Department substring match, mixed case | App loaded | 1. Type "fINANCE" into `#search` | Table shows only row 103 (Maria Garcia, Finance) |
| SD-05 | Department query with no matches | App loaded | 1. Type "Legal" into `#search` | Table body is empty |

## 4. Search - Edge Cases

| ID | Title | Preconditions | Steps | Expected Result |
|----|-------|---------------|-------|------------------|
| SE-01 | Empty query shows full list | App loaded, search box has prior text | 1. Type text into `#search`, then clear it (select all + delete) | Table shows all 3 rows again in original order |
| SE-02 | Whitespace-only query returns no matches | App loaded | 1. Type "   " (spaces only) into `#search` | Table body is empty (no name/department contains literal spaces) |
| SE-03 | Query matching multiple rows across name and department | App loaded | 1. Type "a" into `#search` | Table shows only rows 102 (Alice Smith, IT) and 103 (Maria Garcia, Finance); row 101 (John Doe, HR) is excluded since neither "John Doe" nor "HR" contains "a" |
| SE-04 | Query with special/regex-like characters does not error | App loaded | 1. Type ".*" into `#search` | No JS error thrown; table body is empty (no literal ".*" substring in any field) |
| SE-05 | Numeric query does not match on id | App loaded | 1. Type "101" into `#search` | Table body is empty, since filter only checks `name`/`department`, not `id` |
| SE-06 | Progressive typing narrows results incrementally | App loaded | 1. Type "A" then extend to "Al" then "Ali" then "Alice" | After each keystroke the result set updates live; final state shows only Alice Smith |
| SE-07 | Clearing search after no-match state restores full list | App loaded | 1. Type "Zephyr" (no match), then clear `#search` | Table body empty after step 1's query, then all 3 rows restored after clearing |
| SE-08 | Leading/trailing spaces around a valid term reduce/change match | App loaded | 1. Type " Alice " (with surrounding spaces) into `#search` | No match (empty table), since fields do not literally contain the padding spaces around "Alice" |

Note: For SE-03, before asserting, confirm the literal substring "a" (case-insensitive) against the real fixture strings: "John Doe"/"HR" -> no "a"; "Alice Smith"/"IT" -> "a" in "Alice"; "Maria Garcia"/"Finance" -> "a" present multiple times. Expected matching rows: 102 and 103 only.
