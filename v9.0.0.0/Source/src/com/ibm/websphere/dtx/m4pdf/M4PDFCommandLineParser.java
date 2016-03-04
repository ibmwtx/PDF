
package com.ibm.websphere.dtx.m4pdf;

import com.ibm.websphere.dtx.dtxpi.tools.commandline.*;
import com.ibm.websphere.dtx.dtxpi.*;

/**
 * This class encapsulates common adapter/importer command-line options.
 */
public class M4PDFCommandLineParser
{
	protected String iTraceFile = null;
    protected boolean isTraceAppend;
    protected boolean isTraceError;
    protected boolean isTraceVerbatim;
    
  
    protected String iURL = null;
    protected String iSchemaDocument = null;
    protected String iTemplateDocument = null;
    protected boolean iStandardEncryption = false;
    protected boolean iPublicEncryption = false;
    protected String iUserName = null;
    protected String iPassWord = null;
    protected String iCertificate = null;
    protected boolean iValidation = false;  
    protected int iPage = -1;
    protected int iEndPage = -1;
    protected boolean bAppend = false;
    
    protected MCommandLineParser m_parser = null;
    protected String m_defaultTraceFile = null;    
        

    public M4PDFCommandLineParser(String defaultTraceFile) throws MException
    {
        m_parser = new MCommandLineParser();

        // Set the m_parser up
		/*
		 * @param longForm        Long form command option alias
		 * @param shortForm       Short form command option alias
		 * @param description     Command option description
		 * @param isRequired      Controls if the option can be specified multiple times
		 * @param allowsMultiple  Controls if the option must appear at least once
		 * @param reqArgCount     Minimum number of arguments that must be specified
		 * @param optArgCount     Maximum number of optional arguments that can be specified
		 */			
		m_parser.addCommand("-URL",    		"-U",      	"URL",         		             	 false,  false,  1,  1);										
		m_parser.addCommand("-SCHEMA",   	"-S",      	"SCHEMA",         		         	 false,  false,  1,  1);
		m_parser.addCommand("-TEMPLATE",   	"-P",      	"TEMPLATE",         		     	 false,  false,  1,  1);
		m_parser.addCommand("-STANDARD",   	"-SE",  	"Standard Encryption",         		 false,  false,  0,  1);
		m_parser.addCommand("-KEY",   		"-PE",  	"Public Key Encryption",         	 false,  false,  0,  1);
		m_parser.addCommand("-USER",   		"-USER",  	"User Name",         	 			 false,  false,  1,  1);
		m_parser.addCommand("-PASSWORD", 	"-PASS",  	"Password",         	 			 false,  false,  1,  1);
		m_parser.addCommand("-CERT", 		"-CERT",  	"Certificate",         	 			 false,  false,  1,  1);
		m_parser.addCommand("-VAL", 		"-VAL",  	"PDF/A Validation",         		 false,  false,  0,  1);
		m_parser.addCommand("-PAGE", 		"-PAGE",  	"Page Text",         		 		 false,  false,  0,  1);
		m_parser.addCommand("-APPEND", 		"-APPEND",  "Append Page",         		 		 false,  false,  0,  1);
        m_parser.addCommand("-T",       	"-T",      	"Adapter Trace (overwrite)",         false,  false,  0,  1);
        m_parser.addCommand("-T+",       	"-T+",     	"Adapter Trace (append)",            false,  false,  0,  1);
        m_parser.addCommand("-TE",      	"-TE",    	"Adapter Trace Error(overwrite)",    false,  false,  0,  1);
        m_parser.addCommand("-TE+",     	"-TE+",   	"Adapter Trace Error (append)",      false,  false,  0,  1);
        m_parser.addCommand("-TV",          "-TV",     	"Adapter Trace Verbatim(overwrite)", false,  false,  0,  1);
        m_parser.addCommand("-TV+",         "-TV+",     "Adapter Trace Verbatim (append)",   false,  false,  0,  1);

        m_defaultTraceFile = defaultTraceFile;

        // Calls the internal version to make sure the overriden forms are not called at this point.
        // It is the responsibility of the subclass constructor to initialize its own elements.
        // This method is called before any other in the subclass constructor.
        setDefaultValuesInternal();
    }

    private void setDefaultValuesInternal()
    {
        iTraceFile = null;
        isTraceAppend = false;
        isTraceError = false;
		
		
	
    }

    protected void setDefaultValues()
    {
        setDefaultValuesInternal();
    }

    /**
     * Parses the given command-line string.
     *
     * @param cmdLine Command-line string.
     * @exception MException
     */
    public void parseCommandLine(String cmdLine,int iCommonTraceSwitch, String traceFileName) throws MException
    {
        setDefaultValues();
        m_parser.parseCommandLine(cmdLine);
        validateCommandOptionCombinations();
        populateFields(iCommonTraceSwitch,traceFileName);
    }

    /**
     * Validates trace options. It prevents both -T and -T+ to
     * be specified at the same time.
     *
     * @throws MException
     */
    protected void validateTraceOptions() throws MException
    {
        MCommandOption trace = m_parser.getCommandOption("-T");
        MCommandOption traceAppend = m_parser.getCommandOption("-T+");
        if( trace != null && traceAppend != null )
            throw new M4PDFException(-1);

        MCommandOption traceError = m_parser.getCommandOption("-TE");
        MCommandOption traceErrorAppend = m_parser.getCommandOption("-TE+");
        if( traceError != null && traceErrorAppend != null )
        {
            throw new M4PDFException(-1);
        }
        
        MCommandOption traceVerbatimError = m_parser.getCommandOption("-TV");
        MCommandOption traceVerbatimAppend = m_parser.getCommandOption("-TV+");
        if( traceVerbatimError != null && traceVerbatimAppend != null )
        {
            throw new M4PDFException(-1);
        }

        if( (trace != null || traceAppend != null) && (traceError != null || traceErrorAppend != null) )
        {
            throw new M4PDFException(-1);
        }
    }

    protected void validateCommandOptionCombinations() throws MException
    {
        validateTraceOptions();        
    }

    /**
     * Sets traceFile and isTraceAppend fields based on the command line.
     *
     * @throws MException
     */
    protected void populateTraceOptions(int iCommonTraceSwitch, String traceFileName) throws MException
    {
        // Get the trace file
        MCommandOption traceOption = m_parser.getCommandOption("-T");
        
        isTraceVerbatim = false;

        if( traceOption == null )
        {
            // try trace append
            traceOption = m_parser.getCommandOption("-T+");
            if( traceOption != null )
            {
                iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
                isTraceAppend = true;
            }
        }
        else
        {
            iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
            isTraceAppend = false;
        }

        traceOption = m_parser.getCommandOption("-TE");
        if( traceOption == null )
        {
            // try trace append
            traceOption = m_parser.getCommandOption("-TE+");
            if( traceOption != null )
            {
                iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
                isTraceAppend = true;
                isTraceError = true;
            }
            else
            {
                traceOption = m_parser.getCommandOption("-TV");
                if( traceOption == null )
                {
                    // try trace append
                    traceOption = m_parser.getCommandOption("-TV+");
                    if( traceOption != null )
                    {
                        iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
                        isTraceAppend = true;
                        isTraceError = false;
                        isTraceVerbatim = true;
                    }
                    else
                    {
                    	 iTraceFile = null;
                         isTraceAppend = false;
                         isTraceError = false;
                         isTraceVerbatim = true;
                    }
                }
                else
                {
                	 iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
                     isTraceAppend = false;
                     isTraceVerbatim = true;
                     isTraceError = false;
                }
            }
        }
        else
        {
            iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
            isTraceAppend = false;
            isTraceError = true;
        }
        
        

        if( iCommonTraceSwitch == MConstants.MPI_TRACE_SWITCH_ON )
        {
            iTraceFile = traceFileName;
            isTraceAppend = true;
            isTraceError = false;
        }
    }

    /**
     * Populates command option fields.
     *
     * @exception MException
     */
    protected void populateFields(int iCommonTraceSwitch, String traceFileName) throws MException
    {
        MCommandOption opt = null;
		
        opt = m_parser.getCommandOption("-SCHEMA");
       
        if( opt != null )
		{
        	iSchemaDocument = opt.getArgument(0, null);
		}
        else
		{
			opt = m_parser.getCommandOption("-S");
			
			if( opt != null )
			{
				iSchemaDocument = opt.getArgument(0, null);
			}
		}
        
        opt = m_parser.getCommandOption("-URL");
        if( opt != null )
		{
            iURL = opt.getArgument(0, null);
		}
        else
		{
			opt = m_parser.getCommandOption("-U");
			
			if( opt != null )
			{
				iURL = opt.getArgument(0, null);
			}
		}
        
        
        opt = m_parser.getCommandOption("-TEMPLATE");
        if( opt != null )
		{
        	iTemplateDocument = opt.getArgument(0, null);
		}
        else
		{
			opt = m_parser.getCommandOption("-P");
			
			if( opt != null )
			{
				iTemplateDocument = opt.getArgument(0, null);
			}
		}
        
        opt = m_parser.getCommandOption("-PAGE");
       
        if( opt != null )
		{
        	String range = opt.getArgument(0, null);
        	
        	if (range.indexOf('-') != -1)
        	{
        		String[] ranges = range.split("-");
        		iPage = new Integer(ranges[0]).intValue();
        		iEndPage = new Integer(ranges[1]).intValue();;
        	}
        	else
        	{        	
        		iPage = new Integer(range).intValue();
        		iEndPage = iPage;
        	}
		}
        
        opt = m_parser.getCommandOption("-APPEND");
        
        if( opt != null )
		{
        	bAppend = true;
		}
      
      
        
                                     
       	// Get trace
        populateTraceOptions(iCommonTraceSwitch,traceFileName);
    }

    /**
     * Dumps the options found on the command line to the specified trace file.
     *
     * @param trace Trace file object.
     */
    public void dumpCommandOptions(MTrace trace)
    {
        if( trace == null )
            return;

        try
        {
            if( !isTraceError )
            {
                trace.println(" Trace file      | " + iTraceFile);
                trace.println(" Trace append    | " + isTraceAppend);
                trace.println(" Trace error     | " + isTraceError);
            }		
            
            trace.println(" Template file      | " + iTemplateDocument);
            trace.println(" Schema Document    | " + iSchemaDocument);
            trace.println(" URL   			   | " + iURL);
            trace.println(" Page			   | " + iPage);
        }
        catch( MException e )
        {
            // ignore, this is just a trace, there is no corrective action
        }
    }
}
