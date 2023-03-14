package com.game.service;

import com.game.dto.PlayerDto;
import com.game.dto.RequestDto;
import com.game.entity.Player;
import com.game.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class PlayerService {
    private final Repository repository;
    private final Validator validator;
    private final Mapper mapper;
    private RequestDto requestDto;

    public Mapper getMapper() {
        return mapper;
    }

    private Specification<Player> specification = new Specification<Player>() {
        @Override
        public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (requestDto.getName() != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + requestDto.getName().toLowerCase() + "%"));
            }
            if (requestDto.getTitle() != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + requestDto.getTitle().toLowerCase() + "%"));
            }
            if (requestDto.getRace() != null) {
                predicates.add(criteriaBuilder.equal(root.get("race"), requestDto.getRace()));
            }
            if (requestDto.getProfession() != null) {
                predicates.add(criteriaBuilder.equal(root.get("profession"), requestDto.getProfession()));
            }
            if (requestDto.getMinExperience() < requestDto.getMaxExperience()) {
                predicates.add(criteriaBuilder.between(root.get("experience"), requestDto.getMinExperience(), requestDto.getMaxExperience()));
            } else if (requestDto.getMinExperience() != 0 && requestDto.getMaxExperience() == 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), requestDto.getMinExperience()));
            } else if (requestDto.getMinExperience() == 0 && requestDto.getMaxExperience() != 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("experience"), requestDto.getMaxExperience()));
            }

            Date before = new Date(requestDto.getBefore());
            Date after = new Date(requestDto.getAfter());
            if (requestDto.getAfter() != 0 && requestDto.getBefore() != 0 && requestDto.getAfter() < requestDto.getBefore()) {
                predicates.add(criteriaBuilder.between(root.get("birthday"), after, before));
            } else if (requestDto.getAfter() != 0 && requestDto.getBefore() == 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), after));
            } else if (requestDto.getAfter() == 0 && requestDto.getBefore() != 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), before));
            }

            if (requestDto.getBanned() != null) {
                predicates.add(criteriaBuilder.equal(root.get("banned"), requestDto.getBanned()));
            }

            if (requestDto.getMinLevel() != 0 && requestDto.getMaxLevel() != 0 && requestDto.getMinLevel() < requestDto.getMaxLevel()) {
                predicates.add(criteriaBuilder.between(root.get("level"), requestDto.getMinLevel(), requestDto.getMaxLevel()));
            } else if (requestDto.getMinLevel() != 0 && requestDto.getMaxLevel() == 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("level"), requestDto.getMinLevel()));
            } else if (requestDto.getMinLevel() == 0 && requestDto.getMaxLevel() != 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("level"), requestDto.getMaxLevel()));
            }

            orderBy(requestDto, root, criteriaBuilder, query);

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }
    };

    @Autowired
    public PlayerService(Repository repository, Validator validator, Mapper mapper) {
        this.repository = repository;
        this.validator = validator;
        this.mapper = mapper;
    }

    private void setFilterDto(RequestDto requestDto) {
        this.requestDto = requestDto;
    }

    // Get players count
    public int playersCount(RequestDto requestDto) {
        setFilterDto(requestDto);
        if (requestDto == null) {
            return Math.toIntExact(repository.count());
        }
        return Math.toIntExact(repository.count(specification));
    }

    // Get player
    public Player findById(Long id) {
        return repository.findById(id).get();
    }

    // Create player
    public Player createPlayer(PlayerDto playerDto) {
        Player player = new Player();
        return repository.save(mapper.mapDtoToEntity(player, playerDto));
    }


    // Get players page
    public Page<Player> getListPlayers(RequestDto requestDto) {
        setFilterDto(requestDto);

        Pageable pageable = PageRequest.of(requestDto.getPageNumber(), requestDto.getPageSize());

        Page<Player> page = repository.findAll(specification, pageable);

        return page;
    }

    // Delete player
    public void deletePlayer(Long id) {
        repository.deleteById(id);
    }

    // Update player
    public Player updatePlayer(Long playerId, PlayerDto playerDto) {
        Player player = findById(playerId);

        if (playerDto.getName() != null && validator.isNameLengthValid(playerDto)) {
            player.setName(playerDto.getName());
        }
        if (playerDto.getTitle() != null && validator.isTitleLengthValid(playerDto)) {
            player.setTitle(playerDto.getTitle());
        }
        if (playerDto.getRace() != null) {
            player.setRace(playerDto.getRace());
        }
        if (playerDto.getProfession() != null) {
            player.setProfession(playerDto.getProfession());
        }

        if (playerDto.getBirthday() != null) {
            validator.validateBirthday(playerDto);
            player.setBirthday(new Date(playerDto.getBirthday()));
        }

        if (playerDto.getBanned() != null) {
            player.setBanned(playerDto.getBanned());
        }

        if (playerDto.getExperience() != null) {
            validator.validateExperience(playerDto);
            player.setExperience(playerDto.getExperience());

            int level = mapper.levelEvaluation(playerDto);
            player.setLevel(level);

            int untilNextLevel = mapper.untilNextLevelEvaluation(playerDto, level);
            player.setUntilNextLevel(untilNextLevel);
        }
        return repository.saveAndFlush(player);
    }



    private void orderBy(RequestDto requestDto, Root<Player> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query) {
        Order order = null;
        switch (requestDto.getOrder()) {
            case ID:
                order = criteriaBuilder.asc(root.get("id"));
                break;
            case NAME:
                order = criteriaBuilder.asc(root.get("name"));
                break;
            case EXPERIENCE:
                order = criteriaBuilder.asc(root.get("experience"));
                break;
            case BIRTHDAY:
                order = criteriaBuilder.asc(root.get("birthday"));
                break;
            case LEVEL:
                order = criteriaBuilder.asc(root.get("level"));
                break;
        }
        query.orderBy(order);
    }

}
