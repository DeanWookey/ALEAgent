package rl.memory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import rl.domain.State;

/**
 *
 * @author Craig Bester
 */
public class Memory {
    //LinkedList<Sample> history;
    ArrayList<Sample> history; //should be fine
    
    
    int replacementIndex;
    int maxLength; // maximum history length
    Random random;
    
    public Memory(int maxLength) {
        this.maxLength = maxLength;
        random = new Random();
        history = new ArrayList<>(maxLength);
        replacementIndex = 0;
    }
    
    public void addSample(State state, int action, double reward, State nextState) {
        addSample(new Sample(state,action,reward,nextState));
    }
    
    public void addSample(Sample s) {
        if(history.size() >= maxLength) {
            //history.removeFirst();
            history.set(replacementIndex, s);
            replacementIndex++;
        }
        history.add(s);
    }
    
    public Sample getRandomSample() {
        return (history.get(random.nextInt(history.size())));
    }
    
    public ArrayList<Sample> getRandomBatch(int size) {
        if(size > history.size()) {
            //System.err.println("");
            return history;
        }
        if(size == history.size())  {
            return history;
        }
        ArrayList<Sample> batch = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            batch.add(history.get(random.nextInt(history.size())));
        }
        return batch;
    }
}
