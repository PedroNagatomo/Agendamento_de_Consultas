package com.agendamento.config;

import com.agendamento.model.*;
import com.agendamento.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepository) {
        return args -> {
            // Cria admin se não existir
            if (usuarioRepository.findByEmail("admin@email.com").isEmpty()) {
                Admin admin = new Admin("Administrador", "admin@email.com", "admin123", "11999999999");
                usuarioRepository.save(admin);
                System.out.println("✅ Admin criado: admin@email.com / admin123");
            }

            // Cria médico exemplo
            if (usuarioRepository.findByEmail("dr.silva@email.com").isEmpty()) {
                Medico medico = new Medico("Dr. João Silva", "dr.silva@email.com", "medico123",
                        "11988888888", "CRM/SP 123456", "Cardiologia");
                usuarioRepository.save(medico);
                System.out.println("✅ Médico criado: dr.silva@email.com / medico123");
            }

            // Cria paciente exemplo
            if (usuarioRepository.findByEmail("paciente@email.com").isEmpty()) {
                Paciente paciente = new Paciente("Maria Santos", "paciente@email.com", "paciente123",
                        "11977777777", "1990-05-15", "Alergia a penicilina");
                usuarioRepository.save(paciente);
                System.out.println("✅ Paciente criado: paciente@email.com / paciente123");
            }

            // Conta usuários
            long total = usuarioRepository.count();
            System.out.println("📊 Total de usuários no banco: " + total);
        };
    }
}