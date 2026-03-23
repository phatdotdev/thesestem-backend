package com.dev.thesis_management.thesis.service;

import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.file_asset.dto.FolderResponse;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.entity.Folder;
import com.dev.thesis_management.file_asset.mapper.FileMapper;
import com.dev.thesis_management.file_asset.repository.FolderRepository;
import com.dev.thesis_management.file_asset.service.FileService;
import com.dev.thesis_management.file_asset.service.FolderService;
import com.dev.thesis_management.specifications.ThesisSpecification;
import com.dev.thesis_management.thesis.dto.SemesterResponse;
import com.dev.thesis_management.thesis.dto.submission.SubmissionResponse;
import com.dev.thesis_management.thesis.dto.thesis.ThesisRequest;
import com.dev.thesis_management.thesis.dto.thesis.ThesisResponse;
import com.dev.thesis_management.thesis.dto.thesis.ThesisSearchForm;

import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.thesis.entity.ThesisSubmission;
import com.dev.thesis_management.thesis.mapper.SubmissionMapper;
import com.dev.thesis_management.thesis.mapper.ThesisMapper;
import com.dev.thesis_management.thesis.repository.SubmissionRepository;
import com.dev.thesis_management.thesis.repository.ThesisRepository;
import com.dev.thesis_management.user.entity.Student;
import com.dev.thesis_management.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThesisService {

    ThesisRepository thesisRepository;
    UserRepository userRepository;
    SubmissionRepository submissionRepository;

    FolderService folderService;
    FileService fileStorageService;

    FolderRepository folderRepository;
    SemesterService semesterService;


    public Page<ThesisResponse> searchCurrentThesis(
            ThesisSearchForm form,
            Pageable pageable,
            UUID userId
    ){

        SemesterResponse semester = semesterService.getCurrentSemester(userId);
        return thesisRepository.findAll(
                ThesisSpecification.search(form, semester.getId()),
                pageable
        ).map(ThesisMapper::toThesisResponseWithMentor);
    }

    public List<ThesisResponse> getCurrentStudentTheses(UUID userId){
        Student student = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"))
                .getStudent();
        SemesterResponse semester = semesterService.getCurrentSemester(userId);
        return thesisRepository.findAllByTopic_Group_Semester_IdAndStudent(semester.getId(), student)
                .stream()
                .map(ThesisMapper::toThesisResponseWithMentor)
                .toList();
    }

    public ThesisResponse getThesisById(UUID thesisId){
        return ThesisMapper.toThesisResponseWithMentor(thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found")));
    }

    public FolderResponse getThesisDraft(UUID thesisId){
        return FileMapper.toFolderResponse(thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found")).getFolder());
    }

    // Update thesis
    @Transactional
    public ThesisResponse updateThesis(
            UUID thesisId,
            ThesisRequest request,
            UUID userId
    ){

        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        checkThesisPermission(thesis, userId);

        thesis.setTitle(request.title());
        thesis.setTitleEn(request.titleEn());
        thesis.setDescription(request.description());
        thesis.setDescriptionEn(request.descriptionEn());
        thesis.setStatus(request.status());
        thesis.setProgressPercent(request.progressPercent());

        thesisRepository.save(thesis);

        return ThesisMapper.toThesisResponse(thesis);
    }

    public void rejectThesis(UUID thesisId, UUID userId){
        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));
        checkThesisPermission(thesis, userId);
        if(!thesis.getStatus().equals(Thesis.Status.PROPOSAL)){
            throw new BadRequestException("Can not delete thesis");
        }
        thesisRepository.delete(thesis);
    }

    /* FILES */
    @Transactional
    public ThesisResponse addFileToThesis(UUID thesisId, UUID folderId, MultipartFile file, UUID userId){

        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        checkThesisPermission(thesis, userId);

        if(isFolderInThesis(thesis, folderId)){
            throw new BadRequestException("Folder not belong to thesis");
        }

        FileAsset fileAsset = fileStorageService.upload(file, userId);

        folderService.addFile(folderId, fileAsset);

        return ThesisMapper.toThesisResponse(thesis);
    }

    @Transactional
    public ThesisResponse removeFileFromThesis(UUID thesisId, UUID folderId, UUID fileId, UUID userId){

        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        checkThesisPermission(thesis, userId);

        if(isFolderInThesis(thesis, folderId)){
            throw new BadRequestException("Folder not belong to thesis");
        }

        fileStorageService.delete(fileId, userId);

        return ThesisMapper.toThesisResponse(thesis);
    }

    @Transactional
    public ThesisResponse addFolderToThesis(UUID thesisId, UUID folderId, String name, UUID userId){

        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        checkThesisPermission(thesis, userId);

        if(isFolderInThesis(thesis, folderId)){
            throw new BadRequestException("Folder not belong to thesis");
        }

        folderService.createSubFolder(folderId, name);

        return ThesisMapper.toThesisResponse(thesis);
    }

    @Transactional
    public ThesisResponse removeFolderFormThesis(UUID thesisId, UUID removeFolderId, UUID userId){

        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        checkThesisPermission(thesis, userId);

        if(isFolderInThesis(thesis, removeFolderId)){
            throw new BadRequestException("Folder not belong to thesis");
        }

        folderService.deleteFolder(removeFolderId);

        return ThesisMapper.toThesisResponse(thesis);
    }

    /* HELPER METHODS */

    private boolean isFolderInThesis(Thesis thesis, UUID folderId){

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BadRequestException("Folder not found"));

        Folder root = thesis.getFolder();

        while(folder != null){
            if(folder.getId().equals(root.getId())){
                return false;
            }
            folder = folder.getParent();
        }

        return true;
    }

    private void checkThesisPermission(Thesis thesis, UUID userId){

        boolean isStudent = thesis.getStudent().getUser().getId().equals(userId);

        boolean isMentor = thesis.getTopic()
                .getGroup()
                .getMentor()
                .getUser()
                .getId()
                .equals(userId);

        if(!isStudent && !isMentor){
            throw new BadRequestException("Permission denied");
        }
    }

    @Transactional
    public SubmissionResponse submitThesis(UUID id, List<MultipartFile> files, UUID userId) {

        Thesis thesis = thesisRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        Integer lastVersion = submissionRepository
                .findMaxVersionByThesisId(id)
                .orElse(0);

        int newVersion = lastVersion + 1;

        ThesisSubmission submission = ThesisSubmission.builder()
                .thesis(thesis)
                .version(newVersion)
                .submittedAt(LocalDateTime.now())
                .build();

        List<FileAsset> fileAssets = new ArrayList<>();

        for (MultipartFile file : files) {

            FileAsset asset = fileStorageService.upload(
                    file,
                    userId
            );

            asset.setSubmission(submission);

            fileAssets.add(asset);
        }

        submission.setFiles(fileAssets);

        thesis.setStatus(Thesis.Status.SUBMITTED);

        submissionRepository.save(submission);

        return SubmissionMapper.toResponse(submission);
    }
}
