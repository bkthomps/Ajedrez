package backend;

public final class Position {
    final int row;
    final int column;

    private Position() {
        throw new IllegalStateException("Disable default constructor");
    }

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
