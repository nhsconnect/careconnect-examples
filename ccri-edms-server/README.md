#Install Alfresco community edition

(Do custom install and change tomcat ports if required)

Create a site 'fhir' 

Add user test/test

Create model fhir (namespace etc fhir)

Within model create a type and then within that type add properties:

* patientNumber (will use id from ccri) d:long mandatory
* type (SNOMED documentReference types) d:test ListOfValues (Mental health care plan, Discharge letter)

#alfresco

Mac: Don't run start up script as root. https://docs.alfresco.com/community5.1/tasks/simpleinstall-community-mac.html




