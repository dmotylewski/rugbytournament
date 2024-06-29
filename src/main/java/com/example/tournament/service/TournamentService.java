package com.example.tournament.service;

import com.example.tournament.model.Match;
import com.example.tournament.model.Team;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TournamentService {
    private List<Team> teams = new ArrayList<>();
    private List<Match> schedule = new ArrayList<>();
    private List<List<Team>> groups = new ArrayList<>();
    private List<Match> finals = new ArrayList<>();

    public boolean addTeam(String name) {
        if (teams.stream().anyMatch(t -> t.getName().equalsIgnoreCase(name))) {
            return false; // Duplicate name found
        }
        teams.add(new Team(name));
        return true;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void generateGroups(int numberOfGroups) {
        Collections.shuffle(teams);
        groups.clear();
        for (int i = 0; i < numberOfGroups; i++) {
            groups.add(new ArrayList<>());
        }
        for (int i = 0; i < teams.size(); i++) {
            if (groups.get(i % numberOfGroups).size() < 4) {
                groups.get(i % numberOfGroups).add(teams.get(i));
            }
        }
    }

    public List<List<Team>> getGroups() {
        return groups;
    }

    public void generateSchedule() {
        schedule.clear();
        for (List<Team> group : groups) {
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    schedule.add(new Match(group.get(i), group.get(j)));
                }
            }
        }
    }

    public void generateFinals() {
        finals.clear();
        List<Team> finalists = new ArrayList<>();
        for (List<Team> group : groups) {
            List<Team> sortedGroup = group.stream()
                    .sorted(Comparator.comparingInt(Team::getPoints).reversed())
                    .collect(Collectors.toList());
            if (sortedGroup.size() > 1) {
                finalists.add(sortedGroup.get(0));
                finalists.add(sortedGroup.get(1));
            }
        }
        for (int i = 0; i < finalists.size(); i += 2) {
            if (i + 1 < finalists.size()) {
                finals.add(new Match(finalists.get(i), finalists.get(i + 1)));
            }
        }
    }

    public List<Match> getSchedule() {
        return schedule;
    }

    public List<Match> getFinals() {
        return finals;
    }

    public void updateScore(int matchIndex, int score1, int score2) {
        Match match = schedule.get(matchIndex);
        match.setScore1(score1);
        match.setScore2(score2);
        if (score1 > score2) {
            match.getTeam1().addPoints(3);
        } else if (score1 < score2) {
            match.getTeam2().addPoints(3);
        } else {
            match.getTeam1().addPoints(1);
            match.getTeam2().addPoints(1);
        }
    }

    public void updateFinalScore(int matchIndex, int score1, int score2) {
        Match match = finals.get(matchIndex);
        match.setScore1(score1);
        match.setScore2(score2);
        if (score1 > score2) {
            match.getTeam1().addPoints(3);
        } else if (score1 < score2) {
            match.getTeam2().addPoints(3);
        } else {
            match.getTeam1().addPoints(1);
            match.getTeam2().addPoints(1);
        }
    }

    public List<Team> getUniqueTeamsFromSchedule() {
        return schedule.stream()
                .flatMap(match -> java.util.stream.Stream.of(match.getTeam1(), match.getTeam2()))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Team> getUniqueTeamsFromFinals() {
        return finals.stream()
                .flatMap(match -> java.util.stream.Stream.of(match.getTeam1(), match.getTeam2()))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Team> getFinalResults() {
        List<Team> allTeams = new ArrayList<>(teams);
        allTeams.sort(Comparator.comparingInt(Team::getPoints).reversed());
        return allTeams;
    }
}
