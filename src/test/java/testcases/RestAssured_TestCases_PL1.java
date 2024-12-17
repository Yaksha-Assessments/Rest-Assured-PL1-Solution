package testcases;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import coreUtilities.utils.FileOperations;
import rest.ApiUtil;
import rest.CustomResponse;

public class RestAssured_TestCases_PL1 {

	FileOperations fileOperations = new FileOperations();

	private final String EXCEL_FILE_PATH = "src/main/resources/config.xlsx"; // Path to the Excel file
	private final String SHEET_NAME = "PostData"; // Sheet name in the Excel file
	private final String FILEPATH = "src/main/java/rest/ApiUtil.java";
	ApiUtil apiUtil;

	public static int appointmentId;

	@Test(priority = 1, groups = { "PL1" }, description = "Precondition: Create an appointment via the API\n"
			+ "1. Send POST request to create a new appointment with provided data\n"
			+ "2. Verify the response status code is 200 OK\n" + "3. Validate the response contains 'Status' as 'OK'\n"
			+ "4. Retrieve and validate the Appointment ID from the response")
	public void createAppointmentTest() throws Exception {

		Map<String, String> body = fileOperations.readExcelPOI(EXCEL_FILE_PATH, SHEET_NAME);

		apiUtil = new ApiUtil();

		// Retrieve values from the Map
		String firstName = body.get("FirstName");
		String lastName = body.get("LastName");
		String gender = body.get("Gender");
		String age = body.get("Age");
		String contactNumber = body.get("ContactNumber");
		String appointmentDate = body.get("AppointmentDate");
		String appointmentTime = body.get("AppointmentTime");
		String performerName = body.get("PerformerName");
		String appointmentType = body.get("AppointmentType");
		String departmentId = body.get("DepartmentId");

		// Construct the JSON payload as a string
		String requestBody = "{ " + "\"FirstName\": \"" + firstName + "\", " + "\"LastName\": \"" + lastName + "\", "
				+ "\"Gender\": \"" + gender + "\", " + "\"Age\": \"" + age + "\", " + "\"ContactNumber\": \""
				+ contactNumber + "\", " + "\"AppointmentDate\": \"" + appointmentDate + "\", "
				+ "\"AppointmentTime\": \"" + appointmentTime + "\", " + "\"PerformerName\": \"" + performerName
				+ "\", " + "\"AppointmentType\": \"" + appointmentType + "\", " + "\"DepartmentId\": " + departmentId
				+ " }";

		CustomResponse customResponse = apiUtil.createAppointmentWithAuth("/Appointment/AddAppointment", requestBody);

		// Validate method's source code
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"createAppointmentWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"createAppointmentWithAuth must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("createAppointmentWithAuth", customResponse),
				"Must have all fields");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 201 Created.");

		// Validate key fields in the response
		String status = customResponse.getStatus();
		Assert.assertEquals(status, customResponse.getResponse().jsonPath().getString("Status"),
				"Status should be OK.");

		// Parse the "Results" object
		appointmentId = customResponse.getAppointmentId();
		Assert.assertNotNull(appointmentId, "Appointment ID should not be null.");

		// Print the full response body
		System.out.println("Create Appointment Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 2, groups = { "PL1" }, description = "Precondition: Multiple applicable doctors must exist\n"
			+ "1. Validate that the response contains list of Doctors\n"
			+ "2. Verify the response status code is 200.\n" + "3. Verify Performer IDs are unique")
	public void getAllApplicableDoctorsTest() throws IOException {
		apiUtil = new ApiUtil();
		CustomResponse customResponse = apiUtil.getAllApplicableDoctorsWithAuth("/Visit/AppointmentApplicableDoctors",
				null);

		// Validate method's source code
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"getAllApplicableDoctorsWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(TestCodeValidator.validateResponseFields("getAllApplicableDoctorsWithAuth", customResponse),
				"Must have all fields");

		Assert.assertTrue(isValidationSuccessful,
				"getAllApplicableDoctorsWithAuth must be implemented using Rest Assured methods only.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, customResponse.getResponse().jsonPath().getString("Status"),
				"Status should be OK.");

		List<Map<String, Object>> results = customResponse.getListResults();
		Assert.assertTrue(results.size() > 1, "Results should contain multiple doctors.");

		Set<Integer> performerIds = results.stream().map(result -> (Integer) result.get("PerformerId"))
				.collect(Collectors.toSet());

		Assert.assertEquals(performerIds.size(), results.size(), "Each doctor should have a unique PerformerId.");

		System.out.println("Appointment Applicable Doctors List Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 3, groups = {
			"PL1" }, dependsOnMethods = "createAppointmentTest", description = "Precondition: An appointment must be created successfully.\n"
					+ "1. Validate that the appointment ID is not null.\n"
					+ "2. Send a PUT request to cancel the appointment using the appointment ID.\n"
					+ "3. Verify the response status code is 200.\n"
					+ "4. Validate the response indicates successful cancellation.")
	public void cancelAppointmentTest() throws IOException {
		apiUtil = new ApiUtil();

		Assert.assertNotNull(appointmentId, "Appointment ID should be set by the createAppointmentTest.");

		// Call the updated method
		CustomResponse customResponse = apiUtil.cancelAppointmentWithAuth(
				"/Appointment/AppointmentStatus?appointmentId=" + appointmentId + "&status=cancelled", null);

		// Validate the implementation of cancelAppointmentWithAuth
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"cancelAppointmentWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"cancelAppointmentWithAuth must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("cancelAppointmentWithAuth", customResponse),
				"Must have all fields");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		// Validate the response fields
		String status = customResponse.getStatus();
		Assert.assertEquals(status, customResponse.getResponse().jsonPath().getString("Status"),
				"Status should be OK.");

		String resultMessage = customResponse.getResultMessage();
		Assert.assertEquals(resultMessage, "Appointment information updated successfully.",
				"Message should confirm the update.");

		// Print the response from canceling the appointment
		System.out.println("Cancelled Appointment Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 4, groups = {
			"PL1" }, description = "Precondition: Patients and Doctor must be created successfully.\n"
					+ "1. Validate clashing appointments.\n" + "2. Verify response status code and results.")
	public void clashAppointmentTest() throws Exception {
		Map<String, String> clashedData = fileOperations.readExcelPOI(EXCEL_FILE_PATH, SHEET_NAME);
		apiUtil = new ApiUtil();

		String requestDate = clashedData.get("requestDate");
		String performerId = clashedData.get("performerId");
		String patientId = clashedData.get("patientId");

		CustomResponse customResponse = apiUtil
				.clashAppointmentWithAuth("/Appointment/CheckClashingAppointment?patientId=" + patientId
						+ "&requestDate=" + requestDate + "&performerId=" + performerId, null);

		// Validate implementation of clashAppointmentWithAuth
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"clashAppointmentWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"clashAppointmentWithAuth must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("clashAppointmentWithAuth", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		String resultMessage = customResponse.getResultMessage();
		Assert.assertEquals(resultMessage, "false", "Clashing status should confirm no clash.");

		System.out.println("Clash Appointment Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 5, groups = { "PL1" }, description = "Precondition: Patients must exist in the system.\n"
			+ "1. Search for patients by query string.\n" + "2. Validate response status code and matching results.")
	public void searchPatientTest() throws Exception {
		apiUtil = new ApiUtil();

		CustomResponse customResponse = apiUtil.searchPatientWithAuth("/Patient/SearchRegisteredPatient?search=Test",
				null);

		// Validate implementation of searchPatientWithAuth
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH, "searchPatientWithAuth",
				List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"searchPatientWithAuth must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("searchPatientWithAuth", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		List<Map<String, Object>> results = customResponse.getListResults();
		Assert.assertFalse(results.isEmpty(), "Results should not be empty.");

		String firstName = (String) results.get(0).get("FirstName");
		String shortName = (String) results.get(0).get("ShortName");

		Assert.assertTrue(firstName.contains("Test"), "FirstName does not contain 'Test'.");
		Assert.assertTrue(shortName.contains("Test"), "ShortName does not contain 'Test'.");

		System.out.println("Search Patient Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 6, groups = {
			"PL1" }, description = "Precondition: Appointments must be created within the specified date range.\n"
					+ "1. Retrieve appointments for a performer within a date range.\n"
					+ "2. Validate response status code and appointment dates.")
	public void bookingListTest() throws Exception {
		Map<String, String> searchResult = fileOperations.readExcelPOI(EXCEL_FILE_PATH, SHEET_NAME);
		apiUtil = new ApiUtil();

		LocalDate currentDate = LocalDate.now();
		LocalDate dateFiveDaysBefore = currentDate.minusDays(5);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		String currentDateStr = currentDate.format(formatter);
		String dateFiveDaysBeforeStr = dateFiveDaysBefore.format(formatter);
		String performerId = searchResult.get("performerId");

		CustomResponse customResponse = apiUtil.bookingListWithAuthInRange("/Appointment/Appointments?FromDate="
				+ dateFiveDaysBeforeStr + "&ToDate=" + currentDateStr + "&performerId=" + performerId + "&status=new",
				null);

		// Validate implementation of bookingListWithAuthInRange
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"bookingListWithAuthInRange", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"bookingListWithAuthInRange must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("bookingListWithAuthInRange", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		List<Map<String, Object>> results = customResponse.getListResults();
		for (Map<String, Object> result : results) {
			String appointmentDateStr = result.get("AppointmentDate").toString().substring(0, 10);
			LocalDate appointmentDate = LocalDate.parse(appointmentDateStr);

			Assert.assertTrue(!appointmentDate.isBefore(dateFiveDaysBefore) && !appointmentDate.isAfter(currentDate),
					"AppointmentDate " + appointmentDate + " is not within the expected range: " + dateFiveDaysBeforeStr
							+ " to " + currentDateStr);
		}

		System.out.println("Booking List Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 7, groups = { "PL1" }, description = "Retrieve and validate the complete list of stock details.")
	public void AllStockDetailsTest() throws IOException {
		apiUtil = new ApiUtil();

		CustomResponse customResponse = apiUtil.AllStockDetailsWithAuth("/PharmacyStock/AllStockDetails", null);

		// Validate method implementation
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"AllStockDetailsWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"AllStockDetailsWithAuth must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("AllStockDetailsWithAuth", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		List<Map<String, Object>> results = customResponse.getListResults();
		Assert.assertFalse(results.isEmpty(), "Results should not be empty.");

		for (Map<String, Object> item : results) {
			Assert.assertNotNull(item.get("ItemId"), "ItemId should not be null.");
		}

		System.out.println("Stock Details Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 8, groups = {
			"PL1" }, description = "Retrieve and validate the main store details from the pharmacy settings.")
	public void MainStoreTest() throws IOException {
		apiUtil = new ApiUtil();

		CustomResponse customResponse = apiUtil.MainStoreDetailsWithAuth("/PharmacySettings/MainStore", null);

		// Validate method implementation
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"MainStoreDetailsWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"MainStoreDetailsWithAuth must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("MainStoreDetailsWithAuth", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		Map<String, Object> results = customResponse.getMapResults();
		Assert.assertNotNull(results.get("Name"), "Store Name should not be null.");
		Assert.assertNotNull(results.get("StoreDescription"), "Store Description should not be null.");
		Assert.assertNotNull(results.get("StoreId"), "StoreId should not be null.");

		System.out.println("Main Store Details Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 9, groups = { "PL1" }, description = "Retrieve and validate the list of pharmacy stores.")
	public void PharmacyStoreTest() throws IOException {
		apiUtil = new ApiUtil();

		CustomResponse customResponse = apiUtil.PharmacyStoresWithAuth("/Dispensary/PharmacyStores", null);

		// Validate method implementation
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"PharmacyStoresWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"PharmacyStoresWithAuth must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("PharmacyStoresWithAuth", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		List<Map<String, Object>> results = customResponse.getListResults();
		Assert.assertFalse(results.isEmpty(), "Results should not be empty.");

		for (Map<String, Object> store : results) {
			Assert.assertNotNull(store.get("StoreId"), "StoreId should not be null.");
			Assert.assertNotNull(store.get("Name"), "Store Name should not be null.");
		}

		System.out.println("Pharmacy Stores Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 10, groups = { "PL1" }, description = "Retrieve and validate patient consumption details.")
	public void PatientConsumptionTest() throws IOException {
		apiUtil = new ApiUtil();

		CustomResponse customResponse = apiUtil.PatientConsumption("/PatientConsumption/PatientConsumptions", null);

		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH, "PatientConsumption",
				List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"PatientConsumption must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("PatientConsumption", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		List<Map<String, Object>> results = customResponse.getListResults();
		for (Map<String, Object> patient : results) {
			Assert.assertNotNull(patient.get("PatientId"), "PatientId should not be null.");
			Assert.assertNotNull(patient.get("PatientName"), "PatientName should not be null.");
		}

		System.out.println("Patient Consumption Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 11, groups = { "PL1" }, description = "Activate a pharmacy counter and validate the response.")
	public void ActivatePharmCountTest() throws Exception {
		apiUtil = new ApiUtil();

		Map<String, String> searchResult = fileOperations.readExcelPOI(EXCEL_FILE_PATH, SHEET_NAME);
		String counterId = searchResult.get("counterId");
		String counterName = searchResult.get("counterName");

		CustomResponse customResponse = apiUtil.ActivatePharmCount(
				"/Security/ActivatePharmacyCounter?counterId=" + counterId + "&counterName=" + counterName, null);

		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH, "ActivatePharmCount",
				List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"ActivatePharmCount must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("ActivatePharmCount", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		Map<String, Object> results = customResponse.getMapResults();
		Assert.assertNotNull(results.get("CounterName"), "CounterName should not be null.");
		Assert.assertNotNull(results.get("CounterId"), "CounterId should not be null.");

		System.out.println("Activated Pharmacy Counter Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 12, groups = { "PL1" }, description = "Deactivate a pharmacy counter and validate the response.")
	public void DeactivatePharmCountTest() throws IOException {
		apiUtil = new ApiUtil();

		CustomResponse customResponse = apiUtil.DeactivatePharmCount("/Security/DeactivatePharmacyCounter", null);

		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH, "DeactivatePharmCount",
				List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"DeactivatePharmCount must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("DeactivatePharmCount", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		Map<String, Object> results = customResponse.getMapResults();
		Assert.assertTrue(results.get("StatusCode").toString().equals("200"),
				"StatusCode should be 200 but is " + results.get("StatusCode"));

		System.out.println("Deactivated Pharmacy Counter Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 13, groups = {
			"PL1" }, description = "Retrieve and validate the list of appointment applicable departments.")
	public void AppointApplicDeptTest() throws Exception {
		apiUtil = new ApiUtil();

		CustomResponse customResponse = apiUtil.AppointApplicDept("/Master/AppointmentApplicableDepartments", null);

		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH, "AppointApplicDept",
				List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"AppointApplicDept must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("AppointApplicDept", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		List<Map<String, Object>> results = customResponse.getListResults();
		for (Map<String, Object> department : results) {
			Assert.assertNotNull(department.get("DepartmentId"), "DepartmentId should not be null.");
			Assert.assertNotNull(department.get("DepartmentName"), "DepartmentName should not be null.");
		}

		System.out.println("Appointment Applicable Departments Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 14, groups = {
			"PL1" }, description = "Retrieve and validate the list of currently admitted patients.")
	public void AdmittedPatientsData() throws Exception {
		apiUtil = new ApiUtil();

		CustomResponse customResponse = apiUtil
				.admittedPatientData("/Admission/AdmittedPatientsData?admissionStatus=admitted", null);

		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH, "admittedPatientData",
				List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"admittedPatientData must be implemented using Rest Assured methods only.");

		Assert.assertTrue(TestCodeValidator.validateResponseFields("admittedPatientData", customResponse),
				"Must have all fields.");

		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		List<Map<String, Object>> results = customResponse.getListResults();
		for (Map<String, Object> patient : results) {
			Assert.assertNotNull(patient.get("PatientId"), "PatientId should not be null.");
			Assert.assertNotNull(patient.get("AdmittedDate"), "AdmittedDate should not be null.");
			Assert.assertNull(patient.get("DischargedDate"), "DischargedDate should be null for admitted patients.");
		}

		System.out.println("Admitted Patients Data Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 15, groups = { "PL1" }, description = "Add a new currency and validate the response.")
	public void addcurrencyTest() throws Exception {
		apiUtil = new ApiUtil();

		// Generate random currency code
		String randomCurrencyCode = new Random().ints(5, 0, 26).mapToObj(i -> Character.toString((char) ('A' + i)))
				.collect(Collectors.joining());

		// Construct the request body map
		Map<String, String> postData = new HashMap<>();
		postData.put("CurrencyCode", randomCurrencyCode);
		postData.put("Description", "Description"); // Static description
		postData.put("CreatedBy", "1"); // Assuming CreatedBy is 1 for this test
		postData.put("CreatedOn",
				ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX")));
		postData.put("IsActive", "True");

		// Send the POST request
		CustomResponse customResponse = apiUtil.addCurrencyWithAuth("/InventorySettings/Currency", postData);

		// Validate the test method implementation
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH, "addCurrencyWithAuth",
				List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"addCurrencyWithAuth must be implemented using Rest Assured methods only.");

		// Validate response fields
		Assert.assertTrue(TestCodeValidator.validateResponseFields("addCurrencyWithAuth", customResponse),
				"Must have all fields.");

		// Assert status code
		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		// Assert status
		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		// Extract results and validate fields
		Map<String, Object> results = customResponse.getMapResults();
		Assert.assertEquals(results.get("CurrencyCode"), randomCurrencyCode, "CurrencyCode mismatch.");
		Assert.assertEquals(results.get("Description"), "Description", "Description mismatch.");
		Assert.assertEquals(results.get("CreatedBy").toString(), "1", "CreatedBy mismatch.");
		Assert.assertNotNull(results.get("CreatedOn"), "CreatedOn should not be null.");
		Assert.assertEquals(results.get("IsActive").toString(), "true", "IsActive should be True.");

		// Log the response for debugging
		System.out.println("Added Currency Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 16, groups = {
			"PL1" }, description = "Find a patient with matching phone number and validate the response.")
	public void findMatchingPatientTest() throws Exception {
		apiUtil = new ApiUtil();

		Map<String, String> postData = fileOperations.readExcelPOI(EXCEL_FILE_PATH, "MatchingPatient");

		// Extract values from the JSON
		String firstName = postData.get("FirstName");
		String lastName = postData.get("LastName");
		String phoneNumber = postData.get("PhoneNumber");
		String age = postData.get("Age");
		String gender = postData.get("Gender");
		String isInsurance = postData.get("IsInsurance");
		String imisCode = postData.get("IMISCode");

		// Send the GET request
		CustomResponse customResponse = apiUtil.findMatchingPatientWithAuth("/Patient/MatchingPatients?FirstName="
				+ firstName + "&LastName=" + lastName + "&PhoneNumber=" + phoneNumber + "&Age=" + age + "&Gender="
				+ gender + "&IsInsurance=" + isInsurance + "&IMISCode=" + imisCode, null);

		// Validate the test method implementation
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"findMatchingPatientWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"findMatchingPatientWithAuth must be implemented using Rest Assured methods only.");

		// Validate response fields
		Assert.assertTrue(TestCodeValidator.validateResponseFields("findMatchingPatientWithAuth", customResponse),
				"Must have all fields.");

		// Assert status code
		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		// Assert status
		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		// Validate phone number
		String actualPhoneNumber = customResponse.getListResults().get(0).get("PhoneNumber").toString();
		Assert.assertEquals(actualPhoneNumber, phoneNumber, "Phone number does not match.");

		// Log the response for debugging
		System.out.println("Matching Patient Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 17, groups = {
			"PL1" }, description = "Retrieve all registered patients and validate unique Patient IDs.")
	public void getAllRegisteredPatientsTest() throws IOException {
		apiUtil = new ApiUtil();

		// Send the GET request
		CustomResponse customResponse = apiUtil
				.getRegisteredPatientsWithAuth("/Patient/SearchRegisteredPatient?search=", null);

		// Validate the test method implementation
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"getRegisteredPatientsWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"getRegisteredPatientsWithAuth must be implemented using Rest Assured methods only.");

		// Validate response fields
		Assert.assertTrue(TestCodeValidator.validateResponseFields("getRegisteredPatientsWithAuth", customResponse),
				"Must have all fields.");

		// Assert status code
		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		// Assert status
		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		// Validate unique Patient IDs
		List<Map<String, Object>> results = customResponse.getListResults();
		Set<Integer> patientIds = results.stream().map(result -> (Integer) result.get("PatientId"))
				.collect(Collectors.toSet());

		Assert.assertEquals(patientIds.size(), results.size(), "Each patient should have a unique PatientID.");

		// Log the response for debugging
		System.out.println("Registered Patients Response:");
		customResponse.getResponse().prettyPrint();
	}

	@Test(priority = 18, groups = {
			"PL1" }, description = "Retrieve all billing counters and validate unique Counter IDs.")
	public void getAllBillingCounters() throws IOException {
		apiUtil = new ApiUtil();

		// Send the GET request
		CustomResponse customResponse = apiUtil.getBillingCountersWithAuth("/billing/BillingCounters", null);

		// Validate the test method implementation
		boolean isValidationSuccessful = TestCodeValidator.validateTestMethodFromFile(FILEPATH,
				"getBillingCountersWithAuth", List.of("given", "then", "extract", "response"));

		Assert.assertTrue(isValidationSuccessful,
				"getBillingCountersWithAuth must be implemented using Rest Assured methods only.");

		// Validate response fields
		Assert.assertTrue(TestCodeValidator.validateResponseFields("getBillingCountersWithAuth", customResponse),
				"Must have all fields.");

		// Assert status code
		Assert.assertEquals(customResponse.getStatusCode(), customResponse.getResponse().getStatusCode(),
				"Status code should be 200 OK.");

		// Assert status
		String status = customResponse.getStatus();
		Assert.assertEquals(status, "OK", "Status should be OK.");

		// Validate unique Counter IDs
		List<Map<String, Object>> results = customResponse.getListResults();
		Set<Integer> counterIds = results.stream().map(result -> (Integer) result.get("CounterId"))
				.collect(Collectors.toSet());

		Assert.assertEquals(counterIds.size(), results.size(), "Each counter should have a unique CounterID.");

		// Log the response for debugging
		System.out.println("Billing Counters Response:");
		customResponse.getResponse().prettyPrint();
	}
}
