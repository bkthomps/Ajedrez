package frontend;

public class BoardController {
    private PlayerData player;

    void setPlayerData(PlayerData player) {
        this.player = player;
        System.out.println(this.player.count);
        System.out.println(this.player.color);
    }
}
