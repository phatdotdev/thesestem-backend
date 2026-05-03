package com.dev.thesis_management.thesis.service;

import com.dev.thesis_management.communication.entity.Notification;
import com.dev.thesis_management.communication.service.NotificationService;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.service.FileService;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.service.OrgService;
import com.dev.thesis_management.specifications.ThesisDefenseSpecification;
import com.dev.thesis_management.thesis.dto.SemesterResponse;
import com.dev.thesis_management.thesis.dto.defense.*;
import com.dev.thesis_management.thesis.entity.*;

import com.dev.thesis_management.thesis.mapper.DefenseMapper;
import com.dev.thesis_management.thesis.repository.*;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefenseService {

    ThesisDefenseRepository defenseRepository;
    ThesisRepository thesisRepository;
    CouncilMemberRepository councilMemberRepository;
    DefenseScoreRepository defenseScoreRepository;
    CouncilRepository councilRepository;
    UserRepository userRepository;
    FileService fileService;
    NotificationService notificationService;

    SemesterService semesterService;
    OrgService orgService;

    public List<DefenseResponse> listDefense(
                DefenseSearchForm form, UUID userId
    ){
        Organization organization = orgService.findByUserId(userId);
        return defenseRepository.findAll(
                ThesisDefenseSpecification.search(form, form != null ? form.semesterId() : null, organization.getId()))
                .stream()
                .map(DefenseMapper::toDefenseResponse)
                .toList();
    }

    public Page<DefenseResponse> searchDefense(
            DefenseSearchForm form, Pageable pageable, UUID userId
    ){
        Organization organization = orgService.findByUserId(userId);
        return defenseRepository.findAll(
                ThesisDefenseSpecification.search(form, form != null ? form.semesterId() : null, organization.getId()),
                pageable
        ).map(DefenseMapper::toDefenseResponse);
    }


    public List<DefenseResponse> listCurrentDefense(DefenseSearchForm form, UUID userId) {
        Organization organization = orgService.findByUserId(userId);
        SemesterResponse semester = semesterService.getCurrentSemester(userId);
        return defenseRepository.findAll(
                ThesisDefenseSpecification.search(form, semester.getId(), organization.getId()))
                .stream()
                .map(DefenseMapper::toDefenseResponse)
                .toList();
    }

    public List<DefenseResponse> listDefenseBySemester(UUID id, DefenseSearchForm form, UUID userId) {
        Organization organization = orgService.findByUserId(userId);
        return defenseRepository.findAll(
                        ThesisDefenseSpecification.search(form, id, organization.getId()))
                .stream()
                .map(DefenseMapper::toDefenseResponse)
                .toList();
    }

    public Page<DefenseResponse> searchDefenseBySemester(UUID id, DefenseSearchForm form, Pageable pageable, UUID userId) {
        Organization organization = orgService.findByUserId(userId);
        return defenseRepository.findAll(
                ThesisDefenseSpecification.search(form, id, organization.getId()),
                pageable
        ).map(DefenseMapper::toDefenseResponse);
    }

    public Page<DefenseResponse> searchCurrentDefense(DefenseSearchForm form, Pageable pageable, UUID userId){
        Organization organization = orgService.findByUserId(userId);
        SemesterResponse semester = semesterService.getCurrentSemester(userId);
        return defenseRepository.findAll(
                ThesisDefenseSpecification.search(form, semester.getId(), organization.getId()),
                pageable
        ).map(DefenseMapper::toDefenseResponse);
    }

    public List<DefenseResponse> getByCouncilId(UUID id, UUID userId) {
        return defenseRepository.findAllByCouncilId(id).stream()
                .map(DefenseMapper::toDefenseResponse)
                .toList();
    }

    public DefenseResponse getByThesisId(UUID id, UUID userId) {
        return DefenseMapper.toDefenseResponse(defenseRepository.findAllByThesisId(id)
                .orElseThrow(() -> new BadRequestException("Defense not found")));
    }

    public DefenseResponse getDefenseById(UUID id) {
        return DefenseMapper.toDefenseResponse(
                defenseRepository.findById(id)
                        .orElseThrow(() -> new BadRequestException("Defense not found"))
        );
    }

    public DefenseResponse getDefenseByIdForMentor(UUID defenseId, UUID userId) {
        // 1. Lấy lecturer từ userId
        Lecturer lecturer = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"))
                .getLecturer();

        if (lecturer == null) {
            throw new BadRequestException("User is not a lecturer");
        }

        // 2. Lấy defense
        ThesisDefense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new BadRequestException("Defense not found"));

        // 3. Map response
        DefenseResponse response = DefenseMapper.toDefenseResponse(defense);

        // 4. Tìm member tương ứng (safe)
        var member = response.getCouncil().getMembers().stream()
                .filter(m -> m.getLecturer().getId().equals(lecturer.getId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("You are not in this council"));

        // 5. Set member hiện tại
        response.setMember(member);

        return response;
    }

    @Transactional
    public DefenseResponse assignCouncil(
            DefenseRequest request,
            UUID userId
    ){
        Organization org = orgService.findByUserId(userId);
        Semester semester = semesterService.getCurrentSemester(org);
        Thesis thesis = thesisRepository.findById(request.thesisId())
                .orElseThrow(() -> new BadRequestException("Thesis not found"));

        Council council = councilRepository.findByIdAndSemester(request.councilId(), semester)
                .orElseThrow(() -> new BadRequestException("Semester not found"));

        if(defenseRepository.existsByThesisId(request.thesisId())){
            throw new BadRequestException("Defense already assigned");
        }

        ThesisDefense defense = ThesisDefense.builder()
                .thesis(thesis)
                .council(council)
                .defenseTime(request.defenseTime())
                .location(request.location())
                .build();

        ThesisDefense savedDefense = defenseRepository.save(defense);

//        notifyCouncilAssigned(savedDefense, userId);

        return DefenseMapper.toDefenseResponse(savedDefense);
    }

    public DefenseResponse updateDefense(UUID id, DefenseRequest request, UUID userId) {
        Organization org = orgService.findByUserId(userId);
        Semester semester = semesterService.getCurrentSemester(org);

        ThesisDefense defense = defenseRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Defense not found"));

        if(!defense.getThesis().getTopic().getGroup().getSemester().equals(semester)){
            throw new BadRequestException("Defense not found");
        }

        defense.setDefenseTime(request.defenseTime());
        defense.setLocation(request.location());

        return DefenseMapper.toDefenseResponse(defenseRepository.save(defense));
    }

    @Transactional
    public void removeDefense(UUID defenseId){

        ThesisDefense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new BadRequestException("Defense not found"));

        defenseRepository.delete(defense);
    }

    @Transactional
    public DefenseResponse uploadMinutesFile(UUID defenseId, MultipartFile file, UUID userId) {
        ThesisDefense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new BadRequestException("Defense not found"));

        Lecturer lecturer = getLecturerByUserId(userId);
        ensureCouncilMember(defense, lecturer.getId());

        FileAsset previousMinutesFile = defense.getMinutesFile();
        FileAsset uploadedFile = fileService.upload(file, userId);

        defense.setMinutesFile(uploadedFile);
        ThesisDefense savedDefense = defenseRepository.save(defense);

        if (previousMinutesFile != null) {
            fileService.deleteById(previousMinutesFile.getId());
        }

        return DefenseMapper.toDefenseResponse(savedDefense);
    }

    @Transactional
    public DefenseResponse deleteMinutesFile(UUID defenseId, UUID userId) {
        ThesisDefense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new BadRequestException("Defense not found"));

        Lecturer lecturer = getLecturerByUserId(userId);
        ensureCouncilMember(defense, lecturer.getId());

        FileAsset minutesFile = defense.getMinutesFile();
        if (minutesFile == null) {
            throw new BadRequestException("Minutes file not found");
        }

        defense.setMinutesFile(null);
        ThesisDefense savedDefense = defenseRepository.save(defense);
        fileService.deleteById(minutesFile.getId());

        return DefenseMapper.toDefenseResponse(savedDefense);
    }


    @Transactional
    public DefenseScoreResponse scoreThesis(
            UUID defenseId,
            UUID councilMemberId,
            ScoreRequest request,
            UUID userId
    ) {

        ThesisDefense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new BadRequestException("Defense not found"));

        CouncilMember member = councilMemberRepository.findById(councilMemberId)
                .orElseThrow(() -> new BadRequestException("Council member not found"));

        if (!member.getLecturer().getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission");
        }

        double scoreValue = request.score();
        if (scoreValue < 0 || scoreValue > 10) {
            throw new BadRequestException("Score must be between 0 and 10");
        }

        DefenseScore defenseScore = defenseScoreRepository
                .findByDefenseIdAndCouncilMemberId(defenseId, councilMemberId)
                .orElseGet(() -> DefenseScore.builder()
                        .defense(defense)
                        .councilMember(member)
                        .build()
                );

        defenseScore.setScore(scoreValue);
        defenseScore.setComment(request.comment());

        DefenseScore saved = defenseScoreRepository.save(defenseScore);

        Thesis thesis = defense.getThesis();
        if (thesis.getStatus() != Thesis.Status.GRADED) {
            thesis.setStatus(Thesis.Status.GRADED);
            thesisRepository.save(thesis);
        }

        // notifyScoreUpdated(defense, member.getLecturer().getUser(), userId);

        return DefenseMapper.toDefenseScoreResponse(saved);
    }

    private void notifyCouncilAssigned(ThesisDefense defense, UUID actorId) {
        Thesis thesis = defense.getThesis();

        notifyIfOtherUser(
                thesis.getStudent().getUser(),
                actorId,
                "Phân công hội đồng",
                "Luận văn \"" + thesis.getTitle() + "\" đã được phân công hội đồng bảo vệ.",
                Notification.Type.THESIS_ASSIGNED_COUNCIL
        );

        notifyIfOtherUser(
                thesis.getTopic().getGroup().getMentor().getUser(),
                actorId,
                "Phân công hội đồng",
                "Luận văn \"" + thesis.getTitle() + "\" đã được phân công hội đồng bảo vệ.",
                Notification.Type.THESIS_ASSIGNED_COUNCIL
        );

        defense.getCouncil().getMembers().forEach(member -> notifyIfOtherUser(
                member.getLecturer().getUser(),
                actorId,
                "Hội đồng mới",
                "Bạn được phân công chấm bảo vệ cho luận văn \"" + thesis.getTitle() + "\".",
                Notification.Type.COUNCIL_ASSIGNED
        ));
    }

    private void notifyScoreUpdated(ThesisDefense defense, User scorer, UUID actorId) {
        Thesis thesis = defense.getThesis();
        String scorerName = scorer.getLecturer() != null && scorer.getLecturer().getFullName() != null
                ? scorer.getLecturer().getFullName()
                : scorer.getUsername();

        String content = "Luận văn \"" + thesis.getTitle() + "\" vừa nhận điểm từ thành viên hội đồng " + scorerName + ".";

        notifyIfOtherUser(
                thesis.getStudent().getUser(),
                actorId,
                "Cập nhật chấm điểm",
                content,
                Notification.Type.COUNCIL_GRADE_UPDATED
        );

        notifyIfOtherUser(
                thesis.getTopic().getGroup().getMentor().getUser(),
                actorId,
                "Cập nhật chấm điểm",
                content,
                Notification.Type.COUNCIL_GRADE_UPDATED
        );
    }

    private void notifyIfOtherUser(User recipient, UUID actorId, String title, String content, Notification.Type type) {
        if (recipient != null && !recipient.getId().equals(actorId)) {
            notificationService.notifyUser(recipient, title, content, type);
        }
    }

    private Lecturer getLecturerByUserId(UUID userId) {
        Lecturer lecturer = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"))
                .getLecturer();

        if (lecturer == null) {
            throw new BadRequestException("User is not a lecturer");
        }

        return lecturer;
    }

    private void ensureCouncilMember(ThesisDefense defense, UUID lecturerId) {
        boolean isCouncilMember = councilMemberRepository
                .existsByCouncilIdAndLecturerId(defense.getCouncil().getId(), lecturerId);

        if (!isCouncilMember) {
            throw new BadRequestException("You are not in this council");
        }
    }
}