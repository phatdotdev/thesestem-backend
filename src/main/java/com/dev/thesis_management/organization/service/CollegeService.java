package com.dev.thesis_management.organization.service;

import com.dev.thesis_management.organization.dto.AddCollegeRequest;
import com.dev.thesis_management.organization.dto.AddFacultyRequest;
import com.dev.thesis_management.organization.dto.CollegeResponse;
import com.dev.thesis_management.organization.dto.UpdateCollegeRequest;
import com.dev.thesis_management.organization.entity.College;
import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.mapper.CollegeMapper;
import com.dev.thesis_management.organization.repository.CollegeRepository;
import com.dev.thesis_management.organization.repository.FacultyRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.organization.mapper.CollegeMapper.*;
import static com.dev.thesis_management.organization.mapper.FacultyMapper.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CollegeService {
    CollegeRepository collegeRepository;
    FacultyRepository facultyRepository;

    OrgService orgService;

    public List<CollegeResponse> getAllColleges(UUID userId){
        return orgService.findByUserId(userId)
                .getColleges()
                .stream().map(CollegeMapper::collegeToResponse)
                .toList();
    }

    public CollegeResponse addCollege(AddCollegeRequest request, UUID userId){
        College college = addCollegeRequestToCollege(request);
        Organization org = orgService.findByUserId(userId);
        college.setOrganization(org);
        return collegeToResponse(collegeRepository.save(college));
    }

    public CollegeResponse updateCollege(UUID collegeId, UpdateCollegeRequest request, UUID userId) {
        College college = findCollegeById(collegeId);
        if(!college.getOrganization().getManager().getId().equals(userId)){
            throw new RuntimeException();
        }
        updateCollegeFromRequest(college, request);
        return collegeToResponse(collegeRepository.save(college));
    }

    @Transactional
    public CollegeResponse addFaculty(UUID collegeId, AddFacultyRequest request, UUID userId) throws AccessDeniedException {
        Organization org = orgService.findByUserId(userId);

        College college = findCollegeById(collegeId);

        UUID managerId = college.getOrganization().getManager().getId();
        if (!managerId.equals(userId)) {
            throw new AccessDeniedException("You are not allowed to add faculty to this college");
        }

        Faculty faculty = addFacultyRequestToFaculty(request);
        faculty.setCollege(college);
        faculty.setOrganization(org);

        facultyRepository.save(faculty);
        college.getFaculties().add(faculty);

        return collegeToResponse(college);
    }

    public void deleteCollege(UUID collegeId, UUID userId){
        College college = findCollegeById(collegeId);
        if(!college.getOrganization().getManager().getId().equals(userId)){
            throw new RuntimeException();
        }
        collegeRepository.delete(college);
    }


    public College findCollegeById(UUID id){
        return  collegeRepository.findById(id).orElseThrow();
    }
}
