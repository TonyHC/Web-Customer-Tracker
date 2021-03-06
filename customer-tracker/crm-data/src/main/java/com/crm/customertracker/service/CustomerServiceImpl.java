package com.crm.customertracker.service;

import com.crm.customertracker.entity.customer.Customer;
import com.crm.customertracker.entity.customer.License;
import com.crm.customertracker.repository.customer.CustomerRepository;
import com.crm.customertracker.repository.customer.LicenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {
	private final CustomerRepository customerRepository;
	private final LicenseRepository licenseRepository;

	public CustomerServiceImpl(CustomerRepository customerRepository, LicenseRepository licenseRepository) {
		this.customerRepository = customerRepository;
		this.licenseRepository = licenseRepository;
	}

	@Override
	public List<Customer> findAllCustomers() {
		// Return all Customers sorted by last name in ascending order
		return customerRepository.findAllByOrderByLastNameAsc();
	}

	@Override
	public Customer findCustomerById(int customerId) {
		// Optional: Different pattern instead of having to check nulls
		// Find a Customer by its ID
		Optional<Customer> result = customerRepository.findById(customerId);
		
		Customer customer = null;
		
		// If value is present, then return the value 
		if (result.isPresent()) {
			customer = result.get();
		} else {
			// Otherwise, throw a Runtime Exception
			throw new RuntimeException("Customer ID not found: " + customerId);
		}
		
		return customer;
	}

	@Override
	public void saveCustomer(Customer customer) {
		// Save the Customer
		customerRepository.save(customer);
	}

	@Override
	public void deleteCustomerById(int customerId) {
		// Delete the Customer by its ID
		customerRepository.deleteById(customerId);
	}

	@Override
	public Customer findCustomerLicenses(int customerId) {
		// Get the Customer along with its Licenses by the ID (Primary Key)
		Optional<Customer> result = Optional.ofNullable(customerRepository.findCustomerLicenses(customerId));
		
		Customer customer = null;
		
		// If result is not null, store the result in the Customer object
		if (result.isPresent()) {
			customer = result.get();
		} else {
			// Else create a new Customer object
			customer = Customer.builder().build();
		}
		
		return customer;
	}

	@Override
	public List<Customer> findCustomersByName(String customerName) {
		List<Customer> customers = null;
		
		// If form input data is not null, empty or length of String is less than or equal to zero
		if (customerName != null && customerName.trim().length() > 0) {
			// Then, perform a Query to search for Employees whose first or last name matches form input data
			// and sort by last name in ascending order
			customers = customerRepository.searchEmployeeByFirstOrLastName(customerName);
		} else {
			// Else, perform a Query to get all Employees and sort by last name ascending order 
			customers = customerRepository.findAllByOrderByLastNameAsc();
		}
		
		return customers;
	}

	@Override
	public Page<Customer> findPaginatedCustomers(int pageNumber, int pageSize, String sortField, String sortDirection) {
		// Create a Sort either ascending or descending based on if Sort Direction in URL is same as sort direction passed in
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() 
					: Sort.by(sortField).descending();
		
		// Create a Pageable object to perform PageRequest with sorted parameters applied
		Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

		// Return a page containing a list of customers
		return customerRepository.findAll(pageable);
	}

	@Override
	public List<License> findAllLicenses() {
		// Return all Licenses
		return licenseRepository.findAll();
	}

	@Override
	public void deleteLicenseById(int licenseId) {
		// Delete License by its ID (Primary Key)
		licenseRepository.deleteById(licenseId);
	}

	@Override
	public Page<License> findPaginatedLicenses(int pageNumber, int pageSize, String sortField, String sortDirection) {
		// Create a Sort either ascending or descending based on if Sort Direction in URL is same as sort direction passed in
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
					: Sort.by(sortField).descending();

		// Create a Pageable object to perform PageRequest with sorted parameters applied
		Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

		// Return a page containing a list of licenses
		return licenseRepository.findAll(pageable);
	}
}