package com.andreluiskg.clients;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MessageClient {

	private static final Logger LOGGER = Logger.getLogger(MessageClient.class);

	@Inject
	@ConfigProperty(name = "rabbitmq.queue.name")
	String rabbitmqQueueName;

	@Inject
	@ConfigProperty(name = "rabbitmq.host")
	String rabbitmqHost;

	@Inject
	@ConfigProperty(name = "rabbitmq.username")
	String rabbitmqUsername;

	@Inject
	@ConfigProperty(name = "rabbitmq.password")
	String rabbitmqPassword;

	@Inject
	@ConfigProperty(name = "rabbitmq.port")
	int rabbitmqPort;

	public boolean sendMessageToQueue(String fileName, String sourceName, String userName) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(rabbitmqHost);
		factory.setPort(rabbitmqPort);
		factory.setUsername(rabbitmqUsername);
		factory.setPassword(rabbitmqPassword);
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
			channel.queueDeclare(rabbitmqQueueName, true, false, false, null);
			String message = String.format("File Name: %s, Source Name: %s, User Name: %s, Date/Time: %s", fileName,
					sourceName, userName, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
			channel.basicPublish("", rabbitmqQueueName, null, message.getBytes());
			LOGGER.info("\n***Message sent to RabbitMQ: " + message + "***");
			return true;
		} catch (Exception e) {
			LOGGER.error("\n***Error sending message to RabbitMQ***", e);
			return false;
		}
	}

}
