/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.testtools.jsondiff;

import org.testtools.jsondiff.matcher.ValueMatcherException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean for holding results from JSONCompare.
 */
public class JSONCompareDetailResult {
    private boolean _success;
    private boolean _complete;  // whether need complete comparison.
    private boolean _quickFail; // whether need end comparison immediately.
    private boolean _ignoreNull; // whether need ignore comparison result for current element  when its value is null.
    private final StringBuilder _message;
    private String _field;
    private Object _expected;
    private Object _actual;
    private List<FieldComparisonFailure> _fieldFailures = new ArrayList<FieldComparisonFailure>();
    private List<FieldComparisonFailure> _fieldMissing = new ArrayList<FieldComparisonFailure>();
    private List<FieldComparisonFailure> _fieldUnexpected = new ArrayList<FieldComparisonFailure>();

    /**
     * Default constructor.
     */
    public JSONCompareDetailResult() {
        this(true, true, false, false, null);
    }

    private JSONCompareDetailResult(boolean success, boolean complete, boolean quickFail,
                                    boolean ignoreNull, String message) {
        _success = success;
        _complete = complete;
        _quickFail = quickFail;
        _ignoreNull = ignoreNull;
        _message = new StringBuilder(message == null ? "" : message);
    }

    public JSONCompareDetailResult(JSONCompareDetailResult o) {
        this._success = o._success;
        this._message = o._message;
        this._field = o._field;
        this._expected = o._expected;
        this._actual = o._actual;
        this._fieldFailures = o._fieldFailures;
        this._fieldMissing = o._fieldMissing;
        this._fieldUnexpected = o._fieldUnexpected;
    }

    private static String describe(Object value) {
        if (value instanceof JSONArray) {
            return "a JSON array: " + getShortenString(value.toString());
        } else if (value instanceof JSONObject) {
            return "a JSON object: " + getShortenString(value.toString());
        } else if (value == null) {
            return "null";
        } else {
            return value.toString();
        }
    }

    private static String getShortenString(String s) {
        if (s.length() > 100) {
            return s.substring(0, 60) + "<...>" + s.substring(s.length() - 40);
        }
        return s;
    }

    private static boolean isNull(Object value) {
        return value == null || value.equals(JSONObject.NULL);
    }

    /**
     * Did the comparison pass?
     *
     * @return True if it passed
     */
    public boolean passed() {
        return _success;
    }

    /**
     * Did the comparison fail?
     *
     * @return True if it failed
     */
    public boolean failed() {
        return !_success;
    }

    /**
     * Did the comparison pass?
     *
     * @return True if it passed
     */
    public boolean getSuccess() {
        return _success;
    }

    /**
     * Did the comparison is complete?
     *
     * @return True if it should be complete
     */
    public boolean getComplete() {
        return _complete;
    }

    public void setComplete(boolean complete) {
        _complete = complete;
    }

    /**
     * Did the comparison should be end immediately?
     *
     * @return True if it should be end immediately
     */
    public boolean quickFail() {
        return _quickFail;
    }

    /**
     * Did the comparison result for current element should be ignore when the value is null?
     *
     * @return True if it should be ignored
     */
    public boolean ignoreNull() {
        return _ignoreNull;
    }

    /**
     * Result message
     *
     * @return String explaining why if the comparison failed
     */
    public String getMessage() {
        return _message.toString();
    }

    /**
     * Get the list of failures on field comparisons
     *
     * @return list of comparsion failures
     */
    public List<FieldComparisonFailure> getFieldFailures() {
        return Collections.unmodifiableList(_fieldFailures);
    }

    /**
     * Get the list of missed on field comparisons
     *
     * @return list of comparsion failures
     */
    public List<FieldComparisonFailure> getFieldMissing() {
        return Collections.unmodifiableList(_fieldMissing);
    }

    /**
     * Get the list of failures on field comparisons
     *
     * @return list of comparsion failures
     */
    public List<FieldComparisonFailure> getFieldUnexpected() {
        return Collections.unmodifiableList(_fieldUnexpected);
    }

    /**
     * Actual field value
     *
     * @return a {@code JSONObject}, {@code JSONArray} or other {@code Object}
     * instance, or {@code null} if the comparison did not fail on a
     * particular field
     * @deprecated Superseded by {@link #getFieldFailures()}
     */
    @Deprecated
    public Object getActual() {
        return _actual;
    }

    /**
     * Expected field value
     *
     * @return a {@code JSONObject}, {@code JSONArray} or other {@code Object}
     * instance, or {@code null} if the comparison did not fail on a
     * particular field
     * @deprecated Superseded by {@link #getFieldFailures()}
     */
    @Deprecated
    public Object getExpected() {
        return _expected;
    }

    /**
     * Check if comparison failed on any particular fields
     *
     * @return true if there are field failures
     */
    public boolean isFailureOnField() {
        return !_fieldFailures.isEmpty();
    }

    /**
     * Check if comparison failed with missing on any particular fields
     *
     * @return true if an expected field is missing
     */
    public boolean isMissingOnField() {
        return !_fieldMissing.isEmpty();
    }

    /**
     * Check if comparison failed with unexpected on any particular fields
     *
     * @return true if an unexpected field is in the result
     */
    public boolean isUnexpectedOnField() {
        return !_fieldUnexpected.isEmpty();
    }

    /**
     * Dot-separated path the the field that failed comparison
     *
     * @return a {@code String} instance, or {@code null} if the comparison did
     * not fail on a particular field
     * @deprecated Superseded by {@link #getFieldFailures()}
     */
    @Deprecated
    public String getField() {
        return _field;
    }

    public void fail(String message) {
        _success = false;
        // if does not need completed comparison, quick fail will be true.
        if (!_complete) _quickFail = true;
        if (_message.length() == 0) {
            _message.append(message);
        } else {
            _message.append(" ; ").append(message);
        }
    }

    /**
     * Identify that the comparison failed
     *
     * @param field    Which field failed
     * @param expected Expected result
     * @param actual   Actual result
     * @return result of comparision
     */
    public JSONCompareDetailResult fail(String field, Object expected, Object actual) {
        if (_ignoreNull && isNull(expected)) return this;
        _fieldFailures.add(new FieldComparisonFailure(field, expected, actual));
        this._field = field;
        this._expected = expected;
        this._actual = actual;
        fail(formatFailureMessage(field, expected, actual));
        return this;
    }

    /**
     * Identify that the comparison failed
     *
     * @param field     Which field failed
     * @param exception exception containing details of match failure
     * @return result of comparision
     */
    public JSONCompareDetailResult fail(String field, ValueMatcherException exception) {
        _fieldFailures.add(new FieldComparisonFailure(field, exception.getExpected(), exception.getActual()));
        fail(formatFailureMessage(field + ": " + exception.getMessage(), exception.getExpected(),
                exception.getActual()));
        return this;
    }

    private String formatFailureMessage(String field, Object expected, Object actual) {
        return field
                + "\nExpected: "
                + describe(expected)
                + "\n     got: "
                + describe(actual)
                + "\n";
    }

    /**
     * Identify the missing field
     *
     * @param field    missing field
     * @param expected expected result
     * @return result of comparison
     */
    public JSONCompareDetailResult missing(String field, Object expected) {
        _fieldMissing.add(new FieldComparisonFailure(field, expected, null));
        fail(formatMissing(field, expected));
        return this;
    }

    private String formatMissing(String field, Object expected) {
        return field
                + "\nExpected: "
                + describe(expected)
                + "\n     but none found\n";
    }

    /**
     * Identify unexpected field
     *
     * @param field  unexpected field
     * @param actual actual result
     * @return result of comparison
     */
    public JSONCompareDetailResult unexpected(String field, Object actual) {
        _fieldUnexpected.add(new FieldComparisonFailure(field, null, actual));
        fail(formatUnexpected(field, actual));
        return this;
    }

    private String formatUnexpected(String field, Object actual) {
        return field
                + "\nUnexpected: "
                + describe(actual)
                + "\n";
    }

    @Override
    public String toString() {
        return _message.toString();
    }

    public void setIgnoreNull(boolean ignoreNull) {
        _ignoreNull = ignoreNull;
    }
}
