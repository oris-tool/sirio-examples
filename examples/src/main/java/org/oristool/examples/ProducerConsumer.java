package org.oristool.examples;

import java.math.BigDecimal;

import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.TransientSolutionViewer;
import org.oristool.models.stpn.trans.RegTransient;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class ProducerConsumer {
    public static void main(String[] args) {
        PetriNet pn = new PetriNet();

        // first produce-consume loop
        Place producing1 = pn.addPlace("producing1");
        Transition produce1 = pn.addTransition("produce1");
        Place buffer1 = pn.addPlace("buffer1");
        Transition consume1 = pn.addTransition("consume1");

        pn.addPrecondition(producing1, produce1);
        pn.addPostcondition(produce1, buffer1);
        pn.addPrecondition(buffer1, consume1);
        pn.addPostcondition(consume1, producing1);

        // second produce-consume loop
        Place producing2 = pn.addPlace("producing2");
        Transition produce2 = pn.addTransition("produce2");
        Place buffer2 = pn.addPlace("buffer2");
        Transition consume2 = pn.addTransition("consume2");

        pn.addPrecondition(producing2, produce2);
        pn.addPostcondition(produce2, buffer2);
        pn.addPrecondition(buffer2, consume2);
        pn.addPostcondition(consume2, producing2);

        // consume1 has priority over consume2
        pn.addInhibitorArc(buffer1, consume2);

        // durations are all uniform over [1,2]
        produce1.addFeature(StochasticTransitionFeature.newUniformInstance("1", "2"));
        produce2.addFeature(StochasticTransitionFeature.newUniformInstance("1", "2"));
        consume1.addFeature(StochasticTransitionFeature.newUniformInstance("1", "2"));
        consume2.addFeature(StochasticTransitionFeature.newUniformInstance("1", "2"));

        // initial state
        Marking m = new Marking();
        m.addTokens(producing1, 1);
        m.addTokens(producing2, 1);

        // transient until time=12, error 0.005 (per epoch), integration step=0.02
        RegTransient analysis = RegTransient.builder()
                .greedyPolicy(new BigDecimal("12"), new BigDecimal("0.005"))
                .timeStep(new BigDecimal("0.02")).build();

        TransientSolution<DeterministicEnablingState, Marking> solution = 
                analysis.compute(pn, m);

        // display transient probabilities
        new TransientSolutionViewer(solution);
    }
}
