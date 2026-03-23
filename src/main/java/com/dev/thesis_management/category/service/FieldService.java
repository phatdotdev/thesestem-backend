package com.dev.thesis_management.category.service;

import com.dev.thesis_management.category.entity.Field;
import com.dev.thesis_management.category.repository.FieldRepository;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.service.OrgService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FieldService {
    FieldRepository fieldRepository;
    OrgService orgService;

    public List<Field> getFields(UUID userId){
        Organization organization = orgService.findByUserId(userId);
        return fieldRepository.findAllByOrganizationId(organization.getId());
    }

    public Field createField(Field request, UUID userId){
        Organization organization = orgService.findByUserId(userId);
        return fieldRepository.save(Field.builder()
                        .code(request.getCode())
                        .name(request.getName())
                        .organization(organization)
                        .description(request.getDescription())
                .build());
    }

    public Field updateField(UUID id, Field request, UUID userId){
        Organization organization = orgService.findByUserId(userId);
        Field field = fieldRepository.findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new BadRequestException("Field not found"));
        field.setName(request.getName());
        field.setCode(request.getCode());
        field.setDescription(request.getDescription());
        return fieldRepository.save(field);
    }

    public void deleteField(UUID id, UUID userId){
        Organization organization = orgService.findByUserId(userId);
        Field field = fieldRepository.findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new BadRequestException("Field not found"));
        fieldRepository.delete(field);
    }
}
