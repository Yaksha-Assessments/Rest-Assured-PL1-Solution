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

		default:
			System.out.println("Method " + methodName + " is not recognized for validation.");
			isValid = false;
		}
		return isValid;
	}
}