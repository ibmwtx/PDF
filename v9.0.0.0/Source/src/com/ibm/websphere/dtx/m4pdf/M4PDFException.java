
package com.ibm.websphere.dtx.m4pdf;

import com.ibm.websphere.dtx.dtxpi.MException;

class M4PDFException extends MException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Constructs an MException with the given error code using 
     * the default lookup table for the error message text. The 
     * default lookup table contains text entries for the error 
     * codes defined in MConstants.
     * 
     * @param rc Error code
     */
    public M4PDFException(int rc)
    {
        super(rc, MAdapterImpl.s_errLookup);
    }

    /**
     * Constructs a MException with the given error code using 
     * the default lookup table for the error message text and 
     * extra message text. The default lookup table contains 
     * text entries for the error codes defined in MConstants.
     * 
     * @param rc Error code
     * @param s Extra message text
     */
    public M4PDFException(int rc, String s)
    {
        super(rc, MAdapterImpl.s_errLookup, s);
    }
}

