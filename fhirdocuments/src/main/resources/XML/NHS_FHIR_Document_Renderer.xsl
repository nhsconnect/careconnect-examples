<?xml version="1.0"?>
<!--	FHIR Renderer to display FHIR profiles, the XML needs to be wrapped in tags
			Beginning of file must contain <Bundle xmlns="http://hl7.org/fhir"><entry><resource>
			End the of file must contain </resource></entry></Bundle>
			Version: 1.2
			Last release date: 16/08/2017
			Author: Naminder Singh Soorma
			Dependecies: This renderer has been tested using XML Spy 2016 and XSL Renderer Saxon9he.jar.
			Description: The following FHIR Profiles are rendered.
								- Bundle
								- Composition
								- Patient
								- Practitioner
								- Encounter
								- Observation
			TODO
								- MedicationOrder
								- MedicationStatement
								- AllergyIntolerance
	-->
<xsl:stylesheet version="2.0" xmlns="http://hl7.org/fhir" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fhir="http://hl7.org/fhir" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:saxon="http://saxon.sf.net/" >
    <xsl:output indent="yes" method="html"/>
    <!-- Fixed values are defined as parameters so they can be overridden to expose content in other languages, etc. -->
    <xsl:param name="untitled_doc" select="'Untitled Document'"/>
    <xsl:param name="no_human_display" select="'No human-readable content available'"/>
    <xsl:param name="subject-heading" select="'Subject Details'"/>
    <xsl:param name="author-heading" select="'Author Details'"/>
    <xsl:param name="encounter-heading" select="'Encounter Information'"/>
    <xsl:param name="untitled_section" select="'Untitled Section'"/>
    <xsl:variable name="upper" select = "'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
    <xsl:variable name="lower" select = "'abcdefghijklmnopqrstuvwxyz'"/>

    <xsl:template match="/">
        <html>
            <head>
                <title>FHIR Renderer</title>
                <style type="text/css">
                    body { color: #000000; font-size: 12pt; line-height: normal; font-family: Verdana, Arial, sans-serif; margin: 10px; }
                    h1 { font-size: 14pt; font-weight: bold; color: #000000; margin-top: 5px; margin-bottom: 15px;}
                    h2 { font-size: 12pt; font-weight: bold; color: #000000; margin-top: 5px; margin-bottom: 15px; }
                    h3 { font-size: 10pt; font-weight: bold; color: #000000; margin-top: 5px; margin-bottom: 15px; }
                    h4 { font-size: 10pt; font-weight: bold; text-decoration: underline; color: #000000; margin-top: 5px; margin-bottom: 15px; }
                    h5 { font-size: 10pt; font-weight: normal; text-decoration: underline;  color: #000000; margin-top: 5px; margin-bottom: 15px; }
                    h6 { font-size: 10pt; font-weight: normal; color: #000000; margin-top: 5px; margin-bottom: 15px; }
                    table { border: 0px solid #000000; table-layout: fixed; text-align: left; width: 100%;}
                    th.default {padding: 3px; color: #000000; background-color: #eeeeee; text-align: left;}
                    th {padding: 3px;}
                    td {padding: 3px;}
                    /*Banner styles*/
                    div.banner {margin-bottom: 30px; border: 1px solid #000000; background-color: #eeeeee;}
                    div.banner TABLE {border: 1px solid black; background-color: #ffffff; margin-bottom: 5px; }
                    div.banner TD {background-color: #eeeeee; vertical-align: top; padding: 3px; font-weight: normal; font-size: 12pt;}
                    div.banner TH {background-color: #dddddd; vertical-align: top; padding: 3px; font-weight: bold; font-size: 12pt;}
                    div.banner TABLE P {margin: 0;}
                    div.banner-partial-cream {margin-bottom: 30px; border: 1px solid #000000; background-color: #ffffee; padding-left: 5px;}
                    div.banner-partial-cream TABLE {border: 0px; background-color: #ffffee;}
                    div.banner-partial-cream TD {background-color: #ffffee; vertical-align: top; padding: 3px; font-weight: bold; font-size: 12pt;}
                    div.banner-partial-cream TH {background-color: #ffffee; vertical-align: top; padding: 3px; font-weight: normal; font-style:italic; font-size: 12pt;}
                    div.banner-partial-cream TABLE P {margin: 0;}
                    div.banner-partial-violet {margin-bottom: 30px; border: 1px solid #000000; background-color: #eeeeff; padding-left: 5px;}
                    div.banner-partial-violet TABLE {border: 0px; background-color: #eeeeff; }
                    div.banner-partial-violet TD {background-color: #eeeeff; vertical-align: top; padding: 3px; font-weight: bold; font-size: 12pt;}
                    div.banner-partial-violet TH {background-color: #ffffee; vertical-align: top; padding: 3px; font-weight: normal; font-style:italic; font-size: 12pt;}
                    div.banner-partial-violet TABLE P {margin: 0;}
                    div.banner-partial-gray {margin-bottom: 30px; border: 0px solid #000000; background-color: #ffffff; padding-left: 5px;}
                    div.banner-partial-gray TABLE {border: 1px solid black; background-color: #ffffff; margin-bottom: 5px; }
                    div.banner-partial-gray TD {background-color: #eeeeee; vertical-align: top; padding: 3px; font-weight: normal; font-size: 12pt;}
                    div.banner-partial-gray TH {background-color: #dddddd; vertical-align: top; padding: 3px; font-weight: bold; font-size: 12pt;}
                    div.banner-partial-gray TABLE P {margin: 0; }
                    .label {font-style:italic; font-weight: normal;}
                    .bold {font-weight: bold;}
                    .bold-italic {font-style:italic; font-weight: bold;}

                    /******************** CONFIG OPTIONS FOR RENDERING - START ********************/
                    /************************************************************************************************/
                    /* The FHIR Renderer has two options to either display the Full resource with all elements or a Partial resource with chosen elements,
                    to hide a particular view then un-comment the statement to hide, e.g. un-comment display:none so it will not display that particular view. */
                    #bundledetails-full {display: none;}
                    #full-resources {display: none;}
                    //#partial-resources {display: none;}
                    /**********************************************************************************************/
                    /******************** CONFIG OPTIONS FOR RENDERING - END ********************/
                </style>
            </head>
            <body>
                <div id="bundledetails-full">
                    <xsl:call-template name="bundledetails-full"/>
                </div>
                <div id="partial-resources">
                    <xsl:call-template name="patient-header-partial"/>
                    <xsl:call-template name="clinical-document-partial"/>
                    <xsl:call-template name="document-reference-partial"/>
                </div>
                <div id="full-resources">
                    <xsl:call-template name="patient-full"/>
                    <xsl:call-template name="practitioner-full"/>
                    <xsl:call-template name="compositionheader-full"/>
                    <xsl:call-template name="compositionmain-full"/>
                    <xsl:call-template name="encounter-full"/>
                    <xsl:call-template name="observation-full"/>
                    <xsl:call-template name="medicationorder-full"/>
                    <xsl:call-template name="medicationstatement-full"/>
                    <xsl:call-template name="allergyintolerance-full"/>
                </div>
            </body>
        </html>
    </xsl:template>
    <!-- END OF HTML -->

    <!--	****************************** START OF BUNDLE FULL ******************************	-->
    <xsl:template name="bundledetails-full">
        <xsl:if test="(fhir:Bundle)">
            <div id="bundledetails">
                <xsl:if test="fhir:Bundle/fhir:id/@value != ''"><h2>Bundle Details</h2> Bundle id: <xsl:value-of select="fhir:Bundle/fhir:id/@value"/><br /></xsl:if>
                <xsl:if test="fhir:Bundle/fhir:type/@value != ''">Type: <xsl:value-of select="fhir:Bundle/fhir:type/@value"/><br /></xsl:if>
                <xsl:if test="fhir:Bundle/fhir:entry/fhir:fullUrl/@value != ''">Full Url: <xsl:value-of select="fhir:Bundle/fhir:entry/fhir:fullUrl/@value"/><br /><br /></xsl:if>
            </div>
        </xsl:if>
    </xsl:template>
    <!--	****************************** END OF BUNDLE FULL ******************************	-->

    <!--	****************************** START OF PATIENT FULL ******************************	-->
    <xsl:template name="patient-full">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient)">
            <div id="patient-full" class="banner">
                <h1>Patient Demographics</h1>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Use: </strong></th>
                            <th><strong>Name: </strong></th>
                            <th><strong>Family: </strong></th>
                            <th><strong>Given: </strong></th>
                            <th><strong>Prefix: </strong></th>
                            <th><strong>Suffix: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:use/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:text/@value"/></td>
                            <td><xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:family">
                                <xsl:value-of select="@value"/><br />
                            </xsl:for-each></td>
                            <td><xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:given">
                                <xsl:value-of select="@value"/><br />
                            </xsl:for-each></td>
                            <td><xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:prefix">
                                <xsl:value-of select="@value"/><br />
                            </xsl:for-each></td>
                            <td><xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:suffix">
                                <xsl:value-of select="@value"/><br />
                            </xsl:for-each></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:period/fhir:start/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name/fhir:period/fhir:end/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <td>
                                <strong>Date of Birth: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:birthDate/@value"/>
                            </td>
                            <td>
                                <strong>Gender: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:gender/@value"/>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Identifier: </strong></th>
                            <th><strong>Use: </strong></th>
                            <th><strong>Type: </strong></th>
                            <th><strong>System: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                            <th><strong>Assigner: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:identifier">
                            <tr>
                                <xsl:choose>
                                    <xsl:when test="fhir:system/@value = 'http://fhir.nhs.uk/Id/nhs-number' ">
                                        <td><xsl:value-of select="fhir:value/@value"/></td>
                                        <td><xsl:value-of select="fhir:use/@value"/></td>
                                        <td><xsl:value-of select="fhir:type/@value"/></td>
                                        <td><xsl:value-of select="fhir:system/@value"/></td>
                                        <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                        <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                                        <td><xsl:value-of select="fhir:assigner/@value"/></td>
                                    </xsl:when>
                                </xsl:choose>
                            </tr>
                        </xsl:for-each>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:identifier">
                            <tr>
                                <xsl:choose>
                                    <xsl:when test="fhir:system/@value = 'http://fhir.nhs.uk/Id/local-identifier' ">
                                        <td><xsl:value-of select="fhir:value/@value"/></td>
                                        <td><xsl:value-of select="fhir:use/@value"/></td>
                                        <td><xsl:value-of select="fhir:type/@value"/></td>
                                        <td><xsl:value-of select="fhir:system/@value"/></td>
                                        <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                        <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                                        <td><xsl:value-of select="fhir:assigner/@value"/></td>
                                    </xsl:when>
                                </xsl:choose>
                            </tr>
                        </xsl:for-each>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:identifier">
                            <tr>
                                <xsl:choose>
                                    <xsl:when test="fhir:system/@value = 'http://fhir.nhs.uk/Id/local-patient-identifier' ">
                                        <td><xsl:value-of select="fhir:value/@value"/></td>
                                        <td><xsl:value-of select="fhir:use/@value"/></td>
                                        <td><xsl:value-of select="fhir:type/@value"/></td>
                                        <td><xsl:value-of select="fhir:system/@value"/></td>
                                        <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                        <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                                        <td><xsl:value-of select="fhir:assigner/@value"/></td>
                                    </xsl:when>
                                </xsl:choose>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <td><strong>Deceased DateTime: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:deceasedDateTime/@value"/></td>
                            <td><strong>Deceased Boolean: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:deceasedBoolean/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:address">
                                <td>
                                    <table>
                                        <tbody>
                                            <tr><td><strong>Address: </strong><br /><xsl:value-of select="fhir:use/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:type/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:text/@value"/></td></tr>
                                            <xsl:for-each select="fhir:line">
                                                <tr><td><xsl:value-of select="@value"/></td></tr>
                                            </xsl:for-each>
                                            <tr><td><xsl:value-of select="fhir:city/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:district/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:state/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:postalCode/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:country/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:period/fhir:start/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:period/fhir:end/@value"/></td></tr>
                                        </tbody>
                                    </table>
                                </td>
                            </xsl:for-each>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:telecom">
                                    <strong>System: </strong><xsl:value-of select="fhir:system/@value"/><br />
                                    <strong>Value: </strong><xsl:value-of select="fhir:value/@value"/><br />
                                    <strong>Use: </strong><xsl:value-of select="fhir:use/@value"/><br />
                                    <strong>Rank: </strong><xsl:value-of select="fhir:rank/@value"/><br />
                                    <strong>Period Start: </strong><xsl:value-of select="fhir:period/fhir:start/@value"/><br />
                                    <strong>Period End: </strong><xsl:value-of select="fhir:period/fhir:end/@value"/><br /><br />
                                </xsl:for-each>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <td><strong>maritalStatus system: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:maritalStatus/fhir:coding/fhir:system/@value"/></td>
                            <td><strong>maritalStatus version: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient//fhir:maritalStatus/fhir:coding/fhir:version/@value"/></td>
                            <td><strong>maritalStatus code: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient//fhir:maritalStatus/fhir:coding/fhir:code/@value"/></td>
                            <td><strong>maritalStatus display: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient//fhir:maritalStatus/fhir:coding/fhir:display/@value"/></td>
                            <td><strong>maritalStatus userselected: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient//fhir:maritalStatus/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><strong>maritalStatus text: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient//fhir:maritalStatus/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <td><strong>multipleBirthBoolean: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:multipleBirthBoolean/@value"/></td>
                            <td><strong>multipleBirthInteger: </strong><br /><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:multipleBirthInteger/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Photo contentType: </strong></th>
                            <th><strong>Photo language: </strong></th>
                            <th><strong>Photo data: </strong></th>
                            <th><strong>Photo url: </strong></th>
                            <th><strong>Photo size: </strong></th>
                            <th><strong>Photo hash: </strong></th>
                            <th><strong>Photo title: </strong></th>
                            <th><strong>Photo creation: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:photo">
                            <tr>
                                <td><xsl:value-of select="fhir:contentType/@value"/></td>
                                <td><xsl:value-of select="fhir:language/@value"/></td>
                                <td><xsl:value-of select="fhir:data/@value"/></td>
                                <td><xsl:value-of select="fhir:url/@value"/></td>
                                <td><xsl:value-of select="fhir:size/@value"/></td>
                                <td><xsl:value-of select="fhir:hash/@value"/></td>
                                <td><xsl:value-of select="fhir:title/@value"/></td>
                                <td><xsl:value-of select="fhir:creation/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:contact">
                    <table>
                        <tbody>
                            <tr>
                                <th>Contact <br /> Relationship System</th>
                                <th>Relationship Version</th>
                                <th>Relationship Code</th>
                                <th>Relationship Display</th>
                                <th>Relationship userSelected</th>
                            </tr>
                            <xsl:for-each select="fhir:relationship">
                                <tr>
                                    <td><xsl:value-of select="fhir:coding/fhir:system/@value"/></td>
                                    <td><xsl:value-of select="fhir:coding/fhir:version/@value"/></td>
                                    <td><xsl:value-of select="fhir:coding/fhir:code/@value"/></td>
                                    <td><xsl:value-of select="fhir:coding/fhir:display/@value"/></td>
                                    <td><xsl:value-of select="fhir:coding/fhir:userSelected/@value"/></td>
                                </tr>
                            </xsl:for-each>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>Use: </strong></th>
                                <th><strong>Name: </strong></th>
                                <th><strong>Family: </strong></th>
                                <th><strong>Given: </strong></th>
                                <th><strong>Prefix: </strong></th>
                                <th><strong>Suffix: </strong></th>
                                <th><strong>Period Start: </strong></th>
                                <th><strong>Period End: </strong></th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:name/fhir:use/@value"/></td>
                                <td><xsl:value-of select="fhir:name/fhir:text/@value"/></td>
                                <td>
                                    <xsl:for-each select="fhir:name/fhir:family">
                                        <xsl:value-of select="@value"/><br />
                                    </xsl:for-each></td>
                                <td>
                                    <xsl:for-each select="fhir:name/fhir:given">
                                        <xsl:value-of select="@value"/><br />
                                    </xsl:for-each></td>
                                <td>
                                    <xsl:for-each select="fhir:name/fhir:prefix">
                                        <xsl:value-of select="@value"/><br />
                                    </xsl:for-each></td>
                                <td>
                                    <xsl:for-each select="fhir:name/fhir:suffix">
                                        <xsl:value-of select="@value"/><br />
                                    </xsl:for-each></td>
                                <td><xsl:value-of select="fhir:name/fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:name/fhir:period/fhir:end/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <td>
                                    <table>
                                        <tbody>
                                            <tr><td><strong>Address: </strong><br /><xsl:value-of select="fhir:address/fhir:use/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:address/fhir:type/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:address/fhir:text/@value"/></td></tr>
                                            <xsl:for-each select="fhir:address/fhir:line">
                                                <tr><td><xsl:value-of select="@value"/></td></tr>
                                            </xsl:for-each>
                                            <tr><td><xsl:value-of select="fhir:address/fhir:city/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:address/fhir:district/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:address/fhir:state/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:address/fhir:postalCode/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:address/fhir:country/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:address/fhir:period/fhir:start/@value"/></td></tr>
                                            <tr><td><xsl:value-of select="fhir:address/fhir:period/fhir:end/@value"/></td></tr>
                                        </tbody>
                                    </table>
                                </td>
                                <td>
                                    <xsl:for-each select="fhir:telecom">
                                        <strong>System: </strong><xsl:value-of select="fhir:system/@value"/><br />
                                        <strong>Value: </strong><xsl:value-of select="fhir:value/@value"/><br />
                                        <strong>Use: </strong><xsl:value-of select="fhir:use/@value"/><br />
                                        <strong>Rank: </strong><xsl:value-of select="fhir:rank/@value"/><br />
                                        <strong>Period Start: </strong><xsl:value-of select="fhir:period/fhir:start/@value"/><br />
                                        <strong>Period End: </strong><xsl:value-of select="fhir:period/fhir:end/@value"/><br /><br />
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>Gender:</strong></th>
                                <th><strong>Organization Reference:</strong></th>
                                <th><strong>Organization Display:</strong></th>
                                <th><strong>Period Start:</strong></th>
                                <th><strong>Period End:</strong></th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:gender/@value"/></td>
                                <td><xsl:value-of select="fhir:organization/fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:organization/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                </xsl:for-each>
                <strong>Communication</strong>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>System:</strong></th>
                            <th><strong>Code:</strong></th>
                            <th><strong>Display:</strong></th>
                            <th><strong>Text:</strong></th>
                            <th><strong>Preferred:</strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:communication">
                            <tr>
                                <td><xsl:value-of select="fhir:language/fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:language/fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:language/fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:language/fhir:text/@value"/></td>
                                <td><xsl:value-of select="fhir:preferred/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <strong>Care Provider</strong>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Reference:</strong></th>
                            <th><strong>Display:</strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:careProvider">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <strong>Managing Organization</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>Reference:</th>
                            <th>Display:</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:managingOrganization/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:managingOrganization/fhir:display/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>Link</strong>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Other reference:</strong></th>
                            <th><strong>Type:</strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:link">
                            <tr>
                                <td><xsl:value-of select="fhir:other/fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:type/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </div>
        </xsl:if>
    </xsl:template>

    <!--	****************************** END OF PATIENT FULL ******************************	-->

    <!--	****************************** START OF PRACTITIONER FULL ******************************	-->

    <xsl:template name="practitioner-full">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner)">
            <div id="practitioner" class="banner">
                <h1>Practitioner</h1>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Identifier: </strong></th>
                            <th><strong>Use: </strong></th>
                            <th><strong>Type: </strong></th>
                            <th><strong>System: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                            <th><strong>Assigner: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:identifier">
                            <tr>
                                <td><xsl:value-of select="fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:use/@value"/></td>
                                <td><xsl:value-of select="fhir:type/@value"/></td>
                                <td><xsl:value-of select="fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                                <td><xsl:value-of select="fhir:assigner/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Use: </strong></th>
                            <th><strong>Name: </strong></th>
                            <th><strong>Family: </strong></th>
                            <th><strong>Given: </strong></th>
                            <th><strong>Prefix: </strong></th>
                            <th><strong>Suffix: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:use/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:text/@value"/></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:family">
                                    <xsl:value-of select="@value"/><br />
                                </xsl:for-each></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:given">
                                    <xsl:value-of select="@value"/><br />
                                </xsl:for-each></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:prefix">
                                    <xsl:value-of select="@value"/><br />
                                </xsl:for-each></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:suffix">
                                    <xsl:value-of select="@value"/><br />
                                </xsl:for-each></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:period/fhir:start/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:period/fhir:end/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr><xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:address">
                            <td>
                                <table>
                                    <tbody>
                                        <tr><td><strong>Address: </strong><br /><xsl:value-of select="fhir:use/@value"/></td></tr>
                                        <tr><td><xsl:value-of select="fhir:type/@value"/></td></tr>
                                        <tr><td><xsl:value-of select="fhir:text/@value"/></td></tr>
                                        <xsl:for-each select="fhir:line">
                                            <tr><td><xsl:value-of select="@value"/></td></tr>
                                        </xsl:for-each>
                                        <tr><td><xsl:value-of select="fhir:city/@value"/></td></tr>
                                        <tr><td><xsl:value-of select="fhir:district/@value"/></td></tr>
                                        <tr><td><xsl:value-of select="fhir:state/@value"/></td></tr>
                                        <tr><td><xsl:value-of select="fhir:postalCode/@value"/></td></tr>
                                        <tr><td><xsl:value-of select="fhir:country/@value"/></td></tr>
                                        <tr><td><xsl:value-of select="fhir:period/fhir:start/@value"/></td></tr>
                                        <tr><td><xsl:value-of select="fhir:period/fhir:end/@value"/></td></tr>
                                    </tbody>
                                </table>
                            </td></xsl:for-each>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:telecom">
                                    <strong>System: </strong><xsl:value-of select="fhir:system/@value"/><br />
                                    <strong>Value: </strong><xsl:value-of select="fhir:value/@value"/><br />
                                    <strong>Use: </strong><xsl:value-of select="fhir:use/@value"/><br />
                                    <strong>Rank: </strong><xsl:value-of select="fhir:rank/@value"/><br />
                                    <strong>Period Start: </strong><xsl:value-of select="fhir:period/fhir:start/@value"/><br />
                                    <strong>Period End: </strong><xsl:value-of select="fhir:period/fhir:end/@value"/><br /><br />
                                </xsl:for-each>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <td>
                                <strong>Active: </strong><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:active/@value"/>
                            </td>
                            <td>
                                <strong>Gender: </strong><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:gender/@value"/>
                            </td>
                            <td>
                                <strong>Date of Birth: </strong><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:birthDate/@value"/>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Photo contentType: </strong></th>
                            <th><strong>Photo language: </strong></th>
                            <th><strong>Photo data: </strong></th>
                            <th><strong>Photo url: </strong></th>
                            <th><strong>Photo size: </strong></th>
                            <th><strong>Photo hash: </strong></th>
                            <th><strong>Photo title: </strong></th>
                            <th><strong>Photo creation: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:photo">
                            <tr>
                                <td><xsl:value-of select="fhir:contentType/@value"/></td>
                                <td><xsl:value-of select="fhir:language/@value"/></td>
                                <td><xsl:value-of select="fhir:data/@value"/></td>
                                <td><xsl:value-of select="fhir:url/@value"/></td>
                                <td><xsl:value-of select="fhir:size/@value"/></td>
                                <td><xsl:value-of select="fhir:hash/@value"/></td>
                                <td><xsl:value-of select="fhir:title/@value"/></td>
                                <td><xsl:value-of select="fhir:creation/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:practitionerRole">
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>Managing Organization Reference: </strong></th>
                                <th><strong>Display: </strong></th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:managingOrganization/fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:managingOrganization/fhir:display/@value"/></td>
                            </tr>
                            <tr>
                                <th><strong>Role Coding System: </strong></th>
                                <th><strong>Version: </strong></th>
                                <th><strong>Code: </strong></th>
                                <th><strong>Display: </strong></th>
                                <th><strong>userSelected: </strong></th>
                                <th><strong>Text: </strong></th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:role/fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:role/fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:role/fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:role/fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:role/fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:role/fhir:text/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>Specialty Coding System: </strong></th>
                                <th><strong>Version: </strong></th>
                                <th><strong>Code: </strong></th>
                                <th><strong>Display: </strong></th>
                                <th><strong>userSelected: </strong></th>
                                <th><strong>Text: </strong></th>
                            </tr>
                            <xsl:for-each select="fhir:specialty">
                                <tr>
                                    <td><xsl:value-of select="fhir:coding/fhir:system/@value"/></td>
                                    <td><xsl:value-of select="fhir:coding/fhir:version/@value"/></td>
                                    <td><xsl:value-of select="fhir:coding/fhir:code/@value"/></td>
                                    <td><xsl:value-of select="fhir:coding/fhir:display/@value"/></td>
                                    <td><xsl:value-of select="fhir:coding/fhir:userSelected/@value"/></td>
                                    <td><xsl:value-of select="fhir:text/@value"/></td>
                                </tr></xsl:for-each>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>Period Start: </strong></th>
                                <th><strong>Period End: </strong></th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>Location Reference: </strong></th>
                                <th><strong>Display: </strong></th>
                            </tr>
                            <xsl:for-each select="fhir:location">
                                <tr>
                                    <td><xsl:value-of select="fhir:reference/@value"/></td>
                                    <td><xsl:value-of select="fhir:display/@value"/></td>
                                </tr></xsl:for-each>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>Health care Service Reference: </strong></th>
                                <th><strong>Display: </strong></th>
                            </tr>
                            <xsl:for-each select="fhir:healthcareService">
                                <tr>
                                    <td><xsl:value-of select="fhir:reference/@value"/></td>
                                    <td><xsl:value-of select="fhir:display/@value"/></td>
                                </tr></xsl:for-each>
                        </tbody>
                    </table>
                </xsl:for-each>
                <hr></hr>
                <strong>Qualification</strong>
                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:qualification">
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>Identifier: </strong></th>
                                <th><strong>Use: </strong></th>
                                <th><strong>Type: </strong></th>
                                <th><strong>System: </strong></th>
                                <th><strong>Period Start: </strong></th>
                                <th><strong>Period End: </strong></th>
                                <th><strong>Assigner: </strong></th>
                            </tr>
                            <xsl:for-each select="fhir:identifier">
                                <tr>
                                    <td><xsl:value-of select="fhir:value/@value"/></td>
                                    <td><xsl:value-of select="fhir:use/@value"/></td>
                                    <td><xsl:value-of select="fhir:type/@value"/></td>
                                    <td><xsl:value-of select="fhir:system/@value"/></td>
                                    <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                    <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                                    <td><xsl:value-of select="fhir:assigner/@value"/></td>
                                </tr>
                            </xsl:for-each>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>Coding System: </strong></th>
                                <th><strong>Version: </strong></th>
                                <th><strong>Code: </strong></th>
                                <th><strong>Display: </strong></th>
                                <th><strong>userSelected: </strong></th>
                                <th><strong>Text: </strong></th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:text/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th>Period Start: </th>
                                <th>Period End: </th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th>Issuer Reference: </th>
                                <th>Display: </th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:issuer/fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:issuer/fhir:display/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                </xsl:for-each>
                <strong>Communication</strong>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>System:</strong></th>
                            <th><strong>Code:</strong></th>
                            <th><strong>Display:</strong></th>
                            <th><strong>Text:</strong></th>
                            <th><strong>Preferred:</strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:communication">
                            <tr>
                                <td><xsl:value-of select="fhir:language/fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:language/fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:language/fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:language/fhir:text/@value"/></td>
                                <td><xsl:value-of select="fhir:preferred/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </div>
        </xsl:if>
    </xsl:template>

    <!--	****************************** END OF PRACTITIONER FULL ******************************	-->

    <!--	****************************** START OF COMPOSITION FULL ******************************	-->
    <xsl:template name="compositionheader-full">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition)">
            <div id="compositionheader" class="banner">
                <h1>Composition Header</h1>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Id: </strong></th>
                            <th><strong>Identifier system: </strong></th>
                            <th><strong>Identifier value: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:id/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:identifier/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:identifier/fhir:value/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Date: </strong></th>
                            <th><strong>Type coding system: </strong></th>
                            <th>Type coding code: </th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:date/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:type/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:type/fhir:coding/fhir:code/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Type text:</strong></th>
                            <th><strong>Title: </strong></th>
                            <th><strong>Status: </strong></th>
                            <th><strong>Confidentiality: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:type/fhir:text/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:title/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:status/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:confidentiality/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Class coding system:</strong></th>
                            <th><strong>Class coding code: </strong></th>
                            <th><strong>Class coding display: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:class/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:class/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:class/fhir:coding/fhir:display/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Subject display: </strong></th>
                            <th><strong>Subject reference: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:subject/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:subject/fhir:reference/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>author reference</th>
                            <th>author display</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:author">
                            <tr>
                                <td>
                                    <xsl:value-of select="fhir:reference/@value"/>
                                </td>
                                <td>
                                    <xsl:value-of select="fhir:display/@value"/>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>attester mode</th>
                            <th>attester time</th>
                            <th>attester reference</th>
                            <th>attester display</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:attester">
                            <tr>
                                <td>
                                    <xsl:for-each select="fhir:mode">
                                        <xsl:value-of select="@value"/>
                                        <xsl:element name="br"/>
                                    </xsl:for-each>
                                </td>
                                <td>
                                    <xsl:value-of select="fhir:time/@value"/>
                                </td>
                                <td>
                                    <xsl:value-of select="fhir:party/fhir:reference/@value"/>
                                </td>
                                <td>
                                    <xsl:value-of select="fhir:party/fhir:display/@value"/>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Custodian reference: </strong></th>
                            <th><strong>Custodian display: </strong></th>
                            <th><strong>Encounter: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:custodian/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:custodian/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:encounter/fhir:reference/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>event system</th>
                            <th>event code</th>
                            <th>event display</th>
                            <th>event period start</th>
                            <th>event period end</th>
                            <th>event detail reference</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:event">
                            <tr>
                                <td>
                                    <xsl:value-of select="fhir:code/fhir:coding/fhir:system/@value"/>
                                </td>
                                <td>
                                    <xsl:for-each select="fhir:code/fhir:coding/fhir:code">
                                        <xsl:value-of select="@value"/>
                                        <xsl:element name="br"/>
                                    </xsl:for-each>
                                </td>
                                <td>
                                    <xsl:value-of select="fhir:code/fhir:coding/fhir:display/@value"/>
                                </td>
                                <td>
                                    <xsl:value-of select="fhir:period/fhir:start/@value"/>
                                </td>
                                <td>
                                    <xsl:value-of select="fhir:period/fhir:end/@value"/>
                                </td>
                                <td>
                                    <xsl:for-each select="fhir:detail/fhir:reference">
                                        <xsl:value-of select="@value"/>
                                        <xsl:element name="br"/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="compositionmain-full">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition)">
            <div id="compsections" class="banner">
                <h1>Composition Sections</h1>
                <p>
                    <table>
                        <tbody>
                            <tr>
                                <th>Title</th>
                                <th>Code - System/Code/Display</th>
                                <th>Mode</th>
                                <th>OrderedBy - System/Code/Display</th>
                                <th>Entry</th>
                                <th>emptyReason - System/Code/Display</th>
                            </tr>
                            <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:section">
                                <tr>
                                    <td>
                                        <xsl:value-of select="fhir:title/@value"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="fhir:code/fhir:coding/fhir:system/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:code/fhir:coding/fhir:code/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:code/fhir:coding/fhir:display/@value"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="fhir:mode/@value"/>
                                        <xsl:element name="br"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="fhir:orderedBy/fhir:coding/fhir:system/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:orderedBy/fhir:coding/fhir:code/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:orderedBy/fhir:coding/fhir:display/@value"/>
                                    </td>
                                    <td>
                                        <xsl:for-each select="fhir:entry">
                                            <xsl:value-of select="fhir:reference/@value"/>
                                            <xsl:element name="br"/>
                                        </xsl:for-each>
                                    </td>
                                    <td>
                                        <xsl:value-of select="fhir:emptyReason/fhir:coding/fhir:system/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:emptyReason/fhir:coding/fhir:code/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:emptyReason/fhir:coding/fhir:display/@value"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                </tr>
                                <hr></hr>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:section/fhir:section">
                                    <tr>
                                        <td>
                                            - <xsl:value-of select="fhir:title/@value"/><!--Sub Section title -->
                                        </td>
                                        <td>
                                            - <xsl:value-of select="fhir:code/fhir:coding/fhir:system/@value"/><!--Sub Section system -->
                                            <xsl:element name="br"/>
                                            - <xsl:value-of select="fhir:code/fhir:coding/fhir:code/@value"/><!--Sub Section code -->
                                            <xsl:element name="br"/>
                                            - <xsl:value-of select="fhir:code/fhir:coding/fhir:display/@value"/><!--Sub Section display -->
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </xsl:for-each>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th>Title</th>
                                <th>Code - System/Code/Display</th>
                                <th>Mode</th>
                                <th>OrderedBy - System/Code/Display</th>
                                <th>Entry</th>
                                <th>emptyReason - System/Code/Display</th>
                            </tr>
                            <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:section/fhir:section">
                                <tr>
                                    <td>
                                        - <xsl:value-of select="fhir:title/@value"/><!--Sub Section title -->
                                    </td>
                                    <td>
                                        - <xsl:value-of select="fhir:code/fhir:coding/fhir:system/@value"/><!--Sub Section system -->
                                        <xsl:element name="br"/>
                                        - <xsl:value-of select="fhir:code/fhir:coding/fhir:code/@value"/><!--Sub Section code -->
                                        <xsl:element name="br"/>
                                        - <xsl:value-of select="fhir:code/fhir:coding/fhir:display/@value"/><!--Sub Section display -->
                                    </td>
                                    <td>
                                        <xsl:value-of select="fhir:mode/@value"/>
                                        <xsl:element name="br"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="fhir:orderedBy/fhir:coding/fhir:system/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:orderedBy/fhir:coding/fhir:code/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:orderedBy/fhir:coding/fhir:display/@value"/>
                                    </td>
                                    <td>
                                        <xsl:for-each select="fhir:entry">
                                            <xsl:value-of select="fhir:reference/@value"/>
                                            <xsl:element name="br"/>
                                        </xsl:for-each>
                                    </td>
                                    <td>
                                        <xsl:value-of select="fhir:emptyReason/fhir:coding/fhir:system/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:emptyReason/fhir:coding/fhir:code/@value"/>
                                        <xsl:element name="br"/>
                                        <xsl:value-of select="fhir:emptyReason/fhir:coding/fhir:display/@value"/>
                                    </td>
                                </tr>
                            </xsl:for-each>
                        </tbody>
                    </table>
                </p>
            </div>
        </xsl:if>
        <hr></hr>
        <div class="banner-partial-gray">
            <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition)">
                <p>
                    <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:section">
                        <h2><xsl:value-of select="fhir:title/@value"/></h2>
                        <xsl:copy-of select="fhir:text/." />
                        <xsl:element name="br"/>
                    </xsl:for-each>
                    <hr></hr>

                    <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:section/fhir:section">
                        <h2><xsl:value-of select="fhir:title/@value"/></h2><!--Sub Section title -->
                        <xsl:copy-of select="fhir:text/."/>
                        <xsl:element name="br"/>
                    </xsl:for-each>
                </p>
            </xsl:if>
        </div>
    </xsl:template>

    <!--	****************************** END OF COMPOSITION FULL ******************************	-->

    <!--	****************************** START OF ENCOUNTER FULL ******************************	-->

    <xsl:template name="encounter-full">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter)">
            <div id="encounter" class="banner">
                <h1>Encounter</h1>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Identifier: </strong></th>
                            <th><strong>Use: </strong></th>
                            <th><strong>Type: </strong></th>
                            <th><strong>System: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                            <th><strong>Assigner: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:identifier">
                            <tr>
                                <td><xsl:value-of select="fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:use/@value"/></td>
                                <td><xsl:value-of select="fhir:type/@value"/></td>
                                <td><xsl:value-of select="fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                                <td><xsl:value-of select="fhir:assigner/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Status</strong></th>
                            <th><strong>Class</strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:status/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:class/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Status History: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:statusHistory">
                            <tr>
                                <td><xsl:value-of select="fhir:status/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Type System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:type">
                            <tr>
                                <td><xsl:value-of select="fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:text/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Priority System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:priority/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:priority/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:priority/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:priority/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:priority/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:priority/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Patient Reference: </th>
                            <th>Display: </th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:patient/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:patient/fhir:display/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>episodeOfCare Reference</th>
                            <th>Display</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:episodeOfCare">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>incomingReferral Reference</th>
                            <th>Display</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:incomingReferral">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <strong>Participant</strong>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Type Code/Display: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                            <th><strong>Individual Reference: </strong></th>
                            <th><strong>Individual Display: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:participant">
                            <tr>
                                <td>
                                    <xsl:for-each select="fhir:type">
                                        <xsl:value-of select="fhir:code/@value"/> / <xsl:value-of select="fhir:display/@value"/><br />
                                    </xsl:for-each>
                                </td>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                                <td><xsl:value-of select="fhir:individual/fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:individual/fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Appointment Reference: </th>
                            <th>Display: </th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:appointment/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:appointment/fhir:display/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Period Start: </th>
                            <th>Period End: </th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:period/fhir:start/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:period/fhir:end/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Length Value: </th>
                            <th>Comparator: </th>
                            <th>Unit: </th>
                            <th>System: </th>
                            <th>Code: </th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:length/fhir:value/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:length/fhir:comparator/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:length/fhir:unit/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:length/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:length/fhir:code/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Reason System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:reason">
                            <tr>
                                <td><xsl:value-of select="fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:text/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Indication Reference</th>
                            <th>Display</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:indication">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <strong>Hospitalization</strong>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>preAdmissionIdentifier: </strong></th>
                            <th><strong>Use: </strong></th>
                            <th><strong>Type: </strong></th>
                            <th><strong>System: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                            <th><strong>Assigner: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:preAdmissionIdentifier/fhir:value/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:preAdmissionIdentifier/fhir:use/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:preAdmissionIdentifier/fhir:type/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:preAdmissionIdentifier/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:preAdmissionIdentifier/fhir:period/fhir:start/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:preAdmissionIdentifier/fhir:period/fhir:end/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:preAdmissionIdentifier/fhir:assigner/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Origin Reference</th>
                            <th>Display</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:origin">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>admitSource System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:admitSource">
                            <tr>
                                <td><xsl:value-of select="fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:text/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>admittingDiagnosis Reference</th>
                            <th>Display</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:admittingDiagnosis">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>reAdmission System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:reAdmission/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:reAdmission/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:reAdmission/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:reAdmission/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:reAdmission/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:reAdmission/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>dietPreference System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dietPreference">
                            <tr>
                                <td><xsl:value-of select="fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:text/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>specialCourtesy System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:specialCourtesy">
                            <tr>
                                <td><xsl:value-of select="fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:text/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>specialArrangement System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:specialArrangement">
                            <tr>
                                <td><xsl:value-of select="fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:text/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Destination Reference</th>
                            <th>Display</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:destination">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>dischargeDisposition System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dischargeDisposition/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dischargeDisposition/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dischargeDisposition/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dischargeDisposition/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dischargeDisposition/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dischargeDisposition/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>dischargeDiagnosis Reference</th>
                            <th>Display</th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dischargeDiagnosis">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <strong>Location</strong>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Reference: </strong></th>
                            <th><strong>Display: </strong></th>
                            <th><strong>Status: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>

                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:location">
                            <tr>
                                <td><xsl:value-of select="fhir:location/fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:location/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:status/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>serviceProvider Reference: </th>
                            <th>Display: </th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:serviceProvider/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:serviceProvider/fhir:display/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>partOf Reference: </th>
                            <th>Display: </th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:partOf/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:partOf/fhir:display/@value"/></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </xsl:if>
    </xsl:template>

    <!--	****************************** END OF ENCOUNTER FULL ******************************	-->

    <!--	****************************** START OF OBSERVATION FULL ******************************	-->

    <xsl:template name="observation-full">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation)">
            <div id="observation" class="banner">
                <h1>Observation</h1>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Identifier: </strong></th>
                            <th><strong>Use: </strong></th>
                            <th><strong>Type: </strong></th>
                            <th><strong>System: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                            <th><strong>Assigner: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:identifier">
                            <tr>
                                <td><xsl:value-of select="fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:use/@value"/></td>
                                <td><xsl:value-of select="fhir:type/@value"/></td>
                                <td><xsl:value-of select="fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                                <td><xsl:value-of select="fhir:assigner/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Status: </strong></th>
                            <th><strong>Subject Reference: </strong></th>
                            <th><strong>Subject Display: </strong></th>
                            <th><strong>Encounter Reference: </strong></th>
                            <th><strong>Encounter Display: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:status/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:subject/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:subject/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:encounter/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:encounter/fhir:display/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Category System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:category/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:category/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:category/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:category/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:category/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:category/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Code System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:code/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:code/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:code/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:code/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:code/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:code/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>effectiveDateTime: </strong></th>
                            <th><strong>effectivePeriod Start: </strong></th>
                            <th><strong>effectivePeriod End: </strong></th>
                            <th><strong>Issued: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:effectiveDateTime/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:effectivePeriod/fhir:start/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:effectivePeriod/fhir:end/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:issued/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Performer reference: </strong></th>
                            <th><strong>Display: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:performer">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <strong>valueQuantity</strong>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>value: </strong></th>
                            <th><strong>comparator: </strong></th>
                            <th><strong>unit: </strong></th>
                            <th><strong>system: </strong></th>
                            <th><strong>code: </strong></th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueQuantity/fhir:value/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueQuantity/fhir:comparator/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueQuantity/fhir:unit/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueQuantity/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueQuantity/fhir:code/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>valueCodeableConcept</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>Code System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueCodeableConcept/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueCodeableConcept/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueCodeableConcept/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueCodeableConcept/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueCodeableConcept/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueCodeableConcept/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>valueString</th>
                            <th>valueRange low</th>
                            <th>valueRange high</th>
                            <th>valueRatio numerator</th>
                            <th>valueRatio denominator</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueString/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueRange/fhir:low/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueRange/fhir:high/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueRatio/fhir:numerator/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueRatio/fhir:denominator/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>valueSampledData</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>origin</th>
                            <th>period</th>
                            <th>factor</th>
                            <th>lowerLimit</th>
                            <th>upperLimit</th>
                            <th>dimensions</th>
                            <th>data</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueSampledData/fhir:origin/fhir:value/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueSampledData/fhir:period/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueSampledData/fhir:factor/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueSampledData/fhir:lowerLimit/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueSampledData/fhir:upperLimit/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueSampledData/fhir:dimensions/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueSampledData/fhir:data/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>valueAttachment</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>contentType</th>
                            <th>language</th>
                            <th>data</th>
                            <th>url</th>
                            <th>size</th>
                            <th>hash</th>
                            <th>title</th>
                            <th>creation</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueAttachment/fhir:contentType/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueAttachment/fhir:language/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueAttachment/fhir:data/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueAttachment/fhir:url/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueAttachment/fhir:size/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueAttachment/fhir:hash/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueAttachment/fhir:title/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueAttachment/fhir:creation/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>valueTime</th>
                            <th>valueDateTime</th>
                            <th>valuePeriod start</th>
                            <th>valuePeriod end</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueTime/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valueDateTime/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valuePeriod/fhir:start/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:valuePeriod/fhir:end/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>dataAbsentReason</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>Code System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:dataAbsentReason/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:dataAbsentReason/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:dataAbsentReason/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:dataAbsentReason/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:dataAbsentReason/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:dataAbsentReason/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>interpretation</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>Code System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:interpretation/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:interpretation/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:interpretation/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:interpretation/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:interpretation/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:interpretation/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <td><strong>Comments: </strong> <xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:comments/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>bodySite</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>Code System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:bodySite/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:bodySite/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:bodySite/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:bodySite/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:bodySite/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:bodySite/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>method</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>Code System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:method/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:method/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:method/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:method/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:method/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:method/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Specimen reference: </strong></th>
                            <th><strong>Display: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:specimen">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Device reference: </strong></th>
                            <th><strong>Display: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:device">
                            <tr>
                                <td><xsl:value-of select="fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:display/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <strong>referenceRange</strong>
                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:referenceRange">
                    <table>
                        <tbody>
                            <tr>
                                <th>low value</th>
                                <th>comparator</th>
                                <th>unit</th>
                                <th>system</th>
                                <th>code</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:low/fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:low/fhir:comparator/@value"/></td>
                                <td><xsl:value-of select="fhir:low/fhir:unit/@value"/></td>
                                <td><xsl:value-of select="fhir:low/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:low/fhir:code/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th>high value</th>
                                <th>comparator</th>
                                <th>unit</th>
                                <th>system</th>
                                <th>code</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:high/fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:high/fhir:comparator/@value"/></td>
                                <td><xsl:value-of select="fhir:high/fhir:unit/@value"/></td>
                                <td><xsl:value-of select="fhir:high/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:high/fhir:code/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th>Meaning System</th>
                                <th>Version</th>
                                <th>Code</th>
                                <th>Display</th>
                                <th>userSelected</th>
                                <th>Text</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:meaning/fhir:text/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th>Age low value</th>
                                <th>comparator</th>
                                <th>unit</th>
                                <th>system</th>
                                <th>code</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:age/fhir:low/fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:age/fhir:low/fhir:comparator/@value"/></td>
                                <td><xsl:value-of select="fhir:age/fhir:low/fhir:unit/@value"/></td>
                                <td><xsl:value-of select="fhir:age/fhir:low/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:age/fhir:low/fhir:code/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th>Age high value</th>
                                <th>comparator</th>
                                <th>unit</th>
                                <th>system</th>
                                <th>code</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:age/fhir:high/fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:age/fhir:high/fhir:comparator/@value"/></td>
                                <td><xsl:value-of select="fhir:age/fhir:high/fhir:unit/@value"/></td>
                                <td><xsl:value-of select="fhir:age/fhir:high/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:age/fhir:high/fhir:code/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <td><strong>Text: </strong> <xsl:value-of select="fhir:text/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                </xsl:for-each>
                <hr></hr>
                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:related">
                    <table>
                        <tbody>
                            <tr>
                                <th>Related type</th>
                                <th>Target reference</th>
                                <th>Display</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:type/@value"/></td>
                                <td><xsl:value-of select="fhir:target/fhir:reference/@value"/></td>
                                <td><xsl:value-of select="fhir:target/fhir:display/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                </xsl:for-each>
                <hr></hr>
                <strong>Component</strong>
                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Observation/fhir:component">
                    <table>
                        <tbody>
                            <tr>
                                <th>Code System</th>
                                <th>Version</th>
                                <th>Code</th>
                                <th>Display</th>
                                <th>userSelected</th>
                                <th>Text</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:code/fhir:text/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <strong>valueQuantity</strong>
                    <table>
                        <tbody>
                            <tr>
                                <th><strong>value: </strong></th>
                                <th><strong>comparator: </strong></th>
                                <th><strong>unit: </strong></th>
                                <th><strong>system: </strong></th>
                                <th><strong>code: </strong></th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:valueQuantity/fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:valueQuantity/fhir:comparator/@value"/></td>
                                <td><xsl:value-of select="fhir:valueQuantity/fhir:unit/@value"/></td>
                                <td><xsl:value-of select="fhir:valueQuantity/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:valueQuantity/fhir:code/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <strong>valueCodeableConcept</strong>
                    <table>
                        <tbody>
                            <tr>
                                <th>Code System</th>
                                <th>Version</th>
                                <th>Code</th>
                                <th>Display</th>
                                <th>userSelected</th>
                                <th>Text</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:valueCodeableConcept/fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:valueCodeableConcept/fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:valueCodeableConcept/fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:valueCodeableConcept/fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:valueCodeableConcept/fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:valueCodeableConcept/fhir:text/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th>valueString</th>
                                <th>valueRange low</th>
                                <th>valueRange high</th>
                                <th>valueRatio numerator</th>
                                <th>valueRatio denominator</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:valueString/@value"/></td>
                                <td><xsl:value-of select="fhir:valueRange/fhir:low/@value"/></td>
                                <td><xsl:value-of select="fhir:valueRange/fhir:high/@value"/></td>
                                <td><xsl:value-of select="fhir:valueRatio/fhir:numerator/@value"/></td>
                                <td><xsl:value-of select="fhir:valueRatio/fhir:denominator/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <strong>valueSampledData</strong>
                    <table>
                        <tbody>
                            <tr>
                                <th>origin</th>
                                <th>period</th>
                                <th>factor</th>
                                <th>lowerLimit</th>
                                <th>upperLimit</th>
                                <th>dimensions</th>
                                <th>data</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:valueSampledData/fhir:origin/fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:valueSampledData/fhir:period/@value"/></td>
                                <td><xsl:value-of select="fhir:valueSampledData/fhir:factor/@value"/></td>
                                <td><xsl:value-of select="fhir:valueSampledData/fhir:lowerLimit/@value"/></td>
                                <td><xsl:value-of select="fhir:valueSampledData/fhir:upperLimit/@value"/></td>
                                <td><xsl:value-of select="fhir:valueSampledData/fhir:dimensions/@value"/></td>
                                <td><xsl:value-of select="fhir:valueSampledData/fhir:data/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <strong>valueAttachment</strong>
                    <table>
                        <tbody>
                            <tr>
                                <th>contentType</th>
                                <th>language</th>
                                <th>data</th>
                                <th>url</th>
                                <th>size</th>
                                <th>hash</th>
                                <th>title</th>
                                <th>creation</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:valueAttachment/fhir:contentType/@value"/></td>
                                <td><xsl:value-of select="fhir:valueAttachment/fhir:language/@value"/></td>
                                <td><xsl:value-of select="fhir:valueAttachment/fhir:data/@value"/></td>
                                <td><xsl:value-of select="fhir:valueAttachment/fhir:url/@value"/></td>
                                <td><xsl:value-of select="fhir:valueAttachment/fhir:size/@value"/></td>
                                <td><xsl:value-of select="fhir:valueAttachment/fhir:hash/@value"/></td>
                                <td><xsl:value-of select="fhir:valueAttachment/fhir:title/@value"/></td>
                                <td><xsl:value-of select="fhir:valueAttachment/fhir:creation/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <table>
                        <tbody>
                            <tr>
                                <th>valueTime</th>
                                <th>valueDateTime</th>
                                <th>valuePeriod start</th>
                                <th>valuePeriod end</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:valueTime/@value"/></td>
                                <td><xsl:value-of select="fhir:valueDateTime/@value"/></td>
                                <td><xsl:value-of select="fhir:valuePeriod/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:valuePeriod/fhir:end/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <strong>dataAbsentReason</strong>
                    <table>
                        <tbody>
                            <tr>
                                <th>Code System</th>
                                <th>Version</th>
                                <th>Code</th>
                                <th>Display</th>
                                <th>userSelected</th>
                                <th>Text</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="fhir:dataAbsentReason/fhir:coding/fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:dataAbsentReason/fhir:coding/fhir:version/@value"/></td>
                                <td><xsl:value-of select="fhir:dataAbsentReason/fhir:coding/fhir:code/@value"/></td>
                                <td><xsl:value-of select="fhir:dataAbsentReason/fhir:coding/fhir:display/@value"/></td>
                                <td><xsl:value-of select="fhir:dataAbsentReason/fhir:coding/fhir:userSelected/@value"/></td>
                                <td><xsl:value-of select="fhir:dataAbsentReason/fhir:text/@value"/></td>
                            </tr>
                        </tbody>
                    </table>
                    <hr></hr>
                    <strong>referenceRange</strong>
                    <xsl:for-each select="fhir:referenceRange">
                        <table>
                            <tbody>
                                <tr>
                                    <th>low value</th>
                                    <th>comparator</th>
                                    <th>unit</th>
                                    <th>system</th>
                                    <th>code</th>
                                </tr>
                                <tr>
                                    <td><xsl:value-of select="fhir:low/fhir:value/@value"/></td>
                                    <td><xsl:value-of select="fhir:low/fhir:comparator/@value"/></td>
                                    <td><xsl:value-of select="fhir:low/fhir:unit/@value"/></td>
                                    <td><xsl:value-of select="fhir:low/fhir:system/@value"/></td>
                                    <td><xsl:value-of select="fhir:low/fhir:code/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                        <hr></hr>
                        <table>
                            <tbody>
                                <tr>
                                    <th>high value</th>
                                    <th>comparator</th>
                                    <th>unit</th>
                                    <th>system</th>
                                    <th>code</th>
                                </tr>
                                <tr>
                                    <td><xsl:value-of select="fhir:high/fhir:value/@value"/></td>
                                    <td><xsl:value-of select="fhir:high/fhir:comparator/@value"/></td>
                                    <td><xsl:value-of select="fhir:high/fhir:unit/@value"/></td>
                                    <td><xsl:value-of select="fhir:high/fhir:system/@value"/></td>
                                    <td><xsl:value-of select="fhir:high/fhir:code/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                        <hr></hr>
                        <table>
                            <tbody>
                                <tr>
                                    <th>Meaning System</th>
                                    <th>Version</th>
                                    <th>Code</th>
                                    <th>Display</th>
                                    <th>userSelected</th>
                                    <th>Text</th>
                                </tr>
                                <tr>
                                    <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:system/@value"/></td>
                                    <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:version/@value"/></td>
                                    <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:code/@value"/></td>
                                    <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:display/@value"/></td>
                                    <td><xsl:value-of select="fhir:meaning/fhir:coding/fhir:userSelected/@value"/></td>
                                    <td><xsl:value-of select="fhir:meaning/fhir:text/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                        <hr></hr>
                        <table>
                            <tbody>
                                <tr>
                                    <th>Age low value</th>
                                    <th>comparator</th>
                                    <th>unit</th>
                                    <th>system</th>
                                    <th>code</th>
                                </tr>
                                <tr>
                                    <td><xsl:value-of select="fhir:age/fhir:low/fhir:value/@value"/></td>
                                    <td><xsl:value-of select="fhir:age/fhir:low/fhir:comparator/@value"/></td>
                                    <td><xsl:value-of select="fhir:age/fhir:low/fhir:unit/@value"/></td>
                                    <td><xsl:value-of select="fhir:age/fhir:low/fhir:system/@value"/></td>
                                    <td><xsl:value-of select="fhir:age/fhir:low/fhir:code/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                        <hr></hr>
                        <table>
                            <tbody>
                                <tr>
                                    <th>Age high value</th>
                                    <th>comparator</th>
                                    <th>unit</th>
                                    <th>system</th>
                                    <th>code</th>
                                </tr>
                                <tr>
                                    <td><xsl:value-of select="fhir:age/fhir:high/fhir:value/@value"/></td>
                                    <td><xsl:value-of select="fhir:age/fhir:high/fhir:comparator/@value"/></td>
                                    <td><xsl:value-of select="fhir:age/fhir:high/fhir:unit/@value"/></td>
                                    <td><xsl:value-of select="fhir:age/fhir:high/fhir:system/@value"/></td>
                                    <td><xsl:value-of select="fhir:age/fhir:high/fhir:code/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                        <hr></hr>
                        <table>
                            <tbody>
                                <tr>
                                    <td><strong>Text: </strong> <xsl:value-of select="fhir:text/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                    </xsl:for-each>
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>

    <!--	****************************** END OF OBSERVATION FULL ******************************	-->

    <!--	****************************** START OF MEDICATION ORDER FULL ******************************	-->

    <xsl:template name="medicationorder-full">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder)">
            <div id="medicationorder" class="banner">
                <h1>Medication Order</h1>
                <table>
                    <tbody>
                        <tr>
                            <th><strong>Identifier: </strong></th>
                            <th><strong>Use: </strong></th>
                            <th><strong>Type: </strong></th>
                            <th><strong>System: </strong></th>
                            <th><strong>Period Start: </strong></th>
                            <th><strong>Period End: </strong></th>
                            <th><strong>Assigner: </strong></th>
                        </tr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:identifier">
                            <tr>
                                <td><xsl:value-of select="fhir:value/@value"/></td>
                                <td><xsl:value-of select="fhir:use/@value"/></td>
                                <td><xsl:value-of select="fhir:type/@value"/></td>
                                <td><xsl:value-of select="fhir:system/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:start/@value"/></td>
                                <td><xsl:value-of select="fhir:period/fhir:end/@value"/></td>
                                <td><xsl:value-of select="fhir:assigner/@value"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>dateWritten</th>
                            <th>status</th>
                            <th>dateEnded</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:dateWritten/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:status/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:dateEnded/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>reasonEnded</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>Code System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonEnded/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonEnded/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonEnded/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonEnded/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonEnded/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonEnded/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>Patient reference</th>
                            <th>Display</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:patient/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:patient/fhir:display/@value"/></td>
                        </tr>
                        <tr>
                            <th>Prescriber reference</th>
                            <th>Display</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:prescriber/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:prescriber/fhir:display/@value"/></td>
                        </tr>
                        <tr>
                            <th>Encounter reference</th>
                            <th>Display</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:encounter/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:encounter/fhir:display/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <strong>reasonCodeableConcept</strong>
                <table>
                    <tbody>
                        <tr>
                            <th>Code System</th>
                            <th>Version</th>
                            <th>Code</th>
                            <th>Display</th>
                            <th>userSelected</th>
                            <th>Text</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonCodeableConcept/fhir:coding/fhir:system/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonCodeableConcept/fhir:coding/fhir:version/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonCodeableConcept/fhir:coding/fhir:code/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonCodeableConcept/fhir:coding/fhir:display/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonCodeableConcept/fhir:coding/fhir:userSelected/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonCodeableConcept/fhir:text/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>reasonReference reference</th>
                            <th>Display</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonReference/fhir:reference/@value"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:reasonReference/fhir:display/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>
                <table>
                    <tbody>
                        <tr>
                            <th>note</th>
                        </tr>
                        <tr>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationOrder/fhir:note/@value"/></td>
                        </tr>
                    </tbody>
                </table>
                <hr></hr>

            </div>
        </xsl:if>
    </xsl:template>

    <!--	****************************** END OF MEDICATION ORDER FULL ******************************	-->

    <!--	****************************** START OF MEDICATION STATEMENT FULL ******************************	-->

    <xsl:template name="medicationstatement-full">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:MedicationStatement)">
            <div id="medicationstatement" class="banner">
                <h1>Medication Statement</h1>
            </div>
        </xsl:if>
    </xsl:template>

    <!--	****************************** END OF MEDICATION STATEMENT FULL ******************************	-->

    <!--	****************************** START OF ALLERGY INTOLERANCE FULL ******************************	-->

    <xsl:template name="allergyintolerance-full">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:AllergyIntolerance)">
            <div id="allergyintolerance" class="banner">
                <h1>Allergy Intolerance</h1>
            </div>
        </xsl:if>
    </xsl:template>

    <!--	****************************** END OF ALLERGY INTOLERANCE FULL ******************************	-->

    <!--	****************************** START OF PATIENT HEADER PARTIAL ******************************	-->
    <xsl:template name="patient-header-partial">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient)">
            <h1>Patient Demographics</h1>
            <div id="patient-header-partial" class="banner-partial-cream">
                <table>
                    <tbody>
                        <tr>
                            <th>Patient name</th>
                            <th>Date of birth</th>
                            <th>Gender</th>
                            <th>Ethnicity:</th>
                            <th>Identifier</th>
                        </tr>
                        <tr>
                            <td>

                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:name">
                                    <span class="label">(<xsl:value-of select="fhir:use/@value"/>)</span>&#160;<xsl:value-of select="fhir:family/@value"/>, <xsl:value-of select="fhir:given/@value"/>

                                    <xsl:choose>
                                        <xsl:when test="fhir:prefix/@value != '' ">
                                            (<xsl:value-of select="fhir:prefix/@value"/>)
                                        </xsl:when>
                                    </xsl:choose>
                                    <br />
                                </xsl:for-each>
                            </td>
                            <td><xsl:value-of select="format-date(fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:birthDate/@value, '[D01]-[MNn,*-3]-[Y0001]', 'en', (), ())"/></td>
                            <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:gender/@value"/></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:extension">
                                    <xsl:choose>
                                        <xsl:when test="contains(lower-case(@url),'ethnic')">
                                            <xsl:value-of select="fhir:valueCodeableConcept/fhir:coding/fhir:display/@value"/>
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                            </td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:identifier">
                                    <xsl:choose>
                                        <xsl:when test="fhir:extension/fhir:valueCodeableConcept/fhir:coding/fhir:display/@value != ' ' ">
                                            <span class="label"><xsl:value-of select="fhir:extension/fhir:valueCodeableConcept/fhir:coding/fhir:display/@value"/></span><br/>
                                            <xsl:value-of select="fhir:value/@value"/><br/><br/>
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:identifier">
                                    <xsl:choose>
                                        <xsl:when test="fhir:system/@value = 'http://fhir.nhs.uk/Id/local-identifier' ">
                                            <span class="label"><xsl:value-of select="fhir:system/@value"/></span><br/>
                                            <xsl:value-of select="fhir:value/@value"/><br/>
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:identifier">
                                    <xsl:choose>
                                        <xsl:when test="fhir:system/@value = 'http://fhir.nhs.uk/Id/local-patient-identifier' ">
                                            <span class="label"><xsl:value-of select="fhir:system/@value"/></span><br/>
                                            <xsl:value-of select="fhir:value/@value"/><br/>
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <p></p>
                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient">
                    <table>
                        <tbody>
                            <tr>
                                <xsl:for-each select="fhir:address">
                                    <td>
                                        <table>
                                            <tbody>
                                                <tr>
                                                    <td>
                                                        <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' "><xsl:text> </xsl:text></xsl:if></xsl:variable>
                                                        <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper,$lower)" /></xsl:variable>
                                                        <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                                        <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/>address</span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <xsl:value-of select="fhir:text/@value"/>
                                                    </td>
                                                </tr>
                                                <xsl:for-each select="fhir:line">
                                                    <tr>
                                                        <td>
                                                            <xsl:value-of select="@value"/></td></tr>
                                                </xsl:for-each>
                                                <tr>
                                                    <td>
                                                        <xsl:value-of select="fhir:city/@value"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <xsl:value-of select="fhir:district/@value"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <xsl:value-of select="fhir:state/@value"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <xsl:value-of select="fhir:postalCode/@value"/>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </td>
                                </xsl:for-each>
                                <td>
                                    <xsl:for-each select="fhir:telecom">
                                        <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' and fhir:system/@value != ''"><xsl:text> </xsl:text></xsl:if><xsl:value-of select="fhir:system/@value"/>:<xsl:text> </xsl:text></xsl:variable>
                                        <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper, $lower)" /></xsl:variable>
                                        <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                        <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/></span>
                                        <xsl:value-of select="fhir:value/@value"/><br/>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </xsl:for-each>

            </div>

            <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:careProvider/fhir:reference)">
                <h1>GP Practice</h1>
                <div id="patient-header-partial" class="banner-partial-cream">
                    <table>
                        <tbody>
                            <tr>
                                <th></th>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization">
                                        <xsl:variable name="concatOrg"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>
                                        <xsl:choose>
                                            <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:careProvider/fhir:reference/@value = $concatOrg">

                                                <table>
                                                    <tbody>
                                                        <tr>
                                                            <td>
                                                                <xsl:value-of select="fhir:name/@value"></xsl:value-of>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <xsl:for-each select="fhir:address">
                                                                <td>
                                                                    <table>
                                                                        <tr>
                                                                            <td>
                                                                                <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' "><xsl:text> </xsl:text></xsl:if>address</xsl:variable>
                                                                                <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper, $lower)" /></xsl:variable>
                                                                                <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                                                                <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/></span>
                                                                            </td>
                                                                        </tr>
                                                                        <tr>
                                                                            <td>
                                                                                <xsl:value-of select="fhir:text/@value"/>
                                                                            </td>
                                                                        </tr>
                                                                        <xsl:for-each select="fhir:line">
                                                                            <tr>
                                                                                <td>
                                                                                    <xsl:value-of select="@value"/>
                                                                                </td>
                                                                            </tr>
                                                                        </xsl:for-each>
                                                                        <tr><td><xsl:value-of select="fhir:city/@value"/></td></tr>
                                                                        <tr><td><xsl:value-of select="fhir:district/@value"/></td></tr>
                                                                        <tr><td><xsl:value-of select="fhir:state/@value"/></td></tr>
                                                                        <tr><td><xsl:value-of select="fhir:postalCode/@value"/></td></tr>
                                                                    </table>
                                                                </td>
                                                            </xsl:for-each>
                                                            <td>
                                                                <xsl:for-each select="fhir:telecom">
                                                                    <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' and fhir:system/@value != ''"><xsl:text> </xsl:text></xsl:if><xsl:value-of select="fhir:system/@value"/>:<xsl:text> </xsl:text></xsl:variable>
                                                                    <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper, $lower)" /></xsl:variable>
                                                                    <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                                                    <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/></span>
                                                                    <xsl:value-of select="fhir:value/@value"/><br/>
                                                                </xsl:for-each>
                                                            </td>
                                                        </tr>
                                                    </tbody>
                                                </table>
                                            </xsl:when>
                                        </xsl:choose>
                                    </xsl:for-each>

                                    <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner">
                                        <xsl:variable name="concatPractitioner"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>
                                        <xsl:choose>
                                            <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:careProvider/fhir:reference/@value = $concatPractitioner"><xsl:value-of select="fhir:name/@value"></xsl:value-of>
                                                <table>
                                                    <tbody>
                                                        <tr>
                                                            <xsl:for-each select="fhir:address">
                                                                <td>
                                                                    <table>
                                                                        <tr><td>
                                                                            <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' "><xsl:text> </xsl:text></xsl:if>address</xsl:variable>
                                                                            <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper, $lower)" /></xsl:variable>
                                                                            <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                                                            <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/></span>
                                                                        </td></tr>
                                                                        <tr><td><xsl:value-of select="fhir:text/@value"/></td></tr>
                                                                        <xsl:for-each select="fhir:line">
                                                                            <tr><td><xsl:value-of select="@value"/></td></tr>
                                                                        </xsl:for-each>
                                                                        <tr><td><xsl:value-of select="fhir:city/@value"/></td></tr>
                                                                        <tr><td><xsl:value-of select="fhir:district/@value"/></td></tr>
                                                                        <tr><td><xsl:value-of select="fhir:state/@value"/></td></tr>
                                                                        <tr><td><xsl:value-of select="fhir:postalCode/@value"/></td></tr>
                                                                    </table>
                                                                </td>
                                                            </xsl:for-each>
                                                            <td>
                                                                <xsl:for-each select="fhir:telecom">
                                                                    <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' and fhir:system/@value != ''"><xsl:text> </xsl:text></xsl:if><xsl:value-of select="fhir:system/@value"/>:<xsl:text> </xsl:text></xsl:variable>
                                                                    <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper, $lower)" /></xsl:variable>
                                                                    <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                                                    <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/></span>
                                                                    <xsl:value-of select="fhir:value/@value"/><br/>
                                                                </xsl:for-each>
                                                            </td>
                                                        </tr>
                                                    </tbody>
                                                </table>
                                            </xsl:when>
                                        </xsl:choose>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p></p>
                    <table>
                        <tbody>
                            <tr>
                                <td>
                                    <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization">
                                        <xsl:variable name="concatOrg"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>

                                        <xsl:choose>
                                            <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient/fhir:careProvider/fhir:reference/@value = $concatOrg">
                                                <table>
                                                    <tbody>
                                                        <tr>
                                                            <th>GP Practice Code</th>
                                                        </tr>
                                                        <tr>
                                                            <td><xsl:value-of select="fhir:identifier/fhir:value/@value"></xsl:value-of></td>
                                                        </tr>
                                                    </tbody>
                                                </table>
                                            </xsl:when>
                                        </xsl:choose>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <table>
                        <tbody>
                            <tr>
                                <td>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

            </xsl:if>

        </xsl:if>
    </xsl:template>
    <!--	****************************** END OF PATIENT HEADER PARTIAL ******************************	-->

    <!--	****************************** START OF CLINICAL DOCUMENT COMPOSITION PARTIAL ******************************	-->
    <xsl:template name="clinical-document-partial">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition)">
            <div id="clinical-document-partial" class="banner-partial-cream">
                <table>
                    <tbody>
                        <tr>
                            <td><span class="label">Document Created</span></td><td><xsl:value-of  select="format-dateTime(fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:date/@value, '[D01]-[MNn,*-3]-[Y0001] [H01]:[m01]', 'en', (), ())"/></td>
                        </tr>
                        <tr>
                            <td><span class="label">Document Owner</span></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization">
                                    <xsl:variable name="custodianOrg"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>

                                    <xsl:choose>
                                        <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:custodian/fhir:reference/@value = $custodianOrg"><xsl:value-of select="fhir:name/@value"></xsl:value-of></xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                            </td>
                        </tr>
                        <tr>
                            <td><span class="label">Authored By</span></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner">
                                    <xsl:variable name="authorPrac"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:author/fhir:reference/@value = $authorPrac"><xsl:value-of select="fhir:name/fhir:family/@value"/>, <xsl:value-of select="fhir:name/fhir:given/@value"/>
                                            <xsl:choose>
                                                <xsl:when test="fhir:name/fhir:prefix/@value != '' ">
                                                    (<xsl:value-of select="fhir:name/fhir:prefix/@value"/>)
                                                </xsl:when>
                                            </xsl:choose>
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="banner-partial-violet">
                <h2>Encounter Summary</h2>

                <xsl:choose>
                    <xsl:when test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:type/fhir:coding/fhir:display/@value != '' ">
                        <table>
                            <tbody>
                                <tr>
                                    <td><span class="label">Encounter Type </span></td>
                                    <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:type/fhir:coding/fhir:display/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                    </xsl:when>
                </xsl:choose>

                <xsl:choose>
                    <xsl:when test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:type/fhir:coding/fhir:display/@value != '' ">
                        <table>
                            <tbody>
                                <tr>
                                    <td><span class="label">Encounter Time </span></td>
                                    <td><xsl:value-of select="format-dateTime(fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:period/fhir:start/@value, '[D01]-[MNn,*-3]-[Y0001] [H01]:[m01]', 'en', (), ())"/> to <xsl:value-of select="format-dateTime(fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:period/fhir:end/@value, '[D01]-[MNn,*-3]-[Y0001] [H01]:[m01]', 'en', (), ())"/></td>
                                </tr>
                            </tbody>
                        </table>
                    </xsl:when>
                </xsl:choose>

                <xsl:choose>
                    <xsl:when test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:type/fhir:coding/fhir:display/@value != '' ">
                        <table>
                            <tbody>
                                <tr>
                                    <td><span class="label">Encounter Disposition </span></td>
                                    <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dischargeDisposition/fhir:coding/fhir:display/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                    </xsl:when>
                </xsl:choose>

                <table>
                    <tbody>
                        <tr>
                            <td><span class="label">Care Setting Type </span></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension">
                                    <xsl:choose>
                                        <xsl:when test="@url='https://fhir.nhs.uk/StructureDefinition/extension-cofe-care-setting-type-1' or 'https://fhir.nhs.uk/StructureDefinition/extension-itk-care-setting-type-1' ">
                                            <xsl:value-of select="fhir:valueCodeableConcept/fhir:coding/fhir:display/@value"/>
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <xsl:choose>
                <xsl:when test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:participant/fhir:individual != '' ">
                    <div class="banner-partial-cream">
                        <table>
                            <tbody>
                                <tr>
                                    <td><span class="bold">Other participant(s) in this document</span></td>
                                    <td></td>
                                </tr>
                            </tbody>
                        </table>
                        <table>
                            <tbody>
                                <tr>
                                    <td>
                                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:participant/fhir:individual">
                                            <xsl:variable name="encounterpractitioner"><xsl:value-of select="fhir:reference/@value"/></xsl:variable>
                                            <xsl:for-each select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:identifier">
                                                <xsl:variable name="practionerlinked"><xsl:value-of select="fhir:system/@value"/>/<xsl:value-of select="fhir:value/@value"/></xsl:variable>
                                                <xsl:choose>
                                                    <xsl:when test="$practionerlinked=$encounterpractitioner">
                                                        <xsl:choose>
                                                            <xsl:when test="$practionerlinked=$encounterpractitioner">
                                                                <table>
                                                                    <tbody>
                                                                        <tr>
                                                                            <td><span class="bold-italic">Participant</span></td>
                                                                            <td></td>
                                                                        </tr>
                                                                        <tr>
                                                                            <td><span class="label">Participant Name </span><xsl:value-of select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:family/@value"/>, <xsl:value-of select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:given/@value"/>
                                                                                <xsl:choose>
                                                                                    <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:prefix/@value != '' ">
                                                                                        (<xsl:value-of select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:prefix/@value"/>)
                                                                                    </xsl:when>
                                                                                </xsl:choose>

                                                                            </td>
                                                                            <td></td>
                                                                        </tr>
                                                                    </tbody>
                                                                </table>
                                                                <table>
                                                                    <tbody>
                                                                        <tr>
                                                                            <xsl:for-each select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:address">
                                                                                <td>
                                                                                    <!--															<xsl:variable name="practioneraddressuse"><xsl:value-of select="fhir:use/@value"/></xsl:variable>
																<xsl:choose>
																	<xsl:when test="$practioneraddressuse='work'">-->
                                                                                    <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' "><xsl:text> </xsl:text></xsl:if>address</xsl:variable>
                                                                                    <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper, $lower)" /></xsl:variable>
                                                                                    <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                                                                    <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/></span><br/>
                                                                                    <xsl:value-of select="fhir:line/@value"/><br/>
                                                                                    <xsl:value-of select="fhir:city/@value"/><br/>
                                                                                    <xsl:value-of select="fhir:state/@value"/><br/>
                                                                                    <xsl:value-of select="fhir:postalCode/@value"/>
                                                                                    <!--																	</xsl:when>
																</xsl:choose>-->
                                                                                </td>
                                                                            </xsl:for-each>
                                                                            <td>
                                                                                <xsl:for-each select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:telecom">
                                                                                    <!--																	<xsl:variable name="practionertelecomuse"><xsl:value-of select="fhir:use/@value"/></xsl:variable>
																		<xsl:choose>
																			<xsl:when test="$practionertelecomuse='work'">-->

                                                                                    <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' and fhir:system/@value != ''"><xsl:text> </xsl:text></xsl:if><xsl:value-of select="fhir:system/@value"/>:<xsl:text> </xsl:text></xsl:variable>
                                                                                    <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper, $lower)" /></xsl:variable>
                                                                                    <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                                                                    <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/></span>

                                                                                    <xsl:value-of select="fhir:value/@value"/><br/>
                                                                                    <!--																		</xsl:when>
																		</xsl:choose>-->
                                                                                </xsl:for-each>
                                                                            </td>
                                                                        </tr>
                                                                    </tbody>
                                                                </table>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:when>
                                                </xsl:choose>
                                            </xsl:for-each>
                                        </xsl:for-each>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </xsl:when>
            </xsl:choose>

            <div class="banner-partial-gray">
                <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition)">
                    <p>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:section">
                            <h1><xsl:value-of select="fhir:title/@value"/></h1>
                            <xsl:copy-of select="fhir:text/." />
                            <hr></hr>
                        </xsl:for-each>
                        <hr></hr>
                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:section/fhir:section">
                            <h1><xsl:value-of select="fhir:title/@value"/></h1><!--Sub Section title -->
                            <xsl:copy-of select="fhir:text/."/>
                            <hr></hr>
                        </xsl:for-each>
                    </p>
                </xsl:if>
            </div>



            <xsl:choose>
                <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url  != '' ">
                    <h1>Distribution list</h1>
                    <div class="banner-partial-cream">
                        <table>
                            <tbody>
                                <tr>
                                    <th><span class="label">Recipient name</span></th>
                                    <th><span class="label">Job role</span></th>
                                    <th><span class="label">Phone</span></th>
                                </tr>
                            </tbody>
                        </table>

                        <table>
                            <tbody>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient">
                                    <xsl:variable name="primaryrecipPatient"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>
                                    <tr>
                                        <td>

                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPatient) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">

                                                    <xsl:for-each select="fhir:name">
                                                        <xsl:choose>
                                                            <xsl:when test="(fhir:use/@value = 'official' or fhir:use/@value = 'usual') ">
                                                                <span class="label">(<xsl:value-of select="fhir:use/@value"/>)</span>&#160;<xsl:value-of select="fhir:family/@value"/>, <xsl:value-of select="fhir:given/@value"/>
                                                                <xsl:choose>
                                                                    <xsl:when test="fhir:prefix/@value != '' ">
                                                                        (<xsl:value-of select="fhir:prefix/@value"/>)
                                                                    </xsl:when>
                                                                </xsl:choose>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPatient) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    Patient
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPatient) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    <xsl:for-each select="fhir:telecom">
                                                        <xsl:choose>
                                                            <xsl:when test="fhir:system/@value = 'phone' ">
                                                                <xsl:value-of select="fhir:value/@value"/>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </xsl:for-each>





                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:RelatedPerson">
                                    <xsl:variable name="primaryrecipRelatedPerson"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>
                                    <tr>
                                        <td>

                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipRelatedPerson) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">

                                                    <xsl:for-each select="fhir:name">
                                                        <xsl:choose>
                                                            <xsl:when test="fhir:use/@value = 'official' ">
                                                                <span class="label">(<xsl:value-of select="fhir:use/@value"/>)</span>&#160;<xsl:value-of select="fhir:family/@value"/>, <xsl:value-of select="fhir:given/@value"/>
                                                                <xsl:choose>
                                                                    <xsl:when test="fhir:prefix/@value != '' ">
                                                                        (<xsl:value-of select="fhir:prefix/@value"/>)
                                                                    </xsl:when>
                                                                </xsl:choose>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipRelatedPerson) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    Related Person
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipRelatedPerson) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    <xsl:for-each select="fhir:telecom">
                                                        <xsl:choose>
                                                            <xsl:when test="fhir:system/@value = 'phone' ">
                                                                <xsl:value-of select="fhir:value/@value"/>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </xsl:for-each>










                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner">
                                    <xsl:variable name="primaryrecipPrac"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>
                                    <tr>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    <xsl:value-of select="fhir:name/fhir:family/@value"/>, <xsl:value-of select="fhir:name/fhir:given/@value"/>
                                                    <xsl:choose>
                                                        <xsl:when test="fhir:name/fhir:prefix/@value != '' ">
                                                            (<xsl:value-of select="fhir:name/fhir:prefix/@value"/>)
                                                        </xsl:when>
                                                    </xsl:choose>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:for-each select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:PractitionerRole">
                                                <xsl:choose>
                                                    <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                        <xsl:choose>
                                                            <xsl:when test="(fhir:practitioner/fhir:reference/@value = $primaryrecipPrac ) ">
                                                                <xsl:value-of select="fhir:code/fhir:coding/fhir:display/@value"/>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:when>
                                                </xsl:choose>
                                            </xsl:for-each>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    <xsl:for-each select="fhir:telecom">
                                                        <xsl:choose>
                                                            <xsl:when test="fhir:system/@value = 'phone' ">
                                                                <xsl:value-of select="fhir:value/@value"/>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </xsl:for-each>



                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization">
                                    <xsl:variable name="primaryrecipPrac"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                            <tr>
                                                <th><span class="label">Organisation name</span></th>
                                                <th><span class="label">Phone</span></th>
                                            </tr>
                                        </xsl:when>
                                    </xsl:choose>
                                    <tr>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) "><xsl:value-of select="fhir:name/@value"/>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:Composition/fhir:extension/@url = 'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    <xsl:for-each select="fhir:telecom">
                                                        <xsl:choose>
                                                            <xsl:when test="fhir:system/@value = 'phone' ">
                                                                <xsl:value-of select="fhir:value/@value"/>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </tbody>
                        </table>
                    </div>
                </xsl:when>
            </xsl:choose>

        </xsl:if>
    </xsl:template>

    <!--	****************************** END OF CLINICAL DOCUMENT COMPOSITION PARTIAL ******************************	-->

    <!--	****************************** START OF DOCUMENT REFERENCE PARTIAL ******************************	-->
    <xsl:template name="document-reference-partial">
        <xsl:if test="(fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference)">

            <!--
		<div id="document-reference-partial" class="banner-partial-cream">
		<table>
			<tbody>
				<tr>
					<th>Subject Reference</th>
					<th>Context Related Ref</th>
					<th>Context Related Display</th>
				</tr>
				<tr>
					<td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:subject/fhir:reference/@value "/></td>
					<td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:context/fhir:related/fhir:ref/fhir:reference/@value "/></td>
					<td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:context/fhir:related/fhir:ref/fhir:display/@value "/></td>
				</tr>
			</tbody>
		</table>
		</div>
-->

            <div class="banner-partial-gray">
                <!--
		<table>
			<tbody>
				<tr>
					<th>Binary id</th>
					<th>Meta Profile</th>
					<th>contentType</th>
				</tr>
				<tr>
					<td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Binary/fhir:id/@value "/></td>
					<td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Binary/fhir:meta/fhir:profile/@value "/></td>
					<td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Binary/fhir:contentType/@value "/></td>
				</tr>
			</tbody>
		</table>
-->

                <table>
                    <tbody>
                        <tr>
                            <th>Encoded Content</th>
                        </tr>
                        <tr>
                            <td>
                                <xsl:variable name="encodedContent"><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Binary/fhir:content/@value "/></xsl:variable>
                                <xsl:result-document href="outputencodedcode.html" method="html">
                                    <html>
                                        <body>
                                            <xsl:value-of select="$encodedContent"/>
                                        </body>
                                    </html>
                                </xsl:result-document>
                                <a href="outputencodedcode.html">Click here to view encoded data</a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="banner-partial-cream">
                <table>
                    <tbody>
                        <tr>
                            <td><span class="label">Document Created</span></td><td><xsl:value-of  select="format-dateTime(fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:created/@value, '[D01]-[MNn,*-3]-[Y0001] [H01]:[m01]', 'en', (), ())"/></td>
                        </tr>
                        <tr>
                            <td><span class="label">Document Owner</span></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization">
                                    <xsl:variable name="custodianOrg"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>

                                    <xsl:choose>
                                        <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:custodian/fhir:reference/@value = $custodianOrg"><xsl:value-of select="fhir:name/@value"></xsl:value-of></xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                            </td>
                        </tr>
                        <tr>
                            <td><span class="label">Authored By</span></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner">
                                    <xsl:variable name="authorPrac"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>

                                    <xsl:choose>
                                        <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:author/fhir:reference/@value = $authorPrac"><xsl:value-of select="fhir:name/fhir:family/@value"/>, <xsl:value-of select="fhir:name/fhir:given/@value"/>
                                            <xsl:choose>
                                                <xsl:when test="fhir:name/fhir:prefix/@value != '' ">
                                                    (<xsl:value-of select="fhir:name/fhir:prefix/@value"/>)
                                                </xsl:when>
                                            </xsl:choose>
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="banner-partial-violet">

                <h2>Encounter Summary</h2>

                <xsl:choose>
                    <xsl:when test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:type/fhir:coding/fhir:display/@value != '' ">
                        <table>
                            <tbody>
                                <tr>
                                    <td><span class="label">Encounter Type </span></td>
                                    <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:type/fhir:coding/fhir:display/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                    </xsl:when>
                </xsl:choose>

                <xsl:choose>
                    <xsl:when test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:type/fhir:coding/fhir:display/@value != '' ">
                        <table>
                            <tbody>
                                <tr>
                                    <td><span class="label">Encounter Time </span></td>
                                    <td><xsl:value-of select="format-dateTime(fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:period/fhir:start/@value, '[D01]-[MNn,*-3]-[Y0001] [H01]:[m01]', 'en', (), ())"/> to <xsl:value-of select="format-dateTime(fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:period/fhir:end/@value, '[D01]-[MNn,*-3]-[Y0001] [H01]:[m01]', 'en', (), ())"/></td>
                                </tr>
                            </tbody>
                        </table>
                    </xsl:when>
                </xsl:choose>

                <xsl:choose>
                    <xsl:when test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:type/fhir:coding/fhir:display/@value != '' ">
                        <table>
                            <tbody>
                                <tr>
                                    <td><span class="label">Encounter Disposition </span></td>
                                    <td><xsl:value-of select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:hospitalization/fhir:dischargeDisposition/fhir:coding/fhir:display/@value"/></td>
                                </tr>
                            </tbody>
                        </table>
                    </xsl:when>
                </xsl:choose>

                <table>
                    <tbody>
                        <tr>
                            <td><span class="label">Care Setting Type </span></td>
                            <td>
                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension">
                                    <xsl:choose>
                                        <xsl:when test="@url='https://fhir.nhs.uk/StructureDefinition/extension-cofe-care-setting-type-1' or 'https://fhir.nhs.uk/StructureDefinition/extension-itk-care-setting-type-1' ">
                                            <xsl:value-of select="fhir:valueCodeableConcept/fhir:coding/fhir:display/@value"/>
                                        </xsl:when>
                                    </xsl:choose>
                                </xsl:for-each>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <xsl:choose>
                <xsl:when test="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:participant/fhir:individual != '' ">

                    <div class="banner-partial-cream">
                        <table>
                            <tbody>
                                <tr>
                                    <td><span class="bold">Other participant(s) in this document</span></td>
                                    <td></td>
                                </tr>
                            </tbody>
                        </table>
                        <table>
                            <tbody>
                                <tr>
                                    <td>
                                        <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter/fhir:participant/fhir:individual">
                                            <xsl:variable name="encounterpractitioner"><xsl:value-of select="fhir:reference/@value"/></xsl:variable>
                                            <xsl:for-each select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:identifier">
                                                <xsl:variable name="practionerlinked"><xsl:value-of select="fhir:system/@value"/>/<xsl:value-of select="fhir:value/@value"/></xsl:variable>
                                                <xsl:choose>
                                                    <xsl:when test="$practionerlinked=$encounterpractitioner">
                                                        <xsl:choose>
                                                            <xsl:when test="$practionerlinked=$encounterpractitioner">
                                                                <table>
                                                                    <tbody>
                                                                        <tr>
                                                                            <td><span class="bold-italic">Participant</span></td>
                                                                            <td></td>
                                                                        </tr>
                                                                        <tr>
                                                                            <td><span class="label">Participant Name </span><xsl:value-of select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:family/@value"/>, <xsl:value-of select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:given/@value"/>
                                                                                <xsl:choose>
                                                                                    <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:prefix/@value != '' ">
                                                                                        (<xsl:value-of select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:name/fhir:prefix/@value"/>)
                                                                                    </xsl:when>
                                                                                </xsl:choose>

                                                                            </td>
                                                                            <td></td>
                                                                        </tr>
                                                                    </tbody>
                                                                </table>
                                                                <table>
                                                                    <tbody>
                                                                        <tr>
                                                                            <xsl:for-each select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:address">
                                                                                <td>
                                                                                    <!--															<xsl:variable name="practioneraddressuse"><xsl:value-of select="fhir:use/@value"/></xsl:variable>
																<xsl:choose>
																	<xsl:when test="$practioneraddressuse='work'">-->
                                                                                    <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' "><xsl:text> </xsl:text></xsl:if>address</xsl:variable>
                                                                                    <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper, $lower)" /></xsl:variable>
                                                                                    <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                                                                    <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/></span><br/>
                                                                                    <xsl:value-of select="fhir:line/@value"/><br/>
                                                                                    <xsl:value-of select="fhir:city/@value"/><br/>
                                                                                    <xsl:value-of select="fhir:state/@value"/><br/>
                                                                                    <xsl:value-of select="fhir:postalCode/@value"/>
                                                                                    <!--																	</xsl:when>
																</xsl:choose>-->
                                                                                </td>
                                                                            </xsl:for-each>
                                                                            <td>
                                                                                <xsl:for-each select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner/fhir:telecom">
                                                                                    <!--																	<xsl:variable name="practionertelecomuse"><xsl:value-of select="fhir:use/@value"/></xsl:variable>
																		<xsl:choose>
																			<xsl:when test="$practionertelecomuse='work'">-->
                                                                                    <xsl:variable name="texttoconvert"><xsl:value-of select="fhir:use/@value"/><xsl:if test="fhir:use/@value != '' and fhir:system/@value != ''"><xsl:text> </xsl:text></xsl:if><xsl:value-of select="fhir:system/@value"/>:<xsl:text> </xsl:text></xsl:variable>
                                                                                    <xsl:variable name="passedValue"><xsl:value-of select="translate($texttoconvert, $upper, $lower)" /></xsl:variable>
                                                                                    <xsl:variable name= "ufirstChar" select="translate(substring($passedValue,1,1),$lower,$upper)"/>
                                                                                    <span class="label"><xsl:value-of select="concat($ufirstChar,substring($passedValue,2))"/></span>
                                                                                    <xsl:value-of select="fhir:value/@value"/><br/>
                                                                                    <!--																		</xsl:when>
																		</xsl:choose>-->
                                                                                </xsl:for-each>
                                                                            </td>
                                                                        </tr>
                                                                    </tbody>
                                                                </table>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:when>
                                                </xsl:choose>
                                            </xsl:for-each>
                                        </xsl:for-each>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </xsl:when>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url  != '' ">
                    <h1>Distribution list</h1>
                    <div class="banner-partial-cream">
                        <table>
                            <tbody>

                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Practitioner">
                                    <xsl:variable name="primaryrecipPrac"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>

                                    <xsl:choose>
                                        <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                            <tr>
                                                <th><span class="label">Recipient name</span></th>
                                                <th><span class="label">Job role</span></th>
                                                <th><span class="label">Phone</span></th>
                                            </tr>
                                        </xsl:when>
                                    </xsl:choose>
                                    <tr>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) "><xsl:value-of select="fhir:name/fhir:family/@value"/>, <xsl:value-of select="fhir:name/fhir:given/@value"/>
                                                    <xsl:choose>
                                                        <xsl:when test="fhir:name/fhir:prefix/@value != '' ">
                                                            (<xsl:value-of select="fhir:name/fhir:prefix/@value"/>)
                                                        </xsl:when>
                                                    </xsl:choose>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) "><xsl:value-of select="fhir:practitionerRole/fhir:role/fhir:coding/fhir:display/@value"/>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    <xsl:for-each select="fhir:telecom">
                                                        <xsl:choose>
                                                            <xsl:when test="fhir:system/@value = 'phone' ">
                                                                <xsl:value-of select="fhir:value/@value"/>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </xsl:for-each>

                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:RelatedPerson">
                                    <xsl:variable name="primaryrecipPrac"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>

                                    <xsl:choose>
                                        <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                            <tr>
                                                <th><span class="label">Related Person name</span></th>
                                                <th></th>
                                                <th><xsl:choose><xsl:when test="fhir:telecom/fhir:value/@value != '' ">
                                                    <span class="label">Phone</span>
                                                </xsl:when></xsl:choose></th>
                                            </tr>
                                        </xsl:when>
                                    </xsl:choose>

                                    <tr>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) "><xsl:value-of select="fhir:name/fhir:family/@value"/>, <xsl:value-of select="fhir:name/fhir:given/@value"/>
                                                    <xsl:choose>
                                                        <xsl:when test="fhir:name/fhir:prefix/@value != '' ">
                                                            (<xsl:value-of select="fhir:name/fhir:prefix/@value"/>)
                                                        </xsl:when>
                                                    </xsl:choose>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) "><xsl:value-of select="fhir:practitionerRole/fhir:role/fhir:coding/fhir:display/@value"/>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipPrac) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    <xsl:for-each select="fhir:telecom">
                                                        <xsl:choose>
                                                            <xsl:when test="fhir:system/@value = 'phone' ">
                                                                <xsl:value-of select="fhir:value/@value"/>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </xsl:for-each>

                                <xsl:for-each select="fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization">
                                    <xsl:variable name="primaryrecipOrg"><xsl:text>urn:uuid:</xsl:text><xsl:value-of select="fhir:id/@value"></xsl:value-of></xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipOrg) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                            <tr>
                                                <th><span class="label">Organisation name</span></th>
                                                <th><span class="label">Phone</span></th>
                                            </tr>
                                        </xsl:when>
                                    </xsl:choose>
                                    <tr>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipOrg) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) "><xsl:value-of select="fhir:name/@value"/>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                        <td>
                                            <xsl:choose>
                                                <xsl:when test="(/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/fhir:valueReference/fhir:reference/@value = $primaryrecipOrg) and ((/fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-itk-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/StructureDefinition/extension-cofe-information-recipient-1' or /fhir:Bundle/fhir:entry/fhir:resource/fhir:DocumentReference/fhir:extension/@url =  'https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-InformationRecipient-1')) ">
                                                    <xsl:for-each select="fhir:telecom">
                                                        <xsl:choose>
                                                            <xsl:when test="fhir:system/@value = 'phone' ">
                                                                <xsl:value-of select="fhir:value/@value"/>
                                                            </xsl:when>
                                                        </xsl:choose>
                                                    </xsl:for-each>
                                                </xsl:when>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </xsl:for-each>

                            </tbody>
                        </table>
                    </div>
                </xsl:when>
            </xsl:choose>

        </xsl:if>
    </xsl:template>
    <!--	****************************** END OF DOCUMENT REFERENCE PARTIAL ******************************	-->

</xsl:stylesheet>