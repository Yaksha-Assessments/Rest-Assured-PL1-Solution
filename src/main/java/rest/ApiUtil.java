package rest;

import java.util.List;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ApiUtil {

	private static final String BASE_URL = "https://healthapp.yaksha.com/api";

	/**
	 * @Test1 This method creates a new appointment with authorization.
	 * 
	 * @param endpoint - The API endpoint to which the request is sent.
	 * @param body     - A map containing the appointment details (FirstName,
	 *                 LastName, etc.).
	 * @description This method constructs a JSON payload from the given map, sends
	 *              a POST request to the specified endpoint with the authorization
	 *              header, and returns the response.
	 * @return Response - The response from the API after attempting to create the
	 *         appointment.
	 */
	public CustomResponse createAppointmentWithAuth(String endpoint, String requestBody) {
		Response response = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json").body(requestBody).post(BASE_URL + endpoint).then().extract()
				.response();

		// Extracting additional data to create CustomResponse
		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		Integer appointmentId = response.jsonPath().getInt("Results.AppointmentId");

		// Creating and returning the CustomResponse object
		return new CustomResponse(response, statusCode, status, appointmentId);
	}

	/**
	 * @Test2 This method cancels an existing appointment with authorization.
	 * 
	 * @param endpoint - The API endpoint to which the request is sent for canceling
	 *                 the appointment.
	 * @param body     - An optional object representing the request body. This
	 *                 parameter can be null since the cancelation does not require
	 *                 a body payload.
	 * @description This method builds a PUT request with the authorization header
	 *              and specified endpoint. If a body is provided, it includes that
	 *              in the request; otherwise, it sends the request without a body.
	 * @return Response - The response from the API after attempting to cancel the
	 *         appointment.
	 */
	public CustomResponse getAllApplicableDoctorsWithAuth(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		// Only add the body if it's not null
		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		// Extracting additional data to create CustomResponse
		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		// Creating and returning the CustomResponse object with additional results data
		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test3 This method cancels an existing appointment with authorization.
	 * 
	 * @param endpoint - The API endpoint to which the request is sent for canceling
	 *                 the appointment.
	 * @param body     - An optional object representing the request body. This
	 *                 parameter can be null since the cancelation does not require
	 *                 a body payload.
	 * @description This method builds a PUT request with the authorization header
	 *              and specified endpoint. If a body is provided, it includes that
	 *              in the request; otherwise, it sends the request without a body.
	 * @return CustomResponse - The response from the API after attempting to cancel
	 *         the appointment.
	 */
	public CustomResponse cancelAppointmentWithAuth(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		// Only add the body if it's not null
		if (body != null) {
			request.body(body);
		}

		Response response = request.put(BASE_URL + endpoint).then().extract().response();

		// Extract additional data for creating CustomResponse
		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		String resultMessage = response.jsonPath().getString("Results");

		// Creating and returning the CustomResponse object
		return new CustomResponse(response, statusCode, status, resultMessage);
	}

	/**
	 * @Test4 This method finds if there is any clashing appointment.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @description This method sends a GET request to the specified endpoint with
	 *              the necessary authorization header and query parameters to
	 *              search for clashing appointments in the system.
	 * @return CustomResponse - The API's response after attempting to search for
	 *         clashing appointments, including HTTP status code, status message,
	 *         and results.
	 */
	public CustomResponse clashAppointmentWithAuth(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		// Add the body if not null
		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		// Extract data for CustomResponse
		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		String resultMessage = response.jsonPath().getString("Results");

		return new CustomResponse(response, statusCode, status, resultMessage);
	}

	/**
	 * @Test5 This method searches for a patient using specified query parameters.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @description This method sends a GET request to the specified endpoint with
	 *              the necessary authorization header and query parameters to
	 *              search for patients in the system.
	 * @return CustomResponse - The API's response, including HTTP status code,
	 *         status message, and the list of matching patients.
	 */
	public CustomResponse searchPatientWithAuth(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		// Add the body if not null
		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		// Extract data for CustomResponse
		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test6 This method retrieves a list of appointments for a specified performer
	 *        within a given date range.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @description This method retrieves appointments within a given date range.
	 * @return CustomResponse - The API's response, including HTTP status code,
	 *         status message, and list of appointments in the "Results" field.
	 */
	public CustomResponse bookingListWithAuthInRange(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		// Add the body if not null
		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		// Extract data for CustomResponse
		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test7 This method retrieves the complete list of stock details from the
	 *        pharmacy.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @return CustomResponse - The API response, including HTTP status code, status
	 *         message, and a list of stock items in the "Results" field.
	 */
	public CustomResponse AllStockDetailsWithAuth(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		// Add the body if not null
		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		// Extract data for CustomResponse
		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test8 This method retrieves details of the main store in the pharmacy
	 *        settings.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @return CustomResponse - The API response, including HTTP status code, status
	 *         message, and the store details in the "Results" field.
	 */
	public CustomResponse MainStoreDetailsWithAuth(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		// Add the body if not null
		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		// Extract data for CustomResponse
		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		Map<String, Object> results = response.jsonPath().getMap("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test9 This method retrieves a list of pharmacy stores and verifies the
	 *        details of each store.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @return CustomResponse - The API response, including HTTP status code, status
	 *         message, and the list of stores in the "Results" field.
	 */
	public CustomResponse PharmacyStoresWithAuth(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		// Add the body if not null
		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		// Extract data for CustomResponse
		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test10 This method retrieves and verifies patient consumption details.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @return CustomResponse - The API response, including HTTP status code, status
	 *         message, and the list of patients' consumption details in the
	 *         "Results" field.
	 */
	public CustomResponse PatientConsumption(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test11 This method activates a pharmacy counter using counter details.
	 *
	 * @param endpoint - The API endpoint to which the PUT request is sent.
	 * @return CustomResponse - The API response, including HTTP status code, status
	 *         message, and the details of the activated counter in the "Results"
	 *         field.
	 */
	public CustomResponse ActivatePharmCount(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		if (body != null) {
			request.body(body);
		}

		Response response = request.put(BASE_URL + endpoint).then().extract().response();

		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		Map<String, Object> results = response.jsonPath().getMap("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test12 This method deactivates a pharmacy counter.
	 *
	 * @param endpoint - The API endpoint to which the PUT request is sent.
	 * @return CustomResponse - The API response, including HTTP status code, status
	 *         message, and details of the deactivated counter in the "Results"
	 *         field.
	 */
	public CustomResponse DeactivatePharmCount(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		if (body != null) {
			request.body(body);
		}

		Response response = request.put(BASE_URL + endpoint).then().extract().response();

		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		Map<String, Object> results = response.jsonPath().getMap("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test13 This method retrieves and verifies the list of appointment applicable
	 *         departments.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @return CustomResponse - The API response, including HTTP status code, status
	 *         message, and the list of applicable departments in the "Results"
	 *         field.
	 */
	public CustomResponse AppointApplicDept(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test14 This method retrieves and verifies the list of currently admitted
	 *         patients.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @return CustomResponse - The API response, including HTTP status code, status
	 *         message, and the list of admitted patients in the "Results" field.
	 */
	public CustomResponse admittedPatientData(String endpoint, Object body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test15 This method adds a new currency using the provided data.
	 *
	 * @param endpoint - The API endpoint to which the POST request is sent.
	 * @param body     - The payload containing currency data.
	 * @return CustomResponse - The API response, including HTTP status code, status
	 *         message, and details of the added currency in the "Results" field.
	 */
	public CustomResponse addCurrencyWithAuth(String endpoint, Map<String, String> body) {
		Response response = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json").body(body).post(BASE_URL + endpoint).then().extract()
				.response();

		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		Map<String, Object> results = response.jsonPath().getMap("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test16 This method finds if a patient with the requested phone number
	 *         already exists.
	 *
	 * @param endpoint - The API endpoint to which the request is sent.
	 * @param body     - A map containing the body of the request.
	 * @return CustomResponse - The response from the API, including status, status
	 *         code, and results.
	 */
	public CustomResponse findMatchingPatientWithAuth(String endpoint, Map<String, String> body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		if (body != null) {
			request.body(body);
		}
		System.out.println(BASE_URL + endpoint);
		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test17 This method lists all registered patients and checks if their patient
	 *         IDs are unique.
	 *
	 * @param endpoint - The API endpoint to which the request is sent.
	 * @param body     - A map containing the body of the request.
	 * @return CustomResponse - The response from the API, including status, status
	 *         code, and results.
	 */
	public CustomResponse getRegisteredPatientsWithAuth(String endpoint, Map<String, String> body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}

	/**
	 * @Test18 This method retrieves and verifies the list of Billing Counters.
	 *
	 * @param endpoint - The API endpoint to which the GET request is sent.
	 * @param body     - Optional body for the request.
	 * @return CustomResponse - The response from the API, including status, status
	 *         code, and results.
	 */
	public CustomResponse getBillingCountersWithAuth(String endpoint, Map<String, String> body) {
		RequestSpecification request = RestAssured.given().header("Authorization", AuthUtil.getAuthHeader())
				.header("Content-Type", "application/json");

		if (body != null) {
			request.body(body);
		}

		Response response = request.get(BASE_URL + endpoint).then().extract().response();

		int statusCode = response.statusCode();
		String status = response.jsonPath().getString("Status");
		List<Map<String, Object>> results = response.jsonPath().getList("Results");

		return new CustomResponse(response, statusCode, status, results);
	}
}