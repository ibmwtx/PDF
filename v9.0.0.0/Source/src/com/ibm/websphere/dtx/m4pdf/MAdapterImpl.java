
package com.ibm.websphere.dtx.m4pdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioCollection;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;
import org.apache.pdfbox.util.PDFTextStripper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.websphere.dtx.dtxpi.MAdapter;
import com.ibm.websphere.dtx.dtxpi.MBase;
import com.ibm.websphere.dtx.dtxpi.MCard;
import com.ibm.websphere.dtx.dtxpi.MConnection;
import com.ibm.websphere.dtx.dtxpi.MConstants;
import com.ibm.websphere.dtx.dtxpi.MException;
import com.ibm.websphere.dtx.dtxpi.MMap;
import com.ibm.websphere.dtx.dtxpi.MStream;
import com.ibm.websphere.dtx.dtxpi.MTrace;
import com.ibm.websphere.dtx.dtxpi.MUnexpectedException;
import com.ibm.websphere.dtx.dtxpi.MXDSAdapter;



public class MAdapterImpl extends MXDSAdapter
{
    private int m_mapInstance = 0;
    private MTrace  m_trace;               		// Trace
    protected static HashMap s_errLookup;       // Error message lookup    
 
    private int m_iCommonTraceSwitch = 0;       // Common trace value
    private M4PDFCommandLineParser m_cmdLine;    // Adapter command line parse object.	    
    private String iURL = null;   
    private String iSchemaDocument = null;
    private String iTemplateDocument = null;
    private boolean isTraceError = false;
    private boolean _traceUsedInConnection = false;
    private boolean bAppend = false;
    private boolean bAddPage = false;
	
    private int elementStatus = 0;
    private int iPage = -1;
    private int iEndPage = -1;
	
	protected StringBuilder iStringBuilder = null;
	public static String SCHEMA_LANGUAGE =
	      "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
	                        XML_SCHEMA =
	      "http://www.w3.org/2001/XMLSchema",
	                        SCHEMA_SOURCE =
	      "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	PDField iField = null;
	
    // Construction
    protected MAdapterImpl ( MCard card,
                             long lReserved ) throws Exception
    {
        super( lReserved );

        newPropertySet( MConstants.MPI_PROPBASE_USER,
                        M4PDFConstants.M4STERLING_EDH_ADAPTER_PROPS.length,
                        M4PDFConstants.M4STERLING_EDH_ADAPTER_PROPS );
        setIntegerProperty( MConstants.MPIP_ADAPTER_LISTEN_USESAMECONN, 0, 1 );
        setDefaultProperties();
    }

    // Dummy Constructor used for testing
    public MAdapterImpl() throws Exception
    {
        super( 0 );
    }
	
	static
    {
        try
        {
            // initialize error lookup table
            s_errLookup = new HashMap();
            
        }
        catch( Exception e )
        {
        }
    }
	
	private void setDefaultProperties()
	{
	}
	
	protected MTrace getTrace()
    {
        return m_trace;
    }

    protected int getMapInstance() throws MException
    {
        if( m_mapInstance == 0 )
        {
            m_mapInstance = getCard().getMap().getIntegerProperty( MConstants.MPIP_MAP_INSTANCE, 0 );
        }
        return m_mapInstance;
    }
	
	public int compareWatches( MAdapter adapter ) throws Exception
    {
        return MConstants.MPIRC_E_ILLEGAL_CALL;
    }
	
	public int compareResources( MAdapter adapter ) throws Exception
    {
        return MConstants.MPI_CMP_DIFFER;
    }
	
	public void endTransaction( MConnection connection, 
                                int iTransAction ) throws Exception
    {
        M4PDFLogger.traceEntry( getTrace(), "endTransaction" );
		
		if( iTransAction == MConstants.MPI_COMMIT )
		{
			M4PDFLogger.tracePrintLn(getTrace(), "COMMIT");
					
		}
		else
		{
		    M4PDFLogger.tracePrintLn(getTrace(), "ROLLBACK");			
		}

        M4PDFLogger.traceExit( getTrace(), "endTransaction", 0 );
    }

    public void beginTransaction( MConnection connection ) throws Exception
    {
        M4PDFLogger.traceEntry( getTrace(), "beginTransaction" );

        int iOnFailure = getIntegerProperty( MConstants.MPIP_ADAPTER_ON_FAILURE, 0 );

        if( iOnFailure == MConstants.MPI_ACTION_ROLLBACK )
        {
            M4PDFLogger.tracePrintLn( getTrace(), "Starting transaction" );
        }
        else
        {
            M4PDFLogger.tracePrintLn( getTrace(), "Transactions disabled" );
        }
        M4PDFLogger.traceExit( getTrace(), "beginTransaction", 0 );
    }

    public int compareConnection( MConnection connection ) throws Exception
    {
        return MConstants.MPI_CMP_DIFFER;
    }
	
	public int listen( MConnection connection ) throws Exception
    {
        // Listener not supported for this adapter
        M4PDFLogger.traceEntry( getTrace(), "listen" );
        M4PDFLogger.tracePrintLn( getTrace(), "Listening for events not supported" );
        M4PDFLogger.traceExit( getTrace(), "validateConnection", MConstants.MPIRC_E_ILLEGAL_CALL );
        return MConstants.MPIRC_E_ILLEGAL_CALL;
    }
	
	public int validateConnection( MConnection connection ) throws Exception
    {
        return MConstants.MPIRC_SUCCESS;
    }
	
	public void validateProperties() throws Exception
    {
        try
        {
            // Get the command-line string
            String cmdLineString = getTextProperty(MConstants.MPIP_ADAPTER_COMMANDLINE, 0);
            String traceFileName = null;

            // Get common trace values
            MCard card = getCard();
            MMap map = card.getMap();
            m_iCommonTraceSwitch = map.getIntegerProperty(MConstants.MPIP_MAP_COMMON_TRACE_SWITCH, 0);

            if( m_iCommonTraceSwitch == MConstants.MPI_TRACE_SWITCH_ON )
            {
                traceFileName = map.getTextProperty(MConstants.MPIP_MAP_COMMON_TRACE_FILE, 0);
            }
            // Init command line parser
            m_cmdLine = new M4PDFCommandLineParser("M4PDF.mtr");

            // Parse the command line
            try
            {
                m_cmdLine.parseCommandLine(cmdLineString, m_iCommonTraceSwitch, traceFileName);
            }
            catch( Exception e )
            {
            }
						
			
			isTraceError = m_cmdLine.isTraceError;
			iURL = m_cmdLine.iURL;
			iSchemaDocument = m_cmdLine.iSchemaDocument;
			iTemplateDocument = m_cmdLine.iTemplateDocument;
			bAppend = m_cmdLine.bAppend;			
			iPage = m_cmdLine.iPage;
			iEndPage = m_cmdLine.iEndPage;
			
			int adapterContext = getIntegerProperty(MConstants.MPIP_ADAPTER_CONTEXT, 0);
	        
	    	if (adapterContext == MConstants.MPI_CONTEXT_SOURCE || 
	    		adapterContext == MConstants.MPI_CONTEXT_GET)
	    	{			
	    		if (iSchemaDocument == null && iPage < 0) {
	    			throw new MException(MConstants.MPIRC_E_NULL_ARGUMENT, "Schema Document name must be specified");
	    		}
	    	}
	    	else if (adapterContext == MConstants.MPI_CONTEXT_TARGET || 
	    			adapterContext == MConstants.MPI_CONTEXT_PUT)
	    	{
	    		if (iSchemaDocument == null && iTemplateDocument == null)
	    			bAddPage = true;
	    	}
		
								
            traceFileName = m_cmdLine.iTraceFile;

            // Initialize trace
            if( traceFileName != null )
            {
                String expandedPath = expandPathToMapDir(traceFileName);
                if( m_iCommonTraceSwitch != MConstants.MPI_TRACE_SWITCH_OFF )
                {
                    m_trace = new MTrace(expandedPath,
                                         m_cmdLine.isTraceAppend ? MConstants.LOG_MODE_APPEND : MConstants.LOG_MODE_DEFAULT,
                                         m_cmdLine.isTraceVerbatim ? MConstants.LOG_LEVEL_MAX : MConstants.LOG_LEVEL_DEFAULT);
                }
            }
            else
                m_trace = null;

            m_cmdLine.dumpCommandOptions(m_trace);                       
        }
        catch( MException e )
        {
            reportException( e );
            return;
        }
        catch( Exception e )
        {
        	reportException( e );
        	return;            
        }
    }
	
	 public void SetTraceUsedInConnection()
     {
        _traceUsedInConnection = true;
     }
	
	public void onNotify( int iID, 
                          int iParam, 
                          MBase Param ) throws Exception
    {
		int rc = MConstants.MPIRC_SUCCESS;
        if((m_iCommonTraceSwitch != MConstants.MPI_TRACE_SWITCH_ON) || (iID != MConstants.MPIN_OBJECT_PREPARE_DESTROY))

		{
			if(!isTraceError)
				TRACE(getTrace(), "onNotify");
		}
        
        switch (iID)
        {
        case (MConstants.MPIN_ADAPTER_GETSTART):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for GETSTART");
                break;
            }
        case (MConstants.MPIN_ADAPTER_PUTSTART):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for PUTSTART");
                break;
            }
        case (MConstants.MPIN_ADAPTER_GETSTOP):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for GETSTOP");
                break;
            }
        case (MConstants.MPIN_ADAPTER_PUTSTOP):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for PUTSTOP");
                break;
            }
        case (MConstants.MPIN_OBJECT_PREPARE_DESTROY):
            {
				if(!isTraceError && (m_iCommonTraceSwitch != MConstants.MPI_TRACE_SWITCH_ON))
					TRACE(getTrace(), "OnNotify called for PREPARE_DESTROY");
                break;
            }
        case (MConstants.MPIN_ADAPTER_MAPABORT):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for PREPARE_MAPABORT");
                break;
            }
        }
        if (iID == MConstants.MPIN_ADAPTER_GETSTOP ||
            iID == MConstants.MPIN_ADAPTER_PUTSTOP)
        {
            // Close the trace
            if (getTrace() != null)
            {
                if (!_traceUsedInConnection)
                {
                    // This trace will not be used any more and
                    // can be closed
                    if(!isTraceError)
                    {
                    	TRACE(getTrace(), "Closing trace file and exiting function");                    	
					}
                    getTrace().close();
					getTrace().finalize();					
                }
                else
                {
                    // The trace will be closed on disconnect
                    if(!isTraceError)
                    	traceExit(getTrace(), "onNotify", rc);
                }
            }
        }
        else
        {
			if((m_iCommonTraceSwitch != MConstants.MPI_TRACE_SWITCH_ON) || (iID != MConstants.MPIN_OBJECT_PREPARE_DESTROY))
			{
				if(!isTraceError)
					traceExit(getTrace(), "onNotify", rc);
			}

        }
	}
	

    static void traceExit(MTrace trace, String functionName, int returnCode)
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
		
	private boolean isRelative(String path) {
		
		if (path != null)
		{
			if (path.indexOf("http:") == -1 || path.indexOf("file:") == -1) {
				return true;
			}	
		}
		
		return false;
	}
	
	public byte[] ExtractText() throws Exception
	{	
		if (iURL != null)
		{
			URL u = null;
			
			if (isRelative(iURL))
			{
				u = new URL("file:///" + expandPathToMapDir(iURL));
			}
			else
			{
				u = new URL(iURL);
			}
			
			if (isLocalFile(u))
			{
				 InputStream file = new FileInputStream(u.getPath());			     			
			     return extractText(file);
			}
			else
			{			
				URLConnection urlCon= u.openConnection();
				urlCon.setDoOutput(false);
				urlCon.setDoInput(true);
				urlCon.connect();
				 
				InputStream fin = urlCon.getInputStream();																	
				return extractText(fin);
			}
		}
		else
		{
			 // Open the streams
            MStream in = getInputStream();              

            // Read the input stream
            long sizeInput = in.getSize();
            byte[] data = new byte[(int)sizeInput];                  
            in.read(data);                      
			return extractText(new ByteArrayInputStream(data));
		}
	}
	
	public byte[] ReadPage() throws Exception
	{	
		if (iURL != null)
		{
			URL u = null;
			
			if (isRelative(iURL))
			{
				u = new URL("file:///" + expandPathToMapDir(iURL));
			}
			else
			{
				u = new URL(iURL);
			}
			
			if (isLocalFile(u))
			{
				 InputStream file = new FileInputStream(u.getPath());			     			
			     return ReadPage(file);
			}
			else
			{			
				URLConnection urlCon= u.openConnection();
				urlCon.setDoOutput(false);
				urlCon.setDoInput(true);
				urlCon.connect();
				 
				InputStream fin = urlCon.getInputStream();																	
				return ReadPage(fin);
			}
		}
		else
		{
			 // Open the streams
            MStream in = getInputStream();              

            // Read the input stream
            long sizeInput = in.getSize();
            byte[] data = new byte[(int)sizeInput];                  
            in.read(data);                      
			return ReadPage(new ByteArrayInputStream(data));
		}
	}
		
	public byte[] ReadPage(InputStream obj) throws Exception
	{						 		
		   PDFParser parser = new PDFParser(obj); 
		   
		   parser.parse();
		   COSDocument cosDoc = parser.getDocument();
		   PDFTextStripper pdfStripper = new PDFTextStripper();
		   PDDocument pdDoc = new PDDocument(cosDoc);
	       pdDoc.getNumberOfPages();
	       pdfStripper.setStartPage(iPage);
	       pdfStripper.setEndPage(iEndPage);
	       
	       String text = pdfStripper.getText(pdDoc);
	       return text.getBytes();
		
	}
		
	
	/* convert Java Object to XML String */
	public byte[] extractText(InputStream obj) throws Exception
	{		
		StringBuilder stringBuilder = new StringBuilder();
			
		stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><wtxpdf:Document  xmlns:wtxpdf=\"http://www.ibm.com/wtx/PDF\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.ibm.com/wtx/PDF ");
		stringBuilder.append(iSchemaDocument + "\">");		
		
		stringBuilder.append("<Properties>");
		
		try
		{
			PDDocument pdfDocument = PDDocument.load(obj);			
		 	PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
		 	
		 	PDDocumentInformation properties = pdfDocument.getDocumentInformation();		 			
			
			stringBuilder.append("<PageCount>");
			stringBuilder.append(pdfDocument.getNumberOfPages());
			stringBuilder.append("</PageCount>");
			
			String property = properties.getSubject();
			
			if (property != null && property.length() > 0)
			{		
				stringBuilder.append("<Subject>");
				stringBuilder.append(property);
				stringBuilder.append("</Subject>");
			}
			
			property = properties.getProducer();
			
			if (property != null && property.length() > 0)
			{		
				stringBuilder.append("<Producer>");
				stringBuilder.append(property);
				stringBuilder.append("</Producer>");
			}
			
			property = properties.getCreator();
			
			if (property != null && property.length() > 0)
			{		
				stringBuilder.append("<Creator>");
				stringBuilder.append(property);
				stringBuilder.append("</Creator>");
			}
			
			Calendar propertyC = properties.getCreationDate();
			
			if (propertyC != null)
			{		
				stringBuilder.append("<CreationDate>");
				stringBuilder.append(""); // TO DO
				stringBuilder.append("</CreationDate>");
			}
			
			property = properties.getTitle();
			
			if (property != null && property.length() > 0)
			{		
				stringBuilder.append("<Title>");
				stringBuilder.append(property);
				stringBuilder.append("</Title>");
			}		
			
			stringBuilder.append("</Properties>");
		
			PDAcroForm acroForm = docCatalog.getAcroForm();	        
	        startModel(stringBuilder, acroForm);	
	        
	        stringBuilder.append("</wtxpdf:Document>");
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}				
		
		System.out.println(stringBuilder.toString());
		
		return stringBuilder.toString().getBytes();
	}
	
	private String normalizeName(String name) {
		
		if (name != null && name.length() > 0)
		{
			if (Character.isDigit(name.charAt(0)))			
				name = "T_" + name;
	
			name = name.replace("&", "");
			name = name.replace(",", "");
			name = name.replace(' ', '_');
			
			if (name.length() > 30)
			{
				String first = name.substring(0, 15);
				String last = name.substring(name.length() - 15);			
				name = first + last;	
			}
			
			if (NameFactory.getItsInstance().hasString(name))
			{			
				name = name + "_" + NameFactory.getItsInstance().getUniqueInstance();
			}
		}
		
		return name;
	}

	private void startModel(StringBuilder stringBuilder, PDAcroForm acroForm) throws IOException
	{	 
	    List<PDField> fields = acroForm.getFields();        
         
        for (int i = 0; i < fields.size(); i++) 
        {
        	PDField field = fields.get(i);  
        	
        	buildValue(stringBuilder, field);        	        	        	      
        }                          	       
	}
	
	private void buildKids(PDField field, StringBuilder stringBuilder) throws IOException 
	{
		String strName = null;
		
		List kids = field.getKids();		
    	
    	if (kids != null)
    	{
    		int size = kids.size();
    		
    		for (int i = 0; i < size; i++)
    		{
    			PDField firstKid = (PDField) kids.get(i);
    			    	
    			if (firstKid instanceof PDRadioCollection)   
    			{    
    				strName = normalizeName(firstKid.getPartialName());
    				stringBuilder.append("<" + strName + ">" );    	
    				buildKids(firstKid, stringBuilder);    				
    				stringBuilder.append("</" + strName + ">" );
    			}   		
    			else if (firstKid instanceof PDCheckbox)    
    			{    				
    				if ((((PDCheckbox)firstKid).isChecked()))
    				{
    					String str = normalizeName(firstKid.getPartialName());
    		    		
    		    		if (str == null)
    		    			str = normalizeName((((PDCheckbox)firstKid).getOnValue()));
            		
    					stringBuilder.append("<" + str + ">YES"  +  "</" + str + ">");
    				}
    			}    			   			
    			else if (firstKid instanceof PDSignature)
    			{
    				if (firstKid.getValue() != null)
    				{
    					strName = normalizeName(firstKid.getPartialName());    				    				    			
    					stringBuilder.append("<" + strName + ">" + firstKid.getValue() +  "</" + strName + ">");
    				}
    			}    				
    			else if (firstKid instanceof PDSignatureField)
    			{
    				if (firstKid.getValue() != null)
    				{
    				
    					strName = normalizeName(firstKid.getPartialName());
    					stringBuilder.append("<" + strName + ">" + firstKid.getValue() +  "</" + strName + ">");
    				}
    			}    				
    			else if (firstKid instanceof PDVariableText)
    			{
    				if (firstKid.getValue() != null)
    				{    				
    					strName = normalizeName(firstKid.getPartialName());
    					stringBuilder.append("<" + strName + ">" + firstKid.getValue() +  "</" + strName + ">");
    				}
    			}    				    			    			
    		}
    	}
	}

	

	private void buildValue(StringBuilder stringBuilder, PDField field) throws IOException {
		
		String strName = null;
		
		if (field instanceof PDCheckbox)  
    	{        					
			if ((((PDCheckbox)field).isChecked()))
			{
	    		String str = normalizeName(field.getPartialName());
	    		
	    		if (str == null)
	    			str = normalizeName((((PDCheckbox)field).getOnValue()));
	    		
	    		stringBuilder.append("<" + str + ">YES"  +  "</" + str + ">");
			}
    	}
    	else if (field instanceof PDRadioCollection)
    	{
    		strName =  normalizeName(field.getPartialName());
    		
    		stringBuilder.append("<" + strName + ">" );
    		buildKids(field, stringBuilder);    				
			stringBuilder.append("</" + strName + ">" );
    	}
		else if (field instanceof PDSignature)
		{
			if (field.getValue() != null)
			{
				strName =  normalizeName(field.getPartialName());
				stringBuilder.append("<" + strName + ">" + field.getValue() +  "</" + strName + ">");				
			}
		}
		else if (field instanceof PDSignatureField)
		{
			if (field.getValue() != null)
			{
				strName =  normalizeName(field.getPartialName());
				stringBuilder.append("<" + strName + ">" + field.getValue() +  "</" + strName+ ">");
			}
		}
		else if (field instanceof PDVariableText)
		{
			if (field.getValue() != null)
			{
				strName =  normalizeName(field.getPartialName());
				stringBuilder.append("<" + strName + ">" + field.getValue() +  "</" + strName + ">");
			}
		}		
	}

	public int get( MConnection c ) throws Exception
    {
        M4PDFLogger.traceEntry( getTrace(), "get" );
      
        int rc = MConstants.MPIRC_SUCCESS;

        try
        {        
	    	int adapterContext = getIntegerProperty(MConstants.MPIP_ADAPTER_CONTEXT, 0);
	        
	    	if (adapterContext == MConstants.MPI_CONTEXT_SOURCE_EVENT)
	        {
	        	 M4PDFLogger.tracePrintLn(getTrace(), "PDF adapter cannot be used as source event on a input card");
	             rc = MConstants.MPIRC_E_ILLEGAL_CALL;
	        }
	    	
	    	if (iPage >= 0)
	    	{
	    		byte[] outdata = ReadPage();				
				
				if (outdata == null)
					throw new M4PDFException(MConstants.MPIRC_E_FAILED, "ERROR: Failed marshalling java object");
							
				
				if( outdata != null && outdata.length > 0 )
				{
					MStream out = getOutputStream();
					out.setSize(0);
					out.write(outdata);
					out.flush();
				}		
				else
				{
					MStream out = getOutputStream();
					out.setSize(0);								
				}
	
	            setIntegerProperty( MConstants.MPIP_ADAPTER_NUM_MESSAGES, 0, 1 );	    		
	    	}
	    	else
	    	{                          
				byte[] outdata = ExtractText();				
									
				if (outdata == null)
					throw new M4PDFException(MConstants.MPIRC_E_FAILED, "ERROR: Failed marshalling java object");
							
				
				if( outdata != null && outdata.length > 0 )
				{
					MStream out = getOutputStream();
					out.setSize(0);
					out.write(outdata);
					out.flush();
				}		
				else
				{
					MStream out = getOutputStream();
					out.setSize(0);								
				}
	
	            setIntegerProperty( MConstants.MPIP_ADAPTER_NUM_MESSAGES, 0, 1 );
	    	}
        }             
        
        catch( Exception e )
        {
            reportException(c, new M4PDFException(MConstants.MPIRC_E_EXCEPTION, e.getMessage()));
            return MConstants.MPIRC_E_EXCEPTION;
        }

        M4PDFLogger.traceExit( getTrace(), "get", rc );

        return rc;
    }
	
	public void put( MConnection conn ) throws Exception
    {
        M4PDFLogger.traceEntry( getTrace(), "put" );
		
		int rc = MConstants.MPIRC_SUCCESS;
				
		
		try
		{
		    // Open the streams
            MStream in = getInputStream();

            // Read the input stream
            long sizeInput = in.getSize();
            byte[] data = new byte[(int)sizeInput];
            
            in.read(data);
            
            if (bAddPage)
            {
            	writePage(data);
            }
            else
            {
            	writeDocument(data);
            }
						
		}
				
		
        catch( MException e )
        {
            reportException(conn, e);
            return;
        }
        
        catch( Exception e )
        {
            reportException(conn, e);
            return;
        }

        M4PDFLogger.traceExit( getTrace(), "put", rc );
	}
	
	private void writePage(byte[] data) throws Exception
	{							
			if (iURL != null)
			{					
				String outputDocument = iURL ;
				
				if (isRelative(iURL))
				{
					outputDocument  = expandPathToMapDir(iURL);
										
				}
				
				if (!new File(outputDocument).exists())
					bAppend = false;
				
				if (bAppend)
				{
					
					PDDocument doc = PDDocument.load(new File(outputDocument));	
					PDPage page = new PDPage();
					 
			        doc.addPage(page);
			 
			        PDPageContentStream content = new PDPageContentStream(doc, page);
			        
			        content.beginText();			      
			        content.drawString(new String(data));
			        content.endText();			        			        			       			        			        			      			     
			        
			        content.close();
			        doc.save(outputDocument);
			        doc.close();	
				}
				else
				{
				
					PDDocument doc = new PDDocument();
			        PDPage page = new PDPage();
			        			        
			 
			        doc.addPage(page);
			 
			        PDPageContentStream content = new PDPageContentStream(doc, page);			        			     
			        content.beginText();				      					        			      
			        content.drawString(new String(data));
			        content.endText();			        			        			       			        			        			      			     
			        
			        content.close();
			        doc.save(outputDocument);
			        doc.close();	
				}
			}									
	}

  
	private void writeDocument(byte[] data) {
		
		elementStatus = -1;
	
		
		try
		{
			if (iURL != null)
			{
				
				String outputDocument = iTemplateDocument;
				
				if (isRelative(iTemplateDocument))
				{
					outputDocument  = expandPathToMapDir(iTemplateDocument);
										
				}
				
				PDDocument pdfDocument = PDDocument.load(new File(outputDocument));	            				
				
				SAXParserFactory factory = SAXParserFactory.newInstance();	
				
		        
				factory.setValidating(true);				
				SAXParser saxParser = factory.newSAXParser();				
				saxParser.setProperty(SCHEMA_LANGUAGE,XML_SCHEMA);
				saxParser.setProperty(SCHEMA_SOURCE, iSchemaDocument);
				
						
			 	PDDocumentCatalog docCatalog = pdfDocument.getDocumentCatalog();
		        final PDAcroForm acroForm = docCatalog.getAcroForm();	   
				
		        final PDDocumentInformation properties = pdfDocument.getDocumentInformation();													
				
				DefaultHandler defaultHandler = new DefaultHandler()
				{ 						
					
						public void startElement(String uri, 
												 String localName,
												 String qName,  
												 Attributes attributes) throws SAXException {  							
							
							if (qName.compareToIgnoreCase("Created") == 0)
							{
								elementStatus = M4PDFConstants.M4PDF_ELEMENT_CREATED;
							}
							else if (qName.compareToIgnoreCase("Creator") == 0)
							{
								elementStatus = M4PDFConstants.M4PDF_ELEMENT_CREATOR;
							}
							else if (qName.compareToIgnoreCase("Description") == 0)
							{
								elementStatus = M4PDFConstants.M4PDF_ELEMENT_CREATED;
							}
							else if (qName.compareToIgnoreCase("Producer") == 0)
							{
								elementStatus = M4PDFConstants.M4PDF_ELEMENT_PRODUCER;
							}
							else if (qName.compareToIgnoreCase("Subject") == 0)
							{
								elementStatus = M4PDFConstants.M4PDF_ELEMENT_SUBJECT;
							}
							else if (qName.compareToIgnoreCase("Title")  == 0)
							{
								elementStatus = M4PDFConstants.M4PDF_ELEMENT_TITLE;
							}	
							else if (qName.compareToIgnoreCase("Document")  == 0)
							{
								elementStatus = M4PDFConstants.M4PDF_ELEMENT_ROOT;
							}
							else
							{
								int size = attributes.getLength();
								
								if (size > 0)
								{
									
									for (int i = 0; i < attributes.getLength(); i++) {
									    String attributeName = attributes.getLocalName(i);
									    String attributeValue = attributes.getValue(i);
									    System.out.println("found attribute with localname=" + attributeName 
									    + " and value=" + attributeValue);
									}
									
									String fieldname = attributes.getValue(0);
									
									if (fieldname != null)
									{
										try {
											iField = acroForm.getField( fieldname );
										} catch (IOException e) {
											// ljnbvTODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
								else
								{
									try {
										iField = acroForm.getField( qName );
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}																							
							}	
						}
						
						public void characters(char ch[], int start, int length)  
					      		throws SAXException {  																																								
							if (iStringBuilder == null)
								iStringBuilder = new StringBuilder();
							
							iStringBuilder.append(new String(ch, start, length));																				
						}
						 
						public void endElement(String uri, String localName, String qName)  
					      throws SAXException {  
							
							elementStatus = -1;
							
							if (elementStatus == M4PDFConstants.M4PDF_ELEMENT_CREATED)
							{
								//properties.setCreationDate(
							}
							else if (elementStatus == M4PDFConstants.M4PDF_ELEMENT_CREATOR)
							{
								properties.setCreator(iStringBuilder.toString());
							}																			
							else if (elementStatus == M4PDFConstants.M4PDF_ELEMENT_SUBJECT)
							{
								properties.setSubject(iStringBuilder.toString());
							}
							else if (elementStatus == M4PDFConstants.M4PDF_ELEMENT_TITLE)
							{
								properties.setTitle(iStringBuilder.toString());
							}
							else if (elementStatus == M4PDFConstants.M4PDF_ELEMENT_PRODUCER)
							{
								properties.setProducer(iStringBuilder.toString());
							}
							else							
							{
								if (iField != null)
								{
									try
									{
										iField.setValue(iStringBuilder.toString());
										iField.setReadonly(true);
									}
									
									catch (Exception e)
									{
										e.printStackTrace();
									}
									
									
								}
							}
							
							iStringBuilder = null;
							iField = null;
						}
					
				};
				
				saxParser.parse(new ByteArrayInputStream(data), defaultHandler);
				
		         
				
				if (isRelative(iURL))
				{
					String source = expandPathToMapDir(iURL);					
					pdfDocument.save(source);										
					
				}
				else
				{	
					pdfDocument.save(iURL);										
					
				}
			}									
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private	 boolean isLocalFile(java.net.URL url) {
	  String scheme = url.getProtocol();
	  return "file".equalsIgnoreCase(scheme) && !hasHost(url);
  	}	
	

	private boolean hasHost(java.net.URL url) {
    	String host = url.getHost();
    	return host != null && !"".equals(host);
    }
	
	
	// All Exception Handling from here on
	private void reportException( MConnection c, 
                                  MException e ) throws MException
    {
        M4PDFLogger.traceException( getTrace(), e );
        M4PDFLogger.tracePrintLn( m_trace, "Error code: " + e.getRC() + " , Error message: " + e.getMessage() );
        setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, e.getRC() );
        setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
        if( c != null )
        {
            c.setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, e.getRC() );
            c.setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
        }
    }

	
    private void reportException( MConnection c, 
                                  Exception e ) throws MException
    {
        M4PDFLogger.traceException( getTrace(), e );
        M4PDFLogger.tracePrintLn( m_trace, "Error code: " + MConstants.MPIRC_E_EXCEPTION + " , Error message: " + e.getMessage() );
        setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, MConstants.MPIRC_E_EXCEPTION );
        setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
        if( c != null )
        {
            c.setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, MConstants.MPIRC_E_EXCEPTION );
            c.setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
        }
    }

    private void reportException( MException e ) throws MException
    {
        M4PDFLogger.traceException( getTrace(), e );
        M4PDFLogger.tracePrintLn( m_trace, "Error code: " + e.getRC() + " , Error message: " + e.getMessage() );
        setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, e.getRC() );
        setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
    }
    
    private void reportException( Exception e ) throws MException
    {
        M4PDFLogger.traceException( getTrace(), e );       
        setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0,  MConstants.MPIRC_E_EXCEPTION);
        setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
    }

    protected String expandPathToMapDir(String path) throws MException, IOException
    {
        File pathObj = new File(path);

        // Check if the given path is already absolute
        if( pathObj.isAbsolute() )
        {
            return pathObj.getCanonicalPath();
        }
        else
        {
            // The given path is relative, get the map path
            String mapPath = getMapDirectory();

            // Use the map path as the current working directory
            return new File(mapPath, path).getCanonicalPath();
        }
    }

    protected String getMapDirectory() throws MException
    {
        MCard card = getCard();
        MMap map = card.getMap();
        String mapFilePath = map.getTextProperty(MConstants.MPIP_MAP_MAP_NAME, 0);
        File mapFilePathObj = new File(mapFilePath);
        return mapFilePathObj.getParent();
    }

    static void TRACE(MTrace trace, String text)
    {
        try
        {
            if( trace != null )
                trace.println(text);
        }
        catch( MException e )
        {
            // Ignore this exception, there is no corrective action that can be taken.
            // The trace information will be lost, but the program execution will not
            // be affected.
        }
    }

    protected MException handleAndConvertException(Exception e)
    {
        printStackTrace(m_trace, e);
        MException e1 = convertException(e);
        TRACE(m_trace, "STATUS: Failure! RC: " + e1.getRC() + ", Message: " + e1.getMessage());
        setErrorProperties(e1);
        try
        {
            if( m_trace != null )
            {
                m_trace.close();
                m_trace = null;
            }
        }
        catch( Exception ex )
        {

        }
        return e1;
    }

    static MException convertException(Exception e)
    {
        if( e instanceof MException )
        {
            // No conversion, just return it
            return(MException)e;
        }
        else
        {
            // Unexpected
            return new MUnexpectedException(e);
        }
    }

    static void printStackTrace(MTrace trace, Throwable t)
    {
        if( trace != null && t != null )
        {
            StackTraceElement[] stackFrames = t.getStackTrace();

            TRACE(trace, "EXCEPTION: " + t.toString());

            for( int i = 0; i < stackFrames.length; i++ )
            {
                String frameText = stackFrames[i].toString();
                TRACE(trace, "  at " + frameText);
            }
        }
    }

    protected void setErrorProperties(MException e)
    {
        try
        {
            setTextProperty(MConstants.MPIP_OBJECT_ERROR_MSG, 0, e.getMessage());
            setIntegerProperty(MConstants.MPIP_OBJECT_ERROR_CODE, 0, e.getRC());
        }
        catch( MException e1 )
        {
            // Ignore this exception. These functions set the error code and message
            // reported to the Resource Manger. If they fail there is nothing that
            // can be done.
        }
    }
    			
}