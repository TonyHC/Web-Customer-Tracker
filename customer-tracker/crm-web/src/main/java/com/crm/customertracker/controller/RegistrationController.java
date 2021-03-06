package com.crm.customertracker.controller;

import com.crm.customertracker.entity.security.RegisterUser;
import com.crm.customertracker.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/register")
@PropertySource("classpath:security.properties")
public class RegistrationController {
	private final UserService userService;

	public RegistrationController(UserService userService) {
		this.userService = userService;
	}

	private final Logger logger = Logger.getLogger(getClass().getName());
	
	// Load all Security Roles from 'security.properties' file
	@Value("#{${security.roles}}")
	private Map<String, String> roles;
	
	@InitBinder
	public void initBinder(WebDataBinder dataBinder) {
		// Every time a String is encountered, the String's leading and trailing white spaces are trimmed
		// If only white spaces, set String to null
		StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
		dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
	}

	@ModelAttribute("registerUser")
	public RegisterUser getNewRegisterUser() {
		// Add an empty RegisterUser object to Model Attribute for Form Validation
		return RegisterUser.builder().build();
	}

	@ModelAttribute("roles")
	public Map<String, String> getSecurityRoles() {
		// Add all the Security Roles to Model Attribute for the Drop-Down List
		return roles;
	}
	
	@GetMapping("/showRegistrationForm")
	public String showRegistrationForm() {
		return "security/registration-form";
	}
	
	@PostMapping("/processRegistrationForm")
	public String processRegistrationForm(@Valid @ModelAttribute("registerUser") RegisterUser registerUser,
			BindingResult bindingResult, Model model) {
		String username = registerUser.getUserName();
		logger.info("Processing Registration Form for: " + username);
		
		// Registration Form Validation to check if any Constraint Validators were violated
		if (bindingResult.hasErrors()) {
			// Send back to Registration Form
			return "security/registration-form";
		}
		
		// Check if Username already exists in DB
		if (userService.findByUserName(username) != null) {
			model.addAttribute("registrationError", "Username already exists");
			logger.warning("Username already exists");
			
			return "security/registration-form";
		}
		
		// Give the newly created User with the default role of "EMPLOYEE"
		// Stores a String representation of an authority granted to the Authentication object
		List<String> roles = new ArrayList<>();
		roles.add("ROLE_EMPLOYEE");
		
        // If the User selects the role other than EMPLOYEE,
        // then also add that role (multiple roles for one user)
		String formRole = registerUser.getFormRole();
		if (!formRole.equals("ROLE_EMPLOYEE")) {
			roles.add(formRole);
		}
		
		// Save the User to DB using User Service
		userService.saveUser(registerUser, roles);
		
		logger.info("Successfully registered the user: " + username);
		
		return "security/registration-confirmation";
	}
}