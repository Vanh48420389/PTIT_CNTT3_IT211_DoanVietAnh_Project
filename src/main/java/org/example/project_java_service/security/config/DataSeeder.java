package org.example.project_java_service.security.config;
import org.example.project_java_service.model.entity.User;
import org.example.project_java_service.model.entity.enumeration.RoleEnum;
import org.example.project_java_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456"))
                    .email("admin@gmail.com")
                    .role(RoleEnum.ROLE_ADMIN)
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            System.out.println(" Đã khởi tạo tài khoản Admin mặc định thành công (Username: admin / Password: 123456)");
        }
    }
}