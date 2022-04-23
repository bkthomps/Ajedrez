package frontend;

import backend.Color;

final class PlayerData {
    final Players count;
    final Color color;

    PlayerData(Players count, Color color) {
        this.count = count;
        this.color = (count == Players.ONE_PLAYER) ? color : Color.WHITE;
    }
}
