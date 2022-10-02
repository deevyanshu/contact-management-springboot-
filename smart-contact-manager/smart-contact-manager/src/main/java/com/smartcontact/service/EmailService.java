package com.smartcontact.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject,String message,String to)
	{
		boolean f=false;
		String host="smtp.gmail.com";
		
		Properties properties=System.getProperties();
		
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.host", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		Session session=Session.getInstance(properties,new Authenticator() {
		  @Override
		protected PasswordAuthentication getPasswordAuthentication() {
			// TODO Auto-generated method stub
			return new PasswordAuthentication("user@gmail.com", "password");
		}
		});
		
		session.setDebug(true);
		
		MimeMessage m= new MimeMessage(session);
		
		try {
			m.setFrom("user@gmail.com");
			
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			m.setSubject(subject);
			
			m.setText(message);
			
			Transport.send(m);
			
			System.out.println("sent success");
			
			f=true;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
}
