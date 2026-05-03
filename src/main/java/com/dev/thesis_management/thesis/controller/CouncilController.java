package com.dev.thesis_management.thesis.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.thesis.dto.council.CouncilRequest;
import com.dev.thesis_management.thesis.dto.council.CouncilResponse;
import com.dev.thesis_management.thesis.dto.council.CouncilSearchForm;
import com.dev.thesis_management.thesis.entity.CouncilRole;
import com.dev.thesis_management.thesis.service.CouncilService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.parseUUID;

@RestController
@RequestMapping("/councils")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouncilController {

    CouncilService councilService;

    /* =========================================================
                        COUNCILS - MANAGER
       ========================================================= */

    @GetMapping("/current/search")
    public ResponseEntity<ApiResponse<Page<CouncilResponse>>> searchCouncils(
            CouncilSearchForm form,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        return ok(
                councilService.searchCouncils(
                        form,
                        pageable,
                        userId
                )
        );
    }

    @GetMapping("/{id}/search")
    public ResponseEntity<ApiResponse<Page<CouncilResponse>>> searchCouncilsBySemester(
            CouncilSearchForm form,
            @PathVariable UUID id,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        return ok(
                councilService.searchCouncilsBySemester(
                        form,
                        id,
                        pageable,
                        userId
                )
        );
    }

    @GetMapping("/current/list")
    public ResponseEntity<ApiResponse<List<CouncilResponse>>> listCouncils(
            CouncilSearchForm form,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        return ok(
                councilService.listCouncils(
                        form,
                        userId
                )
        );
    }

    @PostMapping("/current")
    public ResponseEntity<ApiResponse<CouncilResponse>> createCouncil(
            @RequestBody CouncilRequest request,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        return created(
                councilService.createCouncil(
                        request,
                        userId
                )
        );
    }

    @PutMapping("/current/{id}")
    public ResponseEntity<ApiResponse<CouncilResponse>> updateCouncil(
            @PathVariable UUID id,
            @RequestBody CouncilRequest request,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        return ok(
                councilService.updateCouncil(
                        id,
                        request,
                        userId
                )
        );
    }

    @DeleteMapping("/current/{id}")
    public ResponseEntity<ApiResponse> deleteCouncil(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        councilService.deleteCouncil(id, userId);

        return noContent();
    }

    /* =========================================================
                        COUNCILS - MEMBER
       ========================================================= */

    @GetMapping("/member/current")
    public ResponseEntity<ApiResponse<List<CouncilResponse>>> listMemberCouncils(
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        return ok(
                councilService.getCurrentMemberCouncils(
                        userId
                )
        );
    }

    @GetMapping("/member/semester/{id}")
    public ResponseEntity<ApiResponse<List<CouncilResponse>>> getCouncilsBySemester(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        return ok(
                councilService.getMemberCouncilsBySemester(
                        id,
                        userId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CouncilResponse>> getCouncilById(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        return ok(
                councilService.getCouncilById(
                        id,
                        userId
                )
        );
    }

    /* =========================================================
                        COUNCIL ROLES
       ========================================================= */

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<CouncilRole>>> getCouncilRoles(
            Authentication authentication
    ) {

        UUID userId = getUserId(authentication);

        return ok(
                councilService.getCouncilRolesByUser(
                        userId
                )
        );
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<CouncilRole>> createCouncilRole(
            Authentication authentication,
            @RequestBody CouncilRole request
    ) {

        UUID userId = getUserId(authentication);

        return created(
                councilService.addCouncilRole(
                        request,
                        userId
                )
        );
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<CouncilRole>> updateCouncilRole(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody CouncilRole request
    ) {

        UUID userId = getUserId(authentication);

        return ok(
                councilService.updateCouncilRole(
                        id,
                        request,
                        userId
                )
        );
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<ApiResponse> deleteCouncilRole(
            Authentication authentication,
            @PathVariable UUID id
    ) {

        UUID userId = getUserId(authentication);

        councilService.deleteCouncilRole(id, userId);

        return noContent();
    }

    /* =========================================================
                        HELPER
       ========================================================= */

    private UUID getUserId(Authentication authentication) {
        return parseUUID(authentication.getName());
    }
}