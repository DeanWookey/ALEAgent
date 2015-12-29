package ale;

import java.util.HashMap;
import java.util.Map;
import ale.io.Actions;

/**
 *
 * @author Craig Bester
 */
public class ActionSet {
    public int numActions;
    public String[] actionNames;
    public Map<String,Integer> actionMap;
    
    public ActionSet() {
        //use default set of 18 actions
        actionNames = MinimalActions.get("default");
        numActions = actionNames.length;
        actionMap = new HashMap<>();
        for(String s : actionNames) {
            actionMap.put(s,Actions.map(s));
        }
    }
    
    public ActionSet(String game) {
        actionNames = MinimalActions.get(game);
        numActions = actionNames.length;
        actionMap = new HashMap<>();
        for(String s : actionNames) {
            actionMap.put(s,Actions.map(s));
        }
    }
    
    /** Returns the ALE mapped action index of the given local action index
     * 
     * @param i
     * @return 
     */
    public int get(int i) {
        return actionMap.get(actionNames[i]);
    }

}
