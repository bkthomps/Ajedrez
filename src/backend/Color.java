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
    };

    abstract Color next();

    abstract Color previous();

    abstract String code();
}
