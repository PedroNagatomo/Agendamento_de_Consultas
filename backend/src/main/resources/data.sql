-- Médicos
INSERT INTO medicos (nome, email, telefone, crm, especialidade, data_cadastro) VALUES
('Dr. João Silva', 'joao.silva@clinica.com', '(11) 99999-8888', 'CRM/SP 123456', 'Cardiologia', NOW()),
('Dra. Maria Santos', 'maria.santos@clinica.com', '(11) 98888-7777', 'CRM/SP 234567', 'Dermatologia', NOW()),
('Dr. Pedro Oliveira', 'pedro.oliveira@clinica.com', '(11) 97777-6666', 'CRM/SP 345678', 'Ortopedia', NOW()),
('Dra. Ana Costa', 'ana.costa@clinica.com', '(11) 96666-5555', 'CRM/SP 456789', 'Pediatria', NOW());

-- Pacientes
INSERT INTO pacientes (nome, email, telefone, cpf, data_nascimento, data_cadastro) VALUES
('Carlos Ferreira', 'carlos@email.com', '(11) 95555-4444', '123.456.789-00', '1980-05-15', NOW()),
('Fernanda Lima', 'fernanda@email.com', '(11) 94444-3333', '234.567.890-11', '1992-08-22', NOW()),
('Ricardo Souza', 'ricardo@email.com', '(11) 93333-2222', '345.678.901-22', '1975-12-10', NOW()),
('Juliana Santos', 'juliana@email.com', '(11) 92222-1111', '456.789.012-33', '1988-03-30', NOW());