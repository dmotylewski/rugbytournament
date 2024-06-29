package com.example.tournament.model;

public class Team {
    private String name;
    private int points;
    private int goals;

    public Team(String name) {
        this.name = name;
        this.points = 0;
        this.goals = 0;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public int getGoals() {
        return goals;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void addGoals(int goals) {
        this.goals += goals;
    }
}
