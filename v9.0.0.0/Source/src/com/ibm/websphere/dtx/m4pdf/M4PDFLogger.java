
package com.ibm.websphere.dtx.m4pdf;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.ibm.websphere.dtx.dtxpi.MTrace;

final class M4PDFLogger
{
    /**
     * Writes a string to the trace file. If the MTrace Object is
     * null, no action is performed.
     * 
     * @param trace The trace object - may be null
     * @param traceMessage Th string to write
     */
    protected static void tracePrintLn(MTrace trace, String traceMessage)
    {
        try
        {
            if (trace != null)
            {
                trace.println(traceMessage);
            }
        }
        catch (Throwable e)
        {
            // Nothing can be done
        }
    }

    /**
     * Dumps an exception to the trace file. The format is:
     * 
     * Exception caught : <I>classname_of_exception</I>
     * Localized Message : <I>localized_message</I>
     * Message : <I>message</I>
     * Stack Trace: <I>full_stack_trace</I>
     * 
     * @param trace The trace object - may be null
     * @param e An Exception or Error
     */
    protected static void traceException(MTrace trace, Throwable e)
    {
        tracePrintLn(trace, "Exception caught : " + e.getClass().toString());
        tracePrintLn(trace, "Localized Message : " + e.getLocalizedMessage());
        tracePrintLn(trace, "Message : " + e.getMessage());
        tracePrintLn(trace, "Stack trace : ");
        StringWriter stackStringWriter = new StringWriter();
        PrintWriter stackPrintWriter = new PrintWriter(stackStringWriter);
        e.printStackTrace(stackPrintWriter);
        tracePrintLn(trace, stackStringWriter.toString());
    }

    /**
     * Traces the entry into a function call.
     * 
     * @param trace The trace object - may be null
     * @param functionName The name of the function
     */
    protected static void traceEntry(MTrace trace, String functionName)
    {
        try
        {
            if (trace != null)
            {
                trace.entry(functionName);
            }
        }
        catch (Throwable e)
        {
            // Nothing can be done
        }
    }
    
    /**
     * Traces exit from a function
     * 
     * @param trace The trace object - may be null
     * @param functionName The name of the function
     * @param returnCode The function's return code
     */
    protected static void traceExit(MTrace trace, String functionName, int returnCode)
    {
        try
        {
            if (trace != null)
            {
                trace.exit(functionName, returnCode);
            }
        }
        catch (Throwable e)
        {
            // Nothing can be done
        }
    }

    private M4PDFLogger()
    {
    }
}
