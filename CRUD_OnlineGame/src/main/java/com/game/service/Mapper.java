package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.dto.PlayerDto;
import com.game.dto.RequestDto;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class Mapper {
    public RequestDto mapRequestParamToRequestDto(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel,
            PlayerOrder order,
            Integer pageSize,
            Integer pageNumber
    ) {
        RequestDto requestDto = new RequestDto();
        requestDto.setName(name);
        requestDto.setTitle(title);
        requestDto.setRace(race);
        requestDto.setProfession(profession);
        requestDto.setAfter(after);
        requestDto.setBefore(before);
        requestDto.setBanned(banned);
        requestDto.setMinExperience(minExperience);
        requestDto.setMaxExperience(maxExperience);
        requestDto.setMinLevel(minLevel);
        requestDto.setMaxLevel(maxLevel);
        requestDto.setOrder(order);
        requestDto.setPageSize(pageSize);
        requestDto.setPageNumber(pageNumber);
        return requestDto;
    }

    Player mapDtoToEntity(Player player, PlayerDto playerDto) {
        player.setName(playerDto.getName());
        player.setTitle(playerDto.getTitle());
        player.setRace(playerDto.getRace());
        player.setProfession(playerDto.getProfession());
        player.setBirthday(new Date(playerDto.getBirthday()));
        player.setBanned(playerDto.getBanned());
        player.setExperience(playerDto.getExperience());

        int level = levelEvaluation(playerDto);
        player.setLevel(level);
        player.setUntilNextLevel(untilNextLevelEvaluation(playerDto, level));

        return player;
    }

    Integer levelEvaluation(PlayerDto playerDto) {
        int exp = playerDto.getExperience();
        return (((int) (Math.sqrt(2500 + 200 * exp)) - 50) / 100);
    }

    Integer untilNextLevelEvaluation(PlayerDto playerDto, Integer lvl) {
        int exp = playerDto.getExperience();
        return (50 * (lvl + 1) * (lvl + 2) - exp);
    }

}
