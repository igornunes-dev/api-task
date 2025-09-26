package com.example.apitask.scheduleds;

import com.example.apitask.email.EmailPublisher;
import com.example.apitask.models.Tasks;
import com.example.apitask.models.Users;
import com.example.apitask.repositories.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TaskScheduled {

    private final UsersRepository usersRepository;
    private final EmailPublisher emailPublisher;

    public TaskScheduled(UsersRepository usersRepository, EmailPublisher emailPublisher) {
        this.usersRepository = usersRepository;
        this.emailPublisher = emailPublisher;
    }

    @Scheduled(cron = "0 * * * * ?", zone = "America/Sao_Paulo")
    @Transactional
    public void verificationTaskForUser() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        System.out.println("=======================================");
        System.out.println("Scheduler executando: " + java.time.LocalDateTime.now());
        System.out.println("Verificando tarefas para o dia: " + tomorrow);

        List<Users> users = usersRepository.findUserWithPendingTasks(tomorrow);
        System.out.println("Usuários encontrados: " + users.size());

        for (Users user : users) {
            List<Tasks> pendingTask = user.getTasks().stream()
                    .filter(t -> !t.getCompleted() && t.getDateExpiration() != null && !t.getDateExpiration().isBefore(tomorrow))
                    .toList();

            System.out.println("Usuário: " + user.getEmail() + " | Tarefas pendentes: " + pendingTask.size());

            for (Tasks task : pendingTask) {
                System.out.println("Enviando e-mail para tarefa: " + task.getName() + " | Data de vencimento: " + task.getDateExpiration());
                emailPublisher.sendTaskEmail(user, task);
            }
        }

        System.out.println("Fim da execução do scheduler");
        System.out.println("=======================================");
    }
}
