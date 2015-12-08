package pl.edu.agh.messages;

import pl.edu.agh.model.Street;

import java.util.Map;

public class GenerationPossibleMessage {
    public final Map<Street, Boolean> generationPossible;

    public GenerationPossibleMessage(Map<Street, Boolean> generationPossible) {
        this.generationPossible = generationPossible;
    }
}
