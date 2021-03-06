package com.crm.customertracker.service;

import com.crm.customertracker.entity.security.RegisterUser;
import com.crm.customertracker.entity.security.Role;
import com.crm.customertracker.entity.security.User;
import com.crm.customertracker.repository.security.RoleRepository;
import com.crm.customertracker.repository.security.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
	}

	// Implement this method from UserDetailsService
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// Find the User by its username
		User user = userRepository.findByUserName(username);

		// If User does not exist, then throw an UsernameNotFoundException
		if (user == null) {
			throw new UsernameNotFoundException("Invalid Username or Password");
		}

		// Return new User from Spring Security UserDetails by passing in username, password, and list of Granted Authorities
		return new org.springframework.security.core.userdetails.User(user.getUsername(),
				user.getPassword(), mapRolesToAuthorities(user.getRoles()));
	}

	// Helper method: To convert Collection<Role> to a List<SimpleGrantedAuthority> (with each SimpleGrantedAuthority having a role)
	private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
		return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
	}

	@Override
	public User findByUserName(String userName) {
		// Find and return a User by its username
		return userRepository.findByUserName(userName);
	}

	@Override
	public void saveUser(RegisterUser registerUser, List<String> roles) {
		// Create a BCryptPasswordEncoder object to encode a user's password before saving the user into the database
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		// Create a new User
		User user = User.builder().build();

		// Set all the User's Properties, after all Form Input Data passed the Constraint Validators
		user.setUsername(registerUser.getUserName());
		user.setPassword(passwordEncoder.encode(registerUser.getPassword()));
		user.setFirstName(registerUser.getFirstName());
		user.setLastName(registerUser.getLastName());
		user.setEmail(registerUser.getEmail());

		// Convert the Roles from List<String> to List<Role>
		user.setRoles(userRoles(roles));

		// Save the User along with its Roles to DB
		userRepository.save(user);
	}

	@Override
	public void saveUser(User user) {
		userRepository.save(user);
	}

	// Helper method: Converts List<String> to List<Role>
	private List<Role> userRoles(List<String> roles) {
		List<Role> userRoles = new ArrayList<>();
		for (String role : roles) {
			userRoles.add(roleRepository.findRoleByName(role));
		}
		return userRoles;
	}

	@Override
	public User retrieveAuthenticatedPrincipalByUsername() {
		// Obtain the authentication request token of authenticated User (or authenticated principal - User's identity)
		Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();

		// Get the User's identity (name or username)
		String authenticatedUsername = loggedInUser.getName();

		// Find and retrieve the registered User from DB using the username obtained from authenticated principal
		return userRepository.findByUserName(authenticatedUsername);
	}
}