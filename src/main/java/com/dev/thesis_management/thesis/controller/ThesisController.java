package com.dev.thesis_management.thesis.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.file_asset.dto.FolderRequest;
import com.dev.thesis_management.file_asset.dto.FolderResponse;
import com.dev.thesis_management.thesis.dto.submission.SubmissionResponse;
import com.dev.thesis_management.thesis.dto.thesis.ThesisRequest;
import com.dev.thesis_management.thesis.dto.thesis.ThesisResponse;
import com.dev.thesis_management.thesis.dto.thesis.ThesisSearchForm;
import com.dev.thesis_management.thesis.service.ThesisService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/theses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThesisController {

    ThesisService thesisService;

    @GetMapping("/current/search")
    public ResponseEntity<ApiResponse<Page<ThesisResponse>>> searchTheses(
            @ModelAttribute ThesisSearchForm form,
            Pageable pageable,
            Authentication authentication
    ){
        return ok(thesisService.searchCurrentThesis(
                form,
                pageable,
                parseUUID(authentication.getName())
        ));
    }

    @GetMapping("/student/current")
    public ResponseEntity<ApiResponse<List<ThesisResponse>>> getCurrentStudentTheses(
            Authentication authentication
    ){
        return ok(thesisService.getCurrentStudentTheses(parseUUID(authentication.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ThesisResponse>> getThesisById(
            @PathVariable UUID id
    ){
        return ok(thesisService.getThesisById(id));
    }

    @GetMapping("/{id}/draft")
    public ResponseEntity<ApiResponse<FolderResponse>> getThesisDraft(
            @PathVariable UUID id
    ){
        return ok(thesisService.getThesisDraft(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ThesisResponse>> updateThesis(
            @PathVariable UUID id,
            @RequestBody ThesisRequest request,
            Authentication authentication
    ){
        return ok(thesisService.updateThesis(
                id,
                request,
                parseUUID(authentication.getName())
        ));
    }

    /* FILES */

    @PostMapping(value = "/{id}/files/{folderId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> uploadFile(
            @PathVariable UUID id,
            @PathVariable UUID folderId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ){
        thesisService.addFileToThesis(id, folderId, file, parseUUID(authentication.getName()));
        return noContent();
    }

    @DeleteMapping("/{id}/files/{folderId}/{fileId}")
    public ResponseEntity<ApiResponse> deleteFile(
            @PathVariable UUID id,
            @PathVariable UUID folderId,
            @PathVariable UUID fileId,
            Authentication authentication
    ){
        thesisService.removeFileFromThesis(id, folderId, fileId, parseUUID(authentication.getName()));
        return noContent();
    }

    @PostMapping("/{id}/folders/{parentId}")
    public ResponseEntity<ApiResponse> createFolder(
            @PathVariable UUID id,
            @PathVariable UUID parentId,
            @RequestBody FolderRequest request,
            Authentication authentication
    ){
        thesisService.addFolderToThesis(id, parentId, request.name(), parseUUID(authentication.getName()));
        return noContent();
    }

    @DeleteMapping("/{id}/folders/{folderId}")
    public ResponseEntity<ApiResponse> deleteFolder(
            @PathVariable UUID id,
            @PathVariable UUID folderId,
            Authentication authentication
    ){
        thesisService.removeFolderFormThesis(id, folderId, parseUUID(authentication.getName()));
        return noContent();
    }

    @PostMapping(value = "/{id}/submissions", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitThesis(
            @PathVariable UUID id,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication
    ){
        return ok(thesisService.submitThesis(id, files, parseUUID(authentication.getName())));
    }
}
