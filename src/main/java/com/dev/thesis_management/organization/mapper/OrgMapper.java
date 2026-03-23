package com.dev.thesis_management.organization.mapper;

import com.dev.thesis_management.organization.dto.OrgResponse;
import com.dev.thesis_management.organization.dto.UpdateOrgRequest;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.enums.OrganizationType;

public class OrgMapper {

    public static OrgResponse toOrgInfoResponse(Organization organization){
        return OrgResponse.builder()
                .id(organization.getId())
                .code(organization.getCode())
                .name(organization.getName())
                .type(organization.getType().name())
                .email(organization.getEmail())
                .website(organization.getWebsite())
                .phone(organization.getPhone())
                .description(organization.getDescription())
                .address(organization.getAddress())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .logoUrl(
                        organization.getLogo() != null
                                ? organization.getLogo().getUrl()
                                : null
                )
                .coverUrl(
                        organization.getCover() != null
                                ? organization.getCover().getUrl()
                                : null
                )
                .bannerUrl(
                        organization.getBanner() != null
                                ? organization.getBanner().getUrl()
                                : null
                )
                .managerEmail(organization.getManager().getUsername())
                .build();
    }

    public static void updateOrgFromRequest(Organization organization, UpdateOrgRequest request){
        organization.setAddress(request.getAddress());
        organization.setCode(request.getCode());
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setWebsite(request.getWebsite());
        try {
            OrganizationType type = OrganizationType.valueOf(request.getType());
            organization.setType(type);
        } catch (Exception e) {
            // current type
        }
    }
}
