package uk.nhs.careconnect.fhirdocument;

import ca.uhn.fhir.context.FhirContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
public class CompositionApplication {

    @Autowired
    PdfGenaratorUtil pdfGenaratorUtil;

	public static void main(String[] args) {
        System.getProperties().put( "server.port", 8082 );
		SpringApplication.run(CompositionApplication.class, args).close();
	}

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {

		return args -> {
            ClassLoader classLoader = getContextClassLoader();

			FhirContext fhirCtx = FhirContext.forDstu3();
			Date date = new Date();
            DateFormat df = new SimpleDateFormat("HHmm_dd_MM_yyyy");

            try {
                File file = new File(classLoader.getResource("FHIRDocuments").getFile());

                for (File fileD : file.listFiles()) {

                    performTransform(fileD.getAbsolutePath(),"C:\\Temp\\"+df.format(date)+"+"+fileD.getName()+".html","XML/DocumentToHTML.xslt");

                    saveToPDF("C:\\Temp\\"+df.format(date)+"+"+fileD.getName()+".html", "C:\\Temp\\"+df.format(date)+"+"+fileD.getName()+".pdf");

                }
            } catch (Exception ex) {
                System.out.println("ERROR + "+ex.getMessage());
                throw ex;

            }



		};
	}

	private String saveToPDF(String inputFile, String outputFileName) {
        FileOutputStream os = null;
        File file = new File(inputFile);

        try {
            String processedHtml = org.apache.commons.io.IOUtils.toString(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            final File outputFile = new File(outputFileName);

            os = new FileOutputStream(outputFile);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(processedHtml);
            renderer.layout();
            renderer.createPDF(os, false);
            renderer.finishPDF();
            return outputFile.getAbsolutePath();
        }
        catch(Exception ex) {
            System.out.println("ERROR - "+ex.getMessage());
        }
        finally {

            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) { /*ignore*/ }
            }
            return null;
        }
    }

	private void performTransform(String xmlInput, String htmlOutput, String styleSheet) {
        // Input xml data file
        ClassLoader classLoader = getContextClassLoader();

        // Input xsl (stylesheet) file

        String xslInput = classLoader.getResource(styleSheet).getFile(); // "resources/input.xsl";

        // Set the property to use xalan processor
        System.setProperty("javax.xml.transform.TransformerFactory",
                "org.apache.xalan.processor.TransformerFactoryImpl");

        // try with resources
        try ( FileOutputStream os = new FileOutputStream(htmlOutput) )
        {
            FileInputStream xml = new FileInputStream(xmlInput);
            FileInputStream xsl = new FileInputStream(xslInput);

            // Instantiate a transformer factory
            TransformerFactory tFactory = TransformerFactory.newInstance();

            // Use the TransformerFactory to process the stylesheet source and produce a Transformer
            StreamSource styleSource = new StreamSource(xsl);
            Transformer transformer = tFactory.newTransformer(styleSource);

            // Use the transformer and perform the transformation
            StreamSource xmlSource = new StreamSource(xml);
            StreamResult result = new StreamResult(os);
            transformer.transform(xmlSource, result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
