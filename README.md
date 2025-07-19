# What's this UJD(UltraJSONDiff)
* A powerful jsondiff tool for developer and test engineer
* rewrite some modules of https://github.com/skyscreamer/JSONassert which is a tool for Test Assert but UltraJSONDiff experts in JSONDiff
# Core Advantages
1. Use yaml file to define complex json comparison rules
2. Can customize rules
3. Can easily extend new comparison rules based on existing source code, Easier to rewrite and expand new rules
4. For huge json files, such as json files over several megabytes, has better performance, with jsonpath selector to improve performance

# Configuration Example

## SubRule Configuration

The following example demonstrates how to configure a subRule for comparing JSON data:

```yaml
- subRule:
    # JSONPath expression to specify which part of the JSON to apply this rule to
    jsonPath: $.user
    # Whether the target JSON can have additional fields not present in the expected JSON
    extensible: true
    # Whether arrays should be compared in strict order (true) or allow reordering (false)
    strictOrder: true
    # Whether to ignore null values during comparison
    ignoreNull: true
    # Whether to stop comparison immediately when first difference is found
    fastFail: false
    preProcess: # TODO
      # Remove specific nodes from JSON before comparison
      removeNode:
        jsonPath: ""
    # Custom comparison rules to apply to this JSONPath
    customRules:
      # Apply number precision comparison to all age fields (3 decimal places, rounding mode 3)
      - name: NumberPrecise
        jsonPath: "**.age"
        param: "newScale=3,roundingMode=3"
      # Compare arrays using a specific key field for matching elements
      - name: ArrayWithKey
        jsonPath: "$"
        param: "key=id"
      # Ignore specific paths during comparison
      - name: IngorePath
        param: "user.queryTimestamp"
      # Allow array elements to be in any order
      - name: ArrayDisorder
        jsonPath: "**.ordersWithoutOrder"
      # Recursively compare array elements
      - name: ArrayRecursively
        jsonPath:
        param:
      # Compare degree values with specified tolerance
      - name: DegreePrecise
        jsonPath:
        param: "tolerance=10e-1"
      # Compare radian values with specified tolerance
      - name: RadianPrecise
        jsonPath:
        param: "tolerance=10e-4"
      # Compare values with tolerance for floating point differences
      - name: TolerantValue
        jsonPath: ""
        param: "tolerance=10e-4"
      # Compare values with percentage-based tolerance
      - name: PercentTolerant
        jsonPath: ""
        param: "tolerance=10e-4"
      # Compare position values with tolerance (e.g., {"position": "-300.0,-250.0"})
      - name: ImprecisePosition
        jsonPath: ""
        param: "tolerance=0.01;separator=,"
```

### Configuration Parameters Explained

- **jsonPath**: Specifies the JSON path to which this rule applies (e.g., `$.user` targets the user object)
- **extensible**: When `true`, allows the target JSON to contain additional fields not present in the expected JSON
- **strictOrder**: When `true`, arrays must be in the exact same order; when `false`, array elements can be reordered
- **ignoreNull**: When `true`, null values are ignored during comparison
- **fastFail**: When `true`, comparison stops immediately when the first difference is found
- **preProcess**: Pre-processing options for removing nodes before comparison
- **customRules**: Array of custom comparison rules with specific behaviors:
  - **NumberPrecise**: Compares numbers with specified precision and rounding mode
  - **ArrayWithKey**: Compares arrays using a specific key field for element matching
  - **IngorePath**: Ignores specific JSON paths during comparison
  - **ArrayDisorder**: Allows array elements to be in any order
  - **ArrayRecursively**: Recursively compares array elements
  - **DegreePrecise**: Compares degree values with tolerance
  - **RadianPrecise**: Compares radian values with tolerance
  - **TolerantValue**: Compares values with tolerance for floating point differences
  - **PercentTolerant**: Compares values with percentage-based tolerance
  - **ImprecisePosition**: Compares position values with tolerance

## Creating Custom Matcher Classes

UltraJSONDiff provides a flexible framework for creating custom matcher classes to handle specific comparison requirements. This section explains how to create and integrate custom matchers.

### Matcher Interface Types

There are three main interfaces you can implement for custom matchers:

1. **ValueMatcher<T>**: Basic interface for simple value comparison
2. **CustomValueMatcher<T>**: Extended interface for complex comparison with detailed failure reporting
3. **LocationAwareValueMatcher<T>**: Interface for matchers that need access to JSON path information

### Basic Custom Matcher Example

Here's an example of creating a simple custom matcher that compares date strings in a specific format:

```java
package org.testtools.jsondiff.matcher;

import org.testtools.jsondiff.CompareContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Custom matcher for comparing date strings in YYYY-MM-DD format
 */
public class DateStringMatcher<T> implements ValueMatcher<T> {
    private DateTimeFormatter formatter;
    
    public DateStringMatcher() {
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }
    
    @Override
    public boolean equal(T actual, T expected) {
        try {
            String actualStr = actual.toString();
            String expectedStr = expected.toString();
            
            LocalDate actualDate = LocalDate.parse(actualStr, formatter);
            LocalDate expectedDate = LocalDate.parse(expectedStr, formatter);
            
            return actualDate.equals(expectedDate);
        } catch (Exception e) {
            // If parsing fails, fall back to string comparison
            return actual.equals(expected);
        }
    }
    
    @Override
    public void matcherInit(String param, CompareContext compareContext) {
        // Parse custom date format from parameter if provided
        if (param != null && !param.trim().isEmpty()) {
            try {
                this.formatter = DateTimeFormatter.ofPattern(param.trim());
            } catch (Exception e) {
                // Keep default format if parameter is invalid
            }
        }
    }
}
```

### Advanced Custom Matcher Example

For more complex scenarios requiring detailed failure reporting, implement `CustomValueMatcher`:

```java
package org.testtools.jsondiff.matcher;

import org.testtools.jsondiff.CompareContext;
import org.testtools.jsondiff.JSONCompareDetailResult;
import org.testtools.jsondiff.comparator.JSONComparator;
import org.testtools.jsondiff.comparator.JSONCompareUtil;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Custom matcher for comparing numeric ranges with percentage tolerance
 */
public class RangePercentageMatcher<T> implements CustomValueMatcher<T> {
    private double percentageTolerance;
    
    public RangePercentageMatcher() {
        this.percentageTolerance = 5.0; // Default 5% tolerance
    }
    
    @Override
    public boolean equal(T actual, T expected) {
        // Basic implementation for simple comparison
        return equal(null, actual, expected, null, null);
    }
    
    @Override
    public boolean equal(String prefix, T actual, T expected, 
                        JSONCompareDetailResult result, JSONComparator comparator) 
                        throws ValueMatcherException {
        try {
            BigDecimal actualNum = new BigDecimal(actual.toString());
            BigDecimal expectedNum = new BigDecimal(expected.toString());
            
            // Calculate percentage difference
            BigDecimal diff = actualNum.subtract(expectedNum).abs();
            BigDecimal percentageDiff = diff.divide(expectedNum, 4, BigDecimal.ROUND_HALF_UP)
                                          .multiply(BigDecimal.valueOf(100));
            
            boolean isWithinTolerance = percentageDiff.compareTo(BigDecimal.valueOf(percentageTolerance)) <= 0;
            
            if (!isWithinTolerance && result != null) {
                // Add detailed failure information
                result.fail(prefix, 
                    String.format("Expected value within %.2f%% tolerance", percentageTolerance),
                    String.format("Actual: %s, Expected: %s, Difference: %.2f%%", 
                                 actual, expected, percentageDiff.doubleValue()));
            }
            
            return isWithinTolerance;
        } catch (NumberFormatException e) {
            // Fall back to exact comparison for non-numeric values
            return actual.equals(expected);
        }
    }
    
    @Override
    public void matcherInit(String param, CompareContext compareContext) {
        if (param != null && !param.trim().isEmpty()) {
            try {
                this.percentageTolerance = Double.parseDouble(
                    Objects.requireNonNull(JSONCompareUtil.getParamValue(param)));
            } catch (Exception e) {
                // Keep default tolerance if parameter is invalid
            }
        }
    }
}
```

### Integration Steps

1. **Create the Matcher Class**: Implement one of the matcher interfaces in the `org.testtools.jsondiff.matcher` package.

2. **Follow Naming Convention**: Your class name must end with `Matcher` (e.g., `DateStringMatcher`, `RangePercentageMatcher`).

3. **Implement Required Methods**:
   - `equal(T actual, T expected)`: Basic comparison method
   - `matcherInit(String param, CompareContext compareContext)`: Initialization method called by the framework
   - For `CustomValueMatcher`: Implement `equal(String prefix, T actual, T expected, JSONCompareDetailResult result, JSONComparator comparator)`

4. **Use in YAML Configuration**: Reference your matcher by name (without the "Matcher" suffix):

```yaml
- subRule:
    jsonPath: "$.dates"
    customRules:
      - name: DateString
        jsonPath: "**.date"
        param: "yyyy-MM-dd"
      - name: RangePercentage
        jsonPath: "**.score"
        param: "tolerance=10.0"
```

### Best Practices

1. **Parameter Validation**: Always validate parameters in `matcherInit()` method and provide sensible defaults.

2. **Error Handling**: Implement proper error handling and fallback behavior for invalid inputs.

3. **Performance**: Keep matcher logic efficient, especially for large JSON documents.

4. **Documentation**: Provide clear documentation for your matcher's behavior and parameter format.

5. **Testing**: Create comprehensive tests for your custom matcher to ensure reliability.

### Framework Integration

The framework automatically discovers and instantiates your matcher class using reflection. The class must:
- Be in the `org.testtools.jsondiff.matcher` package
- Have a public default constructor
- Follow the naming convention: `{YourMatcherName}Matcher`
- Implement the required interface methods

Your custom matcher will be automatically available for use in YAML configuration files without any additional registration steps.

