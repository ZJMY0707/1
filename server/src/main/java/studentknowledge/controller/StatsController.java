package studentknowledge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import studentknowledge.dto.*;
import studentknowledge.service.StatsService;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService service;

    @GetMapping("/my")
    public ApiResponse<StatsDTO> myStats(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(service.userStats(userId));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StatsDTO> adminStats() {
        return ApiResponse.ok(service.adminStats());
    }
}
