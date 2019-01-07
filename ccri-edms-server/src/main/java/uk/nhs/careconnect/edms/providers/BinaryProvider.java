package uk.nhs.careconnect.edms.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;

import ca.uhn.fhir.rest.server.IResourceProvider;


import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;

import javax.servlet.http.HttpServletRequest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.IOUtils;
import uk.nhs.careconnect.edms.OpenCMIS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


@Component
public class BinaryProvider implements IResourceProvider {

    @Autowired
    FhirContext ctx;


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BinaryProvider.class);

    Session session;

    public Class<? extends IBaseResource> getResourceType() {
        return Binary.class;
    }


    @Read
    public Binary getBinaryById(HttpServletRequest request, @IdParam IdType internalId) {

        Binary binary = null;
        OpenCMIS openCMIS = new OpenCMIS();
        session = openCMIS.getSession("fhir","test","test");

        //openCMIS.listTopFolder(session);
        //Folder root = session.getRootFolder();
        //getFolderContents(root, "/");
        log.info("Get Binary /"+internalId.getValue());
        try {
            CmisObject fileObj = session.getObject(internalId.getValue());
            log.info("Name = "+fileObj.getName());
            if (fileObj instanceof Document) {
                Document docMetadata = (Document) fileObj;
                ContentStream docContent = docMetadata.getContentStream();
                binary = new Binary();
                binary.setContentType(docContent.getMimeType());
                binary.setId(internalId.getValue());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try {
                    org.apache.commons.io.IOUtils.copyLarge(docContent.getStream(),buffer);
                    byte[] byteArray = buffer.toByteArray();
                    binary.setContent(byteArray);
                } catch (IOException ex) {
                    binary = null;
                }
            }

        } catch (CmisObjectNotFoundException obj) {
            log.info("CMIS Object not found");
        }

        return binary;
    }

    Folder getFolderContents(Folder folder, String path) {

        ItemIterable<CmisObject> contentItems = folder.getChildren();
        for (CmisObject contentItem : contentItems) {
            if (contentItem instanceof Document) {
                Document docMetadata = (Document) contentItem;
                ContentStream docContent = docMetadata.getContentStream();
                log.info("["+path+"] :"+docMetadata.getName() + " id="+ docMetadata.getId() + " [size=" + docContent.getLength() + "][Mimetype=" +
                        docContent.getMimeType() + "][type=" + docMetadata.getType().getDisplayName() + "]");
            } else if (contentItem instanceof Folder) {
                log.info("["+path+"] :"+contentItem.getName() + " [type=" + contentItem.getType().getDisplayName() + "]");
                if (contentItem.getName().equals("Sites")) { getFolderContents((Folder) contentItem, path+"Sites/"); }
                if (contentItem.getName().equals("fhir")) { getFolderContents((Folder) contentItem,path+"fhir/" ); }
                if (contentItem.getName().equals("documentLibrary")) { getFolderContents((Folder) contentItem,path+"documentLibrary/" ); }
            }
            else {
                log.info("["+path+"] :"+contentItem.getName() + " [type=" + contentItem.getType().getDisplayName() + "]");
                log.info("["+path+"] :"+contentItem.getName() + " [type=" + contentItem.getType().getClass().getCanonicalName() + "]");
            }
        }
        return folder;
    }


}
