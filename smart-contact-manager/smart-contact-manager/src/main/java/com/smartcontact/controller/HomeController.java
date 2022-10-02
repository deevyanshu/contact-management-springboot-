package com.smartcontact.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontact.dao.UserRepository;
import com.smartcontact.entities.User;
import com.smartcontact.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title","home page");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title","about page");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model)
	{
		model.addAttribute("title","register");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@RequestMapping(value = "/do_register",method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result,@RequestParam(value = "agreement"
	,defaultValue = "false") boolean agreement,Model model,HttpSession session)
	{
		try {
			if(!agreement)
			{
				throw new Exception("you have not accepted terms and conditioins");
			}
			if(result.hasErrors())
			{
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			/*System.out.println("agreement"+agreement);
			System.out.println("user"+user);*/
			
			User res=userRepository.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully-register","alert-success"));
			return "signup";
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong "+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
		
	}
	
	@RequestMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title","signin");
		return "login";
	}

}
