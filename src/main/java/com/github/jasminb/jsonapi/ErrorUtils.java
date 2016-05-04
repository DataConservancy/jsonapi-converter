package com.github.jasminb.jsonapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.models.errors.ErrorResponse;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

/**
 * Utility class providing methods needed for parsing JSON API Spec errors.
 *
 * @author jbegic
 */
public class ErrorUtils {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	private ErrorUtils() {
		// Private constructor
	}

	/**
	 * Parses provided ResponseBody and returns it as ErrorResponse.
	 * @param errorResponse error response body
	 * @return ErrorResponse collection
	 * @throws IOException
	 */
	public static ErrorResponse parseErrorResponse(ResponseBody errorResponse) throws IOException {
		return MAPPER.readValue(errorResponse.bytes(), ErrorResponse.class);
	}

	/**
	 * Parses provided JsonNode and returns it as ErrorResponse.
	 * @param errorResponse error response body
	 * @return ErrorResponse collection
	 * @throws JsonProcessingException thrown in case JsonNode is not parseable
	 */
	public static ErrorResponse parseError(JsonNode errorResponse) throws JsonProcessingException {
		return MAPPER.treeToValue(errorResponse, ErrorResponse.class);
	}

	/**
	 * Returns {@code true} if the candidate node contains an 'errors' member.  Nodes that contain an 'errors' member
	 * may be processed by the {@code ErrorUtils} class.
	 *
	 * @param candidate a {@code JsonNode} that may or may not contain an 'errors' member.
	 * @return true if the candidate node contains an 'errors' member, false otherwise.
     */
	public static boolean hasErrors(JsonNode candidate) {
		return candidate != null && candidate.has(JSONAPISpecConstants.ERRORS);
	}

}
