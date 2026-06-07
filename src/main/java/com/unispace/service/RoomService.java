package com.unispace.service;

import com.unispace.domain.reservation.Reservation;
import com.unispace.domain.reservation.ReservationRepository;
import com.unispace.domain.room.Facility;
import com.unispace.domain.room.FacilityRepository;
import com.unispace.domain.room.Room;
import com.unispace.domain.room.RoomRepository;
import com.unispace.dto.request.FacilityRequest;
import com.unispace.dto.request.RoomRequest;
import com.unispace.dto.response.*;
import com.unispace.exception.BusinessException;
import com.unispace.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final FacilityRepository facilityRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository,
                       FacilityRepository facilityRepository,
                       ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.facilityRepository = facilityRepository;
        this.reservationRepository = reservationRepository;
    }

    // ----- 사용자: 공간 조회 -----
    @Transactional(readOnly = true)
    public List<RoomResponse> getActiveRooms() {
        return roomRepository.findByActiveTrue().stream().map(RoomResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long roomId) {
        return RoomResponse.from(findRoom(roomId));
    }

    /** 공간의 시설 현황 + 지정 기간의 점유 시간대 (대시보드/현황) */
    @Transactional(readOnly = true)
    public RoomAvailabilityResponse getAvailability(Long roomId, LocalDateTime from, LocalDateTime to) {
        Room room = findRoom(roomId);
        if (from == null) from = LocalDateTime.now();
        if (to == null) to = from.plusDays(7);

        List<TimeSlotResponse> slots = reservationRepository
                .findActiveInRange(roomId, from, to).stream()
                .map(this::toSlot)
                .toList();

        List<FacilityResponse> fac = room.getFacilities().stream()
                .map(FacilityResponse::from)
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();

        return new RoomAvailabilityResponse(room.getId(), room.getName(), room.isActive(), fac, slots);
    }

    // ----- Admin: 공간 / 시설 관리 -----
    @Transactional
    public RoomResponse createRoom(RoomRequest req) {
        Room room = Room.builder()
                .name(req.name())
                .location(req.location())
                .capacity(req.capacity())
                .description(req.description())
                .active(req.active() == null || req.active())
                .facilities(resolveFacilities(req.facilityIds()))
                .build();
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse updateRoom(Long roomId, RoomRequest req) {
        Room room = findRoom(roomId);
        if (req.name() != null) room.setName(req.name());
        if (req.location() != null) room.setLocation(req.location());
        if (req.capacity() != null) room.setCapacity(req.capacity());
        if (req.description() != null) room.setDescription(req.description());
        if (req.active() != null) room.setActive(req.active());
        if (req.facilityIds() != null) room.setFacilities(resolveFacilities(req.facilityIds()));
        return RoomResponse.from(room);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = findRoom(roomId);
        roomRepository.delete(room);
    }

    @Transactional
    public FacilityResponse createFacility(FacilityRequest req) {
        Facility facility = facilityRepository.save(
                Facility.builder().name(req.name()).build());
        return FacilityResponse.from(facility);
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> getAllFacilities() {
        return facilityRepository.findAll().stream().map(FacilityResponse::from).toList();
    }

    // ----- helpers -----
    private Room findRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    private Set<Facility> resolveFacilities(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        Set<Facility> result = new HashSet<>();
        for (Long id : ids) {
            result.add(facilityRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FACILITY_NOT_FOUND)));
        }
        return result;
    }

    private TimeSlotResponse toSlot(Reservation r) {
        return new TimeSlotResponse(r.getId(), r.getStartTime(), r.getEndTime(), r.getUser().getName());
    }
}
