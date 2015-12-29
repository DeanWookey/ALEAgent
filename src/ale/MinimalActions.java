package ale;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the minimal action sets of 49 ALE supported Atari 2600 games.
 * Several have a minimum number of supported actions, though most games may use
 * all 18 available actions in some way, such as duplicating another action.
 * This can significantly reduce the dimensionality of the reinforcement
 * learning problem for such games.
 *
 * DeepMind, although not stated, seems to request the minimal action set of
 * games through their ALE wrapper, Torch.
 *
 * @author Craig Bester
 */
public class MinimalActions {

    public static final Map<String, String[]> minimal_actions;

    public static final String[] full_action_set = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] alien = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] amidar = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",};

    public static final String[] assault = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_rightfire",
        "player_a_leftfire",};

    public static final String[] asterix = {"player_a_noop",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",};

    public static final String[] asteroids = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",};

    public static final String[] atlantis = {"player_a_noop",
        "player_a_fire",
        "player_a_rightfire",
        "player_a_leftfire",};

    public static final String[] bank_heist = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] battle_zone = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] beam_rider = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_upright",
        "player_a_upleft",
        "player_a_rightfire",
        "player_a_leftfire",};

    public static final String[] bowling = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_down",
        "player_a_upfire",
        "player_a_downfire",};

    public static final String[] boxing = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] breakout = {"player_a_noop",
        "player_a_fire",
        "player_a_right",
        "player_a_left",};

    public static final String[] centipede = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] chopper_command = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] crazy_climber = {"player_a_noop",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",};

    public static final String[] demon_attack = {"player_a_noop",
        "player_a_fire",
        "player_a_right",
        "player_a_left",
        "player_a_rightfire",
        "player_a_leftfire",};

    public static final String[] double_dunk = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] enduro = {"player_a_noop",
        "player_a_fire",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_downright",
        "player_a_downleft",
        "player_a_rightfire",
        "player_a_leftfire",};

    public static final String[] fishing_derby = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] freeway = {"player_a_noop",
        "player_a_up",
        "player_a_down",};

    public static final String[] frostbite = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] gopher = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",};

    public static final String[] gravitar = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] hero = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] ice_hockey = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] jamesbond = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] kangaroo = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] krull = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] kung_fu_master = {"player_a_noop",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_downright",
        "player_a_downleft",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] montezuma_revenge = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] ms_pacman = {"player_a_noop",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",};

    public static final String[] name_this_game = {"player_a_noop",
        "player_a_fire",
        "player_a_right",
        "player_a_left",
        "player_a_rightfire",
        "player_a_leftfire",};

    public static final String[] pong = {"player_a_noop",
        "player_a_right",
        "player_a_left",};

    public static final String[] private_eye = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] qbert = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",};

    public static final String[] riverraid = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] road_runner = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] robotank = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] seaquest = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] space_invaders = {"player_a_noop",
        "player_a_left",
        "player_a_right",
        "player_a_fire",
        "player_a_leftfire",
        "player_a_rightfire",};

    public static final String[] star_gunner = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] tennis = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] time_pilot = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",};

    public static final String[] tutankham = {"player_a_noop",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",};

    public static final String[] up_n_down = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_down",
        "player_a_upfire",
        "player_a_downfire",};

    public static final String[] venture = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    public static final String[] video_pinball = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",};

    public static final String[] wizard_of_wor = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",};

    public static final String[] zaxxon = {"player_a_noop",
        "player_a_fire",
        "player_a_up",
        "player_a_right",
        "player_a_left",
        "player_a_down",
        "player_a_upright",
        "player_a_upleft",
        "player_a_downright",
        "player_a_downleft",
        "player_a_upfire",
        "player_a_rightfire",
        "player_a_leftfire",
        "player_a_downfire",
        "player_a_uprightfire",
        "player_a_upleftfire",
        "player_a_downrightfire",
        "player_a_downleftfire",};

    static {
        minimal_actions = new HashMap<>();
        minimal_actions.put("default", full_action_set);
        minimal_actions.put("alien", alien);
        minimal_actions.put("amidar", amidar);
        minimal_actions.put("assault", assault);
        minimal_actions.put("asterix", asterix);
        minimal_actions.put("asteroids", asteroids);
        minimal_actions.put("atlantis", atlantis);
        minimal_actions.put("bank_heist", bank_heist);
        minimal_actions.put("battle_zone", battle_zone);
        minimal_actions.put("beam_rider", beam_rider);
        minimal_actions.put("bowling", bowling);
        minimal_actions.put("boxing", boxing);
        minimal_actions.put("breakout", breakout);
        minimal_actions.put("centipede", centipede);
        minimal_actions.put("chopper_command", chopper_command);
        minimal_actions.put("crazy_climber", crazy_climber);
        minimal_actions.put("demon_attack", demon_attack);
        minimal_actions.put("double_dunk", double_dunk);
        minimal_actions.put("enduro", enduro);
        minimal_actions.put("fishing_derby", fishing_derby);
        minimal_actions.put("freeway", freeway);
        minimal_actions.put("frostbite", frostbite);
        minimal_actions.put("gopher", gopher);
        minimal_actions.put("gravitar", gravitar);
        minimal_actions.put("hero", hero);
        minimal_actions.put("ice_hockey", ice_hockey);
        minimal_actions.put("jamesbond", jamesbond);
        minimal_actions.put("kangaroo", kangaroo);
        minimal_actions.put("krull", krull);
        minimal_actions.put("kung_fu_master", kung_fu_master);
        minimal_actions.put("montezuma_revenge", montezuma_revenge);
        minimal_actions.put("ms_pacman", ms_pacman);
        minimal_actions.put("name_this_game", name_this_game);
        minimal_actions.put("pong", pong);
        minimal_actions.put("private_eye", private_eye);
        minimal_actions.put("qbert", qbert);
        minimal_actions.put("riverraid", riverraid);
        minimal_actions.put("road_runner", road_runner);
        minimal_actions.put("robotank", robotank);
        minimal_actions.put("seaquest", seaquest);
        minimal_actions.put("space_invaders", space_invaders);
        minimal_actions.put("star_gunner", star_gunner);
        minimal_actions.put("tennis", tennis);
        minimal_actions.put("time_pilot", time_pilot);
        minimal_actions.put("tutankham", tutankham);
        minimal_actions.put("up_n_down", up_n_down);
        minimal_actions.put("venture", venture);
        minimal_actions.put("video_pinball", video_pinball);
        minimal_actions.put("wizard_of_wor", wizard_of_wor);
        minimal_actions.put("zaxxon", zaxxon);
    }

    /**
     * Returns the action set of the specified game, otherwise returns the full
     * 18 action set.
     *
     * @param game The ALE supported game name
     * @return
     */
    public static String[] get(String game) {
        if (game!=null && minimal_actions.containsKey(game)) {
            return minimal_actions.get(game);
        }
        // deafult - full 18 action set
        return minimal_actions.get("default");
    }

}
