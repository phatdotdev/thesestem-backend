package com.dev.thesis_management.category.service;

import com.dev.thesis_management.category.dto.AddSemesterRequest;
import com.dev.thesis_management.category.entity.AcademicYear;
import com.dev.thesis_management.category.repository.AcademicYearRepository;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.UnauthenticatedException;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.service.OrgService;
import com.dev.thesis_management.thesis.dto.SemesterResponse;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.thesis.mapper.SemesterMapper;
import com.dev.thesis_management.thesis.repository.SemesterRepository;
import com.dev.thesis_management.thesis.service.SemesterService;
import com.dev.thesis_management.user.repository.OrganizationRepository;
import com.dev.thesis_management.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.thesis.mapper.SemesterMapper.createSemesterToSemester;
import static com.dev.thesis_management.thesis.mapper.SemesterMapper.semesterToResponse;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AcademicService {

    UserRepository userRepository;
    AcademicYearRepository academicYearRepository;
    SemesterRepository semesterRepository;
    OrganizationRepository organizationRepository;

    public AcademicYear addAcademicYear(AcademicYear request, UUID userId){
        Organization organization = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthenticatedException("Invalid user"))
                .getOrganization();
        if(!organization.getManager().getId().equals(userId)){
            throw new UnauthorizedException("You do not have permission");
        }
        AcademicYear academicYear = AcademicYear.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(AcademicYear.Status.UPCOMING)
                .organization(organization)
                .build();
        return academicYearRepository.save(academicYear);
    }

    public AcademicYear updateAcademicYear(UUID academicYearId, AcademicYear request, UUID userId) {
        Organization organization = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthenticatedException("Invalid user"))
                .getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new BadRequestException("AcademicYear not found"));

        academicYear.setName(request.getName());
        academicYear.setStartDate(request.getStartDate());
        academicYear.setEndDate(request.getEndDate());
        // academicYear.setStatus(request.getStatus());

        return academicYearRepository.save(academicYear);
    }

    public void deleteAcademicYear(UUID academicYearId, UUID userId) {
        Organization organization = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthenticatedException("Invalid user"))
                .getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new BadRequestException("AcademicYear not found"));

        academicYearRepository.delete(academicYear);
    }

    public List<AcademicYear> getAllAcademicYears(UUID userId) {
        Organization organization = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthenticatedException("Invalid user"))
                .getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        return academicYearRepository.findByOrganizationId(organization.getId());
    }

    public SemesterResponse addSemester(UUID academicYearId, AddSemesterRequest request, UUID userId) {

//        if (request.startDate().isAfter(request.endDate())) {
//            throw new BadRequestException("Start date must be before end date");
//        }

        Organization organization = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthenticatedException("Invalid user"))
                .getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        AcademicYear year = academicYearRepository
                .findByIdAndOrganization(academicYearId, organization)
                .orElseThrow(() -> new BadRequestException("Academic year not found"));

//        if (request.startDate().isBefore(year.getStartDate())
//                || request.endDate().isAfter(year.getEndDate())) {
//            throw new BadRequestException("Semester must be within academic year");
//        }

        Semester semester = Semester.builder()
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(Semester.Status.UPCOMING)
                .organization(organization)
                .academicYear(year)
                .build();

        return SemesterMapper.semesterToResponse(
                semesterRepository.save(semester)
        );
    }


    public List<SemesterResponse> getSemesters(UUID id, UUID userId) {
        Organization organization = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthenticatedException("Invalid user"))
                .getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }
        AcademicYear year = academicYearRepository.findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new BadRequestException("Year not found"));
        return semesterRepository.findAllByOrganizationAndAcademicYear(organization, year)
                .stream().map(SemesterMapper::semesterToResponse).toList();
    }
}
