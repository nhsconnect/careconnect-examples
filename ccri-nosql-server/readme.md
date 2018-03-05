
# MongoDb

You need a mongoDb server to run this application. Either:
 
- Use the docker image https://hub.docker.com/_/mongo/ 

- Install mongodb server (and maven) 
    - create directory c:\mongodb\data\db
    - start Mongo server
        - cd "c:\Program Files\MongoDB\Server\3.6\bin"
        - .\mongod.exe --dbpath "C:\mongodb\db\data"

- Mongo can be embedded within this app. See https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-nosql.html#boot-features-mongo-embedded 


# MongoDb FHIR Server

start FHIR Server 

- mvn spring-boot:run


Using a tool such as [Postman(https://www.getpostman.com/)] POST FHIR Documents to:

- http://127.0.0.1:8181/STU3/Bundle

