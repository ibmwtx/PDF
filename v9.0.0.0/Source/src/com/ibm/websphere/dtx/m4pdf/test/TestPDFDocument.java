package com.ibm.websphere.dtx.m4pdf.test;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioCollection;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;

import com.ibm.websphere.dtx.dtxpi.MConstants;
import com.ibm.websphere.dtx.dtxpi.MException;
import com.ibm.websphere.dtx.dtxpi.MMap;



public class TestPDFDocument {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

        try
        {
            // Initialize the API
            MMap.initializeAPI(null);

            // Create a map 
            MMap map = new MMap("Map name");
                                                

            // Run the map
            map.run();

            // Check the return status
            int iRC = map.getIntegerProperty(MConstants.MPIP_OBJECT_ERROR_CODE, 0);
            String szMsg = map.getTextProperty(MConstants.MPIP_OBJECT_ERROR_MSG, 0);
            System.out.println("Map status: " + szMsg + " (" + iRC + ")");	           
        
            // Clean up
            map.unload();
          
            MMap.terminateAPI();
        }
        catch( MException e )
        {
            e.printStackTrace();
        }	
	}

}
