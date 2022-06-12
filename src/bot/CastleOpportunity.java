package bot;

import backend.Color;
import backend.Game;

final class CastleOpportunity {
    private static final int CASTLE_OPPORTUNITY_VALUE = 25;

    static int evaluate(Game game) {
        return CASTLE_OPPORTUNITY_VALUE * game.castleOpportunities(Color.WHITE)
                - CASTLE_OPPORTUNITY_VALUE * game.castleOpportunities(Color.BLACK);
    }
}
