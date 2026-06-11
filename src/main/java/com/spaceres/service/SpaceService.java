package com.spaceres.service;

import com.spaceres.dto.request.SpaceRequest;
import com.spaceres.dto.response.SpaceResponse;
import com.spaceres.entity.Space;
import com.spaceres.exception.BusinessException;
import com.spaceres.exception.ErrorCode;
import com.spaceres.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;

    // ── 공간 목록 (사용 가능한 것만) ─────────────────────
    @Transactional(readOnly = true)
    public List<SpaceResponse> getAvailableSpaces() {
        return spaceRepository.findByStatusOrderByNameAsc(Space.SpaceStatus.AVAILABLE)
                .stream()
                .map(SpaceResponse::from)
                .toList();
    }

    // ── 전체 공간 목록 (관리자용) ─────────────────────────
    @Transactional(readOnly = true)
    public List<SpaceResponse> getAllSpaces() {
        return spaceRepository.findAll()
                .stream()
                .map(SpaceResponse::from)
                .toList();
    }

    // ── 공간 상세 조회 ────────────────────────────────────
    @Transactional(readOnly = true)
    public SpaceResponse getSpace(Long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPACE_NOT_FOUND));
        return SpaceResponse.from(space);
    }

    // ── 공간 생성 (관리자) ────────────────────────────────
    @Transactional
    public SpaceResponse createSpace(SpaceRequest request) {
        Space space = Space.builder()
                .name(request.getName())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .facilities(request.getFacilities())
                .building(request.getBuilding())
                .spaceType(request.getSpaceType())
                .status(Space.SpaceStatus.AVAILABLE)
                .build();

        Space saved = spaceRepository.save(space);
        log.info("공간 생성: {}", saved.getName());
        return SpaceResponse.from(saved);
    }

    // ── 공간 수정 (관리자) ────────────────────────────────
    @Transactional
    public SpaceResponse updateSpace(Long id, SpaceRequest request) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPACE_NOT_FOUND));

        space.setName(request.getName());
        space.setDescription(request.getDescription());
        space.setCapacity(request.getCapacity());
        space.setLocation(request.getLocation());
        space.setFacilities(request.getFacilities());
        space.setBuilding(request.getBuilding());
        space.setSpaceType(request.getSpaceType());

        return SpaceResponse.from(space);
    }

    // ── 공간 상태 변경 (관리자) ───────────────────────────
    @Transactional
    public SpaceResponse updateStatus(Long id, Space.SpaceStatus status) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPACE_NOT_FOUND));
        space.setStatus(status);
        log.info("공간 상태 변경: {} → {}", space.getName(), status);
        return SpaceResponse.from(space);
    }
}
