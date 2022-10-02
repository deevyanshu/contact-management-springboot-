package com.smartcontact.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontact.service.EmailService;

@Controller
public class ForgotController {

	Random random=new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	@RequestMapping(value = "/send-otp",method = RequestMethod.POST)
	public String sendOtp(@RequestParam("email") String email,HttpSession session)
	{
		
		int otp=random.nextInt(9999);
		String subject="OTP from SCM";
		String message=""+otp;
		boolean flag=emailService.sendEmail(subject, message, email);
		if(flag)
		{
			return "verify_otp";
			
		}else
		{
			session.setAttribute("message", "check your email id");
			return "forgot_email_form";
		}
		
	}
}
