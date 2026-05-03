package com.dev.thesis_management.thesis.service;

import com.dev.thesis_management.communication.entity.Notification;
import com.dev.thesis_management.communication.service.NotificationService;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.file_asset.dto.FolderResponse;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.entity.Folder;
import com.dev.thesis_management.file_asset.mapper.FileMapper;
import com.dev.thesis_management.file_asset.repository.FolderRepository;
import com.dev.thesis_management.file_asset.service.FileService;
import com.dev.thesis_management.file_asset.service.FolderService;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.specifications.ThesisSpecification;
import com.dev.thesis_management.thesis.dto.SemesterResponse;
import com.dev.thesis_management.thesis.dto.submission.SubmissionResponse;
import com.dev.thesis_management.thesis.dto.thesis.ThesisRequest;
import com.dev.thesis_management.thesis.dto.thesis.ThesisResponse;
import com.dev.thesis_management.thesis.dto.thesis.ThesisSearchForm;

import com.dev.thesis_management.thesis.entity.Group;
import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.thesis.entity.ThesisSubmission;
import com.dev.thesis_management.thesis.mapper.SubmissionMapper;
import com.dev.thesis_management.thesis.mapper.ThesisMapper;
import com.dev.thesis_management.thesis.repository.GroupRepository;
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
    GroupRepository groupRepository;

    FolderService folderService;
    FileService fileStorageService;

    FolderRepository folderRepository;
    SemesterService semesterService;
    NotificationService notificationService;

    public Page<ThesisResponse> searchTheses(ThesisSearchForm form, Pageable pageable) {
        return thesisRepository.findAll(
                ThesisSpecification.search(form),
                pageable
        ).map(ThesisMapper::toThesisResponseWithMentorAndOrganization);
    }

    public Page<ThesisResponse> searchThesesForManager(ThesisSearchForm form, Pageable pageable, UUID userId) {
        Organization organization = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"))
                .getOrganization();
        return thesisRepository.findAll(
                ThesisSpecification.searchForManager(form, organization.getId()),
                pageable
        ).map(ThesisMapper::toThesisResponseWithMentorAndOrganization);
    }

    public Page<ThesisResponse> searchPublicTheses(ThesisSearchForm form, Pageable pageable) {
        return thesisRepository.findAll(
                ThesisSpecification.search(form, Thesis.AccessLevel.PUBLIC),
                pageable
        ).map(ThesisMapper::toThesisResponseWithMentorAndOrganization);
    }

    public ThesisResponse getPublicThesisById(UUID id) {
        Thesis thesis = thesisRepository.findByIdAndAccessLevel(id, Thesis.AccessLevel.PUBLIC)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        return ThesisMapper.toThesisResponseWithMentorAndOrganizationAndSubmissions(thesis);
    }

    public Page<ThesisResponse> searchCurrentTheses(
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

    public List<ThesisResponse> getCurrentTheses(ThesisSearchForm form, UUID userId) {
        SemesterResponse semester = semesterService.getCurrentSemester(userId);
        Organization organization = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"))
                .getOrganization();
        return thesisRepository.findAll(ThesisSpecification.search(form, semester.getId()))
                .stream()
                .map(ThesisMapper::toThesisResponseWithMentor)
                .toList();
    }

    public Page<ThesisResponse> searchThesisBySemester(
            ThesisSearchForm form,
            Pageable pageable,
            UUID semesterId
    ) {
        return thesisRepository.findAll(
                ThesisSpecification.search(form, semesterId),
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

    public List<ThesisResponse> getStudentThesesBySemester(UUID userId, UUID semesterId) {
        Student student = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"))
                .getStudent();
        return thesisRepository.findAllByTopic_Group_Semester_IdAndStudent(semesterId, student)
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

        Thesis.Status previousStatus = thesis.getStatus();

        thesis.setTitle(request.title());
        thesis.setTitleEn(request.titleEn());
        thesis.setDescription(request.description());
        thesis.setDescriptionEn(request.descriptionEn());
        thesis.setStatus(request.status());
        thesis.setProgressPercent(request.progressPercent());

        thesisRepository.save(thesis);

        if (!userId.equals(thesis.getStudent().getUser().getId())) {
            notificationService.notifyUser(
                    thesis.getStudent().getUser(),
                    "Cập nhật luận văn",
                    "Luận văn \"" + thesis.getTitle() + "\" đã được cập nhật bởi giảng viên hướng dẫn.",
                    Notification.Type.THESIS_UPDATED
            );
        }

        if (!userId.equals(thesis.getTopic().getGroup().getMentor().getUser().getId())) {
            notificationService.notifyUser(
                    thesis.getTopic().getGroup().getMentor().getUser(),
                    "Cập nhật luận văn",
                    "Sinh viên đã cập nhật luận văn \"" + thesis.getTitle() + "\".",
                    Notification.Type.STUDENT_UPDATE_THESIS
            );
        }

        if (previousStatus != request.status()) {
            notifyThesisStatusChanged(thesis, userId, previousStatus, request.status());
        }

        return ThesisMapper.toThesisResponse(thesis);
    }

    public void confirmThesis(UUID thesisId, UUID userId){
        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));
        checkThesisPermission(thesis, userId);
        if(!thesis.getStatus().equals(Thesis.Status.PROPOSAL)){
            throw new BadRequestException("Can not confirm thesis");
        }
        thesis.setStatus(Thesis.Status.IN_PROGRESS);
        thesisRepository.save(thesis);

        notifyThesisStatusChanged(thesis, userId, Thesis.Status.PROPOSAL, Thesis.Status.IN_PROGRESS);
    }

    public void approveThesis(UUID thesisId, UUID userId){
        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));
        checkThesisPermission(thesis, userId);
        if(!thesis.getStatus().equals(Thesis.Status.IN_PROGRESS)){
            throw new BadRequestException("Can not approve thesis");
        }
        thesis.setStatus(Thesis.Status.APPROVED);
        thesisRepository.save(thesis);

        notifyThesisStatusChanged(thesis, userId, Thesis.Status.IN_PROGRESS, Thesis.Status.APPROVED);
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

    public void changeAccessLevel(UUID id, String accessLevel, UUID userId) {

        Thesis thesis = thesisRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        if (!thesis.getTopic().getGroup().getSemester().getOrganization().getManager().getId().equals(userId)) {
            throw new BadRequestException("Permission denied");
        }

        try {
            Thesis.AccessLevel level = Thesis.AccessLevel.valueOf(accessLevel.toUpperCase());
            thesis.setAccessLevel(level);
            thesisRepository.save(thesis);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid access level");
        }
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

        if (!userId.equals(thesis.getTopic().getGroup().getMentor().getUser().getId())) {
            notificationService.notifyUser(
                    thesis.getTopic().getGroup().getMentor().getUser(),
                    "Nộp luận văn mới",
                    "Sinh viên đã nộp bản mới cho luận văn \"" + thesis.getTitle() + "\" (phiên bản " + newVersion + ").",
                    Notification.Type.STUDENT_SUBMIT_THESIS
            );
        }

        return SubmissionMapper.toResponse(submission);
    }

    private void notifyThesisStatusChanged(
            Thesis thesis,
            UUID actorId,
            Thesis.Status oldStatus,
            Thesis.Status newStatus
    ) {
        String content = "Trạng thái luận văn \"" + thesis.getTitle() + "\" đã chuyển từ "
                + oldStatus + " sang " + newStatus + ".";

        if (!actorId.equals(thesis.getStudent().getUser().getId())) {
            notificationService.notifyUser(
                    thesis.getStudent().getUser(),
                    "Trạng thái luận văn thay đổi",
                    content,
                    Notification.Type.THESIS_UPDATED
            );
        }

        if (!actorId.equals(thesis.getTopic().getGroup().getMentor().getUser().getId())) {
            notificationService.notifyUser(
                    thesis.getTopic().getGroup().getMentor().getUser(),
                    "Trạng thái luận văn thay đổi",
                    content,
                    Notification.Type.THESIS_UPDATED
            );
        }
    }

    public List<SubmissionResponse> getSubmissions(UUID id) {

        Thesis thesis = thesisRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        return thesis.getSubmissions() != null
                ? thesis.getSubmissions().stream().map(SubmissionMapper::toResponse).toList()
                : List.of();
    }

    public List<ThesisResponse> getThesisByGroupId(UUID groupId, UUID userId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BadRequestException("Group not found"));

        if(!group.getMentor().getUser().getId().equals(userId)){
            throw new BadRequestException("Permission denied");
        }
        List<Thesis> theses = thesisRepository.findByTopic_Group_Id(groupId);

        return theses
                .stream()
                .map(ThesisMapper::toThesisResponse)
                .toList();
    }

    public ThesisResponse getThesisByIdAndGroupId(UUID thesisId, UUID groupId, UUID userId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BadRequestException("Group not found"));

        if(!group.getMentor().getUser().getId().equals(userId)){
            throw new BadRequestException("Permission denied");
        }

        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        if(!thesis.getTopic().getGroup().getId().equals(groupId)){
            throw new BadRequestException("Thesis not belong to group");
        }

        return ThesisMapper.toThesisResponse(thesis);
    }
}
