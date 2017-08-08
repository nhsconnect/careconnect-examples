Feature: Phase 1 Exemplar Test
  As a receptionist
  I want to search for a Patient by NHS Number


  Scenario: Patient Search by NHS Number
    Given I search for a Patient by NHS Number 9876543210
    Then the result should be a valid FHIR Bundle
    And the results should be valid CareConnect Profiles