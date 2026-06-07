package com.api.springcore.mapper;

import com.api.springcore.dto.SessionResponse;
import com.api.springcore.entity.Session;
import com.api.springcore.entity.SessionQr;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(target = "id",           expression = "java(session.getId())")
    @Mapping(target = "eventId",      expression = "java(session.getEvent().getId())")
    @Mapping(target = "eventTitle",   expression = "java(session.getEvent().getTitle())")
    @Mapping(target = "title",        expression = "java(session.getTitle())")
    @Mapping(target = "room",         expression = "java(session.getRoom())")
    @Mapping(target = "startTime",    expression = "java(session.getStartTime())")
    @Mapping(target = "endTime",      expression = "java(session.getEndTime())")
    @Mapping(target = "capacity",     expression = "java(session.getCapacity())")
    @Mapping(target = "checkInCount", expression = "java(checkInCount)")
    @Mapping(target = "createdAt",    expression = "java(session.getCreatedAt())")
    SessionResponse.Simple toSimpleDto(Session session, long checkInCount);

    @Mapping(target = "id",             expression = "java(session.getId())")
    @Mapping(target = "eventId",        expression = "java(session.getEvent().getId())")
    @Mapping(target = "eventTitle",     expression = "java(session.getEvent().getTitle())")
    @Mapping(target = "title",          expression = "java(session.getTitle())")
    @Mapping(target = "room",           expression = "java(session.getRoom())")
    @Mapping(target = "startTime",      expression = "java(session.getStartTime())")
    @Mapping(target = "endTime",        expression = "java(session.getEndTime())")
    @Mapping(target = "capacity",       expression = "java(session.getCapacity())")
    @Mapping(target = "checkInCount",   expression = "java(checkInCount)")
    @Mapping(target = "sessionQrToken", expression = "java(sessionQr != null ? sessionQr.getToken() : null)")
    @Mapping(target = "qrExpiresAt",    expression = "java(sessionQr != null ? sessionQr.getExpiresAt() : null)")
    @Mapping(target = "createdAt",      expression = "java(session.getCreatedAt())")
    @Mapping(target = "updatedAt",      expression = "java(session.getUpdatedAt())")
    SessionResponse.Detail toDetailDto(Session session, long checkInCount, SessionQr sessionQr);
}