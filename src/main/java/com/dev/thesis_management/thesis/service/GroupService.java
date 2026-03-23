package com.dev.thesis_management.thesis.service;

import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.file_asset.entity.Folder;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.service.OrgService;
import com.dev.thesis_management.thesis.dto.CreateGroupRequest;
import com.dev.thesis_management.thesis.dto.GroupResponse;
import com.dev.thesis_management.thesis.dto.UpdateGroupRequest;
import com.dev.thesis_management.thesis.dto.group.*;
import com.dev.thesis_management.thesis.dto.thesis.ThesisResponse;
import com.dev.thesis_management.thesis.entity.*;
import com.dev.thesis_management.thesis.mapper.*;
import com.dev.thesis_management.thesis.repository.*;
import com.dev.thesis_management.user.dto.StudentResponse;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.Student;
import com.dev.thesis_management.user.mapper.StudentMapper;
import com.dev.thesis_management.user.repository.StudentRepository;
import com.dev.thesis_management.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.thesis.mapper.GroupMapper.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupService {

    UserRepository userRepository;
    SemesterRepository semesterRepository;
    GroupRepository groupRepository;
    AssignmentRepository assignmentRepository;
    TopicRepository topicRepository;
    StudentRepository studentRepository;
    ThesisRepository thesisRepository;
    MeetingRepository meetingRepository;
    OrgService orgService;

    /* =========================================================
                        GROUP
    ========================================================= */

    public List<GroupResponse> getMentorGroupsBySemester(UUID semesterId, UUID userId) {

        Semester semester = findSemester(semesterId);
        Lecturer mentor = findLecturerByUserId(userId);

        return groupRepository
                .findAllBySemesterAndMentor(semester, mentor)
                .stream()
                .map(GroupMapper::toGroupResponse)
                .toList();
    }

    public List<GroupResponse> getStudentGroupsBySemester(UUID semesterId, UUID userId) {

        Semester semester = findSemester(semesterId);
        Student student = findStudentByUserId(userId);

        return groupRepository
                .findAllBySemesterAndStudentsContains(semester, student)
                .stream()
                .map(GroupMapper::toGroupResponseWithMentor)
                .toList();
    }

    public List<GroupResponse> getCurrentMentorGroups(UUID userId) {

        Organization org = orgService.findByUserId(userId);
        Lecturer mentor = findLecturerByUserId(userId);
        Semester semester = getCurrentSemester(org);

        return groupRepository
                .findAllBySemesterAndMentor(semester, mentor)
                .stream()
                .map(GroupMapper::toGroupResponse)
                .toList();
    }

    public List<GroupResponse> getCurrentStudentGroups(UUID userId) {

        Organization org = orgService.findByUserId(userId);
        Student student = findStudentByUserId(userId);
        Semester semester = getCurrentSemester(org);

        return groupRepository
                .findAllBySemesterAndStudentsContains(semester, student)
                .stream()
                .map(GroupMapper::toGroupResponseWithMentor)
                .toList();
    }

    public GroupResponse getGroupById(UUID groupId) {
        return toGroupResponse(findGroupById(groupId));
    }

    /* =========================================================
                        CREATE GROUP
    ========================================================= */

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, UUID userId) {

        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);
        Lecturer mentor = findLecturerByUserId(userId);

        if (!semester.getMentors().contains(mentor)) {
            throw new UnauthorizedException("You do not have permission");
        }

        Group group = createGroupRequestToGroup(request);

        group.setMentor(mentor);
        group.setSemester(semester);

        semester.getGroups().add(group);

        Folder root = new Folder();
        root.setName("Root");
        root.setGroup(group);

        group.setRootFolder(root);

        semesterRepository.save(semester);

        return toGroupResponse(group);
    }

    /* =========================================================
                        UPDATE GROUP
    ========================================================= */

    @Transactional
    public GroupResponse updateGroup(UUID groupId,
                                     UpdateGroupRequest request,
                                     UUID userId) {

        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);

        Lecturer mentor = findLecturerByUserId(userId);

        if (!semester.getMentors().contains(mentor)) {
            throw new UnauthorizedException("You are not mentor of this semester");
        }

        Group group = semester.getGroups()
                .stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Group not found"));

        if (!group.getMentor().getId().equals(mentor.getId())) {
            throw new UnauthorizedException("You can only update your own group");
        }

        updateGroupFromRequest(group, request);

        groupRepository.save(group);

        return toGroupResponse(group);
    }

    /* =========================================================
                        DELETE GROUP
    ========================================================= */

    @Transactional
    public void deleteGroup(UUID groupId, UUID userId) {

        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);
        Lecturer mentor = findLecturerByUserId(userId);

        if (!semester.getMentors().contains(mentor)) {
            throw new UnauthorizedException("You are not mentor of this semester");
        }

        Group group = semester.getGroups()
                .stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Group not found"));

        if (!group.getMentor().getId().equals(mentor.getId())) {
            throw new UnauthorizedException("You can only delete your own group");
        }

        semester.getGroups().remove(group);
    }

    /* =========================================================
                        ASSIGNMENT
    ========================================================= */

    public List<AssignmentResponse> getAssignmentGroup(UUID groupId) {

        return findGroupById(groupId)
                .getAssignments()
                .stream()
                .map(AssignmentMapper::assignmentToResponse)
                .toList();
    }

    @Transactional
    public AssignmentResponse createAssignment(UUID groupId,
                                               CreateAssignmentRequest request,
                                               UUID userId) {

        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        Assignment assignment = Assignment.builder()
                .name(request.name())
                .description(request.description())
                .deadline(request.deadline())
                .status(Assignment.Status.OPEN)
                .group(group)
                .build();

        assignmentRepository.save(assignment);

        return AssignmentMapper.assignmentToResponse(assignment);
    }

    @Transactional
    public AssignmentResponse updateAssignment(UUID groupId,
                                               UUID assignmentId,
                                               UpdateAssignmentRequest request,
                                               UUID userId) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BadRequestException("Assignment not found"));

        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        if (!group.getAssignments().contains(assignment)) {
            throw new BadRequestException("Group do not contain this assignment");
        }

        assignment.setName(request.name());
        assignment.setDeadline(request.deadline());
        assignment.setDescription(request.description());

        assignmentRepository.save(assignment);

        return AssignmentMapper.assignmentToResponse(assignment);
    }

    @Transactional
    public void deleteAssignment(UUID groupId,
                                 UUID assignmentId,
                                 UUID userId) {

        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        Assignment assignment = assignmentRepository
                .findByIdAndGroupId(assignmentId, groupId)
                .orElseThrow(() ->
                        new BadRequestException("Assignment not found in this group"));

        assignmentRepository.delete(assignment);
    }

    /* =========================================================
                        MEETINGS
    ========================================================= */

    public List<MeetingResponse> getMeetingGroup(UUID groupId) {
        return findGroupById(groupId)
                .getMeetings()
                .stream()
                .map(MeetingMapper::meetingToResponse)
                .toList();
    }

    public MeetingResponse createMeeting(UUID groupId, MeetingRequest request, UUID userId){
        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        Meeting meeting = Meeting.builder()
                .title(request.title())
                .description(request.description())
                .startAt(request.start())
                .endAt(request.end())
                .group(group)
                .build();
        meeting.setUrl("https://meet.jit.si/" + meeting.getId().toString());
       return  MeetingMapper.meetingToResponse(meetingRepository.save(meeting));
    }

    public MeetingResponse updateMeeting(UUID meetingId, MeetingRequest request, UUID userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        checkGroupPermission(meeting.getGroup(), userId);

        meeting.setTitle(request.title());
        meeting.setDescription(request.description());
        meeting.setStartAt(request.start());
        meeting.setEndAt(request.end());

        Meeting updated = meetingRepository.save(meeting);
        return MeetingMapper.meetingToResponse(updated);
    }

    public void deleteMeeting(UUID meetingId, UUID userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        checkGroupPermission(meeting.getGroup(), userId);

        meetingRepository.delete(meeting);
    }


    /* =========================================================
                        STUDENT
    ========================================================= */

    public List<StudentResponse> getStudents(UUID groupId, UUID userId) {

        return findGroupById(groupId)
                .getStudents()
                .stream()
                .map(StudentMapper::studentToResponse)
                .toList();
    }

    @Transactional
    public void addStudentToGroup(UUID studentId,
                                  UUID groupId,
                                  UUID userId) {

        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);

        Student student = studentRepository
                .findByIdAndOrganization(studentId, org)
                .orElseThrow(() -> new BadRequestException("Student not found"));

        if (!semester.getStudents().contains(student)) {
            throw new BadRequestException("Student is not in current semester");
        }

        group.getStudents().add(student);

        groupRepository.save(group);
    }

    public void removeStudentFromGroup(UUID studentId,
                                       UUID groupId,
                                       UUID userId) {

        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        Student student = group.getStudents()
                .stream()
                .filter(std -> std.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Student not in group"));

        group.getStudents().remove(student);

        groupRepository.save(group);
    }

    /* =========================================================
                        TOPIC
    ========================================================= */

    public List<TopicResponse> getGroupTopics(UUID groupId) {

        return findGroupById(groupId)
                .getTopics()
                .stream()
                .map(TopicMapper::topicToResponse)
                .toList();
    }

    public TopicResponse createTopic(UUID groupId,
                                     CreateTopicRequest request,
                                     UUID userId) {

        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        Topic topic = Topic.builder()
                .title(request.title())
                .maxStudents(request.maxStudents())
                .description(request.description())
                .group(group)
                .build();

        return TopicMapper.topicToResponse(
                topicRepository.save(topic)
        );
    }

    public TopicResponse updateTopic(UUID groupId,
                                     UUID topicId,
                                     UpdateTopicRequest request,
                                     UUID userId) {

        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new BadRequestException("Topic not found"));

        if (!topic.getGroup().equals(group)) {
            throw new UnauthorizedException("You do not have permission");
        }

        topic.setTitle(request.title());
        topic.setMaxStudents(request.maxStudents());
        topic.setDescription(request.description());

        return TopicMapper.topicToResponse(
                topicRepository.save(topic)
        );
    }

    public void removeTopic(UUID groupId,
                            UUID topicId,
                            UUID userId) {

        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new BadRequestException("Topic not found"));

        if (!topic.getGroup().equals(group)) {
            throw new UnauthorizedException("You do not have permission");
        }

        topicRepository.delete(topic);
    }

    /* =========================================================
                        ASSIGN STUDENT TO TOPIC
    ========================================================= */

    @Transactional
    public void assignStudentToTopic(UUID groupId,
                                     UUID topicId,
                                     UUID studentId,
                                     UUID userId) {

        Group group = findGroupById(groupId);

        checkGroupPermission(group, userId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new BadRequestException("Topic not found"));

        if (!topic.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Topic does not belong to this group");
        }

        Student student = group.getStudents()
                .stream()
                .filter(std -> std.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Student not in group"));

        if (topic.getTheses()
                .stream()
                .anyMatch(thesis -> thesis.getStudent().getId().equals(studentId))) {

            throw new BadRequestException("Student has been assigned to this topic");
        }

        Folder folder = Folder.builder()
                .name("Thesis_" + student.getId())
                .build();

        Thesis thesis = Thesis.builder()
                .topic(topic)
                .student(student)
                .title(topic.getTitle())
                .titleEn(topic.getTitle())
                .folder(folder)
                .status(Thesis.Status.PROPOSAL)
                .build();

        topic.getTheses().add(thesis);

        topicRepository.save(topic);
    }

    /* =========================================================
                        THESES
    ========================================================= */

    public List<ThesisResponse> getGroupTheses(UUID groupId) {

        return thesisRepository
                .findAllByTopic_Group_Id(groupId)
                .stream()
                .map(ThesisMapper::toThesisResponse)
                .toList();
    }

    /* =========================================================
                        HELPER
    ========================================================= */

    public Semester getCurrentSemester(Organization organization) {

        return semesterRepository
                .findByOrganizationAndStatus(
                        organization,
                        Semester.Status.ACTIVE
                )
                .orElseThrow(() ->
                        new BadRequestException("Semester not found"));
    }

    public Group findGroupById(UUID id) {

        return groupRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException("Group not found"));
    }

    private Semester findSemester(UUID id) {

        return semesterRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException("Semester not found"));
    }

    private Lecturer findLecturerByUserId(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BadRequestException("User not found"))
                .getLecturer();
    }

    private Student findStudentByUserId(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BadRequestException("User not found"))
                .getStudent();
    }

    public void checkGroupPermission(Group group, UUID userId) {

        if (!group.getMentor()
                .getUser()
                .getId()
                .equals(userId)) {

            throw new UnauthorizedException("You do not have permission");
        }
    }
}