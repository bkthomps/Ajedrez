package backend;

public enum Color {
    WHITE {
        @Override
        Color next() {
            return BLACK;
        }

        @Override
        Color previous() {
            return BLACK;
        }

        @Override
        String code() {
            return "b";
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
    },
    BLACK {
        @Override
        Color next() {
            return WHITE;
        }

        @Override
        Color previous() {
            return WHITE;
        }

        @Override
        String code() {
            return "w";
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
    };

    abstract Color next();

    abstract Color previous();

    abstract String code();

    abstract int pawnMove();

    abstract int piecesRow();

    abstract int pawnsRow();

    abstract int endRow();
}
