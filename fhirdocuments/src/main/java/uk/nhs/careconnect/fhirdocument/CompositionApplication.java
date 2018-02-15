package uk.nhs.careconnect.fhirdocument;

import ca.uhn.fhir.context.FhirContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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
                    System.out.println(fileD.getName());


                    performTransform(fileD.getAbsolutePath(),"C:\\Temp\\"+df.format(date)+fileD.getName()+".hl7xslt.html","XML/DocumentToHTML.xslt");

                    /*

                    Thymeleaf processing and pdf conversion. Disable for now

                    String contents = org.apache.commons.io.IOUtils.toString(new InputStreamReader(new FileInputStream(fileD), "UTF-8"));
                    IBaseResource document = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(contents).newParser(fhirCtx).parseResource(contents);


                    if (document instanceof Bundle) {

                        Bundle bundle = (Bundle) document;

                        Map<String,Object> data = new HashMap<>();

                        String body = "";
                        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

                            if (entry.getResource() instanceof Composition) {
                                Composition composition = (Composition) entry.getResource();
                                for (Composition.SectionComponent section : composition.getSection()) {
                                    body = body + section.getText().getDiv().toString();
                                }
                            }
                            if (entry.getResource() instanceof Patient) {

                                CareConnectPatient ccPatient = new CareConnectPatient((Patient) entry.getResource());
                                data.put("patient",ccPatient);
                            }
                        }

                        Patient patient = new Patient();
                       //
                        data.put("body",body);

                        String pdfFile = pdfGenaratorUtil.createPdf("eDischarge",data);
                        Path FROM = Paths.get(pdfFile);

                        Path TO = Paths.get("C:\\Temp\\"+df.format(date)+fileD.getName()+".thymeleaf.pdf");
                        //overwrite existing file, if exists
                        CopyOption[] options = new CopyOption[]{
                                StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.COPY_ATTRIBUTES
                        };
                        Files.copy(FROM, TO, options);
                        System.out.println("Done");


                       // performTransform(fileD.getAbsolutePath(),"C:\\Temp\\"+df.format(date)+fileD.getName()+".nhsdxslt.html","XML/NHS_FHIR_Document_Renderer.xsl");
                    }
*/
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                throw ex;

            }



		};
	}

	private void performTransform(String xmlInput, String htmlOutput, String styleSheet) {
        // Input xml data file
        ClassLoader classLoader = getContextClassLoader();

        // Input xsl (stylesheet) file
        //String xslInput = classLoader.getResource("resources/input.xsl").getFile(); // "resources/input.xsl";
        String xslInput = classLoader.getResource(styleSheet).getFile(); // "resources/input.xsl";
        System.out.println("In Transform");
        System.out.println("xmlInput="+xmlInput);
        System.out.println("htmlOutput="+htmlOutput);
        System.out.println("xslInput="+xslInput);

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
