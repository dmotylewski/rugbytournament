package com.example.tournament.controller;

import com.example.tournament.model.Match;
import com.example.tournament.model.Team;
import com.example.tournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/")
    public String index(Model model, @RequestParam(name = "error", required = false) String error) {
        model.addAttribute("teams", tournamentService.getTeams());
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "index";
    }

    @PostMapping("/addTeam")
    public String addTeam(@RequestParam String teamName) {
        if (!tournamentService.addTeam(teamName)) {
            return "redirect:/?error=Nazwa drużyny musi być unikalna";
        }
        return "redirect:/";
    }

    @PostMapping("/generateGroups")
    public String generateGroups(@RequestParam int numberOfGroups, Model model) {
        tournamentService.generateGroups(numberOfGroups);
        model.addAttribute("groups", tournamentService.getGroups());
        return "groups";
    }

    @PostMapping("/generateSchedule")
    public String generateSchedule(Model model) {
        tournamentService.generateSchedule();
        model.addAttribute("schedule", tournamentService.getSchedule());
        model.addAttribute("teams", tournamentService.getUniqueTeamsFromSchedule());
        return "schedule";
    }

    @PostMapping("/generateFinals")
    public String generateFinals(Model model) {
        tournamentService.generateFinals();
        model.addAttribute("finals", tournamentService.getFinals());
        model.addAttribute("teams", tournamentService.getUniqueTeamsFromFinals());
        return "finals";
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {
        model.addAttribute("schedule", tournamentService.getSchedule());
        model.addAttribute("teams", tournamentService.getUniqueTeamsFromSchedule());
        return "schedule";
    }

    @GetMapping("/finals")
    public String finals(Model model) {
        model.addAttribute("finals", tournamentService.getFinals());
        model.addAttribute("teams", tournamentService.getUniqueTeamsFromFinals());
        return "finals";
    }

    @PostMapping("/updateScore")
    public String updateScore(@RequestParam int matchIndex, @RequestParam int score1, @RequestParam int score2) {
        tournamentService.updateScore(matchIndex, score1, score2);
        return "redirect:/schedule";
    }

    @PostMapping("/updateFinalScore")
    public String updateFinalScore(@RequestParam int matchIndex, @RequestParam int score1, @RequestParam int score2) {
        tournamentService.updateFinalScore(matchIndex, score1, score2);
        return "redirect:/finals";
    }

    @GetMapping("/results")
    public String results(Model model) {
        model.addAttribute("results", tournamentService.getFinalResults());
        return "results";
    }
}
