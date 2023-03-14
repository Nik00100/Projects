package com.game.service;

import com.game.dto.PlayerDto;
import com.game.exception.HTTPException;
import com.game.exception.PlayerNotFoundException;
import com.game.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class Validator {
    private final Repository repository;

    @Autowired
    public Validator(Repository repository) {
        this.repository = repository;
    }

    // validator for playerController (check id)
    public void validateId(Long id) {
        if (id <= 0) {
            throw new HTTPException();
        } else if (id > repository.count() || !repository.findById(id).isPresent()) {
            throw new PlayerNotFoundException();
        }
    }

    // validator for playerController (check name, title, experience, birthday)
    public void validatePlayerForCreate(PlayerDto playerDto) {
        if (!isPlayerDtoValidForCreate(playerDto)) {
            throw new HTTPException();
        }
    }

    // check that all playerDTO valid
    private boolean isPlayerDtoValidForCreate(PlayerDto playerDto) {
        if (isNullInDtoParamsForCreate(playerDto)) {
            return false;
        }
        return (isNameLengthValid(playerDto) && isTitleLengthValid(playerDto) && isExperienceValid(playerDto) && isBirthdayValid(playerDto));
    }



    // check that all params in playerDTO not null
    private boolean isNullInDtoParamsForCreate(PlayerDto playerDto) {
        boolean isNullInDtoParams =
                playerDto.getName() == null ||
                        playerDto.getTitle() == null ||
                        playerDto.getRace() == null ||
                        playerDto.getProfession() == null ||
                        playerDto.getBirthday() == null ||
                        playerDto.getExperience() == null;

        return isNullInDtoParams;
    }

    // check 0 < length of name < 12
    boolean isNameLengthValid(PlayerDto playerDto) {
        int nameLength = playerDto.getName().trim().length();
        return nameLength > 0 && nameLength <= 12;
    }

    // check 0 < length of title < 30
    boolean isTitleLengthValid(PlayerDto playerDto) {
        int titleLength = playerDto.getTitle().trim().length();
        return titleLength > 0 && titleLength <= 30;
    }

    // check 0 < experience <1000000
    boolean isExperienceValid(PlayerDto playerDto) {
        return playerDto.getExperience() >= 0 && playerDto.getExperience() <= 10000000;
    }

    // check 2000 < year < 3000
    boolean isBirthdayValid(PlayerDto playerDto) {
        if (playerDto.getBirthday() < 0) {
            return false;
        }
        Date date = new Date(playerDto.getBirthday());
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();

        return date.getTime() >= 0 && (year >= 2000 && year <= 3000);
    }

    void validateBirthday(PlayerDto playerDto) {
        if (!isBirthdayValid(playerDto)) {
            throw new HTTPException();
        }
    }

    void validateExperience(PlayerDto playerDto) {
        if (!isExperienceValid(playerDto)) {
            throw new HTTPException();
        }
    }
}
