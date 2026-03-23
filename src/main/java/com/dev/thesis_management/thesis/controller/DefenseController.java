package com.dev.thesis_management.thesis.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.thesis.dto.defense.*;
import com.dev.thesis_management.thesis.service.DefenseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/defenses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefenseController {

    DefenseService defenseService;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<DefenseResponse>>> listDefenses(
            DefenseSearchForm form,
            Authentication authentication
    ){
        return ok(defenseService.listCurrentDefense(form, parseUUID(authentication.getName())));
    }

    @GetMapping("/current/search")
    public ResponseEntity<ApiResponse<Page<DefenseResponse>>> searchDefenses(
            DefenseSearchForm form,
            Pageable pageable,
            Authentication authentication
    ){
        return ok(defenseService.searchCurrentDefense(form, pageable, parseUUID(authentication.getName())));
    }

    @GetMapping("/council/{id}")
    public ResponseEntity<ApiResponse<List<DefenseResponse>>> getDefensesByCouncilId(
            @PathVariable UUID id,
            Authentication authentication
    ){
        return ok(defenseService.getByCouncilId(id, parseUUID(authentication.getName())));
    }

    @GetMapping("/thesis/{id}")
    public ResponseEntity<ApiResponse<DefenseResponse>> getDefensesByThesis(
            @PathVariable UUID id,
            Authentication authentication
    ){
        return ok(defenseService.getByThesisId(id, parseUUID(authentication.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DefenseResponse>> getDefenseById(
            @PathVariable UUID id,
            Authentication authentication
    ){
        return ok(defenseService.getDefenseById(id));
    }

    @GetMapping("/{id}/mentor")
    public ResponseEntity<ApiResponse<DefenseResponse>> getDefensesByIdForMentor(
            @PathVariable UUID id,
            Authentication authentication
    ){
        return ok(defenseService.getDefenseByIdForMentor(id, parseUUID(authentication.getName())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DefenseResponse>> addThesisDefense(
            @RequestBody DefenseRequest request,
            Authentication authentication
            ){
        return ok(defenseService.assignCouncil(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DefenseResponse>> updateThesisDefense(
            @PathVariable UUID id,
            @RequestBody DefenseRequest request,
            Authentication authentication
    ){
        return ok(defenseService.updateDefense(id, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> removeDefense(
            @PathVariable UUID id,
            Authentication authentication
    ){
        defenseService.removeDefense(id);
        return noContent();
    }

    @PostMapping("/{id}/scores/{memberId}")
    public ResponseEntity<ApiResponse<DefenseScoreResponse>> scoreThesis(
            @PathVariable UUID id,
            @RequestBody ScoreRequest request,
            @PathVariable UUID memberId,
            Authentication authentication
    ){
        return ok(defenseService.scoreThesis(id, memberId, request, parseUUID(authentication.getName())));
    }

}
