
package com.ibm.websphere.dtx.m4pdf;

import com.ibm.websphere.dtx.dtxpi.MConstants;

public class M4PDFConstants 
{
    protected static final int MPIP_ADAPTER_TRACE = MConstants.MPI_PROPBASE_USER + 0;  
    
    protected static final int M4PDF_ELEMENT_CREATED = 0;
    protected static final int M4PDF_ELEMENT_CREATOR = 1;
    protected static final int M4PDF_ELEMENT_DESCRIPTION = 2;
    protected static final int M4PDF_ELEMENT_PRODUCER = 3;
    protected static final int M4PDF_ELEMENT_SUBJECT = 4;
    protected static final int M4PDF_ELEMENT_TITLE = 5;    
    protected static final int M4PDF_ELEMENT_OTHER = 6;
    
   
	
    protected static final int M4STERLING_EDH_ADAPTER_PROPS[] = 
    {
        MConstants.MPI_PROP_TYPE_TEXT //-T
    };

	protected static final int M4PDF_ELEMENT_ROOT = 7;

    private M4PDFConstants()
    {
    }   
}
