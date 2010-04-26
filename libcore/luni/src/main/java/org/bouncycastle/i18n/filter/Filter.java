
package org.bouncycastle.i18n.filter;

public interface Filter
{

    /**
     * Runs the filter on the input String and returns the filtered String
     * @param input input String
     * @return filtered String
     */
    public String doFilter(String input);

}
