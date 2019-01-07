package uk.nhs.careconnect.edms.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.edms.OpenCMIS;

import javax.print.Doc;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
public class DocumentReferenceProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;


    public Class<? extends IBaseResource> getResourceType() {
        return DocumentReference.class;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DocumentReferenceProvider.class);

    Session session;


    @Read
    public DocumentReference getDocumentReferenceById(HttpServletRequest request, @IdParam IdType internalId) {

        DocumentReference documentReference = null;
        OpenCMIS openCMIS = new OpenCMIS();
        session = openCMIS.getSession("fhir","test","test");


        log.info("Get Binary /"+internalId.getValue());
        try {
            CmisObject fileObj = session.getObject(internalId.getValue());
            log.info("Name = "+fileObj.getName());
            if (fileObj instanceof Document) {
                Document docMetadata = (Document) fileObj;
                ContentStream docContent = docMetadata.getContentStream();
                documentReference = new DocumentReference();

                documentReference.setId(internalId.getIdPart());

                documentReference.setCreated(docMetadata.getCreationDate().getTime());
                documentReference.setIndexed(docMetadata.getCreationDate().getTime());

                documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);

                DocumentReference.DocumentReferenceContentComponent content = documentReference.addContent();
                content.getAttachment()
                        .setContentType(docContent.getMimeType())
                        .setTitle(docContent.getFileName())
                        .setUrl("http://127.0.0.1:8184/STU3/Binary/"+internalId.getIdPart());

                for (Property property : docMetadata.getProperties()) {
                    switch (property.getQueryName()) {
                        case "cmis:createdBy" :
                            documentReference.addAuthor().setDisplay(property.getValueAsString());
                            break;

                        case "fhir:patientNumber" :
                            documentReference.setSubject(new Reference("Patient/"+property.getValueAsString()));
                            break;
                        case "fhir:type" :
                            Coding code = documentReference.getType().addCoding().setSystem("http://snomed.info/sct");
                            switch (property.getValueAsString()) {
                                case "Mental health care plan" :
                                    code.setCode("718347000").setDisplay(property.getValueAsString());
                                    break;
                                case "Discharge letter":
                                    code.setCode("823701000000103").setDisplay(property.getValueAsString());
                                    break;
                            }
                            break;
                        case "fhir:careSetting" :
                            Coding setting = documentReference.getContext().getPracticeSetting().addCoding().setSystem("http://snomed.info/sct");
                            switch (property.getValueAsString()) {
                                case "General surgical service" :
                                    setting.setCode("310156009").setDisplay(property.getValueAsString());
                                    break;
                                case "Breast screening service":
                                    setting.setCode("310126000").setDisplay(property.getValueAsString());
                                    break;
                                case "Urology service":
                                    setting.setCode("310167005").setDisplay(property.getValueAsString());
                                    break;
                                case "Orthopedic service":
                                    setting.setCode("310161006").setDisplay(property.getValueAsString());
                                    break;

                            }
                            break;
                    }
                    log.info(property.getQueryName() + "("+ property.getDisplayName() + ") Value = "+property.getValueAsString());
                }
            }

        } catch (CmisObjectNotFoundException obj) {
            log.info("CMIS Object not found");
        }

        return documentReference;
    }

    @Search
    public List<Resource> searchComposition(HttpServletRequest theRequest
            , @OptionalParam(name = DocumentReference.SP_RES_ID) TokenParam resid
            , @OptionalParam(name = DocumentReference.SP_PATIENT) ReferenceParam patient

    ) {

        List<Resource> results =  null; //compositionDao.search(ctx,resid,patient);

        OpenCMIS openCMIS = new OpenCMIS();
        session = openCMIS.getSession("fhir","test","test");

        RepositoryInfo repoInfo = session.getRepositoryInfo();
        if (repoInfo.getCapabilities().getQueryCapability().equals(CapabilityQuery.METADATAONLY)) {
            log.warn("Repository does not support FTS [repoName=" + repoInfo.getProductName() +
                    "][repoVersion=" + repoInfo.getProductVersion() + "]");
        } else {
            String query = "SELECT * FROM cmis:document WHERE cmis:name LIKE 'Screen%'";
            ItemIterable<QueryResult> searchResult = session.query(query, false);
            openCMIS.logSearchResult(query, searchResult);

            query = "SELECT * FROM cmis:document WHERE cmis:name LIKE 'OpenCMIS%' AND CONTAINS('testing')";
            searchResult = session.query(query, false);
            openCMIS.logSearchResult(query, searchResult);
        }


        return results;

    }
}
