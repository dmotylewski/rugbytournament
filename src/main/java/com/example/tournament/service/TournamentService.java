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
    private List<Match> semiFinals = new ArrayList<>();
    private List<Match> thirdPlaceMatch = new ArrayList<>();
    private List<Match> finalMatch = new ArrayList<>();

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

        // Oblicz minimalną liczbę grup, aby każda grupa miała co najmniej 3 drużyny
        int totalTeams = teams.size();
        int minGroups = (int) Math.ceil(totalTeams / 4.0);
        numberOfGroups = Math.max(numberOfGroups, minGroups);

        // Dodaj grupy
        for (int i = 0; i < numberOfGroups; i++) {
            groups.add(new ArrayList<>());
        }

        // Przydziel drużyny do grup
        for (int i = 0; i < totalTeams; i++) {
            groups.get(i % numberOfGroups).add(teams.get(i));
        }

        // Sprawdź, czy któraś grupa ma mniej niż 3 drużyny
        boolean hasSmallGroups;
        do {
            hasSmallGroups = false;
            for (List<Team> group : groups) {
                if (group.size() < 3) {
                    hasSmallGroups = true;
                    redistributeTeams();
                    break;
                }
            }
        } while (hasSmallGroups);
    }

    private void redistributeTeams() {
        List<Team> allTeams = new ArrayList<>();
        for (List<Team> group : groups) {
            allTeams.addAll(group);
        }
        Collections.shuffle(allTeams);

        groups.clear();
        int numberOfGroups = (int) Math.ceil(allTeams.size() / 3.0);

        for (int i = 0; i < numberOfGroups; i++) {
            groups.add(new ArrayList<>());
        }

        for (int i = 0; i < allTeams.size(); i++) {
            groups.get(i % numberOfGroups).add(allTeams.get(i));
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
        semiFinals.clear();
        thirdPlaceMatch.clear();
        finalMatch.clear();

        List<Team> finalists = new ArrayList<>();
        for (List<Team> group : groups) {
            List<Team> sortedGroup = group.stream()
                    .sorted(Comparator.comparingInt(Team::getPoints)
                            .thenComparingInt(Team::getGoals)
                            .reversed())
                    .collect(Collectors.toList());
            if (sortedGroup.size() > 1) {
                finalists.add(sortedGroup.get(0));
                finalists.add(sortedGroup.get(1));
            }
        }

        if (finalists.size() > 4) {
            for (int i = 0; i < finalists.size(); i += 2) {
                if (i + 1 < finalists.size()) {
                    semiFinals.add(new Match(finalists.get(i), finalists.get(i + 1)));
                }
            }
        } else {
            finals.addAll(finalists.stream()
                    .map(team -> new Match(team, team))
                    .collect(Collectors.toList()));
        }
    }

    public void generateFinalMatches() {
        if (semiFinals.size() < 4) {
            throw new IllegalStateException("Not enough teams for final matches");
        }
        List<Team> winners = new ArrayList<>();
        List<Team> losers = new ArrayList<>();
        for (Match match : semiFinals) {
            if (match.getScore1() > match.getScore2()) {
                winners.add(match.getTeam1());
                losers.add(match.getTeam2());
            } else {
                winners.add(match.getTeam2());
                losers.add(match.getTeam1());
            }
        }

        if (winners.size() < 4) {
            throw new IllegalStateException("Not enough winners for final matches");
        }

        winners.sort(Comparator.comparingInt(Team::getPoints)
                .thenComparingInt(Team::getGoals)
                .reversed());

        finalMatch.add(new Match(winners.get(0), winners.get(1))); // 1st and 2nd place match
        thirdPlaceMatch.add(new Match(winners.get(2), winners.get(3))); // 3rd and 4th place match
    }

    public List<Match> getSchedule() {
        return schedule;
    }

    public List<Match> getFinals() {
        return finals;
    }

    public List<Match> getSemiFinals() {
        return semiFinals;
    }

    public List<Match> getThirdPlaceMatch() {
        return thirdPlaceMatch;
    }

    public List<Match> getFinalMatch() {
        return finalMatch;
    }

    public void updateScore(int matchIndex, int score1, int score2) {
        Match match = schedule.get(matchIndex);
        if (match.isFinalized()) {
            return;
        }
        match.setScore1(score1);
        match.setScore2(score2);
        match.setFinalized(true);
        match.getTeam1().addGoals(score1);
        match.getTeam2().addGoals(score2);
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
        if (match.isFinalized()) {
            return;
        }
        match.setScore1(score1);
        match.setScore2(score2);
        match.setFinalized(true);
        match.getTeam1().addGoals(score1);
        match.getTeam2().addGoals(score2);
        if (score1 > score2) {
            match.getTeam1().addPoints(3);
        } else if (score1 < score2) {
            match.getTeam2().addPoints(3);
        } else {
            match.getTeam1().addPoints(1);
            match.getTeam2().addPoints(1);
        }
    }

    public void updateSemiFinalScore(int matchIndex, int score1, int score2) {
        Match match = semiFinals.get(matchIndex);
        if (match.isFinalized()) {
            return;
        }
        match.setScore1(score1);
        match.setScore2(score2);
        match.setFinalized(true);
        match.getTeam1().addGoals(score1);
        match.getTeam2().addGoals(score2);
        if (score1 > score2) {
            match.getTeam1().addPoints(3);
        } else if (score1 < score2) {
            match.getTeam2().addPoints(3);
        } else {
            match.getTeam1().addPoints(1);
            match.getTeam2().addPoints(1);
        }
    }

    public void updateThirdPlaceMatchScore(int matchIndex, int score1, int score2) {
        Match match = thirdPlaceMatch.get(matchIndex);
        if (match.isFinalized()) {
            return;
        }
        match.setScore1(score1);
        match.setScore2(score2);
        match.setFinalized(true);
        match.getTeam1().addGoals(score1);
        match.getTeam2().addGoals(score2);
        if (score1 > score2) {
            match.getTeam1().addPoints(3);
        } else if (score1 < score2) {
            match.getTeam2().addPoints(3);
        } else {
            match.getTeam1().addPoints(1);
            match.getTeam2().addPoints(1);
        }
    }

    public void updateFinalMatchScore(int matchIndex, int score1, int score2) {
        Match match = finalMatch.get(matchIndex);
        if (match.isFinalized()) {
            return;
        }
        match.setScore1(score1);
        match.setScore2(score2);
        match.setFinalized(true);
        match.getTeam1().addGoals(score1);
        match.getTeam2().addGoals(score2);
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
        allTeams.sort(Comparator.comparingInt(Team::getPoints).thenComparingInt(Team::getGoals).reversed());
        return allTeams;
    }
}
