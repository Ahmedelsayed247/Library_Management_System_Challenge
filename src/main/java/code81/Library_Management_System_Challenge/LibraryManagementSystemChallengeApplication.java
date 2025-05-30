package code81.Library_Management_System_Challenge;

import code81.Library_Management_System_Challenge.domain.model.Role;
import code81.Library_Management_System_Challenge.domain.model.User;
import code81.Library_Management_System_Challenge.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class LibraryManagementSystemChallengeApplication implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(LibraryManagementSystemChallengeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		createDefaultUsers();
	}

	private void createDefaultUsers() {
		// Check if users already exist
		if (userRepository.count() == 0) {
			System.out.println("Creating default users...");

			// Create Admin user
			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode("password123"));
			admin.setEmail("admin@library.com");
			admin.setFirstName("System");
			admin.setLastName("Administrator");
			admin.setRole(Role.ADMINISTRATOR);
			admin.setActive(true);
			userRepository.save(admin);
			System.out.println("Created admin user: admin/password123");

			// Create Librarian user
			User librarian = new User();
			librarian.setUsername("librarian1");
			librarian.setPassword(passwordEncoder.encode("password123"));
			librarian.setEmail("librarian@library.com");
			librarian.setFirstName("John");
			librarian.setLastName("Librarian");
			librarian.setRole(Role.LIBRARIAN);
			librarian.setActive(true);
			userRepository.save(librarian);
			System.out.println("Created librarian user: librarian1/password123");

			// Create Staff user
			User staff = new User();
			staff.setUsername("staff1");
			staff.setPassword(passwordEncoder.encode("password123"));
			staff.setEmail("staff@library.com");
			staff.setFirstName("Jane");
			staff.setLastName("Staff");
			staff.setRole(Role.STAFF);
			staff.setActive(true);
			userRepository.save(staff);
			System.out.println("Created staff user: staff1/password123");

			System.out.println("Default users created successfully!");
		} else {
			System.out.println("Users already exist, skipping default user creation.");
		}
	}
}
