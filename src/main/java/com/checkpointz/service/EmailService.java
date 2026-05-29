package com.checkpointz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Importação nova
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

   
    @Value("${spring.mail.username}")
    private String emailRemetente; 

    public void enviarEmailVerificacao(String emailDestino, String token) {
        String linkValidacao = "http://localhost:8080/api/verificar-email?token=" + token;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        
        
        mensagem.setFrom(emailRemetente.trim()); 
        mensagem.setTo(emailDestino.trim());     
        
        mensagem.setSubject("Verifique a sua conta no CheckpointZ!");
        mensagem.setText("Olá!\n\nBem-vindo ao CheckpointZ. Para ativar a sua conta e começar a fazer publicações, por favor clique no link abaixo:\n\n" 
                         + linkValidacao + "\n\nObrigado!");

        mailSender.send(mensagem);
    }
}