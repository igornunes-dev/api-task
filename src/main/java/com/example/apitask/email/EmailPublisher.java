package com.example.apitask.email;

import com.example.apitask.models.Tasks;
import com.example.apitask.models.Users;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String queueNameForWelcome;
    private final String queueNameForTask;

    public EmailPublisher(RabbitTemplate rabbitTemplate, @Value("${rabbit.name}") String queueName, @Value("${rabbit.task.name}") String queueNameForTask) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueNameForWelcome = queueName;
        this.queueNameForTask = queueNameForTask;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    public void sendWelcomeEmail(Users users) {
        EmailMessage mail = new EmailMessage(
                users.getEmail(), "Welcome to apitask", "Hello, " + users.getUsername() + ", Welcome to apitask."
        );
        rabbitTemplate.convertAndSend(queueNameForWelcome, mail);
    }

    public void sendTaskEmail(Users users, Tasks tasks) {
        EmailMessageForTask emailMessage = new EmailMessageForTask();
        emailMessage.setTo(users.getEmail());
        emailMessage.setUserName(users.getUsername());
        emailMessage.setTaskName(tasks.getName());
        emailMessage.setTaskDueDate(tasks.getDateExpiration().toString());

        rabbitTemplate.convertAndSend(queueNameForTask, emailMessage);
    }
}
