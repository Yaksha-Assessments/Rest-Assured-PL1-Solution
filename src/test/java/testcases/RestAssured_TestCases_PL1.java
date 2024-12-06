package testcases;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

		List<Map<String, Object>> results = customResponse.getResults();
		Assert.assertTrue(results.size() > 1, "Results should contain multiple doctors.");

		Set<Integer> performerIds = results.stream().map(result -> (Integer) result.get("PerformerId"))
				.collect(Collectors.toSet());

		Assert.assertEquals(performerIds.size(), results.size(), "Each doctor should have a unique PerformerId.");

		System.out.println("Appointment Applicable Doctors List Response:");
		customResponse.getResponse().prettyPrint();
	}
}
