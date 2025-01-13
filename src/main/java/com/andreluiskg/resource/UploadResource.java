package com.andreluiskg.resource;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import com.andreluiskg.service.UploadService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class UploadResource {

	@Inject
	UploadService greetingService;

	private static final Logger LOGGER = Logger.getLogger(UploadResource.class.getName());

	@GET
	@Path("/hello")
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		return "Hello RESTEasy";
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(MultipartFormDataInput input) {
		return greetingService.uploadFile(input);
	}

	@POST
	@Path("/logfile")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response logFileDetails(MultipartFormDataInput input) {
		try {
			Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
			List<InputPart> inputParts = uploadForm.get("file");

			for (InputPart inputPart : inputParts) {
				// Extract file details
				String fileName = inputPart.getHeaders().getFirst("Content-Disposition").split(";")[2].split("=")[1].trim().replaceAll("\"", "");
				InputStream inputStream = inputPart.getBody(InputStream.class, null);
				int fileSize = inputStream.available();

				// Log file details
				LOGGER.info("*******************");
				LOGGER.info("File Name: " + fileName);
				LOGGER.info("File Size: " + fileSize + " bytes");
				LOGGER.info("*******************\n");
			}
			return Response.ok().build();
		} catch (Exception e) {
			LOGGER.severe("Error processing file: " + e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/read-log-messages")
	@Produces(MediaType.TEXT_PLAIN)
	public Response readAndLogMessages() {
		messageClient.readMessagesFromQueue();
		return Response.ok("Reading and logging messages from RabbitMQ...").build();
	}

}

/**
 * Exemplo de comando curl para enviar um arquivo para o endpoint /logfile:
 * curl -X POST -F "file=@caminho/do/arquivo" http://localhost:8081/logfile
 */
