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
    };

    public abstract Color next();

    public abstract Color previous();
}
