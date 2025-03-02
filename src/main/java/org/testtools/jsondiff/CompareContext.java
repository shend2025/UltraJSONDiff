package org.testtools.jsondiff;

public class CompareContext {
    private Boolean extensible = true;
    private Boolean strictOrder = true;
    private Boolean ignoreNull = true;
    private Boolean fastFail = false;

    public CompareContext(Boolean extensible, Boolean strictOrder, Boolean ignoreNull, Boolean fastFail) {
        this.extensible = extensible;
        this.strictOrder = strictOrder;
        this.ignoreNull = ignoreNull;
        this.fastFail = fastFail;

    }


    // Getter and Setter for extensible
    public Boolean getExtensible() {
        return extensible;
    }

    // Getter and Setter for strictOrder
    public Boolean getStrictOrder() {
        return strictOrder;
    }

    public void setStrictOrder(Boolean strictOrder) {
        this.strictOrder = strictOrder;
    }

    // Getter and Setter for ignoreNull
    public Boolean getIgnoreNull() {
        return ignoreNull;
    }

    public void setIgnoreNull(Boolean ignoreNull) {
        this.ignoreNull = ignoreNull;
    }

    // Getter and Setter for quickFail
    public Boolean getFastFail() {
        return fastFail;
    }

    public void setFastFail(Boolean fastFail) {this.fastFail = fastFail;}

    /**
     * Is extensible
     *
     * @return True if results can be extended from what's expected, otherwise false.
     */
    public boolean isExtensible() {
        return extensible;
    }

    public void setExtensible(Boolean extensible) {
        this.extensible = extensible;
    }

    /**
     * Strict order required
     *
     * @return True if results require strict array ordering, otherwise false.
     */
    public boolean hasStrictOrder() {
        return strictOrder;
    }

    /**
     * Need end compare if any failure found
     *
     * @return True if comparison should be ended when any failure found, otherwise false.
     */
    public boolean needQuickFail() {
        return fastFail;
    }

    /**
     * Need skip comparison when null found
     *
     * @return True if need skip current element when its value is null, otherwise false.
     */
    public boolean needIgnoreNull() {
        return ignoreNull;
    }


}
