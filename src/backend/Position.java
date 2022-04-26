package backend;

public final class Position {
    public final int row;
    public final int column;

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public String toString() {
        return (char) (column + 'a') + "" + (row + 1);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Position that
                && row == that.row
                && column == that.column;
    }

    @Override
    public int hashCode() {
        return row * Board.COLUMN_COUNT + column;
    }
}
