package studentknowledge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import studentknowledge.dto.*;
import studentknowledge.exception.BizException;
import studentknowledge.model.User;
import studentknowledge.model.enums.Role;
import studentknowledge.repository.UserRepository;
import studentknowledge.security.JwtUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest req) {
        User user = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new BizException("用户名或密码错误"));
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return LoginResponse.builder()
                .token(token).userId(user.getId())
                .username(user.getUsername()).email(user.getEmail())
                .role(user.getRole().name()).build();
    }

    public void register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            throw new BizException("用户名已存在");
        }
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new BizException("邮箱已被注册");
        }
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .role(Role.STUDENT).build();
        userRepo.save(user);
    }

    public List<User> findAll() { return userRepo.findAll(); }

    public void resetPassword(Long userId, String newPwd) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));
        user.setPassword(encoder.encode(newPwd));
        userRepo.save(user);
    }

    public void deleteUser(Long userId) {
        userRepo.deleteById(userId);
    }
}
