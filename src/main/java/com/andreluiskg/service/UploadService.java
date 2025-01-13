package com.andreluiskg.service;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.andreluiskg.clients.FileServerClient;
import com.andreluiskg.clients.MessageClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class UploadService {

	private static final Logger LOGGER = Logger.getLogger(UploadService.class);

	@Inject
	FileServerClient fileServerClient;

	@Inject
	MessageClient messageClient;

	public Response uploadFile(MultipartFormDataInput input) {
		try {
			InputStream uploadedInputStream = getUploadedInputStream(input);
			if (uploadedInputStream == null) {
				LOGGER.error("\n***File input stream is missing***");
				return Response.status(400).entity("File is missing").build();
			}

			String fileName = getFileName(input);
			String sourceName = input.getFormDataPart("sourceName", String.class, null);
			String userName = input.getFormDataPart("userName", String.class, null);
			
			// Calculate the file size correctly
			int fileSize = uploadedInputStream.available();
			byte[] fileContent = uploadedInputStream.readAllBytes();
			fileSize = fileContent.length;
			
			LOGGER.info("\n*** fileName = "
					+ fileName + ", sourceName = "
					+ sourceName + ", userName = " 
					+ userName	+ ", fileSize(bytes) = "
					+ fileSize + " ***");

			return processFileUpload(uploadedInputStream, fileName, sourceName, userName);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("\n***Error processing form data***", e);
			return Response.status(500).entity("Error processing form data").build();
		}
	}

	private InputStream getUploadedInputStream(MultipartFormDataInput input) throws IOException {
		if (input.getFormDataMap().containsKey("file")) {
			InputStream uploadedInputStream = input.getFormDataPart("file", InputStream.class, null);
			LOGGER.info("\n***File input stream obtained successfully***");
			return uploadedInputStream;
		}
		return null;
	}

	private String getFileName(MultipartFormDataInput input) {
		return input
				.getFormDataMap().get("file")
				.get(0).getHeaders()
				.getFirst("Content-Disposition")
				.split(";")[2]
				.split("=")[1].trim().replaceAll("\"", "");
	}

	private Response processFileUpload(InputStream uploadedInputStream, String fileName, String sourceName, String userName) {
		boolean isUploaded = fileServerClient.uploadToMinIO(uploadedInputStream, fileName);
		if (isUploaded) {
			boolean isMessageSent = messageClient.sendMessageToQueue(fileName, sourceName, userName);
			if (isMessageSent) {
				LOGGER.info("\n***File uploaded successfully to MinIO and message sent to RabbitMQ***");
				return Response.status(200).entity("File uploaded successfully to MinIO and message sent to RabbitMQ").build();
			} else {
				fileServerClient.deleteFromMinIO(fileName);
				LOGGER.error("\n***Error sending message to RabbitMQ, file deleted from MinIO***");
				return Response.status(500).entity("Error sending message to RabbitMQ, file deleted from MinIO").build();
			}
		} else {
			LOGGER.error("\n***Error uploading file to MinIO***");
			return Response.status(500).entity("Error uploading file to MinIO").build();
		}
	}

}
