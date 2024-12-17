package testcases;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rest.CustomResponse;

public class TestCodeValidator {

	// Method to validate if specific keywords are used in the method's source code
	public static boolean validateTestMethodFromFile(String filePath, String methodName, List<String> keywords)
			throws IOException {
		// Read the content of the test class file
		String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));

		// Extract the method body for the specified method using regex
		String methodRegex = "(public\\s+CustomResponse\\s+" + methodName + "\\s*\\(.*?\\)\\s*\\{)([\\s\\S]*?)}";
		Pattern methodPattern = Pattern.compile(methodRegex);
		Matcher methodMatcher = methodPattern.matcher(fileContent);

		if (methodMatcher.find()) {

			String methodBody = fetchBody(filePath, methodName);

			// Now we validate the method body for the required keywords
			boolean allKeywordsPresent = true;

			// Loop over the provided keywords and check if each one is present in the
			// method body
			for (String keyword : keywords) {
				Pattern keywordPattern = Pattern.compile("\\b" + keyword + "\\s*\\(");
				if (!keywordPattern.matcher(methodBody).find()) {
					System.out.println("'" + keyword + "()' is missing in the method.");
					allKeywordsPresent = false;
				}
			}

			return allKeywordsPresent;

		} else {
			System.out.println("Method " + methodName + " not found in the file.");
			return false;
		}
	}

	// This method takes the method name as an argument and returns its body as a
	// String.
	public static String fetchBody(String filePath, String methodName) {
		StringBuilder methodBody = new StringBuilder();
		boolean methodFound = false;
		boolean inMethodBody = false;
		int openBracesCount = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				// Check if the method is found by matching method signature
				if (line.contains("public CustomResponse " + methodName + "(")
						|| line.contains("public String " + methodName + "(")
						|| line.contains("public Response " + methodName + "(")) {
					methodFound = true;
				}

				// Once the method is found, start capturing lines
				if (methodFound) {
					if (line.contains("{")) {
						inMethodBody = true;
						openBracesCount++;
					}

					// Capture the method body
					if (inMethodBody) {
						methodBody.append(line).append("\n");
					}

					// Check for closing braces to identify the end of the method
					if (line.contains("}")) {
						openBracesCount--;
						if (openBracesCount == 0) {
							break; // End of method body
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return methodBody.toString();
	}

	public static boolean validateResponseFields(String methodName, CustomResponse customResponse) {
		boolean isValid = true;

		switch (methodName) {
		case "createAppointmentWithAuth":
			// Expected fields in the "Results" section of the response
			List<String> expectedFields = List.of("AppointmentId", "PatientId", "FirstName", "MiddleName", "LastName",
					"Gender", "Age", "ContactNumber", "AppointmentDate", "AppointmentTime", "PerformerId",
					"PerformerName", "AppointmentType", "AppointmentStatus", "CreatedOn", "CreatedBy", "ModifiedOn",
					"ModifiedBy", "Reason", "CancelledOn", "CancelledBy", "CancelledRemarks", "DepartmentId");

			// Validate "Results" section
			Map<String, Object> results = customResponse.getResponse().jsonPath().getMap("Results");
			if (results == null) {
				isValid = false;
				System.out.println("Results section is missing in the response.");
				break;
			}

			for (String field : expectedFields) {
				if (!results.containsKey(field)) {
					isValid = false;
					System.out.println("Missing field in Results: " + field);
				}
			}

			// Validate top-level fields
			if (customResponse.getResponse().jsonPath().getString("Status") == null) {
				isValid = false;
				System.out.println("Status field is missing in the response.");
			}

			if (customResponse.getResponse().jsonPath().getString("ErrorMessage") == null) {
				isValid = false;
				System.out.println("ErrorMessage field is missing in the response.");
			}
			break;

		case "getAllApplicableDoctorsWithAuth":
			// Expected fields in each "Results" item
			List<String> doctorExpectedFields = List.of("DepartmentId", "DepartmentName", "PerformerId",
					"PerformerName");

			// Validate "Results" array
			List<Map<String, Object>> doctorResults = customResponse.getResponse().jsonPath().getList("Results");
			if (doctorResults == null || doctorResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < doctorResults.size(); i++) {
				Map<String, Object> doctor = doctorResults.get(i);
				for (String field : doctorExpectedFields) {
					if (!doctor.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			if (customResponse.getResponse().jsonPath().getString("Status") == null) {
				isValid = false;
				System.out.println("Status field is missing in the response.");
			}

			if (customResponse.getResponse().jsonPath().getString("ErrorMessage") == null) {
				isValid = false;
				System.out.println("ErrorMessage field is missing in the response.");
			}
			break;
		case "cancelAppointmentWithAuth":
			// Expected fields in the response
			List<String> cancelExpectedFields = List.of("Status", "Results");

			// Validate the response
			String statusField = customResponse.getResponse().jsonPath().getString("Status");
			if (statusField == null || !statusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}

			String resultField = customResponse.getResponse().jsonPath().getString("Results");
			if (resultField == null || !resultField.equals("Appointment information updated successfully.")) {
				isValid = false;
				System.out.println("Results field is missing or invalid in the response.");
			}
			break;

		case "clashAppointmentWithAuth":
			// Validation logic for clashAppointmentWithAuth
			List<String> clashExpectedFields = List.of("Status", "Results");

			// Validate top-level fields
			String clashStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (clashStatusField == null || !clashStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}

			String clashResultField = customResponse.getResponse().jsonPath().getString("Results");
			if (clashResultField == null) {
				isValid = false;
				System.out.println("Results field is missing in the response.");
			}
			break;

		case "searchPatientWithAuth":
			// Validation logic for searchPatientWithAuth
			List<String> patientExpectedFields = List.of("PatientId", "ShortName", "FirstName", "LastName", "Age");

			// Validate "Results" array
			List<Map<String, Object>> patientResults = customResponse.getResponse().jsonPath().getList("Results");
			if (patientResults == null || patientResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < patientResults.size(); i++) {
				Map<String, Object> patient = patientResults.get(i);
				for (String field : patientExpectedFields) {
					if (!patient.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			String patientStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (patientStatusField == null || !patientStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "bookingListWithAuthInRange":
			// Validation logic for bookingListWithAuthInRange
			List<String> bookingExpectedFields = List.of("AppointmentId", "PatientId", "FullName", "AppointmentDate",
					"AppointmentTime", "AppointmentStatus");

			// Validate "Results" array
			List<Map<String, Object>> bookingResults = customResponse.getResponse().jsonPath().getList("Results");
			if (bookingResults == null || bookingResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < bookingResults.size(); i++) {
				Map<String, Object> appointment = bookingResults.get(i);
				for (String field : bookingExpectedFields) {
					if (!appointment.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			String bookingStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (bookingStatusField == null || !bookingStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "AllStockDetailsWithAuth":
			// Expected fields in each stock item
			List<String> stockExpectedFields = List.of("ItemId");

			// Validate "Results" array
			List<Map<String, Object>> stockResults = customResponse.getResponse().jsonPath().getList("Results");
			if (stockResults == null || stockResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < stockResults.size(); i++) {
				Map<String, Object> stockItem = stockResults.get(i);
				for (String field : stockExpectedFields) {
					if (!stockItem.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			String stockStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (stockStatusField == null || !stockStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "MainStoreDetailsWithAuth":
			// Expected fields in main store details
			List<String> mainStoreExpectedFields = List.of("Name", "StoreDescription", "StoreId");

			// Validate "Results" map
			Map<String, Object> mainStoreResults = customResponse.getResponse().jsonPath().getMap("Results");
			if (mainStoreResults == null || mainStoreResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (String field : mainStoreExpectedFields) {
				if (!mainStoreResults.containsKey(field)) {
					isValid = false;
					System.out.println("Missing field in Results: " + field);
				}
			}

			// Validate top-level fields
			String mainStoreStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (mainStoreStatusField == null || !mainStoreStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "PharmacyStoresWithAuth":
			// Expected fields in each pharmacy store
			List<String> pharmacyStoreExpectedFields = List.of("StoreId", "Name");

			// Validate "Results" array
			List<Map<String, Object>> pharmacyStoreResults = customResponse.getResponse().jsonPath().getList("Results");
			if (pharmacyStoreResults == null || pharmacyStoreResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < pharmacyStoreResults.size(); i++) {
				Map<String, Object> store = pharmacyStoreResults.get(i);
				for (String field : pharmacyStoreExpectedFields) {
					if (!store.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			String pharmacyStoreStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (pharmacyStoreStatusField == null || !pharmacyStoreStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "PatientConsumption":
			// Expected fields in each patient's consumption details
			List<String> patientConsumptionFields = List.of("PatientId", "PatientName");

			// Validate "Results" array
			List<Map<String, Object>> patientResultsList = customResponse.getResponse().jsonPath().getList("Results");
			if (patientResultsList == null || patientResultsList.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < patientResultsList.size(); i++) {
				Map<String, Object> patient = patientResultsList.get(i);
				for (String field : patientConsumptionFields) {
					if (!patient.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			String patientConsumptionStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (patientConsumptionStatusField == null || !patientConsumptionStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "ActivatePharmCount":
			// Expected fields in the activated counter details
			List<String> activatePharmFields = List.of("CounterId", "CounterName");

			// Validate "Results" map
			Map<String, Object> activatePharmResults = customResponse.getResponse().jsonPath().getMap("Results");
			if (activatePharmResults == null || activatePharmResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (String field : activatePharmFields) {
				if (!activatePharmResults.containsKey(field)) {
					isValid = false;
					System.out.println("Missing field in Results: " + field);
				}
			}

			// Validate top-level fields
			String activatePharmStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (activatePharmStatusField == null || !activatePharmStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "DeactivatePharmCount":
			// Expected fields in the deactivated counter response
			List<String> deactivatePharmFields = List.of("StatusCode");

			// Validate "Results" map
			Map<String, Object> deactivatePharmResults = customResponse.getResponse().jsonPath().getMap("Results");
			if (deactivatePharmResults == null || deactivatePharmResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (String field : deactivatePharmFields) {
				if (!deactivatePharmResults.containsKey(field)) {
					isValid = false;
					System.out.println("Missing field in Results: " + field);
				}
			}

			String statusCodeField = deactivatePharmResults.get("StatusCode").toString();
			if (!statusCodeField.equals("200")) {
				isValid = false;
				System.out.println(
						"StatusCode field is invalid in the response. Expected: 200, Found: " + statusCodeField);
			}

			// Validate top-level fields
			String deactivatePharmStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (deactivatePharmStatusField == null || !deactivatePharmStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "AppointApplicDept":
			// Expected fields in each department's details
			List<String> departmentExpectedFields = List.of("DepartmentId", "DepartmentName");

			// Validate "Results" array
			List<Map<String, Object>> departmentResults = customResponse.getResponse().jsonPath().getList("Results");
			if (departmentResults == null || departmentResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < departmentResults.size(); i++) {
				Map<String, Object> department = departmentResults.get(i);
				for (String field : departmentExpectedFields) {
					if (!department.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			String departmentStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (departmentStatusField == null || !departmentStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "admittedPatientData":
			// Expected fields in each admitted patient's details
			List<String> admittedPatientFields = List.of("PatientId", "AdmittedDate");

			// Validate "Results" array
			List<Map<String, Object>> admittedPatientResults = customResponse.getResponse().jsonPath()
					.getList("Results");
			if (admittedPatientResults == null || admittedPatientResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < admittedPatientResults.size(); i++) {
				Map<String, Object> patient = admittedPatientResults.get(i);
				for (String field : admittedPatientFields) {
					if (!patient.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
				if (patient.get("DischargedDate") != null) {
					isValid = false;
					System.out.println("DischargedDate should be null for admitted patients.");
				}
			}

			// Validate top-level fields
			String admittedPatientStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (admittedPatientStatusField == null || !admittedPatientStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "addCurrencyWithAuth":
			// Expected fields in the added currency details
			List<String> currencyExpectedFields = List.of("CurrencyCode", "CreatedBy", "CreatedOn", "IsActive");

			System.out.println("Raw Response Body: ");
			customResponse.getResponse().prettyPrint();

			// Validate "Results" map
			Map<String, Object> currencyResults = customResponse.getResponse().jsonPath().getMap("Results");
			if (currencyResults == null || currencyResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (String field : currencyExpectedFields) {
				if (!currencyResults.containsKey(field)) {
					isValid = false;
					System.out.println("Missing field in Results: " + field);
				}
			}

			// Validate specific fields
			if (!Boolean.TRUE.equals(currencyResults.get("IsActive"))) {
				isValid = false;
				System.out.println("IsActive field should be true.");
			}

			// Validate top-level fields
			String currencyStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (currencyStatusField == null || !currencyStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "findMatchingPatientWithAuth":
			// Expected fields in each patient's details
			List<String> matchingPatientExpectedFields = List.of("PatientId", "FirstName", "LastName", "PhoneNumber");

			customResponse.getResponse().prettyPrint();
			
			// Validate "Results" array
			List<Map<String, Object>> matchingPatientResults = customResponse.getResponse().jsonPath()
					.getList("Results");
			if (matchingPatientResults == null || matchingPatientResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < matchingPatientResults.size(); i++) {
				Map<String, Object> patient = matchingPatientResults.get(i);
				for (String field : matchingPatientExpectedFields) {
					if (!patient.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			String matchingPatientStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (matchingPatientStatusField == null || !matchingPatientStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "getRegisteredPatientsWithAuth":
			// Expected fields in each registered patient's details
			List<String> registeredPatientExpectedFields = List.of("PatientId", "FirstName", "LastName");

			// Validate "Results" array
			List<Map<String, Object>> registeredPatientResults = customResponse.getResponse().jsonPath()
					.getList("Results");
			if (registeredPatientResults == null || registeredPatientResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < registeredPatientResults.size(); i++) {
				Map<String, Object> patient = registeredPatientResults.get(i);
				for (String field : registeredPatientExpectedFields) {
					if (!patient.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			String registeredPatientStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (registeredPatientStatusField == null || !registeredPatientStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		case "getBillingCountersWithAuth":
			// Expected fields in each billing counter's details
			List<String> billingCounterExpectedFields = List.of("CounterId", "CounterName");

			// Validate "Results" array
			List<Map<String, Object>> billingCounterResults = customResponse.getResponse().jsonPath()
					.getList("Results");
			if (billingCounterResults == null || billingCounterResults.isEmpty()) {
				isValid = false;
				System.out.println("Results section is missing or empty in the response.");
				break;
			}

			for (int i = 0; i < billingCounterResults.size(); i++) {
				Map<String, Object> counter = billingCounterResults.get(i);
				for (String field : billingCounterExpectedFields) {
					if (!counter.containsKey(field)) {
						isValid = false;
						System.out.println("Missing field in Results[" + i + "]: " + field);
					}
				}
			}

			// Validate top-level fields
			String billingCounterStatusField = customResponse.getResponse().jsonPath().getString("Status");
			if (billingCounterStatusField == null || !billingCounterStatusField.equals("OK")) {
				isValid = false;
				System.out.println("Status field is missing or invalid in the response.");
			}
			break;

		default:
			System.out.println("Method " + methodName + " is not recognized for validation.");
			isValid = false;
		}
		return isValid;
	}
}