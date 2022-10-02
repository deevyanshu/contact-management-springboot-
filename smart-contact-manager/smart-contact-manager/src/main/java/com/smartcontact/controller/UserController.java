package com.smartcontact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartcontact.dao.ContactRepository;
import com.smartcontact.dao.UserRepository;
import com.smartcontact.entities.Contact;
import com.smartcontact.entities.User;
import com.smartcontact.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder encoder;

	@Autowired
	private UserRepository repository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to all response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)//principal is imported from java.security package to retreive the information of logged in user
	{
		String username=principal.getName();
		//System.out.println(username);
		User user=repository.getUserByUserName(username);
		//System.out.println(user);
		model.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashbord(Model model,Principal principal)
	{
		model.addAttribute("title","dashboard");
		return "user_dashbord";
	}
	
	@RequestMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("form_title","Add Contact");
		model.addAttribute("action","process-contact");
		model.addAttribute("contact",new Contact());
		return "add_contact";
	}
	
	@RequestMapping(value = "/process-contact",method = RequestMethod.POST)
	public String processContact(@Valid @ModelAttribute("contact") Contact contact,BindingResult result
			,@RequestParam("profileImage") MultipartFile file,Principal principal,Model model,HttpSession session)
	{
		try {
			if(result.hasErrors())
			{
				model.addAttribute("contact",contact);
				return "add_contact";
			}
			String username=principal.getName();
			User user=repository.getUserByUserName(username);
			if(file.isEmpty())
			{
				contact.setImage("contact.png");
			}else
			{
				contact.setImage(file.getOriginalFilename());
				// uploading file in img folder
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			repository.save(user);
			model.addAttribute("contact",new Contact());
			session.setAttribute("message",new Message("contact added,Add more", "alert-success"));// session is used for sending the success or failure message on page. 
			return "add_contact";
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			session.setAttribute("message",new Message("somethin went wrong. Try again", "alert-danger"));
			return "add_contact";
		}
	}
	
	@RequestMapping(value = "show-contacts/{page}")
	public String showContacts(@PathVariable("page")int page, Model model,Principal principal)
	{
		// one of the way of retrieving all the contacts.
		/*String username=principal.getName();
		User user=repository.getUserByUserName(username);
		List<Contact> contacts=user.getContacts();
		model.addAttribute("title","contacts");
		model.addAttribute("contacts",contacts);*/
		
		String username=principal.getName();
		User user=repository.getUserByUserName(username);
		
		Pageable pageable= PageRequest.of(page, 5);
		Page<Contact> contacts=contactRepository.findContactsById(user.getId(),pageable);
		model.addAttribute("title","contacts");
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentpage",page);
		model.addAttribute("totalpages",contacts.getTotalPages());
		return "show_contacts";
	}
	
	// to show particular contact details.
	@RequestMapping("/contact/{cid}")
	public String showContactDetail(@PathVariable("cid")int cid,Model model,Principal principal)
	{
		model.addAttribute("title","contact details");
		Optional<Contact> contactOptional= contactRepository.findById(cid);
		if(contactOptional.isEmpty())
		{
			model.addAttribute("error_message","No contact found");
			return "contact_detail";
		}
		Contact contact=contactOptional.get();
		String username=principal.getName();
		User user=repository.getUserByUserName(username);
		if(user.getId()==contact.getUser().getId()) // if contacts belong to the logged in user.
		{
			model.addAttribute("contact",contact);
		}else // if contact does not belong to logged in user.
		{
			model.addAttribute("error_message","You do not have permission to view this contact");
		}
		
		return "contact_detail";
	}
	
	@RequestMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") int cid,Model model,Principal principal)
	{
		String username=principal.getName();
		User user=repository.getUserByUserName(username);
		Optional<Contact> contacOptional=contactRepository.findById(cid);
		Contact contact=contacOptional.get();
		if(user.getId()==contact.getUser().getId())
		{
			contactRepository.deleteById(cid);
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	@RequestMapping(value = "/update-contact/{cid}",method = RequestMethod.GET)
	public String openUpdateContact(@PathVariable("cid") int cid,Model model)
	{
		model.addAttribute("title","update");
		model.addAttribute("form_title","Update Contact");
		model.addAttribute("action","process-update");
		Optional<Contact> contactOptional=contactRepository.findById(cid);
		Contact contact=contactOptional.get();
		model.addAttribute("contact",contact);
		
		return "add_contact";
	}
	
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateContact(@Valid @ModelAttribute("contact") Contact contact,BindingResult result,Principal principal,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session)
	{
		if(result.hasErrors())
		{
			model.addAttribute("contact",contact);
			return "add_contact";
		}
		String username=principal.getName();
		User user=repository.getUserByUserName(username);
		contact.setUser(user);
		try {
			if(file.isEmpty())
			{
				contact.setImage("contact.png");
			}else
			{
				contact.setImage(file.getOriginalFilename());
				// uploading file in img folder
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
			}
			contactRepository.save(contact);
			session.setAttribute("message", new Message("updated successfully", "alert-success"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			session.setAttribute("message", new Message("something went wrong", "alert-danger"));
		}
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	@RequestMapping("/profile")
	public String profile(Model model)
	{
		model.addAttribute("title","profile");
		return "profile";
	}
	
	@RequestMapping("settings")
	public String openSetting()
	{
		return "settings";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldpassword") String oldpassword,@RequestParam("newpassword") String newpassword,Principal principal,HttpSession session)
	{
		String username=principal.getName();
		User user=repository.getUserByUserName(username);
		if(encoder.matches(oldpassword, user.getPassword()))
		{
			user.setPassword(encoder.encode(newpassword));
			repository.save(user);
			session.setAttribute("message", new Message("succesfully changed", "alert-success"));
			return "redirect:/user/index";
		}else
		{
			session.setAttribute("message", new Message("wrong old password", "alert-danger"));
			return "settings";
		}
		
	}
}
