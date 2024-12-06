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
}