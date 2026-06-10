package studentknowledge.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import studentknowledge.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) {
        String encoded = passwordEncoder.encode("123456");
        userRepository.findAll().forEach(user -> {
            if (!passwordEncoder.matches("123456", user.getPassword())) {
                user.setPassword(encoded);
                userRepository.save(user);
            }
        });
    }
}
