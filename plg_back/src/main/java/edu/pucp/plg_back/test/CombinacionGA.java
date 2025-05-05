package edu.pucp.plg_back.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CombinacionGA {
    int popSize;
    int nGenerations;
    double crossoverProb;
    double mutationProb;
}
