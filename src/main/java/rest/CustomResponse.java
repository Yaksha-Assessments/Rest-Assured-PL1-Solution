package rest;

import java.util.List;
import java.util.Map;

import io.restassured.response.Response;

public class CustomResponse {
	private Response response;
	private int statusCode;
	private String status;
	private Integer appointmentId;
	private List<Map<String, Object>> results;

	public CustomResponse(Response response, int statusCode, String status, Integer appointmentId) {
		this.response = response;
		this.statusCode = statusCode;
		this.status = status;
		this.appointmentId = appointmentId;
	}

	public CustomResponse(Response response, int statusCode, String status, List<Map<String, Object>> results) {
		this.response = response;
		this.statusCode = statusCode;
		this.status = status;
		this.results = results;
	}

	public Response getResponse() {
		return response;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatus() {
		return status;
	}

	public Integer getAppointmentId() {
		return appointmentId;
	}

	public List<Map<String, Object>> getResults() {
		return results;
	}

	public void setResults(List<Map<String, Object>> results) {
		this.results = results;
	}
}
