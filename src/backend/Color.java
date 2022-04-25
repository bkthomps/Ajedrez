package backend;

public enum Color {
    WHITE {
        @Override
        public Color next() {
            return BLACK;
        }

        @Override
        public Color previous() {
            return BLACK;
        }

        @Override
        int pawnMove() {
            // White pawns advance by decreasing the row value
            return -1;
        }

        @Override
        int piecesRow() {
            return Board.ROW_COUNT - 1;
        }

        @Override
        int pawnsRow() {
            return piecesRow() - 1;
        }

        @Override
        int endRow() {
            return 0;
        }

        @Override
        public int bitIndex() {
            return 0;
        }
    },
    BLACK {
        @Override
        public Color next() {
            return WHITE;
        }

        @Override
        public Color previous() {
            return WHITE;
        }

        @Override
        int pawnMove() {
            // Black pawns advance by increasing the row value
            return 1;
        }

        @Override
        int piecesRow() {
            return 0;
        }

        @Override
        int pawnsRow() {
            return piecesRow() + 1;
        }

        @Override
        int endRow() {
            return Board.ROW_COUNT - 1;
        }

        @Override
        public int bitIndex() {
            return 1;
        }
    };

    public abstract Color next();

    public abstract Color previous();

    abstract int pawnMove();

    abstract int piecesRow();

    abstract int pawnsRow();

    abstract int endRow();

    public abstract int bitIndex();
}
