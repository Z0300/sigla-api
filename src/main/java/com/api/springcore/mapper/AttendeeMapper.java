package com.api.springcore.mapper;

import com.api.springcore.dto.AttendeeResponse;
import com.api.springcore.entity.Attendee;
import com.api.springcore.entity.QrToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendeeMapper {
    @Mapping(target = "id",           expression = "java(attendee.getId())")
    @Mapping(target = "eventId",      expression = "java(attendee.getEvent().getId())")
    @Mapping(target = "eventTitle",   expression = "java(attendee.getEvent().getTitle())")
    @Mapping(target = "userId",       expression = "java(attendee.getUser().getId())")
    @Mapping(target = "userFullName", expression = "java(attendee.getUser().getFirstName() + ' ' + attendee.getUser().getLastName())")
    @Mapping(target = "userEmail",    expression = "java(attendee.getUser().getEmail())")
    @Mapping(target = "status",       expression = "java(attendee.getStatus())")
    @Mapping(target = "qrToken",      expression = "java(qrToken != null ? qrToken.getToken() : null)")
    @Mapping(target = "registeredAt", expression = "java(attendee.getRegisteredAt())")
    AttendeeResponse.Simple toSimpleDto(Attendee attendee, QrToken qrToken);

    @Mapping(target = "id",           expression = "java(attendee.getId())")
    @Mapping(target = "userId",       expression = "java(attendee.getUser().getId())")
    @Mapping(target = "userFullName", expression = "java(attendee.getUser().getFirstName() + ' ' + attendee.getUser().getLastName())")
    @Mapping(target = "userEmail",    expression = "java(attendee.getUser().getEmail())")
    @Mapping(target = "status",       expression = "java(attendee.getStatus())")
    @Mapping(target = "registeredAt", expression = "java(attendee.getRegisteredAt())")
    AttendeeResponse.Summary toSummaryDto(Attendee attendee);
}
