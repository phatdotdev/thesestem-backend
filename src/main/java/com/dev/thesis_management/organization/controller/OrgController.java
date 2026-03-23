package com.dev.thesis_management.organization.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.organization.dto.StructureResponse;
import com.dev.thesis_management.organization.dto.UpdateOrgRequest;
import com.dev.thesis_management.organization.service.OrgService;
import com.dev.thesis_management.organization.dto.OrgResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/orgs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrgController {

    OrgService orgService;

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<OrgResponse>> getCurrentOrgInfo(
            Authentication authentication
            ){
        return ok(orgService.getCurrentOrg(parseUUID(authentication.getName())));
    }

    @GetMapping("/search/{code}")
    public ResponseEntity<ApiResponse<OrgResponse>> getOrgByCode(@PathVariable String code){
        return ok(orgService.getOrgByCode(code));
    }

    @GetMapping("/mine/structure")
    public ResponseEntity<ApiResponse<StructureResponse>> getOrgStructure(Authentication authentication){
        return ok(orgService.getCurrentOrgStructure(parseUUID(authentication.getName())));
    }

    @PutMapping("/mine/info")
    public ResponseEntity<ApiResponse<OrgResponse>> updateOrgInfo(
            Authentication authentication,
            @RequestBody  UpdateOrgRequest request
    ){
        return ok(orgService.updateOrgInfo(parseUUID(authentication.getName()), request));
    }

    @PutMapping("/mine/media")
    public ResponseEntity<ApiResponse<OrgResponse>> updateOrgMedia(
            Authentication authentication,
            MultipartFile logo,
            MultipartFile cover,
            MultipartFile banner
    ){
        return ok(orgService.updateOrgMedia(parseUUID(authentication.getName()), logo, cover, banner));
    }
}
