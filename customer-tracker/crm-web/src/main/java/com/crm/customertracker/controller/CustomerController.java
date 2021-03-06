package com.crm.customertracker.controller;

import com.crm.customertracker.entity.customer.Customer;
import com.crm.customertracker.entity.customer.License;
import com.crm.customertracker.entity.security.User;
import com.crm.customertracker.service.CustomerService;
import com.crm.customertracker.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {
	private final CustomerService customerService;
	private final UserService userService;

	public CustomerController(CustomerService customerService, UserService userService) {
		this.customerService = customerService;
		this.userService = userService;
	}

	@ModelAttribute("firstName")
	public String getAuthenticatedUserFirstName() {
		// Obtain the authenticated User from User Service
		User user = userService.retrieveAuthenticatedPrincipalByUsername();

		// Add Authenticated User's First Name to Model Attribute
		return user.getFirstName();
	}

	@GetMapping("/list")
	public String listCustomers(Model model) {
		// Call the findPaginated(): Set the starting page number (zero-based), sort field,
		// sort direction, and model object
		return findPaginatedCustomers(1, "firstName", "asc", model);
	}

	@GetMapping("/showFormForAddingCustomer")
	public String showFormForAddingCustomer(Model model) {
		// Create an empty Customer object
		Customer customer = Customer.builder().build();

		// Add empty Customer object to Model Attribute
		model.addAttribute("customer", customer);

		return "customers/customer-form";
	}

	@PostMapping("/saveCustomer")
	public String saveCustomer(@Valid @ModelAttribute("customer") Customer customer, BindingResult bindingResult) {
		// If the 'customer-form' has any Form Errors, then return to the 'customer-form'
		if (bindingResult.hasErrors()) {
			return "customers/customer-form";
		} else {
			// Else either save or update the Customer depending on whether the Customer has an id (Primary Key)
			customerService.saveCustomer(customer);

			// Once the Customer has been saved, redirect back to 'customers-lists'
			return "redirect:/customers/list";
		}
	}

	@GetMapping("/showFormForUpdatingCustomer")
	public String showFormForUpdatingCustomer(@RequestParam("customerId") int customerId, Model model) {
		// Find a Customer by its ID using Customer Service
		Customer customer = customerService.findCustomerById(customerId);

		// Add the existing Customer to the Model Attribute to populate the Form
		model.addAttribute("customer", customer);

		return "customers/customer-form";
	}

	@GetMapping("/deleteCustomer")
	public String deleteCustomer(@RequestParam("customerId") int customerId) {
		// Delete an existing Customer by its ID using Customer Service
		customerService.deleteCustomerById(customerId);

		return "redirect:/customers/list";
	}

	@GetMapping("/licenses")
	public String listCustomerLicenses(@RequestParam("customerId") int customerId, Model model) {
		// Obtain the Customer along with its License using Customer Service
		Customer customer = customerService.findCustomerLicenses(customerId);

		// Add Customer to Model Attribute
		model.addAttribute("customer", customer);

		// Retrieve all Licenses from Customer
		List<License> licenses = customer.getLicenses();

		// Add License to Model Attribute
		model.addAttribute("licenses", licenses);

		return "customers/customer-licenses";
	}

	@GetMapping("/searchCustomers")
	public String searchCustomersByName(@RequestParam("customerName") String customerName, Model model) {
		// Find a List of Customers whose name matches the name being searched using Customer Service
		List<Customer> customers = customerService.findCustomersByName(customerName);

		// Add the matching Customers to the Model Attribute
		model.addAttribute("customers", customers);

		return "customers/list-customers";
	}

	@GetMapping("/page/{pageNumber}")
	public String findPaginatedCustomers(@PathVariable(value = "pageNumber") int pageNumber,
										 @RequestParam("sortField") String sortField,
										 @RequestParam("sortDirection") String sortDirection,
										 Model model) {
		// Set Page Size for each Page
		int pageSize = 5;

		// Get all Customers from Page using CustomerService
		Page<Customer> page = customerService.findPaginatedCustomers(pageNumber, pageSize, sortField, sortDirection);
		List<Customer> customers = page.getContent();

		// Set Pagination Values to Model Attribute
		model.addAttribute("currentPage", pageNumber);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// Set Sort Values to Model Attribute
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDirection", sortDirection);
		model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

		// Add List of Customers to Model Attribute
		model.addAttribute("customers", customers);

		return "customers/list-customers";
	}
}