package Backend;

import Backend.stage.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WorldCupSimulator {

    private List<Team> teams;
    private QualifyingStage qualifiers;
    private GroupStage roundRobin;
    private KnockoutStage brackets;
    private static DataLoader dataLoader;

    public WorldCupSimulator(){
        dataLoader = new DataLoader();
        this.startProgram();
        qualifiers = new QualifyingStage(teams);
        roundRobin = null;
        brackets = null;
    }
    private void startProgram() {
        teams = dataLoader.loadTeamData();
    }

    public List<Team> getTeams() {
        return teams;
    }
    public List<Match> stageMatches(int stage){ //1 = qualifier, 2 = groups, 3 = knockout
        switch (stage){
            case 1:
                qualifiers.arrangeMatches();
                return qualifiers.getMatches();
            case 2:
                teams = qualifiers.qualifiedTeams();
                roundRobin = new GroupStage(teams);
                roundRobin.arrangeMatches();
                roundRobin.calculateMatchResults();
                return roundRobin.getMatches();
            case 3:
                teams = roundRobin.qualifiedTeams();
                brackets = new KnockoutStage(teams);
                brackets.arrangeMatches();
                brackets.calculateMatchResults();
                return brackets.getMatches();
        }
        return null;
    }

    public static DataLoader getDataLoader() {
        return dataLoader;
    }
}